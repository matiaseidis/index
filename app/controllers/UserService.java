package controllers;

import models.User;
import net.sf.oval.constraint.NotNull;
import play.data.validation.Email;
import play.data.validation.IPv4Address;
import play.data.validation.Min;
import utils.SiteNotifier;
import controllers.response.Ok;

public class UserService extends BaseService {

	public static void create(@NotNull String name,
			@NotNull @Email String email, @NotNull @IPv4Address String ip,
			@NotNull @Min(1) Integer servlePort,
			@NotNull @Min(1) Integer dimonPort) { // POST

		if (validation.hasErrors()) {
			play.Logger.error("Invalid params: %s", params);
			for (play.data.validation.Error error : validation.errors()) {
				play.Logger.warn(error.getKey() + " - " + error.message());
			}
			renderJSON(new Error("Invalid params"));
			// jsonError("Invalid params");
		}

		play.Logger.info("registrando usuario " + name);

		boolean userExists = User.find("email=? or ip=?", email, ip).first() != null;
		if (!userExists) {

			User user = new User(name, email, ip, servlePort, dimonPort);
			user.save();
			play.Logger.info("usuario creado: " + name);

			new SiteNotifier().notifyNewUser(user);

			// jsonOk(user);
			renderJSON(new Ok(user));

		} else {
			play.Logger.error("Error intentando crear usuario: %s. params: %s",
					name, params);
//			jsonError("el usuario " + name + " ya esta registrado");
			renderJSON(new Error("el usuario " + name + " ya esta registrado"));
		}
	}
}
