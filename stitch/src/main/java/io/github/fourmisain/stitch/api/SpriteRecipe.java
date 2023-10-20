package io.github.fourmisain.stitch.api;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.util.Identifier;

import java.util.Set;

/** An instruction of how a sprite is crafted from other sprite dependencies */
public interface SpriteRecipe {
	/**
	 * The atlas loader id this recipe is made for. One of
	 *   minecraft:banner_patterns
	 *   minecraft:beds
	 *   minecraft:chests
	 *   minecraft:shield_patterns
	 *   minecraft:signs
	 *   minecraft:shulker_boxes
	 *   minecraft:blocks
	 * (see BakedModelManager.LAYERS_TO_LOADERS) or of
	 *   minecraft:mob_effects
	 *   minecraft:paintings
	 *   minecraft:particles
	 */
	default Identifier getAtlasId() {
		return new Identifier("blocks"); // corresponds to BLOCK_ATLAS_TEXTURE
	}

	/** Which sprites this recipe depends on. */
	Set<Identifier> getDependencies();

	/** The id of the sprite this recipe generates or overwrites */
	Identifier getSpriteId();

	/** Called for each sprite dependency, if it exists. */
	void collectSprite(SpriteContents spriteContents);

	SpriteDimensions generateSize();

	ResourceMetadata generateResourceMetadata() ;

	NativeImage generateImage(ResourceManager resourceManager);
}
