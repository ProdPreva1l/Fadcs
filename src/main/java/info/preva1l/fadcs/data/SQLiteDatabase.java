package info.preva1l.fadcs.data;

import info.preva1l.fadcs.Fadcs;
import info.preva1l.fadcs.config.Config;
import info.preva1l.fadcs.objects.AdminChestShop;
import info.preva1l.fadcs.objects.ChestShop;
import info.preva1l.fadcs.objects.Listings;
import info.preva1l.fadcs.objects.PlayerChestShop;
import info.preva1l.fadcs.utils.Serializers;
import info.preva1l.fadcs.utils.TaskManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sqlite.SQLiteConfig;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class SQLiteDatabase implements Database {
    private static final String DATABASE_FILE_NAME = "FadahData.db";
    @Getter
    @Setter
    private boolean connected = false;
    private File databaseFile;
    private Connection connection;

    @SuppressWarnings("SameParameterValue")
    @NotNull
    private String[] getSchemaStatements(@NotNull String schemaFileName) throws IOException {
        return new String(Objects.requireNonNull(Fadcs.instance().getResource(schemaFileName))
                .readAllBytes(), StandardCharsets.UTF_8).split(";");
    }

    private Connection getConnection() throws SQLException {
        if (connection == null) {
            setConnection();
        } else if (connection.isClosed()) {
            setConnection();
        }
        return connection;
    }

    private void setConnection() {
        try {
            if (databaseFile.createNewFile()) {
                Fadcs.getConsole().info("Created the SQLite database file");
            }

            Class.forName("org.sqlite.JDBC");

            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            config.setEncoding(SQLiteConfig.Encoding.UTF8);
            config.setSynchronous(SQLiteConfig.SynchronousMode.FULL);

            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath(), config.toProperties());
        } catch (IOException e) {
            Fadcs.getConsole().log(Level.SEVERE, "An exception occurred creating the database file", e);
        } catch (SQLException e) {
            Fadcs.getConsole().log(Level.SEVERE, "An SQL exception occurred initializing the SQLite database", e);
        } catch (ClassNotFoundException e) {
            Fadcs.getConsole().log(Level.SEVERE, "Failed to load the necessary SQLite driver", e);
        }
    }

    private void backupFlatFile(@NotNull File file) {
        if (!file.exists()) {
            return;
        }

        final File backup = new File(file.getParent(), String.format("%s.bak", file.getName()));
        try {
            if (!backup.exists() || backup.delete()) {
                Files.copy(file.toPath(), backup.toPath());
            }
        } catch (IOException e) {
            Fadcs.getConsole().log(Level.WARNING, "Failed to backup flat file database", e);
        }
    }

    @Override
    public void connect() {
        databaseFile = new File(Fadcs.instance().getDataFolder(), DATABASE_FILE_NAME);

        this.setConnection();
        this.backupFlatFile(databaseFile);

        try {
            final String[] databaseSchema = getSchemaStatements(String.format("database/%s_schema.sql", Config.DATABASE_TYPE.toDBTypeEnum().getId()));
            try (Statement statement = connection.createStatement()) {
                for (String tableCreationStatement : databaseSchema) {
                    statement.execute(tableCreationStatement);
                }
            } catch (SQLException e) {
                destroy();
                throw new IllegalStateException("Failed to create database tables. Please ensure you are running MySQL v8.0+ " +
                        "and that your connecting user account has privileges to create tables.", e);
            }
        } catch (IOException e) {
            destroy();
            throw new IllegalStateException("Failed to create database tables. Please ensure you are running MySQL v8.0+ " +
                    "and that your connecting user account has privileges to create tables.", e);
        }
        setConnected(true);
    }

    @Override
    public void destroy() {
        try {
            if (connection != null) {
                if (!connection.isClosed()) {
                    connection.close();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        setConnected(false);
    }

    @Override
    public void createChestShop(ChestShop chestShop) {
        if (!isConnected()) {
            Fadcs.getConsole().severe("Tried to perform database action when the database is not connected!");
            return;
        }
        TaskManager.Async.run(Fadcs.instance(), () -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO `chest_shops`
                        (`location`, `owner`, `adminShop`,`listings`)
                        VALUES (?,?,?,?);""")) {
                    statement.setString(1, Serializers.locationToString(chestShop.getLocation()));
                    statement.setString(2, chestShop.getOwner() == null ? null : chestShop.getOwner().toString());
                    statement.setBoolean(3, chestShop.isAdminShop());
                    statement.setString(4, chestShop.getListings().getAsJson().getAsString());
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                Fadcs.getConsole().severe("Failed to add item to collection box!");
            }
        });
    }

    @Override
    public void removeChestShop(Location location) {
        if (!isConnected()) {
            Fadcs.getConsole().severe("Tried to perform database action when the database is not connected!");
            return;
        }
        TaskManager.Async.run(Fadcs.instance(), () -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        DELETE FROM `chest_shops`
                        WHERE `location`=?""")) {
                    statement.setString(1, Serializers.locationToString(location));
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                Fadcs.getConsole().severe("Failed to remove item from collection box!");
            }
        });
    }

    @Override
    public CompletableFuture<@Nullable ChestShop> getChestShop(@NotNull Location location) {
        if (!isConnected()) {
            Fadcs.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(() -> null);
        }
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        SELECT `owner`,  `adminShop`, `listings`
                        FROM `chest_shops`
                        WHERE `location`=?;""")) {
                    statement.setString(1, Serializers.locationToString(location));
                    final ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        boolean isAdmin = resultSet.getBoolean("adminShop");
                        Listings listings = Listings.fromJsonString(resultSet.getString("listings"));

                        if (isAdmin) {
                            return new AdminChestShop(listings, location);
                        } else {
                            try {
                                UUID owner = UUID.fromString(resultSet.getString("owner"));
                                return new PlayerChestShop(owner, listings, location);
                            } catch (IllegalArgumentException e) {
                                Fadcs.getConsole().severe("Manual Data Manipulation Detected, please dont!!!!! you just broke someones chest shop :(");
                                Fadcs.getConsole().severe("Failed to parse UUID '%s'".formatted(resultSet.getString("owner")));
                                return null;
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                Fadcs.getConsole().severe("Failed to get chest shop at location: %s".formatted(location));
            }
            return null;
        });
    }
}
