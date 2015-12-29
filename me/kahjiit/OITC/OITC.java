package me.kahjiit.OITC;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import me.kahjiit.OITC.arena.Arena;
import me.kahjiit.OITC.arena.Manager;
import me.kahjiit.OITC.listeners.GameListener;
import me.kahjiit.OITC.listeners.SignListener;
import me.kahjiit.OITC.misc.Commands;
import me.kahjiit.OITC.misc.Settings;

public class OITC extends JavaPlugin {

	public GameListener gl = new GameListener();
	public SignListener sl = new SignListener();
	
	public static String prefix = "[" + ChatColor.GOLD + "OITC" + ChatColor.RESET + "] " + ChatColor.GRAY;
	
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
	
	public static void setMainScoreboard(Player player) {
		Scoreboard msb = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective objective = msb.registerNewObjective(ChatColor.GOLD + "OITC Lobby", "oitc");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.getScore(ChatColor.YELLOW + "Kills: " + ChatColor.WHITE + Settings.getInstance().getStat(player, "Kills")).setScore(5);
		objective.getScore(ChatColor.YELLOW + "Deaths: " + ChatColor.WHITE + Settings.getInstance().getStat(player, "Deaths")).setScore(4);
		objective.getScore(ChatColor.GOLD + "KDR: " + ChatColor.WHITE + Settings.getInstance().getKDR(player)).setScore(3);
		objective.getScore(ChatColor.YELLOW + "Games: " + ChatColor.WHITE + Settings.getInstance().getStat(player, "Games")).setScore(2);
		objective.getScore(ChatColor.YELLOW + "Wins: " + ChatColor.WHITE + Settings.getInstance().getStat(player, "Wins")).setScore(1);
		objective.getScore(ChatColor.GOLD + "WGR: " + ChatColor.WHITE + Settings.getInstance().getWGR(player)).setScore(0);
		
		player.setScoreboard(msb);
	}
}	
