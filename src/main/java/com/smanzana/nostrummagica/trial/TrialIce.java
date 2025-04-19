package com.smanzana.nostrummagica.trial;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TrialIce extends WorldTrial {

	public TrialIce() {
		super(EMagicElement.ICE);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onDeath(LivingDeathEvent e) {
		if (e.getEntityLiving() instanceof Player) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(e.getEntityLiving());
			if (attr == null || !attr.isUnlocked())
				return;
			
			if (!attr.hasTrial(this.element))
				return;
			
			if (e.getSource() != DamageSource.DROWN)
				return;
			
			Vec3 pos = e.getEntityLiving().position();
			final boolean cold = e.getEntityLiving().level.getBiome(
					new BlockPos(pos.x, pos.y, pos.z)).containsTag(Tags.Biomes.IS_COLD);
			
			if (!cold)
				return;
			
			this.complete((Player) e.getEntityLiving());
		}
	}
	
}
