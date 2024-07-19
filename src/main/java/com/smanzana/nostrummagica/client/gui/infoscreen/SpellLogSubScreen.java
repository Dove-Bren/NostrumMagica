package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.Collection;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.log.SpellLogEntry;
import com.smanzana.nostrummagica.spell.log.SpellLogStageSummary;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;

public class SpellLogSubScreen implements IInfoSubScreen {

	private SpellLogEntry log;
	
	public SpellLogSubScreen(SpellLogEntry log) {
		this.log = log;
	}
	
	@Override
	public void draw(INostrumMagic attr, Minecraft mc, MatrixStack matrixStackIn, int x, int y, int width, int height, int mouseX, int mouseY) {
		
		matrixStackIn.push();
		matrixStackIn.translate(x, y, 0);
		
		String title = this.log.getSpell().getName();
		int len = mc.fontRenderer.getStringWidth(title);
		mc.fontRenderer.drawStringWithShadow(matrixStackIn, title, width / 2 + (-len / 2), 0, 0xFFFFFFFF);
		
		matrixStackIn.translate(0, mc.fontRenderer.FONT_HEIGHT + 2, 0);
		
		final int left = 5;
		for (int stageIdx = 0; stageIdx < log.getStageIndexCount(); stageIdx++) {
			SpellLogStageSummary stageSummary = log.getStages(stageIdx);
			final boolean emptyStage = !stageSummary.hasEffects();
			final int stageHeight;
			if (emptyStage) {
				stageHeight = 32;
			} else {
				stageHeight = 50;
			}
			RenderFuncs.drawRect(matrixStackIn, left, 0, left + width - (10), 0 + stageHeight, 0x40FFFFFF);
			
			matrixStackIn.push();
			matrixStackIn.translate(width/2, 0, 0);
			matrixStackIn.scale(.75f, .75f, 0);
			AbstractGui.drawCenteredString(matrixStackIn, mc.fontRenderer, stageSummary.getLabel(), 0, 2, 0xFFFFFFFF);
			matrixStackIn.pop();
			
			matrixStackIn.push();
			matrixStackIn.translate(left, 0, 0);
			matrixStackIn.scale(.75f, .75f, 1f);
			matrixStackIn.translate(0, mc.fontRenderer.FONT_HEIGHT + 2, 0);
			
			//drawAffectedEntCount(matrixStackIn, stageSummary.getAffectedEntCounts().size(), 3, 0);
			mc.fontRenderer.drawStringWithShadow(matrixStackIn, "Affected " + stageSummary.getAffectedEntCounts().size() + " entities", 3, 0, 0xFFAAAAAA);
			matrixStackIn.translate(0, mc.fontRenderer.FONT_HEIGHT + 2, 0);
			//drawAffectedLocCount(matrixStackIn, stageSummary.getAffectedLocCounts().size(), 3, 0);
			mc.fontRenderer.drawStringWithShadow(matrixStackIn, "Affected " + stageSummary.getAffectedLocCounts().size() + " blocks", 3, 0, 0xFFAAAAAA);
			matrixStackIn.translate(0, mc.fontRenderer.FONT_HEIGHT + 2, 0);
			//drawTriggerCount(matrixStackIn, stageSummary.getStages().size(), 3, 0);
			mc.fontRenderer.drawStringWithShadow(matrixStackIn, "Triggered " + stageSummary.getStages().size() + " time(s)", 3, 0, 0xFFAAAAAA);
			matrixStackIn.translate(0, mc.fontRenderer.FONT_HEIGHT + 2, 0);
			if (!emptyStage) {
				//drawTotalDamage(matrixStackIn, stageSummary.getTotalDamage(), 3, 0);
				mc.fontRenderer.drawStringWithShadow(matrixStackIn, String.format("Total Damage: %.1f", stageSummary.getTotalDamage()), 3, 0, 0xFFAAAAAA);
				matrixStackIn.translate(0, mc.fontRenderer.FONT_HEIGHT + 2, 0);
				//drawTotalHealing(matrixStackIn, stageSummary.getTotalHeal(), 3, 0);
				mc.fontRenderer.drawStringWithShadow(matrixStackIn, String.format("Total Healing: %.1f", stageSummary.getTotalHeal()), 3, 0, 0xFFAAAAAA);
				matrixStackIn.translate(0, mc.fontRenderer.FONT_HEIGHT + 2, 0);
			}
			
			matrixStackIn.pop();
			matrixStackIn.translate(0, stageHeight + 5, 0);
			
//			for (SpellLogStage stage : log.getStages()) {
//				
//				final int top = y + 15 + (i * 55);
//				
//				
//				AbstractGui.drawCenteredString(matrixStackIn, mc.fontRenderer, stage.getLabel(), x + (width/2), top + 2, 0xFFFFFFFF);
//				
//				final String timeStr = String.format("@%.2f secs", ((float) stage.getElapsedTicks() / 20.0f));
//				mc.fontRenderer.drawString(matrixStackIn, timeStr, x + width - (5 + 2 + mc.fontRenderer.getStringWidth(timeStr)), top + 2, 0xFF808080);
//				
//				boolean foundEffect = false;
//				int effectlessCount = 0;
//				float totalDamage = 0f;
//				float totalHeal = 0f;
//				for (Entry<LivingEntity, SpellLogEffectSummary> entry : stage.getAffectedEnts().entrySet()) {
//					if (entry.getValue() == null) {
//						effectlessCount++;
//					} else {
//						foundEffect = true;
//						totalDamage += entry.getValue().getTotalDamage();
//						totalHeal += entry.getValue().getTotalHeal();
//					}
//				}
//				
//				if (!foundEffect) {
//					mc.fontRenderer.drawString(matrixStackIn, effectlessCount + " Entities", left + 5, top + 2 + 8, 0xFF808080);
//				} else {
//					mc.fontRenderer.drawString(matrixStackIn, "Total Damage: " + totalDamage, left + 5, top + 2 + 8, 0xFF808080);
//					mc.fontRenderer.drawString(matrixStackIn, "Total Healing: " + totalHeal, left + 5, top + 2 + 8 + 8, 0xFF808080);
//					
//					int j = 0;
//					for (Entry<LivingEntity, SpellLogEffectSummary> entry : stage.getAffectedEnts().entrySet()) {
//						mc.fontRenderer.drawString(matrixStackIn, entry.getKey().getDisplayName().getString() + ": " + entry.getValue().getTotalDamage(),
//								left + 5, top + 2 + 8 + 8 + 8 + (j * 8), 0xFF808080);
//						j++;
//					}
//				}
//				
//				i++;
//			}
		}
		
		matrixStackIn.pop();
	}

	@Override
	public Collection<ISubScreenButton> getButtons() {
		return null;
	}

}
