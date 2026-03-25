package fourmisain.dirtnt.mixin;

import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Level.class)
public interface WorldAccessor {
	@Accessor
	static WeightedList<ExplosionParticleInfo> getDEFAULT_EXPLOSION_BLOCK_PARTICLES() {
		throw new AssertionError();
	}
}
