package com.smanzana.nostrummagica.client.gui.mirror;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagic;
import com.smanzana.nostrummagica.client.gui.book.BookScreen;
import com.smanzana.nostrummagica.client.gui.book.IBookPage;
import com.smanzana.nostrummagica.client.gui.book.ReferencePage;
import com.smanzana.nostrummagica.client.gui.widget.MoveableObscurableWidget;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.ClientPurchaseResearchMessage;
import com.smanzana.nostrummagica.progression.requirement.IRequirement;
import com.smanzana.nostrummagica.progression.research.NostrumResearch;
import com.smanzana.nostrummagica.progression.research.NostrumResearch.NostrumResearchTab;
import com.smanzana.nostrummagica.progression.research.NostrumResearch.Size;
import com.smanzana.nostrummagica.util.Curves;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class MirrorResearchSubscreen extends PanningMirrorSubscreen {

	private static final ResourceLocation RES_ICONS = NostrumMagica.Loc("textures/gui/mirror_research.png");
	private static final ResourceLocation RES_BACK = NostrumMagica.Loc("textures/gui/mirror_back_research.png");
	
	private static Set<NostrumResearch> seenResearch = null;
	
	private final ITextComponent name;
	private final ItemStack icon;
	
	private IMirrorScreen parent;
	private PlayerEntity player;
	private INostrumMagic attr;
	private int width;
	private int height;
	
	private Map<NostrumResearchTab, ResearchMirrorTab> mirrorTabs;
	private Map<NostrumResearch, ResearchButton> researchButtons;
	private NostrumResearchTab activeTab;
	
	public MirrorResearchSubscreen() {
		name = new TranslationTextComponent("mirror.tab.research.name");
		icon = new ItemStack(NostrumItems.spellTomePage, 1);
		
		researchButtons = new HashMap<>();
		mirrorTabs = new HashMap<>();
		for (NostrumResearchTab tab : NostrumResearchTab.All()) {
			mirrorTabs.put(tab, new ResearchMirrorTab(this, tab));
		}
	}
	
	@Override
	public ITextComponent getName() {
		return name;
	}
	
	@Override
	public ItemStack getIcon() {
		return icon;
	}
	
	@Override
	public boolean isVisible(IMirrorScreen parent, PlayerEntity player) {
		return true;
	}

	@Override
	public void show(IMirrorScreen parent, PlayerEntity player, int width, int height, int guiLeft, int guiTop) {
		this.player = player;
		this.attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			return;
		}
		
		this.parent = parent;
		this.width = width;
		this.height = height;
		
		researchButtons.clear();
		
		// Add buttons that show up on the research screen.
		// We discover which tabs to display at the same time.
		Map<NostrumResearchTab, List<NostrumResearch>> tabs = new HashMap<>();
		boolean firstTime = false;
		
		// Set up seen research if this is our first time through
		if (seenResearch == null) {
			seenResearch = new HashSet<>();
			firstTime = true;
		}
		
		final Rectangle2d bounds = new Rectangle2d(guiLeft, guiTop, width, height);
		for (NostrumResearch research: NostrumResearch.AllResearch()) {
			final int buttonCenterX = guiLeft + (width/2) + (research.getX() * GRID_SCALE);
			final int buttonCenterY = guiTop + (height/2) + (research.getY() * GRID_SCALE);
			final int buttonWidth = WidthForSize(research.getSize());
			final int buttonHeight = HeightForSize(research.getSize());
			final int buttonX = buttonCenterX - (buttonWidth/2);
			final int buttonY = buttonCenterY - (buttonHeight/2);
			ResearchButton button = new ResearchButton(this, research,
					buttonX, buttonY,
					buttonWidth, buttonHeight
					);
			button.setBounds(bounds);
			
			//parent.addWidget(button); Wait to add till after tabs
			researchButtons.put(research, button);
			
			if (research.getTab() == null) {
				NostrumMagica.logger.error("Research has no tab: " + research.getKey());
			}
			
			if (!tabs.containsKey(research.getTab())) {
				tabs.put(research.getTab(), new ArrayList<>(32));
			}
			tabs.get(research.getTab()).add(research);
			
			// If actually visible, update new counts
			if (!button.isHidden()) {
				
				// If it's the first time through, pretend we've already seen it since presumably
				// it's unlocked from a previous time unlocking stuff in the mirror.
				if (firstTime) {
					seenResearch.add(research);
				}
			}
		}
		
		List<NostrumResearchTab> tabList = Lists.newArrayList(tabs.keySet());
		Collections.sort(tabList, (l, r) -> {
			if (l == NostrumResearchTab.MAGICA) {
				return -1;
			}
			if (r == NostrumResearchTab.MAGICA) {
				return 1;
			}
			return l.getRawKey().compareTo(r.getRawKey());
		});
		for (NostrumResearchTab tab : tabList) {
			ResearchMirrorTab mirrorTab = mirrorTabs.get(tab);
			mirrorTab.setChildren(tabs.get(tab));
			parent.addMinorTab(mirrorTab); // Has the side effect of setting this.activeTab
		}
		
		for (ResearchButton button : this.researchButtons.values()) {
			parent.addWidget(button);
		}
		
		super.show(parent, player, width, height, guiLeft, guiTop);
		
		this.refreshButtonPositions();
	}

	@Override
	public void hide(IMirrorScreen parent, PlayerEntity player) {
		// TODO Auto-generated method stub
	}

	@Override
	public void drawBackground(IMirrorScreen parent, MatrixStack matrixStackIn, int width, int height, int mouseX, int mouseY, float partialTicks) {
		float extra = .1f * (float) Math.sin((double) System.currentTimeMillis() / 1500.0);
		float inv = .1f - extra;
		Minecraft.getInstance().getTextureManager().bind(RES_BACK);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, 0, 0, TEX_BACK_WIDTH, TEX_BACK_HEIGHT, width, height, TEX_BACK_WIDTH, TEX_BACK_HEIGHT,
				.9f + extra, 1f, .8f + inv, 1f);
	}

	@Override
	public void drawForeground(IMirrorScreen parent, MatrixStack matrixStackIn, int width, int height, int mouseX, int mouseY, float partialTicks) {
		final Minecraft mc = Minecraft.getInstance();
		final FontRenderer font = mc.font;
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(width/2, 20, 0);
		RenderFuncs.drawGradientRect(matrixStackIn, -40, 0, 40, 20,
				0xFF000000, 0xFF000000,
				0x40000000, 0x40000000);
		
		matrixStackIn.translate(0, 4, 0);
		String str = "Points: " + attr.getResearchPoints();
		int strWidth = font.width(str);
		matrixStackIn.pushPose();
		matrixStackIn.scale(.75f, .75f, 1f);
		font.draw(matrixStackIn, str, -strWidth/2, 0, 0xFFFFFFFF);
		matrixStackIn.popPose();
		
		matrixStackIn.translate(0, font.lineHeight, 0);
		
		{
			final int actKnowledge = NostrumMagic.getKnowledge(attr); 
			final int knowledge = actKnowledge - NostrumMagic.KnowledgeCurves.maxKnowledge(NostrumMagic.KnowledgeCurves.knowledgeLevel(actKnowledge) - 1);
			final int maxKnowledge = NostrumMagic.KnowledgeCurves.maxKnowledge(NostrumMagic.KnowledgeCurves.knowledgeLevel(actKnowledge))
					- NostrumMagic.KnowledgeCurves.maxKnowledge(NostrumMagic.KnowledgeCurves.knowledgeLevel(actKnowledge) - 1);
			
			final int barWidth = 64;
			final int barHeight = 3;
			final int x = Math.round(((float) knowledge / (float) maxKnowledge) * (float) barWidth);
			RenderFuncs.drawRect(matrixStackIn, - (barWidth/2), 0, + (barWidth / 2), 2 + barHeight, 0xFF555555);
			RenderFuncs.drawRect(matrixStackIn, - (barWidth/2) + 1, 0 + 1, (barWidth / 2) - 1, 1 + barHeight, 0xFF000000);
			RenderFuncs.drawRect(matrixStackIn, - (barWidth/2) + 1, 0 + 1, - (barWidth/2) + 1 + x, 1 + barHeight, 0xFFFFFF00);
		}
		
		matrixStackIn.popPose();
		
//		width = 200;
//		int knowledgeHeight = 4;
//		y += font.FONT_HEIGHT + 2;
//		final int x = Math.round(((float) knowledge / (float) maxKnowledge) * (float) width);
//		RenderFuncs.drawRect(matrixStackIn, centerX - (width/2), y, centerX + (width / 2), y + 2 + knowledgeHeight, 0xFF555555);
//		RenderFuncs.drawRect(matrixStackIn, centerX - (width/2) + 1, y + 1, centerX + (width / 2) - 1, y + 1 + knowledgeHeight, 0xFF000000);
//		RenderFuncs.drawRect(matrixStackIn, centerX - (width/2) + 1, y + 1, centerX - (width/2) + 1 + x, y + 1 + knowledgeHeight, 0xFFFFFF00);
//		
//		boolean mouseContent =  (mouseX >= leftOffset + TEXT_CONTENT_HOFFSET && mouseX <= leftOffset + TEXT_CONTENT_HOFFSET + TEXT_CONTENT_WIDTH
//				&& mouseY >= topOffset + TEXT_CONTENT_VOFFSET && mouseY <= topOffset + TEXT_CONTENT_VOFFSET + TEXT_CONTENT_HEIGHT);
//		for (int i = 0; i < this.buttons.size(); ++i) {
//			Button button = (Button)this.buttons.get(i);
//			if (button instanceof ResearchButton) {
//				if (mouseContent) {
//					((ResearchButton) button).drawOverlay(matrixStackIn, mc, mouseX, mouseY);
//				}
//			} else if (button instanceof ResearchTabButton) {
//				((ResearchTabButton) button).drawOverlay(matrixStackIn, mc, mouseX, mouseY);
//			}
//		}
	}
	
	@Override
	protected void onPan(int panX, int panY, float scale) {
		refreshButtonPositions();
	}
	
	@Override
	protected void onZoom(int panX, int panY, float scale) {
		refreshButtonPositions();
	}
	
	protected void refreshButtonPositions() {
		for (ResearchButton button : researchButtons.values()) {
			final int offsetX = button.getStartingX() - (width / 2);
			final int offsetY = button.getStartingY() - (height / 2);
			
			
			final int scaledX = (width/2) + (int) ((offsetX + this.getPanX()) * this.getPanScale());
			final int scaledY = (height/2) + (int) ((offsetY + this.getPanY()) * this.getPanScale());
			button.setPosition(scaledX, scaledY);
		}
	}
	
	protected void onButtonResearchTab(NostrumResearchTab tab) {
		this.activeTab = tab;
		
		this.resetPan();
	}
	
	protected void onButtonResearch(ResearchState state, NostrumResearch research) {
		//if (ignoreButton(button)) return;
		
		if (state == ResearchState.INACTIVE && attr.getResearchPoints() > 0) {
			NetworkHandler.sendToServer(
				new ClientPurchaseResearchMessage(research)	
				);
		} else if (state == ResearchState.COMPLETED) {
			String info = I18n.get(research.getInfoKey(), new Object[0]);
			List<IBookPage> pages = new LinkedList<>();
			BookScreen.makePagesFrom(pages, info);
			
			// Add reference page at the end if we have references
			String[] references = research.getRawReferences();
			String[] displays = research.getDisplayedReferences();

			if (references != null && references.length > 0) {
			
				// translate display names
				String[] displaysFixed = new String[displays.length];
				for (int i = 0; i < displays.length; i++) {
					displaysFixed[i] = I18n.get(displays[i], new Object[0]);
				}
				pages.add(new ReferencePage(displaysFixed, references, false));
			}
			
			parent.showPopupScreen(new BookScreen(System.currentTimeMillis() + "", pages));
		}
	}
	
	protected static enum ResearchState {
		UNAVAILABLE,
		INACTIVE,
		COMPLETED
	}
	
	private static class ResearchButton extends MoveableObscurableWidget {
		
		private final MirrorResearchSubscreen subscreen; 
		private final NostrumResearch research;
		private final List<ITextComponent> tooltip;
		private final float fontScale = 0.75f;
		
		private ResearchState state;
		private long animStartMS;
		private boolean wasHidden;
		
		public ResearchButton(MirrorResearchSubscreen subscreen, NostrumResearch research, int x, int y, int width, int height) {
			super(x, y, width, height, StringTextComponent.EMPTY);
			this.subscreen = subscreen;
			this.research = research;
			updateResearchState();
			this.tooltip = genTooltip();
			wasHidden = this.isHidden();
		}
		
		protected void updateResearchState() {
			if (NostrumMagica.getCompletedResearch(subscreen.player).contains(research))
				state = ResearchState.COMPLETED;
			else if (!research.isPurchaseDisallowed() && NostrumMagica.canPurchaseResearch(subscreen.player, research))
				state = ResearchState.INACTIVE;
			else
				state = ResearchState.UNAVAILABLE;
		}
		
		@Override
		public boolean isHidden() {
			return !NostrumMagica.getResearchVisible(subscreen.player, research);
		}
		
		protected boolean shouldShow() {
			return this.research.getTab() == subscreen.activeTab && !isHidden();
		}
		
		public void startAnim() {
			animStartMS = System.currentTimeMillis();
		}
		
		public void drawTreeLines(MatrixStack matrixStackIn, Minecraft mc) {
			if (research.getParentKeys() != null && research.getParentKeys().length != 0) {
				for (String key : research.getParentKeys()) {
					NostrumResearch parentResearch = NostrumResearch.lookup(key);
					if (parentResearch == null)
						continue;
					
					if (parentResearch.getTab() != research.getTab()) {
						continue;
					}
					
					ResearchButton other = subscreen.researchButtons.get(parentResearch);
					if (other != null)
						renderLine(matrixStackIn, other);
				}
			}
		}
		
		private void renderLine(MatrixStack matrixStackIn, ResearchButton other) {
			// Render 2 flat lines with a nice circle-arc between them
			
			float alpha = (float) (this.animStartMS == 0 ? 1 : ((double)(System.currentTimeMillis() - animStartMS) / 500.0));
			
			matrixStackIn.pushPose();
			BufferBuilder buf = Tessellator.getInstance().getBuilder();
	        RenderSystem.enableBlend();
	        RenderSystem.disableColorLogicOp();
	        RenderSystem.disableTexture();
	        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
	        //GlStateManager.disableDepth();
	        RenderSystem.lineWidth(3.5f);
	        
	        Vector2f child = new Vector2f(x + ((float) width / 2f), y + ((float) height / 2f));
	        Vector2f parent = new Vector2f(other.x + ((float) other.width / 2f), other.y + ((float) other.height / 2f));
	        Vector2f diff = new Vector2f(this.research.getX() - other.research.getX(), this.research.getY() - other.research.getY());
	        
	        Vector2f myCenter = child; // Stash for later
	        
	        if (child.x == parent.x || child.y == parent.y) {
	        	// Straight line
	        	
	        	if (child.x == parent.x) {
	        		// vertical line. Shrink both sides in y
	        		child = new Vector2f(child.x, child.y + (-Math.signum(diff.y) * ((float) height / 2f)));
	        		parent = new Vector2f(parent.x, parent.y - (-Math.signum(diff.y) * ((float) other.height / 2f)));
	        	} else {
	        		// horizional. "" x
	        		child = new Vector2f(child.x + (-Math.signum(diff.x) * ((float) width / 2f)), child.y);
	        		parent = new Vector2f(parent.x - (-Math.signum(diff.x) * ((float) other.width / 2f)), parent.y);
	        	}
	        	
	        	buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		        buf.vertex(child.x, child.y, 0).color(.8f, .8f, .8f, alpha).endVertex();
		        buf.vertex(parent.x, parent.y, 0).color(.8f, .8f, .8f, alpha).endVertex();
		        Tessellator.getInstance().end();
	        } else {
		        boolean vertical;// = (Math.abs(diff.y) > Math.abs(diff.x));
		        
		        // figure out radius of arc to draw
		        double radius;
		        if (Math.abs(diff.y - diff.x) < .1f) {
		        	radius = Math.abs(diff.y);
		        	vertical = false;
		        } else if (Math.abs(diff.x) < Math.abs(diff.y)) {
		        	radius = Math.abs(diff.x);
		        	vertical = true;
		        } else {
		        	radius = Math.abs(diff.y);
		        	vertical = false;
		        }
		        radius = Math.min(Math.max(radius * .5f, 12), 12);//*= .5f;
		        double radiusX = (diff.x < 0 ? -1 : 1) * radius;
		        double radiusY = (diff.y < 0 ? -1 : 1) * radius;
		        Vector2f center = new Vector2f(vertical ? parent.x : child.x, vertical ? child.y : parent.y);
		        
		        Vector2f childTo = new Vector2f(vertical ? center.x + (float) radiusX : center.x, vertical ? center.y : center.y + (float) radiusY);
		        Vector2f parentTo = new Vector2f(vertical ? center.x : center.x - (float) radiusX, vertical ? center.y - (float) radiusY : center.y);
		        
		        if (vertical) {
	        		// vertical at parent. Shrink parent y and child x
		        	child = new Vector2f(child.x + (-Math.signum(diff.x) * ((float) width / 2f)), child.y);
	        		parent = new Vector2f(parent.x, parent.y - (-Math.signum(diff.y) * ((float) other.height / 2f)));
	        	} else {
	        		// inverse of above
	        		child = new Vector2f(child.x, child.y + (-Math.signum(diff.y) * ((float) height / 2f)));
	        		parent = new Vector2f(parent.x - (-Math.signum(diff.x) * ((float) other.width / 2f)), parent.y);
	        	}
		        
		        {
		        	final Matrix4f transform = matrixStackIn.last().pose();
			        buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			        buf.vertex(transform, child.x, child.y, 0).color(.8f, .8f, .8f, alpha).endVertex();
			        buf.vertex(transform, childTo.x, childTo.y, 0).color(.8f, .8f, .8f, alpha).endVertex();
			        buf.vertex(transform, parentTo.x, parentTo.y, 0).color(.8f, .8f, .8f, alpha).endVertex();
			        buf.vertex(transform, parent.x, parent.y, 0).color(.8f, .8f, .8f, alpha).endVertex();
			        Tessellator.getInstance().end();
		        }
		        
		        // Draw inside curve
		        int points = 30;
		        matrixStackIn.pushPose();
		        matrixStackIn.translate(parentTo.x, parentTo.y, 0);
		        float rotate = 0f;
		        boolean flip = false;
		        if (!vertical) {
		        	if (diff.x < 0) {
		        		rotate = 90f;
		        		flip = (diff.y >= 0);
		        	} else {
		        		rotate = 270f;
		        		flip = (diff.y < 0);
		        	}
		        } else {
		        	if (diff.y < 0) {
		        		rotate = 180f;
		        		flip = (diff.x < 0);
		        	} else {
		        		rotate = 0f;
		        		flip = (diff.x >= 0);
		        	}
		        }
		        matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(rotate));
		        {
		        	final Matrix4f transform = matrixStackIn.last().pose();
			        buf.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
			        for (int i = 0; i <= points; i++) {
			        	float progress = (float) i / (float) points;
			        	Vector2f point = Curves.alignedArc2D(progress, Vector2f.ZERO, radius, flip);
			        	buf.vertex(transform, point.x, point.y, 0).color(.8f, .8f, .8f, alpha).endVertex();
			        }
			        Tessellator.getInstance().end();
		        }
		        matrixStackIn.popPose();
	        }
	        
	        matrixStackIn.translate(child.x, child.y, .2);
	        if (child.x < myCenter.x) {
	        	// from left
	        	matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(-90f));
	        } else if (child.x > myCenter.x) {
	        	// from right
	        	matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(90f));
	        } else if (child.y > myCenter.y) {
	        	// from bottom
	        	matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(180f));
	        }
	        RenderSystem.enableTexture();
			RenderSystem.enableBlend();
			Minecraft.getInstance().getTextureManager().bind(RES_ICONS);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, -(TEX_ARROW_WIDTH/2) - 1, -(TEX_ARROW_HEIGHT/2), TEX_ARROW_HOFFSET,
					TEX_ARROW_VOFFSET, TEX_ARROW_WIDTH, TEX_ARROW_HEIGHT, 14, 7, TEX_UTILS_WIDTH,  TEX_UTILS_HEIGHT,
					1f, 1f, 1f, alpha);
			RenderSystem.enableDepthTest();
			matrixStackIn.popPose();
		}
		
		@Override
		public void render(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			updateResearchState();
			
			if (this.shouldShow()) {
				if (this.wasHidden != this.isHidden()) {
					this.startAnim();
					this.wasHidden = isHidden();
				}
				
				RenderSystem.enableDepthTest();
				matrixStackIn.pushPose();
				matrixStackIn.translate(0, 0, .1);
				super.render(matrixStackIn, mouseX, mouseY, partialTicks);
				matrixStackIn.translate(0, 0, -.1);
				drawTreeLines(matrixStackIn, Minecraft.getInstance());
				matrixStackIn.popPose();
			}
		}
		
		@Override
		public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			float alpha = (float) (this.animStartMS == 0 ? 1 : ((double)(System.currentTimeMillis() - animStartMS) / 500.0));
			final int textureX;
			final int textureY;
			final int textureW;
			final int textureH;
			
			if (alpha >= 1f) {
				this.animStartMS = 0;
			}
			
			switch (research.getSize()) {
			case GIANT:
				if (this.isHovered()) {
					textureX = TEX_RESEARCHBUTTON_GIANT_HIGH_HOFFSET;
					textureY = TEX_RESEARCHBUTTON_GIANT_HIGH_VOFFSET;
				} else {
					textureX = TEX_RESEARCHBUTTON_GIANT_HOFFSET;
					textureY = TEX_RESEARCHBUTTON_GIANT_VOFFSET;
				}
				textureW = TEX_RESEARCHBUTTON_GIANT_WIDTH;
				textureH = TEX_RESEARCHBUTTON_GIANT_HEIGHT;
				break;
			case LARGE:
				if (this.isHovered()) {
					textureX = TEX_RESEARCHBUTTON_LARGE_HIGH_HOFFSET;
					textureY = TEX_RESEARCHBUTTON_LARGE_HIGH_VOFFSET;
				} else {
					textureX = TEX_RESEARCHBUTTON_LARGE_HOFFSET;
					textureY = TEX_RESEARCHBUTTON_LARGE_VOFFSET;
				}
				
				textureW = TEX_RESEARCHBUTTON_LARGE_WIDTH;
				textureH = TEX_RESEARCHBUTTON_LARGE_HEIGHT;
				break;
			case NORMAL:
			default:
				if (this.isHovered()) {
					textureX = TEX_RESEARCHBUTTON_SMALL_HIGH_HOFFSET;
					textureY = TEX_RESEARCHBUTTON_SMALL_HIGH_VOFFSET;
				} else {
					textureX = TEX_RESEARCHBUTTON_SMALL_HOFFSET;
					textureY = TEX_RESEARCHBUTTON_SMALL_VOFFSET;
				}
				
				textureW = TEX_RESEARCHBUTTON_SMALL_WIDTH;
				textureH = TEX_RESEARCHBUTTON_SMALL_HEIGHT;
				break;
			}
			
			matrixStackIn.pushPose();
			float[] color = {1f, 1f, 1f, 1f};
			switch (state) {
			case COMPLETED:
				color = new float[] {.2f, 2f/3f, .2f, alpha};
				break;
			case INACTIVE:
				color = new float[] {2f/3f, 0f, 2f/3f, alpha};
				break;
			case UNAVAILABLE:
				color = new float[] {.8f, .0f, .0f, alpha};
				break;
			}
			
			RenderSystem.enableTexture();
			RenderSystem.enableBlend();
			Minecraft.getInstance().getTextureManager().bind(RES_ICONS);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, x, y, textureX,
					textureY, textureW, textureH, this.width, this.height, TEX_UTILS_WIDTH, TEX_UTILS_HEIGHT,
					color[0], color[1], color[2], color[3]);
			
			// Now draw icon
			RenderHelper.turnBackOn();
			// RenderGuiItem moves 100 forward. Blocks render several z deep.
			// Squish to 8 deep, and shift back
			matrixStackIn.scale(1f, 1f, .4f);
			matrixStackIn.translate(0, 0, -90);
			RenderFuncs.RenderGUIItem(research.getIconItem(), matrixStackIn, x + (width - 16) / 2, y + (height - 16) / 2);
			RenderSystem.enableDepthTest();
			RenderHelper.turnOff();
			
			matrixStackIn.popPose();
		}
		
		@Override
		public void renderToolTip(MatrixStack matrixStackIn, int mouseX, int mouseY) {
			if (shouldShow() && this.visible && isHovered()) {
				final Minecraft mc = Minecraft.getInstance();
				final FontRenderer font = mc.font;
		        matrixStackIn.pushPose();
		        matrixStackIn.scale(fontScale, fontScale, 1f);
		        matrixStackIn.translate((int) (mouseX / fontScale) - mouseX, (int) (mouseY / fontScale) - mouseY, 0);
		        GuiUtils.drawHoveringText(matrixStackIn, tooltip, mouseX, mouseY, subscreen.width, subscreen.height, 400, font); // drawTooltip with array of text components
				matrixStackIn.popPose();
			}
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (shouldShow()) {
				return super.mouseClicked(mouseX, mouseY, button);
			}
			
			return false;
		}
		
		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
			return false;
		}
		
		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			return false;
		}
		
		@Override
		public void onClick(double mouseX, double mouseY) {
			subscreen.onButtonResearch(this.state, this.research);
			this.updateResearchState();
		}
		
		private List<ITextComponent> genTooltip() {
			List<ITextComponent> tooltip = new LinkedList<>();
			tooltip.add(new TranslationTextComponent(research.getNameKey(), new Object[0]).withStyle(TextFormatting.BLUE));
			tooltip.add(new TranslationTextComponent(research.getDescKey(), new Object[0]).withStyle(TextFormatting.GRAY));
			
			TextFormatting bad = TextFormatting.RED;
			boolean first = true;
			
	        // Requirements?
	        if (research.getRequirements() != null) {
	        	for (IRequirement req : research.getRequirements()) {
	        		if (!req.matches(subscreen.player)) {
	        			if (first) {
							first = false;
							tooltip.add(new StringTextComponent(""));
							tooltip.add(new TranslationTextComponent("info.requirement.missing"));
						}
	        			for (ITextComponent line : req.getDescription(subscreen.player)) {
	        				if (line instanceof TextComponent) {
	        					tooltip.add(((TextComponent) line).withStyle(bad));
	        				} else {
	        					tooltip.add(line);
	        				}
	        			}
	        		}
	        	}
	        }
			
			if (this.state == ResearchState.INACTIVE && subscreen.attr.getResearchPoints() > 0 && NostrumMagica.canPurchaseResearch(subscreen.player, research)) {
				tooltip.add(new StringTextComponent(" "));
				tooltip.add(new TranslationTextComponent("info.research.purchase").withStyle(TextFormatting.GREEN));
			} else if (this.state == ResearchState.COMPLETED) {
				tooltip.add(new StringTextComponent(" "));
				tooltip.add(new TranslationTextComponent("info.research.view").withStyle(TextFormatting.GREEN));
			} else if (research.isPurchaseDisallowed()) {
				tooltip.add(new StringTextComponent(" "));
				tooltip.add(new TranslationTextComponent("info.research.disallowed").withStyle(TextFormatting.DARK_AQUA));
			}
			
			return tooltip;
		}
	}
	
	private static final class ResearchMirrorTab implements IMirrorMinorTab {
		
		protected final NostrumResearchTab tab;
		private final ITextComponent name;
		private final List<NostrumResearch> children;
		
		private boolean hasNew;
		
		public ResearchMirrorTab(MirrorResearchSubscreen subscreen, NostrumResearchTab tab) {
			this.tab = tab;
			this.name = new TranslationTextComponent(tab.getNameKey());
			this.children = new ArrayList<>(32);
			hasNew = false;
		}

		@Override
		public void onClick(IMirrorScreen parent, IMirrorSubscreen subscreenIn) {
			MirrorResearchSubscreen subscreen = ((MirrorResearchSubscreen) subscreenIn);
			subscreen.onButtonResearchTab(this.tab);
			hasNew = false;
			
			for (NostrumResearch child : children) {
				if (!subscreen.researchButtons.get(child).isHidden()) {
					MirrorResearchSubscreen.seenResearch.add(child);
				}
			}
		}

		@Override
		public ITextComponent getName() {
			return name;
		}

		@Override
		public void renderTab(IMirrorScreen parent, IMirrorSubscreen subscreen, MatrixStack matrixStackIn, int width, int height) {
			RenderFuncs.RenderGUIItem(tab.getIcon(), matrixStackIn, (width - 16) / 2, (height - 16) / 2);
		}

		@Override
		public boolean hasNewEntry(IMirrorScreen parent, IMirrorSubscreen subscreenIn) {
			MirrorResearchSubscreen subscreen = ((MirrorResearchSubscreen) subscreenIn);
			
			if (subscreen.activeTab == this.tab) {
				hasNew = false;
				for (NostrumResearch child : children) {
					if (!subscreen.researchButtons.get(child).isHidden()) {
						MirrorResearchSubscreen.seenResearch.add(child);
					}
				}
				return false;
			}
			
			if (hasNew) {
				return true;
			}
			
			// Scan
			for (NostrumResearch child : children) {
				if (!subscreen.researchButtons.get(child).isHidden()
						&& !MirrorResearchSubscreen.seenResearch.contains(child)) {
					hasNew = true;
					break;
				}
			}
			return hasNew;
		}

		@Override
		public boolean isVisible(IMirrorScreen parent, IMirrorSubscreen subscreenIn) {
			MirrorResearchSubscreen subscreen = (MirrorResearchSubscreen) subscreenIn;
			for (NostrumResearch child : children) {
				if (NostrumMagica.getResearchVisible(subscreen.player, child)) {
					return true;
				}
			}
			return false;
		}
		
		public void setChildren(Collection<NostrumResearch> children) {
			this.children.clear();
			this.children.addAll(children);
		}
	}
	
	private static final int TEX_BACK_WIDTH = 256;
	private static final int TEX_BACK_HEIGHT = 256;
	
	private static final int TEX_UTILS_WIDTH = 128;
	private static final int TEX_UTILS_HEIGHT = 128;
	
	private static final int TEX_RESEARCHBUTTON_SMALL_HOFFSET = 0;
	private static final int TEX_RESEARCHBUTTON_SMALL_VOFFSET = 0;
	private static final int TEX_RESEARCHBUTTON_SMALL_WIDTH = 24;
	private static final int TEX_RESEARCHBUTTON_SMALL_HEIGHT = 24;
	
	private static final int TEX_RESEARCHBUTTON_SMALL_HIGH_HOFFSET = TEX_RESEARCHBUTTON_SMALL_HOFFSET;
	private static final int TEX_RESEARCHBUTTON_SMALL_HIGH_VOFFSET = TEX_RESEARCHBUTTON_SMALL_VOFFSET + TEX_RESEARCHBUTTON_SMALL_HEIGHT;
	
	private static final int TEX_RESEARCHBUTTON_LARGE_HOFFSET = 24;
	private static final int TEX_RESEARCHBUTTON_LARGE_VOFFSET = 0;
	private static final int TEX_RESEARCHBUTTON_LARGE_WIDTH = 32;
	private static final int TEX_RESEARCHBUTTON_LARGE_HEIGHT = 32;
	
	private static final int TEX_RESEARCHBUTTON_LARGE_HIGH_HOFFSET = TEX_RESEARCHBUTTON_LARGE_HOFFSET;
	private static final int TEX_RESEARCHBUTTON_LARGE_HIGH_VOFFSET = TEX_RESEARCHBUTTON_LARGE_VOFFSET + TEX_RESEARCHBUTTON_LARGE_HEIGHT;
	
	private static final int TEX_RESEARCHBUTTON_GIANT_HOFFSET = 56;
	private static final int TEX_RESEARCHBUTTON_GIANT_VOFFSET = 0;
	private static final int TEX_RESEARCHBUTTON_GIANT_WIDTH = 46;
	private static final int TEX_RESEARCHBUTTON_GIANT_HEIGHT = 46;
	
	private static final int TEX_RESEARCHBUTTON_GIANT_HIGH_HOFFSET = TEX_RESEARCHBUTTON_GIANT_HOFFSET;
	private static final int TEX_RESEARCHBUTTON_GIANT_HIGH_VOFFSET = TEX_RESEARCHBUTTON_GIANT_VOFFSET + TEX_RESEARCHBUTTON_GIANT_HEIGHT;
	
	private static final int TEX_ARROW_HOFFSET = 0;
	private static final int TEX_ARROW_VOFFSET = 103;
	private static final int TEX_ARROW_WIDTH = 13;
	private static final int TEX_ARROW_HEIGHT = 9;
	
	private static final int GRID_SCALE = TEX_RESEARCHBUTTON_SMALL_WIDTH + 16;
	
	private static final int POS_RESEARCHBUTTON_SMALL_WIDTH = 24;
	private static final int POS_RESEARCHBUTTON_SMALL_HEIGHT = 24;
	private static final int POS_RESEARCHBUTTON_LARGE_WIDTH = 28;
	private static final int POS_RESEARCHBUTTON_LARGE_HEIGHT = 28;
	private static final int POS_RESEARCHBUTTON_GIANT_WIDTH = 40;
	private static final int POS_RESEARCHBUTTON_GIANT_HEIGHT = 40;
	
	private static int HeightForSize(Size size) {
		switch (size) {
		case GIANT:
			return POS_RESEARCHBUTTON_GIANT_HEIGHT;
		case LARGE:
			return POS_RESEARCHBUTTON_LARGE_HEIGHT;
		case NORMAL:
		default:
			return POS_RESEARCHBUTTON_SMALL_HEIGHT;
		}
	}
	
	private static int WidthForSize(Size size) {
		switch (size) {
		case GIANT:
			return POS_RESEARCHBUTTON_GIANT_WIDTH;
		case LARGE:
			return POS_RESEARCHBUTTON_LARGE_WIDTH;
		case NORMAL:
		default:
			return POS_RESEARCHBUTTON_SMALL_WIDTH;
		}
	}
	
	public static void ResetSeenCache() {
		seenResearch = null;
	}

}
