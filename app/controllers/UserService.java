package controllers;

import javax.persistence.UniqueConstraint;

import models.User;
import net.sf.oval.constraint.NotNull;
import play.data.validation.Email;
import play.data.validation.IPv4Address;
import play.data.validation.Min;
import play.data.validation.Unique;
import play.mvc.Controller;
import controllers.response.Ok;
import controllers.response.TodoMal;
import flexjson.JSONSerializer;

public class UserService extends Controller {

	public static void create(@NotNull String name, @NotNull @Email String email, 
			@NotNull @IPv4Address String ip, @NotNull @Min(1) Integer port){ //POST

		if(validation.hasErrors()){
			play.Logger.error("Invalid params: %s", params);
			for(play.data.validation.Error error : validation.errors()) {
				play.Logger.warn(error.getKey()+" - "+error.message());
				
			}
			renderJSON(new JSONSerializer().serialize(new TodoMal("Invalid params")));
		}

		play.Logger.info("registrando usuario "+name);

		boolean userExists = User.find("email=? or ip=?", email, ip).first() != null;
		if(!userExists){

			User user = new User(name, email, ip, port);
			user.save();
			play.Logger.error("usuario creado: "+name);
			
//			JSONSerializer userSerializer = new JSONSerializer().include(
//					"*");
//			String serialize = userSerializer.serialize(user);
			

			renderJSON(new JSONSerializer().serialize(new Ok(user)));
		} else {
			play.Logger.error("Error intentando crear usuario: %s. params: %s",name, params);
			renderJSON(new JSONSerializer().serialize(new TodoMal("el usuario "+name+" ya esta registrado")));
		}
	}
}
