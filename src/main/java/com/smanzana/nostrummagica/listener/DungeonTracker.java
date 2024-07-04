package com.smanzana.nostrummagica.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.DungeonTrackerUpdateMessage;
import com.smanzana.nostrummagica.world.dungeon.DungeonRecord;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructure;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.event.EntityViewRenderEvent;
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
	
	private final Map<PlayerEntity, DungeonRecord> dungeonMap;
	
	public DungeonTracker() {
		this.dungeonMap = new HashMap<>();
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public DungeonRecord getDungeon(PlayerEntity player) {
		return dungeonMap.get(player);
	}
	
	protected void setDungeon(PlayerEntity player, @Nullable DungeonRecord record) {
		DungeonRecord prev = this.dungeonMap.put(player, record);
		if ((prev == null || !Objects.equals(prev, record)) && !player.getEntityWorld().isRemote()) {
			notifyPlayer((ServerPlayerEntity) player);
		}
	}
	
	protected void notifyPlayer(ServerPlayerEntity player) {
		@Nullable DungeonRecord record = this.getDungeon(player);
		NetworkHandler.sendTo(new DungeonTrackerUpdateMessage(player.getUniqueID(), record), player);
	}
	
	public void overrideClientDungeon(PlayerEntity player, @Nullable DungeonRecord record) {
		this.setDungeon(player, record);
	}
	
	protected void updatePlayer(ServerPlayerEntity player) {
		final @Nullable DungeonRecord current;
		if (!player.isAlive()) {
			current = null;
		} else {
			current = NostrumDungeonStructure.GetDungeonAt(player.getServerWorld(), player.getPosition());
		}
		setDungeon(player, current);
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
	
	public static class Client extends DungeonTracker {
	
		public Client() {
			super();
		}
		
		@SubscribeEvent
		public void clientTick(ClientTickEvent event) {
			if (event.phase == TickEvent.Phase.END) {
				PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
				if (player != null) {
					DungeonRecord record = getDungeon(player);
					if (record != null) {
						record.structure.getDungeon().clientTick(player.getEntityWorld(), player);
					}
				}
			}
		}
		
		@SubscribeEvent
		public void onFogDensityCheck(EntityViewRenderEvent.FogDensity event) {
			PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
			if (player != null) {
				DungeonRecord record = getDungeon(player);
				if (record != null) {
					record.structure.getDungeon().setClientFogDensity(player.world, player, event);
				}
			}
		}
		
		@SubscribeEvent
		public void onFogColorCheck(EntityViewRenderEvent.FogColors event) {
			PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
			if (player != null) {
				DungeonRecord record = getDungeon(player);
				if (record != null) {
					record.structure.getDungeon().setClientFogColor(player.world, player, event);
				}
			}
		}
	}
}
