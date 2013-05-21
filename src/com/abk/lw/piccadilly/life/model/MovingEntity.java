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
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

/**
 * dna ~
 * 
 * 0 - 19: movement 20 - 22: color 23: rest cycles 24: radius
 * 
 * @author kgilmer
 * 
 */
public class MovingEntity implements ISimEntity {
    private Body body;
    private static final int STEP_DELAY = 40;
    /**
     * Entities cannot reproduce before this age.
     */
    private static final int MIN_REPRODUCTION_AGE = 100;
    /**
     * Minimum amount of energy expended by any dynamic entity.
     */
    private static final float HEART_BEAT_ENERGY = .05f;

    private int instructionIndex = 0;
    private int steps = 0;
    private float energy = 0f;
    private MovingEntityDNA dna;
    private final World world;
    private final List<ISimEntity> entites;
    private int age = 0;
    

    /**
     * @param body
     * @param dna
     */
    public MovingEntity(List<ISimEntity> entites, World world, float x, float y, MovingEntityDNA dna, float initialEnergy) {
        super();
        this.entites = entites;
        this.world = world;
        this.dna = dna;
        this.energy = initialEnergy;
        this.body = createCircleBody(x, y, dna.getRadius());
        this.body.m_userData = this;
    }

    private Body createCircleBody(float x, float y, float radius) {
        BodyDef def = new BodyDef();
        def.type = BodyType.DYNAMIC;
        def.position.set(x, y);
        def.angularDamping = 0.05f;
        def.linearDamping = 0.3f;
        Body body = world.createBody(def);
        CircleShape shape = new CircleShape();
        shape.m_radius = radius;
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.restitution = 0.95f;
        body.createFixture(fixtureDef);
        return body;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.abk.lw.piccadilly.life.model.ISimEntity#timeStep()
     */
    @Override
    public void timeStep() {
        steps++;
        age ++;
        energy -= HEART_BEAT_ENERGY * body.m_mass;

        if (steps < STEP_DELAY)
            return;
        
        steps = 0;

        float fx = 0f, fy = 0f;
        float cx = (body.getMass() * 2) / 4f;

        //Get next movement instruction
        MovementInstructions gi = MovementInstructions.resolveMovementGene(dna.getMovement()[instructionIndex]);

        switch (gi)
        {
            case MOVE_E:
                fy = cx;
                break;
            case MOVE_N:
                fx = -cx;
                break;
            case MOVE_S:
                fx = cx;
                break;
            case MOVE_W:
                fy = -cx;
                break;
        }

        //Decrement 'energy' based on movement force.
        energy -= Math.abs(fx) * (body.m_mass * 10);
        energy -= Math.abs(fy) * (body.m_mass * 10);

        body.applyLinearImpulse(new Vec2(fx, fy), body.getPosition());

        instructionIndex++;
        if (instructionIndex >= MovingEntityDNA.MAX_MOVEMENT_GENES)
            instructionIndex = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.abk.lw.piccadilly.life.model.ISimEntity#getBody()
     */
    @Override
    public Body getBody() {
        return body;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.abk.lw.piccadilly.life.model.ISimEntity#getEnergy()
     */
    @Override
    public float getEnergy() {
        return energy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.abk.lw.piccadilly.life.model.ISimEntity#isAlive()
     */
    @Override
    public boolean isAlive() {
        return energy > 0f;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.abk.lw.piccadilly.life.model.ISimEntity#isStatic()
     */
    @Override
    public boolean isStatic() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.abk.lw.piccadilly.life.model.ISimEntity#collision(com.abk.lw.piccadilly.life.model.ISimEntity)
     */
    @Override
    public void collision(ISimEntity other) {
        if (other.isStatic()) {
            incEnergy(body.getMass() * ISimEntity.TRANSFER_FACTOR);
        
            if (energy > dna.getReproductionThreshold() && age > MIN_REPRODUCTION_AGE) {
                energy = energy / 2;
                age = 0;
                entites.add(this);
            }
        } else {
            dna.putLastEncounter(((MovingEntityDNA) other.getDNA()).toDNA());
        }
    }

    /**
     * @param mass
     */
    private void incEnergy(float mass) {
        energy += mass;
    }

    /* (non-Javadoc)
     * @see com.abk.lw.piccadilly.life.model.ISimEntity#getDNA()
     */
    @Override
    public Object getDNA() {
        return dna;
    }
}
