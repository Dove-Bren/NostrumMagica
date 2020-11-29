package com.smanzana.nostrummagica.blocks;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.utils.Inventories;
import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * Like a hopper except it only has 1 slot and always keeps 1 item.
 * It can also be placed sideways, and will still pull from inventories it's pointing to
 * @author Skyler
 *
 */
public class ActiveHopper extends BlockContainer {
	
	public static final PropertyDirection FACING = PropertyDirection.create("facing", (facing) -> {
		return facing != null && facing != EnumFacing.UP;
	});
	
	public static final PropertyBool ENABLED = PropertyBool.create("enabled");
	
	protected static final AxisAlignedBB BASE_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.625D, 1.0D);
	protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.125D);
	protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.875D, 1.0D, 1.0D, 1.0D);
	protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.875D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
	protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.125D, 1.0D, 1.0D);
	
	public static final String ID = "active_hopper";
	
	public static final ActiveHopper instance = new ActiveHopper();
	
	public static void init() {
		GameRegistry.registerTileEntity(ActiveHopperTileEntity.class, "active_hopper_te");
	}
	
	public ActiveHopper() {
		super(Material.IRON, MapColor.STONE);
		this.setUnlocalizedName(ID);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.DOWN).withProperty(ENABLED, true));
	}
	
	public static EnumFacing GetFacing(IBlockState state) {
		return state.getValue(FACING);
	}
	
	public static boolean GetEnabled(IBlockState state) {
		return state.getValue(ENABLED);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, ENABLED, FACING);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		boolean enabled = ((meta & 0x1) == 1);
		EnumFacing facing = EnumFacing.VALUES[(meta >> 1) & 7];
		return getDefaultState().withProperty(ENABLED, enabled).withProperty(FACING, facing);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(ENABLED) ? 1 : 0) | (state.getValue(FACING).ordinal() << 1);
	}
	
	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}
	
	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
		return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new ActiveHopperTileEntity();
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
		if (facing == EnumFacing.UP) {
			facing = EnumFacing.DOWN;
		}
		return this.getDefaultState().withProperty(FACING, facing);
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return true;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return Block.FULL_BLOCK_AABB;
	}
	
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn) {
		if (GetFacing(state) == EnumFacing.DOWN) {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, BASE_AABB);
			addCollisionBoxToList(pos, entityBox, collidingBoxes, SOUTH_AABB);
			addCollisionBoxToList(pos, entityBox, collidingBoxes, NORTH_AABB);
			addCollisionBoxToList(pos, entityBox, collidingBoxes, EAST_AABB);
			addCollisionBoxToList(pos, entityBox, collidingBoxes, WEST_AABB);
		} else {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, Block.FULL_BLOCK_AABB);
		}
	}
	
	private void updateState(World worldIn, BlockPos pos, IBlockState state) {
		boolean flag = !worldIn.isBlockPowered(pos);

		if (flag != ((Boolean)state.getValue(ENABLED)).booleanValue()) {
			worldIn.setBlockState(pos, state.withProperty(ENABLED, Boolean.valueOf(flag)), 3);
		}
	}
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		updateState(worldIn, pos, state);
	}
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
		updateState(worldIn, pos, state);
	}
	
	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
		return Container.calcRedstone(worldIn.getTileEntity(pos));
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity tileentity = worldIn.getTileEntity(pos);

		if (tileentity instanceof ActiveHopperTileEntity) {
			InventoryHelper.dropInventoryItems(worldIn, pos, (ActiveHopperTileEntity)tileentity);
			worldIn.updateComparatorOutputLevel(pos, this);
		}
		
		super.breakBlock(worldIn, pos, state);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		playerIn.openGui(NostrumMagica.instance,
				NostrumGui.activeHopperID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
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
	
	public static class ActiveHopperTileEntity extends TileEntity implements IHopper, ISidedInventory, ITickable {
		
		private static final String NBT_SLOT = "slot";
		private static final String NBT_CUSTOMNAME = "custom_name";
		private static final String NBT_COOLDOWN = "cooldown";
		
		private @Nullable ItemStack slot;
		private @Nullable String customName;
		private int transferCooldown = -1;
		
		public ActiveHopperTileEntity() {
			super();
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			
			if (slot != null) {
				nbt.setTag(NBT_SLOT, slot.serializeNBT());
			}
			
			if (customName != null) {
				nbt.setString(NBT_CUSTOMNAME, customName);
			}
			
			nbt.setInteger(NBT_COOLDOWN, transferCooldown);
			
			return nbt;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			
			slot = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag(NBT_SLOT)); // nulls if empty
			customName = (nbt.hasKey(NBT_CUSTOMNAME) ? nbt.getString(NBT_CUSTOMNAME) : null);
			transferCooldown = nbt.getInteger(NBT_COOLDOWN);
		}
		
		@Override
		public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
			return !(newState.getBlock() instanceof ActiveHopper);
		}
		
		private static final int NUM_SLOTS = 1;
		private static final int[] SLOTS_ARR = new int[NUM_SLOTS];
		{
			for (int i = 0; i < NUM_SLOTS; i++) {
				SLOTS_ARR[i] = i;
			}
		}

		@Override
		public int getSizeInventory() {
			return NUM_SLOTS;
		}

		@Override
		public ItemStack getStackInSlot(int index) {
			if (index != 0) {
				return null;
			}
			
			return slot;
		}

		@Override
		public ItemStack decrStackSize(int index, int count) {
			if (index != 0 || slot == null) {
				return null;
			}
			
			ItemStack ret = slot.splitStack(count);
			if (slot.stackSize <= 0) {
				slot = null;
			}
			this.markDirty();
			return ret;
		}

		@Override
		public ItemStack removeStackFromSlot(int index) {
			if (index != 0 || slot == null) {
				return null;
			}
			
			ItemStack ret = slot;
			slot = null;
			this.markDirty();
			return ret;
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack) {
			if (index == 0) {
				slot = stack;
				this.markDirty();
			}
		}

		@Override
		public int getInventoryStackLimit() {
			return 64;
		}

		@Override
		public boolean isUseableByPlayer(EntityPlayer player) {
			return true;
		}

		@Override
		public void openInventory(EntityPlayer player) {
			;
		}

		@Override
		public void closeInventory(EntityPlayer player) {
			;
		}

		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack) {
			return true;
		}

		@Override
		public int getField(int id) {
			return 0;
		}

		@Override
		public void setField(int id, int value) {
			;
		}

		@Override
		public int getFieldCount() {
			return 0;
		}

		@Override
		public void clear() {
			if (slot != null) {
				slot = null;
				this.markDirty();
			}
		}

		@Override
		public String getName() {
			return hasCustomName() ? this.customName : "Active Hopper";
		}

		@Override
		public boolean hasCustomName() {
			return customName != null;
		}
		
		public void setCustomName(String name) {
			customName = name;
		}

		@Override
		public double getXPos() {
			return pos.getX() + .5;
		}

		@Override
		public double getYPos() {
			return pos.getY() + .5;
		}

		@Override
		public double getZPos() {
			return pos.getZ() + .5;
		}

		@Override
		public void update() {
			if (worldObj != null && !worldObj.isRemote) {
				this.transferCooldown--;
				
				if (!isOnTransferCooldown() && GetEnabled(worldObj.getBlockState(pos))) {
					this.hopperTick();
					this.setTransferCooldown(8); // same rate as vanilla
				}
			}
		}
		
		public void setTransferCooldown(int cooldown) {
			this.transferCooldown = cooldown;
		}
		
		public int getTransferCooldown() {
			return this.transferCooldown;
		}
		
		public boolean isOnTransferCooldown() {
			return this.transferCooldown > 0;
		}
		
		/**
		 * Cheap check to see if it's even possible to pull ANY item
		 * @return
		 */
		public boolean canPullAny() {
			return slot != null
					&& slot.isStackable()
					&& slot.stackSize < slot.getMaxStackSize();
		}
		
		public boolean canPull(@Nullable ItemStack stack) {
			if (stack == null) {
				return false;
			}
			
			if (!canPullAny()) {
				return false;
			}
			
			return ItemStacks.stacksMatch(stack, slot);
		}
		
		public void addStack(@Nullable ItemStack stack) {
			if (slot == null) {
				// Error condition
				slot = stack;
				this.markDirty();
			} else if (ItemStacks.stacksMatch(slot, stack)) {
				slot.stackSize = Math.min(slot.getMaxStackSize(), slot.stackSize + stack.stackSize);
				this.markDirty();
			}
		}
		
		public boolean canPush() {
			return slot != null
					&& slot.isStackable()
					&& slot.stackSize > 1; // don't push out last item stack!
		}
		
		// Called to actually pull and push items
		private void hopperTick() {
			// Pull from inventory first. // TODO consider pulling from multiple blocks 'above' ?
			if (canPullAny()) {
				pullItems();
			}
			
			// Regardless of whether we were able to or not, try and pick up any floating items
			if (canPullAny()) {
				captureNearbyItems();
			}
			
			// Then push items into wherever we're facing
			if (canPush()) {
				pushItems();
			}
		}
		
		private boolean pushItems() {
			// Inventory we want to push into is in direction FACING
			final EnumFacing direction = GetFacing(worldObj.getBlockState(pos));
			@Nullable TileEntity te = worldObj.getTileEntity(pos.offset(direction));
			
			if (te != null) {
				if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite())) {
					@Nullable IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite());
					return pushInto(handler, direction);
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
					
					return pushInto(inv, direction);
				}
			}
			
			final AxisAlignedBB captureBox = getCaptureBB(false);
			for (Entity e : worldObj.getEntitiesInAABBexcluding(null, captureBox, EntitySelectors.HAS_INVENTORY)) {
				// Vanilla uses a random entity in the list. We'll just use the first.
				return pushInto((IInventory) e, direction);
			}
			
			return false;
		}
		
		private boolean pushInto(IInventory inventory, EnumFacing direction) {
			ItemStack copyToInsert = slot.copy();
			copyToInsert.stackSize = 1;
			if (inventory instanceof ISidedInventory) {
				ISidedInventory sided = (ISidedInventory) inventory;
				for (int insertIndex : sided.getSlotsForFace(direction.getOpposite())) {
					if (!sided.canInsertItem(insertIndex, copyToInsert, direction.getOpposite())) {
						continue;
					}
					
					// Can insert. Would it fit?
					@Nullable ItemStack inSlot = sided.getStackInSlot(insertIndex);
					if (inSlot == null) {
						sided.setInventorySlotContents(insertIndex, copyToInsert);
						this.decrStackSize(0, 1);
						return true;
					}
					
					if (!inSlot.isStackable()
							|| inSlot.stackSize >= inSlot.getMaxStackSize()
							|| inSlot.stackSize >= sided.getInventoryStackLimit()) {
						continue;
					}
					
					if (!ItemStacks.stacksMatch(copyToInsert, inSlot)) {
						continue;
					}
					
					inSlot.stackSize++;
					sided.setInventorySlotContents(insertIndex, inSlot);
					this.decrStackSize(0, 1);
					return true;
				}
			} else {
				if (Inventories.addItem(inventory, copyToInsert) == null) {
					this.decrStackSize(0, 1);
					return true;
				}
				
				return false;
			}
			
			return false;
		}
		
		private boolean pushInto(IItemHandler handler, EnumFacing direction) {
			ItemStack copyToInsert = slot.copy();
			copyToInsert.stackSize = 1;
			
			@Nullable ItemStack ret = ItemHandlerHelper.insertItem(handler, copyToInsert, true);
			if (ret == null || ret.stackSize == 0) {
				ItemHandlerHelper.insertItem(handler, copyToInsert, false);
				this.decrStackSize(0, 1);
				return true;
			}
			
			return false;
		}
		
		private boolean pullItems() {
			// We want to pull from opposite(FACING)
			final EnumFacing direction = GetFacing(worldObj.getBlockState(pos)).getOpposite();
			@Nullable TileEntity te = worldObj.getTileEntity(pos.offset(direction));
			
			if (te != null) {
				if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction)) {
					@Nullable IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction);
					return pullFrom(handler, direction);
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
					return pullFrom(inv, direction);
				}
			}
			
			final AxisAlignedBB captureBox = getCaptureBB(true);
			for (Entity e : worldObj.getEntitiesInAABBexcluding(null, captureBox, EntitySelectors.HAS_INVENTORY)) {
				// Vanilla uses a random entity in the list. We'll just use the first.
				return pullFrom((IInventory) e, direction);
			}
			
			return false;
		}
		
		private boolean pullFrom(IInventory inventory, EnumFacing direction) {
			if (inventory instanceof ISidedInventory) {
				ISidedInventory sided = (ISidedInventory) inventory;
				for (int i : sided.getSlotsForFace(direction)) {
					@Nullable ItemStack inSlot = sided.getStackInSlot(i);
					if (inSlot == null) {
						continue;
					}
					
					if (!sided.canExtractItem(i, inSlot, direction)) {
						continue;
					}
					
					if (!canPull(inSlot)) {
						continue;
					}
					
					ItemStack pulled = sided.decrStackSize(i, 1);
					this.addStack(pulled);
					return true;
				}
			} else {
				for (int i = 0; i < inventory.getSizeInventory(); i++) {
					@Nullable ItemStack inSlot = inventory.getStackInSlot(i);
					if (inSlot == null) {
						continue;
					}
					
					if (!canPull(inSlot)) {
						continue;
					}
					
					ItemStack pulled = inventory.decrStackSize(i, 1);
					this.addStack(pulled);
					return true;
				}
			}
			
			return false;
		}
		
		private boolean pullFrom(IItemHandler handler, EnumFacing direction) {
			for (int i = 0; i < handler.getSlots(); i++) {
				if (handler.extractItem(i, 1, true) != null) {
					@Nullable ItemStack drawn = handler.extractItem(i, 1, false);
					this.addStack(drawn);
					return true;
				}
			}
			
			return false;
		}
		
		private boolean captureNearbyItems() {
			for (EntityItem entity : worldObj.getEntitiesWithinAABB(EntityItem.class, getCaptureBB(true))) {
				// try and pull from the stack
				@Nullable ItemStack stack = entity.getEntityItem();
				if (canPull(stack)) {
					this.addStack(stack.splitStack(1)); // reduces stack size by 1
					entity.setEntityItemStack(stack); // Try and force an update
					return true;
				}
			}
			
			return false;
		}
		
		private AxisAlignedBB getCaptureBB(boolean forPull) {
			final EnumFacing direction = GetFacing(worldObj.getBlockState(pos));
			
			if (direction == EnumFacing.DOWN) {
				// Down has different collision so do a custom box
				return new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(pos).expand(0, 1, 0);
			}
			
			final BlockPos spot = forPull ? pos.offset(direction.getOpposite()) : pos.offset(direction);
			return new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(spot);
		}
		
		@Override
		public int[] getSlotsForFace(EnumFacing side) {
			return SLOTS_ARR;
		}

		@Override
		public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing side) {
			final EnumFacing direction = GetFacing(worldObj.getBlockState(pos));
			if (side == direction) {
				// Coming in our output
				return false;
			}
			
			return canPull(itemStackIn);
		}

		@Override
		public boolean canExtractItem(int index, ItemStack stack, EnumFacing side) {
			final EnumFacing direction = GetFacing(worldObj.getBlockState(pos));
			if (side == direction.getOpposite()) {
				// pulling from our mouth?
				return false;
			}
			
			return true;
		}
		
	}
}
