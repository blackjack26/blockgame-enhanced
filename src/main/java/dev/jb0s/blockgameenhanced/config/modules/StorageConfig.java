package dev.jb0s.blockgameenhanced.config.modules;

import dev.jb0s.blockgameenhanced.storage.backend.Backend;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "storage")
public class StorageConfig implements ConfigData {
  @ConfigEntry.Gui.Tooltip
  @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
  public Backend.Type backendType;

  public StorageConfig() {
    backendType = Backend.Type.NBT;
  }
}
