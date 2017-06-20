package net.masonapps.csgvr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import net.masonapps.csgvr.vr.LoadingScreen;

import org.masonapps.libgdxgooglevr.gfx.VrGame;

/**
 * Created by Bob on 5/26/2017.
 */

class SolidModelingVrGame extends VrGame {

    private Skin skin;

    @Override
    public void create() {
        super.create();
        skin = new Skin();
        loadAsset(Style.ATLAS_FILE, TextureAtlas.class);
        setScreen(new LoadingScreen(this));
    }

    @Override
    protected void doneLoading(AssetManager assets) {
        super.doneLoading(assets);
        skin.addRegions(assets.get(Style.ATLAS_FILE, TextureAtlas.class));
        setupSkin();
        setScreen(new CsgVrTestScreen(this));
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

    public Skin getSkin() {
        return skin;
    }
}
