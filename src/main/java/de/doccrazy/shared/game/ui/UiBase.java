package de.doccrazy.shared.game.ui;

import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import de.doccrazy.shared.game.BaseGameRenderer;
import de.doccrazy.shared.game.world.Box2dWorld;

public abstract class UiBase<W extends Box2dWorld<W>, R extends BaseGameRenderer<W>, I extends InputListener> extends Table {
    private W world;
    private R renderer;
    private I input;

    public UiBase(Stage stage, W world, R renderer) {
        this.world = world;
        this.renderer = renderer;
        stage.addActor(this);
        setFillParent(true);

        input = createGameInput();
        if (input != null) {
            world.stage.addListener(input);
        }
        InputListener uiInput = createUiInput();
        if (uiInput != null) {
            stage.addListener(uiInput);
        }

        top();
    }

    protected I createGameInput() {
        return null;
    }

    protected InputListener createUiInput() {
        return null;
    }

    public W getWorld() {
        return world;
    }

    public R getRenderer() {
        return renderer;
    }

    public I getInput() {
        return input;
    }
}
