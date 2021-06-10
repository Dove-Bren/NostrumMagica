package com.smanzana.nostrummagica.blocks;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.integration.aetheria.blocks.AetherInfuser.AetherInfuserTileEntity;
import com.smanzana.nostrummagica.items.AltarItem;
import com.smanzana.nostrummagica.items.IAetherInfuserLens;
import com.smanzana.nostrummagica.loretag.ILoreTagged;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class AltarBlock extends Block implements ITileEntityProvider {
	
	public static final String ID = "altar_block";
	protected static final AxisAlignedBB ALTAR_AABB = new AxisAlignedBB(0.3D, 0.0D, 0.3D, 0.7D, 0.8D, 0.7D);
	private static final int TICK_DELAY = 5;
	
	private static AltarBlock instance = null;
	public static AltarBlock instance() {
		if (instance == null)
			instance = new AltarBlock();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(AltarTileEntity.class, "nostrum_altar_te");
	}
	
	public AltarBlock() {
		super(Material.ROCK, MapColor.OBSIDIAN);
		this.setUnlocalizedName(ID);
		this.setHardness(3.5f);
		this.setResistance(10.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		
		this.isBlockContainer = true;
		this.setLightOpacity(1);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return ALTAR_AABB;
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
	public boolean isFullCube(IBlockState state) {
        return false;
    }
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
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
		EntityItem item = new EntityItem(world,
				pos.getX() + .5,
				pos.getY() + .5,
				pos.getZ() + .5,
				new ItemStack(AltarItem.instance()));
		world.spawnEntityInWorld(item);
		
		TileEntity te = world.getTileEntity(pos);
		if (te != null) {
			AltarTileEntity altar = (AltarTileEntity) te;
			if (altar.getItem() != null) {
				item = new EntityItem(world,
						pos.getX() + .5,
						pos.getY() + .5,
						pos.getZ() + .5,
						altar.getItem());
				world.spawnEntityInWorld(item);
			}
		}
		
        world.removeTileEntity(pos);
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return super.getItemDropped(state, rand, fortune);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int eventID, int eventParam) {
		super.eventReceived(state, worldIn, pos, eventID, eventParam);
		TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(eventID, eventParam);
	}
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		if (!worldIn.isUpdateScheduled(pos, this)) {
			worldIn.scheduleUpdate(pos, this, TICK_DELAY);
		}
		
		super.onBlockAdded(worldIn, pos, state);
	}
	
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te != null && te instanceof AltarTileEntity && ((AltarTileEntity) te).getItem() == null) {
			AltarTileEntity altar = (AltarTileEntity) te;
			List<EntityItem> items = worldIn.getEntitiesWithinAABB(EntityItem.class, Block.FULL_BLOCK_AABB.offset(pos).offset(0, 1, 0).expand(0, 1, 0));
			if (items != null && !items.isEmpty()) {
				EntityItem first = items.get(0);
				ItemStack stack = first.getEntityItem();
				
				altar.setItem(stack.splitStack(1));
				if (stack.stackSize <= 0) {
					first.setDead();
				}
			}
		}
		
		if (!worldIn.isUpdateScheduled(pos, this)) {
			worldIn.scheduleUpdate(pos, this, TICK_DELAY);
		}
		
		super.updateTick(worldIn, pos, state, rand);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null)
			return false;
		
		AltarTileEntity altar = (AltarTileEntity) te;
		if (altar.getItem() == null) {
			// Accepting items
			if (heldItem != null) {
				altar.setItem(heldItem.splitStack(1));
				return true;
			} else
				return false;
		} else {
			// Has an item
			if (heldItem == null) {
				final ItemStack altarItem = altar.getItem();
				if (!playerIn.inventory.addItemStackToInventory(altarItem)) {
					worldIn.spawnEntityInWorld(
							new EntityItem(worldIn,
									pos.getX() + .5, pos.getY() + 1.2, pos.getZ() + .5,
									altar.getItem())
							);
				} else {
					INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
					if (attr != null && attr.isUnlocked()) {
						if (altarItem.getItem() instanceof ILoreTagged) {
							attr.giveBasicLore((ILoreTagged) altarItem.getItem());
						} else if (altarItem.getItem() instanceof ItemBlock &&
								((ItemBlock) altarItem.getItem()).getBlock() instanceof ILoreTagged) {
							attr.giveBasicLore((ILoreTagged) ((ItemBlock) altarItem.getItem()).getBlock());
						}
					}
				}
				altar.setItem(null);
				return true;
			} else
				return false;
		}
		
	}
	
	public static class AltarTileEntity extends TileEntity implements ISidedInventory, IAetherInfusableTileEntity {
		
		private ItemStack stack;
		
		public AltarTileEntity() {
			
		}
		
		public ItemStack getItem() {
			return stack;
		}
		
		public void setItem(ItemStack stack) {
			this.stack = stack;
			dirty();
		}
		
		private static final String NBT_ITEM = "item";
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			
			if (stack != null) {
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
				stack = null;
			} else {
				NBTTagCompound tag = nbt.getCompoundTag(NBT_ITEM);
				stack = ItemStack.loadItemStackFromNBT(tag);
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
			worldObj.markBlockRangeForRenderUpdate(pos, pos);
			worldObj.notifyBlockUpdate(pos, this.worldObj.getBlockState(pos), this.worldObj.getBlockState(pos), 3);
			worldObj.scheduleBlockUpdate(pos, this.getBlockType(),0,0);
			markDirty();
		}

		@Override
		public int getSizeInventory() {
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int index) {
			if (index > 0)
				return null;
			return this.stack;
		}

		@Override
		public ItemStack decrStackSize(int index, int count) {
			if (index > 0)
				return null;
			ItemStack ret = this.stack.splitStack(count);
			if (this.stack.stackSize == 0)
				this.stack = null;
			this.dirty();
			return ret;
		}

		@Override
		public ItemStack removeStackFromSlot(int index) {
			if (index > 0)
				return null;
			ItemStack ret = this.stack;
			this.stack = null;
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
			this.stack = null;
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
			
			return stack == null;
		}

		@Override
		public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
			return index == 0 && stack != null;
		}

		@Override
		public boolean canAcceptAetherInfuse(AetherInfuserTileEntity source, int maxAether) {
			return stack != null && stack.getItem() instanceof IAetherInfuserLens;
		}

		@Override
		public int acceptAetherInfuse(AetherInfuserTileEntity source, int maxAether) {
			final IAetherInfuserLens infusable = ((IAetherInfuserLens) stack.getItem());
			final int leftover;
			if (infusable.canAcceptAetherInfuse(stack, pos, source, maxAether)) {
				leftover = infusable.acceptAetherInfuse(stack, pos, source, maxAether);
			} else {
				leftover = maxAether;
			}
			return leftover;
		}
		
	}
}
