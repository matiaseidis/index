package models;

import javax.persistence.Entity;

import net.sf.oval.constraint.NotNull;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class UserChunk extends Model{

	@Required @NotNull
	public Integer position;
	
	public UserChunk(int index) {
		this.position = index;
	}
	
	@Override
	public String toString() {
		return position.toString();
	}
}
