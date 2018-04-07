package com.smanzana.nostrummagica.client.gui;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ClientSkillUpMessage;
import com.smanzana.nostrummagica.network.messages.ClientSkillUpMessage.Type;
import com.smanzana.nostrummagica.network.messages.ClientUpdateQuestMessage;
import com.smanzana.nostrummagica.quests.NostrumQuest;
import com.smanzana.nostrummagica.quests.rewards.AlterationReward;
import com.smanzana.nostrummagica.quests.rewards.AttributeReward;
import com.smanzana.nostrummagica.quests.rewards.AttributeReward.AwardType;
import com.smanzana.nostrummagica.quests.rewards.IReward;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class MirrorGui extends GuiScreen {
	
	private static final int TEXT_WIDTH = 300;
	private static final int TEXT_HEIGHT = 300;
	//private static final int GUI_HEIGHT = 242;
	private static final int TEXT_BOTTOM_HOFFSET = 25;
	private static final int TEXT_BOTTOM_VOFFSET = 170;
	private static final int TEXT_BOTTOM_WIDTH = 251;
	private static final int TEXT_CONTENT_HOFFSET = 16;
	private static final int TEXT_CONTENT_VOFFSET = 16;
	private static final int TEXT_CONTENT_WIDTH = 269;
	private static final int TEXT_CONTENT_HEIGHT = 151;
	private static final int TEXT_BUTTON_VOFFSET = 242;
	private static final int TEXT_BUTTON_LENGTH = 16;
	private static final int TEXT_QUEST_VOFFSET = 258;
	private static final int TEXT_QUEST_LENGTH = 18;
	private static final int TEXT_REWARD_OFFSET = 48;
	private static final int TEXT_REWARD_WIDTH = 32;
	
	private static final ResourceLocation RES_BACK_CLOUD = new ResourceLocation(
			NostrumMagica.MODID, "textures/gui/container/mirror_back_clouds.png");
	private static final ResourceLocation RES_BACK_CLEAR = new ResourceLocation(
			NostrumMagica.MODID, "textures/gui/container/mirror_back_clear.png");
	private static final ResourceLocation RES_FORE = new ResourceLocation(
			NostrumMagica.MODID, "textures/gui/container/mirror_foreground.png");
	
	private static final int KEY_WIDTH = 70;
	
	//private INostrumMagic attr;
	private EntityPlayer player;
	
	// Cache attributes. Don't be dumb
	private float xp, maxXP;
	private int technique;
	private int finesse;
	private int control;
	private int level;
	private int skillPoints;
	private boolean unlocked;
	private String unlockPrompt;
	
	
	private ImproveButton buttonControl;
	private ImproveButton buttonTechnique;
	private ImproveButton buttonFinesse;
	
	private static final int guiScale = TEXT_QUEST_LENGTH + 8;
	private int guiX;
	private int guiY;
	private int mouseClickX;
	private int mouseClickY;
	private int mouseClickXOffset; //xoffset at time of click
	private int mouseClickYOffset; //yoffset at time of click
	
	private Map<NostrumQuest, QuestButton> questButtons;
	private int buttonIDs;
	
	public MirrorGui(EntityPlayer player) {
		this.width = TEXT_WIDTH;
		//this.height = GUI_HEIGHT;
		this.height = 242;
		cacheAttributes(NostrumMagica.getMagicWrapper(player));
		this.player = player;
		questButtons = new HashMap<>();
		buttonIDs = 0;
	}
	
	private void cacheAttributes(INostrumMagic attr) {
		this.unlocked = attr.isUnlocked();
		if (!unlocked)
			unlockPrompt = getUnlockPrompt(attr);
		this.level = attr.getLevel();
		this.skillPoints = attr.getSkillPoints();
		this.xp = attr.getXP();
		this.maxXP = attr.getMaxXP();
		this.technique = attr.getTech();
		this.control = attr.getControl();
		this.finesse = attr.getFinesse();
	}
	
	@Override
	public void initGui() {
		super.initGui();
		
		int GUI_HEIGHT = 242;
		int KEY_HEIGHT = 15 + 5;
		int KEY_VOFFSET = 10;
		int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
		int topOffset = (this.height - GUI_HEIGHT) / 2;
		
		this.guiX = leftOffset + TEXT_CONTENT_HOFFSET + (int) ((float) TEXT_CONTENT_WIDTH / 2f);
		this.guiY = topOffset + TEXT_CONTENT_VOFFSET + (int) ((float) TEXT_CONTENT_HEIGHT / 2f);
		
		buttonControl = new ImproveButton(buttonIDs++, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (KEY_WIDTH + TEXT_BUTTON_LENGTH),
				topOffset + TEXT_BOTTOM_VOFFSET + KEY_VOFFSET);
		buttonTechnique = new ImproveButton(buttonIDs++, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (KEY_WIDTH + TEXT_BUTTON_LENGTH),
				topOffset + TEXT_BOTTOM_VOFFSET + KEY_VOFFSET + KEY_HEIGHT);
		buttonFinesse = new ImproveButton(buttonIDs++, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (KEY_WIDTH + TEXT_BUTTON_LENGTH),
				topOffset + TEXT_BOTTOM_VOFFSET + KEY_VOFFSET + KEY_HEIGHT + KEY_HEIGHT);
		
		refreshButtons();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		int GUI_HEIGHT = 242;
		int KEY_HEIGHT = 15;
		int KEY_VOFFSET = 9;
		int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
		int topOffset = (this.height - GUI_HEIGHT) / 2;
		
		if (unlocked)
			Minecraft.getMinecraft().getTextureManager().bindTexture(RES_BACK_CLEAR);
		else
			Minecraft.getMinecraft().getTextureManager().bindTexture(RES_BACK_CLOUD);
		GlStateManager.color(1f, 1f, 1f, 1f);
		Gui.drawScaledCustomSizeModalRect(leftOffset, topOffset, 0, 0, TEXT_WIDTH, GUI_HEIGHT, TEXT_WIDTH, GUI_HEIGHT, TEXT_WIDTH, GUI_HEIGHT);
		
		// CONTENT DRAWING
		if (unlocked) {
			for (int i = 0; i < this.buttonList.size(); ++i) {
				GuiButton button = (GuiButton)this.buttonList.get(i);
				if (button != buttonControl
						&& button != buttonTechnique
						&& button != buttonFinesse)
					((QuestButton) button).drawTreeLines(mc);
			}
			for (int i = 0; i < this.buttonList.size(); ++i) {
				GuiButton button = (GuiButton)this.buttonList.get(i);
				if (button != buttonControl
						&& button != buttonTechnique
						&& button != buttonFinesse)
					button.drawButton(this.mc, mouseX, mouseY);
			}
			for (int i = 0; i < this.buttonList.size(); ++i) {
				GuiButton button = (GuiButton)this.buttonList.get(i);
				if (button != buttonControl
						&& button != buttonTechnique
						&& button != buttonFinesse)
					((QuestButton) button).drawOverlay(mc, mouseX, mouseY);
			}
		} else {
			int y = 0;
			String str = "Magic Not Yet Unlocked";
			int len = this.fontRendererObj.getStringWidth(str);
			this.fontRendererObj.drawString(str, (this.width - len) / 2, topOffset + (TEXT_CONTENT_HEIGHT / 2), 0xFFFFFFFF, true);
			
			y = fontRendererObj.FONT_HEIGHT + 2;
			
			len = this.fontRendererObj.getStringWidth(unlockPrompt);
			this.fontRendererObj.drawString(unlockPrompt, (this.width - len) / 2, y + topOffset + (TEXT_CONTENT_HEIGHT / 2), 0xFFDFD000, false);
		}
		
		// Black out surrounding screen
		int color = 0xFF000000;
		Gui.drawRect(0, 0, this.width, topOffset, color);
		Gui.drawRect(0, topOffset + GUI_HEIGHT, this.width, this.height, color);
		Gui.drawRect(0, 0, leftOffset, this.height, color);
		Gui.drawRect(leftOffset + TEXT_WIDTH - 1, 0, this.width, this.height, color);
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(RES_FORE);
		GlStateManager.color(1f, 1f, 1f, 1f);
		GlStateManager.disableBlend();
		GlStateManager.disableLighting();
		Gui.drawScaledCustomSizeModalRect(leftOffset, topOffset, 0, 0, TEXT_WIDTH, GUI_HEIGHT, TEXT_WIDTH, GUI_HEIGHT, TEXT_WIDTH, TEXT_HEIGHT);
		
		// TEXT DRAWING
		if (unlocked) {
			// DRAW STATS
			int y = 2;
			int len;
			int colorKey = 0xFF0A8E0A;
			int colorVal = 0xFFE4E5D5;
			String str;
			
			str = "Level " + level;
			len = fontRendererObj.getStringWidth(str);
			this.fontRendererObj.drawString(str, (this.width - len) / 2, topOffset + TEXT_BOTTOM_VOFFSET, 0xFFFFFFFF, true);
			y += fontRendererObj.FONT_HEIGHT + 10;
			int yTop = y = KEY_VOFFSET + topOffset + TEXT_BOTTOM_VOFFSET;
			
			//leftOffset + TEXT_BOTTOM_HOFFSET, y + topOffset + TEXT_BOTTOM_VOFFSET, colorKey
			// XP, points
			Gui.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET - 2, y, leftOffset + TEXT_BOTTOM_HOFFSET + KEY_WIDTH + 2, y + KEY_HEIGHT, 0xD0000000);
			str = "XP: ";
			len = fontRendererObj.getStringWidth(String.format("%.02f%%", 100f * xp/maxXP));
			this.fontRendererObj.drawString(str, leftOffset + TEXT_BOTTOM_HOFFSET, y + (KEY_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2 + 1, colorKey);
			this.fontRendererObj.drawString(String.format("%.02f%%", 100f * xp/maxXP), leftOffset + TEXT_BOTTOM_HOFFSET + KEY_WIDTH - (len), y + (KEY_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2 + 1, colorVal);
			y += KEY_HEIGHT + 5;
			
//			Gui.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET - 2, topOffset + TEXT_BOTTOM_VOFFSET + y - 2, leftOffset + TEXT_BOTTOM_HOFFSET + keyWidth + 2, topOffset + TEXT_BOTTOM_VOFFSET + y + this.fontRendererObj.FONT_HEIGHT, 0xD0000000);
//			str = "Technique: ";
//			len = fontRendererObj.getStringWidth("" + attr.getTech());
//			this.fontRendererObj.drawString(str, leftOffset + TEXT_BOTTOM_HOFFSET, y + topOffset + TEXT_BOTTOM_VOFFSET, colorKey);
//			this.fontRendererObj.drawString("" + attr.getTech(), leftOffset + TEXT_BOTTOM_HOFFSET + keyWidth - (len), y + topOffset + TEXT_BOTTOM_VOFFSET, colorVal);
			y += KEY_HEIGHT + 5;
			
			Gui.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET - 2, y, leftOffset + TEXT_BOTTOM_HOFFSET + KEY_WIDTH + 2, y + KEY_HEIGHT, 0xD0000000);
			str = "Skill Points: ";
			len = fontRendererObj.getStringWidth("" + skillPoints);
			this.fontRendererObj.drawString(str, leftOffset + TEXT_BOTTOM_HOFFSET, y + (KEY_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2 + 1, colorKey);
			this.fontRendererObj.drawString("" + skillPoints, leftOffset + TEXT_BOTTOM_HOFFSET + KEY_WIDTH - (len), y + (KEY_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2 + 1, colorVal);
			y += KEY_HEIGHT + 5;
			
			// stats
			y = yTop;
			Gui.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (2 + KEY_WIDTH), y, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH + 2, y + KEY_HEIGHT, 0xD0000000);
			str = "Control: ";
			len = fontRendererObj.getStringWidth("" + control);
			this.fontRendererObj.drawString(str, leftOffset + TEXT_BOTTOM_WIDTH + TEXT_BOTTOM_HOFFSET - (KEY_WIDTH), y + (KEY_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2 + 1, colorKey);
			this.fontRendererObj.drawString("" + control, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (len), y + (KEY_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2 + 1, colorVal);
			y += KEY_HEIGHT + 5;
			
			Gui.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (2 + KEY_WIDTH), y, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH + 2, y + KEY_HEIGHT, 0xD0000000);
			str = "Technique: ";
			len = fontRendererObj.getStringWidth("" + technique);
			this.fontRendererObj.drawString(str, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - KEY_WIDTH, y + (KEY_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2 + 1, colorKey);
			this.fontRendererObj.drawString("" + technique, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (len), y + (KEY_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2 + 1, colorVal);
			y += KEY_HEIGHT + 5;
			
			Gui.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (2 + KEY_WIDTH), y, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH + 2, y + KEY_HEIGHT, 0xD0000000);
			str = "Finess: ";
			len = fontRendererObj.getStringWidth("" + finesse);
			this.fontRendererObj.drawString(str, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - KEY_WIDTH, y + (KEY_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2 + 1, colorKey);
			this.fontRendererObj.drawString("" + finesse, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (len), y + (KEY_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2 + 1, colorVal);
			y += KEY_HEIGHT + 5;
			
			buttonControl.drawButton(mc, mouseX, mouseY);
			buttonTechnique.drawButton(mc, mouseX, mouseY);
			buttonFinesse.drawButton(mc, mouseX, mouseY);
			
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			int mana = attr.getMana();
			int maxMana = attr.getMaxMana();
			float bonusMana = attr.getManaModifier();
			float bonusManaRegen = attr.getManaRegenModifier();
			float bonusManaCost = attr.getManaCostModifier();
			int fh = fontRendererObj.FONT_HEIGHT;
			this.fontRendererObj.drawString("Mana:",
					leftOffset + TEXT_WIDTH, topOffset + 5, 0xFFFFFFFF);
			this.fontRendererObj.drawString(" " + mana + "/" + maxMana,
					leftOffset + TEXT_WIDTH, topOffset + 5 + fh, 0xFFFFFFFF);
			
			this.fontRendererObj.drawString("Bonus Mana:",
					leftOffset + TEXT_WIDTH, topOffset + 5 + fh * 4, 0xFFFFFFFF);
			this.fontRendererObj.drawString(String.format("%+.1f%%", bonusMana * 100f),
					leftOffset + TEXT_WIDTH, topOffset + 5 + fh * 5, 0xFFFFFFFF);

			this.fontRendererObj.drawString("Mana Regen:",
					leftOffset + TEXT_WIDTH, topOffset + 5 + fh * 6, 0xFFFFFFFF);
			this.fontRendererObj.drawString(String.format("%+.1f%%", bonusManaRegen * 100f),
					leftOffset + TEXT_WIDTH, topOffset + 5 + fh * 7, 0xFFFFFFFF);

			this.fontRendererObj.drawString("Mana Cost:",
					leftOffset + TEXT_WIDTH, topOffset + 5 + fh * 8, 0xFFFFFFFF);
			this.fontRendererObj.drawString(String.format("%+.1f%%", bonusManaCost * 100f),
					leftOffset + TEXT_WIDTH, topOffset + 5 + fh * 9, 0xFFFFFFFF);
			
		} else {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			// DRAW ICONS
			EMagicElement element = null; // Which element we know
			SpellTrigger trigger = null;
			SpellShape shape = null;
			
			Map<EMagicElement, Boolean> map = attr.getKnownElements();
			Boolean val;
			for (EMagicElement elem : map.keySet()) {
				val = map.get(elem);
				if (val != null && val) {
					element = elem;
					break;
				}
			}
			
			if (!attr.getTriggers().isEmpty())
				trigger = attr.getTriggers().get(0);
			
			if (!attr.getShapes().isEmpty())
				shape = attr.getShapes().get(0);
			
			final int width = 24;
			final int space = 32;
			final long cycle = 1500;
			int x = leftOffset + (int) (.5 * TEXT_WIDTH) + (-width / 2) + (-space) + (-width);
			int y = topOffset + TEXT_BOTTOM_VOFFSET + width;
			int strLen;
			String str;

			GlStateManager.enableBlend();
			drawRect(x - 2, y - 2, x + width + 2, y + width + 2, 0xA0000000);
			if (element != null)
				GlStateManager.color(1f, 1f, 1f, 1f);
			else {
				GlStateManager.color(.8f, .5f, .5f, .5f);
				element = EMagicElement.values()[
	                  (int) (Minecraft.getSystemTime() / cycle) % EMagicElement.values().length
				      ];
			}
			SpellIcon.get(element).draw(this, this.fontRendererObj, x, y, width, width);
			str = I18n.format("element.name", new Object[0]);
			strLen = this.fontRendererObj.getStringWidth(str);
			this.fontRendererObj.drawString(str, (x + width / 2) - strLen/2, y - (3 + this.fontRendererObj.FONT_HEIGHT), 0xFFFFFF);
			
			x += width + space;
			drawRect(x - 2, y - 2, x + width + 2, y + width + 2, 0xA0000000);
			if (trigger != null)
				GlStateManager.color(1f, 1f, 1f, 1f);
			else {
				GlStateManager.color(.8f, .5f, .5f, .5f);
				Collection<SpellTrigger> triggers = SpellTrigger.getAllTriggers();
				SpellTrigger[] trigArray = triggers.toArray(new SpellTrigger[0]);
				trigger = trigArray[
	                  (int) (Minecraft.getSystemTime() / cycle) % trigArray.length
				      ];
			}
			SpellIcon.get(trigger).draw(this, this.fontRendererObj, x, y, width, width);
			str = I18n.format("trigger.name", new Object[0]);
			strLen = this.fontRendererObj.getStringWidth(str);
			this.fontRendererObj.drawString(str, (x + width / 2) - strLen/2, y - (3 + this.fontRendererObj.FONT_HEIGHT), 0xFFFFFF);
			
			x += width + space;
			drawRect(x - 2, y - 2, x + width + 2, y + width + 2, 0xA0000000);
			if (shape != null)
				GlStateManager.color(1f, 1f, 1f, 1f);
			else {
				GlStateManager.color(.8f, .5f, .5f, .5f);
				Collection<SpellShape> shapes = SpellShape.getAllShapes();
				SpellShape[] shapeArray = shapes.toArray(new SpellShape[0]);
				shape = shapeArray[
	                  (int) (Minecraft.getSystemTime() / cycle) % shapeArray.length
				      ];
			}
			SpellIcon.get(shape).draw(this, this.fontRendererObj, x, y, width, width);
			str = I18n.format("shape.name", new Object[0]);
			strLen = this.fontRendererObj.getStringWidth(str);
			this.fontRendererObj.drawString(str, (x + width / 2) - strLen/2, y - (3 + this.fontRendererObj.FONT_HEIGHT), 0xFFFFFF);
		}
		
		//super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	private static String getUnlockPrompt(INostrumMagic attr) {
		if (attr.isUnlocked())
			return "";
		
		Map<EMagicElement, Boolean> map = attr.getKnownElements();
		boolean found = false;
		for (EMagicElement elem : EMagicElement.values()) {
			if (map.get(elem) != null && map.get(elem)) {
				found = true;
				break;
			}
		}
		if (!found)
			return "Unlock an element";
		
		if (attr.getTriggers().isEmpty())
			return "Unlock at least one trigger";
		
		return "Unlock at least one shape";
	}
	
	@Override
	public void actionPerformed(GuiButton button) {
		if (!button.visible)
			return;
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		
		if (button == this.buttonControl) {
			attr.takeSkillPoint(); // take a local point so our update makes sense
			NetworkHandler.getSyncChannel().sendToServer(
					new ClientSkillUpMessage(Type.CONTROL)
					);
		} else if (button == this.buttonFinesse) {
			attr.takeSkillPoint(); // take a local point so our update makes sense
			NetworkHandler.getSyncChannel().sendToServer(
					new ClientSkillUpMessage(Type.FINESSE)
					);
		} else if (button == this.buttonTechnique) {
			attr.takeSkillPoint(); // take a local point so our update makes sense
			NetworkHandler.getSyncChannel().sendToServer(
					new ClientSkillUpMessage(Type.TECHNIQUE)
					);
		} else {
			// Quest button
			QuestButton qb = (QuestButton) button;
			NostrumQuest quest = qb.quest;
			
			if (qb.state == QuestState.INACTIVE || qb.state == QuestState.TAKEN) {
				NetworkHandler.getSyncChannel().sendToServer(
					new ClientUpdateQuestMessage(quest)	
					);
			}
		}
		
		refreshButtons();
	}
	
	private void refreshButtons() {
		if (skillPoints == 0) {
			buttonControl.visible
				= buttonTechnique.visible
				= buttonFinesse.visible
				= false;
		} else {
			buttonControl.visible
			= buttonTechnique.visible
			= buttonFinesse.visible
			= true;
		}
		
		this.buttonList.clear();
		questButtons.clear();
		this.addButton(buttonControl);
		this.addButton(buttonTechnique);
		this.addButton(buttonFinesse);
		
		for (NostrumQuest quest : NostrumQuest.allQuests()) {
			if (!NostrumMagica.getQuestAvailable(player, quest))
				continue;
			
			QuestState state;
			if (NostrumMagica.getCompletedQuests(player).contains(quest))
				state = QuestState.COMPLETED;
			else if (NostrumMagica.getActiveQuests(player).contains(quest))
				state = QuestState.TAKEN;
			else if (canTake(quest))
				state = QuestState.INACTIVE;
			else
				state = QuestState.UNAVAILABLE;
			
			QuestButton button = new QuestButton(buttonIDs++,
					quest.getPlotX(), quest.getPlotY(),
					quest, state);
			this.addButton(button);
			questButtons.put(quest, button);
		}
	}
	
	private boolean canTake(NostrumQuest quest) {
		return quest.getReqLevel() <= level
				&& quest.getReqControl() <= control
				&& quest.getReqTechnique() <= technique
				&& quest.getReqFinesse() <= finesse;
	}
	
	/**
	 * Refresh state from attributes, including re-evaluating quest state
	 */
	public void refresh() {
		this.cacheAttributes(NostrumMagica.getMagicWrapper(player));
		refreshButtons();
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		mouseClickX = mouseX;
		mouseClickY = mouseY;
		mouseClickXOffset = guiX;
		mouseClickYOffset = guiY;
		
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		guiX = mouseClickXOffset - (mouseClickX - mouseX);
		guiY = mouseClickYOffset - (mouseClickY - mouseY);
		
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}
	
    static class ImproveButton extends GuiButton {
		
		public ImproveButton(int parButtonId, int parPosX, int parPosY) {
			super(parButtonId, parPosX, parPosY, 12, 12, "");
		}
		
		@Override
        public void drawButton(Minecraft mc, int parX, int parY) {
			if (visible) {
				int textureX = 0;
				int textureY = TEXT_BUTTON_VOFFSET;
            	if (parX >= xPosition 
                  && parY >= yPosition 
                  && parX < xPosition + width 
                  && parY < yPosition + height) {
            		textureX += TEXT_BUTTON_LENGTH;
            	}
                
            	GlStateManager.color(1f, 1f, 1f, 1f);
                mc.getTextureManager().bindTexture(RES_FORE);
                Gui.drawScaledCustomSizeModalRect(xPosition, yPosition, textureX, textureY,
        				TEXT_BUTTON_LENGTH, TEXT_BUTTON_LENGTH, this.width, this.height, TEXT_WIDTH, TEXT_HEIGHT);
            }
        }
	}
    
    protected static enum QuestState {
		UNAVAILABLE,
		INACTIVE,
		TAKEN,
		COMPLETED
	}
	
    class QuestButton extends GuiButton {
    	
    	private NostrumQuest quest;
    	private QuestState state;
    	private int offsetX;
    	private int offsetY;
    	
    	private List<String> tooltip;
    	private boolean mouseOver;
    	private final float fontScale = 0.75f;
    	private boolean canTurnin;
    	private SpellIcon icon; // Icon to use as this ocon
    	private int iconOffset; // If icon is null, offset for our icon on the main texture
		
		public QuestButton(int parButtonId, int parPosX, int parPosY,
				NostrumQuest quest, QuestState state) {
			super(parButtonId, parPosX, parPosY, TEXT_QUEST_LENGTH, TEXT_QUEST_LENGTH, "");
			this.quest = quest;
			this.state = state;
			this.offsetX = parPosX;
			this.offsetY = parPosY;
			genTooltip();
			canTurnin = false;
			if (state == QuestState.TAKEN) {
				if (quest.getObjective().isComplete(NostrumMagica.getMagicWrapper(player)))
					canTurnin = true;
			}
			getIcon();
		}
		
		public void drawTreeLines(Minecraft mc) {
			if (quest.getParentKeys() != null && quest.getParentKeys().length != 0) {
				for (String key : quest.getParentKeys()) {
					NostrumQuest quest = NostrumQuest.lookup(key);
					if (quest == null)
						continue;
					
					QuestButton other = questButtons.get(quest);
					if (other != null)
						renderLine(other);
				}
			}
		}
		
		private void renderLine(QuestButton other) {
			GlStateManager.pushMatrix();
			GlStateManager.pushAttrib();
			GlStateManager.translate(width / 2, height / 2, 0);
			VertexBuffer buf = Tessellator.getInstance().getBuffer();
			//GlStateManager.enableBlend();
	        GlStateManager.disableTexture2D();
	        //GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
	        GlStateManager.color(1.0f, 1.0f, 1.0f, 0.6f);
	        buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
	        buf.pos(xPosition, yPosition, 0).endVertex();
	        buf.pos(other.xPosition, other.yPosition, 0).endVertex();
	        Tessellator.getInstance().draw();
	        GlStateManager.enableTexture2D();
	        //GlStateManager.disableBlend();
			
	        GlStateManager.popAttrib();
			GlStateManager.popMatrix();
		}
		
		@Override
        public void drawButton(Minecraft mc, int parX, int parY) {
			mouseOver = false;
			if (visible) {
				int textureX = 0;
				int textureY = TEXT_QUEST_VOFFSET;
				
				xPosition = (this.offsetX * guiScale) + guiX;
				yPosition = (this.offsetY * guiScale) + guiY;
				
				xPosition -= this.width / 2;
				yPosition -= this.height / 2;
				
            	if (parX >= xPosition 
                  && parY >= yPosition 
                  && parX < xPosition + width 
                  && parY < yPosition + height) {
            		textureY += TEXT_QUEST_LENGTH;
            		mouseOver = true;
            	}
            	textureX += TEXT_QUEST_LENGTH * quest.getType().ordinal();
                
            	switch (state) {
				case COMPLETED:
					GlStateManager.color(.2f, 2f/3f, .2f, 1f);
					break;
				case INACTIVE:
					GlStateManager.color(2f/3f, 0f, 2f/3f, .8f);
					break;
				case TAKEN: {
					float amt = 0f;
					if (canTurnin) {
						amt = (float) Math.sin(2.0 * Math.PI * (double) (Minecraft.getSystemTime() % 1000) / 1000.0);
						amt *= .1f;
					}
					GlStateManager.color(1f/3f + amt, .2f + amt, 2f/3f + amt, 1f);
					break;
				}
				case UNAVAILABLE:
					GlStateManager.color(.8f, .0f, .0f, .6f);
					break;
            	}
                
            	GlStateManager.disableLighting();
            	GlStateManager.disableBlend();
                mc.getTextureManager().bindTexture(RES_FORE);
                Gui.drawScaledCustomSizeModalRect(xPosition, yPosition, textureX, textureY,
                		TEXT_QUEST_LENGTH, TEXT_QUEST_LENGTH, this.width, this.height, TEXT_WIDTH, TEXT_HEIGHT);
                
                if (icon != null) {
                	icon.draw(this, fontRendererObj, xPosition + 2, yPosition + 3, 12, 12);
                } else {
                	GlStateManager.enableBlend();
                	GlStateManager.color(1f, 1f, 1f, .8f);
                	Gui.drawScaledCustomSizeModalRect(xPosition + 4, yPosition + 5, iconOffset, TEXT_BUTTON_VOFFSET,
                		TEXT_REWARD_WIDTH, TEXT_REWARD_WIDTH, 8, 8, TEXT_WIDTH, TEXT_HEIGHT);
                }
            }
        }
		
		public void drawOverlay(Minecraft mc, int parX, int parY) {
			if (mouseOver) {
				GlStateManager.pushMatrix();
				GlStateManager.scale(fontScale, fontScale, 1f);
				GlStateManager.translate((int) (parX / fontScale) - parX, (int) (parY / fontScale) - parY, 0);
				drawHoveringText(tooltip, parX, parY);
				GlStateManager.popMatrix();
			}
		}
		
		private void getIcon() {
			icon = null;
			iconOffset = -1;
			IReward reward = quest.getRewards()[0];
			if (reward == null)
				return;
			
			if (reward instanceof AlterationReward) {
				this.icon = SpellIcon.get(((AlterationReward) reward).getAlteration());
			} else if (reward instanceof AttributeReward) {
				AwardType type = ((AttributeReward) reward).getType();
				iconOffset = TEXT_REWARD_OFFSET + (type.ordinal() * TEXT_REWARD_WIDTH);
			}
		}
		
		private List<String> genTooltip() {
			int maxWidth = 100; 
			tooltip = new LinkedList<>();
			String title = I18n.format("quest." + quest.getKey() + ".name", new Object[0]);
			title = TextFormatting.BLUE + title + TextFormatting.RESET;
            tooltip.add(title);
            
            TextFormatting bad = TextFormatting.RED;
            TextFormatting good = TextFormatting.GREEN;
            if (quest.getReqLevel() > 0)
            	tooltip.add("" + (level >= quest.getReqLevel() ? good : bad)
            			+ "Level: " + quest.getReqLevel() + TextFormatting.RESET);
            if (quest.getReqControl() > 0)
            	tooltip.add("" + (control >= quest.getReqControl() ? good : bad)
            			+ "Control: " + quest.getReqControl() + TextFormatting.RESET);
            if (quest.getReqTechnique() > 0)
            	tooltip.add("" + (technique >= quest.getReqTechnique() ? good : bad)
            			+ "Technique: " + quest.getReqTechnique() + TextFormatting.RESET);
            if (quest.getReqFinesse() > 0)
            	tooltip.add("" + (finesse >= quest.getReqFinesse() ? good : bad)
            			+ "Finesse: " + quest.getReqFinesse() + TextFormatting.RESET);
            
            if (quest.getObjective() != null) {
            	tooltip.add(quest.getObjective().getDescription());
            }
            
            if (quest.getRewards() != null && quest.getRewards().length != 0)
            for (IReward reward : quest.getRewards()) {
            	String desc = reward.getDescription();
            	if (desc != null && !desc.isEmpty())
            		tooltip.add(TextFormatting.GOLD + desc + TextFormatting.RESET);
            }
            
            if (this.state == QuestState.INACTIVE && canTake(quest)) {
            	tooltip.add(TextFormatting.GREEN + "Click to Accept" + TextFormatting.RESET);
            }
            
            for (String line : tooltip) {
            	int width = fontRendererObj.getStringWidth(line);
            	if (width > maxWidth)
            		maxWidth = width;
            }
            
            String desc = I18n.format("quest." + quest.getKey() + ".desc", new Object[0]);
            if (desc != null && !desc.isEmpty()) {
            	tooltip.add("");
            	StringBuffer buf = new StringBuffer();
            	int index = 0;
            	while (index < desc.length()) {
            		if (desc.charAt(index) == '|') {
            			tooltip.add(buf.toString());
            			buf = new StringBuffer();
            		} else {
	            		int oldlen = fontRendererObj.getStringWidth(buf.toString());
	            		if (oldlen + fontRendererObj.getCharWidth(desc.charAt(index)) > maxWidth) {
	            			// Go back until we find a space
	            			boolean isSpace = desc.charAt(index) == ' ';
	            			if (!isSpace) {
	            				int last = buf.length() - 1;
	            				while (last > 0 && buf.charAt(last) != ' ')
	            					last--;
	            				
	            				if (last == 0) {
	            					// oh well
	            					tooltip.add(buf.toString());
	            					buf = new StringBuffer();
	            				} else {
	            					tooltip.add(buf.substring(0, last));
	            					StringBuffer oldbuf = buf;
	            					buf = new StringBuffer();
	            					buf.append(oldbuf.substring(last + 1));
	            				}
	            			} else {
	            				tooltip.add(buf.toString());
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
            		tooltip.add(buf.toString());
            }
            
            return tooltip;
		}
	}
}
