package io.github.fourmisain.stitch.mixin;

import com.google.common.collect.ImmutableList;
import io.github.fourmisain.stitch.impl.DummySpriteContents;
import io.github.fourmisain.stitch.impl.StitchImpl;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.atlas.AtlasLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Supplier;

// step 2: add a dummy Supplier<SpriteContents> which passes the parameters from method_47661 over the different threads to method_47663
@Mixin(AtlasLoader.class)
public abstract class AtlasLoaderMixin {
	@ModifyVariable(method = "loadSources",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lcom/google/common/collect/ImmutableList;builder()Lcom/google/common/collect/ImmutableList$Builder;"
		)
	)
	public ImmutableList.Builder<Supplier<SpriteContents>> stitch$addDummySpriteSupplier(ImmutableList.Builder<Supplier<SpriteContents>> builder) {
		builder.add(new Supplier<>() {
			private final Identifier atlasId = StitchImpl.atlasId.get();
			private final ResourceManager resourceManager = StitchImpl.resourceManager.get();

			@Override
			public SpriteContents get() {
				// step 3: pass parameters from dummy supplier to dummy sprite
				return new DummySpriteContents(atlasId, resourceManager);
			}
		});

		return builder;
	}
}
