package controllers.response;

import java.io.Serializable;

public class Respuesta<T> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public T body;
	public String code;
	
	public Respuesta(T body, String code){
		this.body = body;
		this.code = code;
	}
	
}
