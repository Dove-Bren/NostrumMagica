package com.smanzana.nostrummagica.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.utils.Inventories;
import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * It's an item pipe!
 * @author Skyler
 *
 */
public class ItemDuct extends BlockContainer {
	
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
	protected static final AxisAlignedBB SIDE_AABBs[] = {DOWN_AABB, UP_AABB, NORTH_AABB, SOUTH_AABB, WEST_AABB, EAST_AABB}; // EnumFacing 'index' is index
	
	// Large static pre-made set of selection AABBs.
	// This is an array like fence has where index is bit field of different options.
	// these are:
	// D U N S W E (the EnumFacing 'index's)
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
	
	public static void init() {
		GameRegistry.registerTileEntity(ItemDuctTileEntity.class, "item_duct_te");
	}
	
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
	
	public static boolean GetFacingActive(IBlockState state, EnumFacing face) {
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
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState();
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new ItemDuctTileEntity();
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
		return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, stack);
	}
	
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return state
				.withProperty(NORTH, canConnect(worldIn, pos, EnumFacing.NORTH))
				.withProperty(SOUTH, canConnect(worldIn, pos, EnumFacing.SOUTH))
				.withProperty(EAST, canConnect(worldIn, pos, EnumFacing.EAST))
				.withProperty(WEST, canConnect(worldIn, pos, EnumFacing.WEST))
				.withProperty(UP, canConnect(worldIn, pos, EnumFacing.UP))
				.withProperty(DOWN, canConnect(worldIn, pos, EnumFacing.DOWN));
	}
	
	protected boolean canConnect(IBlockAccess world, BlockPos centerPos, EnumFacing direction) {
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
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		final IBlockState actualState = state.getActualState(source, pos);
		
		int index = 0;
		for (EnumFacing face : EnumFacing.VALUES) {
			if (GetFacingActive(actualState, face)) {
				index |= (1 << (5 - face.getIndex()));
			}
		}
		
		return SELECTION_AABS[index];
	}
	
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn) {
		addCollisionBoxToList(pos, entityBox, collidingBoxes, BASE_AABB);
		for (EnumFacing dir : EnumFacing.VALUES) {
			if (GetFacingActive(state.getActualState(worldIn, pos), dir)) {
				addCollisionBoxToList(pos, entityBox, collidingBoxes, SIDE_AABBs[dir.getIndex()]);
			}
		}
	}
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
	}
	
	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
		final TileEntity tileentity = worldIn.getTileEntity(pos);
		final int output;

		if (tileentity instanceof ItemDuctTileEntity) {
			// 16 slots but have to adapt to 0-15 (with 1-15 if there are ANY stacks)
			ItemDuctTileEntity ent = (ItemDuctTileEntity) tileentity;
			if (ent.itemQueue.isEmpty()) {
				output = 0;
			} else {
				float frac = (float) ent.itemQueue.size() / (float) ItemDuctTileEntity.MAX_STACKS;
				output = 1 + MathHelper.floor_float(frac * 14);
			}
		} else {
			output = 0;
		}
		
		return output;
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
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
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
//		playerIn.openGui(NostrumMagica.instance,
//				NostrumGui.activeHopperID, worldIn,
//				pos.getX(), pos.getY(), pos.getZ());
		
//		return true;
		
		return false;
	}
	
	@Override
	public boolean isFullyOpaque(IBlockState state) {
		return true; // Copying vanilla
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}
	
	public static class ItemDuctTileEntity extends TileEntity implements /* IInventory, */ ITickable {
		
		private static final class ItemEntry {
			
			private static final String NBT_TICK = "tick";
			private static final String NBT_ITEM = "item";
			private static final String NBT_DIRECTION = "dir";
			
			public final long addTick;
			public final ItemStack stack;
			public final EnumFacing inputDirection;
			
			public ItemEntry(long addTick, ItemStack stack, EnumFacing inputDirection) {
				this.addTick = addTick;
				this.stack = stack;
				this.inputDirection = inputDirection;
			}
			
			public NBTTagCompound toNBT() {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setLong(NBT_TICK, addTick);
				tag.setTag(NBT_ITEM, stack.serializeNBT());
				tag.setInteger(NBT_DIRECTION, inputDirection.getIndex());
				return tag;
			}
			
			public static final ItemEntry fromNBT(NBTTagCompound tag) {
				final long tick = tag.getLong(NBT_TICK);
				final ItemStack stack = ItemStack.loadItemStackFromNBT(tag.getCompoundTag(NBT_ITEM));
				final EnumFacing dir = EnumFacing.VALUES[tag.getInteger(NBT_DIRECTION)];
				return new ItemEntry(tick, stack, dir);
			}
		}
		
		private static final class SidedItemHandler implements IItemHandler {

			public final EnumFacing side;
			private final ItemDuctTileEntity entity;
			
			public SidedItemHandler(ItemDuctTileEntity entity, EnumFacing side) {
				this.entity = entity;
				this.side = side;
			}
			
			@Override
			public int getSlots() {
				return 1 + entity.itemQueue.size();
			}

			@Override
			public ItemStack getStackInSlot(int slot) {
				// Treat slot 0 as special always-available slot for insertion
				if (slot == 0) {
					return null;
				}
				
				if (slot >= entity.itemQueue.size()) {
					return null;
				}
				
				return entity.itemQueue.get(slot).stack;
			}

			@Override
			public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
				if (slot != 0) {
					return stack;
				}
				
				return entity.insertItem(stack, this.side, simulate);
			}

			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate) {
				if (slot != 0) {
					return null;
				}
				
				return entity.extractItem(amount, this.side, simulate);
			}
			
		}
		
		private static final String NBT_SORTED = "sorted_list";
		private static final String NBT_TICKS = "ticks";
		
		private static final int MAX_STACKS = 16;
		private static final long TICKS_LATENCY = 8; // how long it takes stacks to get through the pipe in ticks
		
		private final List<ItemEntry> itemQueue; // List of items being moved through. Naturally sorted by input (and therefore output) time
		private long ticks;
		private final SidedItemHandler[] handlers;
		
		public ItemDuctTileEntity() {
			super();
			itemQueue = new LinkedList<>();
			ticks = 0;
			handlers = new SidedItemHandler[] { // D U N S W E
				new SidedItemHandler(this, EnumFacing.DOWN),
				new SidedItemHandler(this, EnumFacing.UP),
				new SidedItemHandler(this, EnumFacing.NORTH),
				new SidedItemHandler(this, EnumFacing.SOUTH),
				new SidedItemHandler(this, EnumFacing.WEST),
				new SidedItemHandler(this, EnumFacing.EAST),
			};
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			
			NBTTagList list = new NBTTagList();
			for (ItemEntry entry : itemQueue) {
				list.appendTag(entry.toNBT());
			}
			nbt.setTag(NBT_SORTED, list);
			
			nbt.setLong(NBT_TICKS, ticks);
			
			return nbt;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			
			itemQueue.clear();
			NBTTagList list = nbt.getTagList(NBT_SORTED, NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound tag = list.getCompoundTagAt(i);
				itemQueue.add(ItemEntry.fromNBT(tag));
			}
			
			ticks= nbt.getLong(NBT_TICKS);
		}
		
		@Override
		public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
			return !(newState.getBlock() instanceof ItemDuct);
		}
		
		/**
		 * Returns the real list of all items in this duct
		 * @return
		 */
		public List<ItemStack> getAllItems() {
			List<ItemStack> list = new ArrayList<>();
			
			for (ItemEntry entry : itemQueue) {
				list.add(entry.stack);
			}
			
			return list;
		}
		
		public void clear() {
			this.itemQueue.clear();
			this.markDirty();
		}
		
		protected ItemEntry addItem(@Nonnull ItemStack stack, EnumFacing dir) {
			ItemEntry entry = new ItemEntry(ticks, stack, dir);
			this.itemQueue.add(entry);
			this.markDirty();
			return entry;
		}
		
		public boolean isFull() {
			return itemQueue.size() >= MAX_STACKS;
		}
		
		protected boolean tryAdd(@Nullable ItemStack stack, EnumFacing dir) {
			if (stack == null) {
				return false;
			}
			
			if (isFull()) {
				// Drop on floor
				// TODO make it actually drop on the face that it's being added?
				InventoryHelper.spawnItemStack(worldObj, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, stack);
				return false;
			}
			
			addItem(stack, dir);
			return true;
		}
		
		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			// Maybe this should check if we're connected?
			return (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
				return (T) handlers[facing.getIndex()];
			}
			return super.getCapability(capability, facing);
		}
		
		public ItemStack insertItem(ItemStack stack, EnumFacing side, boolean simulate) {
			if (!simulate) {
				tryAdd(stack, side);
			}
			
			return null; // Always 'take' it
		}

		public ItemStack extractItem(int amount, EnumFacing side, boolean simulate) {
			// No extraction at all!
			return null;
		}
		
		@Override
		public void update() {
			if (worldObj != null && !worldObj.isRemote) {
				this.ticks++;
				
				// Check any items and see if it's time they move on.
				// Queue is sorted with earliest inserted at head. So as soon as we find false, stop.
				while (true) {
					if (!pushItem()) {
						break;
					}
				}
			}
		}
		
		/**
		 * Attemps to push the first item in the queue to where it needs to go.
		 * @return true if item was remove from inventory
		 */
		protected boolean pushItem() {
			if (itemQueue.isEmpty()) {
				return false;
			}
			
			final ItemEntry entry = itemQueue.get(0);
			final long duration = ticks - entry.addTick;
			if (duration < 0 || duration > TICKS_LATENCY) {
				// Item is good to push!
				pushItem(entry);
				itemQueue.remove(0);
				this.markDirty();
				return true;
			}
			
			return false;
		}
		
		private void pushItem(ItemEntry entry) {
			// Insert into targetted inventory.
			// Attempt to keep the item going in the same direction. If that doesn't work,
			// pick a random one!
			
			// Special case: 0 connections or 1 connection but it's where the item came from.
			// If that happens, pop it on the floor
			
			ItemStack stack = entry.stack;
			
			// First, try direction it wants
			stack = attemptPush(stack, entry.inputDirection.getOpposite());
			
			if (stack != null) {
				// Push in random directions
				List<EnumFacing> rand = Lists.newArrayList(EnumFacing.VALUES);
				Collections.shuffle(rand);
				for (EnumFacing dir : rand) {
					// Don't go backwards and skip forwards since we just tried that
					if (dir == entry.inputDirection || dir == entry.inputDirection.getOpposite()) {
						continue;
					}
					
					stack = attemptPush(stack, dir);
							
					if (stack == null) {
						break;
					}
				}
			}
			
			if (stack != null) {
				// Throw on the floor!
				// TODO make it actually drop on the face that we would be coming out of
				InventoryHelper.spawnItemStack(worldObj, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, stack);
			}
		}
		
		/**
		 * Tries to push the provided item stack in the given direction.
		 * Pushes as many of the items in the stack as possible.
		 * Returns what couldn't be pushed.
		 * @param stack
		 * @param direction
		 * @return
		 */
		private @Nullable ItemStack attemptPush(ItemStack stack, EnumFacing direction) {
			@Nullable TileEntity te = worldObj.getTileEntity(pos.offset(direction));
			
			if (te != null) {
				if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite())) {
					@Nullable IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite());
					return pushInto(stack, handler, direction);
				}
				
				if (te instanceof IInventory) {
					
					IInventory inv = (IInventory) te;
					
					// Special cast for stupid chests :P
					if (te instanceof TileEntityChest) {
						IBlockState state = worldObj.getBlockState(pos.offset(direction));
						if (state != null && state.getBlock() instanceof BlockChest) {
							inv = ((BlockChest)state.getBlock()).getContainer(worldObj, pos.offset(direction), true);
						}
					}
					
					return pushInto(stack, inv, direction);
				}
			}
			
			return stack;
		}
		
		private @Nullable ItemStack pushInto(ItemStack stack, IInventory inventory, EnumFacing direction) {
			if (inventory instanceof ISidedInventory) {
				ISidedInventory sided = (ISidedInventory) inventory;
				for (int insertIndex : sided.getSlotsForFace(direction.getOpposite())) {
					if (!sided.canInsertItem(insertIndex, stack, direction.getOpposite())) {
						continue;
					}
					
					// Can insert. Would it fit?
					@Nullable ItemStack inSlot = sided.getStackInSlot(insertIndex);
					final int maxStack = (stack.isStackable() ? Math.min(stack.getMaxStackSize(), sided.getInventoryStackLimit()) : 1);
					final int taken;
					if (inSlot == null) {
						taken = Math.min(maxStack, stack.stackSize);
						inSlot = stack.splitStack(maxStack);
					} else if (ItemStacks.stacksMatch(stack, inSlot)) {
						taken = Math.min(maxStack - inSlot.stackSize, stack.stackSize);
						if (taken > 0) {
							stack.splitStack(taken);
							inSlot.stackSize += taken;
						}
					} else {
						taken = 0;
					}
					
					if (taken > 0) {
						sided.setInventorySlotContents(insertIndex, inSlot);
						if (stack.stackSize <= 0) {
							stack = null;
							break;
						}
					}
				}
			} else {
				stack = Inventories.addItem(inventory, stack);
			}
			
			return stack;
		}
		
		private @Nullable ItemStack pushInto(ItemStack stack, IItemHandler handler, EnumFacing direction) {
			// TODO safe to always run with false?
			return ItemHandlerHelper.insertItem(handler, stack, false);
		}
	}
}
