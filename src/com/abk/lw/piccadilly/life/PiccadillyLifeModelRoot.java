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
package com.abk.lw.piccadilly.life;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jbox2d.callbacks.ContactFilter;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;
import com.abk.lw.piccadilly.life.model.FixedEntity;
import com.abk.lw.piccadilly.life.model.ISimEntity;
import com.abk.lw.piccadilly.life.model.MovingEntity;
import com.abk.lw.piccadilly.life.model.MovingEntityDNA;
import com.abk.lw.piccadilly.life.model.StaticEntityDNA;

public class PiccadillyLifeModelRoot {
    private final static Random RND = new Random();

	private static final String TAG = PiccadillyLifeModelRoot.class.getSimpleName();

	private World world;
	private Body worldEdge;
	private List<ISimEntity> simEntities = new CopyOnWriteArrayList<ISimEntity>();
	private List<ISimEntity> babyEntities = new ArrayList<ISimEntity>();
	private List<ISimEntity> deadEntities = new ArrayList<ISimEntity>();
	
	private long timeAccumulator;
	
	private static final long stepInMillis = 20;
	private static final float stepInSeconds = stepInMillis / 1000.0f;
	private static final int velocityIterations = 10;
	private static final int positionIterations = 5;

    private static final float WORLD_SIZE_X = 8.0f;
    private static final float WORLD_SIZE_Y = 4.0f;

    private static final float INITIAL_MOVING_ENTITY_ENERGY = 30f;

    private static final float INITIAL_STATIC_ENTITY_ENERGY = 100F;

    private static final int DEFAULT_STATIC_BODIES = 20;
	
	private List<MouseJoint> userActions = new ArrayList<MouseJoint>();
	
	public PiccadillyLifeModelRoot() {
		initializeWorld();
	}
	
	private void initializeWorld() {
	    //Define basic physical constants
		Vec2 gravity = new Vec2(0.0f, 0.0f);
		boolean doSleep = true;
		world = new World(gravity, doSleep);
		
		//Create the borders of the world
		BodyDef groundBodyDef = new BodyDef();
		worldEdge = world.createBody(groundBodyDef);
		PolygonShape edgeShape = new PolygonShape();
		edgeShape.setAsEdge(new Vec2(-WORLD_SIZE_X, -WORLD_SIZE_Y), new Vec2(WORLD_SIZE_X, -WORLD_SIZE_Y));
		worldEdge.createFixture(edgeShape, 1.0f);
		edgeShape.setAsEdge(new Vec2(-WORLD_SIZE_X, -WORLD_SIZE_Y), new Vec2(-WORLD_SIZE_X, WORLD_SIZE_Y));
		worldEdge.createFixture(edgeShape, 1.0f);
		edgeShape.setAsEdge(new Vec2(WORLD_SIZE_X, -WORLD_SIZE_Y), new Vec2(WORLD_SIZE_X, WORLD_SIZE_Y));
		worldEdge.createFixture(edgeShape, 1.0f);
		edgeShape.setAsEdge(new Vec2(-WORLD_SIZE_X, WORLD_SIZE_Y), new Vec2(WORLD_SIZE_X, WORLD_SIZE_Y));
		worldEdge.createFixture(edgeShape, 1.0f);
		
		Point center = new Point(0, 0);
		PointF size = new PointF(4f, 4f);
		
		//Create dynamic entities
		simEntities.addAll(generateRandomCircleBodies(20, .3f, false, center, size));
		
		//Create the static entities (food)
		size = new PointF(4f, 6f);
		center = new Point(6, 0);
		simEntities.addAll(generateRandomCircleBodies(DEFAULT_STATIC_BODIES / 2, .3f, true, center, size));
		center = new Point(-6, 0);
		simEntities.addAll(generateRandomCircleBodies(DEFAULT_STATIC_BODIES / 2, .3f, true, center, size));
		
		//Handle collisions between bodies
		world.setContactListener(new ContactListener() {
            
            @Override
            public void preSolve(Contact contact, Manifold arg1) {
            }
            
            @Override
            public void postSolve(Contact contact, ContactImpulse arg1) {
            }
            
            @Override
            public void endContact(Contact contact) {
            }
            
            @Override
            public void beginContact(Contact contact) {
                ISimEntity se1 = (ISimEntity) contact.getFixtureA().getBody().getUserData();
                ISimEntity se2 = (ISimEntity) contact.getFixtureB().getBody().getUserData();
                
                if (se1 == null || se2 == null)
                    return;
                
                if (se1.isStatic() && se2.isStatic())
                    return;

                se1.collision(se2);
                se2.collision(se1);
                
            }
        });	
		
		world.setContactFilter(new ContactFilter() {
		    /* (non-Javadoc)
		     * @see org.jbox2d.callbacks.ContactFilter#shouldCollide(org.jbox2d.dynamics.Fixture, org.jbox2d.dynamics.Fixture)
		     */
		    @Override
		    public boolean shouldCollide(Fixture fixtureA, Fixture fixtureB) {
		        return super.shouldCollide(fixtureA, fixtureB);
		    }
		});
	}
	
	private List<ISimEntity> generateRandomCircleBodies(int bodyCount, float maxRadius, boolean isFood, Point center, PointF size) {
	    List<ISimEntity> bl = new ArrayList<ISimEntity>();
	    
	    for (int i = 0; i < bodyCount; ++i) {
	        float x = center.x + (size.x * (RND.nextFloat() - .5f));
	        float y = center.y + (size.y * (RND.nextFloat() - .5f));
	        float s = RND.nextFloat() * maxRadius;
	        int cl = Color.rgb(RND.nextInt(256), RND.nextInt(256), RND.nextInt(256));
	        
	        if (isFood)
	            bl.add(new FixedEntity(babyEntities, world, x, y, new StaticEntityDNA(s), INITIAL_STATIC_ENTITY_ENERGY));
	        else
	            bl.add(new MovingEntity(babyEntities, world, x, y, new MovingEntityDNA(MovingEntityDNA.generateRandomGenes(20), cl, RND.nextInt(20) + 20, s, RND.nextInt(241) + 15, RND.nextInt(256)), INITIAL_MOVING_ENTITY_ENERGY));
	    }
	    
	    return bl;
	}
	
	public void update(long dt) {
		timeAccumulator += dt;
		
		for (ISimEntity lu : simEntities) {
		    if (lu.isAlive()) {
		        lu.timeStep();
		    } else {
		        world.destroyBody(lu.getBody());
		        deadEntities.add(lu);
		    }
        }
		
		for (ISimEntity e : deadEntities) 
		    simEntities.remove(e);
		
		deadEntities.clear();
		
		while (timeAccumulator >= stepInMillis) {
			world.step(stepInSeconds, velocityIterations, positionIterations);
			timeAccumulator -= stepInMillis;
		}
		
		for (ISimEntity e : babyEntities) {
		    if (e instanceof MovingEntity) {
		        MovingEntityDNA dna = (MovingEntityDNA) e.getDNA();
		        MovingEntityDNA newDNA = dna.copy();
	        
		        simEntities.add(new MovingEntity(babyEntities, world, e.getBody().getPosition().x, e.getBody().getPosition().y, newDNA, e.getEnergy() / 2));
		    }
		}
		babyEntities.clear();
		
		if (getStaticEntitiesCount(simEntities) < DEFAULT_STATIC_BODIES / 2) {
		    PointF size = new PointF(4f, 6f);
	        Point center = new Point(RND.nextInt(12) - 6, 0);
	        simEntities.addAll(generateRandomCircleBodies(DEFAULT_STATIC_BODIES / 2, .3f, true, center, size));
		}
	}

	/**
     * @param simEntities2
     * @return
     */
    private int getStaticEntitiesCount(List<ISimEntity> simEntities2) {
        int c = 0;
        for (ISimEntity e : simEntities2)
            if (e.isStatic())
                c++;
        
        return c;
    }

    public Body getBodyList() {
		return world.getBodyList();
	}
	
	public List<ISimEntity> getEntites() {
	    return simEntities;
	}

	public void userActionStart(int pointerId, final float x, final float y) {
		final List<Fixture> fixtures = new ArrayList<Fixture>();
		final Vec2 vec = new Vec2(x, y);
		world.queryAABB(new QueryCallback() {
			public boolean reportFixture(Fixture fixture) {
				//Log.i(TAG, "reportFixture: " + fixture);
				fixtures.add(fixture);
				return true;
			}
		}, new AABB(vec, vec));
		if (fixtures.size() > 0) {
			Fixture fixture = fixtures.get(0);
			//Log.i(TAG, "creating mouse joint: " + fixture);
			Body body = fixture.getBody();
			
			MouseJointDef def = new MouseJointDef();
			def.bodyA = body;
			def.bodyB = body;
			def.maxForce = 1000.0f * body.getMass();
			def.target.set(x, y);
			
			MouseJoint joint = (MouseJoint) world.createJoint(def);
			
			if (joint != null) {
        		joint.m_userData = pointerId;
        		userActions.add(joint);
			}
		} 
	}

	public void userActionUpdate(int pointerId, float x, float y) {
		for (MouseJoint joint : userActions) {
			if (pointerId == (Integer) joint.m_userData) {
				joint.setTarget(new Vec2(x, y));
				break;
			}
		}
	}

	public void userActionEnd(int pointerId, float x, float y) {
		for (MouseJoint joint : userActions) {
			if (pointerId == (Integer) joint.m_userData) {
				world.destroyJoint(joint);
				userActions.remove(joint);
				break;
			}
		}
	}
	
	public void worldForce(float x, float y) {
	    for (ISimEntity lu : simEntities) {
	        if (!lu.isStatic()) {
    	        Body b = lu.getBody();
    	        b.applyForce(new Vec2(x  * b.getMass(), y * b.getMass()), b.getPosition());
	        }
	    }
	}
}
