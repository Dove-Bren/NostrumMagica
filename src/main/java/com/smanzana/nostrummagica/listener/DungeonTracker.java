package com.smanzana.nostrummagica.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.DungeonTrackerUpdateMessage;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonInstance;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructure;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

/**
 * Track which dungeon a player is in. Synchronized between server and client.
 * @author Skyler
 *
 */
public class DungeonTracker {
	
	public static final class DungeonRecord {
		public final @Nullable DungeonInstance instance;
		
		public DungeonRecord(@Nullable DungeonInstance instance) {
			this.instance = instance;
		}
		
		@Override
		public int hashCode() {
			return instance.hashCode() * 5441 + 91;
		}
		
		@Override
		public boolean equals(Object o) {
			return o instanceof DungeonRecord && Objects.equals(((DungeonRecord) o).instance, this.instance);
		}
		
		private static final String NBT_INSTANCE = "instance";
		
		public CompoundNBT toNBT() {
			CompoundNBT tag = new CompoundNBT();
			if (instance != null) {
				tag.put(NBT_INSTANCE, instance.toNBT());
			}
			return tag;
		}
		
		public static final DungeonRecord FromNBT(CompoundNBT nbt) {
			return new DungeonRecord(nbt.contains(NBT_INSTANCE) ? DungeonInstance.FromNBT(nbt.get(NBT_INSTANCE)) : null);
		}
	}

	private final Map<PlayerEntity, DungeonRecord> dungeonMap;
	
	public DungeonTracker() {
		this.dungeonMap = new HashMap<>();
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public DungeonRecord getDungeon(PlayerEntity player) {
		return dungeonMap.computeIfAbsent(player, p -> new DungeonRecord(null));
	}
	
	protected void setDungeon(PlayerEntity player, DungeonRecord record) {
		DungeonRecord prev = this.dungeonMap.put(player, record);
		if ((prev == null || !prev.equals(record)) && !player.getEntityWorld().isRemote()) {
			notifyPlayer((ServerPlayerEntity) player);
		}
	}
	
	protected void notifyPlayer(ServerPlayerEntity player) {
		DungeonRecord record = this.getDungeon(player);
		NetworkHandler.sendTo(new DungeonTrackerUpdateMessage(player.getUniqueID(), record), player);
	}
	
	public void overrideClientDungeon(PlayerEntity player, DungeonRecord record) {
		this.setDungeon(player, record);
	}
	
	protected void updatePlayer(ServerPlayerEntity player) {
		final @Nullable DungeonInstance current;
		if (!player.isAlive()) {
			current = null;
		} else {
			current = NostrumDungeonStructure.GetDungeonAt(player.getServerWorld(), player.getPosition());
		}
		setDungeon(player, new DungeonRecord(current));
	}
	
	@SubscribeEvent
	public void serverTick(ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			for (ServerWorld world : LogicalSidedProvider.INSTANCE.<MinecraftServer>get(LogicalSide.SERVER).getWorlds()) {
				if (world.getPlayers().isEmpty()) {
					continue;
				}
				
				for (ServerPlayerEntity player : world.getPlayers()) {
					updatePlayer(player);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void clientTick(ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
			if (player != null) {
				DungeonRecord record = getDungeon(player);
				if (record.instance != null) {
					Random rand = player.world.rand;
					final float range = 15;
					for (int i = 0; i < 15; i++) {
						NostrumParticles.GLOW_ORB.spawn(player.world, new SpawnParams(
							1, player.getPosX() + (rand.nextGaussian() * range), player.getPosY() + (rand.nextGaussian() * 4), player.getPosZ() + (rand.nextGaussian() * range), .5,
							80, 30,
							new Vector3d(0, .025, 0), new Vector3d(.01, .0125, .01)
							).color(1f, .4f, .3f, .8f));
					}
				}
			}
		}
	}
}
