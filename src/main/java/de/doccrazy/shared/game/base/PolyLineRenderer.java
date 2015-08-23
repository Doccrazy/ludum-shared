package de.doccrazy.shared.game.base;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ShortArray;

public class PolyLineRenderer {
	private final static Vector2[] vertices = new Vector2[1000];
	private final static Vector2[] verticesTex = new Vector2[1000];
	private final static float[] vertColors = new float[1000];
	private final static float[] vertTmp = new float[5000];
	private final static float[] vertParts = new float[5000];

	private final static PolygonSpriteBatch shapeRenderer = new PolygonSpriteBatch();
	private final static EarClippingTriangulator triangulator = new EarClippingTriangulator();

	public static void drawLine(List<Vector2> points, float width, Matrix4 projMatrix, Texture texture) {
	    drawLine(points, width, projMatrix, texture, null);
	}

	public static void drawLine(List<Vector2> points, float width, Matrix4 projMatrix, Texture texture, float[] colors) {
        shapeRenderer.setProjectionMatrix(projMatrix);
        shapeRenderer.begin();
        drawSegments(points, width/2f, texture, texture.getHeight() / (float)texture.getWidth(), colors);
        shapeRenderer.end();
	}

	private static void drawSegments(List<Vector2> points, float halfWidth, Texture tex, float texStretch, float[] colors) {
	    float colorWhite = new Color(1,1,1,1).toFloatBits();
	    int vertexCount = points.size()*2;
	    Vector2 rot;
	    float len = 0;
	    for (int i = 1; i < points.size() - 1; i++) {
	        Vector2 p0 = points.get(i - 1);
	        Vector2 p1 = points.get(i);
	        Vector2 p2 = points.get(i + 1);
	        len += p1.dst(p0);
	        if (i == 1) {
	            rot = p1.cpy().sub(p0).nor().rotate90(1);
	            vertices[0].x = p0.x - rot.x * halfWidth;
	            vertices[0].y = p0.y - rot.y * halfWidth;
	            vertices[1].x = p0.x + rot.x * halfWidth;
	            vertices[1].y = p0.y + rot.y * halfWidth;
	            verticesTex[0].x = 0;
                verticesTex[0].y = 0;
                verticesTex[1].x = 0;
                verticesTex[1].y = 1;
                vertColors[0] = colors == null ? colorWhite : colors[0];
                vertColors[1] = colors == null ? colorWhite : colors[0];
	        }
            rot = p2.cpy().sub(p0).nor().rotate90(1);
            vertices[i + 1].x = p1.x + rot.x * halfWidth;
            vertices[i + 1].y = p1.y + rot.y * halfWidth;
            vertices[vertexCount - i].x = p1.x - rot.x * halfWidth;
            vertices[vertexCount - i].y = p1.y - rot.y * halfWidth;
            verticesTex[i + 1].x = (len / (halfWidth*2)) * texStretch;
            verticesTex[i + 1].y = 1;
            verticesTex[vertexCount - i].x = (len / (halfWidth*2)) * texStretch;
            verticesTex[vertexCount - i].y = 0;
            vertColors[i + 1] = colors == null ? colorWhite : colors[i];
            vertColors[vertexCount - i] = colors == null ? colorWhite : colors[i];
            if (i == points.size() - 2) {
                len += p2.dst(p1);
                rot = p2.cpy().sub(p1).nor().rotate90(1);
                vertices[points.size()].x = p2.x + rot.x * halfWidth;
                vertices[points.size()].y = p2.y + rot.y * halfWidth;
                vertices[points.size() + 1].x = p2.x - rot.x * halfWidth;
                vertices[points.size() + 1].y = p2.y - rot.y * halfWidth;
                verticesTex[points.size()].x = (len / (halfWidth*2)) * texStretch;
                verticesTex[points.size()].y = 1;
                verticesTex[points.size() + 1].x = (len / (halfWidth*2)) * texStretch;
                verticesTex[points.size() + 1].y = 0;
                vertColors[points.size()] = colors == null ? colorWhite : colors[points.size() - 1];
                vertColors[points.size() + 1] = colors == null ? colorWhite : colors[points.size() - 1];
            }
	    }
        drawSolidPolygon(vertices, verticesTex, vertColors, vertexCount, tex);
	}

	private static void drawSolidPolygon (Vector2[] vertices, Vector2[] verticesTex, float[] vertColors, int vertexCount, Texture tex) {
		for (int i = 0; i < vertexCount; i++) {
			Vector2 v = vertices[i];
			Vector2 vTex = verticesTex[i];
			vertParts[i*5] = v.x;
			vertParts[i*5+1] = v.y;
			vertParts[i*5+2] = vertColors[i];  //new Color(1,1,1,1).toFloatBits();
			vertParts[i*5+3] = vTex.x;
			vertParts[i*5+4] = vTex.y;

			vertTmp[i*2] = v.x;
			vertTmp[i*2+1] = v.y;
		}

		ShortArray idx = triangulator.computeTriangles(vertTmp, 0, vertexCount*2);
		shapeRenderer.draw(tex, vertParts, 0, vertexCount*5,
				idx.items, 0, Math.max(0, vertexCount - 2) * 3);
	}

	static {
		for (int i = 0; i < vertices.length; i++)
			vertices[i] = new Vector2();
		for (int i = 0; i < verticesTex.length; i++)
		    verticesTex[i] = new Vector2();
	}
}
