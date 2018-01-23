package CheckersServer;

import java.util.Objects;

/**
 * @author Nafiur Rahman Khadem
 */
public class UserId {
	private String username, password;
	private int gamesPlayed, gamesWon;
	
	public UserId (String username, String password) {
		this.username = username;
		this.password = password;
		gamesPlayed = 0;
		gamesWon = 0;
	}
	
	public UserId (String username, String password, int gamesPlayed, int gamesWon) {
		this.username = username;
		this.password = password;
		this.gamesPlayed = gamesPlayed;
		this.gamesWon = gamesWon;
	}
	
	public String getUsername () {
		return username;
	}
	
	public String getPassword () {
		return password;
	}
	
	public int getGamesPlayed () {
		return gamesPlayed;
	}
	
	public void setGamesPlayed (int gamesPlayed) {
		this.gamesPlayed = gamesPlayed;
	}
	
	public int getGamesWon () {
		return gamesWon;
	}
	
	public void setGamesWon (int gamesWon) {
		this.gamesWon = gamesWon;
	}
	
	@Override
	public String toString () {
		return "UserId{"+"username='"+username+'\''+", password='"+password+'\''+", gamesPlayed="+gamesPlayed+", gamesWon="+gamesWon+'}';
	}
	
	@Override
	public boolean equals (Object o) {
		if (this == o) return true;
		if (!(o instanceof UserId)) return false;
		UserId userId = (UserId) o;
		return Objects.equals (username, userId.username) && Objects.equals (password, userId.password);
	}
	
	@Override
	public int hashCode () {
		return Objects.hash (username, password);
	}
}
