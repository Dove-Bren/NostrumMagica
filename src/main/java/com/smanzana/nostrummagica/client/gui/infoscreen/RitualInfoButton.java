package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class RitualInfoButton extends InfoButton {

	private RitualRecipe ritual;
	
	public RitualInfoButton(InfoScreen screen, RitualRecipe ritual) {
		super(screen, 0, 0);
		this.ritual = ritual;
	}

	@Override
	public IInfoSubScreen getScreen(INostrumMagic attr) {
		return new RitualInfoSubScreen(ritual);
	}

	@Override
	public void render(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		float tint = 1f;
		if (mouseX >= this.x 
			&& mouseY >= this.y 
			&& mouseX < this.x + width 
			&& mouseY < this.y + height) {
			tint = .75f;
		}
		
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bind(InfoScreen.background);
		RenderSystem.enableBlend();
		RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, this.x, this.y, 0,
				0, width,
				height, InfoScreen.TEXT_WHOLE_WIDTH, InfoScreen.TEXT_WHOLE_HEIGHT,
				tint, tint, tint, 1f);
		RenderSystem.disableBlend();
		
		final int itemLength = 16;
		
		@Nonnull ItemStack iconStack = ritual.getIcon();
		if (!iconStack.isEmpty()) {
			ItemRenderer ItemRenderer = mc.getItemRenderer();
			int x = this.x + (width - itemLength) / 2;
			int y = this.y + (height - itemLength) / 2;
			
			ItemRenderer.renderGuiItem(iconStack, x, y);
		}
	}
	
	private List<Component> desc = new ArrayList<>(1);
	@Override
	public List<Component> getDescription() {
		if (desc.isEmpty())
			desc.add(new TranslatableComponent("ritual." + ritual.getTitleKey() + ".name", new Object[0]));
		
		return desc;
	}
}
