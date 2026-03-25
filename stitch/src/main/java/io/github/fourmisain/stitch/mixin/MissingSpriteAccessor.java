package io.github.fourmisain.stitch.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MissingTextureAtlasSprite.class)
public interface MissingSpriteAccessor {
	@Invoker
	static NativeImage invokeGenerateMissingImage(int width, int height) {
		throw new AssertionError();
	}
}
