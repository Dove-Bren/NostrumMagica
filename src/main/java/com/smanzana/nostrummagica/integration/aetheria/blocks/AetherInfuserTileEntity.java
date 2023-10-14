package com.smanzana.nostrummagica.integration.aetheria.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrumaetheria.api.blocks.AetherTickingTileEntity;
import com.smanzana.nostrumaetheria.api.proxy.APIProxy;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.IAetherInfusableTileEntity;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.ITameableEntity;
import com.smanzana.nostrummagica.integration.aetheria.AetheriaProxy;
import com.smanzana.nostrummagica.items.IAetherInfuserLens;
import com.smanzana.nostrummagica.tiles.AltarTileEntity;
import com.smanzana.nostrummagica.utils.Inventories.ItemStackArrayWrapper;
import com.smanzana.nostrummagica.utils.WorldUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AetherInfuserTileEntity extends AetherTickingTileEntity {

		private static final String NBT_CHARGE = "charge";
		public static final int MAX_CHARGE = 5000;
		public static final int CHARGE_PER_TICK = 100;
		
		private static final int MAX_SPARKS = 20;
		
		// Synced+saved
		private int charge;
		
		// Transient
		private boolean active; // use getter+setter to sync to client
		private @Nullable AltarTileEntity centerAltar;
		private Map<BlockPos, IAetherInfusableTileEntity> nearbyChargeables; // note: NOT center altar
		private int lastScanRadius;
		
		// Client-only + transient
		private int effectTime; // forever-growing at rate dependent on 'active'
		@OnlyIn(Dist.CLIENT)
		private List<AetherInfuserTileEntity.EffectSpark> sparks;
		
		public AetherInfuserTileEntity() {
			super(AetheriaProxy.AetherInfuserTileEntityType, 0, MAX_CHARGE);
			this.setAutoSync(5);
			this.compWrapper.configureInOut(true, false);
			nearbyChargeables = new HashMap<>();
			
			MinecraftForge.EVENT_BUS.register(this);
		}
		
		public static final void DoChargeEffect(LivingEntity entity, int count, int color) {
			NostrumParticles.GLOW_ORB.spawn(entity.getEntityWorld(), new SpawnParams(
					count,
					entity.posX, entity.posY + entity.getHeight()/2f, entity.posZ, 2.0,
					40, 0,
					entity.getEntityId()
					).color(color));
		}
		
		public static final void DoChargeEffect(World world, BlockPos pos, int count, int color) {
			DoChargeEffect(world, new Vec3d(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5), count, color);
		}
		
		public static final void DoChargeEffect(World world, Vec3d center, int count, int color) {
			NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
					count,
					center.x, center.y, center.z, 2.0,
					40, 0,
					center
					).color(color));
		}
		
		public static final void DoChargeEffect(World world, Vec3d start, Vec3d end, int count, int color) {
			NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
					count,
					start.x, start.y, start.z, .5,
					60, 20,
					end
					).color(color));
		}
		
		protected void chargePlayer(PlayerEntity player) {
			int chargeAmount = Math.min(CHARGE_PER_TICK, this.getCharge());
			final int startAmount = chargeAmount;
			// Try both regular inventory and bauble inventory
			IInventory inv = player.inventory;
			chargeAmount -= APIProxy.pushToInventory(world, player, inv, chargeAmount);
			
			inv = NostrumMagica.instance.curios.getCurios(player);
			if (inv != null) {
				chargeAmount -= APIProxy.pushToInventory(world, player, inv, chargeAmount);
			}
			
			if (startAmount != chargeAmount) {
				this.getHandler().drawAether(null, startAmount - chargeAmount);
				
				final int diff = startAmount - chargeAmount;
				float countRaw = (float) diff / (float) (CHARGE_PER_TICK / 3);
				final int whole = (int) countRaw;
				if (whole > 0 || NostrumMagica.rand.nextFloat() < countRaw) {
//					NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
//							whole > 0 ? whole : 1,
//							player.posX, player.posY + player.height/2f, player.posZ, 2.0,
//							40, 0,
//							player.getEntityId()
//							));
					DoChargeEffect(player, whole > 0 ? whole : 1, 0x4D3366FF);
				}
			}
			
			// TODO look at held item for lenses
		}
		
		protected void chargeAltar(BlockPos pos, AltarTileEntity te) {
			int chargeAmount = Math.min(CHARGE_PER_TICK, this.getCharge());
			final int startAmount = chargeAmount;
			if (te != null && !te.getItem().isEmpty()) {
				ItemStack held = te.getItem();
				IInventory inv = new ItemStackArrayWrapper(new ItemStack[] {held});
				chargeAmount -= APIProxy.pushToInventory(world, null, inv, chargeAmount);
			}
			
			if (startAmount != chargeAmount) {
				this.getHandler().drawAether(null, startAmount - chargeAmount);
				// Set number to spawn based on how much aether we actually put in
				final int diff = startAmount - chargeAmount;
				float countRaw = (float) diff / (float) (CHARGE_PER_TICK / 3);
				final int whole = (int) countRaw;
				if (whole > 0 || NostrumMagica.rand.nextFloat() < countRaw) {
//					NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
//							whole > 0 ? whole : 1,
//							pos.getX() + .5, pos.getY() + 1.2, pos.getZ() + .5, 2.0,
//							40, 0,
//							new Vec3d(pos.getX() + .5, pos.getY() + 1.2, pos.getZ() + .5)
//							));
					DoChargeEffect(world, new Vec3d(pos.getX() + .5, pos.getY() + 1.2, pos.getZ() + .5), whole > 0 ? whole : 1, 0x4D3366FF);
				}
			}
		}
		
		@Override
		public void tick() {
			super.tick();
			
			if (world.isRemote) {
				effectTime++;
				
				if (this.isActive()) {
					effectTime++; // double speed
				}
				
				this.updateSparks();
				
				if (this.getCharge() > 0) {
					// extra particles
					final float CHANCE = (float) getCharge() / ((float) MAX_CHARGE * 1f);
					final float RADIUS = 3;
					final Random rand = NostrumMagica.rand;
					if (NostrumMagica.rand.nextFloat() < CHANCE) {
						final double x = (pos.getX() + .5 + (NostrumMagica.rand.nextFloat() * RADIUS)) - (RADIUS / 2f);
						final double y = (pos.getY() + 1.5 + (NostrumMagica.rand.nextFloat() * RADIUS)) - 1;
						final double z = (pos.getZ() + .5 + (NostrumMagica.rand.nextFloat() * RADIUS)) - (RADIUS / 2f);
						world.addParticle(ParticleTypes.MYCELIUM,
								x, y, z,
								0, 0, 0);
						
						int num = (active ? 10 : NostrumMagica.rand.nextFloat() < .05f ? 1 : 0);
						NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
								num,
								x, y, z, 0, 100, 20,
								new Vec3d(rand.nextFloat() * .05 - .025, rand.nextFloat() * .05, rand.nextFloat() * .05 - .025), null
							).color(.1f, .3f, 1f, .4f));
					}
				}
				
				return;
			}
			
			// First, use our set up altar if we have one
			if (world.getGameTime() % 5 == 0) {
				refreshAltar();
			}
			
			if (this.centerAltar != null && !this.hasAreaCharge()) {
				// Altar!
				if (this.hasLens()) {
					int maxAether = Math.min(CHARGE_PER_TICK, this.getCharge());
					final int originalMax = maxAether;
					int lastAether = maxAether;
					if (this.hasAreaInfuse()) {
						for (IAetherInfusableTileEntity te : this.nearbyChargeables.values()) {
							if (te.canAcceptAetherInfuse(this, maxAether)) {
								maxAether = te.acceptAetherInfuse(this, maxAether);
								
								if (lastAether != maxAether) {
									if (te instanceof TileEntity) {
										final TileEntity teRaw = (TileEntity) te;
										final int diff = lastAether - maxAether;
										float countRaw = (float) diff / (float) (CHARGE_PER_TICK / 3);
										final int whole = (int) countRaw;
										if (whole > 0 || NostrumMagica.rand.nextFloat() < countRaw) {
											DoChargeEffect(world,
													new Vec3d(pos.getX() + .5, pos.getY() + 1.2, pos.getZ() + .5),
													new Vec3d(teRaw.getPos().getX() + .5, teRaw.getPos().getY() + 1.2, teRaw.getPos().getZ() + .5),
													whole > 0 ? whole : 1,
													0x4D3366FF);
											
											
										}
									}
									
									lastAether = maxAether;
								}
							}
						}
					} else if (this.hasMobSpawnPrevention()) {
						; // No work to do here but don't want to tick lens either
					} else {
						// Do whatever lense says to do
						ItemStack lensItem = centerAltar.getItem();
						IAetherInfuserLens lens = (IAetherInfuserLens) lensItem.getItem();
						if (lens.canAcceptAetherInfuse(lensItem, pos.up(), this, maxAether)) {
							maxAether = lens.acceptAetherInfuse(lensItem, pos.up(), this, maxAether);
							
							if (originalMax != maxAether) {
								final int diff = lastAether - maxAether;
								float countRaw = (float) diff / (float) (CHARGE_PER_TICK / 3);
								final int whole = (int) countRaw;
								if (whole > 0 || NostrumMagica.rand.nextFloat() < countRaw) {
									DoChargeEffect(world,
											new Vec3d(centerAltar.getPos().getX() + .5, centerAltar.getPos().getY() + 1.2, centerAltar.getPos().getZ() + .5),
											whole > 0 ? whole : 1,
											0x4D3366FF);
								}
							}
						}
					}
					
					if (maxAether != originalMax) {
						this.getHandler().drawAether(null, originalMax - maxAether);
					}
				} else {
					chargeAltar(pos.up(), (AltarTileEntity) world.getTileEntity(pos.up()));
				}
			} else {
				// Check for entities in AoE
				final int radius = this.hasAreaCharge() ? this.getChargeAreaRadius() : 4; // 4 is size of bubble
				final BlockPos min = (pos.add(-radius, -radius, -radius));
				final BlockPos max = (pos.add(radius, radius, radius));
				List<Entity> candidates = world.getEntitiesWithinAABB(PlayerEntity.class, new AxisAlignedBB(
						min, max
						));
				PlayerEntity minPlayer = null;
				double minDist = Double.MAX_VALUE;
				final double radiusSq = radius * radius;
				for (Entity candidate : candidates) {
					if (!(candidate instanceof PlayerEntity)) {
						continue;
					}
					
					PlayerEntity player = (PlayerEntity) candidate;
					final double dist = player.getDistanceSq(pos.getX() + .5, pos.getY() + .5 + 2, pos.getZ() + .5);
					if (dist < radiusSq && dist < minDist) {
						minDist = dist;
						minPlayer = player;
					}
				}
				
				if (minPlayer != null) {
					chargePlayer(minPlayer);
				}
			}
			
		}
		
		@Override
		public CompoundNBT write(CompoundNBT nbt) {
			nbt = super.write(nbt);
			
			if (nbt == null)
				nbt = new CompoundNBT();
			
			nbt.putInt(NBT_CHARGE, charge);
			
			return nbt;
		}
		
		@Override
		public void read(CompoundNBT nbt) {
			super.read(nbt);
			
			if (nbt == null)
				return;
			
			this.charge = nbt.getInt(NBT_CHARGE);
		}
		
		@Override
		public void setWorld(World world) {
			super.setWorld(world);
			
			if (!world.isRemote) {
				this.compWrapper.setAutoFill(true, 20);
			} else {
				this.sparks = new ArrayList<>();
			}
			
			NostrumMagica.playerListener.registerTimer((type, entity, data)->{
				//Event type, LivingEntity entity, Object data
				refreshNearbyBlocks();
				return true;
			}, 1, 0);
		}
		
		public int getCharge() {
			// Convenience wrapper around all the optional aether bits
			return this.getHandler().getAether(null); // We require aether to work anyways so being unsafe
		}
		
		public float getChargePerc() {
			return ((float) getCharge()) / (float) MAX_CHARGE;
		}
		
		@Override
		public boolean receiveClientEvent(int id, int type) {
			if (id == 0) {
				if (this.world != null && this.world.isRemote) {
					setActive(type == 1);
				}
				return true;
			}
			
			return super.receiveClientEvent(id, type);
		}
		
		protected void onActiveChange() {
			
		}
		
		private void setActive(boolean active) {
			if (this.active != active && world != null) {
				
				if (!world.isRemote) {
					world.addBlockEvent(getPos(), getBlockState().getBlock(), 0, active ? 1 : 0);
				}
				
				this.active = active;
				onActiveChange();
			}
		}
		
		public boolean isActive() {
			return active;
		}
		
		@OnlyIn(Dist.CLIENT)
		public int getEffectTicks() {
			return effectTime;
		}
		
		@OnlyIn(Dist.CLIENT)
		public void spawnSpark() {
			synchronized(sparks) {
				sparks.add(new EffectSpark(
						effectTime,
						20 * (10 + NostrumMagica.rand.nextInt(5)),
						20 * (30 + NostrumMagica.rand.nextInt(20)),
						NostrumMagica.rand.nextBoolean(),
						0f, // always start at bottom
						NostrumMagica.rand.nextFloat(),
						.5f // brightness but will be adjusted right after
					));
			}
		}
		
		@OnlyIn(Dist.CLIENT)
		public void removeSpark() {
			synchronized(sparks) {
				if (sparks.isEmpty()) {
					return;
				}
				
				sparks.remove(NostrumMagica.rand.nextInt(sparks.size()));
			}
		}
		
		@OnlyIn(Dist.CLIENT)
		public List<AetherInfuserTileEntity.EffectSpark> getSparks(@Nullable List<AetherInfuserTileEntity.EffectSpark> storage) {
			if (storage == null) {
				storage = new ArrayList<>();
			} else {
				storage.clear();
			}
			
			synchronized(sparks) {
				storage.addAll(sparks);
			}
			
			return storage;
		}
		
		@OnlyIn(Dist.CLIENT)
		public void updateSparks() {
			// Spawn or despawn sparks, and adjust brightness if necessary
			float chargePerc = getChargePerc();
			int sparkCount = Math.round(chargePerc * MAX_SPARKS);
			
			// Make spawning/despawning slow and a little random
			if (sparkCount != sparks.size() && NostrumMagica.rand.nextFloat() < .05f) {
				if (sparkCount > sparks.size()) {
					spawnSpark();
				} else {
					removeSpark();
				}
			}
			
			for (AetherInfuserTileEntity.EffectSpark spark : sparks) {
				spark.brightness = chargePerc;
			}
		}
		
		@OnlyIn(Dist.CLIENT)
		public net.minecraft.util.math.AxisAlignedBB getRenderBoundingBox() {
			return TileEntity.INFINITE_EXTENT_AABB;
		}
		
		protected void refreshAltar() {
			this.centerAltar = null;
			BlockPos pos = this.pos.up();
			TileEntity te = world.getTileEntity(pos);
			if (te != null && te instanceof AltarTileEntity) {
				this.centerAltar = (AltarTileEntity) te;
				if (this.hasAreaInfuse() && getInfuseAreaRadius() != lastScanRadius) {
					lastScanRadius = getInfuseAreaRadius();
					this.onInfuseAreaChange();
				} else if (!this.hasAreaInfuse()) {
					lastScanRadius = 0;
				}
			}
		}
		
		public boolean hasLens() {
			if (this.centerAltar != null) {
				ItemStack held = centerAltar.getItem();
				if (!held.isEmpty() && held.getItem() instanceof IAetherInfuserLens) {
					return true;
				}
			}
			
			return false;
		}
		
		/**
		 * Specifically charging players!
		 * @return
		 */
		public boolean hasAreaCharge() {
			if (hasLens()) {
				ItemStack held = centerAltar.getItem();
				return !held.isEmpty()
						&& held.getItem() == AetheriaProxy.chargeAetherLens;
			}
			
			return false;
		}
		
		/**
		 * Whether this infuser should prevent nearby mob spawns
		 * @return
		 */
		public boolean hasMobSpawnPrevention() {
			if (hasLens()) {
				ItemStack held = centerAltar.getItem();
				return !held.isEmpty()
						&& held.getItem() == AetheriaProxy.noSpawnAetherLens;
			}
			
			return false;
		}
		
		/**
		 * Area to remotely charge player's inventories
		 * @return
		 */
		public int getChargeAreaRadius() {
			return 50; // TODO different areas?
		}
		
		/**
		 * Specifically giving aether to altars with lenses!
		 * @return
		 */
		public boolean hasAreaInfuse() {
			if (hasLens()) {
				ItemStack held = centerAltar.getItem();
				return !held.isEmpty()
						&& held.getItem() == AetheriaProxy.spreadAetherLens;
			}
			return false;
		}
		
		/**
		 * Area to look for blocks with infusables
		 * @return
		 */
		public int getInfuseAreaRadius() {
			return 20; // Make upgradeable?
		}
		
		public int getMobSpawnProtectionRadius() {
			return 50; // Make upgradeable?
		}
		
		protected void onInfuseAreaChange() {
			if (!hasAreaInfuse()) {
				this.nearbyChargeables.clear();
			} else {
				this.refreshNearbyBlocks();
			}
		}
		
		@SubscribeEvent(priority=EventPriority.LOWEST)
		public void onMobSpawn(LivingSpawnEvent.CheckSpawn event) {
			if (event.getResult() != Result.DEFAULT) {
				return;
			}
			
			if (!this.hasMobSpawnPrevention()
					|| event.isSpawner()
					|| event.getWorld() != this.world
					|| event.getEntityLiving() == null
					|| !event.getEntityLiving().isNonBoss()
					) {
				event.setResult(Result.DEFAULT);
				return;
			}
			
			// We have to have aether
			if (this.getCharge() <= 0) {
				event.setResult(Result.DEFAULT);
				return;
			}
			
			final LivingEntity entity = event.getEntityLiving();
			
			// Figure out what to not allow through
			boolean isBad = false;
			if (entity instanceof MonsterEntity) {
				isBad = true;
				
				// But make an exception for 'tameable' mobs
				if (entity instanceof ITameableEntity) {
					isBad = false;
				}
			}
			
			event.setResult(Result.DEFAULT);
			
			if (isBad) {
				final double radius = this.getMobSpawnProtectionRadius();
				if (entity.getDistanceSq(pos.getX() + .5, pos.getY(), pos.getZ() + .5) < radius * radius) {
					event.setResult(Result.DENY);
					
					// Mob spawning happens a lot (and in bursts, or continuously if there's a spawner)
					// so make most prevents free
					if (NostrumMagica.rand.nextInt(20) == 0) {
						this.getHandler().drawAether(null, 1);
						AetherInfuserTileEntity.DoChargeEffect(world, pos.up().up(), 1, 0xFF807020);
					}
				}
			}
		}
		
		@SubscribeEvent
		public void onBlockBreak(BreakEvent event) {
			if (event.getWorld().isRemote()) {
				return;
			}
			
			final BlockPos blockpos = event.getPos().toImmutable();
			if (blockpos.equals(this.pos.up())) {
				this.refreshAltar();
				return;
			}
			
			if (!hasAreaInfuse()) {
				return;
			}
			
			final int radius = getInfuseAreaRadius();
			
			if (Math.abs(blockpos.getX() - this.pos.getX()) <= radius
					&& Math.abs(blockpos.getY() - this.pos.getY()) <= radius
					&& Math.abs(blockpos.getZ() - this.pos.getZ()) <= radius) {
				// Use a timer since there isn't a POST break event
				NostrumMagica.playerListener.registerTimer((type, entity, data) -> {
					refreshNearbyBlock(blockpos);
					//refreshNearbyBlocks();
					return true;
				}, 1, 1);
			}
		}
		
		@SubscribeEvent
		public void onBlockPlace(EntityPlaceEvent event) {
			if (event.getWorld().isRemote()) {
				return;
			}
			
			final BlockPos blockpos = event.getPos().toImmutable();
			if (blockpos.equals(this.pos.up())) {
				this.refreshAltar();
				return;
			}
			
			if (!hasAreaInfuse()) {
				return;
			}
			
			final int radius = getInfuseAreaRadius();
			if (Math.abs(blockpos.getX() - this.pos.getX()) <= radius
					&& Math.abs(blockpos.getY() - this.pos.getY()) <= radius
					&& Math.abs(blockpos.getZ() - this.pos.getZ()) <= radius
					) {
				// ""
				NostrumMagica.playerListener.registerTimer((type, entity, data) -> {
					refreshNearbyBlock(blockpos);
					//refreshNearbyBlocks();
					return true;
				}	, 1, 1);
			}
		}
		
		@SubscribeEvent
		public void onChunkLoad(ChunkEvent.Load event) {
			if (event.getWorld().isRemote()) {
				return;
			}
			
			if (!hasAreaInfuse()) {
				return;
			}
			
			final int radius = getInfuseAreaRadius() + 16; // easier than looking at both min and max
			final IChunk chunk = event.getChunk();
			final BlockPos chunkMin = new BlockPos(chunk.getPos().x << 4, this.getPos().getY(), chunk.getPos().z << 4);
			if (WorldUtil.getBlockDistance(chunkMin, this.getPos()) < radius) {
				// ""
				NostrumMagica.playerListener.registerTimer((type, entity, data) -> {
					refreshChunk(chunk);
					//refreshNearbyBlocks();
					return true;
				}	, 1, 1);
			}
		}
		
		protected @Nullable IAetherInfusableTileEntity checkBlock(BlockPos blockpos) {
			if (!blockpos.equals(this.pos.up())) { // ignore altar above platform
				TileEntity te = world.getTileEntity(blockpos);
				if (te != null && te instanceof IAetherInfusableTileEntity) {
					return (IAetherInfusableTileEntity) te;
				}
			}
			return null;
		}
		
		protected void refreshNearbyBlocks() {
			this.nearbyChargeables.clear();
			
			// get radius from item in altar?
			final int radius = getInfuseAreaRadius();
			WorldUtil.ScanBlocks(world,
					new BlockPos(pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius),
					new BlockPos(pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius),
					(world, blockpos) -> {
						IAetherInfusableTileEntity te = checkBlock(blockpos);
						if (te != null) {
							nearbyChargeables.put(blockpos.toImmutable(), (IAetherInfusableTileEntity) te);
						}
						return true;
					});
		}
		
		protected void refreshChunk(IChunk chunk) {
			// Actually get min and max based on distance from us
			final int radius = getInfuseAreaRadius();
			final BlockPos min;
			final BlockPos max;
			final int minChunkX = chunk.getPos().x << 4;
			final int minChunkZ = chunk.getPos().z << 4;
			final int maxChunkX = minChunkX + 16;
			final int maxChunkZ = minChunkZ + 16;
			final int minBlockX = this.pos.getX() - radius;
			final int minBlockZ = this.pos.getZ() - radius;
			final int minBlockY = this.pos.getY() - radius;
			final int maxBlockX = this.pos.getX() - radius;
			final int maxBlockZ = this.pos.getZ() - radius;
			final int maxBlockY = this.pos.getY() - radius;
			
			final int minX = Math.min(minChunkX, minBlockX);
			final int minY = minBlockY;
			final int minZ = Math.min(minChunkZ, minBlockZ);
			final int maxX = Math.min(maxChunkX, maxBlockX);
			final int maxY = maxBlockY;
			final int maxZ = Math.min(maxChunkZ, maxBlockZ);
			
			min = new BlockPos(minX, minY, minZ);
			max = new BlockPos(maxX, maxY, maxZ);
			WorldUtil.ScanBlocks(world, min, max, (world, blockpos) -> {
				refreshNearbyBlock(blockpos);
				return true;
			});
		}
		
		protected void refreshNearbyBlock(BlockPos blockpos) {
			nearbyChargeables.remove(blockpos);
			
			IAetherInfusableTileEntity te = checkBlock(blockpos);
			if (te != null) {
				nearbyChargeables.put(blockpos.toImmutable(), (IAetherInfusableTileEntity) te);
			}
		}
		
		@OnlyIn(Dist.CLIENT)
		public static class EffectSpark {
			
			public static final int BLINK_PERIOD = (20 * 4);
			public static final float BLINK_FACTOR = 1f / (float) BLINK_PERIOD;
			
			public float brightness; // [0-1]
			public final float pitchStart; // [0-1]
			public final float yawStart; // [0-1]
			public final float pitchFactor; // [-1-1]
			public final float yawFactor; // [-1-1]
			
			public final int spawnTime;
			
			public EffectSpark(int spawnTime, 
					float pitchFactor, float yawFactor, float startingPitch, float startingYaw, float brightness) {
				this.spawnTime = spawnTime;
				this.pitchStart = startingPitch;
				this.yawStart = startingYaw;
				this.pitchFactor = pitchFactor;
				this.yawFactor = yawFactor;
			}
			
			public EffectSpark(int spawnTime, float pitchPeriod, float yawPeriod, boolean forwardDir,
					float startingPitch, float startingYaw, float brightness) {
				// period is ticks for a rotation, ofc
				this(spawnTime,
					(1f / pitchPeriod) * (forwardDir ? 1 : -1),
					(1f / yawPeriod) * (forwardDir ? 1 : -1),
					startingPitch, startingYaw, brightness);
			}
			
			private static final float Clamp(float in) {
				return in % 1f;
			}
			
			public float getPitch(int ticks, float partialTicks) {
				return Clamp(pitchStart + (float) (
						((double) (ticks - spawnTime) + (double) partialTicks) * pitchFactor)
					);
			}
			
			public float getYaw(int ticks, float partialTicks) {
				return Clamp(yawStart + (float) (((double) (ticks - spawnTime) + (double) partialTicks) * yawFactor)
					);
			}
			
			public float getBrightness(int ticks, float partialTicks) {
				brightness = 1f;
				// use input brightness (0-1) at 60% to allow for glowing
				// glow based on BLINK_PERIOD
				final float t = Clamp((float) (((double) (ticks - spawnTime) + partialTicks) * BLINK_FACTOR));
				final double tRad = t * Math.PI * 2;
				final float tAdj = (float) (Math.sin(tRad) + 1f) / 2f;
				return brightness * (.2f + .8f * tAdj);
			}
		}
	}