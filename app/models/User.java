package models;

import javax.persistence.Entity;

import net.sf.oval.constraint.NotEmpty;
import play.data.validation.Email;
import play.data.validation.IPv4Address;
import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.Model;

@Entity
public class User extends Model{

	@Required @NotEmpty
	public String name;
	@Required @NotEmpty @Email
	public String email;
	@Required @Unique @NotEmpty @IPv4Address
	public String ip;
	@Required
	public int port;
	
//	public Map<Video, List<Integer>> videoChunks = new HashMap<Video, List<Integer>>();
	
	public User(@Required String name, @Required String email, @Required String ip, @Required int port) {
		this.name = name;
		this.email = email;
		this.ip = ip;
		this.port = port;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
