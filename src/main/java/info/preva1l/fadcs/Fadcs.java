package info.preva1l.fadcs;

import info.preva1l.fadcs.utils.BasicConfig;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class Fadcs extends JavaPlugin {
    @Getter private BasicConfig configFile;

    @Override
    public void onEnable() {
        loadFiles();
    }

    @Override
    public void onDisable() {

    }

    private void loadFiles() {
        configFile = new BasicConfig(this, "config.yml");
    }

    private Fadcs instance() {
        return this;
    }
}
