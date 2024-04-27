package com.smanzana.nostrummagica.client.gui.book;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.Tag;

public class ItemPage implements IBookPage {
	
	private int widthCache;
	
	private int heightCache;
	
	private ItemStack[] itemImages;
	
	public ItemPage(Tag<Item> itemTag) {
		Collection<Item> items = itemTag.getAllElements();
		List<ItemStack> stacks = new ArrayList<>(items.size());
		
		for (Item item : items) {
			stacks.add(new ItemStack(item));
		}
		itemImages = items.toArray(new ItemStack[items.size()]);
	}
	
	public ItemPage(@Nonnull ItemStack item) {
		itemImages = new ItemStack[]{item};
	}
	
	@Override
	public void draw(BookScreen parent, MatrixStack matrixStackIn, FontRenderer fonter, int xoffset, int yoffset, int width, int height) {
		widthCache = width;
		heightCache = height;
		
		int centerx = xoffset + (width / 2);
		int centery = yoffset + (height / 2);
		centerx -= 8; //offset for 16x16 item icon
		centery -= 8;

		int displayIndex = (int) (System.currentTimeMillis() / 1500);
		displayIndex %= itemImages.length;
		ItemStack item = itemImages[displayIndex];
		
		int unused; // uh oh; matrix stack isn't used?
		matrixStackIn.push();
		
		ItemRenderer itemRender = parent.getItemRenderer();
		matrixStackIn.translate(0.0F, 0.0F, 32.0F);
        itemRender.zLevel = 200.0F;
        FontRenderer font = null;
        if (!item.isEmpty()) font = item.getItem().getFontRenderer(item);
        if (font == null) font = fonter;
        itemRender.renderItemAndEffectIntoGUI(item, centerx, centery);
        itemRender.zLevel = 0.0F;
		
        matrixStackIn.pop();
	}

	@Override
	public void overlay(BookScreen parent, MatrixStack matrixStackIn, FontRenderer fonter, int mouseX, int mouseY, int trueX, int trueY) {
		int displayIndex = (int) (System.currentTimeMillis() / 1500);
		displayIndex %= itemImages.length;
		ItemStack item = itemImages[displayIndex];
		if (!item.isEmpty()) {
			int centerx = widthCache / 2;
			int centery = heightCache / 2;
			int x = centerx - 8;
			int y = centery - 8;
			
			if (mouseX > x && mouseX < x + 16)
			if (mouseY > y && mouseY < y + 16)
				parent.renderTooltip(matrixStackIn, item, trueX, trueY);
		}
		
		
	}
	
}
