<<<<<<< Upstream, based on branch 'machineGUI' of https://github.com/jamesmowens/hit-automaton
package bidDatabase;

import java.util.*;

public interface BidDatabase {
	public void updateInfo(int bid);
	public ArrayList<Integer> returnBids();
	public ArrayList<Integer> returnConstrainedBids(int lowerBound, int upperBound);
}
=======
package bidDatabase;

import java.util.*;

public class BidDatabase {
	public static ArrayList<Integer> bids = new ArrayList();
}
>>>>>>> 1fae682 Makes Strings centered in the ovals
