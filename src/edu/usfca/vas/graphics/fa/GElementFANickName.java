package edu.usfca.vas.graphics.fa;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.*;

import edu.usfca.vas.layout.JSONReaders;
import edu.usfca.xj.appkit.gview.object.GElement;
import edu.usfca.xj.appkit.gview.object.GLink;
import query.Query;
import query.QueryImpl;


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
	JPanel linkPanel;
	JPanel elementPanel;
	JPanel executionPanel;
	JPanel queryPanel;
	JPanel  queryEditPanel;
	JTabbedPane tabs;
	GViewFAMachine mac;
	DefaultComboBoxModel model = new DefaultComboBoxModel();
	
	JComboBox contextList = new JComboBox(model);
	String condition = "";
	String set = "";
	ArrayList<String> states = new ArrayList<String>();
	HashMap<String,JButton> buttonCache = new HashMap<String,JButton>();
	TextField setFill = new TextField("Press enter to submit command", 20);	
	TextField conditionFill = new TextField("Press enter to submit command", 20);
	HashMap<String, LinkedList<Query>> database = new HashMap();
	
	public void removeStateFromDropdown(String state){
		
	}
	
	//creates the naming panel
	public GElementFANickName() {
		super();
		this.setName("LowerPanel");
		this.tabs = new JTabbedPane();
		this.tabs.setName("LowerPanelTabs"); //Named this so it can be used in xml file
		tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabs.setPreferredSize(new Dimension(950, 180));
		//this.add(tabs);
		this.linkPanel = new JPanel();
		linkPanel.setName("LinkPanel");
		this.elementPanel = new JPanel();
		this.executionPanel = new JPanel();
		this.linkPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
		this.elementPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
		this.executionPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
		executionPanel.add(activeStateLabel);
 		//this.linkPanel.setSize(new Dimension(600, 100));
		//this.elementPanel.setSize(new Dimension(600, 100));
		tabs.addTab("Transition Labels", this.linkPanel);
		tabs.addTab("State Names", this.elementPanel);
		tabs.addTab("Process Summaries", this.executionPanel);
		
		makeQueryStuff();
		
		//tabs.addTab("New Tab", null);
		tabs.setVisible(true);
		linkPanel.setVisible(true);
		JScrollPane scroll = new JScrollPane(tabs, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); // TODO Check
		scroll.setName("LowerScrollPane");
		scroll.setPreferredSize(new Dimension(950, 190));
		this.add(scroll);

	}
	
	private void makeQueryStuff(){
		//Query Creation
		this.queryPanel = new JPanel();
		this.queryEditPanel = new JPanel();
		this.queryPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
		this.queryEditPanel.setLayout(new BoxLayout(queryEditPanel, BoxLayout.Y_AXIS));
		tabs.addTab("Pertaining Queries", this.queryPanel);
		tabs.addTab("Query Editor", this.queryEditPanel);
		
		JLabel context = new JLabel("Context");
		JPanel contextPanel = new JPanel(); 
		JButton addContext = new JButton("+");
		contextPanel.setLayout(new BoxLayout(contextPanel, BoxLayout.X_AXIS));
		contextPanel.add(Box.createRigidArea(new Dimension(5,0)));
		contextPanel.add(context);
		contextPanel.add(Box.createRigidArea(new Dimension(10,0)));
		contextPanel.add(contextList);
		contextPanel.add(Box.createRigidArea(new Dimension(10,0)));
		contextPanel.add(addContext);
		contextPanel.add(Box.createRigidArea(new Dimension(700,0)));
		
		JLabel pertaining =  new JLabel("For");
		JComboBox pertainingList = new JComboBox();
		JButton addPertain = new JButton("+");
		JPanel pertainPanel = new JPanel();
		pertainPanel.setLayout(new BoxLayout(pertainPanel, BoxLayout.X_AXIS));
		pertainPanel.add(Box.createRigidArea(new Dimension(5,0)));
		pertainPanel.add(pertaining);
		pertainPanel.add(Box.createRigidArea(new Dimension(10,0)));
		pertainPanel.add(pertainingList);
		pertainPanel.add(Box.createRigidArea(new Dimension(10,0)));
		pertainPanel.add(addPertain);
		pertainPanel.add(Box.createRigidArea(new Dimension(700,0)));
		
		JPanel frequencyPanel = new JPanel();
		JLabel frequency = new JLabel("Frequency");
		JComboBox frequencyList = new JComboBox(new String[] {"once","continuous"});
		frequencyPanel.setLayout(new BoxLayout(frequencyPanel, BoxLayout.X_AXIS));
		frequencyPanel.add(Box.createRigidArea(new Dimension(5,0)));
		frequencyPanel.add(frequency);
		frequencyPanel.add(Box.createRigidArea(new Dimension(10,0)));
		frequencyPanel.add(frequencyList);
		frequencyPanel.add(Box.createRigidArea(new Dimension(700,0)));
		
		JPanel conditionPanel = new JPanel();
		JLabel condition = new JLabel("Condition");
		conditionFill.addActionListener(this);
		conditionPanel.setLayout(new BoxLayout(conditionPanel, BoxLayout.X_AXIS));
		conditionPanel.add(Box.createRigidArea(new Dimension(5,0)));
		conditionPanel.add(condition);
		conditionPanel.add(Box.createRigidArea(new Dimension(5,0)));
		conditionPanel.add(conditionFill).setForeground(Color.BLACK);
		conditionPanel.add(Box.createRigidArea(new Dimension(693,0)));
		
		JPanel setPanel = new JPanel();
		setPanel.setLayout(new BoxLayout(setPanel, BoxLayout.X_AXIS));
		setPanel.add(Box.createRigidArea(new Dimension(5,0)));
		JLabel set = new JLabel("set");
		setFill.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				
			}
		});
		setPanel.add(Box.createRigidArea(new Dimension(5,0)));
		setPanel.add(set);
		setPanel.add(Box.createRigidArea(new Dimension(5,0)));
		setPanel.add(setFill).setForeground(Color.BLACK);
		setPanel.add(Box.createRigidArea(new Dimension(656,0)));
		
		JPanel buttons = new JPanel();
		JButton clear = new JButton("Clear");
		clear.addActionListener(this);
		JButton submit = new JButton("Submit");
		submit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				
			}
		});
		buttonCache.put("Clear", clear);
		buttonCache.put("Submit", submit);
		submit.addActionListener(this);
		buttons.add(clear);
		buttons.add(submit);
				
		queryEditPanel.add(contextPanel);
		queryEditPanel.add(pertainPanel);
		queryEditPanel.add(frequencyPanel);
		queryEditPanel.add(conditionPanel);
		queryEditPanel.add(setPanel);
		queryEditPanel.add(buttons);
	}
	
	public void actionPerformed(ActionEvent e){
		if(e.getSource().equals(buttonCache.get("Clear"))){
			conditionFill.setText("");
			setFill.setText("");
			System.out.println("Cleared");}
		else if(e.getSource().equals(buttonCache.get("Submit"))){
			String eval = conditionFill.getText();
			String set = setFill.getText();
			Query query = new QueryImpl(eval, set);
			System.out.println(query.queryInfo());
			//If its not in there, put it in there
			if(database.get(contextList.getSelectedItem()) == null){
				database.put((String)contextList.getSelectedItem(), new LinkedList<Query>());
			}
			database.get((String)contextList.getSelectedItem()).add(query);
			System.out.println("Submitted");}
	}
	
	private void updateQueryDropdown(String stateIn, Boolean flag){
		//System.out.println("State in: " + stateIn);
		for(String state: states)
			if(state.equals(stateIn))
			{
				if(flag){
					states.remove(stateIn);
					model.removeElement(stateIn);
					contextList.setModel(model);
				}
				else
					return;
			}
		
		states.add(stateIn);
		model.addElement(stateIn);
		contextList.setModel(model);
		
		String[] stuff = new String[states.size()];
		int ind = 0;
		for(String state: states){
			stuff[ind] = state;
			ind++;
		}
		
		System.out.println(Arrays.toString(stuff));
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
			if (test.getText().equals(newLink.getPattern())){
				exists = true;}
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
				//update the text field
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
		this.linkPanel.add(p);
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
					 linkPanel.remove(panels.get(i));
					 panels.remove(i);
					 labels.remove(i);
					 textfields.remove(i);
					 linkPanel.updateUI();
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
		 this.linkPanel = new JPanel();
		 this.linkPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
	     panels.clear();
		 labels.clear();
		 textfields.clear();
		 linkPanel.updateUI();
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
		 	p.setName("TabsPanel");
			JLabel tf = new JLabel(newElement.getLabel());
			updateQueryDropdown(newElement.getLabel(), false);
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
			this.elementPanel.add(p);
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
					 //updateQueryDropdown(element.getLabel(), true);
					 elementPanel.remove(Epanels.get(i));
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
		 
		 public ArrayList<GLink> getGLinks() {
			 return this.glinks;
		 }
		 
		 //clears everything from the NamingPanel (all links and transitions are deleted)
		 public void clear(){
			 //get rid of all the links
			 linkPanel.removeAll();
			 linkPanel.updateUI();
			 panels.clear();
			 textfields.clear();
			 labels.clear();
			 glinks.clear(); 
			 //get rid of all the elements
			 elementPanel.removeAll();
			 elementPanel.updateUI();
			 Epanels.clear();
			 Etextfields.clear();
			 Elabels.clear();
			 gelements.clear();
			
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

		public LinkedList<Query> getUpdatedQueries(GElement findState) {
			String name = findState.getLabel();
			System.out.println("GElementFANickName getUpdatedQueries, Name of the query: "+name);
			if(!(database.get(name) instanceof LinkedList<?>)){
				System.out.println("It returned null");
				return null;
			}
			for (Query n : database.get(name)) {
				System.out.println("GElementFANickName getUpdatedQueries, for query in database: "+n.queryInfo());
			}
			return database.get(name);
		}
}