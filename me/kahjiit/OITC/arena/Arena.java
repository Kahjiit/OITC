package me.kahjiit.OITC.arena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.kahjiit.OITC.OITC;
import me.kahjiit.OITC.misc.Settings;

public class Arena {

	OITC plugin;
	private String name;
	private int countdown, id;
	private State state = State.WAITING;
	private List<Player> players = new ArrayList<Player>();
	private HashMap<Player, ItemStack[]> armor = new HashMap<Player, ItemStack[]>();
	private HashMap<Player, ItemStack[]> inventory = new HashMap<Player, ItemStack[]>();
	
	public Arena(String name) {
		this.name = name;
		this.plugin = (OITC) OITC.getPlugin();
	}
	
	public String getName() {
		return name;
	}
	
	public List<Player> getPlayers() {
		return players;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}
	
	public void addPlayer(Player player) {
		if (!players.contains(player)) {
			players.add(player);
			Manager.addPlayer(player, this);
			
			saveInventory(player);
			player.teleport(Settings.getInstance().getLocation(name + ".Lobby"));
			updateSign();
			if (canStart()) {
				start();
			}
		}
	}
	
	public void removePlayer(Player player) {
		if (players.contains(player)) {
			players.remove(player);
			Manager.removePlayer(player);
			
			player.getInventory().clear();
			loadInventory(player);
			player.teleport(player.getWorld().getSpawnLocation());
			updateSign();
		}
	}
	
	public void loadInventory(Player player) {
		if (armor.containsKey(player)) {
			player.getInventory().setArmorContents((ItemStack[]) armor.get(player));
			armor.remove(player);
		}
		if (inventory.containsKey(player)) {
			player.getInventory().setContents((ItemStack[]) inventory.get(player));
			inventory.remove(player);
		}
	}
	
	public void saveInventory(Player player) {
		armor.put(player, player.getInventory().getArmorContents());
		inventory.put(player, player.getInventory().getContents());

		player.getInventory().setArmorContents(null);
		player.getInventory().clear();
	}
	
	public void addArrow(Player player) {
		ItemStack arrow = new ItemStack(Material.ARROW, 1);
		player.getInventory().addItem(arrow);
	}
	
	public boolean canStart() {
		if (getState() == State.WAITING && players.size() >= getAutoStartPlayers()) {
			return true;
		}
		return false;
	}

	public int getAutoStartPlayers() {
		return Settings.getInstance().config.getInt("Arenas." + name + ".AutoStartPlayers");
	}
	
	public int getMaxPlayers() {
		return Settings.getInstance().config.getInt("Arenas." + name + ".MaxPlayers");
	}
	
	public boolean hasEnoughPlayers() {
		if (players.size() >= getAutoStartPlayers()) {
			return true;
		}
		return false;
	}
	
	public void healPlayers() {
		for (Player player : players) {
			if (player != null) {
				player.setHealth(20);
				player.setFoodLevel(20);
			}
		}
	}
	
	public boolean isRunning() {
		if (getState() != State.WAITING) {
			return true;
		}
		return false;
	}
	
	public void sendArenaMessage(String message) {
		for (Player player : players) {
			if (player != null) {
				player.sendMessage(message);
			}
		}
	}
	
	public void setDefaultInventory(Player player) {
		ItemStack bow = new ItemStack(Material.BOW, 1);
		ItemStack wood_sword = new ItemStack(Material.WOOD_SWORD, 1);
		ItemStack arrow = new ItemStack(Material.ARROW, 1);
		
		player.getInventory().addItem(bow);
		player.getInventory().addItem(wood_sword);
		player.getInventory().addItem(arrow);
	}
	
	public void setInventories() {
		for (Player player : players) {
			if (player != null) {
				setDefaultInventory(player);
			}
		}
	}
	
	public void start() {
		setInventories();
		countdown = Settings.getInstance().config.getInt("Arenas." + name + ".Countdown");
		id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				if (countdown > 0) {
					setState(State.STARTING);
					updateSign();
					if (countdown % 5 == 0 || countdown < 5) {
						sendArenaMessage(OITC.prefix + "The game will start in " + countdown + " seconds!");
					}
					/* if (!hasEnoughPlayers()) {
						sendArenaMessage(OITC.prefix + "The countdown has been cancelled due to the low amount of players!");
						stop();
					} */
				}
				else {
					Bukkit.getScheduler().cancelTask(id);
					setState(State.INGAME);
					sendArenaMessage(OITC.prefix + "Eliminate other players.");
					
					healPlayers();
					updateSign();
				}
				countdown --;
			}
		}, 0L, 20L);
	}
	
	public void stop() {
		if (getState() == State.STARTING) {
			Bukkit.getScheduler().cancelTask(id);
			setState(State.WAITING);
			updateSign();
			for (Player player : players) {
				player.getInventory().clear();
			}
			return;
		}
		setState(State.RESTARTING);
		updateSign();
		healPlayers();
		for (Player player : players) {
			if (player != null) {
				player.teleport(player.getWorld().getSpawnLocation());
				Manager.removePlayer(player);
				loadInventory(player);
			}
		}
		players.clear();
		setState(State.WAITING);
		updateSign();
	}
	
	public void updateSign() {
		if (Settings.getInstance().arenas.contains(name + ".Sign")) {
			if (Settings.getInstance().getLocation(name + ".Sign").getBlock().getState() instanceof Sign) {
				Sign sign = (Sign) Settings.getInstance().getLocation(getName() + ".Sign").getBlock().getState();
				sign.setLine(3, players.size() + "/" + getMaxPlayers());
				if (getState() == State.INGAME) {
					sign.setLine(2, ChatColor.RED + ChatColor.BOLD.toString() + "IN GAME");
				}
				if (getState() == State.RESTARTING) {
					sign.setLine(2, ChatColor.DARK_GRAY + ChatColor.BOLD.toString() + "RESTARTING");
				}
				if (getState() == State.STARTING) {
					sign.setLine(2, ChatColor.GOLD + ChatColor.BOLD.toString() + "STARTING");
				}
				if (getState() == State.WAITING) {
					sign.setLine(2, ChatColor.DARK_GREEN + ChatColor.BOLD.toString() + "WAITING");
				}
				sign.update();
			}
		}
	}
}
