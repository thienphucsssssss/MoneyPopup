package com.starg.moneypopup;

import java.math.BigDecimal;
import java.util.UUID;
import net.ess3.api.events.UserBalanceUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class VaultListener implements Listener {

    private final MoneyPopup plugin;
    private final PopupManager popupManager;

    public VaultListener(MoneyPopup plugin, PopupManager popupManager) {
        this.plugin = plugin;
        this.popupManager = popupManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBalanceUpdate(UserBalanceUpdateEvent event) {
        if (!plugin.getConfig().getBoolean("vault.enabled", true)) {
            return;
        }

        UUID playerId = event.getPlayer().getUniqueId();
        BigDecimal oldBalance = event.getOldBalance();
        BigDecimal newBalance = event.getNewBalance();
        BigDecimal diff = newBalance.subtract(oldBalance);
        BigDecimal minAmount = BigDecimal.valueOf(plugin.getConfig().getDouble("vault.min_amount", 1.0D));

        if (diff.abs().compareTo(minAmount) < 0) {
            return;
        }

        boolean income = diff.signum() > 0;
        boolean shouldShow = income
                ? plugin.getConfig().getBoolean("vault.show_income", true)
                : plugin.getConfig().getBoolean("vault.show_expense", false);

        if (!shouldShow) {
            return;
        }

        BigDecimal absAmount = diff.abs();
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                return;
            }

            popupManager.queueVaultPopup(player, absAmount, income);
        });
    }
}
