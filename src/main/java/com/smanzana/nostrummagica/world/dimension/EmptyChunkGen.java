package com.smanzana.nostrummagica.world.dimension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep.Carving;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EmptyChunkGen extends ChunkGenerator {
	
//	protected static class EmptyGenerationSettings {
//		
//	}
	
	public static final String ID = "emptychunks";
	
	public static final Codec<EmptyChunkGen> CODEC = RecordCodecBuilder.create(instance -> instance.group( 
		RegistryOps.retrieveRegistry(Registry.STRUCTURE_SET_REGISTRY).forGetter((p_208008_) -> {
	         return p_208008_.structureSets;
	      }),
		RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter((p_161916_) -> {
	         return p_161916_.biomeRegistry;
	      }),
		Biome.CODEC.fieldOf("biome").forGetter((p_209807_) -> {
	         return p_209807_.biome;
	      })
	).apply(instance, EmptyChunkGen::new));
	
	protected final Registry<Biome> biomeRegistry;
	protected final Holder<Biome> biome;
	
	public EmptyChunkGen(Registry<StructureSet> structures, Registry<Biome> biomes, Holder<Biome> biome) {
		super(structures, Optional.empty(), new FixedBiomeSource(biome));
		this.biomeRegistry = biomes;
		this.biome = biome;
	}
	
	public Registry<Biome> getBiomeRegistry() {
		return biomeRegistry;
	}
	
	public Holder<Biome> getBiome() {
		return biome;
	}
	
	@Override
	public void buildSurface(WorldGenRegion p_187697_, StructureFeatureManager p_187698_, ChunkAccess p_187699_) {
		;
	}

//	@Override
//	public void carve(IChunk chunkIn, GenerationStage.Carving carvingSettings) {
//		;
//	}

//	@Override
//	public int getGroundHeight() {
//		return SPAWN_Y;
//	}
	
//	@Override
//	public boolean hasStructure(Biome biomeIn, Structure<? extends IFeatureConfig> structureIn) {
//		return false;
//	}
	
//	@Override
//	public <C extends IFeatureConfig> C getStructureConfig(Biome biomeIn, Structure<C> structureIn) {
//		return null;
//	}

//	@Override
//	public List<Biome.SpawnListEntry> getPossibleCreatures(EntityClassification creatureType, BlockPos pos) {
//		return null;
//	}
	
//	@Override
//	public void makeBase(IWorld worldIn, IChunk chunkIn) {
//		;
//	}
	
	@Override
	public int getBaseHeight(int x, int z, Types heightmapType, LevelHeightAccessor accessor) {
		return 0;
	}

	@Override
	protected Codec<? extends EmptyChunkGen> codec() {
		return CODEC;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ChunkGenerator withSeed(long p_230349_1_) { // Some sort of dupe but with new seed?
		return this; // No difference based on seed, like FlatChunkGenerator
	}

	@Override
	public CompletableFuture<ChunkAccess> fillFromNoise(Executor p_230352_1_, Blender p_187749_, StructureFeatureManager p_230352_2_, ChunkAccess p_230352_3_) { // Actual generator?
		return CompletableFuture.completedFuture(p_230352_3_); // Do nothing
	}
	
	@Override
	public NoiseColumn getBaseColumn(int p_230348_1_, int p_230348_2_, LevelHeightAccessor height) { // I'm not sure what this is? Reader for a single x/z column?
		return new NoiseColumn(0, new BlockState[] {Blocks.AIR.defaultBlockState()});
	}

	@Override
	public Sampler climateSampler() {
		return Climate.empty();
	}

	@Override
	public void applyCarvers(WorldGenRegion p_187691_, long p_187692_, BiomeManager p_187693_,
			StructureFeatureManager p_187694_, ChunkAccess p_187695_, Carving p_187696_) {
		
	}

	@Override
	public void spawnOriginalMobs(WorldGenRegion p_62167_) {
		
	}

	@Override
	public int getGenDepth() {
		return 256;
	}

	@Override
	public int getSeaLevel() {
		return -63;
	}

	@Override
	public int getMinY() {
		return 0;
	}

	@Override
	public void addDebugScreenInfo(List<String> p_208054_, BlockPos p_208055_) {
		
	}
}
