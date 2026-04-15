package com.starg.moneypopup;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class PopupManager {

    private static final DecimalFormat VAULT_FORMAT =
            new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(Locale.US));

    private final MoneyPopup plugin;
    private final ConcurrentMap<PopupKey, PendingPopup> pendingPopups = new ConcurrentHashMap<>();
    private final Map<UUID, ArmorStand> activePopups = new ConcurrentHashMap<>();

    private int durationTicks;
    private double floatHeight;
    private double offsetY;
    private long mergeWindowTicks;

    public PopupManager(MoneyPopup plugin) {
        this.plugin = plugin;
        reloadSettings();
    }

    public void reloadSettings() {
        ConfigurationSection display = plugin.getConfig().getConfigurationSection("display");
        this.durationTicks = display != null ? display.getInt("duration_ticks", 40) : 40;
        this.floatHeight = display != null ? display.getDouble("float_height", 1.5D) : 1.5D;
        this.offsetY = display != null ? display.getDouble("offset_y", 2.0D) : 2.0D;
        this.mergeWindowTicks = display != null ? display.getLong("merge_window_ticks", 10L) : 10L;
    }

    public void queueVaultPopup(Player player, BigDecimal amount, boolean income) {
        queuePopup(player, PopupChannel.VAULT, amount, income);
    }

    public void queuePointsPopup(Player player, int amount, boolean income) {
        queuePopup(player, PopupChannel.PLAYER_POINTS, BigDecimal.valueOf(amount), income);
    }

    public void showPopup(Player player, String text) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> showPopup(player, text));
            return;
        }

        if (!player.isOnline()) {
            return;
        }

        Location spawnLocation = player.getLocation().add(0.0D, offsetY, 0.0D);
        ArmorStand armorStand = player.getWorld().spawn(spawnLocation, ArmorStand.class, stand -> {
            stand.setInvisible(true);
            stand.setCustomNameVisible(true);
            stand.setMarker(true);
            stand.setGravity(false);
            stand.setSmall(true);
            stand.setCustomName(text);
            stand.addScoreboardTag(MoneyPopup.POPUP_TAG);
            stand.setMetadata(MoneyPopup.POPUP_TAG, new FixedMetadataValue(plugin, true));
        });

        activePopups.put(armorStand.getUniqueId(), armorStand);

        double stepY = durationTicks <= 0 ? 0.0D : floatHeight / durationTicks;

        new BukkitRunnable() {
            private int ticksLived;

            @Override
            public void run() {
                if (!armorStand.isValid()) {
                    activePopups.remove(armorStand.getUniqueId());
                    cancel();
                    return;
                }

                if (ticksLived++ >= durationTicks) {
                    armorStand.remove();
                    activePopups.remove(armorStand.getUniqueId());
                    cancel();
                    return;
                }

                Location current = armorStand.getLocation();
                armorStand.teleport(current.add(0.0D, stepY, 0.0D));
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    public void cleanup() {
        for (PendingPopup pendingPopup : pendingPopups.values()) {
            pendingPopup.cancelTask();
        }
        pendingPopups.clear();

        for (UUID entityId : activePopups.keySet()) {
            Entity entity = Bukkit.getEntity(entityId);
            if (entity != null) {
                entity.remove();
            }
        }
        activePopups.clear();
    }

    private void queuePopup(Player player, PopupChannel channel, BigDecimal amount, boolean income) {
        if (mergeWindowTicks <= 0) {
            String text = formatText(channel, amount, income);
            showPopup(player, text);
            return;
        }

        PopupKey key = new PopupKey(player.getUniqueId(), channel, income);

        pendingPopups.compute(key, (ignored, existing) -> {
            if (existing == null) {
                PendingPopup created = new PendingPopup(player, channel, income, amount);
                created.scheduleFlush();
                return created;
            }

            existing.addAmount(amount);
            return existing;
        });
    }

    private void flushPopup(PopupKey key) {
        PendingPopup pending = pendingPopups.remove(key);
        if (pending == null) {
            return;
        }

        Player player = Bukkit.getPlayer(pending.playerId());
        if (player == null || !player.isOnline()) {
            return;
        }

        String text = formatText(pending.channel(), pending.totalAmount(), pending.income());
        showPopup(player, text);
    }

    private String formatText(PopupChannel channel, BigDecimal amount, boolean income) {
        String root = channel == PopupChannel.VAULT ? "vault" : "playerpoints";
        String formatPath = income ? "income_format" : "expense_format";
        String template = plugin.getConfig().getString(root + "." + formatPath, income ? "&a+%amount%" : "&c-%amount%");

        String defaultCurrency = channel == PopupChannel.VAULT ? "$" : "Points";
        String currencyName = plugin.getConfig().getString(root + ".currency_name", defaultCurrency);

        String amountText = channel == PopupChannel.VAULT
                ? VAULT_FORMAT.format(amount)
                : Integer.toString(amount.intValue());

        return ChatColor.translateAlternateColorCodes('&',
                template.replace("%amount%", amountText).replace("%currency%", currencyName));
    }

    private enum PopupChannel {
        VAULT,
        PLAYER_POINTS
    }

    private record PopupKey(UUID playerId, PopupChannel channel, boolean income) {
    }

    private final class PendingPopup {
        private final UUID playerId;
        private final PopupChannel channel;
        private final boolean income;

        private BigDecimal totalAmount;
        private BukkitTask flushTask;

        private PendingPopup(Player player, PopupChannel channel, boolean income, BigDecimal totalAmount) {
            this.playerId = player.getUniqueId();
            this.channel = channel;
            this.income = income;
            this.totalAmount = totalAmount;
        }

        private UUID playerId() {
            return playerId;
        }

        private PopupChannel channel() {
            return channel;
        }

        private boolean income() {
            return income;
        }

        private synchronized BigDecimal totalAmount() {
            return totalAmount;
        }

        private synchronized void addAmount(BigDecimal amount) {
            totalAmount = totalAmount.add(amount);
        }

        private void scheduleFlush() {
            this.flushTask = Bukkit.getScheduler().runTaskLater(plugin, () -> flushPopup(
                    new PopupKey(playerId, channel, income)
            ), mergeWindowTicks);
        }

        private void cancelTask() {
            if (flushTask != null) {
                flushTask.cancel();
            }
        }
    }
}
