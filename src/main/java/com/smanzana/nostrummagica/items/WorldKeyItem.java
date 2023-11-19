package com.smanzana.nostrummagica.items;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.tiles.IWorldKeyHolder;
import com.smanzana.nostrummagica.tiles.LockedChestEntity;
import com.smanzana.nostrummagica.world.NostrumKeyRegistry.NostrumWorldKey;

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
		super(NostrumItems.PropUnstackable());
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		String keyName = "No key set";
		NostrumWorldKey key = getKey(stack);
		if (key != null) {
			keyName = key.toString();
		}
		
		tooltip.add(new StringTextComponent(keyName).applyTextStyle(TextFormatting.GREEN));
	}
	
	public NostrumWorldKey getKey(ItemStack stack) {
		if (!stack.hasTag() || !stack.getTag().contains(NBT_KEY_ID)) {
			setKey(stack, new NostrumWorldKey());
		}
		
		return NostrumWorldKey.fromNBT(stack.getTag().getCompound(NBT_KEY_ID));
	}
	
	public void setKey(ItemStack stack, NostrumWorldKey key) {
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
	public ActionResultType onItemUse(ItemUseContext context) {
		final World worldIn = context.getWorld();
		final BlockPos pos = context.getPos();
		final PlayerEntity playerIn = context.getPlayer();
		final @Nonnull ItemStack stack = context.getItem();
		
		if (worldIn.isRemote)
			return ActionResultType.SUCCESS;
		
		if (pos == null)
			return ActionResultType.PASS;
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof IWorldKeyHolder) {
			IWorldKeyHolder holder = (IWorldKeyHolder) te;
			if (playerIn.isSneaking()) {
				NostrumWorldKey key = getKey(stack);
				holder.setWorldKey(key);
			} else {
				if (holder.hasWorldKey()) {
					NostrumWorldKey key = holder.getWorldKey();
					setKey(stack, key);
				} else {
					playerIn.sendMessage(new StringTextComponent("No key to take"));
				}
			}
			return ActionResultType.SUCCESS;
		}
		
		if (te instanceof ChestTileEntity) {
			// Convert chests to locked chests
			if (!LockedChestEntity.LockChest(worldIn, pos)) {
				playerIn.sendMessage(new StringTextComponent("Failed to lock chest"));
			}
		}
		
		return ActionResultType.PASS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		final @Nonnull ItemStack itemStackIn = playerIn.getHeldItem(hand);
		
		if (playerIn.isSneaking()) {
			clearKey(itemStackIn);
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
		}
		
		return new ActionResult<ItemStack>(ActionResultType.PASS, itemStackIn);
	}
}
