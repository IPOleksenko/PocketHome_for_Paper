package dev.ipoleksenko.PocketHome.GUI;

import dev.ipoleksenko.PocketHome.PocketHomePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GUI implements Listener {
	private final Inventory inv;
	private final Inventory settings;

	public GUI() {
		// Create a new inventory, with no owner (as this isn't a real inventory), a size of nine, called example
		inv = Bukkit.createInventory(null, 9, "PocketHome");
		settings = Bukkit.createInventory(null, 9, "PocketHome");
	}

	// You can call this whenever you want to put the items in
	public void initializeItems(Player player) {
		ItemStack empty = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1 , (byte) 15);
		ItemMeta emptyMeta = empty.getItemMeta();
		emptyMeta.setDisplayName("PocketHome");
		empty.setItemMeta(emptyMeta);
		for (int i = 0; i<10; i+=2)
			inv.setItem(i, empty);

		inv.addItem(createGuiItem(Material.RED_BED,
				"Teleport",
				(!PocketHomePlugin.getInstance().getPocketManager().isInPocket(player) ? "Teleport on home" : "Teleport from home")));
		inv.addItem(createGuiItem(Material.ACACIA_DOOR, "Enter someone else's house", "Enter a house whose owner has allowed entry"));
		inv.addItem(createGuiItem(Material.REDSTONE, "Settings", "Your home settings"));
		inv.addItem(createGuiItem(Material.BOOK, "Help", "Help using PocketHome"));

		settings.addItem(createGuiItem(Material.IRON_DOOR, "Request access to the house", "Request access to someone else's home"));
		settings.addItem(createGuiItem(Material.LEVER, "Access to the house", "Grant access to home visits"));
	}

	// Nice little method to create a gui item with a custom name, and description
	protected ItemStack createGuiItem(final Material material, final String name, final String... lore) {
		final ItemStack item = new ItemStack(material, 1);
		final ItemMeta meta = item.getItemMeta();

		// Set the name of the item
		meta.setDisplayName(name);

		// Set the lore of the item
		meta.setLore(Arrays.asList(lore));

		item.setItemMeta(meta);

		return item;
	}

	// You can open the inventory with this
	public void OpenGUI(final Player player ) {
		initializeItems(player);
		player.openInventory(inv);
	}

	// Check for clicks on items
	@EventHandler
	public void onInventoryClick(final InventoryClickEvent event) {
		if(!event.getView().getTitle().equalsIgnoreCase("PocketHome"))
			return;

		event.setCancelled(true);

		final ItemStack clickedItem = event.getCurrentItem();

		// verify current item is not null
		if (clickedItem == null || clickedItem.getType().isAir()) return;

		final Player player = (Player) event.getWhoClicked();

		// Using slots click is a best option for your inventory click's
		if(event.getRawSlot() == 1)
			if (!PocketHomePlugin.getInstance().getPocketManager().isInPocket(player)) {
				player.sendMessage("Teleport to HOME...");
				PocketHomePlugin.getInstance().getPocketManager().teleportToPocket(player);
			} else {
				player.sendMessage("Teleport from HOME...");
				PocketHomePlugin.getInstance().getPocketManager().teleportFromPocket(player);
			}

		if(event.getRawSlot() == 3)
			player.sendMessage("Hi");

		if(event.getRawSlot() == 5)
			player.sendMessage("Hi");

		if(event.getRawSlot() == 7)
			player.sendMessage("Hi");
	}

	// Cancel dragging in our inventory
	@EventHandler
	public void onInventoryClick(final InventoryDragEvent e) {
		if (e.getInventory().equals(inv)) {
			e.setCancelled(true);
		}
	}
}

