package com.github.tommyettinger.atlatl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.OrderedMap;

/**
 * An alternative to {@link TextureAtlas}.
 */
public class AtlAtlas implements Disposable {
    public OrderedMap<String, AtlasRegion[]> regions;
    public ObjectSet<Texture> textures;


    public static class AtlAtlasData {

        public static class Page {
            /** May be null if this page isn't associated with a file. In that case, {@link #texture} must be set. */
            public @Null FileHandle textureFile;
            /** May be null if the texture is not yet loaded. */
            public @Null Texture texture;
            public Pixmap.Format format = Pixmap.Format.RGBA8888;
            public Texture.TextureFilter minFilter = Texture.TextureFilter.Nearest, magFilter = Texture.TextureFilter.Nearest;
            public Texture.TextureWrap uWrap = Texture.TextureWrap.ClampToEdge, vWrap = Texture.TextureWrap.ClampToEdge;
            public boolean pma;

            public Page(){

            }

            public String getName() {
                return textureFile == null ? "(UNNAMED)" : textureFile.name();
            }
            public boolean isMipMap() {
                return minFilter.isMipMap();
            }

            public int getWidth() {
                return (texture == null) ? 0 : texture.getWidth();
            }

            public int getHeight() {
                return (texture == null) ? 0 : texture.getHeight();
            }
        }
    }


    @Override
    public void dispose() {
        for(Texture t : textures){
            t.dispose();
        }
        textures.clear();
    }
}
