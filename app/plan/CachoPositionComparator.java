package plan;

import java.util.Comparator;

import models.Cacho;

public class CachoPositionComparator implements Comparator<Cacho> {

	@Override
	public int compare(Cacho c1, Cacho c2) {
		Long p1 = c1.start;
		Long p2 = c2.start;
		return p1.compareTo(p2);
	}

}
