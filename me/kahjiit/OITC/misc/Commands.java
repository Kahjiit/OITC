package me.kahjiit.OITC.misc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.kahjiit.OITC.OITC;
import me.kahjiit.OITC.arena.Arena;
import me.kahjiit.OITC.arena.Manager;
import me.kahjiit.OITC.arena.State;

public class Commands implements CommandExecutor {
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("oitc")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Only players may use this command.");
				return true;
			}
			Player player = (Player) sender;
			if (args.length == 0) {
				player.sendMessage(ChatColor.GOLD + ChatColor.STRIKETHROUGH.toString() + "---" + ChatColor.RESET + ChatColor.GOLD + ChatColor.BOLD.toString() + " OITC Help " + ChatColor.RESET + ChatColor.GOLD + ChatColor.STRIKETHROUGH.toString() + "---");
				player.sendMessage(ChatColor.YELLOW + "/oitc join <arena>: " + ChatColor.GRAY + "Joins the selected arena.");
				player.sendMessage(ChatColor.YELLOW + "/oitc leave: " + ChatColor.GRAY + "Leaves your current arena.");
				player.sendMessage(ChatColor.YELLOW + "/oitc version: " + ChatColor.GRAY + "Shows the current version of " + ChatColor.GOLD + "OITC" + ChatColor.GRAY + ".");
				if (player.hasPermission("oitc.admin")) {
					player.sendMessage(ChatColor.GOLD + ChatColor.STRIKETHROUGH.toString() + "---" + ChatColor.RESET + ChatColor.GOLD + ChatColor.BOLD.toString() + " Admin Commands " + ChatColor.RESET + ChatColor.GOLD + ChatColor.STRIKETHROUGH.toString() + "---");
					player.sendMessage(ChatColor.YELLOW + "/oitc addspawn <arena>: " + ChatColor.GRAY + "Adds a spawn to a certain arena.");
					player.sendMessage(ChatColor.YELLOW + "/oitc create <arena>: " + ChatColor.GRAY + "Creates a new arena.");
					player.sendMessage(ChatColor.YELLOW + "/oitc reload: " + ChatColor.GRAY + "Reloads all configuration files.");
					player.sendMessage(ChatColor.YELLOW + "/oitc setlobby <arena>: " + ChatColor.GRAY + "Sets the waiting lobby for an arena.");
				}
				player.sendMessage(ChatColor.GOLD + ChatColor.STRIKETHROUGH.toString() + "-----------------------");
			}
			
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("help")) {
					Bukkit.dispatchCommand(player, "oitc");
				}
				if (args[0].equalsIgnoreCase("join")) {
					player.sendMessage(OITC.prefix + "Please specify the arena you want to join!");
				}
				if (args[0].equalsIgnoreCase("leave")) {
					if (!Manager.isPlaying(player)) {
						player.sendMessage(OITC.prefix + "You are not playing.");
						return true;
					}
					Arena arena = Manager.getArena(player);
					arena.removePlayer(player);
					Manager.removePlayer(player);
					player.sendMessage(OITC.prefix + "You have left your current arena.");
				}
				if (args[0].equalsIgnoreCase("version")) {
					player.sendMessage(OITC.prefix + "Running OITC v" + OITC.getPlugin().getDescription().getVersion() + " by Kahjiit");
				}
				if (player.hasPermission("oitc.admin")) {
					if (args[0].equalsIgnoreCase("reload")) {
						Settings.getInstance().load();
						player.sendMessage(OITC.prefix + "Reloaded all configuration files.");
					}
				}
			}
			
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("join")) {
					if (!Manager.arenaExists(args[1])) {
						player.sendMessage(OITC.prefix + "That arena doesn't exist!");
						return true;
					}
					Arena arena = Manager.getArena(args[1]);
					if (arena.getState() != State.WAITING) {
						player.sendMessage(OITC.prefix + "You can't join that arena as it isn't joinable now!");
						return true;
					}
					if (arena.getPlayers().size() < arena.getMaxPlayers()) {
						arena.addPlayer(player);
						player.sendMessage(OITC.prefix + "You have joined the arena: " + arena.getName() + "!");
					}
					else {
						player.sendMessage(OITC.prefix + "That arena is full! Try again later.");
					}
				}
				if (player.hasPermission("oitc.admin")) {
					if (args[0].equalsIgnoreCase("addspawn")) {
						if (Manager.arenaExists(args[1])) {
							Arena arena = Manager.getArena(args[1]);
							for (int index = 1; index <= arena.getMaxPlayers(); index ++) {
								if (!Settings.getInstance().arenas.contains(arena.getName() + ".Spawns.Spawn" + index)) {
									Settings.getInstance().setLocation(arena.getName() + ".Spawns.Spawn" + index, player.getLocation());
									player.sendMessage(OITC.prefix + "You have added a spawn to the arena " + arena.getName() + " with the index of " + index + ".");
									return true;
								}
							}
							player.sendMessage(OITC.prefix + "There are no more spawns needed to be set!");
						}
						else {
							player.sendMessage(OITC.prefix + "That arena doesn't exist!");
						}
					}
					if (args[0].equalsIgnoreCase("create")) {
						if (!Manager.arenaExists(args[1])) {
							Settings.getInstance().config.addDefault("Arenas." + args[1] + ".AutoStartPlayers", 2);
							Settings.getInstance().config.addDefault("Arenas." + args[1] + ".Countdown", 30);
							Settings.getInstance().config.addDefault("Arenas." + args[1] + ".MaxPlayers", 8);
							Arena arena = new Arena(args[1]);
							Manager.addArena(arena);
							Settings.getInstance().save();
							player.sendMessage(OITC.prefix + "You have created the arena " + args[1] + "!");
						}
						else {
							player.sendMessage(OITC.prefix + "That arena already exists!");
						}
					}
					if (args[0].equalsIgnoreCase("setlobby")) {
						if (Manager.arenaExists(args[1])) {
							Settings.getInstance().setLocation(args[1] + ".Lobby", player.getLocation());
							player.sendMessage(OITC.prefix + "You have set the waiting lobby for the arena: " + args[1] + "!");
						}
						else {
							player.sendMessage(OITC.prefix + "That arena doesn't exist!");
						}
					}
					if (args[0].equalsIgnoreCase("start")) {
						if (Manager.arenaExists(args[1])) {
							Manager.getArena(args[1]).start();
						}
					}
				}
			}
		}
 		return true;
	}
}
