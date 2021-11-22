package fourmisain.dirtnt.client;

import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.api.API;
import fourmisain.dirtnt.api.SpriteRecipe;
import net.minecraft.client.resource.metadata.AnimationFrameResourceMetadata;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class DirtTntSpriteRecipe implements SpriteRecipe {
	private int w, h;
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
	}

	@Override
	public Identifier getSpriteId() {
		return id;
	}

	@Override
	public Sprite.Info generateSpriteInfo() {
		DirTnt.LOGGER.debug("generateSpriteInfo() {} {}x{}", getSpriteId(), w, h);
		return new Sprite.Info(getSpriteId(), w, h, new AnimationResourceMetadata(List.of(new AnimationFrameResourceMetadata(0, -1)), w, h, 1, false));
	}

	@Override
	public void collectSprite(Sprite sprite) {
		DirTnt.LOGGER.debug("collectSprite() {}", sprite.getId());
		this.texture = API.getImage(sprite);
	}

	@Override
	public NativeImage generateImage(ResourceManager resourceManager) {
		DirTnt.LOGGER.debug("generateImage() {}x{}", texture.getWidth(), texture.getHeight()); // TODO can be 16x288?
		NativeImage image = new NativeImage(texture.getWidth(), texture.getHeight(), false);
		image.copyFrom(texture);

		//load template texture
		NativeImage templateTexture;
		Identifier templateId = API.getTexturePath(DirTnt.id("block/tnt_" + side + "_template"));

		try (Resource res = resourceManager.getResource(templateId)) {
			templateTexture = NativeImage.read(res.getInputStream());
		} catch (IOException e) {
			DirTnt.LOGGER.error("couldn't load texture template {}", templateId, e);
			return MissingSprite.getMissingSpriteTexture().getImage();
		}

		// blend textures together
		int xScale = image.getWidth() / 16;
		int yScale = image.getHeight() / 16;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				image.blend(x, y, templateTexture.getColor(x / xScale, y / yScale));
			}
		}

		templateTexture.close();

		return image;
	}
}
