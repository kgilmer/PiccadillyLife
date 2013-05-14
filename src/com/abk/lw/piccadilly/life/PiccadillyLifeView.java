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

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import com.abk.lw.piccadilly.life.model.ISimEntity;
import com.abk.lw.piccadilly.life.model.MovingEntity;
import com.abk.lw.piccadilly.life.model.MovingEntityDNA;

public class PiccadillyLifeView extends View {
    private static final Paint FOOD_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint LIVE_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint LINE_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint DEAD_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    static {
        FOOD_PAINT.setColor(0xFF66FFFF);
        LIVE_PAINT.setColor(0xFFFF6666);
        DEAD_PAINT.setColor(0xFF444444);
        LINE_PAINT.setColor(0xFFFFFFFF);
    }

    private PiccadillyLifeModelRoot model;

    public static final float VIEWPORT_SIZE = 16.0f; // meters

    public PiccadillyLifeView(Context context) {
        super(context);
    }

    public void setModel(PiccadillyLifeModelRoot model) {
        this.model = model;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawARGB(255, 0, 0, 0);
        canvas.translate(getWidth() / 2, getHeight() / 2);
        drawBodies(canvas);
        canvas.drawText("" + model.getEntites().size(), 40, 40, LINE_PAINT);
    }

    private void drawBodies(Canvas canvas) {
        for (ISimEntity e : model.getEntites()) {
            Body b = e.getBody();
            Fixture f = b.getFixtureList();

            while (f != null) {
                Shape s = f.getShape();
                drawShape(canvas, b.getPosition(), b.getAngle(), s, getPaintForBody(e), e);
                f = f.getNext();
            }
        }
    }

    /**
     * @param e
     * @return
     */
    private Paint getPaintForBody(ISimEntity e) {
        Paint p;

        if (e.isStatic())
            p = FOOD_PAINT;
        else if (e.isAlive())
            p = LIVE_PAINT;
        else
            p = DEAD_PAINT;

        if (p != DEAD_PAINT) {
            if (e instanceof MovingEntity)
                p.setColor(((MovingEntityDNA)e.getDNA()).getColor());
            
            int en = 4 * (int) e.getEnergy();

            if (en < 1)
                p.setAlpha(64);
            else if (en > 255)
                p.setAlpha(255);
            else
                p.setAlpha(en);
        }

        return p;
    }

    private void drawShape(Canvas canvas, Vec2 pos, float angle, Shape shape, Paint bodyPaint, ISimEntity e) {
        float scale = getWidth() / VIEWPORT_SIZE;
        pos = new Vec2(pos).mulLocal(scale);
        canvas.save();
        canvas.rotate(180.0f * angle / MathUtils.PI, pos.x, pos.y);
        if (shape.m_type == ShapeType.CIRCLE) {
            CircleShape circle = (CircleShape) shape;
            pos.addLocal(circle.m_p.mul(scale));
            float radius = circle.m_radius * scale;
            canvas.drawCircle(pos.x, pos.y, radius, bodyPaint);

            if (!e.isStatic()) {
                canvas.drawLine(pos.x, pos.y, pos.x + radius, pos.y, LINE_PAINT);
                canvas.drawText("" + e.getEnergy(), pos.x, pos.y, LINE_PAINT);
            }
        }
        canvas.restore();
    }
}
