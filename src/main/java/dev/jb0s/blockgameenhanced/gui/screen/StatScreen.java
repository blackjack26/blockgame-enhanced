package dev.jb0s.blockgameenhanced.gui.screen;

import dev.jb0s.blockgameenhanced.gamefeature.statprofiles.StatScreenManager;
import dev.jb0s.blockgameenhanced.gamefeature.statprofiles.attribute.PlayerAttribute;
import dev.jb0s.blockgameenhanced.gui.widget.stats.ModifierListWidget;
import dev.jb0s.blockgameenhanced.gui.widget.stats.StatListWidget;
import dev.jb0s.blockgameenhanced.helper.GUIHelper;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class StatScreen extends Screen {
  private static final Identifier BACKGROUND_SPRITE = GUIHelper.sprite("background");
  private static final ItemStack BOOK_ITEM = new ItemStack(Items.WRITABLE_BOOK);

  private static final int BUTTON_SIZE = 14;
  private static final int MENU_WIDTH = 224;
  private static final int MENU_HEIGHT = 224;
  private static final int TITLE_LEFT = 8;
  private static final int TITLE_TOP = 8;

  @Getter
  private final StatScreenManager screenManager;

  private int left = 0;
  private int top = 0;

  @Nullable
  private PlayerAttribute hoveredAttribute;

  private StatListWidget statListWidget;
  private ModifierListWidget modifierListWidget;

  private TexturedButtonWidget reallocateButton;
  private TexturedButtonWidget previewButton;
  private TexturedButtonWidget cancelPreviewButton;
  private TexturedButtonWidget changeProfileButton;

  public StatScreen(Text title, StatScreenManager screenManager) {
    super(title);

    this.screenManager = screenManager;
    this.hoveredAttribute = null;

    if (!this.screenManager.getStatAllocator().isAllocating()) {
      // This allows any errors to be cleared when the screen is opened again
      this.screenManager.changeState(StatScreenManager.State.IDLE);
    }
  }

  @Override
  protected void init() {
    this.left = (this.width - MENU_WIDTH) / 2;
    this.top = (this.height - MENU_HEIGHT) / 2;

    super.init();

    var currentAttrs = this.screenManager.getStatAllocator().getAttributes();

    // List widget
    this.statListWidget = new StatListWidget(this, this.left + 8, this.top + 40, MENU_WIDTH - 20, MENU_HEIGHT - 48);
    this.statListWidget.visible = false;
    this.statListWidget.setOnAttributeHover((attribute, hovered) -> {
      if (hovered) {
        this.hoveredAttribute = attribute;
      } else if (this.hoveredAttribute == attribute) {
        this.hoveredAttribute = null;
      }
    });
    this.addDrawableChild(this.statListWidget);
    if (currentAttrs != null && !currentAttrs.isEmpty()) {
      this.statListWidget.build();
    }

    // Reallocation button
    this.reallocateButton = GUIHelper.button(
        this.left + MENU_WIDTH - (3 + BUTTON_SIZE),
        this.top + 5,
        "widgets/reallocate",
        "menu.blockgame.stats.reallocate",
        (button) -> {
          this.screenManager.getStatAllocator().resetStats();
        });
    this.reallocateButton.visible = this.screenManager.getSyncId() != -1;
    this.addDrawableChild(this.reallocateButton);

    // Preview Button
    this.previewButton = GUIHelper.button(
        this.left + MENU_WIDTH - 2 * (3 + BUTTON_SIZE),
        this.top + 5,
        "widgets/preview_stats",
        "menu.blockgame.stats.preview",
        (button) -> {
          this.screenManager.getStatAllocator().setPreview(true);
          this.previewButton.visible = false;
          this.changeProfileButton.visible = false;
          this.cancelPreviewButton.visible = true;
        });
    this.previewButton.visible = !this.screenManager.getStatAllocator().isPreview();
    this.addDrawableChild(this.previewButton);

    // Cancel Preview Button
    this.cancelPreviewButton = GUIHelper.button(
        this.left + MENU_WIDTH - 2 * (3 + BUTTON_SIZE),
        this.top + 5,
        "widgets/remove",
        "menu.blockgame.stats.cancel_preview",
        (button) -> {
          this.screenManager.getStatAllocator().setPreview(false);
          this.previewButton.visible = true;
          this.changeProfileButton.visible = true;
          this.cancelPreviewButton.visible = false;
        });
    this.cancelPreviewButton.visible = this.screenManager.getStatAllocator().isPreview();
    this.addDrawableChild(this.cancelPreviewButton);

    // Change Profile button
    this.changeProfileButton = GUIHelper.button(
        this.left + MENU_WIDTH - 3 * (3 + BUTTON_SIZE),
        this.top + 5,
        "widgets/change_profile",
        "menu.blockgame.stats.change_profile",
        (button) -> {
          // TODO: Open profile selection
        });
    this.changeProfileButton.visible = !this.screenManager.getStatAllocator().isPreview();
    this.addDrawableChild(this.changeProfileButton);

    // Modifier list widget
    this.modifierListWidget = new ModifierListWidget(this, this.left + MENU_WIDTH + 4, this.top, 175, MENU_HEIGHT);
    this.modifierListWidget.visible = false;
    this.addDrawableChild(this.modifierListWidget);
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    super.render(context, mouseX, mouseY, delta);

    // Title
    context.drawText(this.textRenderer, this.title, this.left + TITLE_LEFT, this.top + TITLE_TOP, 0xFFFFFF, false);

    // Centered Book Item
    context.drawItem(BOOK_ITEM, this.left + (MENU_WIDTH - 16) / 2, this.top + 8);

    // Centered below the book item, "Available X/Y"
    int totalPoints = this.screenManager.getStatAllocator().getTotalPoints();
    MutableText text = Text.literal("Available: "); // TODO: translatable
    if (totalPoints == -1) {
      text.append(Text.literal("?").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
    } else {
      text.append(Text.literal("" + this.screenManager.getStatAllocator().getAvailablePoints())
              .formatted(this.screenManager.getStatAllocator().getAvailablePoints() > 0 ? Formatting.GREEN : Formatting.RED, Formatting.BOLD))
          .append(Text.literal("/" + totalPoints));
    }
    context.drawText(this.textRenderer, text, this.left + (MENU_WIDTH - this.textRenderer.getWidth(text)) / 2, this.top + 25, 0xFFFFFF, false);

    // Render tooltip
    this.renderTooltip(context, mouseX, mouseY);
  }

  private void renderTooltip(DrawContext context, int mouseX, int mouseY) {
    if (this.hoveredAttribute == null) {
      return;
    }

    // Render tooltip
    context.getMatrices().push();
    context.getMatrices().translate(0, 0, 200.0f);
    context.drawItemTooltip(this.textRenderer, this.hoveredAttribute.getItemStack(), mouseX, mouseY);
    context.getMatrices().pop();
  }

  @Override
  public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    super.renderBackground(context, mouseX, mouseY, delta);

    // Background
    context.drawGuiTexture(BACKGROUND_SPRITE, this.left, this.top, MENU_WIDTH, MENU_HEIGHT);
  }

  @Override
  public void close() {
    super.close();

    // Send a packet to the server saying we have closed the screen
    if (this.client != null && this.client.player != null) {
      this.client.player.closeHandledScreen();
    }

    this.screenManager.onScreenClose();
  }

  public void onReallocationSlotSet() {
    this.reallocateButton.visible = true;
  }

  public void onAttributesChanged() {
    this.statListWidget.build();
    this.modifierListWidget.build();
  }
}
