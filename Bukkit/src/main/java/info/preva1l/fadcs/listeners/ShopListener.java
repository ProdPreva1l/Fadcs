package info.preva1l.fadcs.listeners;

import info.preva1l.fadcs.Fadcs;
import info.preva1l.fadcs.cache.ShopCache;
import info.preva1l.fadcs.config.Config;
import info.preva1l.fadcs.objects.AdminChestShop;
import info.preva1l.fadcs.objects.PlayerChestShop;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Optional;

public class ShopListener implements Listener {
    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void signChangeEvent(SignChangeEvent event) {
        if (!event.getPlayer().hasPermission("fadcs.use")) {
            return;
        }

        if (ShopCache.getShopAtLocation(event.getBlock().getLocation()) != null) {
            event.setCancelled(true);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(Fadcs.instance(), ()->{
            Block block = event.getBlock();
            if (!(block.getState() instanceof WallSign)) {
                return;
            }

            BlockData data = block.getBlockData();
            Block chest = block.getRelative(((Directional) data).getFacing().getOppositeFace());

            if (signHasAdminShop(event.getLines()) && event.getPlayer().hasPermission("fadcs.create.admin")) {
                Optional<AdminChestShop> shop = AdminChestShop.create(block.getLocation(), (Chest) chest);
                if (shop.isEmpty()) {
                    event.getPlayer().sendMessage(Config.LOCALE_GENERIC_ERROR.toFormattedString("SL-SHAS-SAE"));
                    return;
                }

                event.setLine(0, Config.ADMIN_FORMAT_1.toFormattedString(event.getPlayer()));
                event.setLine(1, Config.ADMIN_FORMAT_2.toFormattedString(event.getPlayer()));
                event.setLine(2, Config.ADMIN_FORMAT_3.toFormattedString(event.getPlayer()));
                event.setLine(3, Config.ADMIN_FORMAT_4.toFormattedString(event.getPlayer()));
            }
            if (signHasShop(event.getLines()) && event.getPlayer().hasPermission("fadcs.create")) {
                Optional<PlayerChestShop> shop = PlayerChestShop.create(event.getPlayer(), event.getBlock().getLocation(), (Chest) chest);
                if (shop.isEmpty()) {
                    event.getPlayer().sendMessage(Config.LOCALE_GENERIC_ERROR.toFormattedString("SL-SHS-SAE"));
                    return;
                }

                event.setLine(0, Config.PLAYER_FORMAT_1.toFormattedString(event.getPlayer()));
                event.setLine(1, Config.PLAYER_FORMAT_2.toFormattedString(event.getPlayer()));
                event.setLine(2, Config.PLAYER_FORMAT_3.toFormattedString(event.getPlayer()));
                event.setLine(3, Config.PLAYER_FORMAT_4.toFormattedString());
            }
        });
    }

    private boolean signHasShop(String[] lines) {
        for (String line : lines) {
            if (Config.PLAYER_CREATE_FORMAT.toStringList().contains(line)) {
                return true;
            }
        }
        return false;
    }

    private boolean signHasAdminShop(String[] lines) {
        for (String line : lines) {
            if (Config.ADMIN_CREATE_FORMAT.toStringList().contains(line)) {
                return true;
            }
        }
        return false;
    }
}
