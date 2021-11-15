package fourmisain.dirtnt.mixin;

import fourmisain.dirtnt.DirTnt;
import net.minecraft.block.Block;
import net.minecraft.resource.ResourceManager;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroupLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(TagGroupLoader.class)
public abstract class TagGroupLoaderMixin {
	@Shadow @Final
	private String dataType;

	@Inject(method = "loadTags", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
	public void dirtnt$endermenLoveDirtTnt(ResourceManager manager, CallbackInfoReturnable<Map<Identifier, Tag.Builder>> cir, Map<Identifier, Tag.Builder> map) {
		if (dataType.equals("tags/blocks")) {
			Tag.Builder builder = map.get(BlockTags.ENDERMAN_HOLDABLE.id());

			for (Block dirtTntBlock : DirTnt.BLOCK_MAP.values()) {
				builder.add(Registry.BLOCK.getId(dirtTntBlock), DirTnt.MOD_ID);
			}
		}
	}
}
