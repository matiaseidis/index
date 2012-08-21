package models;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserRepo {
	
	private ConcurrentMap<String, User> users = new ConcurrentHashMap<String, User>();
	
	public boolean addUser(User user){
		return users.putIfAbsent(user.name, user) == null;
	}

//	public User getById(String userName) {
//		return users.get(userId);
//	}
	
}
