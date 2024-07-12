package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.RuneShaperBlock;
import com.smanzana.nostrummagica.client.gui.container.RuneShaperGui;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client has clicked to submit a rune shaper operation
 * @author Skyler
 *
 */
public class RuneShaperMessage {

	public static void handle(RuneShaperMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Verify the block, and then try to forward to the shaper container
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(() -> {
			PlayerEntity sp = ctx.get().getSender();
			World world = sp.world;
			
			// Get the TE
			BlockState state = world.getBlockState(message.pos);
			if (!(state.getBlock() instanceof RuneShaperBlock)) {
				NostrumMagica.logger.warn("Got rune shaper message that didn't line up with a rune shaper table. This is a bug!");
				return;
			}
			
			if (sp.openContainer == null || !(sp.openContainer instanceof RuneShaperGui.RuneShaperContainer)) {
				NostrumMagica.logger.warn("Got rune shaper message from a player who doesn't have a rune shaper container open");
				return;
			}
			
			((RuneShaperGui.RuneShaperContainer) sp.openContainer).handleSubmitAttempt(message.shape, message.property, message.propertyValueIdx);
		});
	}

	private final BlockPos pos;
	private final SpellShape shape;
	private final SpellShapeProperty<?> property;
	private final int propertyValueIdx;
	
	public RuneShaperMessage(BlockPos pos, SpellShape shape, SpellShapeProperty<?> property, int propertyValueIdx) {
		this.pos = pos;
		this.shape = shape;
		this.property = property;
		this.propertyValueIdx = propertyValueIdx;
	}

	public static RuneShaperMessage decode(PacketBuffer buf) {
		final BlockPos pos = buf.readBlockPos();
		final String shapeKey = buf.readString();
		final String propName = buf.readString();
		final int idx = buf.readVarInt();
		
		final SpellShape shape = SpellShape.get(shapeKey);
		if (shape == null) {
			throw new RuntimeException("Unknown shape " + shapeKey);
		}
		
		final SpellShapeProperty<?> prop = shape.getProperty(propName);
		if (prop == null) {
			throw new RuntimeException("Unknown shape property " + propName + " for shape " + shapeKey);
		}
		
		return new RuneShaperMessage(pos, shape, prop, idx);
	}

	public static void encode(RuneShaperMessage msg, PacketBuffer buf) {
		buf.writeBlockPos(msg.pos);
		buf.writeString(msg.shape.getShapeKey());
		buf.writeString(msg.property.getName());
		buf.writeVarInt(msg.propertyValueIdx);
	}

}
