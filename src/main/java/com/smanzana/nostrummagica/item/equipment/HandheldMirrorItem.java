package com.smanzana.nostrummagica.item.equipment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.item.IPositionHolderItem;
import com.smanzana.nostrummagica.item.ISelectionItem;
import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class HandheldMirrorItem extends Item implements IPositionHolderItem, ISelectionItem {
	
	// info about when we last warned the player that they are about to set the position
	private static final class WarnData {
		public final int ticks;
		public final RegistryKey<World> dimension;
		public final BlockPos pos;
		
		public WarnData(int ticks, RegistryKey<World> dimension, BlockPos pos) {
			this.ticks = ticks;
			this.dimension = dimension;
			this.pos = pos;
		}
	}
	
	private final Map<PlayerEntity, WarnData> playerWarnings;
	
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
	protected boolean checkAndAddWarn(PlayerEntity player, RegistryKey<World> dimension, BlockPos pos) {
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
	public boolean shouldRenderSelection(PlayerEntity player, ItemStack stack) {
		return player.isShiftKeyDown() && DimensionUtils.InDimension(player, IPositionHolderItem.getDimension(stack));
	}

	@Override
	public @Nullable BlockPos getAnchor(PlayerEntity player, ItemStack stack) {
		return IPositionHolderItem.getBlockPosition(stack);
	}

	@Override	
	public @Nullable BlockPos getBoundingPos(PlayerEntity player, ItemStack stack) {
		return IPositionHolderItem.getBlockPosition(stack);
	}

	@Override
	public boolean isSelectionValid(PlayerEntity player, ItemStack selectionStack) {
		return IPositionHolderItem.getBlockPosition(selectionStack) != null;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		RegistryKey<World> dim = IPositionHolderItem.getDimension(stack);
		BlockPos pos = IPositionHolderItem.getBlockPosition(stack);
		
		if (pos == null)
			return;
		
		String dimName = PositionCrystal.getDimensionName(dim);
		if (dimName == null)
			dimName = "An Unknown Dimension";
		
		tooltip.add(new StringTextComponent("<" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ">").withStyle(TextFormatting.GREEN));
		tooltip.add(new StringTextComponent(dimName).withStyle(TextFormatting.DARK_GREEN));
	}
	
	@Override
	public ActionResultType useOn(ItemUseContext context) {
		final World worldIn = context.getLevel();
		final BlockPos pos = context.getClickedPos();
		final PlayerEntity playerIn = context.getPlayer();
		final @Nonnull ItemStack stack = context.getItemInHand();
		
		if (pos == null)
			return ActionResultType.PASS;
		
		if (!playerIn.isShiftKeyDown()) {
			//onItemRightClick(worldIn, playerIn, context.getHand());
			return ActionResultType.PASS;
		}
		
		if (worldIn.isClientSide)
			return ActionResultType.SUCCESS;
		
		if (!canStore(worldIn, pos)) {
			return ActionResultType.FAIL;
		}
		
		// Warn player before setting position
		if (!checkAndAddWarn(playerIn, worldIn.dimension(), pos)) {
			// Warn
			playerIn.sendMessage(new TranslationTextComponent("info.heldmirror.set_warning"), Util.NIL_UUID);
		} else {
			IPositionHolderItem.setPosition(stack, DimensionUtils.GetDimension(playerIn), pos);
		}
		
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand hand) {
		final @Nonnull ItemStack stack = playerIn.getItemInHand(hand);
		
		RegistryKey<World> dimension = IPositionHolderItem.getDimension(stack);
		BlockPos pos = IPositionHolderItem.getBlockPosition(stack);
		if (pos != null && dimension != null) {
			handleOpen(playerIn, hand, stack);
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, stack);
		}
		
		return new ActionResult<ItemStack>(ActionResultType.FAIL, stack);
	}
	
	protected boolean canStore(World world, BlockPos pos) {
		return !DimensionUtils.IsSorceryDim(world);
	}
	
	protected abstract void handleOpen(PlayerEntity player, Hand hand, ItemStack stack);

}
