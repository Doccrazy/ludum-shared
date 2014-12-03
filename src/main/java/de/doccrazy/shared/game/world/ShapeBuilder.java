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
	
	public Shape build() {
		return shape;
	}
}