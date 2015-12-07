package de.doccrazy.shared.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import de.doccrazy.shared.game.base.BlurUtils;

public abstract class ResourcesBase {
    protected final TextureAtlas atlas;

    public ResourcesBase() {
        atlas = null;
    }

    public ResourcesBase(String atlasFile) {
        atlas = new TextureAtlas(Gdx.files.internal(atlasFile)) {
            @Override
            public Sprite createSprite(String name) {
                Sprite result = super.createSprite(name);
                if (result == null) {
                    throw new IllegalArgumentException("Sprite " + name + " not found");
                }
                return result;
            }
            @Override
            public Sprite createSprite(String name, int index) {
                Sprite result = super.createSprite(name, index);
                if (result == null) {
                    throw new IllegalArgumentException("Sprite " + name + " not found at " + index);
                }
                return result;
            }
            @Override
            public Array<AtlasRegion> findRegions(String name) {
                Array<AtlasRegion> result = super.findRegions(name);
                if (result.size == 0) {
                    throw new IllegalArgumentException("Regions " + name + " not found");
                }
                return result;
            }
        };
    }

    protected Texture texture(String filename) {
        return new Texture(Gdx.files.internal(filename));
    }

    protected Texture textureWrap(String filename) {
        Texture tex = texture(filename);
        tex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return tex;
    }

    protected Sound sound(String filename) {
        return Gdx.audio.newSound(Gdx.files.internal(filename));
    }

    protected Music music(String filename) {
        Music music = Gdx.audio.newMusic(Gdx.files.internal(filename));
        music.setLooping(true);
        return music;
    }

    protected BitmapFont bitmapFont(String name) {
        return new BitmapFont(Gdx.files.internal(name + ".fnt"), Gdx.files.internal(name + ".png"), false);
    }

    protected Sprite colorSprite(Color color, int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        return new Sprite(tex);
    }

    protected ParticleEffectPool particle(String filename, float scale) {
    	ParticleEffect effect = new ParticleEffect();
    	effect.load(Gdx.files.internal(filename), atlas);
    	effect.scaleEffect(scale);
    	return new ParticleEffectPool(effect, 10, 100);
    }

    protected Animation flip(Animation anim, boolean flipX, boolean flipY) {
        TextureRegion[] framesFlipped = new TextureRegion[anim.getKeyFrames().length];
        for (int i = 0; i < anim.getKeyFrames().length; i++) {
            framesFlipped[i] = new TextureRegion(anim.getKeyFrames()[i]);
            framesFlipped[i].flip(flipX, flipY);
        }
        Animation result = new Animation(anim.getFrameDuration(), framesFlipped);
        result.setPlayMode(anim.getPlayMode());
        return result;
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    protected Animation[] createBlurLevels(Animation src, int radius, int levels) {
        Animation[] result = new Animation[levels];
        result[0] = src;
        for (int i = 1; i < levels; i++) {
            result[i] = blur(src, radius, i);
        }
        return result;
    }

    protected Sprite[] createBlurLevels(Sprite src, int levels) {
        Sprite[] result = new Sprite[levels];
        result[0] = src;
        for (int i = 1; i < levels; i++) {
            result[i] = blur(src, 2, i);
        }
        return result;
    }

    protected Texture blur(Texture src, int radius, int iterations) {
        src.getTextureData().prepare();
        Pixmap pixmap = src.getTextureData().consumePixmap();
        Pixmap blurred = BlurUtils.blur(pixmap, radius, iterations, src.getTextureData().disposePixmap());
        return new Texture(blurred);
    }

    protected Sprite blur(Sprite src, int radius, int iterations) {
        src.getTexture().getTextureData().prepare();
        Pixmap pixmap = src.getTexture().getTextureData().consumePixmap();
        Pixmap blurred = BlurUtils.blur(pixmap, src.getRegionX(), src.getRegionY(), src.getRegionWidth(), src.getRegionHeight(),
                0, 0, src.getRegionWidth(), src.getRegionHeight(), radius, iterations, src.getTexture().getTextureData().disposePixmap());
        return new Sprite(new Texture(blurred));
    }

    protected Animation blur(Animation src, int radius, int iterations) {
        Array<TextureRegion> newFrames = new Array<>();
        for (TextureRegion frame : src.getKeyFrames()) {
            frame.getTexture().getTextureData().prepare();
            Pixmap pixmap = frame.getTexture().getTextureData().consumePixmap();
            Pixmap blurred = BlurUtils.blur(pixmap, frame.getRegionX(), frame.getRegionY(), frame.getRegionWidth(), frame.getRegionHeight(),
                    0, 0, frame.getRegionWidth(), frame.getRegionHeight(), radius, iterations, frame.getTexture().getTextureData().disposePixmap());
            newFrames.add(new TextureRegion(new Texture(blurred)));
        }
        return new Animation(src.getFrameDuration(), newFrames, src.getPlayMode());
    }

}
