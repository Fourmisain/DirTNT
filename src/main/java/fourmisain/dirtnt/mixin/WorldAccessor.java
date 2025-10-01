package fourmisain.dirtnt.mixin;

import net.minecraft.particle.BlockParticleEffect;
import net.minecraft.util.collection.Pool;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(World.class)
public interface WorldAccessor {
	@Accessor
	static Pool<BlockParticleEffect> getEXPLOSION_BLOCK_PARTICLES() {
		throw new AssertionError();
	}
}
