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
					Collections.sort(enCarrera, new LargestCachoComparator(from, video.chunks.size()-1));
					
					UserChunks shortest = selectCacho(from, enCarrera);
					UserChunks shorted = cutCacho(from, shortest, planRequesterChunks);
					
					return shorted;
				}

	}

	private UserChunks cutCacho(int from, UserChunks shortest, UserChunks planRequesterChunks) {
		UserChunks result = new UserChunks(shortest.user);
		
		for(int i = from; i < maxCachoSize+from; i++){
			if(planRequesterChunks.hasChunk(i)){
				return result;
			}
			result.addChunk(new UserChunk(i));
		}
		
		return result;
	}

	private UserChunks selectCacho(int from, List<UserChunks> enCarrera) {
		UserChunks winner = enCarrera.get(0); // el mas grande
		
		for(UserChunks current : enCarrera) {
			for(int i = from; i < maxCachoSize+from; i++){
				if(!current.hasChunk(i)){
					return winner;
				} else {
					winner = current;
				}
			}
		}
		
		return winner;
	}
}
