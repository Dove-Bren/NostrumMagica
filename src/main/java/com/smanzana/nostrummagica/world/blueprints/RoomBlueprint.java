package com.smanzana.nostrummagica.world.blueprints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.blocks.IDirectionalBlock;
import com.smanzana.nostrummagica.blocks.IHorizontalBlock;
import com.smanzana.nostrummagica.blocks.ManiCrystal;
import com.smanzana.nostrummagica.blocks.MimicOnesidedBlock;
import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.blocks.NostrumSingleSpawner;
import com.smanzana.nostrummagica.tiles.IOrientedTileEntity;
import com.smanzana.nostrummagica.tiles.IUniqueDungeonTileEntity;
import com.smanzana.nostrummagica.tiles.NostrumTileEntities;
import com.smanzana.nostrummagica.utils.PortingUtil;
import com.smanzana.nostrummagica.utils.WorldUtil;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

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
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BedPart;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Contains all the data needed to spawn a room in the world
 * @author Skyler
 *
 */
public class RoomBlueprint {
	
	public static class BlueprintBlock {
		
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
			SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 0, NostrumBlocks.kaniCrystalBlock.getDefaultState().with(ManiCrystal.FACING, Direction.DOWN));
			SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 1, NostrumBlocks.kaniCrystalBlock.getDefaultState().with(ManiCrystal.FACING, Direction.UP));
			SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 2, NostrumBlocks.kaniCrystalBlock.getDefaultState().with(ManiCrystal.FACING, Direction.NORTH));
			SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 3, NostrumBlocks.kaniCrystalBlock.getDefaultState().with(ManiCrystal.FACING, Direction.SOUTH));
			SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 4, NostrumBlocks.kaniCrystalBlock.getDefaultState().with(ManiCrystal.FACING, Direction.WEST));
			SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 5, NostrumBlocks.kaniCrystalBlock.getDefaultState().with(ManiCrystal.FACING, Direction.EAST));
			SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 8, NostrumBlocks.vaniCrystalBlock.getDefaultState().with(ManiCrystal.FACING, Direction.DOWN));
			SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 9, NostrumBlocks.vaniCrystalBlock.getDefaultState().with(ManiCrystal.FACING, Direction.UP));
			SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 10, NostrumBlocks.vaniCrystalBlock.getDefaultState().with(ManiCrystal.FACING, Direction.NORTH));
			SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 11, NostrumBlocks.vaniCrystalBlock.getDefaultState().with(ManiCrystal.FACING, Direction.SOUTH));
			SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 12, NostrumBlocks.vaniCrystalBlock.getDefaultState().with(ManiCrystal.FACING, Direction.WEST));
			SetOldMetaBlockState(NostrumBlocks.maniCrystalBlock, 13, NostrumBlocks.vaniCrystalBlock.getDefaultState().with(ManiCrystal.FACING, Direction.EAST));
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
			SetOldMetaBlockState(NostrumBlocks.candle, 5, NostrumBlocks.candle.getDefaultState().with(Candle.FACING, Direction.NORTH).with(Candle.LIT, true)); // 010 2
			SetOldMetaBlockState(NostrumBlocks.candle, 7, NostrumBlocks.candle.getDefaultState().with(Candle.FACING, Direction.SOUTH).with(Candle.LIT, true)); // 011 3
			SetOldMetaBlockState(NostrumBlocks.candle, 9, NostrumBlocks.candle.getDefaultState().with(Candle.FACING, Direction.WEST).with(Candle.LIT, true)); // 100 4
			SetOldMetaBlockState(NostrumBlocks.candle, 11, NostrumBlocks.candle.getDefaultState().with(Candle.FACING, Direction.EAST).with(Candle.LIT, true)); // 101 5
			SetOldMetaBlockState(NostrumBlocks.singleSpawner, 0, NostrumBlocks.singleSpawner.getDefaultState().with(NostrumSingleSpawner.MOB, NostrumSingleSpawner.Type.GOLEM_EARTH));
			SetOldMetaBlockState(NostrumBlocks.singleSpawner, 1, NostrumBlocks.singleSpawner.getDefaultState().with(NostrumSingleSpawner.MOB, NostrumSingleSpawner.Type.GOLEM_ENDER));
			SetOldMetaBlockState(NostrumBlocks.singleSpawner, 2, NostrumBlocks.singleSpawner.getDefaultState().with(NostrumSingleSpawner.MOB, NostrumSingleSpawner.Type.GOLEM_FIRE));
			SetOldMetaBlockState(NostrumBlocks.singleSpawner, 3, NostrumBlocks.singleSpawner.getDefaultState().with(NostrumSingleSpawner.MOB, NostrumSingleSpawner.Type.GOLEM_ICE));
			SetOldMetaBlockState(NostrumBlocks.singleSpawner, 4, NostrumBlocks.singleSpawner.getDefaultState().with(NostrumSingleSpawner.MOB, NostrumSingleSpawner.Type.GOLEM_LIGHTNING));
			SetOldMetaBlockState(NostrumBlocks.singleSpawner, 5, NostrumBlocks.singleSpawner.getDefaultState().with(NostrumSingleSpawner.MOB, NostrumSingleSpawner.Type.GOLEM_PHYSICAL));
			SetOldMetaBlockState(NostrumBlocks.singleSpawner, 6, NostrumBlocks.singleSpawner.getDefaultState().with(NostrumSingleSpawner.MOB, NostrumSingleSpawner.Type.GOLEM_WIND));
			SetOldMetaBlockState(NostrumBlocks.singleSpawner, 7, NostrumBlocks.singleSpawner.getDefaultState().with(NostrumSingleSpawner.MOB, NostrumSingleSpawner.Type.DRAGON_RED));
			SetOldMetaBlockState(NostrumBlocks.singleSpawner, 8, NostrumBlocks.singleSpawner.getDefaultState().with(NostrumSingleSpawner.MOB, NostrumSingleSpawner.Type.PLANT_BOSS));
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
			OldTENameMap_1_12_2.put("nostrum_symbol_te", TileEntityType.getId(NostrumTileEntities.SymbolTileEntityType).toString());
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
	}
	
	public static final class SpawnContext {
		
		public final IWorld world;
		public final BlockPos at;
		public final Direction direction;
		public final @Nullable MutableBoundingBox bounds;
		public final UUID globalID;
		public final UUID roomID;
		
		public SpawnContext(IWorld world, BlockPos pos, Direction direction, @Nullable MutableBoundingBox bounds, UUID globalID, UUID roomID) {
			this.world = world;
			this.at = pos;
			this.direction = direction;
			this.bounds = bounds;
			this.globalID = globalID;
			this.roomID = roomID;
		}
		
		public SpawnContext(IWorld world, BlockPos pos, Direction direction, @Nullable MutableBoundingBox bounds, UUID globalID) {
			this(world, pos, direction, bounds, globalID, UUID.randomUUID());
		}
	}
	
	public static final String NBT_DIMS = "dimensions";
	public static final String NBT_WHOLE_DIMS = "master_dimensions";
	public static final String NBT_BLOCK_LIST = "blocks";
	public static final String NBT_DOOR_LIST = "doors";
	public static final String NBT_ENTRY = "entry";
	public static final String NBT_VERSION = "version";
	public static final String NBT_ENTITIES = "entities";
	public static final String NBT_PIECE_OFFSET = "part_offset";
	public static final String NBT_PIECE_COMPOSITE = "composite_marker";
	public static final int MAX_BLUEPRINT_BLOCKS = 32 * 32 * 32;
	
	private BlockPos dimensions;
	private BlueprintBlock[] blocks;
	private DungeonExitPoint entry;
	private Set<DungeonExitPoint> doors;
	private int partOffset; // Only used for fragments
	
	// Overriding/wrapping interface
	protected IBlueprintSpawner spawnerFunc;
	
	// Cached sublist of blocks for previews (5x2x5)
	private BlueprintBlock[][][] previewBlocks = new BlueprintBlock[5][2][5];
	
	
	public RoomBlueprint(BlockPos dimensions, BlueprintBlock[] blocks, Set<DungeonExitPoint> exits, DungeonExitPoint entry) {
		if (dimensions != null && blocks != null
				&& (dimensions.getX() * dimensions.getY() * dimensions.getZ() != blocks.length)) {
			throw new RuntimeException("Dimensions do not match block array provided to blueprint constructor!");
		}
		this.dimensions = dimensions;
		this.blocks = blocks;
		this.doors = exits == null ? new HashSet<>() : exits;
		this.entry = entry;

		refreshPreview();
	}
	
	/**
	 * Scans the world between two block positions and captures all blocks, blockstates, and tile entities
	 * to save as a blueprint.
	 * @param world
	 * @param pos1
	 * @param pos2
	 * @param usePlaceholders if true, special blocks to mark entries and doors (For dungeon genning) are detected. Otherwise, just a regular room.
	 */
	public RoomBlueprint(IWorld world, BlockPos pos1, BlockPos pos2, boolean usePlaceholders) {
		this(world, pos1, pos2, usePlaceholders, null, null);
	}
	
	public RoomBlueprint(IWorld world, BlockPos pos1, BlockPos pos2, boolean usePlaceholders, BlockPos origin, Direction originDir) {
		this(null, null, null, null);
		
		BlockPos low = new BlockPos(pos1.getX() < pos2.getX() ? pos1.getX() : pos2.getX(),
				pos1.getY() < pos2.getY() ? pos1.getY() : pos2.getY(),
				pos1.getZ() < pos2.getZ() ? pos1.getZ() : pos2.getZ());
		if (!low.equals(pos1)) {
			BlockPos high = new BlockPos(pos1.getX() > pos2.getX() ? pos1.getX() : pos2.getX(),
					pos1.getY() > pos2.getY() ? pos1.getY() : pos2.getY(),
					pos1.getZ() > pos2.getZ() ? pos1.getZ() : pos2.getZ());
			pos1 = low;
			pos2 = high;
		}
		
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		this.dimensions = new BlockPos(
				1 + (pos2.getX() - pos1.getX()),
				1 + (pos2.getY() - pos1.getY()),
				1 + (pos2.getZ() - pos1.getZ()));
		final int width = dimensions.getX();
		final int height = dimensions.getY();
		final int length = dimensions.getZ();
		this.blocks = new BlueprintBlock[width * height * length];
		final List<DungeonExitPoint> doorsRaw = new ArrayList<>();
		
		int slot = 0;
		for (int i = 0; i < width; i++)
		for (int j = 0; j < height; j++)
		for (int k = 0; k < length; k++) {
			cursor.setPos(pos1.getX() + i, pos1.getY() + j, pos1.getZ() + k);
			
			if (cursor.getX() == 120320 && cursor.getZ() == 673280) {
				NostrumMagica.logger.info(".");
			}
			
			BlueprintBlock block = new BlueprintBlock(world, cursor);
			
			// Maybe check for blocks th at actually indicate entries and exits
			if (usePlaceholders) {
				if (block.isDoorIndicator()) {
					doorsRaw.add(new DungeonExitPoint(cursor.toImmutable().subtract(pos1), block.getFacing().getOpposite()));
					block = new BlueprintBlock((BlockState) null, null); // Make block an air one
				} else if (block.isEntry()) {
					if (this.entry != null) {
						NostrumMagica.logger.error("Found multiple entry points to room while creating blueprint!");
					}
					this.entry = new DungeonExitPoint(cursor.toImmutable().subtract(pos1), block.getFacing());
					block = new BlueprintBlock((BlockState) null, null); // Make block an air one
				}
			}
			
			blocks[slot++] = block;
		}
		
		// If no placeholders, center xz in the blueprint
		if (origin != null && originDir != null) {
			if (this.entry != null) {
				NostrumMagica.logger.error("Had a legit entry point but stamping another in");
			}
			this.entry = new DungeonExitPoint(origin, originDir);
		}
		
		if (this.entry == null && !usePlaceholders) {
			this.entry = new DungeonExitPoint(new BlockPos(width / 2, 0, length / 2), Direction.NORTH);
		}
		
		// Adjust found doors to be offsets from entry
		if (entry != null) {
			for (DungeonExitPoint door : doorsRaw) {
				doors.add(new DungeonExitPoint(
						door.getPos().subtract(entry.getPos()),
						door.getFacing()
						));
			}
		}
		refreshPreview();
	}
	
	protected void refreshPreview() {
		// Get preview based on 'entry' origin point and blocks around it
		if (dimensions != null) {
			BlockPos offset = (entry == null ? BlockPos.ZERO : entry.getPos());
			for (int xOff = -2; xOff <= 2; xOff++)
			for (int yOff = 0; yOff <= 1; yOff++)
			for (int zOff = -2; zOff <= 2; zOff++) {
				final int x = xOff + offset.getX();
				final int y = yOff + offset.getY();
				final int z = zOff + offset.getZ();
				
				if (x < 0 || y < 0 || z < 0
					|| x >= dimensions.getX()
					|| y >= dimensions.getY()
					|| z >= dimensions.getZ()) {
					previewBlocks[xOff + 2][yOff][zOff + 2] = null;
					continue;
				}
				
				final int bIndex = (x * dimensions.getY() * dimensions.getZ())
						+ (y * dimensions.getZ())
						+ z;
				if (bIndex < 0 || bIndex >= blocks.length) {
					previewBlocks[xOff + 2][yOff][zOff + 2] = null;
				} else {
					previewBlocks[xOff + 2][yOff][zOff + 2] =
							blocks[bIndex];
				}
			}
		}
	}
	
	public void setSpawningFunc(@Nullable IBlueprintSpawner spawner) {
		this.spawnerFunc = spawner;
	}
	
	public static Direction getModDir(Direction original, Direction newFacing) {
		Direction out = Direction.NORTH;
		int rotCount = (4 + newFacing.getHorizontalIndex() - original.getHorizontalIndex()) % 4;
		while (rotCount-- > 0) {
			out = out.rotateY();
		}
		return out;
	}
	
	public static BlockPos applyRotation(BlockPos input, Direction modDir) {
		int x = 0;
		int z = 0;
		final int dx = input.getX();
		final int dz = input.getZ();
		switch (modDir) {
		case DOWN:
		case UP:
		case NORTH:
			 x = dx;
			 z = dz;
			break;
		case EAST:
			// Single rotation: (-z, x)
			x = -dz;
			z = dx;
			break;
		case SOUTH:
			// Double rotation: (-x, -z)
			x = -dx;
			z = -dz;
			break;
		case WEST:
			// Triple: (z, -x)
			x = dz;
			z = -dx;
			break;
		}
		return new BlockPos(x, input.getY(), z);
	}
	
	private static final boolean isSecondPassBlock(BlueprintBlock block) {
		if (block.state != null) {
			//if (!(block.state.isFullBlock() || block.state.isOpaqueCube() || block.state.isNormalCube())) {
			if (!block.state.isSolid()) {
				return true;
			}
		}
		
		return false;
	}
	
	protected void placeBlock(SpawnContext context, BlockPos at, Direction direction, BlueprintBlock block) {
		
		final boolean worldGen = (context.world instanceof WorldGenRegion);
		
		if (spawnerFunc != null) {
			spawnerFunc.spawnBlock(context, at, direction, block);
		} else {
			BlockState placeState = block.getSpawnState(direction);
			if (placeState != null) {
				// TODO: add fluid state support
				context.world.setBlockState(at, placeState, 2);
				if (WorldUtil.blockNeedsGenFixup(block.state)) {
					if (worldGen) {
						context.world.getChunk(at).markBlockForPostprocessing(at);
					} else {
						BlockState blockstate = context.world.getBlockState(at);
						BlockState blockstate1 = Block.getValidBlockForPosition(blockstate, context.world, at);
						context.world.setBlockState(at, blockstate1, 20);
					}
				}
				
				CompoundNBT tileEntityData = block.getTileEntityData();
				if (tileEntityData != null) {
					TileEntity te = TileEntity.readTileEntity(placeState, tileEntityData.copy());
					if (te == null) {
						// Before 1.12.2 ids weren't namespaced. Now they are. Check if it's an old Nostrum TE
						final CompoundNBT copy = tileEntityData.copy();
						final String newID = NostrumMagica.MODID + ":" + copy.getString("id");
						copy.putString("id", newID);
						te = TileEntity.readTileEntity(placeState, copy);
						if (te != null) {
							block.fixupOldTEs(newID);
						}
					}
					
					if (te != null) {
						if (worldGen || !(context.world instanceof IServerWorld)) {
							context.world.getChunk(at).addTileEntity(at, te);
						} else {
							((IServerWorld) context.world).getWorld().setTileEntity(at, te);
						}
						if (te instanceof IOrientedTileEntity) {
							// Let tile ent respond to rotation
							((IOrientedTileEntity) te).setSpawnedFromRotation(direction, worldGen);
						}
						if (te instanceof IUniqueDungeonTileEntity) {
							((IUniqueDungeonTileEntity) te).onDungeonSpawn(context.globalID, context.roomID, worldGen);
						}
					} else {
						NostrumMagica.logger.error("Could not deserialize TileEntity with id \"" + tileEntityData.getString("id") + "\"");
					}
				}
			} else {
				//world.removeBlock(at, false);
				context.world.setBlockState(at, Blocks.AIR.getDefaultState(), 2);
			}
		}
	}
	
	public void spawn(IWorld world, BlockPos at, UUID globalID) {
		this.spawn(world, at, Direction.NORTH, globalID);
	}
	
	public void spawn(IWorld world, BlockPos at, Direction direction, UUID globalID) {
		this.spawn(world, at, direction, (MutableBoundingBox) null, globalID);
	}
	
	public void spawn(IWorld world, BlockPos at, Direction direction, @Nullable MutableBoundingBox bounds, UUID globalID) {
		
		SpawnContext context = new SpawnContext(world, at, direction, bounds, globalID);
		
		final int width = dimensions.getX();
		final int height = dimensions.getY();
		final int length = dimensions.getZ();
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		Direction modDir = Direction.NORTH; // 0
		
		// Apply rotation changes
		if (this.entry != null) {
			// Get facing mod
			modDir = getModDir(entry.getFacing(), direction);
		}
		
		BlockPos adjustedDims;
		{
			BlockPos wrapper = new BlockPos(width, 0, length);
			adjustedDims = applyRotation(wrapper, modDir);
			
			// Note: Some vals may be negative. We leave them for now to get proper offset adjustement, and then
			// straighten them out for later calcs
		}
		
		// Outside loop for each chunk
		// Inner loop loops i, j, k to 0 to < 16 in each
		// Loops basically always go from 0 to 16 except in edge ones I suppose
		BlockPos offset = entry == null ? new BlockPos(0,0,0) : entry.getPos();
		BlockPos unit = applyRotation(new BlockPos(1, 0, 1), modDir);
		
		// To get actual least-x leastz coordinates, we need to add offset rotated to proper orientation
		BlockPos origin;
		{
			BlockPos.Mutable rotOffset = new BlockPos.Mutable().setPos(applyRotation(offset, modDir));
			//BlockPos rotOffset = applyRotation(offset, modDir.getHorizontalIndex() % 2 == 1 ? modDir.getOpposite() : modDir);
			
			// To get proper offset, need to move so that our old origin (0,0) is at the real one, which is encoded in our adjusted dimensions
			// Negative values there mean we need to shift our offset
			if (unit.getX() < 0 || unit.getZ() < 0) {
				int px = Math.abs(adjustedDims.getX());
				int pz = Math.abs(adjustedDims.getZ());
				
				if (adjustedDims.getX() < 0) {
					rotOffset.setPos(rotOffset.getX() + (px - 1), rotOffset.getY(), rotOffset.getZ());
				}
				
				if (adjustedDims.getZ() < 0) {
					rotOffset.setPos(rotOffset.getX(), rotOffset.getY(), rotOffset.getZ() + (pz - 1));
				}
				
				adjustedDims = new BlockPos(px, adjustedDims.getY(), pz);
			}
			
			origin = at.toImmutable().subtract(rotOffset);
		}
		final int chunkStartX = origin.getX() >> 4;
		final int chunkStartZ = origin.getZ() >> 4;
		final int chunkRemX = ((origin.getX() % 16) + 16) % 16;
		final int chunkRemZ = ((origin.getZ() % 16) + 16) % 16;
		
		// To get number of chunks in either direction we're going to go, we have to apply rotation to width and height
		// Note: add 'how far in the chunk we start' to width and height to find actual number of chunks affected
		final int numChunkX = (adjustedDims.getX() + chunkRemX) / 16;
		final int numChunkZ = (adjustedDims.getZ() + chunkRemZ) / 16;
		
		// Have a list for second-pass block placement
		List<BlueprintBlock> secondPassBlocks = new ArrayList<>(16 * 4);
		List<BlockPos> secondPassPos = new ArrayList<>(16 * 4);
		
		// Data has inverse rotation
		final Direction dataDir = modDir.getHorizontalIndex() % 2 == 1 ? modDir.getOpposite() : modDir;
		
		// Loop over all chunks from <x to >x (and <z to >z)
		for (int cx = 0; cx <= numChunkX; cx++)
		for (int cz = 0; cz <= numChunkZ; cz++) {
			final int chunkOffsetX = (cx + chunkStartX) * 16;
			final int chunkOffsetZ = (cz + chunkStartZ) * 16;
			final int startX = (cx == 0 ? chunkRemX : 0);
			final int startZ = (cz == 0 ? chunkRemZ : 0);
			final int endX = (cx == numChunkX ? (((origin.getX() + adjustedDims.getX()) % 16) + 16) % 16 : 16);
			final int endZ = (cz == numChunkZ ? (((origin.getZ() + adjustedDims.getZ()) % 16) + 16) % 16 : 16);
			
			
			
			// For each chunk, loop over x, y, z and spawn templated blocks
			// On low boundary chunks, start i and k and chunkRemX/Z
			// For high boundary chunks, cap i and k and (width or length / 16)
			secondPassBlocks.clear();
			secondPassPos.clear();
			for (int i = startX; i < endX; i++)
			for (int j = 0; j < height; j++)
			for (int k = startZ; k < endZ; k++) {
				// IWorld pos is simply chunk position + inner loop offset
				// (j is data y, so offset to world y)
				cursor.setPos(chunkOffsetX + i, j + origin.getY(), chunkOffsetZ + k);
				
				if (bounds != null && !bounds.isVecInside(cursor)) {
					continue;
				}
				
				// Find data position by applying rotation to transform x and z coords into u and v data coords
				unit = applyRotation(new BlockPos(1, 0, 1), dataDir);
				BlockPos dataPos = applyRotation(cursor.toImmutable().subtract(origin), dataDir);
				
				// Negative values here, though, imply reflection. So make "-x" be "max - x"
				if (unit.getX() < 0 || unit.getZ() < 0) {
					// If negative, shift by dimension size (-1 cause 0 offset)
					int px = dataPos.getX();
					int pz = dataPos.getZ();
					
					// if x < 0, make into width-x
					if (unit.getX() < 0) {
						px = width + (px - 1);
					}
					
					// ...
					if (unit.getZ() < 0) {
						pz = length + (pz - 1);
					}
					
					dataPos = new BlockPos(px, dataPos.getY(), pz);
				}
				
				BlueprintBlock block = blocks[
   				       (dataPos.getX() * length * height)
   				       + (j * length)
   				       + dataPos.getZ()
   				       ];
				
				// Either set the block now, or set it up to be placed later (if it might break based on order)
				if (isSecondPassBlock(block)) {
					secondPassBlocks.add(block);
					secondPassPos.add(new BlockPos(cursor));
				} else {
					placeBlock(context, cursor, modDir, block);
				}
			}
			
			// Spawn all second-pass blocks
			for (int i = 0; i < secondPassBlocks.size(); i++) {
				BlockPos secondPos = secondPassPos.get(i);
				BlueprintBlock block = secondPassBlocks.get(i);
				placeBlock(context, secondPos, modDir, block);
			}
			
			world.getChunk(cursor).setModified(true);
		}
	}
	
	public BlockPos getDimensions() {
		return this.dimensions;
	}
	
	/**
	 * Returns total space dimensions of the blueprint, if it were rotated to the desired facing.
	 * @param facing The desired facing for the entry way, if there is one.
	 * @return
	 */
	public BlockPos getAdjustedDimensions(Direction facing) {
		Direction mod = getModDir(entry == null ? Direction.NORTH : entry.getFacing(), facing);
		int width = dimensions.getX();
		int height = dimensions.getY();
		int length = dimensions.getZ();
		
		switch (mod) {
		case DOWN:
		case UP:
		case NORTH:
			break;
		case EAST:
			// Single rotation: (-z, x)
			width = -dimensions.getZ();
			length = dimensions.getX();
			break;
		case SOUTH:
			// Double rotation: (-x, -z)
			width = -dimensions.getX();
			length = -dimensions.getZ();
			break;
		case WEST:
			// Triple: (z, -x)
			width = dimensions.getZ();
			length = -dimensions.getX();
			break;
		}
		
		return new BlockPos(width, height, length);
	}
	
	/**
	 * Returns entry point offsets when blueprint is rotated to the desired facing.
	 * @param facing
	 * @return
	 */
	public BlockPos getAdjustedOffset(Direction facing) {
		BlockPos offset = entry == null ? new BlockPos(0,0,0) : entry.getPos().toImmutable();
		Direction mod = getModDir(entry == null ? Direction.NORTH : entry.getFacing(), facing);
		
		int x = offset.getX();
		int y = offset.getY();
		int z = offset.getZ();
		
		switch (mod) {
		case DOWN:
		case UP:
		case NORTH:
			break;
		case EAST:
			// Single rotation: (-z, x)
			x = -offset.getZ();
			z = offset.getX();
			break;
		case SOUTH:
			// Double rotation: (-x, -z)
			x = -offset.getX();
			z = -offset.getZ();
			break;
		case WEST:
			// Triple: (z, -x)
			x = offset.getZ();
			z = -offset.getX();
			break;
		}
		
		return new BlockPos(x, y, z);
	}
	
	public Collection<DungeonExitPoint> getExits() {
		return this.doors;
	}
	
	public DungeonExitPoint getEntry() {
		return this.entry;
	}
	
	/**
	 * Returns a preview of the blueprint centered around the blueprint entry point.
	 * Note that the preview is un-rotated. You must rotate yourself if you want that.
	 * Also note that this is a 5x2x5 preview.
	 * And again, Note that air blocks and blocks outside the template are null in the arrays.
	 * @return
	 */
	public BlueprintBlock[][][] getPreview() {
		return this.previewBlocks;
	}
	
	/**
	 * Adds one blueprint to the other. Returns the original blueprint modified to include blocks from the other.
	 * This SHOULD be compatible with completely different blueprints.
	 * For now, all I'm implementing is piecing blueprints broken apart with {@link #toNBTWithBreakdown()}, where
	 * all blueprints passed in should be within the dimensions of the original.
	 * @param blueprint
	 * @return
	 */
	public RoomBlueprint join(RoomBlueprint blueprint) {
		// TODO expand this to accept different ones!
		
		final long start = System.currentTimeMillis();
		
		if (blueprint.dimensions.getZ() != dimensions.getZ()
				|| blueprint.dimensions.getY() != dimensions.getY()) {
			throw new RuntimeException("Can't combine blueprints that don't have the same base size!");
		}
		
		if (!blueprint.entry.equals(entry)) {
			throw new RuntimeException("Arbitrary blueprint joining not implemented!");
		}
		
		if (blueprint.partOffset == 0) {
			throw new RuntimeException("Can only join sub-blueprints");
		}
		
		final int base = dimensions.getY() * dimensions.getZ();
		final int count = blueprint.dimensions.getX() * base;
		final int offset = blueprint.partOffset * base;
		System.arraycopy(blueprint.blocks, 0, blocks, offset, Math.min(count, blocks.length - offset));
		
		final long now = System.currentTimeMillis();
		if (now - start > 100) {
			NostrumMagica.logger.info("Joining to took " + (now - start) + "ms!");
		}
		
		return this;
	}
	
	public boolean shouldSplit() {
		return dimensions.getX() * dimensions.getY() * dimensions.getZ() > MAX_BLUEPRINT_BLOCKS; 
	}
	
	public static interface BlueprintScanner {
		public void scan(BlockPos offset, BlueprintBlock block);
	}
	
	public void scanBlocks(BlueprintScanner scanner) {
		int slot = 0;
		final int width = this.dimensions.getX();
		final int height = this.dimensions.getY();
		final int length = this.dimensions.getZ();
		final BlockPos origin = this.entry.getPos();
		for (int i = 0; i < width; i++)
		for (int j = 0; j < height; j++)
		for (int k = 0; k < length; k++) {
			BlockPos offsetOrigOrient = new BlockPos(i, j, k).subtract(origin);
			BlockPos offsetNorthOrient = applyRotation(offsetOrigOrient, 
						getModDir(Direction.NORTH, this.entry.getFacing())
					); // rotate to north
			scanner.scan(offsetNorthOrient, blocks[slot++]);
		}
	}
	
	private static class BlueprintSavedTE {
		
		public static final String NBT_DATA = "te_data";
		public static final String NBT_POS = "pos";
		
		public CompoundNBT nbtTagData;
		public BlockPos pos;
		
		public BlueprintSavedTE(CompoundNBT data, BlockPos pos) {
			this.nbtTagData = data;
			this.pos = pos;
		}
		
		@SuppressWarnings("unused")
		public CompoundNBT toNBT() {
			CompoundNBT tag = new CompoundNBT();
			tag.put(NBT_DATA, nbtTagData);
			tag.putLong(NBT_POS, pos.toLong());
			return tag;
		}
		
		public static BlueprintSavedTE fromNBT(CompoundNBT nbt) {
			long pos = nbt.getLong(NBT_POS);
			return new BlueprintSavedTE(nbt.getCompound(NBT_DATA), WorldUtil.blockPosFromLong1_12_2(pos));
		}
	}
	
	private static RoomBlueprint deserializeNBTStyleInternal(CompoundNBT nbt, byte version) {
		BlockPos dims = NBTUtil.readBlockPos(nbt.getCompound(NBT_DIMS));
		// When breaking blueprints into pieces, the first one has an actual copy of the real size of the whole thing.
		// If we have one of those, allocate the FULL array size instead of the small one
		BlockPos masterDims = NBTUtil.readBlockPos(nbt.getCompound(NBT_WHOLE_DIMS));
		BlueprintBlock[] blocks = null;
		Set<DungeonExitPoint> doors = null;
		DungeonExitPoint entry = null;
		
		if (dims.distanceSq(0, 0, 0, false) == 0) {
			return null;
		} else {
			ListNBT list = nbt.getList(NBT_BLOCK_LIST, NBT.TAG_COMPOUND);
			
			final int count = dims.getX() * dims.getY() * dims.getZ();
			if (count != list.size()) {
				return null;
			}
			
//			ProgressBar bar = null;
//			if (!NostrumMagica.initFinished) {
//				bar = ProgressManager.push("Loading Room", 2);
//				bar.step("Blocks");
//			}
			
			if (masterDims.distanceSq(0, 0, 0, false) == 0) {
				blocks = new BlueprintBlock[count];
			} else {
				blocks = new BlueprintBlock[masterDims.getX() * masterDims.getY() * masterDims.getZ()];
			}
			
			for (int i = 0; i < count; i++) {
				CompoundNBT tag = (CompoundNBT) list.get(i);
				blocks[i] = BlueprintBlock.fromNBT((byte)version, tag);
			}
			
//			if (!NostrumMagica.initFinished && bar != null) {
//				bar.step("Doors and Exits");
//			}
			
			list = nbt.getList(NBT_DOOR_LIST, NBT.TAG_COMPOUND);
			doors = new HashSet<>();
			int listCount = list.size();
			for (int i = 0; i < listCount; i++) {
				CompoundNBT tag = (CompoundNBT) list.getCompound(i);
				DungeonExitPoint door;
				door = DungeonExitPoint.fromNBT(tag);
				doors.add(door);
			}
			
			if (nbt.contains(NBT_ENTRY)) {
				CompoundNBT tag = nbt.getCompound(NBT_ENTRY);
				entry = DungeonExitPoint.fromNBT(tag);
			}
			
			if (!NostrumMagica.initFinished) {
//				ProgressManager.pop(bar);
			}
		}
		
		return new RoomBlueprint(masterDims.distanceSq(0, 0, 0, false) == 0 ? dims : masterDims, blocks, doors, entry);
	}
	
	private static RoomBlueprint deserializeVersion1(CompoundNBT nbt) {
		return deserializeNBTStyleInternal(nbt, (byte)0);
	}
	
	private static RoomBlueprint deserializeVersion2(CompoundNBT nbt) {
		// Store blocks as int array
		// Store TileEntities separately since there are likely few of them
		
		BlockPos dims = NBTUtil.readBlockPos(nbt.getCompound(NBT_DIMS));
		// When breaking blueprints into pieces, the first one has an actual copy of the real size of the whole thing.
		// If we have one of those, allocate the FULL array size instead of the small one
		BlockPos masterDims = NBTUtil.readBlockPos(nbt.getCompound(NBT_WHOLE_DIMS));
		BlueprintBlock[] blocks = null;
		Set<DungeonExitPoint> doors = null;
		DungeonExitPoint entry = null;
		
		if (dims.distanceSq(0, 0, 0, false) == 0) {
			return null;
		} else {
			int[] list = nbt.getIntArray(NBT_BLOCK_LIST);
			
			final int count = dims.getX() * dims.getY() * dims.getZ();
			if (count != list.length) {
				return null;
			}
			
//			ProgressBar bar = null;
//			if (!NostrumMagica.initFinished) {
//				bar = ProgressManager.push("Loading Room", 2);
//				bar.step("Blocks");
//			}
			
			if (masterDims.distanceSq(0, 0, 0, false) == 0) {
				blocks = new BlueprintBlock[count];
			} else {
				blocks = new BlueprintBlock[masterDims.getX() * masterDims.getY() * masterDims.getZ()];
			}
			
			for (int i = 0; i < count; i++) {
				int id = list[i];
				blocks[i] = new BlueprintBlock(id == 0 ? null : Block.getStateById(list[i]), null);
			}
			
			if (nbt.contains(NBT_ENTITIES)) {
				ListNBT entities = nbt.getList(NBT_ENTITIES, NBT.TAG_COMPOUND);
				int entCount = entities.size();
				for (int i = 0; i < entCount; i++) {
					BlueprintSavedTE te = BlueprintSavedTE.fromNBT(entities.getCompound(i));
					
					// Find offset into data blocks, and then transfer data onto that block
					blocks[
					    (te.pos.getX() * dims.getZ() * dims.getY())
					    + (te.pos.getY() * dims.getZ())
					    + te.pos.getZ()
					   ].tileEntityData = te.nbtTagData;
				}
			}
			
//			if (!NostrumMagica.initFinished && bar != null) {
//				bar.step("Doors and Exits");
//			}
			
			ListNBT doorList = nbt.getList(NBT_DOOR_LIST, NBT.TAG_COMPOUND);
			doors = new HashSet<>();
			
			int listCount = doorList.size();
			for (int i = 0; i < listCount; i++) {
				CompoundNBT tag = doorList.getCompound(i);
				doors.add(DungeonExitPoint.fromNBT(tag));
			}
			
			if (nbt.contains(NBT_ENTRY)) {
				entry = DungeonExitPoint.fromNBT(nbt.getCompound(NBT_ENTRY));
			}
			
			if (!NostrumMagica.initFinished) {
				//ProgressManager.pop(bar);
			}
		}
		
		return new RoomBlueprint(masterDims.distanceSq(0, 0, 0, false) == 0 ? dims : masterDims, blocks, doors, entry);
	}
	
	private static RoomBlueprint deserializeVersion3(CompoundNBT nbt) {
		RoomBlueprint blueprint = deserializeNBTStyleInternal(nbt, (byte) 2);
		
		if (nbt.contains(NBT_PIECE_OFFSET)) {
			blueprint.partOffset = nbt.getInt(NBT_PIECE_OFFSET);
		}
		
		return blueprint;
	}
	
	private static RoomBlueprint deserializeVersion4(CompoundNBT nbt) {
		RoomBlueprint blueprint = deserializeNBTStyleInternal(nbt, (byte) 3);
		
		if (nbt.contains(NBT_PIECE_OFFSET)) {
			blueprint.partOffset = nbt.getInt(NBT_PIECE_OFFSET);
		}
		
		return blueprint;
	}
	
	public static RoomBlueprint fromNBT(CompoundNBT nbt) {
		byte version = nbt.getByte(NBT_VERSION);
		switch (version) {
		case 0:
			return deserializeVersion1(nbt);
		case 1:
			return deserializeVersion2(nbt);
		case 2:
			return deserializeVersion3(nbt);
		case 3:
			return deserializeVersion4(nbt);
		default:
			NostrumMagica.logger.fatal("Blueprint has version we don't understand");
			throw new RuntimeException("Could not parse blueprint version " + version);
		}
	}
	
//	public CompoundNBT toNBT() {
//		CompoundNBT nbt = new CompoundNBT();
//		ListNBT entList = new ListNBT();
//		int[] blockList = new int[this.blocks.length];
//		
//		for (int i = 0; i < blocks.length; i++) {
//			BlockState state = this.blocks[i].state;
//			if (state == null) {
//				blockList[i] = 0;
//			} else {
//				blockList[i] = Block.getStateId(this.blocks[i].state);
//			}
//			if (this.blocks[i].tileEntityData != null) {
//				BlueprintSavedTE te = new BlueprintSavedTE(
//						this.blocks[i].tileEntityData,
//						new BlockPos(i / (dimensions.getZ() * dimensions.getY()),
//								((i / dimensions.getZ()) % dimensions.getY()),
//								(i % dimensions.getZ())));
//				entList.add(te.toNBT());
//			}
//		}
//		
//		nbt.setIntArray(NBT_BLOCK_LIST, blockList);
//		nbt.put(NBT_ENTITIES, entList);
//		nbt.put(NBT_DIMS, NBTUtil.createPosTag(dimensions));
//		if (this.entry != null) {
//			nbt.put(NBT_ENTRY, entry.toNBT());
//		}
//		if (this.doors != null && !this.doors.isEmpty()) {
//			ListNBT doorList = new ListNBT();
//			for (DungeonExitPoint door : doors) {
//				doorList.add(door.toNBT());
//			}
//			nbt.put(NBT_DOOR_LIST, doorList);
//		}
//		
//		nbt.setByte(NBT_VERSION, (byte)1);
//		return nbt;
//	}
	
	// Version 1
//	public CompoundNBT toNBT() {
//		CompoundNBT nbt = new CompoundNBT();
//		ListNBT list = new ListNBT();
//		
//		if (blocks != null) {
//			for (int i = 0; i < blocks.length; i++) {
//				list.add(blocks[i].toNBT());
//			}
//		}
//		
//		nbt.put(NBT_BLOCK_LIST, list);
//		nbt.put(NBT_DIMS, NBTUtil.createPosTag(dimensions));
//		if (this.entry != null) {
//			nbt.put(NBT_ENTRY, entry.toNBT());
//		}
//		if (this.doors != null && !this.doors.isEmpty()) {
//			list = new ListNBT();
//			for (DungeonExitPoint door : doors) {
//				list.add(door.toNBT());
//			}
//			nbt.put(NBT_DOOR_LIST, list);
//		}
//		
//		nbt.setByte(NBT_VERSION, (byte)0);
//		
//		return nbt;
//	}
	
	public CompoundNBT toNBT() {
		if (shouldSplit()) {
			// Too big! Use toNBTWithBreakdown() instead!
			throw new RuntimeException("Blueprint too large to be written as single blob!");
		}
		
		return toNBTInternal(0, MAX_BLUEPRINT_BLOCKS);
	}
	
	public INBTGenerator toNBTWithBreakdown() {
		if (!shouldSplit()) {
			return new INBTGenerator() {
				boolean used = false;
				
				@Override
				public CompoundNBT next() {
					used = true;
					return toNBT();
				}

				@Override
				public int getTotal() {
					return 1;
				}

				@Override
				public boolean hasNext() {
					return !used;
				}
			};
			//return new CompoundNBT[]{toNBT()};
		}
		
		// Need to be writing out whole blocks. Get number of blocks based on whole dimensions and whatever fits within the block limit.
		// Attempt to grab some number of entire Z by Y slices. If we can't even grab a full one, bump it up to a full one anyways.
		int size = (dimensions.getZ() * dimensions.getY()) / MAX_BLUEPRINT_BLOCKS; //int division
		if (size == 0) {
			// Not even one slice could fit. Fudge it anyways.
			size = dimensions.getZ() * dimensions.getY();
		}
		final int xSlices = (int) Math.ceil((double) blocks.length / (double) size);
		final int fSize = size;
		
		return new INBTGenerator() {

			int i = 0;
			
			@Override
			public CompoundNBT next() {
				if (i >= xSlices) {
					return null;
				}
				return toNBTInternal(i++ * fSize, fSize);
			}

			@Override
			public int getTotal() {
				return xSlices;
			}

			@Override
			public boolean hasNext() {
				return i < xSlices;
			}
			
		};
	}
	
//	// Version 3
//	protected CompoundNBT toNBTInternal(int startIdx, int count) {
//		
//		CompoundNBT nbt = new CompoundNBT();
//		ListNBT list = new ListNBT();
//		final int endIdx = Math.min(blocks.length, startIdx + count);
//		
//		if (blocks != null) {
//			for (int i = startIdx; i < endIdx; i++) {
//				list.add(blocks[i].toNBT());
//			}
//		}
//		
//		nbt.put(NBT_BLOCK_LIST, list);
//		{
//			// If whole struct, just dump dims
//			if (startIdx == 0 && endIdx == blocks.length) {
//				nbt.put(NBT_DIMS, NBTUtil.writeBlockPos(dimensions));
//			} else {
//				// Else figure out how big it was
//				final int numBlocks = endIdx - startIdx;
//				final int base = dimensions.getY() * dimensions.getZ();
//				nbt.put(NBT_DIMS, NBTUtil.writeBlockPos(new BlockPos(numBlocks / base, dimensions.getY(), dimensions.getZ())));
//			}
//		}
//		if (this.entry != null) {
//			nbt.put(NBT_ENTRY, entry.toNBT());
//		}
//		
//		// First blueprint (when splitting) has all the extra pieces
//		if (startIdx == 0) {
//			if (this.doors != null && !this.doors.isEmpty()) {
//				list = new ListNBT();
//				for (DungeonExitPoint door : doors) {
//					list.add(door.toNBT());
//				}
//				nbt.put(NBT_DOOR_LIST, list);
//			}
//			
//			// Master ALSO has the REAL size, so we can allocate all the space up front instead of resizing
//			nbt.put(NBT_WHOLE_DIMS, NBTUtil.writeBlockPos(dimensions));
//		} else {
//			// Others have their row offset recorded
//			nbt.putInt(NBT_PIECE_OFFSET, startIdx / (dimensions.getY() * dimensions.getZ()));
//		}
//		
//		nbt.putByte(NBT_VERSION, (byte)2);
//		
//		return nbt;
//	}
	
	// Version 4
	protected CompoundNBT toNBTInternal(int startIdx, int count) {
		
		CompoundNBT nbt = new CompoundNBT();
		ListNBT list = new ListNBT();
		final int endIdx = Math.min(blocks.length, startIdx + count);
		
		if (blocks != null) {
			for (int i = startIdx; i < endIdx; i++) {
				list.add(blocks[i].toNBT());
			}
		}
		
		nbt.put(NBT_BLOCK_LIST, list);
		{
			// If whole struct, just dump dims
			if (startIdx == 0 && endIdx == blocks.length) {
				nbt.put(NBT_DIMS, NBTUtil.writeBlockPos(dimensions));
			} else {
				// Else figure out how big it was
				final int numBlocks = endIdx - startIdx;
				final int base = dimensions.getY() * dimensions.getZ();
				nbt.put(NBT_DIMS, NBTUtil.writeBlockPos(new BlockPos(numBlocks / base, dimensions.getY(), dimensions.getZ())));
			}
		}
		if (this.entry != null) {
			nbt.put(NBT_ENTRY, entry.toNBT());
		}
		
		// First blueprint (when splitting) has all the extra pieces
		if (startIdx == 0) {
			if (this.doors != null && !this.doors.isEmpty()) {
				list = new ListNBT();
				for (DungeonExitPoint door : doors) {
					list.add(door.toNBT());
				}
				nbt.put(NBT_DOOR_LIST, list);
			}
			
			// Master ALSO has the REAL size, so we can allocate all the space up front instead of resizing
			nbt.put(NBT_WHOLE_DIMS, NBTUtil.writeBlockPos(dimensions));
		} else {
			// Others have their row offset recorded
			nbt.putInt(NBT_PIECE_OFFSET, startIdx / (dimensions.getY() * dimensions.getZ()));
		}
		
		nbt.putByte(NBT_VERSION, (byte)3);
		
		return nbt;
	}
	
	public static interface INBTGenerator {
		
		public CompoundNBT next();
		
		public int getTotal();
		
		public boolean hasNext();
		
	}
	
	public static interface IBlueprintSpawner {
		
		public void spawnBlock(SpawnContext context, BlockPos pos, Direction direction, BlueprintBlock block);
	}
	
}
