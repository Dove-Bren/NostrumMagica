package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.book.BookScreen;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PaginatedInfoSubScreen implements IInfoSubScreen {

	private static final int LINE_HEIGHT_EXTRA = 2;
	
	private String key;
	private String desc;
	private String pages[][];
	private int page;
	private final InfoScreen screen;
	
	public PaginatedInfoSubScreen(InfoScreen screen, String key) {
		desc = I18n.get("info." + key + ".name", (Object[]) null);
		this.key = key;
		this.page = 0;
		this.screen = screen;
	}
	
	@Override
	public void draw(INostrumMagic attr, Minecraft mc, PoseStack matrixStackIn, int x, int y, int width, int height, int mouseX, int mouseY) {
		int len = mc.font.width(desc);
		mc.font.drawShadow(matrixStackIn, desc, x + (width / 2) + (-len / 2), y, 0xFFFFFFFF);
		
		if (pages == null) {
			String translation = I18n.get("info." + key + ".desc", (Object[]) null);
			paginate(translation, width - 10, height - 20);
		}
		
		int i = 0;
		for (String line : pages[page])
			mc.font.draw(matrixStackIn, line,
					x + 5,
					y + 20 + (i++ * (mc.font.lineHeight + LINE_HEIGHT_EXTRA)),
					0xFFFFFFFF);
		
		if (pages.length > 1) {
			String str = (page + 1) + " / " + pages.length;
			len = mc.font.width(str);
			mc.font.draw(matrixStackIn, str,
					x + (width - len) / 2,
					y + height - (mc.font.lineHeight + 2), 0xFFDD55);
		}
	}
	
	private void paginate(String input, int pageWidth, int pageHeight) {
		Minecraft mc = Minecraft.getInstance();
		Font fonter = mc.font;
		final int maxLines = (pageHeight / (LINE_HEIGHT_EXTRA + fonter.lineHeight)) - 1;
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
					
					int width = fonter.width(word);
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
	public Collection<AbstractWidget> getWidgets(int x, int y, int width, int height) {
		return Lists.newArrayList(
				new NextPageButton(this.screen, 0, 0, false),
				new NextPageButton(this.screen, 0, 0, true),
				new HomeButton(this.screen, 0, 0)
		);
	}
	
	class NextPageButton extends AbstractButton {
		private final boolean isNextButton;

		public NextPageButton(InfoScreen screen, int parPosX, int parPosY, boolean parIsNextButton) {
			super(parPosX, parPosY, 23, 13, TextComponent.EMPTY);
			this.width = 23;
			this.height = 13;
			isNextButton = parIsNextButton;
		}

		@Override
		public void render(PoseStack matrixStackIn, int parX, int parY, float partialTicks) {
			if (visible) {
				boolean isButtonPressed = (parX >= this.x 
						&& parY >= this.y 
						&& parX < this.x + width 
						&& parY < this.y + height);

				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				RenderSystem.setShaderTexture(0, BookScreen.background);
				int textureX = 0;
				int textureY = 223;

				if (isButtonPressed) {
					textureX += 23;
				}

				if (!isNextButton) {
					textureY += 13;
				}

				RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, this.x, this.y, textureX,
						textureY, 23, 13, BookScreen.TEXT_WHOLE_WIDTH, BookScreen.TEXT_WHOLE_HEIGHT);

			}
		}
		
		@Override
		public void onPress() {
			if (this.isNextButton)
				page = Math.min(pages.length - 1, page+1);
			else
				page = Math.max(0, page-1);
		}

		@Override
		public void updateNarration(NarrationElementOutput p_169152_) {
			this.defaultButtonNarrationText(p_169152_);
		}
	}

	@OnlyIn(Dist.CLIENT)
    class HomeButton extends AbstractButton {
		public HomeButton(InfoScreen screen, int parPosX, int parPosY) {
            super(parPosX, parPosY, 23, 13, TextComponent.EMPTY);
		}

        @Override
        public void render(PoseStack matrixStackIn, int parX, int parY, float partialTicks) {
        	if (visible) {
        		boolean isButtonPressed = (parX >= this.x 
        				&& parY >= this.y 
                      && parX < this.x + width 
                      && parY < this.y + height);

        		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        		RenderSystem.setShaderTexture(0, BookScreen.background);
                int textureX = 48;
                int textureY = 221;

                if (isButtonPressed) {
                	textureX += 16;
                }

                RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, this.x, this.y, textureX,
                		textureY, 16, 16, BookScreen.TEXT_WHOLE_WIDTH, BookScreen.TEXT_WHOLE_HEIGHT);
                
            }
        }
        
        public void playPressSound(SoundManager soundHandlerIn) {
        	soundHandlerIn.play(SimpleSoundInstance.forUI(NostrumMagicaSounds.UI_TICK.getEvent(), 1.0F));
        }
		
		@Override
		public void onPress() {
			page = 0;
		}

		@Override
		public void updateNarration(NarrationElementOutput p_169152_) {
			this.defaultButtonNarrationText(p_169152_);
		}
    }

}
