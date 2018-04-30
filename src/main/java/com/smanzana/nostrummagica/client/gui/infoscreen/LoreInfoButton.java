package com.smanzana.nostrummagica.client.gui.infoscreen;

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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class LoreInfoButton extends InfoButton {

	private ILoreTagged lore;
	
	// Cache
	private ItemStack iconStack = null;
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
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		float tint = 1f;
		if (mouseX >= xPosition 
			&& mouseY >= yPosition 
			&& mouseX < xPosition + width 
			&& mouseY < yPosition + height) {
			tint = .75f;
		}
		
		GL11.glColor4f(tint, tint, tint, 1f);
		mc.getTextureManager().bindTexture(InfoScreen.background);
		GlStateManager.enableBlend();
		Gui.drawModalRectWithCustomSizedTexture(xPosition, yPosition, 0, 0,
				width, height,
				InfoScreen.TEXT_WHOLE_WIDTH, InfoScreen.TEXT_WHOLE_HEIGHT);
		GlStateManager.disableBlend();
		
		final int itemLength = 16;
		
		if (iconStack == null && iconEntity == null) {
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
			} else if (lore instanceof LoreRegistry.Preset) {
				LoreRegistry.Preset preset = (LoreRegistry.Preset) lore;
				if (preset.getBlock() != null) {
					Item item = Item.getItemFromBlock(preset.getBlock());
					if (item != null)
						iconStack = new ItemStack(item, 1);
				} else if (preset.getEntity(mc.theWorld) != null) {
					iconEntity = preset.getEntity(mc.theWorld);
				}
					
			}
		}
		
		if (iconStack != null) {
			int x = xPosition + (width - itemLength) / 2;
			int y = yPosition + (height - itemLength) / 2;
			mc.getRenderItem().renderItemIntoGUI(iconStack, x, y);
		} else if (iconEntity != null) {
			int x = xPosition + (width / 2);
			int y = yPosition + (width - 1);
			GuiInventory.drawEntityOnScreen(x, y,
					(int) (width * .4), (float)(xPosition) - mouseX, (float)(yPosition) - mouseY, (EntityLivingBase)iconEntity);
		}
	}
}
