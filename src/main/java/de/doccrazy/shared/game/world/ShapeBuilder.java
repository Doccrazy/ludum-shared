package de.doccrazy.shared.game.world;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;

public class ShapeBuilder {
	private final Shape shape;

	private ShapeBuilder(Shape shape) {
		this.shape = shape;
	}

	public static ShapeBuilder circle(float radius) {
		CircleShape s = new CircleShape();
		s.setRadius(radius);
		return new ShapeBuilder(s);
	}

	public static ShapeBuilder circle(float radius, Vector2 position) {
		CircleShape s = new CircleShape();
		s.setRadius(radius);
		s.setPosition(position);
		return new ShapeBuilder(s);
	}

	public static ShapeBuilder box(float halfWidth, float halfHeight) {
		PolygonShape s = new PolygonShape();
		s.setAsBox(halfWidth, halfHeight);
		return new ShapeBuilder(s);
	}

	public static ShapeBuilder box(float halfWidth, float halfHeight, Vector2 center, float angle) {
		PolygonShape s = new PolygonShape();
		s.setAsBox(halfWidth, halfHeight, center, angle);
		return new ShapeBuilder(s);
	}

    public static ShapeBuilder boxAbs(float width, float height) {
        PolygonShape s = new PolygonShape();
        s.setAsBox(width/2f, height/2f, new Vector2(width/2f, height/2f), 0);
        return new ShapeBuilder(s);
    }

	public static ShapeBuilder poly(Vector2[] vertices) {
		PolygonShape s = new PolygonShape();
		s.set(vertices);
		return new ShapeBuilder(s);
	}

	/**
	 * Return polygon shape with coordinates relative to vertices[0]. Vertices will be modified!
     */
	public static ShapeBuilder polyRel(Vector2[] vertices) {
		for (int i = vertices.length - 1; i >= 0; i--) {
			vertices[i].sub(vertices[0]);
		}
		return poly(vertices);
	}

	public Shape build() {
		return shape;
	}
}