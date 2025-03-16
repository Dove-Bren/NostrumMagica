package com.smanzana.nostrummagica.network.message;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.effects.ClientPredefinedEffect;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.NetUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * Server is signalling that a ritual has been performed, and the effects should be shown.
 * Perhaps this should be more generic
 * @author Skyler
 *
 */
public class SpawnNostrumRitualEffectMessage {

	public static void handle(SpawnNostrumRitualEffectMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		Player player = NostrumMagica.instance.proxy.getPlayer();
		if (!DimensionUtils.InDimension(player, message.dimension)) {
			return;
		}
		
//			Minecraft.getInstance().runAsync(() -> {
//				ClientEffectRenderer.instance().addEffect(ClientEffectRitual.Create(
//						new Vector3d(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5),
//						element, centerF, extrasF, types, outputF
//						));
//			});
		ClientPredefinedEffect.SpawnRitualEffect(message.pos, message.element, message.center, message.extras, message.reagents, message.output);
	}

	private final ResourceKey<Level> dimension;
	private final BlockPos pos;
	private final EMagicElement element;
	private final List<ItemStack> reagents;
	private final ItemStack center;
	private final @Nullable List<ItemStack> extras;
	private final ItemStack output;
	
	public SpawnNostrumRitualEffectMessage(ResourceKey<Level> dimension, BlockPos pos, EMagicElement element, List<ItemStack> reagents,
			ItemStack center, @Nullable List<ItemStack> extras, ItemStack output) {
		this.dimension = dimension;
		this.pos = pos;
		this.element = element;
		this.reagents = reagents;
		this.center = center;
		this.extras = extras;
		this.output = output;
	}

	public static SpawnNostrumRitualEffectMessage decode(FriendlyByteBuf buf) {
		final ResourceKey<Level> dimID;
		final BlockPos pos;
		final EMagicElement element;
		final List<ItemStack> reagents;
		final ItemStack center;
		final @Nullable List<ItemStack> extras;
		final ItemStack output;
		
		dimID = NetUtils.unpackDimension(buf);
		pos = buf.readBlockPos();
		element = buf.readEnum(EMagicElement.class);
		
		final int reagentLen = buf.readVarInt();
		reagents = NonNullList.withSize(reagentLen, ItemStack.EMPTY);
		for (int i = 0; i < reagentLen; i++) {
			reagents.set(i, ItemStack.of(buf.readNbt()));
		}
		
		if (buf.readBoolean()) {
			center = ItemStack.of(buf.readNbt());
		} else {
			center = ItemStack.EMPTY;
		}
		
		final int extrasLen = buf.readVarInt();
		if (extrasLen > 0) {
			extras = NonNullList.withSize(extrasLen, ItemStack.EMPTY);
			for (int i = 0; i < extrasLen; i++) {
				extras.set(i, ItemStack.of(buf.readNbt()));
			}
		} else {
			extras = null;
		}
		
		if (buf.readBoolean()) {
			output = ItemStack.of(buf.readNbt());
		} else {
			output = ItemStack.EMPTY;
		}
		
		return new SpawnNostrumRitualEffectMessage(dimID, pos, element, reagents, center, extras, output);
	}

	public static void encode(SpawnNostrumRitualEffectMessage msg, FriendlyByteBuf buf) {
		NetUtils.packDimension(buf, msg.dimension);
		buf.writeBlockPos(msg.pos);
		buf.writeEnum(msg.element);
		
		buf.writeVarInt(msg.reagents.size());
		for (ItemStack reagent : msg.reagents) {
			buf.writeNbt(reagent.serializeNBT());
		}
		
		buf.writeBoolean(!msg.center.isEmpty());
		if (!msg.center.isEmpty()) {
			buf.writeNbt(msg.center.serializeNBT());
		}
		
		buf.writeVarInt(msg.extras == null ? 0 : msg.extras.size());
		if (msg.extras != null) {
			for (ItemStack extra : msg.extras) {
				buf.writeNbt(extra.serializeNBT());
			}
		}
		
		buf.writeBoolean(!msg.output.isEmpty());
		if (!msg.output.isEmpty()) {
			buf.writeNbt(msg.output.serializeNBT());
		}
	}

}
