package dev.jb0s.blockgameenhanced.storage.backend;

import dev.jb0s.blockgameenhanced.BlockgameEnhanced;
import dev.jb0s.blockgameenhanced.helper.FileHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class FileBasedBackend implements Backend {
  public static final String STAT_PROFILES_NAME = "stat_profiles";

  @Override
  public void delete() {
    getRelevantPaths().forEach(path -> {
      if (Files.isRegularFile(path)) {
        try {
          Files.delete(path);
          BlockgameEnhanced.LOGGER.info("Deleted file {}", path);
        } catch (IOException e) {
          BlockgameEnhanced.LOGGER.error("Failed to delete file {}", path, e);
        }
      }
    });
  }

  public abstract String extension();

  protected List<Path> getRelevantPaths() {
    return List.of(
        FileHelper.getBlockgamePath().resolve(STAT_PROFILES_NAME + extension())
    );
  }
}
