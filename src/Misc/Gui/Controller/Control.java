
package Misc.Gui.Controller;

import ML.featureDetection.NormalizeBeat;
import Misc.Gui.Actions.*;
import Misc.Gui.Main.ExtractFeatures;
import javax.swing.JTable;

public class Control
{

    public FilesTableModel filesList;
    public ExtractFeatures exfeat;
//    public ExitAction exitAction;
    public ViewFileInfoAction viewFileInfoAction;
    public AddRecordingAction addRecordingsAction;
//    public AboutAction aboutAction;
    public NormalizeBeat norm;
    JTable recordTable ;

    public Control()
        throws Exception
    {
        /*
        exitAction = new ExitAction(); */
        addRecordingsAction = new AddRecordingAction();
/*        aboutAction = new AboutAction();
        */
        filesList = new FilesTableModel(new Object[] {
            "Name", "Path"
        }, 0, 60);
        addRecordingsAction.setModel(this);
        viewFileInfoAction = new ViewFileInfoAction(this, recordTable);
    }
}
