package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class VideoChunk extends Model{

	public Integer position;
	public String hash;
	
	public VideoChunk(int index, String hash) {
		this.position = index;
		this.hash = hash;
	}
}
