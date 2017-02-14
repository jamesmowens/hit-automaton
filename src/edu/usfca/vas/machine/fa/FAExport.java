package edu.usfca.vas.machine.fa;

import java.util.ArrayList;
import java.util.List;

import edu.usfca.vas.graphics.fa.GElementFAState;
import edu.usfca.vas.graphics.fa.GElementFAStateDoubleCircle;
import edu.usfca.vas.graphics.fa.GElementFAStateDoubleRectangle;
import edu.usfca.vas.graphics.fa.GElementFAStateRectangle;
import edu.usfca.xj.appkit.gview.object.GElement;
import edu.usfca.xj.appkit.gview.object.GExport;
import edu.usfca.xj.foundation.XJXMLSerializable;

public class FAExport implements XJXMLSerializable {

	protected List<GExport> export = new ArrayList<GExport>();
	protected List<Object> link = new ArrayList<Object>();
	
	public FAExport(){
	}
	
	// does ge1 contain ge2? if so return true, else false
    public boolean contains(GExport ge1, GExport ge2){
    	//if ge1 rectangular
    	if (ge1.getGE() instanceof GElementFAStateRectangle || ge1.getGE() instanceof GElementFAStateDoubleRectangle){
    		double thisX = ge2.getGE().getPosition().x;
			double thisY = ge2.getGE().getPosition().y;
			double thisX2 = ge2.getGE().getPosition().x2;
			double thisY2 = ge2.getGE().getPosition().y2;
    		// if ge2 circular
    		if (ge2.getGE() instanceof GElementFAState || ge2.getGE() instanceof GElementFAStateDoubleCircle){
    			if (thisX > ge1.getGE().getPosition().x && thisX < ge1.getGE().getPosition().x2 &&
						thisY > ge1.getGE().getPosition().y && thisY < ge1.getGE().getPosition().y2){
    				return true;
    			}
    			else {
    				return false;
    			}
    		}
    		// if ge2 rectangular
    		else {
    			if (thisX > ge1.getGE().getPosition().x && thisX2 < ge1.getGE().getPosition().x2 &&
						thisY > ge1.getGE().getPosition().y && thisY2 < ge1.getGE().getPosition().y2){
    				return true;
    			}
    			else {
    				return false;
    			}
    		}
    	}
    	else {
    		return false;
    	}
    }
    
    public void addGExport(GExport ge){
    	int j = 0;
    	// loop to see if new GExport contains any uncontained elements
    	if (ge.getGE() instanceof GElementFAStateRectangle || ge.getGE() instanceof GElementFAStateDoubleRectangle){
    		for (j = 0; j < export.size(); j++){
    			GExport holder = (GExport)export.get(j);
    			double thisX = holder.getGE().getPosition().x;
    			double thisY = holder.getGE().getPosition().y;
    			
    			if (holder.getGE() instanceof GElementFAState || holder.getGE() instanceof GElementFAStateDoubleCircle){
    				// does new GExport contain the other element
    				if (thisX > ge.getGE().getPosition().x && thisX < ge.getGE().getPosition().x2 &&
    						thisY > ge.getGE().getPosition().y && thisY < ge.getGE().getPosition().y2){
    					// if yes add circle to contains list
    					ge.addToContains((GExport)export.get(j));
    					export.get(j).addToContainedBy(ge);
    					// remove holder from export
    					export.remove(export.get(j));
    					j = 0;
    					j--;
    				}
    			} else {
    				double thisX2 = holder.getGE().getPosition().x2;
    				double thisY2 = holder.getGE().getPosition().y2;
    				// does new GExport contain the other element
    				if (thisX > ge.getGE().getPosition().x && thisX2 < ge.getGE().getPosition().x2 &&
    					thisY > ge.getGE().getPosition().y && thisY2 < ge.getGE().getPosition().y2){
    					// if yes add other element to contains list
    					ge.addToContains((GExport)export.get(j));
    					export.get(j).addToContainedBy(ge);
    					// remove holder from export
    					export.remove(export.get(j));
    					j = 0;
    					j--;
    				}
    			}
    		}
    	}
    	
    	// loop to see if it is contained by any elements
    	List<GExport> list = export;
    	GExport lastGExport = null;
    	int i = 0;
    	while(i < list.size()){
    		GExport gExp = (GExport)list.get(i);
    		// if it contains the gExport
    		if (contains(ge, gExp) == true){
    			ge.addToContains((GExport)list.get(i));
    			export.get(i).addToContainedBy(ge);
    			list.remove(list.get(i));
    		}
    		i++;
    		// if it is contained by the GExport
    		if (contains(gExp, ge) == true){
    			lastGExport = gExp;
    			list = gExp.getContains();
    			i = 0;
    		}
    	}
    	if (lastGExport != null){
    		lastGExport.addToContains(ge);
    		ge.addToContainedBy(lastGExport);
    	} else {
    		export.add(ge);
    	}
    }
    
    
    

    public int getEnd(ArrayList<GExport> ar){
    	if (ar.size() > 0) {
    		return ar.size() - 1;
    	} else {
    		return 0;
    	}
    }
    
    public void removeGExport(GElement ge){
    	ArrayList<GExport> newList = new ArrayList<GExport>();
    	List<?> holdList;
    	for (int i = 0; i < export.size(); i++){
    		GExport gExp = (GExport)export.get(i);
    		
    		// if first level element of export the GExport we want to remove
    		if (gExp.getGE().equals(ge) == true){
    			// remove element
    			export.remove(i);
    			while (gExp.getContains().size() > 0){
    				// move elements from removed element's contains to export
    				GExport holder = gExp.getContains().get(0);
    				gExp.getContains().remove(0);
    				addGExport(holder);
    			}
    		}
    		else {
    			newList.add(gExp);
    		}
    		
    		// check objects that are contained
    		if (newList.size() > 0){
    			for (int j = 0; newList.get(getEnd(newList)).getContains().size() > 0; j++){
    				GExport nextGExport = newList.get(getEnd(newList)).getContains().get(j);
    				if (nextGExport.getGE().equals(ge) == true){
    					// remove element
    					newList.get(getEnd(newList)).getContains().remove(j);
    					while (nextGExport.getContains().size() > 0){
    						// move elements from removed element's contains to export
    						GExport holder = nextGExport.getContains().get(0);
    						nextGExport.getContains().remove(0);
    						addGExport(holder);
    					}
    					j = 0;
    					break;
    				}
    				else {
    					newList.add(gExp);
    					j = 0;
    					j--;
    				}
    			}
    		}
    	}
    }
    
    public List<GExport> getExport() {
		return this.export;
	}
	
	public List<Object> getLink() {
		return this.link;
	}
	
	public void setLink(List<Object> link) {
		this.link = link;
	}
	
	public void setExport(List<GExport> export) {
		this.export = export;
	}
}