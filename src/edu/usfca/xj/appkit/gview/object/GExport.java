package edu.usfca.xj.appkit.gview.object;

import java.util.List;
import java.util.ArrayList;

import edu.usfca.xj.foundation.XJXMLSerializable;

public class GExport implements XJXMLSerializable {

	protected GElement gE;
	protected String type;
	protected List<GExport> contains = new ArrayList<GExport>();
	protected List<GExport> containedBy = new ArrayList<GExport>();
	
	public GExport(GElement gEle, String type2){
		gE = gEle;
		type = type2;
	}
	
	public GExport() {
		
	}
	
	public void addToContains(GExport ge){
		contains.add(ge);
	}
	
	public void addToContainedBy(GExport ge){
		containedBy.add(ge);
	}
	
	public void removeFromContains(GExport ge){
		contains.remove(ge);
	}
	
	public List<GExport> getContains(){
		return this.contains;
	}
	
	public List<GExport> getContainedBy() {
		return this.containedBy;
	}
	
	public GElement getGE(){
		return this.gE;
	}
	
	public void setGE (GElement gE) {
		this.gE = gE;
	}

	public void setContains(List<GExport> contains) {
		this.contains = contains;
	}

	public void setContainedBy(List<GExport> containedBy) {
		this.containedBy = containedBy;
	}
}
