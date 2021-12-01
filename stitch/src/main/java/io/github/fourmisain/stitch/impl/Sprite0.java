package io.github.fourmisain.stitch.impl;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;

/** Used as syntactic sugar despite being sugar-free. */
public class Sprite0 extends Sprite {
	public Sprite0(SpriteAtlasTexture atlas, Info info, int maxLevel, int atlasWidth, int atlasHeight, int x, int y, NativeImage nativeImage) {
		super(atlas, info, maxLevel, atlasWidth, atlasHeight, x, y, nativeImage);
	}
}
