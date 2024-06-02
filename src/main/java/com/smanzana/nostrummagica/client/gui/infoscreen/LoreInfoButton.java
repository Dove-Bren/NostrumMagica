package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

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
	public void render(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		final Minecraft mc = Minecraft.getInstance();
		float tint = 1f;
		if (mouseX >= this.x 
			&& mouseY >= this.y 
			&& mouseX < this.x + width 
			&& mouseY < this.y + height) {
			tint = .75f;
		}
		
		mc.getTextureManager().bindTexture(InfoScreen.background);
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
			} else if (lore instanceof LivingEntity) { int unused; // Need some way to get ent from non-ents?
				iconEntity = (Entity) lore;
				if (iconEntity.world == null)
					iconEntity.world = mc.world;
			} else if (lore instanceof LoreRegistry.Preset) {
				LoreRegistry.Preset preset = (LoreRegistry.Preset) lore;
				if (preset.getBlock() != null) {
					Item item = ((Block) preset.getBlock()).asItem();
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
			Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(iconStack, x, y);
		} else if (iconEntity != null) {
			int x = this.x + (width / 2);
			int y = this.y + (width - 1);
			RenderHelper.disableStandardItemLighting();
			InventoryScreen.drawEntityOnScreen(x, y,
					(int) (width * .4), (float)(this.x) - mouseX, (float)(this.y) - mouseY, (LivingEntity)iconEntity);
		}
	}

	private List<ITextComponent> desc = new ArrayList<>(1);
	@Override
	public List<ITextComponent> getDescription() {
		if (desc.isEmpty())
			desc.add(new StringTextComponent(lore.getLoreDisplayName()));
		
		return desc;
	}
}
