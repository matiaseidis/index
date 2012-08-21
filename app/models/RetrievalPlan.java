package models;

import java.io.Serializable;
import java.util.List;

public class RetrievalPlan implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final Video video;
	private final List<UserCacho> userCachos;
	
	public RetrievalPlan(Video video, List<UserCacho> userCachos) {
		super();
		this.video = video;
		this.userCachos = userCachos;
	}
	
	public Video getVideo() {
		return video;
	}

	public List<UserCacho> getUserCachos() {
		return userCachos;
	}
}
