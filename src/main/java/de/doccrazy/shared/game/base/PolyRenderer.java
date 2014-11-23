package de.doccrazy.shared.game.base;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape.Type;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.utils.ShortArray;

import java.util.List;

public class PolyRenderer {
	private final static Vector2[] vertices = new Vector2[1000];
	private final static Vector2[] verticesRaw = new Vector2[1000];
	private final static short[] indices = new short[1000];
	private final static float[] vertTmp = new float[5000];
	private final static float[] vertParts = new float[5000];

	private final static PolygonSpriteBatch shapeRenderer = new PolygonSpriteBatch();
	private final static EarClippingTriangulator triangulator = new EarClippingTriangulator();

	public static void drawBodies(List<Body> bodies, Matrix4 projMatrix, Texture texture) {
        shapeRenderer.setProjectionMatrix(projMatrix);
        shapeRenderer.begin();
 		for (Body body : bodies) {
			renderBody(body, texture);
		}
        shapeRenderer.end();
	}

	public static void renderBody(Body body, Texture texture) {
		Transform transform = body.getTransform();
		for (Fixture fixture : body.getFixtureList()) {
			if (fixture.isSensor()) {
				continue;
			}
			drawShape(fixture, transform, texture);
		}
	}

	private static void drawShape(Fixture fixture, Transform transform, Texture tex) {
		Vector2 rnd = new Vector2((float)Math.random(), (float)Math.random());
		if (fixture.getType() == Type.Polygon) {
			PolygonShape chain = (PolygonShape)fixture.getShape();
			int vertexCount = chain.getVertexCount();
			for (int i = 0; i < vertexCount; i++) {
				chain.getVertex(i, vertices[i]);
				chain.getVertex(i, verticesRaw[i]);
				transform.mul(vertices[i]);
			}
			drawSolidPolygon(vertices, verticesRaw, vertexCount, tex, rnd);
			shapeRenderer.flush();
			return;
		}
	}

	private static void drawSolidPolygon (Vector2[] vertices, Vector2[] verticesRaw, int vertexCount, Texture tex, Vector2 texRnd) {
		for (int i = 0; i < vertexCount; i++) {
			Vector2 v = vertices[i];
			Vector2 raw = verticesRaw[i];
			vertParts[i*5] = v.x;
			vertParts[i*5+1] = v.y;
			vertParts[i*5+2] = new Color(1,1,1,1).toFloatBits();
			vertParts[i*5+3] = texRnd.x + raw.x/4f;
			vertParts[i*5+4] = texRnd.y + raw.y/4f;

			vertTmp[i*2] = v.x;
			vertTmp[i*2+1] = v.y;
			indices[i] = (short) i;
		}

		ShortArray idx = triangulator.computeTriangles(vertTmp, 0, vertexCount*2);
		shapeRenderer.draw(tex, vertParts, 0, vertexCount*5,
				idx.items, 0, Math.max(0, vertexCount - 2) * 3);
	}

	static {
		for (int i = 0; i < vertices.length; i++)
			vertices[i] = new Vector2();
		for (int i = 0; i < verticesRaw.length; i++)
			verticesRaw[i] = new Vector2();
	}
}
