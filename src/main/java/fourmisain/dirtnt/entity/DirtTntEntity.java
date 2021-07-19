package fourmisain.dirtnt.entity;

import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.Dirtable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class DirtTntEntity extends TntEntity {
	public static final int RADIUS = 4;

	public DirtTntEntity(EntityType<? extends TntEntity> entityType, World world) {
		super(entityType, world);
		((Dirtable) this).makeDirty();
	}

	public DirtTntEntity(World world, double x, double y, double z) {
		this(DirTnt.DIRT_TNT_ENTITY_TYPE, world);
		this.setPosition(x, y, z);
		double angle = world.random.nextDouble() * 2*Math.PI;
		this.setVelocity(-Math.sin(angle) * 0.02, 0.2, -Math.cos(angle) * 0.02);
		this.setFuse(80);
		this.prevX = x;
		this.prevY = y;
		this.prevZ = z;
	}

	public static void createDirtExplosion(Entity entity, World world, BlockPos blockPos) {
		if (!world.isClient) {
			world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.2F) * 0.7F);
		}

		Vec3d blockCenter = Vec3d.ofCenter(blockPos);

		// for every 'target' block within a distance of RADIUS
		for (int x = -RADIUS; x <= RADIUS; x++) {
			for (int y = -RADIUS; y <= RADIUS; y++) {
				for (int z = -RADIUS; z <= RADIUS; z++) {
					BlockPos targetBlockPos = blockPos.add(x, y, z);

					if (targetBlockPos.isWithinDistance(blockPos, RADIUS)) {
						RaycastContext context = new RaycastContext(blockCenter, Vec3d.ofCenter(targetBlockPos),
							RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, entity);

						// walk through all blocks from the explosion center to the target block
						BlockView.raycast(context, (ctx, pos) -> {
							BlockState state = world.getBlockState(pos);

							// place dirt if possible
							if (state.getMaterial().isReplaceable()) {
								world.setBlockState(pos, Blocks.DIRT.getDefaultState());
								return null;
							}

							// walk through dirt
							if (state.isOf(Blocks.DIRT))
								return null;

							// else abort
							return state;
						}, (ctx) -> null);
					}
				}
			}
		}
	}
}
