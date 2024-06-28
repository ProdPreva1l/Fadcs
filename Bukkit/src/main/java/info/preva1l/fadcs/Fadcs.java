package info.preva1l.fadcs;

import info.preva1l.fadcs.api.BukkitChestShopAPI;
import info.preva1l.fadcs.api.ChestShopAPI;
import info.preva1l.fadcs.config.Config;
import info.preva1l.fadcs.data.Database;
import info.preva1l.fadcs.data.MongoDatabase;
import info.preva1l.fadcs.data.MySQLDatabase;
import info.preva1l.fadcs.data.SQLiteDatabase;
import info.preva1l.fadcs.listeners.ShopListener;
import info.preva1l.fadcs.utils.BasicConfig;
import info.preva1l.fadcs.utils.StringUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class Fadcs extends JavaPlugin {
    private static Fadcs instance;

    @Getter private static Logger console;

    @Getter private BasicConfig configFile;

    @Getter private Database database;

    @Override
    public void onEnable() {
        instance = this;

        console = getLogger();

        loadFiles();
        loadDatabase();
        loadListeners();

        getConsole().info("Enabling the API...");
        ChestShopAPI.setInstance(new BukkitChestShopAPI());
        getConsole().info("API Enabled!");

        Bukkit.getConsoleSender().sendMessage(StringUtils.colorize("&2&l------------------------------"));
        Bukkit.getConsoleSender().sendMessage(StringUtils.colorize("&a Finally a Decent Chest Shop"));
        Bukkit.getConsoleSender().sendMessage(StringUtils.colorize("&a   has successfully started!"));
        Bukkit.getConsoleSender().sendMessage(StringUtils.colorize("&2&l------------------------------"));
    }

    @Override
    public void onDisable() {
        database.destroy();
    }

    private void loadFiles() {
        configFile = new BasicConfig(this, "config.yml");
    }

    private void loadDatabase() {
        database = switch (Config.DATABASE_TYPE.toDBTypeEnum()) {
            case SQLITE -> new SQLiteDatabase();
            case MYSQL, MARIADB -> new MySQLDatabase();
            case MONGO -> new MongoDatabase();
        };
        database.connect();
    }

    private void loadListeners() {
        getServer().getPluginManager().registerEvents(new ShopListener(), this);
    }

    public static Fadcs instance() {
        return instance;
    }
}
