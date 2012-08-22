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

	/*
	 * TODO usar el maxCachoSize para limitar el tamaño de los cachos
	 */
	private int maxCachoSize = Integer.valueOf(Play.configuration.getProperty("max.cacho.size")); 
	private long chunkSize = Long.valueOf(Play.configuration.getProperty("chunk.size")) * 1024 * 1024;

	public RetrievalPlanCreator(Video video, User user) {
		super();
		this.video = video;
		this.planRequester = user;
	}

	public RetrievalPlan generateRetrievalPlan(){

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
				nextCacho = requesterCacho(i, planRequesterChunks);

			} else {
				/*
				 * en cuento deja de tener, inflo uno para el que menos tenga a partir del current chunk 
				 * o hasta que el requester vuelva a tener el chunk
				 * 
				 */
				UserChunks noRequesterShortestCacho = noRequesterShortestCacho(i, planRequesterChunks, noPlanRequesterChunks);
				if(noRequesterShortestCacho == null) {
					return null;
				}
				nextCacho = noRequesterShortestCacho;

			}
			result.add(nextCacho);
			if(nextCacho.hasChunk(video.chunks.size()-1)){
				return retrievalPlanFor(video, cachosFrom(result, video));
			} 
			i = higherChunk(nextCacho);
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

	private List<UserChunks> thisUserChunkList(List<UserChunks> userChunks) {

		return (List<UserChunks>) CollectionUtils.collect(userChunks, new Transformer() {

			@Override
			public Object transform(Object obj) {
				UserChunks uc = (UserChunks) obj;
				if (uc.user.equals(planRequester) ) {
					return uc;
				}
				return null;
			}
		});
	}

	private int higherChunk(UserChunks nextCacho) {

		int result = 0;

		for(UserChunk i : nextCacho.chunks) {
			if(i.position > result) {
				result = i.position;
			}
		}

		return result;
	}

	private UserChunks noRequesterShortestCacho(int from,
			UserChunks planRequesterChunks,
			List<UserChunks> noPlanRequesterChunks) {

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

	private UserChunks requesterCacho(int from, UserChunks planRequesterChunks) {
		UserChunks uc = new UserChunks(planRequester);

		for(int i = from; i<video.chunks.size();i++){
			if(planRequesterChunks.hasChunk(i)){
				uc.chunks.add(new UserChunk(i));
			} else {
				return uc;
			}
		}
		/*
		 * si el requester tiene todo el video...
		 */
		return uc;
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
