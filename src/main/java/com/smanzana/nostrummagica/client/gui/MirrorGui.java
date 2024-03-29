package com.smanzana.nostrummagica.client.gui;

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
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagic;
import com.smanzana.nostrummagica.client.gui.book.BookScreen;
import com.smanzana.nostrummagica.client.gui.book.IBookPage;
import com.smanzana.nostrummagica.client.gui.book.ReferencePage;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ClientPurchaseResearchMessage;
import com.smanzana.nostrummagica.network.messages.ClientSkillUpMessage;
import com.smanzana.nostrummagica.network.messages.ClientSkillUpMessage.Type;
import com.smanzana.nostrummagica.network.messages.ClientUpdateQuestMessage;
import com.smanzana.nostrummagica.quests.NostrumQuest;
import com.smanzana.nostrummagica.quests.rewards.AlterationReward;
import com.smanzana.nostrummagica.quests.rewards.AttributeReward;
import com.smanzana.nostrummagica.quests.rewards.AttributeReward.AwardType;
import com.smanzana.nostrummagica.quests.rewards.IReward;
import com.smanzana.nostrummagica.quests.rewards.TriggerReward;
import com.smanzana.nostrummagica.research.NostrumResearch;
import com.smanzana.nostrummagica.research.NostrumResearch.NostrumResearchTab;
import com.smanzana.nostrummagica.research.NostrumResearch.Size;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.utils.Curves;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class MirrorGui extends Screen {
	
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
	private static final int TEXT_ICON_BUTTON_VOFFSET = 198;
	private static final int TEXT_ICON_BUTTON_LENGTH = 16;
	private static final int TEXT_ICON_QUEST_VOFFSET = 214;
	private static final int TEXT_ICON_QUEST_LENGTH = 18;
	private static final int TEXT_ICON_REWARD_OFFSET = 48;
	private static final int TEXT_ICON_REWARD_WIDTH = 32;
	private static final int TEXT_ICON_MAJORBUTTON_HOFFSET = 0;
	private static final int TEXT_ICON_MAJORBUTTON_VOFFSET = 0;
	private static final int TEXT_ICON_MAJORBUTTON_WIDTH = 49;
	private static final int TEXT_ICON_MAJORBUTTON_HEIGHT = 43;
	private static final int TEXT_ICON_MINORBUTTON_HOFFSET = 101;
	private static final int TEXT_ICON_MINORBUTTON_VOFFSET = 0;
	private static final int TEXT_ICON_MINORBUTTON_WIDTH = 35;
	private static final int TEXT_ICON_MINORBUTTON_HEIGHT = 28;
	private static final int TEXT_ICON_RESEARCHBUTTON_SMALL_HOFFSET = 0;
	private static final int TEXT_ICON_RESEARCHBUTTON_SMALL_VOFFSET = 86;
	private static final int TEXT_ICON_RESEARCHBUTTON_SMALL_WIDTH = 24;
	private static final int TEXT_ICON_RESEARCHBUTTON_SMALL_HEIGHT = 24;
	private static final int TEXT_ICON_RESEARCHBUTTON_LARGE_HOFFSET = 24;
	private static final int TEXT_ICON_RESEARCHBUTTON_LARGE_VOFFSET = 86;
	private static final int TEXT_ICON_RESEARCHBUTTON_LARGE_WIDTH = 32;
	private static final int TEXT_ICON_RESEARCHBUTTON_LARGE_HEIGHT = 32;
	private static final int TEXT_ICON_RESEARCHBUTTON_GIANT_HOFFSET = 56;
	private static final int TEXT_ICON_RESEARCHBUTTON_GIANT_VOFFSET = 86;
	private static final int TEXT_ICON_RESEARCHBUTTON_GIANT_WIDTH = 46;
	private static final int TEXT_ICON_RESEARCHBUTTON_GIANT_HEIGHT = 46;
	private static final int TEXT_ICON_ARROW_HOFFSET = 0;
	private static final int TEXT_ICON_ARROW_VOFFSET = 189;
	private static final int TEXT_ICON_ARROW_WIDTH = 13;
	private static final int TEXT_ICON_ARROW_HEIGHT = 9;
	
	private static final int BUTTON_QUEST_WIDTH = 18;
	private static final int BUTTON_QUEST_HEIGHT = 18;
	private static final int BUTTON_MAJOR_WIDTH = 40;
	private static final int BUTTON_MAJOR_HEIGHT = 43;
	private static final int BUTTON_MINOR_WIDTH = 35;
	private static final int BUTTON_MINOR_HEIGHT = 28;
	private static final int BUTTON_RESEARCH_SMALL_WIDTH = 24;
	private static final int BUTTON_RESEARCH_SMALL_HEIGHT = 24;
	private static final int BUTTON_RESEARCH_LARGE_WIDTH = 28;
	private static final int BUTTON_RESEARCH_LARGE_HEIGHT = 28;
	private static final int BUTTON_RESEARCH_GIANT_WIDTH = 40;
	private static final int BUTTON_RESEARCH_GIANT_HEIGHT = 40;
	
	private static final ResourceLocation RES_BACK_CLOUD = new ResourceLocation(
			NostrumMagica.MODID, "textures/gui/container/mirror_back_clouds.png");
	private static final ResourceLocation RES_BACK_CLEAR = new ResourceLocation(
			NostrumMagica.MODID, "textures/gui/container/mirror_back_clear.png");
	private static final ResourceLocation RES_FORE = new ResourceLocation(
			NostrumMagica.MODID, "textures/gui/container/mirror_foreground.png");
	private static final ResourceLocation RES_ICONS = new ResourceLocation(
			NostrumMagica.MODID, "textures/gui/container/mirror_icons.png");
	
	private static final int KEY_WIDTH = 70;
	private final static float fontScale = 0.75f;
	
	//private INostrumMagic attr;
	private PlayerEntity player;
	
	// Cache attributes. Don't be dumb
	private float xp, maxXP;
	private int technique;
	private int finesse;
	private int control;
	private int level;
	private int skillPoints;
	private int researchPoints;
	private List<ILoreTagged> lore;
	private List<String> questsCompleted;
	private int knowledge;
	private int maxKnowledge;
	private boolean unlocked;
	private String unlockPrompt;
	
	private static boolean isCharacter = true; // static so we go back to the last one the next time you open it up :)
	private static NostrumResearchTab currentTab = NostrumResearchTab.MAGICA;
	
	private static Set<NostrumResearch> newResearch = null;
	private static Set<NostrumResearch> seenResearch = null;
	
	private ImproveButton buttonControl;
	private ImproveButton buttonTechnique;
	private ImproveButton buttonFinesse;
	private MajorTabButton tabCharacter;
	private MajorTabButton tabResearch;
	private Map<NostrumResearchTab, ResearchTabButton> tabButtons;
	
	private static BookScreen currentInfoScreen = null;
	
	private static final int guiScale = BUTTON_QUEST_WIDTH + 8;
	private static int guiX;
	private static int guiY;
	private double mouseClickX;
	private double mouseClickY;
	private double mouseClickXOffset; //xoffset at time of click
	private double mouseClickYOffset; //yoffset at time of click
	
	private Map<NostrumQuest, QuestButton> questButtons;
	private Map<NostrumResearch, ResearchButton> researchButtons;
	private final Minecraft mc;
	
	public MirrorGui(PlayerEntity player) {
		super(new StringTextComponent("Nostrum Mirror"));
		this.width = TEXT_WIDTH + BUTTON_MAJOR_WIDTH;
		//this.height = GUI_HEIGHT;
		this.height = 242;
		cacheAttributes(NostrumMagica.getMagicWrapper(player));
		this.player = player;
		questButtons = new HashMap<>();
		researchButtons = new HashMap<>();
		tabButtons = new HashMap<>();
		this.mc = Minecraft.getInstance();
		
		for (NostrumResearchTab tab : NostrumResearchTab.All()) {
			tabButtons.put(tab, new ResearchTabButton(this, tab, 0, 0));
		}
	}
	
	private void cacheAttributes(INostrumMagic attr) {
		this.unlocked = attr.isUnlocked();
		if (!unlocked)
			unlockPrompt = getUnlockPrompt(attr);
		this.level = attr.getLevel();
		this.skillPoints = attr.getSkillPoints();
		this.researchPoints = attr.getResearchPoints();
		this.xp = attr.getXP();
		this.maxXP = attr.getMaxXP();
		this.technique = attr.getTech();
		this.control = attr.getControl();
		this.finesse = attr.getFinesse();
		this.lore = attr.getAllLore();
		final int actKnowledge = NostrumMagic.getKnowledge(attr); 
		this.knowledge = actKnowledge - NostrumMagic.KnowledgeCurves.maxKnowledge(NostrumMagic.KnowledgeCurves.knowledgeLevel(actKnowledge) - 1);
		this.maxKnowledge = NostrumMagic.KnowledgeCurves.maxKnowledge(NostrumMagic.KnowledgeCurves.knowledgeLevel(actKnowledge))
				- NostrumMagic.KnowledgeCurves.maxKnowledge(NostrumMagic.KnowledgeCurves.knowledgeLevel(actKnowledge) - 1);
		refreshQuests(attr);
	}
	
	private void refreshQuests(INostrumMagic attr) {
		this.questsCompleted = attr.getCompletedQuests();
	}
	
	private void setScreen(boolean character) {
		final int GUI_HEIGHT = 242;
		final int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
		final int topOffset = (this.height - GUI_HEIGHT) / 2;
		
		if (isCharacter != character) {
			isCharacter = character;
			
			// Reset view
			MirrorGui.guiX = leftOffset + TEXT_CONTENT_HOFFSET + (int) ((float) TEXT_CONTENT_WIDTH / 2f);
			MirrorGui.guiY = topOffset + TEXT_CONTENT_VOFFSET + (int) ((float) TEXT_CONTENT_HEIGHT / 2f);

			refreshButtons();
		}
	}
	
	private void setResearchTab(NostrumResearchTab tab) {
		final int GUI_HEIGHT = 242;
		final int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
		final int topOffset = (this.height - GUI_HEIGHT) / 2;
		if (tab != currentTab) {
			currentTab = tab;
			
			MirrorGui.guiX = leftOffset + TEXT_CONTENT_HOFFSET + (int) ((float) TEXT_CONTENT_WIDTH / 2f);
			MirrorGui.guiY = topOffset + TEXT_CONTENT_VOFFSET + (int) ((float) TEXT_CONTENT_HEIGHT / 2f);
			tab.clearNew();
			
			this.refreshButtons();
		}
	}
	
	@Override
	public void init() {
		super.init();
		
		int GUI_HEIGHT = 242;
		int KEY_HEIGHT = 15 + 5;
		int KEY_VOFFSET = 10;
		int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
		int topOffset = (this.height - GUI_HEIGHT) / 2;
		
		if (guiX == 0 && guiY == 0) {
			MirrorGui.guiX = leftOffset + TEXT_CONTENT_HOFFSET + (int) ((float) TEXT_CONTENT_WIDTH / 2f);
			MirrorGui.guiY = topOffset + TEXT_CONTENT_VOFFSET + (int) ((float) TEXT_CONTENT_HEIGHT / 2f);
		}
		
		tabCharacter = new MajorTabButton(this, "character", new ItemStack(Items.PLAYER_HEAD, 1), leftOffset - BUTTON_MAJOR_WIDTH, topOffset);
		tabResearch = new MajorTabButton(this, "research", new ItemStack(NostrumItems.spellTomePage), leftOffset - BUTTON_MAJOR_WIDTH, topOffset + TEXT_ICON_MAJORBUTTON_HEIGHT);
		buttonControl = new ImproveButton(this, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (KEY_WIDTH + TEXT_ICON_BUTTON_LENGTH),
				topOffset + TEXT_BOTTOM_VOFFSET + KEY_VOFFSET);
		buttonTechnique = new ImproveButton(this, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (KEY_WIDTH + TEXT_ICON_BUTTON_LENGTH),
				topOffset + TEXT_BOTTOM_VOFFSET + KEY_VOFFSET + KEY_HEIGHT);
		buttonFinesse = new ImproveButton(this, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (KEY_WIDTH + TEXT_ICON_BUTTON_LENGTH),
				topOffset + TEXT_BOTTOM_VOFFSET + KEY_VOFFSET + KEY_HEIGHT + KEY_HEIGHT);
		
		refreshButtons();
	}
	
	private void drawCharacterScreenBackground(int mouseX, int mouseY, float partialTicks) {
		int GUI_HEIGHT = 242;
		int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
		int topOffset = (this.height - GUI_HEIGHT) / 2;
		float extra = .2f * (float) Math.sin((double) System.currentTimeMillis() / 1500.0);
		float inv = .2f - extra;
		GlStateManager.color4f(.8f + extra, 1f, .8f + inv, 1f);
		Minecraft.getInstance().getTextureManager().bindTexture(RES_BACK_CLEAR);
		RenderFuncs.drawScaledCustomSizeModalRect(leftOffset, topOffset, 0, 0, TEXT_WIDTH, GUI_HEIGHT, TEXT_WIDTH, GUI_HEIGHT, TEXT_WIDTH, GUI_HEIGHT);
		
		for (int i = 0; i < this.buttons.size(); ++i) {
			Button button = (Button)this.buttons.get(i);
			if (button instanceof QuestButton)
				((QuestButton) button).drawTreeLines(mc);
		}
		for (int i = 0; i < this.buttons.size(); ++i) {
			Button button = (Button)this.buttons.get(i);
			if (button instanceof QuestButton)
				button.render(mouseX, mouseY, partialTicks);
		}
	}
	
	private void drawCharacterScreenForeground(int mouseX, int mouseY, float partialTicks) {
		int GUI_HEIGHT = 242;
		int KEY_HEIGHT = 15;
		int KEY_VOFFSET = 9;
		int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
		int topOffset = (this.height - GUI_HEIGHT) / 2;
		Minecraft.getInstance().getTextureManager().bindTexture(RES_FORE);
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		GlStateManager.disableBlend();
		GlStateManager.disableLighting();
		RenderFuncs.drawScaledCustomSizeModalRect(leftOffset, topOffset, 0, 0, TEXT_WIDTH, GUI_HEIGHT, TEXT_WIDTH, GUI_HEIGHT, TEXT_WIDTH, TEXT_HEIGHT);
		
		// DRAW STATS
		int y = 2;
		int len;
		int colorKey = 0xFF0A8E0A;
		int colorVal = 0xFFE4E5D5;
		String str;
		
		str = "Level " + level;
		len = font.getStringWidth(str);
		this.font.drawStringWithShadow(str, (this.width - len) / 2, topOffset + TEXT_BOTTOM_VOFFSET, 0xFFFFFFFF);
		y += font.FONT_HEIGHT + 10;
		int yTop = y = KEY_VOFFSET + topOffset + TEXT_BOTTOM_VOFFSET;
		
		//leftOffset + TEXT_BOTTOM_HOFFSET, y + topOffset + TEXT_BOTTOM_VOFFSET, colorKey
		// XP, points
		RenderFuncs.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET - 2, y, leftOffset + TEXT_BOTTOM_HOFFSET + KEY_WIDTH + 2, y + KEY_HEIGHT, 0xD0000000);
		str = "XP: ";
		len = font.getStringWidth(String.format("%.02f%%", 100f * xp/maxXP));
		this.font.drawString(str, leftOffset + TEXT_BOTTOM_HOFFSET, y + (KEY_HEIGHT - font.FONT_HEIGHT) / 2 + 1, colorKey);
		this.font.drawString(String.format("%.02f%%", 100f * xp/maxXP), leftOffset + TEXT_BOTTOM_HOFFSET + KEY_WIDTH - (len), y + (KEY_HEIGHT - font.FONT_HEIGHT) / 2 + 1, colorVal);
		y += KEY_HEIGHT + 5;
		
//					RenderFuncs.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET - 2, topOffset + TEXT_BOTTOM_VOFFSET + y - 2, leftOffset + TEXT_BOTTOM_HOFFSET + keyWidth + 2, topOffset + TEXT_BOTTOM_VOFFSET + y + this.font.FONT_HEIGHT, 0xD0000000);
//					str = "Technique: ";
//					len = font.getStringWidth("" + attr.getTech());
//					this.font.drawString(str, leftOffset + TEXT_BOTTOM_HOFFSET, y + topOffset + TEXT_BOTTOM_VOFFSET, colorKey);
//					this.font.drawString("" + attr.getTech(), leftOffset + TEXT_BOTTOM_HOFFSET + keyWidth - (len), y + topOffset + TEXT_BOTTOM_VOFFSET, colorVal);
		y += KEY_HEIGHT + 5;
		
		RenderFuncs.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET - 2, y, leftOffset + TEXT_BOTTOM_HOFFSET + KEY_WIDTH + 2, y + KEY_HEIGHT, 0xD0000000);
		str = "Skill Points: ";
		len = font.getStringWidth("" + skillPoints);
		this.font.drawString(str, leftOffset + TEXT_BOTTOM_HOFFSET, y + (KEY_HEIGHT - font.FONT_HEIGHT) / 2 + 1, colorKey);
		this.font.drawString("" + skillPoints, leftOffset + TEXT_BOTTOM_HOFFSET + KEY_WIDTH - (len), y + (KEY_HEIGHT - font.FONT_HEIGHT) / 2 + 1, colorVal);
		y += KEY_HEIGHT + 5;
		
		// stats
		y = yTop;
		RenderFuncs.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (2 + KEY_WIDTH), y, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH + 2, y + KEY_HEIGHT, 0xD0000000);
		str = "Control: ";
		len = font.getStringWidth("" + control);
		this.font.drawString(str, leftOffset + TEXT_BOTTOM_WIDTH + TEXT_BOTTOM_HOFFSET - (KEY_WIDTH), y + (KEY_HEIGHT - font.FONT_HEIGHT) / 2 + 1, colorKey);
		this.font.drawString("" + control, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (len), y + (KEY_HEIGHT - font.FONT_HEIGHT) / 2 + 1, colorVal);
		y += KEY_HEIGHT + 5;
		
		RenderFuncs.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (2 + KEY_WIDTH), y, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH + 2, y + KEY_HEIGHT, 0xD0000000);
		str = "Technique: ";
		len = font.getStringWidth("" + technique);
		this.font.drawString(str, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - KEY_WIDTH, y + (KEY_HEIGHT - font.FONT_HEIGHT) / 2 + 1, colorKey);
		this.font.drawString("" + technique, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (len), y + (KEY_HEIGHT - font.FONT_HEIGHT) / 2 + 1, colorVal);
		y += KEY_HEIGHT + 5;
		
		RenderFuncs.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (2 + KEY_WIDTH), y, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH + 2, y + KEY_HEIGHT, 0xD0000000);
		str = "Finess: ";
		len = font.getStringWidth("" + finesse);
		this.font.drawString(str, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - KEY_WIDTH, y + (KEY_HEIGHT - font.FONT_HEIGHT) / 2 + 1, colorKey);
		this.font.drawString("" + finesse, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (len), y + (KEY_HEIGHT - font.FONT_HEIGHT) / 2 + 1, colorVal);
		y += KEY_HEIGHT + 5;
		
		buttonControl.render(mouseX, mouseY, partialTicks);
		buttonTechnique.render(mouseX, mouseY, partialTicks);
		buttonFinesse.render(mouseX, mouseY, partialTicks);
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		int mana = attr.getMana();
		int maxMana = attr.getMaxMana();
		float bonusMana = attr.getManaModifier();
		float bonusManaRegen = attr.getManaRegenModifier();
		float bonusManaCost = attr.getManaCostModifier();
		int fh = font.FONT_HEIGHT;
		this.font.drawString("Mana:",
				leftOffset + TEXT_WIDTH, topOffset + 5, 0xFFFFFFFF);
		this.font.drawString(" " + mana + "/" + maxMana,
				leftOffset + TEXT_WIDTH, topOffset + 5 + fh, 0xFFFFFFFF);
		
		this.font.drawString("Bonus Mana:",
				leftOffset + TEXT_WIDTH, topOffset + 5 + fh * 4, 0xFFFFFFFF);
		this.font.drawString(String.format("%+.1f%%", bonusMana * 100f),
				leftOffset + TEXT_WIDTH, topOffset + 5 + fh * 5, 0xFFFFFFFF);

		this.font.drawString("Mana Regen:",
				leftOffset + TEXT_WIDTH, topOffset + 5 + fh * 6, 0xFFFFFFFF);
		this.font.drawString(String.format("%+.1f%%", bonusManaRegen * 100f),
				leftOffset + TEXT_WIDTH, topOffset + 5 + fh * 7, 0xFFFFFFFF);

		this.font.drawString("Mana Cost:",
				leftOffset + TEXT_WIDTH, topOffset + 5 + fh * 8, 0xFFFFFFFF);
		this.font.drawString(String.format("%+.1f%%", bonusManaCost * 100f),
				leftOffset + TEXT_WIDTH, topOffset + 5 + fh * 9, 0xFFFFFFFF);
		
		if (mouseX >= leftOffset + TEXT_CONTENT_HOFFSET && mouseX <= leftOffset + TEXT_CONTENT_HOFFSET + TEXT_CONTENT_WIDTH
				&& mouseY >= topOffset + TEXT_CONTENT_VOFFSET && mouseY <= topOffset + TEXT_CONTENT_VOFFSET + TEXT_CONTENT_HEIGHT) {
			for (int i = 0; i < this.buttons.size(); ++i) {
				Button button = (Button)this.buttons.get(i);
				if (button instanceof QuestButton)
					((QuestButton) button).drawOverlay(mc, mouseX, mouseY);
			}
		}
	}
	
	private void drawResearchScreenBackground(int mouseX, int mouseY, float partialTicks) {
		int GUI_HEIGHT = 242;
		int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
		int topOffset = (this.height - GUI_HEIGHT) / 2;
		float extra = .2f * (float) Math.sin((double) System.currentTimeMillis() / 1500.0);
		float inv = .2f - extra;
		GlStateManager.color4f(.8f + extra, 1f, .8f + inv, 1f);
		Minecraft.getInstance().getTextureManager().bindTexture(RES_BACK_CLEAR);
		RenderFuncs.drawScaledCustomSizeModalRect(leftOffset, topOffset, 0, 0, TEXT_WIDTH, GUI_HEIGHT, TEXT_WIDTH, GUI_HEIGHT, TEXT_WIDTH, GUI_HEIGHT);
		
		for (int i = 0; i < this.buttons.size(); ++i) {
			Button button = (Button)this.buttons.get(i);
			if (button instanceof ResearchButton)
				button.render(mouseX, mouseY, partialTicks);
		}
		for (int i = 0; i < this.buttons.size(); ++i) {
			Button button = (Button)this.buttons.get(i);
			if (button instanceof ResearchButton)
				((ResearchButton) button).drawTreeLines(mc);
		}
	}
	
	private void drawResearchScreenForeground(int mouseX, int mouseY, float partialTicks) {
		int GUI_HEIGHT = 242;
		int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
		int topOffset = (this.height - GUI_HEIGHT) / 2;
		Minecraft.getInstance().getTextureManager().bindTexture(RES_FORE);
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		GlStateManager.disableBlend();
    	GlStateManager.enableAlphaTest();
		GlStateManager.disableLighting();
		RenderFuncs.drawScaledCustomSizeModalRect(leftOffset, topOffset, 0, 0, TEXT_WIDTH, GUI_HEIGHT, TEXT_WIDTH, GUI_HEIGHT, TEXT_WIDTH, TEXT_HEIGHT);
		
		for (int i = 0; i < this.buttons.size(); ++i) {
			Button button = (Button)this.buttons.get(i);
			if (button instanceof ResearchTabButton) {
				button.render(mouseX, mouseY, partialTicks);
			}
		}
		
		int y = topOffset + TEXT_BOTTOM_VOFFSET + BUTTON_MINOR_HEIGHT + 10;
		int centerX = leftOffset + (TEXT_WIDTH / 2);
		int width = 200;
		RenderFuncs.drawRect(centerX - (width/2), y - 4, centerX + (width / 2), y + font.FONT_HEIGHT + 2, 0xD0000000);
		String str = "Points: " + researchPoints;
		width = font.getStringWidth(str);
		this.font.drawString(str, centerX - (width/2), y, 0xFFFFFFFF);
		
		width = 200;
		int knowledgeHeight = 4;
		y += font.FONT_HEIGHT + 2;
		final int x = Math.round(((float) knowledge / (float) maxKnowledge) * (float) width);
		RenderFuncs.drawRect(centerX - (width/2), y, centerX + (width / 2), y + 2 + knowledgeHeight, 0xFF555555);
		RenderFuncs.drawRect(centerX - (width/2) + 1, y + 1, centerX + (width / 2) - 1, y + 1 + knowledgeHeight, 0xFF000000);
		RenderFuncs.drawRect(centerX - (width/2) + 1, y + 1, centerX - (width/2) + 1 + x, y + 1 + knowledgeHeight, 0xFFFFFF00);
		
		boolean mouseContent =  (mouseX >= leftOffset + TEXT_CONTENT_HOFFSET && mouseX <= leftOffset + TEXT_CONTENT_HOFFSET + TEXT_CONTENT_WIDTH
				&& mouseY >= topOffset + TEXT_CONTENT_VOFFSET && mouseY <= topOffset + TEXT_CONTENT_VOFFSET + TEXT_CONTENT_HEIGHT);
		for (int i = 0; i < this.buttons.size(); ++i) {
			Button button = (Button)this.buttons.get(i);
			if (button instanceof ResearchButton) {
				if (mouseContent) {
					((ResearchButton) button).drawOverlay(mc, mouseX, mouseY);
				}
			} else if (button instanceof ResearchTabButton) {
				((ResearchTabButton) button).drawOverlay(mc, mouseX, mouseY);
			}
		}
	}
	
	private void drawLockedScreenBackground(int mouseX, int mouseY, float partialTicks) {
		int GUI_HEIGHT = 242;
		int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
		int topOffset = (this.height - GUI_HEIGHT) / 2;
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		Minecraft.getInstance().getTextureManager().bindTexture(RES_BACK_CLOUD);
		RenderFuncs.drawScaledCustomSizeModalRect(leftOffset, topOffset, 0, 0, TEXT_WIDTH, GUI_HEIGHT, TEXT_WIDTH, GUI_HEIGHT, TEXT_WIDTH, GUI_HEIGHT);
		
		int y = 0;
		String str = "Magic Not Yet Unlocked";
		int len = this.font.getStringWidth(str);
		this.font.drawStringWithShadow(str, (this.width - len) / 2, topOffset + (TEXT_CONTENT_HEIGHT / 2), 0xFFFFFFFF);
		
		y = font.FONT_HEIGHT + 2;
		
		len = this.font.getStringWidth(unlockPrompt);
		this.font.drawString(unlockPrompt, (this.width - len) / 2, y + topOffset + (TEXT_CONTENT_HEIGHT / 2), 0xFFDFD000);
	}
	
	private void drawLockedScreenForeground(int mouseX, int mouseY, float partialTicks) {
		int GUI_HEIGHT = 242;
		int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
		int topOffset = (this.height - GUI_HEIGHT) / 2;
		
		Minecraft.getInstance().getTextureManager().bindTexture(RES_FORE);
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		GlStateManager.disableBlend();
		GlStateManager.disableLighting();
		RenderFuncs.drawScaledCustomSizeModalRect(leftOffset, topOffset, 0, 0, TEXT_WIDTH, GUI_HEIGHT, TEXT_WIDTH, GUI_HEIGHT, TEXT_WIDTH, TEXT_HEIGHT);
		
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
		RenderFuncs.drawRect(x - 2, y - 2, x + width + 2, y + width + 2, 0xA0000000);
		if (element != null)
			GlStateManager.color4f(1f, 1f, 1f, 1f);
		else {
			GlStateManager.color4f(.8f, .5f, .5f, .5f);
			element = EMagicElement.values()[
                  (int) (System.currentTimeMillis() / cycle) % EMagicElement.values().length
			      ];
		}
		SpellComponentIcon.get(element).draw(this, this.font, x, y, width, width);
		str = I18n.format("element.name", new Object[0]);
		strLen = this.font.getStringWidth(str);
		this.font.drawString(str, (x + width / 2) - strLen/2, y - (3 + this.font.FONT_HEIGHT), 0xFFFFFF);
		
		x += width + space;
		RenderFuncs.drawRect(x - 2, y - 2, x + width + 2, y + width + 2, 0xA0000000);
		if (trigger != null)
			GlStateManager.color4f(1f, 1f, 1f, 1f);
		else {
			GlStateManager.color4f(.8f, .5f, .5f, .5f);
			Collection<SpellTrigger> triggers = SpellTrigger.getAllTriggers();
			SpellTrigger[] trigArray = triggers.toArray(new SpellTrigger[0]);
			trigger = trigArray[
                  (int) (System.currentTimeMillis() / cycle) % trigArray.length
			      ];
		}
		SpellComponentIcon.get(trigger).draw(this, this.font, x, y, width, width);
		str = I18n.format("trigger.name", new Object[0]);
		strLen = this.font.getStringWidth(str);
		this.font.drawString(str, (x + width / 2) - strLen/2, y - (3 + this.font.FONT_HEIGHT), 0xFFFFFF);
		
		x += width + space;
		RenderFuncs.drawRect(x - 2, y - 2, x + width + 2, y + width + 2, 0xA0000000);
		if (shape != null)
			GlStateManager.color4f(1f, 1f, 1f, 1f);
		else {
			GlStateManager.color4f(.8f, .5f, .5f, .5f);
			Collection<SpellShape> shapes = SpellShape.getAllShapes();
			SpellShape[] shapeArray = shapes.toArray(new SpellShape[0]);
			shape = shapeArray[
                  (int) (System.currentTimeMillis() / cycle) % shapeArray.length
			      ];
		}
		SpellComponentIcon.get(shape).draw(this, this.font, x, y, width, width);
		str = I18n.format("shape.name", new Object[0]);
		strLen = this.font.getStringWidth(str);
		this.font.drawString(str, (x + width / 2) - strLen/2, y - (3 + this.font.FONT_HEIGHT), 0xFFFFFF);
	}
	
	private void drawResearchPages(int mouseX, int mouseY, float partialTicks) {
		GlStateManager.enableAlphaTest();
		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		RenderFuncs.drawRect(0, 0, this.width, this.height, 0x60000000);
		currentInfoScreen.render(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		int GUI_HEIGHT = 242;
		int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
		int topOffset = (this.height - GUI_HEIGHT) / 2;
		
		if (unlocked) {
			if (isCharacter) {
				drawCharacterScreenBackground(mouseX, mouseY, partialTicks);
			} else {
				drawResearchScreenBackground(mouseX, mouseY, partialTicks);
			}
		} else {
			drawLockedScreenBackground(mouseX, mouseY, partialTicks);
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0, 0, 50);
		
		// Black out surrounding screen
		int color = 0xFF000000;
		RenderFuncs.drawRect(0, 0, this.width, topOffset, color);
		RenderFuncs.drawRect(0, topOffset + GUI_HEIGHT, this.width, this.height, color);
		RenderFuncs.drawRect(0, 0, leftOffset, this.height, color);
		RenderFuncs.drawRect(leftOffset + TEXT_WIDTH - 1, 0, this.width, this.height, color);
		
		if (unlocked) {
			if (isCharacter) {
				drawCharacterScreenForeground(mouseX, mouseY, partialTicks);
			} else {
				drawResearchScreenForeground(mouseX, mouseY, partialTicks);
			}
		} else {
			drawLockedScreenForeground(mouseX, mouseY, partialTicks);
		}
		
		if (unlocked) {
			// Draw major tab buttons
			tabCharacter.render(mouseX, mouseY, partialTicks);
			tabResearch.render(mouseX, mouseY, partialTicks);
			if (MirrorGui.currentInfoScreen == null) {
				tabCharacter.drawOverlay(mc, mouseX, mouseY);
				tabResearch.drawOverlay(mc, mouseX, mouseY);
			}
			
			
			if (MirrorGui.currentInfoScreen != null) {
				GlStateManager.pushMatrix();
				GlStateManager.translatef(0, 0, 500);
				drawResearchPages(mouseX, mouseY, partialTicks);
				GlStateManager.popMatrix();
			}
		}

		GlStateManager.popMatrix();
		
		//super.render(mouseX, mouseY, partialTicks);
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
	
	protected boolean ignoreButton(Button button) {
		if (!button.visible) return true;
		if (MirrorGui.currentInfoScreen != null) return true;
		
		return false;
	}
	
	protected void onImproveControl(Button button) {
		if (ignoreButton(button)) return;
		
		final Type type;
		if (button == this.buttonControl) {
			type = Type.CONTROL;
		} else if (button == this.buttonFinesse) {
			type = Type.FINESSE;
		} else {
			type = Type.TECHNIQUE;
		}
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		attr.takeSkillPoint(); // take a local point so our update makes sense
		NetworkHandler.sendToServer(
				new ClientSkillUpMessage(type)
				);
		refreshButtons();
	}
	
	protected void onButtonQuest(Button button) {
		if (ignoreButton(button)) return;
		
		// Quest button
		QuestButton qb = (QuestButton) button;
		NostrumQuest quest = qb.quest;
		
		if (qb.state == QuestState.INACTIVE || qb.state == QuestState.TAKEN) {
			NetworkHandler.sendToServer(
				new ClientUpdateQuestMessage(quest)	
				);
			refreshButtons();
		}
	}
	
	protected void onButtonMajorTab(Button button) {
		if (ignoreButton(button)) return;
		
		this.setScreen(button == tabCharacter);
	}
	
	protected void onButtonResearchTab(Button button) {
		if (ignoreButton(button)) return;
		
		this.setResearchTab(((ResearchTabButton) button).tab);
	}
	
	protected void onButtonResearch(Button button) {
		if (ignoreButton(button)) return;
		
		// Research button
		ResearchButton rb = (ResearchButton) button;
		NostrumResearch research = rb.research;
		
		if (rb.state == ResearchState.INACTIVE && researchPoints > 0) {
			NetworkHandler.sendToServer(
				new ClientPurchaseResearchMessage(research)	
				);
			refreshButtons();
		} else if (rb.state == ResearchState.COMPLETED) {
			String info = I18n.format(rb.research.getInfoKey(), new Object[0]);
			List<IBookPage> pages = new LinkedList<>();
			BookScreen.makePagesFrom(pages, info);
			
			// Add reference page at the end if we have references
			String[] references = rb.research.getRawReferences();
			String[] displays = rb.research.getDisplayedReferences();

			if (references != null && references.length > 0) {
			
				// translate display names
				String[] displaysFixed = new String[displays.length];
				for (int i = 0; i < displays.length; i++) {
					displaysFixed[i] = I18n.format(displays[i], new Object[0]);
				}
				pages.add(new ReferencePage(displaysFixed, references, false));
			}
			
			currentInfoScreen = new BookScreen(System.currentTimeMillis() + "", pages);
			currentInfoScreen.init(mc, width, height);
			refreshButtons();
		}
	}
	
	private void refreshButtons() {
		int GUI_HEIGHT = 242;
		int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
		int topOffset = (this.height - GUI_HEIGHT) / 2;
//		for (Button button : buttons) {
//			button.visible = false;
//		}
		this.buttons.clear();
		this.children.clear();
		questButtons.clear();
		researchButtons.clear();
		
		// Add main tabs
		this.addButton(tabCharacter);
		this.addButton(tabResearch);
		
		if (isCharacter) {
			// Add buttons that show up on the character screen
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
			
			
			this.addButton(buttonControl);
			this.addButton(buttonTechnique);
			this.addButton(buttonFinesse);
			
			for (NostrumQuest quest : NostrumQuest.allQuests()) {
				if (!ModConfig.config.displayAllMirrorQuestNodes() && !NostrumMagica.getQuestAvailable(player, quest))
					continue;
				
				QuestState state;
				if (NostrumMagica.getCompletedQuests(player).contains(quest))
					state = QuestState.COMPLETED;
				else if (NostrumMagica.getActiveQuests(player).contains(quest))
					state = QuestState.TAKEN;
				else if (NostrumMagica.canTakeQuest(player, quest))
					state = QuestState.INACTIVE;
				else
					state = QuestState.UNAVAILABLE;
				
				QuestButton button = new QuestButton(this,
						quest.getPlotX(), quest.getPlotY(),
						quest, state);
				this.addButton(button);
				questButtons.put(quest, button);
			}
		} else {
			// Add buttons that show up on the research screen.
			// We discover which tabs to display while also filtering research down
			// to those on the active tab at the same time.
			Set<NostrumResearchTab> visibleTabs = new HashSet<>();
			boolean firstTime = false;
			
			// Set up seen research if this is our first time through
			if (seenResearch == null) {
				seenResearch = new HashSet<>();
				newResearch = new HashSet<>();
				firstTime = true;
			}
			
			for (NostrumResearch research: NostrumResearch.AllResearch()) {
				if (!NostrumMagica.getResearchVisible(player, research))
					continue;
				
				visibleTabs.add(research.getTab());
				
				if (firstTime) {
					seenResearch.add(research);
					newResearch.add(research);
				}
				
				if (research.getTab() != currentTab) {
					if (!newResearch.contains(research)) {
						research.getTab().markHasNew();
						newResearch.add(research);
					}
					continue;
				}
				
				newResearch.add(research);
				
				ResearchState state;
				if (NostrumMagica.getCompletedResearch(player).contains(research))
					state = ResearchState.COMPLETED;
				else if (NostrumMagica.canPurchaseResearch(player, research))
					state = ResearchState.INACTIVE;
				else
					state = ResearchState.UNAVAILABLE;
				
				ResearchButton button = new ResearchButton(this,
						research.getX(), research.getY(),
						research, state);
				
				if (!seenResearch.contains(research)) {
					// play animation
					button.startAnim();
					seenResearch.add(research);
				}
				this.addButton(button);
				researchButtons.put(research, button);
			}
			
			int x = leftOffset + TEXT_BOTTOM_HOFFSET - 12;
			List<NostrumResearchTab> tabList = Lists.newArrayList(visibleTabs);
			Collections.sort(tabList, (l, r) -> {
				ResearchTabButton lButton = tabButtons.get(l);
				ResearchTabButton rButton = tabButtons.get(r);
				return lButton.tab.getRawKey().compareTo(rButton.tab.getRawKey());
			});
			for (NostrumResearchTab tab : tabList) {
				ResearchTabButton button = tabButtons.get(tab);
				button.x = x;
				button.y = topOffset + TEXT_BOTTOM_VOFFSET - 1;
				addButton(button);
				x += TEXT_ICON_MINORBUTTON_WIDTH;
			}
		}
	}
	
	/**
	 * Refresh state from attributes, including re-evaluating quest state
	 */
	public void refresh() {
		this.cacheAttributes(NostrumMagica.getMagicWrapper(player));
		refreshButtons();
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		
		if (!unlocked) {
			return false;
		}
		
		if (MirrorGui.currentInfoScreen != null) {
			MirrorGui.currentInfoScreen.mouseClicked(mouseX, mouseY, mouseButton);
			return false;
		}
		
		int GUI_HEIGHT = 242;
		int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
		int topOffset = (this.height - GUI_HEIGHT) / 2;
		
		if (mouseX > leftOffset + TEXT_CONTENT_HOFFSET && mouseX < leftOffset + TEXT_CONTENT_HOFFSET + TEXT_CONTENT_WIDTH
				&& mouseY > topOffset + TEXT_CONTENT_VOFFSET && mouseY < topOffset + TEXT_CONTENT_VOFFSET + TEXT_CONTENT_HEIGHT) {
			mouseClickX = mouseX;
			mouseClickY = mouseY;
			mouseClickXOffset = guiX;
			mouseClickYOffset = guiY;
		} else {
			mouseClickX = -500;
		}
		
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double idk, double idk2) {
		if (currentInfoScreen != null) {
			mouseClickX = -500;
			//this.currentInfoScreen.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
			return false;
		}
		
		if (mouseClickX > 0) {
			guiX = (int) (mouseClickXOffset - (mouseClickX - mouseX));
			guiY = (int) (mouseClickYOffset - (mouseClickY - mouseY));
			return true;
		}
		
		return super.mouseDragged(mouseX, mouseY, clickedMouseButton, idk, idk2);
	}
	
	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (p_keyPressed_1_ == 256 && currentInfoScreen != null) {
			currentInfoScreen = null;
			return true;
		}
		
		return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}
	
	private class MajorTabButton extends Button {
		
		private final ItemStack icon;
		private boolean mouseOver;
		private List<String> tooltip;
		
		public MajorTabButton(MirrorGui gui, String name, ItemStack icon, int parPosX, int parPosY) {
			super(parPosX, parPosY, BUTTON_MAJOR_WIDTH, BUTTON_MAJOR_HEIGHT, "", (b) -> {
				gui.onButtonMajorTab(b);
			});
			this.icon = icon;
			tooltip = Lists.newArrayList(I18n.format("mirror.tab." + name + ".name", new Object[0]));
		}
		
		@Override
        public void render(int parX, int parY, float partialTicks) {
			if (visible) {
				int textureX = TEXT_ICON_MAJORBUTTON_HOFFSET;
				int textureY = TEXT_ICON_MAJORBUTTON_VOFFSET;
				mouseOver = false;
            	if (parX >= x 
                  && parY >= y 
                  && parX < x + width 
                  && parY < y + height) {
            		textureY += TEXT_ICON_MAJORBUTTON_HEIGHT;
            		mouseOver = true;
            	}
            	RenderHelper.disableStandardItemLighting();
            	GlStateManager.disableLighting();
            	GlStateManager.enableAlphaTest();
            	GlStateManager.color4f(1f, 1f, 1f, 1f);
                mc.getTextureManager().bindTexture(RES_ICONS);
                RenderFuncs.drawScaledCustomSizeModalRect(x, y, textureX, textureY,
        				TEXT_ICON_MAJORBUTTON_WIDTH, TEXT_ICON_MAJORBUTTON_HEIGHT, this.width, this.height, 256, 256);
                
                // Now draw icon
                GlStateManager.pushMatrix();
                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.translatef(0, 0, -50);
                mc.getItemRenderer().renderItemIntoGUI(icon, x + (width - 16) / 2, y + (height - 16) / 2);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();
            }
        }
		
		public void drawOverlay(Minecraft mc, int parX, int parY) {
			if (mouseOver) {
				GlStateManager.pushMatrix();
				renderTooltip(tooltip, parX, parY);
				GlStateManager.popMatrix();
			}
		}
	}
	
    static class ImproveButton extends Button {
		
		public ImproveButton(MirrorGui gui, int parPosX, int parPosY) {
			super(parPosX, parPosY, 12, 12,"", (b) -> {
				gui.onImproveControl(b);
			});
		}
		
		@Override
        public void render(int parX, int parY, float partialTicks) {
			if (visible) {
				int textureX = 0;
				int textureY = TEXT_ICON_BUTTON_VOFFSET;
            	if (parX >= x 
                  && parY >= y 
                  && parX < x + width 
                  && parY < y + height) {
            		textureX += TEXT_ICON_BUTTON_LENGTH;
            	}
                
            	GlStateManager.color4f(1f, 1f, 1f, 1f);
                Minecraft.getInstance().getTextureManager().bindTexture(RES_ICONS);
                RenderFuncs.drawScaledCustomSizeModalRect(x, y, textureX, textureY,
        				TEXT_ICON_BUTTON_LENGTH, TEXT_ICON_BUTTON_LENGTH, this.width, this.height, 256, 256);
            }
        }
	}
    
    protected static enum QuestState {
		UNAVAILABLE,
		INACTIVE,
		TAKEN,
		COMPLETED
	}
    
    private static abstract class GuiObscuredButton extends Button {

		public GuiObscuredButton(int x, int y, int widthIn, int heightIn, String buttonText, Button.IPressable action) {
			super(x, y, widthIn, heightIn, buttonText, action);
		}
		
		// Check if can click
		@Override
		protected boolean clicked(double mouseX, double mouseY) {
			final Minecraft mc = Minecraft.getInstance();
			final int GUI_HEIGHT = 242;
			final int leftOffset = (mc.currentScreen.width - TEXT_WIDTH) / 2; //distance from left
			final int topOffset = (mc.currentScreen.height - GUI_HEIGHT) / 2;
			if (super.clicked(mouseX, mouseY)) {
				if (mouseX >= leftOffset + TEXT_CONTENT_HOFFSET && mouseX <= leftOffset + TEXT_CONTENT_HOFFSET + TEXT_CONTENT_WIDTH
						&& mouseY >= topOffset + TEXT_CONTENT_VOFFSET && mouseY <= topOffset + TEXT_CONTENT_VOFFSET + TEXT_CONTENT_HEIGHT) {
					return true;
				}
			}
			return false;
		}
    }
	
    private class QuestButton extends GuiObscuredButton {
    	
    	private NostrumQuest quest;
    	private QuestState state;
    	private int offsetX;
    	private int offsetY;
    	
    	private List<String> tooltip;
    	private boolean mouseOver;
    	private final float fontScale = 0.75f;
    	private boolean canTurnin;
    	private SpellComponentIcon icon; // Icon to use as this ocon
    	private int iconOffset; // If icon is null, offset for our icon on the main texture
		
		public QuestButton(MirrorGui gui, int parPosX, int parPosY,
				NostrumQuest quest, QuestState state) {
			super(parPosX, parPosY, BUTTON_QUEST_WIDTH, BUTTON_QUEST_HEIGHT, "", (b) -> {
				gui.onButtonQuest(b);
			});
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
			GlStateManager.pushLightingAttributes();
			GlStateManager.translatef(width / 2, height / 2, 0);
			BufferBuilder buf = Tessellator.getInstance().getBuffer();
			//GlStateManager.enableBlend();
	        GlStateManager.disableTexture();
	        //GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
	        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 0.6f);
	        buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
	        buf.pos(x - 1, y, 0).endVertex();
	        buf.pos(other.x - 1, other.y, 0).endVertex();
	        Tessellator.getInstance().draw();
	        GlStateManager.enableTexture();
	        //GlStateManager.disableBlend();
			
	        GlStateManager.popAttributes();
			GlStateManager.popMatrix();
		}
		
		@Override
        public void render(int parX, int parY, float partialTicks) {
			mouseOver = false;
			if (visible) {
				int textureX = 0;
				int textureY = TEXT_ICON_QUEST_VOFFSET;
				
				x = (this.offsetX * guiScale) + guiX;
				y = (this.offsetY * guiScale) + guiY;
				
				x -= this.width / 2;
				y -= this.height / 2;
				
            	if (parX >= x 
                  && parY >= y 
                  && parX < x + width 
                  && parY < y + height) {
            		textureY += TEXT_ICON_QUEST_LENGTH;
            		mouseOver = true;
            	}
            	textureX += TEXT_ICON_QUEST_LENGTH * quest.getType().ordinal();
                
            	switch (state) {
				case COMPLETED:
					GlStateManager.color4f(.2f, 2f/3f, .2f, 1f);
					break;
				case INACTIVE:
					GlStateManager.color4f(2f/3f, 0f, 2f/3f, .8f);
					break;
				case TAKEN: {
					float amt = 0f;
					if (canTurnin) {
						amt = (float) Math.sin(2.0 * Math.PI * (double) (System.currentTimeMillis() % 1000) / 1000.0);
						amt *= .1f;
					}
					GlStateManager.color4f(1f/3f + amt, .2f + amt, 2f/3f + amt, 1f);
					break;
				}
				case UNAVAILABLE:
					GlStateManager.color4f(.8f, .0f, .0f, .6f);
					break;
            	}
                
            	GlStateManager.disableLighting();
            	GlStateManager.disableBlend();
                mc.getTextureManager().bindTexture(RES_ICONS);
                RenderFuncs.drawScaledCustomSizeModalRect(x, y, textureX, textureY,
                		TEXT_ICON_QUEST_LENGTH, TEXT_ICON_QUEST_LENGTH, this.width, this.height, 256, 256);
                
                if (icon != null) {
                	icon.draw(this, font, x + 2, y + 3, 12, 12);
                } else {
                	GlStateManager.enableBlend();
                	GlStateManager.color4f(1f, 1f, 1f, .8f);
                	RenderFuncs.drawScaledCustomSizeModalRect(x + 4, y + 5, iconOffset, TEXT_ICON_BUTTON_VOFFSET,
                		TEXT_ICON_REWARD_WIDTH, TEXT_ICON_REWARD_WIDTH, 8, 8, 256, 256);
                }
            }
        }
		
		public void drawOverlay(Minecraft mc, int parX, int parY) {
			if (mouseOver) {
				GlStateManager.pushMatrix();
				GlStateManager.scalef(fontScale, fontScale, 1f);
				GlStateManager.translatef((int) (parX / fontScale) - parX, (int) (parY / fontScale) - parY, 0);
				renderTooltip(tooltip, parX, parY);
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
				this.icon = SpellComponentIcon.get(((AlterationReward) reward).getAlteration());
			} else if (reward instanceof TriggerReward) {
				this.icon = SpellComponentIcon.get(((TriggerReward) reward).getTrigger());
			} else if (reward instanceof AttributeReward) {
				AwardType type = ((AttributeReward) reward).getType();
				iconOffset = TEXT_ICON_REWARD_OFFSET + (type.ordinal() * TEXT_ICON_REWARD_WIDTH);
			}
		}
		
		private List<String> genTooltip() {
			int maxWidth = 200; 
			tooltip = new LinkedList<>();
			String title = I18n.format("quest." + quest.getKey() + ".name", new Object[0]);
			title = TextFormatting.BLUE + title + TextFormatting.RESET;
            tooltip.add(title);
            
            TextFormatting bad = TextFormatting.RED;
            TextFormatting good = TextFormatting.GREEN;
            TextFormatting unique = TextFormatting.DARK_AQUA;
            if (quest.getReqLevel() > 0)
            	tooltip.add("" + (level >= quest.getReqLevel() ? good : bad)
            			+ I18n.format("level.name") + ": " + quest.getReqLevel() + TextFormatting.RESET);
            if (quest.getReqControl() > 0)
            	tooltip.add("" + (control >= quest.getReqControl() ? good : bad)
            			+ I18n.format("control.name") + ": " + quest.getReqControl() + TextFormatting.RESET);
            if (quest.getReqTechnique() > 0)
            	tooltip.add("" + (technique >= quest.getReqTechnique() ? good : bad)
            			+ I18n.format("technique.name") + ": " + quest.getReqTechnique() + TextFormatting.RESET);
            if (quest.getReqFinesse() > 0)
            	tooltip.add("" + (finesse >= quest.getReqFinesse() ? good : bad)
            			+ I18n.format("finesse.name") + ": " + quest.getReqFinesse() + TextFormatting.RESET);
            
            // Lore reqs?
            if (quest.getLoreKeys() != null) {
            	for (String loreKey : quest.getLoreKeys()) {
            		ILoreTagged loreItem = LoreRegistry.instance().lookup(loreKey);
            		if (loreItem != null) {
            			if (!lore.contains(loreItem)) {
            				tooltip.add(bad
            						+ I18n.format("info.quest.lore_missing", new Object[]{unique + loreItem.getLoreDisplayName() + bad})
            						+ TextFormatting.RESET);
            			}
            		}
            	}
            }
            
            if (quest.getObjective() != null) {
            	tooltip.add(quest.getObjective().getDescription());
            }
            
            if (quest.getRewards() != null && quest.getRewards().length != 0)
            for (IReward reward : quest.getRewards()) {
            	String desc = reward.getDescription();
            	if (desc != null && !desc.isEmpty())
            		tooltip.add(TextFormatting.GOLD + desc + TextFormatting.RESET);
            }
            
            if (this.state == QuestState.INACTIVE && NostrumMagica.canTakeQuest(player, quest)) {
            	tooltip.add(TextFormatting.GREEN + I18n.format("info.quest.accept") + TextFormatting.RESET);
            }
            
            if (this.state == QuestState.TAKEN || this.state == QuestState.COMPLETED) {
	            for (String line : tooltip) {
	            	int width = font.getStringWidth(line);
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
		            		int oldlen = font.getStringWidth(buf.toString());
		            		if (oldlen + font.getCharWidth(desc.charAt(index)) > maxWidth) {
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
            }
            
            return tooltip;
		}
	}
    
    private class ResearchTabButton extends Button {
		
		private final NostrumResearchTab tab;
		private boolean mouseOver;
		private List<String> tooltip;
		
		public ResearchTabButton(MirrorGui gui, NostrumResearchTab tab, int parPosX, int parPosY) {
			super(parPosX, parPosY, BUTTON_MINOR_WIDTH, BUTTON_MINOR_HEIGHT, "", (b) -> {
				gui.onButtonResearchTab(b);
			});
			this.tab = tab;
			tooltip = Lists.newArrayList(I18n.format(tab.getNameKey(), new Object[0]));
		}
		
		@Override
        public void render(int parX, int parY, float partialTicks) {
			if (visible) {
				int textureX = TEXT_ICON_MINORBUTTON_HOFFSET;
				int textureY = TEXT_ICON_MINORBUTTON_VOFFSET;
				mouseOver = false;
				if (currentTab == this.tab) {
					textureY += TEXT_ICON_MINORBUTTON_HEIGHT + TEXT_ICON_MINORBUTTON_HEIGHT;
				}else if (parX >= x 
                  && parY >= y 
                  && parX < x + width 
                  && parY < y + height) {
            		textureY += TEXT_ICON_MINORBUTTON_HEIGHT;
            		mouseOver = true;
            	}
                
            	GlStateManager.color4f(1f, 1f, 1f, 1f);
            	RenderHelper.disableStandardItemLighting();
            	GlStateManager.enableAlphaTest();
                mc.getTextureManager().bindTexture(RES_ICONS);
                RenderFuncs.drawScaledCustomSizeModalRect(x, y, textureX, textureY,
                		TEXT_ICON_MINORBUTTON_WIDTH, TEXT_ICON_MINORBUTTON_HEIGHT, this.width, this.height, 256, 256);
                
                // Now draw icon
                GlStateManager.pushMatrix();
                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.translatef(0, 0, -50);
                mc.getItemRenderer().renderItemIntoGUI(tab.getIcon(), x + (width - 16) / 2, y + (height - 16) / 2);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();
                
                // Draw new tab if there's something new
                if (tab.hasNew()) {
                	GlStateManager.color4f(1f, 1f, 1f, 1f);
                	RenderHelper.disableStandardItemLighting();
                    mc.getTextureManager().bindTexture(RES_ICONS);
                    RenderFuncs.drawScaledCustomSizeModalRect(x, y, TEXT_ICON_MINORBUTTON_HOFFSET + TEXT_ICON_MINORBUTTON_WIDTH, TEXT_ICON_MINORBUTTON_VOFFSET,
                    		TEXT_ICON_MINORBUTTON_WIDTH, TEXT_ICON_MINORBUTTON_HEIGHT, this.width, this.height, 256, 256);
                }
            }
        }
		
		public void drawOverlay(Minecraft mc, int parX, int parY) {
			if (mouseOver) {
				GlStateManager.pushMatrix();
				renderTooltip(tooltip, parX, parY);
				GlStateManager.popMatrix();
			}
		}
	}
    
    protected static enum ResearchState {
		UNAVAILABLE,
		INACTIVE,
		COMPLETED
	}
	
    private class ResearchButton extends GuiObscuredButton {
    	
    	private final NostrumResearch research;
    	private final ResearchState state;
    	private final int offsetX;
    	private final int offsetY;
    	
    	private List<String> tooltip;
    	private boolean mouseOver;
    	private long animStartMS;
		
		public ResearchButton(MirrorGui gui, int parPosX, int parPosY,
				NostrumResearch research, ResearchState state) {
			super(parPosX, parPosY, WidthForSize(research.getSize()), HeightForSize(research.getSize()), "", (b) -> {
				gui.onButtonResearch(b);
			});
			this.research = research;
			this.state = state;
			this.offsetX = parPosX;
			this.offsetY = parPosY;
			genTooltip();
		}
		
		public void startAnim() {
			animStartMS = System.currentTimeMillis();
		}
		
		public void drawTreeLines(Minecraft mc) {
			if (research.getParentKeys() != null && research.getParentKeys().length != 0) {
				for (String key : research.getParentKeys()) {
					NostrumResearch parentResearch = NostrumResearch.lookup(key);
					if (parentResearch == null)
						continue;
					
					if (parentResearch.getTab() != research.getTab()) {
						continue;
					}
					
					ResearchButton other = researchButtons.get(parentResearch);
					if (other != null)
						renderLine(other);
				}
			}
		}
		
		private void renderLine(ResearchButton other) {
			// Render 2 flat lines with a nice circle-arc between them
			
			float alpha = (float) (this.animStartMS == 0 ? 1 : ((double)(System.currentTimeMillis() - animStartMS) / 500.0));
			
			GlStateManager.pushMatrix();
			BufferBuilder buf = Tessellator.getInstance().getBuffer();
	        GlStateManager.enableBlend();
	        GlStateManager.enableAlphaTest();
	        GlStateManager.disableColorMaterial();
	        GlStateManager.disableColorLogicOp();
	        GlStateManager.disableTexture();
	        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.disableLighting();
	        //GlStateManager.disableDepth();
	        GlStateManager.lineWidth(3.5f);
	        GlStateManager.color4f(.8f, .8f, .8f, alpha);
	        
	        Vec2f child = new Vec2f(x + ((float) width / 2f), y + ((float) height / 2f));
	        Vec2f parent = new Vec2f(other.x + ((float) other.width / 2f), other.y + ((float) other.height / 2f));
	        Vec2f diff = new Vec2f(child.x - parent.x, child.y - parent.y);
	        
	        Vec2f myCenter = child; // Stash for later
	        
	        if (child.x == parent.x || child.y == parent.y) {
	        	// Straight line
	        	
	        	if (child.x == parent.x) {
	        		// vertical line. Shrink both sides in y
	        		child = new Vec2f(child.x, child.y + (-Math.signum(diff.y) * ((float) height / 2f)));
	        		parent = new Vec2f(parent.x, parent.y - (-Math.signum(diff.y) * ((float) other.height / 2f)));
	        	} else {
	        		// horizional. "" x
	        		child = new Vec2f(child.x + (-Math.signum(diff.x) * ((float) width / 2f)), child.y);
	        		parent = new Vec2f(parent.x - (-Math.signum(diff.x) * ((float) other.width / 2f)), parent.y);
	        	}
	        	
	        	buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
		        buf.pos(child.x, child.y, 0).endVertex();
		        buf.pos(parent.x, parent.y, 0).endVertex();
		        Tessellator.getInstance().draw();
	        } else {
		        boolean vertical;// = (Math.abs(diff.y) > Math.abs(diff.x));
		        
		        // figure out radius of arc to draw
		        double radius;
		        if (Math.abs(diff.x) < Math.abs(diff.y)) {
		        	radius = Math.abs(diff.x);
		        	vertical = true;
		        } else {
		        	radius = Math.abs(diff.y);
		        	vertical = false;
		        }
		        radius = Math.min(Math.max(radius * .5f, 12), 12);//*= .5f;
		        double radiusX = (diff.x < 0 ? -1 : 1) * radius;
		        double radiusY = (diff.y < 0 ? -1 : 1) * radius;
		        Vec2f center = new Vec2f(vertical ? parent.x : child.x, vertical ? child.y : parent.y);
		        
		        Vec2f childTo = new Vec2f(vertical ? center.x + (float) radiusX : center.x, vertical ? center.y : center.y + (float) radiusY);
		        Vec2f parentTo = new Vec2f(vertical ? center.x : center.x - (float) radiusX, vertical ? center.y - (float) radiusY : center.y);
		        
		        if (vertical) {
	        		// vertical at parent. Shrink parent y and child x
		        	child = new Vec2f(child.x + (-Math.signum(diff.x) * ((float) width / 2f)), child.y);
	        		parent = new Vec2f(parent.x, parent.y - (-Math.signum(diff.y) * ((float) other.height / 2f)));
	        	} else {
	        		// inverse of above
	        		child = new Vec2f(child.x, child.y + (-Math.signum(diff.y) * ((float) height / 2f)));
	        		parent = new Vec2f(parent.x - (-Math.signum(diff.x) * ((float) other.width / 2f)), parent.y);
	        	}
		        
		        buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
		        buf.pos(child.x, child.y, 0).endVertex();
		        buf.pos(childTo.x, childTo.y, 0).endVertex();
		        buf.pos(parentTo.x, parentTo.y, 0).endVertex();
		        buf.pos(parent.x, parent.y, 0).endVertex();
		        Tessellator.getInstance().draw();
		        
		        // Draw inside curve
		        int points = 30;
		        GlStateManager.pushMatrix();
		        GlStateManager.translatef(parentTo.x, parentTo.y, 0);
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
		        GlStateManager.rotatef(rotate, 0, 0, 1);
		        buf.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
		        for (int i = 0; i <= points; i++) {
		        	float progress = (float) i / (float) points;
		        	Vec2f point = Curves.alignedArc2D(progress, Vec2f.ZERO, radius, flip);
		        	buf.pos(point.x, point.y, 0).endVertex();
		        }
		        Tessellator.getInstance().draw();
		        GlStateManager.popMatrix();
	        }
	        
	        GlStateManager.translated(child.x, child.y, .1);
	        if (child.x < myCenter.x) {
	        	// from left
	        	GlStateManager.rotatef(-90f, 0, 0, 1);
	        } else if (child.x > myCenter.x) {
	        	// from right
	        	GlStateManager.rotatef(90f, 0, 0, 1);
	        } else if (child.y > myCenter.y) {
	        	// from bottom
	        	GlStateManager.rotatef(180f, 0, 0, 1);
	        }
	        GlStateManager.disableLighting();
            GlStateManager.enableTexture();
            GlStateManager.enableBlend();
            GlStateManager.color4f(1f, 1f, 1f, alpha);
            mc.getTextureManager().bindTexture(RES_ICONS);
            RenderFuncs.drawScaledCustomSizeModalRect(-(TEXT_ICON_ARROW_WIDTH/2) - 1, -(TEXT_ICON_ARROW_HEIGHT/2), TEXT_ICON_ARROW_HOFFSET, TEXT_ICON_ARROW_VOFFSET,
            		TEXT_ICON_ARROW_WIDTH, TEXT_ICON_ARROW_HEIGHT, 14, 7, 256, 256);
            GlStateManager.enableDepthTest();
			GlStateManager.popMatrix();
		}
		
		@Override
        public void render(int parX, int parY, float partialTicks) {
			mouseOver = false;
			if (visible) {
				float alpha = (float) (this.animStartMS == 0 ? 1 : ((double)(System.currentTimeMillis() - animStartMS) / 500.0));
				int textureX;
				int textureY;
				int textureW;
				int textureH;
				
				if (alpha >= 1f) {
					this.animStartMS = 0;
				}
				
				switch (research.getSize()) {
				case GIANT:
					textureX = TEXT_ICON_RESEARCHBUTTON_GIANT_HOFFSET;
					textureY = TEXT_ICON_RESEARCHBUTTON_GIANT_VOFFSET;
					textureW = TEXT_ICON_RESEARCHBUTTON_GIANT_WIDTH;
					textureH = TEXT_ICON_RESEARCHBUTTON_GIANT_HEIGHT;
					break;
				case LARGE:
					textureX = TEXT_ICON_RESEARCHBUTTON_LARGE_HOFFSET;
					textureY = TEXT_ICON_RESEARCHBUTTON_LARGE_VOFFSET;
					textureW = TEXT_ICON_RESEARCHBUTTON_LARGE_WIDTH;
					textureH = TEXT_ICON_RESEARCHBUTTON_LARGE_HEIGHT;
					break;
				case NORMAL:
				default:
					textureX = TEXT_ICON_RESEARCHBUTTON_SMALL_HOFFSET;
					textureY = TEXT_ICON_RESEARCHBUTTON_SMALL_VOFFSET;
					textureW = TEXT_ICON_RESEARCHBUTTON_SMALL_WIDTH;
					textureH = TEXT_ICON_RESEARCHBUTTON_SMALL_HEIGHT;
					break;
				}
				
				int guiScale = TEXT_ICON_RESEARCHBUTTON_SMALL_WIDTH + 16; // TODO move up?
				x = (this.offsetX * guiScale) + guiX;
				y = (this.offsetY * guiScale) + guiY;
				
				x -= this.width / 2;
				y -= this.height / 2;
				
            	if (parX >= x 
                  && parY >= y 
                  && parX < x + width 
                  && parY < y + height) {
            		textureY += textureH;
            		mouseOver = true;
            	}
            	

                GlStateManager.pushMatrix();
            	switch (state) {
				case COMPLETED:
					GlStateManager.color4f(.2f, 2f/3f, .2f, alpha);
					break;
				case INACTIVE:
					GlStateManager.color4f(2f/3f, 0f, 2f/3f, alpha);
					break;
				case UNAVAILABLE:
					GlStateManager.color4f(.8f, .0f, .0f, alpha);
					break;
            	}
                
            	GlStateManager.disableLighting();
                GlStateManager.enableTexture();
                GlStateManager.enableBlend();
                GlStateManager.enableAlphaTest();
                mc.getTextureManager().bindTexture(RES_ICONS);
                RenderFuncs.drawScaledCustomSizeModalRect(x, y, textureX, textureY,
                		textureW, textureH, this.width, this.height, 256, 256);
                
                // Now draw icon
                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.translated(0, 0, -140.5);
                mc.getItemRenderer().renderItemAndEffectIntoGUI(research.getIconItem(), x + (width - 16) / 2, y + (height - 16) / 2);
                mc.getItemRenderer().renderItemOverlayIntoGUI(font, research.getIconItem(), x + (width - 16) / 2, y + (height - 16) / 2, null);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.enableDepthTest();
                GlStateManager.enableBlend();
                
//                RenderHelper.enableGUIStandardItemLighting();
//                mc.getItemRenderer().renderItemIntoGUI(research.getIconItem(), x + (width -16) / 2, y + (height -16) / 2);
//                RenderHelper.disableStandardItemLighting();
//            	GlStateManager.enableBlend();
                
                GlStateManager.popMatrix();
            }
        }
		
		public void drawOverlay(Minecraft mc, int parX, int parY) {
			if (mouseOver) {
		        GlStateManager.enableBlend();
		        GlStateManager.enableAlphaTest();
				GlStateManager.pushMatrix();
				GlStateManager.scalef(fontScale, fontScale, 1f);
				GlStateManager.translatef((int) (parX / fontScale) - parX, (int) (parY / fontScale) - parY, 0);
				renderTooltip(tooltip, parX, parY);
				GlStateManager.popMatrix();
			}
		}
		
		private List<String> genTooltip() {
			tooltip = new LinkedList<>();
            tooltip.add(TextFormatting.BLUE + I18n.format(research.getNameKey(), new Object[0]) + TextFormatting.RESET);
            tooltip.add(TextFormatting.GRAY + I18n.format(research.getDescKey(), new Object[0]) + TextFormatting.RESET);
            
            TextFormatting bad = TextFormatting.RED;
            TextFormatting missingQuest = TextFormatting.DARK_PURPLE;
            TextFormatting missingLore = TextFormatting.DARK_AQUA;
            boolean first = false;
            
	        // Quest reqs?
	        if (research.getRequiredQuests() != null) {
	        	for (String questKey : research.getRequiredQuests()) {
        			if (!questsCompleted.contains(questKey)) {
        				if (!first) {
        					first = true;
        					tooltip.add("");
        				}
        				
        				NostrumQuest questItem = NostrumQuest.lookup(questKey);
        				String display = questItem == null ? questKey : I18n.format("quest." + questItem.getKey() + ".name");
        				
        				tooltip.add(bad
        						+ I18n.format("info.research.quest_missing", new Object[]{missingQuest + display + bad})
        						+ TextFormatting.RESET);
        			}
	        	}
	        }
            
            // Lore reqs?
            if (research.getRequiredLore() != null) {
            	for (String loreKey : research.getRequiredLore()) {
            		ILoreTagged loreItem = LoreRegistry.instance().lookup(loreKey);
            		if (loreItem != null) {
            			if (!lore.contains(loreItem)) {
            				if (!first) {
            					first = true;
            					tooltip.add("");
            				}
            				
            				tooltip.add(bad
            						+ I18n.format("info.research.lore_missing", new Object[]{missingLore + loreItem.getLoreDisplayName() + bad})
            						+ TextFormatting.RESET);
            			}
            		}
            	}
            }
            
            
            if (this.state == ResearchState.INACTIVE && researchPoints > 0 && NostrumMagica.canPurchaseResearch(player, research)) {
            	tooltip.add("");
            	tooltip.add(TextFormatting.GREEN + I18n.format("info.research.purchase") + TextFormatting.RESET);
            } else if (this.state == ResearchState.COMPLETED) {
            	tooltip.add("");
            	tooltip.add(TextFormatting.GREEN + I18n.format("info.research.view") + TextFormatting.RESET);
            }
            
            return tooltip;
		}
	}
    
    private static int HeightForSize(Size size) {
		switch (size) {
		case GIANT:
			return BUTTON_RESEARCH_GIANT_HEIGHT;
		case LARGE:
			return BUTTON_RESEARCH_LARGE_HEIGHT;
		case NORMAL:
		default:
			return BUTTON_RESEARCH_SMALL_HEIGHT;
		}
	}
	
	private static int WidthForSize(Size size) {
		switch (size) {
		case GIANT:
			return BUTTON_RESEARCH_GIANT_WIDTH;
		case LARGE:
			return BUTTON_RESEARCH_LARGE_WIDTH;
		case NORMAL:
		default:
			return BUTTON_RESEARCH_SMALL_WIDTH;
		}
	}
	
	public static void resetSeenCache() {
		seenResearch = null;
		newResearch = null;
	}
}
