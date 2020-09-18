package com.smanzana.nostrummagica.blocks;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MimicBlock extends BlockDirectional {
	
	public static IUnlistedProperty<IBlockState> NESTED_STATE = new IUnlistedProperty<IBlockState>() {

		@Override
		public String getName() {
			return "Mimic::NestedState";
		}

		@Override
		public boolean isValid(IBlockState value) {
			return value != null;
		}

		@Override
		public Class<IBlockState> getType() {
			return IBlockState.class;
		}

		@Override
		public String valueToString(IBlockState value) {
			return value.toString();
		}
		
	};
	
	public static PropertyBool UNBREAKABLE = PropertyBool.create("unbreakable");

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
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer.Builder(this).add(FACING).add(UNBREAKABLE).add(NESTED_STATE).build();
	}
	
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		IExtendedBlockState ext = (IExtendedBlockState) state;
		EnumFacing face = state.getValue(FACING);
		
		if (face == EnumFacing.DOWN) {
			pos = pos.east();
		} else {
			pos = pos.down();
		}
		state = world.getBlockState(pos);
		ext = ext.withProperty(NESTED_STATE, state.getBlock().getExtendedState(state, world, pos));
		
		return ext;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		int faceMeta = meta & 0x7;
		int unbreakableMeta = (meta >> 3) & 1;
		return getDefaultState().withProperty(FACING, EnumFacing.values()[faceMeta]).withProperty(UNBREAKABLE, unbreakableMeta == 1);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(FACING).ordinal()) | ((state.getValue(UNBREAKABLE) ? 1 : 0) << 3);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return super.getActualState(state, worldIn, pos);
		//return worldIn.getBlockState(pos.down()).getActualState(worldIn, pos.down());
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
		return blockState.getValue(UNBREAKABLE) ? -1f : super.getBlockHardness(blockState, worldIn, pos);
	}
	
	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn) {
		boolean solid = false;
		
		if (!isDoor) {
			solid = false;
		} else if (entityIn != null) {
			EnumFacing side = state.getValue(FACING);
			Vec3d center = entityBox.getCenter();
			//center = new Vec3d(center.xCoord, entityBox.minY, center.zCoord);
			
			// XZ motion isn't stored on the server and is handled client-side
			double dx = entityIn.posX - entityIn.lastTickPosX;
			double dz = entityIn.posZ - entityIn.lastTickPosZ;
			
			// Offset center back to old position to prevent sneaking back inside!
			center = center.addVector(-dx, 0, -dz);
			
			switch (side) {
			case DOWN:
				solid = center.yCoord < pos.getY() && entityIn.motionY >= 0;
				break;
			case EAST:
				solid = center.xCoord > pos.getX() + 1 && dx <= 0;
				break;
			case NORTH:
				solid = center.zCoord < pos.getZ() && dz >= 0;
				break;
			case SOUTH:
				solid = center.zCoord > pos.getZ() + 1 && dz <= 0;
				break;
			case UP:
			default:
				solid = center.yCoord > pos.getY() + 1 && entityIn.motionY <= 0;
				break;
			case WEST:
				solid = center.xCoord < pos.getX() && dx >= 0;
				break;
			}
		}
		
		if (solid) {
			super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn);
		}
    }
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		pos = pos.down();
		state = source.getBlockState(pos);
		return state.getBlock().getExtendedState(state, source, pos).getBoundingBox(source, pos);
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
		//EnumFacing enumfacing = EnumFacing.getHorizontal(MathHelper.floor_double((double)(placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3).getOpposite();
		return this.getDefaultState()
				.withProperty(FACING, BlockPistonBase.getFacingFromEntity(pos, placer))
				.withProperty(UNBREAKABLE, meta == 1);
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }
	
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return side == blockState.getValue(FACING);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@SubscribeEvent
	public void onBlockHighlight(DrawBlockHighlightEvent event) {
		if (event.getTarget().typeOfHit == RayTraceResult.Type.BLOCK) {
			BlockPos pos = event.getTarget().getBlockPos();
			IBlockState hit = event.getPlayer().worldObj.getBlockState(pos);
			if (hit != null && hit.getBlock() == this) {
				EnumFacing face = hit.getValue(FACING);
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
