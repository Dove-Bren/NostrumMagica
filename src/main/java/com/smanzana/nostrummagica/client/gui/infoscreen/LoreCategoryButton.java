package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class LoreCategoryButton extends InfoButton {

	private final ELoreCategory category;
	private final ItemStack iconStack;
	
	public LoreCategoryButton(InfoScreen screen, ELoreCategory category) {
		super(screen, 0, 0);
		this.category = category;
		this.iconStack = category.makeIcon();
	}

	@Override
	public IInfoSubScreen getScreen(INostrumMagic attr) {
		return new LoreCategorySubScreen(category);
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
		
		RenderSystem.setShaderTexture(0, InfoScreen.background);
		RenderSystem.enableBlend();
		RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, this.x, this.y, 0,
				0, width,
				height, InfoScreen.TEXT_WHOLE_WIDTH, InfoScreen.TEXT_WHOLE_HEIGHT,
				tint, tint, tint, 1f);
		RenderSystem.disableBlend();
		
		final int itemLength = 16;
		
		int x = this.x + (width - itemLength) / 2;
		int y = this.y + (height - itemLength) / 2;
		Minecraft.getInstance().getItemRenderer().renderGuiItem(iconStack, x, y);
	}

	private List<Component> desc = new ArrayList<>(1);
	@Override
	public List<Component> getDescription() {
		if (desc.isEmpty())
			desc.add(category.getTitle());
		
		return desc;
	}

	@Override
	public void updateNarration(NarrationElementOutput p_169152_) {
		this.defaultButtonNarrationText(p_169152_);
	}
}
