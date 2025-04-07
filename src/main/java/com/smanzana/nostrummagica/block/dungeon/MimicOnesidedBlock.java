package com.smanzana.nostrummagica.block.dungeon;

import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.autodungeons.block.IDirectionalBlock;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.DelayLoadedMimicBlockTileEntity;
import com.smanzana.nostrummagica.tile.NostrumTileEntities;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MimicOnesidedBlock extends MimicBlock implements IDirectionalBlock {
	
	public static final String ID_DOOR = "mimic_door";
	public static final String ID_DOOR_UNBREAKABLE = "mimic_door_unbreakable";
	public static final String ID_FACADE = "mimic_facade";
	public static final String ID_FACADE_UNBREAKABLE = "mimic_facade_unbreakable";
	
	public static final DirectionProperty FACING = IDirectionalBlock.FACING;
	
	private final boolean isDoor;
	private final boolean isUnbreakable;
	
	public MimicOnesidedBlock(boolean isDoor, boolean isUnbreakable) {
		super(Block.Properties.of(Material.GLASS)
				.strength(isUnbreakable ? -1.0F : 1.0f, isUnbreakable ? 3600000.8F : 1.0f)
				.dynamicShape()
				);
		
		this.isDoor = isDoor;
		this.isUnbreakable = isUnbreakable;
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public boolean isUnbreakable() {
		return this.isUnbreakable;
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	protected Vec3 getEntEffectiveMotion(Entity entityIn) {
		// XZ motion isn't stored on the server and is handled client-side
		// Server also resets lastPos in an inconvenient way.
		final double dx;
		final double dz;
		if (entityIn instanceof Player) {
			dx = entityIn.level.isClientSide()
					? (entityIn.getDeltaMovement().x)
					: (entityIn.getX() - NostrumMagica.playerListener.getLastTickPos(entityIn).x);
			dz = entityIn.level.isClientSide()
					? (entityIn.getDeltaMovement().z)
					: (entityIn.getZ() - NostrumMagica.playerListener.getLastTickPos(entityIn).z);
		} else {
			dx = entityIn.getDeltaMovement().x;
			dz = entityIn.getDeltaMovement().z;
		}
		
//				final double dx = worldIn.isRemote
//						? (entityIn.getMotion().x)
//						: (entityIn.getPosX() - NostrumMagica.playerListener.getLastTickPos(entityIn).x);
//				final double dz = worldIn.isRemote
//						? (entityIn.getMotion().z)
//						: (entityIn.getPosZ() - NostrumMagica.playerListener.getLastTickPos(entityIn).z);
		
		return new Vec3(dx, entityIn.getDeltaMovement().y, dz);
	}
	
	protected Vec3 getEntEffectivePos(Entity entityIn, @Nullable Vec3 motion) {
		//final AxisAlignedBB entityBox = entityIn.getCollisionBoundingBox();
		
		// cant use getCenter cause it's client-side only
		//Vector3d center = entityBox.getCenter();
		//Vector3d center = new Vector3d(entityBox.minX + (entityBox.maxX - entityBox.minX) * 0.5D, entityBox.minY + (entityBox.maxY - entityBox.minY) * 0.5D, entityBox.minZ + (entityBox.maxZ - entityBox.minZ) * 0.5D);
		Vec3 center = entityIn.position();
		
		if (motion == null) {
			motion = getEntEffectiveMotion(entityIn);
		}
		
		
		// Offset center back to old position to prevent sneaking back inside!
		center = center.add(-motion.x, 0, -motion.z);
		return center;
	}
	
	// 120286 130 673212
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		// Render/particle code calls with dummy sometimes and crashes if you return an empty cube
		if (context != CollisionContext.empty() && context instanceof EntityCollisionContext) {
			final @Nullable Entity entity = ((EntityCollisionContext) context).getEntity().orElse(null);
			if (entity == null || !(entity instanceof Player) || !((Player) entity).isCreative()) {
				// Hide if looking at from the right way
				final Vec3 center = getEntEffectivePos(entity, null);
				final Direction side = state.getValue(FACING);
				final boolean blocks;
				
				switch (side) {
				case DOWN:
					blocks = center.y < pos.getY();
					break;
				case EAST:
					blocks = center.x > pos.getX() + 1;
					break;
				case NORTH:
					blocks = center.z < pos.getZ();
					break;
				case SOUTH:
					blocks = center.z > pos.getZ();
					break;
				case UP:
				default:
					blocks = center.y > pos.getY();
					break;
				case WEST:
					blocks = center.x < pos.getX();
					break;
				}
				
				if (!blocks) {
					return Shapes.empty();
				}
			}
		}
		
		return super.getShape(state, worldIn, pos, context);
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		boolean solid = false;
		
		if (!isDoor) {
			solid = false;
		} else if (context instanceof EntityCollisionContext) {
			final @Nullable Entity entityIn = ((EntityCollisionContext) context).getEntity().orElse(null);
			if (entityIn != null) {
				final Vec3 motion = this.getEntEffectiveMotion(entityIn);
				final Vec3 center = this.getEntEffectivePos(entityIn, motion);
				Direction side = state.getValue(FACING);
				
				switch (side) {
				case DOWN:
					solid = center.y < pos.getY() && motion.y >= 0;
					break;
				case EAST:
					solid = center.x > pos.getX() + 1 && motion.x <= 0;
					break;
				case NORTH:
					solid = center.z < pos.getZ() && motion.z >= 0;
					break;
				case SOUTH:
					solid = center.z > pos.getZ() + 1 && motion.z <= 0;
					break;
				case UP:
				default:
					solid = center.y > pos.getY() + 1 && motion.y <= 0;
					break;
				case WEST:
					solid = center.x < pos.getX() && motion.x >= 0;
					break;
				}
			}
		}
		
		if (solid) {
			return Shapes.block();
		} else {
			return Shapes.empty();
		}
    }
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		//Direction enumfacing = Direction.getHorizontal(MathHelper.floor_double((double)(placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3).getOpposite();
		return this.defaultBlockState()
				.setValue(FACING,context.getNearestLookingDirection().getOpposite())
				;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
		return side != state.getValue(FACING);
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onBlockHighlight(DrawSelectionEvent.HighlightBlock event) {
		if (event.getTarget().getType() == HitResult.Type.BLOCK) {
			BlockPos pos = new BlockPos(event.getTarget().getLocation());
			BlockState hit = event.getInfo().getEntity().level.getBlockState(pos);
			if (hit != null && hit.getBlock() == this) {
				Direction face = hit.getValue(FACING);
				boolean outside = false;
				
				switch (face) {
				case DOWN:
					outside = event.getInfo().getPosition().y < pos.getY();
					break;
				case EAST:
					outside = event.getInfo().getPosition().x > pos.getX() + 1;
					break;
				case NORTH:
					outside = event.getInfo().getPosition().z < pos.getZ();
					break;
				case SOUTH:
					outside = event.getInfo().getPosition().z > pos.getZ() + 1;
					break;
				case UP:
				default:
					outside =  event.getInfo().getPosition().y > pos.getY() + 1;
					break;
				case WEST:
					outside = event.getInfo().getPosition().x < pos.getX();
					break;
				}
				
				if (!outside) {
					event.setCanceled(true);
				}
				return;
			}
		}
	}
	
	@Override
	protected boolean shouldRefreshFromNeighbor(BlockState state, Level worldIn, BlockPos myPos, BlockPos fromPos) {
		// Mimic blocks mimic what's below them, unless placed up/down in which case they go north
		Direction mimicFacing = state.getValue(FACING);
		final BlockPos samplePos = (mimicFacing.getAxis() == Axis.Y
				? myPos.north()
				: myPos.below());
		return samplePos.equals(fromPos);
	}
	
	@Override
	public @Nonnull BlockState getMimickedState(BlockState mimicBlockState, Level world, BlockPos myPos) {
		// Mimic blocks mimic what's below them, unless placed up/down in which case they go north
		Direction mimicFacing = mimicBlockState.getValue(FACING);
		final Function<BlockPos, BlockPos> moveCursor;
		if (mimicFacing.getAxis() == Axis.Y) {
			moveCursor = (pos) -> pos.north();
		} else {
			moveCursor = (pos) -> pos.below();
		}
		
		BlockPos pos = moveCursor.apply(myPos);
		BlockState state = world.getBlockState(pos);
		
		// If it's another mimic block, look below it.
		// I want to just say "getMirrorState" but that doesn't always work? logic puzzle.
		while (state.getBlock() instanceof MimicBlock) {
			pos = moveCursor.apply(pos);
			if (pos.getY() <= 0) {
				state = Blocks.AIR.defaultBlockState();
			} else {
				state = world.getBlockState(pos);
			}
		}
		
		if (state.isAir()) {
			state = null;
		}
		return state;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		if (state.getValue(FACING).getAxis() == Axis.Y) {
			return new DelayLoadedMimicBlockTileEntity(pos, state);
		}
		
		return super.newBlockEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return TickableBlockEntity.createTickerHelper(type, NostrumTileEntities.DelayedMimicBlockTileEntityType);
	}
}
