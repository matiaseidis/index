package index;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import models.User;
import models.UserChunk;
import models.UserChunks;

import org.junit.Assert;
import org.junit.Test;

import plan.ShortestCachoComparator;
import play.test.UnitTest;


public class ShortestCachoComparatorTest extends UnitTest {
	
	@Test
	public void testComparator() {
		
		ShortestCachoComparator comparator = new ShortestCachoComparator(0, 10);
		
		UserChunks uc1 = new UserChunks(new User("pepe", "p@p.com", "1.1.1.1", 123));
		UserChunks uc2 = new UserChunks(new User("juan", "j@j.com", "2.2.2.2", 456));
		
		for(int i = 0; i<10; i++) {
			uc1.addChunk(new UserChunk(i));
			uc2.addChunk(new UserChunk(i));
		}
		uc1.addChunk(new UserChunk(10));
		
		List<UserChunks> chunks = new ArrayList<UserChunks>();
		chunks.add(uc1);
		chunks.add(uc2);
		
		Collections.sort(chunks, comparator);
		Assert.assertEquals(uc2,chunks.get(0));
		Assert.assertEquals(uc1,chunks.get(1));
		
		chunks = new ArrayList<UserChunks>();
		chunks.add(uc2);
		chunks.add(uc1);
		
		Collections.sort(chunks, comparator);
		Assert.assertEquals(uc2,chunks.get(0));
		Assert.assertEquals(uc1,chunks.get(1));
		
		
	}

}
