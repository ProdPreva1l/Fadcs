package info.preva1l.fadcs.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import info.preva1l.fadcs.Fadcs;
import info.preva1l.fadcs.config.Config;
import info.preva1l.fadcs.objects.ChestShop;
import info.preva1l.fadcs.utils.Serializers;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@UtilityClass
public final class ShopCache {
    private final Cache<Location, ChestShop> chestShopCache = CacheBuilder.newBuilder().expireAfterAccess(Config.CACHE_LENGTH.toInteger(), TimeUnit.MINUTES).build();

    public void invalidate(@NotNull Location location) {
        chestShopCache.invalidate(location);
    }

    public void invalidate() {
        chestShopCache.invalidateAll();
    }

    @SneakyThrows
    @Nullable
    public ChestShop getShopAtLocation(@NotNull Location location) {
        return chestShopCache.get(location, () -> {
            try {
                Fadcs.instance().getDatabase().getChestShop(location).thenAccept(shop -> {
                    if (shop == null) {
                        Fadcs.getConsole().severe("Tried to load a chestshop at %s that does not exist!".formatted(Serializers.locationToString(location)));
                        return;
                    }
                    addToCache(location, shop);
                });
                return chestShopCache.asMap().get(location);
            } catch (Exception e) {
                Fadcs.getConsole().log(Level.SEVERE,"Failed to load data into cache!", e);
                return null;
            }
        });
    }

    public void addToCache(@NotNull Location location, @NotNull ChestShop chestShop) {
        chestShopCache.put(location, chestShop);
    }

    public List<ChestShop> getAllShops() {
        List<ChestShop> shops = new ArrayList<>();
        chestShopCache.asMap().forEach((l, shop) -> shops.add(shop));
        return shops;
    }
}
