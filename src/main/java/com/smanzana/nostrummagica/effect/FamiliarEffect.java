package com.smanzana.nostrummagica.effect;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

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

/**
 * Fake potion effect that shows when you have a familiar and when you don't
 * @author Skyler
 *
 */
public class FamiliarEffect extends Effect {
	
	public static final String ID = "potions-familiar-shadow";

	public FamiliarEffect() {
		super(EffectType.BENEFICIAL, 0xFF310033);

		//this.setPotionName("potion.familiar.name");
		//this.setRegistryName(Resource);
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
	public void removeAttributesModifiersFromEntity(LivingEntity entityLivingBaseIn, AttributeModifierManager attributeMapIn, int amplifier) {
		// We were removed for whatever reason. Kill the familiars
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entityLivingBaseIn);
		if (attr != null)
			attr.clearFamiliars();
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack matrixStackIn, int x, int y, float z) {
		PotionIcon.FAMILIAR.draw(matrixStackIn, gui.getMinecraft(), x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, MatrixStack matrixStackIn, int x, int y, float z, float alpha) {
		PotionIcon.FAMILIAR.draw(matrixStackIn, Minecraft.getInstance(), x + 3, y + 3);
	}
}
