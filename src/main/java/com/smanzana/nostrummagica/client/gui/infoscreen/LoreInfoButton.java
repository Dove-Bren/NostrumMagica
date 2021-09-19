package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class LoreInfoButton extends InfoButton {

	private ILoreTagged lore;
	
	// Cache
	private @Nonnull ItemStack iconStack = ItemStack.EMPTY;
	private Entity iconEntity = null;
	
	public LoreInfoButton(int buttonId, ILoreTagged lore) {
		super(buttonId, 0, 0);
		this.lore = lore;
	}

	@Override
	public IInfoSubScreen getScreen(INostrumMagic attr) {
		return new LoreInfoSubScreen(lore);
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		float tint = 1f;
		if (mouseX >= this.x 
			&& mouseY >= this.y 
			&& mouseX < this.x + width 
			&& mouseY < this.y + height) {
			tint = .75f;
		}
		
		GL11.glColor4f(tint, tint, tint, 1f);
		mc.getTextureManager().bindTexture(InfoScreen.background);
		GlStateManager.enableBlend();
		Gui.drawModalRectWithCustomSizedTexture(this.x, this.y, 0, 0,
				width, height,
				InfoScreen.TEXT_WHOLE_WIDTH, InfoScreen.TEXT_WHOLE_HEIGHT);
		GlStateManager.disableBlend();
		
		final int itemLength = 16;
		
		if (iconStack.isEmpty() && iconEntity == null) {
			if (lore instanceof Item) {
				if (lore instanceof SpellRune) {
					iconStack = SpellRune.getRune(EMagicElement.FIRE, 1);
				} else {
					iconStack = new ItemStack((Item) lore, 1);
				}
			} else if (lore instanceof Block) {
				Item item = Item.getItemFromBlock((Block) lore);
				if (item != null)
					iconStack = new ItemStack(item, 1);
			} else if (lore instanceof EntityLivingBase) {
				iconEntity = (Entity) lore;
				if (iconEntity.world == null)
					iconEntity.world = mc.world;
			} else if (lore instanceof LoreRegistry.Preset) {
				LoreRegistry.Preset preset = (LoreRegistry.Preset) lore;
				if (preset.getBlock() != null) {
					Item item = Item.getItemFromBlock(preset.getBlock());
					if (item != null)
						iconStack = new ItemStack(item, 1);
				} else if (preset.getEntity(mc.world) != null) {
					iconEntity = preset.getEntity(mc.world);
				}
					
			}
		}
		
		if (!iconStack.isEmpty()) {
			int x = this.x + (width - itemLength) / 2;
			int y = this.y + (height - itemLength) / 2;
			mc.getRenderItem().renderItemIntoGUI(iconStack, x, y);
		} else if (iconEntity != null) {
			int x = this.x + (width / 2);
			int y = this.y + (width - 1);
			RenderHelper.disableStandardItemLighting();
			GuiInventory.drawEntityOnScreen(x, y,
					(int) (width * .4), (float)(this.x) - mouseX, (float)(this.y) - mouseY, (EntityLivingBase)iconEntity);
		}
	}

	private List<String> desc = new ArrayList<>(1);
	@Override
	public List<String> getDescription() {
		if (desc.isEmpty())
			desc.add(lore.getLoreDisplayName());
		
		return desc;
	}
}
