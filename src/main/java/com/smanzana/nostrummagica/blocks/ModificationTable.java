package com.smanzana.nostrummagica.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.items.SpellTomePage;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancementWrapper;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModificationTable extends BlockContainer {
	
	public static final String ID = "modification_table";
	
	private static ModificationTable instance = null;
	public static ModificationTable instance() {
		if (instance == null)
			instance = new ModificationTable();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(ModificationTableEntity.class,
				new ResourceLocation(NostrumMagica.MODID, "modification_table"));
	}
	
	public ModificationTable() {
		super(Material.WOOD, MapColor.WOOD);
		this.setUnlocalizedName(ID);
		this.setHardness(2.0f);
		this.setResistance(10.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 0);
	}
	
//	@Override
//	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
//		return true;
//	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		playerIn.openGui(NostrumMagica.instance,
				NostrumGui.modtableID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
	public static class ModificationTableEntity extends TileEntity implements IInventory {

		private static final String NBT_INV = "modtable";
		
		/**
		 * Inventory:
		 *   0 - Center Icon
		 *   1 - Input Slot
		 */
		
		private String displayName;
		private ItemStack slots[];
		
		public ModificationTableEntity() {
			displayName = "Modification Table";
			slots = new ItemStack[getSizeInventory()];
		}
		
		@Override
		public String getName() {
			return displayName;
		}

		@Override
		public boolean hasCustomName() {
			return false;
		}
		
		public @Nonnull ItemStack getMainSlot() {
			return this.getStackInSlot(0);
		}
		
		public @Nonnull ItemStack getInputSlot() {
			return this.getStackInSlot(1);
		}
		
		@Override
		public int getSizeInventory() {
			return 2;
		}

		@Override
		public @Nonnull ItemStack getStackInSlot(int index) {
			if (index < 0 || index >= getSizeInventory())
				return ItemStack.EMPTY;
			
			ItemStack stack = slots[index];
			return stack == null ? ItemStack.EMPTY : stack;
		}

		@Override
		public @Nonnull ItemStack decrStackSize(int index, int count) {
			if (index < 0 || index >= getSizeInventory() || slots[index] == null)
				return ItemStack.EMPTY;
			
			ItemStack taken = ItemStack.EMPTY;
			ItemStack there = this.getStackInSlot(index);
			
			if (there == ItemStack.EMPTY)
				return taken;
			
			if (there.getCount() <= count) {
				taken = this.getStackInSlot(index);
				slots[index] = null;
			} else {
				taken = slots[index].copy();
				taken.setCount(count);
				slots[index].shrink(count);
			}
			
			this.markDirty();
			
			return taken;
		}

		@Override
		public @Nonnull ItemStack removeStackFromSlot(int index) {
			if (index < 0 || index >= getSizeInventory())
				return ItemStack.EMPTY;
			
			ItemStack stack = this.getStackInSlot(index);
			slots[index] = null;
			
			this.markDirty();
			return stack;
		}

		@Override
		public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
			if (!isItemValidForSlot(index, stack))
				return;
			
			if (stack == ItemStack.EMPTY)
				stack = null;
			
			slots[index] = stack;
			this.markDirty();
		}

		@Override
		public int getInventoryStackLimit() {
			return 1;
		}

		@Override
		public boolean isUsableByPlayer(EntityPlayer player) {
			return true;
		}

		@Override
		public void openInventory(EntityPlayer player) {
		}

		@Override
		public void closeInventory(EntityPlayer player) {
		}

		@Override
		public boolean isItemValidForSlot(int index, @Nullable ItemStack stack) {
			if (index < 0 || index >= getSizeInventory())
				return false;
			
			if (stack == null || stack == ItemStack.EMPTY)
				return true;
			
			if (index == 0) {
				return stack.getItem() instanceof SpellTome
						|| stack.getItem() instanceof SpellRune;
			}
			
			if (index == 1)
				return true;
			
			return false;
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
			for (int i = 0; i < getSizeInventory(); i++)
				removeStackFromSlot(i);
		}
		
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			NBTTagCompound compound = new NBTTagCompound();
			
			for (int i = 0; i < getSizeInventory(); i++) {
				if (getStackInSlot(i) == ItemStack.EMPTY)
					continue;
				
				NBTTagCompound tag = new NBTTagCompound();
				compound.setTag(i + "", getStackInSlot(i).writeToNBT(tag));
			}
			
			if (nbt == null)
				nbt = new NBTTagCompound();
			
			nbt.setTag(NBT_INV, compound);
			return nbt;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			
			if (nbt == null || !nbt.hasKey(NBT_INV, NBT.TAG_COMPOUND))
				return;
			this.clear();
			NBTTagCompound items = nbt.getCompoundTag(NBT_INV);
			for (String key : items.getKeySet()) {
				int id;
				try {
					id = Integer.parseInt(key);
				} catch (NumberFormatException e) {
					NostrumMagica.logger.error("Failed reading SpellTable inventory slot: " + key);
					continue;
				}
				
				ItemStack stack = new ItemStack(items.getCompoundTag(key));
				this.setInventorySlotContents(id, stack);
			}
		}
		
		// Submit current staged modifications
		public void modify(boolean valB, float valF) {
			ItemStack stack = this.getMainSlot();
			if (stack.getItem() instanceof SpellTome) {
				if (this.getInputSlot() != ItemStack.EMPTY && this.getInputSlot().getItem() instanceof SpellTomePage) {
					SpellTome.addEnhancement(stack, new SpellTomeEnhancementWrapper( 
							SpellTomePage.getEnhancement(this.getInputSlot()),
							SpellTomePage.getLevel(this.getInputSlot())));
					int mods = Math.max(0, SpellTome.getModifications(stack) - 1);
					SpellTome.setModifications(stack, mods);
					this.setInventorySlotContents(1, null);
				}
			} else if (stack.getItem() instanceof SpellRune) {
				this.setInventorySlotContents(1, null);
				SpellRune.setPieceParam(stack, new SpellPartParam(valF, valB));
			}
			
		}

		@Override
		public boolean isEmpty() {
			for (ItemStack stack : slots) {
				if (stack != null && stack.getCount() != 0)
					return false;
			}
			return true;
		}
		
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new ModificationTableEntity();
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		destroy(world, pos, state);
		super.breakBlock(world, pos, state);
	}
	
	private void destroy(World world, BlockPos pos, IBlockState state) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof ModificationTableEntity))
			return;
		
		ModificationTableEntity table = (ModificationTableEntity) ent;
		for (int i = 0; i < table.getSizeInventory(); i++) {
			if (table.getStackInSlot(i) != ItemStack.EMPTY) {
				EntityItem item = new EntityItem(
						world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
						table.removeStackFromSlot(i));
				world.spawnEntity(item);
			}
		}
		
	}
}
