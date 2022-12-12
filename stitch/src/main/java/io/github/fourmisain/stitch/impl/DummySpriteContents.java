package io.github.fourmisain.stitch.impl;

import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class DummySpriteContents extends SpriteContents {
	public static final Identifier DUMMY_ID = new Identifier(StitchImpl.MOD_ID, "dummy");

	public final Identifier atlasId;
	public final ResourceManager resourceManager;

	public DummySpriteContents(Identifier atlasId, ResourceManager resourceManager) {
		super(DUMMY_ID, new SpriteDimensions(1, 1), new NativeImage(1, 1, false), AnimationResourceMetadata.EMPTY);
		this.atlasId = atlasId;
		this.resourceManager = resourceManager;
	}
}
