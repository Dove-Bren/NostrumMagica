package com.smanzana.nostrummagica.client.gui.book;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RitualRecipePage implements IBookPage {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation(NostrumMagica.MODID, "textures/gui/book_extras.png");
	private static final int TEXT_WIDTH = 256;
	private static final int TEXT_HEIGHT = 128;
	private static final int TEXT_HOFFSET = 64;
	private static final int COMPONENT_WIDTH[] = new int[] {51, 54, 80};
	private static final int COMPONENT_HEIGHT[] = new int[] {52, 54, 83};
	private static final int CANDLE_HOFFSET = 19;
	private static final int CANDLE_VOFFSET = 19;
	private static final int ALTAR_OFFSET = 33;
	
	private RitualRecipe recipe;
	private List<String> tooltip;
	
	private int widthCache;
	private int heightCache;
	private int effWidth;
	private int effHeight;
	
	public RitualRecipePage(RitualRecipe recipe) {
		this.recipe = recipe;
		tooltip = makeTooltip(recipe);
	}
	
	@Override
	public void draw(BookScreen parent, FontRenderer fonter, int xoffset, int yoffset, int width, int height) {
		this.widthCache = width;
		this.heightCache = height;
		Minecraft mc = Minecraft.getMinecraft();
		
		mc.getTextureManager().bindTexture(TEXTURE);
		GlStateManager.pushMatrix();
		
		int tier = recipe.getTier();
		float scale = 1.0f;
		{
			float hscale, vscale;
			hscale = ((float) width) / ((float) COMPONENT_WIDTH[tier]);
			vscale = ((float) height) / ((float) COMPONENT_HEIGHT[tier]);
			if (hscale < scale)
				scale = hscale;
			if (vscale < scale)
				scale = vscale;
		}
		
		effWidth = COMPONENT_WIDTH[tier];
		effHeight = COMPONENT_HEIGHT[tier];
		if (scale < .8f) {
			effWidth = (int) (((float) effWidth) * scale);
			effHeight = (int) (((float) effHeight) * scale);
		}
		int centerx = xoffset + (width / 2);
		int centery = yoffset + (height / 2) + 10;
		int x = centerx - (effWidth / 2);
		int y = centery - (effHeight / 2);
		
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		Gui.drawModalRectWithCustomSizedTexture(x, y, TEXT_HOFFSET * tier, 0, effWidth, effHeight, TEXT_WIDTH, TEXT_HEIGHT);

		ItemStack item;
		
		// Flavor Gem
		item = getGemItem(recipe.getElement());
		mc.getRenderItem().renderItemIntoGUI(item, centerx - 8, yoffset + 18);
		
		if (tier == 0) {
			// reagent on candle in center
			item = getReagentItem(recipe.getTypes()[0]);
			mc.getRenderItem().renderItemIntoGUI(item, centerx - 8, centery - 8);
		} else {
			// reagents in 4 candles around. Item in center altar
			int count = 0;
			for (int i = -1; i <= 1; i+=2)
			for (int j = -1; j <= 1; j+=2) {
				ReagentType type = recipe.getTypes()[count++];
				if (type == null)
					break;
				
				x = centerx + (i * CANDLE_HOFFSET);
				y = centery + (j * CANDLE_VOFFSET);
				
				item = getReagentItem(type);
				mc.getRenderItem().renderItemIntoGUI(item, x - 8, y - 8);
			}
			
			// Has center item
			item = recipe.getCenterItem();
			if (item != null) {
				mc.getRenderItem().renderItemIntoGUI(item, centerx - 8, centery - 8);
			}
			
			if (tier == 2) {
				// altars around and 4 items on them
				count = 0;
				for (int i = -1; i <= 1; i++) {
					int diff = 1 - Math.abs(i);
					for (int j = -diff; j <= diff; j+=2) {
						item = recipe.getExtraItems()[count++];
						if (item == null)
							break;
						
						x = centerx + (i * ALTAR_OFFSET);
						y = centery + (j * ALTAR_OFFSET);
						
						mc.getRenderItem().renderItemIntoGUI(item, x - 8, y - 8);
					}
				}
			}
		}
		
		String name = I18n.format(recipe.getTitleKey(), new Object[0]);
		int len = fonter.getStringWidth(name);
		fonter.drawString(name, centerx - (len / 2), yoffset + 5, 0xFF000000);
		GlStateManager.popMatrix();
	}

	@Override
	public void overlay(BookScreen parent, FontRenderer fonter, int mouseX, int mouseY, int trueX, int trueY) {
		if (tooltip != null) {
			int centerx = widthCache / 2;
			int centery = heightCache / 2;
			int x = centerx - (effWidth / 2);
			int y = centery - (effHeight / 2) + 10;
			
			if (mouseX > x && mouseX < x + effWidth)
			if (mouseY > y && mouseY < y + effHeight)
				parent.renderTooltip(tooltip, trueX, trueY);
		}
	}
	
	private static EnumMap<ReagentType, ItemStack> reagentCache = new EnumMap<>(ReagentType.class);
	private ItemStack getReagentItem(ReagentType type) {
		if (reagentCache.get(type) == null) {
			reagentCache.put(type, ReagentItem.instance().getReagent(type, 1));
		}
		
		return reagentCache.get(type);
	}
	
	private List<String> makeTooltip(RitualRecipe recipe) {
		if (recipe == null)
			return Lists.newArrayList("Invalid Recipe!");
		
		List<String> list = new LinkedList<>();
		
		int tier = recipe.getTier();
		if (tier == 0) {
			list.add(recipe.getTypes()[0].prettyName());
		} else {
			list.add(recipe.getCenterItem().getDisplayName() + " (center)");
			for (ReagentType type : recipe.getTypes()) {
				if (type == null)
					continue;
				list.add(type.prettyName() + " (candle)");
			}
			
			if (tier == 2)
			for (ItemStack extra : recipe.getExtraItems()) {
				if (extra == null)
					continue;
				
				list.add(extra.getDisplayName());
			}
		}
		
		return list;
		
	}
	
	private Map<EMagicElement, ItemStack> gems = new HashMap<>();
	private ItemStack getGemItem(EMagicElement element) {
		if (gems.get(element) == null) {
			gems.put(element, InfusedGemItem.instance().getGem(element, 1));
		}
		
		return gems.get(element);
	}
}
