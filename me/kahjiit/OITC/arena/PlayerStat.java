package me.kahjiit.OITC.arena;

public class PlayerStat {

	private int kills;
	private int deaths;
	
	public PlayerStat() {
		kills = 0;
		deaths = 0;
	}
	
	public void addDeath() {
		deaths ++;
	}
	
	public void addKill() {
		kills ++;
	}
	
	public int getDeaths() {
		return deaths;
	}
	
	public int getKills() {
		return kills;
	}
}
