package info.preva1l.fadcs.data;

import info.preva1l.fadcs.cache.ShopCache;
import info.preva1l.fadcs.config.Config;
import info.preva1l.fadcs.objects.ChestShop;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.List;

@UtilityClass
public class PermissionsData {
    public int getCurrentShops(Player player) {
        List<ChestShop> chestShops = ShopCache.getAllShops();
        chestShops.removeIf(shop -> shop.getOwner() != player.getUniqueId());
        return chestShops.size();
    }

    public int valueFromPermission(PermissionType type, Player player) {
        int currentMax = 0;
        boolean matched = false;
        for (PermissionAttachmentInfo effectivePermission : player.getEffectivePermissions()) {
            if (!effectivePermission.getPermission().startsWith(type.permissionString)) continue;
            String numberStr = effectivePermission.getPermission().substring(type.permissionString.length());
            try {
                if (currentMax < Integer.parseInt(numberStr)) {
                    currentMax = Integer.parseInt(numberStr);
                    matched = true;
                }
            } catch (NumberFormatException ignored) {}
        }
        return matched ? currentMax : Config.DEFAULT_MAX_SHOPS.toInteger();
    }

    @AllArgsConstructor
    public enum PermissionType {
        MAX_SHOPS("fadcs.max-shops."),
        ;
        private final String permissionString;
    }
}
