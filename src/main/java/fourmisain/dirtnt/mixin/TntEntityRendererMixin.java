package fourmisain.dirtnt.mixin;

import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.Dirtable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.entity.TntEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Allow TNT renderer to be dirty */
@Environment(EnvType.CLIENT)
@Mixin(TntEntityRenderer.class)
public abstract class TntEntityRendererMixin implements Dirtable {
	@Unique
	private boolean isDirty = false;

	public void makeDirty() {
		isDirty = true;
	}

	@ModifyArg(method = "render(Lnet/minecraft/entity/TntEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/entity/TntMinecartEntityRenderer;renderFlashingBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IZ)V"
			),
			index = 0)
	private BlockState replaceTntTexture(BlockState blockState) {
		return isDirty ? DirTnt.DIRT_TNT_BLOCK.getDefaultState() : blockState;
	}
}
