package plan;

import java.util.Comparator;

import models.UserChunks;

public class ChunkPositionComparator implements Comparator<UserChunks> {

	@Override
	public int compare(UserChunks o1, UserChunks o2) {
		Integer p1 = o1.higherChunkPosition();
		Integer p2 = o2.higherChunkPosition();
		return p1.compareTo(p2);
	}

}
