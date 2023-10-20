package io.github.fourmisain.stitch.mixin;

import com.google.common.collect.ImmutableList;
import io.github.fourmisain.stitch.impl.StitchImpl;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.client.texture.SpriteOpener;
import net.minecraft.client.texture.atlas.AtlasLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(value = SpriteLoader.class, priority = 950)
public abstract class SpriteLoaderMixin {
	@Shadow
	public abstract SpriteLoader.StitchResult stitch(List<SpriteContents> list, int i, Executor executor);

	/**
	 * Adds two inbetween steps to read the original SpriteContents and generate new sprites from them.
	 * @reason See the StitchImpl comment, especially of the previous commit for full explanation for this overwrite.
	 * @author Fourmisain
	 */
	@Overwrite
	public CompletableFuture<SpriteLoader.StitchResult> load(ResourceManager resourceManager, Identifier atlasId, int mipmapLevel, Executor executor, Collection<ResourceMetadataReader<?>> metadatas) {
		SpriteOpener spriteOpener = SpriteOpener.create(metadatas);
		return CompletableFuture.supplyAsync(() -> {
				StitchImpl.atlasId.set(atlasId); // for use in loadSources/AtlasLoaderAtlasSourceSpriteRegions  mixin
				return AtlasLoader.of(resourceManager, atlasId).loadSources(resourceManager);
			}, executor)
			.thenCompose(list -> SpriteLoader.loadAll(spriteOpener, list, executor))
			.thenApply(list -> StitchImpl.prepareGenerating(list, atlasId, resourceManager))
			.thenCompose(stage -> {
				if (stage.generators().isEmpty()) {
					return CompletableFuture.completedFuture(stage.current());
				} else {
					// start generating sprites, returning the merged list when done
					return SpriteLoader.loadAll(spriteOpener, stage.generators(), executor)
						.thenApply(list -> ImmutableList.<SpriteContents>builder()
							.addAll(stage.current())
							.addAll(list)
							.build());
				}
			})
			.thenApply(list -> stitch(list, mipmapLevel, executor));
	}
}
