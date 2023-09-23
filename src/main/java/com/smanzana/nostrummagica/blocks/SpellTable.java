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
import net.minecraft.state.BooleanProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SpellTable extends BlockHorizontal implements ITileEntityProvider {
	
	private static final BooleanProperty MASTER = BooleanProperty.create("master");
	
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
		
		this.setDefaultState(this.stateContainer.getBaseState().with(MASTER, true)
				.with(FACING, Direction.NORTH));
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
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
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(MASTER, FACING);
	}
	
	@Override
	public BlockState getStateFromMeta(int meta) {
		Direction enumfacing = Direction.getHorizontal(meta);
		return getDefaultState().with(FACING, enumfacing)
				.with(MASTER, ((meta >> 2) & 1) == 1);
	}
	
	@Override
	public int getMetaFromState(BlockState state) {
		return ((state.get(MASTER) ? 1 : 0) << 2) | (state.get(FACING).getHorizontalIndex());
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		if (state.get(MASTER)) {
			TileEntity ent = world.getTileEntity(pos);
			if (!world.isRemote && ent != null) {
				SpellTableEntity table = (SpellTableEntity) ent;
				for (int i = 0; i < table.getSizeInventory(); i++) {
					if (table.getStackInSlot(i) != null) {
						ItemEntity item = new ItemEntity(
								world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
								table.removeStackFromSlot(i));
						world.addEntity(item);
					}
				}
			}
		}
		
		world.setBlockToAir(getPaired(state, pos));
	}
	
	private BlockPos getPaired(BlockState state, BlockPos pos) {
		return pos.offset(state.get(FACING));
	}
	
//	private BlockPos getMaster(BlockState state, BlockPos pos) {
//		if (state.get(MASTER))
//			return pos;
//		
//		return pos.offset(state.get(FACING));
//	}
	
	@OnlyIn(Dist.CLIENT)
    public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	public BlockState getSlaveState(Direction direction) {
		return this.getDefaultState().with(MASTER, false)
				.with(FACING, direction);
	}


	public BlockState getMaster(Direction enumfacing) {
		return this.getDefaultState().with(MASTER, true)
				.with(FACING, enumfacing);
	}
	
	@Override
	public Item getItemDropped(BlockState state, Random rand, int fortune) {
		return SpellTableItem.instance();//state.get(MASTER) ? SpellTableItem.instance() : null;
	}
	
	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, World world, BlockPos pos, PlayerEntity player) {
		return new ItemStack(SpellTableItem.instance(), 1);
	}


	@Override
	public boolean hasTileEntity() {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		BlockState state = this.getStateFromMeta(meta);
		if (state.get(MASTER))
			return new SpellTableEntity();
		
		return null;
	}
	
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) { broke();
		this.destroy(world, pos, state);
		
		world.removeTileEntity(pos);
		super.breakBlock(world, pos, state);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param) {
		super.eventReceived(state, worldIn, pos, id, param);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		
		if (state.get(MASTER) == false) {
			pos = pos.offset(state.get(FACING));
		}
		
		playerIn.openGui(NostrumMagica.instance,
				NostrumGui.spellTableID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
}
