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

import org.jbox2d.dynamics.Body;

/**
 * Root interface for all bodies in simulation.
 * 
 * @author kgilmer
 *
 */
public interface ISimEntity {

    /**
     * Amount of 'energy' that is transfered in a collision.
     */
    float TRANSFER_FACTOR = 100;

    /**
     * Increment time
     */
    public abstract void timeStep();

    /**
     * @return the body
     */
    public abstract Body getBody();

    /**
     * @return true if body has positive 'energy'.
     */
    boolean isAlive();
    
    /**
     * @return true if body does not move
     */
    boolean isStatic();

    /**
     * Handle collision with another body.
     * 
     * @param other 
     */
    void collision(ISimEntity other);

    /**
     * @return amount of 'energy' entity has at current time step.
     */
    float getEnergy();

    /**
     * @return set of instructions for entity.
     */
    public abstract Object getDNA();

}