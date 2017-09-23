package Misc.Gui.Main;

import ML.Classify.Classify;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import Misc.Gui.HMM.LoadHMM;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public final class MainPanel extends JPanel implements ActionListener {

    static final long serialVersionUID = 1L;
    public OuterFrame outer_frame;
    JTextArea textArea;
    JButton classify_button;

    public MainPanel(final OuterFrame aThis) {
        FileReader fr = null;
        try {
            this.outer_frame = aThis;
            final Color blue = new Color(0.75f, 0.85f, 1.0f);
            final int horizontal_gap = 6;
            final int vertical_gap = 11;
            this.setLayout(new BorderLayout(horizontal_gap, vertical_gap));

            this.textArea = new JTextArea(10, 120) ;
            this.textArea.setEditable(false);
            this.textArea.setSize(new Dimension(385, 135));
            this.textArea.setLineWrap(true);
            this.textArea.setWrapStyleWord(true);
            add(this.textArea);


            this.classify_button = new JButton("Classify a new file with trained ML");
            this.classify_button.addActionListener(this);

            this.add(this.classify_button, "South");

            // load the trained HMM
            final File dest = new File("./HMM/trainedHMM_v0.3");
            fr = new FileReader(dest);
            LoadHMM.loadHMM(fr);
            try {
                fr.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
    
    public void actionPerformed(final ActionEvent event) {
        if (event.getSource().equals(outer_frame.mainPanel.classify_button)) {
            outer_frame.c.addRecordingsAction.addFile();
            if (outer_frame.c.exfeat != null) {
                Classify a = new Classify(outer_frame.c, this.outer_frame, outer_frame.c.exfeat.recordingInfo);
                float score;

//                        simi.textArea.setText("Similarity score (between 0 and 1)\n");
                final String similScore = EntryPoint.hmmTest.getSimilarity();
                score = Float.parseFloat(similScore);
                if (score <= 0.5) {
                    textArea.setText("The tested file looks quite different from the training set\n");
                } else if ((score > 0.5) && (score < 0.75)) {
                    textArea.setText("The tested file has some similarity to the training set\n");
                } else {
                    textArea.setText("The tested file has good similarity to the training set\n");
                }

                // if it has too many nasty sounds
                float scoreSx = EntryPoint.hmmTest.getSxScore();

                // We inform the user only if there are more than 10% Sx sounds (sounds other than S/S2)
                if (scoreSx > 0.1) {
                    if (scoreSx > 0.5) {
                        textArea.append("Unfortunately nearly no beat has a classical pattern\n");
                    } else // scoreSx <= 0.5
                    {
                        if (scoreSx < 0.2) {
                            // 0.2 > scoreSx >= 0.1
                            textArea.append("In addition one heart sound per " + String.valueOf(Math.round(1 / scoreSx)) + " beat(s) were not a classical S1 or S2 sound\n");
                            textArea.append("\nThis ratio is not a problem, but you should record it every week and ");
                            textArea.append("make a do a weighted average on five last values\n");
                        } else {
                            // 0.5 >= scoreSx >= 0.2
                            textArea.append("In addition one heart sound per " + String.valueOf(Math.round(1 / scoreSx)) + " beat(s) were not a classical S1 or S2 sound\n");
                            textArea.append("This ratio is too high, you should consult your doctor\n");
                        }
                    }
                }

                // We do not show a similarity score that is too low
                if ((score > 0.5) && (scoreSx < 0.1)) {
                    textArea.append("Similarity value found: " + similScore);
                } else {

                    /* Both the third heart sound (S3) and systolic time intervals (STIs) are 
                            proven noninvasive clinical indicators of heart failure. 
                            The auscultated S3 is a validated indicator of LV dysfunction, typically 
                            associated with increased LV filling pressure.
                            STIs have been correlated with measures of LV function, such as 
                            LV end-diastolic volume, stroke volume, cardiac output, and 
                            LV ejection fraction (LVEF).
                     */
                    // S3 in older people (40+) is associated with heart failure.
                    // S3 is more often heard in adults who are very lean. 
                    // S3 is heard normally in 80% of pregnant women. 
                    final float s3Score = EntryPoint.hmmTest.getS3Score();
                    if ((s3Score > 0.1) && (s3Score < 0.8)) {
                        textArea.append("\nS3 score: " + (Math.round(s3Score * 100)) + "%\n");

                        textArea.append("Too much S3 events means that the heart is not working as it should\n");
                        textArea.append("If this is detected consistently during several days \n");
                        textArea.append("It might be a good idea to consult your doctor\n\n");
                    }
//                            final float lvstScore = EntryPoint.hmmTest.getLVSTScore();
//                            simi.textArea.append("LVST score will be computed in a future version");
                }

            }
        }
        textArea.setVisible(true);
        this.repaint();
        this.outer_frame.repaint();
    }
}
