package fourmisain.dirtnt.mixin;

import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MissingSprite.class)
public interface MissingSpriteAccessor {
	@Invoker
	static NativeImage invokeCreateImage(int width, int height) {
		throw new AssertionError();
	}
}
