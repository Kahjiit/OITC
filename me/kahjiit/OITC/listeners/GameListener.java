package me.kahjiit.OITC.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.kahjiit.OITC.OITC;
import me.kahjiit.OITC.arena.Arena;
import me.kahjiit.OITC.arena.Manager;
import me.kahjiit.OITC.arena.State;

public class GameListener implements Listener {

	OITC plugin;
	
	public GameListener() {
		this.plugin = (OITC) OITC.getPlugin();
	}
	
	@EventHandler
	public void onArrowHit(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();
			if (arrow.getShooter() instanceof Player) {
				Player player = (Player) event.getEntity();
				Player shooter = (Player) arrow.getShooter();
				if (Manager.isPlaying(player) && Manager.isPlaying(shooter)) {
					if (Manager.getArena(player).isRunning()) {
						if (player == shooter) {
							event.setCancelled(true);
							return;
						}
						player.getWorld().createExplosion(player.getLocation(), 0F);
						event.setDamage(30);
					}
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
	
	@EventHandler
	public void onCommandSend(PlayerCommandPreprocessEvent event) {
		if (!Manager.isPlaying(event.getPlayer())) {
			return;
		}
		if (Manager.getArena(event.getPlayer()).getState() == State.INGAME) {
			if (event.getMessage().startsWith("/oitc")) {
				return;
			}
			event.getPlayer().sendMessage(ChatColor.RED + "You may not use commands while playing OITC!");
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
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (!Manager.isPlaying(player)) {
			return;
		}
		if (Manager.getArena(player).getState() == State.STARTING) {
			if (event.getTo().getX() != event.getFrom().getX() && event.getTo().getZ() != event.getFrom().getZ()) {
				player.teleport(player.getLocation());
			}
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
}
