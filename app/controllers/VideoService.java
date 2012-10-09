package controllers;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import utils.SiteNotifier;

import models.User;
import models.Video;
import net.sf.oval.constraint.NotNull;

public class VideoService extends BaseService {
	
	public static void registerVideo(@NotNull String videoId, @NotNull  String fileName, @NotNull Long lenght, @NotNull String userId, @NotNull String chunks){
		
		if(validation.hasErrors()){
			play.Logger.error("Invalid params: %s", params);
			jsonError("Invalid params");
		}
		
		play.Logger.info("Video registration requested by user: "+userId+" for video: "+videoId);
		
		User registrationRequester = User.find("email", userId).first();
		Video video = Video.find("videoId", videoId).first();
		
		if(registrationRequester == null){
			play.Logger.error("No existe el registrationRequester: %s", userId);
			jsonError("No existe el registrationRequester "+userId);
		}
		
		if(video != null){
			play.Logger.error("El video que se quiere registrar ya existe en el indice: %s", videoId);
			String extraInfo = " ya existia para el requester. No se hace nada";
			if(video.registerFullVideoFor(registrationRequester) ) {
				extraInfo = "se registro para el requester";
			}
			jsonOk("El video "+videoId+" que se quiere registrar ya existe en el indice - "+extraInfo);
		}
		
		video = new Video(videoId, fileName, lenght, chunkIds(chunks), registrationRequester);
		
		try{
			video = video.save();
			play.Logger.info("Se registro el video <id: "+videoId+"><fileName: "+fileName+"> en el indice");
			
			new SiteNotifier().notifyNewVideo(video);
			
			jsonOk("Se registro el video <id: "+videoId+"><fileName: "+fileName+"> en el indice");
		} catch(Exception e) {
			play.Logger.error("No se pudo registrar el video <id: "+videoId+"><fileName: "+fileName+"> en el indice");
			jsonError("No se pudo registrar el video <id: "+videoId+"><fileName: "+fileName+"> en el indice");
		}
		
	}
	
	

	private static List<String> chunkIds(String chunks) {
		return Arrays.asList(chunks.split(CHUNK_FOR_REGISTER_SEPARATOR));
	}
}

