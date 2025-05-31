package com.smanzana.nostrummagica.item.equipment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.item.api.IPositionHolderItem;
import com.smanzana.nostrummagica.item.api.ISelectionItem;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceKey;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class HandheldMirrorItem extends Item implements IPositionHolderItem, ISelectionItem {
	
	// info about when we last warned the player that they are about to set the position
	private static final class WarnData {
		public final int ticks;
		public final ResourceKey<Level> dimension;
		public final BlockPos pos;
		
		public WarnData(int ticks, ResourceKey<Level> dimension, BlockPos pos) {
			this.ticks = ticks;
			this.dimension = dimension;
			this.pos = pos;
		}
	}
	
	private final Map<Player, WarnData> playerWarnings;
	
	public HandheldMirrorItem(Item.Properties props) {
		super(props);
		playerWarnings = new HashMap<>();
	}
	
	/**
	 * Check if a player warning for this selection was already issued. Update warning timings regardless.
	 * @param player
	 * @param dimension
	 * @param pos
	 * @return Whether a valid recent warning was already in place
	 */
	protected boolean checkAndAddWarn(Player player, ResourceKey<Level> dimension, BlockPos pos) {
		final WarnData existing = playerWarnings.get(player);
		final boolean wasValid = existing != null
								&& existing.ticks < player.tickCount
								&& player.tickCount - existing.ticks < 60
								&& DimensionUtils.DimEquals(dimension, existing.dimension)
								&& pos.equals(existing.pos)
				;
		
		playerWarnings.put(player, new WarnData(player.tickCount, dimension, pos));
		return wasValid;
	}
	
	@Override
	public boolean shouldRenderSelection(Player player, ItemStack stack) {
		return player.isShiftKeyDown() && DimensionUtils.InDimension(player, IPositionHolderItem.getDimension(stack));
	}

	@Override
	public @Nullable BlockPos getAnchor(Player player, ItemStack stack) {
		return IPositionHolderItem.getBlockPosition(stack);
	}

	@Override	
	public @Nullable BlockPos getBoundingPos(Player player, ItemStack stack) {
		return IPositionHolderItem.getBlockPosition(stack);
	}

	@Override
	public boolean isSelectionValid(Player player, ItemStack selectionStack) {
		return IPositionHolderItem.getBlockPosition(selectionStack) != null;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		ResourceKey<Level> dim = IPositionHolderItem.getDimension(stack);
		BlockPos pos = IPositionHolderItem.getBlockPosition(stack);
		
		if (pos == null)
			return;
		
		String dimName = PositionCrystal.getDimensionName(dim);
		if (dimName == null)
			dimName = "An Unknown Dimension";
		
		tooltip.add(new TextComponent("<" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ">").withStyle(ChatFormatting.GREEN));
		tooltip.add(new TextComponent(dimName).withStyle(ChatFormatting.DARK_GREEN));
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		final Level worldIn = context.getLevel();
		final BlockPos pos = context.getClickedPos();
		final Player playerIn = context.getPlayer();
		final @Nonnull ItemStack stack = context.getItemInHand();
		
		if (pos == null)
			return InteractionResult.PASS;
		
		if (!playerIn.isShiftKeyDown()) {
			//onItemRightClick(worldIn, playerIn, context.getHand());
			return InteractionResult.PASS;
		}
		
		if (worldIn.isClientSide)
			return InteractionResult.SUCCESS;
		
		if (!canStore(worldIn, pos)) {
			return InteractionResult.FAIL;
		}
		
		// Warn player before setting position
		if (!checkAndAddWarn(playerIn, worldIn.dimension(), pos)) {
			// Warn
			playerIn.sendMessage(new TranslatableComponent("info.heldmirror.set_warning"), Util.NIL_UUID);
		} else {
			IPositionHolderItem.setPosition(stack, DimensionUtils.GetDimension(playerIn), pos);
		}
		
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		final @Nonnull ItemStack stack = playerIn.getItemInHand(hand);
		
		ResourceKey<Level> dimension = IPositionHolderItem.getDimension(stack);
		BlockPos pos = IPositionHolderItem.getBlockPosition(stack);
		if (pos != null && dimension != null) {
			handleOpen(playerIn, hand, stack);
			return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, stack);
		}
		
		return new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, stack);
	}
	
	protected boolean canStore(Level world, BlockPos pos) {
		return !DimensionUtils.IsSorceryDim(world);
	}
	
	protected abstract void handleOpen(Player player, InteractionHand hand, ItemStack stack);

}
