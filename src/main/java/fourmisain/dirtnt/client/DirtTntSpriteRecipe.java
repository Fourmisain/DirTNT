package fourmisain.dirtnt.client;

import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.mixin.MissingSpriteAccessor;
import io.github.fourmisain.stitch.api.SpriteRecipe;
import io.github.fourmisain.stitch.api.Stitch;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.*;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

public class DirtTntSpriteRecipe implements SpriteRecipe {
	// collected data
	private int w = 16, h = 16;
	private NativeImage image;
	private AnimationResourceMetadata animationData = AnimationResourceMetadata.EMPTY;

	private final String side;
	private final Identifier id;
	private final Identifier dirtTexture;

	public DirtTntSpriteRecipe(Identifier dirtType, String side) {
		this.side = side;
		Identifier blockId = DirTnt.getDirtTntBlockId(dirtType);
		this.id = new Identifier(blockId.getNamespace(), "block/" + blockId.getPath() + "_" + side);

		// note: this doesn't always correspond to the block's sprite, e.g. the dark_oak_button block uses the dark_oak_planks sprite
		// collectSpriteData() will therefore not be called for it
		this.dirtTexture = new Identifier(dirtType.getNamespace(), "block/" + dirtType.getPath());
	}

	@Override
	public Identifier getSpriteId() {
		return id;
	}

	@Override
	public Set<Identifier> getDependencies() {
		return Set.of(dirtTexture);
	}

	@Override
	public void collectSprite(SpriteContents sprite) {
		this.w = sprite.getWidth();
		this.h = sprite.getHeight();
		this.image = Stitch.getImage(sprite);
		this.animationData = Stitch.getAnimationData(sprite);
	}

	@Override
	public SpriteDimensions generateSize() {
		return new SpriteDimensions(w, h);
	}

	@Override
	public AnimationResourceMetadata generateAnimationData() {
		return animationData;
	}

	@Override
	public NativeImage generateImage(ResourceManager resourceManager) {
		//load template texture
		NativeImage templateTexture;
		Identifier templateId = Stitch.getTextureResourcePath(DirTnt.id("block/tnt_" + side + "_template"));

		Optional<Resource> maybeResource = resourceManager.getResource(templateId);
		if (maybeResource.isEmpty()) {
			DirTnt.LOGGER.error("texture template doesn't exist: {}", templateId);
			return null;
		}

		try (InputStream input = maybeResource.get().getInputStream()) {
			templateTexture = NativeImage.read(input);
		} catch (IOException e) {
			DirTnt.LOGGER.error("couldn't load texture template {}", templateId, e);
			return null;
		}

		// use missing texture if block id didn't correspond to texture id
		if (this.image == null) {
			this.image = MissingSpriteAccessor.invokeCreateImage(w, h);
		}

		NativeImage image = new NativeImage(this.image.getWidth(), this.image.getHeight(), false);
		image.copyFrom(this.image);

		// scaling factors
		int xScale = w / 16;
		int yScale = h / 16;

		int xFrames = image.getWidth() / w;
		int yFrames = image.getHeight() / h;

		// for each frame
		for (int j = 0; j < yFrames; j++) {
			for (int i = 0; i < xFrames; i++) {
				// blend textures together
				for (int y = 0; y < h; y++) {
					for (int x = 0; x < w; x++) {
						image.blend(i * w + x, j * h + y, templateTexture.getColor(x / xScale, y / yScale));
					}
				}

			}
		}

		templateTexture.close();

		return image;
	}
}
