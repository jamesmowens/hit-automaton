/*

[The "BSD licence"]
Copyright (c) 2004 Jean Bovet
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Modified: MaryAnn VanValkenburg (mevanvalkenburg@wpi.edu) 02/26/2017, updateSidePanelVariables
 */

package edu.usfca.vas.window.fa;

import edu.usfca.vas.layout.LeftSideBar;
import edu.usfca.xj.appkit.gview.object.GElement;
import edu.usfca.xj.appkit.gview.object.GLink;
import query.Query;
import edu.usfca.vas.app.Localized;
import edu.usfca.vas.data.DataWrapperFA;
import edu.usfca.vas.graphics.fa.GElementFAMachine;
import edu.usfca.vas.graphics.fa.GElementFANickName;
import edu.usfca.vas.graphics.fa.GElementFASidePanel;
import edu.usfca.vas.graphics.fa.GViewFAMachine;
import edu.usfca.vas.machine.fa.FAMachine;
import edu.usfca.vas.window.WindowMachineAbstract;
import edu.usfca.vas.window.tools.DesignToolsFA;
import edu.usfca.xj.appkit.frame.XJFrame;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import connection.Step;
import connection.XMLParser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import Query.*;

public class WindowMachineFA extends WindowMachineAbstract {

	protected WindowMachineFASettings settings = null;
	protected GElementFAMachine machine;

	protected JTextField alphabetTextField;
	protected JTextField stringTextField;
	protected JComboBox typeComboBox;
	protected JPanel mainPanel;
	// Bottom Panel
	protected GElementFANickName namingPanel;
	// Side panel
	protected GElementFASidePanel sidePanel;
	protected JSplitPane mainPanelSplit;
	protected JScrollPane mainPanelScrollPane;
	protected JScrollPane namingPanelScrollPane;
	protected ArrayList<GElement> highlighted = new ArrayList<GElement>();
	protected boolean start = true;

	protected DesignToolsFA designToolFA;

	protected WindowMachineFAOverlay overlay;
	protected boolean overlayVisible;
	protected GElement currentState = null;

	// This is the stepList that is set by the XML parser
	// protected ArrayList<Step> stepList;
	protected ArrayList<Step> stepList = new ArrayList<Step>();
	JButton startButton;
	JButton querySaveButton, queryLoadButton;
	protected String currentDocPath;
	protected ArrayList<String> activeStates = new ArrayList();

	protected ArrayList<DataNode> dataList;
	protected int dataIndex = 0;
	protected DataNode currentData;

	Object playingFlagLock = new Object();
	boolean playingFlag = false;
	int timeBetweenStep = 1500;
	String dataDocPath = "";

	public WindowMachineFA(XJFrame parent) {
		super(parent);
		this.machine = machine;
	}

	public void setMachine(GElementFAMachine machine) {
		this.machine = machine;
	}

	public void init() {
		setGraphicPanel(new GViewFAMachine(parent, null));
		getFAGraphicPanel().setDelegate(this);
		getFAGraphicPanel().setMachine(getDataWrapperFA().getGraphicMachine());
		getFAGraphicPanel().setRealSize(getDataWrapperFA().getSize());
		getFAGraphicPanel().adjustSizePanel();

		setLayout(new BorderLayout());

		add(createUpperPanel(), BorderLayout.NORTH);

		JSplitPane split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createAutomataPanel(), createSidePanel());
		split1.setResizeWeight(1); // REIGHT view gets all extra space
		split1.setEnabled(false); // Do not allow user to set divider
		split1.setDividerLocation(625);

		JSplitPane split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, split1, add(createNamingPanel())); // Why
																											// add();
																											// ??
		split2.setResizeWeight(1);
		split2.setDividerLocation(360);

		add(split2);

		overlay = new WindowMachineFAOverlay(parent.getJFrame(), mainPanel);
		overlay.setStringField(stringTextField);
	}

	public WindowFA getWindowFA() {
		return (WindowFA) getWindow();
	}

	public DataWrapperFA getDataWrapperFA() {
		return (DataWrapperFA) getDataWrapper();
	}

	public GViewFAMachine getFAGraphicPanel() {
		return (GViewFAMachine) getGraphicPanel();
	}

	public JPanel createUpperPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setMaximumSize(new Dimension(99999, 30));

		panel.add(designToolFA = new DesignToolsFA(), BorderLayout.WEST);
		panel.add(createControlPanel(), BorderLayout.EAST);

		getFAGraphicPanel().setDesignToolsPanel(designToolFA);

		return panel;
	}

	public JPanel createControlPanel() {
		JPanel panel = new JPanel();
		panel.setMaximumSize(new Dimension(99999, 30));

		// Load Button
		JButton load = new JButton(Localized.getString("faWMLoad"));
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// System.out.println("createControlPanel in
				// vas.window.fa.WindowMachineFA");
				String docPath = changeSave(); // uncomment this to get doc
												// path, save for Chaos, etc..
				System.out.println(docPath);
				// dataDocPath = docPath;
				if (docPath == null) {
					return;
				}
				currentDocPath = docPath;
				System.out.println(currentDocPath);

				updateSidePanelVariables();
			}
		});

		querySaveButton = new JButton("Save Queries");
		querySaveButton.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new FileNameExtensionFilter(
					"Context-Aware Event Stream Analytics Report (.caesar)",
					".caesar", "caesar", ".CAESAR", "CAESAR"));
			if(namingPanel != null && chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				String chosenName = chooser.getSelectedFile().getAbsolutePath();
				if(chosenName.lastIndexOf(".") < 0 || !chosenName.substring(chosenName.lastIndexOf(".")).equals("caesar")) {
					chosenName += ".caesar";
				}
				DataQuerySave save = new DataQuerySave(namingPanel.getDatabase());
				save.writeToFile(chosenName);
			} else {
				System.err.println("Nothing to write");
			}
		});
		panel.add(querySaveButton);

		queryLoadButton = new JButton("Load Queries");
		queryLoadButton.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new FileNameExtensionFilter(
					"Context-Aware Event Stream Analytics Report (.caesar)",
					".caesar", "caesar", ".CAESAR", "CAESAR"));
			if(namingPanel != null && chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				DataQuerySave save = new DataQuerySave(chooser.getSelectedFile());
				final Map<String, LinkedList<Query>> loadedDatabase = save.getQueryDatabase();
				for(String k : loadedDatabase.keySet()) {
					final LinkedList<Query> queries = loadedDatabase.get(k);
					for(Query q : queries) {
						namingPanel.submitQuery(k, q);
					}
				}
				//namingPanel.setDatabase(save.getQueryDatabase());
			} else {
				System.err.println("WindowMachineFA: queryLoadButton: Nothing to write");
			}
		});
		panel.add(queryLoadButton);

		startButton = new JButton("Start");
		startButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (WindowMachineFA.this.isStart()) {

					updateSidePanelVariables();
					// TODO uncomment this out
					dataList = XMLParser.getListDataNodes(currentDocPath);
					stepList.add(grabFirstStep());
					currentData = dataList.get(0);


					highLightObject();

					WindowMachineFA.this.setStart(false);
					WindowMachineFA.this.startButton.setLabel("Stop");

					sidePanel.initDataPanel(dataList);
					Thread th = new Thread() {
						public void run() {
							startPlaying();
						}
					};
					th.start();
				}

				else {

					stopPlaying();

					/**
					 * Replace this logic (go form previous state to next) With
					 * new logic (Run queries on current state, and if a query
					 * that wants to change a state happens, we go to the next
					 * state)
					 */
					unHighlight();
					stepList = new ArrayList<Step>();

					WindowMachineFA.this.setStart(true);
					WindowMachineFA.this.startButton.setLabel("Start");
				}
			}
		});

		panel.add(load);
		panel.add(startButton);

		return panel;
	}

	protected Step grabFirstStep() {
		// TODO implement
		String source = "Traffic Monitoring";
		Step firstStep = new Step(source, source, "firstStep");
		return firstStep;
	}

	protected void startPlaying() {
		synchronized (playingFlagLock) {
			playingFlag = true;
		}

		do {
			try {
				// This is what dictates the speed at which the switching occurs
				Thread.sleep(timeBetweenStep);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			{
			// This goes in order of the indexes of the current line in the
			// flows of the side panel.
			// runQueriesOnCurrentStates();
			System.out.println("Unhighlighting");
			unHighlight();
			// Put currentData node into variableMap
			System.out.println("updating data");
			UpdateData.updateData(currentData,GElementFAMachine.variableMap); //TODO is this the right map?
			System.out.println("Getting the current State");
			GElement state = machine.getCurrentState();
			System.out.println("Highlighting & running queries");
			highLightObject();
			//setActiveStates(sidePanel.getCurrent());
			System.out.println("Setting new data");
			setNewData();
			sidePanel.advanceDataList(); //Advance the currently displayed data
			//LAYOUTTODO
			//stepList
			}
		}
		while(stepList.size() > 0);
		//Might be a bug here
		//while (stepList.size() >= 0);

		stopPlaying();
}

	private void stopPlaying() {
		synchronized (playingFlagLock) {
			playingFlag = false;
		}
	}

	private void setNewData(){
		this.dataIndex++;
		if(dataIndex < dataList.size()){stopPlaying();}
		System.out.println("The data that is gonna be next for stuff(window machine FA pespective): " + this.dataList.get(this.dataIndex).getCost());
		this.currentData = this.dataList.get(this.dataIndex);
		System.out.println("The current Data was succesfully set to the new node");
	}

	public String next() {
		System.out.println("Highlight Next");
		unHighlight();
		namingPanel.unColor();
		String next = sidePanel.highlightNext();
		// if something is highlighted, highlight it in the naming panel and
		// drawing as well
		GLink transition = sidePanel.getTransition();
		GElement target = sidePanel.getTarget();
		if (next != null) {
			// highlight in the namingPanel
			System.out.println(transition.getPattern());
			namingPanel.highlightLink(transition);
			namingPanel.highlightElement(target);
			// highlight in the drawingPanel
			machine.highlightShape(transition);
			machine.highlightShape(transition.getTarget());
			highlighted.add(transition);
			highlighted.add(transition.getTarget());
		}
		getWindowFA().updateExecutionComponents();
		return next;
	}

	// unhighlights everything in the drawing panel
	public void unHighlight() {
		System.out.println("We made it to the unHighlight() method");
		System.out.println("Highlighted list size: " + highlighted.size());
		for (GElement element : highlighted) {
			System.out.println("unhighlighting this element: " + element.getLabel());
			machine.findState(element.getLabel()).setHighLight(false);
			// machine.unhighlightShape(element);
		}
		highlighted.clear();
	}

	public JComponent createAutomataPanel() {
		mainPanelScrollPane = new JScrollPane(getGraphicPanel());
		mainPanelScrollPane.setPreferredSize(new Dimension(640, 480));
		mainPanelScrollPane.setWheelScrollingEnabled(true);

		mainPanelSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainPanelSplit.setContinuousLayout(true);

		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(mainPanelScrollPane, BorderLayout.CENTER);

		return mainPanel;
	}

	// makes the naming panel
	public JPanel createNamingPanel() {
		GElementFANickName names = new GElementFANickName();
		names.setFAMac(getFAGraphicPanel());
		// names.setSize(new Dimension(640, 350));
		namingPanel = names;
		namingPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
		namingPanel.setPreferredSize(new Dimension(300, 200));
		
    	setVisible(true);
    	
    	getDataWrapperFA().getMachine().setNaming(namingPanel);
    	return namingPanel;
    	
    }
    //makes the side panel
    public JPanel createSidePanel() {
    	GElementFASidePanel side = new GElementFASidePanel();
        //side.setSize(new Dimension(250, 400)); // Why commented?
    	sidePanel=side;
    	//sidePanel.setLayout(new GridLayout(0,1));
    	//sidePanel.setLayout(new BorderLayout()); // Align// to// RIGHT
    	//sidePanel.setLayout(new BorderLayout());
		//sidePanel.setPreferredSize(new Dimension(350, 335));
    	setVisible(true);
    	getDataWrapperFA().getMachine().setSide(sidePanel);
    	return sidePanel;
    	
    }
    
    //when a new GLink is added to the system.. add it with this function. 
    public void addGLinkName(GLink newLink){
    	JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JLabel tf = new JLabel(newLink.pattern + " is: ");
		tf.setForeground(newLink.getColor());
		p.add(tf);
		final JTextField tf2 = new JTextField(35);
		final GLink instance = newLink;
		tf2.setText(newLink.nickname);
		tf2.setForeground(Color.BLACK);
		p.add(tf2);
		namingPanel.add(p);
		tf2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// if the user enters a new nickname, update the GLink to show
				// that
				instance.setNickname(tf2.getText());
			}
		});
	}

	public boolean supportsOverlay() {
		return true;
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible && overlayVisible)
			overlay.setVisible(true);
		else if (!visible && overlayVisible)
			overlay.setVisible(false);
	}

	public boolean isOverlayVisible() {
		return overlay.isVisible();
	}

	public void toggleOverlayVisibility() {
		overlay.setVisible(!overlay.isVisible());
		overlayVisible = overlay.isVisible();
	}

	// *** Event methods

	public void handleAlphabetTextFieldEvent() {
		String s = alphabetTextField.getText();
		if (!s.equals(getDataWrapperFA().getSymbolsString())) {
			getDataWrapperFA().setSymbolsString(s);
			changeOccured();
		}
	}

	public void handleStringTextFieldEvent() {
		String s = stringTextField.getText();
		if (!s.equals(getDataWrapperFA().getString())) {
			getDataWrapperFA().setString(s);
			overlay.textChanged();
			changeOccured();
		}
	}

	public String getString() {
		return stringTextField.getText();
	}

	// *** Public methods

	public FAMachine convertNFA2DFA() {
		return getDataWrapperFA().getMachine().convertNFA2DFA();
	}

	public void setFAMachine(FAMachine machine) {
		getDataWrapperFA().setMachine(machine);
		getDataWrapperFA().getGraphicMachine().setMachine(machine);
		getDataWrapperFA().getGraphicMachine().reconstruct();

		getFAGraphicPanel().setMachine(getDataWrapperFA().getGraphicMachine());
		getFAGraphicPanel().centerAll();
		getFAGraphicPanel().repaint();
	}

	public void rebuild() {
		super.rebuild();
		getFAGraphicPanel().setMachine(getDataWrapperFA().getGraphicMachine());
	}

	public void setTitle(String title) {
		getWindowFA().setWindowMachineTitle(this, title);
		getDataWrapperFA().setName(title);
		changeOccured();
	}

	public String getTitle() {
		return getWindowFA().getWindowMachineTitle(this);
	}

	public void displaySettings() {
		if (settings == null)
			settings = new WindowMachineFASettings(this);

		settings.display();
	}

	public void setGraphicsSize(int dx, int dy) {
		getGraphicPanel().setRealSize(dx, dy);
		getDataWrapperFA().setSize(new Dimension(dx, dy));
		changeOccured();
	}

	public Dimension getGraphicSize() {
		return getGraphicPanel().getRealSize();
	}

	public void setDebugInfo(String remaining) {
		String original = stringTextField.getText();
		overlay.setString(original, original.length() - remaining.length());
	}

	private void highLightObject() {
		updateSidePanelVariables();
		// Always grabs the first one by default
		// Step currentStep = stepList.get(0);
		// Not sure if we need this anymore
		/*
		 * if(machine.findTransition(currentStep.getSource(),currentStep.
		 * getTarget(),currentStep.getLabel())!=null) {
		 * machine.findTransition(currentStep.getSource(),currentStep.getTarget(
		 * ),currentStep.getLabel()).setHighLight(true); } else {
		 * System.out.println("can't find transition " + currentStep.getSource()
		 * + " " + currentStep.getTarget()); }
		 */
		if (stepList.size() < 1) {
			return;
		}
		Step currentStep = stepList.get(0);
		if (machine.findState(currentStep.getTarget()) != null) {
			GElement state = machine.findState(currentStep.getTarget());
			//System.out.println("State should be highlighted");

			// This is the highlighting part
			System.out.println("The highlight is about to be set");
			state.setHighLight(true);
			System.out.println("The highlight was set");
			// add to list of highlighted states
			highlighted.add(state);

			// This is the part that updates the query list, then runs the
			// queries, then grabs any steps from the queries and puts it in
			// The reason we want to run this while the highlighting is
			// happening is so the user can update the queries at any point
			LinkedList<Query> updatedQueries = namingPanel.getQueries(state.getLabel());
			machine.addQueries(machine.findState(currentStep.getTarget()), updatedQueries);

			this.stepList.clear();
			while (state != null) {
				state.runQueries();
				// This grabs the step list from the state, then the step list
				// in the state should clear.
				addSteps(state.grabStepList());
				// TODO Gets the parents once the parent methods work
				// state = state.getParentState();
				state = null;
			}
		}
		repaint();
	}

	private void updateSidePanelVariables() {
		BufferedReader br = null;
		ArrayList<String> input = new ArrayList<String>();

		// Add every element of the variableMap to sidepanel
		for (String key : GElementFAMachine.variableMap.keySet()) {
			double value = GElementFAMachine.variableMap.get(key).getValue();
			if (key == "time") {
				int time_hr = (int) value / 60;
				int time_min = (int) value % 60;
				input.add(key+" = "+time_hr+":"+time_min);
			} else {
			input.add(key+" = "+value);
			}
		}

		sidePanel.clear();
		for (String event : input) {
			sidePanel.add(event);
		}
	}

	private void addSteps(ArrayList<Step> stepList) {
		this.stepList.addAll(stepList);
	}

	private void setActiveStates(int i) {
		if (i >= 0 && i < activeStates.size()) {
			namingPanel.setActiveStates(activeStates.get(i));
		}
	}

	private void unHighLightObject() {
		// iterates through step list...keep this
		// Gets the first step in the step list right now
		Step currentStep = stepList.get(0);
		if (machine.findTransition(currentStep.getSource(), currentStep.getTarget(), currentStep.getLabel()) != null) {
			machine.findTransition(currentStep.getSource(), currentStep.getTarget(), currentStep.getLabel())
					.setHighLight(false);
		}
		if (machine.findState(currentStep.getTarget()) != null) {
			machine.findState(currentStep.getTarget()).setHighLight(false);
		}
		repaint();
	}

	public boolean isStart() {
		return start;
	}

	public void setStart(boolean start) {
		this.start = start;
	}

	@Override
	public void viewSizeDidChange() {
		// TODO Auto-generated method stub
	}

	public Component getMasterComp() {
    	return this.mainPanel;
	}
}
