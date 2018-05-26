package com.smanzana.nostrummagica.client.gui.book;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
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
	
	private static Map<String, Integer> lastPage;
	private static int getLastPage(String key) {
		if (lastPage == null)
			lastPage = new HashMap<>();
		
		Integer val = lastPage.get(key);
		if (val == null) {
			val = 0;
			lastPage.put(key, val);
		}
		
		return val;
	}
	
	private static void setLastPage(String key, int page) {
		if (lastPage == null)
			lastPage = new HashMap<>();
		
		lastPage.put(key, page);
	}
	
	public static final ResourceLocation background = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/book_back.png");
	
	protected static final int TEXT_WIDTH = 350;
	
	protected static final int TEXT_HEIGHT = 210;
	
	public static final int TEXT_WHOLE_WIDTH = 350;
	
	public static final int TEXT_WHOLE_HEIGHT = 250;
	
	protected static final int PAGE_WIDTH = 132;
	
	protected static final int PAGE_HEIGHT = 180;
	
	protected static final int PAGE_HOFFSET = 25;
	
	protected static final int PAGE_VOFFSET = 14;
	
	/**
	 * Distance between left and right page
	 */
	protected static final int PAGE_DISTANCE = 35;
	
	private int currentPage;
	
	private int maxPage;
	
	private List<IBookPage> pages;
	
	private String screenKey;
	
	private NextPageButton backButton;
	private NextPageButton nextButton;
	private HomeButton homeButton;
	
	public BookScreen(String screenKey, List<IBookPage> pages) {
		this(screenKey, pages, true);
	}
	
	public BookScreen(String screenKey, List<IBookPage> pages, boolean tableOfContents){
		this.pages = pages;
		this.currentPage = 0;
		this.maxPage = (pages.size() - 1) / 2;
		
		this.screenKey = screenKey;
		
		this.width = TEXT_WIDTH;
		this.height = TEXT_HEIGHT;
		
		if (tableOfContents) {
			LinkedList<String> titles = new LinkedList<>();
			LinkedList<Integer> nums = new LinkedList<>();
			int index = 1;
			int pos = 0;
			for (IBookPage page : pages) {
				if (page instanceof TitlePage) {
					TitlePage t = (TitlePage) page;
					if (t.shouldIndex()) {
						titles.add(t.getTitle());
						nums.add(index);
					} else {
						if (titles.isEmpty()) {
							// only encountered titles that aren't indexed
							pos++;
						}
					}
				}
				index++;
			}
			
			if (!titles.isEmpty()) {
				TableOfContentsPage contents = new TableOfContentsPage(
						titles.toArray(new String[0]),
						nums.toArray(new Integer[0]),
						true
						);
				pages.add(pos, contents);
			}
		}
	}

	@Override
	public void initGui() {
		currentPage = getLastPage(screenKey);
		if (currentPage > maxPage) {
			// This is probably a different book
			setLastPage(screenKey, 0);
			currentPage = 0;
		}
		int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
		int topOffset = (this.height - TEXT_HEIGHT) / 2;
		backButton = new NextPageButton(0, leftOffset + 30, topOffset + PAGE_HEIGHT + 5, false);
		this.buttonList.add(backButton);
		nextButton = new NextPageButton(1, leftOffset + TEXT_WIDTH - (35 + 23), topOffset + PAGE_HEIGHT + 5, true);
		this.buttonList.add(nextButton);
		homeButton = new HomeButton(2, leftOffset + 30 + 24, topOffset + PAGE_HEIGHT + 3);
		this.buttonList.add(homeButton);
	}
	
	@Override	
	public void updateScreen() {
		backButton.visible = currentPage > 0;
		nextButton.visible = currentPage < maxPage;
		homeButton.visible = currentPage > 0;
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
		
		// Do buttons and other parent stuff
		super.drawScreen(parWidth, parHeight, p_73863_3_);
		
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
		} else if (button == homeButton) {
			currentPage = 0;
		}
		
		setLastPage(screenKey, currentPage);
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
	
	public void requestPageChange(int newIndex) {
		// index is page index. Not book index.
		currentPage = newIndex / 2; // Now it's book index
		setLastPage(this.screenKey, currentPage);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		
		// going to be checked twice, but oh well. Check our buttons
		if (backButton.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)
				|| nextButton.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
			;
		} else {
			int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
			int topOffset = (this.height - TEXT_HEIGHT) / 2;
			
			if (mouseX > (leftOffset + PAGE_HOFFSET) && mouseX < (leftOffset + TEXT_WIDTH) - PAGE_HOFFSET
					&& mouseY > topOffset + PAGE_VOFFSET && mouseY < (topOffset + TEXT_HEIGHT) + PAGE_VOFFSET) {
				//in bounds. Now figure out which it is
				IBookPage page;
				if (mouseX < (width/2) - PAGE_HOFFSET) {
					page = pages.get(currentPage * 2);
					if (page instanceof IClickableBookPage) {
						((IClickableBookPage) page).onClick(this, mouseX - (leftOffset + PAGE_HOFFSET), mouseY - (topOffset + PAGE_VOFFSET), mouseButton);
					}
				} else if (pages.size() > (currentPage * 2) + 1 && mouseX > (width / 2) + PAGE_HOFFSET) {
					page = pages.get((currentPage * 2) + 1);
					if (page instanceof IClickableBookPage) {
						((IClickableBookPage) page).onClick(this, mouseX - (leftOffset + PAGE_HOFFSET + PAGE_WIDTH + PAGE_DISTANCE), mouseY - (topOffset + PAGE_VOFFSET), mouseButton);
					}
				}
			}
		}
		
		super.mouseClicked(mouseX, mouseY, mouseButton);
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
                int textureY = 223;

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

	@SideOnly(Side.CLIENT)
    static class HomeButton extends GuiButton {
		public HomeButton(int parButtonId, int parPosX, int parPosY) {
            super(parButtonId, parPosX, parPosY, 23, 13, "");
		}

		/**
		 *Draws this button to the screen.
		 */
        @Override
        public void drawButton(Minecraft mc, int parX, int parY) {
        	if (visible) {
        		boolean isButtonPressed = (parX >= xPosition 
        				&& parY >= yPosition 
                      && parX < xPosition + width 
                      && parY < yPosition + height);

        		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(background);
                int textureX = 48;
                int textureY = 221;

                if (isButtonPressed) {
                	textureX += 16;
                }

                Gui.drawModalRectWithCustomSizedTexture(xPosition, yPosition, textureX, textureY,
                		16, 16, TEXT_WHOLE_WIDTH, TEXT_WHOLE_HEIGHT);
                
            }
        }
        
        public void playPressSound(SoundHandler soundHandlerIn) {
        	soundHandlerIn.playSound(PositionedSoundRecord.getMasterRecord(NostrumMagicaSounds.UI_TICK.getEvent(), 1.0F));
        }
    }
	
	/**
	 * Splits one big long string into multiple lined pages based on the
	 * size of the pages.
	 * @param pages
	 * @param input
	 */
	public static void makePagesFrom(List<IBookPage> pages, String input) {
		FontRenderer fonter = Minecraft.getMinecraft().fontRendererObj;
		final int maxLines = (PAGE_HEIGHT / (LinedTextPage.LINE_HEIGHT_EXTRA + fonter.FONT_HEIGHT)) - 1;
		String lines[];
		int count;
		StringBuffer buffer = new StringBuffer();
		while (!input.trim().isEmpty()) {
			lines = new String[maxLines];
			count = 0;
			while (count < maxLines) {
				
				if (input.trim().isEmpty()) {
					lines[count++] = buffer.toString();
					buffer = new StringBuffer();
					break;
				}
				int length = 0;
				while (!input.trim().isEmpty()) {
					int pos = input.indexOf(' ');
					String word;
					if (pos == -1)
						word = input;
					else
						word = input.substring(0, pos + 1);
					
					int width = fonter.getStringWidth(word);
					if (length > 0 && length + width > PAGE_WIDTH) {
						lines[count++] = buffer.toString();
						buffer = new StringBuffer();
						break;
					} else {
						buffer.append(word);
						input = input.substring(pos == -1 ? word.length() : pos + 1);
						length += width;
					}
				}
			}
			pages.add(new LinedTextPage(lines));
		}
	}
	
	private static class TableOfContentsPage implements IClickableBookPage {

		private boolean title;
		private String[] pages;
		private Integer[] indices;
		
		private int widthCache;
		private int xCache;
		private int yCache;
		
		public TableOfContentsPage(String[] pages, Integer[] indices, boolean title) {
			this.title = title;
			this.pages = pages;
			this.indices = indices;
		}
		
		@Override
		public void draw(BookScreen parent, FontRenderer fonter, int xoffset, int yoffset, int width, int height) {
			widthCache = width;
			xCache = xoffset;
			
			if (title) {
				int x = xoffset + (width / 2);
				x -= fonter.getStringWidth("Table Of Contents") / 2;
				fonter.drawString("Table Of Contents", x, yoffset + 5, 0xFF202020);
				yoffset += 10 + (fonter.FONT_HEIGHT);
			}
			
			yCache = yoffset;
			
			for (int i = 0; i < pages.length; i++) {
				fonter.drawString(pages[i], xoffset, yoffset, 0xFF400070);
				yoffset += fonter.FONT_HEIGHT + 2;
			}
		}

		@Override
		public void overlay(BookScreen parent, FontRenderer fonter, int mouseX, int mouseY, int trueX, int trueY) {
			if (title) {
				mouseY -= fonter.FONT_HEIGHT + 10;
			}
			int index = mouseY / (fonter.FONT_HEIGHT + 2);
			if (index < pages.length && index >= 0)
				Gui.drawRect(xCache, yCache + (index * (fonter.FONT_HEIGHT + 2)) - 1, xCache + widthCache, yCache + (index * (fonter.FONT_HEIGHT + 2) + fonter.FONT_HEIGHT) - 1, 0x30000000);
		}

		@Override
		public boolean onClick(BookScreen parent, int mouseX, int mouseY, int button) {
			if (title) {
				mouseY -= parent.fontRendererObj.FONT_HEIGHT + 10;
			}
			if (button == 0) {
				int index = mouseY / (parent.fontRendererObj.FONT_HEIGHT + 2);
				if (index < pages.length) {
					parent.requestPageChange(indices[index]);
					return true;
				}
			}
			
			return false;
		}
		
	}
	
}
