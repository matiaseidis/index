package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import models.User;
import models.Video;
import net.sf.oval.constraint.NotNull;

import org.apache.commons.collections.MapUtils;

import play.Play;

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
		
		Map<Integer, String> chunkOrdinals = chunkOrdinalsForExistentVideo(video, chunks);
		
		if(MapUtils.isEmpty(chunkOrdinals)){
			play.Logger.error("No chunks passed for register for video: "+videoId);
			jsonError("No chunks passed for register for video: "+videoId);
		}
		
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
		
		Map<Integer, String> chunkOrdinals = chunkOrdinalsForExistentVideo(video, chunks);
		if(MapUtils.isEmpty(chunkOrdinals)){
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
	
	public static Map<Integer, String> chunkOrdinalsForExistentVideo(Video video, String chunks) {

		Map<Integer, String> result = new HashMap<Integer, String>();
		
		for(String chunk : chunks.split("\\"+CHUNK_FOR_REGISTER_SEPARATOR)){
			String[] splittedChunk = chunk.split("!");
			int chunkOrdinal = Integer.parseInt(splittedChunk[0]);
			String chunkId = splittedChunk[1];
			
			if(chunkOrdinal > video.chunks.size()) {
				throw new IllegalArgumentException("Trying to register chunk that is beyond video's size");
			}
			
			if(video.chunks.get(chunkOrdinal) != null && video.chunks.get(chunkOrdinal).hash.equals(chunkId)){
				result.put(chunkOrdinal, chunkId);
			} 
		}
		return result;
	}

}
