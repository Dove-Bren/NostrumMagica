package com.smanzana.nostrummagica.potions;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Fake potion effect that shows when you have a familiar and when you don't
 * @author Skyler
 *
 */
public class FamiliarPotion extends Potion {

	private static final ResourceLocation Resource = new ResourceLocation(
			NostrumMagica.MODID, "potions-familiar-shadow");
	
	private static FamiliarPotion instance;
	public static FamiliarPotion instance() {
		if (instance == null)
			instance = new FamiliarPotion();
		
		return instance;
	}
	
	private FamiliarPotion() {
		super(false, 0xFF310033);

		this.setBeneficial();
		this.setPotionName("potion.familiar.name");
		this.setRegistryName(Resource);
	}
	
	@Override
	public boolean isReady(int duration, int amp) {
		return duration > 0 && duration % 5 == 0; // Check every 1/4 a second :shrug:
		// Note: actual apply check is done in anon class when applying, and validates familiar status
	}
	
	@Override
	public void performEffect(LivingEntity entityLivingBaseIn, int p_76394_2_) {
		;
	}
	
	@Override
	public void removeAttributesModifiersFromEntity(LivingEntity entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
		// We were removed for whatever reason. Kill the familiars
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entityLivingBaseIn);
		if (attr != null)
			attr.clearFamiliars();
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		PotionIcon.FAMILIAR.draw(mc, x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha) {
		PotionIcon.FAMILIAR.draw(mc, x + 3, y + 3);
	}
}
