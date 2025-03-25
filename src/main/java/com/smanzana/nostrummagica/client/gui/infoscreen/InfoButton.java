package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.IForegroundRenderable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public abstract class InfoButton extends AbstractButton implements IForegroundRenderable {

	protected static final int BUTTON_WIDTH = 18;
	
	protected final InfoScreen screen;
	
	public InfoButton(InfoScreen screen, int x, int y) {
		super(x, y, BUTTON_WIDTH, BUTTON_WIDTH, TextComponent.EMPTY);
		this.screen = screen;
	}

	public abstract IInfoSubScreen getScreen(INostrumMagic attr);
	
	public abstract List<Component> getDescription();
	
	@Override
	public void renderForeground(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		if (mouseX >= this.x && mouseY > this.y
			&& mouseX <= this.x + this.width
			&& mouseY <= this.y + this.height) {
			Minecraft mc = Minecraft.getInstance();
			matrixStackIn.pushPose();
			matrixStackIn.translate(0, 0, 500);
			screen.renderTooltip(matrixStackIn, getDescription(), Optional.empty(),
					mouseX,
					mouseY,
					mc.font);
			matrixStackIn.popPose();
		}
	}
	
	@Override
	public void onPress() {
		screen.selectScreen(this);
	}
	
	@Override
	public void updateNarration(NarrationElementOutput p_169152_) {
		this.defaultButtonNarrationText(p_169152_);
	}
}
