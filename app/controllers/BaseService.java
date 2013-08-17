package controllers;

import play.Play;
import play.mvc.Controller;
import controllers.response.Ok;
import controllers.response.Respuesta;
import controllers.response.TodoMal;
import flexjson.JSONSerializer;

public class BaseService extends Controller {
	
	public static final String CHUNK_SEPARATOR = Play.configuration.getProperty("chunk.separator");
	public static final String CHUNK_FOR_REGISTER_SEPARATOR = Play.configuration.getProperty("chunk.registration.separator");
	
	protected static void jsonOk(Object obj) {
		JSONSerializer serializer = new JSONSerializer();
		serializer.include("body.userCachos");
		serializer.include("body.video");
		serializer.exclude("body.class");
		serializer.exclude("body.video.chunks");

		try {
			
			Object serial = serializer.deepSerialize(obj);
			String result = serial.toString();
			play.Logger.info("result: %s", result);
			renderJSON(new Ok(result));
		
		} catch(Exception e) {
		
			play.Logger.error(e, "TODO MAL");
			renderJSON(new Error(e));
		}
	}
	
	protected static void jsonError(Object obj) {
		JSONSerializer serializer = new JSONSerializer();
		renderJSON(serializer.serialize(new TodoMal(obj)));
	}

}
