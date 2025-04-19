package com.github.tommyettinger.atlatl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.*;

import java.util.Arrays;

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
            int i = 0;
            for(AtlasRegion ar : ent.value){
                ar.index = i++;
            }
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


    /** Adds a region to the atlas. The specified texture will be disposed when the atlas is disposed. */
    public AtlasRegion addRegion (String name, Texture texture, int x, int y, int width, int height) {
        textures.add(texture);
        AtlasRegion region = new AtlasRegion(texture, x, y, width, height);
        region.name = name;
        AtlasRegion[] found = regions.get(name);
        if(found == null) {
            region.index = 0;
            regions.put(name, new AtlasRegion[]{region});
        } else {
            found = Arrays.copyOf(found, found.length + 1);
            region.index = found.length - 1;
            found[region.index] = region;
            regions.put(name, found);
        }
        return region;
    }

    /** Adds a region to the atlas. The texture for the specified region will be disposed when the atlas is disposed. */
    public AtlasRegion addRegion (String name, TextureRegion textureRegion) {
        textures.add(textureRegion.getTexture());
        AtlasRegion region = new AtlasRegion(textureRegion);
        region.name = name;
        AtlasRegion[] found = regions.get(name);
        if(found == null) {
            region.index = 0;
            regions.put(name, new AtlasRegion[]{region});
        } else {
            found = Arrays.copyOf(found, found.length + 1);
            region.index = found.length - 1;
            found[region.index] = region;
            regions.put(name, found);
        }
        return region;
    }

    /**
     * Gets the OrderedMap of all names this atlas can look up mapped to all groups of AtlasRegion with that name, where
     * a group differs by index but shares one base name. Groups are always sorted by index in ascending order, starting
     * at 0, even if only one AtlasRegion is present.
     * @return the map this uses to look up groups of AtlasRegions associated with a String name, as a direct reference
     */
    public OrderedMap<String, AtlasRegion[]> getRegions () {
        return regions;
    }

    /**
     * Gets the unordered set of all Texture pages used in this atlas.
     * @return the textures of the pages, unordered, as a direct reference
     */
    public ObjectSet<Texture> getTextures () {
        return textures;
    }

    @Override
    public void dispose() {
        for(Texture t : textures){
            t.dispose();
        }
        textures.clear(0);
    }
}
