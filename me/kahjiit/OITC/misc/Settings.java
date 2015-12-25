package me.kahjiit.OITC.misc;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import me.kahjiit.OITC.arena.Arena;
import me.kahjiit.OITC.arena.Manager;

public class Settings {

	private Settings() { }
	
	private static Settings instance = new Settings();
	
	public static Settings getInstance() {
		return instance;
	}
	
	public File arenasFile, configFile;
	public FileConfiguration arenas, config;
	
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
	
	public void init(Plugin plugin) {
		arenasFile = new File(plugin.getDataFolder(), "arenas.yml");
		configFile = new File(plugin.getDataFolder(), "config.yml");
		
		arenas = new YamlConfiguration();
		config = new YamlConfiguration();
		
		arenas.options().copyDefaults(true);
		config.options().copyDefaults(true);
		
		load();
	}
	
	public void load() {
		try {
			arenas.load(arenasFile);
			config.load(configFile);
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
		}
		catch (IOException e) {
			e.printStackTrace();
		}
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
}
