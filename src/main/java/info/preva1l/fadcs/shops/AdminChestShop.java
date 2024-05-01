package info.preva1l.fadcs.shops;

import java.util.List;

public record AdminChestShop(List<StockableItem> listings) implements ChestShop {
    @Override
    public boolean isAdminShop() {
        return true;
    }
}
