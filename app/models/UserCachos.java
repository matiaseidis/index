package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import play.db.jpa.Model;

@Entity
public class UserCachos extends Model{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@OneToOne
	public User user;
	@OneToMany(cascade=CascadeType.ALL)
//	@OrderBy("from")
//    @JoinTable(name = "USERCACHOS_CACHO", joinColumns = @JoinColumn(name = "CACHO_ID"), inverseJoinColumns = @JoinColumn(name = "USERCACHO_ID"))
//	@Sort(type=SortType.COMPARATOR,comparator=CachoPositionComparator.class)
	public List<Cacho> cachos = new ArrayList<Cacho>();
	
	
//	public SortedSet<Cacho> cachos = new TreeSet<Cacho>(new CachoPositionComparator());
	
	public UserCachos(User user) {
		this.user = user;
	}
	
	public boolean addCacho(Cacho newCacho){
		
		List<Cacho> toAdd = new ArrayList<Cacho>();
		List<Cacho> toRemove = new ArrayList<Cacho>();
		boolean absent = true;
		
		for ( Cacho cacho : cachos) {
			if (cacho.isChoterThan(newCacho)){
				toAdd.add(newCacho);
				absent = false;
				toRemove.add(cacho);
			}
		}
		
		if(absent){
			toAdd.add(newCacho);
		}

		cachos.removeAll(toRemove);
		cachos.addAll(toAdd);
		
		return !toAdd.isEmpty();
	}
	
	public boolean removeCacho(Cacho cacho){
		return cachos.remove ( cacho );
	}

	public boolean addCachos(List<Cacho> cachosNuevos) {
		
		boolean added = false;
		
		for(Cacho cacho : cachosNuevos){
			added = added ? true : this.addCacho(cacho);
		}
		
		return added;
	}
}
