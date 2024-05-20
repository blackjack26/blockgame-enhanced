package dev.jb0s.blockgameenhanced.storage.backend;

import dev.jb0s.blockgameenhanced.storage.BlockgameData;
import dev.jb0s.blockgameenhanced.storage.Storage;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A handler for storing a memory bank
 */
public interface Backend {
  /**
   * Loads the journal from the backend if it exists, or returns null if not
   * @return Loaded journal or null if not available
   */
  @Nullable
  BlockgameData load();

  /**
   * Deletes the journal from the storage. Not reversible.
   */
  void delete();

  /**
   * Saves this journal to the backend
   * @param data BlockgameData to save to this storage
   */
  boolean save(BlockgameData data);

  enum Type {
    NBT(new NbtBackend()),
    MEMORY(new GameMemoryBackend());

    public final Backend instance;

    Type(Backend instance) {
      this.instance = instance;
    }

    public void load() {
      BlockgameData.unload();
      Storage.setBackend(this.instance);
    }
  }
}