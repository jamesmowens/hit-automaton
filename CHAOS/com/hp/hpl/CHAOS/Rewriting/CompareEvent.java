package com.hp.hpl.CHAOS.Rewriting;
import java.util.ArrayList;
import java.util.Comparator;

import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;

public class CompareEvent implements Comparator<ArrayList<byte[]>> {

	//smaller time first
	public int compare(ArrayList<byte[]> r1, ArrayList<byte[]> r2, SchemaElement[] schArray, int index) {
		
		byte[] tuple1 = r1.get(index); 
		byte[] tuple2 = r2.get(index);
		double timestamp1 = StreamAccessor.getDoubleCol(tuple1, schArray, 1);
		double timestamp2 = StreamAccessor.getDoubleCol(tuple2, schArray, 1);
		if(timestamp1 < timestamp2)
			return 1;
		
		return 0;
	}

	@Override
	public int compare(ArrayList<byte[]> o1, ArrayList<byte[]> o2) {
		// TODO Auto-generated method stub
		return 0;
	}


}
