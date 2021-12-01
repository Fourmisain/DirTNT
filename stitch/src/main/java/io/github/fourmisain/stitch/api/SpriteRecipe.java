package io.github.fourmisain.stitch.api;

import io.github.fourmisain.stitch.impl.Sprite0;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.Set;

/** A step by step instruction of how a sprite is crafted from other sprite dependencies */
public interface SpriteRecipe {
	// in the order they get executed:

	Identifier getAtlasId();

	Set<Identifier> getDependencies();

	void collectSpriteInfo(Sprite.Info info);

	Identifier getSpriteId();

	Sprite.Info generateSpriteInfo();

	void collectSprite(Sprite sprite);

	NativeImage generateImage(ResourceManager resourceManager);

	default Sprite generateSprite(ResourceManager resourceManager, SpriteAtlasTexture atlasTexture, int maxLevel, int atlasWidth, int atlasHeight, int x, int y) {
		return new Sprite0(atlasTexture, generateSpriteInfo(), maxLevel, atlasWidth, atlasHeight, x, y, generateImage(resourceManager));
	}
}
