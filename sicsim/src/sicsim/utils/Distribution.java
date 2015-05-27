/*
 * Copyright (c) 2008
 *  
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package sicsim.utils;

import java.util.Random;

import sicsim.config.SicsimConfig;

/**
 * Class to generate different kind of distributions.
 */
public class Distribution {
	private static Random poissonRandom = new Random(SicsimConfig.NETWORK_SEED * 2);
	private static Random expRandom = new Random(SicsimConfig.NETWORK_SEED * 3);
	private static Random paretoRandom = new Random(SicsimConfig.NETWORK_SEED * 4);
	private static Random clusterRandom = new Random(SicsimConfig.NETWORK_SEED * 5);
	private static Random triRandom = new Random(SicsimConfig.NETWORK_SEED * 6);
	private static Random normalRandom = new Random(SicsimConfig.NETWORK_SEED * 7);
	private static Random sripanidkulachiRandom = new Random(SicsimConfig.BANDWIDTH_SEED);
	private static Random uniformRandom = new Random(SicsimConfig.LATENCY_SEED);
	
//----------------------------------------------------------------------------------
	/**
	 * Generates poisson random number with parameter 'lambda'.
	 * @param lambda Specifies the parameter of distribution.
	 * @return Integer number generated by poisson distribution.
 	 */
	public static int poisson(double lambda) {
		double L = 1 / Math.exp(lambda);
		int k = 0;
		double p = 1;
		double u;
		
	    do {
	         k = k + 1;
	         u = Distribution.poissonRandom.nextDouble();
	         p = p * u;
	    } while (p >= L);

	    return k - 1;
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Generates normal random number with mean 'mean' and standard deviation 'var'.
	 * @param mean Specifies the mean value in normal distribution.
	 * @param var Specifies the standard deviation value in normal distribution.
	 * @param seed Specifies the seed for generating normal random numbers.
	 * @return Integer number generated by normal distribution.
 	 */
	public static int normal(double mean, double var, int seed) {
		double a = (new Random(seed)).nextGaussian();
		int result = (int)(Math.round((var * a) + mean));
		
		if (result < 0)
			result = 0;
		
		return result;
		
	}

//----------------------------------------------------------------------------------
	/**
	 * Generates normal random number with mean 'mean' and standard deviation 'var'.
	 * @param mean Specifies the mean value in normal distribution.
	 * @param var Specifies the standard deviation value in normal distribution.
	 * @return Integer number generated by normal distribution.
 	 */
	public static int normal(double mean, double var) {
		double a = Distribution.normalRandom.nextGaussian();
		int result = (int)(Math.round((var * a) + mean));
		
		if (result < 0)
			result = 0;
		
		return result;
		
	}

//----------------------------------------------------------------------------------
	/**
	 * Generates uniform random number between 0 and 'value'.
	 * @param value Specifies the bound on the random number to be returned. It must be positive.
	 * @return Integer number generated uniformly between 0 and value.
 	 */
	public static int uniform(int value) {
		return Distribution.uniformRandom.nextInt(value);
	}

//----------------------------------------------------------------------------------
	/**
	 * Generates exponential random number with mean 'mean'.
	 * @param mean Specifies the mean value in exponential value. It is 1 over 'lambda' the parameter of exponential distribution.
	 * @return Integer number generated by exponential distribution.
 	 */
	public static int exp(double mean) {
		return (int)(-mean * Math.log(1 - Distribution.expRandom.nextDouble()));
	}
	
//----------------------------------------------------------------------------------
	/**
	 * Generates triangular random number with parameters 'a', 'b' and 'c'.
	 * @param a Specifies the lower limit on distribution parameter.
	 * @param b Specifies the mode on distribution parameter.
	 * @param c Specifies the upper limit on distribution parameter.
	 * @return Integer number generated by triangular distribution.
 	 */
	public static int traiangular(double a, double b, double c) {
		int x;
		float r = Distribution.triRandom.nextFloat();
		
		if (r <= (b - a) / (c - a)) {
			x = (int)(a + Math.sqrt(r * (c - a) * (b - a)));
		} else {
			x = (int)(c - Math.sqrt((1 - r) * (c - a) * (c - b)));
		}
		
		return x;
	}
//----------------------------------------------------------------------------------
    public static int sripanidkulachi() {
        double randomNumber = sripanidkulachiRandom.nextDouble() * 100;
        int result = -1;
        int bitRate = 250;
        
        while (result < 0) {
            if (randomNumber <= 49.3)
                result = 0 + sripanidkulachiRandom.nextInt(bitRate);
            else if (randomNumber <= 68)
                result = 1 * bitRate + sripanidkulachiRandom.nextInt(bitRate);
            else if (randomNumber <= 76.4)
                result = 2 * bitRate + sripanidkulachiRandom.nextInt(bitRate);
            else if (randomNumber <= 81.6)
                result = (3 + sripanidkulachiRandom.nextInt(16)) * bitRate + sripanidkulachiRandom.nextInt(bitRate);
            else if (randomNumber <= 88.4)
                result = 20 * bitRate;
            else
                randomNumber = ((100 - randomNumber) * 100) / 11.6; //using the same distribution again
        }
        
        return result;
    }

//----------------------------------------------------------------------------------
    public static int pareto(long xm, double k) {
        double uniformRandomNumber = paretoRandom.nextDouble();
        double paretoRandomNumber =  xm / (Math.pow(uniformRandomNumber, (1 / k)));
        return (int)paretoRandomNumber;
    }

//----------------------------------------------------------------------------------
	/**
	 * Generates a skewed distribution that contains 'm' clusters, and 'd' percent of generated numbers are in these clusters.
	 * @param m Specifies the number of cluster.
	 * @param d Specifies the percentage of peers in clusters.
	 * @return Integer number generated by cluster distribution.
 	 */
    public static int cluster(int m, double d) {
    	int result = 0;
        double randNum = clusterRandom.nextDouble();
        
        for (double i = 0; i < m; i++) {
        	if (randNum >= i / m && randNum < (d / m) + (i / m)) {
        		result = (int)i * 2;
        		break;
        	}

        	if (randNum >= (d / m) + (i / m) && randNum < (i + 1) / m) {
        		result = (int)i * 2 + 1;
        		break;
        	}
        }
        
        return result;
    }
}
