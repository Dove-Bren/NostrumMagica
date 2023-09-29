package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.tiles.SpellTableEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

@SuppressWarnings("deprecation")
public class SpellTable extends HorizontalBlock implements ITileEntityProvider {
	
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	private static final BooleanProperty MASTER = BooleanProperty.create("master");
	
	public static final String ID = "spell_table";
	
	public SpellTable() {
		super(Block.Properties.create(Material.WOOD)
				.hardnessAndResistance(3.0f, 15.0f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				.harvestLevel(1)
				);
		
		this.setDefaultState(this.stateContainer.getBaseState().with(MASTER, true)
				.with(FACING, Direction.NORTH));
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }
	
	// public BlockRenderLayer getRenderLayer() ?
//	@Override
//	public boolean isFullCube(BlockState state) {
//		return false;
//	}
//	
//	@Override
//	public boolean isOpaqueCube(BlockState state) {
//		return false;
//	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(MASTER, FACING);
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
		
		world.removeBlock(getPaired(state, pos), false);
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
	public boolean hasTileEntity() {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		if (state.get(MASTER))
			return new SpellTableEntity();
		
		return null;
	}
	
	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			this.destroy(world, pos, state);
			world.removeTileEntity(pos);
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param) {
		super.eventReceived(state, worldIn, pos, id, param);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand handIn, BlockRayTraceResult hit) {
		
		if (state.get(MASTER) == false) {
			pos = pos.offset(state.get(FACING));
		}
		
		playerIn.openGui(NostrumMagica.instance,
				NostrumGui.spellTableID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
