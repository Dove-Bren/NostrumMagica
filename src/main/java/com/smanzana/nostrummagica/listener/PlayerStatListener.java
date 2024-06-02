package com.smanzana.nostrummagica.listener;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.entity.IElementalEntity;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.MagicDamageSource;
import com.smanzana.nostrummagica.stat.PlayerStat;
import com.smanzana.nostrummagica.stat.PlayerStatTracker;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerStatListener {

	public PlayerStatListener() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onDamage(LivingHurtEvent event) {
		if (event.isCanceled()) {
			return;
		}
		
		if (event.getSource() instanceof MagicDamageSource) {
			MagicDamageSource source = (MagicDamageSource) event.getSource();
			if (event.getAmount() > 0f) {
				if (source.getTrueSource() instanceof PlayerEntity) {
					PlayerStatTracker.Update((PlayerEntity) source.getTrueSource(), (stats) -> {
						stats.addMagicDamageDealt(event.getAmount(), source.getElement());
					});
				}
			}
		}
		
		if (event.getEntityLiving() instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) event.getEntityLiving();
			@Nullable EMagicElement element;
			if (event.getSource() instanceof MagicDamageSource) {
				element = ((MagicDamageSource) event.getSource()).getElement();
			} else {
				element = null;
			}
			
			PlayerStatTracker.Update(player, (stats) -> stats.addDamageReceived(event.getAmount(), element));
		}
	}
	
	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		if (event.isCanceled())
			return;
		
		if (event.getSource() != null && event.getSource().getTrueSource() != null && event.getSource().getTrueSource() instanceof PlayerEntity) {
			final PlayerEntity player = (PlayerEntity) event.getSource().getTrueSource();
			final LivingEntity killed = event.getEntityLiving();
			PlayerStatTracker.Update(player, (stats) -> stats.incrStat(PlayerStat.EntityKills(killed.getType())));
			
			if (event.getSource() instanceof MagicDamageSource) {
				MagicDamageSource magicSource = (MagicDamageSource) event.getSource();
				PlayerStatTracker.Update(player, (stats) -> stats.incrStat(PlayerStat.KillsWithMagic).incrStat(PlayerStat.KillsWithElement(magicSource.getElement())));
			}
			
			
			if (killed instanceof IElementalEntity) {
				@Nullable EMagicElement element = ((IElementalEntity) killed).getElement();
				if (element != null) {
					PlayerStatTracker.Update(player, (stats) -> stats.incrStat(PlayerStat.ElementalKills(element)));
				}
			}
		}
	}
	
}
