package dev.jb0s.blockgameenhanced.gui.screen.title;

import com.google.common.util.concurrent.Runnables;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.jb0s.blockgameenhanced.BlockgameEnhanced;
import dev.jb0s.blockgameenhanced.BlockgameEnhancedClient;
import dev.jb0s.blockgameenhanced.eggs.ThorScreen;
import dev.jb0s.blockgameenhanced.config.ConfigManager;
import lombok.SneakyThrows;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.client.network.*;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Constructor;

public class TitleScreen extends Screen {

    private static final Identifier BLOCKGAME_LOGO_TEXTURE = new Identifier("blockgame", "textures/gui/title/blockgame.png");
    private static final Identifier BACKGROUND_TEXTURE = new Identifier("blockgame", "textures/gui/title/titlescreen.png");
    private final MutableText BUTTON_PLAY = Text.translatable("menu.blockgame.title.play");
    private final MutableText BUTTON_WEBSITE = Text.translatable("menu.blockgame.title.website");
    private final MutableText BUTTON_WIKI = Text.translatable("menu.blockgame.title.wiki");
    private final MutableText WATERMARK = Text.translatable("menu.blockgame.title.watermark", FabricLoader.getInstance().getModContainer("blockgameenhanced").get().getMetadata().getVersion().getFriendlyString());
    private final MutableText SERVER_STATUS_ONLINE_EMPTY = Text.translatable("menu.blockgame.status.online.empty");
    private final MutableText SERVER_STATUS_ONLINE_NOTEMPTY = Text.translatable("menu.blockgame.status.online");
    private final MutableText SERVER_STATUS_OFFLINE = Text.translatable("menu.blockgame.status.offline");

    private MultiplayerServerListPinger pinger;
    private FakePlayer fakePlayer;

    private ServerInfo serverInfo;
    private int eggClicks;

    public TitleScreen() {
        super(Text.of("Title Screen"));
    }

    @Override
    @SneakyThrows
    public void init() {

        // If we load this screen and the user doesn't have custom title screens enabled, (this usually happens when the user has just changed this setting)
        // then we want to abandon this title screen and load the vanilla one. This system is kinda hacky but meh.
        if(!BlockgameEnhanced.getConfig().getAccessibilityConfig().enableCustomTitleScreen) {
            client.setScreen(new net.minecraft.client.gui.screen.TitleScreen());
            return;
        }

        ConfigManager configManager = BlockgameEnhancedClient.getConfigManager();

        // Initialize server pinger & server info
        pinger = new MultiplayerServerListPinger();
        serverInfo = new ServerInfo("Blockgame", "mc.blockgame.info", ServerInfo.ServerType.OTHER);

        // Start pinging server
        Thread pingThread = new Thread(() -> {
            try {
                pinger.add(serverInfo, () -> serverInfo.online = true);
            }
            catch (Exception e) {
                BlockgameEnhanced.LOGGER.error("Failed to fetch server status: " + e.getMessage());
                serverInfo.online = false;
                serverInfo.ping = -1L;
                serverInfo.playerListSummary = null;
            }
        });
        pingThread.start();

        // Initialize player
        //fakePlayer = new FakePlayer();
        //fakePlayer.getInventory().readNbt(configManager.getInventorySnapshot());

        // Initialize ui
        initButtons();
        initCopyright();
        initWatermark();
        if(BlockgameEnhanced.DEBUG) initDebug();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        renderPlayer(mouseX, mouseY);
        renderLogo(context, width, 1, 30);

        super.render(context, mouseX, mouseY, delta);

        // Render this after super, because super renders the buttons
        renderServerStatus(context);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        int sw = width;
        int sh = height;
        if(sh >= sw) {
            sw = (int)(sh * 1.77f);
        }
        if(sw >= sh) {
            sh = (int)(sw * 0.56f);
        }
        if(sh < height) {
            int missing = height - sh;
            sh += missing;
            sw += missing * 1.77f;
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        context.drawTexture(BACKGROUND_TEXTURE, 0, 0, -500, 0, 0, sw, sh, sw, sh);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        // Refresh bind
        if (keyCode == GLFW.GLFW_KEY_F5) {
            client.setScreen(new TitleScreen());
            return true;
        }

        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    /**
     * Renders the logo on the main menu.
     * @param context Current DrawContext.
     * @param screenWidth The width of the screen.
     * @param alpha The alpha to render the logo at.
     * @param y2 The Y coordinate to render the logo at.
     */
    public void renderLogo(DrawContext context, int screenWidth, float alpha, int y2) {
        RenderSystem.enableBlend();;
        RenderSystem.setShaderTexture(0, BLOCKGAME_LOGO_TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

        int textureWidth = (int)(232 / 1.3f);
        int textureHeight = (int)(66 / 1.3f);
        int i = (screenWidth / 4) - textureWidth / 2;
        context.drawTexture(BLOCKGAME_LOGO_TEXTURE, i, y2, 0, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    /**
     * Renders the player preview on the title screen.
     * @param mouseX The screen space X-coordinate of the mouse, which the player will look at.
     * @param mouseY The screen space Y-coordinate of the mouse, which the player will look at.
     */
    private void renderPlayer(int mouseX, int mouseY) {
        try {
            int size = height / 5;
            int x = (int)(width * 0.75);
            int y = (int)(height * 0.75) + (int)(size * 0.25);
            // todo InventoryScreen.drawEntity(x, y, size, -mouseX + x, -mouseY + y - size * 2 + size / 2f, fakePlayer);
        }
        catch(Exception e) {
            // cry, weep, shit pants and cum
            // this usually happens when player uses mods that alter player appearance
        }
    }

    /**
     * Renders the Blockgame server's status on the title screen.
     * @param context Current DrawContext.
     */
    private void renderServerStatus(DrawContext context) {
        try {
            boolean playerCountEmpty = serverInfo.playerCountLabel == null || serverInfo.playerCountLabel.getString().isEmpty();

            if(!playerCountEmpty) {
                // Draw summarizing text ("There are X players online" or "There are currently no players online.")
                MutableText key = serverInfo.playerListSummary != null ? Text.translatable(((TranslatableTextContent) SERVER_STATUS_ONLINE_NOTEMPTY.getContent()).getKey(), serverInfo.playerListSummary.size()) : SERVER_STATUS_ONLINE_EMPTY;
                context.drawText(client.textRenderer, key, (width / 2) + 4, 7, Integer.MAX_VALUE, true);

                // Draw player list
                if(serverInfo.playerListSummary != null) {
                    for (int i = 0; i < serverInfo.playerListSummary.size(); i++) {
                        context.drawText(client.textRenderer, serverInfo.playerListSummary.get(i), (width / 2) + 4, 21 + (12 * i), Integer.MAX_VALUE, true);
                    }
                }

                // Update prelogin latency info
                BlockgameEnhancedClient.setPreLoginLatency((int) serverInfo.ping);
            }
            else if(serverInfo.label != null) {
                context.drawText(client.textRenderer, serverInfo.label, (width / 2) + 4, 7, Integer.MAX_VALUE, true);
            }
        }
        catch (Exception e) {
            context.drawText(client.textRenderer, SERVER_STATUS_OFFLINE, (width / 2) + 4, 7, Integer.MAX_VALUE, true);
            throw e;
        }

        // I have no idea how Mojang does anything, their UI code sucks balls
        // This draws the server ping icon on the Play button by the way
        /*int l = (height / 2) - 7;
        int x = width / 4 - 75 + 132;
        int y = l + 5;
        int pi = serverInfo.online ? serverInfo.ping < 0L ? 5 : (serverInfo.ping < 50L ? 0 : (serverInfo.ping < 100L ? 1 : (serverInfo.ping < 175L ? 2 : (serverInfo.ping < 300L ? 3 : 4)))) : 5;
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, DrawableHelper.GUI_ICONS_TEXTURE);
        context.drawTexture(matrices, x, y, 0, 176 + pi * 8, 10, 8, 256, 256);*/
    }

    /**
     * Initializes button widgets.
     */
    private void initButtons() {
        assert client != null;
        int l = (height / 2) - 7;

        // Add Website Button
        addDrawableChild(ButtonWidget.builder(BUTTON_WEBSITE, (button) -> Util.getOperatingSystem().open("https://blockgame.info")).dimensions(width / 4 - 75, l + 24, 74, 20).build());

        // Add Wiki Button
        addDrawableChild(ButtonWidget.builder(BUTTON_WIKI, (button) -> Util.getOperatingSystem().open("https://blockgame.piratesoftware.wiki")).dimensions(width / 4 + 1, l + 24, 74, 20).build());

        // Add Play Button
        addDrawableChild(ButtonWidget.builder(BUTTON_PLAY, (button) -> ConnectScreen.connect(this, this.client, ServerAddress.parse("mc.blockgame.info"), new ServerInfo("BlockGame", "mc.blockgame.info", ServerInfo.ServerType.OTHER), true)).dimensions(width / 4 - 75, l, 150, 20).build());

        int bottomRowYOffset = 0;
        if(BlockgameEnhanced.isModmenuPresent()) {
            bottomRowYOffset = 24;

            addDrawableChild(ButtonWidget.builder(Text.of("Mods"), (button) -> {
                // Detect if ModMenu mod is present
                try
                {
                    Class<?> modsScreenClass = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
                    Constructor<?> constructor = modsScreenClass.getConstructor(Screen.class);
                    Screen screen = (Screen)constructor.newInstance(this);

                    client.setScreen(screen);
                }
                catch (Exception e)
                {
                    client.getToastManager().add(new SystemToast(SystemToast.Type.PACK_LOAD_FAILURE, Text.of("Error"), Text.of(e.getMessage())));
                    client.setScreen(new ThorScreen(this));
                }
            }).dimensions(width / 4 - 75, l + 48, 150, 20).build());
        }

        // Add Options Button
        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.options"), (button) -> {
            assert client != null;
            client.setScreen(new OptionsScreen(this, client.options));
        }).dimensions(width / 4 - 75, l + 48 + bottomRowYOffset, 74, 20).build());

        // Add Quit Game Button
        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.quit"), (button) -> {
            client.scheduleStop();
        }).dimensions(width / 4 + 1, l + 48 + bottomRowYOffset, 74, 20).build());
    }

    /**
     * Adds debug elements.
     */
    private void initDebug() {
        int l = (height / 2) - 7;
        addDrawableChild(ButtonWidget.builder(Text.of("auth"), (button) -> {
            try
            {
                Class<?> modsScreenClass = Class.forName("me.axieum.mcmod.authme.impl.gui.MicrosoftAuthScreen");
                Constructor<?> constructor = modsScreenClass.getConstructor(Screen.class, Screen.class);
                Screen screen = (Screen)constructor.newInstance(this, new TitleScreen());

                client.setScreen(screen);
            }
            catch (Exception e)
            {
                client.getToastManager().add(new SystemToast(SystemToast.Type.PACK_LOAD_FAILURE, Text.of("Error"), Text.of(e.getMessage())));
                client.setScreen(new ThorScreen(this));
            }
        }).dimensions(12, 12, 50, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.of("testworld"), (button) -> {
            client.setScreen(new SelectWorldScreen(this));
        }).dimensions(64, 12, 50, 20).build());
    }

    /**
     * Initializes the copyright notice from Mojang.
     */
    private void initCopyright() {
        assert client != null;
        int i = this.textRenderer.getWidth(net.minecraft.client.gui.screen.TitleScreen.COPYRIGHT);
        int j = this.width - i - 2;

        addDrawableChild(new PressableTextWidget(j, this.height - 22, i, 10, net.minecraft.client.gui.screen.TitleScreen.COPYRIGHT, (button) -> client.setScreen(new CreditsScreen(false, Runnables.doNothing())), textRenderer));
    }

    /**
     * Initializes the mod credit watermark.
     */
    private void initWatermark() {
        assert client != null;

        if(BlockgameEnhanced.DEBUG) {
            Text txt = Text.of("Blockgame Enhanced TEST BUILD");
            int i = this.textRenderer.getWidth(txt);
            int j = this.width - i - 2;
            this.addDrawableChild(new PressableTextWidget(j, this.height - 10, i, 10, txt, (x) -> {}, this.textRenderer));
            return;
        }

        int i = this.textRenderer.getWidth(WATERMARK);
        int j = this.width - i - 2;
        this.addDrawableChild(new PressableTextWidget(j, this.height - 10, i, 10, WATERMARK, (button) -> {
            eggClicks++;
            if(eggClicks % 7 == 0) {
                client.setScreen(new ThorScreen(this));
            }
        }, this.textRenderer));
    }
}
