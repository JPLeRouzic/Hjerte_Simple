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
public class Event {
    private int startTimeStamp ;
    private int endTimeStamp ;
    int tooEarly ;
    int tooLate ;
    
    public Event(int strttsp, int endtsp, float trshld, int early, int late ) {
        startTimeStamp = strttsp ;
        endTimeStamp = endtsp ;
        tooEarly = early ;
        tooLate = late ;
    }
    
    public int timeStampBeg() {
        return startTimeStamp ;
    }
    
    public int timeStampEnd() {
        return endTimeStamp ;
    }
}
