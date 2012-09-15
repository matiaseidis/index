package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class UserChunk extends Model {

	public int position;

	public UserChunk(int i) {
		this.position = i;
	}


}
