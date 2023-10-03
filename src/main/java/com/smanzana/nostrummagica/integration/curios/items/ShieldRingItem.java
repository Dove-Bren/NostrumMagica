package com.smanzana.nostrummagica.integration.curios.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class ShieldRingItem extends NostrumCurio {

	public static final String ID_SMALL = "shield_ring_small";
	public static final String ID_LARGE = "shield_ring_large";
	
	private final double shieldAmt;
	
	public ShieldRingItem(double shieldAmt, String descKey) {
		super(NostrumCurios.PropCurio(), descKey);
		this.shieldAmt = shieldAmt;
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity player) {
		super.onWornTick(stack, player);
		
		if (!player.world.isRemote) {
			int cost = (int) (shieldAmt * 10);
			
			// Check if we have enough aether and if the player is missing a shield
			if (player.ticksExisted % 40 == 0 && NostrumMagica.magicEffectProxy.getData(player, SpecialEffect.SHIELD_PHYSICAL) == null) {
				IInventory inv = null;
				if (player instanceof PlayerEntity) {
					inv = ((PlayerEntity) player).inventory;
				}
				
				if (inv != null) {
					int taken = APIProxy.drawFromInventory(player.world, player, inv, cost, stack);
					if (taken > 0) {
						// Apply shields! Amount depends on how much aether was consumed
						shieldAmt *= ((float) taken / (float) cost);
						NostrumMagica.magicEffectProxy.applyPhysicalShield(player, shieldAmt);
					}
				}
			}
		}
	}
}
