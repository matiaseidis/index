package controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.User;
import models.Video;
import net.sf.oval.constraint.NotNull;

import org.apache.commons.collections.MapUtils;

import play.mvc.Controller;
import controllers.response.Ok;
import controllers.response.TodoMal;

public class VideoService extends BaseService {

	public static void registerVideo(@NotNull String videoId, @NotNull  String fileName, @NotNull Long lenght, @NotNull String userId, @NotNull String chunks){
		
		if(validation.hasErrors()){
			play.Logger.error("Invalid params: %s", params);
			jsonError("Invalid params");
		}
		
		play.Logger.info("Video registration requested by user: "+userId+" for video: "+videoId);
		
		User registrationRequester = User.find("userId=?", userId).first();
		Video video = Video.find("videoId=?", videoId).first();
		
		if(registrationRequester == null){
			play.Logger.error("No existe el registrationRequester: %s", userId);
			jsonError("No existe el registrationRequester "+userId);
		}
		
		if(video != null){
			play.Logger.error("El video que se quiere registrar ya existe en el indice: %s", videoId);
			jsonError("El video que se quiere registrar ya existe en el indice: "+videoId);
		}
		
		video = new Video(videoId, fileName, lenght, chunkIds(chunks), registrationRequester);
		
		try{
			video = video.save();
			play.Logger.info("Se registro el video <id: "+videoId+"><fileName: "+fileName+"> en el indice");
			jsonOk("Se registro el video <id: "+videoId+"><fileName: "+fileName+"> en el indice");
		} catch(Exception e) {
			play.Logger.error("No se pudo registrar el video <id: "+videoId+"><fileName: "+fileName+"> en el indice");
			jsonError("No se pudo registrar el video <id: "+videoId+"><fileName: "+fileName+"> en el indice");
		}
		
	}

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
		
		for(String chunk : chunks.split("\\&")){
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
	
	private static List<String> chunkIds(String chunks) {
		return Arrays.asList(chunks.split("!"));
	}
}

