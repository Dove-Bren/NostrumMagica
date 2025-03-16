package com.smanzana.nostrummagica.trial;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.MinecraftForge;
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
			Biome biome = e.getEntityLiving().level.getBiome(
					new BlockPos(pos.x, pos.y, pos.z));
			ResourceKey<Biome> biomeKey = ResourceKey.create(Registry.BIOME_REGISTRY, biome.getRegistryName());
			
			if (!BiomeDictionary.hasType(biomeKey, Type.COLD))
				return;
			
			this.complete((Player) e.getEntityLiving());
		}
	}
	
}
