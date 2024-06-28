package info.preva1l.fadcs.objects;

import info.preva1l.fadcs.Fadcs;
import lombok.Getter;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

@Getter
@Value
public class PlayerChestShop implements ChestShop {
    @NotNull UUID owner;
    Listings listings;
    Location location;
    boolean adminShop = false;

    public static Optional<PlayerChestShop> create(Player player, Location location, Chest chest) {
        if (ChestShop.getShopAt(location).isPresent()) {
            return Optional.empty();
        }
        Listings temp = Listings.newEmpty();
        for (ItemStack itemStack : chest.getBlockInventory()) {
            temp.add(new StockableItem(false, false, 0.00, 0.00, itemStack, itemStack.getAmount()));
        }

        PlayerChestShop shop = new PlayerChestShop(player.getUniqueId(), temp, location);
        Fadcs.instance().getDatabase().createChestShop(shop);
        return Optional.of(shop);
    }
}