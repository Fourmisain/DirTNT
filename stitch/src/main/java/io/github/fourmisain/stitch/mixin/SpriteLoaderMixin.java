package io.github.fourmisain.stitch.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.fourmisain.stitch.impl.StitchImpl;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.client.texture.SpriteOpener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Mixin(value = SpriteLoader.class, priority = 950)
public abstract class SpriteLoaderMixin {
	@Shadow
	private static CompletableFuture<List<SpriteContents>> loadAll(SpriteOpener opener, List<Function<SpriteOpener, SpriteContents>> sources, Executor executor) {
		throw new AssertionError();
	}

	// Adds two in between steps to read the original SpriteContents and generate new sprites from them.
	@ModifyExpressionValue(
		method = "load(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/Identifier;ILjava/util/concurrent/Executor;Ljava/util/Set;)Ljava/util/concurrent/CompletableFuture;",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/concurrent/CompletableFuture;thenCompose(Ljava/util/function/Function;)Ljava/util/concurrent/CompletableFuture;"
		)
	)
	public CompletableFuture<List<SpriteContents>> addStitchSteps(CompletableFuture<List<SpriteContents>> original,
			@Local(argsOnly = true) ResourceManager resourceManager, @Local(argsOnly = true) Identifier atlasId, @Local(argsOnly = true) Executor executor, @Local SpriteOpener spriteOpener) {
		return stitch$stitchSteps(resourceManager, atlasId, executor, original, spriteOpener);
	}

	@Unique
	private static CompletableFuture<List<SpriteContents>> stitch$stitchSteps(ResourceManager resourceManager, Identifier atlasId, Executor executor, CompletableFuture<List<SpriteContents>> future, SpriteOpener spriteOpener) {
		return future.thenApply(list -> StitchImpl.prepareGenerating(list, atlasId, resourceManager))
			.thenCompose(stage -> {
				if (stage.generators().isEmpty()) {
					return CompletableFuture.completedFuture(stage.current());
				} else {
					// start generating sprites, returning the merged list when done
					return loadAll(spriteOpener, stage.generators(), executor)
						.thenApply(list -> ImmutableList.<SpriteContents>builder()
							.addAll(stage.current())
							.addAll(list)
							.build());
				}
			});
	}
}
