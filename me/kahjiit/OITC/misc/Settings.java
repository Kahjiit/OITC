package me.kahjiit.OITC.misc;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import me.kahjiit.OITC.arena.Arena;
import me.kahjiit.OITC.arena.Manager;

//Anything file-related here
public class Settings {

	private Settings() { }
	
	private static Settings instance = new Settings();
	
	public static Settings getInstance() {
		return instance;
	}
	
	public File arenasFile, configFile, playersFile;
	public FileConfiguration arenas, config, players;
	
	public void init(Plugin plugin) {
		arenasFile = new File(plugin.getDataFolder(), "arenas.yml");
		configFile = new File(plugin.getDataFolder(), "config.yml");
		playersFile = new File(plugin.getDataFolder(), "players.yml");
		
		arenas = new YamlConfiguration();
		config = new YamlConfiguration();
		players = new YamlConfiguration();
		
		arenas.options().copyDefaults(true);
		config.options().copyDefaults(true);
		players.options().copyDefaults(true);
		
		load();
	}
	
	public void load() {
		try {
			arenas.load(arenasFile);
			config.load(configFile);
			players.load(playersFile);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			for (String string : config.getConfigurationSection("Arenas").getKeys(false)) {
				Arena arena = new Arena(string);
				Bukkit.getLogger().info("[OITC] Loading the arena: " + arena.getName() + ".");
				Manager.addArena(arena);
				arena.updateSign();
				
				Bukkit.getLogger().info("[OITC] Succesfully loaded the arena: " + arena.getName() + ".");
			}
			Bukkit.getLogger().info("[OITC] Finished initializing arenas.");
		}
		catch (Exception e) {
			Bukkit.getLogger().warning("[OITC] Failed to load arenas!");
		}
	}
	
	public void save() {
		try {
			arenas.save(arenasFile);
			config.save(configFile);
			players.save(playersFile);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addPlayerStats(Player player, Arena arena, boolean countGame) {
		int kills = arena.getStats(player).getKills();
		int deaths = arena.getStats(player).getDeaths();
		players.set(player.getUniqueId() + ".Kills", players.getInt(player.getUniqueId() + ".Kills") + kills);
		players.set(player.getUniqueId() + ".Deaths", players.getInt(player.getUniqueId() + ".Deaths") + deaths);
		if (countGame) {
			players.set(player.getUniqueId() + ".Games", players.getInt(player.getUniqueId() + ".Games") + 1);
			if (kills >= 20) {
				players.set(player.getUniqueId() + ".Wins", players.getInt(player.getUniqueId() + ".Wins") + 1);
			}
		}
		save();
	}
	
	public String getWGR(Player player) {
		double games = players.getInt(player.getUniqueId() + ".Games");
		double wins = players.getInt(player.getUniqueId() + ".Wins");
		DecimalFormat df = new DecimalFormat("0.00");
		if (games == 0.0) {
			return "0%";
		}
		if (games > 0.0 && wins == 0.0) {
			return "0%";
		}
		if (games == 0.0 && wins > 0.0) {
			return "hacker";
		}
		if (games == wins) {
			return "100%";
		}
		double wgr = wins / games * 100;
		return df.format(wgr) + "%";
	}
	
	public String getKDR(Player player) {
		double kills = players.getInt(player.getUniqueId() + ".Kills");
		double deaths = players.getInt(player.getUniqueId() + ".Deaths");
		DecimalFormat df = new DecimalFormat("0.00");
		if (kills == 0.0 && deaths == 0.0) {
			return "0.00";
		}
		if (kills > 0.0 && deaths == 0.0) {
			return df.format(kills);
		}
		if (deaths > 0.0 && kills == 0.0) {
			return "0.00";
		}
		double kdr = kills / deaths;
		return df.format(kdr);
	}
	
	public Location getLocation(String path) {
		if (arenas.contains(path + ".X")) {
			double x = arenas.getDouble(path + ".X");
			double y = arenas.getDouble(path + ".Y");
			double z = arenas.getDouble(path + ".Z");
			Location location = new Location(Bukkit.getWorld(arenas.getString(path + ".World")), x, y, z);
			return location;
		}
		return null;
	}
	
	public int getStat(Player player, String stat) {
		return players.getInt(player.getUniqueId() + "." + stat);
	}
	
	public void setLocation(String path, Location location) {
		if (!arenas.contains(path)) {
			arenas.addDefault(path + ".X", location.getX());
			arenas.addDefault(path + ".Y", location.getY());
			arenas.addDefault(path + ".Z", location.getZ());
			arenas.addDefault(path + ".World", location.getWorld().getName());
		}
		else {
			arenas.set(path + ".X", location.getX());
			arenas.set(path + ".Y", location.getY());
			arenas.set(path + ".Z", location.getZ());
			arenas.set(path + ".World", location.getWorld().getName());
		}
		save();
	}
	
	public void setPlayerStats(Player player) {
		if (!players.contains(player.getUniqueId() + ".Kills")) {
			players.addDefault(player.getUniqueId() + ".Kills", 0);
			players.addDefault(player.getUniqueId() + ".Deaths", 0);
			players.addDefault(player.getUniqueId() + ".Games", 0);
			players.addDefault(player.getUniqueId() + ".Wins", 0);
			save();
		}
	}
}
