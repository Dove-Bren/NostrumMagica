package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.SpellTableEntity;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SpellTable extends BlockHorizontal implements ITileEntityProvider {
	
	private static final PropertyBool MASTER = PropertyBool.create("master");
	
	public static final String ID = "spell_table";
	
	private static SpellTable instance = null;
	public static SpellTable instance() {
		if (instance == null)
			instance = new SpellTable();
		
		return instance;
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
				.withProperty(FACING, Direction.NORTH));
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
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
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, MASTER, FACING);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		Direction enumfacing = Direction.getHorizontal(meta);
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
		
		if (state.getValue(MASTER)) {
			TileEntity ent = world.getTileEntity(pos);
			if (!world.isRemote && ent != null) {
				SpellTableEntity table = (SpellTableEntity) ent;
				for (int i = 0; i < table.getSizeInventory(); i++) {
					if (table.getStackInSlot(i) != null) {
						EntityItem item = new EntityItem(
								world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
								table.removeStackFromSlot(i));
						world.spawnEntity(item);
					}
				}
			}
		}
		
		world.setBlockToAir(getPaired(state, pos));
	}
	
	private BlockPos getPaired(IBlockState state, BlockPos pos) {
		return pos.offset(state.getValue(FACING));
	}
	
//	private BlockPos getMaster(IBlockState state, BlockPos pos) {
//		if (state.getValue(MASTER))
//			return pos;
//		
//		return pos.offset(state.getValue(FACING));
//	}
	
	@OnlyIn(Dist.CLIENT)
    public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	public IBlockState getSlaveState(Direction direction) {
		return this.getDefaultState().withProperty(MASTER, false)
				.withProperty(FACING, direction);
	}


	public IBlockState getMaster(Direction enumfacing) {
		return this.getDefaultState().withProperty(MASTER, true)
				.withProperty(FACING, enumfacing);
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return SpellTableItem.instance();//state.getValue(MASTER) ? SpellTableItem.instance() : null;
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, PlayerEntity player) {
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
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, PlayerEntity playerIn, EnumHand hand, Direction side, float hitX, float hitY, float hitZ) {
		
		if (state.getValue(MASTER) == false) {
			pos = pos.offset(state.getValue(FACING));
		}
		
		playerIn.openGui(NostrumMagica.instance,
				NostrumGui.spellTableID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
}
