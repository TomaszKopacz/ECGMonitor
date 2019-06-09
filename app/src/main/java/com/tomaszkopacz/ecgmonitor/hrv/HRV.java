package com.tomaszkopacz.ecgmonitor.hrv;

import java.util.ArrayList;
import java.util.List;

public class HRV {

    double[] timeArray = new double[1000];

    private static final double MIN_RR = 0.27;

    private static double countAverage(double[] values){
        double avg = 0;
        for (double value : values)
            avg += value;
        avg = avg/values.length;

        return avg;
    }

    private static double countVariance(double[] values){
        double avg = countAverage(values);
        double factor;
        double sum = 0;

        for (double value : values) {
            factor = Math.pow(value - avg, 2);
            sum += factor;
        }

        return sum/values.length;
    }

    private static double countStandardDeviation(double[] values){
        double var = countVariance(values);
        return Math.sqrt(var);
    }


    public static double[] countRR(double[] time, double[] diff){

        double deviation = countStandardDeviation(diff);

        double rrStart = -1;
        double rrEnd = -1;
        boolean peakFound = false;
        List<Double> RRs = new ArrayList<>();
        List<Double> peaks = new ArrayList<>();

        for (int i = 0; i < diff.length; i++){

            double maxValue = -128;
            while (diff[i] > 10*deviation){
                peakFound = true;

                if (diff[i] > maxValue) {
                    maxValue = diff[i];
                    rrEnd = time[i];
                }

                i++;
                if (i == diff.length-1)
                    break;
            }

            if (peakFound && rrStart != -1) {
                double rr = rrEnd - rrStart;

                if (rr > MIN_RR){
                    if (peaks.size() == 0){
                        RRs.add(rrEnd - rrStart);
                        peaks.add(maxValue);
                        rrStart = rrEnd;


                    } else if (peaks.size() > 0 && maxValue > 0.3 * peaks.get(peaks.size() - 1)){
                        RRs.add(rrEnd - rrStart);
                        peaks.add(maxValue);
                        rrStart = rrEnd;
                    }
                }
            }

            if (rrStart == -1)
                rrStart = rrEnd;

            peakFound = false;
        }

        double[] result = new double[RRs.size()];
        for (int i = 0; i < RRs.size(); i++)
            result[i] = RRs.get(i);

        return result;
    }
}
