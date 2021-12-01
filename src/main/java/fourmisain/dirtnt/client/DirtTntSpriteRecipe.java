package fourmisain.dirtnt.client;

import fourmisain.dirtnt.DirTnt;
import io.github.fourmisain.stitch.api.SpriteRecipe;
import io.github.fourmisain.stitch.api.Stitch;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Set;

public class DirtTntSpriteRecipe implements SpriteRecipe {
	private int w, h;
	private AnimationResourceMetadata animationData;
	private NativeImage texture;

	private final String side;
	private final Identifier id;
	private final Identifier dirtTexture;

	public DirtTntSpriteRecipe(Identifier dirtType, String side) {
		this.side = side;
		this.dirtTexture = new Identifier(dirtType.getNamespace(), "block/" + dirtType.getPath());
		Identifier blockId = DirTnt.getDirtTntBlockId(dirtType);
		this.id = new Identifier(blockId.getNamespace(), "block/" + blockId.getPath() + "_" + side);
	}

	@Override
	public Identifier getAtlasId() {
		return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
	}

	@Override
	public Set<Identifier> getDependencies() {
		return Set.of(dirtTexture);
	}

	@Override
	public void collectSpriteInfo(Sprite.Info info) {
		DirTnt.LOGGER.debug("collectSpriteInfo() {} {}x{}", info.getId(), info.getWidth(), info.getHeight());
		w = Math.max(w, info.getWidth());
		h = Math.max(h, info.getHeight());
		animationData = Stitch.getAnimationData(info);
	}

	@Override
	public Identifier getSpriteId() {
		return id;
	}

	@Override
	public Sprite.Info generateSpriteInfo() {
		DirTnt.LOGGER.debug("generateSpriteInfo() {} {}x{}", getSpriteId(), w, h);
		return new Sprite.Info(getSpriteId(), w, h, animationData);
	}

	@Override
	public void collectSprite(Sprite sprite) {
		DirTnt.LOGGER.debug("collectSprite() {}", sprite.getId());
		this.texture = Stitch.getImage(sprite);
	}

	@Override
	public NativeImage generateImage(ResourceManager resourceManager) {
		DirTnt.LOGGER.debug("generateImage() {}x{}", texture.getWidth(), texture.getHeight());
		NativeImage image = new NativeImage(texture.getWidth(), texture.getHeight(), false);
		image.copyFrom(texture);

		//load template texture
		NativeImage templateTexture;
		Identifier templateId = Stitch.getTextureResourcePath(DirTnt.id("block/tnt_" + side + "_template"));

		try (Resource res = resourceManager.getResource(templateId)) {
			templateTexture = NativeImage.read(res.getInputStream());
		} catch (IOException e) {
			DirTnt.LOGGER.error("couldn't load texture template {}", templateId, e);
			return MissingSprite.getMissingSpriteTexture().getImage();
		}

		// frame dimensions
		int fw = animationData.getWidth(w);
		int fh = animationData.getHeight(h);

		// scaling factors
		int xScale = fw / 16;
		int yScale = fh / 16;

		int xFrames = image.getWidth() / animationData.getWidth(w);
		int yFrames = image.getHeight() / animationData.getHeight(h);

		// for each frame
		for (int j = 0; j < yFrames; j++) {
			for (int i = 0; i < xFrames; i++) {
				// blend textures together
				for (int y = 0; y < fh; y++) {
					for (int x = 0; x < fw; x++) {
						image.blend(i * fw + x, j * fh + y, templateTexture.getColor(x / xScale, y / yScale));
					}
				}

			}
		}

		templateTexture.close();

		return image;
	}
}
