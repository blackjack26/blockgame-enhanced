package dev.jb0s.blockgameenhanced.config.modules;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "stat")
public class StatConfig implements ConfigData {
  @ConfigEntry.Gui.Tooltip
  public boolean enableEnhancedStats;

  @ConfigEntry.Gui.Tooltip
  public boolean useOldStatView;

  public StatConfig() {
    enableEnhancedStats = true;
    useOldStatView = false;
  }
}
