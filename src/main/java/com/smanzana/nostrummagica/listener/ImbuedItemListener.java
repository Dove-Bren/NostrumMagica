package com.smanzana.nostrummagica.listener;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.capabilities.CapabilityHandler;
import com.smanzana.nostrummagica.capabilities.IImbuedProjectile;
import com.smanzana.nostrummagica.entity.ArrowFiredEvent;
import com.smanzana.nostrummagica.spell.ItemImbuement;

import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
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
		
		if (event.getSource().isProjectile()) {
			handleProjectile(event);
			return;
		}
		
		if (event.getSource().isExplosion()) {
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
	
	protected void handleProjectile(LivingAttackEvent event) {
		Entity projectile = event.getSource().getDirectEntity();
		if (!(projectile instanceof AbstractArrow arrow)) {
			return;
		}
		
		if (!(event.getSource().getEntity() instanceof LivingEntity livingSource)) {
			return;
		}
		
		if (recursionGuard) {
			return;
		}
		recursionGuard = true;
		
		IImbuedProjectile cap = arrow.getCapability(CapabilityHandler.CAPABILITY_IMBUED_PROJECTILE).orElse(null);
		ItemImbuement imbue = cap.getImbuement();
		if (imbue != null) {
			imbue.triggerOn(livingSource, event.getEntityLiving());
		}
		recursionGuard = false;
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onProjectileShot(ArrowFiredEvent event) {
		// probably need to add a capability or something to the projectile?
		System.out.println("fired arrow");
		
		ItemImbuement imbue = null;
		ItemStack fromStack = ItemStack.EMPTY;
		if (!event.getAmmo().isEmpty()) {
			imbue = ItemImbuement.FromItemStack(event.getAmmo());
			if (imbue != null) {
				fromStack = event.getAmmo();
			}
		}
		if (imbue == null) {
			imbue = ItemImbuement.FromItemStack(event.getBow());
			if (imbue != null) {
				fromStack = event.getBow();
			}
		}
		
		if (imbue == null) {
			return;
		}
		
		event.getArrow().getCapability(CapabilityHandler.CAPABILITY_IMBUED_PROJECTILE).orElse(null).setImbuement(imbue);
		ItemImbuement.ClearStack(fromStack);
	}
	
	@SubscribeEvent
	public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof AbstractArrow) {
			ArrowImbuementCap inst = new ArrowImbuementCap();
			LazyOptional<IImbuedProjectile> lazy = LazyOptional.of(() -> inst);
			event.addCapability(CapabilityHandler.CAPABILITY_IMBUED_PROJECTILE_LOC, new ICapabilityProvider() {
				@Override
				public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
					return CapabilityHandler.CAPABILITY_IMBUED_PROJECTILE.orEmpty(cap, lazy);
				}
			});
		}
	}
	
	protected static class ArrowImbuementCap implements IImbuedProjectile {

		private @Nullable ItemImbuement imbue;
		
		public ArrowImbuementCap() {
		}

		@Override
		public ItemImbuement getImbuement() {
			return imbue;
		}

		@Override
		public void setImbuement(ItemImbuement imbuement) {
			this.imbue = imbuement;
		}
		
	}
	
}
