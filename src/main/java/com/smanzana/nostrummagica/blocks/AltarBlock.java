package com.smanzana.nostrummagica.blocks;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.AltarItem;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTome;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class AltarBlock extends Block implements ITileEntityProvider {
	
	public static final String ID = "altar_block";
	protected static final AxisAlignedBB ALTAR_AABB = new AxisAlignedBB(0.3D, 0.0D, 0.3D, 0.7D, 0.8D, 0.7D);
	
	private static AltarBlock instance = null;
	public static AltarBlock instance() {
		if (instance == null)
			instance = new AltarBlock();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(AltarTileEntity.class,
				new ResourceLocation(NostrumMagica.MODID, "nostrum_altar_te"));
	}
	
	
	public AltarBlock() {
		super(Material.ROCK, MapColor.OBSIDIAN);
		this.setUnlocalizedName(ID);
		this.setHardness(3.5f);
		this.setResistance(10.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		
		this.setLightOpacity(1);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return ALTAR_AABB;
	}
	
//	@Override
//	public boolean isVisuallyOpaque() {
//		return false;
//	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
        return false;
    }
	
//	@Override
//	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
//		return true;
//	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return false;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		AltarTileEntity ent = new AltarTileEntity();
		
		return ent;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
		
		EntityItem item = new EntityItem(world,
				pos.getX() + .5,
				pos.getY() + .5,
				pos.getZ() + .5,
				new ItemStack(AltarItem.instance()));
		world.spawnEntity(item);
		
        world.removeTileEntity(pos);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int eventID, int eventParam) {
		super.eventReceived(state, worldIn, pos, eventID, eventParam);
		TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(eventID, eventParam);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null)
			return false;
		
		@Nonnull
		ItemStack heldItem = playerIn.getHeldItem(hand);
		
		AltarTileEntity altar = (AltarTileEntity) te;
		if (altar.getItem() == ItemStack.EMPTY) {
			// Accepting items
			if (heldItem != ItemStack.EMPTY) {
				altar.setItem(heldItem.splitStack(1));
				return true;
			} else
				return false;
		} else {
			// Has an item
			if (heldItem == ItemStack.EMPTY) {
				if (!playerIn.inventory.addItemStackToInventory(altar.getItem())) {
					worldIn.spawnEntity(
							new EntityItem(worldIn,
									pos.getX() + .5, pos.getY() + 1.2, pos.getZ() + .5,
									altar.getItem())
							);
				}
				altar.setItem(ItemStack.EMPTY);
				return true;
			} else if (heldItem.getItem() instanceof SpellScroll) {
				if (heldItem.getMetadata() != 2)
					return false;
				
				// meta 2 means an awakened scroll. If we have a tome, BIND!!!!!
				ItemStack tome = altar.getItem();
				if (!(tome.getItem() instanceof SpellTome))
					return false;
				
				if (SpellTome.startBinding(playerIn, tome, heldItem, false)) {
					heldItem.splitStack(1);
					return true;
				} else {
					return false;
				}
			} else
				return false;
		}
		
	}
	
	public static class AltarTileEntity extends TileEntity implements ISidedInventory {
		
		@Nonnull
		private ItemStack stack;
		
		public AltarTileEntity() {
			stack = ItemStack.EMPTY;
		}
		
		public @Nonnull ItemStack getItem() {
			return stack;
		}
		
		public void setItem(@Nonnull ItemStack stack) {
			this.stack = stack;
			dirty();
		}
		
		private static final String NBT_ITEM = "item";
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			
			if (stack != ItemStack.EMPTY) {
				NBTTagCompound tag = new NBTTagCompound();
				tag = stack.writeToNBT(tag);
				nbt.setTag(NBT_ITEM, tag);
			}
			
			return nbt;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			
			if (nbt == null)
				return;
				
			if (!nbt.hasKey(NBT_ITEM, NBT.TAG_COMPOUND)) {
				stack = ItemStack.EMPTY;
			} else {
				NBTTagCompound tag = nbt.getCompoundTag(NBT_ITEM);
				stack = new ItemStack(tag);
			}
		}
		
		@Override
		public SPacketUpdateTileEntity getUpdatePacket() {
			return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
		}

		@Override
		public NBTTagCompound getUpdateTag() {
			return this.writeToNBT(new NBTTagCompound());
		}
		
		@Override
		public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
			super.onDataPacket(net, pkt);
			handleUpdateTag(pkt.getNbtCompound());
		}
		
		private void dirty() {
			world.markBlockRangeForRenderUpdate(pos, pos);
			world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
			world.scheduleBlockUpdate(pos, this.getBlockType(),0,0);
			markDirty();
		}

		@Override
		public int getSizeInventory() {
			return 1;
		}

		@Override
		public @Nonnull ItemStack getStackInSlot(int index) {
			if (index > 0)
				return ItemStack.EMPTY;
			return this.stack;
		}

		@Override
		public @Nonnull ItemStack decrStackSize(int index, int count) {
			if (index > 0)
				return ItemStack.EMPTY;
			ItemStack ret = this.stack.splitStack(count);
			if (this.stack.getCount() == 0)
				this.stack = ItemStack.EMPTY;
			this.dirty();
			return ret;
		}

		@Override
		public @Nonnull ItemStack removeStackFromSlot(int index) {
			if (index > 0)
				return ItemStack.EMPTY;
			ItemStack ret = this.stack;
			this.stack = ItemStack.EMPTY;
			dirty();
			return ret;
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack) {
			if (index > 0)
				return;
			this.stack = stack;
			dirty();
		}

		@Override
		public int getInventoryStackLimit() {
			return 1;
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
			return index == 0;
		}

		@Override
		public int getField(int id) {
			return 0;
		}

		@Override
		public void setField(int id, int value) {
			
		}

		@Override
		public int getFieldCount() {
			return 0;
		}

		@Override
		public void clear() {
			this.stack = ItemStack.EMPTY;
			dirty();
		}

		@Override
		public String getName() {
			return "Altar";
		}

		@Override
		public boolean hasCustomName() {
			return false;
		}

		@Override
		public int[] getSlotsForFace(EnumFacing side) {
			return new int[] {0};
		}

		@Override
		public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
			if (index != 0)
				return false;
			
			return stack == ItemStack.EMPTY;
		}

		@Override
		public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
			return index == 0 && stack != ItemStack.EMPTY;
		}

		@Override
		public boolean isEmpty() {
			return this.stack == ItemStack.EMPTY;
		}

		@Override
		public boolean isUsableByPlayer(EntityPlayer arg0) {
			return true;
		}
		
	}
}
