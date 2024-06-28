package info.preva1l.fadcs.data;

import info.preva1l.fadcs.Fadcs;
import info.preva1l.fadcs.cache.ShopCache;
import info.preva1l.fadcs.objects.ChestShop;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

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
     * @return a completable future of true if the value was added to cache or false if it failed in any way
     */
    default CompletableFuture<Boolean> load(@NotNull Location location) {
        if (!isConnected()) {
            Fadcs.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(false);
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                ShopCache.invalidate(location);
                ChestShop shop = getChestShop(location).join();
                if (shop == null) {
                    return false;
                }
                ShopCache.addToCache(location, shop);
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Create a chestshop
     * @param chestShop the shop to actually create
     */
    CompletableFuture<Void> createChestShop(ChestShop chestShop);

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