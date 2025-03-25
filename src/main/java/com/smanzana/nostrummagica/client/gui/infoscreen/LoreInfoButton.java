package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.loretag.IBlockLoreTagged;
import com.smanzana.nostrummagica.loretag.IEntityLoreTagged;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class LoreInfoButton extends InfoButton {

	private ILoreTagged lore;
	
	// Cache
	private @Nonnull ItemStack iconStack = ItemStack.EMPTY;
	private Entity iconEntity = null;
	
	public LoreInfoButton(InfoScreen screen, ILoreTagged lore) {
		super(screen, 0, 0);
		this.lore = lore;
	}

	@Override
	public IInfoSubScreen getScreen(INostrumMagic attr) {
		return new LoreInfoSubScreen(lore);
	}

	@Override
	public void render(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		final Minecraft mc = Minecraft.getInstance();
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
		
		if (iconStack.isEmpty() && iconEntity == null) {
			if (lore instanceof Item) {
				if (lore instanceof SpellRune) {
					iconStack = SpellRune.getRune(EMagicElement.FIRE);
				} else {
					iconStack = new ItemStack((Item) lore, 1);
				}
			} else if (lore instanceof Block) {
				Item item = ((Block) lore).asItem();
				if (item != null)
					iconStack = new ItemStack(item, 1);
			} else if (lore instanceof IEntityLoreTagged) {
				iconEntity = ((IEntityLoreTagged<?>) lore).makeEntity(mc.level);
			} else if (lore instanceof IBlockLoreTagged) {
				Item item = ((IBlockLoreTagged) lore).getBlock().asItem();
				if (item != null) {
					iconStack = new ItemStack(item, 1);
				}
			}
		}
		
		if (!iconStack.isEmpty()) {
			int x = this.x + (width - itemLength) / 2;
			int y = this.y + (height - itemLength) / 2;
			Minecraft.getInstance().getItemRenderer().renderGuiItem(iconStack, x, y);
		} else if (iconEntity != null) {
			int x = this.x + (width / 2);
			int y = this.y + (width - 1);
			InventoryScreen.renderEntityInInventory(x, y,
					(int) (width * .4), (float)(this.x) - mouseX, (float)(this.y) - mouseY, (LivingEntity)iconEntity);
		}
	}

	private List<Component> desc = new ArrayList<>(1);
	@Override
	public List<Component> getDescription() {
		if (desc.isEmpty())
			desc.add(new TextComponent(lore.getLoreDisplayName()));
		
		return desc;
	}

	@Override
	public void updateNarration(NarrationElementOutput p_169152_) {
		this.defaultButtonNarrationText(p_169152_);
	}
}
