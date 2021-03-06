
package edu.usfca.vas.graphics.fa;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.*;

import Query.DataNode;
import Query.StreamPanel;
import edu.usfca.xj.appkit.gview.object.GElement;
import edu.usfca.xj.appkit.gview.object.GLink;

public class GElementFASidePanel extends JPanel {

	private static final long serialVersionUID = -511482713793989551L;

	//hold the flow stuff
	JPanel panel;
	JTabbedPane tabs;
	ArrayList<JLabel> labels = new ArrayList<JLabel>();
	ArrayList<JPanel> panels = new ArrayList<JPanel>();
	ArrayList<String> strings = new ArrayList<String>();
	JPanel variablesPanel;
	StreamPanel streamPanel;
	static ArrayList<GLink> transitions = new ArrayList<GLink>();
	static ArrayList<GElement> starts = new ArrayList<GElement>();
	static ArrayList<GElement> targets = new ArrayList<GElement>();
	int currentIndex = -1;


	//creates the side panel
	public GElementFASidePanel() {
		super(new GridLayout());
		//this.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
		this.setPreferredSize(new Dimension(350, 500));
		this.setLayout(new BorderLayout());
		setVisible(true);
		panel = new JPanel(new GridLayout());
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
//		panel.setPreferredSize(new Dimension(500, 25));
		panel.setVisible(true);
		JScrollPane scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//		scroll.setPreferredSize(new Dimension(325, 345));
		this.add(scroll, BorderLayout.CENTER);
		this.tabs = new JTabbedPane();
		this.variablesPanel = new JPanel(new GridLayout());
		variablesPanel.add(scroll);
		tabs.add("Variables", variablesPanel);
		streamPanel = new StreamPanel();
		tabs.add("Data Stream", streamPanel);
		this.add(tabs);
	}
//gets the transition that should be highlighted
//gets the transition that should be highlighted
	public GLink getTransition(){
		int i = 0;
		for (JLabel label: labels){
			if (label.getForeground() == Color.RED){
				return transitions.get(i);
			}
			i++;
		}
		return null;
	}

	//gets the state that should be highlighted
		public GElement getTarget(){
			int i = 0;
			for (JLabel label: labels){
				if (label.getForeground() == Color.RED){
					return targets.get(i);
				}
				i++;
			}
			return null;
		}

	//parses to a readable format
	public static String parse(String flow){
		String delims = "[ ]+";
		String[] tokens = flow.split(delims);
		String parsed = tokens[0];
		parsed = parsed.concat("(");
		int i=3;
		while (!tokens[i].startsWith("from")){
			if (tokens[i].equals("nothing")) //done parsing
				break;
			//else if (isInteger(tokens[i])){

				//if (tokens.length > i+1 && !tokens[i+1].startsWith("from"))
					//parsed = parsed.concat(", ");

			//}
			else{
				parsed = parsed.concat(tokens[i] + "(");
				parsed = parsed.concat(tokens[i+1] + ")");
				i++;
				i++;
				if (!tokens[i].startsWith("from"))
					parsed = parsed.concat(", ");
			}
		}

		parsed = parsed.concat(")");
		parsed = parsed.concat(" at [" + tokens[1] + "," + tokens[2] + "]");

		return parsed;
	}
	//figures out if something is an integer
	public static boolean isInteger(String s) {
	    try {
	        Integer.parseInt(s);
	    } catch(NumberFormatException e) {
	        return false;
	    }
	    // only got here if we didn't return false
	    return true;
	}

	//returns the size of the current flow
	public ArrayList<String> getStrings(){
		return strings;
	}

	public void add(String flow) {
		strings.add(flow);
		panel.setPreferredSize(new Dimension(500, strings.size()*25));
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		p.setSize(new Dimension(15, 500));
		JLabel tf = new JLabel(flow);
		//System.out.println("This is a flow: " + flow);
		tf.setPreferredSize(new Dimension(400, 15));
		p.add(tf);
		panel.add(p);
		labels.add(tf);
		panels.add(p);
		this.updateUI();
	}

	//deletes a flow
	public void removeFlow(String flow){
		if (!strings.contains(flow))//the flow doesn't exist,  so don't remove it.
			return;
		strings.remove(flow);
		flow = parse(flow);
		int i = 0;
		for (JLabel test: labels){
			if (test.getText().equals(flow)){
				panel.remove(panels.get(i));
				panels.remove(i);
				labels.remove(i);
				return;
			}
			i++;
		}
		this.updateUI();
		return;
	}

	//colors a entry (label ) red to indicate that it is being looked at.
	public void highlightFlow(String flow){

		if (!strings.contains(flow)) //if the flow doesnt exist, we cant highlight it
			return;
		int i = 0;
		//System.out.println("high");
		for (String test: strings){
			if (test.equals(flow)){
				//System.out.println("highlighting");
				labels.get(i).setForeground(Color.RED);
				return;
			}
			i++;
		}
		this.updateUI();
	}

	//highlights the next thing in the stream (if there is a next thing.)
	public String highlightNext(){
		int i = -1;
		int j = 0;
		for (JLabel test: labels){
			if (test.getForeground() == Color.RED)
				i = j;
			j++;
		}
		
		//make sure there is something highlighted...
		if (i == -1 && labels.size() > 0){
			labels.get(0).setForeground(Color.RED);
			return strings.get(0);
		}
		unHighlight();
		if (labels.size() >= i+2){
			labels.get(i+1).setForeground(Color.RED);
			return strings.get(i+1);
		}
		return null;
	}

	//highlights the previous thing in the stream (if there is a next thing.)
		public String highlightPrevious(){
			int i = -1;
			int j = 0;
			for (JLabel test: labels){
				if (test.getForeground() == Color.RED)
					i = j;
				j++;
			}
			//make sure there is something highlighted...
			if (i == -1 && labels.size() > 0){
				labels.get(labels.size()-1).setForeground(Color.RED);
				return strings.get(labels.size()-1);
			}
			unHighlight();
			if (i != 0){
				labels.get(i-1).setForeground(Color.RED);
				return strings.get(i-1);
			}
			return null;
		}

	//unhighlights everything
	public void unHighlight(){
		int i = 0;
		for (JLabel test: labels){
			labels.get(i).setForeground(Color.BLACK);
			i++;
		}
		return;
	}

	//removes everything from the side panel
	public void clear(){
		for (JPanel panel: panels){
			//this.remove(panel);
			panel.remove(panel);
		}
		strings.clear();
		panels.clear();
		labels.clear();
		panel.removeAll();
		return;
	}

	public void setCurrent(int i) {
		if(i<0 || i >= panels.size()) return;
		if(currentIndex>=0) {
			labels.get(currentIndex).setForeground(Color.BLACK);
		}
		currentIndex = i;
		labels.get(currentIndex).setForeground(Color.RED);
	}

	public int getCurrent() {
		return currentIndex;
	}

	public ArrayList<String> getStringLabels() {
		ArrayList<String> list = new ArrayList<String>();
		for(JLabel l : labels) {
			list.add(l.getText());
		}
		return list;
	}

	/**
	 * Initialize the text display of data nodes
	 * @param dataNodes The list of dataNodes
	 */
	public void initDataPanel(ArrayList<DataNode> dataNodes) {
		streamPanel.setData(dataNodes);
		streamPanel.initDataList();
	}

	/**
	 * Advance the text display of data nodes
	 */
	public void advanceDataList() {
		streamPanel.advanceDataNodes();
	}

}