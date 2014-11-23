package de.doccrazy.shared.spriter;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.brashmonkey.spriter.Data;
import com.brashmonkey.spriter.File;
import com.brashmonkey.spriter.FileReference;
import com.brashmonkey.spriter.Loader;

public class AtlasLoader extends Loader<Sprite> {
    private final TextureAtlas atlas;

    public AtlasLoader(Data data, TextureAtlas atlas) {
        super(data);
        this.atlas = atlas;
    }

    @Override
    protected Sprite loadResource(FileReference ref) {
        File file = data.getFile(ref);
        //cut off extension
        String nameInAtlas = file.name.substring(0, file.name.length()-4);
        Sprite sprite = atlas.createSprite(nameInAtlas);
        if (sprite == null) {
            throw new IllegalArgumentException("File " + nameInAtlas + " not found in atlas");
        }
        return sprite;
    }
}
