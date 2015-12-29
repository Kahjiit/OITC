package me.kahjiit.OITC.arena;

public class PlayerStat {

	private int kills;
	private int deaths;
	private int streak;
	
	public PlayerStat() {
		kills = 0;
		deaths = 0;
		streak = 0;
	}
	
	public void addDeath() {
		deaths ++;
	}
	
	public void addKill() {
		kills ++;
	}
	
	public void addStreak() {
		streak ++;
	}
	
	public int getDeaths() {
		return deaths;
	}
	
	public int getKills() {
		return kills;
	}
	
	public int getStreak() {
		return streak;
	}
	
	public void resetStreak() {
		streak = 0;
	}
}
