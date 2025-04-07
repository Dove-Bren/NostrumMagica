package com.smanzana.nostrummagica.client.gui.mirror;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.client.gui.widget.MoveableObscurableWidget;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.ClientUpdateQuestMessage;
import com.smanzana.nostrummagica.progression.quest.NostrumQuest;
import com.smanzana.nostrummagica.progression.requirement.IRequirement;
import com.smanzana.nostrummagica.progression.reward.AlterationReward;
import com.smanzana.nostrummagica.progression.reward.AttributeReward;
import com.smanzana.nostrummagica.progression.reward.AttributeReward.AwardType;
import com.smanzana.nostrummagica.progression.reward.IReward;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MirrorQuestSubscreen extends PanningMirrorSubscreen {
	
	private static final ResourceLocation RES_ICONS = NostrumMagica.Loc("textures/gui/mirror_quest.png");
	private static final ResourceLocation RES_BACK = NostrumMagica.Loc("textures/gui/mirror_quest_back.png");
	
	private final Component name;
	private final ItemStack icon;
	private final Map<NostrumQuest, QuestButton> buttons;
	
	private Player player;
	private INostrumMagic attr;
	private int width;
	private int height;
	
	public MirrorQuestSubscreen() {
		name = new TranslatableComponent("mirror.tab.quest.name");
		icon = new ItemStack(Items.FILLED_MAP, 1);
		buttons = new HashMap<>();
	}
	
	@Override
	public Component getName() {
		return name;
	}
	
	@Override
	public ItemStack getIcon() {
		return icon;
	}
	
	@Override
	public boolean isVisible(IMirrorScreen parent, Player player) {
		return true;
	}
	
	@Override
	public void show(IMirrorScreen parent, Player player, int width, int height, int guiLeft, int guiTop) {
		this.player = player;
		attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			return;
		}
		
		this.width = width;
		this.height = height;
		
		buttons.clear();
		final Rect2i bounds = new Rect2i(guiLeft, guiTop, width, height);
		for (NostrumQuest quest : NostrumQuest.allQuests()) {
			final int buttonX = guiLeft + (width/2) + (quest.getPlotX() * GRID_SCALE);
			final int buttonY = guiTop + (height/2) + (-quest.getPlotY() * GRID_SCALE);
			
			QuestButton button = new QuestButton(this, quest,
					buttonX, buttonY,
					POS_BUTTON_WIDTH, POS_BUTTON_HEIGHT);
			button.setBounds(bounds);
			parent.addWidget(button);
			buttons.put(quest, button);
		}
		
		setButtonPositions();
		
		// Add parent last so it's below everything
		super.show(parent, player, width, height, guiLeft, guiTop);
	}

	@Override
	public void hide(IMirrorScreen parent, Player player) {
		; // Not sure there's much to do. Parent will clean things up for us
	}

	@Override
	public void drawBackground(IMirrorScreen parent, PoseStack matrixStackIn, int width, int height, int mouseX, int mouseY, float partialTicks) {
		float extra = .1f * (float) Math.sin((double) System.currentTimeMillis() / 1500.0);
		float inv = .1f - extra;
		
		RenderSystem.setShaderTexture(0, RES_BACK);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0,
				0, 0, TEX_BACK_WIDTH, TEX_BACK_HEIGHT,
				width, height, TEX_BACK_WIDTH, TEX_BACK_HEIGHT,
				.9f + extra, 1f, .8f + inv, 1f);
	}

	@Override
	public void drawForeground(IMirrorScreen parent, PoseStack matrixStackIn, int width, int height, int mouseX, int mouseY, float partialTicks) {
		
	}
	
	@Override
	protected void onPan(int panX, int panY, float scale) {
		setButtonPositions();
	}
	
	@Override
	protected void onZoom(int panX, int panY, float scale) {
		setButtonPositions();
	}
	
	protected void setButtonPositions() {
		for (QuestButton button : buttons.values()) {
			final int offsetX = button.getStartingX() - (width / 2);
			final int offsetY = button.getStartingY() - (height / 2);
			
			
			final int scaledX = (width/2) + (int) ((offsetX + this.getPanX()) * this.getPanScale());
			final int scaledY = (height/2) + (int) ((offsetY + this.getPanY()) * this.getPanScale());
			button.setPosition(scaledX, scaledY);
		}
	}
	
	protected void onButtonQuest(QuestButton button) {
		//if (ignoreButton(button)) return;
		
		// Quest button
		NostrumQuest quest = button.quest;
		
		if (button.state == QuestState.INACTIVE) {
			NetworkHandler.sendToServer(
				new ClientUpdateQuestMessage(quest)	
				);
		}
	}
	
	protected static enum QuestState {
		UNAVAILABLE,
		INACTIVE,
		COMPLETED
	}
	
	private static class QuestButton extends MoveableObscurableWidget {
		
		private final MirrorQuestSubscreen subscreen;
		private final NostrumQuest quest;
		
		private final List<Component> tooltip;
		private final float fontScale = 0.75f;
		
		private QuestState state;
		private SpellComponentIcon icon; // Icon to use as this icon
		private int iconU; // If icon is null, uv coords for our icon on the icon texture
		private int iconV;
		
		public QuestButton(MirrorQuestSubscreen subscreen, NostrumQuest quest, int x, int y, int width, int height) {
			super(x, y, width, height, TextComponent.EMPTY);
			this.subscreen = subscreen;
			this.quest = quest;
			this.tooltip = genTooltip();
			this.state = updateQuestState();
			getIcon();
		}
		
		protected QuestState updateQuestState() {
			if (NostrumMagica.getCompletedQuests(subscreen.attr).contains(quest))
				state = QuestState.COMPLETED;
			else if (NostrumMagica.canTakeQuest(subscreen.player, quest))
				state = QuestState.INACTIVE;
			else
				state = QuestState.UNAVAILABLE;
			
			return state;
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (!this.isHidden()) {
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
			subscreen.onButtonQuest(this);
			updateQuestState();
		}
		
		public void drawTreeLines(PoseStack matrixStackIn, Minecraft mc) {
			if (quest.getParentKeys() != null && quest.getParentKeys().length != 0) {
				for (String key : quest.getParentKeys()) {
					NostrumQuest quest = NostrumQuest.lookup(key);
					if (quest == null)
						continue;
					
					QuestButton other = subscreen.buttons.get(quest);
					if (other != null && !other.isHidden())
						renderLine(matrixStackIn, other);
				}
			}
		}
		
		private void renderLine(PoseStack matrixStackIn, QuestButton other) {
			matrixStackIn.pushPose();
//			GlStateManager.pushLightingAttributes();
			matrixStackIn.translate(width / 2, height / 2, 0);
			
			matrixStackIn.translate(1, .5, 0);
			
			final Matrix4f transform = matrixStackIn.last().pose();
			
			final float dx = other.x - x;
			final float dy = other.y - y;
			final float dd = Mth.sqrt(dx * dx + dy * dy);
			
			BufferBuilder buf = Tesselator.getInstance().getBuilder();
			RenderSystem.depthMask(true);
			RenderSystem.disableCull();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.disableTexture();
			RenderSystem.lineWidth(3f);
			//RenderSystem.disableDepthTest();
	        //GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//	        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 0.6f);
			RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
	        buf.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
	        buf.vertex(transform, x, y, 0).color(1f, 1f, 1f, .6f).normal(dx / dd, dy / dd, 0).endVertex();
	        buf.vertex(transform, other.x, other.y, 0).color(1f, 1f, 1f, .6f).normal(dx / dd, dy / dd, 0).endVertex();
	        Tesselator.getInstance().end();
	        RenderSystem.enableTexture();
//	        GlStateManager.enableTexture();
	        RenderSystem.disableBlend();
			RenderSystem.lineWidth(1f);
			
//	        GlStateManager.popAttributes();
	        matrixStackIn.popPose();
		}
		
		@Override
		public void render(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			updateQuestState();
			
			// Render tree lines even if we're out of bounds
			if (!this.isHidden()) {
				matrixStackIn.pushPose();
				matrixStackIn.translate(0, 0, 0);
				drawTreeLines(matrixStackIn, Minecraft.getInstance());
				matrixStackIn.translate(0, 0, 10);
				super.render(matrixStackIn, mouseX, mouseY, partialTicks);
				matrixStackIn.popPose();
			}
		}
		
		@Override
		public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			final int u;
			final int v;
			final int uw;
			final int vh;
			switch (quest.getType()) {
			case REGULAR:
			default:
				u = TEX_ICON_QUEST_HOFFSET;
				v = TEX_ICON_QUEST_VOFFSET;
				uw = TEX_ICON_QUEST_WIDTH;
				vh = TEX_ICON_QUEST_HEIGHT;
				break;
			case CHALLENGE:
				u = TEX_ICON_BUTTON_HOFFSET;
				v = TEX_ICON_BUTTON_VOFFSET;
				uw = TEX_ICON_BUTTON_WIDTH;
				vh = TEX_ICON_BUTTON_HEIGHT;
				break;
			}
			
			float[] color = {1, 1, 1, 1};
			switch (state) {
			case COMPLETED:
				color = new float[] {.2f, 2f/3f, .2f, 1f};
				break;
			case INACTIVE:
				color = new float[] {2f/3f, 0f, 2f/3f, 1f};
				break;
			case UNAVAILABLE:
				color = new float[] {.8f, .0f, .0f, 1f};
				break;
			}
			
			if (this.isHovered()) {
				color[0] *= .8f;
				color[1] *= .8f;
				color[2] *= .8f;
			}
			
			RenderSystem.enableBlend();
			RenderSystem.setShaderTexture(0, RES_ICONS);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, x, y,
					u, v, uw, vh, this.width, this.height, TEX_ICON_WIDTH, TEX_ICON_HEIGHT,
					color[0], color[1], color[2], color[3]);
			
			if (icon != null) {
				icon.draw(matrixStackIn, x + 2, y + 2, width-4, height-4); // Blend with color?
			} else {
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, x + 4, y + 4,
						iconU, iconV, TEX_ICON_REWARD_WIDTH, TEX_ICON_REWARD_HEIGHT, width-8, height-8, TEX_ICON_WIDTH, TEX_ICON_HEIGHT,
					1f, 1f, 1f, .8f);
			}
		}
		
		@Override
		public void renderToolTip(PoseStack matrixStackIn, int mouseX, int mouseY) {
			if (this.isHovered()) { 
				final Minecraft mc = Minecraft.getInstance();
				final Font font = mc.font;
				matrixStackIn.pushPose();
				matrixStackIn.scale(fontScale, fontScale, 1f);
				matrixStackIn.translate((int) (mouseX / fontScale) - mouseX, (int) (mouseY / fontScale) - mouseY, 0);
				mc.screen.renderTooltip(matrixStackIn, tooltip, Optional.empty(), mouseX, mouseY, font);
				matrixStackIn.popPose();
			}
		}
		
		private void getIcon() {
			icon = null;
			iconU = 0;
			iconV = 0;
			IReward reward = quest.getReward();
			if (reward == null)
				return;
			
			if (reward instanceof AlterationReward) {
				this.icon = SpellComponentIcon.get(((AlterationReward) reward).getAlteration());
			} else if (reward instanceof AttributeReward) {
				AwardType type = ((AttributeReward) reward).getType();
				switch (type) {
				case COST:
				default:
					iconU = TEX_ICON_REWARD_DISCOUNT_HOFFSET;
					iconV = TEX_ICON_REWARD_DISCOUNT_VOFFSET;
					break;
				case MANA:
					iconU = TEX_ICON_REWARD_MANA_HOFFSET;
					iconV = TEX_ICON_REWARD_MANA_VOFFSET;
					break;
				case REGEN:
					iconU = TEX_ICON_REWARD_REGEN_HOFFSET;
					iconV = TEX_ICON_REWARD_REGEN_VOFFSET;
					break;
				}
			}
		}
		
		private List<Component> genTooltip() {
			int maxWidth = 200; 
			List<Component> tooltip = new LinkedList<>();
			tooltip.add(new TranslatableComponent("quest." + quest.getKey() + ".name", new Object[0])
					.withStyle(ChatFormatting.BLUE));
			
			ChatFormatting bad = ChatFormatting.RED;
			
			// Requirements
	        if (quest.getRequirements() != null && quest.getRequirements().length > 0) {
	        	tooltip.add(new TranslatableComponent("info.requirement.missing"));
	        	for (IRequirement req : quest.getRequirements()) {
	        		ChatFormatting style = ChatFormatting.GRAY;
	        		if (!req.matches(subscreen.player)) {
	        			style = bad;
	        		}
        			for (Component line : req.getDescription(subscreen.player)) {
        				if (line instanceof BaseComponent) {
        					tooltip.add(((BaseComponent) line).withStyle(style));
        				} else {
        					tooltip.add(line);
        				}
        			}
	        	}
	        	tooltip.add(new TextComponent(""));
	        }
			
			if (quest.getReward() != null) {
				String desc = quest.getReward().getDescription();
				if (desc != null && !desc.isEmpty())
					tooltip.add(new TextComponent(desc).withStyle(ChatFormatting.GOLD));
			}
			
			if (this.state == QuestState.INACTIVE && NostrumMagica.canTakeQuest(subscreen.player, quest)) {
				tooltip.add(new TranslatableComponent("info.quest.accept").withStyle(ChatFormatting.GREEN));
			}
			
			if (this.state == QuestState.COMPLETED) {
				final Minecraft mc = Minecraft.getInstance();
				final Font font = mc.font;
	            for (Component line : tooltip) {
	            	int width = font.width(line);
	            	if (width > maxWidth)
	            		maxWidth = width;
	            }
	            
	            String desc = I18n.get("quest." + quest.getKey() + ".desc", new Object[0]);
	            if (desc != null && !desc.isEmpty()) {
	            	tooltip.add(new TextComponent(""));
	            	StringBuffer buf = new StringBuffer();
	            	int index = 0;
	            	while (index < desc.length()) {
	            		if (desc.charAt(index) == '|') {
	            			tooltip.add(new TextComponent(buf.toString()));
	            			buf = new StringBuffer();
	            		} else {
		            		int oldlen = font.width(buf.toString());
		            		if (oldlen + font.width("" + desc.charAt(index)) > maxWidth) {
		            			// Go back until we find a space
		            			boolean isSpace = desc.charAt(index) == ' ';
		            			if (!isSpace) {
		            				int last = buf.length() - 1;
		            				while (last > 0 && buf.charAt(last) != ' ')
		            					last--;
		            				
		            				if (last == 0) {
		            					// oh well
		            					tooltip.add(new TextComponent(buf.toString()));
		            					buf = new StringBuffer();
		            				} else {
		            					tooltip.add(new TextComponent(buf.substring(0, last)));
		            					StringBuffer oldbuf = buf;
		            					buf = new StringBuffer();
		            					buf.append(oldbuf.substring(last + 1));
		            				}
		            			} else {
		            				tooltip.add(new TextComponent(buf.toString()));
			            			buf = new StringBuffer();
		            				index++;
			            			continue; // Don't add it
		            			}
		            		}
	            		
	            			buf.append(desc.charAt(index));
	            		}
	            		index++;
	            	}
	            	if (buf.length() > 0)
	            		tooltip.add(new TextComponent(buf.toString()));
	            }
			}
			
			return tooltip;
		}
		
		public boolean isHidden() {
			return !ModConfig.config.displayAllMirrorQuestNodes() && !NostrumMagica.getQuestAvailable(subscreen.player, quest);
		}
	}
	
	private static final int TEX_BACK_WIDTH = 256;
	private static final int TEX_BACK_HEIGHT = 256;
	
	private static final int TEX_ICON_WIDTH = 64;
	private static final int TEX_ICON_HEIGHT = 64;
	
	private static final int TEX_ICON_BUTTON_HOFFSET = 0;
	private static final int TEX_ICON_BUTTON_VOFFSET = 32;
	private static final int TEX_ICON_BUTTON_WIDTH = 32;
	private static final int TEX_ICON_BUTTON_HEIGHT = 32;
	
	private static final int TEX_ICON_QUEST_HOFFSET = 32;
	private static final int TEX_ICON_QUEST_VOFFSET = 32;
	private static final int TEX_ICON_QUEST_WIDTH = 32;
	private static final int TEX_ICON_QUEST_HEIGHT = 32;
	
	private static final int TEX_ICON_REWARD_WIDTH = 16;
	private static final int TEX_ICON_REWARD_HEIGHT = 16;
	
	private static final int TEX_ICON_REWARD_DISCOUNT_HOFFSET = 32;
	private static final int TEX_ICON_REWARD_DISCOUNT_VOFFSET = 0;
	private static final int TEX_ICON_REWARD_MANA_HOFFSET = 48;
	private static final int TEX_ICON_REWARD_MANA_VOFFSET = 0;
	private static final int TEX_ICON_REWARD_REGEN_HOFFSET = 32;
	private static final int TEX_ICON_REWARD_REGEN_VOFFSET = 16;
	
	private static final int POS_BUTTON_WIDTH = 18;
	private static final int POS_BUTTON_HEIGHT = 18;
	
	private static final int GRID_SCALE = POS_BUTTON_WIDTH + 8;
//	private static final int TEXT_ICON_BUTTON_VOFFSET = 198;
//	private static final int TEXT_ICON_BUTTON_LENGTH = 16;
//	private static final int TEXT_ICON_QUEST_VOFFSET = 214;
//	private static final int TEXT_ICON_QUEST_LENGTH = 18;
//	private static final int TEXT_ICON_REWARD_OFFSET = 48;
//	private static final int TEXT_ICON_REWARD_WIDTH = 32;

}
