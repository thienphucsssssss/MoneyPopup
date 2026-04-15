package com.starg.moneypopup;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MoneyPopup extends JavaPlugin implements CommandExecutor {

    public static final String POPUP_TAG = "moneypopup";

    private PopupManager popupManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.popupManager = new PopupManager(this);
        cleanupOrphanedArmorStands();

        registerCommand();
        registerListeners();
    }

    @Override
    public void onDisable() {
        if (popupManager != null) {
            popupManager.cleanup();
        }
    }

    public PopupManager getPopupManager() {
        return popupManager;
    }

    private void registerCommand() {
        PluginCommand command = getCommand("moneypopup");
        if (command == null) {
            getLogger().warning("Command /moneypopup was not found in plugin.yml.");
            return;
        }

        command.setExecutor(this);
    }

    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();

        if (isPluginEnabled("Vault") && isPluginEnabled("Essentials")) {
            pluginManager.registerEvents(new VaultListener(this, popupManager), this);
        } else {
            getLogger().warning("Vault listener was skipped because Vault or Essentials is missing.");
        }

        if (getConfig().getBoolean("playerpoints.enabled", true) && isPluginEnabled("PlayerPoints")) {
            pluginManager.registerEvents(new PointsListener(this, popupManager), this);
        } else {
            getLogger().info("PlayerPoints listener was skipped.");
        }
    }

    private boolean isPluginEnabled(String name) {
        return getServer().getPluginManager().isPluginEnabled(name);
    }

    private void cleanupOrphanedArmorStands() {
        int removed = 0;
        for (World world : getServer().getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(ArmorStand.class)) {
                if (entity.getScoreboardTags().contains(POPUP_TAG)) {
                    entity.remove();
                    removed++;
                }
            }
        }

        if (removed > 0) {
            getLogger().info("Removed " + removed + " leftover popup armor stand(s).");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("moneypopup.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&cBan khong co quyen su dung lenh nay."));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            popupManager.reloadSettings();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&aMoneyPopup da duoc reload."));
            return true;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&eSu dung: /" + label + " reload"));
        return true;
    }
}
