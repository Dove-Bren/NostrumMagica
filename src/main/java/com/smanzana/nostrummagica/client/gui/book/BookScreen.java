package com.smanzana.nostrummagica.client.gui.book;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BookScreen extends GuiScreen {
	
	protected static final ResourceLocation background = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/book_back.png");
	
	protected static final int TEXT_WIDTH = 293;
	
	protected static final int TEXT_HEIGHT = 180;
	
	protected static final int TEXT_WHOLE_WIDTH = 300;
	
	protected static final int TEXT_WHOLE_HEIGHT = 220;
	
	protected static final int PAGE_WIDTH = 110;
	
	protected static final int PAGE_HEIGHT = 150;
	
	protected static final int PAGE_HOFFSET = 17;
	
	protected static final int PAGE_VOFFSET = 16;
	
	/**
	 * Distance between left and right page
	 */
	protected static final int PAGE_DISTANCE = 40;
	
	private int currentPage;
	
	private int maxPage;
	
	private List<IBookPage> pages;
	
	private NextPageButton backButton;
	
	private NextPageButton nextButton;
	
	public BookScreen(List<IBookPage> pages) {
		this.pages = pages;
		this.currentPage = 0;
		this.maxPage = (pages.size() - 1) / 2;
	}

	@Override
	public void initGui() {
		currentPage = 0;
		int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
		int topOffset = (this.height - TEXT_HEIGHT) / 2;
		backButton = new NextPageButton(0, leftOffset + 20, topOffset + 150, false);
		this.buttonList.add(backButton);
		nextButton = new NextPageButton(1, leftOffset + TEXT_WIDTH - (20 + 23), topOffset + 150, true);
																	//     /\ arrow size
		this.buttonList.add(nextButton);
	}
	
	@Override
	public void updateScreen() {
		backButton.visible = currentPage > 0;
		nextButton.visible = currentPage < maxPage;
	}
	
	@Override
	public void drawScreen(int parWidth, int parHeight, float p_73863_3_) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		Minecraft.getMinecraft().getTextureManager().bindTexture(background);
		
		int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
		int topOffset = (this.height - TEXT_HEIGHT) / 2;
		//float hscale = ((float) this.width / (float) TEXT_WIDTH);
		//float vscale = ((float) this.height / (float) TEXT_HEIGHT);
		
		Gui.drawModalRectWithCustomSizedTexture(leftOffset, topOffset, 0, 0,
				TEXT_WIDTH, TEXT_HEIGHT, TEXT_WHOLE_WIDTH, TEXT_WHOLE_HEIGHT);
		
		pages.get(currentPage * 2).draw(this, fontRendererObj, leftOffset + PAGE_HOFFSET, topOffset + PAGE_VOFFSET,
				PAGE_WIDTH, PAGE_HEIGHT);
		
		if (pages.size() > (currentPage * 2) + 1)
			pages.get((currentPage * 2) + 1).draw(this, fontRendererObj, leftOffset + PAGE_HOFFSET + PAGE_WIDTH + PAGE_DISTANCE, topOffset + PAGE_VOFFSET,
					PAGE_WIDTH, PAGE_HEIGHT);
		
		//now do overlays
		if (parWidth > (leftOffset + PAGE_HOFFSET) && parWidth < (leftOffset + TEXT_WIDTH) - PAGE_HOFFSET
				&& parHeight > topOffset + PAGE_VOFFSET && parHeight < (topOffset + TEXT_HEIGHT) + PAGE_VOFFSET) {
			//in bounds. Now figure out which it is
			if (parWidth < (width/2) - PAGE_HOFFSET) {
				pages.get(currentPage * 2).overlay(this, fontRendererObj,
						parWidth - (leftOffset + PAGE_HOFFSET), parHeight - (topOffset + PAGE_VOFFSET), parWidth, parHeight);
			} else if (pages.size() > (currentPage * 2) + 1 && parWidth > (width / 2) + PAGE_HOFFSET) {
				pages.get((currentPage * 2) + 1).overlay(this, fontRendererObj,
						parWidth - (leftOffset + PAGE_HOFFSET + PAGE_WIDTH + PAGE_DISTANCE), parHeight - (topOffset + PAGE_VOFFSET), parWidth, parHeight); 
			}
		}
		
		super.drawScreen(parWidth, parHeight, p_73863_3_);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}
	
	@Override
	public void actionPerformed(GuiButton button) {
		//knotty button got pressed
		if (button == backButton) {
			//previous button
			if (currentPage > 0)
				currentPage--;
		} else if (button == nextButton) {
			if (currentPage < maxPage)
				currentPage++;
		}
	}
	
	public RenderItem getRenderItem() {
		return this.itemRender;
	}
	
	public void renderTooltip(ItemStack item, int x, int y) {
		GlStateManager.pushAttrib();
		this.renderToolTip(item, x, y);
		GlStateManager.popAttrib();
		GlStateManager.enableBlend();
	}
	
	public void renderTooltip(List<String> lines, int x, int y) {
		GlStateManager.pushAttrib();
		this.drawHoveringText(lines, x, y, this.fontRendererObj);
		GlStateManager.popAttrib();
		GlStateManager.enableBlend();
	}
	
	/**
	 * Taken from Jabelar's block gui tutorial
	 * http://jabelarminecraft.blogspot.com/p/minecraft-modding-block-with-simple-gui.html
	 * @author Skyler
	 *
	 */
	@SideOnly(Side.CLIENT)
    static class NextPageButton extends GuiButton
    {
        private final boolean isNextButton;

        public NextPageButton(int parButtonId, int parPosX, int parPosY, 

              boolean parIsNextButton)
        {
            super(parButtonId, parPosX, parPosY, 23, 13, "");
            isNextButton = parIsNextButton;
        }

        /**
         * Draws this button to the screen.
         */
        @Override
        public void drawButton(Minecraft mc, int parX, int parY)
        {
            if (visible)
            {
                boolean isButtonPressed = (parX >= xPosition 

                      && parY >= yPosition 

                      && parX < xPosition + width 

                      && parY < yPosition + height);

                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(background);
                int textureX = 0;
                int textureY = 192;

                if (isButtonPressed)
                {
                    textureX += 23;
                }

                if (!isNextButton)
                {
                    textureY += 13;
                }
                
                Gui.drawModalRectWithCustomSizedTexture(xPosition, yPosition, textureX, textureY,
        				23, 13, TEXT_WHOLE_WIDTH, TEXT_WHOLE_HEIGHT);
                
            }
        }
    }
	
}
