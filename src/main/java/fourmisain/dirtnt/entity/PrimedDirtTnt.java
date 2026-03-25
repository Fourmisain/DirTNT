package fourmisain.dirtnt.entity;

import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.Dirtable;
import fourmisain.dirtnt.block.DirtTntBlock;
import fourmisain.dirtnt.mixin.LevelAccessor;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PrimedDirtTnt extends PrimedTnt {
	public static final int RADIUS = 3;

	public PrimedDirtTnt(Identifier dirtType, EntityType<? extends PrimedTnt> entityType, Level level) {
		super(entityType, level);
		((Dirtable) this).makeDirty(dirtType);
	}

	public PrimedDirtTnt(Identifier dirtType, Level level, double x, double y, double z) {
		this(dirtType, DirTnt.ENTITY_TYPE_MAP.get(dirtType), level);
		this.setPos(x, y, z);
		double angle = level.random.nextDouble() * 2*Math.PI;
		this.setDeltaMovement(-Math.sin(angle) * 0.02, 0.2, -Math.cos(angle) * 0.02);
		this.setFuse(80);
		this.xo = x;
		this.yo = y;
		this.zo = z;
	}

	public static void createDirtExplosion(Identifier dirtType, Entity entity, Level level) {
		if (level.isClientSide()) return;

		// emitGameEvent seems to mainly be used for the Sculk Sensor
		level.gameEvent(entity, GameEvent.EXPLODE, new Vec3(entity.getX(), entity.getY(), entity.getZ()));

		// center explosion at the entity
		BlockPos centerBlockPos = entity.blockPosition();
		Vec3 centerVec = entity.getBoundingBox().getCenter();

		MutableBlockPos targetBlockPos = new MutableBlockPos();

		Optional<Block> maybeDirtBlock = BuiltInRegistries.BLOCK.getOptional(dirtType);
		if (maybeDirtBlock.isEmpty()) throw new AssertionError("Dirt TNT entity exists but block is not registered!");

		Block dirtBlock = maybeDirtBlock.get();

		int[] blockCount = new int[1];

		// for every 'target' block within a distance of RADIUS
		for (int x = -RADIUS; x <= RADIUS; x++) {
			for (int y = -RADIUS; y <= RADIUS; y++) {
				for (int z = -RADIUS; z <= RADIUS; z++) {
					targetBlockPos.set(centerVec.x + x, centerVec.y + y, centerVec.z + z);
					Vec3 targetVec = Vec3.atCenterOf(targetBlockPos);

					if (targetBlockPos.closerThan(centerBlockPos, RADIUS + 1)) {
						ClipContext context = new ClipContext(centerVec, targetVec,
							ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);

						// walk through all blocks from the explosion center to the target block
						BlockGetter.traverseBlocks(context.getFrom(), context.getTo(), context, (ctx, pos) -> {
							BlockState state = level.getBlockState(pos);

							// skip over/trace through dirt
							if (state.is(dirtBlock)) {
								return null;
							}

							igniteDirtTnt(level, pos);

							// test the block's shape for a collision
							VoxelShape blockShape = ctx.getBlockShape(state, level, pos);
							BlockHitResult hitResult = level.clipWithInteractionOverride(ctx.getFrom(), ctx.getTo(), pos, blockShape, state);

							// if nothing was hit
							if (hitResult == null) {
								// place dirt if possible
								if (state.canBeReplaced()) {
									level.setBlockAndUpdate(pos, dirtBlock.defaultBlockState());
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

		for (var player : ((ServerLevel) level).players()) {
			if (player.distanceToSqr(centerVec) < 64 * 64) {
				player.connection.send(new ClientboundExplodePacket(centerVec, RADIUS + 1, blockCount[0], Optional.empty(),
					ParticleTypes.EXPLOSION, SoundEvents.GENERIC_EXPLODE, LevelAccessor.getDEFAULT_EXPLOSION_BLOCK_PARTICLES()));
			}
		}
	}

	public static void igniteDirtTnt(Level level, BlockPos pos) {
		if (level.getBlockState(pos).getBlock() instanceof DirtTntBlock dirtTntBlock) {
			Identifier dirtType = ((Dirtable) dirtTntBlock).getDirtType();

			level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

			PrimedTnt tnt = new PrimedDirtTnt(dirtType, level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			int t = tnt.getFuse();
			tnt.setFuse(level.random.nextInt(t / 4) + t / 8);
			level.addFreshEntity(tnt);
		}
	}
}
