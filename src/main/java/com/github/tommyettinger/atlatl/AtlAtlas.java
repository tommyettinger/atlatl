package com.github.tommyettinger.atlatl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.*;

import java.util.Arrays;

/**
 * An alternative to {@link TextureAtlas}.
 */
public class AtlAtlas implements Disposable {
    public final OrderedMap<String, AtlasRegion[]> regions;
    public final ObjectSet<Texture> textures;
    /**
     * An empty Sprite array that is reused when this needs to return an array with nothing in it.
     */
    protected static final Sprite[] EMPTY_SPRITE_ARRAY = new Sprite[0];

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


    /**
     * Adds a region to the atlas. The texture for the specified region will be disposed when the atlas is disposed.
     * The index of the region will be 0 if no existing regions had the specified name, or otherwise will be one greater
     * than the highest index of a region with the specified name.
     * @param name the name of the region to use
     * @param texture the Texture to use in the AtlasRegion, as the page image or "parent"
     * @param x the x-coordinate to get a region from texture
     * @param y the y-coordinate to get a region from texture
     * @param width the width of the region to use from texture
     * @param height the height of the region to use from texture
     * @return the new AtlasRegion this created and added
     */
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

    /**
     * Adds a region to the atlas. The texture for the specified region will be disposed when the atlas is disposed.
     * The index of the region will be 0 if no existing regions had the specified name, or otherwise will be one greater
     * than the highest index of a region with the specified name.
     * @param name the name of the region to use
     * @param textureRegion the TextureRegion to use in the AtlasRegion
     * @return the new AtlasRegion this created and added
     */
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

    /**
     * Returns the first region found with the specified name.
     * This method does not need to be cached; it is a simple map lookup and array access.
     * @param name the name to look up
     * @return the first region found with the specified name (first by index), or null if the name was not found
     */
    public @Null AtlasRegion findRegion (String name) {
        AtlasRegion[] found = regions.get(name);
        if(found == null || found.length == 0) return null;
        return found[0];
    }

    /**
     * Returns the region found with the specified name and index.
     * This method does not need to be cached; it is a simple map lookup and array access.
     * If index is negative, it will be treated as 0 for compatibility; if it is larger than the largest index this
     * knows for the specified name, then this returns null.
     * @param name the name to look up
     * @param index the index of the region to get with the specified name
     * @return the region with the specified name and index if found, or null if the name and index were not found
     */
    public @Null AtlasRegion findRegion (String name, int index) {
        index ^= index >> 31; // branch-less way to assign 0 to any negative index.
        AtlasRegion[] found = regions.get(name);
        if(found == null || index >= found.length) return null;
        return found[index];
    }

    /**
     * Returns all regions found with the specified name, sorted by smallest to largest {@link AtlasRegion#index index}.
     * This method does not need to be cached; it is a simple map lookup.
     * @param name the name to look up
     * @return the array of all regions with the specified name, sorted by index in ascending order
     */
    public @Null AtlasRegion[] findRegions (String name) {
        AtlasRegion[] found = regions.get(name);
        if(found == null || found.length == 0) return null;
        return found;
    }

    /**
     * Appends all regions found with the specified name into the given Array of TextureRegion or AtlasRegion.
     * @param name the name to look up
     * @param toFill an Array that will be modified in-place if regions are found with the specified name
     * @return {@code toFill}, potentially after modifications
     */
    public Array<TextureRegion> appendRegionsInto (String name, Array<TextureRegion> toFill) {
        AtlasRegion[] found = regions.get(name);
        if (found != null && found.length != 0)
            toFill.addAll(found);
        return toFill;
    }

    /**
     * Creates a Sprite or AtlasSprite, as appropriate, from the given AtlasRegion. If the region has no stripped
     * whitespace, this will return a Sprite that is really just a Sprite; otherwise, this will return an AtlasSprite
     * that uses the full set of extra info from the AtlasRegion.
     * @param region an AtlasRegion to use to create a Sprite
     * @return the created Sprite or AtlasSprite
     */
    protected Sprite newSprite (AtlasRegion region) {
        if (region.packedWidth == region.originalWidth && region.packedHeight == region.originalHeight) {
            if (region.rotate) {
                Sprite sprite = new Sprite(region);
                sprite.setBounds(0, 0, region.getRegionHeight(), region.getRegionWidth());
                sprite.rotate90(true);
                return sprite;
            }
            return new Sprite(region);
        }
        return new AtlasSprite(region);
    }

    /**
     * Returns a Sprite or AtlasSprite created from the first region found with the specified name as a sprite.
     * If whitespace was stripped from the region when it was packed, the sprite is automatically positioned as if
     * whitespace had not been stripped. This always either allocates a new Sprite (or AtlasSprite, if whitespace was
     * stripped from the found region), or returns null (if no region has the specified name).
     * @param name the name to look up
     * @return a new Sprite created from the first region found with the specified name (first by index), or
     * null if the name was not found
     */
    public @Null Sprite createSprite (String name) {
        AtlasRegion[] found = regions.get(name);
        if(found == null || found.length == 0) return null;
        return newSprite(found[0]);
    }

    /**
     * Returns a Sprite or AtlasSprite created from the region found with the specified name and index.
     * If index is negative, it will be treated as 0 for compatibility; if it is larger than the largest index this
     * knows for the specified name, then this returns null.
     * If whitespace was stripped from the region when it was packed, the sprite is automatically positioned as if
     * whitespace had not been stripped. This always either allocates a new Sprite (or AtlasSprite, if whitespace was
     * stripped from the found region), or returns null (if no region has the specified name).
     * @param name the name to look up
     * @param index the index of the region to get with the specified name
     * @return a new Sprite created from the region with the specified name and index if found, or
     * null if the name and index were not found
     */
    public @Null Sprite createSprite (String name, int index) {
        index ^= index >> 31; // branch-less way to assign 0 to any negative index.
        AtlasRegion[] found = regions.get(name);
        if(found == null || index >= found.length) return null;
        return newSprite(found[index]);
    }

    /**
     * Creates and returns an array of Sprite created from all regions found with the specified name, sorted by smallest
     * to largest {@link AtlasRegion#index index}. This does not allocate a new array if the name was not found.
     * @param name the name to look up
     * @return the array of Sprites made from all regions with the specified name, sorted by index in ascending order
     */
    public Sprite[] createSprites (String name) {
        AtlasRegion[] found = regions.get(name);
        int len;
        if(found == null || (len = found.length) == 0) return EMPTY_SPRITE_ARRAY;
        Sprite[] made = new Sprite[len];
        for (int i = 0; i < len; i++) {
            made[i] = newSprite(found[i]);
        }
        return made;
    }

    /**
     * Appends Sprites made from all regions found with the specified name into the given Array of Sprite.
     * @param name the name to look up
     * @param toFill an Array that will be modified in-place if regions are found with the specified name
     * @return {@code toFill}, potentially after modifications
     */
    public Array<Sprite> appendSpritesInto (String name, Array<Sprite> toFill) {
        AtlasRegion[] found = regions.get(name);
        int len;
        if(found == null || (len = found.length) == 0) return toFill;
        for (int i = 0; i < len; i++) {
            toFill.add(newSprite(found[i]));
        }
        return toFill;
    }

    @Override
    public void dispose() {
        for(Texture t : textures){
            t.dispose();
        }
        textures.clear(0);
    }
}
