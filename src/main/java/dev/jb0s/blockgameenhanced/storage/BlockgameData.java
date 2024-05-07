package dev.jb0s.blockgameenhanced.storage;

import com.mojang.serialization.Codec;
import dev.jb0s.blockgameenhanced.BlockgameEnhanced;
import dev.jb0s.blockgameenhanced.gamefeature.statprofiles.allocate.StatProfile;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Getter
public class BlockgameData {
  public static final Codec<Map<String, StatProfile>> STAT_PROFILES_CODEC = Codec
      .unboundedMap(Codec.STRING, StatProfile.CODEC)
      .xmap(HashMap::new, Function.identity());

  @Nullable
  public static BlockgameData INSTANCE = null;

  private final Map<String, StatProfile> statProfiles;

  public BlockgameData() {
    this.statProfiles = new HashMap<>();
  }

  public static void loadOrCreate() {
    BlockgameData.unload();
    BlockgameData.INSTANCE = Storage.load().orElseGet(BlockgameData::new);
    BlockgameData.save();
  }

  public static void save() {
    if (BlockgameData.INSTANCE == null) {
      return;
    }

    Storage.save(BlockgameData.INSTANCE);
    BlockgameEnhanced.LOGGER.info("Blockgame data saved");
  }

  public static void unload() {
    if (BlockgameData.INSTANCE == null) {
      return;
    }

    Storage.save(BlockgameData.INSTANCE);
    BlockgameData.INSTANCE = null;
    BlockgameEnhanced.LOGGER.info("Blockgame data unloaded");
  }
}
