package index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import models.UserChunk;
import models.UserChunks;

import org.junit.Assert;
import org.junit.Test;

import plan.ChunkPositionComparator;
import play.test.UnitTest;

public class ChunkPositionComparatorTest extends UnitTest {
	
	@Test
	public void testPositionComparator(){
		UserChunks uc1 = new UserChunks(null);
		uc1.addChunk(new UserChunk(25));
		UserChunks uc2 = new UserChunks(null);
		uc1.addChunk(new UserChunk(1));
		
		List<UserChunks> result = new ArrayList<UserChunks>();
		
		result.add(uc1);
		result.add(uc2);
		
		Collections.sort(result, new ChunkPositionComparator());
		
		Assert.assertTrue(result.get(0).equals(uc2));
	}

}
