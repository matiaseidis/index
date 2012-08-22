package controllers;

import controllers.response.Ok;
import controllers.response.TodoMal;
import flexjson.JSONSerializer;
import play.Play;
import play.mvc.Controller;

public class BaseService extends Controller {
	
	public static final String CHUNK_SEPARATOR = Play.configuration.getProperty("chunk.separator");
	public static final String CHUNK_FOR_REGISTER_SEPARATOR = Play.configuration.getProperty("chunk.registration.separator");
	
	
	protected static void jsonOk(Object obj) {
		renderJSON(new JSONSerializer().serialize(new Ok(obj)));
	}
	
	protected static void jsonError(Object obj) {
		renderJSON(new JSONSerializer().serialize(new TodoMal(obj)));
	}

}
