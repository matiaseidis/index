package index;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import models.RetrievalPlan;
import models.User;
import models.UserCacho;
import models.UserChunk;
import models.UserChunks;
import models.Video;

import org.junit.Assert;
import org.junit.Test;

import plan.RetrievalPlanCreator;
import play.mvc.Http.Response;
import controllers.ChunkService;


public class RetrievalPlanTest extends BaseFunctionalTest{
	
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
	
	User user1 = null;
	User user2 = null;
	User user3 = null;
	User user4 = null;
	Video video = null;
	List<UserChunk> chunks = null;
	

	@Test
	public void testRestrievalPlan(){

		user1 = createUser(userId_1, userId_1+"@gmail.com", "10.10.10.10", 1234);
		user2 = createUser(userId_2, userId_2+"@gmail.com", "10.10.10.11", 1234);
		user3 = createUser(userId_3, userId_3+"@gmail.com", "10.10.10.12", 1234);
		user4 = createUser(userId_4, userId_4+"@gmail.com", "10.10.10.13", 1234);
		
		registerNewVideo(user1);
		
		registerChunksForAllUsers();
		
		/*
		 * retrieve plan
		 */
		RetrievalPlan retrievalPlan = new RetrievalPlanCreator(video, user1).generateRetrievalPlan();
		
		Assert.assertNotNull(retrievalPlan);
		
		List<UserCacho> userCachos = assertCachos(retrievalPlan, 4);

		assertTotalSize(retrievalPlan.getUserCachos());
		
		UserCacho primerCachoDelUser1 = cachoDelUser(userCachos, user1);
		UserCacho userCachoDelUser2 = cachoDelUser(userCachos, user2);
		UserCacho userCachoDelUser3 = cachoDelUser(userCachos, user3);
		UserCacho userCachoDelUser4 = cachoDelUser(userCachos, user4);
		
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

		userCachos = assertCachos(retrievalPlan, 5);
		
		primerCachoDelUser1 = primerCachoDelUser1(userCachos, user1);
		userCachoDelUser2 = cachoDelUser(userCachos, user2);
		userCachoDelUser3 = cachoDelUser(userCachos, user3);
		userCachoDelUser4 = cachoDelUser(userCachos, user4);
		UserCacho segundoCachoDelUser1 = segundoCachoDelUser1(userCachos, user1);
		
		assertTotalSize(retrievalPlan.getUserCachos());
		
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
		
		userCachos = assertCachos(retrievalPlan, 4);
		
		
		primerCachoDelUser1 = primerCachoDelUser1(userCachos, user1);
		userCachoDelUser2 = cachoDelUser(userCachos, user2);
		userCachoDelUser3 = cachoDelUser(userCachos, user3);
		segundoCachoDelUser1 = segundoCachoDelUser1(userCachos, user1);

		assertTotalSize(retrievalPlan.getUserCachos());

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
	
	
	private List<UserCacho> assertCachos(RetrievalPlan retrievalPlan, int cachos) {
		Assert.assertNotNull(retrievalPlan);
		Assert.assertTrue("Cachos: "+retrievalPlan.getUserCachos().size(),retrievalPlan.getUserCachos().size() == cachos);
		return retrievalPlan.getUserCachos();
	}


	private long assertTotalSize(List<UserCacho> userCachos) {
		long size = 0;
		for(UserCacho uc : userCachos) {
			size += uc.getCacho().lenght;
		}
		
		Assert.assertEquals(
				"Video size:"+videoLenght+" should be equal to the sum of all the retrieval plan cachos sizes: "+size,
				size, videoLenght);
		return size;
	}


	private void registerChunksForAllUsers() {
		registerChunks(user1, 0, 99, video);
		registerChunks(user2, 100, 199, video);
		registerChunks(user3, 200, 299, video);
		registerChunks(user4, 300, 400, video);
		
		/*
		 * load user cachos   
		 */
		chunks = video.getChunksFrom(user1).chunks;
		Assert.assertEquals(userId_1, 100, chunks.size());
		
		chunks = video.getChunksFrom(user2).chunks;
		Assert.assertEquals(userId_2, 100, chunks.size());
		
		chunks = video.getChunksFrom(user3).chunks;
		Assert.assertEquals(userId_3, 100, chunks.size());
		
		chunks = video.getChunksFrom(user4).chunks;
		Assert.assertEquals(userId_4, 101, chunks.size());
		
		
	}


	private User createUser(String userName, String email, String ip, int port) {
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("name", userName);
		params.put("email", email);
		params.put("ip", ip);
		params.put("port", Integer.toString(port));
		
		Response response = callService("/userService/create", params);
		super.codeOk(response);
		
		User user = User.find("email=?", email).first();
		Assert.assertNotNull(user);
		return user;
		
	}

	private UserCacho primerCachoDelUser1(List<UserCacho> userCachos, User user) {
		for(UserCacho uc : userCachos) {
			if(uc.getUser().email.equals(user.email)){
				if(uc.getCacho().from == 0)
					return uc;
			}
		}
		TestCase.fail();
		return null;
	}

	
	private UserCacho segundoCachoDelUser1(List<UserCacho> userCachos, User user) {
		for(UserCacho uc : userCachos) {
			if(uc.getUser().email.equals(user.email)){
				if(uc.getCacho().from != 0)
					return uc;
			}
		}
		TestCase.fail();
		return null;
	}


	private UserCacho cachoDelUser(List<UserCacho> userCachos, User user) {
		for(UserCacho uc : userCachos) {
			if(uc.getUser().email.equals(user.email)){
				return uc;
			}
		}
		TestCase.fail();
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

		callService("/chunkService/registerChunks", params);
		
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

		callService("/chunkService/unregisterChunks", params);
		
		video.refresh();
		
		for(int i = from; i<=to; i++){
			Assert.assertFalse("User should not have chunk: "+i,video.getChunksFrom(user).hasChunk(i));
		}
	}

	private Video registerNewVideo(User user) {
		
		List<String> chunks = new ArrayList<String>();
		for(int i = 0; i<401; i++) {
			chunks.add(Integer.toString(i));
		}
		/*
		 * alta de video
		 */
		video = new Video(videoId, fileName, videoLenght, chunks, user);
		
		Assert.assertTrue(video.create());
		return video;

	}
	
	private List<Integer> chunkOrdinalsForExistentVideo(Video video, String chunks) {
		List<Integer> result = new ArrayList<Integer>(
				ChunkService.chunkOrdinalsForExistentVideo(video, chunks).keySet()
				);
		return result;
	}

}
