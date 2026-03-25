package io.github.fourmisain.stitch.api;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.List;
import java.util.Optional;
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
		return Identifier.withDefaultNamespace("blocks"); // corresponds to BLOCK_ATLAS_TEXTURE
	}

	/** Which sprites this recipe depends on. */
	Set<Identifier> getDependencies();

	/** The id of the sprite this recipe generates or overwrites */
	Identifier getSpriteId();

	/** Called for each sprite dependency, if it exists. */
	void collectSprite(SpriteContents spriteContents);

	FrameSize generateSize();

	Optional<AnimationMetadataSection> generateAnimationResourceMetadata();

	Optional<TextureMetadataSection> generateTextureResourceMetadata();

	List<MetadataSectionType.WithValue<?>> generateAdditionalMetadata();

	NativeImage generateImage(ResourceManager resourceManager);
}
