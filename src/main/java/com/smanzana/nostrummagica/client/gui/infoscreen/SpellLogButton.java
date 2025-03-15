package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.spell.log.SpellLogEntry;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class SpellLogButton extends InfoButton {

	private SpellLogEntry log;
	
	public SpellLogButton(InfoScreen screen, SpellLogEntry log) {
		super(screen, 0, 0);
		this.log = log;
	}

	@Override
	public IInfoSubScreen getScreen(INostrumMagic attr) {
		return new SpellLogSubScreen(log);
	}

	@Override
	public void render(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		final Minecraft mc = Minecraft.getInstance();
		float tint = 1f;
		if (mouseX >= this.x 
			&& mouseY >= this.y 
			&& mouseX < this.x + width 
			&& mouseY < this.y + height) {
			tint = .75f;
		}
		
		mc.getTextureManager().bind(InfoScreen.background);
		RenderSystem.enableBlend();
		RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, this.x, this.y, 0,
				0, width,
				height, InfoScreen.TEXT_WHOLE_WIDTH, InfoScreen.TEXT_WHOLE_HEIGHT,
				tint, tint, tint, 1f);
		RenderSystem.disableBlend();
		
		final int itemLength = 16;
		
		int x = this.x + (width - itemLength) / 2;
		int y = this.y + (height - itemLength) / 2;
		SpellIcon.get(log.getSpell().getIconIndex()).render(mc, matrixStackIn, x, y, itemLength, itemLength, tint, tint, tint, 1f);
	}

	private List<ITextComponent> desc = new ArrayList<>(1);
	@Override
	public List<ITextComponent> getDescription() {
		if (desc.isEmpty())
			desc.add(new StringTextComponent(log.getSpell().getName()));
		
		return desc;
	}
}
