package dev.jb0s.blockgameenhanced.gamefeature.statprofiles.allocate;

import dev.jb0s.blockgameenhanced.BlockgameEnhanced;
import dev.jb0s.blockgameenhanced.event.chat.ReceiveChatMessageEvent;
import dev.jb0s.blockgameenhanced.gamefeature.statprofiles.StatScreenManager;
import dev.jb0s.blockgameenhanced.gamefeature.statprofiles.attribute.PlayerAttribute;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;

public class StatAllocator {
  private final StatScreenManager screenManager;

  /**
   * The slot that holds the item to clear the stats. This is important to keep track of when we need to reset
   * the stats.
   */
  @Setter
  private int reallocationSlot = -1;

  @Getter
  @Nullable
  private Map<String, PlayerAttribute> attributes;

  private boolean preview;

  @Nullable
  private Iterator<Command> commandIterator;

  public StatAllocator(StatScreenManager screenManager) {
    this.screenManager = screenManager;

    this.attributes = null;
    this.commandIterator = null;
    this.preview = false;

    ReceiveChatMessageEvent.EVENT.register(this::handleReceiveChatMessage);
  }

  public void onScreenRecreate() {
    this.attributes = null;
  }

  public void nextStep() {
    if (this.commandIterator == null || !this.commandIterator.hasNext()) {
      this.stop();
      return;
    }

    // Wait for sync ID and inventory
    if (this.missingRequirements()) {
      return;
    }

    Command command = this.commandIterator.next();
    if (command.isReset()) {
      this.resetStats();
    } else if (command.isIncrement()) {
      PlayerAttribute attribute = this.attributes.get(command.attrName());
      if (attribute == null) {
        BlockgameEnhanced.LOGGER.warn("Attribute not found: {}", command.attrName());
        this.nextStep();
      } else {
        this.incrementStat(attribute);
      }
    } else {
      BlockgameEnhanced.LOGGER.warn("Unknown command: {}", command);
      this.nextStep();
    }
  }

  /**
   * Reset the stats by clicking the reallocation item slot.
   */
  public void resetStats() {
    StatScreenManager.State state = this.screenManager.getState();
    if (state != StatScreenManager.State.IDLE && state != StatScreenManager.State.CAPACITY_EXCEEDED) {
      BlockgameEnhanced.LOGGER.warn("Cannot reset stats while in state: {}", state);
      return;
    }

    if (this.screenManager.waitingForScreen()) {
      BlockgameEnhanced.LOGGER.warn("Waiting for screen. Cannot reset stats.");
      this.stop(StatScreenManager.State.FAILED);
      return;
    }

    if (this.reallocationSlot == -1) {
      BlockgameEnhanced.LOGGER.warn("Reallocation slot is not set. Cannot reset stats.");
      this.stop(StatScreenManager.State.FAILED);
      return;
    }

    if (this.preview) {
      BlockgameEnhanced.LOGGER.warn("Preview mode is enabled. Skipping reset");
      // TODO
      return;
    }

    ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;
    if (interactionManager == null) {
      BlockgameEnhanced.LOGGER.warn("Interaction manager is not available. Cannot reset stats.");
      this.stop(StatScreenManager.State.FAILED);
      return;
    }

    this.screenManager.changeState(StatScreenManager.State.WAITING_FOR_RESET);
    this.screenManager.invalidateScreen();

    interactionManager.clickSlot(this.screenManager.getSyncId(), this.reallocationSlot, 0, SlotActionType.PICKUP, MinecraftClient.getInstance().player);
  }

  /**
   * Increment the stat by clicking the attribute slot.
   * @param attribute The attribute to increment
   */
  public void incrementStat(PlayerAttribute attribute) {
    if (this.screenManager.getState() != StatScreenManager.State.IDLE) {
      BlockgameEnhanced.LOGGER.warn("Cannot increment stat while in state: {}", this.screenManager.getState());
      return;
    }

    if (this.screenManager.waitingForScreen()) {
      BlockgameEnhanced.LOGGER.warn("Waiting for screen. Cannot increment stat.");
      this.stop(StatScreenManager.State.FAILED);
      return;
    }

    ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;
    if (interactionManager == null) {
      BlockgameEnhanced.LOGGER.warn("Interaction manager is not available. Cannot increment stat.");
      this.stop(StatScreenManager.State.FAILED);
      return;
    }

    this.screenManager.changeState(StatScreenManager.State.WAITING_FOR_ALLOCATION);
    this.screenManager.invalidateScreen();

    interactionManager.clickSlot(this.screenManager.getSyncId(), attribute.getSlot(), 0, SlotActionType.PICKUP, MinecraftClient.getInstance().player);
  }

  public void stop() {
    this.stop(StatScreenManager.State.IDLE);
  }

  private void stop(StatScreenManager.State state) {
    this.commandIterator = null;
    this.screenManager.changeState(state);
  }

  public void setAttributes(Map<String, PlayerAttribute> attributes) {
    this.attributes = attributes;
    // FIXME: Notify change?

    if (this.screenManager.getState() == StatScreenManager.State.WAITING_FOR_INVENTORY) {
      this.screenManager.changeState(StatScreenManager.State.IDLE);
      this.nextStep();
    }
  }

  public boolean isAllocating() {
    return this.commandIterator != null;
  }

  private boolean missingRequirements() {
    return !this.checkSyncId() || !this.checkInventory();
  }

  private boolean checkSyncId() {
    if (this.screenManager.isDirtySyncId() || this.screenManager.getSyncId() == -1) {
      this.screenManager.changeState(StatScreenManager.State.WAITING_FOR_SYNC_ID);
      return false;
    }

    return true;
  }

  private boolean checkInventory() {
    if (this.reallocationSlot == -1 || this.attributes == null) {
      this.screenManager.changeState(StatScreenManager.State.WAITING_FOR_INVENTORY);
      return false;
    }

    return true;
  }

  private ActionResult handleReceiveChatMessage(MinecraftClient client, String message) {
    StatScreenManager.State state = this.screenManager.getState();

    if (state == StatScreenManager.State.WAITING_FOR_RESET) {
      if (message.contains("You successfully reset your attributes")) {
        this.nextStep();
        return ActionResult.CONSUME;
      }
    }

    if (state == StatScreenManager.State.WAITING_FOR_ALLOCATION) {
      if (message.contains("You successfully leveled up your")) {
        this.nextStep();
        return ActionResult.CONSUME;
      }

      if (message.contains("You do not have 1 attribute point")) {
        this.stop(StatScreenManager.State.CAPACITY_EXCEEDED);

        // We need to mark the sync ID as not dirty because the screen does not get recreated
        // when the capacity is exceeded.
        this.screenManager.setDirtySyncId(false);
        return ActionResult.CONSUME;
      }
    }

    return ActionResult.PASS;
  }
}
