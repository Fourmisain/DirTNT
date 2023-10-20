package io.github.fourmisain.stitch.mixin;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.resource.metadata.ResourceMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpriteContents.class)
public interface SpriteContentsAccessor {
	@Accessor
	NativeImage getImage();
	@Accessor("metadata")
	ResourceMetadata getResourceMetadata();
}
