package com.starg.moneypopup;

import java.util.UUID;
import org.black_ixx.playerpoints.event.PlayerPointsChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class PointsListener implements Listener {

    private final MoneyPopup plugin;
    private final PopupManager popupManager;

    public PointsListener(MoneyPopup plugin, PopupManager popupManager) {
        this.plugin = plugin;
        this.popupManager = popupManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPointsChange(PlayerPointsChangeEvent event) {
        if (!plugin.getConfig().getBoolean("playerpoints.enabled", true)) {
            return;
        }

        UUID playerId = event.getPlayerId();
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            return;
        }

        int change = event.getChange();
        int minAmount = plugin.getConfig().getInt("playerpoints.min_amount", 1);

        if (Math.abs(change) < minAmount) {
            return;
        }

        if (change > 0 && plugin.getConfig().getBoolean("playerpoints.show_income", true)) {
            popupManager.queuePointsPopup(player, change, true);
        } else if (change < 0 && plugin.getConfig().getBoolean("playerpoints.show_expense", false)) {
            popupManager.queuePointsPopup(player, Math.abs(change), false);
        }
    }
}
