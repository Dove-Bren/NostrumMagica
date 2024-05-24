package com.smanzana.nostrummagica.items;

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
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		final ItemStack held = playerIn.getHeldItem(hand);
		
		// Don't do when sneaking so players can still use a shield
		if (!playerIn.isSneaking()) {
			playerIn.setActiveHand(hand);
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, held);
		}
		
		return new ActionResult<ItemStack>(ActionResultType.PASS, held);
	}
	
	@Override
	public UseAction getUseAction(ItemStack stack) {
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
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
		
		// Only do something if enough time has passed
		final int duration = stack.getUseDuration() - timeLeft;
		if (worldIn.isRemote || duration < this.getTotalChargeTime(stack)) {
			return;
		}
		
		final Hand hand = entityLiving.getHeldItemMainhand() == stack ? Hand.MAIN_HAND : Hand.OFF_HAND;
		fireChargedWeapon(worldIn, entityLiving, hand, stack);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static final float ModelCharge(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
		if (entityIn == null) {
			return 0.0F;
		} else {
			return !(entityIn.getActiveItemStack().getItem() instanceof ChargingSwordItem) ? 0.0F : (float)(stack.getUseDuration() - entityIn.getItemInUseCount()) / ((ChargingSwordItem) stack.getItem()).getTotalChargeTime(stack);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static final float ModelCharging(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
		return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
	}
	
}
