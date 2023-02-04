package dev.ipoleksenko.PocketHome.GUI;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GUI{
    public Inventory inv;
    public GUI()
    {
        inv = Bukkit.createInventory(null, 9, "GUI Name");
        inv.setItem(0, new ItemStack(Material.DIAMOND,1));
    }

    public void OpenGUI(Player player)
    {
        player.openInventory(inv);
    }
};

