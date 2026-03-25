package fourmisain.dirtnt.client;

import com.mojang.blaze3d.platform.NativeImage;
import fourmisain.dirtnt.DirTnt;
import io.github.fourmisain.stitch.api.SpriteRecipe;
import io.github.fourmisain.stitch.api.Stitch;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

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
	private Optional<AnimationMetadataSection> animationResourceMetadata = Optional.empty();
	private Optional<TextureMetadataSection> textureResourceMetadata = Optional.empty();
	private List<MetadataSectionType.WithValue<?>> additionalMetadata;

	private final String side;
	private final Identifier id;
	private final Identifier dirtTexture;

	public DirtTntSpriteRecipe(Identifier dirtType, String side) {
		this.side = side;
		Identifier blockId = DirTnt.getDirtTntBlockId(dirtType);
		this.id = Identifier.fromNamespaceAndPath(blockId.getNamespace(), "block/" + blockId.getPath() + "_" + side);

		// note: this doesn't always correspond to the block's sprite, e.g. the dark_oak_button block uses the dark_oak_planks sprite
		// collectSpriteData() will therefore not be called for it
		this.dirtTexture = Identifier.fromNamespaceAndPath(dirtType.getNamespace(), "block/" + dirtType.getPath());
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
		this.w = sprite.width();
		this.h = sprite.height();
		this.image = Stitch.getImage(sprite);
		this.animationResourceMetadata = Stitch.getAnimationResourceMetadata(sprite);
		this.textureResourceMetadata = Stitch.getTextureResourceMetadata(sprite);
		this.additionalMetadata = Stitch.getAdditionalMetadata(sprite);
	}

	@Override
	public FrameSize generateSize() {
		return new FrameSize(w, h);
	}

	@Override
	public Optional<AnimationMetadataSection> generateAnimationResourceMetadata() {
		return animationResourceMetadata;
	}

	@Override
	public Optional<TextureMetadataSection> generateTextureResourceMetadata() {
		return textureResourceMetadata;
	}

	@Override
	public List<MetadataSectionType.WithValue<?>> generateAdditionalMetadata() {
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

		try (InputStream input = maybeResource.get().open()) {
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
						Stitch.blendColors(image, i * w + x, j * h + y, templateTexture.getPixel(x / xScale, y / yScale));
					}
				}
			}
		}

		templateTexture.close();

		return image;
	}
}
