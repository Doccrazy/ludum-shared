package de.doccrazy.shared.game.svg;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import de.doccrazy.shared.game.base.PolyRenderer;
import de.doccrazy.shared.game.world.BodyBuilder;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PathTriangulator {
    /**
     * Converts the outline of an AWT {@link Shape} to polygons, then triangulates those polygons into Box2D shapes.
     * @param path the {@link Shape} to process
     * @param transform the combined affine transformation matrix
     * @param flatness the maximum allowable distance between the control points and the flattened curve
     * @param bodyConsumer receives generated bodies
     */
    public static void process(Shape path, AffineTransform transform, float flatness, Consumer<BodyBuilder> bodyConsumer) {
        FlatteningPathIterator iter = new FlatteningPathIterator(path.getPathIterator(transform), flatness, 6);
        List<Vector2> polyPoints = new ArrayList<>();
        Vector2 start = new Vector2();
        float[] coords=new float[6];
        while (!iter.isDone()) {
            int type = iter.currentSegment(coords);
            switch(type) {
                case PathIterator.SEG_MOVETO:
                    start.set(coords[0], coords[1]);
                    System.out.println("Iter move " + start);
                    break;
                case PathIterator.SEG_LINETO:
                    polyPoints.add(new Vector2(coords[0] - start.x, coords[1] - start.y));
                    System.out.println("Iter line " + polyPoints.get(polyPoints.size() - 1));
                    break;
                case PathIterator.SEG_CLOSE:
                    System.out.print("Iter close");
                    if (Math.abs(polyPoints.get(polyPoints.size()-1).x) > 0.001f
                            || Math.abs(polyPoints.get(polyPoints.size()-1).y) > 0.001f) {
                        polyPoints.add(Vector2.Zero);
                        System.out.println(" (insert start point)");
                    } else {
                        System.out.println();
                    }

                    List<PolygonShape> shapes = PolyRenderer.createPolyShape(polyPoints);
                    BodyBuilder bodyBuilder = BodyBuilder.forStatic(start);
                    for (int i = 0; i < shapes.size(); i++) {
                        if (i > 0) {
                            bodyBuilder.newFixture();
                        }
                        bodyBuilder.fixShape(shapes.get(i));
                    }
                    bodyConsumer.accept(bodyBuilder);
                    polyPoints.clear();
                    break;
            }
            iter.next();
        }
    }
}
