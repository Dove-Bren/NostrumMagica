package com.smanzana.nostrummagica.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public record GridStructureSetPlacement(int gridSize, BlockPos gridOffset) implements StructurePlacement {

	public static final Codec<GridStructureSetPlacement> CODEC = RecordCodecBuilder.create((builder) -> builder.group(
			Codec.intRange(1, 512000).fieldOf("grid_size").forGetter(GridStructureSetPlacement::gridSize),
			BlockPos.CODEC.optionalFieldOf("offset", BlockPos.ZERO).forGetter(GridStructureSetPlacement::gridOffset)
			).apply(builder, GridStructureSetPlacement::new));
	
	@Override
	public boolean isFeatureChunk(ChunkGenerator generator, long p_212320_, int chunkX, int chunkZ) {
		// grid is OFFSET_CHUNK_LEN scale. So lower corners in block scale are (0,0),  (OFFSET_CHUNK_LEN, 0), (0, OFFSET_CHUNK_LEN), (OFFSET_CHUNK_LEN, OFFSET_CHUNK_LEN), etc.
		// So checking if a block pos is lower-left is as easy as (x % OFFSET_CHUNK_LEN == 0) and (z % OFFSET_CHUNK_LEN == 0).
		// Checking if it's in the center is more like ((x - (OFFSET_CHUNK_LEN/2) % OFFSET_CHUNK_LEN == 0) etc.
		// I think the math for chunk coords instead is the same but OFFSET_CHUNK_LEN should also be divided by chunk size (16)
		final int CHUNK_SPAN = gridSize() / 16;
		final boolean center = ((chunkX - ((CHUNK_SPAN / 2) + (gridOffset().getX() >> 4))) % CHUNK_SPAN == 0)
				&& ((chunkZ - ((CHUNK_SPAN / 2) + (gridOffset().getZ() >> 4))) % CHUNK_SPAN == 0);
		
		return center;
	}

	@Override
	public StructurePlacementType<?> type() {
		return NostrumStructures.PLACEMENT_FIXED_GRID;
	}
	
}