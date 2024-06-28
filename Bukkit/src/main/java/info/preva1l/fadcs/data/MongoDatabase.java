package info.preva1l.fadcs.data;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import info.preva1l.fadcs.Fadcs;
import info.preva1l.fadcs.cache.ShopCache;
import info.preva1l.fadcs.config.Config;
import info.preva1l.fadcs.objects.AdminChestShop;
import info.preva1l.fadcs.objects.ChestShop;
import info.preva1l.fadcs.objects.Listings;
import info.preva1l.fadcs.objects.PlayerChestShop;
import info.preva1l.fadcs.utils.TaskManager;
import info.preva1l.fadcs.utils.mongo.CacheHandler;
import info.preva1l.fadcs.utils.mongo.CollectionHelper;
import info.preva1l.fadcs.utils.mongo.MongoConnectionHandler;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MongoDatabase implements Database {
    @Getter @Setter private boolean connected = false;
    private MongoConnectionHandler connectionHandler;
    private CacheHandler cacheHandler;
    @Getter private CollectionHelper collectionHelper;

    @Override
    public void connect() {
        try {
            @NotNull String connectionURI = Config.DATABASE_URI.toString();
            @NotNull String database = Config.DATABASE.toString();
            Fadcs.getConsole().info("Connecting to: " + connectionURI);
            connectionHandler = new MongoConnectionHandler(connectionURI, database);
            cacheHandler = new CacheHandler(connectionHandler);
            collectionHelper = new CollectionHelper(connectionHandler.getDatabase(), cacheHandler);
            setConnected(true);
        } catch (Exception e) {
            destroy();
            throw new IllegalStateException("Failed to establish a connection to the MongoDB database. " +
                    "Please check the supplied database credentials in the config file", e);
        }
    }

    @Override
    public void destroy() {
        setConnected(false);
        if (connectionHandler != null) connectionHandler.closeConnection();
        collectionHelper = null;
        cacheHandler = null;
        connectionHandler = null;
    }

    @Override
    public CompletableFuture<Void> createChestShop(ChestShop chestShop) {
        if (!isConnected()) {
            Fadcs.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
        Document document = new Document("owner", chestShop.getOwner())
                .append("listings", chestShop.getListings().getAsJson())
                .append("adminShop", chestShop.isAdminShop())
                .append("location", chestShop.getLocation());
        collectionHelper.insertDocument("chest_shops", document);
        return null;
        });
    }

    @Override
    public void removeChestShop(Location location) {
        if (!isConnected()) {
            Fadcs.getConsole().severe("Tried to perform database action when the database is not connected!");
            return;
        }
        if (ShopCache.getShopAtLocation(location) == null) {
            return;
        }
        TaskManager.Async.run(Fadcs.instance(), () -> collectionHelper.deleteDocument("chest_shops", getDocument(location)));
    }

    @Override
    public CompletableFuture<@Nullable ChestShop> getChestShop(@NotNull Location location) {
        if (!isConnected()) {
            Fadcs.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(() -> null);
        }
        return CompletableFuture.supplyAsync(() -> {
            final Document document = getDocument(location);
            if (document == null) {
                return null;
            }

            boolean isAdmin = document.getBoolean("adminShop");
            Listings listings = Listings.fromJsonString(document.getString("listings"));

            if (isAdmin) {
                return new AdminChestShop(listings, location);
            } else {
                try {
                    UUID owner = UUID.fromString(document.getString("owner"));
                    return new PlayerChestShop(owner, listings, location);
                } catch (IllegalArgumentException e) {
                    Fadcs.getConsole().severe("Manual Data Manipulation Detected, please dont!!!!! you just broke someones chest shop :(");
                    Fadcs.getConsole().severe("Failed to parse UUID '%s'".formatted(document.getString("owner")));
                    return null;
                }
            }
        });
    }
    @Nullable
    private Document getDocument(@NotNull Location location) {
        MongoCollection<Document> collection = collectionHelper.getCollection("chest_shops");
        return collection.find().filter(Filters.eq("location", location)).first();
    }
}
