package dev.ipoleksenko.PocketHome.GUI;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class settingsGUI extends GUI {
    public final Inventory settings = Bukkit.createInventory(null, 9, "PocketHome");

    public void initializeItems() {
        settings.setItem(1, createGuiItem(Material.IRON_DOOR, "Request access to the house", "Request access to someone else's home"));
        settings.setItem(3, createGuiItem(Material.LEVER, "Access to the house", "Grant access to home visits"));
        settings.setItem(8, createGuiItem(Material.STICK, "Back", "Back to main menu"));
    }

    public void OpenGUI(final @NotNull Player player) {
        initializeItems();
        initializeEmptyItems(settings);
        player.openInventory(settings);
    }
}
