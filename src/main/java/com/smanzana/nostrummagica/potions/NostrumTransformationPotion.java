package com.smanzana.nostrummagica.potions;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Way to mark entities for later transformation.
 * Not intended to be put on a player.
 * @author Skyler
 *
 */
public class NostrumTransformationPotion extends Potion {

	private static final ResourceLocation Resource = new ResourceLocation(
			NostrumMagica.MODID, "potions-transformation");
	
	private static NostrumTransformationPotion instance;
	public static NostrumTransformationPotion instance() {
		if (instance == null)
			instance = new NostrumTransformationPotion();
		
		return instance;
	}
	
	private NostrumTransformationPotion() {
		super(false, 0xFF000000);
		this.setBeneficial();
		
		this.setPotionName("potion.transformation.name");

		this.setRegistryName(Resource);
	}
	
	public boolean isReady(int duration, int amp) {
		return duration % 20 == 0;
	}

	@Override
	public void performEffect(LivingEntity entity, int amp) {
		if (entity.getHealth() > 1f) {
			entity.attackEntityFrom(DamageSource.MAGIC, 1f);
		}
    }
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		; //PotionIcon.NATURESBLESSING.draw(mc, x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha) {
		; //PotionIcon.NATURESBLESSING.draw(mc, x + 3, y + 3);
	}
	
	@Override
	public boolean shouldRender(PotionEffect effect) {
		return false;
	}
	
}
