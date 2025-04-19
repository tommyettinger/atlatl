package com.github.tommyettinger.atlatl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.*;

/**
 * An alternative to {@link TextureAtlas}.
 */
public class AtlAtlas implements Disposable {
    public final OrderedMap<String, AtlasRegion[]> regions;
    public final ObjectSet<Texture> textures;

    public AtlAtlas() {
        textures = new ObjectSet<>(1);
        regions = new OrderedMap<>(64, 0.625f);
    }

    public AtlAtlas(TextureAtlas ta) {
        textures = ta.getTextures();
        Array<AtlasRegion> rs = ta.getRegions();
        OrderedMap<String, Array<AtlasRegion>> map = new OrderedMap<>(rs.size, 0.625f);

        for (int i = 0, n = rs.size; i < n; i++) {
            AtlasRegion region = rs.get(i);
            Array<AtlasRegion> matched = map.get(region.name);
            if(matched == null) {
                matched = new Array<>(AtlasRegion.class);
                map.put(region.name, matched);
            }
            matched.add(region);
        }
        regions = new OrderedMap<>(map.size, 0.625f);
        for(OrderedMap.Entry<String, Array<AtlasRegion>> ent : map.entries()) {
            ent.value.sort((a, b) -> (a.index - b.index));
            regions.put(ent.key, ent.value.toArray());
        }
    }

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
