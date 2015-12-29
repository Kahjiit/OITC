package me.kahjiit.OITC.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import me.kahjiit.OITC.OITC;
import me.kahjiit.OITC.arena.Arena;
import me.kahjiit.OITC.arena.Manager;
import me.kahjiit.OITC.arena.State;
import me.kahjiit.OITC.misc.Settings;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand.EnumClientCommand;

public class GameListener implements Listener {

	private int kills;
	int id;
	
	@EventHandler
	public void onArrowHit(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();
			if (arrow.getShooter() instanceof Player) {
				Player player = (Player) event.getEntity();
				Player shooter = (Player) arrow.getShooter();
				if (Manager.isPlaying(player) && Manager.isPlaying(shooter)) {
					if (player == shooter) {
						event.setCancelled(true);
						return;
					}
					event.setDamage(30);
					arrow.remove();
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (Manager.isPlaying(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (Manager.isPlaying(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBowShot(ProjectileLaunchEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			Player player = (Player) event.getEntity().getShooter();
			if (Manager.getArena(player).getState() == State.STARTING) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler (priority = EventPriority.LOW)
	public void onChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if (Manager.isPlaying(player)) {
			event.setCancelled(true);
			Manager.getArena(player).sendArenaMessage(OITC.prefix + ChatColor.WHITE + player.getName() + ": " + ChatColor.GRAY + event.getMessage());
		}

	}
	
	@EventHandler
	public void onCommandSend(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		if (player.hasPermission("oitc.admin")) {
			return;
		}
		if (!Manager.isPlaying(player)) {
			return;
		}
		if (Manager.getArena(player).getState() == State.INGAME) {
			if (event.getMessage().startsWith("/oitc") || event.getMessage().startsWith("/msg")) {
				return;
			}
			player.sendMessage(ChatColor.RED + "You may not use commands while playing OITC!");
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		if (Manager.isPlaying(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (Manager.isPlaying(player)) {
				if (Manager.getArena(player).getState() == State.WAITING) {
					event.setCancelled(true);
				}
				if (event.getCause() == DamageCause.FALL) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (Manager.isPlaying(player)) {
			event.getDrops().clear();
			event.setDeathMessage("");
			event.setDroppedExp(0);
			player.getWorld().createExplosion(player.getLocation(), 0F);
			player.getKiller().setHealth(20);
			player.getInventory().clear();
			
			id = Bukkit.getScheduler().scheduleSyncDelayedTask(OITC.getPlugin(), new Runnable() {
				public void run() {
					((CraftPlayer) player).getHandle().playerConnection.a(new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN));
				}
			}, 10L);
			
			if (event.getEntity().getLastDamageCause().getCause() == DamageCause.ENTITY_ATTACK) {
				onPlayerKill(player, player.getKiller(), "stabbed");
			}
			if (event.getEntity().getLastDamageCause().getCause() == DamageCause.PROJECTILE) {
				onPlayerKill(player, player.getKiller(), "shot");
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (Manager.isPlaying(event.getPlayer())) {
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		OITC.setMainScoreboard(player);
		Settings.getInstance().setPlayerStats(player);
		player.sendMessage(OITC.prefix + ChatColor.AQUA + "Please notice OITC is currently in Beta. Bugs will occur more frequently and stats may get wiped.");
	}
	
	public void onPlayerKill(Player player, Player killer, String reason) {
		Arena arena = Manager.getArena(player);
		
		arena.getStats(killer).addKill();
		arena.getStats(killer).addStreak();
		arena.getStats(player).addDeath();
		
		arena.sendArenaMessage(OITC.prefix + ChatColor.RED + player.getName() + quickStats(arena, player) + ChatColor.AQUA + " has been " + reason
				+ " by " + ChatColor.RED + killer.getName() + quickStats(arena, killer) + ChatColor.AQUA + " !");
		
		if (arena.getStats(player).getStreak() > 0) {
			if (arena.getStats(player).getStreak() >= 5) {
				arena.sendArenaMessage(OITC.prefix + ChatColor.RED + killer.getName() + ChatColor.AQUA
						+ " has broken the killstreak of " + ChatColor.RED + player.getName() + ChatColor.AQUA + "!");
			}
			arena.getStats(player).resetStreak();
		}
		if (arena.getStats(killer).getStreak() >= 3) {
			arena.sendArenaMessage(OITC.prefix + ChatColor.RED + killer.getName() + ChatColor.AQUA + " is on a " + ChatColor.GOLD
					+ arena.getStats(killer).getStreak() + ChatColor.AQUA + " killstreak!");
		}
		
		arena.addArrow(killer);
		
		Scoreboard sb = killer.getScoreboard();
		
		if (killer.hasPermission("oitc.perks")) {
			Score score = sb.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.GREEN + killer.getName());
			kills = score.getScore() + 1;
			score.setScore(kills);
		}
		else {
			Score score = sb.getObjective(DisplaySlot.SIDEBAR).getScore(killer.getName());
			kills = score.getScore() + 1;
			score.setScore(kills);
		}
		
		if (kills >= 20) {
			arena.sendArenaMessage(OITC.prefix + ChatColor.AQUA + ChatColor.STRIKETHROUGH.toString() + "=============================================");
			arena.sendArenaMessage(OITC.prefix + ChatColor.GOLD + killer.getName() + " has reached 20 kills and has won the game.");
			arena.sendArenaMessage(OITC.prefix + ChatColor.AQUA + ChatColor.STRIKETHROUGH.toString() + "=============================================");
			arena.stop();
		}
	}
	
	private String quickStats(Arena arena, Player player) {
		String s = ChatColor.WHITE + " [" + ChatColor.GREEN + arena.getStats(player).getKills() + ChatColor.WHITE + "/" + ChatColor.RED
				+ arena.getStats(player).getDeaths() + ChatColor.WHITE + "]";
		return s;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (!Manager.isPlaying(player)) {
			return;
		}
		if (Manager.getArena(player).getState() == State.STARTING) {
			if (event.getTo().getX() != event.getFrom().getX() || event.getTo().getZ() != event.getFrom().getZ()) {
				player.teleport(player.getLocation());
			}
		}
	}
	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (event.getItem().getItemStack().getType() == Material.ARROW) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (Manager.isPlaying(player)) {
			Arena arena = Manager.getArena(player);
			arena.removePlayer(player);
			Manager.removePlayer(player);
			arena.sendArenaMessage(OITC.prefix + player.getName() + " has left the game.");
		}
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if (!Manager.isPlaying(player)) {
			return;
		}
		Arena arena = Manager.getArena(player);
		event.setRespawnLocation(arena.getNextSpawn());
		arena.setDefaultInventory(player);
	}
}
