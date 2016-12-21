package com.hp.hpl.CHAOS.Aggregation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/** 
 * This class represents a unique identifier for each subsequence.
 * It contains the query id and the list of all subsequences in the
 * query as well as the location in that list that identifies the
 * subsequence number.
 * @author Kenneth J. Loomis, 2012
 *
 */
public class SeqMetaData {

	private Integer qID; // Query ID
	private Integer loc; // Subseq location in list
	private ArrayList<Integer> subList; // List of subsequences
	
	/** 
	 * Constructor
	 * @param query
	 * @param index
	 * @param list
	 */
	SeqMetaData ( Integer query, Integer index, ArrayList<Integer> list)
	{
		this.qID = query;
		this.loc = index;
		this.subList = list;
	}
	
	/** 
	 * Get the Subsequence id.
	 * @return
	 */
	public int getSubID ( )
	{
		return this.subList.get(loc);
	}
	
	/**
	 * get the preceding subseq ID
	 */
	public int getPreSubID() {
		return this.subList.get(loc - 1);
	}
	
	/** 
	 * Get the Query id
	 * @return
	 */
	public int getQID ( )
	{
		return this.qID;
	}
	
	/**
	 * Determine if the subsequence is the last one in the list. 
	 * This should be used for check whether an event Type is in TES or not
	 */	
	public boolean isLast() {
		return (loc == subList.size()-1);
	}

	/**
	 * Determine if the Subsequence is the first in the list. This
	 * should be used before cloning the preceding list or before
	 * generating the string representation to be used for hashing.
	 * @return
	 */
	public boolean isFirst ()
	{
		return ( this.loc == 0 );
	}
	
	/** 
	 * Get the preceding subsequence by cloning the current.
	 * @return
	 */
	public SeqMetaData clonePreceding ( )
	{
		if ( this.loc > 0 )
			return (new SeqMetaData(this.qID, ((Integer)(this.loc-1)), this.subList));
		return null;
	}
	
	/** 
	 * Get the string representation of the object.
	 */
	public String toString ()
	{
		String rtn = new String();
		rtn += this.qID.toString();
		rtn += "," + this.loc.toString();
		for ( Integer num : this.subList )
		{
			rtn += "," + num.toString();
		}
		rtn += "\n";
		return rtn;
	}

	/*
	  Testing
	 
	public static void main(String[] args)
	{
		Integer ssl[] = {1, 2, 3, 4};
		List<SubSeqID> ssidList = new ArrayList<SubSeqID>();
		
		ssidList.add( new SubSeqID ( new Integer(1), new Integer(0), ssl) );
		ssidList.add( new SubSeqID ( new Integer(1), new Integer(1), ssl) );
		ssidList.add( new SubSeqID ( new Integer(1), new Integer(3), ssl) );
		
		for ( SubSeqID ss : ssidList)
		{
			System.out.print("<------Testing-------->\n");
			System.out.print("Query: " + ss.GetQID() + "\n");
			System.out.print("SubSeq: " + ss.GetSSID() + "\n");
			System.out.print(ss.toString());
			if ( ss.IsFirst() )
				System.out.print("Is first\n");
			else
				System.out.print(ss.ClonePreceding().toString());
			System.out.print("Hash: " + ss.hashCode() + "\n");	
		}
		
	}
	
	*/

}