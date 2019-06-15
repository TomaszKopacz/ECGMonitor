package com.tomaszkopacz.ecgmonitor.hrv;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.List;

public class HRV {

    private static final double MIN_RR = 0.27;

    public static double countAverage(double[] values){
        double avg = 0;
        for (double value : values)
            avg += value;
        avg = avg/values.length;

        return avg;
    }

    public static double countVariance(double[] values){
        double avg = countAverage(values);
        double factor;
        double sum = 0;

        for (double value : values) {
            factor = Math.pow(value - avg, 2);
            sum += factor;
        }

        return sum/values.length;
    }

    public static double countStandardDeviation(double[] values){
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

            double maxValue = -1000;
            while (diff[i] > deviation){
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

    public static double countRMSSD(double[] values){

        double sum = 0;

        for (int i = 1; i < values.length; i++){
            double difference = values[i] - values[i-1];
            double square = Math.pow(difference,2);

            sum += square;
        }

        return Math.sqrt(sum/values.length);
    }

    public static int countNN50(double[] values){

        int numOfHigherThan50ms = 0;

        for (int i = 1; i < values.length; i++){
            double difference = Math.abs(values[i] - values[i-1]);
            if (difference > 0.05)
                numOfHigherThan50ms++;
        }

        return numOfHigherThan50ms;
    }

    public static double countPNN50(double[] values){

        int numOfDifferences = values.length - 1;
        int numOfHigherThan50ms = countNN50(values);

        return ((double) numOfHigherThan50ms/numOfDifferences) * 100;
    }

    public static double countSDSD(double[] values){

        double[] diffs = new double[values.length-1];

        for (int i = 0; i < diffs.length; i++)
            diffs[i] = Math.abs(values[i+1] - values[i]);

        return countStandardDeviation(diffs);
    }

    public static DataPoint[] countPoincarePoints(double[] values){

        DataPoint[] points = new DataPoint[values.length - 1];

        for (int i = 1; i < values.length; i++){
            double x = values[i];
            double y = values[i-1];

            points[i-1] = new DataPoint(x,y);
        }

        return points;
    }
}
