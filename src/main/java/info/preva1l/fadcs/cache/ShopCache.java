package info.preva1l.fadcs.cache;

import info.preva1l.fadcs.shops.ChestShop;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

@UtilityClass
public final class ShopCache {
    private final HashMap<Location, ChestShop> chestShopCache = new HashMap<>();

    public void invalidate(@NotNull Location location) {
        chestShopCache.remove(location);
    }

    public void invalidate() {
        chestShopCache.clear();
    }

    @Nullable
    public ChestShop getShopAtLocation(@NotNull Location location) {
        return chestShopCache.get(location);
    }

    public void addToCache(@NotNull Location location, @NotNull ChestShop chestShop) {
        chestShopCache.put(location, chestShop);
    }
}
