import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.User;
import models.Video;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;


public class VideoCreationTest extends FunctionalTest {
	
	private long chunkSize = 1024*1024;
	private long lastChunkSize = chunkSize/2;//bytes
	private long videoLenght = chunkSize * 400 + lastChunkSize;
	private String chunkSeparator = "!";
	private String chunkForRegisterSeparator = "&";
	
	@Before
    public void setUp() {
        Fixtures.deleteDatabase();
    }

	@Test
	public void createVideo(){
		User user1 = new User("userId_1", "userId_1@gmail.com", "10.10.10.10", 1234);
		Assert.assertTrue(user1.create());
		
		
		List<String> chunks = new ArrayList<String>();
		for(int i = 0; i<401; i++) {
			chunks.add(Integer.toString(i));
		}
		/*
		 * alta de video
		 */
		Video video = new Video("videoId", "fileName", videoLenght, chunks, user1);
		
		Assert.assertTrue(video.create());
		
		registerChunks(user1, 0, 99, video);
		
		video.delete();
		
		Assert.assertTrue("No deberia haber videos",Video.count() == 0);
	}
	
	private void registerChunks(User user, int from, int to, Video video) {

		StringBuilder sb = new StringBuilder();
		for(int i = from; i<=to; i++){
			sb.append(""+i+chunkSeparator+i+chunkForRegisterSeparator);
		}
		String chunksForRegisterByUser = sb.toString();
		
		System.out.println("Chunks from "+from+" to "+to+" for user "+user.email+": "+chunksForRegisterByUser);
		
		video.save();
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("videoId",video.videoId);
		params.put("userId",user.email);
		params.put("chunks",chunksForRegisterByUser);

		callService("/videoService/registerChunks", params);
		
		video.refresh();
		
		for(int i = from; i<=to; i++){
			Assert.assertTrue("User ["+user.email+"] should have chunk: "+i,video.getChunksFrom(user).hasChunk(i));
		}
	}
	
	private void callService(String url, Map<String, String> params) {
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		
		Response response = POST(url, params);
        assertIsOk(response);
        assertContentType("application/json", response);
        assertCharset("utf-8", response);
	}
}
