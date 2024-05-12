package info.preva1l.fadcs.config;

import com.google.common.collect.ImmutableList;
import info.preva1l.fadcs.Fadcs;
import info.preva1l.fadcs.data.DatabaseType;
import info.preva1l.fadcs.utils.BasicConfig;
import info.preva1l.fadcs.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public enum Config {
    DEFAULT_MAX_SHOPS("default-max-shops", 3),

    PLAYER_CREATE_FORMAT("sign-format.player.create", List.of("[shop]", "[chestshop]")),
    ADMIN_CREATE_FORMAT("sign-format.admin.create", List.of("[admin shop]", "[admin chestshop]")),

    PLAYER_FORMAT_1("sign-format.player.line-1", "&e&lChest Shop"),
    PLAYER_FORMAT_2("sign-format.player.line-2", "Right Click"),
    PLAYER_FORMAT_3("sign-format.player.line-3", "&fOwner:"),
    PLAYER_FORMAT_4("sign-format.player.line-4", "{0}"),
    ADMIN_FORMAT_1("sign-format.admin.line-1", "&e&lChest Shop"),
    ADMIN_FORMAT_2("sign-format.admin.line-2", "Right Click"),
    ADMIN_FORMAT_3("sign-format.admin.line-3", ""),
    ADMIN_FORMAT_4("sign-format.admin.line-4", "&f(Admin Shop)"),

    CACHE_LENGTH("cache-length", 30),
    STRICT_CHECKS("strict-checks", false),

    LOG_TO_FILE("log-to-file", true),

    DATABASE_TYPE("database.type", "SQLITE"),
    DATABASE_URI("database.uri", "jdbc:mysql://username:password@127.0.0.1:3306/Fadcs"),
    DATABASE("database.database", "Fadcs"),

    REDIS_ENABLED("redis.enabled", false),
    REDIS_HOST("redis.host", "127.0.0.1"),
    REDIS_PORT("redis.port", 6379),
    REDIS_PASSWORD("redis.password", "password"),
    REDIS_CHANNEL("redis.channel", "chestshop.cache"),
    ;

    private final String path;
    private final Object defaultValue;

    public static void loadDefault() {
        BasicConfig configFile = Fadcs.instance().getConfigFile();

        for (Config config : Config.values()) {
            String path = config.getPath();
            String str = configFile.getString(path);
            if (str.equals(path)) {
                configFile.getConfiguration().set(path, config.getDefaultValue());
            }
        }

        configFile.save();
        configFile.load();
    }

    public String toString() {
        String str = Fadcs.instance().getConfigFile().getString(path);
        if (str.equals(path)) {
            return defaultValue.toString();
        }
        return str;
    }

    public DatabaseType toDBTypeEnum() {
        DatabaseType databaseType;
        try {
            databaseType = DatabaseType.valueOf(toString());
        } catch (EnumConstantNotPresentException ex) {
            Fadcs.getConsole().warning(StringUtils.formatPlaceholders("Database Type \"{0}\" does not exist! \n Defaulting to MongoDB", toString()));
            return DatabaseType.MONGO;
        }
        return databaseType;
    }

    public String toFormattedString() {
        String str = Fadcs.instance().getConfigFile().getString(path);
        if (str.equals(path)) {
            return StringUtils.colorize(defaultValue.toString());
        }
        return StringUtils.colorize(str);
    }

    public String toFormattedString(Object... replacements) {
        String str = Fadcs.instance().getConfigFile().getString(path);
        if (str.equals(path)) {
            return StringUtils.formatPlaceholders(StringUtils.colorize(defaultValue.toString()), replacements);
        }
        return StringUtils.colorize(StringUtils.formatPlaceholders(str, replacements));
    }

    public List<String> toStringList() {
        List<String> str = Fadcs.instance().getConfigFile().getStringList(path);
        if (str.isEmpty() || str.get(0).equals(path)) {
            return (List<String>) defaultValue;
        }
        if (str.get(0).equals("null")) {
            return ImmutableList.of();
        }
        return str;
    }

    public List<String> toLore() {
        List<String> str = Fadcs.instance().getConfigFile().getStringList(path);
        if (str.isEmpty() || str.get(0).equals(path)) {
            List<String> ret = new ArrayList<>();
            for (String line : (List<String>) defaultValue) ret.add(StringUtils.formatPlaceholders(line));
            return StringUtils.colorizeList(ret);
        }
        if (str.get(0).equals("null")) {
            return ImmutableList.of();
        }
        List<String> ret = new ArrayList<>();
        for (String line : str) ret.add(StringUtils.formatPlaceholders(line));
        return StringUtils.colorizeList(ret);
    }

    public List<String> toLore(Object... replacements) {
        List<String> str = Fadcs.instance().getConfigFile().getStringList(path);
        if (str.isEmpty() || str.get(0).equals(path)) {
            List<String> ret = new ArrayList<>();
            for (String line : (List<String>) defaultValue) ret.add(StringUtils.formatPlaceholders(line, replacements));
            return StringUtils.colorizeList(ret);
        }
        if (str.get(0).equals("null")) {
            return ImmutableList.of();
        }
        List<String> ret = new ArrayList<>();
        for (String line : str) {
            ret.add(StringUtils.formatPlaceholders(line, replacements));
        }
        return StringUtils.colorizeList(ret);
    }

    public boolean toBoolean() {
        return Boolean.parseBoolean(toString());
    }

    public int toInteger() {
        return Integer.parseInt(toString());
    }

    public double toDouble() {
        return Double.parseDouble(toString());
    }
}