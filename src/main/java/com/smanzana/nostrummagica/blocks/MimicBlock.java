package com.smanzana.nostrummagica.blocks;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.state.BooleanProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MimicBlock extends DirectionalBlock {
	
	public static IUnlistedProperty<BlockState> NESTED_STATE = new IUnlistedProperty<BlockState>() {

		@Override
		public String getName() {
			return "Mimic::NestedState";
		}

		@Override
		public boolean isValid(BlockState value) {
			return value != null;
		}

		@Override
		public Class<BlockState> getType() {
			return BlockState.class;
		}

		@Override
		public String valueToString(BlockState value) {
			return value.toString();
		}
		
	};
	
	public static BooleanProperty UNBREAKABLE = BooleanProperty.create("unbreakable");

	public static String ID_DOOR = "mimic_door";
	public static String ID_FACADE = "mimic_facade";
	
	private static MimicBlock door = null;
	public static MimicBlock door() {
		if (door == null) {
			door = new MimicBlock(ID_DOOR, true);
		}
		
		return door;
	}
	
	private static MimicBlock facade = null;
	public static MimicBlock facade() {
		if (facade == null) {
			facade = new MimicBlock(ID_FACADE, false);
		}
		
		return facade;
	}
	
	private final boolean isDoor;
	
	public MimicBlock(String ID, boolean isDoor) {
		super(Material.GLASS);
		this.setUnlocalizedName(ID);
		this.setHardness(1.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.WOOD);
		
		this.isDoor = isDoor;
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this));
		list.add(new ItemStack(this, 1, 1));
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		return new BlockStateContainer.Builder(this).add(FACING).add(UNBREAKABLE).add(NESTED_STATE).build();
	}
	
	@Override
	public BlockState getExtendedState(BlockState state, IBlockAccess world, BlockPos pos) {
		IExtendedBlockState ext = (IExtendedBlockState) state;
		Direction face = state.get(FACING);
		
		if (face == Direction.DOWN || face == Direction.UP) {
			pos = pos.east();
		} else {
			pos = pos.down();
		}
		state = world.getBlockState(pos);
		ext = ext.with(NESTED_STATE, state.getBlock().getExtendedState(state, world, pos));
		
		return ext;
	}
	
	@Override
	public BlockState getStateFromMeta(int meta) {
		int faceMeta = meta & 0x7;
		int unbreakableMeta = (meta >> 3) & 1;
		return getDefaultState().with(FACING, Direction.values()[faceMeta]).with(UNBREAKABLE, unbreakableMeta == 1);
	}
	
	@Override
	public int getMetaFromState(BlockState state) {
		return (state.get(FACING).ordinal()) | ((state.get(UNBREAKABLE) ? 1 : 0) << 3);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public BlockState getActualState(BlockState state, IBlockAccess worldIn, BlockPos pos) {
		return super.getActualState(state, worldIn, pos);
		//return worldIn.getBlockState(pos.down()).getActualState(worldIn, pos.down());
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public float getBlockHardness(BlockState blockState, World worldIn, BlockPos pos) {
		return blockState.get(UNBREAKABLE) ? -1f : super.getBlockHardness(blockState, worldIn, pos);
	}
	
	@Override
	public boolean isSideSolid(BlockState base_state, IBlockAccess world, BlockPos pos, Direction side) {
		return false;
	}
	
//	@Override
//	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, Direction side) {
//		return false;
//	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void addCollisionBoxToList(BlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
		boolean solid = false;
		
		if (!isDoor) {
			solid = false;
		} else if (entityIn != null) {
			Direction side = state.get(FACING);
			// cant use getCenter cause it's client-side only
			//Vec3d center = entityBox.getCenter();
			Vec3d center = new Vec3d(entityBox.minX + (entityBox.maxX - entityBox.minX) * 0.5D, entityBox.minY + (entityBox.maxY - entityBox.minY) * 0.5D, entityBox.minZ + (entityBox.maxZ - entityBox.minZ) * 0.5D);
			
			// XZ motion isn't stored on the server and is handled client-side
			// Server also resets lastPos in an inconvenient way.
			final double dx;
			final double dz;
			if (entityIn instanceof PlayerEntity) {
				dx = worldIn.isRemote
						? (entityIn.getMotion().x)
						: (entityIn.posX - NostrumMagica.playerListener.getLastTickPos(entityIn).x);
				dz = worldIn.isRemote
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
			super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
		}
    }
	
	@Override
	public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
		pos = pos.down();
		state = source.getBlockState(pos);
		return state.getBlock().getExtendedState(state, source, pos).getBoundingBox(source, pos);
	}
	
	@Override
	public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
		//Direction enumfacing = Direction.getHorizontal(MathHelper.floor_double((double)(placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3).getOpposite();
		return this.getDefaultState()
				.with(FACING, Direction.getDirectionFromEntityLiving(pos, placer))
				.with(UNBREAKABLE, meta == 1);
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }
	
	@Override
	public boolean isFullCube(BlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(BlockState state) {
		return false;
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	public boolean doesSideBlockRendering(BlockState state, IBlockAccess world, BlockPos pos, Direction face) {
		return false;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean shouldSideBeRendered(BlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side) {
		return side == blockState.get(FACING);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onBlockHighlight(DrawBlockHighlightEvent event) {
		if (event.getTarget().typeOfHit == RayTraceResult.Type.BLOCK) {
			BlockPos pos = event.getTarget().getBlockPos();
			BlockState hit = event.getPlayer().world.getBlockState(pos);
			if (hit != null && hit.getBlock() == this) {
				Direction face = hit.get(FACING);
				boolean outside = false;
				
				switch (face) {
				case DOWN:
					outside = event.getPlayer().posY + event.getPlayer().eyeHeight < pos.getY();
					break;
				case EAST:
					outside = event.getPlayer().posX > pos.getX() + 1;
					break;
				case NORTH:
					outside = event.getPlayer().posZ < pos.getZ();
					break;
				case SOUTH:
					outside = event.getPlayer().posZ > pos.getZ() + 1;
					break;
				case UP:
				default:
					outside =  event.getPlayer().posY + event.getPlayer().eyeHeight > pos.getY() + 1;
					break;
				case WEST:
					outside = event.getPlayer().posX < pos.getX();
					break;
				}
				
				if (!outside) {
					event.setCanceled(true);
				}
				return;
			}
		}
	}
	
}
