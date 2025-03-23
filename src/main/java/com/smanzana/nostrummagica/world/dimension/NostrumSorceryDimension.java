package com.smanzana.nostrummagica.world.dimension;

import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.autodungeons.world.dungeon.room.IDungeonRoomRef.DungeonRoomRef;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.INostrumMagic.VanillaRespawnInfo;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;

public class NostrumSorceryDimension {
	
	public static final int SPAWN_Y = 128;
	
	private static final ResourceLocation DIMENSION_ENTRY_TEMPLATE = NostrumMagica.Loc("sorcery_lobby");
	private static final ResourceLocation DIMENSION_WHOLE_TEMPLATE = NostrumMagica.Loc("sorcery_dungeon");
	
	public static final void RegisterListener() {
		DistExecutor.unsafeRunForDist(() -> DimensionClientListener::new, () -> DimensionListener::new);
	}
	
	private static final class DungeonSpawner {
		private static DungeonRoomRef lobbyBlueprint = null;
		private static DungeonRoomRef wholeBlueprint = null;
		
		private static void initBlueprints() {
			lobbyBlueprint = new DungeonRoomRef(DIMENSION_ENTRY_TEMPLATE);
			wholeBlueprint = new DungeonRoomRef(DIMENSION_WHOLE_TEMPLATE);
			
			if (!lobbyBlueprint.isValid()) {
				NostrumMagica.logger.fatal("Failed to load sorcery lobby from name " + DIMENSION_ENTRY_TEMPLATE);
			}
			
			if (!wholeBlueprint.isValid()) {
				NostrumMagica.logger.fatal("Failed to load sorcery dungeon from name " + DIMENSION_WHOLE_TEMPLATE);
			}
		}
		
		protected static final void SpawnDungeon(ServerLevel world, BlockPos center, UUID dungeonID) {
			if (lobbyBlueprint == null) {
				initBlueprints();
			}
			
			long startTime = System.currentTimeMillis();
			lobbyBlueprint.spawn(world, new BlueprintLocation(center, Direction.NORTH), null, dungeonID);
			NostrumMagica.logger.info("Took " + ((double) (System.currentTimeMillis() - startTime) / 1000.0) + " seconds to generate sorcery lobby");
			
			startTime = System.currentTimeMillis();
			wholeBlueprint.spawn(world, new BlueprintLocation(center, Direction.NORTH), null, dungeonID);
			NostrumMagica.logger.info("Took " + ((double) (System.currentTimeMillis() - startTime) / 1000.0) + " seconds to generate whole dungeon");
		}
		
		protected static final boolean DungeonPresent(ServerLevel world, BlockPos center) {
			return !world.isEmptyBlock(center.above());
		}
		
	}
		
	public static class DimensionEntryTeleporter implements ITeleporter {
		
		public static final DimensionEntryTeleporter INSTANCE = new DimensionEntryTeleporter();
		
		private DimensionEntryTeleporter() {
			
		}
		
		@Override
		public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
			if (!(entity instanceof ServerPlayer)) {
				return entity;
			}
			
			final ServerPlayer player = (ServerPlayer) entity;
			player.fallDistance = 0; // Copied from TwilightForest, even though entities don't fall into our portals
			
			// I wish we got passed the PortalInfo that's gonna be used to move us in repositionEntity so I didn't
			// have to recompute this and rely on it being the same!
			final BlockPos center = NostrumMagica.getOrCreatePlayerDimensionSpawn(player);
			final BlockPos spawn = center.north();
			
			// Capture current spawn point and then set new one in the sorcery dimension
			if (NostrumMagica.getMagicWrapper(player) != null) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
				BlockPos blockpos = player.getRespawnPosition();
				
				// Vanilla encodes no specific respawn as blockpos==null
				if (blockpos != null) {
					float yawToSave = player.getRespawnAngle();
					boolean force = player.isRespawnForced();
					ResourceKey<Level> dim = player.getRespawnDimension();
					attr.setSavedRespawnInfo(new VanillaRespawnInfo(dim, blockpos, yawToSave, force));
				}
				// else clear?
			}
			player.setRespawnPosition(NostrumDimensions.GetSorceryDimension(), spawn.above(2), Direction.NORTH.toYRot(), true, false);
			
			if (!player.isCreative() && !player.isSpectator()) {
				player.setGameMode(GameType.ADVENTURE);
			}
			
			return repositionEntity.apply(false);
		}
		
		@Override
		@Nullable
		public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
			if (!(entity instanceof ServerPlayer)) {
				return null;
			}
			
			final ServerPlayer player = (ServerPlayer) entity;
			final BlockPos center = NostrumMagica.getOrCreatePlayerDimensionSpawn(player);
			final UUID dungeonID = player.getUUID(); // Use entity UUID as dungeon ID?
			
			if (!DungeonSpawner.DungeonPresent(destWorld, center)) {
				DungeonSpawner.SpawnDungeon(destWorld, center, dungeonID);
			}
			
			final BlockPos spawn = center.north(); // Idr why this is like it is
			Vec3 pos = new Vec3(spawn.getX() + .5, spawn.getY() + 2, spawn.getZ() + .5);
			float yaw = Direction.NORTH.toYRot();
			float pitch = 0;
			return new PortalInfo(pos, Vec3.ZERO, yaw, pitch);
		}
		
		@Override
		public boolean isVanilla() {
			return false;
		}
		
		@Override
		public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
			return true;
		}
		
		/**
		 * Respawn the player in their sorcery dimension dungeon.
		 * Note duplicates logic from getPortalInfo (for positioning) and placeEntity (for entity setup)
		 * @param player
		 */
		public static void respawnPlayer(ServerPlayer player) {
			if (player.level.isClientSide) {
				return;
			}
			
			BlockPos spawn = NostrumMagica.getOrCreatePlayerDimensionSpawn(player);
			if (spawn == null) {
				NostrumMagica.logger.warn("Unable to find player spawning location. Sending to overworld.");
				player.changeDimension(player.server.getLevel(Level.OVERWORLD));
			} else {
				spawn = spawn.north();
				
				player.teleportTo(player.server.getLevel(NostrumDimensions.GetSorceryDimension()),
						spawn.getX() + .5, spawn.getY() + 2, spawn.getZ() + .5,
						Direction.NORTH.toYRot(),
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
		public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
			entity.fallDistance = 0;
			
			// Restore previous spawn point and clear portal entry since it's been used
			if (entity instanceof ServerPlayer && NostrumMagica.getMagicWrapper(entity) != null) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
				@Nullable VanillaRespawnInfo info = attr.getSavedRespawnInfo();
				if (info != null) {
					((ServerPlayer) entity).setRespawnPosition(info.dimension, info.pos, info.yaw, info.forced, false);
					attr.setSavedRespawnInfo(null);
				}
				
				attr.clearSorceryPortal();
			}
			
			if (entity instanceof ServerPlayer) {
				if (((ServerPlayer) entity).gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
					((ServerPlayer) entity).setGameMode(GameType.SURVIVAL);
				}
			}
			
			return repositionEntity.apply(false);
		}
		
		@Override
		public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
			BlockPos pos = null;
			
			if (NostrumMagica.getMagicWrapper(entity) != null) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
				pos = attr.getSorceryPortalPos();
				
				// If no saved portal, try and used saved respawn info
				if (pos == null) {
					@Nullable VanillaRespawnInfo info = attr.getSavedRespawnInfo();
					if (info != null && info.dimension.equals(destWorld.dimension())) {
						pos = info.pos;
					}
				}
			}
			
			if (pos == null) {
				pos = destWorld.getSharedSpawnPos();
			}
			
			while (pos.getY() < destWorld.getMaxBuildHeight() && (!destWorld.isEmptyBlock(pos) || !destWorld.isEmptyBlock(pos.above()))) {
				pos = pos.above();
			}
			
			Vec3 setPos = new Vec3(pos.getX() + .5, pos.getY() + 2, pos.getZ() + .5);
			float yaw = Direction.NORTH.toYRot();
			float pitch = 0;
			return new PortalInfo(setPos, Vec3.ZERO, yaw, pitch);
		}
		
		@Override
		public boolean isVanilla() {
			return false;
		}
		
		@Override
		public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
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
		
		protected boolean checkDimension(Level world) {
			return checkDimension(DimensionUtils.GetDimension(world));
		}
		
		protected boolean checkDimension(ResourceKey<Level> dimension) {
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
			if (checkDimension(event.getEntityLiving()) && event.getEntityLiving() instanceof Player) {
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
					&& event.getEntity().level != null
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
			
			if (event.world == null || event.world.isClientSide()) {
				return;
			}
			final ServerLevel world = (ServerLevel) event.world;
			
			if (!checkDimension(world)) {
				return;
			}
			
			for (ServerPlayer player : world.players()) {
				
				// Make sure they aren't falling out of the world
				if (player.getY() < 1) {
					NostrumMagica.logger.info("Respawning player " + player + " because they seem to have fallen out of the world");
					DimensionEntryTeleporter.respawnPlayer(player);
					continue; // skip rate limitting
				}
				
				if (player.isCreative() || player.isSpectator()) {
					continue;
				}
				
				double distSqr = Math.pow(player.getX() - player.xOld, 2)
						+ Math.pow(player.getZ() - player.zOld, 2)
						+ Math.pow(player.getY() - player.yOld, 2);
				if (distSqr > 25) {
					// Player appears to have teleported
					player.teleportTo(player.xOld, player.yOld, player.zOld);
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
			final Entity entity = event.getInfo().getEntity();
			if (!checkDimension(entity.level)) {
				return;
			}
			
			event.setCanceled(true);
			
			if (entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.BLINDNESS)) {
				//final Minecraft mc = Minecraft.getInstance();
				float farPlaneDistance = event.getRenderer().getRenderDistance();
				//final Vector3d cameraPos = event.getInfo().getProjectedView();
				//boolean nearFog = ((ClientWorld) entity.world).effects().isFoggyAt(MathHelper.floor(cameraPos.getX()), MathHelper.floor(cameraPos.getY())) || mc.ingameGUI.getBossOverlay().shouldCreateFog();
				
				int i = ((LivingEntity)entity).getEffect(MobEffects.BLINDNESS).getDuration();
				float rangeMod = Mth.lerp(Math.min(1.0F, (float)i / 20.0F), farPlaneDistance, 5.0F);
				final float near;
				final float far;
				if (event.getType() == FogRenderer.FogMode.FOG_SKY) {
					near = 0.0F;
					far = rangeMod * 0.8F;
				} else {
					near = rangeMod * 0.25F;
					far = rangeMod;
				}

//				RenderSystem.fogStart(near);
//				RenderSystem.fogEnd(far);
//				RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
//				RenderSystem.setupNvFogDistance();
				RenderSystem.setShaderFogStart(near);
				RenderSystem.setShaderFogEnd(far);
				net.minecraftforge.client.ForgeHooksClient.onFogRender(event.getType(), event.getInfo(), (float) event.getRenderPartialTicks(), far);
			} else {
				event.setDensity(.03f);
			}
		}
		
		@SubscribeEvent
		public void onFogColorCheck(EntityViewRenderEvent.FogColors event) {
			if (!checkDimension(event.getInfo().getEntity().level)) {
				return;
			}
			event.setRed(.2f);
			event.setGreen(0f);
			event.setBlue(.2f);
		}
	}
	
}
