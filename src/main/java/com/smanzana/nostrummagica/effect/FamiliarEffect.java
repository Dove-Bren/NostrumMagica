package com.smanzana.nostrummagica.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Fake potion effect that shows when you have a familiar and when you don't
 * @author Skyler
 *
 */
public class FamiliarEffect extends MobEffect {
	
	public static final String ID = "familiar-shadow";

	public FamiliarEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFF310033);

		//this.setPotionName("potion.familiar.name");
		//this.setRegistryName(Resource);
	}
	
	@Override
	public boolean isDurationEffectTick(int duration, int amp) {
		return duration > 0 && duration % 5 == 0; // Check every 1/4 a second :shrug:
		// Note: actual apply check is done in anon class when applying, and validates familiar status
	}
	
	@Override
	public void applyEffectTick(LivingEntity entityLivingBaseIn, int p_76394_2_) {
		;
	}
	
	@Override
	public void removeAttributeModifiers(LivingEntity entityLivingBaseIn, AttributeMap attributeMapIn, int amplifier) {
		// We were removed for whatever reason. Kill the familiars
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entityLivingBaseIn);
		if (attr != null)
			attr.clearFamiliars();
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, PoseStack matrixStackIn, int x, int y, float z) {
		PotionIcon.FAMILIAR.draw(matrixStackIn, gui.getMinecraft(), x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(MobEffectInstance effect, GuiComponent gui, PoseStack matrixStackIn, int x, int y, float z, float alpha) {
		PotionIcon.FAMILIAR.draw(matrixStackIn, Minecraft.getInstance(), x + 3, y + 3);
	}
}
