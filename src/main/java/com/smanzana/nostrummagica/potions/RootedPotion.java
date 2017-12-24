package com.smanzana.nostrummagica.potions;

import java.awt.Color;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

public class RootedPotion extends Potion {

	private static final ResourceLocation Resource = new ResourceLocation(
			NostrumMagica.MODID, "potions/rooted");
	
	private static RootedPotion instance;
	public static RootedPotion instance() {
		if (instance == null)
			instance = new RootedPotion();
		
		return instance;
	}
	
	private RootedPotion() {
		super(Resource, false, (new Color(100, 60, 25)).getRGB());
	}
	
	public boolean isReady(int duration, int amp) {
		return true; // Every tick
	}

	@Override
	public void performEffect(EntityLivingBase entity, int amp)
    {
        if (entity.ridingEntity != null) {
        	entity.dismountEntity(entity.ridingEntity);
        }
        
        if (entity.motionY > 0) {
        	entity.motionY = 0;
        }
    }
	
}
