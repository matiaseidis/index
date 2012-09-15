package index;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Cacho;
import models.RetrievalPlan;
import models.User;
import models.UserCacho;
import models.UserCachos;
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
	
	int dimonPort = 10002;

	long chunkSize = Integer.valueOf(Play.configuration.getProperty("chunk.size"))*1024*1024; 
	
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
		
		User register = createUser("register", "register@gmail.com", "10.10.10.1", 9999, dimonPort);
		Video v = registerNewVideo(register, totalChunks, vLenght);
//		registerChunks(register, 0, totalChunks -1, v);

		User pobreton = createUser("pobreton", "pobreton@gmail.com", "1.1.1.1", 9999, dimonPort);
		
		for(int i = 1; i<usersWithCachos;i++) {
			
			String uName = "userId_"+i;
			String uEmail = "userId_full_video_"+i+"@gmail.com";
			String uIp = "10.10.10.1"+i;
			
			User u = createUser(uName, uEmail, uIp, i, dimonPort);
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

	private User createUser(String userName, String email, String ip, int servlePort, int dimonPort) {
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("name", userName);
		params.put("email", email);
		params.put("ip", ip);
		params.put("servlePort", Integer.toString(servlePort));
		params.put("dimonPort", Integer.toString(dimonPort));
		
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
		
		List<Integer> ucList = new ArrayList<Integer>();
		for(int i = from; i<=to; i++){
			ucList.add(i);
		}
		
		Cacho real =  video.getCachosFrom(user).cachos.get(0);
		Cacho expected = video.getCachoFromChunks(ucList, video.getTotalChunks(), video.lenght);
		
		Assert.assertTrue(
				"Video lenght: "+video.lenght+" - User ["+user.email+"] should have cacho from : "+from+" of "+(to-from)*chunkSize+
				" but have "+real.start+" - "+real.lenght, 
				real.equals(expected));
		
		UserCachos userCachos = video.getCachosFrom(user);
		Assert.assertEquals(user.email, 1, userCachos.cachos.size()); // <to> is zero based
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
		int vChunks = video.chunks.size();
		Assert.assertTrue(vChunks == totalChunks);
		return video;
	}
}
