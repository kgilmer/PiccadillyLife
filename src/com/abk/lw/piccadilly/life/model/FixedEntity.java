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

import java.util.List;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

/**
 * Food
 * 
 * @author kgilmer
 *
 */
public class FixedEntity implements ISimEntity {
    private static final float ENERGY_INC = .1f;
    private Body body;
    private float energy;
    private final World world;
    private final StaticEntityDNA dna;
    
    /**
     * @param body
     * @param dna
     */
    public FixedEntity(List<ISimEntity> entities, World world, float x, float y, StaticEntityDNA dna, float initialEnergy) {
        super();
        this.world = world;
        this.dna = dna;
        this.body = createFoodBody(x, y, dna.getRadius());
        this.energy = initialEnergy;
        this.body.m_userData = this;
    }
    
    private void decrementEnergy(float c) {
        energy -= c;
    }
    
    /**
     * @return the energy
     */
    @Override
    public float getEnergy() {
        return energy;
    }
    
    @Override
    public boolean isAlive() {
        return energy > 0f;
    }
    
    /**
     * @return the body
     */
    public Body getBody() {
        return body;
    }
    
    private Body createFoodBody(float x, float y, float radius) {
        BodyDef def = new BodyDef();
        def.type = BodyType.STATIC;
        def.position.set(x, y);
        Body body = world.createBody(def);
        CircleShape shape = new CircleShape();
        shape.m_radius = radius;
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        body.createFixture(fixtureDef);
        return body;
    }

    /* (non-Javadoc)
     * @see com.abk.lw.piccadilly.life.model.ISimEntity#timeStep()
     */
    @Override
    public void timeStep() {
        energy += (ENERGY_INC * body.getFixtureList().getShape().m_radius);
    }

    /* (non-Javadoc)
     * @see com.abk.lw.piccadilly.life.model.ISimEntity#isStatic()
     */
    @Override
    public boolean isStatic() {
        return true;
    }

    /* (non-Javadoc)
     * @see com.abk.lw.piccadilly.life.model.ISimEntity#collision(com.abk.lw.piccadilly.life.model.ISimEntity)
     */
    @Override
    public void collision(ISimEntity other) {
        decrementEnergy(other.getBody().getMass() * ISimEntity.TRANSFER_FACTOR);
    }

    /* (non-Javadoc)
     * @see com.abk.lw.piccadilly.life.model.ISimEntity#getDNA()
     */
    @Override
    public Object getDNA() {
        
        return dna;
    }
}
