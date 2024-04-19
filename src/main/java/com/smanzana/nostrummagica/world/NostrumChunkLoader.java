package com.smanzana.nostrummagica.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;

public class NostrumChunkLoader {
	
	public static void forceChunk(ServerWorld world, TicketType<BlockPos> ticket, BlockPos pos) {
		world.getChunkProvider().registerTickingTicket(ticket, new ChunkPos(pos), 1, pos); // 1 chunk radius?
	}
	
	public static void unforceChunk(ServerWorld world, TicketType<BlockPos> ticket, BlockPos pos) {
		world.getChunkProvider().releaseTickingTicket(ticket, new ChunkPos(pos), 1, pos);
	}

//	private static final String NBT_TICKET_BASE = "nostrum_chunk_ticket";
//	private static final String NBT_TICKET_REGKEY = "nostrum_chunk_ticket_key";
//	private static NostrumChunkLoader instance = null;
//	public static NostrumChunkLoader instance() {
//		if (instance == null)
//			instance = new NostrumChunkLoader();
//		
//		return instance;
//	}
//	
//	private Map<String, Ticket> tickets;
//	
//	private NostrumChunkLoader() {
//		tickets = new HashMap<>();
//		ForgeChunkManager.setForcedChunkLoadingCallback(NostrumMagica.instance, this);
//	}
//	
//	@Override
//	public void ticketsLoaded(List<Ticket> tickets, World world) {
//		for (Ticket ticket : tickets) {
//			CompoundNBT nbt = ticket.getModData();
//			if (nbt.contains(NBT_TICKET_BASE, NBT.TAG_BYTE)
//					&& nbt.getBoolean(NBT_TICKET_BASE)
//					&& nbt.contains(NBT_TICKET_REGKEY, NBT.TAG_STRING))
//				this.tickets.put(nbt.getString(NBT_TICKET_REGKEY), ticket);
//		}
//	}
//	
//	/**
//	 * Looks up a ticket by a key. Returns it, but does not remove it
//	 * from the registry
//	 * @param key
//	 * @return
//	 */
//	public Ticket findTicket(String key) {
//		return tickets.get(key);
//	}
//	
//	/**
//	 * Tries to look up a ticket. If it finds it, removes it before
//	 * returning it
//	 * @param key
//	 * @return
//	 */
//	public Ticket pullTicket(String key) {
//		return tickets.remove(key);
//	}
//	
//	public void addTicket(String key, Ticket ticket) {
//		ticket.getModData().setString(NBT_TICKET_REGKEY, key);
//		ticket.getModData().setBoolean(NBT_TICKET_BASE, true);
//		tickets.put(key, ticket);
//	}
}
