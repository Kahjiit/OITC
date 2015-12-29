package me.kahjiit.OITC.arena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import me.kahjiit.OITC.OITC;
import me.kahjiit.OITC.misc.Settings;

public class Arena {

	OITC plugin;
	private String name;
	private int countdown, id, timer;
	private int index = 1;
	private State state = State.WAITING;
	private Map<Player, PlayerStat> players = new HashMap<Player, PlayerStat>();
	private Map<Player, ItemStack[]> armor = new HashMap<Player, ItemStack[]>();
	private Map<Player, ItemStack[]> inventory = new HashMap<Player, ItemStack[]>();
	
	public Arena(String name) {
		this.name = name;
		this.plugin = (OITC) OITC.getPlugin();
	}
	
	public String getName() {
		return name;
	}
	
	public List<Player> getPlayers() {
		List<Player> list = new ArrayList<Player>();
		for (Player player : players.keySet()) {
			list.add(player);
		}
		return list;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}
	
	public void addPlayer(Player player) {
		if (!players.containsKey(player)) {
			players.put(player, new PlayerStat());
			Manager.addPlayer(player, this);
			
			saveInventory(player);
			player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			player.teleport(Settings.getInstance().getLocation(name + ".Lobby"));
			updateSign();
			if (getPlayers().size() == getAutoStartPlayers() && canStart()) {
				start();
			}
		}
	}
	
	public void removePlayer(Player player) {
		if (players.containsKey(player)) {
			if (getState() == State.INGAME) {
				Settings.getInstance().addPlayerStats(player, this, true);
			}
			player.getInventory().clear();
			loadInventory(player);
			player.teleport(player.getWorld().getSpawnLocation());
			player.setGameMode(GameMode.SURVIVAL);
			OITC.setMainScoreboard(player);
			
			players.remove(player);
			Manager.removePlayer(player);
			updateSign();
			
			if (getState() == State.INGAME || getState() == State.STARTING) {
				if (!hasEnoughPlayers()) {
					Bukkit.getScheduler().cancelTask(id);
					sendArenaMessage(OITC.prefix + ChatColor.AQUA + "The game has been cancelled because too many players have left!");
					stop();
				}
			}
		}
	}
	
	public void loadInventory(Player player) {
		player.getInventory().clear();
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
		if (getState() == State.WAITING && players.size() >= getAutoStartPlayers() && !isRunning()) {
			if (!isRunning()) {
				return true;
			}
		}
		return false;
	}
	
	public String formatTime() {
		String seconds;
		String minute = timer / 60 + "";
		if (timer % 60 < 10) {
			seconds = "0" + timer % 60;
		}
		else {
			seconds = timer % 60 + "";
		}
		String time = minute + ":" + seconds;
		return time;
	}
	
	public int getAutoStartPlayers() {
		return Settings.getInstance().config.getInt("Arenas." + name + ".AutoStartPlayers");
	}
	
	public int getKillsToWin() {
		return Settings.getInstance().config.getInt("Arenas," + name + ".KillsToWin");
	}
	
	public int getMaxPlayers() {
		return Settings.getInstance().config.getInt("Arenas." + name + ".MaxPlayers");
	}
	
	public Location getNextSpawn() {
		if (Settings.getInstance().arenas.contains(name + ".Spawns.Spawn" + index)) {
			Location location = Settings.getInstance().getLocation(name + ".Spawns.Spawn" + index);
			index ++;
			if (index > getMaxPlayers()) {
				index = 1;
			}
			return location;
		}
		return null;
	}
	
	public Scoreboard getScoreboard() {
		Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective objective = sb.registerNewObjective(ChatColor.GOLD + "OITC " + formatTime(), "oitc");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		for (Player player : getPlayers()) {
			if (player != null) {
				if (player.hasPermission("oitc.perks")) {
					objective.getScore(ChatColor.GREEN + player.getName()).setScore(getStats(player).getKills());
				}
				else {
					objective.getScore(player.getName()).setScore(getStats(player).getKills());
				}
			}
		}
		return sb;
	}
	
	public PlayerStat getStats(Player player) {
		if (players.containsKey(player)) {
			return players.get(player);
		}
		return null;
	}
	
	public int getTime() {
		return Settings.getInstance().config.getInt("Arenas." + name + ".Time");
	}
	
	public boolean hasEnoughPlayers() {
		if (players.size() >= getAutoStartPlayers()) {
			return true;
		}
		return false;
	}
	
	public void healPlayers() {
		for (Player player : getPlayers()) {
			if (player != null) {
				player.setHealth(20);
				player.setFoodLevel(20);
			}
		}
	}
	
	public boolean isRunning() {
		if (getState() != State.WAITING && Bukkit.getScheduler().isCurrentlyRunning(id)) {
			return true;
		}
		return false;
	}
	
	//This method might cause lag on the server. Remove if causing errors
	public void removeArrows(World world) {
		for (Entity entity : world.getEntities()) {
			if (entity instanceof Arrow) {
				Arrow arrow = (Arrow) entity;
				if (arrow.isOnGround() || arrow.isInsideVehicle()) {
					arrow.remove();
				}
			}
		}
	}
	
	public void sendArenaMessage(String message) {
		for (Player player : getPlayers()) {
			if (player != null) {
				player.sendMessage(message);
			}
		}
	}
	
	public void setDefaultInventory(Player player) {
		ItemStack bow = new ItemStack(Material.BOW, 1);
		ItemStack sword = new ItemStack(Material.WOOD_SWORD, 1);
		ItemStack arrow = new ItemStack(Material.ARROW, 1);
		
		if (player.hasPermission("oitc.perks")) {
			bow.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
			sword.addEnchantment(Enchantment.LOOT_BONUS_MOBS, 1);
		}
		
		player.getInventory().addItem(bow);
		player.getInventory().addItem(sword);
		player.getInventory().addItem(arrow);
	}
	
	public void setGamemodes(GameMode gm) {
		for (Player player : getPlayers()) {
			if (player != null) {
				player.setGameMode(gm);
			}
		}
	}
	
	public void setInventories() {
		for (Player player : getPlayers()) {
			if (player != null) {
				setDefaultInventory(player);
			}
		}
	}
	
	public void setScoreboards(Scoreboard scoreboard) {
		for (Player player : getPlayers()) {
			if (player != null) {
				player.setScoreboard(scoreboard);
			}
		}
	}
	
	public void spawnPlayers() {
		for (Player player : getPlayers()) {
			if (player != null) {
				player.teleport(getNextSpawn());
			}
		}
	}
	
	public void start() {
		setGamemodes(GameMode.ADVENTURE);
		countdown = Settings.getInstance().config.getInt("Arenas." + name + ".Countdown");
		id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				if (countdown > 0) {
					updateSign();
					if (countdown % 10 == 0 || countdown <= 5) {
						sendArenaMessage(OITC.prefix + "You will be teleported in " + countdown + " seconds.");
					}
					if (!hasEnoughPlayers()) {
						sendArenaMessage(OITC.prefix + ChatColor.AQUA + "The countdown has been cancelled due to the low amount of players!");
						stop();
					}
				}
				else {
					Bukkit.getScheduler().cancelTask(id);
					
					setState(State.STARTING);
					setInventories();
					spawnPlayers();
					updateSign();
					sendArenaMessage(OITC.prefix + "The game will start in 10 seconds.");
					
					//Remove this if you want players to be able to move right away
					id = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							setState(State.INGAME);
							sendArenaMessage(OITC.prefix + "Eliminate other players.");
							
							setScoreboards(getScoreboard());
							healPlayers();
							updateSign();
							startTimer();
						}
					}, 200L);
				}
				countdown --;
			}
		}, 0L, 20L);
	}
	
	public void startTimer() {
		timer = getTime();
		id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				if (timer > 0) {
					if (!hasEnoughPlayers()) {
						sendArenaMessage(OITC.prefix + ChatColor.AQUA + "The game has ended because too many players have left!");
						stop();
					}
					timer --;
					removeArrows(Bukkit.getWorld(Settings.getInstance().arenas.getString(name + ".Lobby.World")));
					setScoreboards(getScoreboard());
				}
				else {
					sendArenaMessage(OITC.prefix + ChatColor.AQUA + "Time is up! The game will be resetted.");
					stop();
				}
			}
		}, 0L, 20L);
	}
	
	public void stop() {
		Bukkit.getScheduler().cancelTask(id);
		if (getState() == State.WAITING || getState() == State.STARTING) {
			for (Player player : getPlayers()) {
				Bukkit.getScheduler().cancelTask(id);
				player.getInventory().clear();
				removePlayer(player);
				addPlayer(player);
				if (getState() == State.STARTING) {
					setState(State.WAITING);
				}
			}
			return;
		}
		setState(State.RESTARTING);
		setGamemodes(GameMode.SURVIVAL);
		updateSign();
		healPlayers();
		for (Player player : getPlayers()) {
			if (player != null) {
				Settings.getInstance().addPlayerStats(player, this, true);
				player.teleport(player.getWorld().getSpawnLocation());
				OITC.setMainScoreboard(player);
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
