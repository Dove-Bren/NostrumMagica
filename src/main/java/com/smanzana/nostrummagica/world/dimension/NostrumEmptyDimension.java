package com.smanzana.nostrummagica.world.dimension;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry;

import net.minecraft.block.BlockFire;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.init.Biomes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NostrumEmptyDimension {
	
	private static final String DIMENSION_NAME = "NostrumSorceryDim";
	private static final String DIMENSION_SUFFIX = "_" + DIMENSION_NAME;
	private static DimensionType DIMENSION_TYPE;
	
	public static final int SPAWN_Y = 128;
	
	private static final String DIMENSION_ENTRY_TEMPLATE = "sorcery_lobby";
	private static final String DIMENSION_WHOLE_TEMPLATE = "sorcery_dungeon";
	
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
			
			this.nether = false;
			this.skyColor = new Vec3d(.2D, 0D, .2D);
			fogColor = new Vec3d(.2, .2, .2);
		}
		
		protected void onWorldAttached() {
			// Can't do this. Gamerules aren't dimension specific
//			// Force keep-inventory game rule
//			if (world != null) {
//				world.getGameRules().setOrCreateGameRule("keepInventory", "true");
//			}
		}
		
		@Override
		protected void init() {
			super.init();
			this.hasSkyLight = false;
			this.biomeProvider = new BiomeProviderSingle(Biomes.SKY);
			
			// Sucky place for this hook but the actual hook is final
			this.onWorldAttached();
		}
		
		@Override
		public IChunkGenerator createChunkGenerator() {
			return new ChunkGeneratorEmpty(this.world);
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
			return true;
		}
		
		@Override
		public int getRespawnDimension(ServerPlayerEntity player) {
			return this.getDimension();
		}
		
		@OnlyIn(Dist.CLIENT)
		@Override
		public double getVoidFogYFactor() {
			return 0.8D;
		}
		
		@OnlyIn(Dist.CLIENT)
		@Override
		public boolean doesXZShowFog(int x, int z) {
			return true;
		}
		
		@OnlyIn(Dist.CLIENT)
		@Override
		public Vec3d getSkyColor(Entity cameraEntity, float partialTicks) {
			return skyColor;
		}
		
		@Override
		public boolean doesWaterVaporize() {
			return true;
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
		
		@OnlyIn(Dist.CLIENT)
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
			for (PlayerEntity player : world.playerEntities) {
				
				// Make sure they aren't falling out of the world
				if (player.posY < 1) {
					NostrumMagica.logger.info("Respawning player " + player + " because they seem to have fallen out of the world");
					DimensionEntryTeleporter.respawnPlayer(player);
					continue; // skip rate limitting
				}
				
				if (player.isCreative() || player.isSpectator()) {
					continue;
				}
				
				double distSqr = Math.pow(player.posX - player.lastTickPosX, 2)
						+ Math.pow(player.posZ - player.lastTickPosZ, 2)
						+ Math.pow(player.posY - player.lastTickPosY, 2);
				if (distSqr > 25) {
					// Player appears to have teleported
					player.setPositionAndUpdate(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ);
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
		public Chunk generateChunk(int x, int z) {
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
		public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position,
				boolean findUnexplored) {
			return null;
		}

		@Override
		public void recreateStructures(Chunk chunkIn, int x, int z) {
			
		}

		@Override
		public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
			return false;
		}

	}
	
	public static class DimensionEntryTeleporter extends Teleporter {
		
		private ServerWorld world;
		private RoomBlueprint lobbyBlueprint;
		private RoomBlueprint wholeBlueprint;
		
		public DimensionEntryTeleporter(ServerWorld worldIn) {
			super(worldIn);
			
			this.world = worldIn;
			
			lobbyBlueprint = DungeonRoomRegistry.instance().getRoom(DIMENSION_ENTRY_TEMPLATE);
			wholeBlueprint = DungeonRoomRegistry.instance().getRoom(DIMENSION_WHOLE_TEMPLATE);
			
			if (lobbyBlueprint == null) {
				NostrumMagica.logger.fatal("Failed to load sorcery lobby from name " + DIMENSION_ENTRY_TEMPLATE);
			}
			
			if (wholeBlueprint == null) {
				NostrumMagica.logger.fatal("Failed to load sorcery dungeon from name " + DIMENSION_WHOLE_TEMPLATE);
			}
		}
		
		@Override
		public boolean placeInExistingPortal(Entity entityIn, float yaw) {
			if (!(entityIn instanceof PlayerEntity)) {
				return false;
			}
			
			PlayerEntity player = (PlayerEntity) entityIn;
			
			if (!portalExists(player)) {
				this.makePortal(player);
			}
			
			respawnPlayer(player);
			
			return true;
		}
		
		public static void respawnPlayer(PlayerEntity player) {
			if (player.world.isRemote) {
				return;
			}
			
			BlockPos spawn = NostrumMagica.getOrCreatePlayerDimensionSpawn(player);
			if (spawn == null) {
				NostrumMagica.logger.warn("Unable to find player spawning location. Sending to overworld.");
				player.changeDimension(0);
			} else {
				spawn = spawn.north();
				player.rotationYaw = Direction.NORTH.getHorizontalAngle();
				player.setPositionAndUpdate(spawn.getX() + .5, spawn.getY() + 2, spawn.getZ() + .5);
				player.getMotion().x = player.getMotion().y = player.getMotion().z = 0;
				player.fallDistance = 0;
				player.setSpawnChunk(spawn.up(2), true, ModConfig.config.sorceryDimensionIndex());
				
				try {
					Field field = ObfuscationReflectionHelper.findField(ServerPlayerEntity.class, "field_184851_cj"); //"invulnerableDimensionChange");
					field.setAccessible(true);
					FieldUtils.writeField(field, player, true);
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (!player.isCreative() && !player.isSpectator()) {
					player.setGameType(GameType.ADVENTURE);
				}
			}
		}
		
		@Override
		public void placeInPortal(Entity entityIn, float yaw) {
			super.placeInPortal(entityIn, yaw);
		}
		
		public boolean portalExists(PlayerEntity player) {
			BlockPos spawn = NostrumMagica.getOrCreatePlayerDimensionSpawn(player);
			return !world.isAirBlock(spawn.up());
		}
		
		@Override
		public boolean makePortal(Entity entityIn) {
			if (!(entityIn instanceof PlayerEntity)) {
				return false;
			}
			
			BlockPos spawn = NostrumMagica.getOrCreatePlayerDimensionSpawn((PlayerEntity) entityIn);
			
//			for (int i = -5; i <= 5; i++) {
//				for (int j = -5; j <= 5; j++) {
//					pos.setPos(spawn.getX() + i, spawn.getY(), spawn.getZ() + j);
//					world.setBlockState(pos, Blocks.GOLD_BLOCK.getDefaultState());
//				}
//			}
			long startTime = System.currentTimeMillis();
			lobbyBlueprint.spawn(world, spawn);
			NostrumMagica.logger.info("Took " + ((double) (System.currentTimeMillis() - startTime) / 1000.0) + " seconds to generate sorcery lobby");
			
			startTime = System.currentTimeMillis();
			wholeBlueprint.spawn(world, spawn);
			NostrumMagica.logger.info("Took " + ((double) (System.currentTimeMillis() - startTime) / 1000.0) + " seconds to generate whole dungeon");
			
			return true;
		}
		
		@Override
		public void removeStalePortalLocations(long worldTime) {
			; // 
		}
	}
	
	public static class DimensionReturnTeleporter extends Teleporter {
		
		private ServerWorld world;
		
		public DimensionReturnTeleporter(ServerWorld worldIn) {
			super(worldIn);
			
			this.world = worldIn;
		}
		
		@Override
		public boolean placeInExistingPortal(Entity entityIn, float yaw) {
			BlockPos pos = null;
			
			if (NostrumMagica.getMagicWrapper(entityIn) != null) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(entityIn);
				pos = attr.getSorceryPortalPos();
				attr.clearSorceryPortal();
			}
			
			if (pos == null && entityIn instanceof PlayerEntity) {
				PlayerEntity player = (PlayerEntity) entityIn;
				pos = player.getBedLocation(world.provider.getDimension());
			}
			
			if (pos == null) {
				pos = world.getSpawnPoint();
			}
			
			while (pos.getY() < world.provider.getActualHeight() && (!world.isAirBlock(pos) || !world.isAirBlock(pos.up()))) {
				pos = pos.up();
			}
			
			if (entityIn instanceof ServerPlayerEntity) {
				try {
					Field field = ObfuscationReflectionHelper.findField(ServerPlayerEntity.class, "field_184851_cj"); //"invulnerableDimensionChange");
					field.setAccessible(true);
					FieldUtils.writeField(field, ((ServerPlayerEntity) entityIn), true);
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (((ServerPlayerEntity) entityIn).interactionManager.getGameType() == GameType.ADVENTURE) {
					((ServerPlayerEntity) entityIn).setGameType(GameType.SURVIVAL);
				}
				
				((ServerPlayerEntity)entityIn).connection.setPlayerLocation(pos.getX() + .5, pos.getY(), pos.getZ() + .5, entityIn.rotationYaw, entityIn.rotationPitch);
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
		public void onEnderTeleport(EnderTeleportEvent event) {
			if (event.getEntityLiving() != null && event.getEntityLiving().dimension == dim && event.getEntityLiving() instanceof PlayerEntity) {
				event.setCanceled(true);
			}
		}
		
		private Map<UUID, Boolean> teleportingMarker = new HashMap<>();
		
		@SubscribeEvent
		public void onTeleport(EntityTravelToDimensionEvent event) {
			if (event.getDimension() == dim) {
				Entity ent = event.getEntity();
				Boolean marker = teleportingMarker.get(ent.getPersistentID());
				if (marker != null && marker){
					return;
				}
				
				event.setCanceled(true);
				
				if (ent instanceof ServerPlayerEntity) {
					ServerPlayerEntity player = (ServerPlayerEntity) ent;
					MinecraftServer server = player.getServer();
					teleportingMarker.put(player.getPersistentID(), true);
					server.getPlayerList().transferPlayerToDimension(
							player, dim, new DimensionEntryTeleporter(server.getWorld(dim)));
					teleportingMarker.put(player.getPersistentID(), false);
				}
			} else if (event.getEntity().dimension == dim && event.getDimension() == 0) {
				// Leaving our dimension to homeworld
				Entity ent = event.getEntity();
				
				Boolean marker = teleportingMarker.get(ent.getPersistentID());
				if (marker != null && marker){
					return;
				}
				
				
				event.setCanceled(true);

				if (ent instanceof ServerPlayerEntity) {
					ServerPlayerEntity player = (ServerPlayerEntity) ent;
					MinecraftServer server = player.getServer();
					teleportingMarker.put(player.getPersistentID(), true);
					
					int toDim = 0;
					if (NostrumMagica.getMagicWrapper(player) != null) {
						INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
						if (attr.getSorceryPortalPos() != null) {
							toDim = attr.getSorceryPortalDimension();
						}
					}
					server.getPlayerList().transferPlayerToDimension(
							player, toDim, new DimensionReturnTeleporter(server.getWorld(toDim)));
					teleportingMarker.put(player.getPersistentID(), false);
				}
			}
		}
		
		@SubscribeEvent
		public void onExplosion(ExplosionEvent.Detonate event) {
			if (event.getWorld().provider.getDimension() == dim && event.getAffectedBlocks() != null) {
				event.getAffectedBlocks().clear();;
			}
		}
		
		@SubscribeEvent
		public void onBlockPlace(@SuppressWarnings("deprecation") BlockEvent.PlaceEvent event) {
			if (event.getWorld().provider.getDimension() == dim && (
					event.getPlacedBlock().getBlock() instanceof BlockFire
					|| event.getPlacedBlock().getMaterial() == Material.FIRE
				)) {
				event.setCanceled(true);
			}
			
		}
	}
	
}
