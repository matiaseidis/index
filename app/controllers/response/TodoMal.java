package controllers.response;

public class TodoMal<T> extends Respuesta<T> {

	public TodoMal(T body) {
		super(body, "ERROR");
	}

}
