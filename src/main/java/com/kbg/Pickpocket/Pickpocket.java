package com.kbg.pickpocket;

import java.util.stream.IntStream;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Pickpocket extends org.bukkit.plugin.java.JavaPlugin implements org.bukkit.event.Listener {
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
    }
    @org.bukkit.event.EventHandler
    public void onRightClickPlayer(org.bukkit.event.player.PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player && !isPlayerInCone(event.getPlayer(), (Player) event.getRightClicked(), (event.getPlayer().isSneaking() ? 70 : 90), 5)) //clicking player isn't visible to clicked
                event.getPlayer().openInventory(scramble(event.getPlayer(), (Player) event.getRightClicked()));         
    }
    private static boolean isPlayerInCone(Player p, Player clicked, double coneAngle, double radius) {
        final double coneArea = Math.tan(coneAngle) * Math.tan(coneAngle), radiusSquared = radius * radius;
        org.bukkit.util.Vector n = p.getLocation().toVector().subtract(clicked.getLocation().toVector()).normalize();
        return (clicked.getLocation().getDirection().normalize().crossProduct(n).lengthSquared() <= coneArea // within cone
                && clicked.getLocation().distanceSquared(p.getLocation()) <= radiusSquared // within radius
                && clicked.getLocation().getDirection().dot(n) >= 0); // same direction     
    }
    private static Inventory scramble(Player player, Player clicked) {
        Inventory scrambled = org.bukkit.Bukkit.createInventory(clicked, player.getInventory().getSize(), "Pickpocket");
        IntStream.range(0, clicked.getInventory().getContents().length).filter(i -> clicked.getInventory().getContents()[i] != null).forEach(i -> {
            ItemStack item = new ItemStack(clicked.getInventory().getContents()[i].getType(), clicked.getInventory().getContents()[i].getAmount());
            ItemMeta im = item.getItemMeta();
            im.setDisplayName(org.bukkit.ChatColor.MAGIC + im.getDisplayName());
            item.setItemMeta(im);
            item.setType(org.bukkit.Material.PAPER);
            scrambled.setItem(i, item);
        });
        return scrambled;
    }
    @org.bukkit.event.EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && event.getClickedInventory().getName().equals("Pickpocket")) {
            ((Player) event.getWhoClicked()).getInventory().addItem(((Player) event.getInventory().getHolder()).getInventory().getItem(event.getSlot()));
            ((Player) event.getInventory().getHolder()).getInventory().clear(event.getSlot());
            ((Player) event.getWhoClicked()).closeInventory();
            event.setCancelled(true);
        }
    }
}
