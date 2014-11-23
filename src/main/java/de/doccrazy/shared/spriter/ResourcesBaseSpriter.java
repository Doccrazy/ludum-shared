package de.doccrazy.shared.spriter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.brashmonkey.spriter.Data;
import com.brashmonkey.spriter.Entity;
import com.brashmonkey.spriter.Loader;
import com.brashmonkey.spriter.SCMLReader;

public class ResourcesBaseSpriter {
    private final Data data;
    private final AtlasLoader loader;
    private final GdxDrawer drawer;

    public ResourcesBaseSpriter(String dataFile, TextureAtlas atlas) {
        data = scml(dataFile);
        loader = new AtlasLoader(data, atlas);
        loader.load("");
        drawer = new GdxDrawer(loader);
    }

    private Data scml(String filename) {
        return new SCMLReader(Gdx.files.internal(filename).read()).getData();
    }

    public Loader<Sprite> getLoader() {
        return loader;
    }

    public GdxDrawer getDrawer() {
        return drawer;
    }

    protected Entity entity(String name) {
        return data.getEntity(name);
    }
}
