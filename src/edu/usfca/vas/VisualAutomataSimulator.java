package edu.usfca.vas;

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
 * Help glossary set up here & some startup options. MAIN FILE.
 */

import java.awt.Frame;
import java.awt.Window;
import java.lang.reflect.Method;
import java.net.URL;

import edu.usfca.vas.app.*;
import edu.usfca.vas.data.DataFA;
import edu.usfca.vas.window.fa.WindowFA;
import edu.usfca.xj.appkit.app.XJApplication;
import edu.usfca.xj.appkit.app.XJApplicationDelegate;
import edu.usfca.xj.appkit.frame.XJPanel;
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.appkit.update.XJUpdateManager;
import edu.usfca.xj.foundation.XJSystem;

import javax.swing.*;
import javax.swing.plaf.synth.SynthLookAndFeel;

import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import com.nilo.plaf.nimrod.NimRODTheme;

public class VisualAutomataSimulator extends XJApplicationDelegate {

    public Class appPreferencesPanelClass() {
        return Preferences.class;
    }

    public XJPanel appInstanciateAboutPanel() {
        return new About();
    }

    public boolean appHasPreferencesMenuItem() {
        return true;
    }

    public boolean appShouldQuitAfterLastWindowClosed() {
        return false;
    }

    public Class appPreferencesClass() {
        return VisualAutomataSimulator.class;
    }

    public void appShowHelp(int i) {		//displays help message depending on option selected
        //XJAlert.display(null, Localized.getString("waHelpTitle"),
                        //Localized.getString("waHelpMessage"));
        switch(i) {
        	case 0:
        		XJAlert.display(null, "Opening Files",
                        "File->Open: Make sure to select the .fa file");
        		break;
        	case 1:
        		XJAlert.display(null, "Saving Files",
                        "File->Save As: Saves as both a .fa and .xml document.\n'XML' will be appended " +
                        "to the end of the .xml file name. The .fa files contain the graphical" +
                        " information for the document so that they can be reopened in the application" +
                        " and edited. The .xml files only contain the non-graphical information and " +
                        "are meant for the CHAOS system. Note: Changes through the bottom naming panel will not" +
                        " register as changes for the purposes for enabling Save. You will have to perform some action" +
                        " on the drawing panel to enable Save (such as moving).");
        		break;
        	case 2:
        		XJAlert.display(null, "Export Machine",
                        "File->Export Machine: Saves as only the .xml file, this menu option is used " +
                        "mostly for debugging " +
                        "purposes.");
        		break;
        	case 3:
        		XJAlert.display(null, "Atomic States",
                        "To draw an atomic state select the circle icon on the toolbar and then click anywhere in the " +
                        " drawing panel to draw an atomic state. Enter in a short name and " +
                        "select a color.\n\r" +
                        "Double click an atomic state to change its short name, color, or long name. " +
                        "Right click on an atomic state to change its color.");
        		break;
        	case 4:
        		XJAlert.display(null, "Non-Atomic State",
                        "To draw a non-atomic state select the rectangle icon on the toolbar and then click twice in the drawing" +
                        " panel, first for the top left corner and then for the" +
                        " bottom right corner. Enter in a short name and select a color.\n\r" +
                        "To edit the short name or long name double click on a non-atomic state. To change" +
                        " the color, to collapse, or to set the contains color right click" +
                        " on the non-atomic state.");
        		break;
        	case 5:
        		XJAlert.display(null, "Transitions",
                        "Select the arc icon on the toolbar and then select two states to create a transition." +
                        " A transition can only be created between two circles or from a rectangle to a circle.\n\r" +
                        "Right click on a transition to edit the short name, color, or long name.");
        		break;
        	case 6:
        		XJAlert.display(null, "Collapsing",
                        "Right click on a rectangle to collapse it. Everything inside will be saved as is.");
        		break;
        	case 7:
        		XJAlert.display(null, "Entering Transition",
                        "Right click on an atomic state and select 'Set As Starting State.' The transition coming from this state" +
                        " is designated as the enterting transition.");
        		break;
        	case 8:
        		XJAlert.display(null, "Long Names",
                        "There are long names for both states and transitions." +
                        " These can be edited by double clicking a state/transition or by using the bottom panel in the application." +
                        " If using the bottom panel, once a long name is made, or changed, a user must push 'enter'" +
                        " before it will take effect." +
                        " Transitions with the same short names will have the same long names automatically." +
                        " States cannot have the same short names but they can have the same long names.");
        		break;
        	case 9:
        		XJAlert.display(null, "Short Names",
                        "There are short names for both states and transitions." +
                        " The short name can be edited by double clicking a state/transition." +
                        " If two short names for a transition" +
                        " are the same then they will be given the same long name." +
                        " States cannot have the same short names but can have the same long names.");
        		break;
        	case 10:
        		XJAlert.display(null, "Colors",
                        "Colors for states & transitions can be changed by double clicking or right clicking on" +
                        " a state/transition. For contains color any states contained inside will be given" +
                        " the selected color. Transitions will not be affected.");
        		break;
        	case 11:
        		XJAlert.display(null, "Accepting State",
                        "To turn an atomic state into an accepting state right click on it and select" +
                        " 'Set As Accepting State.' Accepting states are represented as a double circle.");
        		break;
        	case 12:
        		XJAlert.display(null, "Tabs",
                        "Machine->New: Will create a new tab. Machine->Close will close the tab.");
        		break;
        	case 13:
        		XJAlert.display(null, "Stream",
                        "Edit this in appShowHelp() in edu.usfca.vas.VisualAutomataSimulator");
        		break;
        	case 14:
        		XJAlert.display(null, "Deleting Objects",
                        "Right click on an object and select the 'delete' option. At this time there is no undo nor is" +
                        " there confirmation when deleting.");
        		break;
        		
        	//HIT Glossary, not used 
//        	case 15:
//        		//Despite weird symbols in Java, they should still all work (at least on Win8 x64)
//        		XJAlert.display(null, "HIT Glossary",
//                        "Definition 1 (Hierarchical Instantiating Timed automaton (HIT)). Let Labels" +
//                        " be the set of transition labels. A HIT automaton H is a quintuple" +
//                        " (S; Start;End; children; T) where:\n\r- S is a set of states." +
//                        " Start ⊆ S is a set of start states. End ⊆ S is a set of end states.\n\r" +
//                        " - Children : S → 2^S edu.usfca.vas.maps each state to the set of its direct substates." +
//                        " A state s is atomic if children(s) = ∅. An atomic state corresponds" +
//                        " to a state of a classical Finite State Automaton. Otherwise s is non-atomic," +
//                        " i.e., a HIT automaton itself. s is said to be parent of the states in" +
//                        " children(s). A state without a parent is a root state. Descendants of s are" +
//                        " the direct or indirect substates of s, denoted descendants(s)." +
//                        " Ancestors of s are the direct or indirect superstates of s, denoted ancestors(s).\n\r" +
//                        "- T ⊆ S × Labels × S is a set of transitions. A transition t has a source state" +
//                        " source(t), a label label(t), and a target state target(t), denoted t : source" +
//                        " -label-> target. label(t) consists of a literal literal(label(t)) and" +
//                        " constraints constraints(label(t)). t is terminating if source(t) ∈ End. t is" +
//                        " instantiating if target(t) ∈ Start. t is an enter transition if it is" +
//                        " instantiating and has no source state, denoted t : ∅ -label-> target.");
//				break;
        	default:
        		XJAlert.display(null, "Whoops",
                        "Something went wrong in the help menu!");
        		break;
        }
    }

    public static void main(String[] args) {
        setTheme();
        if (args.length == 7 && args[0].equals("-t")) {
            new Test(Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                    Integer.parseInt(args[3]), Integer.parseInt(args[4]),
                    Integer.parseInt(args[5]), Integer.parseInt(args[6]));
            return;
        } else if (args.length == 1 && args[0].equals("-b")) {
//            new BatchTestTM();
            return;
        }

        XJApplication.run(new VisualAutomataSimulator(), args);
    }

    private static void setTheme() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    initLookAndFeel();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private static void initLookAndFeel() {
        SynthLookAndFeel lookAndFeel = new SynthLookAndFeel();
        try {
        	
         UIManager.setLookAndFeel( new com.nilo.plaf.nimrod.NimRODLookAndFeel());
         NimRODTheme nt = new NimRODTheme( "NimRODThemeFile.theme");
         System.out.println(System.getProperty("user.dir"));
       	 NimRODLookAndFeel nf = new NimRODLookAndFeel();
        	 nf.setCurrentTheme(nt);
        	 UIManager.setLookAndFeel( nf);
        }

        catch (Exception e) {
            System.err.println("Couldn't get specified look and feel ("
                    + lookAndFeel
                    + "), for some reason.");
            System.err.println("Using the default look and feel.");
            e.printStackTrace();
        }

    }

    public void appDidLaunch(String[] args) {
        XJApplication.setPropertiesPath("edu/usfca/vas/properties/");
        XJApplication.addDocumentType(Document.class, WindowFA.class, DataFA.class, "fa", Localized.getString("documentFADescription"));
//        XJApplication.addDocumentType(Document.class, WindowTM.class, DataTM.class, "tm", Localized.getString("documentTMDescription"));

        
        switch (Preferences.getStartupAction()) {
            case Preferences.STARTUP_DO_NOTHING:
                // Do nothing only on Mac OS (because the menu bar is always visible)
                if(XJSystem.isMacOS())
                    break;
            case Preferences.STARTUP_NEW_FA_DOC:
                XJApplication.shared().newDocumentOfData(DataFA.class);
                break;
//            case Preferences.STARTUP_NEW_TM_DOC:
//                XJApplication.shared().newDocumentOfData(DataTM.class);
//                break;
            case Preferences.STARTUP_OPEN_LAST_DOC:
                if (!XJApplication.shared().openLastUsedDocument())
                    XJApplication.shared().newDocument();
                break;
        }

        /*
         * Should be in another place.
         * 
        if (XJSystem.isMacOS()) {
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "HIT");
			Frame frame = (Frame)XJApplication.getActiveContainer();
			enableFullScreenMode(frame);
		}*/
        
        //System.setProperty("apple.laf.useScreenMenuBar", "true");
        
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                checkForUpdates(true);
            }
        });
    }

    // enabling FullScreen mode on Mac OS X
    public static void enableFullScreenMode(Window window) {
        String className = "com.apple.eawt.FullScreenUtilities";
        String methodName = "setWindowCanFullScreen";
 
        try {
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getMethod(methodName, new Class<?>[] {
            		Window.class, boolean.class });
            method.invoke(null, window, true);
        } catch (Throwable t) {
            System.err.println("Full screen mode is not supported");
            t.printStackTrace();
        }
    }
    
    public static void checkForUpdates(boolean automatic) {
        String url;
        if(XJSystem.isMacOS())
            url = Localized.getString("UpdateOSXXMLURL");
        else
            url = Localized.getString("UpdateXMLURL");
        
        XJUpdateManager um = new XJUpdateManager(null, null);
        um.checkForUpdates(Localized.getString("AppVersionShort"),
                           url,
                           System.getProperty("user.home"),
                           automatic);
    }

}
