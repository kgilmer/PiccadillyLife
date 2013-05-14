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
 * Instructions for dynamic entities.
 * 
 * @author kgilmer
 *
 */
public enum MovementInstructions {

    REST,
    MOVE_N,
    MOVE_S,
    MOVE_E,
    MOVE_W;
    
    private static final Random RND = new Random();
    
    /**
     * @param gene
     * @return
     */
    public static MovementInstructions resolveMovementGene(int gene) {

        if (gene < (51 * 1))
            return MovementInstructions.REST;

        if (gene < (51 * 2))
            return MovementInstructions.MOVE_E;

        if (gene < (51 * 3))
            return MovementInstructions.MOVE_N;

        if (gene < (51 * 4))
            return MovementInstructions.MOVE_S;

        return MovementInstructions.MOVE_W;
    }
    
    /**
     * @param length
     * @return sequence of random genes of length 'length'.
     */
    public static MovementInstructions[] createRandomGenes(int length) {
        MovementInstructions genes[] = new MovementInstructions[length];

        int gl = MovementInstructions.values().length;

        for (int i = 0; i < genes.length; ++i) {
            genes[i] = MovementInstructions.values()[RND.nextInt(gl)];
        }

        return genes;
    }
}
