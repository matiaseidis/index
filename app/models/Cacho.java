package models;

import javax.persistence.Entity;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Cacho extends Model{

	@Required
	public long start;
	@Required
	public long lenght;
	
	public Cacho(){}

	public Cacho(Long from, Long lenght) {
		this.start = from;
		this.lenght = lenght;
	}
	
	public long lastByte(){
		return start + lenght - 1;
	}

	public boolean isChoterThan(Cacho newCacho) {
		if (newCacho == null) 
			return false;
		return this.start >= newCacho.start && this.lastByte() <= newCacho.lastByte();
	}
	
	
	
	@Override
	public String toString() {
		return "cacho from:"+this.start+" - lenght: "+this.lenght;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (lenght ^ (lenght >>> 32));
		result = prime * result + (int) (start ^ (start >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		Cacho other = (Cacho) obj;
		if (lenght != other.lenght)
			return false;
		if (start != other.start)
			return false;
		return true;
	}

	public boolean contains(Cacho otroCacho) {
		if (otroCacho == null) 
			return false;
		return this.start >= otroCacho.start && this.lastByte() <= otroCacho.lastByte();
	}
	
	
}
