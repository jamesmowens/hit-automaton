/*

[The "BSD licence"]
Copyright (c) 2004 Jean Bovet
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package edu.usfca.vas.data;

import edu.usfca.vas.graphics.fa.GElementFAState;
import edu.usfca.vas.graphics.fa.GElementFAStateDoubleCircle;
import edu.usfca.vas.graphics.fa.GElementFAStateDoubleRectangle;
import edu.usfca.vas.graphics.fa.GElementFAStateInterface;
import edu.usfca.vas.graphics.fa.GElementFAStateRectangle;
import edu.usfca.xj.appkit.document.XJDataXML;
import edu.usfca.xj.appkit.gview.object.GExport;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public abstract class DataAbstract extends XJDataXML {

    protected static List<DataWrapperAbstract> wrappers = new ArrayList<DataWrapperAbstract>();
    protected int currentWrapperIndex = -1;
    
    public int num_tabs = 0;

    public DataAbstract() {
    }

    public DataWrapperAbstract getDataWrapperAtIndex(int index) {
        return (DataWrapperAbstract)wrappers.get(index);
    }

    public void removeDataWrapperAtIndex(int index) {
        wrappers.remove(index);
    }

    public void addWrapper(DataWrapperAbstract wrapper) {
        wrappers.add(wrapper);
    }

    public List<DataWrapperAbstract> getWrappers() {
        return wrappers;
    }

    public void setCurrentWrapperIndex(int index) {
        currentWrapperIndex = index;
    }

    public int getCurrentWrapperIndex() {
        return currentWrapperIndex;
    }

    public void clear() {
        wrappers.clear();
    }

    public void customReadData(XMLDecoder d) {
        wrappers = (List<DataWrapperAbstract>)d.readObject();
        currentWrapperIndex = ((Integer)d.readObject()).intValue();

        for (Iterator iterator = wrappers.iterator(); iterator.hasNext();) {
            DataWrapperAbstract dataWrapper = (DataWrapperAbstract) iterator.next();
            dataWrapper.wrapperDidLoad();
        }
    }

    public void customWriteData(XMLEncoder e) {
        e.writeObject(wrappers);
        e.writeObject(new Integer(currentWrapperIndex));
    }

    public void printGEContains(GExport ge, XMLEncoder e){
    	for (int i = 0; i < ge.getContains().size(); i++){
    		if (ge.getContains().get(i).getGE() instanceof GElementFAState){
    			GElementFAStateInterface newState = (GElementFAStateInterface)ge.getContains().get(i).getGE();
    			for (int k = 0; k < num_tabs; k++){
    				e.writeObject("\t");
    			}
    			e.writeObject(newState.getState().name);
    			num_tabs++;
    			printGEContains(ge.getContains().get(i), e);
    		}
    		else if (ge.getContains().get(i).getGE() instanceof GElementFAStateDoubleCircle){
    			GElementFAStateDoubleCircle newState = (GElementFAStateDoubleCircle)ge.getContains().get(i).getGE();
    			for (int k = 0; k < num_tabs; k++){
    				e.writeObject("\t");
    			}
    			e.writeObject(newState.getState().name);
    			num_tabs++;
    			printGEContains(ge.getContains().get(i), e);
    		}
    		else if (ge.getContains().get(i).getGE() instanceof GElementFAStateRectangle){
    			GElementFAStateRectangle newState = (GElementFAStateRectangle)ge.getContains().get(i).getGE();
    			for (int k = 0; k < num_tabs; k++){
    				e.writeObject("\t");
    			}
    			e.writeObject(newState.getState().name);
    			num_tabs++;
    			printGEContains(ge.getContains().get(i), e);
    		}
    		else {
    			GElementFAStateDoubleRectangle newState = (GElementFAStateDoubleRectangle)ge.getContains().get(i).getGE();
    			for (int k = 0; k < num_tabs; k++){
    				e.writeObject("\t");
    			}
    			e.writeObject(newState.getState().name);
    			num_tabs++;
    			printGEContains(ge.getContains().get(i), e);
    		}
    	}
    	num_tabs--;
    }
    
    public void printGExport(XMLEncoder e){
    	List<GExport> export = ((DataWrapperFA) wrappers.get(0)).getMachine().getExport().getExport();
    	for(int i = 0; i < export.size(); i++){
    		num_tabs = 0;
  
    		if (((GExport)export.get(i)).getGE() instanceof GElementFAState){
    			GElementFAStateInterface newState = (GElementFAStateInterface)((GExport)export.get(i)).getGE();
    			e.writeObject(newState.getState().name);
    		}
    		if (((GExport)export.get(i)).getGE() instanceof GElementFAStateDoubleCircle){
    			GElementFAStateDoubleCircle newState = (GElementFAStateDoubleCircle)((GExport)export.get(i)).getGE();
    			e.writeObject(newState.getState().name);
    		}
    		if (((GExport)export.get(i)).getGE() instanceof GElementFAStateRectangle){
    			GElementFAStateRectangle newState = (GElementFAStateRectangle)((GExport)export.get(i)).getGE();
    			e.writeObject(newState.getState().name);
    		}
    		if (((GExport)export.get(i)).getGE() instanceof GElementFAStateDoubleRectangle){
    			GElementFAStateDoubleRectangle newState = (GElementFAStateDoubleRectangle)((GExport)export.get(i)).getGE();
    			e.writeObject(newState.getState().name);
    		}
    		num_tabs++;
    		printGEContains((GExport)export.get(i), e);
    	}
    }
    
    public void customWriteExportData(XMLEncoder e){
		printGExport(e);
    }
    
    public void customWriteMachineData(XMLEncoder e) {
    	e.writeObject(((DataWrapperFA) wrappers.get(0)).getMachine());
    	e.writeObject(new Integer(currentWrapperIndex));
    }
    
}
