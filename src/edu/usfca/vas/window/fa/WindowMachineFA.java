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


package edu.usfca.vas.window.fa;

import edu.usfca.xj.appkit.gview.object.GElement;
import edu.usfca.xj.appkit.gview.object.GLink;
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.appkit.utils.XJFileChooser;
import edu.usfca.xj.appkit.utils.XJLocalizable;
import edu.usfca.vas.app.Localized;
import edu.usfca.vas.data.DataWrapperFA;
import edu.usfca.vas.graphics.fa.GElementFAMachine;
import edu.usfca.vas.graphics.fa.GElementFANickName;
import edu.usfca.vas.graphics.fa.GElementFASidePanel;
import edu.usfca.vas.graphics.fa.GElementFAState;
import edu.usfca.vas.graphics.fa.GViewFAMachine;
import edu.usfca.vas.machine.fa.FAMachine;
import edu.usfca.vas.window.WindowMachineAbstract;
import edu.usfca.vas.window.tools.DesignToolsFA;
import edu.usfca.xj.appkit.app.XJApplication;
import edu.usfca.xj.appkit.document.CHAOSUtil;
import edu.usfca.xj.appkit.document.XJData;
import edu.usfca.xj.appkit.frame.XJFrame;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import connection.Connection;
import connection.Step;
import connection.StreamXMLGenerator;
import connection.XMLParser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class WindowMachineFA extends WindowMachineAbstract {

    protected WindowMachineFASettings settings = null;
    protected GElementFAMachine machine;

    protected JTextField alphabetTextField;
    protected JTextField stringTextField;
    protected JComboBox typeComboBox;
    protected JPanel mainPanel;
    //Bottom Panel
    protected GElementFANickName namingPanel;
    //Side panel
    protected GElementFASidePanel sidePanel;
    protected JSplitPane mainPanelSplit;
    protected JScrollPane mainPanelScrollPane;
    protected JScrollPane namingPanelScrollPane;
    protected ArrayList<GElement> highlighted = new ArrayList<GElement>();
	protected boolean start = true;

	protected DesignToolsFA designToolFA;

    protected WindowMachineFAOverlay overlay;
    protected boolean overlayVisible;
    
    //This is the stepList that is set by the XML parser
    protected ArrayList<Step> stepList;
    JButton startButton;
    protected String currentDocPath;
    protected ArrayList<String> activeStates;
    
    Object playingFlagLock = new Object();
	boolean playingFlag = false;
	int timeBetweenStep = 1500;

    public WindowMachineFA(XJFrame parent) {
        super(parent);
        this.machine = machine;
    }
    
    public void setMachine(GElementFAMachine machine){
    	this.machine = machine;
    }
    
    public void init() {
        setGraphicPanel(new GViewFAMachine(parent,null));
        getFAGraphicPanel().setDelegate(this);
        getFAGraphicPanel().setMachine(getDataWrapperFA().getGraphicMachine());
        getFAGraphicPanel().setRealSize(getDataWrapperFA().getSize());
        getFAGraphicPanel().adjustSizePanel();

        setLayout(new BorderLayout());
        
        add(createUpperPanel(), BorderLayout.NORTH);
        
        /*
        JSplitPane split1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createAutomataPanel(), createSidePanel());
        split1.setResizeWeight(0);
        split1.setEnabled(false);
        split1.setDividerLocation(335);
        
        JSplitPane split2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, split1, createNamingPanel());
        split2.setResizeWeight(1);
        split2.setDividerLocation(625);
        
        add(split2);
        */
        
        
        JSplitPane split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,createAutomataPanel(),createSidePanel());
        split1.setResizeWeight(1); // REIGHT view gets all extra space
        split1.setEnabled(false); // Do not allow user to set divider
        split1.setDividerLocation(625);
                
        JSplitPane split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, split1, add(createNamingPanel())); // Why add(); ??
        split2.setResizeWeight(1);
        split2.setDividerLocation(360);
        
        add(split2);

        overlay = new WindowMachineFAOverlay(parent.getJFrame(), mainPanel);
        overlay.setStringField(stringTextField);
    }

    public WindowFA getWindowFA() {
        return (WindowFA)getWindow();
    }

    public DataWrapperFA getDataWrapperFA() {
        return (DataWrapperFA)getDataWrapper();
    }

    public GViewFAMachine getFAGraphicPanel() {
        return (GViewFAMachine)getGraphicPanel();
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

        //Not needed anymore, was for NFA/DFA details
        
//        panel.add(new JLabel(Localized.getString("faWMAutomaton")));
//        typeComboBox = new JComboBox(new String[] { Localized.getString("DFA"),
//                                                    Localized.getString("NFA") });
//        typeComboBox.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                int type = typeComboBox.getSelectedIndex();
//                if(type !=getDataWrapperFA().getMachineType()) {
//                    getDataWrapperFA().setMachineType(type);
//                    changeOccured();
//                }
//            }
//        });
//
//        panel.add(typeComboBox);
//
//        panel.add(new JLabel(Localized.getString("faWMAlphabet")));
//
//        alphabetTextField = new JTextField(getDataWrapperFA().getSymbolsString());
//        alphabetTextField.setPreferredSize(new Dimension(100, 20));
//        alphabetTextField.addCaretListener(new CaretListener() {
//            public void caretUpdate(CaretEvent e) {
//                handleAlphabetTextFieldEvent();
//            }
//
//        });
//
//        alphabetTextField.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                handleAlphabetTextFieldEvent();
//            }
//        });
//
//        panel.add(alphabetTextField);
//
//        panel.add(new JLabel(Localized.getString("faWMString")));
//
//        stringTextField = new JTextField("");
//        stringTextField.setPreferredSize(new Dimension(100, 20));
//        stringTextField.addCaretListener(new CaretListener() {
//            public void caretUpdate(CaretEvent e) {
//                handleStringTextFieldEvent();
//            }
//
//        });
//
//        stringTextField.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                handleStringTextFieldEvent();
//            }
//        });
//
//        panel.add(stringTextField);
        
        //Next Button
        JButton next = new JButton(Localized.getString("faWMNext"));
        next.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {            	
            	stopPlaying();
            	
            	/**
            	 * Replace this logic (go form previous state to next)
            	 * With new logic (Run queries on current state, and if a query that wants to change a state happens, we go to the next state)
            	 */
            	unHighLightObject(sidePanel.getCurrent());
            	sidePanel.setCurrent(sidePanel.getCurrent()+1);
            	highLightObject(sidePanel.getCurrent());
            	setActiveStates(sidePanel.getCurrent());
            }
          
        });
        
      
        
        //Load Button
        JButton load = new JButton(Localized.getString("faWMLoad"));
        load.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	//System.out.println("createControlPanel in vas.window.fa.WindowMachineFA");
                //TODO
            	String docPath = changeSave();  //uncomment this to get doc path, save for Chaos, etc..
            	if (docPath == null){
            		return;
            	}
            	
            	BufferedReader br = null;
            	ArrayList<String> input = new ArrayList<String>();
            	
            	try {
					br = new BufferedReader(new FileReader(docPath));
					String temp = null;
					while ((temp = br.readLine()) != null){
						input.add(temp);
					}
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
					changeSaveError(e1);
				} catch (IOException e1) {
					e1.printStackTrace();
					changeSaveError(e1);
				}

            	sidePanel.clear();
            	for(String event: input) {
            		sidePanel.add(event);
            	}
            	
            	currentDocPath = docPath;
            }
          
        });
        
        
        //Back Button
        JButton back = new JButton(Localized.getString("faWMBack"));
        back.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	stopPlaying();
            	
            	unHighLightObject(sidePanel.getCurrent());
            	sidePanel.setCurrent(sidePanel.getCurrent()-1);
            	highLightObject(sidePanel.getCurrent());
    			setActiveStates(sidePanel.getCurrent());
            }
          
        });
        startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
        	
        	public void actionPerformed(ActionEvent e) {
        		if(WindowMachineFA.this.isStart()) {
        			
        			String data = new String("<data>\n");

        			data += StreamXMLGenerator.generate(sidePanel.getStringLabels()) + "\n";        			
        			
        			String systemXMLfile = getWindow().getDocument().getDocumentPath();
        	        systemXMLfile = systemXMLfile.substring(0, systemXMLfile.length() - 3);	//removes .fa from file path
        	        systemXMLfile = systemXMLfile.concat("XML.xml");		//adds XML.xml to file path
        	        
        	        data += CHAOSUtil.getStringFromFile(systemXMLfile);
        	        data += "</data>";
        	        
        	        if(!Connection.sendData(data)) return;
        	        
        	        stepList = XMLParser.getListHighlightObjects();
        	        activeStates = XMLParser.getActiveStates();

        			sidePanel.setCurrent(0);
        			highLightObject(0);
        			setActiveStates(0);
        			
        			WindowMachineFA.this.setStart(false);
        			WindowMachineFA.this.startButton.setLabel("Stop");
        			
        			Thread th = new Thread() {
        				public void run() {
        			        startPlaying();
        			    }
        			};
        			
        			th.start();
        		}

        		else {
        			
        			stopPlaying();
        			
        			unHighLightObject(sidePanel.getCurrent());
        			sidePanel.unHighlight();
        			stepList = new ArrayList<Step>();
        			namingPanel.setActiveStates("");

        			WindowMachineFA.this.setStart(true);
        			WindowMachineFA.this.startButton.setLabel("Start");
        		}
        	}
        });

        panel.add(load);
        panel.add(back);
        panel.add(startButton);
        panel.add(next);


        return panel;
    }

    protected void startPlaying() {
    	synchronized (playingFlagLock) {
			playingFlag = true;
		}
    	
    	do
    	{
    		try {
				Thread.sleep(timeBetweenStep);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		synchronized (playingFlagLock) {
    			if(!playingFlag) {
    				return;
    			}
			}
    		
        	unHighLightObject(sidePanel.getCurrent());
        	sidePanel.setCurrent(sidePanel.getCurrent()+1);
        	highLightObject(sidePanel.getCurrent());
        	setActiveStates(sidePanel.getCurrent());    		
    	}
    	while(sidePanel.getCurrent()!=stepList.size()-1);
    	
    	stopPlaying();
	}

	private void stopPlaying() {
		synchronized (playingFlagLock) {
			playingFlag = false;
		}
	}

	public String next(){
    	System.out.println("Highlight Next");
    	unHighlight();
    	namingPanel.unColor();
    	String next = sidePanel.highlightNext();
    	//if something is highlighted, highlight it in the naming panel and drawing as well
    	GLink transition = sidePanel.getTransition();
    	GElement target = sidePanel.getTarget();
    	if (next != null){
    		//highlight in the namingPanel
    		System.out.println(transition.getPattern());
    		namingPanel.highlightLink(transition);
    		namingPanel.highlightElement(target);
    		//highlight in the drawingPanel
    		machine.highlightShape(transition);
    		machine.highlightShape(transition.getTarget());
    		highlighted.add(transition);
    		highlighted.add(transition.getTarget());
    	}
    	getWindowFA().updateExecutionComponents();
    	return next;
    }

    //unhighlights everything in the drawing panel
    public void unHighlight(){
    	for (GElement element: highlighted){
    		machine.unhighlightShape(element);
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
    
    //makes the naming panel
    public JPanel createNamingPanel() {
    	GElementFANickName names = new GElementFANickName();
    	names.setFAMac(getFAGraphicPanel());
    	//names.setSize(new Dimension(640, 350));
    	namingPanel=names;
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
    	sidePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5)); // Align to RIGHT
    	//sidePanel.setLayout(new BorderLayout());
		sidePanel.setPreferredSize(new Dimension(350, 335));
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
		tf2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				//if the user enters a new nickname, update the GLink to show that
				instance.setNickname(tf2.getText());
			}
		});
    }

    public boolean supportsOverlay() {
        return true;
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if(visible && overlayVisible)
            overlay.setVisible(true);
        else if(!visible && overlayVisible)
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
        if(!s.equals(getDataWrapperFA().getSymbolsString())) {
            getDataWrapperFA().setSymbolsString(s);
            changeOccured();
        }
    }

    public void handleStringTextFieldEvent() {
        String s = stringTextField.getText();
        if(!s.equals(getDataWrapperFA().getString())) {
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
        if(settings == null)
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
        overlay.setString(original, original.length()-remaining.length());
    }

    public void viewSizeDidChange() {
        // do nothing
    }


	private void highLightObject(int i) {
		if(i>=0 && i<stepList.size()) {
			Step currentStep = stepList.get(i);
			if(machine.findTransition(currentStep.getSource(),currentStep.getTarget(),currentStep.getLabel())!=null) {
				machine.findTransition(currentStep.getSource(),currentStep.getTarget(),currentStep.getLabel()).setHighLight(true);	
			}
			else
			{
				System.out.println("can't find transition " + currentStep.getSource() + " " + currentStep.getTarget());
			}
			if(machine.findState(currentStep.getTarget())!=null) {
				machine.findState(currentStep.getTarget()).setHighLight(true);
			}
			repaint();
		}
	}
	
	private void setActiveStates(int i) {
		if(i>=0 && i<activeStates.size()) {
			namingPanel.setActiveStates(activeStates.get(i));
		}
	}
	
	private void unHighLightObject(int i) {
		if(i>=0 && i<stepList.size()) {
			Step currentStep = stepList.get(i);
			if(machine.findTransition(currentStep.getSource(),currentStep.getTarget(),currentStep.getLabel())!=null) {
				machine.findTransition(currentStep.getSource(),currentStep.getTarget(),currentStep.getLabel()).setHighLight(false);
			}
			if(machine.findState(currentStep.getTarget())!=null) {
				machine.findState(currentStep.getTarget()).setHighLight(false);
			}
			repaint();
		}
	}

    public boolean isStart() {
		return start;
	}

	public void setStart(boolean start) {
		this.start = start;
	}
}
