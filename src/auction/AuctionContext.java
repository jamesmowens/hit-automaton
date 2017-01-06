package auction;

import query.Context;

public class AuctionContext implements Context{
	
	private String description = "An item is being offered in the bidding process";

	@Override
	/**
	 * This is not necessary to implement right now, but for later use I believe
	 * having the power to compare a context to another one is important
	 * @param Context
	 */
	public int compareTo(Context o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	//
	/**
	 * The evaluation process I will use in this case is if a bid is over 100 dollars
	 * @param parameter is just the cost of the bid
	 * @return boolean that tells if the bid was over 100 or not on that item
	 */
	public boolean evaluate(int parameter) {
		// TODO Auto-generated method stub
		return (parameter > 100);
	}

	@Override
	public String description() {
		// TODO Auto-generated method stub
		return "The context is currently: " + this.description;
	}

}
