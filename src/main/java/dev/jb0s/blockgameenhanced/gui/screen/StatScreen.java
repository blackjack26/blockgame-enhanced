package dev.jb0s.blockgameenhanced.gui.screen;

import dev.jb0s.blockgameenhanced.BlockgameEnhanced;
import dev.jb0s.blockgameenhanced.gamefeature.statprofiles.StatScreenManager;
import dev.jb0s.blockgameenhanced.gamefeature.statprofiles.allocate.StatProfile;
import dev.jb0s.blockgameenhanced.gamefeature.statprofiles.attribute.PlayerAttribute;
import dev.jb0s.blockgameenhanced.gui.widget.stats.ModifierListWidget;
import dev.jb0s.blockgameenhanced.gui.widget.stats.StatGridWidget;
import dev.jb0s.blockgameenhanced.gui.widget.stats.StatListWidget;
import dev.jb0s.blockgameenhanced.helper.GUIHelper;
import dev.jb0s.blockgameenhanced.storage.BlockgameData;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class StatScreen extends Screen {
  private static final Identifier BACKGROUND_SPRITE = GUIHelper.sprite("background");
  private static final ItemStack BOOK_ITEM = new ItemStack(Items.WRITABLE_BOOK);

  private static final int BUTTON_SIZE = 14;

  private static final int MENU_WIDTH = 224;
  private static final int OLD_MENU_WIDTH = 176;
  private static final int MENU_HEIGHT = 224;
  private static final int OLD_MENU_HEIGHT = 154; // 5 * 18 + 48 + 16

  private static final int TITLE_LEFT = 8;
  private static final int TITLE_TOP = 8;

  @Getter
  private final StatScreenManager screenManager;

  private int left = 0;
  private int top = 0;

  @Nullable
  private PlayerAttribute hoveredAttribute;
  @Nullable
  private StatProfile currentProfile;

  private StatListWidget statListWidget;
  private StatGridWidget statGridWidget;
  private ModifierListWidget modifierListWidget;
  private TextFieldWidget profileNameField;

  private TexturedButtonWidget reallocateButton;
  private TexturedButtonWidget addProfileButton;
  private TexturedButtonWidget cancelPreviewButton;
  private TexturedButtonWidget saveProfileButton;
  private TexturedButtonWidget deleteProfileButton;
  private Map<String, TexturedButtonWidget> profileButtons;

  public StatScreen(Text title, StatScreenManager screenManager) {
    super(title);

    this.screenManager = screenManager;
    this.hoveredAttribute = null;
    this.currentProfile = null;

    if (!this.screenManager.getStatAllocator().isAllocating()) {
      // This allows any errors to be cleared when the screen is opened again
      this.screenManager.changeState(StatScreenManager.State.IDLE);
    }
  }

  @Override
  protected void init() {
    this.left = (this.width - getMenuWidth()) / 2;
    this.top = (this.height - getMenuHeight()) / 2;

    super.init();

    var currentAttrs = this.screenManager.getStatAllocator().getAttributes();

    // TODO: Determine current profile

    // List widget
    this.statListWidget = new StatListWidget(this, this.left + 8, this.top + 40, getMenuWidth() - 20, getMenuHeight() - 48);
    this.statListWidget.visible = false;
    this.statListWidget.setOnAttributeHover((attribute, hovered) -> {
      if (hovered) {
        this.hoveredAttribute = attribute;
      } else if (this.hoveredAttribute == attribute) {
        this.hoveredAttribute = null;
      }
    });

    if (!BlockgameEnhanced.getConfig().getStatConfig().useOldStatView) {
      this.addDrawableChild(this.statListWidget);
      if (currentAttrs != null && !currentAttrs.isEmpty()) {
        this.statListWidget.build();
      }
    }

    // Grid widget
    this.statGridWidget = new StatGridWidget(this, this.left + 7, this.top + 40);
    this.statGridWidget.visible = true;
    this.statGridWidget.setOnAttributeHover((attribute) -> this.hoveredAttribute = attribute);

    if (BlockgameEnhanced.getConfig().getStatConfig().useOldStatView) {
      this.addDrawableChild(this.statGridWidget);
    }

    // Reallocation button
    this.reallocateButton = GUIHelper.button(
        this.left + getMenuWidth() - (3 + BUTTON_SIZE),
        this.top + 5,
        "widgets/reallocate",
        "menu.blockgame.stats.reallocate",
        (button) -> {
          this.screenManager.getStatAllocator().resetStats();
        });
    this.reallocateButton.visible = this.screenManager.getSyncId() != -1;
    this.addDrawableChild(this.reallocateButton);

    // Add Profile Button
    this.addProfileButton = GUIHelper.button(
        this.left + 7,
        this.top + 5 + (3 + BUTTON_SIZE),
        "widgets/create",
        "menu.blockgame.stats.create_profile",
        (button) -> {
          this.currentProfile = null;
          this.openProfilePreview();
        }
    );
    this.addProfileButton.visible = !this.screenManager.getStatAllocator().isPreview();
    this.addDrawableChild(this.addProfileButton);

    // Profile Buttons
    this.profileButtons = new HashMap<>();
    if (BlockgameData.INSTANCE != null) {
      var profiles = new ArrayList<>(BlockgameData.INSTANCE.getStatProfiles().values());
      profiles.sort(Comparator.comparingInt(StatProfile::getOrder));
      for (int i = 0; i < profiles.size(); i++) {
        this.addProfileButton(profiles.get(i), i);
      }
    }

    // Profile name field
    this.profileNameField = this.addDrawableChild(new TextFieldWidget(
        this.textRenderer,
        this.left + 8,
        this.top + 6 + (3 + BUTTON_SIZE),
        getMenuWidth() - 16,
        12,
        this.profileNameField,
        Text.literal("")
    ));
    this.profileNameField.setPlaceholder(Text.literal("Profile Name"));
    this.profileNameField.setDrawsBackground(true);
    this.profileNameField.setEditableColor(0xFFFFFF);
    this.profileNameField.visible = this.screenManager.getStatAllocator().isPreview();

    // Cancel Preview Button
    this.cancelPreviewButton = GUIHelper.button(
        this.left + getMenuWidth() - 2 * (3 + BUTTON_SIZE),
        this.top + 5,
        "widgets/close",
        "menu.blockgame.stats.cancel_preview",
        (button) -> this.closeProfilePreview()
    );
    this.cancelPreviewButton.visible = this.screenManager.getStatAllocator().isPreview();
    this.addDrawableChild(this.cancelPreviewButton);

    // Save Profile button
    this.saveProfileButton = GUIHelper.button(
        this.left + getMenuWidth() - 3 * (3 + BUTTON_SIZE),
        this.top + 5,
        "widgets/save_profile",
        "menu.blockgame.stats.save_profile",
        (button) -> {
          int order;
          if (this.currentProfile != null) {
            BlockgameData.removeProfile(this.currentProfile.getName());
            order = this.currentProfile.getOrder();
          } else {
            // Get the next available order
            order = 0;
            for (StatProfile profile : BlockgameData.INSTANCE.getStatProfiles().values()) {
              if (profile.getOrder() >= order) {
                order = profile.getOrder() + 1;
              }
            }
          }

          StatProfile newProfile = new StatProfile(this.profileNameField.getText(), order);
          newProfile.fromAttributes(this.screenManager.getStatAllocator().getAttributes());
          BlockgameData.saveProfile(newProfile);

          if (this.currentProfile != null) {
            // Update existing profile button
            TexturedButtonWidget existingButton = this.profileButtons.get(this.currentProfile.getName());
            if (existingButton != null) {
              existingButton.setTooltip(Tooltip.of(Text.literal(newProfile.getName())));
            }
          } else {
            // Add new profile button
            this.addProfileButton(newProfile, this.profileButtons.size());
          }

          this.closeProfilePreview();
          this.adjustProfileButtons();

          // Reallocate stats to the new profile
          this.getScreenManager().getStatAllocator().allocate(newProfile);
        });
    this.saveProfileButton.visible = this.screenManager.getStatAllocator().isPreview();
    this.addDrawableChild(this.saveProfileButton);

    // Delete Profile button
    this.deleteProfileButton = GUIHelper.button(
        this.left + 7,
        this.top + getMenuHeight() - (3 + BUTTON_SIZE) - 3,
        "widgets/remove",
        "menu.blockgame.stats.delete_profile",
        (button) -> {
          if (this.currentProfile != null) {
            BlockgameData.removeProfile(this.currentProfile.getName());
            this.remove(this.profileButtons.remove(this.currentProfile.getName()));

            this.closeProfilePreview();
            this.adjustProfileButtons();
          }
        });
    this.deleteProfileButton.visible = this.screenManager.getStatAllocator().isPreview();
    this.addDrawableChild(this.deleteProfileButton);

    // Modifier list widget
    this.modifierListWidget = new ModifierListWidget(this, this.left + getMenuWidth() + 4, this.top, 175, getMenuHeight());
    this.modifierListWidget.visible = false;
    this.addDrawableChild(this.modifierListWidget);

    this.adjustProfileButtons();
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    super.render(context, mouseX, mouseY, delta);

    // Title
    MutableText titleText = Text.literal(this.title.getString());
    if (this.getScreenManager().getStatAllocator().isPreview()) {
      titleText.append(Text.literal(" (Preview)").formatted(Formatting.GRAY));
    }
    context.drawText(this.textRenderer, titleText, this.left + TITLE_LEFT, this.top + TITLE_TOP, 0xFFFFFF, false);

    // Centered Book Item
    if (!BlockgameEnhanced.getConfig().getStatConfig().useOldStatView) {
      context.drawItem(BOOK_ITEM, this.left + (getMenuWidth() - 16) / 2, this.top + 8);
    }

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

    int availableX = this.left + (getMenuWidth() - this.textRenderer.getWidth(text)) / 2;
    int availableY = this.top + 25;

    if (BlockgameEnhanced.getConfig().getStatConfig().useOldStatView) {
      // Place below the inventory grid
      availableY = this.top + 40 + (5 * 18) + 7;
    }

    context.drawText(this.textRenderer, text, availableX, availableY, 0xFFFFFF, false);

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

    if (this.screenManager.getStatAllocator().isPreview()) {
      // Background border
      context.fillGradient(this.left - 1, this.top - 1, this.left + getMenuWidth() + 2, this.top + getMenuHeight() + 2, 0x88_FBDB6C, 0x88_FBDB6C);
    }

    // Background
    context.drawGuiTexture(BACKGROUND_SPRITE, this.left, this.top, getMenuWidth(), getMenuHeight());
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

  public int getMenuWidth() {
    if (BlockgameEnhanced.getConfig().getStatConfig().useOldStatView) {
      return OLD_MENU_WIDTH;
    }
    return MENU_WIDTH;
  }

  public int getMenuHeight() {
    if (BlockgameEnhanced.getConfig().getStatConfig().useOldStatView) {
      return OLD_MENU_HEIGHT;
    }
    return MENU_HEIGHT;
  }

  private void openProfilePreview() {
    this.screenManager.getStatAllocator().setPreview(true, this.currentProfile);

    this.addProfileButton.visible = false;
    for (TexturedButtonWidget button : this.profileButtons.values()) {
      button.visible = false;
    }

    this.saveProfileButton.visible = true;
    this.deleteProfileButton.visible = true;
    this.cancelPreviewButton.visible = true;
    this.profileNameField.visible = true;

    if (this.currentProfile == null) {
      this.profileNameField.setText("");
    }
  }

  private void closeProfilePreview() {
    this.screenManager.getStatAllocator().setPreview(false);

    this.addProfileButton.visible = true;
    for (TexturedButtonWidget button : this.profileButtons.values()) {
      button.visible = true;
    }

    this.saveProfileButton.visible = false;
    this.deleteProfileButton.visible = false;
    this.cancelPreviewButton.visible = false;
    this.profileNameField.visible = false;
    this.currentProfile = null;
  }

  private void adjustProfileButtons() {
    if (BlockgameData.INSTANCE == null) {
      return;
    }

    var statProfiles = BlockgameData.INSTANCE.getStatProfiles();

    // Only show the add profile button if we are not previewing and there are less than 5 profiles
    this.addProfileButton.visible = !this.screenManager.getStatAllocator().isPreview() && statProfiles.size() < 5;
    this.addProfileButton.setX(
        this.left + 7 + (3 + BUTTON_SIZE) * statProfiles.size()
    );

    // Reorder the profile buttons
    var profiles = new ArrayList<>(statProfiles.values());
    profiles.sort(Comparator.comparingInt(StatProfile::getOrder));
    for (int i = 0; i < profiles.size(); i++) {
      TexturedButtonWidget button = this.profileButtons.get(profiles.get(i).getName());
      if (button != null) {
        button.setX(this.left + 7 + (3 + BUTTON_SIZE) * i);
      }
    }
  }

  private void addProfileButton(StatProfile profile, int index) {
    TexturedButtonWidget button = GUIHelper.button(
        this.left + 7 + (3 + BUTTON_SIZE) * index,
        this.top + 5 + (3 + BUTTON_SIZE),
        "widgets/change_profile",
        profile.getName(),
        (b) -> {
          this.currentProfile = profile;
          this.profileNameField.setText(profile.getName());
          this.openProfilePreview();
        });
    this.profileButtons.put(profile.getName(), button);
    this.addDrawableChild(button);
  }
}
