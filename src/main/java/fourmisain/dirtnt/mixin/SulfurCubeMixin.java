package fourmisain.dirtnt.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import fourmisain.dirtnt.Dirtable;
import fourmisain.dirtnt.block.DirtTntBlock;
import fourmisain.dirtnt.entity.PrimedDirtTnt;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SulfurCube.class)
public abstract class SulfurCubeMixin extends AbstractCubeMob {
	protected SulfurCubeMixin(EntityType<? extends AbstractCubeMob> type, Level level) {
		super(type, level);
	}

	@WrapWithCondition(method = "tickFuse", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)V"))
	public boolean primeDirtTnt(ServerLevel instance, Entity entity, DamageSource damageSource, ExplosionDamageCalculator damageCalculator, double x, double y, double z, float r, boolean fire, Level.ExplosionInteraction interactionType) {
		ItemStack itemStack = getItemBySlot(EquipmentSlot.BODY);

		if (itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof DirtTntBlock dirtTntBlock) {
			Identifier dirtType = ((Dirtable) dirtTntBlock).getDirtType();
			boolean doExplode = level() instanceof ServerLevel serverLevel && serverLevel.getGameRules().get(GameRules.MOB_GRIEFING);

			PrimedDirtTnt.createDirtExplosion(dirtType, this, this.level(), doExplode);
			return false;
		}

		return true;
	}
}
