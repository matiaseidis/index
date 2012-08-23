package plan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.Cacho;
import models.RetrievalPlan;
import models.User;
import models.UserCacho;
import models.UserChunk;
import models.UserChunks;
import models.Video;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

import play.Play;

public class RetrievalPlanCreator {

	private final Video video;
	private final User planRequester;
	private final CachoPropioMaker cachoPropioMaker;
	private final CachoAjenoMaker cachoAjenoMaker;

	/*
	 * TODO usar el maxCachoSize para limitar el tamaño de los cachos -> ¿va esto?
	 */
	private int maxCachoSize = Integer.valueOf(Play.configuration.getProperty("max.cacho.size")); 
	private long chunkSize = Long.valueOf(Play.configuration.getProperty("chunk.size")) * 1024 * 1024;

	public RetrievalPlanCreator(Video video, User user) {
		super();
		this.video = video;
		this.planRequester = user;
		cachoPropioMaker = new CachoPropioMaker(user, video);
		cachoAjenoMaker = new CachoAjenoMaker(video);
	}

	public RetrievalPlan generateRetrievalPlan(){
		
		boolean firstCacho = true;

		List<UserChunks> userChunks = new ArrayList<UserChunks>(video.userChunks);
	
		UserChunks planRequesterChunks = video.getChunksFrom(planRequester);

		Set<UserChunks> result = new HashSet<UserChunks>();

		List<UserChunks> noPlanRequesterChunks = noPlanRequesterChunks(userChunks);

		for(int i = 0; i<video.chunks.size(); i++) {
			/*
			 * mientras el requester tenga este chunk, voy inflando un userChunks para el
			 */
			UserChunks nextCacho = null;
			if (planRequesterChunks.hasChunk(i)){
				nextCacho = cachoPropioMaker.makeCacho(i, planRequesterChunks);
				
			} else {
				/*
				 * en cuento deja de tener, inflo uno para el que menos tenga a partir del current chunk 
				 * o hasta que el requester vuelva a tener el chunk
				 * 
				 */
				UserChunks noRequesterShortestCacho = cachoAjenoMaker.makeCacho(i, planRequesterChunks, noPlanRequesterChunks, firstCacho);
				
				if(noRequesterShortestCacho == null) {
					return null;
				}
				nextCacho = noRequesterShortestCacho;

			}
			firstCacho = false;
			result.add(nextCacho);
			if(nextCacho.hasChunk(video.chunks.size()-1)){
				return retrievalPlanFor(video, cachosFrom(result, video));
			} 
			i = nextCacho.higherChunkPosition();
		}
		play.Logger.error("Unable to ellaborate retrieving plan for video %s for user %s - not enough sources available", video.videoId, planRequester.email);
		return null;
	}

	private List<UserChunks> noPlanRequesterChunks(List<UserChunks> userChunks) {

		List<UserChunks> result = new ArrayList<UserChunks>();

		for(UserChunks uc :  userChunks) {
			if(!uc.user.email.equals(planRequester.email)) {
				result.add(uc);
			}
		}

		return result;
	}

	private RetrievalPlan retrievalPlanFor(Video video,
			List<UserCacho> userCachos) {
		RetrievalPlan rp = new RetrievalPlan(video, userCachos); 
		return rp;
	}

	private List<UserCacho> cachosFrom(Set<UserChunks> chunks, Video video) {

		List<UserCacho> result = new ArrayList<UserCacho>();
		for(UserChunks uc : chunks) {
			result.add(new UserCacho(uc.user, cachoFromUserChunks(uc, video)));
		}
		return result;
	}

	private Cacho cachoFromUserChunks(UserChunks uc, Video video) {

		long from =  uc.chunks.get(0).position * chunkSize;
		long lenght = uc.chunks.size() * chunkSize;

		boolean lastCacho = (uc.chunks.get(uc.chunks.size()-1).position) == video.chunks.size()-1;

		if(lastCacho){
			long diff  = chunkSize - (video.lenght % chunkSize);
			lenght-= diff;
		}
		Cacho cacho = new Cacho(from, lenght);

		return cacho;
	}
}
