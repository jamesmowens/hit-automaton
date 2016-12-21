package com.hp.hpl.CHAOS.Aggregation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GenerateStream {

	private String[] s_names;
	private int cnt;
	private int rate;
	private int window;
	
	GenerateStream(int cnt, int rate, int window )
	{
		/*this.s_names = new String[]{"DELL","AMAT","YHOO","AMAZ","MSFT","ORCL","RIMM","CSCO",
									"INTC","IPIX","FABE","QQQ","IBM","GOOG","LINK","TIWW",
									"NETAPP","WALM","EMC","SUBWAY","ADOBE","APPL","KFC","UNO",
									"PEPSI","SUN","ORACLE","INTC","QQQ","AMAT","IPIX","ORCL",
									"INTC","QQQ","AMAT","IPIX","ORCL","IPIX"};*/
		
		/*this.s_names = new String[]{"DELL","AMAT","YHOO","AMAZ","MSFT","ORCL","RIMM","CSCO",
									"INTC","IPIX","FABE","QQQ","IBM","GOOG","LINK","TIWW",
									"NETAPP","WALM","EMC","VMWARE","ADOBE","APPL","CISO",
									"PEPSI","SUN","ORACLE","INTC","QQQ","AMAT","IPIX","ORCL",
									"INTC","QQQ","AMAT","IPIX","ORCL","IPIX","AMAT","YHOO",
									"AMAZ","MSFT","ORCL","RIMM","CSCO","IPIX","INTC","AMAT",
									"AMAZ","MSFT","ORCL","RIMM","CSCO","IPIX","INTC","AMAT",
									"AMAZ","MSFT","ORCL","RIMM","CSCO","IPIX","INTC","AMAT",
									"AMAZ","MSFT","ORCL","RIMM","CSCO","IPIX","INTC","AMAT",
									"AMAZ","MSFT","ORCL","RIMM","CSCO","IPIX","INTC","AMAT",
									"AMAZ","MSFT","ORCL","RIMM","CSCO","IPIX","INTC","AMAT",
									"AMAZ","MSFT","ORCL","RIMM","CSCO","IPIX","INTC","AMAT",
									"AMAZ","MSFT","ORCL","RIMM","CSCO","IPIX","INTC","AMAT",
									"AMAZ","MSFT","ORCL","RIMM","CSCO","IPIX","INTC","AMAT",
									"AMAZ","MSFT","ORCL","RIMM","CSCO","IPIX","INTC","AMAT"};*/

//Event Distribution Test~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		
		//a lot of start event types (around 45% DELL)
		this.s_names = new String[]{"DELL","DELL","DELL","AMAT","DELL","DELL","INTC","QQQ",
		                             "ORCL","RIMM","CSCO","IPIX","YHOO","MSFT","DELL","DELL"};
		
					//********************************************
		
		//generate different distribution stream for SEQ(DELL,IPIX,QQQ,AMAT,MSFT)
		
		//a lot of update event types (around 20% IPIX, QQQ, AMAT each, Q Length = 5)
		//this.s_names = new String[]{"IPIX","QQQ","IPIX","AMAT","AMAT","DELL","INTC","QQQ",
									 // "ORCL","RIMM","CSCO","IPIX","YHOO","MSFT","AMAT","QQQ"};
		
		//a lot of trigger event types(around 45% MSFT)
		//this.s_names = new String[]{"MSFT","MSFT","MSFT","AMAT","MSFT","DELL","INTC","QQQ",
        							  //"ORCL","RIMM","CSCO","IPIX","YHOO","MSFT","MSFT","MSFT"};		
		
						//********************************************
		//generate different distribution stream for SEQ(DELL,IPIX,QQQ)
		
		//a lot of update event types (around 45% IPIX)
		//this.s_names = new String[]{"IPIX","IPIX","IPIX","AMAT","IPIX","DELL","INTC","IPIX",
									//"ORCL","RIMM","CSCO","IPIX","YHOO","MSFT","IPIX","QQQ"};
		
		//a lot of trigger event types(around 45% QQQ)
		/*this.s_names = new String[]{"DELL","IPIX","QQQ","QQQ","QQQ","AMAT","QQQ","INTC","QQQ",
        							"ORCL","RIMM","CSCO","YHOO","MSFT","QQQ","QQQ"};*/
			
						//********************************************
		//uniformly distributed (each type around 13%)
		//this.s_names = new String[]{"MSFT","MSFT","AMAT","AMAT","DELL","DELL","INTC","QQQ",
									//"ORCL","RIMM","CSCO","IPIX","YHOO","IPIX","QQQ"};
		
		
//CC VS Naive very few SSES stream 
		/*this.s_names = new String[]{"MSFT","MSFT","AMAT","AMAT","DELL","DELL","INTC","INTC",
									"ORCL","RIMM","CSCO","IPIX","YHOO","IPIX","RIMM",
									"MSFT","MSFT","AMAT","AMAT","DELL","DELL","INTC","INTC",
									"ORCL","RIMM","CSCO","IPIX","YHOO","IPIX","RIMM","YHOO",
									"ORCL","RIMM","CSCO","IPIX","YHOO","IPIX","RIMM","YHOO",
									"MSFT","MSFT","AMAT","AMAT","DELL","DELL","INTC","INTC",
									"MSFT","MSFT","AMAT","AMAT","DELL","DELL","INTC","INTC"};*/
		
		
		this.cnt = cnt;
		this.rate = rate;
		this.window = window;
	}
	
	// Random stream
	void go ()
	{
		Random generator = new Random();
		double time = 0;
		for (int i=0; i < this.cnt; i++)
		{
			int get = generator.nextInt(7);
			time = time + ((double) this.window / (double) this.rate);
			System.out.println( this.s_names[get] + " " + time);
		}
	}
	
	// uniform stream
	void go2 ()
	{
		List<String> mixedup = Arrays.asList(this.s_names);
		double time = 0;
		for (int i=0; i < this.cnt/16; i++)
		{
			Collections.shuffle(mixedup);
			for ( String str : mixedup )
			{
				time = time + ((double) this.window / (double) this.rate);
				System.out.println( str + " " + time);
			}
		}
	}

	public static void main(String[] args)
	{
		(new GenerateStream(10000,200,50)).go2();
	}

	
}

