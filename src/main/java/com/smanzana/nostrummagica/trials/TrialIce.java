package com.smanzana.nostrummagica.trials;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TrialIce extends ShrineTrial {

	public TrialIce() {
		super(EMagicElement.ICE);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onDeath(LivingDeathEvent e) {
		if (e.getEntityLiving() instanceof EntityPlayer) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(e.getEntityLiving());
			if (attr == null || !attr.isUnlocked())
				return;
			
			if (!attr.hasTrial(this.element))
				return;
			
			if (e.getSource() != DamageSource.drown)
				return;
			
			DamageSource source = e.getSource();
			Vec3d pos = source.getDamageLocation();
			Biome biome = e.getEntityLiving().worldObj.getBiome(
					new BlockPos(pos.xCoord, pos.yCoord, pos.zCoord));
			
			if (!BiomeDictionary.isBiomeOfType(biome, Type.COLD))
				return;
			
			this.complete((EntityPlayer) e.getEntityLiving());
		}
	}
	
}
