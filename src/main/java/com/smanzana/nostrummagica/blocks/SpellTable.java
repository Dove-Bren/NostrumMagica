package com.smanzana.nostrummagica.blocks;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTableItem;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpellTable extends BlockHorizontal implements ITileEntityProvider {
	
	public static class SpellTableEntity extends TileEntity implements IInventory {

		private static final String NBT_INV = "inventory";
		
		/**
		 * Inventory:
		 *   0 - Spell scroll slot
		 *   1-16 - Rune Slots
		 *   17-25 - Reagent slots
		 */
		
		private String displayName;
		private ItemStack slots[];
		
		public SpellTableEntity() {
			displayName = "Spell Table";
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
		
		public int getRuneSlotIndex() {
			return 1;
		}
		
		public int getRuneSlotCount() {
			return 16;
		}
		
		public int getScrollSlotIndex() {
			return 0;
		}
		
		public int getReagentSlotIndex() {
			return 17;
		}
		
		public int getReagentSlotCount() {
			return 9;
		}
		
		public ItemStack[] getReagentSlots() {
			broke() go see callers
			return Arrays.copyOfRange(slots, getReagentSlotIndex(), getReagentSlotIndex() + getReagentSlotCount() - 1);
		}

		@Override
		public int getSizeInventory() {
			return 26;
		}

		@Override
		public @Nonnull ItemStack getStackInSlot(int index) {
			if (index < 0 || index >= getSizeInventory())
				return ItemStack.EMPTY;
			
			return slots[index] == null ? ItemStack.EMPTY : slots[index];
		}

		@Override
		public ItemStack decrStackSize(int index, int count) {
			if (index < 0 || index >= getSizeInventory() || slots[index] == null)
				return ItemStack.EMPTY;
			
			ItemStack stack;
			if (slots[index].getCount() <= count) {
				stack = slots[index];
				slots[index] = null;
			} else {
				stack = slots[index].copy();
				stack.setCount(count);
				slots[index].shrink(count);
			}
			
			this.markDirty();
			
			return stack;
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
			if (index < 0 || index >= getSizeInventory())
				return;
			
			if (stack == ItemStack.EMPTY)
				stack = null;
			
			slots[index] = stack;
			this.markDirty();
		}

		@Override
		public int getInventoryStackLimit() {
			return 64;
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
				return stack.getItem() instanceof BlankScroll;
			}
			
			if (index < getReagentSlotIndex()) {
			
				if (!(stack.getItem() instanceof SpellRune))
					return false;
				
				if (index == 1) {
					return SpellRune.isTrigger(stack);
				}

				return true;
			}
			
			// Reagent bag
			return stack.getItem() instanceof ReagentItem;
			
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
		
		public void clearBoard() {
			for (int i = 0; i < getReagentSlotIndex(); i++) {
				removeStackFromSlot(i);
			}
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
		
		public void craft(String name) {
			ItemStack stack = this.getStackInSlot(0);
			if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof BlankScroll)) {
				return;
			}
			
			Spell spell = SpellCreationGui.SpellCreationContainer.craftSpell(
					name, this, new LinkedList<String>(), new LinkedList<String>(), true, true);
			
			if (spell != null) {
				NostrumMagica.spellRegistry.register(spell);
				ItemStack scroll = new ItemStack(SpellScroll.instance(), 1);
				SpellScroll.setSpell(scroll, spell);
				this.clearBoard();
				this.setInventorySlotContents(0, scroll);
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

	private static final PropertyBool MASTER = PropertyBool.create("master");
	
	public static final String ID = "spell_table";
	
	private static SpellTable instance = null;
	public static SpellTable instance() {
		if (instance == null)
			instance = new SpellTable();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(SpellTableEntity.class,
				new ResourceLocation("spell_table"));
	}
	
	public SpellTable() {
		super(Material.WOOD, MapColor.WOOD);
		this.setUnlocalizedName(ID);
		this.setHardness(3.0f);
		this.setResistance(15.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 1);
		
		this.setDefaultState(this.blockState.getBaseState().withProperty(MASTER, true)
				.withProperty(FACING, EnumFacing.NORTH));
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
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
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, MASTER, FACING);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.getHorizontal(meta);
		return getDefaultState().withProperty(FACING, enumfacing)
				.withProperty(MASTER, ((meta >> 2) & 1) == 1);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return ((state.getValue(MASTER) ? 1 : 0) << 2) | (state.getValue(FACING).getHorizontalIndex());
	}
	
	private void destroy(World world, BlockPos pos, IBlockState state) {
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		BlockPos master = getMaster(state, pos);
		TileEntity ent = world.getTileEntity(master);
		if (!world.isRemote && ent != null) {
			SpellTableEntity table = (SpellTableEntity) ent;
			for (int i = 0; i < table.getSizeInventory(); i++) {
				if (table.getStackInSlot(i) != ItemStack.EMPTY) {
					EntityItem item = new EntityItem(
							world, master.getX() + .5, master.getY() + .5, master.getZ() + .5,
							table.removeStackFromSlot(i));
					world.spawnEntity(item);
				}
			}
		}
		
		EntityItem item = new EntityItem(world, master.getX() + .5, master.getY() + .5, master.getZ() + .5,
				new ItemStack(SpellTableItem.instance()));
		world.spawnEntity(item);
		
		world.setBlockToAir(getPaired(state, pos));
	}
	
	private BlockPos getPaired(IBlockState state, BlockPos pos) {
		return pos.offset(state.getValue(FACING));
	}
	
	private BlockPos getMaster(IBlockState state, BlockPos pos) {
		if (state.getValue(MASTER))
			return pos;
		
		return pos.offset(state.getValue(FACING));
	}
	
	@SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	public IBlockState getSlaveState(EnumFacing direction) {
		return this.getDefaultState().withProperty(MASTER, false)
				.withProperty(FACING, direction);
	}


	public IBlockState getMaster(EnumFacing enumfacing) {
		return this.getDefaultState().withProperty(MASTER, true)
				.withProperty(FACING, enumfacing);
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return state.getValue(MASTER) ? SpellTableItem.instance() : null;
	}
	
	@Override
	public @Nonnull ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(SpellTableItem.instance(), 1);
	}


	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		IBlockState state = this.getStateFromMeta(meta);
		if (state.getValue(MASTER))
			return new SpellTableEntity();
		
		return null;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		this.destroy(world, pos, state);
		
		world.removeTileEntity(pos);
		super.breakBlock(world, pos, state);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
		super.eventReceived(state, worldIn, pos, id, param);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		if (state.getValue(MASTER) == false) {
			pos = pos.offset(state.getValue(FACING));
		}
		
		playerIn.openGui(NostrumMagica.instance,
				NostrumGui.spellTableID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
}
