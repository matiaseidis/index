package index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;
import models.Cacho;
import models.UserCacho;

import org.junit.Test;

import plan.CachoSizeComparator;

public class CachoSizeComparatorTest {
	
	@Test
	public void cachoSizeComparatorShouldReturnBiggerFirst(){
		List<UserCacho> uc = new ArrayList<UserCacho>();
		uc.add(new UserCacho(null, new Cacho(0L, 200L)));
		uc.add(new UserCacho(null, new Cacho(0L, 100L)));
		Collections.sort(uc, new CachoSizeComparator());
		
		Assert.assertTrue("Bigger cacho should be last after CachoSizeComparator", uc.get(1).getCacho().lenght == 200);
	}

}
