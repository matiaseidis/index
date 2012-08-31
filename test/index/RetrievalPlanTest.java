package index;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.RetrievalPlan;
import models.User;
import models.UserCacho;
import models.UserChunk;
import models.Video;

import org.apache.commons.collections.Transformer;
import org.junit.Assert;
import org.junit.Test;

import plan.RetrievalPlanCreator;
import play.Play;
import play.mvc.Http.Response;


public class RetrievalPlanTest extends BaseFunctionalTest{
	
	String videoId = "test-video-id";
	String fileName = "test-file-name";

	String chunkSeparator = Play.configuration.getProperty("chunk.separator");
	String chunkForRegisterSeparator = Play.configuration.getProperty("chunk.registration.separator");
	int maxChunkAjenoSize = Integer.valueOf(Play.configuration.getProperty("max.cacho.size")); 
	int primerCachoMaxSize = Integer.valueOf(Play.configuration.getProperty("first.cacho.size"));
	
	/**
	 * Crea un video de 224,5MB, lo registra entero para 4 usuarios.
	 * Pide un plan con un usuario que no tiene ningun cacho de ese video.
	 * 
	 * Espera 5 cachos por el round robin, no deberia ocurrir un merge de cachos consecutivos para el mismo usuario
	 * Espera un primer cacho de Integer.valueOf(Play.configuration.getProperty("first.cacho.size"))
	 * luego, 3 cachos de Integer.valueOf(Play.configuration.getProperty("max.cacho.size"))
	 * y por ultimo, uno de 0.5 MB.
	 */
	@Test 
	public void testForSeveralUsersWithTheFullVideo() {
		/*
		 * crea usuarios
		 */
		int usersWithCachos = 4;
		int mega = 1024*1024;
		int totalChunks = primerCachoMaxSize + (usersWithCachos-1)*maxChunkAjenoSize + 1;
		long vLenght = totalChunks*mega - mega/2;
		
		User register = createUser("register", "register@gmail.com", "10.10.10.1", 9999);
		Video v = registerNewVideo(register, totalChunks, vLenght);
		registerChunks(register, 0, totalChunks -1, v);

		User pobreton = createUser("pobreton", "pobreton@gmail.com", "1.1.1.1", 9999);
		
		for(int i = 1; i<usersWithCachos;i++) {
			User u = createUser("userId_"+i, "userId_full_video_"+i+"@gmail.com", "10.10.10.1"+i, i);
			registerChunks(u, 0, totalChunks -1, v);
		}
		
		RetrievalPlan p = new RetrievalPlanCreator(v, pobreton).generateRetrievalPlan();
		
		/*
		 * el primer cacho es mas corto que los demas, y por el round robin siempre voy a duplicar un usuario pero recien en el reintento
		 */
		List<UserCacho> userCachos = assertCachos(p, usersWithCachos+1);
		
		Assert.assertEquals(primerCachoMaxSize*1024*1024, p.getUserCachos().get(0).getCacho().lenght);
		Assert.assertEquals(maxChunkAjenoSize*1024*1024, p.getUserCachos().get(1).getCacho().lenght);
		Assert.assertEquals(maxChunkAjenoSize*1024*1024, p.getUserCachos().get(2).getCacho().lenght);
		Assert.assertEquals(maxChunkAjenoSize*1024*1024, p.getUserCachos().get(3).getCacho().lenght);
		Assert.assertEquals((1024*1024)/2, p.getUserCachos().get(4).getCacho().lenght);

		assertTotalSize(vLenght, p.getUserCachos());
		
		Set<String> uniqueUsers = new HashSet<String>(org.apache.commons.collections.CollectionUtils.collect(userCachos, new Transformer() {
			
			@Override
			public Object transform(Object arg0) {
				UserCacho userCacho = (UserCacho)arg0;
				return userCacho.getUser().email;
			}
		}));
		
		Assert.assertEquals(uniqueUsers.size(), usersWithCachos);
		for(int i = 1; i<usersWithCachos;i++) {
			Assert.assertTrue(uniqueUsers.contains("userId_full_video_"+i+"@gmail.com"));
		}
	}
	
	
	private List<UserCacho> assertCachos(RetrievalPlan rp, int cachos) {
		Assert.assertNotNull(rp);
		Assert.assertTrue("Cachos for retrieval plan: "+rp.getUserCachos().size()+" and no "+cachos
				,rp.getUserCachos().size() == cachos);
		return rp.getUserCachos();
	}

	private long assertTotalSize(long videoLenght, List<UserCacho> userCachos) {
		long size = 0;
		for(UserCacho uc : userCachos) {
			size += uc.getCacho().lenght;
		}
		
		Assert.assertEquals(
				"Video size:"+videoLenght+" should be equal to the sum of all the retrieval plan cachos sizes: "+size,
				size, videoLenght);
		return size;
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
		
		List<UserChunk> chunks = video.getChunksFrom(user).chunks;
		Assert.assertEquals(user.email, to+1, chunks.size()); // <to> is zero based
	}
	
	private Video registerNewVideo(User user, int totalChunks, long lenght) {
		
		List<String> chunks = new ArrayList<String>();
		for(int i = 0; i<totalChunks; i++) {
			chunks.add(Integer.toString(i));
		}
		/*
		 * alta de video
		 */
		double random = Math.random()*1000;

		Video video = new Video(videoId+random, fileName+random, lenght, chunks, user);
		
		Assert.assertTrue(video.create());
		Assert.assertTrue(video.chunks.size() == totalChunks);
		return video;
	}
}
