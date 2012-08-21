package models;

import java.io.Serializable;

public class UserCacho implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private final User user;
	private final Cacho cacho;
	
	public UserCacho(User user, Cacho c){
		this.user = user;
		this.cacho = c;
	}

	public User getUser() {
		return user;
	}

	public Cacho getCacho() {
		return cacho;
	}

}
