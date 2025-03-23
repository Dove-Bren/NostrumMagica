package com.smanzana.nostrummagica.world.dimension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EmptyChunkGen extends ChunkGenerator {
	
//	protected static class EmptyGenerationSettings {
//		
//	}
	
	public static final String ID = "emptychunks";
	
	public static final Codec<EmptyChunkGen> CODEC = RecordCodecBuilder.create(instance -> instance.group( 
		RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(EmptyChunkGen::getBiomeRegistry),
		ResourceLocation.CODEC.xmap(s -> ResourceKey.create(Registry.BIOME_REGISTRY, s), k -> k.location()).fieldOf("biome").forGetter(EmptyChunkGen::getBiome)
	).apply(instance, EmptyChunkGen::new));
	
	protected final Registry<Biome> biomeRegistry;
	protected final ResourceKey<Biome> biome;
	
	public EmptyChunkGen(Registry<Biome> biomes, ResourceKey<Biome> biome) {
		super(new CheckerboardColumnBiomeSource(Lists.newArrayList(() ->biomes.getOrThrow(biome)), 1),
				new StructureSettings(false));
		this.biomeRegistry = biomes;
		this.biome = biome;
	}
	
	public Registry<Biome> getBiomeRegistry() {
		return biomeRegistry;
	}
	
	public ResourceKey<Biome> getBiome() {
		return biome;
	}

	@Override
	public void buildSurfaceAndBedrock(WorldGenRegion region, ChunkAccess chunkIn) {
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
	public CompletableFuture<ChunkAccess> fillFromNoise(Executor p_230352_1_, StructureFeatureManager p_230352_2_, ChunkAccess p_230352_3_) { // Actual generator?
		return CompletableFuture.completedFuture(p_230352_3_); // Do nothing
	}

	@Override
	public NoiseColumn getBaseColumn(int p_230348_1_, int p_230348_2_, LevelHeightAccessor height) { // I'm not sure what this is? Reader for a single x/z column?
		return new NoiseColumn(0, new BlockState[] {Blocks.AIR.defaultBlockState()});
	}
}
