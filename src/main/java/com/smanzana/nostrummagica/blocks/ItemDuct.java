package com.smanzana.nostrummagica.blocks;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.ItemDuctTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraftforge.items.CapabilityItemHandler;

/**
 * It's an item pipe!
 * @author Skyler
 *
 */
public class ItemDuct extends ContainerBlock {
	
	public static final PropertyBool NORTH = PropertyBool.create("north");
	public static final PropertyBool SOUTH = PropertyBool.create("south");
	public static final PropertyBool EAST = PropertyBool.create("east");
	public static final PropertyBool WEST = PropertyBool.create("west");
	public static final PropertyBool UP = PropertyBool.create("up");
	public static final PropertyBool DOWN = PropertyBool.create("down");
	
	private static final double INNER_RADIUS = (3.0 / 16.0);
	protected static final AxisAlignedBB BASE_AABB = new AxisAlignedBB(.5 - INNER_RADIUS, .5 - INNER_RADIUS, .5 - INNER_RADIUS, .5 + INNER_RADIUS, .5 + INNER_RADIUS, .5 + INNER_RADIUS);
	protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(.5 - INNER_RADIUS, .5 - INNER_RADIUS, 0, .5 + INNER_RADIUS, .5 + INNER_RADIUS, .5 - INNER_RADIUS);
	protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(.5 - INNER_RADIUS, .5 - INNER_RADIUS, .5 + INNER_RADIUS, .5 + INNER_RADIUS, .5 + INNER_RADIUS, .1);
	protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0, .5 - INNER_RADIUS, .5 - INNER_RADIUS, .5 - INNER_RADIUS, .5 + INNER_RADIUS, .5 + INNER_RADIUS);
	protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(.5 + INNER_RADIUS, .5 - INNER_RADIUS, .5 - INNER_RADIUS, 1, .5 + INNER_RADIUS, .5 + INNER_RADIUS);
	protected static final AxisAlignedBB UP_AABB = new AxisAlignedBB(.5 - INNER_RADIUS, .5 - INNER_RADIUS, .5 + INNER_RADIUS, .5 + INNER_RADIUS, .5 + INNER_RADIUS, 1);
	protected static final AxisAlignedBB DOWN_AABB = new AxisAlignedBB(.5 + INNER_RADIUS, .5 - INNER_RADIUS, 0, 1, .5 + INNER_RADIUS, .5 - INNER_RADIUS);
	protected static final AxisAlignedBB SIDE_AABBs[] = {DOWN_AABB, UP_AABB, NORTH_AABB, SOUTH_AABB, WEST_AABB, EAST_AABB}; // Direction 'index' is index
	
	// Large static pre-made set of selection AABBs.
	// This is an array like fence has where index is bit field of different options.
	// these are:
	// D U N S W E (the Direction 'index's)
	// so the BB for a duct with north and east is [001001] (9)
	protected static final AxisAlignedBB SELECTION_AABS[] = {
			new AxisAlignedBB(0.3125, 0.3125, 0.3125, 0.6875, 0.6875, 0.6875),
			new AxisAlignedBB(0.3125, 0.3125, 0.3125, 1.0, 0.6875, 0.6875),
			new AxisAlignedBB(0.0, 0.3125, 0.3125, 0.6875, 0.6875, 0.6875),
			new AxisAlignedBB(0.0, 0.3125, 0.3125, 1.0, 0.6875, 0.6875),
			new AxisAlignedBB(0.3125, 0.3125, 0.3125, 0.6875, 0.6875, 1.0),
			new AxisAlignedBB(0.3125, 0.3125, 0.3125, 1.0, 0.6875, 1.0),
			new AxisAlignedBB(0.0, 0.3125, 0.3125, 0.6875, 0.6875, 1.0),
			new AxisAlignedBB(0.0, 0.3125, 0.3125, 1.0, 0.6875, 1.0),
			new AxisAlignedBB(0.3125, 0.3125, 0.0, 0.6875, 0.6875, 0.6875),
			new AxisAlignedBB(0.3125, 0.3125, 0.0, 1.0, 0.6875, 0.6875),
			new AxisAlignedBB(0.0, 0.3125, 0.0, 0.6875, 0.6875, 0.6875),
			new AxisAlignedBB(0.0, 0.3125, 0.0, 1.0, 0.6875, 0.6875),
			new AxisAlignedBB(0.3125, 0.3125, 0.0, 0.6875, 0.6875, 1.0),
			new AxisAlignedBB(0.3125, 0.3125, 0.0, 1.0, 0.6875, 1.0),
			new AxisAlignedBB(0.0, 0.3125, 0.0, 0.6875, 0.6875, 1.0),
			new AxisAlignedBB(0.0, 0.3125, 0.0, 1.0, 0.6875, 1.0),
			new AxisAlignedBB(0.3125, 0.3125, 0.3125, 0.6875, 1.0, 0.6875),
			new AxisAlignedBB(0.3125, 0.3125, 0.3125, 1.0, 1.0, 0.6875),
			new AxisAlignedBB(0.0, 0.3125, 0.3125, 0.6875, 1.0, 0.6875),
			new AxisAlignedBB(0.0, 0.3125, 0.3125, 1.0, 1.0, 0.6875),
			new AxisAlignedBB(0.3125, 0.3125, 0.3125, 0.6875, 1.0, 1.0),
			new AxisAlignedBB(0.3125, 0.3125, 0.3125, 1.0, 1.0, 1.0),
			new AxisAlignedBB(0.0, 0.3125, 0.3125, 0.6875, 1.0, 1.0),
			new AxisAlignedBB(0.0, 0.3125, 0.3125, 1.0, 1.0, 1.0),
			new AxisAlignedBB(0.3125, 0.3125, 0.0, 0.6875, 1.0, 0.6875),
			new AxisAlignedBB(0.3125, 0.3125, 0.0, 1.0, 1.0, 0.6875),
			new AxisAlignedBB(0.0, 0.3125, 0.0, 0.6875, 1.0, 0.6875),
			new AxisAlignedBB(0.0, 0.3125, 0.0, 1.0, 1.0, 0.6875),
			new AxisAlignedBB(0.3125, 0.3125, 0.0, 0.6875, 1.0, 1.0),
			new AxisAlignedBB(0.3125, 0.3125, 0.0, 1.0, 1.0, 1.0),
			new AxisAlignedBB(0.0, 0.3125, 0.0, 0.6875, 1.0, 1.0),
			new AxisAlignedBB(0.0, 0.3125, 0.0, 1.0, 1.0, 1.0),
			new AxisAlignedBB(0.3125, 0.0, 0.3125, 0.6875, 0.6875, 0.6875),
			new AxisAlignedBB(0.3125, 0.0, 0.3125, 1.0, 0.6875, 0.6875),
			new AxisAlignedBB(0.0, 0.0, 0.3125, 0.6875, 0.6875, 0.6875),
			new AxisAlignedBB(0.0, 0.0, 0.3125, 1.0, 0.6875, 0.6875),
			new AxisAlignedBB(0.3125, 0.0, 0.3125, 0.6875, 0.6875, 1.0),
			new AxisAlignedBB(0.3125, 0.0, 0.3125, 1.0, 0.6875, 1.0),
			new AxisAlignedBB(0.0, 0.0, 0.3125, 0.6875, 0.6875, 1.0),
			new AxisAlignedBB(0.0, 0.0, 0.3125, 1.0, 0.6875, 1.0),
			new AxisAlignedBB(0.3125, 0.0, 0.0, 0.6875, 0.6875, 0.6875),
			new AxisAlignedBB(0.3125, 0.0, 0.0, 1.0, 0.6875, 0.6875),
			new AxisAlignedBB(0.0, 0.0, 0.0, 0.6875, 0.6875, 0.6875),
			new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.6875, 0.6875),
			new AxisAlignedBB(0.3125, 0.0, 0.0, 0.6875, 0.6875, 1.0),
			new AxisAlignedBB(0.3125, 0.0, 0.0, 1.0, 0.6875, 1.0),
			new AxisAlignedBB(0.0, 0.0, 0.0, 0.6875, 0.6875, 1.0),
			new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.6875, 1.0),
			new AxisAlignedBB(0.3125, 0.0, 0.3125, 0.6875, 1.0, 0.6875),
			new AxisAlignedBB(0.3125, 0.0, 0.3125, 1.0, 1.0, 0.6875),
			new AxisAlignedBB(0.0, 0.0, 0.3125, 0.6875, 1.0, 0.6875),
			new AxisAlignedBB(0.0, 0.0, 0.3125, 1.0, 1.0, 0.6875),
			new AxisAlignedBB(0.3125, 0.0, 0.3125, 0.6875, 1.0, 1.0),
			new AxisAlignedBB(0.3125, 0.0, 0.3125, 1.0, 1.0, 1.0),
			new AxisAlignedBB(0.0, 0.0, 0.3125, 0.6875, 1.0, 1.0),
			new AxisAlignedBB(0.0, 0.0, 0.3125, 1.0, 1.0, 1.0),
			new AxisAlignedBB(0.3125, 0.0, 0.0, 0.6875, 1.0, 0.6875),
			new AxisAlignedBB(0.3125, 0.0, 0.0, 1.0, 1.0, 0.6875),
			new AxisAlignedBB(0.0, 0.0, 0.0, 0.6875, 1.0, 0.6875),
			new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 0.6875),
			new AxisAlignedBB(0.3125, 0.0, 0.0, 0.6875, 1.0, 1.0),
			new AxisAlignedBB(0.3125, 0.0, 0.0, 1.0, 1.0, 1.0),
			new AxisAlignedBB(0.0, 0.0, 0.0, 0.6875, 1.0, 1.0),
			new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0),
	};
	
	
	public static final String ID = "item_duct";
	
	public static final ItemDuct instance = new ItemDuct();
	
	public ItemDuct() {
		super(Material.IRON, MapColor.STONE);
		this.setUnlocalizedName(ID);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setDefaultState(this.blockState.getBaseState()
				.withProperty(NORTH, false)
				.withProperty(SOUTH, false)
				.withProperty(EAST, false)
				.withProperty(WEST, false)
				.withProperty(UP, false)
				.withProperty(DOWN, false));
	}
	
	public static boolean GetFacingActive(BlockState state, Direction face) {
		switch (face) {
		case DOWN:
			return state.getValue(DOWN);
		case EAST:
			return state.getValue(EAST);
		case NORTH:
		default:
			return state.getValue(NORTH);
		case SOUTH:
			return state.getValue(SOUTH);
		case UP:
			return state.getValue(UP);
		case WEST:
			return state.getValue(WEST);
		}
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, NORTH, SOUTH, EAST, WEST, UP, DOWN);
	}
	
	@Override
	public BlockState getStateFromMeta(int meta) {
		return this.getDefaultState();
	}
	
	@Override
	public int getMetaFromState(BlockState state) {
		return 0;
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new ItemDuctTileEntity();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
		return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);
	}
	
	@Override
	public BlockState getActualState(BlockState state, IBlockAccess worldIn, BlockPos pos) {
		return state
				.withProperty(NORTH, canConnect(worldIn, pos, Direction.NORTH))
				.withProperty(SOUTH, canConnect(worldIn, pos, Direction.SOUTH))
				.withProperty(EAST, canConnect(worldIn, pos, Direction.EAST))
				.withProperty(WEST, canConnect(worldIn, pos, Direction.WEST))
				.withProperty(UP, canConnect(worldIn, pos, Direction.UP))
				.withProperty(DOWN, canConnect(worldIn, pos, Direction.DOWN));
	}
	
	protected boolean canConnect(IBlockAccess world, BlockPos centerPos, Direction direction) {
		// Should be whether there's another pipe or anything else with an inventory?
		final BlockPos atPos = centerPos.offset(direction);
		@Nullable TileEntity te = world.getTileEntity(atPos);
		if (te != null) {
			if (te instanceof IInventory) {
				return true;
			}
			if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite())) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return true;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
		final BlockState actualState = state.getActualState(source, pos);
		
		int index = 0;
		for (Direction face : Direction.VALUES) {
			if (GetFacingActive(actualState, face)) {
				index |= (1 << (5 - face.getIndex()));
			}
		}
		
		return SELECTION_AABS[index];
	}
	
	@Override
	public void addCollisionBoxToList(BlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
		addCollisionBoxToList(pos, entityBox, collidingBoxes, BASE_AABB);
		for (Direction dir : Direction.VALUES) {
			if (GetFacingActive(state.getActualState(worldIn, pos), dir)) {
				addCollisionBoxToList(pos, entityBox, collidingBoxes, SIDE_AABBs[dir.getIndex()]);
			}
		}
	}
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, BlockState state) {
		super.onBlockAdded(worldIn, pos, state);
	}
	
	@Override
	public boolean hasComparatorInputOverride(BlockState state) {
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
		final TileEntity tileentity = worldIn.getTileEntity(pos);
		final int output;

		if (tileentity instanceof ItemDuctTileEntity) {
			// 16 slots but have to adapt to 0-15 (with 1-15 if there are ANY stacks)
			ItemDuctTileEntity ent = (ItemDuctTileEntity) tileentity;
			float frac = ent.getFilledPercent();
			if (frac <= 0.0f) {
				output = 0;
			} else {
				output = 1 + MathHelper.floor(frac * 14);
			}
		} else {
			output = 0;
		}
		
		return output;
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, BlockState state) {
		TileEntity tileentity = worldIn.getTileEntity(pos);

		if (tileentity instanceof ItemDuctTileEntity) {
			for (ItemStack stack : ((ItemDuctTileEntity) tileentity).getAllItems()) {
				InventoryHelper.spawnItemStack(worldIn, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, stack);
			}
			worldIn.updateComparatorOutputLevel(pos, this);
		}
		
		super.breakBlock(worldIn, pos, state);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
//		playerIn.openGui(NostrumMagica.instance,
//				NostrumGui.activeHopperID, worldIn,
//				pos.getX(), pos.getY(), pos.getZ());
		
//		return true;
		
		return false;
	}
	
//	@Override
//	public boolean isFullyOpaque(BlockState state) {
//		return true; // Copying vanilla
//	}
	
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
}
