package dev.jb0s.blockgameenhanced.config;

import dev.jb0s.blockgameenhanced.BlockgameEnhanced;
import dev.jb0s.blockgameenhanced.BlockgameEnhancedClient;
import dev.jb0s.blockgameenhanced.config.modules.ModConfig;
import dev.jb0s.blockgameenhanced.helper.FileHelper;
import lombok.SneakyThrows;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtTagSizeTracker;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Path INVENTORY_SNAPSHOT_PATH = Path.of(FileHelper.getBlockgamePath() + "/INVENTORY.dat");

    public ConfigManager() {
        // Register events
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if(!handler.getConnection().isLocal()) {
                ConfigManager cfgMgr = BlockgameEnhancedClient.getConfigManager();
                cfgMgr.saveInventorySnapshot(client.player);
            }
        });
    }

    public Screen constructConfigScreen(Screen parent) {
        return AutoConfig.getConfigScreen(ModConfig.class, parent).get();
    }

    public NbtList getInventorySnapshot() {
        // If the inventory snapshot is not yet there, just return nothing instead of trying to read it.
        if(Files.notExists(INVENTORY_SNAPSHOT_PATH)) {
            return new NbtList();
        }

        try {
            DataInput input = new DataInputStream(new FileInputStream(INVENTORY_SNAPSHOT_PATH.toFile()));
            return NbtList.TYPE.read(input, new NbtTagSizeTracker(9000000, 40));
        }
        catch (Exception e) {
            BlockgameEnhanced.LOGGER.warn("Failed to load inventory snapshot!");
            return new NbtList();
        }
    }

    @SneakyThrows
    public void saveInventorySnapshot(ClientPlayerEntity playerEntity) {
        // If no inventory snapshot file exists, create an empty file there, so we can write to it.
        if(Files.notExists(INVENTORY_SNAPSHOT_PATH)) {
            Files.createFile(INVENTORY_SNAPSHOT_PATH);
        }

        PlayerInventory inv = playerEntity.getInventory();
        DataOutput testOutput = new DataOutputStream(new FileOutputStream(INVENTORY_SNAPSHOT_PATH.toFile()));
        NbtList out = new NbtList();
        inv.writeNbt(out);
        out.write(testOutput);
    }
}
