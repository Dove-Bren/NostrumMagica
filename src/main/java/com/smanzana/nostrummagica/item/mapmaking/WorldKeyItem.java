package com.smanzana.nostrummagica.item.mapmaking;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.autodungeons.tile.IWorldKeyHolder;
import com.smanzana.autodungeons.world.WorldKey;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.tile.LockedChestTileEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Item for map makers to generate and transfer key info
 * @author Skyler
 *
 */
public class WorldKeyItem extends Item {

	public static final String ID = "world_key";
	private static final String NBT_KEY_ID = "key";

	public WorldKeyItem() {
		super(NostrumItems.PropDungeonUnstackable());
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		String keyName = "No key set";
		WorldKey key = getKey(stack);
		if (key != null) {
			keyName = key.toString();
		}
		
		tooltip.add(new TextComponent(keyName).withStyle(ChatFormatting.GREEN));
	}
	
	public WorldKey getKey(ItemStack stack) {
		if (!stack.hasTag() || !stack.getTag().contains(NBT_KEY_ID)) {
			setKey(stack, new WorldKey());
		}
		
		return WorldKey.fromNBT(stack.getTag().getCompound(NBT_KEY_ID));
	}
	
	public void setKey(ItemStack stack, WorldKey key) {
		CompoundTag compound = stack.getTag();
		if (compound == null) {
			compound = new CompoundTag();
		}
		
		compound.put(NBT_KEY_ID, key.asNBT());
		
		stack.setTag(compound);
	}
	
	public void clearKey(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof WorldKeyItem))
			return;
		
		CompoundTag tag;
		if (!stack.hasTag())
			return;
		
		tag = stack.getTag();
		tag.remove(NBT_KEY_ID);
		
		stack.setTag(tag);
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		final Level worldIn = context.getLevel();
		final BlockPos pos = context.getClickedPos();
		final Player playerIn = context.getPlayer();
		final @Nonnull ItemStack stack = context.getItemInHand();
		
		if (worldIn.isClientSide)
			return InteractionResult.SUCCESS;
		
		if (pos == null)
			return InteractionResult.PASS;
		
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te instanceof IWorldKeyHolder) {
			IWorldKeyHolder holder = (IWorldKeyHolder) te;
			if (playerIn.isShiftKeyDown()) {
				WorldKey key = getKey(stack);
				holder.setWorldKey(key);
				playerIn.sendMessage(new TextComponent("Set object's key to " + key.toString().substring(0, 8)), Util.NIL_UUID);
			} else {
				if (holder.hasWorldKey()) {
					WorldKey key = holder.getWorldKey();
					setKey(stack, key);
					playerIn.sendMessage(new TextComponent("Remembered key " + key.toString().substring(0, 8)), Util.NIL_UUID);
				} else {
					playerIn.sendMessage(new TextComponent("No key to take"), Util.NIL_UUID);
				}
			}
			return InteractionResult.SUCCESS;
		}
		
		if (te instanceof ChestBlockEntity) {
			// Convert chests to locked chests
			final WorldKey key = this.getKey(stack);
			if (!LockedChestTileEntity.LockChest(worldIn, pos, key)) {
				playerIn.sendMessage(new TextComponent("Failed to lock chest"), Util.NIL_UUID);
			} else {
				playerIn.sendMessage(new TextComponent("Locked chest with key " + key.toString().substring(0, 8)), Util.NIL_UUID); 
			}
		}
		
		return InteractionResult.PASS;
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		final @Nonnull ItemStack itemStackIn = playerIn.getItemInHand(hand);
		
		if (!worldIn.isClientSide() && playerIn.isShiftKeyDown()) {
			clearKey(itemStackIn);
			final WorldKey key = this.getKey(itemStackIn);
			playerIn.sendMessage(new TextComponent("Generated new key " + key.toString().substring(0, 8)), Util.NIL_UUID);
			return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, itemStackIn);
		}
		
		return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, itemStackIn);
	}
}
