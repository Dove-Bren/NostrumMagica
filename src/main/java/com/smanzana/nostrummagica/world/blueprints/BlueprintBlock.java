package com.smanzana.nostrummagica.world.blueprints;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.CandleBlock;
import com.smanzana.nostrummagica.block.IDirectionalBlock;
import com.smanzana.nostrummagica.block.IHorizontalBlock;
import com.smanzana.nostrummagica.block.ManiCrystalBlock;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.MimicOnesidedBlock;
import com.smanzana.nostrummagica.block.dungeon.SingleSpawnerBlock;
import com.smanzana.nostrummagica.tile.NostrumTileEntities;
import com.smanzana.nostrummagica.util.PortingUtil;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.block.RedstoneWallTorchBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BedPart;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;

public class BlueprintBlock {
		
	private static Map<String, Block> BLOCK_CACHE = new HashMap<>();
	private static Map<BlockState, BlueprintBlock> BLUEPRINT_CACHE = new HashMap<>();
	
	private static @Nullable Block CHECK_BLOCK_CACHE(String name) {
		return BLOCK_CACHE.get(name);
	}
	
	private static void SET_BLOCK_CACHE(String name, @Nonnull Block block) {
		BLOCK_CACHE.put(name, block);
	}
	
	private static BlueprintBlock CHECK_BLUEPRINT_CACHE(BlockState state) {
		return BLUEPRINT_CACHE.get(state);
	}
	
	private static void SET_BLUEPRINT_CACHE(BlockState state, BlueprintBlock block) {
		BLUEPRINT_CACHE.put(state, block);
	}
	
	public static BlueprintBlock getBlueprintBlock(BlockState state, CompoundNBT teData) {
		BlueprintBlock block = null;
		if (teData == null) {
			block = CHECK_BLUEPRINT_CACHE(state);
		}
		
		if (block == null) {
			block = new BlueprintBlock(state, teData);
			if (teData == null) {
				SET_BLUEPRINT_CACHE(state, block);
			}
		}
		
		return block;
	}
	
	private BlockState state;
	private CompoundNBT tileEntityData;
	
	private BlueprintBlock(BlockState state, CompoundNBT teData) {
		this.state = state;
		this.tileEntityData = teData;
		
		// Refuse to store air
		if (state != null && state.getBlock() == Blocks.AIR) {
			state = null;
			tileEntityData = null;
		}
	}
	
	public BlueprintBlock(IWorld world, BlockPos pos) {
		if (world.isAirBlock(pos)) {
			; //leave null
		} else {
			this.state = world.getBlockState(pos);
			TileEntity te = world.getTileEntity(pos);
			if (te != null) {
				this.tileEntityData = new CompoundNBT();
				te.write(this.tileEntityData);
			}
		}
	}
	
	public static BlueprintBlock MakeFromData(BlockState state, CompoundNBT teData) {
		return new BlueprintBlock(state, teData);
	}
	
	public static BlueprintBlock Air() {
		return new BlueprintBlock((BlockState) null, null);
	}
	
	protected void fixupOldTEs(String newID) {
		tileEntityData.putString("id", newID);
	}
	
	private static Map<Block, Map<Integer, BlockState>> OldMetaMap = null;
	
	private static final @Nullable BlockState GetBlockStateFromOldMeta(Block block, int meta) {
		if (OldMetaMap == null) {
			OldMetaMap = new HashMap<>();
			InitOldMetaBlockStates();
		}
		
		Map<Integer, BlockState> innerMap = OldMetaMap.get(block);
		if (innerMap == null) {
			return null;
		}
		
		return innerMap.get(Integer.valueOf(meta));
	}
	
	private static final void SetOldMetaBlockState(Block block, int meta, BlockState state) {
		Map<Integer, BlockState> innerMap = OldMetaMap.get(block);
		if (innerMap == null) {
			innerMap = new HashMap<>();
			OldMetaMap.put(block, innerMap);
		}
		
		innerMap.put(Integer.valueOf(meta), state);
	}
	
	private static final void InitOldMetaBlockStates() {
		// Has to be called later so that Mod blocks can be filled in
		
		// Note: bed color was in TE, so just assuming red for all (first 2 bits are SWNE)
		SetOldMetaBlockState(Blocks.RED_BED, 0, Blocks.RED_BED.getDefaultState().with(BedBlock.PART, BedPart.FOOT).with(BedBlock.HORIZONTAL_FACING, Direction.SOUTH)); // SOUTH
		SetOldMetaBlockState(Blocks.RED_BED, 1, Blocks.RED_BED.getDefaultState().with(BedBlock.PART, BedPart.FOOT).with(BedBlock.HORIZONTAL_FACING, Direction.WEST)); // WEST
		SetOldMetaBlockState(Blocks.RED_BED, 2, Blocks.RED_BED.getDefaultState().with(BedBlock.PART, BedPart.FOOT).with(BedBlock.HORIZONTAL_FACING, Direction.NORTH)); // NORTH
		SetOldMetaBlockState(Blocks.RED_BED, 3, Blocks.RED_BED.getDefaultState().with(BedBlock.PART, BedPart.FOOT).with(BedBlock.HORIZONTAL_FACING, Direction.EAST)); // EAST
		SetOldMetaBlockState(Blocks.RED_BED, 8, Blocks.RED_BED.getDefaultState().with(BedBlock.PART, BedPart.HEAD).with(BedBlock.HORIZONTAL_FACING, Direction.SOUTH)); // HEAD, SOUTH 
		SetOldMetaBlockState(Blocks.RED_BED, 9, Blocks.RED_BED.getDefaultState().with(BedBlock.PART, BedPart.HEAD).with(BedBlock.HORIZONTAL_FACING, Direction.WEST)); // HEAD, WEST
		SetOldMetaBlockState(Blocks.RED_BED, 10, Blocks.RED_BED.getDefaultState().with(BedBlock.PART, BedPart.HEAD).with(BedBlock.HORIZONTAL_FACING, Direction.NORTH)); // HEAD, NORTH
		SetOldMetaBlockState(Blocks.RED_BED, 11, Blocks.RED_BED.getDefaultState().with(BedBlock.PART, BedPart.HEAD).with(BedBlock.HORIZONTAL_FACING, Direction.EAST)); // HEAD, EAST
		SetOldMetaBlockState(Blocks.RED_CARPET, 14, Blocks.RED_CARPET.getDefaultState());
		
		// Chest meta is NSWE (0 and 1 are up and down)
		SetOldMetaBlockState(Blocks.CHEST, 2, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.NORTH));
		SetOldMetaBlockState(Blocks.CHEST, 3, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.SOUTH));
		SetOldMetaBlockState(Blocks.CHEST, 4, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.WEST));
		SetOldMetaBlockState(Blocks.CHEST, 5, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.EAST));
		SetOldMetaBlockState(Blocks.LADDER, 2, Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.NORTH));
		SetOldMetaBlockState(Blocks.LADDER, 3, Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.SOUTH));
		SetOldMetaBlockState(Blocks.LADDER, 4, Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.WEST));
		SetOldMetaBlockState(Blocks.LADDER, 5, Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.EAST));
		SetOldMetaBlockState(Blocks.LAVA, 1, Blocks.LAVA.getDefaultState().with(FlowingFluidBlock.LEVEL, 1));
		SetOldMetaBlockState(Blocks.LAVA, 2, Blocks.LAVA.getDefaultState().with(FlowingFluidBlock.LEVEL, 2));
		SetOldMetaBlockState(Blocks.LAVA, 3, Blocks.LAVA.getDefaultState().with(FlowingFluidBlock.LEVEL, 3));
		SetOldMetaBlockState(Blocks.LAVA, 8, Blocks.LAVA.getDefaultState().with(FlowingFluidBlock.LEVEL, 8));
		SetOldMetaBlockState(Blocks.LAVA, 9, Blocks.LAVA.getDefaultState().with(FlowingFluidBlock.LEVEL, 9));
		SetOldMetaBlockState(Blocks.LAVA, 10, Blocks.LAVA.getDefaultState().with(FlowingFluidBlock.LEVEL, 10));
		SetOldMetaBlockState(Blocks.LAVA, 11, Blocks.LAVA.getDefaultState().with(FlowingFluidBlock.LEVEL, 11));
		SetOldMetaBlockState(Blocks.NETHER_BRICK_STAIRS, 0, Blocks.NETHER_BRICK_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.FACING, Direction.EAST));
		SetOldMetaBlockState(Blocks.NETHER_BRICK_STAIRS, 1, Blocks.NETHER_BRICK_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.FACING, Direction.WEST));
		SetOldMetaBlockState(Blocks.NETHER_BRICK_STAIRS, 2, Blocks.NETHER_BRICK_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.FACING, Direction.SOUTH));
		SetOldMetaBlockState(Blocks.NETHER_BRICK_STAIRS, 3, Blocks.NETHER_BRICK_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.FACING, Direction.NORTH));
		SetOldMetaBlockState(Blocks.NETHER_BRICK_STAIRS, 4, Blocks.NETHER_BRICK_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.TOP).with(StairsBlock.FACING, Direction.EAST));
		SetOldMetaBlockState(Blocks.NETHER_BRICK_STAIRS, 5, Blocks.NETHER_BRICK_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.TOP).with(StairsBlock.FACING, Direction.WEST));
		SetOldMetaBlockState(Blocks.NETHER_BRICK_STAIRS, 6, Blocks.NETHER_BRICK_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.TOP).with(StairsBlock.FACING, Direction.SOUTH));
		SetOldMetaBlockState(Blocks.NETHER_BRICK_STAIRS, 7, Blocks.NETHER_BRICK_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.TOP).with(StairsBlock.FACING, Direction.NORTH));
		SetOldMetaBlockState(Blocks.QUARTZ_STAIRS, 0, Blocks.QUARTZ_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.FACING, Direction.EAST));
		SetOldMetaBlockState(Blocks.QUARTZ_STAIRS, 1, Blocks.QUARTZ_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.FACING, Direction.WEST));
		SetOldMetaBlockState(Blocks.QUARTZ_STAIRS, 2, Blocks.QUARTZ_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.FACING, Direction.SOUTH));
		SetOldMetaBlockState(Blocks.QUARTZ_STAIRS, 3, Blocks.QUARTZ_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.FACING, Direction.NORTH));
		SetOldMetaBlockState(Blocks.QUARTZ_STAIRS, 4, Blocks.QUARTZ_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.TOP).with(StairsBlock.FACING, Direction.EAST));
		SetOldMetaBlockState(Blocks.QUARTZ_STAIRS, 5, Blocks.QUARTZ_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.TOP).with(StairsBlock.FACING, Direction.WEST));
		SetOldMetaBlockState(Blocks.QUARTZ_STAIRS, 6, Blocks.QUARTZ_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.TOP).with(StairsBlock.FACING, Direction.SOUTH));
		SetOldMetaBlockState(Blocks.QUARTZ_STAIRS, 7, Blocks.QUARTZ_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.TOP).with(StairsBlock.FACING, Direction.NORTH));
		SetOldMetaBlockState(Blocks.REDSTONE_TORCH, 1, Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.EAST));
		SetOldMetaBlockState(Blocks.REDSTONE_TORCH, 2, Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.WEST));
		SetOldMetaBlockState(Blocks.REDSTONE_TORCH, 3, Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.SOUTH));
		SetOldMetaBlockState(Blocks.REDSTONE_TORCH, 4, Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.NORTH));
		SetOldMetaBlockState(Blocks.REDSTONE_TORCH, 5, Blocks.REDSTONE_TORCH.getDefaultState());
		SetOldMetaBlockState(Blocks.STONE_BRICK_STAIRS, 0, Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.FACING, Direction.EAST));
		SetOldMetaBlockState(Blocks.STONE_BRICK_STAIRS, 1, Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.FACING, Direction.WEST));
		SetOldMetaBlockState(Blocks.STONE_BRICK_STAIRS, 2, Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.FACING, Direction.SOUTH));
		SetOldMetaBlockState(Blocks.STONE_BRICK_STAIRS, 3, Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.FACING, Direction.NORTH));
		SetOldMetaBlockState(Blocks.STONE_BRICK_STAIRS, 4, Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.TOP).with(StairsBlock.FACING, Direction.EAST));
		SetOldMetaBlockState(Blocks.STONE_BRICK_STAIRS, 5, Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.TOP).with(StairsBlock.FACING, Direction.WEST));
		SetOldMetaBlockState(Blocks.STONE_BRICK_STAIRS, 6, Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.TOP).with(StairsBlock.FACING, Direction.SOUTH));
		SetOldMetaBlockState(Blocks.STONE_BRICK_STAIRS, 7, Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.HALF, Half.TOP).with(StairsBlock.FACING, Direction.NORTH));
		SetOldMetaBlockState(Blocks.STONE_SLAB, 5, Blocks.SMOOTH_STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM)); // var5 SMOOTH STONE bottom
		SetOldMetaBlockState(Blocks.STONE_SLAB, 7, Blocks.QUARTZ_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM)); // var7 QUARTZ bottom
		SetOldMetaBlockState(Blocks.STONE_SLAB, 13, Blocks.SMOOTH_STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP)); // SMOOTH STONE top
		SetOldMetaBlockState(Blocks.STONE, 6, Blocks.POLISHED_ANDESITE.getDefaultState());
		SetOldMetaBlockState(Blocks.STONE_BRICKS, 1, Blocks.MOSSY_STONE_BRICKS.getDefaultState());
		SetOldMetaBlockState(Blocks.TORCH, 1, Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.HORIZONTAL_FACING, Direction.EAST));
		SetOldMetaBlockState(Blocks.TORCH, 2, Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.HORIZONTAL_FACING, Direction.WEST));
		SetOldMetaBlockState(Blocks.TORCH, 3, Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.HORIZONTAL_FACING, Direction.SOUTH));
		SetOldMetaBlockState(Blocks.TORCH, 4, Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.HORIZONTAL_FACING, Direction.NORTH));
		SetOldMetaBlockState(Blocks.TORCH, 5, Blocks.TORCH.getDefaultState());
		SetOldMetaBlockState(Blocks.WATER, 1, Blocks.WATER.getDefaultState().with(FlowingFluidBlock.LEVEL, 1));
		SetOldMetaBlockState(Blocks.WATER, 8, Blocks.WATER.getDefaultState().with(FlowingFluidBlock.LEVEL, 8));
		SetOldMetaBlockState(Blocks.WATER, 9, Blocks.WATER.getDefaultState().with(FlowingFluidBlock.LEVEL, 9));
		SetOldMetaBlockState(Blocks.WHITE_WOOL, 11, Blocks.BLUE_WOOL.getDefaultState());
		SetOldMetaBlockState(Blocks.WHITE_WOOL, 13, Blocks.GREEN_WOOL.getDefaultState());
		SetOldMetaBlockState(Blocks.WHITE_WOOL, 14, Blocks.RED_WOOL.getDefaultState());
		SetOldMetaBlockState(Blocks.WHITE_WOOL, 15, Blocks.BLACK_WOOL.getDefaultState());
		SetOldMetaBlockState(NostrumBlocks.lightDungeonBlock, 1, NostrumBlocks.dungeonBlock.getDefaultState());
		SetOldMetaBlockState(NostrumBlocks.logicDoor, 0, NostrumBlocks.logicDoor.getSlaveState(Direction.SOUTH));
		SetOldMetaBlockState(NostrumBlocks.logicDoor, 1, NostrumBlocks.logicDoor.getMaster(Direction.SOUTH));
		SetOldMetaBlockState(NostrumBlocks.logicDoor, 2, NostrumBlocks.logicDoor.getSlaveState(Direction.WEST));
		SetOldMetaBlockState(NostrumBlocks.logicDoor, 3, NostrumBlocks.logicDoor.getMaster(Direction.WEST));
		SetOldMetaBlockState(NostrumBlocks.logicDoor, 4, NostrumBlocks.logicDoor.getSlaveState(Direction.NORTH));
		SetOldMetaBlockState(NostrumBlocks.logicDoor, 5, NostrumBlocks.logicDoor.getMaster(Direction.NORTH));
		SetOldMetaBlockState(NostrumBlocks.logicDoor, 6, NostrumBlocks.logicDoor.getSlaveState(Direction.EAST));
		SetOldMetaBlockState(NostrumBlocks.logicDoor, 7, NostrumBlocks.logicDoor.getMaster(Direction.EAST));
		SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 0, NostrumBlocks.kaniCrystalBlock.getDefaultState().with(ManiCrystalBlock.FACING, Direction.DOWN));
		SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 1, NostrumBlocks.kaniCrystalBlock.getDefaultState().with(ManiCrystalBlock.FACING, Direction.UP));
		SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 2, NostrumBlocks.kaniCrystalBlock.getDefaultState().with(ManiCrystalBlock.FACING, Direction.NORTH));
		SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 3, NostrumBlocks.kaniCrystalBlock.getDefaultState().with(ManiCrystalBlock.FACING, Direction.SOUTH));
		SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 4, NostrumBlocks.kaniCrystalBlock.getDefaultState().with(ManiCrystalBlock.FACING, Direction.WEST));
		SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 5, NostrumBlocks.kaniCrystalBlock.getDefaultState().with(ManiCrystalBlock.FACING, Direction.EAST));
		SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 8, NostrumBlocks.vaniCrystalBlock.getDefaultState().with(ManiCrystalBlock.FACING, Direction.DOWN));
		SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 9, NostrumBlocks.vaniCrystalBlock.getDefaultState().with(ManiCrystalBlock.FACING, Direction.UP));
		SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 10, NostrumBlocks.vaniCrystalBlock.getDefaultState().with(ManiCrystalBlock.FACING, Direction.NORTH));
		SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 11, NostrumBlocks.vaniCrystalBlock.getDefaultState().with(ManiCrystalBlock.FACING, Direction.SOUTH));
		SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 12, NostrumBlocks.vaniCrystalBlock.getDefaultState().with(ManiCrystalBlock.FACING, Direction.WEST));
		SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 13, NostrumBlocks.vaniCrystalBlock.getDefaultState().with(ManiCrystalBlock.FACING, Direction.EAST));
		// Note: Lying and saying all said unbreakable. Sorcery breakable ones are fine. Some in plant room are 'breakable' on accident.
		SetOldMetaBlockState(NostrumBlocks.mimicDoor, 3, NostrumBlocks.mimicDoorUnbreakable.getDefaultState().with(MimicOnesidedBlock.FACING, Direction.SOUTH));
		SetOldMetaBlockState(NostrumBlocks.mimicDoor, 4, NostrumBlocks.mimicDoorUnbreakable.getDefaultState().with(MimicOnesidedBlock.FACING, Direction.WEST));
		SetOldMetaBlockState(NostrumBlocks.mimicDoor, 5, NostrumBlocks.mimicDoorUnbreakable.getDefaultState().with(MimicOnesidedBlock.FACING, Direction.EAST));
		SetOldMetaBlockState(NostrumBlocks.mimicDoor, 8, NostrumBlocks.mimicDoorUnbreakable.getDefaultState().with(MimicOnesidedBlock.FACING, Direction.DOWN));
		SetOldMetaBlockState(NostrumBlocks.mimicDoor, 10, NostrumBlocks.mimicDoorUnbreakable.getDefaultState().with(MimicOnesidedBlock.FACING, Direction.NORTH));
		SetOldMetaBlockState(NostrumBlocks.mimicDoor, 11, NostrumBlocks.mimicDoorUnbreakable.getDefaultState().with(MimicOnesidedBlock.FACING, Direction.SOUTH));
		SetOldMetaBlockState(NostrumBlocks.mimicDoor, 12, NostrumBlocks.mimicDoorUnbreakable.getDefaultState().with(MimicOnesidedBlock.FACING, Direction.WEST));
		SetOldMetaBlockState(NostrumBlocks.mimicDoor, 13, NostrumBlocks.mimicDoorUnbreakable.getDefaultState().with(MimicOnesidedBlock.FACING, Direction.EAST));
		SetOldMetaBlockState(NostrumBlocks.mimicFacade, 2, NostrumBlocks.mimicFacadeUnbreakable.getDefaultState().with(MimicOnesidedBlock.FACING, Direction.NORTH));
		SetOldMetaBlockState(NostrumBlocks.mimicFacade, 3, NostrumBlocks.mimicFacadeUnbreakable.getDefaultState().with(MimicOnesidedBlock.FACING, Direction.SOUTH));
		SetOldMetaBlockState(NostrumBlocks.mimicFacade, 4, NostrumBlocks.mimicFacadeUnbreakable.getDefaultState().with(MimicOnesidedBlock.FACING, Direction.WEST));
		SetOldMetaBlockState(NostrumBlocks.mimicFacade, 10, NostrumBlocks.mimicFacadeUnbreakable.getDefaultState().with(MimicOnesidedBlock.FACING, Direction.NORTH));
		SetOldMetaBlockState(NostrumBlocks.mimicFacade, 12, NostrumBlocks.mimicFacadeUnbreakable.getDefaultState().with(MimicOnesidedBlock.FACING, Direction.WEST));
		SetOldMetaBlockState(NostrumBlocks.mimicFacade, 13, NostrumBlocks.mimicFacadeUnbreakable.getDefaultState().with(MimicOnesidedBlock.FACING, Direction.EAST));
		SetOldMetaBlockState(NostrumBlocks.candle, 5, NostrumBlocks.candle.getDefaultState().with(CandleBlock.FACING, Direction.NORTH).with(CandleBlock.LIT, true)); // 010 2
		SetOldMetaBlockState(NostrumBlocks.candle, 7, NostrumBlocks.candle.getDefaultState().with(CandleBlock.FACING, Direction.SOUTH).with(CandleBlock.LIT, true)); // 011 3
		SetOldMetaBlockState(NostrumBlocks.candle, 9, NostrumBlocks.candle.getDefaultState().with(CandleBlock.FACING, Direction.WEST).with(CandleBlock.LIT, true)); // 100 4
		SetOldMetaBlockState(NostrumBlocks.candle, 11, NostrumBlocks.candle.getDefaultState().with(CandleBlock.FACING, Direction.EAST).with(CandleBlock.LIT, true)); // 101 5
		SetOldMetaBlockState(NostrumBlocks.singleSpawner, 0, NostrumBlocks.singleSpawner.getDefaultState().with(SingleSpawnerBlock.MOB, SingleSpawnerBlock.Type.GOLEM_EARTH));
		SetOldMetaBlockState(NostrumBlocks.singleSpawner, 1, NostrumBlocks.singleSpawner.getDefaultState().with(SingleSpawnerBlock.MOB, SingleSpawnerBlock.Type.GOLEM_ENDER));
		SetOldMetaBlockState(NostrumBlocks.singleSpawner, 2, NostrumBlocks.singleSpawner.getDefaultState().with(SingleSpawnerBlock.MOB, SingleSpawnerBlock.Type.GOLEM_FIRE));
		SetOldMetaBlockState(NostrumBlocks.singleSpawner, 3, NostrumBlocks.singleSpawner.getDefaultState().with(SingleSpawnerBlock.MOB, SingleSpawnerBlock.Type.GOLEM_ICE));
		SetOldMetaBlockState(NostrumBlocks.singleSpawner, 4, NostrumBlocks.singleSpawner.getDefaultState().with(SingleSpawnerBlock.MOB, SingleSpawnerBlock.Type.GOLEM_LIGHTNING));
		SetOldMetaBlockState(NostrumBlocks.singleSpawner, 5, NostrumBlocks.singleSpawner.getDefaultState().with(SingleSpawnerBlock.MOB, SingleSpawnerBlock.Type.GOLEM_PHYSICAL));
		SetOldMetaBlockState(NostrumBlocks.singleSpawner, 6, NostrumBlocks.singleSpawner.getDefaultState().with(SingleSpawnerBlock.MOB, SingleSpawnerBlock.Type.GOLEM_WIND));
		SetOldMetaBlockState(NostrumBlocks.singleSpawner, 7, NostrumBlocks.singleSpawner.getDefaultState().with(SingleSpawnerBlock.MOB, SingleSpawnerBlock.Type.DRAGON_RED));
		SetOldMetaBlockState(NostrumBlocks.singleSpawner, 8, NostrumBlocks.singleSpawner.getDefaultState().with(SingleSpawnerBlock.MOB, SingleSpawnerBlock.Type.PLANT_BOSS));
		SetOldMetaBlockState(NostrumBlocks.progressionDoor, 0, NostrumBlocks.progressionDoor.getSlaveState(Direction.SOUTH));
		SetOldMetaBlockState(NostrumBlocks.progressionDoor, 1, NostrumBlocks.progressionDoor.getMaster(Direction.SOUTH));
		SetOldMetaBlockState(NostrumBlocks.progressionDoor, 2, NostrumBlocks.progressionDoor.getSlaveState(Direction.WEST));
		SetOldMetaBlockState(NostrumBlocks.progressionDoor, 3, NostrumBlocks.progressionDoor.getMaster(Direction.WEST));
		SetOldMetaBlockState(NostrumBlocks.progressionDoor, 4, NostrumBlocks.progressionDoor.getSlaveState(Direction.NORTH));
		SetOldMetaBlockState(NostrumBlocks.progressionDoor, 5, NostrumBlocks.progressionDoor.getMaster(Direction.NORTH));
		SetOldMetaBlockState(NostrumBlocks.progressionDoor, 6, NostrumBlocks.progressionDoor.getSlaveState(Direction.EAST));
		SetOldMetaBlockState(NostrumBlocks.progressionDoor, 7, NostrumBlocks.progressionDoor.getMaster(Direction.EAST));
		SetOldMetaBlockState(NostrumBlocks.sorceryPortal, 0, NostrumBlocks.sorceryPortal.getSlaveState());
		SetOldMetaBlockState(NostrumBlocks.sorceryPortal, 1, NostrumBlocks.sorceryPortal.getMaster());
	}
	
	private static Map<String, Block> OldBlockNameMap_1_12_2 = null;
	
	private static final @Nullable Block FindBlockWithOldName(String name) {
		if (OldBlockNameMap_1_12_2 == null) {
			OldBlockNameMap_1_12_2 = new HashMap<>();
			InitOldBlockNames();
		}
		return OldBlockNameMap_1_12_2.get(name);
	}
	
	private static final void InitOldBlockNames() {
		OldBlockNameMap_1_12_2.put("minecraft:bed", Blocks.RED_BED);
		OldBlockNameMap_1_12_2.put("minecraft:carpet", Blocks.RED_CARPET);
		OldBlockNameMap_1_12_2.put("minecraft:nether_brick", Blocks.NETHER_BRICKS);
		OldBlockNameMap_1_12_2.put("minecraft:red_nether_brick", Blocks.RED_NETHER_BRICKS);
		OldBlockNameMap_1_12_2.put("minecraft:stonebrick", Blocks.STONE_BRICKS);
		OldBlockNameMap_1_12_2.put("minecraft:web", Blocks.COBWEB);
		OldBlockNameMap_1_12_2.put("minecraft:wool", Blocks.WHITE_WOOL);
		OldBlockNameMap_1_12_2.put("nostrummagica:dungeon_block", NostrumBlocks.lightDungeonBlock);
		OldBlockNameMap_1_12_2.put("nostrummagica:mani_crystal", NostrumBlocks.maniCrystalBlock);
	}
	
	private static Map<String, String> OldTENameMap_1_12_2 = null;
	
	private static final @Nullable String FindTileEntityWithOldName(String name) {
		if (OldTENameMap_1_12_2 == null) {
			OldTENameMap_1_12_2 = new HashMap<>();
			InitOldTENames();
		}
		return OldTENameMap_1_12_2.get(name);
	}
	
	private static final void InitOldTENames() {
		OldTENameMap_1_12_2.put("nostrum_altar_te", TileEntityType.getId(NostrumTileEntities.AltarTileEntityType).toString());
		OldTENameMap_1_12_2.put("nostrum_mob_spawner_te", TileEntityType.getId(NostrumTileEntities.SingleSpawnerTileEntityType).toString());
		//OldTENameMap_1_12_2.put("nostrum_symbol_te", TileEntityType.getId(NostrumTileEntities.SymbolTileEntityType).toString());
		OldTENameMap_1_12_2.put("progression_door", TileEntityType.getId(NostrumTileEntities.ProgressionDoorTileEntityType).toString());
		OldTENameMap_1_12_2.put("sorcery_portal", TileEntityType.getId(NostrumTileEntities.SorceryPortalTileEntityType).toString());
		OldTENameMap_1_12_2.put("switch_block_tile_entity", TileEntityType.getId(NostrumTileEntities.SwitchBlockTileEntityType).toString());
		OldTENameMap_1_12_2.put("teleport_rune", TileEntityType.getId(NostrumTileEntities.TeleportRuneTileEntityType).toString());
	}
	
	public static final String NBT_BLOCK = "block_id";
	public static final String NBT_BLOCK_TYPE = "block_type";
	public static final String NBT_BLOCK_STATE = "block_meta";
	public static final String NBT_TILE_ENTITY = "te_data";
	public static final String NBT_BLOCKSTATE_TAG = "blockstate";
	
	@SuppressWarnings("deprecation")
	public static BlueprintBlock fromNBT(byte version, CompoundNBT nbt) {
		BlockState state = null;
		CompoundNBT teData = null;
		switch (version) {
		case 0:
//				state = Block.getStateById(nbt.getInt(NBT_BLOCK));
//				
//				// Block.getStateById defaults to air. Remove it!
//				if (state != null && state.getBlock() == Blocks.AIR) {
//					state = null;
//				}
//				
//				if (state != null && nbt.contains(NBT_TILE_ENTITY)) {
//					teData = nbt.getCompound(NBT_TILE_ENTITY);
//				}
//				break;
			// was int id for blockstate; deprecated (and not minecraft save portable)
			throw new RuntimeException("Blueprint block doesn't understand version " + version);
		case 1:
			// I don't remember what this version was
			throw new RuntimeException("Blueprint block doesn't understand version " + version);
		case 2:
			// Was block name + (int) meta saving.
			// No more meta, so this doesn't work anymore
			String type = nbt.getString(NBT_BLOCK_TYPE).toLowerCase();
			if (!type.isEmpty()) {
				Block block = CHECK_BLOCK_CACHE(type);
				if (block == null) {
					block = Registry.BLOCK.getOptional(new ResourceLocation(type)).orElse(null);
					
					// Another chance with manual fixup
					if (block == null) {
						block = FindBlockWithOldName(type);
					}
					
					SET_BLOCK_CACHE(type, block);
				}
				if (block != null) {
					int meta = nbt.getInt(NBT_BLOCK_STATE);
					
					// Always try a lookup even with a meta of 0 in case defaults have changed
					state = GetBlockStateFromOldMeta(block, meta);
					if (state == null) {
						if (meta != 0) {
							// Concerning, since the meta was expressing _something_
							NostrumMagica.logger.warn("Found blockstate (block=" + type + ", meta=" + meta + ") that we can't parse! Using default, but that's probably wrong!");
						}
						
						// Whether meta was 0 and we don't have an override because the current default is still good or not,
						// use default state for this block.
						state = block.getDefaultState();
					}
				} else {
					NostrumMagica.logger.warn("Couldn't find block for type: " + type);
				}
			}
			
			// Prevent air from getting in
			if (state != null && state.getBlock() == Blocks.AIR) {
				state = null;
			}
			
			if (state != null && nbt.contains(NBT_TILE_ENTITY)) {
				teData = nbt.getCompound(NBT_TILE_ENTITY);
				
				String id = teData.getString("id");
				// Fix casing
				if (!id.equals(id.toLowerCase())) {
					id = id.toLowerCase();
					teData.putString("id", id);
				}
				
				// Detect missing ones
				if (id != null) {
					Optional<TileEntityType<?>> teType = Registry.BLOCK_ENTITY_TYPE.getOptional(new ResourceLocation(id));
					if (!teType.isPresent()) {
						
						// Try to fix up
						String otherID = FindTileEntityWithOldName(id);
						if (otherID != null && Registry.BLOCK_ENTITY_TYPE.getOptional(new ResourceLocation(otherID)).isPresent()) {
							teData.putString("id", otherID);
						} else {
							NostrumMagica.logger.warn("Can't find tile entity with id: " + id);
						}
					}
				}
				
				// Fixup tile entities we know broke
				teData = PortingUtil.fixupTileEntity_12_2(teData);
			}
			break;
			// throw new RuntimeException("Blueprint block doesn't understand version " + version); TODO do this
//				state = null;
//				teData = null;
		case 3:
			state = NBTUtil.readBlockState(nbt.getCompound(NBT_BLOCKSTATE_TAG));
			
			if (state != null && nbt.contains(NBT_TILE_ENTITY)) {
				teData = nbt.getCompound(NBT_TILE_ENTITY);
			}
			break;
		default:
			throw new RuntimeException("Blueprint block doesn't understand version " + version);
		}
		
		return BlueprintBlock.getBlueprintBlock(state, teData);
	}
	
	public CompoundNBT toNBT() {
		CompoundNBT tag = new CompoundNBT();
		
		// Version 0
//			if (state != null) {
//				tag.putInt(NBT_BLOCK, Block.getStateId(state));
//				if (tileEntityData != null) {
//					tag.put(NBT_TILE_ENTITY, tileEntityData);
//				}
//			}
		
//			// Version 2
//			if (state != null) {
//				tag.putString(NBT_BLOCK_TYPE, state.getBlock().getRegistryName().toString());
//				tag.putInt(NBT_BLOCK_STATE, state.getBlock().getMetaFromState(state));
//				if (tileEntityData != null) {
//					tag.put(NBT_TILE_ENTITY, tileEntityData);
//				}
//			}
		
		// Version 3
		if (state != null) {
			tag.put(NBT_BLOCKSTATE_TAG, NBTUtil.writeBlockState(state));
			if (tileEntityData != null) {
				tag.put(NBT_TILE_ENTITY, tileEntityData);
			}
		}
		
		
		return tag;
	}
	
	private static Direction rotate(Direction in, Direction mod) {
		if (in != Direction.UP && in != Direction.DOWN) {
			int count = mod.getOpposite().getHorizontalIndex();
			while (count > 0) {
				count--;
				in = in.rotateY();
			}
		}
		
		return in;
	}
	
	public BlockState getSpawnState(Direction facing) {
//			if (state != null) {
//				BlockState placeState = state;
//				
//				if (facing != null && facing.getOpposite().getHorizontalIndex() != 0) {
//					
//					Block block = placeState.getBlock();
//					if (block instanceof HorizontalBlock) {
//						Direction cur = placeState.get(HorizontalBlock.FACING);
//						cur = rotate(cur, facing);
//						placeState = placeState.with(HorizontalBlock.FACING, cur);
//					} else if (block instanceof TorchBlock) {
//						Direction cur = placeState.get(TorchBlock.FACING);
//						cur = rotate(cur, facing);
//						placeState = placeState.with(TorchBlock.FACING, cur);
//					} else if (block instanceof LadderBlock) {
//						Direction cur = placeState.get(LadderBlock.FACING);
//						cur = rotate(cur, facing);
//						placeState = placeState.with(LadderBlock.FACING, cur);
//					} else if (block instanceof StairsBlock) {
//						Direction cur = placeState.get(StairsBlock.FACING);
//						cur = rotate(cur, facing);
//						placeState = placeState.with(StairsBlock.FACING, cur);
//					}
//				}
//				
//				world.setBlockState(at, placeState, 2);
//				if (tileEntityData != null) {
//					TileEntity te = TileEntity.create(world, tileEntityData.copy());
//					world.setTileEntity(at, te);
//				}
//			} else {
//				world.removeTileEntity(at);
//				world.setBlockToAir(at);
//			}
		
		if (state != null) {
			BlockState placeState = state;
			
			if (facing != null && facing.getOpposite().getHorizontalIndex() != 0) {
				
				Block block = placeState.getBlock();
				if (block instanceof HorizontalBlock) {
					Direction cur = placeState.get(HorizontalBlock.HORIZONTAL_FACING);
					cur = rotate(cur, facing);
					placeState = placeState.with(HorizontalBlock.HORIZONTAL_FACING, cur);
				} else if (block instanceof WallTorchBlock) {
					Direction cur = placeState.get(WallTorchBlock.HORIZONTAL_FACING);
					cur = rotate(cur, facing);
					placeState = placeState.with(WallTorchBlock.HORIZONTAL_FACING, cur);
				} else if (block instanceof RedstoneWallTorchBlock) {
					Direction cur = placeState.get(RedstoneWallTorchBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.with(RedstoneWallTorchBlock.FACING, cur);
				} else if (block instanceof LadderBlock) {
					Direction cur = placeState.get(LadderBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.with(LadderBlock.FACING, cur);
				} else if (block instanceof StairsBlock) {
					Direction cur = placeState.get(StairsBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.with(StairsBlock.FACING, cur);
				} else if (block instanceof DirectionalBlock) {
					// Only want to rotate horizontally
					Direction cur = placeState.get(DirectionalBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.with(DirectionalBlock.FACING, cur);
				} else if (block instanceof IHorizontalBlock) {
					// Only want to rotate horizontally
					Direction cur = placeState.get(IHorizontalBlock.HORIZONTAL_FACING);
					cur = rotate(cur, facing);
					placeState = placeState.with(IHorizontalBlock.HORIZONTAL_FACING, cur);
				} else if (block instanceof IDirectionalBlock) {
					// Only want to rotate horizontally
					Direction cur = placeState.get(IDirectionalBlock.FACING);
					cur = rotate(cur, facing);
					placeState = placeState.with(IDirectionalBlock.FACING, cur);
				}
			}
			
			return placeState;
		} else {
			return null;
		}
	}
	
	public CompoundNBT getTileEntityData() {
		return tileEntityData;
	}
	
	public boolean isDoorIndicator() {
		return state != null && state.getBlock() == Blocks.REPEATER;
	}
	
	public boolean isEntry() {
		return state != null && state.getBlock() == Blocks.COMPARATOR;
	}
	
	public Direction getFacing() {
		Direction ret = null;
		Block block = state.getBlock();
		if (block instanceof HorizontalBlock) {
			ret = state.get(HorizontalBlock.HORIZONTAL_FACING);
			
			// HACK: Reverse if special enterance block cause they're backwards LOL
			if (block instanceof RedstoneDiodeBlock || block instanceof ComparatorBlock) {
				ret = ret.getOpposite();
			}
			
		} else if (block instanceof WallTorchBlock) {
			ret = state.get(WallTorchBlock.HORIZONTAL_FACING);
		} else if (block instanceof RedstoneWallTorchBlock) {
			ret = state.get(RedstoneWallTorchBlock.FACING);
		} else if (block instanceof LadderBlock) {
			ret = state.get(LadderBlock.FACING);
		} else if (block instanceof StairsBlock) {
			ret = state.get(StairsBlock.FACING);
		}
		return ret;
	}
	
	public BlockState getState() {
		return this.state;
	}
	
	public @Nullable CompoundNBT getRawTileEntityData() {
		return this.tileEntityData;
	}
}
