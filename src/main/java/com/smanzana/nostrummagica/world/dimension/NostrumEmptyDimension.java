package com.smanzana.nostrummagica.world.dimension;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry;

import net.minecraft.block.FireBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameType;
import net.minecraft.world.IWorld;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import net.minecraft.world.biome.provider.SingleBiomeProviderSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class NostrumEmptyDimension {
	
	public static final String TYPE_ID = "nostrumsorcerydim";
	//private static final String DIMENSION_SUFFIX = "_" + DIMENSION_NAME;
	//private static DimensionType DIMENSION_TYPE;
	
	public static final int SPAWN_Y = 128;
	
	private static final String DIMENSION_ENTRY_TEMPLATE = "sorcery_lobby";
	private static final String DIMENSION_WHOLE_TEMPLATE = "sorcery_dungeon";
	
//	public static boolean register(int dim, String identifier) {
//		if (DimensionManager.isDimensionRegistered(dim)) {
//			return false;
//		}
//		
//		if (DIMENSION_TYPE == null) {
//			DIMENSION_TYPE = DimensionType.register(DIMENSION_NAME, DIMENSION_SUFFIX, dim, EmptyDimensionProvider.class, false);
//		}
//		
//		DimensionManager.registerDimension(dim, DIMENSION_TYPE);
//		new DimensionListener(dim); // TODO leaving these around will grow and grow listeners as saves are loaded...
//		return true;
//	}
	
	public static class EmptyDimensionFactory extends ModDimension {

		@Override
		public BiFunction<World, DimensionType, ? extends Dimension> getFactory() {
			return EmptyDimension::new;
		}
		
	}
	
	public static class EmptyDimension extends Dimension {
		
		protected static final Map<DimensionType, DimensionListener> listeners = new HashMap<>();
		
		protected Vector3d skyColor;
		protected Vector3d fogColor;
		
		public EmptyDimension(World worldIn, DimensionType typeIn) {
			super(worldIn, typeIn);
			
			this.nether = false;
			this.skyColor = new Vector3d(.2D, 0D, .2D);
			fogColor = new Vector3d(.2, .2, .2);
			
			if (!listeners.containsKey(typeIn)) {
				listeners.put(typeIn, new DimensionListener(typeIn));
			}
		}
		
		protected void onWorldAttached() {
			// Can't do this. Gamerules aren't dimension specific
//			// Force keep-inventory game rule
//			if (world != null) {
//				world.getGameRules().setOrCreateGameRule("keepInventory", "true");
//			}
		}
		
		@Override
		public ChunkGenerator<?> createChunkGenerator() {
			return new ChunkGeneratorEmpty(this.world, new SingleBiomeProvider(new SingleBiomeProviderSettings().setBiome(Biomes.THE_END)), new EmptyGenerationSettings());
		}
		
		@Override
		public boolean hasSkyLight() {
			return false;
		}

//		@Override
//		public DimensionType getDimensionType() {
//			return DimensionManager.getProviderType(this.getDimension());
//		}
		
		@Override
		public boolean canDoRainSnowIce(Chunk chunk) {
			return false;
		}
		
		@Override
		public boolean canRespawnHere() {
			return true;
		}
		
		@Override
		public DimensionType getRespawnDimension(ServerPlayerEntity player) {
			return this.getType();
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
		public Vector3d getSkyColor(BlockPos cameraPos, float partialTicks) {
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
		public Vector3d getFogColor(float p_76562_1_, float p_76562_2_) {
			fogColor = new Vector3d(.1, 0, .1);
			return fogColor;
		}
		
		@Override
		public void tick() {
			// Make sure players aren't teleporting.
			// TODO this even is fired before updating, sadly. That feels incorrect.
			// Does it act weirdly?
			for (ServerPlayerEntity player : ((ServerWorld) world).getPlayers()) {
				
				// Make sure they aren't falling out of the world
				if (player.getPosY() < 1) {
					NostrumMagica.logger.info("Respawning player " + player + " because they seem to have fallen out of the world");
					DimensionEntryTeleporter.respawnPlayer(player);
					continue; // skip rate limitting
				}
				
				if (player.isCreative() || player.isSpectator()) {
					continue;
				}
				
				double distSqr = Math.pow(player.getPosX() - player.lastTickPosX, 2)
						+ Math.pow(player.getPosZ() - player.lastTickPosZ, 2)
						+ Math.pow(player.getPosY() - player.lastTickPosY, 2);
				if (distSqr > 25) {
					// Player appears to have teleported
					player.setPositionAndUpdate(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ);
				}
			}
		}

		@Override
		public BlockPos findSpawn(ChunkPos chunkPosIn, boolean checkValid) {
			return findSpawn(chunkPosIn.getXStart(), chunkPosIn.getZStart(), checkValid);
		}

		@Override
		public BlockPos findSpawn(int posX, int posZ, boolean checkValid) {
			return new BlockPos(posX, SPAWN_Y, posZ);
		}

		@Override
		public boolean isSurfaceWorld() {
			return false;
		}
	}
	
	protected static class EmptyGenerationSettings extends GenerationSettings {
		
	}
	
	public static class ChunkGeneratorEmpty extends ChunkGenerator<EmptyGenerationSettings> {
		
		protected IWorld world;
		
		public ChunkGeneratorEmpty(IWorld world, BiomeProvider biomeProvider, EmptyGenerationSettings settings) {
			super(world, biomeProvider, settings);
			this.world = world;
		}

		@Override
		public void generateSurface(IChunk chunkIn) {
			;
		}

		@Override
		public void carve(IChunk chunkIn, GenerationStage.Carving carvingSettings) {
			;
		}

		@Override
		public int getGroundHeight() {
			return SPAWN_Y;
		}
		
		@Override
		public boolean hasStructure(Biome biomeIn, Structure<? extends IFeatureConfig> structureIn) {
			return false;
		}
		
		@Override
		public <C extends IFeatureConfig> C getStructureConfig(Biome biomeIn, Structure<C> structureIn) {
			return null;
		}

		@Override
		public List<Biome.SpawnListEntry> getPossibleCreatures(EntityClassification creatureType, BlockPos pos) {
			return null;
		}
		
		@Override
		public void makeBase(IWorld worldIn, IChunk chunkIn) {
			;
		}
		
		@Override
		public int func_222529_a(int p_222529_1_, int p_222529_2_, Heightmap.Type p_222529_3_) {
			return 0;
		}
	}
	
	// Note: "Teleporter" class not used here. Just conveniently wrapping making portals, finding spawn location,
	// and placing the entity. The vanilla interface has changed to only use teleporters when leaving a dimension
	// and doesn't let you replace the teleporter to use.
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
		public boolean placeInPortal(Entity entityIn, float yaw) { // placeInPortal, placeInExistingPortal
			if (!(entityIn instanceof PlayerEntity)) {
				return false;
			}
			
			ServerPlayerEntity player = (ServerPlayerEntity) entityIn;
			
			if (!portalExists(player)) {
				this.makePortal(player);
			}
			
			respawnPlayer(player);
			
			return true;
		}
		
		public static void respawnPlayer(ServerPlayerEntity player) {
			if (player.world.isRemote) {
				return;
			}
			
			BlockPos spawn = NostrumMagica.getOrCreatePlayerDimensionSpawn(player);
			if (spawn == null) {
				NostrumMagica.logger.warn("Unable to find player spawning location. Sending to overworld.");
				player.changeDimension(DimensionType.OVERWORLD);
			} else {
				spawn = spawn.north();
				
				player.teleport(player.server.getWorld(NostrumDimensions.EmptyDimension),
						spawn.getX() + .5, spawn.getY() + 2, spawn.getZ() + .5,
						Direction.NORTH.getHorizontalAngle(),
						0
						);
//				player.rotationYaw = Direction.NORTH.getHorizontalAngle();
//				player.setPositionAndUpdate(spawn.getX() + .5, spawn.getY() + 2, spawn.getZ() + .5);
//				player.setMotion(Vector3d.ZERO);
//				player.fallDistance = 0;
				player.setSpawnPoint(spawn.up(2), true, NostrumDimensions.EmptyDimension);
				
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
		
//		@Override
//		public void placeInPortal(Entity entityIn, float yaw) {
//			super.placeInPortal(entityIn, yaw);
//		}
		
		@Override
		@Nullable
		public BlockPattern.PortalInfo placeInExistingPortal(BlockPos p_222272_1_, Vector3d p_222272_2_, Direction p_222272_3_, double p_222272_4_, double p_222272_6_, boolean p_222272_8_) {
			// "GetExistingPortalLocation" that base entity uses to try and move to another dimension.
			// Our dimension doesn't support non-players going through as we don't have a spot for them, so return NULL.
			return null;
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
			final UUID dungeonID = entityIn.getUniqueID(); // Use entity UUID as dungeon ID?
			
//			for (int i = -5; i <= 5; i++) {
//				for (int j = -5; j <= 5; j++) {
//					pos.setPos(spawn.getX() + i, spawn.getY(), spawn.getZ() + j);
//					world.setBlockState(pos, Blocks.GOLD_BLOCK.getDefaultState());
//				}
//			}
			long startTime = System.currentTimeMillis();
			lobbyBlueprint.spawn(world, spawn, dungeonID);
			NostrumMagica.logger.info("Took " + ((double) (System.currentTimeMillis() - startTime) / 1000.0) + " seconds to generate sorcery lobby");
			
			startTime = System.currentTimeMillis();
			wholeBlueprint.spawn(world, spawn, dungeonID);
			NostrumMagica.logger.info("Took " + ((double) (System.currentTimeMillis() - startTime) / 1000.0) + " seconds to generate whole dungeon");
			
			return true;
		}
		
		@Override
		public void tick(long worldTime) {
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
		public boolean placeInPortal(Entity entityIn, float yaw) { // placeInExistingPortal
			BlockPos pos = null;
			
			if (NostrumMagica.getMagicWrapper(entityIn) != null) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(entityIn);
				pos = attr.getSorceryPortalPos();
				attr.clearSorceryPortal();
			}
			
			if (pos == null && entityIn instanceof PlayerEntity) {
				PlayerEntity player = (PlayerEntity) entityIn;
				pos = player.getBedLocation(world.getDimension().getType());
			}
			
			if (pos == null) {
				pos = world.getSpawnPoint();
			}
			
			while (pos.getY() < world.getActualHeight() && (!world.isAirBlock(pos) || !world.isAirBlock(pos.up()))) {
				pos = pos.up();
			}
			
			if (entityIn instanceof ServerPlayerEntity) {
				ServerPlayerEntity player = ((ServerPlayerEntity) entityIn);
				
				player.teleport(this.world,
						pos.getX() + .5, pos.getY() + 2, pos.getZ() + .5,
						Direction.NORTH.getHorizontalAngle(),
						0
						);
//				player.rotationYaw = Direction.NORTH.getHorizontalAngle();
//				player.setPositionAndUpdate(spawn.getX() + .5, spawn.getY() + 2, spawn.getZ() + .5);
//				player.setMotion(Vector3d.ZERO);
//				player.fallDistance = 0;
				
				try {
					Field field = ObfuscationReflectionHelper.findField(ServerPlayerEntity.class, "field_184851_cj"); //"invulnerableDimensionChange");
					field.setAccessible(true);
					FieldUtils.writeField(field, player, true);
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (((ServerPlayerEntity) entityIn).interactionManager.getGameType() == GameType.ADVENTURE) {
					((ServerPlayerEntity) entityIn).setGameType(GameType.SURVIVAL);
				}
				
			} else {
				entityIn.changeDimension(this.world.dimension.getType());
			}
			
			return true;
		}
		
		@Override
		@Nullable
		public BlockPattern.PortalInfo placeInExistingPortal(BlockPos p_222272_1_, Vector3d p_222272_2_, Direction p_222272_3_, double p_222272_4_, double p_222272_6_, boolean p_222272_8_) {
			// We can put items 'back' and default to world spawn...
			// But nah just ignore them
			return null;
		}
		
//		@Override
//		public void placeInPortal(Entity entityIn, float yaw) {
//			super.placeInPortal(entityIn, yaw);
//		}
		
		@Override
		public boolean makePortal(Entity entityIn) {
			return true;
		}
		
		@Override
		public void tick(long worldTime) {
			;
		}
	}
	
	public static class DimensionListener {

		private DimensionType dim;
		
		public DimensionListener(DimensionType dim) {
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
		
		// Hook travel event and change teleporter to our custom once, since
		// default teleporter looks for nether portals...
		@SubscribeEvent
		public void onTeleport(EntityTravelToDimensionEvent event) {
			if (event.getDimension() == dim) {
				Entity ent = event.getEntity();
				Boolean marker = teleportingMarker.get(ent.getUniqueID()); // how is this recursing?
				if (marker != null && marker){
					return;
				}
				
				event.setCanceled(true);
				
				if (ent instanceof ServerPlayerEntity) {
					ServerPlayerEntity player = (ServerPlayerEntity) ent;
					MinecraftServer server = player.getServer();
					teleportingMarker.put(player.getUniqueID(), true);
					DimensionEntryTeleporter.respawnPlayer(player);
					new DimensionEntryTeleporter(server.getWorld(dim))
						.placeInPortal(player, 0f);
//					server.getPlayerList().transferPlayerToDimension(
//							player, dim, new DimensionEntryTeleporter(server.getWorld(dim)));
					teleportingMarker.put(player.getUniqueID(), false);
				}
			} else if (event.getEntity().dimension == dim && event.getDimension() == DimensionType.OVERWORLD) {
				// Leaving our dimension to homeworld
				Entity ent = event.getEntity();
				
				Boolean marker = teleportingMarker.get(ent.getUniqueID());
				if (marker != null && marker){
					return;
				}
				
				// Can't set the world's teleporter to a return one :/
				event.setCanceled(true);

				if (ent instanceof ServerPlayerEntity) {
					ServerPlayerEntity player = (ServerPlayerEntity) ent;
					MinecraftServer server = player.getServer();
					teleportingMarker.put(player.getUniqueID(), true);
					
					DimensionType toDim = DimensionType.OVERWORLD;
					if (NostrumMagica.getMagicWrapper(player) != null) {
						INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
						if (attr.getSorceryPortalPos() != null) {
							toDim = attr.getSorceryPortalDimension();
						}
					}
					
					new DimensionReturnTeleporter(server.getWorld(toDim))
						.placeInPortal(player, 0f);
//					server.getPlayerList().transferPlayerToDimension(
//							player, toDim, new DimensionReturnTeleporter(server.getWorld(toDim)));
					teleportingMarker.put(player.getUniqueID(), false);
				}
			}
		}
		
		@SubscribeEvent
		public void onExplosion(ExplosionEvent.Detonate event) {
			if (event.getWorld().getDimension().getType() == dim && event.getAffectedBlocks() != null) {
				event.getAffectedBlocks().clear();;
			}
		}
		
		@SubscribeEvent
		public void onBlockPlace(EntityPlaceEvent event) {
			if (event.getWorld().getDimension().getType() == dim && (
					event.getPlacedBlock().getBlock() instanceof FireBlock
					|| event.getPlacedBlock().getMaterial() == Material.FIRE
				)) {
				event.setCanceled(true);
			}
			
		}
		
		@SubscribeEvent
		public void onMobGrief(EntityMobGriefingEvent event) {
			if (event.getEntity() != null
					&& event.getEntity().world != null
					&& event.getEntity().world.getDimension() != null
					&& event.getEntity().world.getDimension().getType() == dim) {
				event.setResult(Result.DENY);
			}
		}
	}
	
}
