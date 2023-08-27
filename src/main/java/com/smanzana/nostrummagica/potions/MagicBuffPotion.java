package com.smanzana.nostrummagica.potions;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MagicBuffPotion extends Potion {

	private static final ResourceLocation Resource = new ResourceLocation(
			NostrumMagica.MODID, "potions-magicbuff");
	
	private static MagicBuffPotion instance;
	public static MagicBuffPotion instance() {
		if (instance == null)
			instance = new MagicBuffPotion();
		
		return instance;
	}
	
	private MagicBuffPotion() {
		super(false, 0xFF80805D);

		this.setBeneficial();
		this.setPotionName("potion.magicbuff.name");
		this.setRegistryName(Resource);
	}
	
	@Override
	public boolean isReady(int duration, int amp) {
		return false; // No tick actions
	}
	
	@Override
	public void applyAttributesModifiersToEntity(LivingEntity entity, AbstractAttributeMap attributeMap, int amplifier) {
		super.applyAttributesModifiersToEntity(entity, attributeMap, amplifier);
	}
	
	@Override
	public void removeAttributesModifiersFromEntity(LivingEntity entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
		NostrumMagica.magicEffectProxy.remove(SpecialEffect.MAGIC_BUFF, entityLivingBaseIn);
		super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
    }
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		PotionIcon.ENCHANT.draw(mc, x + 6, y + 7);
		
		EffectData data = NostrumMagica.magicEffectProxy.getData(mc.player, SpecialEffect.MAGIC_BUFF);
		int count = data == null ? 0 : data.getCount();
		if (count > 0) {
			String display = "" + count;
			int width = mc.font.getStringWidth(display);
			mc.font.drawString("" + count, x + 6 + (20 - width), y + 7 + (20 - mc.font.FONT_HEIGHT), 0xFFFFFFFF);
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha) {
		PotionIcon.ENCHANT.draw(mc, x + 3, y + 3);
		
		EffectData data = NostrumMagica.magicEffectProxy.getData(mc.player, SpecialEffect.MAGIC_BUFF);
		int count = data == null ? 0 : data.getCount();
		if (count > 0) {
			String display = "" + count;
			int width = mc.font.getStringWidth(display);
			mc.font.drawString("" + count, x + 6 + (16 - width), y + 7 + (16 - mc.font.FONT_HEIGHT), 0xFFFFFFFF);
		}
		
	}
}
