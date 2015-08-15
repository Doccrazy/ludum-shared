package de.doccrazy.shared.game.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

public class TemporaryLabel extends Label {
    private float stateTime;

    public TemporaryLabel(String text, LabelStyle style, float time) {
        super(text, style);
        stateTime = time;

        setFillParent(true);
        setAlignment(Align.center | Align.bottom);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime -= delta;
        if (stateTime < 0f) {
            remove();
        }
    }
}
