package models;

import java.io.Serializable;

public class Cacho implements Serializable{

	public long from;
	public long lenght;

	public Cacho(Long from, Long lenght) {
		this.from = from;
		this.lenght = lenght;
	}
	
	public long lastByte(){
		return from + lenght - 1;
	}

	public boolean isChoterThan(Cacho newCacho) {
		if (newCacho == null) 
			return false;
		return this.from >= newCacho.from && this.lastByte() <= newCacho.lastByte();
	}
	
	@Override
	public String toString() {
		return "cacho from:"+this.from+" - lenght: "+this.lenght;
	}
}
