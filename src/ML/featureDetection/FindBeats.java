/*
 * First, the algorithm applies a low-pass filter. 
 * Filtered audio data are divided into windows of samples.
 * A peak detection algorithm is applied to each window and identifies as peaks 
 * all values above a certain threshold. 
 *
 * For each peak the distance in number of samples between it and its neighbouring peaks is stored.
 * For each distance between peaks the algorithm counts how many times it has been detected. 
 * Once all windows are processed the algorithm has built a map distanceHistogram where for each 
 * possible distance its number of occurrences is stored.
 */
package ML.featureDetection;

import java.util.LinkedList;

/**
 *
 * @author jplr
 */
public class FindBeats {

    // bag of events probably including S1, S2, S3, S4 sounds
    private LinkedList moreBeatEvents = new LinkedList();

    // S1 events as found in data flow, maybe wrong because of spikes
    private LinkedList probableS1Beats = new LinkedList();

    // Normalized data that is correlated to moreBeatEvents and probableS1Beats
    private float[] normalizedData;

    // average value of sound 
    float aver;

    // max value of sound
    float maxi;

    // treshhold to obtain the beat rate, useful to qualify the quality of the file sound
    // and helping in segmenting the heart sounds
    float treshFind;

    // number of time a resample was needed
    public int resampleCount;

    // number of time a signal normalisation was needed
    public int normalizeCount;

    // Evaluate is file is noisy
    int noisyFile = 0;

    /**
     */
    public FindBeats() {
        treshFind = 0;
        resampleCount = 0;
        normalizeCount = 0;

        moreBeatEvents = new LinkedList();
        probableS1Beats = new LinkedList();
    }

    public void averMax(float[] data) {
        float absData;
        int idx = 0;
        float sum = 0;

        // find this file average
        while (idx < data.length) {
            absData = data[idx];
            if (absData < 0) {
                absData = -absData;
            }
            // in order to find average value
            sum += absData;
            // in order to find maximum value
            if (absData > maxi) {
                maxi = absData;
            }

            idx++;
        }

        // find this file average
        aver = sum / data.length;
    }

    /**
     * It calculates the beat in dataIn, this is not the same as heart beat rate
     * because the heart has several sounds inside one beat We will count
     * downward changes that occur at intensities between max value and average
     * value in order to stay away from noise A good reference is a0004.wav that
     * has 36 events and lasts 36 seconds.
     *
     * @param data
     * @param sampling_rate
     * @param beatMin
     *
     */
    public void calcBeat(float[] data, int sampling_rate, int beatMin) {
        float[] dataIn = data;
        LinkedList events = null;
        float sum = 0, max, ave;
        int idx;
        LinkedList preBeats = new LinkedList();
        LinkedList prepreBeats = new LinkedList();
        float s2Shift = (float) 0.15;
        float earlyS1, lateS1;

        FindEvents cef = new FindEvents();

        // average beat rate may be one per second.
        float nbSecInFile = ((float) dataIn.length) / ((float) sampling_rate);

        averMax(dataIn);
        ave = aver;
        max = maxi;

        // evaluating the tresholdS1        
        treshFind = 6;

        if (beatMin == 0) {
            // assume 60
            beatMin = 60;
        }

        int floor_low, floor_high, ceiling_low, ceiling_high;
        floor_low = (int) (beatMin * 0.41675);       // 25 events per minute 
        floor_high = (int) (beatMin * 0.83335);      // 50 events per minute 
        ceiling_low = (int) (beatMin * 1.33335);     // 80 events per minute 
        ceiling_high = (int) (beatMin * 2.6667);   // 160 events per minute 

        earlyS1 = ((sampling_rate * 60) / beatMin) * (float) 0.75;
        lateS1 = ((sampling_rate * 60) / beatMin) * (float) 1.25;

        while (treshFind > 0.05) {
            events = cef.calcBeatFind(this, dataIn, sampling_rate, earlyS1, lateS1, s2Shift, treshFind);

            // Number of events per minute
            int nbEvents = (int) ((events.size() * 60) / nbSecInFile);

            if ((nbEvents > (floor_low)) && (nbEvents < (ceiling_high))) {
                if ((nbEvents > (floor_high)) && (nbEvents < (ceiling_low))) {
                    normalizedData = dataIn;
                    probableS1Beats = events;
                    float tresholdS1 = (ave + (treshFind * max)) / (treshFind + 1);
                    // Find next sounds in this beat, we try to find four times the probableS1Beats
                    // for having candidates for S1, S2, S3, S4 events

                    moreBeatEvents = findNextSounds(cef, dataIn, sampling_rate, s2Shift, tresholdS1, probableS1Beats.size() * 4);
// System.out.println("findNextSounds 1");

                    // The file is noisy, record this fact
                    noisyFile++;

                    return;
                }
                // Normally we should converge, if not then return last heart rate
                int un = preBeats.size() - prepreBeats.size();
                int deux = events.size() - preBeats.size();
                if ((un < deux) && (un > 0) && (deux > 0) && (preBeats.size() > 0)) {
                    // Find next sounds in this beat
                    normalizedData = dataIn;
                    probableS1Beats = events;
                    float tresholdS1 = (ave + (treshFind * max)) / (treshFind + 1);
                    // Find next sounds in this beat, we try to find four times the probableS1Beats
                    // for having candidates for S1, S2, S3, S4 events
                    moreBeatEvents = findNextSounds(cef, dataIn, sampling_rate, s2Shift, tresholdS1, probableS1Beats.size() * 4);
// System.out.println("findNextSounds 2");
                    return;
                } else {
                    // Maybe the real heart wrong is wrong
                    if ((events.size() == preBeats.size()) && (events.size() == prepreBeats.size())) {
                        probableS1Beats = events;
                        break;
                    }
                    prepreBeats = preBeats;
                    preBeats = events;
                }
                if ((nbEvents < (floor_high)) && (probableS1Beats.size() < events.size())) {
                    probableS1Beats = events;
                } else if ((nbEvents > (ceiling_low)) && (probableS1Beats.size() > events.size())) {
                    probableS1Beats = events;
                } // troisième cas ?

                // The file is noisy, record this fact
                noisyFile++;

            } else if (events.size() > (nbSecInFile * 4)) {
                // Something wrong with dataIn, way too much events, so we filter it heavily
                Resample resp = new Resample();
                dataIn = resp.downSample(dataIn, sampling_rate, 1024);
                resampleCount++;
                continue;
            } else if (events.size() < (nbSecInFile / 3)) {
                // Something wrong with dataIn, not enough events, so we normalize dataIn
                NormalizeBeat nb = new NormalizeBeat();
                dataIn = nb.normalizeAmplitude(dataIn);
                if (normalizeCount > 1) {
                    treshFind -= 0.5;
                }
                normalizeCount++;
                continue;
            }
            // Finer grain as tresholdS1 becomes only slightly higher than average
            if (treshFind > 1) {
                treshFind -= 0.5;
            } else {
                treshFind -= 0.125;
            }
        }
        // Find next sounds in this beat
        normalizedData = dataIn;
        probableS1Beats = events;
        float tresholdS1 = (ave + (2 * max)) / (2 + 1);

        moreBeatEvents = findNextSounds(cef, dataIn, sampling_rate, s2Shift, tresholdS1, probableS1Beats.size() * 4);
// System.out.println("findNextSounds 3");

    }

    private LinkedList findNextSounds(FindEvents cef, float[] dataIn, int sampling_rate,
            float s2Shift, float tresholdS1, int neededSize) {
        LinkedList binary = new LinkedList();
        LinkedList LastBinary = new LinkedList();
        LinkedList duo = null;
        LinkedList triple = null;
        LinkedList tetra = null;

        boolean stop = false;
        while ((binary.size() < neededSize) && (stop == false)) {
            int last = binary.size();
            if (((binary.size() > (probableS1Beats.size() * 1.8)))
                    && (binary.size() < (probableS1Beats.size() * 2.2))) {
                duo = binary;
            }
            if (((binary.size() > (probableS1Beats.size() * 2.7)))
                    && (binary.size() < (probableS1Beats.size() * 3.3))) {
                triple = binary;
            }
            if (((binary.size() > (probableS1Beats.size() * 3.5)))
                    && (binary.size() < (probableS1Beats.size() * 4.5))) {
                tetra = binary;
            }
            LastBinary = binary;
            binary = cef.calcBeatFind(this, dataIn, sampling_rate, 0, sampling_rate * 4, s2Shift / 8, tresholdS1);
            tresholdS1 = (float) (tresholdS1 * 0.9);
            if (binary.size() <= last) {
                stop = true;
            }
        }

        if ((tetra != null) && ((tetra.size() / 4) != probableS1Beats.size())) {
//            System.out.println("tetra != null, beat = " + (tetra.size() / 4));
            return tetra;
        } else {
            if ((triple != null) && ((triple.size() / 3) != probableS1Beats.size())) {
//                System.out.println("triple != null, beat = " + (triple.size() / 3));
                return triple;
            } else {
                if ((duo != null) && ((duo.size() / 2) != probableS1Beats.size())) {
//                    System.out.println("duo != null, beat = " + (duo.size() / 2));
                    return duo;
                } else {
//                    System.out.println("no multiples");
                }
            }
        }

        if (LastBinary.size() == 0) {
            return binary;
        } else {
            return LastBinary;
        }
    }

    public LinkedList getMoreBeats() {
        return moreBeatEvents;
    }

    public LinkedList getProbableBeats() {
        return probableS1Beats;
    }

    public float[] getNormalizedData() {
        return normalizedData;
    }

    /**
     * A Heuristic to mitigate noise
     *
     * @return
     */
    public int getNoisyFile() {
        float div = 4 * (moreBeatEvents.size() / probableS1Beats.size());
        return (int) ((4 * noisyFile) / div);
    }

}
