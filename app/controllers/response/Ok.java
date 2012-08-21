package controllers.response;

public class Ok<T> extends Respuesta<T> {
	
	public Ok(T body) {
		super(body, "OK");
	}

}
