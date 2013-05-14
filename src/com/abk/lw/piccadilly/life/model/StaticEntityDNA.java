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

import java.util.Random;

/**
 * 0 - 19: movement
 * 20 - 22: color
 * 23: rest cycles
 * 24: radius
 * @author kgilmer
 *
 */
public class StaticEntityDNA {
    private static final Random RND = new Random();
    private static final float RADIUS_FACTOR = 100;
    private float radius;
    private final int[] dna;
    
    public StaticEntityDNA(int[] dna) {
        this.dna = dna;
       radius = dna[0] / RADIUS_FACTOR;        
    }
           
    /**
     * @param movement
     * @param color
     * @param restCycles
     * @param radius
     * @param dna
     */
    public StaticEntityDNA(float radius) {
        super();
        this.radius = radius;
        this.dna = new int[1];
        
        dna[0] = (int) (radius * RADIUS_FACTOR);
    }

    /**
     * @return the radius
     */
    public float getRadius() {
        return radius;
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
}
