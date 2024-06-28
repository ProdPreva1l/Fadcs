package info.preva1l.fadcs.objects;

import info.preva1l.fadcs.Fadcs;
import lombok.Getter;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;
import java.util.UUID;

@Getter
@Value
public class AdminChestShop implements ChestShop {
    UUID owner = null;
    Listings listings;
    Location location;
    boolean adminShop = true;

    /**
     * Create a new admin shop
     * @param location location of the sign
     * @return an optional of the chestshop object if it was successful or an empty optional if fail
     */
    @ApiStatus.Internal
    public static Optional<AdminChestShop> create(Location location, Chest chest) {
        if (ChestShop.getShopAt(location).isPresent()) {
            return Optional.empty();
        }
        Listings temp = Listings.newEmpty();
        for (ItemStack itemStack : chest.getBlockInventory()) {
            temp.add(new StockableItem(false, false, 0.00, 0.00, itemStack, itemStack.getAmount()));
        }

        AdminChestShop shop = new AdminChestShop(temp, location);
        Fadcs.instance().getDatabase().createChestShop(shop);
        return Optional.of(shop);
    }
}
