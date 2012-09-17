package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.Play;
import play.db.jpa.Model;

@Entity
public class Video extends Model{

	private long chunkSize = Long.valueOf(Play.configuration.getProperty("chunk.size")) * 1024 * 1024;

	public String videoId; 
	public String fileName; 
	public long lenght; 

	/**
	 * Esto pasa a ser un map de int, String
	 * con el ordinal y el hash
	 */
	@ElementCollection
	public Map<Integer, String> chunks = new HashMap<Integer, String>();
	//	@OneToMany(cascade=CascadeType.ALL)
	//	public List<VideoChunk> chunks = new ArrayList<VideoChunk>();

	@ManyToOne
	public User addedBy;

	/**
	 * Esto pasa a ser una lista de UserCachos
	 */
	//	@OneToMany(cascade=CascadeType.ALL)
	//	public List<UserChunks> userChunks = new ArrayList<UserChunks>();
	@OneToMany(cascade=CascadeType.ALL)
	public List<UserCachos> userCachos = new ArrayList<UserCachos>();
	
	public Video(){}

	public Video(String videoId, String fileName, long lenght, List<String> plainChunks, User user) {
		this.fileName = fileName;
		this.videoId = videoId;
		this.lenght = lenght;
		this.addedBy = user;

		//		UserChunks uc = new UserChunks(user);
		UserCachos uc = new UserCachos(user);
		uc.addCacho(new Cacho(0L,lenght));

		for (int i = 0; i< plainChunks.size(); i++) {
			chunks.put(i, plainChunks.get(i));
			//			uc.addChunk(new UserChunk(i));
		}
		userCachos.add(uc);
	}

	@Override
	public String toString() {
		return videoId;
	}

	//	public UserChunks getChunksFrom(final User user) {
	public UserCachos getCachosFrom(final User user) {

		play.Logger.info("Video.getChunksFrom() "+user+" - "+this.userCachos+" - "+this.userCachos.size());

		//		for(UserChunks uc : this.userCachos){
		for(UserCachos uc : this.userCachos){

			play.Logger.debug("comparing %s and %s", uc.user.email, user.email);

			if (uc.user.email.equals(user.email) ) {
				return uc;
			}
		}
		//		UserChunks uc = new UserChunks(user);
		/*
		 * si no lo tiene, lo creo y lo devuelvo
		 */
		UserCachos uc = new UserCachos(user);
		this.userCachos.add(uc);
		save();
		return uc;
	}



	public boolean registerChunks(User user, List<Integer> ucList) {

		boolean someAdded = false;

		UserCachos cachos = this.getCachosFrom(user);


		//TODO soporte para registrar mas de un cacho por llamada
		//		List<Cacho> cachosNuevos = this.getCachosFromChunks(ucList, this.getTotalChunks(), this.lenght);
		//		someAdded = cachos.addCachos(cachosNuevos);


		Cacho cachoNuevo = this.getCachoFromChunks(ucList, this.getTotalChunks(), this.lenght);

		someAdded = cachos.addCacho(cachoNuevo);

		if(someAdded){
			save();
		}

		return someAdded;
	}

	//TODO soporte para registrar mas de un cacho por llamada
	private List<Cacho> getCachosFromChunks(List<Integer> ucList,
			int totalChunks, long videoLenght) {

		List<Cacho> cachos = new ArrayList<Cacho>();

		List<List<Integer>> ucChunksConsecutivosList = calcularCachos(ucList);

		for (List<Integer> chunksCachoNuevo : ucChunksConsecutivosList){
			cachos.add(getCachoFromChunks(chunksCachoNuevo, totalChunks, videoLenght));
		}

		return cachos;
	}

	//TODO soporte para registrar mas de un cacho por llamada
	private List<List<Integer>> calcularCachos(List<Integer> ucList) {

		List<List<Integer>> result = new ArrayList<List<Integer>>();

		int lastChunkPosition = ucList.get(0);
		List<Integer> currentChunkList = new ArrayList<Integer>();

		for(int i : ucList){
			if(i == lastChunkPosition){
				currentChunkList.add(i);
				continue;
			}
			if(i == lastChunkPosition+1 ){
				currentChunkList.add(i);
				lastChunkPosition++;
				continue;
			}
			result.add(currentChunkList);
			currentChunkList = new ArrayList<Integer>();
			currentChunkList.add(i);
			lastChunkPosition = i;
		}

		return result;
	}

	//TODO soporte para registrar mas de un cacho por llamada
	public Cacho getCachoFromChunks(List<Integer> ucList, int totalChunks, long videoLenght) {

		long from = ucList.get(0)*chunkSize;
		long last = ucList.get(ucList.size() -1 ); 
		long lenght = (ucList.size() - from) * chunkSize;
		if(last == totalChunks-1 && videoLenght % chunkSize != 0){
			long diff = chunkSize - videoLenght % chunkSize;
			lenght-= diff;
		}
		return new Cacho(from, lenght);
	}

	public synchronized boolean unregisterChunks(User user,
			List<Integer> chunksToRemove) {

		boolean removed = false;

		UserCachos cachos = this.getCachosFrom(user);
		Cacho cacho = this.getCachoFromChunks(chunksToRemove, this.getTotalChunks(), this.lenght);
		removed = cachos.removeCacho(cacho);
		save();
		return removed;
	}

	public int getTotalChunks(){
		int total = this.chunks.size();
		return total;
	}


}


