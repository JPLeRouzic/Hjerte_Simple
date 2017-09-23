package Misc.Gui.Main;

import ML.Classify.Classify;
import ML.Train.HMM;
import Misc.Gui.Controller.Control;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;

public class EntryPoint
{
    public static Control controller;
    public static OuterFrame outer_frame;
    public static HMM hmmTrain;
    public static HMM hmmTest;
    
    public static void main(final String[] args) throws Exception {
        controller = new Control();
        outer_frame = new OuterFrame(controller);
        outer_frame.repaint();
//        EntryPoint.outer_frame.featureSelectorPanel();
        
		// calls the TextAreaDemo constructor.
		outer_frame.mainPanel = new MainPanel(outer_frame);

		//set the text on the frame
		JFrame frm = new JFrame("Text Area Demo");
		frm.setContentPane(outer_frame.mainPanel);

		// setSize() methods is used to specify the width and height of the frame
		frm.setSize(400,200);

		// To display the Frame
		frm.setVisible(true);

		WindowListener listener = new WindowAdapter()
		{
			public void windowClosing(WindowEvent winEvt)
			{
				System.exit(0);
			}
		};  // End of WindowAdaptor() method

		// Window listener activates the windowClosing() method
		frm.addWindowListener(listener);        
    }
}
