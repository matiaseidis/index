package index;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.User;
import models.Video;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import play.Play;
import play.libs.WS.HttpResponse;
import play.mvc.Http.Response;
import play.test.Fixtures;


public class VideoServiceRegistrationTest extends BaseFunctionalTest {
	
	int chunkSize = Integer.valueOf(Play.configuration.getProperty("chunk.size"))*1024*1024; 

	
	@Before
    public void setUp() {
        Fixtures.deleteDatabase();
    }

	@Test
	public void createVideo(){
		User user1 = new User("userId_1", "userId_1@gmail.com", "10.10.10.10", 1234, 10002);
		
		createUserOnSite(user1);
		
		Assert.assertTrue(user1.create());
		
		String videoId = "videoId-test";
		int totalChunks = 401;
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("videoId", videoId);
		params.put("fileName", "fileName");
		params.put("lenght", Integer.toString(totalChunks*1024*1024));
		params.put("userId", user1.email);
		params.put("chunks", videoChunks(totalChunks));
		
		Response response = callService("/videoService/registerVideo", params);
		
		Assert.assertTrue( "Videos: "+Video.count(), Video.count() == 1);
		
		codeOk(response);
		
		Assert.assertEquals(1, Video.count());

		/*
		 * creamos el video en el site 
		 */
		params.put("sharedByEmail", user1.email);
		HttpResponse siteResponse = callSiteService(Play.configuration.getProperty("site.service.new.video"), params);
		play.Logger.info("%s",siteResponse.getJson());
		Assert.assertTrue(siteResponse.getJson().getAsJsonObject().get("code").getAsString().equalsIgnoreCase("ok"));
		siteResponse = callSiteService(Play.configuration.getProperty("site.service.get.video"), params);
		String code = siteResponse.getJson().getAsJsonObject().get("code").getAsString();
		Assert.assertTrue("Code not ok", code.equalsIgnoreCase("ok"));
		
		Video video = Video.find("videoId=?", videoId).first();

		chunkOperation("unregister", user1, 0, video.getTotalChunks() -1 , video);
		
		video.delete();
		
		Assert.assertEquals("No deberia haber videos", 0, Video.count());
	}
	
	private void createUserOnSite(User user) {
		
		Map<String, String> siteParams = new HashMap<String, String>();
		siteParams.put("email", user.email);
		siteParams.put("servlePort", Integer.toString(user.servlePort));
		siteParams.put("name", user.name);
		String newUserServiceUrl = Play.configuration.getProperty("site.service.new.user");
		HttpResponse siteResponse = callSiteService(newUserServiceUrl, siteParams);
		Assert.assertTrue(siteResponse.getJson().getAsJsonObject().get("code").getAsString().equalsIgnoreCase("ok"));
		
		String checkUserServiceUrl = Play.configuration.getProperty("site.service.get.user");
		
		siteResponse = callSiteService(checkUserServiceUrl, siteParams);
		String code = siteResponse.getJson().getAsJsonObject().get("code").getAsString();
		Assert.assertTrue("Code not ok", code.equalsIgnoreCase("ok"));
		
	}

	private void chunkOperation(String action, User user, int from, int to, Video video) {

		String chunksForRegisterByUser = chunks(from, to);
		
		play.Logger.info("Chunks from "+from+" to "+to+" for user "+user.email+": "+chunksForRegisterByUser);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("videoId",video.videoId);
		params.put("userId",user.email);
		params.put("chunks",chunksForRegisterByUser);

		callService("/chunkService/"+action+"Chunks", params);
		
		video.refresh();
		
		
			if(action.equals("register")) {
				Assert.assertTrue("there should be un cacho", video.getCachosFrom(user).cachos.size() == 1);
				Assert.assertTrue("there should be un cacho of "+from+" - "+to+", instead of "+video.getCachosFrom(user).cachos.get(0).lenght, video.getCachosFrom(user).cachos.get(0).lenght == (to-from)*chunkSize);
				
			} else {
				Assert.assertTrue("there should be no chunks, instead of "+video.getCachosFrom(user).cachos.size(), video.getCachosFrom(user).cachos.size() == 0);
			}
		
		
//		for(int i = from; i<=to; i++){
//			if(action.equals("register")) {
//				Assert.assertTrue("there should be 100 chunks, instead of "+video.getCachosFrom(user).chunks.size(), video.getCachosFrom(user).chunks.size()==100);
//				Assert.assertTrue("User ["+user.email+"] should have chunk: "+i,video.getCachosFrom(user).hasChunk(i));
//			} else {
//				Assert.assertTrue("there should be no chunks, instead of "+video.getCachosFrom(user).chunks.size(), video.getCachosFrom(user).chunks.size()==0);
//				Assert.assertFalse("User ["+user.email+"] should not have chunk: "+i,video.getCachosFrom(user).hasChunk(i));
//			}
//		}
	}
	
	private String chunks(int from, int to) {
		
		StringBuilder sb = new StringBuilder();
		for(int i = from; i<=to; i++){
			sb.append(""+i+CHUNK_SEPARATOR+i+CHUNK_FOR_REGISTER_SEPARATOR);
		}
		return sb.toString();
	}

	private String videoChunks(int total) {
		List<String> chunks = new ArrayList<String>();
		for(int i = 0; i<total; i++) {
			chunks.add(Integer.toString(i));
		}
		/*
		 * alta de video
		 */
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i<chunks.size(); i++) {
			sb.append(chunks.get(i) );
			if(i != chunks.size()-1) {
				sb.append(CHUNK_FOR_REGISTER_SEPARATOR);
			}
		}
		
		return sb.toString();
	}
}
