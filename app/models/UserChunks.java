package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import net.sf.cglib.core.CollectionUtils;
import net.sf.cglib.core.Transformer;

import play.db.jpa.Model;

@Entity
public class UserChunks extends Model{
	
	@ManyToOne(cascade=CascadeType.REFRESH)
	public User user;
	
	@OneToMany(cascade=CascadeType.ALL)
	public List<UserChunk> chunks;

	public UserChunks(User user) {
		this(user, new ArrayList<UserChunk>());
	}

	public UserChunks(User user, List<UserChunk> chunks) {
		super();
		this.user = user;
		this.chunks = chunks;
	}
	
	public boolean addChunk(UserChunk chunk){
		return this.chunks.add(chunk);
	}

	@Override
	public String toString() {
		return "UserChunks [userId=" + user.email + ", chunks=" + chunks + "]";
	}
	
	public int higherChunkPosition() {
		
		int result = 0;
		
		for(UserChunk i : this.chunks) {
			if(i.position > result) {
				result = i.position;
			}
		}
		
		return result;
	}

//	public boolean addChunksFromPositions(List<Integer> c) {
//		
//		boolean someAdded = false;
//		for(int chunkIndex : c) {
//			if (!hasChunk(chunkIndex)) {
//				chunks.add(new UserChunk(chunkIndex));
//				someAdded = true;
//			}
//		}
//		
//		return someAdded;
//	}

//	public boolean addChunksFromUserChunks(List<UserChunks> uc) {
//		return addChunksFromPositions((List<Integer>)CollectionUtils.transform(uc, new Transformer() {
//			
//			@Override
//			public Object transform(Object arg0) {
//				return ((UserChunk)arg0).position;
//			}
//		}));
//	}

	
	public synchronized boolean hasChunk(int chunkIndex) {
		
		for(UserChunk uc : chunks) {
			if(uc.position == chunkIndex) {
				return true;
			}
		}
		return false;
	}

	public synchronized boolean removeChunk(int chunkPosition) {
		
		UserChunk chunkToRemove = null;
		boolean removed = false;
		
		if(this.hasChunk(chunkPosition)){
			for(UserChunk uc : this.chunks) {
				if(uc.position == chunkPosition){
					chunkToRemove = uc;
					break;
				}
			}
		}
		if(chunkToRemove != null) {
			removed = this.chunks.remove(chunkToRemove);
		}
		return removed;
	}

	
	
	

}
