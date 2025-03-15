package com.smanzana.nostrummagica.client.gui.book;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BookScreen extends Screen {
	
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
	
	public BookScreen(String screenKey, List<IBookPage> pages, boolean tableOfContents) {
		super(new StringTextComponent("NostrumBookScreen"));
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
	public void init() {
		currentPage = getLastPage(screenKey);
		if (currentPage > maxPage) {
			// This is probably a different book
			setLastPage(screenKey, 0);
			currentPage = 0;
		}
		int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
		int topOffset = (this.height - TEXT_HEIGHT) / 2;
		backButton = new NextPageButton(this, leftOffset + 30, topOffset + PAGE_HEIGHT + 5, false);
		this.buttons.add(backButton);
		nextButton = new NextPageButton(this, leftOffset + TEXT_WIDTH - (35 + 23), topOffset + PAGE_HEIGHT + 5, true);
		this.buttons.add(nextButton);
		homeButton = new HomeButton(this, leftOffset + 30 + 24, topOffset + PAGE_HEIGHT + 3);
		this.buttons.add(homeButton);
	}
	
	@Override	
	public void tick() {
		backButton.visible = currentPage > 0;
		nextButton.visible = currentPage < maxPage;
		homeButton.visible = currentPage > 0;
	}
	
	@Override
	public void render(MatrixStack matrixStackIn, int parWidth, int parHeight, float p_73863_3_) {

		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		Minecraft.getInstance().getTextureManager().bind(background);
		
		int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
		int topOffset = (this.height - TEXT_HEIGHT) / 2;
		//float hscale = ((float) this.width / (float) TEXT_WIDTH);
		//float vscale = ((float) this.height / (float) TEXT_HEIGHT);
		
		RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, leftOffset, topOffset, 0,
				0, TEXT_WIDTH, TEXT_HEIGHT, TEXT_WHOLE_WIDTH, TEXT_WHOLE_HEIGHT);
		
		pages.get(currentPage * 2).draw(this, matrixStackIn, font, leftOffset + PAGE_HOFFSET,
				topOffset + PAGE_VOFFSET, PAGE_WIDTH, PAGE_HEIGHT);
		
		if (pages.size() > (currentPage * 2) + 1)
			pages.get((currentPage * 2) + 1).draw(this, matrixStackIn, font, leftOffset + PAGE_HOFFSET + PAGE_WIDTH + PAGE_DISTANCE,
					topOffset + PAGE_VOFFSET, PAGE_WIDTH, PAGE_HEIGHT);
		
		// Do buttons and other parent stuff
		super.render(matrixStackIn, parWidth, parHeight, p_73863_3_);
		
		//now do overlays
		if (parWidth > (leftOffset + PAGE_HOFFSET) && parWidth < (leftOffset + TEXT_WIDTH) - PAGE_HOFFSET
				&& parHeight > topOffset + PAGE_VOFFSET && parHeight < (topOffset + TEXT_HEIGHT) + PAGE_VOFFSET) {
			//in bounds. Now figure out which it is
			if (parWidth < (width/2) - PAGE_HOFFSET) {
				pages.get(currentPage * 2).overlay(this, matrixStackIn,
						font, parWidth - (leftOffset + PAGE_HOFFSET), parHeight - (topOffset + PAGE_VOFFSET), parWidth, parHeight);
			} else if (pages.size() > (currentPage * 2) + 1 && parWidth > (width / 2) + PAGE_HOFFSET) {
				pages.get((currentPage * 2) + 1).overlay(this, matrixStackIn,
						font, parWidth - (leftOffset + PAGE_HOFFSET + PAGE_WIDTH + PAGE_DISTANCE), parHeight - (topOffset + PAGE_VOFFSET), parWidth, parHeight); 
			}
		}
		
	}
	
	@Override
	public boolean isPauseScreen() {
		return true;
	}
	
	protected void changePage(boolean forward) {
		if (forward) {
			// next button
			if (currentPage < maxPage)
				currentPage++;
		} else {
			//previous button
			if (currentPage > 0)
				currentPage--;
		}
		setLastPage(screenKey, currentPage);
	}
	
	protected void gotoHome() {
		currentPage = 0;
		setLastPage(screenKey, currentPage);
	}
	
	public ItemRenderer getItemRenderer() {
		return this.itemRenderer;
	}
	
	@Override
	protected void renderTooltip(MatrixStack matrixStackIn, ItemStack item, int x, int y) {
		//GlStateManager.pushLightingAttributes();
		super.renderTooltip(matrixStackIn, item, x, y);
		//GlStateManager.popAttributes();
		//GlStateManager.enableBlend();
	}
	
	public void renderTooltipLines(MatrixStack matrixStackIn, List<String> lines, int x, int y) {
		List<ITextProperties> text = new ArrayList<>(lines.size());
		for (String raw : lines) {
			text.add(ITextProperties.of(raw));
		}
		this.renderWrappedToolTip(matrixStackIn, text, x, y, font);
	}
	
//	@Override
//	protected void renderTooltip(MatrixStack matrixStackIn, List<String> lines, int x, int y) {
//		//GlStateManager.pushLightingAttributes();
//		super.renderTooltip(matrixStackIn, lines, x, y, this.font);
//		//GlStateManager.popAttributes();
//		//GlStateManager.enableBlend();
//	}
	
	public void requestPageChange(int newIndex) {
		// index is page index. Not book index.
		currentPage = newIndex / 2; // Now it's book index
		setLastPage(this.screenKey, currentPage);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		
		// going to be checked twice, but oh well. Check our buttons
		if (backButton.mouseClicked(mouseX, mouseY, mouseButton)
				|| nextButton.mouseClicked(mouseX, mouseY, mouseButton)) {
			return true;
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
						return true;
					}
				} else if (pages.size() > (currentPage * 2) + 1 && mouseX > (width / 2) + PAGE_HOFFSET) {
					page = pages.get((currentPage * 2) + 1);
					if (page instanceof IClickableBookPage) {
						((IClickableBookPage) page).onClick(this, mouseX - (leftOffset + PAGE_HOFFSET + PAGE_WIDTH + PAGE_DISTANCE), mouseY - (topOffset + PAGE_VOFFSET), mouseButton);
						return true;
					}
				}
			}
		}
		
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	/**
	 * Taken from Jabelar's block gui tutorial
	 * http://jabelarminecraft.blogspot.com/p/minecraft-modding-block-with-simple-gui.html
	 * @author Skyler
	 *
	 */
	@OnlyIn(Dist.CLIENT)
    static class NextPageButton extends AbstractButton
    {
        private final boolean isNextButton;
        
        private final BookScreen screen;

        public NextPageButton(BookScreen screen, int parPosX, int parPosY, boolean parIsNextButton) {
            super(parPosX, parPosY, 23, 13, StringTextComponent.EMPTY);
            isNextButton = parIsNextButton;
            this.screen = screen;
        }

        /**
         * Draws this button to the screen.
         */
        @Override
        public void render(MatrixStack matrixStackIn, int parX, int parY, float partialTicks) {
        	if (visible) {
                boolean isButtonPressed = (parX >= x 

                      && parY >= y 

                      && parX < x + width 

                      && parY < y + height);

                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                Minecraft.getInstance().getTextureManager().bind(background);
                int textureX = 0;
                int textureY = 223;

                if (isButtonPressed) {
                    textureX += 23;
                }

                if (!isNextButton)
                {
                    textureY += 13;
                }
                
                RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, x, y, textureX,
        				textureY, 23, 13, TEXT_WHOLE_WIDTH, TEXT_WHOLE_HEIGHT);
                
            }
        }

		@Override
		public void onPress() {
			screen.changePage(this.isNextButton);
		}
    }

	@OnlyIn(Dist.CLIENT)
    static class HomeButton extends AbstractButton {
		
		 private final BookScreen screen;
		
		public HomeButton(BookScreen screen, int parPosX, int parPosY) {
            super(parPosX, parPosY, 23, 13, StringTextComponent.EMPTY);
            this.screen = screen;
		}

		/**
		 *Draws this button to the screen.
		 */
        @Override
        public void render(MatrixStack matrixStackIn, int parX, int parY, float partialTicks) {
        	if (visible) {
        		boolean isButtonPressed = (parX >= x 
        				&& parY >= y 
                      && parX < x + width 
                      && parY < y + height);

        		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                Minecraft.getInstance().getTextureManager().bind(background);
                int textureX = 48;
                int textureY = 221;

                if (isButtonPressed) {
                	textureX += 16;
                }

                RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, x, y, textureX,
                		textureY, 16, 16, TEXT_WHOLE_WIDTH, TEXT_WHOLE_HEIGHT);
                
            }
        }
        
        public void playPressSound(SoundHandler soundHandlerIn) {
        	soundHandlerIn.play(SimpleSound.forUI(NostrumMagicaSounds.UI_TICK.getEvent(), 1.0F));
        }

		@Override
		public void onPress() {
			screen.gotoHome();
		}
    }
	
	/**
	 * Splits one big long string into multiple lined pages based on the
	 * size of the pages.
	 * @param pages
	 * @param input
	 */
	public static void makePagesFrom(List<IBookPage> pages, String input) {
		Minecraft mc = Minecraft.getInstance();
		FontRenderer fonter = mc.font;
		final int maxLines = (PAGE_HEIGHT / (LinedTextPage.LINE_HEIGHT_EXTRA + fonter.lineHeight)) - 1;
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
					int posNewline = input.indexOf('|');
					boolean newline = false;
					
					if (posNewline != -1 && posNewline < pos) {
						pos = posNewline;
						newline = true;
					}
					
					String word;
					if (pos == -1)
						word = input;
					else
						word = input.substring(0, pos + (newline ? 0 : 1));
					
					int width = fonter.width(word);
					if (length > 0 && length + width > PAGE_WIDTH) {
						lines[count++] = buffer.toString();
						buffer = new StringBuffer();
						break;
					} else {
						buffer.append(word);
						input = input.substring(pos == -1 ? word.length() : pos + 1);
						length += width;
						
						if (newline) {
							lines[count++] = buffer.toString();
							buffer = new StringBuffer();
							break;
						}
					}
				}
			}
			pages.add(new LinedTextPage(lines));
		}
	}
	
}
