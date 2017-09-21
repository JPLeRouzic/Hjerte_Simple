/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ML.featureDetection;

import java.util.BitSet;
import java.util.LinkedList;

/**
 *
 * @author jplr
 */
public class FindEvents {
    
    private int eventShift;
//    private final boolean randm = false;
        float ave, max;
    
        /**
     * This is for finding S1 events, it is independant of sampling rate and
     * contains no prior knowledge about beat rate
     *
     * @param dataIn
     * @param sampling_rate
     * @param treshFnd
     * @param ave
     * @param max
     * @return
     */
    LinkedList calcBeatFind(
            FindBeats fb, 
            float[] dataIn,
            int smplRate,
            float earlyS1, float lateS1,
            float s2Shift,
            float treshFnd
    ) {
        float ave, max;
        int start = 0;
        int early = 0;
        int late = 0;
        int idxGlobal;
        LinkedList BeatS1Rate = new LinkedList();
        boolean S1Flag = false;
        float tresholdS1;
        float diff;
        float lastAverage;

        // Calculate average and maximum positive value in the sound sample
        // Results in fields aver and maxi
        
        // find this file average
        fb.averMax(dataIn);
        ave = fb.aver;
        max = fb.maxi;

        if (max > 1) {
            max = (float) 0.99;
        }

        if (ave <= 0) {
            ave = max / 3;
        }

        /**/
        tresholdS1 = (ave + (treshFnd * max)) / (treshFnd + 1);

        idxGlobal = 1;
        while (idxGlobal < dataIn.length) {
            // It applies a filter at 40Hz (see above)
            int winLength = smplRate / 40;
            lastAverage = lastWinAve(dataIn, idxGlobal, winLength);
            if ((dataIn[idxGlobal] > tresholdS1) && (S1Flag == true)) {
                if (lastAverage < tresholdS1) {
                    // We went through the tresholdS1 upward, while counting was forbidden
                    // We need to reautorize it at the next downward tresholdS1
                    //
                    // FIXME INSERT START of BEAT, complexity, energy, duration
                    //
                    start = idxGlobal;
                    S1Flag = false;
                }
            }
            if ((dataIn[idxGlobal] < tresholdS1) && (S1Flag == false) && (lastAverage > tresholdS1)) {
                // So we are in a situation when we went through the tresholdS1 when we went 
                // from [idxGlobal-1] to [idxGlobal]
                // Let's count a beat, and rise a flag to count only once downward
                int un = BeatS1Rate.size();
                if (un > 0) {
                    // At least a previous events has been recorded, we can check if this event 
                    // arrival time is early or late by calculating the duration since last event.
                    diff = idxGlobal - ((Event) BeatS1Rate.get(un - 1)).timeStampEnd();
                } else {
                    // No previous event has been recorded, so we cannot check for "too early" or "too late"
                    // We will jump over next code to add this idxGlobal to BeatS1Rate
                    diff = earlyS1 + 1;
                }

                // Is it possibly too early?
                if (diff < earlyS1) {
                    early++;
                    idxGlobal++;
                    continue;
                }
                // Is it possibly too late?
                if (diff > lateS1) {
                    late++;
                    // As we may have miss some previous beats, we lower the treshold
                    tresholdS1 = (float) ((double) tresholdS1 * 0.85D);
                    if (un > 1) {
                        idxGlobal = ((Event) BeatS1Rate.get(un - 2)).timeStampEnd();
                        continue;
                    }
                }
                Event eve = new Event(start, idxGlobal, tresholdS1, early, late);
                BeatS1Rate.add(eve);
                S1Flag = true;

                // try to move idxGlobal a bit, in a effort to thwart spikes
                idxGlobal += s2Shift;
            }
            idxGlobal++;
        }
        setShift(early, late);
        return BeatS1Rate;
    }

    /*
     * Average the past "windowLength" slots in "dataIn" before "idxGlbal"
     */
    float lastWinAve(float[] dataIn, int idxGlbal, int windowLength) {
        int cnt, idxWindow;
        int idxGlobal = idxGlbal;
        float prevWindowAve;

        if (idxGlobal < windowLength) {
            idxGlobal = windowLength;
        }

        cnt = 0;
        prevWindowAve = 0;
        idxWindow = idxGlobal - windowLength;
        while (idxWindow < idxGlobal) {
            if (idxWindow < dataIn.length) {
                if (dataIn[idxWindow] > 0) {
                    prevWindowAve += dataIn[idxWindow];
                    cnt++;
                }
                idxWindow++;
            } else {
                //             int y = 0 ;
                //            break ;
            }
        }
        if (cnt > 0) {
            return prevWindowAve / cnt;
        } else {
            return dataIn[dataIn.length - 1];
        }
    }

    /**
     * The purpose of this method is to calculate a "signature" of the beat. It
     * is a bit string, long as a beat lasts, and having only "0" or "1" values
     * It is created by saturing the signal and compressing it with RLL.
     *
     * It is supposed to be faster than FFT
     *
     * @param fb
     * @param data
     * @return
     */
    public LinkedList beatSign(FindBeats fb, float[] data) {
        // the signature is found
        BitSet binary;

        float treshold;
        
        // find this file average
        fb.averMax(data);
        ave = fb.aver;
        max = fb.maxi;

        treshold = ((2 * ave) + max) / 3;

        binary = betBitSlice(data, treshold);

        // Now do the Run Limited Length algorithm
        int j;
        LinkedList reslt = new LinkedList();

        // We start always with a count of "1" at index == "0"
        int k = 0;
        boolean a = binary.get(0);
        while ((k < binary.size()) && (a != true)) {
            a = binary.get(k);
            k++;
        }

        for (int i = 0; i < binary.length(); i++) {
            int runLength = 1;
            while (i + 1 < binary.length() && binary.get(i) == binary.get(i + 1)) {
                runLength++;
                i++;
            }

            reslt.add(new Integer(runLength));
        }
        return reslt;
    }

    private BitSet betBitSlice(float[] data, float treshold) {
        // treshold is a value between the average value and the max value
        BitSet binary = new BitSet();
        // 
        int idx = 0;
        // find this file average
        while (idx < data.length) {
            if (data[idx] > treshold) {
                binary.set(idx);
            } else {
                binary.clear(idx);
            }
            idx++;
        }
        return binary;
    }

    /**
     * If "eventShift" is positive, the sound comes early, otherwise it arrives
     * in late
     *
     * @return
     */
    public int getShift() {
        return eventShift;
    }

    public void setShift(int early, int late) {
        eventShift = early - late;
    }

    public float getTreshHold() {
        return max / ave; // FIXME is 0
    }

}
