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

import com.google.gson.JsonObject;

import play.Play;
import play.mvc.Http.Response;
import play.test.Fixtures;


public class VideoCreationTest extends BaseFunctionalTest {
	
	@Before
    public void setUp() {
        Fixtures.deleteDatabase();
    }

	@Test
	public void createVideo(){
		User user1 = new User("userId_1", "userId_1@gmail.com", "10.10.10.10", 1234);
		Assert.assertTrue(user1.create());
		
		
		
		String videoId = "videoId";
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("videoId", videoId);
		params.put("fileName", "fileName");
		params.put("lenght", Integer.toString(123456));
		params.put("userId", user1.email);
		params.put("chunks", videoChunks());
		
		Response response = callService("/videoService/registerVideo", params);
		
		Assert.assertTrue( Video.count() == 1);
		
		codeOk(response);
		
		Assert.assertEquals(1, Video.count());
		
		Video video = Video.find("videoId=?", videoId).first();
		
		chunkOperation("register", user1, 0, 99, video);
		
		chunkOperation("unregister", user1, 0, 99, video);
		
		video.delete();
		
		Assert.assertEquals("No deberia haber videos", 0, Video.count());
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
		
		for(int i = from; i<=to; i++){
			if(action.equals("register")) {
				Assert.assertTrue("there should be 100 chunks, instead of "+video.getChunksFrom(user).chunks.size(), video.getChunksFrom(user).chunks.size()==100);
				Assert.assertTrue("User ["+user.email+"] should have chunk: "+i,video.getChunksFrom(user).hasChunk(i));
			} else {
				Assert.assertTrue("there should be no chunks, instead of "+video.getChunksFrom(user).chunks.size(), video.getChunksFrom(user).chunks.size()==0);
				Assert.assertFalse("User ["+user.email+"] should not have chunk: "+i,video.getChunksFrom(user).hasChunk(i));
			}
		}
	}
	
	private String chunks(int from, int to) {
		
		StringBuilder sb = new StringBuilder();
		for(int i = from; i<=to; i++){
			sb.append(""+i+CHUNK_SEPARATOR+i+CHUNK_FOR_REGISTER_SEPARATOR);
		}
		return sb.toString();
	}

	private String videoChunks() {
		List<String> chunks = new ArrayList<String>();
		for(int i = 0; i<401; i++) {
			chunks.add(Integer.toString(i));
		}
		/*
		 * alta de video
		 */
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i<chunks.size(); i++) {
			sb.append(chunks.get(i) );
			if(i != chunks.size()-1) {
				sb.append(CHUNK_SEPARATOR);
			}
		}
		
		return sb.toString();
	}
}
