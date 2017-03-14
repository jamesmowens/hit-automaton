package edu.usfca.vas.graphics.fa;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.*;

import edu.usfca.xj.appkit.frame.XJFrame;
import edu.usfca.xj.appkit.gview.object.GElement;
import edu.usfca.xj.appkit.gview.object.GLink;
import Query.*;
import connection.Step;

public class GElementFANickName extends JPanel implements ActionListener {

	private static final long serialVersionUID = -511482713793989551L;
	//hold the links stuff (for links)
	ArrayList<GLink> glinks = new ArrayList<GLink>();
	ArrayList<JLabel> labels = new ArrayList<JLabel>();
	ArrayList<JPanel> panels = new ArrayList<JPanel>();
	ArrayList<JTextField> textfields = new ArrayList<JTextField>();
	//hold the elements stuff (for states)
	ArrayList<GElement> gelements = new ArrayList<GElement>();
	ArrayList<JLabel> Elabels = new ArrayList<JLabel>();
	ArrayList<JPanel> Epanels = new ArrayList<JPanel>();
	ArrayList<JTextField> Etextfields = new ArrayList<JTextField>();
	//present active states
	JLabel activeStateLabel = new JLabel();
	//JPanel linkPanel;
	//JPanel elementPanel;
	//JPanel executionPanel;
	public static JPanel queryPanel;
	JPanel queryEditPanel;
	JTabbedPane tabs;
	GViewFAMachine mac;

	DefaultComboBoxModel contextModel  = new DefaultComboBoxModel();
	DefaultComboBoxModel destinationModel = new DefaultComboBoxModel();

	//These are the combo boxes, declared here for some reason
	JComboBox contextList = new JComboBox(contextModel);
	JComboBox destinationList = new JComboBox(destinationModel);
	TextField setFill = new TextField("Leave this blank for transition query (delete this comment)", 20);
	TextField conditionFill = new TextField("fill with condition", 20);

	//these are for the new list, specifically the Derivation queries
	JComboBox switchList = new JComboBox(destinationModel);
	JComboBox contextDerivationList = new JComboBox(contextModel);
	JTextField patternDerivationFill = new JTextField("*", 20);
	JTextField whereDerivationFill = new JTextField("This is the if condition",20);

	//these are for the new list, specifically the Processing queries
	JComboBox contextProcessingList = new JComboBox(contextModel);
	JTextField setProcessingFill = new JTextField("this is the set condition",20);
	JTextField patternProcessingFill = new JTextField("This is the pattern statement",20);
	JTextField whereProcessingFill = new JTextField("This is the where statement",20);

	String condition = "";
	String set = "";
	ArrayList<String> states = new ArrayList<String>();
	HashMap<String,JButton> buttonCache = new HashMap<String,JButton>();

	static HashMap<String, LinkedList<Query>> database = new HashMap();
	LinkedList<Query> allQueries = new LinkedList();
	static String currentDisplayName = "";

	JPanel queriesPanel = new JPanel();

	//creates the naming panel
	public GElementFANickName() {
		super();
		this.tabs = new JTabbedPane();
		tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabs.setPreferredSize(new Dimension(960, 500));

		this.queryPanel  = new JPanel();
		this.queryEditPanel = new JPanel();
		makeQueryPanels();

		tabs.setVisible(true);
		setVisible(true);
		JScrollPane scroll = new JScrollPane(tabs, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // TODO Check
		scroll.setPreferredSize(new Dimension(960, 500));
		this.add(scroll);

	}

	//public static void putPertainingQueriesIn(String label){}

	/*
	public static void putPertainingQueriesIn(String label){
		System.out.println(label);
		if(database.get(label) != null){
			//logic sequence that checks if the panel is already displaying the same state, and if its not then it clears
			if(!currentDisplayName.equals(label)){
				GElementFANickName.queryPanel.removeAll();
				currentDisplayName = label;
			}
			//This runs through all the queries to display it
			for(Query que: database.get(label)){
			JLabel name = new JLabel(que.queryInfo());
			//Gets rid of repeating
			if(GElementFANickName.queryPanel.getComponents().length > 0)
			for(Component pan: GElementFANickName.queryPanel.getComponents()){
				if(((JLabel)pan).getText().equals(name.getText())){
					return;
				}
			}
			GElementFANickName.queryPanel.add(name);
		}
	}
}
	*/

	private void makeQueryPanels(){
		this.queryPanel = new JPanel();
		this.queryEditPanel = new JPanel();
		this.queryPanel.setLayout(new BoxLayout(queryPanel, BoxLayout.Y_AXIS));
		this.queryEditPanel.setLayout(new BoxLayout(queryEditPanel, BoxLayout.Y_AXIS));
		tabs.addTab("Query Display", this.queryPanel);
		tabs.addTab("Query Editor", this.queryEditPanel);

		JLabel contextDerivationLabel = new JLabel("Context Derivation");
		contextDerivationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JPanel contextDerivationPanel = new JPanel();
		contextDerivationPanel.setLayout(new BoxLayout(contextDerivationPanel, BoxLayout.X_AXIS));
		contextDerivationPanel.add(contextDerivationLabel);
		contextDerivationPanel.add(Box.createRigidArea(new Dimension(500,0)));
		contextDerivationPanel.setAlignmentY(Component.LEFT_ALIGNMENT);

		JLabel switchLabel = new JLabel("Switch");
		JPanel switchPanel = new JPanel();
		switchPanel.setLayout(new BoxLayout(switchPanel, BoxLayout.X_AXIS));
		switchPanel.add(switchLabel);
		switchPanel.add(Box.createRigidArea(new Dimension(28,0)));
		switchPanel.add(switchList);
		switchPanel.add(Box.createRigidArea(new Dimension(500,0)));
		switchPanel.setAlignmentY(Component.LEFT_ALIGNMENT);
		
		JLabel patternLabel = new JLabel("Pattern");
		JPanel patternPanel = new JPanel();
		patternPanel.setLayout(new BoxLayout(patternPanel, BoxLayout.X_AXIS));
		patternPanel.add(patternLabel);
		patternPanel.add(Box.createRigidArea(new Dimension(25,0)));
		patternPanel.add(patternDerivationFill);
		patternPanel.add(Box.createRigidArea(new Dimension(500,0)));
		patternPanel.setAlignmentY(Component.LEFT_ALIGNMENT);
		
		JLabel whereLabel = new JLabel("Where");
		JPanel wherePanel = new JPanel();
		wherePanel.setLayout(new BoxLayout(wherePanel, BoxLayout.X_AXIS));
		wherePanel.add(whereLabel);
		wherePanel.add(Box.createRigidArea(new Dimension(29,0)));
		wherePanel.add(whereDerivationFill);
		wherePanel.add(Box.createRigidArea(new Dimension(500,0)));
		wherePanel.setAlignmentY(Component.LEFT_ALIGNMENT);
		
		JLabel contextUpLabel = new JLabel("Context");
		JPanel contextUpPanel = new JPanel();
		contextUpPanel.setLayout(new BoxLayout(contextUpPanel, BoxLayout.X_AXIS));
		contextUpPanel.add(contextUpLabel);
		contextUpPanel.add(Box.createRigidArea(new Dimension(24,0)));
		contextUpPanel.add(contextDerivationList);
		contextUpPanel.add(Box.createRigidArea(new Dimension(500,0)));
		contextUpPanel.setAlignmentY(Component.LEFT_ALIGNMENT);

		JLabel contextProcessingLabel = new JLabel("Context Processing");
		JPanel contextProcessingPanel = new JPanel();
		contextProcessingPanel.setLayout(new BoxLayout(contextProcessingPanel, BoxLayout.X_AXIS));
		contextProcessingPanel.add(contextProcessingLabel);
		contextProcessingPanel.add(Box.createRigidArea(new Dimension(500,0)));
		contextProcessingPanel.setAlignmentY(Component.LEFT_ALIGNMENT);

		JLabel setLabel = new JLabel("Set");
		JPanel setPanel = new JPanel();
		setPanel.setLayout(new BoxLayout(setPanel, BoxLayout.X_AXIS));
		setPanel.add(setLabel);
		setPanel.add(Box.createRigidArea(new Dimension(48,0)));
		setPanel.add(setProcessingFill);
		setPanel.add(Box.createRigidArea(new Dimension(500,0)));
		setPanel.setAlignmentY(Component.LEFT_ALIGNMENT);

		JLabel patternProcessingLabel = new JLabel("Pattern");
		JPanel patternProcessingPanel = new JPanel();
		patternProcessingPanel.setLayout(new BoxLayout(patternProcessingPanel, BoxLayout.X_AXIS));
		patternProcessingPanel.add(patternProcessingLabel);
		patternProcessingPanel.add(Box.createRigidArea(new Dimension(25,0)));
		patternProcessingPanel.add(patternProcessingFill);
		patternProcessingPanel.add(Box.createRigidArea(new Dimension(500,0)));
		patternProcessingPanel.setAlignmentY(Component.LEFT_ALIGNMENT);
		
		JLabel whereProcessingLabel = new JLabel("Where");
		JPanel whereProcessingPanel = new JPanel();
		whereProcessingPanel.setLayout(new BoxLayout(whereProcessingPanel, BoxLayout.X_AXIS));
		whereProcessingPanel.add(whereProcessingLabel);
		whereProcessingPanel.add(Box.createRigidArea(new Dimension(29,0)));
		whereProcessingPanel.add(whereProcessingFill);
		whereProcessingPanel.add(Box.createRigidArea(new Dimension(500,0)));
		whereProcessingPanel.setAlignmentY(Component.LEFT_ALIGNMENT);

		JLabel contextDownLabel = new JLabel("Context");
		JPanel contextDownPanel = new JPanel();
		contextDownPanel.setLayout(new BoxLayout(contextDownPanel, BoxLayout.X_AXIS));
		contextDownPanel.add(contextDownLabel);
		contextDownPanel.add(Box.createRigidArea(new Dimension(23,0)));
		contextDownPanel.add(contextProcessingList);
		contextDownPanel.add(Box.createRigidArea(new Dimension(500,0)));
		contextDownPanel.setAlignmentY(Component.LEFT_ALIGNMENT);

		//Put Buttons Here
		JButton clearDerivation = new JButton("Clear Derivation Query");
		clearDerivation.addActionListener(this);
		JButton submitDerivation = new JButton("Submit Derivation Query");
		submitDerivation.addActionListener(this);
		buttonCache.put("Clear Derivation", clearDerivation);
		buttonCache.put("Submit Derivation", submitDerivation);
		JPanel derivationButtonsPanel = new JPanel();
		derivationButtonsPanel.add(clearDerivation);
		derivationButtonsPanel.add(submitDerivation);

		//put Buttons Here
		JButton clearProcessing = new JButton("Clear Processing Query");
		clearProcessing.addActionListener(this);
		JButton submitProcessing = new JButton("Submit Processing Query");
		submitProcessing.addActionListener(this);
		buttonCache.put("Clear Processing", clearProcessing);
		buttonCache.put("Submit Processing", submitProcessing);
		JPanel processingButtonsPanel = new JPanel();
		processingButtonsPanel.add(clearProcessing);
		processingButtonsPanel.add(submitProcessing);

		//wrote this to debug the size increase with states present thing going on with the resizing
		//Object[] nums = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		//queryEditPanel.add(new JComboBox(new DefaultComboBoxModel(nums)));

		queryEditPanel.add(contextDerivationPanel);
		queryEditPanel.add(switchPanel);
		queryEditPanel.add(patternPanel);
		queryEditPanel.add(wherePanel);
		queryEditPanel.add(contextUpPanel);

		queryEditPanel.add(derivationButtonsPanel);

		queryEditPanel.add(contextProcessingPanel);
		queryEditPanel.add(setPanel);
		queryEditPanel.add(patternProcessingPanel);
		queryEditPanel.add(whereProcessingPanel);
		queryEditPanel.add(contextDownPanel);
		queryEditPanel.add(processingButtonsPanel);

		//That is all the stuff for the query maker, so now lets go to the query displayer (playa)

		JButton queryRefresh = new JButton("Refresh list of queries");
		queryRefresh.addActionListener(this);
		buttonCache.put("Query List Refresh",queryRefresh);
		JPanel queryRefreshPanel = new JPanel();
		queryRefreshPanel.add(queryRefresh);
		this.queriesPanel.setLayout(new BoxLayout(queriesPanel, BoxLayout.Y_AXIS));
		queryPanel.add(queriesPanel);
		queryPanel.add(queryRefreshPanel);
		//queryPanel.add(new JLabel("Testing text init"));

	}

	/**
	 * This is how the buttons work, and they call whatever is in the set functions
	 */
	public void actionPerformed(ActionEvent e) {

		if(e.getSource().equals(buttonCache.get("Clear Derivation"))){
			System.out.println("clearing stuff from deriv.");
			//This is supposed to clear the derivation fields
			patternDerivationFill.setText("*");
			whereDerivationFill.setText("*");}

		else if(e.getSource().equals(buttonCache.get("Submit Derivation"))){
			String dest  = (String) switchList.getSelectedItem();
			String pattern = (String) patternDerivationFill.getText();
			Condition eval = new Condition(whereDerivationFill.getText());
			String start = (String) contextDerivationList.getSelectedItem();
			Query query;
			Step transition = new Step(start,dest,"TransStep: " + start + " to " + dest);

			query = new TransitionQuery(eval,start,"Transition Query",transition);
			query.setPattern(pattern);

			//If its not in there, put it in there
			if(database.get(contextDerivationList.getSelectedItem()) == null){
				database.put((String)contextDerivationList.getSelectedItem(), new LinkedList<Query>());
			}
			database.get((String)contextDerivationList.getSelectedItem()).add(query);
			allQueries.add(query);
			System.out.println("Submitted");
		}

		else if(e.getSource().equals(buttonCache.get("Clear Processing"))){
			System.out.println("clearing stuff from processing");
			setProcessingFill.setText("*");
			patternProcessingFill.setText("*");
			whereProcessingFill.setText("*");
			System.out.println("Cleared the processing text fill boxes");
			//This is supposed to clear the processing fields
		}

		else if(e.getSource().equals(buttonCache.get("Submit Processing"))){
			String set = setProcessingFill.getText();
			String pattern = (String) patternProcessingFill.getText();
			Condition eval = new Condition(whereProcessingFill.getText());
			String start = (String) contextProcessingList.getSelectedItem();
			Query query;

			query = new VariableQuery(eval, start, "Variable Query: " + start + " evaluates " + conditionFill.getText() + "" + set , set);
			query.setPattern(pattern);

			System.out.println(query.queryInfo());

			//If its not in there, put it in there
			if(database.get(contextProcessingList.getSelectedItem()) == null){
				database.put((String)contextProcessingList.getSelectedItem(), new LinkedList<Query>());
			}
			database.get((String)contextProcessingList.getSelectedItem()).add(query);
			allQueries.add(query);
			System.out.println("Submitted");}
		else if(e.getSource().equals(buttonCache.get("Query List Refresh"))){
			queriesPanel.removeAll();
			LinkedList<Query> transQueries = new LinkedList();

			System.out.println("List size for queries: " + allQueries.size());
			double count = 0.0;
			JPanel currentPanel = new JPanel();
			currentPanel.setLayout(new BoxLayout(currentPanel, BoxLayout.Y_AXIS));
			for(Query q: allQueries) {
				if (q instanceof VariableQuery) {
					//Creates the query that makes the panel
					JPanel currentQueryPanel = new JPanel();
					currentQueryPanel.setLayout(new BoxLayout(currentQueryPanel, BoxLayout.Y_AXIS));

					JLabel querySet = new JLabel();
					querySet.setText("SET " + ((VariableQuery)q).getSet());
					querySet.setAlignmentX(Component.LEFT_ALIGNMENT);
					currentQueryPanel.add(querySet);
					currentQueryPanel.add(Box.createRigidArea(new Dimension(10,0)));
					if((q.queryPattern() != null) && (q.queryPattern().equals("Driver") || q.queryPattern().equals("Rider"))){
						JLabel queryPattern = new JLabel();
						queryPattern.setText("PATTERN " + q.queryPattern());
						queryPattern.setAlignmentX(Component.LEFT_ALIGNMENT);
						currentQueryPanel.add(queryPattern);
						currentQueryPanel.add(Box.createRigidArea(new Dimension(10,0)));
					}

					JLabel queryWhere = new JLabel();
					queryWhere.setText("WHERE " + q.getEx());
					queryWhere.setAlignmentX(Component.LEFT_ALIGNMENT);
					currentQueryPanel.add(queryWhere);
					currentQueryPanel.add(Box.createRigidArea(new Dimension(10,0)));

					JLabel queryContext = new JLabel();
					queryContext.setText("CONTEXT " + q.getState());
					queryContext.setAlignmentX(Component.LEFT_ALIGNMENT);
					currentQueryPanel.add(queryContext);

					currentPanel.add(currentQueryPanel);
					if(true){
						queriesPanel.add(currentPanel);
						queriesPanel.add(Box.createRigidArea(new Dimension(50,0)));
						currentPanel = new JPanel();
						currentPanel.setLayout(new BoxLayout(currentPanel, BoxLayout.X_AXIS));
					}
					else{
						currentPanel.add(Box.createRigidArea(new Dimension(75,0)));
					}
					count++;
				} else
					transQueries.add(q);
			}
			for(Query q : transQueries){
				JPanel currentQueryPanel = new JPanel();
				currentQueryPanel.setLayout(new BoxLayout(currentQueryPanel, BoxLayout.Y_AXIS));

				JLabel querySwitch = new JLabel();
				querySwitch.setText("SWITCH " + ((TransitionQuery)q).returnSetState());
				querySwitch.setAlignmentX(Component.LEFT_ALIGNMENT);
				currentQueryPanel.add(querySwitch);
				currentQueryPanel.add(Box.createRigidArea(new Dimension(10,0)));

				if(q.queryPattern().equals("Driver") || q.queryPattern().equals("Rider")){
					JLabel queryPattern = new JLabel();
					queryPattern.setText("PATTERN " + q.queryPattern());
					queryPattern.setAlignmentX(Component.LEFT_ALIGNMENT);
					currentQueryPanel.add(queryPattern);
					currentQueryPanel.add(Box.createRigidArea(new Dimension(10,0)));
				}
				JLabel queryWhere = new JLabel();
				queryWhere.setText("WHERE " + q.getEx());
				queryWhere.setAlignmentX(Component.LEFT_ALIGNMENT);
				currentQueryPanel.add(queryWhere);
				currentQueryPanel.add(Box.createRigidArea(new Dimension(10,0)));

				JLabel queryContext = new JLabel();
				queryContext.setText("CONTEXT " + q.getState());
				queryContext.setAlignmentX(Component.LEFT_ALIGNMENT);
				currentQueryPanel.add(queryContext);

				//queriesPanel.add(queryDesc);
				currentPanel.add(currentQueryPanel);
				if(true){
					queriesPanel.add(currentPanel);
					queriesPanel.add(Box.createRigidArea(new Dimension(50,0)));
					currentPanel = new JPanel();
					currentPanel.setLayout(new BoxLayout(currentPanel, BoxLayout.X_AXIS));
				}
				else{
					currentQueryPanel.add(Box.createRigidArea(new Dimension(75,0)));
				}
				count++;
			}
			//makes sure that the last queries were displayed
			queriesPanel.add(currentPanel);
			queriesPanel.updateUI();
		}
	}

	public void submitQuery(String context, Query query) {
		if(database.get(context) == null) {
			database.put(context, new LinkedList<Query>());
		}
		database.get(context).add(query);
		allQueries.add(query);
		System.out.println("Query submitted by save file");
	}

	private void updateQueryDropdown(String stateIn, Boolean flag){
		//System.out.println("State in: " + stateIn);
		for(String state: states)
			if(state.equals(stateIn))
			{
				if(flag){
					states.remove(stateIn);
					contextModel.removeElement(stateIn);
					destinationModel.removeElement(stateIn);
					contextList.setModel(contextModel);
				}
				else
					return;
			}

		states.add(stateIn);
		contextModel.addElement(stateIn);
		destinationModel.addElement(stateIn);
		contextList.setModel(contextModel);
		destinationList.setModel(destinationModel);

		String[] stuff = new String[states.size()];
		int ind = 0;
		for(String state: states){
			stuff[ind] = state;
			ind++;
		}

		//System.out.println(Arrays.toString(stuff));
	}



	//gets a state based on its name
	public GElement getElement(String name){
		for (GElement element: gelements){
			if (element.getLabel().equals(name))
				return element;
		}
		return null;
	}

	//gets a link based on a start state, an end state, the pattern, and the longname
	public GLink getLink(GElement start, GElement target, String longname){
		for (GLink link: glinks){
			if(link.getRealSource().equals(start) && link.getRealTarget().equals(target) && link.getNickname().equals(longname)) {
				return link;
			}
		}
		return null;
	}

	//when a new GLink is added to the system.. add it with this function.
	public void addLink(GLink newLink){
		if (!glinks.contains(newLink)){
			glinks.add(newLink);
		}

		//if it already exists, remove it
		//removeLink(newLink);
		//we are adding a new Link that already exists somewhere else (same name) set the nickname
		boolean exists = false;
		for (JLabel test : labels){
			if (test.getText().equals(newLink.getPattern()))
				exists = true;
		}
		//System.out.println("Exists: " + exists);
		if (exists) {
			if (newLink.getNickname() == null){
				//get the nickname from some other link with the same pattern
				for (GLink test: glinks){
					if (newLink.getPattern().equals(test.getPattern())){
						newLink.setNickname(test.getNickname());
						//glinks.add(newLink);
						//System.out.println("adding new instance: " + newLink.getPattern());
						//System.out.println("Links: ");
						for (GLink link: glinks){
							//System.out.println("Pattern: " + link.getPattern() + " Nickname: " + link.getNickname());
						}
						return;
					}
				}
			}
			else {
				int k = 0;
				//update the textfield
				for (JLabel test: labels){
					if (test.getText().equals(newLink.getPattern())){
						//System.out.println("setting " + test.getText() + " to " + newLink.getNickname());
						textfields.get(k).setText(newLink.getNickname());
						break;
					}
					k++;
				}
				//System.out.println("Links: ");
				for (GLink link: glinks){
					//System.out.println("Pattern: " + link.getPattern() + " Nickname: " + link.getNickname());
				}
				//update every link with the same pattern
				for (GLink test: glinks){
					if (test.getPattern().equals(newLink.getPattern())){
						//System.out.println("settingLink " + test.getPattern() + " to " + newLink.getNickname());
						test.setNickname(newLink.getNickname());
					}
				}
				//glinks.add(newLink);
				//System.out.println("updating nickname: " + newLink.getPattern());
				//System.out.println("Links: ");
				for (GLink link: glinks){
					//System.out.println("Pattern: " + link.getPattern() + " Nickname: " + link.getNickname());
				}
				return;
			}
		}
		//System.out.println("Adding brand new link: " + newLink.getPattern());

		//this is a new link, nothing has the same pattern
		//get the spacing right (so the "is" lines up on all of the links)
		int max = labels.size()+1;
		if (labels.size() < Elabels.size())
			max = Elabels.size();
		if(max > 6){
			this.tabs.setPreferredSize(new Dimension(950, (max)*30));
		}
		else
			this.tabs.setPreferredSize(new Dimension(950, 180));
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JLabel tf = new JLabel(newLink.getPattern());
		int width = tf.getBounds().width;
		tf.setForeground(newLink.getColor());
		tf.setPreferredSize(new Dimension(50, 20));
		p.add(tf);
		JLabel l = new JLabel(" is: ");
		p.add(l);
		final JTextField tf2 = new JTextField(75);
		final GLink instance = newLink;
		tf2.setText(newLink.nickname);
		tf2.setForeground(Color.BLACK);
		p.add(tf2);
		//this.linkPanel.add(p);
		tf2.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent ae) {
				//if the user enters a new nickname, update the GLink to show that
				instance.setNickname(tf2.getText());
				//keep everything the same
				for (GLink test: glinks){
					if (test.getPattern().equals(instance.getPattern())){
						test.setNickname(tf2.getText());
						mac.changeDone();
						//System.out.println("Changing...");
					}
				}
			}
		});

		//glinks.add(newLink);
		textfields.add(tf2);
		labels.add(tf);
		panels.add(p);
		//this.linkPanel.setVisible(true);
		//System.out.println("Links: ");
		for (GLink link: glinks){
			//System.out.println("Pattern: " + link.getPattern() + " Nickname: " + link.getNickname());
		}
	}

	//updates the nickname, and color of a GLink
	public void updateLink(GLink link){
		//System.out.println("UpdateLink");
		int i = 0;
		for (GLink test: glinks){
			if (test.pattern.equals(link.pattern)){
				//update the Nickname
				textfields.get(i).setText(link.getNickname());
				//update the color
				labels.get(i).setForeground(link.getColor());
				//resize it...
				labels.get(i).setPreferredSize(new Dimension(50, 20));
				return;
			}
			i++;
		}
	}

	//updates the name, nickname, and color of a GLink based on the old name
	public void updateLinkName(String oldName, GLink link){
		//System.out.println("UpdateName");
		int i = 0;
		for (GLink test: glinks){
			if (test.pattern.equals(oldName)){
				//update the name
				labels.get(i).setText(link.getPattern());
				//update the nickname
				textfields.get(i).setText(link.getNickname());
				//update the color
				labels.get(i).setForeground(link.getColor());
				//resize it...
				labels.get(i).setPreferredSize(new Dimension(50, 20));
				return;
			}
			i++;
		}
	}

	//deletes a link
	public void removeLink(GLink link){
		//System.out.println("Removing: " + link.getPattern());
		int max = glinks.size()-1;
		if (glinks.size() < gelements.size())
			max = gelements.size();
		if(max > 6){
			this.tabs.setPreferredSize(new Dimension(950, (max)*30));
		}
		else
			this.tabs.setPreferredSize(new Dimension(950, 180));
		int i = 0;
		//remove the GLink
		for (GLink test: glinks){
			if (test == link){
				glinks.remove(link);
				break;
			}
		}
		//see if a link of this pattern still exists
		boolean stillExists = false;
		for (GLink test: glinks){
			if(test.getPattern().equals(link.getPattern())){
				stillExists = true;
			}
		}
		// System.out.println("still exists: " + stillExists);
		//if there are no longer one of these transitions, remove it
		if (!stillExists){
			for(JLabel label: labels){
				if (label.getText().equals(link.getPattern())){
					//System.out.println("Removing from panel");
					//linkPanel.remove(panels.get(i));
					panels.remove(i);
					labels.remove(i);
					textfields.remove(i);
					//linkPanel.updateUI();
					return;
				}
				i++;
			}
		}
		return;
	}

	//clears all the links from the naming panel
	public void clearLink(){
		//System.out.println("clearLink");
		int i = 0;
		//this.linkPanel = new JPanel();
		//this.linkPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
		panels.clear();
		labels.clear();
		textfields.clear();
		//linkPanel.updateUI();
	}

	//finds a transition with a longname that contains this string and highlights it.
	public void highlight(String name){
		//System.out.println("highlighting with name: " + name);
		int j = -1;
		for (JTextField test: textfields){
			if (test.getText().contains(name)){
				break;
			}
		}
		if(j!= -1){
			textfields.get(j).setForeground(Color.RED);
			labels.get(j).setForeground(Color.RED);
		}
	}


	//colors a entry (label and textfield) red to indicate that it is being looked at
	public void highlightLink(GLink link){

		//System.out.println(link.getPattern());
		int i = 0;
		for (JLabel test: labels){
			if (test.getText().equals(link.getPattern())){
				labels.get(i).setForeground(Color.RED);
				textfields.get(i).setForeground(Color.RED);
				return;
			}
			i++;
		}
		int value = -1;
		return;
	}

	//unhighlights and unhovers every link
	public void unColor(){
		int i = 0;
		for (JLabel test: labels){
			Color c = Color.BLACK;
			for (GLink link: glinks){
				if (link.getPattern().equals(test.getText()))
					c = link.getColor();
			}
			test.setForeground(c);
			textfields.get(i).setForeground(Color.BLACK);
			i++;
		}
		i = 0;
		for (JLabel test: Elabels){
			Color c = Color.BLACK;
			for (GElement element: gelements){
				if (element.getLabel().equals(test.getText()))
					c = element.getColor();
			}
			test.setForeground(c);
			Etextfields.get(i).setForeground(Color.BLACK);
			i++;
		}
		return;
	}

	//colors a entry (label and textfield) bnlue to indicate that it is being looked at, unhighlight all others
	public void hoverLink(GLink link){
		int i = 0;
		for (GLink test: glinks){
			if (test == link){
				labels.get(i).setForeground(Color.BLUE);
				textfields.get(i).setForeground(Color.BLUE);
			}
			else{
				labels.get(i).setForeground(test.getColor());
				textfields.get(i).setForeground(Color.BLACK);
			}
			i++;
		}
		return;
	}

	//colors a entry (label and textfield) bnlue to indicate that it is being looked at, unhighlight all others
	public void hoverElement(GElement element){
		int i = 0;
		for (GLink test: glinks){
			if (test == element){
				Elabels.get(i).setForeground(Color.BLUE);
				Etextfields.get(i).setForeground(Color.BLUE);
			}
			else{
				Elabels.get(i).setForeground(test.getColor());
				Etextfields.get(i).setForeground(Color.BLACK);
			}
			i++;
		}
		return;
	}

	//when a new GElement is added to the system.. add it with this function.
	public void addElement(GElement newElement){
		removeElement(newElement);
		int max = labels.size();
		if (labels.size() < Elabels.size()+1)
			max = Elabels.size()+1;
		if(max > 6){
			this.tabs.setPreferredSize(new Dimension(950, (max)*30));
		}
		else
			this.tabs.setPreferredSize(new Dimension(950, 180));
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JLabel tf = new JLabel(newElement.getLabel());
		updateQueryDropdown(newElement.getLabel(),false);
		int width = tf.getBounds().width;
		tf.setForeground(newElement.getColor());
		//add spacing to make it look neat...
		tf.setPreferredSize(new Dimension(50, 20));
		p.add(tf);
		JLabel l = new JLabel(" is: ");
		p.add(l);
		final JTextField tf2 = new JTextField(75);
		final GElement instance = newElement;
		tf2.setText(newElement.getNickname());
		tf2.setForeground(Color.BLACK);
		p.add(tf2);
		//this.elementPanel.add(p);
		tf2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				//if the user enters a new nickname, update the GLink to show that
				instance.setNickname(tf2.getText());
				mac.changeDone();
			}
		});
		gelements.add(newElement);
		Etextfields.add(tf2);
		Elabels.add(tf);
		Epanels.add(p);
		//this.linkPanel.setVisible(true);
	}

	//updates the nickname, and color of a GLink
	public void updateElement(GElement element){
		int i = 0;
		for (GElement test: gelements){
			if (test.getLabel().equals(element.getLabel())){
				//update the Nickname
				Etextfields.get(i).setText(element.getNickname());
				//update the color
				Elabels.get(i).setForeground(element.getLabelColor());
				//resize it...
				Elabels.get(i).setPreferredSize(new Dimension(50, 20));
				return;
			}
			i++;
		}
	}

	//updates the name, nickname, and color of a GLink based on the old name
	public void updateElementName(String oldName, GElement element){
		int i = 0;
		for (GElement test: gelements){
			if (test.getLabel().equals(oldName)){
				//update the name
				Elabels.get(i).setText(element.getLabel());
				//update the nickname
				Etextfields.get(i).setText(element.getNickname());
				//update the color
				Elabels.get(i).setForeground(element.getLabelColor());
				//resize it...
				Elabels.get(i).setPreferredSize(new Dimension(50, 20));
				return;
			}
			i++;
		}
	}

	//deletes an element
	public GElement removeElement(GElement element){
		int max = glinks.size();
		if (glinks.size() < gelements.size()-1)
			max = gelements.size()-1;
		if(max > 6){
			this.tabs.setPreferredSize(new Dimension(950, (max)*30));
		}
		else
			this.tabs.setPreferredSize(new Dimension(950, 180));
		int i = 0;
		for (GElement test: gelements){
			if (test == element){
				gelements.remove(element);
				//elementPanel.remove(Epanels.get(i));
				Epanels.remove(i);
				Elabels.remove(i);
				Etextfields.remove(i);
				return element;
			}
			i++;
		}
		return element;
	}

	//colors a entry (label and textfield) red to indicate that it is being looked at. everything else is unhighlighted
	public void highlightElement(GElement element){
		int i = 0;
		for (GElement test: gelements){
			if (test.getLabel().equals(element.getLabel())){
				Elabels.get(i).setForeground(Color.RED);
				Etextfields.get(i).setForeground(Color.RED);
				return;
			}
			else {
				Elabels.get(i).setForeground(test.getLabelColor());
				Etextfields.get(i).setForeground(Color.BLACK);
			}
			i++;
		}
	}

	//unhighlights every element
	public void unhighlightElement(GElement element){
		int i = 0;
		for (GElement test: gelements){
			Elabels.get(i).setForeground(test.getLabelColor());
			Etextfields.get(i).setForeground(Color.BLACK);
			i++;
		}
	}

	public LinkedList<Query> getQueries(String state){
		return database.get(state);
	}

	public ArrayList<GLink> getGLinks() {
		return this.glinks;
	}

	//clears everything from the NamingPanel (all links and transitions are deleted)
	public void clear(){
		//get rid of all the links
		//linkPanel.removeAll();
		//linkPanel.updateUI();
		panels.clear();
		textfields.clear();
		labels.clear();
		glinks.clear();
		//get rid of all the elements
		//elementPanel.removeAll();
		//elementPanel.updateUI();
		Epanels.clear();
		Etextfields.clear();
		Elabels.clear();
		gelements.clear();

	}

	public static HashMap<String, LinkedList<Query>> getDatabase() {
		return database;
	}

	public void setDatabase(HashMap<String, LinkedList<Query>> database) {
		this.database = database;
	}

	public ArrayList<GElement> getGElements() {
		return this.gelements;
	}

	public void setFAMac(GViewFAMachine faGraphicPanel) {
		this.mac = faGraphicPanel;
	}

	public void setActiveStates(String string) {
		activeStateLabel.setText(convertToMultiline(string));
	}

	public static String convertToMultiline(String orig)
	{
		return "<html>" + orig.replaceAll("\n", "<br>");
	}
}
