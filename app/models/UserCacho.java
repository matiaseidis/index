package models;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import play.db.jpa.Model;

@Entity
public class UserCacho extends Model{
	
	private static final long serialVersionUID = 1L;
	
	@OneToOne
	private final User user;
	@OneToOne
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
