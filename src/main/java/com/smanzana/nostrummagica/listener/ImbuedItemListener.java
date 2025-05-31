package com.smanzana.nostrummagica.listener;

import com.smanzana.nostrummagica.spell.ItemImbuement;

import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingGetProjectileEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ImbuedItemListener {
	
	private boolean recursionGuard = false;

	public ImbuedItemListener() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onItemEaten(LivingEntityUseItemEvent.Finish event) {
		if (event.isCanceled() || event.getEntityLiving().getLevel().isClientSide()) {
			return;
		}
		
		if (recursionGuard) {
			return;
		}
		recursionGuard = true;
		
		final ItemStack usedStack = event.getItem();
		if (usedStack.isEdible()) {
			// assume the USE type was eat...
			// See if it had an imbuement
			ItemImbuement imbuement = ItemImbuement.FromItemStack(usedStack);
			if (imbuement != null) {
				imbuement.triggerOn(event.getEntityLiving(), event.getEntityLiving());
				
				// If stack is the same with same count and everything (not a bunch of stacked ones),
				// remove imbuement
				if (!event.getResultStack().isEmpty() && event.getResultStack().getItem() == usedStack.getItem()
						&& usedStack.equals(event.getResultStack(), false) && usedStack.getCount() == event.getResultStack().getCount()) {
					// doesn't look like result changed, so remove imbuement... unles it's a creative player eating which has the same effect it seems
					if (event.getEntityLiving() instanceof Player player && player.isCreative()) {
						
					} else {
						ItemImbuement.ClearStack(event.getResultStack());
					}
				}
			}
		}
		
		recursionGuard = false;
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onWeaponUsed(LivingAttackEvent event) {
		if (event.isCanceled()) {
			return;
		}
		
		if (event.getAmount() < 1f) {
			return;
		}
		
		final LivingEntity hurtEnt = event.getEntityLiving();
		if (hurtEnt.getLevel().isClientSide()) {
			return;
		}
		
		if (event.getSource().isProjectile() || event.getSource().isExplosion()) {
			return;
		}
		
		if (!(event.getSource() instanceof EntityDamageSource entitySource)
				|| entitySource.getDirectEntity() != entitySource.getEntity()
				|| !(entitySource.getEntity() instanceof LivingEntity living)) {
			return;
		}
		
		if (recursionGuard) {
			return;
		}
		recursionGuard = true;
		
		final ItemStack usedStack = living.getMainHandItem();
		if (!usedStack.isEmpty()) {
			ItemImbuement imbuement = ItemImbuement.FromItemStack(usedStack);
			if (imbuement != null) {
				imbuement.triggerOn(living, hurtEnt);
				ItemImbuement.ClearStack(usedStack);
			}
		}
		
		recursionGuard = false;
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onProjectileShot(LivingGetProjectileEvent event) {
		// probably need to add a capability or something to the projectile?
	}
	
}
