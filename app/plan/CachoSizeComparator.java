package plan;

import java.util.Comparator;

import models.UserCacho;

public class CachoSizeComparator implements Comparator<UserCacho> {

	@Override
	public int compare(UserCacho o1, UserCacho o2) {
		return Long.valueOf(o1.getCacho().lenght).compareTo(o2.getCacho().lenght);
	}

}
