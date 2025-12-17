package fourmisain.dirtnt.client;

import fourmisain.dirtnt.DirTnt;
import io.github.fourmisain.stitch.api.SpriteRecipe;
import io.github.fourmisain.stitch.api.Stitch;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.resource.metadata.TextureResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DirtTntSpriteRecipe implements SpriteRecipe {
	// collected data
	private int w = 16, h = 16;
	private NativeImage image;
	private Optional<AnimationResourceMetadata> animationResourceMetadata = Optional.empty();
	private Optional<TextureResourceMetadata> textureResourceMetadata = Optional.empty();
	private List<ResourceMetadataSerializer.Value<?>> additionalMetadata;

	private final String side;
	private final Identifier id;
	private final Identifier dirtTexture;

	public DirtTntSpriteRecipe(Identifier dirtType, String side) {
		this.side = side;
		Identifier blockId = DirTnt.getDirtTntBlockId(dirtType);
		this.id = Identifier.of(blockId.getNamespace(), "block/" + blockId.getPath() + "_" + side);

		// note: this doesn't always correspond to the block's sprite, e.g. the dark_oak_button block uses the dark_oak_planks sprite
		// collectSpriteData() will therefore not be called for it
		this.dirtTexture = Identifier.of(dirtType.getNamespace(), "block/" + dirtType.getPath());
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
		this.animationResourceMetadata = Stitch.getAnimationResourceMetadata(sprite);
		this.textureResourceMetadata = Stitch.getTextureResourceMetadata(sprite);
		this.additionalMetadata = Stitch.getAdditionalMetadata(sprite);
	}

	@Override
	public SpriteDimensions generateSize() {
		return new SpriteDimensions(w, h);
	}

	@Override
	public Optional<AnimationResourceMetadata> generateAnimationResourceMetadata() {
		return animationResourceMetadata;
	}

	@Override
	public Optional<TextureResourceMetadata> generateTextureResourceMetadata() {
		return textureResourceMetadata;
	}

	@Override
	public List<ResourceMetadataSerializer.Value<?>> generateAdditionalMetadata() {
		return additionalMetadata;
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
			this.image = Stitch.getMissingSprite(w, h);
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
						Stitch.blendColors(image, i * w + x, j * h + y, templateTexture.getColorArgb(x / xScale, y / yScale));
					}
				}
			}
		}

		templateTexture.close();

		return image;
	}
}
