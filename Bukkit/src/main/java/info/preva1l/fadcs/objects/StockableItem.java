package info.preva1l.fadcs.objects;

import org.bukkit.inventory.ItemStack;

public record StockableItem(
        boolean buying,
        boolean selling,
        double buyPrice,
        double sellPrice,
        ItemStack itemStack,
        int buyStock
) {
}
