package DTO;

import java.sql.Timestamp;

public class WatchlistDTO {
	private String user;
	private String market;
	private Timestamp create_at;
	
	public WatchlistDTO() {
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getMarket() {
		return market;
	}

	public void setMarket(String market) {
		this.market = market;
	}

	public Timestamp getCreate_at() {
		return create_at;
	}

	public void setCreate_at(Timestamp create_at) {
		this.create_at = create_at;
	}

}
