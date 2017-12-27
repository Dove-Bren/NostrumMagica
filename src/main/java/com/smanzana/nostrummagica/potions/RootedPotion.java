package com.smanzana.nostrummagica.potions;

import java.awt.Color;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

public class RootedPotion extends Potion {

	private static final ResourceLocation Resource = new ResourceLocation(
			NostrumMagica.MODID, "potions-rooted");
	
	private static RootedPotion instance;
	public static RootedPotion instance() {
		if (instance == null)
			instance = new RootedPotion();
		
		return instance;
	}
	
	private RootedPotion() {
		super(false, (new Color(100, 60, 25)).getRGB());
		
		NostrumMagica.registerPotion(this, Resource);
	}
	
	public boolean isReady(int duration, int amp) {
		return duration > 0; // Every tick
	}

	@Override
	public void performEffect(EntityLivingBase entity, int amp)
    {
        if (entity.isRiding()) {
        	entity.dismountRidingEntity();
        }
        
        if (entity.motionY > 0) {
        	entity.motionY = 0;
        }
        entity.motionX = 0.0;
        entity.motionZ = 0.0;
    }
	
}
