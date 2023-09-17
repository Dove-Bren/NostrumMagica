package com.smanzana.nostrummagica.blocks;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.ActiveHopperTileEntity;
import com.smanzana.nostrummagica.client.gui.NostrumGui;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockStateContainer;

/**
 * Like a hopper except it only has 1 slot and always keeps 1 item.
 * It can also be placed sideways, and will still pull from inventories it's pointing to
 * @author Skyler
 *
 */
public class ActiveHopper extends ContainerBlock {
	
	public static final PropertyDirection FACING = PropertyDirection.create("facing", (facing) -> {
		return facing != null && facing != Direction.UP;
	});
	
	public static final PropertyBool ENABLED = PropertyBool.create("enabled");
	
	protected static final AxisAlignedBB BASE_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.625D, 1.0D);
	protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.125D);
	protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.875D, 1.0D, 1.0D, 1.0D);
	protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.875D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
	protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.125D, 1.0D, 1.0D);
	
	public static final String ID = "active_hopper";
	
	public static final ActiveHopper instance = new ActiveHopper();
	
	public ActiveHopper() {
		super(Material.IRON, MapColor.STONE);
		this.setHardness(3.0F);
		this.setHarvestLevel("pickaxe", 2);
		this.setResistance(8.0F);
		this.setSoundType(SoundType.METAL);
		this.setUnlocalizedName(ID);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, Direction.DOWN).withProperty(ENABLED, true));
	}
	
	public static Direction GetFacing(BlockState state) {
		return state.getValue(FACING);
	}
	
	public static boolean GetEnabled(BlockState state) {
		if (state != null && state.getBlock() instanceof ActiveHopper) {
			return state.getValue(ENABLED);
		}
		return false;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, ENABLED, FACING);
	}
	
	@Override
	public BlockState getStateFromMeta(int meta) {
		boolean enabled = ((meta & 0x1) == 1);
		Direction facing = Direction.VALUES[(meta >> 1) & 7];
		return getDefaultState().withProperty(ENABLED, enabled).withProperty(FACING, facing);
	}
	
	@Override
	public int getMetaFromState(BlockState state) {
		return (state.getValue(ENABLED) ? 1 : 0) | (state.getValue(FACING).ordinal() << 1);
	}
	
	@Override
	public BlockState withRotation(BlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}
	
	@Override
	public BlockState withMirror(BlockState state, Mirror mirrorIn) {
		return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new ActiveHopperTileEntity();
	}
	
	@Override
	public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer, Hand hand) {
		if (facing == Direction.UP) {
			facing = Direction.DOWN;
		}
		return this.getDefaultState().withProperty(FACING, facing);
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return true;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
		return Block.FULL_BLOCK_AABB;
	}
	
	@Override
	public void addCollisionBoxToList(BlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
		if (GetFacing(state) == Direction.DOWN) {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, BASE_AABB);
			addCollisionBoxToList(pos, entityBox, collidingBoxes, SOUTH_AABB);
			addCollisionBoxToList(pos, entityBox, collidingBoxes, NORTH_AABB);
			addCollisionBoxToList(pos, entityBox, collidingBoxes, EAST_AABB);
			addCollisionBoxToList(pos, entityBox, collidingBoxes, WEST_AABB);
		} else {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, Block.FULL_BLOCK_AABB);
		}
	}
	
	private void updateState(World worldIn, BlockPos pos, BlockState state) {
		boolean flag = !worldIn.isBlockPowered(pos);

		if (flag != ((Boolean)state.getValue(ENABLED)).booleanValue()) {
			worldIn.setBlockState(pos, state.withProperty(ENABLED, Boolean.valueOf(flag)), 3);
		}
	}
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, BlockState state) {
		updateState(worldIn, pos, state);
	}
	
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		updateState(worldIn, pos, state);
	}
	
	@Override
	public boolean hasComparatorInputOverride(BlockState state) {
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
		return Container.calcRedstone(worldIn.getTileEntity(pos));
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, BlockState state) {
		TileEntity tileentity = worldIn.getTileEntity(pos);

		if (tileentity instanceof ActiveHopperTileEntity) {
			InventoryHelper.dropInventoryItems(worldIn, pos, (ActiveHopperTileEntity)tileentity);
			worldIn.updateComparatorOutputLevel(pos, this);
		}
		
		super.breakBlock(worldIn, pos, state);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
		playerIn.openGui(NostrumMagica.instance,
				NostrumGui.activeHopperID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(BlockState state) {
		return EnumBlockRenderType.MODEL;
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
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}
	
	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, BlockState state, BlockPos pos, Direction face) {
		return face == Direction.UP ? BlockFaceShape.BOWL : BlockFaceShape.UNDEFINED;
	}
}
