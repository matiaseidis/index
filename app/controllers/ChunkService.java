package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.User;
import models.Video;
import net.sf.oval.constraint.NotNull;
import controllers.response.TodoMal;

public class ChunkService extends BaseService {


	public static void registerChunks(@NotNull String videoId, @NotNull String userId, @NotNull String chunks){

		if(validation.hasErrors()){
			play.Logger.error("Invalid params: %s", params);
			render(new TodoMal("Invalid params"));
		}

		play.Logger.info("Cacho registration requested by user: "+userId+" for video: "+videoId);

		User registrationRequester = User.find("email=?", userId).first();
		Video video = Video.find("videoId=?", videoId).first();

		if(registrationRequester == null){
			play.Logger.error("No existe el cacho registration requester: %s", userId);
			jsonError("No existe el cacho registration requester "+userId);
		}

		if(video == null){
			play.Logger.error("El video del que se quieren registrar cachos no existe en el indice: %s", videoId);
			jsonError("El video del que se quieren registrar cachos no existe en el indice: "+videoId);
		}

		Map<Integer, String> chunkOrdinals = validChunksForVideo(video, chunks);

		if(chunkOrdinals == null || chunkOrdinals.isEmpty()){
			play.Logger.error("No chunks passed for register for video: "+videoId);
			jsonError("No chunks passed for register for video: "+videoId);
		}

//		List<Integer> validChunksOrdinals = validChunks(video, chunkOrdinals);

		play.Logger.info("registering chunks "+chunkOrdinals+" for video "+videoId+" by user " + userId);

		try {
			if (video.registerChunks(registrationRequester, new ArrayList(chunkOrdinals.keySet()))) {
				jsonOk("registered chunks by: "+ userId +" for video: "+videoId+" - "+chunks);
			} else {

				play.Logger.error("unable to register chunks "+ userId +" - "+videoId+" - "+chunks);
				jsonError("unable to register chunks "+ userId +" - "+videoId+" - "+chunks);
			}
		} catch (Exception e) {
		}

		jsonOk(chunkOrdinals);
	}

//	private static List<Integer> validChunks(Video video, Map<Integer, String> chunkOrdinals) {
//
//		List<Integer> validChunks = new ArrayList<Integer>();
//
//		for(Map.Entry<Integer, String> chunkToValidate : chunkOrdinals.entrySet()) {
//			int chunkOrdinal = chunkToValidate.getKey();
//			if(video.chunks.get(chunkOrdinal).equals(chunkToValidate.getValue())){
//				validChunks.add(chunkOrdinal);
//			}
//		}
//		return validChunks;
//	}

	public static void unregisterChunks(@NotNull String videoId, @NotNull String userId, @NotNull String chunks) {

		if(validation.hasErrors()){
			play.Logger.error("Invalid params: %s", params);
			jsonError("Invalid params");
		}

		play.Logger.info("Cacho registration requested by user: "+userId+" for video: "+videoId);

		User registrationRequester = User.find("email=?", userId).first();
		Video video = Video.find("videoId=?", videoId).first();

		if(registrationRequester == null){
			play.Logger.error("No existe el cacho unregistration requester: %s", userId);
			jsonError("No existe el cacho unregistration requester "+userId);
		}

		if(video == null){
			play.Logger.error("El video del que se quieren desregistrar cachos no existe en el indice: %s", videoId);
			jsonError("El video del que se quieren desregistrar cachos no existe en el indice: "+videoId);
		}

		Map<Integer, String> chunkOrdinals = validChunksForVideo(video, chunks);
		if(chunkOrdinals == null || chunkOrdinals.isEmpty()){
			play.Logger.error("No chunks passed for unregister for video: "+videoId);
			jsonError("No chunks passed for unregister for video: "+videoId);
		}

		play.Logger.info("unregistering chunks "+chunkOrdinals+"for video "+videoId+" by user " + userId);

		if (video.unregisterChunks(registrationRequester, new ArrayList(chunkOrdinals.keySet()))) {
			jsonOk("unregistered chunks by: "+ userId +" for video: "+videoId+" - "+chunks);
		} else {
			play.Logger.error("unable to unregistered chunks "+ userId +" - "+videoId+" - "+chunks);
			jsonError("unable to unregistered chunks "+ userId +" - "+videoId+" - "+chunks);
		}

		jsonOk(chunkOrdinals);
	}

	public static Map<Integer, String> validChunksForVideo(Video video, String chunks) {

		Map<Integer, String> result = new HashMap<Integer, String>();

		for(String chunk : chunks.split("\\"+CHUNK_FOR_REGISTER_SEPARATOR)){
			String[] splittedChunk = chunk.split(CHUNK_SEPARATOR);


			int chunkOrdinal = Integer.parseInt(splittedChunk[0]);
			String chunkId = splittedChunk[1];
			if(isValidChunk(chunkOrdinal, chunkId, video)){
				
				if(video.chunks.get(chunkOrdinal) != null && video.chunks.get(chunkOrdinal).equals(chunkId)){
					result.put(chunkOrdinal, chunkId);
				} 
			}

		}
		return result;
	}

	private static boolean isValidChunk(int chunkOrdinal, String chunkId,
			Video video) {

		if(chunkOrdinal>=video.chunks.size()){
			play.Logger.warn("Trying to register chunk at position %s that is beyond video's size", chunkOrdinal);
			return false;
		}
		
		boolean valid = video.chunks.get(chunkOrdinal).equals(chunkId);
		if(!valid){
			play.Logger.warn("Intentando registrar un chunk no valido para video: %s,  posicion %s, chunkId: %s", video.videoId, chunkOrdinal, chunkId);
		}
		return valid;
	}

}
