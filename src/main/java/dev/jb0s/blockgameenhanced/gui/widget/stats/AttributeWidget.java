package dev.jb0s.blockgameenhanced.gui.widget.stats;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.jb0s.blockgameenhanced.gamefeature.statprofiles.attribute.PlayerAttribute;
import dev.jb0s.blockgameenhanced.helper.GUIHelper;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class AttributeWidget extends ClickableWidget {
  private static final Identifier BAR_TEXTURE = GUIHelper.sprite("textures/gui/sprites/widgets/attribute/talentbars.png");

  private final StatListWidget parent;
  private final PlayerAttribute attribute;

  @Setter
  @Nullable
  private BiConsumer<PlayerAttribute, Boolean> onHover;

  public AttributeWidget(StatListWidget parent, PlayerAttribute attribute, int x, int y, int width, int height) {
    super(x, y, width, height, Text.empty());

    this.parent = parent;
    this.attribute = attribute;

    if (this.parent.getScreen().getScreenManager().getStatAllocator().isPreview()) {
      this.attribute.fixLoreText();
    }
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    if (!this.visible) {
      return;
    }

    this.hovered = this.withinBounds(mouseX, mouseY);
    this.renderButton(context, mouseX, mouseY, delta);
  }

  @Override
  protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
    // Render item
    context.drawItem(this.attribute.getItemStack(), this.getX(), this.getY());

    // Render item name
    MutableText nameText = Text.literal(this.attribute.getName());
    context.drawText(MinecraftClient.getInstance().textRenderer, nameText, this.getX() + 20, this.getY(),
        this.isHovered() ? 0xFFFFFF : 0xDDDDDD, false);

    // Render progress bar
    RenderSystem.enableBlend();
    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    RenderSystem.setShader(GameRenderer::getPositionTexProgram);
    RenderSystem.setShaderTexture(0, BAR_TEXTURE);

    // Draw bar
    int barX = this.getX() + 20;
    int barY = this.getY() + 10;
    int barWidth = 64;
    int barHeight = 5;

    int filledWidth = (int) ((this.attribute.getSpent() / (double) this.attribute.getMax()) * barWidth);

    context.drawTexture(BAR_TEXTURE, barX, barY, 0, 0, barWidth, barHeight, 64, 15);

    int v = 5; // Blue bar
    if (this.parent.getScreen().getScreenManager().getStatAllocator().isPreview()) {
      v = 10; // Yellow bar
    }
    context.drawTexture(BAR_TEXTURE, barX, barY, 0, v, filledWidth, barHeight, 64, 15);

    RenderSystem.disableBlend();

    if (this.onHover != null) {
      this.onHover.accept(this.attribute, this.isHovered());
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (!this.visible || !this.withinBounds(mouseX, mouseY)) {
      return false;
    }

    if (button == 0) {
      this.parent.getScreen().getScreenManager().getStatAllocator().incrementStat(this.attribute);
    } else if (button == 1) {
      this.parent.getScreen().getScreenManager().getStatAllocator().decrementStat(this.attribute);
    }
    return true;
  }

  @Override
  protected void appendClickableNarrations(NarrationMessageBuilder builder) {

  }

  private boolean withinBounds(double mouseX, double mouseY) {
    int yPos = (int) (this.getY() - this.parent.getScrollY());
    if (yPos < this.parent.getY() || yPos > this.parent.getY() + this.parent.getContentsHeight()) {
      return false;
    }

    return mouseX >= this.getX() && mouseY >= yPos && mouseX < this.getX() + this.width && mouseY < yPos + this.height;
  }
}
