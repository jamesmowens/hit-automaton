package bidDatabase;

import java.util.*;

public interface BidDatabase {
	public void updateInfo(int bid);
	public ArrayList<Integer> returnBids();
	public ArrayList<Integer> returnConstrainedBids(int lowerBound, int upperBound);
}
