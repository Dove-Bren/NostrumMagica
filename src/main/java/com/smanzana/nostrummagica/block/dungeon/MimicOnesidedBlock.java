package com.smanzana.nostrummagica.block.dungeon;

import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.autodungeons.block.IDirectionalBlock;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.DelayLoadedMimicBlockTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawHighlightEvent;
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
		super(Block.Properties.create(Material.GLASS)
				.hardnessAndResistance(isUnbreakable ? -1.0F : 1.0f, isUnbreakable ? 3600000.8F : 1.0f)
				.variableOpacity()
				);
		
		this.isDoor = isDoor;
		this.isUnbreakable = isUnbreakable;
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public boolean isUnbreakable() {
		return this.isUnbreakable;
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	protected Vector3d getEntEffectiveMotion(Entity entityIn) {
		// XZ motion isn't stored on the server and is handled client-side
		// Server also resets lastPos in an inconvenient way.
		final double dx;
		final double dz;
		if (entityIn instanceof PlayerEntity) {
			dx = entityIn.world.isRemote()
					? (entityIn.getMotion().x)
					: (entityIn.getPosX() - NostrumMagica.playerListener.getLastTickPos(entityIn).x);
			dz = entityIn.world.isRemote()
					? (entityIn.getMotion().z)
					: (entityIn.getPosZ() - NostrumMagica.playerListener.getLastTickPos(entityIn).z);
		} else {
			dx = entityIn.getMotion().x;
			dz = entityIn.getMotion().z;
		}
		
//				final double dx = worldIn.isRemote
//						? (entityIn.getMotion().x)
//						: (entityIn.getPosX() - NostrumMagica.playerListener.getLastTickPos(entityIn).x);
//				final double dz = worldIn.isRemote
//						? (entityIn.getMotion().z)
//						: (entityIn.getPosZ() - NostrumMagica.playerListener.getLastTickPos(entityIn).z);
		
		return new Vector3d(dx, entityIn.getMotion().y, dz);
	}
	
	protected Vector3d getEntEffectivePos(Entity entityIn, @Nullable Vector3d motion) {
		//final AxisAlignedBB entityBox = entityIn.getCollisionBoundingBox();
		
		// cant use getCenter cause it's client-side only
		//Vector3d center = entityBox.getCenter();
		//Vector3d center = new Vector3d(entityBox.minX + (entityBox.maxX - entityBox.minX) * 0.5D, entityBox.minY + (entityBox.maxY - entityBox.minY) * 0.5D, entityBox.minZ + (entityBox.maxZ - entityBox.minZ) * 0.5D);
		Vector3d center = entityIn.getPositionVec();
		
		if (motion == null) {
			motion = getEntEffectiveMotion(entityIn);
		}
		
		
		// Offset center back to old position to prevent sneaking back inside!
		center = center.add(-motion.x, 0, -motion.z);
		return center;
	}
	
	// 120286 130 673212
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		// Render/particle code calls with dummy sometimes and crashes if you return an empty cube
		if (context != ISelectionContext.dummy()) {
			if (context.getEntity() == null || !(context.getEntity() instanceof PlayerEntity) || !((PlayerEntity) context.getEntity()).isCreative()) {
				// Hide if looking at from the right way
				final Vector3d center = getEntEffectivePos(context.getEntity(), null);
				final Direction side = state.get(FACING);
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
					return VoxelShapes.empty();
				}
			}
		}
		
		return super.getShape(state, worldIn, pos, context);
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		boolean solid = false;
		
		if (!isDoor) {
			solid = false;
		} else if (context.getEntity() != null) {
			final Entity entityIn = context.getEntity();
			final Vector3d motion = this.getEntEffectiveMotion(entityIn);
			final Vector3d center = this.getEntEffectivePos(entityIn, motion);
			Direction side = state.get(FACING);
			
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
		
		if (solid) {
			return VoxelShapes.fullCube();
		} else {
			return VoxelShapes.empty();
		}
    }
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		//Direction enumfacing = Direction.getHorizontal(MathHelper.floor_double((double)(placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3).getOpposite();
		return this.getDefaultState()
				.with(FACING,context.getNearestLookingDirection().getOpposite())
				;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
		return side != state.get(FACING);
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onBlockHighlight(DrawHighlightEvent.HighlightBlock event) {
		if (event.getTarget().getType() == RayTraceResult.Type.BLOCK) {
			BlockPos pos = new BlockPos(event.getTarget().getHitVec());
			BlockState hit = event.getInfo().getRenderViewEntity().world.getBlockState(pos);
			if (hit != null && hit.getBlock() == this) {
				Direction face = hit.get(FACING);
				boolean outside = false;
				
				switch (face) {
				case DOWN:
					outside = event.getInfo().getProjectedView().y < pos.getY();
					break;
				case EAST:
					outside = event.getInfo().getProjectedView().x > pos.getX() + 1;
					break;
				case NORTH:
					outside = event.getInfo().getProjectedView().z < pos.getZ();
					break;
				case SOUTH:
					outside = event.getInfo().getProjectedView().z > pos.getZ() + 1;
					break;
				case UP:
				default:
					outside =  event.getInfo().getProjectedView().y > pos.getY() + 1;
					break;
				case WEST:
					outside = event.getInfo().getProjectedView().x < pos.getX();
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
	protected boolean shouldRefreshFromNeighbor(BlockState state, World worldIn, BlockPos myPos, BlockPos fromPos) {
		// Mimic blocks mimic what's below them, unless placed up/down in which case they go north
		Direction mimicFacing = state.get(FACING);
		final BlockPos samplePos = (mimicFacing.getAxis() == Axis.Y
				? myPos.north()
				: myPos.down());
		return samplePos.equals(fromPos);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public @Nonnull BlockState getMimickedState(BlockState mimicBlockState, World world, BlockPos myPos) {
		// Mimic blocks mimic what's below them, unless placed up/down in which case they go north
		Direction mimicFacing = mimicBlockState.get(FACING);
		final Function<BlockPos, BlockPos> moveCursor;
		if (mimicFacing.getAxis() == Axis.Y) {
			moveCursor = (pos) -> pos.north();
		} else {
			moveCursor = (pos) -> pos.down();
		}
		
		BlockPos pos = moveCursor.apply(myPos);
		BlockState state = world.getBlockState(pos);
		
		// If it's another mimic block, look below it.
		// I want to just say "getMirrorState" but that doesn't always work? logic puzzle.
		while (state.getBlock() instanceof MimicBlock) {
			pos = moveCursor.apply(pos);
			if (pos.getY() <= 0) {
				state = Blocks.AIR.getDefaultState();
			} else {
				state = world.getBlockState(pos);
			}
		}
		
		if (state.isAir(world, pos)) {
			state = null;
		}
		return state;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		if (state.get(FACING).getAxis() == Axis.Y) {
			return new DelayLoadedMimicBlockTileEntity();
		}
		
		return super.createTileEntity(state, world);
	}
}
