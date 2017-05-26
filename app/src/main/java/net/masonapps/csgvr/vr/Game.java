package net.masonapps.csgvr.vr;

import android.content.Context;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;

import net.masonapps.csgvr.Style;

import org.masonapps.libgdxgooglevr.gfx.VrGame;

/**
 * Created by Bob on 5/26/2017.
 */

public class Game extends VrGame {

    private final Context context;
    private boolean loading;
    private AssetManager assets;
    private Skin skin;

    public Game(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void create() {
        super.create();
        loading = true;
        setScreen(new LoadingScreen(this));
        skin = new Skin();
        assets = new AssetManager();
        assets.load(Style.ATLAS_FILE, TextureAtlas.class);
    }

    @Override
    public void pause() {
        super.pause();
    }

    @Override
    public void onDrawFrame(HeadTransform headTransform, Eye leftEye, Eye rightEye) {
        super.onDrawFrame(headTransform, leftEye, rightEye);
        if (loading) {
            if (assets.update()) {
                skin.addRegions(assets.get(Style.ATLAS_FILE, TextureAtlas.class));
                setupSkin();
                loading = false;
            }
        }
    }

    private void setupSkin() {
        addFont();
        addSliderStyle();
        addButtonStyle();
        addLabelStyle();
    }

    private void addFont() {
        skin.add(Style.DEFAULT, new BitmapFont(Gdx.files.internal(Style.FONT_FILE), skin.getRegion(Style.FONT_REGION)), BitmapFont.class);
    }

    private void addSliderStyle() {
        skin.add("default-horizontal", new Slider.SliderStyle(skin.newDrawable(Style.Drawables.slider, Style.COLOR_UP_2), skin.newDrawable(Style.Drawables.slider_knob, Style.COLOR_UP_2)), Slider.SliderStyle.class);
    }

    private void addButtonStyle() {
        final TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = skin.getFont(Style.DEFAULT);
        textButtonStyle.up = skin.newDrawable(Style.Drawables.button, Style.COLOR_UP);
        textButtonStyle.over = skin.newDrawable(Style.Drawables.button, Style.COLOR_OVER);
        textButtonStyle.down = skin.newDrawable(Style.Drawables.button, Style.COLOR_DOWN);
        textButtonStyle.checked = null;
        textButtonStyle.fontColor = Color.WHITE;
        skin.add(Style.DEFAULT, textButtonStyle, TextButton.TextButtonStyle.class);

        final TextButton.TextButtonStyle toggleStyle = new TextButton.TextButtonStyle();
        toggleStyle.font = skin.getFont(Style.DEFAULT);
        toggleStyle.up = skin.newDrawable(Style.Drawables.button, Style.COLOR_UP);
        toggleStyle.over = skin.newDrawable(Style.Drawables.button, Style.COLOR_OVER);
        toggleStyle.down = skin.newDrawable(Style.Drawables.button, Style.COLOR_DOWN);
        toggleStyle.checked = skin.newDrawable(Style.Drawables.button, Style.COLOR_DOWN);
        toggleStyle.fontColor = Color.WHITE;
        skin.add(Style.TOGGLE, toggleStyle, TextButton.TextButtonStyle.class);

        final TextButton.TextButtonStyle listBtnStyle = new TextButton.TextButtonStyle();
        listBtnStyle.font = skin.getFont(Style.DEFAULT);
        listBtnStyle.up = skin.newDrawable(Style.Drawables.button, new Color(0, 0, 0, 0.84706f));
        listBtnStyle.over = skin.newDrawable(Style.Drawables.button, new Color(0.15f, 0.15f, 0.15f, 0.84706f));
        listBtnStyle.down = skin.newDrawable(Style.Drawables.button, Style.COLOR_DOWN);
        listBtnStyle.checked = null;
        listBtnStyle.fontColor = Color.WHITE;
        skin.add(Style.LIST_ITEM, listBtnStyle, TextButton.TextButtonStyle.class);
    }

    private void addLabelStyle() {
        skin.add(Style.DEFAULT, new Label.LabelStyle(skin.getFont(Style.DEFAULT), Color.WHITE), Label.LabelStyle.class);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (skin != null)
            skin.dispose();
        skin = null;
    }

    public Skin getSkin() {
        return skin;
    }
}
