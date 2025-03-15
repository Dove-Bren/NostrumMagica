package com.smanzana.nostrummagica.effect;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MagicBoostEffect extends Effect {

	public static final String ID = "magboost";
	private static final String POTENCY_UUID = "718e46ce-f549-4f18-8dcb-d690590e9ba5";
	
	public MagicBoostEffect() {
		super(EffectType.BENEFICIAL, 0xFF47FFAF);
		
		this.addAttributeModifier(NostrumAttributes.magicDamage, POTENCY_UUID, 50.D, AttributeModifier.Operation.ADDITION);
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return false; // No tick effects
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack matrixStackIn, int x, int y, float z) {
		PotionIcon.MAGICBOOST.draw(matrixStackIn, gui.getMinecraft(), x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, MatrixStack matrixStackIn, int x, int y, float z, float alpha) {
		PotionIcon.MAGICBOOST.draw(matrixStackIn, Minecraft.getInstance(), x + 3, y + 3);
	}
}
