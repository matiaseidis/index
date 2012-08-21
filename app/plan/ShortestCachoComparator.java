package plan;

import java.util.Comparator;

import models.UserChunks;

public class ShortestCachoComparator implements Comparator<UserChunks> {

	final int from;
	final int last;
	
	public ShortestCachoComparator(int from, int last) {
		this.from = from;
		this.last = last;
	}

	@Override
	public int compare(UserChunks uc1, UserChunks uc2) {
		
		for(int i = from; i == last;i++){
			if(uc1.hasChunk(i) && !uc2.hasChunk(i)) {
				return -1;
			} else if(!uc1.hasChunk(i) && uc2.hasChunk(i)) {
				return 1;
			} 
		}
		return 0;
	}

}
