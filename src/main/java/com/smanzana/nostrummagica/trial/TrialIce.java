package com.smanzana.nostrummagica.trial;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
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
		if (e.getEntityLiving() instanceof PlayerEntity) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(e.getEntityLiving());
			if (attr == null || !attr.isUnlocked())
				return;
			
			if (!attr.hasTrial(this.element))
				return;
			
			if (e.getSource() != DamageSource.DROWN)
				return;
			
			Vector3d pos = e.getEntityLiving().getPositionVec();
			Biome biome = e.getEntityLiving().world.getBiome(
					new BlockPos(pos.x, pos.y, pos.z));
			RegistryKey<Biome> biomeKey = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, biome.getRegistryName());
			
			if (!BiomeDictionary.hasType(biomeKey, Type.COLD))
				return;
			
			this.complete((PlayerEntity) e.getEntityLiving());
		}
	}
	
}
