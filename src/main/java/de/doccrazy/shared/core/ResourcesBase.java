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
}
