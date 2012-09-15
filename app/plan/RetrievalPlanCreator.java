package plan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import models.Cacho;
import models.RetrievalPlan;
import models.User;
import models.UserCacho;
import models.UserCachos;
import models.UserChunks;
import models.Video;

import org.apache.commons.collections.MapUtils;

import play.Play;

public class RetrievalPlanCreator {

	private final Video video;
	private final User planRequester;
	private final CachoPropioMaker cachoPropioMaker;
	private CachoAjenoMaker cachoAjenoMaker;

	public RetrievalPlanCreator(Video video, User user) {
		super();
		this.video = video;
		this.planRequester = user;
		cachoPropioMaker = new CachoPropioMaker(user, video);
	}

	public RetrievalPlan generateRetrievalPlan(){
		
		play.Logger.info("armando plan para "+planRequester.email+" - video: "+video.videoId+" * "+video.lenght);
		List<UserCachos> userCachos = new ArrayList<UserCachos>(video.userCachos);
		
		UserCachos planRequesterCachos = video.getCachosFrom(planRequester);
		play.Logger.info("plan requester chunks: %s", planRequesterCachos.cachos);
		
		SortedMap<Long, UserCacho> result = new TreeMap<Long, UserCacho>();
		List<UserCachos> noPlanRequesterCachos = noPlanRequesterCachos(userCachos);
		
		/*
		 * ahora la estrategia cambia
		 * 1 - agrego todos los cachos del requester al plan
		 */
		for(Cacho requesterCacho : planRequesterCachos.cachos){
			result.put(requesterCacho.start, new UserCacho(planRequester, requesterCacho));
		}
		
		/*
		 * 2 - recorro los requesterCachos para ver que necesito del resto (los cachos que no tiene el requester)
		 */
		List<Cacho> cachosQueFaltan = new ArrayList<Cacho>();
		if(result.isEmpty()){
			/*
			 * si el requester no tiene nada, necesito el video entero
			 */
			cachosQueFaltan.add(new Cacho(0L, video.lenght));
		} else {
			
			long last = 0;
			for(Map.Entry<Long, UserCacho> entry : result.entrySet()){
				if(entry.getKey().compareTo(last) != 0) {
					/*
					 * falta el siguiente cacho
					 */
					cachosQueFaltan.add(new Cacho(last, entry.getKey()));
					/*
					 * actualizo last
					 */
					last += entry.getValue().getCacho().lenght; 
				}
			}
			UserCacho ultimoRequesterCacho = result.get(result.lastKey());
			long ultimoRequesterCachoLastByteIndex = ultimoRequesterCacho.getCacho().start+ultimoRequesterCacho.getCacho().lenght;
			if(ultimoRequesterCachoLastByteIndex != video.lenght){
				/*
				 * el requester no tiene el ultimo cacho
				 */
				long cachoQueFaltaLenght = video.lenght - ultimoRequesterCachoLastByteIndex;
				cachosQueFaltan.add(new Cacho(ultimoRequesterCachoLastByteIndex+1, cachoQueFaltaLenght));
			}
		}
		
		cachoAjenoMaker = new CachoAjenoMaker(video, cachosQueFaltan, noPlanRequesterCachos);
		
		/*
		 * 3 - los recorro con el criterio de maximo tamaño y round robin
		 */
		if(cachoAjenoMaker.canMakeCachos()){
			List<UserCacho> cachosAjenos = cachoAjenoMaker.getCachos();
			
			for(UserCacho uc : cachosAjenos){

				result.put(uc.getCacho().start, uc);
			}
			
			RetrievalPlan retrievalPlan = new RetrievalPlan(video, new ArrayList<UserCacho>(result.values()));
			return retrievalPlan;
		} else {
			play.Logger.error("Unable to ellaborate retrieving plan for video %s for user %s - not enough sources available", video.videoId, planRequester.email);
			return null;
		}
		
		
	}

	private List<UserCachos> noPlanRequesterCachos(List<UserCachos> userChunks) {

		List<UserCachos> result = new ArrayList<UserCachos>();

		for(UserCachos uc :  userChunks) {
			if(!uc.user.email.equals(planRequester.email)) {
				result.add(uc);
			}
		}

		return result;
	}
}
