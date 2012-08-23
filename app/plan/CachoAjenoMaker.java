package plan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import play.Play;

import models.User;
import models.UserChunk;
import models.UserChunks;
import models.Video;

public class CachoAjenoMaker {

	private final Video video;
	private int firstCachoSize = Integer.valueOf(Play.configuration.getProperty("first.cacho.size"));
	private int maxCachoSize = Integer.valueOf(Play.configuration.getProperty("max.cacho.size")); 
	private long chunkSize = Long.valueOf(Play.configuration.getProperty("chunk.size")) * 1024 * 1024;

	public CachoAjenoMaker(Video video) {
		this.video = video;
	}

	public UserChunks makeCacho(int from,
			UserChunks planRequesterChunks,
			List<UserChunks> noPlanRequesterChunks, boolean firstCacho) {

		List<UserChunks> enCarrera = new ArrayList<UserChunks>();
		
				for(UserChunks uc : noPlanRequesterChunks) {
					if(uc.hasChunk(from)) {
						enCarrera.add(uc);
					}
				}
		
				if(enCarrera.isEmpty()){
		
					return null;
		
				} else {
					Collections.sort(enCarrera, new ShortestCachoComparator(from, video.chunks.size()-1));
		
					UserChunks shortest = new UserChunks(enCarrera.get(0).user);
		
					boolean done = false;
					while(!done) {
		
						if(!planRequesterChunks.hasChunk(from) && enCarrera.get(0).hasChunk(from) ) {
							shortest.chunks.add(new UserChunk(from));
							from++;
						} else {
							done = true;
						}
					}
		
					return shortest;
				}

	}
}
