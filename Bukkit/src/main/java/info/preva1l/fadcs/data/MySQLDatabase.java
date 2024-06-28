package info.preva1l.fadcs.data;

import com.zaxxer.hikari.HikariDataSource;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Setter
@Getter
public class MySQLDatabase implements Database {
    private final String driverClass;
    private boolean connected = false;
    private HikariDataSource dataSource;

    public MySQLDatabase() {
        super();

        this.driverClass = Config.DATABASE_TYPE.toDBTypeEnum() == DatabaseType.MARIADB ? "org.mariadb.jdbc.Driver" : "com.mysql.cj.jdbc.Driver";
    }

    @SuppressWarnings("SameParameterValue")
    @NotNull
    private String[] getSchemaStatements(@NotNull String schemaFileName) throws IOException {
        return new String(Objects.requireNonNull(Fadcs.instance().getResource(schemaFileName))
                .readAllBytes(), StandardCharsets.UTF_8).split(";");
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void connect() {
        dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClass);
        dataSource.setJdbcUrl(Config.DATABASE_URI.toString());

        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(10);
        dataSource.setMaxLifetime(1800000);
        dataSource.setKeepaliveTime(0);
        dataSource.setConnectionTimeout(5000);
        dataSource.setPoolName("FadcsHikariPool");

        final Properties properties = new Properties();
        properties.putAll(
                Map.of("cachePrepStmts", "true",
                        "prepStmtCacheSize", "250",
                        "prepStmtCacheSqlLimit", "2048",
                        "useServerPrepStmts", "true",
                        "useLocalSessionState", "true",
                        "useLocalTransactionState", "true"
                ));
        properties.putAll(
                Map.of(
                        "rewriteBatchedStatements", "true",
                        "cacheResultSetMetadata", "true",
                        "cacheServerConfiguration", "true",
                        "elideSetAutoCommits", "true",
                        "maintainTimeStats", "false")
        );
        dataSource.setDataSourceProperties(properties);

        try (Connection connection = dataSource.getConnection()) {
            final String[] databaseSchema = getSchemaStatements(String.format("database/%s_schema.sql", Config.DATABASE_TYPE.toDBTypeEnum().getId()));
            try (Statement statement = connection.createStatement()) {
                for (String tableCreationStatement : databaseSchema) {
                    statement.execute(tableCreationStatement);
                }
                setConnected(true);
            } catch (SQLException e) {
                destroy();
                throw new IllegalStateException("Failed to create database tables. Please ensure you are running MySQL v8.0+ " +
                        "and that your connecting user account has privileges to create tables.", e);
            }
        } catch (SQLException | IOException e) {
            destroy();
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. " +
                    "Please check the supplied database credentials in the config file", e);
        }
    }

    @Override
    public void destroy() {
        if (dataSource == null) return;
        if (dataSource.isClosed()) return;
        dataSource.close();
        setConnected(false);
    }

    @Override
    public CompletableFuture<Void> createChestShop(ChestShop chestShop) {
        if (!isConnected()) {
            Fadcs.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
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
            return null;
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
