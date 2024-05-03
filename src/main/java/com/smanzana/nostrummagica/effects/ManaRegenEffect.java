package com.smanzana.nostrummagica.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.attributes.NostrumAttributes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ManaRegenEffect extends Effect {

	public static final String ID = "potions-mana-regen";
	
	public ManaRegenEffect() {
		super(EffectType.BENEFICIAL, 0xFFBB6DFF);
		this.addAttributesModifier(NostrumAttributes.manaRegen,
				"74149d64-b22a-4dd9-ab68-030fc195ecfc", 50D, AttributeModifier.Operation.ADDITION);
	}
	
	public boolean isReady(int duration, int amp) {
		return false; // No special effects
	}

	@Override
	public void performEffect(LivingEntity entity, int amp) {
		;
    }
	
	public String getEffectName() {
		return "mana-regen";
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack matrixStackIn, int x, int y, float z) {
		PotionIcon.MANAREGEN.draw(matrixStackIn, gui.getMinecraft(), x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, MatrixStack matrixStackIn, int x, int y, float z, float alpha) {
		PotionIcon.MANAREGEN.draw(matrixStackIn, Minecraft.getInstance(), x + 3, y + 3);
	}
	
}
