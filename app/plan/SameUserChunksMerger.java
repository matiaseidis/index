package plan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.UserChunks;

public class SameUserChunksMerger {

	public Set<UserChunks> mergeChunks(Set<UserChunks> result) {

		Set<UserChunks> toRemove = new HashSet<UserChunks>();
		Set<UserChunks> toAdd = new HashSet<UserChunks>();

		Map<String, List<UserChunks>> chunksMap = new HashMap<String, List<UserChunks>>();

		for(UserChunks uc : result) {
			if(chunksMap.get(uc.user.email) == null){
				chunksMap.put(uc.user.email, new ArrayList<UserChunks>());
			}
			chunksMap.get(uc.user.email).add(uc);
		}

		for(List<UserChunks> userChunksList : chunksMap.values()) {
			
			Collections.sort(userChunksList, new ChunkPositionComparator());

			for(int i = 0; i<userChunksList.size(); i++){
				if(i == (userChunksList.size()-1)){
					/*
					 * ultimo cacho del user, nada que mergear
					 */
					break;
				}
				if(userChunksList.get(i).higherChunkPosition()+1 == userChunksList.get(i+1).lowerChunkPosition()){
					toRemove.add(userChunksList.get(i));
					toRemove.add(userChunksList.get(i+1));
					UserChunks ucToAdd = new UserChunks(userChunksList.get(i).user);
					ucToAdd.chunks.addAll(userChunksList.get(i).chunks);
					ucToAdd.chunks.addAll(userChunksList.get(i+1).chunks);
					toAdd.add(ucToAdd);
				}
			}


		}
		
		result.removeAll(toRemove);
		result.addAll(toAdd);
		return result;
	}

}
