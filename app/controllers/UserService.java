package controllers;

import models.User;
import net.sf.oval.constraint.NotNull;
import play.data.validation.Min;
import play.mvc.Controller;
import controllers.response.Ok;
import controllers.response.TodoMal;

public class UserService extends Controller {

	public static void add(@NotNull String nombre, @NotNull String email, 
			@NotNull String ip, @NotNull @Min(1) Integer port){ //POST

		if(validation.hasErrors()){
			play.Logger.error("Invalid params: %s", params);
			render(new TodoMal("Invalid params"));
		}

		play.Logger.info("registrando usuario "+nombre);

		boolean userExists = User.find("email=?", email).first() != null;
		if(!userExists){

			User user = new User(nombre, email, ip, port);
			user.save();
			play.Logger.error("usuario creado: "+nombre);

			renderJSON(new Ok(user));
		} else {
			play.Logger.error("Error intentando crear usuario: %s. params: %s",nombre, params);
			renderJSON(new TodoMal("el usuario "+nombre+"ya esta registrado"));
		}
	}
}
