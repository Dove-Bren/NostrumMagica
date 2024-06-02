package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.tile.ModificationTableEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client has clicked to submit a modification on the modification table
 * @author Skyler
 *
 */
public class ModifyMessage {

	public static void handle(ModifyMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Find the tile entity.
		// Call 'modify' method on it
		// boom
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(() -> {
			PlayerEntity sp = ctx.get().getSender();
			World world = sp.world;
			
			// Get the TE
			TileEntity TE = world.getTileEntity(message.pos);
			if (TE == null) {
				NostrumMagica.logger.warn("Got modify message that didn't line up with a modification table. This is a bug!");
				return;
			}
			
			ModificationTableEntity entity = (ModificationTableEntity) TE;
			
			entity.modify(message.bool, message.flt);
		});
	}

	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	private final BlockPos pos;
	private final boolean bool;
	private final float flt;
	
	public ModifyMessage(BlockPos pos, boolean bool, float flt) {
		this.pos = pos;
		this.bool = bool;
		this.flt = flt;
	}

	public static ModifyMessage decode(PacketBuffer buf) {
		return new ModifyMessage(buf.readBlockPos(), buf.readBoolean(), buf.readFloat());
	}

	public static void encode(ModifyMessage msg, PacketBuffer buf) {
		buf.writeBlockPos(msg.pos);
		buf.writeBoolean(msg.bool);
		buf.writeFloat(msg.flt);
	}

}
