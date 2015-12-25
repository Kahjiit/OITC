package me.kahjiit.OITC;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.kahjiit.OITC.arena.Arena;
import me.kahjiit.OITC.arena.Manager;
import me.kahjiit.OITC.listeners.GameListener;
import me.kahjiit.OITC.listeners.SignListener;
import me.kahjiit.OITC.misc.Commands;
import me.kahjiit.OITC.misc.Settings;

public class OITC extends JavaPlugin {

	public GameListener gl = new GameListener();
	public SignListener sl = new SignListener();
	
	public static String prefix = "[" + ChatColor.GOLD + ChatColor.BOLD.toString() + "OITC" + ChatColor.RESET + "] " + ChatColor.GRAY;
	
	public void onEnable() {
		Settings.getInstance().init(this);
		
		getServer().getPluginManager().registerEvents(gl, this);
		getServer().getPluginManager().registerEvents(sl, this);
		
		getCommand("oitc").setExecutor(new Commands());
	}
	
	public void onDisable() {
		for (Arena arena : Manager.getArenas()) {
			arena.stop();
		}
		
		Settings.getInstance().save();
	}
	
	public static Plugin getPlugin() {
		return Bukkit.getServer().getPluginManager().getPlugin("OITC");
	}
}	
