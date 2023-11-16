package com.smanzana.nostrummagica.blocks;

import java.util.function.Function;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MimicOnesidedBlock extends MimicBlock {
	
	public static final String ID_DOOR = "mimic_door";
	public static final String ID_DOOR_UNBREAKABLE = "mimic_door_unbreakable";
	public static final String ID_FACADE = "mimic_facade";
	public static final String ID_FACADE_UNBREAKABLE = "mimic_facade_unbreakable";
	
	public static final DirectionProperty FACING = DirectionalBlock.FACING;
	
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
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		boolean solid = false;
		
		if (!isDoor) {
			solid = false;
		} else if (context.getEntity() != null) {
			final Entity entityIn = context.getEntity();
			//final AxisAlignedBB entityBox = entityIn.getCollisionBoundingBox();
			Direction side = state.get(FACING);
			// cant use getCenter cause it's client-side only
			//Vec3d center = entityBox.getCenter();
			//Vec3d center = new Vec3d(entityBox.minX + (entityBox.maxX - entityBox.minX) * 0.5D, entityBox.minY + (entityBox.maxY - entityBox.minY) * 0.5D, entityBox.minZ + (entityBox.maxZ - entityBox.minZ) * 0.5D);
			Vec3d center = entityIn.getPositionVector();
			
			// XZ motion isn't stored on the server and is handled client-side
			// Server also resets lastPos in an inconvenient way.
			final double dx;
			final double dz;
			if (entityIn instanceof PlayerEntity) {
				dx = entityIn.world.isRemote()
						? (entityIn.getMotion().x)
						: (entityIn.posX - NostrumMagica.playerListener.getLastTickPos(entityIn).x);
				dz = entityIn.world.isRemote()
						? (entityIn.getMotion().z)
						: (entityIn.posZ - NostrumMagica.playerListener.getLastTickPos(entityIn).z);
			} else {
				dx = entityIn.getMotion().x;
				dz = entityIn.getMotion().z;
			}
			
//			final double dx = worldIn.isRemote
//					? (entityIn.getMotion().x)
//					: (entityIn.posX - NostrumMagica.playerListener.getLastTickPos(entityIn).x);
//			final double dz = worldIn.isRemote
//					? (entityIn.getMotion().z)
//					: (entityIn.posZ - NostrumMagica.playerListener.getLastTickPos(entityIn).z);
			
			// Offset center back to old position to prevent sneaking back inside!
			center = center.add(-dx, 0, -dz);
			
			switch (side) {
			case DOWN:
				solid = center.y < pos.getY() && entityIn.getMotion().y >= 0;
				break;
			case EAST:
				solid = center.x > pos.getX() + 1 && dx <= 0;
				break;
			case NORTH:
				solid = center.z < pos.getZ() && dz >= 0;
				break;
			case SOUTH:
				solid = center.z > pos.getZ() + 1 && dz <= 0;
				break;
			case UP:
			default:
				solid = center.y > pos.getY() + 1 && entityIn.getMotion().y <= 0;
				break;
			case WEST:
				solid = center.x < pos.getX() && dx >= 0;
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
	public void onBlockHighlight(DrawBlockHighlightEvent event) {
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
		
		return state;
	}
}
