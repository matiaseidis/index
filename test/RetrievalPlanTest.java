

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.RetrievalPlan;
import models.User;
import models.UserCacho;
import models.UserChunk;
import models.UserChunks;
import models.Video;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import plan.RetrievalPlanCreator;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import controllers.VideoService;


public class RetrievalPlanTest extends FunctionalTest{
	
	/*
	 * FIXME registrar usuario y pedir plan por WS, no por api
	 */
	
	
	private String videoId = "test-video-id";
	private String fileName = "test-file-name";

	private long chunkSize = 1024*1024;
	private long lastChunkSize = chunkSize/2;//bytes
	private long videoLenght = chunkSize * 400 + lastChunkSize;
	private String chunkSeparator = "!";
	private String chunkForRegisterSeparator = "&";
	
	private String userId_1 = "test-user-id-1";
	private String userId_2 = "test-user-id-2";
	private String userId_3 = "test-user-id-3";
	private String userId_4 = "test-user-id-4";

	String videoChunks;
	
	@Before
    public void setUp() {
        Fixtures.deleteDatabase();
    }
	
	@Test
	public void testRestrievalPlan(){

//		Tracking tracking = new Tracking();
		
		User user1 = new User(userId_1, userId_1+"@gmail.com", "10.10.10.10", 1234);
		Assert.assertTrue(user1.create());
		User user2 = new User(userId_2, userId_2+"@gmail.com", "10.10.10.10", 1234);
		Assert.assertTrue(user2.create());
		User user3 = new User(userId_3, userId_3+"@gmail.com", "10.10.10.10", 1234);
		Assert.assertTrue(user3.create());
		User user4 = new User(userId_4, userId_4+"@gmail.com", "10.10.10.10", 1234);
		Assert.assertTrue(user4.create());
		
		Video video = registerNewVideo(user1);
		registerChunks(user1, 0, 99, video);
		registerChunks(user2, 100, 199, video);
		registerChunks(user3, 200, 299, video);
		registerChunks(user4, 300, 400, video);
		
		/*
		 * load user cachos   
		 */
		List<UserChunk> chunks = video.getChunksFrom(user1).chunks;
		Assert.assertEquals(userId_1, 100, chunks.size());
		
		chunks = video.getChunksFrom(user2).chunks;
		Assert.assertEquals(userId_2, 100, chunks.size());
		
		chunks = video.getChunksFrom(user3).chunks;
		Assert.assertEquals(userId_3, 100, chunks.size());
		
		chunks = video.getChunksFrom(user4).chunks;
		Assert.assertEquals(userId_4, 101, chunks.size());
		
		/*
		 * retrieve plan
		 */
		RetrievalPlan retrievalPlan = new RetrievalPlanCreator(video, user1).generateRetrievalPlan();
		
		Assert.assertNotNull(retrievalPlan);
		List<UserCacho> userCachos = retrievalPlan.getUserCachos(); 
		
		Assert.assertTrue(retrievalPlan.getUserCachos().size() == 4);

		UserCacho primerCachoDelUser1 = cachoDelUser(userCachos, user1);
		UserCacho userCachoDelUser2 = cachoDelUser(userCachos, user2);
		UserCacho userCachoDelUser3 = cachoDelUser(userCachos, user3);
		UserCacho userCachoDelUser4 = cachoDelUser(userCachos, user4);
		
		Assert.assertNotNull(primerCachoDelUser1);
		Assert.assertNotNull(userCachoDelUser2);
		Assert.assertNotNull(userCachoDelUser3);
		Assert.assertNotNull(userCachoDelUser4);



		Assert.assertEquals(primerCachoDelUser1.getUser().email, chunkSize*100, primerCachoDelUser1.getCacho().lenght);
		Assert.assertEquals(userCachoDelUser2.getUser().email, chunkSize*100, userCachoDelUser2.getCacho().lenght);
		Assert.assertEquals(userCachoDelUser3.getUser().email, chunkSize*100, userCachoDelUser3.getCacho().lenght);
		Assert.assertEquals(userCachoDelUser4.getUser().email, chunkSize*100+lastChunkSize, userCachoDelUser4.getCacho().lenght);
		
		long retrievalSize = primerCachoDelUser1.getCacho().lenght +
				userCachoDelUser2.getCacho().lenght +
				userCachoDelUser3.getCacho().lenght +
				userCachoDelUser4.getCacho().lenght;
		
		Assert.assertEquals(
				"Video size:"+videoLenght+" should be equal to the sum of all the retrieval plan cachos sizes: "+retrievalSize,
				retrievalSize, videoLenght);
		
		/*
		 * registro el mismo chunk que registre con el user 4, con el user 1, que es el que pide el plan.
		 * El plan deberia ignorar al user 4 hasta el ultimo chunk
		 */
		registerChunks(user1, 300, 399, video);
		
		chunks = video.getChunksFrom(user1).chunks;
		Assert.assertEquals(userId_1, chunks.size(), 200);
		/*
		 * retrieve plan
		 */
		retrievalPlan = new RetrievalPlanCreator(video, user1).generateRetrievalPlan();

		Assert.assertNotNull(retrievalPlan);
		userCachos = retrievalPlan.getUserCachos(); 
		Assert.assertEquals(5, userCachos.size());
		
		primerCachoDelUser1 = primerCachoDelUser1(userCachos, user1);
		userCachoDelUser2 = cachoDelUser(userCachos, user2);
		userCachoDelUser3 = cachoDelUser(userCachos, user3);
		userCachoDelUser4 = cachoDelUser(userCachos, user4);
		UserCacho segundoCachoDelUser1 = segundoCachoDelUser1(userCachos, user1);
		
		retrievalSize = primerCachoDelUser1.getCacho().lenght +
				segundoCachoDelUser1.getCacho().lenght +
				userCachoDelUser2.getCacho().lenght +
				userCachoDelUser3.getCacho().lenght +
				userCachoDelUser4.getCacho().lenght;
		
		Assert.assertEquals(
				"Video size:"+videoLenght+" should be equal to the sum of all the retrieval plan cachos sizes: "+retrievalSize,
				retrievalSize, videoLenght);
		
		Assert.assertNotNull(primerCachoDelUser1);
		Assert.assertNotNull(userCachoDelUser2);
		Assert.assertNotNull(userCachoDelUser3);
		Assert.assertNotNull(userCachoDelUser4);
		Assert.assertNotNull(segundoCachoDelUser1);
		
		Assert.assertTrue(primerCachoDelUser1.getUser().email.equals(user1.email));
		Assert.assertTrue(userCachoDelUser2.getUser().email.equals(user2.email));
		Assert.assertTrue(userCachoDelUser3.getUser().email.equals(user3.email));
		Assert.assertTrue(segundoCachoDelUser1.getUser().email.equals(user1.email));
		Assert.assertTrue(userCachoDelUser4.getUser().email.equals(user4.email));
		
		
		/*
		 * registro el mismo chunk que registre con el user 4, con el user 1, que es el que pide el plan.
		 * El plan no deberia incluir al user 4
		 */
		registerChunks(user1, 400, 400, video);
		
		chunks = video.getChunksFrom(user1).chunks;
		Assert.assertEquals(userId_1, chunks.size(), 201);
		/*
		 * retrieve plan
		 */
		retrievalPlan = new RetrievalPlanCreator(video, user1).generateRetrievalPlan(); 
		
		Assert.assertNotNull(retrievalPlan);
		userCachos = retrievalPlan.getUserCachos(); 
		Assert.assertEquals(4, userCachos.size());
		
		
		primerCachoDelUser1 = primerCachoDelUser1(userCachos, user1);
		userCachoDelUser2 = cachoDelUser(userCachos, user2);
		userCachoDelUser3 = cachoDelUser(userCachos, user3);
		segundoCachoDelUser1 = segundoCachoDelUser1(userCachos, user1);

		retrievalSize = primerCachoDelUser1.getCacho().lenght +
				segundoCachoDelUser1.getCacho().lenght +
				userCachoDelUser2.getCacho().lenght +
				userCachoDelUser3.getCacho().lenght;
		
		Assert.assertEquals(
				"Video size:"+videoLenght+" should be equal to the sum of all the retrieval plan cachos sizes: "+retrievalSize,
				retrievalSize, videoLenght);
		
		Assert.assertTrue(primerCachoDelUser1.getUser().email.equals(user1.email));
		Assert.assertTrue(userCachoDelUser2.getUser().email.equals(user2.email));
		Assert.assertTrue(userCachoDelUser3.getUser().email.equals(user3.email));
		Assert.assertTrue(segundoCachoDelUser1.getUser().email.equals(user1.email));
		
		
		Assert.assertEquals(primerCachoDelUser1.getCacho().from, 0);
		Assert.assertEquals(userCachoDelUser2.getCacho().from, chunkSize*100);
		Assert.assertEquals(userCachoDelUser3.getCacho().from, chunkSize*200);
		Assert.assertEquals(segundoCachoDelUser1.getCacho().from, chunkSize*300);
		
		Assert.assertEquals(primerCachoDelUser1.getCacho().lenght, chunkSize*100);
		Assert.assertEquals(userCachoDelUser2.getCacho().lenght, chunkSize*100);
		Assert.assertEquals(userCachoDelUser3.getCacho().lenght, chunkSize*100);
		Assert.assertEquals(segundoCachoDelUser1.getCacho().lenght, chunkSize*100 + chunkSize/2);
		
		
		
		/*
		 * unregister
		 */
		unregisterChunks(user4, 300, 400, video);

		/*
		 * load user cachos   
		 */
		UserChunks uc = video.getChunksFrom(user4);
		Assert.assertEquals(user4.email, uc.chunks.size(), 0);
	}
	
	
	private UserCacho primerCachoDelUser1(List<UserCacho> userCachos, User user) {
		for(UserCacho uc : userCachos) {
			if(uc.getUser().email.equals(user.email)){
				if(uc.getCacho().from == 0)
					return uc;
			}
		}
		return null;
	}

	
	private UserCacho segundoCachoDelUser1(List<UserCacho> userCachos, User user) {
		for(UserCacho uc : userCachos) {
			if(uc.getUser().email.equals(user.email)){
				if(uc.getCacho().from != 0)
					return uc;
			}
		}
		return null;
	}


	private UserCacho cachoDelUser(List<UserCacho> userCachos, User user) {
		for(UserCacho uc : userCachos) {
			if(uc.getUser().email.equals(user.email)){
				return uc;
			}
		}
		return null;
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
	
	private void unregisterChunks(User user, int from, int to, Video video) {

		StringBuilder sb = new StringBuilder();
		for(int i = from; i<=to; i++){
			sb.append(""+i+chunkSeparator+i+chunkForRegisterSeparator);
		}
		String chunksForUnregisterByUser = sb.toString();
		
		play.Logger.info("Chunks from "+from+" to "+to+" for user "+user.email+": "+chunksForUnregisterByUser);

		video.save();
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("videoId",video.videoId);
		params.put("userId",user.email);
		params.put("chunks",chunksForUnregisterByUser);

		callService("/videoService/unregisterChunks", params);
		
		video.refresh();
		
		for(int i = from; i<=to; i++){
			Assert.assertFalse("User should not have chunk: "+i,video.getChunksFrom(user).hasChunk(i));
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


	private Video registerNewVideo(User user) {
		
		List<String> chunks = new ArrayList<String>();
		for(int i = 0; i<401; i++) {
			chunks.add(Integer.toString(i));
		}
		/*
		 * alta de video
		 */
		Video video = new Video(videoId, fileName, videoLenght, chunks, user);
		
		Assert.assertTrue(video.create());
		return video;

	}
	
	private List<Integer> chunkOrdinalsForExistentVideo(Video video, String chunks) {
		List<Integer> result = new ArrayList<Integer>(
				VideoService.chunkOrdinalsForExistentVideo(video, chunks).keySet()
				);
		
		return result;
	}

}
