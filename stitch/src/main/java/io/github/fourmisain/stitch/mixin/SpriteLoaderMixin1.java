package io.github.fourmisain.stitch.mixin;

import io.github.fourmisain.stitch.impl.StitchImpl;
import net.minecraft.client.texture.SpriteLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.concurrent.Executor;

@Mixin(value = SpriteLoader.class, priority = 990)
public abstract class SpriteLoaderMixin1 {
	@ModifyVariable(method = "method_47663", at = @At("HEAD"), argsOnly = true)
	public Executor stitch$captureExecutor(Executor executor) {
		StitchImpl.executor.set(executor);
		return executor;
	}
}
