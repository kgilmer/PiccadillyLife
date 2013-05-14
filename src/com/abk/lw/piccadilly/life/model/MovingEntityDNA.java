/*
 *   Copyright 2013 Ken Gilmer
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.abk.lw.piccadilly.life.model;

import java.util.Arrays;
import java.util.Random;
import android.graphics.Color;

/**
 * 0 - 19: movement
 * 20 - 22: color
 * 23: rest cycles
 * 24: radius
 * @author kgilmer
 *
 */
public class MovingEntityDNA {
    private static final Random RND = new Random();
    private static final float RADIUS_FACTOR = 100;
    public static final int MAX_MOVEMENT_GENES = 20;
    public  static final int TOTAL_GENES = 27;
    private int[] movement;
    private int color;
    private int restCycles;
    private float radius;
    private final int[] dna;
    private final int reproductionThreshold;
    private final int reproductionType;
    private int[] otherDNA;
    
    
    public MovingEntityDNA(int[] dna) {
        this.dna = dna;
        movement = Arrays.copyOfRange(dna, 0, 20);
        color = Color.rgb(dna[20], dna[21], dna[22]);
       restCycles = dna[23];
       radius = dna[24] / RADIUS_FACTOR;   
       reproductionThreshold = dna[25];
       reproductionType = dna[26];
    }
           
    /**
     * @param movement
     * @param color
     * @param restCycles
     * @param radius
     * @param dna
     */
    public MovingEntityDNA(int[] movement, int color, int restCycles, float radius, int reproductionThreshold, int reproductionType) {
        super();
        this.movement = movement;
        this.color = color;
        this.restCycles = restCycles;
        this.radius = radius;
        this.dna = new int[TOTAL_GENES];
        this.reproductionThreshold = reproductionThreshold;
        this.reproductionType = reproductionType;
        
        for (int i = 0; i < MAX_MOVEMENT_GENES; ++i)
            dna[i] = movement[i];
        
        dna[20] = Color.red(color);
        dna[21] = Color.green(color);
        dna[22] = Color.blue(color);
        dna[23] = restCycles;
        dna[24] = (int) (radius * RADIUS_FACTOR);
        dna[25] = reproductionThreshold;
        dna[26] = reproductionType;
    }



    /**
     * @return the movement
     */
    public int[] getMovement() {
        return movement;
    }
    
    /**
     * @return the color
     */
    public int getColor() {
        return color;
    }
    
    /**
     * @return the radius
     */
    public float getRadius() {
        return radius;
    }
    
    /**
     * @return the reproductionThreshold
     */
    public int getReproductionThreshold() {
        return reproductionThreshold;
    }
    
    /**
     * @return the restCycles
     */
    public int getRestCycles() {
        return restCycles;
    }
    
    public int[] toDNA() {
        return dna;
    }
    
    public static int[] generateRandomGenes(int length) {
        int[] g = new int[length];
        
        for (int i = 0; i < length; ++i) {
            g[i] = RND.nextInt(256);
        }
        
        return g;
    }

    /**
     * @param exact
     * @return
     */
    public MovingEntityDNA copy() {
        int[] newdna = null;
        
        DNACopyType conType = getReproductionType(reproductionType);
        
        switch(conType) {
            case EXACT:
                newdna = Arrays.copyOfRange(dna, 0, dna.length);
                break;
            case SCRAMBLED:
                newdna = generateRandomGenes(TOTAL_GENES);
                break;
            case MUTATE:
                newdna = Arrays.copyOfRange(dna, 0, dna.length);
                for (int i = 0; i < RND.nextInt(TOTAL_GENES / 4); ++i) {
                    int ind = RND.nextInt(TOTAL_GENES);
                    int val = RND.nextInt(256);
                    newdna[ind] = val;
                }
                break;
            case COMBINE:
                if (otherDNA == null)
                    break;
                
                int breakPoint = RND.nextInt(TOTAL_GENES);
                newdna = new int[TOTAL_GENES];
                for (int i = 0; i < breakPoint; ++i)
                    newdna[i] = dna[i];
                for (int i = breakPoint; i < TOTAL_GENES; ++i)
                    newdna[i] = otherDNA[i];
                break;
        }
        
        MovingEntityDNA d = new MovingEntityDNA(newdna); 
        
        return d;
    }

    /**
     * @param v
     * @return
     */
    private DNACopyType getReproductionType(int v) {
        int l = DNACopyType.values().length;
        int x = 256 / DNACopyType.values().length;
        
        int c = x;
        for (int i = 0; i < l; ++i) {
            if (v < c)
                return DNACopyType.values()[i];
            
            c += x;
        }
        
        return DNACopyType.values()[l];
    }

    /**
     * Store DNA of last contact in case of reproduction.
     * @param other
     */
    public void putLastEncounter(int[] otherDNA) {
        this.otherDNA = otherDNA;
    }
}
