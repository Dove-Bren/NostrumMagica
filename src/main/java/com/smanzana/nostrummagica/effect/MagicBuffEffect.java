package com.smanzana.nostrummagica.effect;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.SpecialEffect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MagicBuffEffect extends Effect {

	public static final String ID = "magicbuff";
	
	public MagicBuffEffect() {
		super(EffectType.BENEFICIAL, 0xFF80805D);
	}
	
	@Override
	public boolean isDurationEffectTick(int duration, int amp) {
		return false; // No tick actions
	}
	
	@Override
	public void addAttributeModifiers(LivingEntity entity, AttributeModifierManager attributeMap, int amplifier) {
		super.addAttributeModifiers(entity, attributeMap, amplifier);
	}
	
	@Override
	public void removeAttributeModifiers(LivingEntity entityLivingBaseIn, AttributeModifierManager attributeMapIn, int amplifier) {
		NostrumMagica.magicEffectProxy.remove(SpecialEffect.MAGIC_BUFF, entityLivingBaseIn);
		super.removeAttributeModifiers(entityLivingBaseIn, attributeMapIn, amplifier);
    }
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack matrixStackIn, int x, int y, float z) {
		final Minecraft mc = gui.getMinecraft();
		PotionIcon.ENCHANT.draw(matrixStackIn, mc, x + 6, y + 7);
		
		EffectData data = NostrumMagica.magicEffectProxy.getData(mc.player, SpecialEffect.MAGIC_BUFF);
		int count = data == null ? 0 : data.getCount();
		if (count > 0) {
			String display = "" + count;
			int width = mc.font.width(display);
			mc.font.draw(matrixStackIn, "" + count, x + 6 + (20 - width), y + 7 + (20 - mc.font.lineHeight), 0xFFFFFFFF);
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, MatrixStack matrixStackIn, int x, int y, float z, float alpha) {
		Minecraft mc = Minecraft.getInstance();
		PotionIcon.ENCHANT.draw(matrixStackIn, mc, x + 3, y + 3);
		
		EffectData data = NostrumMagica.magicEffectProxy.getData(mc.player, SpecialEffect.MAGIC_BUFF);
		int count = data == null ? 0 : data.getCount();
		if (count > 0) {
			String display = "" + count;
			int width = mc.font.width(display);
			mc.font.draw(matrixStackIn, "" + count, x + 6 + (16 - width), y + 7 + (16 - mc.font.lineHeight), 0xFFFFFFFF);
		}
		
	}
}
