package com.smanzana.nostrummagica.client.gui.mirror;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
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
import com.smanzana.nostrummagica.progression.reward.IReward;
import com.smanzana.nostrummagica.progression.reward.AttributeReward.AwardType;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class MirrorQuestSubscreen extends PanningMirrorSubscreen {
	
	private static final ResourceLocation RES_ICONS = NostrumMagica.Loc("textures/gui/mirror_quest.png");
	private static final ResourceLocation RES_BACK = NostrumMagica.Loc("textures/gui/mirror_quest_back.png");
	
	private final ITextComponent name;
	private final ItemStack icon;
	private final Map<NostrumQuest, QuestButton> buttons;
	
	private PlayerEntity player;
	private INostrumMagic attr;
	private int width;
	private int height;
	
	public MirrorQuestSubscreen() {
		name = new TranslationTextComponent("mirror.tab.quest.name");
		icon = new ItemStack(Items.FILLED_MAP, 1);
		buttons = new HashMap<>();
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
		attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			return;
		}
		
		this.width = width;
		this.height = height;
		
		buttons.clear();
		final Rectangle2d bounds = new Rectangle2d(guiLeft, guiTop, width, height);
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
	public void hide(IMirrorScreen parent, PlayerEntity player) {
		; // Not sure there's much to do. Parent will clean things up for us
	}

	@Override
	public void drawBackground(IMirrorScreen parent, MatrixStack matrixStackIn, int width, int height, int mouseX, int mouseY, float partialTicks) {
		float extra = .1f * (float) Math.sin((double) System.currentTimeMillis() / 1500.0);
		float inv = .1f - extra;
		
		Minecraft.getInstance().getTextureManager().bindTexture(RES_BACK);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0,
				0, 0, TEX_BACK_WIDTH, TEX_BACK_HEIGHT,
				width, height, TEX_BACK_WIDTH, TEX_BACK_HEIGHT,
				.9f + extra, 1f, .8f + inv, 1f);
	}

	@Override
	public void drawForeground(IMirrorScreen parent, MatrixStack matrixStackIn, int width, int height, int mouseX, int mouseY, float partialTicks) {
		
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
		
		private final List<ITextComponent> tooltip;
		private final float fontScale = 0.75f;
		
		private QuestState state;
		private SpellComponentIcon icon; // Icon to use as this icon
		private int iconU; // If icon is null, uv coords for our icon on the icon texture
		private int iconV;
		
		public QuestButton(MirrorQuestSubscreen subscreen, NostrumQuest quest, int x, int y, int width, int height) {
			super(x, y, width, height, StringTextComponent.EMPTY);
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
		
		public void drawTreeLines(MatrixStack matrixStackIn, Minecraft mc) {
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
		
		private void renderLine(MatrixStack matrixStackIn, QuestButton other) {
			matrixStackIn.push();
//			GlStateManager.pushLightingAttributes();
			matrixStackIn.translate(width / 2, height / 2, 0);
			
			final Matrix4f transform = matrixStackIn.getLast().getMatrix();
			
			BufferBuilder buf = Tessellator.getInstance().getBuffer();
			RenderSystem.enableBlend();
			RenderSystem.disableTexture();
			RenderSystem.lineWidth(3f);
	        //GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//	        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 0.6f);
	        buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
	        buf.pos(transform, x, y, 0).color(1f, 1f, 1f, .6f).endVertex();
	        buf.pos(transform, other.x, other.y, 0).color(1f, 1f, 1f, .6f).endVertex();
	        Tessellator.getInstance().draw();
	        RenderSystem.enableTexture();
//	        GlStateManager.enableTexture();
	        RenderSystem.disableBlend();
			RenderSystem.lineWidth(1f);
			
//	        GlStateManager.popAttributes();
	        matrixStackIn.pop();
		}
		
		@Override
		public void render(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			updateQuestState();
			
			// Render tree lines even if we're out of bounds
			if (!this.isHidden()) {
				matrixStackIn.push();
				matrixStackIn.translate(0, 0, 0);
				drawTreeLines(matrixStackIn, Minecraft.getInstance());
				matrixStackIn.translate(0, 0, 1);
				super.render(matrixStackIn, mouseX, mouseY, partialTicks);
				matrixStackIn.pop();
			}
		}
		
		@Override
		public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
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
			Minecraft.getInstance().getTextureManager().bindTexture(RES_ICONS);
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
		public void renderToolTip(MatrixStack matrixStackIn, int mouseX, int mouseY) {
			if (this.isHovered()) { 
				final Minecraft mc = Minecraft.getInstance();
				final FontRenderer font = mc.fontRenderer;
				matrixStackIn.push();
				matrixStackIn.scale(fontScale, fontScale, 1f);
				matrixStackIn.translate((int) (mouseX / fontScale) - mouseX, (int) (mouseY / fontScale) - mouseY, 0);
				GuiUtils.drawHoveringText(matrixStackIn, tooltip, mouseX, mouseY, subscreen.width, subscreen.height, 400, font);
				matrixStackIn.pop();
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
		
		private List<ITextComponent> genTooltip() {
			int maxWidth = 200; 
			List<ITextComponent> tooltip = new LinkedList<>();
			tooltip.add(new TranslationTextComponent("quest." + quest.getKey() + ".name", new Object[0])
					.mergeStyle(TextFormatting.BLUE));
			
			TextFormatting bad = TextFormatting.RED;
			
			// Requirements
	        if (quest.getRequirements() != null && quest.getRequirements().length > 0) {
	        	tooltip.add(new TranslationTextComponent("info.requirement.missing"));
	        	for (IRequirement req : quest.getRequirements()) {
	        		TextFormatting style = TextFormatting.GRAY;
	        		if (!req.matches(subscreen.player)) {
	        			style = bad;
	        		}
        			for (ITextComponent line : req.getDescription(subscreen.player)) {
        				if (line instanceof TextComponent) {
        					tooltip.add(((TextComponent) line).mergeStyle(style));
        				} else {
        					tooltip.add(line);
        				}
        			}
	        	}
	        	tooltip.add(new StringTextComponent(""));
	        }
			
			if (quest.getReward() != null) {
				String desc = quest.getReward().getDescription();
				if (desc != null && !desc.isEmpty())
					tooltip.add(new StringTextComponent(desc).mergeStyle(TextFormatting.GOLD));
			}
			
			if (this.state == QuestState.INACTIVE && NostrumMagica.canTakeQuest(subscreen.player, quest)) {
				tooltip.add(new TranslationTextComponent("info.quest.accept").mergeStyle(TextFormatting.GREEN));
			}
			
			if (this.state == QuestState.COMPLETED) {
				final Minecraft mc = Minecraft.getInstance();
				final FontRenderer font = mc.fontRenderer;
	            for (ITextComponent line : tooltip) {
	            	int width = font.getStringPropertyWidth(line);
	            	if (width > maxWidth)
	            		maxWidth = width;
	            }
	            
	            String desc = I18n.format("quest." + quest.getKey() + ".desc", new Object[0]);
	            if (desc != null && !desc.isEmpty()) {
	            	tooltip.add(new StringTextComponent(""));
	            	StringBuffer buf = new StringBuffer();
	            	int index = 0;
	            	while (index < desc.length()) {
	            		if (desc.charAt(index) == '|') {
	            			tooltip.add(new StringTextComponent(buf.toString()));
	            			buf = new StringBuffer();
	            		} else {
		            		int oldlen = font.getStringWidth(buf.toString());
		            		if (oldlen + font.getStringWidth("" + desc.charAt(index)) > maxWidth) {
		            			// Go back until we find a space
		            			boolean isSpace = desc.charAt(index) == ' ';
		            			if (!isSpace) {
		            				int last = buf.length() - 1;
		            				while (last > 0 && buf.charAt(last) != ' ')
		            					last--;
		            				
		            				if (last == 0) {
		            					// oh well
		            					tooltip.add(new StringTextComponent(buf.toString()));
		            					buf = new StringBuffer();
		            				} else {
		            					tooltip.add(new StringTextComponent(buf.substring(0, last)));
		            					StringBuffer oldbuf = buf;
		            					buf = new StringBuffer();
		            					buf.append(oldbuf.substring(last + 1));
		            				}
		            			} else {
		            				tooltip.add(new StringTextComponent(buf.toString()));
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
	            		tooltip.add(new StringTextComponent(buf.toString()));
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
