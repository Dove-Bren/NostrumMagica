package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTableItem;

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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpellTable extends BlockHorizontal implements ITileEntityProvider {
	
	public static class SpellTableEntity extends TileEntity implements IInventory {

		/**
		 * Inventory:
		 *   0 - Spell scroll slot
		 *   1-9 - Rune Slots
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

		@Override
		public int getSizeInventory() {
			return 10;
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
			if (index < 0 || index >= getSizeInventory())
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
			// TODO Auto-generated method stub
		}

		@Override
		public void closeInventory(EntityPlayer player) {
			// TODO Auto-generated method stub
		}

		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack) {
			if (index < 0 || index >= getSizeInventory())
				return false;
			
			if (stack == null)
				return true;
			
			if (index == 0) {
				return stack.getItem() instanceof BlankScroll;
			}
			
			return stack.getItem() instanceof BlankScroll
					|| stack.getItem() instanceof SpellScroll; // should be rune
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
		GameRegistry.registerTileEntity(SpellTableEntity.class, "spell_table");
	}
	
	public SpellTable() {
		super(Material.WOOD, MapColor.WOOD);
		this.setUnlocalizedName(ID);
		this.setHardness(3.0f);
		this.setResistance(15.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 3);
		
		this.setDefaultState(this.blockState.getBaseState().withProperty(MASTER, true)
				.withProperty(FACING, EnumFacing.NORTH));
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
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
	
	@Override
	public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {
		destroy(worldIn, pos, state);
	}
	
	@Override
	public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn) {
		destroy(worldIn, pos, null);
	}
	
	private void destroy(World world, BlockPos pos, IBlockState state) {
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		world.setBlockToAir(getPaired(state, pos));
	}
	
	private BlockPos getPaired(IBlockState state, BlockPos pos) {
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
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(SpellTableItem.instance(), 1);
	}


	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new SpellTableEntity();
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
		
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null)
			return;
		
		SpellTableEntity table = (SpellTableEntity) ent;
		for (int i = 0; i < table.getSizeInventory(); i++) {
			if (table.getStackInSlot(i) != null) {
				EntityItem item = new EntityItem(
						world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
						table.removeStackFromSlot(i));
				world.spawnEntityInWorld(item);
			}
		}
		
		world.removeTileEntity(pos);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
		super.eventReceived(state, worldIn, pos, id, param);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		if (state.getValue(MASTER) == false) {
			pos = pos.offset(state.getValue(FACING));
		}
		
		playerIn.openGui(NostrumMagica.instance,
				NostrumGui.spellTableID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
}
