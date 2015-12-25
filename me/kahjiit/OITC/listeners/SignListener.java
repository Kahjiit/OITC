package me.kahjiit.OITC.listeners;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import me.kahjiit.OITC.OITC;
import me.kahjiit.OITC.arena.Arena;
import me.kahjiit.OITC.arena.Manager;
import me.kahjiit.OITC.arena.State;
import me.kahjiit.OITC.misc.Settings;

public class SignListener implements Listener {

	OITC plugin;
	
	public SignListener() {
		this.plugin = (OITC) OITC.getPlugin();
	}
	
	@EventHandler
	public void onSignCreate(SignChangeEvent event) {
		Player player = event.getPlayer();
		if (event.getLine(0).equalsIgnoreCase("oitc") && player.hasPermission("oitc.admin")) {
			for (Arena arena : Manager.getArenas()) {
				if (event.getLine(1).equalsIgnoreCase(arena.getName())) {
					event.setLine(0, ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + ChatColor.BOLD.toString() + "OITC" + ChatColor.DARK_GRAY + "]");
					event.setLine(1, arena.getName());
					if (arena.getState() == State.INGAME) {
						event.setLine(2, ChatColor.RED + ChatColor.BOLD.toString() + "IN GAME");
					}
					if (arena.getState() == State.RESTARTING) {
						event.setLine(2, ChatColor.DARK_GRAY + ChatColor.BOLD.toString() + "RESTARTING");
					}
					if (arena.getState() == State.STARTING) {
						event.setLine(2, ChatColor.GOLD + ChatColor.BOLD.toString() + "STARTING");
					}
					if (arena.getState() == State.WAITING) {
						event.setLine(2, ChatColor.DARK_GREEN + ChatColor.BOLD.toString() + "WAITING");
					}
					event.setLine(3, arena.getPlayers().size() + "/" + arena.getMaxPlayers());
					Settings.getInstance().setLocation(arena.getName() + ".Sign", event.getBlock().getLocation());
					player.sendMessage(OITC.prefix + "You have created the join sign for the arena: " + event.getLine(1) + ".");
				}
			}
		}
	}
	
	@EventHandler
	public void onSignInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (event.getClickedBlock().getState() instanceof Sign) {
				Sign sign = (Sign) event.getClickedBlock().getState();
				if (sign.getLine(0).contains("OITC")) {
					if (Manager.isPlaying(player)) {
						player.sendMessage(OITC.prefix + "You are already playing.");
						return;
					}	
					for (Arena arena : Manager.getArenas()) {
						if (sign.getLine(1).equalsIgnoreCase(arena.getName())) {
							if (arena.getState() != State.WAITING) {
								player.sendMessage(OITC.prefix + "You can't join that arena as it isn't joinable now!");
								return;
							}
							if (arena.getPlayers().size() < arena.getMaxPlayers()) {
								arena.addPlayer(player);
								player.sendMessage(OITC.prefix + "You have joined the arena: " + arena.getName() + "!");
							}
							else {
								player.sendMessage(OITC.prefix + "That arena is full! Try again later.");
							}
						}
					}
				}
			}
		}
	}
}
