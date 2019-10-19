package com.smanzana.nostrummagica.world.dimension;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NostrumEmptyDimension {
	
	private static final String DIMENSION_NAME_BASE = "NostrumSorceryDim";
	private static final String DIMENSION_SUFFIX = "_" + DIMENSION_NAME_BASE;
	
	public static boolean register(int dim, String identifier) {
		if (DimensionManager.isDimensionRegistered(dim)) {
			return false;
		}
		
		String name = identifier + DIMENSION_SUFFIX;
		DimensionType type = DimensionType.register(name, "_" + name, dim, EmptyDimensionProvider.class, false);
		DimensionManager.registerDimension(dim, type);
		new DimensionListener(dim);
		return true;
	}
	
	public static class EmptyDimensionProvider extends WorldProvider {
		
		protected Vec3d skyColor;
		protected Vec3d fogColor;
		
		public EmptyDimensionProvider() {
			super();
			
			this.hasNoSky = true;
			this.isHellWorld = false;
			this.skyColor = new Vec3d(.2D, 0D, .2D);
			fogColor = new Vec3d(.2, .2, .2);
		}
		
		@Override
		public void createBiomeProvider() {
			this.biomeProvider = new BiomeProviderSingle(Biomes.SKY);
		}
		
		@Override
		public IChunkGenerator createChunkGenerator() {
			return new ChunkGeneratorEmpty(this.worldObj);
		}

		@Override
		public DimensionType getDimensionType() {
			return DimensionManager.getProviderType(this.getDimension());
		}
		
		@Override
		public boolean canDoRainSnowIce(Chunk chunk) {
			return false;
		}
		
		@Override
		public boolean canRespawnHere() {
			return false;
		}
		
		@SideOnly(Side.CLIENT)
		@Override
		public double getVoidFogYFactor() {
			return 0.8D;
		}
		
		@SideOnly(Side.CLIENT)
		@Override
		public boolean doesXZShowFog(int x, int z) {
			return true;
		}
		
		@SideOnly(Side.CLIENT)
		@Override
		public Vec3d getSkyColor(Entity cameraEntity, float partialTicks) {
			return skyColor;
		}
		
		@Override
		public float calculateCelestialAngle(long worldTime, float partialTicks) {
			return .5f;
		}
		
		@Override
		protected void generateLightBrightnessTable() {
			for (int i = 0; i < 16; i++) {
				float prog = ((float) i / 16f);
				prog *= prog * prog;
				lightBrightnessTable[i] = .8f * prog;
			}
		}
		
		@Override
		public float[] getLightBrightnessTable() {
			return super.getLightBrightnessTable();
		}
		
		@SideOnly(Side.CLIENT)
		@Override
		public Vec3d getFogColor(float p_76562_1_, float p_76562_2_) {
			fogColor = new Vec3d(.1, 0, .1);
			return fogColor;
		}
		
	}
	
	public static class ChunkGeneratorEmpty implements IChunkGenerator {
		
		private World world;
		
		public ChunkGeneratorEmpty(World world) {
			this.world = world;
		}

		@Override
		public Chunk provideChunk(int x, int z) {
			return new Chunk(world, x, z);
		}

		@Override
		public void populate(int x, int z) {
			;
		}

		@Override
		public boolean generateStructures(Chunk chunkIn, int x, int z) {
			return false;
		}

		@Override
		public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
			return null;
		}

		@Override
		public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
			return null;
		}

		@Override
		public void recreateStructures(Chunk chunkIn, int x, int z) {
			
		}

	}
	
	public static class DimensionTeleporter extends Teleporter {
		
		private WorldServer world; 
		
		public DimensionTeleporter(WorldServer worldIn) {
			super(worldIn);
			
			this.world = worldIn;
		}
		
		@Override
		public boolean placeInExistingPortal(Entity entityIn, float yaw) {
			return false;
		}
		
		@Override
		public void placeInPortal(Entity entityIn, float yaw) {
			super.placeInPortal(entityIn, yaw);
		}
		
		@Override
		public boolean makePortal(Entity entityIn) {
			int y = (int) entityIn.posY - 3;
			int x = (int) entityIn.posX;
			int z = (int) entityIn.posZ;
			MutableBlockPos pos = new MutableBlockPos(new BlockPos(entityIn.posX, y, entityIn.posZ));
			
			for (int i = -5; i <= 5; i++) {
				for (int j = -5; j <= 5; j++) {
					pos.setPos(x + i, y, z + j);
					world.setBlockState(pos, Blocks.GOLD_BLOCK.getDefaultState());
				}
			}
			
			entityIn.motionX = entityIn.motionY = entityIn.motionZ = 0;
			
			return true;
		}
		
		@Override
		public void removeStalePortalLocations(long worldTime) {
			super.removeStalePortalLocations(worldTime);
		}
	}
	
	public static class DimensionListener {

		private int dim;
		
		public DimensionListener(int dim) {
			MinecraftForge.EVENT_BUS.register(this);
			this.dim = dim;
		}
		
		@SubscribeEvent
		public void onTeleport(EntityTravelToDimensionEvent event) {
			if (event.getDimension() == dim) {
				event.setCanceled(true);
				
				Entity ent = event.getEntity();
				if (ent instanceof EntityPlayerMP) {
					EntityPlayerMP player = (EntityPlayerMP) ent;
					MinecraftServer server = player.getServer();
					server.getPlayerList().transferPlayerToDimension(
							player, dim, new DimensionTeleporter(server.worldServerForDimension(dim)));
				}
			}
		}
	}
	
	
	
//	public static class EmptyDimensionWorldType extends WorldType {
//
//		public EmptyDimensionWorldType() {
//			super(EMPTY_WORLD_NAME);
//		}
//		
//		@SideOnly(Side.CLIENT)
//		public boolean getCanBeCreated() {
//			return true; // TODO remove! Testing, only!
//		}
//		
//		@Override
//		public BiomeProvider getBiomeProvider() {
//			
//		}
//		
//	}
	
}
