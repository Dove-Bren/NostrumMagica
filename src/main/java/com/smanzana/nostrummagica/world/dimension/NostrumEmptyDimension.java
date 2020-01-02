package com.smanzana.nostrummagica.world.dimension;

import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
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
	
	private static final String DIMENSION_NAME = "NostrumSorceryDim";
	private static final String DIMENSION_SUFFIX = "_" + DIMENSION_NAME;
	private static DimensionType DIMENSION_TYPE;
	
	public static final int SPAWN_Y = 128;
	
	public static boolean register(int dim, String identifier) {
		if (DimensionManager.isDimensionRegistered(dim)) {
			return false;
		}
		
		if (DIMENSION_TYPE == null) {
			DIMENSION_TYPE = DimensionType.register(DIMENSION_NAME, DIMENSION_SUFFIX, dim, EmptyDimensionProvider.class, false);
		}
		
		DimensionManager.registerDimension(dim, DIMENSION_TYPE);
		new DimensionListener(dim); // TODO leaving these around will grow and grow listeners as saves are loaded...
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
		
		@Override
		public void onWorldUpdateEntities() {
			// Make sure players aren't teleporting.
			// TODO this even is fired before updating, sadly. That feels incorrect.
			// Does it act weirdly?
			for (EntityPlayer player : worldObj.playerEntities) {
				if (player.isCreative() || player.isSpectator()) {
					continue;
				}
				
				double distSqr = Math.pow(player.posX - player.lastTickPosX, 2)
						+ Math.pow(player.posZ - player.lastTickPosZ, 2)
						+ Math.pow(player.posY - player.lastTickPosY, 2);
				if (distSqr > 25) {
					// Player appears to have teleported
					player.setPositionAndUpdate(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ);
					System.out.println("Teleport? " + distSqr); //TODO donotcheckin
				}
			}
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
	
	public static class DimensionEntryTeleporter extends Teleporter {
		
		private WorldServer world;
		
		public DimensionEntryTeleporter(WorldServer worldIn) {
			super(worldIn);
			
			this.world = worldIn;
		}
		
		@Override
		public boolean placeInExistingPortal(Entity entityIn, float yaw) {
			if (!(entityIn instanceof EntityPlayer)) {
				return false;
			}
			
			if (!portalExists((EntityPlayer) entityIn)) {
				this.makePortal(entityIn);
			}
			
			BlockPos spawn = NostrumMagica.getOrCreatePlayerDimensionSpawn((EntityPlayer) entityIn);
			
			try {
				FieldUtils.writeField(((EntityPlayerMP)entityIn), "invulnerableDimensionChange", true, true);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			entityIn.setPositionAndUpdate(spawn.getX() + .5, spawn.getY() + 1, spawn.getZ() + .5);
			entityIn.motionX = entityIn.motionY = entityIn.motionZ = 0;
			return true;
		}
		
		@Override
		public void placeInPortal(Entity entityIn, float yaw) {
			super.placeInPortal(entityIn, yaw);
		}
		
		public boolean portalExists(EntityPlayer player) {
			BlockPos spawn = NostrumMagica.getOrCreatePlayerDimensionSpawn(player);
			return !world.isAirBlock(spawn);
		}
		
		@Override
		public boolean makePortal(Entity entityIn) {
			if (!(entityIn instanceof EntityPlayer)) {
				return false;
			}
			
			BlockPos spawn = NostrumMagica.getOrCreatePlayerDimensionSpawn((EntityPlayer) entityIn);
			
			MutableBlockPos pos = new MutableBlockPos(spawn);
			
			for (int i = -5; i <= 5; i++) {
				for (int j = -5; j <= 5; j++) {
					pos.setPos(spawn.getX() + i, spawn.getY(), spawn.getZ() + j);
					world.setBlockState(pos, Blocks.GOLD_BLOCK.getDefaultState());
				}
			}
			
			return true;
		}
		
		@Override
		public void removeStalePortalLocations(long worldTime) {
			; // 
		}
	}
	
	public static class DimensionReturnTeleporter extends Teleporter {
		
		private WorldServer world; 
		
		public DimensionReturnTeleporter(WorldServer worldIn) {
			super(worldIn);
			
			this.world = worldIn;
		}
		
		@Override
		public boolean placeInExistingPortal(Entity entityIn, float yaw) {
			BlockPos pos = null;
			if (entityIn instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) entityIn;
				pos = player.getBedLocation(0);
			}
			
			if (pos == null) {
				pos = world.getSpawnPoint();
			}
			
			if (entityIn instanceof EntityPlayerMP) {
				try {
					FieldUtils.writeField(((EntityPlayerMP)entityIn), "invulnerableDimensionChange", true, true);
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				((EntityPlayerMP)entityIn).connection.setPlayerLocation(pos.getX() + .5, pos.getY(), pos.getZ() + .5, entityIn.rotationYaw, entityIn.rotationPitch);
			} else {
				entityIn.setPositionAndUpdate(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
			}
			
			return true;
		}
		
		@Override
		public void placeInPortal(Entity entityIn, float yaw) {
			super.placeInPortal(entityIn, yaw);
		}
		
		@Override
		public boolean makePortal(Entity entityIn) {
			return true;
		}
		
		@Override
		public void removeStalePortalLocations(long worldTime) {
			;
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
							player, dim, new DimensionEntryTeleporter(server.worldServerForDimension(dim)));
				}
			} else if (event.getEntity().dimension == dim && event.getDimension() == 0) {
				// Leaving our dimension to homeworld
				event.setCanceled(true);
				
				Entity ent = event.getEntity();
				if (ent instanceof EntityPlayerMP) {
					EntityPlayerMP player = (EntityPlayerMP) ent;
					MinecraftServer server = player.getServer();
					server.getPlayerList().transferPlayerToDimension(
							player, event.getDimension(), new DimensionReturnTeleporter(server.worldServerForDimension(event.getDimension())));
				}
			}
		}
	}
	
}
