package com.smanzana.nostrummagica.world.dimension;

import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.INostrumMagic.VanillaRespawnInfo;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry;

import net.minecraft.block.FireBlock;
import net.minecraft.block.PortalInfo;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.living.EntityTeleportEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;

public class NostrumSorceryDimension {
	
	public static final int SPAWN_Y = 128;
	
	private static final String DIMENSION_ENTRY_TEMPLATE = "sorcery_lobby";
	private static final String DIMENSION_WHOLE_TEMPLATE = "sorcery_dungeon";
	
	public static final void RegisterListener() {
		DistExecutor.unsafeRunForDist(() -> DimensionClientListener::new, () -> DimensionListener::new);
	}
	
	private static final class DungeonSpawner {
		private static RoomBlueprint lobbyBlueprint = null;
		private static RoomBlueprint wholeBlueprint = null;
		
		private static void initBlueprints() {
			lobbyBlueprint = DungeonRoomRegistry.instance().getRoom(DIMENSION_ENTRY_TEMPLATE);
			wholeBlueprint = DungeonRoomRegistry.instance().getRoom(DIMENSION_WHOLE_TEMPLATE);
			
			if (lobbyBlueprint == null) {
				NostrumMagica.logger.fatal("Failed to load sorcery lobby from name " + DIMENSION_ENTRY_TEMPLATE);
			}
			
			if (wholeBlueprint == null) {
				NostrumMagica.logger.fatal("Failed to load sorcery dungeon from name " + DIMENSION_WHOLE_TEMPLATE);
			}
		}
		
		protected static final void SpawnDungeon(ServerWorld world, BlockPos center, UUID dungeonID) {
			if (lobbyBlueprint == null) {
				initBlueprints();
			}
			
			long startTime = System.currentTimeMillis();
			lobbyBlueprint.spawn(world, center, dungeonID);
			NostrumMagica.logger.info("Took " + ((double) (System.currentTimeMillis() - startTime) / 1000.0) + " seconds to generate sorcery lobby");
			
			startTime = System.currentTimeMillis();
			wholeBlueprint.spawn(world, center, dungeonID);
			NostrumMagica.logger.info("Took " + ((double) (System.currentTimeMillis() - startTime) / 1000.0) + " seconds to generate whole dungeon");
		}
		
		protected static final boolean DungeonPresent(ServerWorld world, BlockPos center) {
			return !world.isAirBlock(center.up());
		}
		
	}
		
	public static class DimensionEntryTeleporter implements ITeleporter {
		
		public static final DimensionEntryTeleporter INSTANCE = new DimensionEntryTeleporter();
		
		private DimensionEntryTeleporter() {
			
		}
		
		@Override
		public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
			if (!(entity instanceof ServerPlayerEntity)) {
				return entity;
			}
			
			final ServerPlayerEntity player = (ServerPlayerEntity) entity;
			player.fallDistance = 0; // Copied from TwilightForest, even though entities don't fall into our portals
			
			// I wish we got passed the PortalInfo that's gonna be used to move us in repositionEntity so I didn't
			// have to recompute this and rely on it being the same!
			final BlockPos center = NostrumMagica.getOrCreatePlayerDimensionSpawn(player);
			final BlockPos spawn = center.north();
			
			// Capture current spawn point and then set new one in the sorcery dimension
			if (NostrumMagica.getMagicWrapper(player) != null) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
				BlockPos blockpos = player.func_241140_K_();
				
				// Vanilla encodes no specific respawn as blockpos==null
				if (blockpos != null) {
					float yawToSave = player.func_242109_L();
					boolean force = player.func_241142_M_();
					RegistryKey<World> dim = player.func_241141_L_();
					attr.setSavedRespawnInfo(new VanillaRespawnInfo(dim, blockpos, yawToSave, force));
				}
				// else clear?
			}
			player.func_242111_a(NostrumDimensions.GetSorceryDimension(), spawn.up(2), Direction.NORTH.getHorizontalAngle(), true, false);
			
			if (!player.isCreative() && !player.isSpectator()) {
				player.setGameType(GameType.ADVENTURE);
			}
			
			return repositionEntity.apply(false);
		}
		
		@Override
		@Nullable
		public PortalInfo getPortalInfo(Entity entity, ServerWorld destWorld, Function<ServerWorld, PortalInfo> defaultPortalInfo) {
			if (!(entity instanceof ServerPlayerEntity)) {
				return null;
			}
			
			final ServerPlayerEntity player = (ServerPlayerEntity) entity;
			final BlockPos center = NostrumMagica.getOrCreatePlayerDimensionSpawn(player);
			final UUID dungeonID = player.getUniqueID(); // Use entity UUID as dungeon ID?
			
			if (!DungeonSpawner.DungeonPresent(destWorld, center)) {
				DungeonSpawner.SpawnDungeon(destWorld, center, dungeonID);
			}
			
			final BlockPos spawn = center.north(); // Idr why this is like it is
			Vector3d pos = new Vector3d(spawn.getX() + .5, spawn.getY() + 2, spawn.getZ() + .5);
			float yaw = Direction.NORTH.getHorizontalAngle();
			float pitch = 0;
			return new PortalInfo(pos, Vector3d.ZERO, yaw, pitch);
		}
		
		@Override
		public boolean isVanilla() {
			return false;
		}
		
		@Override
		public boolean playTeleportSound(ServerPlayerEntity player, ServerWorld sourceWorld, ServerWorld destWorld) {
			return true;
		}
		
		/**
		 * Respawn the player in their sorcery dimension dungeon.
		 * Note duplicates logic from getPortalInfo (for positioning) and placeEntity (for entity setup)
		 * @param player
		 */
		public static void respawnPlayer(ServerPlayerEntity player) {
			if (player.world.isRemote) {
				return;
			}
			
			BlockPos spawn = NostrumMagica.getOrCreatePlayerDimensionSpawn(player);
			if (spawn == null) {
				NostrumMagica.logger.warn("Unable to find player spawning location. Sending to overworld.");
				player.changeDimension(player.server.getWorld(World.OVERWORLD));
			} else {
				spawn = spawn.north();
				
				player.teleport(player.server.getWorld(NostrumDimensions.GetSorceryDimension()),
						spawn.getX() + .5, spawn.getY() + 2, spawn.getZ() + .5,
						Direction.NORTH.getHorizontalAngle(),
						0
						);
				player.fallDistance = 0;
			}
		}
	}
	
	public static class DimensionReturnTeleporter implements ITeleporter {
		
		public static final DimensionReturnTeleporter INSTANCE = new DimensionReturnTeleporter();
		
		public DimensionReturnTeleporter() {
			
		}
		
		@Override
		public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
			entity.fallDistance = 0;
			
			// Restore previous spawn point and clear portal entry since it's been used
			if (entity instanceof ServerPlayerEntity && NostrumMagica.getMagicWrapper(entity) != null) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
				@Nullable VanillaRespawnInfo info = attr.getSavedRespawnInfo();
				if (info != null) {
					((ServerPlayerEntity) entity).func_242111_a(info.dimension, info.pos, info.yaw, info.forced, false);
					attr.setSavedRespawnInfo(null);
				}
				
				attr.clearSorceryPortal();
			}
			
			if (entity instanceof ServerPlayerEntity) {
				if (((ServerPlayerEntity) entity).interactionManager.getGameType() == GameType.ADVENTURE) {
					((ServerPlayerEntity) entity).setGameType(GameType.SURVIVAL);
				}
			}
			
			return repositionEntity.apply(false);
		}
		
		@Override
		public PortalInfo getPortalInfo(Entity entity, ServerWorld destWorld, Function<ServerWorld, PortalInfo> defaultPortalInfo) {
			BlockPos pos = null;
			
			if (NostrumMagica.getMagicWrapper(entity) != null) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
				pos = attr.getSorceryPortalPos();
				
				// If no saved portal, try and used saved respawn info
				if (pos == null) {
					@Nullable VanillaRespawnInfo info = attr.getSavedRespawnInfo();
					if (info != null && info.dimension.equals(destWorld.getDimensionKey())) {
						pos = info.pos;
					}
				}
			}
			
			if (pos == null) {
				pos = destWorld.getSpawnPoint();
			}
			
			while (pos.getY() < destWorld.getHeight() && (!destWorld.isAirBlock(pos) || !destWorld.isAirBlock(pos.up()))) {
				pos = pos.up();
			}
			
			Vector3d setPos = new Vector3d(pos.getX() + .5, pos.getY() + 2, pos.getZ() + .5);
			float yaw = Direction.NORTH.getHorizontalAngle();
			float pitch = 0;
			return new PortalInfo(setPos, Vector3d.ZERO, yaw, pitch);
		}
		
		@Override
		public boolean isVanilla() {
			return false;
		}
		
		@Override
		public boolean playTeleportSound(ServerPlayerEntity player, ServerWorld sourceWorld, ServerWorld destWorld) {
			return true;
		}
	}
	
	public static class DimensionListener {

		
		public DimensionListener() {
			MinecraftForge.EVENT_BUS.register(this);
		}
		
		protected boolean checkDimension(@Nullable Entity e) {
			if (e == null) {
				return false;
			}
			
			return checkDimension(DimensionUtils.GetDimension(e));
		}
		
		protected boolean checkDimension(World world) {
			return checkDimension(DimensionUtils.GetDimension(world));
		}
		
		protected boolean checkDimension(RegistryKey<World> dimension) {
			return DimensionUtils.IsSorceryDim(dimension);
		}
		
		@SubscribeEvent
		public void onEnderTeleport(EntityTeleportEvent.EnderPearl event) {
			if (checkDimension(event.getPlayer())) {
				event.setCanceled(true);
			}
		}
		
		@SubscribeEvent
		public void onEnderTeleport(EntityTeleportEvent.ChorusFruit event) {
			if (checkDimension(event.getEntityLiving()) && event.getEntityLiving() instanceof PlayerEntity) {
				event.setCanceled(true);
			}
		}
		
//		private Map<UUID, Boolean> teleportingMarker = new HashMap<>();
		
//		// Hook travel event and change teleporter to our custom once, since
//		// default teleporter looks for nether portals...
//		@SubscribeEvent
//		public void onTeleport(EntityTravelToDimensionEvent event) {
//			if (checkDimension(event.getDimension())) {
//				Entity ent = event.getEntity();
//				Boolean marker = teleportingMarker.get(ent.getUniqueID()); // how is this recursing?
//				if (marker != null && marker){
//					return;
//				}
//				
//				event.setCanceled(true);
//				
//				if (ent instanceof ServerPlayerEntity) {
//					ServerPlayerEntity player = (ServerPlayerEntity) ent;
//					MinecraftServer server = player.getServer();
//					teleportingMarker.put(player.getUniqueID(), true);
//					DimensionEntryTeleporter.respawnPlayer(player);
//					new DimensionEntryTeleporter(server.getWorld(NostrumDimensions.GetSorceryDimension()))
//						.placeInPortal(player, 0f);
//					player.teleport(newWorld, x, y, z, yaw, pitch);
////					server.getPlayerList().transferPlayerToDimension(
////							player, dim, new DimensionEntryTeleporter(server.getWorld(dim)));
//					teleportingMarker.put(player.getUniqueID(), false);
//				}
//			} else if (checkDimension(event.getEntity())
//					&& DimensionUtils.IsOverworld(event.getDimension())) {
//				// Leaving our dimension to homeworld
//				Entity ent = event.getEntity();
//				
//				Boolean marker = teleportingMarker.get(ent.getUniqueID());
//				if (marker != null && marker){
//					return;
//				}
//				
//				// Can't set the world's teleporter to a return one :/
//				event.setCanceled(true);
//
//				if (ent instanceof ServerPlayerEntity) {
//					ServerPlayerEntity player = (ServerPlayerEntity) ent;
//					MinecraftServer server = player.getServer();
//					teleportingMarker.put(player.getUniqueID(), true);
//					
//					RegistryKey<World> toDim = World.OVERWORLD;
//					if (NostrumMagica.getMagicWrapper(player) != null) {
//						INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
//						if (attr.getSorceryPortalPos() != null) {
//							toDim = attr.getSorceryPortalDimension();
//						}
//					}
//					
//					new DimensionReturnTeleporter(server.getWorld(toDim))
//						.placeInPortal(player, 0f);
////					server.getPlayerList().transferPlayerToDimension(
////							player, toDim, new DimensionReturnTeleporter(server.getWorld(toDim)));
//					teleportingMarker.put(player.getUniqueID(), false);
//				}
//			}
//		}
		
		@SubscribeEvent
		public void onExplosion(ExplosionEvent.Detonate event) {
			if (checkDimension(event.getWorld()) && event.getAffectedBlocks() != null) {
				event.getAffectedBlocks().clear();
			}
		}
		
		@SubscribeEvent
		public void onBlockPlace(EntityPlaceEvent event) {
			if (checkDimension(event.getEntity()) && (
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
					&& checkDimension(event.getEntity())) {
				event.setResult(Result.DENY);
			}
		}
		
		@SubscribeEvent
		public void tick(WorldTickEvent event) {
			// Make sure players aren't teleporting.
			// TODO this even is fired before updating, sadly. That feels incorrect.
			// Does it act weirdly?
			if (event.phase != Phase.END) {
				return;
			}
			
			if (event.world == null || event.world.isRemote()) {
				return;
			}
			final ServerWorld world = (ServerWorld) event.world;
			
			if (!checkDimension(world)) {
				return;
			}
			
			for (ServerPlayerEntity player : world.getPlayers()) {
				
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
	}
	
	public static class DimensionClientListener extends DimensionListener {
		
		public DimensionClientListener() {
			MinecraftForge.EVENT_BUS.register(this);
		}
		
		@SubscribeEvent
		public void onFogDensityCheck(EntityViewRenderEvent.FogDensity event) {
			final Entity entity = event.getInfo().getRenderViewEntity();
			if (!checkDimension(entity.world)) {
				return;
			}
			
			event.setCanceled(true);
			
			if (entity instanceof LivingEntity && ((LivingEntity)entity).isPotionActive(Effects.BLINDNESS)) {
				//final Minecraft mc = Minecraft.getInstance();
				float farPlaneDistance = event.getRenderer().getFarPlaneDistance();
				//final Vector3d cameraPos = event.getInfo().getProjectedView();
				//boolean nearFog = ((ClientWorld) entity.world).func_239132_a_().func_230493_a_(MathHelper.floor(cameraPos.getX()), MathHelper.floor(cameraPos.getY())) || mc.ingameGUI.getBossOverlay().shouldCreateFog();
				
				int i = ((LivingEntity)entity).getActivePotionEffect(Effects.BLINDNESS).getDuration();
				float rangeMod = MathHelper.lerp(Math.min(1.0F, (float)i / 20.0F), farPlaneDistance, 5.0F);
				final float near;
				final float far;
				if (event.getType() == FogRenderer.FogType.FOG_SKY) {
					near = 0.0F;
					far = rangeMod * 0.8F;
				} else {
					near = rangeMod * 0.25F;
					far = rangeMod;
				}

				RenderSystem.fogStart(near);
				RenderSystem.fogEnd(far);
				RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
				RenderSystem.setupNvFogDistance();
				net.minecraftforge.client.ForgeHooksClient.onFogRender(event.getType(), event.getInfo(), (float) event.getRenderPartialTicks(), far);
			} else {
				event.setDensity(.03f);
			}
		}
		
		@SubscribeEvent
		public void onFogColorCheck(EntityViewRenderEvent.FogColors event) {
			if (!checkDimension(event.getInfo().getRenderViewEntity().world)) {
				return;
			}
			event.setRed(.2f);
			event.setGreen(0f);
			event.setBlue(.2f);
		}
	}
	
}
