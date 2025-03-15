package com.smanzana.nostrummagica.item;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.autodungeons.tile.IWorldKeyHolder;
import com.smanzana.autodungeons.world.WorldKey;
import com.smanzana.nostrummagica.tile.LockedChestTileEntity;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
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
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		String keyName = "No key set";
		WorldKey key = getKey(stack);
		if (key != null) {
			keyName = key.toString();
		}
		
		tooltip.add(new StringTextComponent(keyName).withStyle(TextFormatting.GREEN));
	}
	
	public WorldKey getKey(ItemStack stack) {
		if (!stack.hasTag() || !stack.getTag().contains(NBT_KEY_ID)) {
			setKey(stack, new WorldKey());
		}
		
		return WorldKey.fromNBT(stack.getTag().getCompound(NBT_KEY_ID));
	}
	
	public void setKey(ItemStack stack, WorldKey key) {
		CompoundNBT compound = stack.getTag();
		if (compound == null) {
			compound = new CompoundNBT();
		}
		
		compound.put(NBT_KEY_ID, key.asNBT());
		
		stack.setTag(compound);
	}
	
	public void clearKey(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof WorldKeyItem))
			return;
		
		CompoundNBT tag;
		if (!stack.hasTag())
			return;
		
		tag = stack.getTag();
		tag.remove(NBT_KEY_ID);
		
		stack.setTag(tag);
	}
	
	@Override
	public ActionResultType useOn(ItemUseContext context) {
		final World worldIn = context.getLevel();
		final BlockPos pos = context.getClickedPos();
		final PlayerEntity playerIn = context.getPlayer();
		final @Nonnull ItemStack stack = context.getItemInHand();
		
		if (worldIn.isClientSide)
			return ActionResultType.SUCCESS;
		
		if (pos == null)
			return ActionResultType.PASS;
		
		TileEntity te = worldIn.getBlockEntity(pos);
		if (te instanceof IWorldKeyHolder) {
			IWorldKeyHolder holder = (IWorldKeyHolder) te;
			if (playerIn.isShiftKeyDown()) {
				WorldKey key = getKey(stack);
				holder.setWorldKey(key);
				playerIn.sendMessage(new StringTextComponent("Set object's key to " + key.toString().substring(0, 8)), Util.NIL_UUID);
			} else {
				if (holder.hasWorldKey()) {
					WorldKey key = holder.getWorldKey();
					setKey(stack, key);
					playerIn.sendMessage(new StringTextComponent("Remembered key " + key.toString().substring(0, 8)), Util.NIL_UUID);
				} else {
					playerIn.sendMessage(new StringTextComponent("No key to take"), Util.NIL_UUID);
				}
			}
			return ActionResultType.SUCCESS;
		}
		
		if (te instanceof ChestTileEntity) {
			// Convert chests to locked chests
			final WorldKey key = this.getKey(stack);
			if (!LockedChestTileEntity.LockChest(worldIn, pos, key)) {
				playerIn.sendMessage(new StringTextComponent("Failed to lock chest"), Util.NIL_UUID);
			} else {
				playerIn.sendMessage(new StringTextComponent("Locked chest with key " + key.toString().substring(0, 8)), Util.NIL_UUID); 
			}
		}
		
		return ActionResultType.PASS;
	}
	
	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand hand) {
		final @Nonnull ItemStack itemStackIn = playerIn.getItemInHand(hand);
		
		if (!worldIn.isClientSide() && playerIn.isShiftKeyDown()) {
			clearKey(itemStackIn);
			final WorldKey key = this.getKey(itemStackIn);
			playerIn.sendMessage(new StringTextComponent("Generated new key " + key.toString().substring(0, 8)), Util.NIL_UUID);
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
		}
		
		return new ActionResult<ItemStack>(ActionResultType.PASS, itemStackIn);
	}
}
