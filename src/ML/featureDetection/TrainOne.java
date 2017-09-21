/**
 * This is the general strategy that was followed by winners in Physionet 2016:
 * 
 * We first need to get comparable sounds, so sounds must be downsample
 *     (in order to get the same rate for different sounds)
 *
 * Then we need a clean sound, so we remove spikes
 *
 * Then we use this information to time normalize the sound according to the beat rate
 * in order to minimize the HMM work
 *
 * Then we will segment it to recognize the significant times like the locations for
 * S1, systole, s2 and diastole 
 * Actually there are dozens of features that recognised in Physionet 2016, but they 
 * are common to the full sound file and not specific to some area. On contrary with HMMs
 * we need to segment precisely all the areas in the sound file.
 *
 */
package ML.featureDetection;

import ML.Classify.PDefFeats;
import ML.Train.Segmentation;
import java.util.LinkedList;

public class TrainOne {

    /*
    * Used to find most important frequencies in the sound file
    */
    final int ten = 10 ;
    int freqIdx = 0 ;
    
    /**
     * Basic constructor that sets the definition and dependencies (and their
     * offsets) of this feature.
     */
    public TrainOne() {
        String name = "Train classifier n°1";
        String description = "Train the classifier on given set of files.";
    }

    /**
     * Extracts this feature from the given samples at the given sampling rate
     * and given the other feature values.
     *
     * @param samples
     * @param sampling_rate
     * @param predefFeatures
     * @param norm
     * @return
     * @throws java.lang.Exception
     */
    public LinkedList extractFeature(
            float[] samples,
            float sampling_rate,
            PDefFeats predefFeatures,
            NormalizeBeat norm
    )
            throws Exception {

        int heart_rate = 60 ; 
        // find features
        int smplingRate = (int) sampling_rate;

        /**
         * Normalize dynamics of signal
         *
         */
        float[] data_norm = norm.normalizeAmplitude(samples);

        FindBeats cb = new FindBeats();

	/*
	 * Compute a whole file signature that will be matched with pre-registered 
	*/
        // calculate beat rate
        if(predefFeatures.nbBeats.intValue() != 0)  {
            heart_rate = (predefFeatures.nbBeats.intValue() * 60) / predefFeatures.duration.intValue();
        }
        
        cb.calcBeat(data_norm, smplingRate, heart_rate);        
        System.out.println("TrainOne, probableS1Beats: " + cb.getProbableBeats().size()) ;

        /**
         *
         * Now that we have a clean sound, we will segment it to recognize the
         * significant times like the locations for S1, systole, s2 and diastole
         *
         * This is the labelled observations private LinkedList<String>
         * moreBeatsType = new LinkedList<String>(); private LinkedList<Integer>
         * moreBeatsIndx = new LinkedList<Integer>();
         *
         * Those times (S1, sys, S2, dia) will be our HMM states, and we must
         * have an observation matrix as input to the training, The result of
         * training should fill in the state transition matrix.
         */
        Segmentation segmt = new Segmentation();

        segmt.segmentation(cb, smplingRate);
        
        return segmt.getSegmentedSounds();
    }
}
