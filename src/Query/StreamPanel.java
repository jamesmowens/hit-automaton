package Query;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Thomas Schweich on 3/10/2017.
 *
 * Class representing the panel which displays the data stream
 */
public class StreamPanel extends JPanel {
    private static final int MAX_DATA_NODES = 10;
    JLabel[] streamLabels;
    private LinkedList<DataNode> futureNodes = new LinkedList<DataNode>(), pastNodes = new LinkedList<DataNode>();
    private ArrayList<DataNode> allNodes = new ArrayList<DataNode>();
    private int currNodeIdx;

    public StreamPanel() {
        super(new GridLayout(MAX_DATA_NODES, 1));
    }

    /**
     * Sets the master list of DataNodes
     * @param nodes
     */
    public void setData(ArrayList<DataNode> nodes) {
        allNodes = nodes;
    }

    /**
     * Initializes all the JLabels used for displaying the data stream and adds their initial values
     */
    public void initDataList() {
        super.removeAll();
        streamLabels = new JLabel[MAX_DATA_NODES];
        for(int i = 0; i < MAX_DATA_NODES && i < allNodes.size(); i++) {
            streamLabels[i] = new JLabel(parseDataNode(allNodes.get(i)));
            futureNodes.add(allNodes.get(i));
            super.add(streamLabels[i], i);
        }
        updateNodeDisplay();
    }

    /**
     * Move to the next data node in the display
     */
    public void advanceDataNodes() {
        if(!(futureNodes.size() > 0)) return;
        if(pastNodes.size() > 0) pastNodes.removeFirst();
        pastNodes.add(futureNodes.removeFirst());
        final int lastIdx = currNodeIdx + futureNodes.size();
        if(allNodes.size() >= lastIdx) {
            futureNodes.add(allNodes.get(lastIdx));
            currNodeIdx++;
        }
        updateNodeDisplay();
    }


    /**
     * Updates the text display of the nodes, coloring the current node's display
     */
    private void updateNodeDisplay() {
        if(pastNodes.size() + futureNodes.size() > MAX_DATA_NODES) {
            System.err.println("Couldn't display nodes");
            return; //Something is horribly wrong...
        }

        Iterator<DataNode> pastIter = pastNodes.iterator(), futureIter = futureNodes.iterator();
        int i = 0;
        if(pastIter.hasNext()) {
            while (pastIter.hasNext()) {
                streamLabels[i++].setText(parseDataNode(pastIter.next()));
            }
        }
        if(futureIter.hasNext()) {
            streamLabels[i].setForeground(Color.CYAN);
            streamLabels[i].setText(parseDataNode(futureIter.next()));
            while(futureIter.hasNext()) {
                streamLabels[i++].setText(parseDataNode(futureIter.next()));
            }
        }
        super.updateUI();
    }

    private String parseDataNode(DataNode d) {
        return d.getTime() + ": (" + d.getLatitude() + ", " + d.getLongitude() + ") $" + d.getCost();
    }


}
