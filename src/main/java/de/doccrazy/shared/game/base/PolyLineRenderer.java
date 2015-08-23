package de.doccrazy.shared.game.base;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

public class PolyLineRenderer {
	private final static Vector2[] vertices = new Vector2[4];
	private final static Vector2[] verticesTex = new Vector2[4];
	private final static float[] vertColors = new float[4];
	private final static float[] vertParts = new float[20];

	private final static PolygonSpriteBatch shapeRenderer = new PolygonSpriteBatch();

	public static void drawLine(List<Vector2> points, float width, Matrix4 projMatrix, Texture texture) {
	    drawLine(points, width, projMatrix, texture, null);
	}

	public static void drawLine(List<Vector2> points, float width, Matrix4 projMatrix, Texture texture, float[] colors) {
        shapeRenderer.setProjectionMatrix(projMatrix);
        shapeRenderer.begin();
        if (points.size() == 2) {
            drawSingleSegment(points, width/2f, texture, texture.getHeight() / (float)texture.getWidth(), colors);
        } else {
            drawSegments(points, width/2f, texture, texture.getHeight() / (float)texture.getWidth(), colors);
        }
        shapeRenderer.end();
	}

	private static void drawSegments(List<Vector2> points, float halfWidth, Texture tex, float texStretch, float[] colors) {
	    float colorWhite = new Color(1,1,1,1).toFloatBits();
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
	        } else {
	            vertices[0].set(vertices[3]);
	            verticesTex[0].set(verticesTex[3]);
	            vertColors[0] = vertColors[3];
	            vertices[1].set(vertices[2]);
	            verticesTex[1].set(verticesTex[2]);
	            vertColors[1] = vertColors[2];
	        }
            rot = p2.cpy().sub(p0).nor().rotate90(1);
            vertices[2].x = p1.x + rot.x * halfWidth;
            vertices[2].y = p1.y + rot.y * halfWidth;
            vertices[3].x = p1.x - rot.x * halfWidth;
            vertices[3].y = p1.y - rot.y * halfWidth;
            verticesTex[2].x = (len / (halfWidth*2)) * texStretch;
            verticesTex[2].y = 1;
            verticesTex[3].x = (len / (halfWidth*2)) * texStretch;
            verticesTex[3].y = 0;
            vertColors[2] = colors == null ? colorWhite : colors[i];
            vertColors[3] = colors == null ? colorWhite : colors[i];
            drawQuad(vertices, verticesTex, vertColors, tex);
            if (i == points.size() - 2) {
                len += p2.dst(p1);
                rot = p2.cpy().sub(p1).nor().rotate90(1);
                vertices[1].x = p2.x + rot.x * halfWidth;
                vertices[1].y = p2.y + rot.y * halfWidth;
                vertices[0].x = p2.x - rot.x * halfWidth;
                vertices[0].y = p2.y - rot.y * halfWidth;
                verticesTex[1].x = (len / (halfWidth*2)) * texStretch;
                verticesTex[1].y = 1;
                verticesTex[0].x = (len / (halfWidth*2)) * texStretch;
                verticesTex[0].y = 0;
                vertColors[1] = colors == null ? colorWhite : colors[points.size() - 1];
                vertColors[0] = colors == null ? colorWhite : colors[points.size() - 1];
                drawQuad(vertices, verticesTex, vertColors, tex);
            }
	    }
	}

	private static void drawSingleSegment(List<Vector2> points, float halfWidth, Texture tex, float texStretch, float[] colors) {
        float colorWhite = new Color(1,1,1,1).toFloatBits();
        Vector2 rot;
        Vector2 p0 = points.get(0);
        Vector2 p1 = points.get(1);
        float len = p1.dst(p0);

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

        vertices[2].x = p1.x + rot.x * halfWidth;
        vertices[2].y = p1.y + rot.y * halfWidth;
        vertices[3].x = p1.x - rot.x * halfWidth;
        vertices[3].y = p1.y - rot.y * halfWidth;
        verticesTex[2].x = (len / (halfWidth*2)) * texStretch;
        verticesTex[2].y = 1;
        verticesTex[3].x = (len / (halfWidth*2)) * texStretch;
        verticesTex[3].y = 0;
        vertColors[2] = colors == null ? colorWhite : colors[1];
        vertColors[3] = colors == null ? colorWhite : colors[1];
        drawQuad(vertices, verticesTex, vertColors, tex);
}

	private static void drawQuad(Vector2[] vertices, Vector2[] verticesTex, float[] vertColors, Texture tex) {
        for (int i = 0; i < 4; i++) {
            Vector2 v = vertices[i];
            Vector2 vTex = verticesTex[i];
            vertParts[i*5] = v.x;
            vertParts[i*5+1] = v.y;
            vertParts[i*5+2] = vertColors[i];  //new Color(1,1,1,1).toFloatBits();
            vertParts[i*5+3] = vTex.x;
            vertParts[i*5+4] = vTex.y;
        }
        shapeRenderer.draw(tex, vertParts, 0, 20);
	}

	static {
		for (int i = 0; i < vertices.length; i++)
			vertices[i] = new Vector2();
		for (int i = 0; i < verticesTex.length; i++)
		    verticesTex[i] = new Vector2();
	}
}
