package de.doccrazy.shared.spriter;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.brashmonkey.spriter.Drawer;
import com.brashmonkey.spriter.Loader;
import com.brashmonkey.spriter.Timeline;

public class GdxDrawer extends Drawer<Sprite> {
    private ShapeRenderer renderer;
    private Batch batch;
    private Color color;

    public GdxDrawer(Loader<Sprite> loader) {
        super(loader);
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        renderer.setColor(r, g, b, a);
    }

    @Override
    public void rectangle(float x, float y, float width, float height) {
        renderer.rect(x, y, width, height);
    }

    @Override
    public void line(float x1, float y1, float x2, float y2) {
        renderer.line(x1, y1, x2, y2);
    }

    @Override
    public void circle(float x, float y, float radius) {
        renderer.circle(x, y, radius);
    }

    @Override
    public void draw(Timeline.Key.Object object) {
        Sprite sprite = loader.get(object.ref);
        float newPivotX = (sprite.getWidth() * object.pivot.x);
        float newX = object.position.x - newPivotX;
        float newPivotY = (sprite.getHeight() * object.pivot.y);
        float newY = object.position.y - newPivotY;

        sprite.setX(newX);
        sprite.setY(newY);

        sprite.setOrigin(newPivotX, newPivotY);
        sprite.setRotation(object.angle);

        if (this.color !=  null) {
            sprite.setColor(this.color.r, this.color.g, this.color.b, this.color.a * object.alpha);
        } else {
            sprite.setColor(1f, 1f, 1f, object.alpha);
        }
        sprite.setScale(object.scale.x, object.scale.y);
        sprite.draw(batch);
    }

    public GdxDrawer withBatch(Batch batch) {
        this.batch = batch;
        this.color = null;
        return this;
    }

    public GdxDrawer color(Color color) {
        this.color = color;
        return this;
    }
}
