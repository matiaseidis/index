package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;

import net.sf.oval.constraint.NotEmpty;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class User extends Model{

	@Required @NotEmpty
	public String name;
	@Required @NotEmpty
	public String email;
	@Required @NotEmpty
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
