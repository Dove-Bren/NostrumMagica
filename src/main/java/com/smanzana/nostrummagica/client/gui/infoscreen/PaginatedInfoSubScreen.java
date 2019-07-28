package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.book.BookScreen;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PaginatedInfoSubScreen implements IInfoSubScreen {

	private static final int LINE_HEIGHT_EXTRA = 2;
	
	private String key;
	private String desc;
	private String pages[][];
	private int page;
	
	public PaginatedInfoSubScreen(String key) {
		desc = I18n.format("info." + key + ".name", (Object[]) null);
		this.key = key;
		this.page = 0;
	}
	
	@Override
	public void draw(INostrumMagic attr, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY) {
		int len = mc.fontRendererObj.getStringWidth(desc);
		mc.fontRendererObj.drawStringWithShadow(desc, x + (width / 2) + (-len / 2), y, 0xFFFFFFFF);
		
		if (pages == null) {
			String translation = I18n.format("info." + key + ".desc", (Object[]) null);
			paginate(translation, width - 10, height - 20);
		}
		
		int i = 0;
		for (String line : pages[page])
			mc.fontRendererObj.drawString(line,
					x + 5,
					y + 20 + (i++ * (mc.fontRendererObj.FONT_HEIGHT + LINE_HEIGHT_EXTRA)),
					0xFFFFFFFF);
		
		if (pages.length > 1) {
			String str = (page + 1) + " / " + pages.length;
			len = mc.fontRendererObj.getStringWidth(str);
			mc.fontRendererObj.drawString(str,
					x + (width - len) / 2,
					y + height - (mc.fontRendererObj.FONT_HEIGHT + 2), 0xFFDD55);
		}
	}
	
	private void paginate(String input, int pageWidth, int pageHeight) {
		FontRenderer fonter = Minecraft.getMinecraft().fontRendererObj;
		final int maxLines = (pageHeight / (LINE_HEIGHT_EXTRA + fonter.FONT_HEIGHT)) - 1;
		int count;
		List<String[]> pages = new LinkedList<>();
		String[] lines;
		StringBuffer buffer = new StringBuffer();
		while (!input.trim().isEmpty()) {
			lines = new String[maxLines];
			count = 0;
			while (count < maxLines) {
				
				if (input.trim().isEmpty() || input.charAt(0) == '|') {
					lines[count++] = buffer.toString();
					buffer = new StringBuffer();
					if (!input.trim().isEmpty()) {
						input = input.substring(1);
						continue;
					} else {
						break;
					}
				}
				int length = 0;
				while (!input.trim().isEmpty()) {
					if (input.trim().charAt(0) == '|') {
						lines[count++] = buffer.toString();
						buffer = new StringBuffer();
						break;
					}
					int pos = input.indexOf(' ');
					int split = input.indexOf('|');
					String word;
					if (pos == -1)
						word = input;
					else if (split != -1 && split < pos) {
						word = input.substring(0, split);
						pos = split - 1;
					} else
						word = input.substring(0, pos + 1);
					
					int width = fonter.getStringWidth(word);
					if (length > 0 && length + width > pageWidth) {
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
			pages.add(lines);
		}
		
		this.pages = pages.toArray(new String[0][]);
	}

	@Override
	public Collection<ISubScreenButton> getButtons() {
		return Lists.newArrayList(
				new NextPageButton(90, 0, 0, false),
				new NextPageButton(91, 0, 0, true),
				new HomeButton(92, 0, 0)
		);
	}
	
	class NextPageButton extends ISubScreenButton {
		private final boolean isNextButton;

		public NextPageButton(int parButtonId, int parPosX, int parPosY, boolean parIsNextButton) {
			super(parButtonId, parPosX, parPosY);
			this.width = 23;
			this.height = 13;
			isNextButton = parIsNextButton;
		}

		@Override
		public void drawButton(Minecraft mc, int parX, int parY) {
			if (visible) {
				boolean isButtonPressed = (parX >= xPosition 
						&& parY >= yPosition 
						&& parX < xPosition + width 
						&& parY < yPosition + height);

				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				mc.getTextureManager().bindTexture(BookScreen.background);
				int textureX = 0;
				int textureY = 223;

				if (isButtonPressed) {
					textureX += 23;
				}

				if (!isNextButton) {
					textureY += 13;
				}

				Gui.drawModalRectWithCustomSizedTexture(xPosition, yPosition, textureX, textureY,
						23, 13, BookScreen.TEXT_WHOLE_WIDTH, BookScreen.TEXT_WHOLE_HEIGHT);

			}
		}
		
		@Override
		public void onClick(INostrumMagic attr) {
			if (this.isNextButton)
				page = Math.min(pages.length - 1, page+1);
			else
				page = Math.max(0, page-1);
		}
	}

	@SideOnly(Side.CLIENT)
    class HomeButton extends ISubScreenButton {
		public HomeButton(int parButtonId, int parPosX, int parPosY) {
            super(parButtonId, parPosX, parPosY);
            this.width = 23; 
            this.height = 13;
		}

        @Override
        public void drawButton(Minecraft mc, int parX, int parY) {
        	if (visible) {
        		boolean isButtonPressed = (parX >= xPosition 
        				&& parY >= yPosition 
                      && parX < xPosition + width 
                      && parY < yPosition + height);

        		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(BookScreen.background);
                int textureX = 48;
                int textureY = 221;

                if (isButtonPressed) {
                	textureX += 16;
                }

                Gui.drawModalRectWithCustomSizedTexture(xPosition, yPosition, textureX, textureY,
                		16, 16, BookScreen.TEXT_WHOLE_WIDTH, BookScreen.TEXT_WHOLE_HEIGHT);
                
            }
        }
        
        public void playPressSound(SoundHandler soundHandlerIn) {
        	soundHandlerIn.playSound(PositionedSoundRecord.getMasterRecord(NostrumMagicaSounds.UI_TICK.getEvent(), 1.0F));
        }
		
		@Override
		public void onClick(INostrumMagic attr) {
			page = 0;
		}
    }

}