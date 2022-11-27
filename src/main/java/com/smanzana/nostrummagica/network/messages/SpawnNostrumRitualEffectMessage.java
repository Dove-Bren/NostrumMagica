package com.smanzana.nostrummagica.network.messages;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.effects.ClientPredefinedEffect;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.EMagicElement;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Server is signalling that a ritual has been performed, and the effects should be shown.
 * Perhaps this should be more generic
 * @author Skyler
 *
 */
public class SpawnNostrumRitualEffectMessage implements IMessage {

	public static class Handler implements IMessageHandler<SpawnNostrumRitualEffectMessage, IMessage> {

		@Override
		public IMessage onMessage(SpawnNostrumRitualEffectMessage message, MessageContext ctx) {
			
			final int dimID = message.tag.getInteger(NBT_DIMENSION_ID);
			final BlockPos pos = BlockPos.fromLong(message.tag.getLong(NBT_POS));
			final EMagicElement element = EMagicElement.valueOf(message.tag.getString(NBT_ELEMENT).toUpperCase());
			
			EntityPlayer player = NostrumMagica.proxy.getPlayer();
			if (player.dimension != dimID) {
				return null;
			}
			
			final int[] typesRaw = message.tag.getIntArray(NBT_REAGENTS);
			final ReagentType[] types = new ReagentType[typesRaw == null ? 0 : typesRaw.length];
			for (int i = 0; i < types.length; i++) {
				types[i] = ReagentType.values()[typesRaw[i]];
			}
			
			ItemStack center = ItemStack.EMPTY;
			if (message.tag.hasKey(NBT_CENTER_ITEM)) {
				center = new ItemStack(message.tag.getCompoundTag(NBT_CENTER_ITEM));
			}
			
			ItemStack output = ItemStack.EMPTY;
			if (message.tag.hasKey(NBT_OUTPUT)) {
				output = new ItemStack(message.tag.getCompoundTag(NBT_OUTPUT));
			}
			
			@Nullable NonNullList<ItemStack> extras = null;
			if (message.tag.hasKey(NBT_EXTRA_ITEMS)) {
				NBTTagList list = message.tag.getTagList(NBT_EXTRA_ITEMS, NBT.TAG_COMPOUND);
				extras = NonNullList.create();
				for (int i = 0; i < list.tagCount(); i++) {
					 extras.add(new ItemStack(list.getCompoundTagAt(i)));
				}
			}
			
			final ItemStack centerF = center;
			final ItemStack outputF = output;
			final NonNullList<ItemStack> extrasF = extras;
			
//			Minecraft.getMinecraft().addScheduledTask(() -> {
//				ClientEffectRenderer.instance().addEffect(ClientEffectRitual.Create(
//						new Vec3d(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5),
//						element, centerF, extrasF, types, outputF
//						));
//			});
			ClientPredefinedEffect.SpawnRitualEffect(pos, element, centerF, extrasF, types, outputF);

			return null;
		}
		
	}

	private static final String NBT_DIMENSION_ID = "dim";
	private static final String NBT_POS = "blockPos";
	private static final String NBT_CENTER_ITEM = "centerItem";
	private static final String NBT_EXTRA_ITEMS = "extraItems";
	private static final String NBT_REAGENTS = "reagents";
	private static final String NBT_OUTPUT = "output";
	private static final String NBT_ELEMENT = "element";
	
	protected NBTTagCompound tag;
	
	public SpawnNostrumRitualEffectMessage() {
		tag = new NBTTagCompound();
	}
	
	public SpawnNostrumRitualEffectMessage(int dimension, BlockPos pos, EMagicElement element, ReagentType[] reagents,
			ItemStack center, @Nullable NonNullList<ItemStack> extras, ItemStack output) {
		tag = new NBTTagCompound();
		
		tag.setInteger(NBT_DIMENSION_ID, dimension);
		tag.setLong(NBT_POS, pos.toLong());
		tag.setString(NBT_ELEMENT, element.name());
		
		int[] intArr = new int[reagents.length];
		for (int i = 0; i < reagents.length; i++) {
			intArr[i] = reagents[i].ordinal();
		}
		tag.setIntArray(NBT_REAGENTS, intArr);
		if (!center.isEmpty()) {
			tag.setTag(NBT_CENTER_ITEM, center.serializeNBT());
		}
		if (extras != null) {
			NBTTagList list = new NBTTagList();
			
			for (ItemStack stack : extras) {
				list.appendTag(stack.serializeNBT());
			}
			
			tag.setTag(NBT_EXTRA_ITEMS, list);
		}
		if (!output.isEmpty()) {
			tag.setTag(NBT_OUTPUT, output.serializeNBT());
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		tag = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, tag);
	}

}
