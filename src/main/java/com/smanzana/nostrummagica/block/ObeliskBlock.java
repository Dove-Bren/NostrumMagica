package com.smanzana.nostrummagica.block;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.tile.ObeliskTileEntity;
import com.smanzana.nostrummagica.tile.ObeliskTileEntity.Corner;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;

/**
 * Obelisk block. If tile, has tile entity (and is master or slave).
 * If not tile, regular block.
 * 
 * Structure is 4 pillars that surround a central block. When blocks are broken,
 * They use their tile and master status to determine where they should check to
 * remove the structure.
 * 
 * @author Skyler
 *
 */
public class ObeliskBlock extends Block {

	private static final BooleanProperty MASTER = BooleanProperty.create("master");
	private static final BooleanProperty TILE = BooleanProperty.create("tile");
	
	public static final int TILE_OFFSETY = 3; // height diff between TE in pillars and master TE
	public static final int TILE_OFFSETH = 3; // horizontal distance between master and pillars
	public static final int TILE_HEIGHT = 4; // Total height of corner pillars
	
	private static final VoxelShape TILE_SHAPE = Block.makeCuboidShape(4.8D, 4.8D, 4.8D, 11.2D, 11.2D, 11.2D);
	
	public static final String ID = "nostrum_obelisk";
	
	public ObeliskBlock() {
		super(Block.Properties.create(Material.ROCK)
				.hardnessAndResistance(2f, 10f)
				.sound(SoundType.STONE)
				.harvestTool(ToolType.PICKAXE)
				.harvestLevel(2)
				.noDrops()
				);
		
		this.setDefaultState(this.stateContainer.getBaseState()
				.with(MASTER, false)
				.with(TILE, false));
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }
	
	@Override
	public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
		if (state == null)
			return 0;
		if (!state.get(TILE))
			return 0;
		
		if (!state.get(MASTER))
			return 8;
		
		return 12;
	}
	
	@Override
	public int getOpacity(BlockState state, IBlockReader world, BlockPos pos) {
		if (state == null)
			return 15;
		if (!state.get(TILE))
			return 15;
		
		return 0;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader source, BlockPos pos, ISelectionContext context) {
		if (state.get(TILE) && !state.get(MASTER)) {
			return TILE_SHAPE;
		} else {
			return VoxelShapes.fullCube();
		}
	}
	
	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
		;
	}
	
//	@Override
//	public boolean isFullCube(BlockState state) {
//		return false;
//	}
//	
//	@Override
//	public boolean isOpaqueCube(BlockState state) {
//		return !state.get(TILE) || state.get(MASTER);
//	}
	
	@Override
	public boolean isReplaceable(BlockState state, BlockItemUseContext context) {
        return false;
    }
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(MASTER, TILE);
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		if (world.isRemote)
			return;
		
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		// If we're a tile, get TE and call destroy
		// Else, search up and down to find a tile
		// If none are found, exit; we'll be destroyed
		if (state.get(TILE)) {
			TileEntity ent = world.getTileEntity(pos);
			if (ent == null || !(ent instanceof ObeliskTileEntity))
				return;
			
			ObeliskTileEntity te = (ObeliskTileEntity) ent;
			te.destroy();
		} else {
			// search up and down for a TE
			// This sort of sucks, because it'll be done for every block in the pillar
			// as they are destroyed.
			// I decided to go ahead and do it instead of doing something like storing
			// whether we've been 'destroyed' in the blockstate
			// (tile = 0, master = 1 for example)
			// just cause this is rarely called and it's not THAT many checks
			for (int i = -(TILE_HEIGHT - 1); i < TILE_HEIGHT; i++) {
				BlockPos bp = pos.add(0, i, 0);
				TileEntity ent = world.getTileEntity(bp);
				if (ent == null || !(ent instanceof ObeliskTileEntity))
					continue;
				
				ObeliskTileEntity te = (ObeliskTileEntity) ent;
				te.destroy();
				break;
			}
		}
		
		// We will be destroyed
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	public static boolean blockIsMaster(BlockState state) {
		return state.get(TILE) && state.get(MASTER);
	}
	
	public BlockState getMasterState() {
		return this.getDefaultState().with(MASTER, true)
				.with(TILE, true);
	}


	public BlockState getTileState() {
		return this.getDefaultState().with(MASTER, false)
				.with(TILE, true);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		if (state.get(TILE))
			return new ObeliskTileEntity(state.get(MASTER));
		
		return null;
	}
	
	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			destroy(world, pos, state);
			world.removeTileEntity(pos);
		}
	}
	
//	@SuppressWarnings("deprecation")
//	@Override
//	public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param) {
//		super.eventReceived(state, worldIn, pos, id, param);
//        TileEntity tileentity = worldIn.getTileEntity(pos);
//        return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
//	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		
		if (state.get(MASTER) == false) {
			return ActionResultType.PASS;
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (!ModConfig.config.obeliskReqMagic() && (attr == null || !attr.isUnlocked())) {
			if (worldIn.isRemote) {
				player.sendMessage(new TranslationTextComponent("info.obelisk.nomagic"), Util.DUMMY_UUID);
			}
			return ActionResultType.SUCCESS;
		}
		
		if (!worldIn.isRemote()) {
			worldIn.notifyBlockUpdate(pos, state, state, 2);
		} else {
			NostrumMagica.instance.proxy.openObeliskScreen(worldIn, pos);
		}
		return ActionResultType.SUCCESS;
	}
	
	protected static int xs[] = new int[] {-TILE_OFFSETH, -TILE_OFFSETH, TILE_OFFSETH, TILE_OFFSETH};
	protected static int zs[] = new int[] {-TILE_OFFSETH, TILE_OFFSETH, -TILE_OFFSETH, TILE_OFFSETH};
	protected static Corner corners[] = new Corner[] {Corner.SW, Corner.NW, Corner.SE, Corner.NE};
	
	public static boolean canSpawnObelisk(World world, BlockPos center) {
		BlockState state = world.getBlockState(center);
		if (state == null || state.getBlockHardness(world, center) > 2.0f)
			return false;
		
		for (int i = 0; i < xs.length; i++) {
			if (!checkPillar(world, center.add(xs[i], 1, zs[i])))
				return false;
		}
		
		return true;
	}
	
	public static boolean spawnObelisk(World world, BlockPos center) {
		if (!canSpawnObelisk(world, center)) {
			return false;
		}
		
		for (int i = 0; i < xs.length; i++) {
			spawnPillar(world, center.add(xs[i], 1, zs[i]), corners[i]);
		}
		for (int i = -TILE_OFFSETH; i <= TILE_OFFSETH; i++)
			for (int j = -TILE_OFFSETH; j <= TILE_OFFSETH; j++) {
				world.setBlockState(center.add(i, 0, j), Blocks.OBSIDIAN.getDefaultState());
			}
		for (int i = -1; i <= 1; i++) {
			int offset = (TILE_OFFSETH + 1);
			world.setBlockState(center.add(i, 0, offset), Blocks.OBSIDIAN.getDefaultState());
			world.setBlockState(center.add(i, 0, -offset), Blocks.OBSIDIAN.getDefaultState());
			world.setBlockState(center.add(offset, 0, i), Blocks.OBSIDIAN.getDefaultState());
			world.setBlockState(center.add(-offset, 0, i), Blocks.OBSIDIAN.getDefaultState());
		}
		world.setBlockState(center, NostrumBlocks.obelisk.getMasterState());
		world.setTileEntity(center, new ObeliskTileEntity(true));
		
		((ObeliskTileEntity) world.getTileEntity(center)).init();
		
		return true;
	}
	
	private static boolean checkPillar(World world, BlockPos center) {
		for (int i = 0; i < TILE_HEIGHT; i++) {
			if (!world.isAirBlock(center.add(0, i, 0)))
				return false;
		}
		
		return true;
	}

	private static void spawnPillar(World world, BlockPos center, Corner corner) {
		for (int i = 0; i < TILE_HEIGHT; i++) {
			BlockPos pos = center.add(0, i, 0);
			if (i == TILE_OFFSETY - 1) {
				world.setBlockState(pos, NostrumBlocks.obelisk.getTileState());
				world.setTileEntity(pos, new ObeliskTileEntity(corner));
			} else {
				world.setBlockState(pos, NostrumBlocks.obelisk.getDefaultState());
			}
		}
	}
}
