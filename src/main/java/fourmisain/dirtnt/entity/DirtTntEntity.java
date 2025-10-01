package fourmisain.dirtnt.entity;

import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.Dirtable;
import fourmisain.dirtnt.block.DirtTntBlock;
import fourmisain.dirtnt.mixin.WorldAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.Optional;

public class DirtTntEntity extends TntEntity {
	public static final int RADIUS = 3;

	public DirtTntEntity(Identifier dirtType, EntityType<? extends TntEntity> entityType, World world) {
		super(entityType, world);
		((Dirtable) this).makeDirty(dirtType);
	}

	public DirtTntEntity(Identifier dirtType, World world, double x, double y, double z) {
		this(dirtType, DirTnt.ENTITY_TYPE_MAP.get(dirtType), world);
		this.setPosition(x, y, z);
		double angle = world.random.nextDouble() * 2*Math.PI;
		this.setVelocity(-Math.sin(angle) * 0.02, 0.2, -Math.cos(angle) * 0.02);
		this.setFuse(80);
		this.lastX = x;
		this.lastY = y;
		this.lastZ = z;
	}

	public static void createDirtExplosion(Identifier dirtType, Entity entity, World world) {
		if (world.isClient()) return;

		// emitGameEvent seems to mainly be used for the Sculk Sensor
		world.emitGameEvent(entity, GameEvent.EXPLODE, new Vec3d(entity.getX(), entity.getY(), entity.getZ()));

		// center explosion at the entity
		BlockPos centerBlockPos = entity.getBlockPos();
		Vec3d centerVec = entity.getBoundingBox().getCenter();

		BlockPos.Mutable targetBlockPos = new BlockPos.Mutable();

		Optional<Block> maybeDirtBlock = Registries.BLOCK.getOptionalValue(dirtType);
		if (maybeDirtBlock.isEmpty()) throw new AssertionError("Dirt TNT entity exists but block is not registered!");

		Block dirtBlock = maybeDirtBlock.get();

		int[] blockCount = new int[1];

		// for every 'target' block within a distance of RADIUS
		for (int x = -RADIUS; x <= RADIUS; x++) {
			for (int y = -RADIUS; y <= RADIUS; y++) {
				for (int z = -RADIUS; z <= RADIUS; z++) {
					targetBlockPos.set(centerVec.x + x, centerVec.y + y, centerVec.z + z);
					Vec3d targetVec = Vec3d.ofCenter(targetBlockPos);

					if (targetBlockPos.isWithinDistance(centerBlockPos, RADIUS + 1)) {
						RaycastContext context = new RaycastContext(centerVec, targetVec,
							RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity);

						// walk through all blocks from the explosion center to the target block
						BlockView.raycast(context.getStart(), context.getEnd(), context, (ctx, pos) -> {
							BlockState state = world.getBlockState(pos);

							// skip over/trace through dirt
							if (state.isOf(dirtBlock)) {
								return null;
							}

							igniteDirtTnt(world, pos);

							// test the block's shape for a collision
							VoxelShape blockShape = ctx.getBlockShape(state, world, pos);
							BlockHitResult hitResult = world.raycastBlock(ctx.getStart(), ctx.getEnd(), pos, blockShape, state);

							// if nothing was hit
							if (hitResult == null) {
								// place dirt if possible
								if (state.isReplaceable()) {
									world.setBlockState(pos, dirtBlock.getDefaultState());
									blockCount[0]++;
								}

								// and continue
								return null;
							}

							// else abort
							return state;
						}, (ctx) -> null);
					}
				}
			}
		}

		for (var player : ((ServerWorld) world).getPlayers()) {
			if (player.squaredDistanceTo(centerVec) < 64 * 64) {
				player.networkHandler.sendPacket(new ExplosionS2CPacket(centerVec, RADIUS + 1, blockCount[0], Optional.empty(),
					ParticleTypes.EXPLOSION, SoundEvents.ENTITY_GENERIC_EXPLODE, WorldAccessor.getEXPLOSION_BLOCK_PARTICLES()));
			}
		}
	}

	public static void igniteDirtTnt(World world, BlockPos pos) {
		if (world.getBlockState(pos).getBlock() instanceof DirtTntBlock dirtTntBlock) {
			Identifier dirtType = ((Dirtable) dirtTntBlock).getDirtType();

			world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);

			TntEntity tnt = new DirtTntEntity(dirtType, world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			int t = tnt.getFuse();
			tnt.setFuse(world.random.nextInt(t / 4) + t / 8);
			world.spawnEntity(tnt);
		}
	}
}
