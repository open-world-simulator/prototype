package com.openworldsimulator.tools;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.Random;

public class RandomTools {
    private static Random random = new Random();

    public static boolean testUniformDist(double threshold) {
        return random.nextDouble() <= threshold;
    }

    public static boolean testNormalDist(double x1, double x2, double mean, double stddev, double totalP) {
        NormalDistribution normalDistribution = new NormalDistribution(mean, stddev);
        double prob = normalDistribution.probability(x1, x2);
        if( testUniformDist(prob * totalP) ) {
            return  true;
        } else {
            return false;
        }
    }


    public static double random() {
        return random.nextDouble();
    }

    public static int random(int nOptions) {
        return random.nextInt(nOptions);
    }

    public static double random(double mean, double stddev) {
        return random.nextGaussian() * stddev + mean;
    }

    public static double random(double p, double mean, double stddev) {
        return (random.nextGaussian() * stddev + mean) * p;
    }

    public static double random(double mean, double stddev, double min, double max, String desc ) {
        double r;

        if( min == -1 ) min = Double.MIN_VALUE;
        if( max == -1 ) max = Double.MAX_VALUE;

        int n = 0;
        do {
            r = random.nextGaussian() * stddev + mean;
            n++;
            if( n > 10 ) {
                System.out.println("Random for '" + desc + "' - please, check : Min: " + min + " Max: " + max + " Mean: " + mean + " - Random: " + r);
            }
        } while (r < min || r > max );
        return r;
    }

    public static double randomInt(double mean, double stddev) {
        return (int) random.nextGaussian() * stddev + mean;
    }
}


