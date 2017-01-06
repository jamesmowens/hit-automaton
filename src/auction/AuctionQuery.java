package auction;

import query.Query;

public class AuctionQuery extends Query{

	/**
	 * This constructor does nothing special
	 * Assigns the AuctionContext to the query
	 */
	public AuctionQuery(){
		super(new AuctionContext());
	}
	
}
