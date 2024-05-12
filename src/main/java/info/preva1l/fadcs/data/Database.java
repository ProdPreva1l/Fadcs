package info.preva1l.fadcs.data;

import info.preva1l.fadcs.Fadcs;
import info.preva1l.fadcs.cache.ShopCache;
import info.preva1l.fadcs.objects.ChestShop;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public interface Database {

    /**
     * Connect to the database
     */
    void connect();

    /**
     * End the connection to the database
     */
    void destroy();

    /**
     * Load the chestshop into cache at location (if any exists)
     * @param location location where the shop is
     * @return true if the value was added to cache or false if it failed in any way
     */
    default boolean load(@NotNull Location location) {
        if (!isConnected()) {
            Fadcs.getConsole().severe("Tried to perform database action when the database is not connected!");
            return false;
        }
        try {
            AtomicBoolean exists = new AtomicBoolean(false);
            ShopCache.invalidate(location);
            getChestShop(location).thenAccept(shop -> {
                if (shop == null) {
                    exists.set(true);
                    return;
                }
                ShopCache.addToCache(location, shop);
            });
            if (!exists.get()) {
                return false;
            }
        } catch (Exception e){
            return false;
        }
        return true;
    }

    /**
     * Create a chestshop
     * @param chestShop the shop to actually create
     */
    void createChestShop(ChestShop chestShop);

    /**
     * Remove a chestshop
     * @param location location of the chestshop
     */
    void removeChestShop(Location location);

    /**
     * Get a chestshop object at a location (if it exists)
     * @param location location of the shop
     * @return completable future of a nullable chestshop object
     */
    CompletableFuture<@Nullable ChestShop> getChestShop(@NotNull Location location);

    boolean isConnected();
}