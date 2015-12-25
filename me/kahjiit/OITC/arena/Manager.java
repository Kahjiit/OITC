package me.kahjiit.OITC.arena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

public class Manager {

	private static Map<String, Arena> arenas = new HashMap<String, Arena>();
	private static Map<Player, Arena> players = new HashMap<Player, Arena>();
	
	public static void addArena(Arena arena) {
		if (!arenas.containsKey(arena.getName())) {
			arenas.put(arena.getName(), arena);
		}
	}
	
	public static boolean arenaExists(String name) {
		if (arenas.containsKey(name)) {
			return true;
		}
		return false;
	}
	
	public static Arena getArena(String name) {
		if (arenas.containsKey(name)) {
			return arenas.get(name);
		}
		return null;
	}
	
	public static List<Arena> getArenas() {
		List<Arena> list = new ArrayList<Arena>();
		for (String name : arenas.keySet()) {
			list.add(getArena(name));
		}
		return list;
	}
	
	public static void removeArena(Arena arena) {
		if (arenas.containsKey(arena.getName())) {
			arenas.remove(arena);
		}
	}
	
	public static void addPlayer(Player player, Arena arena) {
		if (!players.containsKey(player)) {
			players.put(player, arena);
		}
	}
	
	public static Arena getArena(Player player) {
		if (players.containsKey(player)) {
			return players.get(player);
		}
		return null;
	}
	
	public static boolean isPlaying(Player player) {
		if (players.containsKey(player)) {
			return true;
		}
		return false;
	}
	
	public static void removePlayer(Player player) {
		if (players.containsKey(player)) {
			players.remove(player);
		}
	}
}
