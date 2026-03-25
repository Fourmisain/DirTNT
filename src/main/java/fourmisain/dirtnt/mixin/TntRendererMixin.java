package fourmisain.dirtnt.mixin;

import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.Dirtable;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Allow TNT renderer to be dirty */
@Mixin(TntRenderer.class)
public abstract class TntRendererMixin implements Dirtable {
	@Unique
	private Identifier dirtType = null;

	@Override
	public void makeDirty(Identifier dirtType) {
		this.dirtType = dirtType;
	}

	@Override
	public Identifier getDirtType() {
		return this.dirtType;
	}

	@ModifyArg(
		method = "submit(Lnet/minecraft/client/renderer/entity/state/TntRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/entity/TntMinecartRenderer;submitWhiteSolidBlock(Lnet/minecraft/world/level/block/state/BlockState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IZI)V"
		),
		index = 0
	)
	private BlockState replaceTntTexture(BlockState blockState) {
		return isDirty() ? DirTnt.BLOCK_MAP.get(dirtType).defaultBlockState() : blockState;
	}
}
