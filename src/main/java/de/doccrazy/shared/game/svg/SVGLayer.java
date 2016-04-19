package de.doccrazy.shared.game.svg;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader;
import de.doccrazy.shared.game.world.BodyBuilder;
import de.doccrazy.shared.game.world.ShapeBuilder;
import org.apache.batik.parser.AWTTransformProducer;
import org.apache.batik.parser.PathParser;
import org.apache.batik.parser.TransformListParser;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SVGLayer {
    public static final String ATTR_LABEL = "inkscape:label";

    private final XmlReader.Element layerElement;
    private AffineTransform transform;
    private Rectangle dimensions;

    public SVGLayer(XmlReader.Element layerElement) {
        this(layerElement, null);
        String[] dimStr = layerElement.get("viewBox").split(" ");
        dimensions = new Rectangle(Float.parseFloat(dimStr[0]), Float.parseFloat(dimStr[1]), Float.parseFloat(dimStr[2]), Float.parseFloat(dimStr[3]));

        transform = new AffineTransform();
        //flip y coordinate to match physics world (Inkscape apparently does this internally, too)
        transform.translate(0f, dimensions.height/2);
        transform.scale(1f, -1f);
        transform.translate(0f, -dimensions.height/2);
    }

    private SVGLayer(XmlReader.Element layerElement, AffineTransform transform) {
        this.layerElement = layerElement;
        this.transform = transform;
    }

    private SVGLayer createSubLayer(XmlReader.Element subLayerElement) {
        AffineTransform groupTransform = createTransform(subLayerElement);
        groupTransform.preConcatenate(transform);
        return new SVGLayer(subLayerElement, groupTransform);
    }

    public void applyScale(float scale) {
        transform.preConcatenate(AffineTransform.getScaleInstance(scale, scale));
    }

    /**
     * Get document dimensions in world coordinates (scaled), as defined by viewBox. Only available for root (document) element
     */
    public Rectangle getDimensionsTransformed() {
        Vector2 pos = new Vector2();
        Vector2 size = new Vector2();
        dimensions.getPosition(pos);
        dimensions.getSize(size);
        applyTransform(transform, pos);
        size = applyDeltaTransform(transform, size);
        //we flipped y in the constructor, which is not reflected in the dimensions box
        return new Rectangle(pos.x, pos.y + size.y, size.x, -size.y);
    }

    public SVGLayer getLayerByLabel(String label) {
        return createSubLayer(childByLabel("g", label));
    }

    public Vector2 getRectSizeImmediate(String label) {
        XmlReader.Element rectElement = childByLabel("rect", label);
        return new Vector2(Float.parseFloat(rectElement.getAttribute("width")), Float.parseFloat(rectElement.getAttribute("height")));
    }

    public Vector2 getRectCenter(String label) {
        return parseRectGetCenter(childByLabel("rect", label));
    }

    public Vector2[] getRectAsPoly(String label) {
        return parseRectAsPoly(childByLabel("rect", label));
    }

    public void processRectCenterByPrefix(String prefix, TriConsumer<String, Vector2, Color> consumer) {
        for (XmlReader.Element element : childrenByPrefix("rect", prefix)) {
            Vector2 center = parseRectGetCenter(element);
            Color color = colorFromStyle(element);
            String objectName = element.getAttribute(ATTR_LABEL).substring(prefix.length());
            consumer.accept(objectName, center, color);
        }
    }

    public void processRectAsPolyByPrefix(String prefix, TriConsumer<String, Vector2[], Color> consumer) {
        for (XmlReader.Element element : childrenByPrefix("rect", prefix)) {
            Vector2[] rect = parseRectAsPoly(element);
            Color color = colorFromStyle(element);
            String objectName = element.getAttribute(ATTR_LABEL).substring(prefix.length());
            consumer.accept(objectName, rect, color);
        }
    }

    public void processCircleByPrefix(String prefix, TriConsumer<String, Circle, Color> consumer) {
        for (XmlReader.Element element : childrenByPrefix("circle", prefix)) {
            Circle circle = parseCircle(element);
            Color color = colorFromStyle(element);
            String objectName = element.getAttribute(ATTR_LABEL).substring(prefix.length());
            consumer.accept(objectName, circle, color);
        }
    }

    public void processArcByPrefix(String prefix, TriConsumer<String, Arc, Color> consumer) {
        for (XmlReader.Element element : childrenByPrefix("path", prefix)) {
            Arc arc = parseArc(element);
            Color color = colorFromStyle(element);
            String objectName = element.getAttribute(ATTR_LABEL).substring(prefix.length());
            consumer.accept(objectName, arc, color);
        }
    }

    public void createPhysicsBodiesRecursive(Consumer<BodyBuilder> bodyConsumer) {
        PathParser parser = new PathParser();
        for (XmlReader.Element path : layerElement.getChildrenByName("path")) {
            AffineTransform pathTransform = createTransform(path);
            pathTransform.preConcatenate(transform);
            BodyCreatingPathHandler handler = new BodyCreatingPathHandler(pathTransform);
            parser.setPathHandler(handler);

            parser.parse(path.getAttribute("d"));
            BodyBuilder builder = handler.getBodyBuilder();
            applyPhysicsProps(path, builder);
            bodyConsumer.accept(builder);
        }
        for (XmlReader.Element rect : layerElement.getChildrenByName("rect")) {
            Vector2[] parsed = parseRectAsPoly(rect);
            BodyBuilder builder = BodyBuilder.forStatic(parsed[0])
                    .fixShape(ShapeBuilder.polyRel(parsed));
            applyPhysicsProps(rect, builder);
            bodyConsumer.accept(builder);
        }
        for (XmlReader.Element subGroup : layerElement.getChildrenByName("g")) {
            SVGLayer subLayer = createSubLayer(subGroup);
            subLayer.createPhysicsBodiesRecursive(bodyConsumer);
        }
    }

    private void applyPhysicsProps(XmlReader.Element rect, BodyBuilder builder) {
        try {
            for (String desc : rect.get("desc").split(";")) {
                if (desc.startsWith("fp:")) {
                    String[] fp = desc.substring("fp:".length()).split(",");
                    builder.fixProps(Float.parseFloat(fp[0]), Float.parseFloat(fp[1]), Float.parseFloat(fp[2]));
                }
            }
        } catch (GdxRuntimeException ignore) {
        }
    }

    private Color colorFromStyle(XmlReader.Element element) {
        String[] style = element.getAttribute("style").split(";");
        Color color = new Color();
        for (String s : style) {
            if (s.startsWith("fill:#")) {
                color.set(Color.valueOf(s.substring("fill:#".length())));
            } else if (s.startsWith("fill-opacity:")) {
                color.set(color.r, color.g, color.b, Float.parseFloat(s.substring("fill-opacity:".length())));
            }
        }
        return color;
    }

    private Vector2 parseRectGetCenter(XmlReader.Element rect) {
        Vector2[] r = parseRectAsPoly(rect);
        return r[0].lerp(r[2], 0.5f);
    }

    /**
     * @return [bottom left, top left, top right, bottom right]
     */
    private Vector2[] parseRectAsPoly(XmlReader.Element rect) {
        AffineTransform rectTransform = createTransform(rect);
        rectTransform.preConcatenate(transform);
        Vector2[] p = new Vector2[4];
        p[0] = new Vector2(Float.parseFloat(rect.getAttribute("x")), Float.parseFloat(rect.getAttribute("y")));
        p[2] = new Vector2(p[0].x + Float.parseFloat(rect.getAttribute("width")), p[0].y + Float.parseFloat(rect.getAttribute("height")));
        p[1] = new Vector2(p[0].x, p[2].y);
        p[3] = new Vector2(p[2].x, p[0].y);
        applyTransform(rectTransform, p[0]);
        applyTransform(rectTransform, p[1]);
        applyTransform(rectTransform, p[2]);
        applyTransform(rectTransform, p[3]);
        return p;
    }

    private Circle parseCircle(XmlReader.Element circle) {
        AffineTransform circleTransform = createTransform(circle);
        circleTransform.preConcatenate(transform);
        Vector2 c = new Vector2(Float.parseFloat(circle.getAttribute("cx")), Float.parseFloat(circle.getAttribute("cy")));
        float r = Float.parseFloat(circle.getAttribute("r"));
        applyTransform(circleTransform, c);
        r = applyDeltaTransform(circleTransform, r);
        return new Circle(c, Math.abs(r));
    }

    private Arc parseArc(XmlReader.Element arc) {
        AffineTransform arcTransform = createTransform(arc);
        arcTransform.preConcatenate(transform);
        Vector2 c = new Vector2(Float.parseFloat(arc.getAttribute("sodipodi:cx")), Float.parseFloat(arc.getAttribute("sodipodi:cy")));
        float r = Float.parseFloat(arc.getAttribute("sodipodi:rx"));
        float a1 = Float.parseFloat(arc.getAttribute("sodipodi:start"));
        float a2 = Float.parseFloat(arc.getAttribute("sodipodi:end"));
        applyTransform(arcTransform, c);
        r = applyDeltaTransform(arcTransform, r);
        Vector2 vMirror = applyDeltaTransform(arcTransform, new Vector2(1, 1));
        if (vMirror.x < 0) {
            a1 = (float) (Math.PI - a1);
            a2 = (float) (Math.PI - a2);
        }
        if (vMirror.y < 0) {
            a1 = -a1;
            a2 = -a2;
        }
        return new Arc(c.x, c.y, Math.abs(r), a1, a2);
    }

    private XmlReader.Element childByLabel(String type, String label) {
        for (XmlReader.Element element : layerElement.getChildrenByName(type)) {
            if (label.equals(element.getAttribute(ATTR_LABEL))) {
                return element;
            }
        }
        throw new IllegalArgumentException("No " + type + " with label '" + label + "' found on this layer");
    }

    private List<XmlReader.Element> childrenByPrefix(String type, String prefix) {
        return StreamSupport.stream(layerElement.getChildrenByName(type).spliterator(), false)
                .filter(e -> hasAttribute(e, ATTR_LABEL) && e.getAttribute(ATTR_LABEL).startsWith(prefix))
                .collect(Collectors.toList());
    }

    private boolean hasAttribute(XmlReader.Element element, String attribute) {
        String val;
        try {
            val = element.getAttribute(attribute);
        } catch (GdxRuntimeException e) {
            return false;
        }
        return val != null && val.length() > 0;
    }

    private AffineTransform createTransform(XmlReader.Element element) {
        String t;
        try {
            t = element.getAttribute("transform");
        } catch (Exception e) {
            return new AffineTransform();
        }
        TransformListParser transformParser = new TransformListParser();
        AWTTransformProducer transformProducer = new AWTTransformProducer();
        transformParser.setTransformListHandler(transformProducer);

        transformParser.parse(t);
        return transformProducer.getAffineTransform();
    }

    private void applyTransform(AffineTransform transform, Vector2 v) {
        Point2D tp = transform.transform(new Point2D.Float(v.x, v.y), new Point2D.Float());
        v.set((float)tp.getX(), (float)tp.getY());
    }

    private float applyDeltaTransform(AffineTransform transform, float l) {
        Point2D tp = transform.deltaTransform(new Point2D.Float(l, 0), new Point2D.Float());
        return (float) tp.getX();
    }

    private Vector2 applyDeltaTransform(AffineTransform transform, Vector2 v) {
        Point2D tp = transform.deltaTransform(new Point2D.Float(v.x, v.y), new Point2D.Float());
        return new Vector2((float) tp.getX(), (float) tp.getY());
    }
}

