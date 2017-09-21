/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ML.featureDetection;

/**
 *
 * @author jplr
 */
public class Average {

    // max value of sound
    float maxi;
    // average value of sound
    float aver;

    /**
     * Find average in data
     *
     * @param data
     */
    void averMax(float[] data, FindBeats findBeats) {
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
    
}
