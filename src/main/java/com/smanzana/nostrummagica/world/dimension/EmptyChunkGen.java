package com.smanzana.nostrummagica.world.dimension;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.Blockreader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.CheckerboardBiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EmptyChunkGen extends ChunkGenerator {
	
//	protected static class EmptyGenerationSettings {
//		
//	}
	
	public static final String ID = "emptychunks";
	
	public static final Codec<EmptyChunkGen> CODEC = RecordCodecBuilder.create(instance -> instance.group( 
		RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(EmptyChunkGen::getBiomeRegistry),
		ResourceLocation.CODEC.xmap(s -> RegistryKey.create(Registry.BIOME_REGISTRY, s), k -> k.location()).fieldOf("biome").forGetter(EmptyChunkGen::getBiome)
	).apply(instance, EmptyChunkGen::new));
	
	protected final Registry<Biome> biomeRegistry;
	protected final RegistryKey<Biome> biome;
	
	public EmptyChunkGen(Registry<Biome> biomes, RegistryKey<Biome> biome) {
		super(new CheckerboardBiomeProvider(Lists.newArrayList(() ->biomes.getOrThrow(biome)), 1),
				new DimensionStructuresSettings(false));
		this.biomeRegistry = biomes;
		this.biome = biome;
	}
	
	public Registry<Biome> getBiomeRegistry() {
		return biomeRegistry;
	}
	
	public RegistryKey<Biome> getBiome() {
		return biome;
	}

	@Override
	public void buildSurfaceAndBedrock(WorldGenRegion region, IChunk chunkIn) {
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
	public int getBaseHeight(int x, int z, Type heightmapType) {
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
	public void fillFromNoise(IWorld p_230352_1_, StructureManager p_230352_2_, IChunk p_230352_3_) { // Actual generator?
		; // Do nothing
	}

	@Override
	public IBlockReader getBaseColumn(int p_230348_1_, int p_230348_2_) { // I'm not sure what this is? Reader for a single x/z column?
		return new Blockreader(new BlockState[] {Blocks.AIR.defaultBlockState()});
	}
	
}
