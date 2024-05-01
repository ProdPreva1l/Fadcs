package info.preva1l.fadcs.shops;

import java.util.List;

public record PlayerChestShop(List<StockableItem> listings) implements ChestShop {
    @Override
    public boolean isAdminShop() {
        return false;
    }
}
