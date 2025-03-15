package com.smanzana.nostrummagica.item.equipment;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * An item base class that extends sword and has built in support for right-click charging the item.
 * @author Skyler
 *
 */
public abstract class ChargingSwordItem extends SwordItem {
	
	public static final ResourceLocation PROPERTY_CHARGING = NostrumMagica.Loc("charging");
	public static final ResourceLocation PROPERTY_CHARGE = NostrumMagica.Loc("charge");
	
	public ChargingSwordItem(IItemTier tier, int attackDamageIn, float attackSpeedIn, Item.Properties builder) {
		super(tier, attackDamageIn, attackSpeedIn, builder);
	}
	
	protected boolean canCharge(World worldIn, PlayerEntity playerIn, Hand hand, ItemStack stack) {
		return true;
	}
	
	/**
	 * Whether this charging item should 'fire' when the charge time is up.
	 * If false, players can keep holding the charge button as long as they want past
	 * the charge time and release when they're ready.
	 * @param stack
	 * @return
	 */
	protected abstract boolean shouldAutoFire(ItemStack stack);
	
	/**
	 * The total number of ticks a player has to charge the item before it's considered
	 * "charged" and can be released to fire.
	 * @param stack
	 * @return
	 */
	protected abstract int getTotalChargeTime(ItemStack stack);
	
	protected abstract void fireChargedWeapon(World worldIn, LivingEntity playerIn, Hand hand, ItemStack stack);
	
	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand hand) {
		final ItemStack held = playerIn.getItemInHand(hand);
		
		if (canCharge(worldIn, playerIn, hand, held)) {
			// Don't do when sneaking so players can still use a shield
			if (!playerIn.isShiftKeyDown()) {
				playerIn.startUsingItem(hand);
				return new ActionResult<ItemStack>(ActionResultType.SUCCESS, held);
			}
		}
		
		return new ActionResult<ItemStack>(ActionResultType.PASS, held);
	}
	
	@Override
	public UseAction getUseAnimation(ItemStack stack) {
		return UseAction.BOW;
	}
	
	@Override
	public int getUseDuration(ItemStack stack) {
		// How long to let the player keep 'using' it.
		if (this.shouldAutoFire(stack)) {
			return this.getTotalChargeTime(stack);
		} else {
			// We set it high so they can hold it even longer than the charge duration
			// so that it doesn't auto-fire.
			// This value is copied from the bow.
			return 270000;
		}
	}
	
	@Override
	public void releaseUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
		
		// Only do something if enough time has passed
		final int duration = stack.getUseDuration() - timeLeft;
		if (duration < this.getTotalChargeTime(stack)) {
			return;
		}
		
		final Hand hand = entityLiving.getMainHandItem() == stack ? Hand.MAIN_HAND : Hand.OFF_HAND;
		fireChargedWeapon(worldIn, entityLiving, hand, stack);
	}
	
	@Override
	public ItemStack finishUsingItem(ItemStack stack, World worldIn, LivingEntity entityLiving) {
		final Hand hand = entityLiving.getMainHandItem() == stack ? Hand.MAIN_HAND : Hand.OFF_HAND;
		fireChargedWeapon(worldIn, entityLiving, hand, stack);
		return stack;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static final float ModelCharge(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
		if (entityIn == null) {
			return 0.0F;
		} else {
			return !(entityIn.getUseItem().getItem() instanceof ChargingSwordItem) ? 0.0F : (float)(stack.getUseDuration() - entityIn.getUseItemRemainingTicks()) / ((ChargingSwordItem) stack.getItem()).getTotalChargeTime(stack);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static final float ModelCharging(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
		return entityIn != null && entityIn.isUsingItem() && entityIn.getUseItem() == stack ? 1.0F : 0.0F;
	}
	
}
