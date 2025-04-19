package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.RuneShaperBlock;
import com.smanzana.nostrummagica.client.gui.container.RuneShaperGui;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

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
			Player sp = ctx.get().getSender();
			Level world = sp.level;
			
			// Get the TE
			BlockState state = world.getBlockState(message.pos);
			if (!(state.getBlock() instanceof RuneShaperBlock)) {
				NostrumMagica.logger.warn("Got rune shaper message that didn't line up with a rune shaper table. This is a bug!");
				return;
			}
			
			if (sp.containerMenu == null || !(sp.containerMenu instanceof RuneShaperGui.RuneShaperContainer)) {
				NostrumMagica.logger.warn("Got rune shaper message from a player who doesn't have a rune shaper container open");
				return;
			}
			
			((RuneShaperGui.RuneShaperContainer) sp.containerMenu).handleSubmitAttempt(message.shape, message.property, message.propertyValueIdx);
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

	public static RuneShaperMessage decode(FriendlyByteBuf buf) {
		final BlockPos pos = buf.readBlockPos();
		final String shapeKey = buf.readUtf();
		final String propName = buf.readUtf();
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

	public static void encode(RuneShaperMessage msg, FriendlyByteBuf buf) {
		buf.writeBlockPos(msg.pos);
		buf.writeUtf(msg.shape.getShapeKey());
		buf.writeUtf(msg.property.getName());
		buf.writeVarInt(msg.propertyValueIdx);
	}

}
