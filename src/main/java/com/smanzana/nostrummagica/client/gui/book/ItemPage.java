package com.smanzana.nostrummagica.client.gui.book;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ItemPage implements IBookPage {
	
	private int widthCache;
	
	private int heightCache;
	
	private ItemStack[] itemImages;
	
	public ItemPage(ItemStack item) {
		if (item.getMetadata() == OreDictionary.WILDCARD_VALUE) {
			List<ItemStack> items = new LinkedList<>();
			int i;
			ItemStack image;
			for (i = 0; i < 16; i++) {
				image = new ItemStack(item.getItem(), 1, i);
				IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(image);
				if (model == null || model == Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getMissingModel())
					break;
				items.add(image);
			}
			itemImages = items.toArray(new ItemStack[items.size()]);
		} else {
			itemImages = new ItemStack[]{item};
		}
	}
	
	@Override
	public void draw(BookScreen parent, FontRenderer fonter, int xoffset, int yoffset, int width, int height) {
		widthCache = width;
		heightCache = height;
		
		int centerx = xoffset + (width / 2);
		int centery = yoffset + (height / 2);
		centerx -= 8; //offset for 16x16 item icon
		centery -= 8;

		int displayIndex = (int) (Minecraft.getSystemTime() / 1500);
		displayIndex %= itemImages.length;
		ItemStack item = itemImages[displayIndex];
		
		GlStateManager.pushMatrix();
		
		RenderItem itemRender = parent.getRenderItem();
		GlStateManager.translate(0.0F, 0.0F, 32.0F);
        itemRender.zLevel = 200.0F;
        net.minecraft.client.gui.FontRenderer font = null;
        if (item != null && item != ItemStack.EMPTY) font = item.getItem().getFontRenderer(item);
        if (font == null) font = fonter;
        itemRender.renderItemAndEffectIntoGUI(item, centerx, centery);
        itemRender.zLevel = 0.0F;
		
		GlStateManager.popMatrix();
	}

	@Override
	public void overlay(BookScreen parent, FontRenderer fonter, int mouseX, int mouseY, int trueX, int trueY) {
		int displayIndex = (int) (Minecraft.getSystemTime() / 1500);
		displayIndex %= itemImages.length;
		ItemStack item = itemImages[displayIndex];
		if (item != null && item != ItemStack.EMPTY) {
			int centerx = widthCache / 2;
			int centery = heightCache / 2;
			int x = centerx - 8;
			int y = centery - 8;
			
			if (mouseX > x && mouseX < x + 16)
			if (mouseY > y && mouseY < y + 16)
				parent.renderTooltip(item, trueX, trueY);
		}
		
		
	}
	
}
