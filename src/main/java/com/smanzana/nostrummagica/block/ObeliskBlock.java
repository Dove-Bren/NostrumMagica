package com.smanzana.nostrummagica.block;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.tile.ObeliskTileEntity;
import com.smanzana.nostrummagica.tile.ObeliskTileEntity.Corner;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

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
public class ObeliskBlock extends Block implements EntityBlock {

	private static final BooleanProperty MASTER = BooleanProperty.create("master");
	private static final BooleanProperty TILE = BooleanProperty.create("tile");
	
	public static final int TILE_OFFSETY = 3; // height diff between TE in pillars and master TE
	public static final int TILE_OFFSETH = 3; // horizontal distance between master and pillars
	public static final int TILE_HEIGHT = 4; // Total height of corner pillars
	
	private static final VoxelShape TILE_SHAPE = Block.box(4.8D, 4.8D, 4.8D, 11.2D, 11.2D, 11.2D);
	
	public static final String ID = "nostrum_obelisk";
	
	public ObeliskBlock() {
		super(Block.Properties.of(Material.STONE)
				.strength(2f, 10f)
				.sound(SoundType.STONE)
				.noDrops()
				);
		
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(MASTER, false)
				.setValue(TILE, false));
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }
	
	@Override
	public int getLightValue(BlockState state, BlockGetter world, BlockPos pos) {
		if (state == null)
			return 0;
		if (!state.getValue(TILE))
			return 0;
		
		if (!state.getValue(MASTER))
			return 8;
		
		return 12;
	}
	
	@Override
	public int getLightBlock(BlockState state, BlockGetter world, BlockPos pos) {
		if (state == null)
			return 15;
		if (!state.getValue(TILE))
			return 15;
		
		return 0;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter source, BlockPos pos, CollisionContext context) {
		if (state.getValue(TILE) && !state.getValue(MASTER)) {
			return TILE_SHAPE;
		} else {
			return Shapes.block();
		}
	}
	
	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {
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
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return false;
    }
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(MASTER, TILE);
	}
	
	private void destroy(Level world, BlockPos pos, BlockState state) {
		if (world.isClientSide)
			return;
		
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		// If we're a tile, get TE and call destroy
		// Else, search up and down to find a tile
		// If none are found, exit; we'll be destroyed
		if (state.getValue(TILE)) {
			BlockEntity ent = world.getBlockEntity(pos);
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
				BlockPos bp = pos.offset(0, i, 0);
				BlockEntity ent = world.getBlockEntity(bp);
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
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	public static boolean blockIsMaster(BlockState state) {
		return state.getValue(TILE) && state.getValue(MASTER);
	}
	
	public BlockState getMasterState() {
		return this.defaultBlockState().setValue(MASTER, true)
				.setValue(TILE, true);
	}


	public BlockState getTileState() {
		return this.defaultBlockState().setValue(MASTER, false)
				.setValue(TILE, true);
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		if (state.getValue(TILE))
			return new ObeliskTileEntity(state.getValue(MASTER));
		
		return null;
	}
	
	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			destroy(world, pos, state);
			world.removeBlockEntity(pos);
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
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		
		if (state.getValue(MASTER) == false) {
			return InteractionResult.PASS;
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (!ModConfig.config.obeliskReqMagic() && (attr == null || !attr.isUnlocked())) {
			if (worldIn.isClientSide) {
				player.sendMessage(new TranslatableComponent("info.obelisk.nomagic"), Util.NIL_UUID);
			}
			return InteractionResult.SUCCESS;
		}
		
		if (!worldIn.isClientSide()) {
			worldIn.sendBlockUpdated(pos, state, state, 2);
		} else {
			NostrumMagica.instance.proxy.openObeliskScreen(worldIn, pos);
		}
		return InteractionResult.SUCCESS;
	}
	
	protected static int xs[] = new int[] {-TILE_OFFSETH, -TILE_OFFSETH, TILE_OFFSETH, TILE_OFFSETH};
	protected static int zs[] = new int[] {-TILE_OFFSETH, TILE_OFFSETH, -TILE_OFFSETH, TILE_OFFSETH};
	protected static Corner corners[] = new Corner[] {Corner.SW, Corner.NW, Corner.SE, Corner.NE};
	
	public static boolean canSpawnObelisk(Level world, BlockPos center) {
		BlockState state = world.getBlockState(center);
		if (state == null || state.getDestroySpeed(world, center) > 2.0f)
			return false;
		
		for (int i = 0; i < xs.length; i++) {
			if (!checkPillar(world, center.offset(xs[i], 1, zs[i])))
				return false;
		}
		
		return true;
	}
	
	public static boolean spawnObelisk(Level world, BlockPos center) {
		if (!canSpawnObelisk(world, center)) {
			return false;
		}
		
		for (int i = 0; i < xs.length; i++) {
			spawnPillar(world, center.offset(xs[i], 1, zs[i]), corners[i]);
		}
		for (int i = -TILE_OFFSETH; i <= TILE_OFFSETH; i++)
			for (int j = -TILE_OFFSETH; j <= TILE_OFFSETH; j++) {
				world.setBlockAndUpdate(center.offset(i, 0, j), Blocks.OBSIDIAN.defaultBlockState());
			}
		for (int i = -1; i <= 1; i++) {
			int offset = (TILE_OFFSETH + 1);
			world.setBlockAndUpdate(center.offset(i, 0, offset), Blocks.OBSIDIAN.defaultBlockState());
			world.setBlockAndUpdate(center.offset(i, 0, -offset), Blocks.OBSIDIAN.defaultBlockState());
			world.setBlockAndUpdate(center.offset(offset, 0, i), Blocks.OBSIDIAN.defaultBlockState());
			world.setBlockAndUpdate(center.offset(-offset, 0, i), Blocks.OBSIDIAN.defaultBlockState());
		}
		world.setBlockAndUpdate(center, NostrumBlocks.obelisk.getMasterState());
		world.setBlockEntity(center, new ObeliskTileEntity(true));
		
		((ObeliskTileEntity) world.getBlockEntity(center)).init();
		
		return true;
	}
	
	private static boolean checkPillar(Level world, BlockPos center) {
		for (int i = 0; i < TILE_HEIGHT; i++) {
			if (!world.isEmptyBlock(center.offset(0, i, 0)))
				return false;
		}
		
		return true;
	}

	private static void spawnPillar(Level world, BlockPos center, Corner corner) {
		for (int i = 0; i < TILE_HEIGHT; i++) {
			BlockPos pos = center.offset(0, i, 0);
			if (i == TILE_OFFSETY - 1) {
				world.setBlockAndUpdate(pos, NostrumBlocks.obelisk.getTileState());
				world.setBlockEntity(pos, new ObeliskTileEntity(corner));
			} else {
				world.setBlockAndUpdate(pos, NostrumBlocks.obelisk.defaultBlockState());
			}
		}
	}
}
