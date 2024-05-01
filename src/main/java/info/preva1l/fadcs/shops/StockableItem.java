package info.preva1l.fadcs.shops;

import org.bukkit.inventory.ItemStack;

public record StockableItem(PurchaseMode purchaseMode, double price, ItemStack itemStack, int available) {
}
