package info.preva1l.fadcs.objects;

import info.preva1l.fadcs.cache.ShopCache;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface ChestShop {
    @Nullable UUID getOwner();
    @NotNull Listings getListings();
    @NotNull Location getLocation();
    boolean isAdminShop();


    static Optional<ChestShop> getShopAt(Location location) {
        return Optional.ofNullable(ShopCache.getShopAtLocation(location));
    }
}