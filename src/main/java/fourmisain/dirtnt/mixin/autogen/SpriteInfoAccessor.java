package fourmisain.dirtnt.mixin.autogen;

import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Sprite.Info.class)
public interface SpriteInfoAccessor {
	@Accessor
	AnimationResourceMetadata getAnimationData();
}
