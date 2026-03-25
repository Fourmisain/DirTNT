package io.github.fourmisain.stitch.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.List;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.server.packs.metadata.MetadataSectionType;

@Mixin(SpriteContents.class)
public interface SpriteContentsAccessor {
	@Accessor
	NativeImage getOriginalImage();
	@Accessor
	List<MetadataSectionType.WithValue<?>> getAdditionalMetadata();
}
