package com.smanzana.nostrummagica.blocks;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.items.SpellTomePage;
import com.smanzana.nostrummagica.items.WarlockSword;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancementWrapper;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
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
		GameRegistry.registerTileEntity(ModificationTableEntity.class, "modification_table");
//		GameRegistry.addShapedRecipe(new ItemStack(instance()),
//				"WPW", "WCW", "WWW",
//				'W', new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE),
//				'P', new ItemStack(Items.PAPER, 1, OreDictionary.WILDCARD_VALUE),
//				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1));
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
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		
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
		
		public ItemStack getMainSlot() {
			return this.getStackInSlot(0);
		}
		
		public ItemStack getInputSlot() {
			return this.getStackInSlot(1);
		}
		
		@Override
		public int getSizeInventory() {
			return 2;
		}

		@Override
		public ItemStack getStackInSlot(int index) {
			if (index < 0 || index >= getSizeInventory())
				return null;
			
			return slots[index];
		}

		@Override
		public ItemStack decrStackSize(int index, int count) {
			if (index < 0 || index >= getSizeInventory() || slots[index] == null)
				return null;
			
			ItemStack stack;
			if (slots[index].stackSize <= count) {
				stack = slots[index];
				slots[index] = null;
			} else {
				stack = slots[index].copy();
				stack.stackSize = count;
				slots[index].stackSize -= count;
			}
			
			this.markDirty();
			
			return stack;
		}

		@Override
		public ItemStack removeStackFromSlot(int index) {
			if (index < 0 || index >= getSizeInventory())
				return null;
			
			ItemStack stack = slots[index];
			slots[index] = null;
			
			this.markDirty();
			return stack;
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack) {
			if (!isItemValidForSlot(index, stack))
				return;
			
			slots[index] = stack;
			this.markDirty();
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
		}

		@Override
		public void closeInventory(EntityPlayer player) {
		}

		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack) {
			if (index < 0 || index >= getSizeInventory())
				return false;
			
			if (stack == null)
				return true;
			
			if (index == 0) {
				return IsModifiable(stack);
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
				if (getStackInSlot(i) == null)
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
				
				ItemStack stack = ItemStack.loadItemStackFromNBT(items.getCompoundTag(key));
				this.setInventorySlotContents(id, stack);
			}
		}
		
		// Submit current staged modifications
		public void modify(boolean valB, float valF) {
			ItemStack stack = this.getMainSlot();
			if (stack.getItem() instanceof SpellTome) {
				if (this.getInputSlot() != null && this.getInputSlot().getItem() instanceof SpellTomePage) {
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
			} else if (stack.getItem() instanceof SpellScroll) {
				Spell spell = SpellScroll.getSpell(stack);
				if (spell != null) {
					spell.setIcon((int) valF);
					this.setInventorySlotContents(1, null);
				}
			} else if (stack.getItem() instanceof WarlockSword) {
				this.setInventorySlotContents(1, null);
				WarlockSword.addCapacity(stack, 2);
			}
			
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
			if (table.getStackInSlot(i) != null) {
				EntityItem item = new EntityItem(
						world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
						table.removeStackFromSlot(i));
				world.spawnEntityInWorld(item);
			}
		}
		
	}
	
	public static boolean IsModifiable(ItemStack stack) {
		if (stack == null) {
			return false;
		}
		
		Item item = stack.getItem();
		
		if (item instanceof SpellRune) {
			SpellComponentWrapper comp = SpellRune.toComponentWrapper(stack);
			
			if (comp.isTrigger()) {
				return comp.getTrigger().supportedFloats() != null || comp.getTrigger().supportsBoolean();
			} else if (comp.isShape()) {
				return comp.getShape().supportedFloats() != null || comp.getShape().supportsBoolean();
			}
		}
		
		return item instanceof SpellTome
				//|| item instanceof SpellRune
				|| (item instanceof SpellScroll && SpellScroll.getSpell(stack) != null)
				|| item instanceof WarlockSword;
	}
}
