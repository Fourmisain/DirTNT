package io.github.fourmisain.stitch.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.fourmisain.stitch.impl.StitchImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(value = SpriteLoader.class, priority = 950)
public abstract class SpriteLoaderMixin {
	@Shadow
	private static CompletableFuture<List<SpriteContents>> runSpriteSuppliers(SpriteResourceLoader opener, List<SpriteSource.Loader> sources, Executor executor) {
		throw new AssertionError();
	}

	// Adds two in between steps to read the original SpriteContents and generate new sprites from them.
	@ModifyExpressionValue(
		method = "loadAndStitch(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/Identifier;ILjava/util/concurrent/Executor;Ljava/util/Set;)Ljava/util/concurrent/CompletableFuture;",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/concurrent/CompletableFuture;thenCompose(Ljava/util/function/Function;)Ljava/util/concurrent/CompletableFuture;"
		)
	)
	public CompletableFuture<List<SpriteContents>> addStitchSteps(CompletableFuture<List<SpriteContents>> original,
			@Local(argsOnly = true) ResourceManager resourceManager, @Local(argsOnly = true) Identifier atlasId, @Local(argsOnly = true) Executor executor, @Local SpriteResourceLoader spriteOpener) {
		return stitch$stitchSteps(resourceManager, atlasId, executor, original, spriteOpener);
	}

	@Unique
	private static CompletableFuture<List<SpriteContents>> stitch$stitchSteps(ResourceManager resourceManager, Identifier atlasId, Executor executor, CompletableFuture<List<SpriteContents>> future, SpriteResourceLoader spriteOpener) {
		return future.thenApply(list -> StitchImpl.prepareGenerating(list, atlasId, resourceManager))
			.thenCompose(stage -> {
				if (stage.generators().isEmpty()) {
					return CompletableFuture.completedFuture(stage.current());
				} else {
					// start generating sprites, returning the merged list when done
					return runSpriteSuppliers(spriteOpener, stage.generators(), executor)
						.thenApply(list -> ImmutableList.<SpriteContents>builder()
							.addAll(stage.current())
							.addAll(list)
							.build());
				}
			});
	}
}
