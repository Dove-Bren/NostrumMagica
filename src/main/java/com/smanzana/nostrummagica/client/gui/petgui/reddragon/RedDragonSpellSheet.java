package com.smanzana.nostrummagica.client.gui.petgui.reddragon;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.entity.dragon.DragonGambit;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity.RedDragonSpellInventory;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.SpellScroll;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.util.RenderFuncs;
import com.smanzana.petcommand.api.client.container.IPetContainer;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;
import com.smanzana.petcommand.api.client.petgui.PetGUIRenderHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class RedDragonSpellSheet implements IPetGUISheet<TameRedDragonEntity> {
	
	private static final ResourceLocation DRAGON_ICON_TEXT = NostrumMagica.Loc("textures/gui/container/dragon_gui.png");
	
	public static int GUI_TEX_TOGGLE_HOFFSET = 0;
	public static int GUI_TEX_TOGGLE_VOFFSET = 18;
	public static int GUI_TEX_TOGGLE_LENGTH = 10;
	private static int GUI_TEX_WIDTH = 64;
	private static int GUI_TEX_HEIGHT = 64;
	
	// Position stuff.
	private static final int cellWidth = 18;
	private static final int invRow = 9;
	private static final int invWidth = cellWidth * invRow;
	private static int leftOffset;
	private static final int dragonTopOffset = 10;
	private static final int playerInvSize = 27 + 9;
	private static final int rowMargin = 2;
	private static final int rowHMargin = 8 + GUI_TEX_TOGGLE_LENGTH;
	private static final int toggleSize = GUI_TEX_TOGGLE_LENGTH;
	private static final int rowIncr = cellWidth + toggleSize + rowMargin;
	
	private TameRedDragonEntity dragon;
	private RedDragonSpellInventory dragonInv;
	private IInventory playerInv;
	private IPetContainer<TameRedDragonEntity> container;
	private int width;
	private int height;
	private int offsetX;
	private int offsetY;
	
	public RedDragonSpellSheet(TameRedDragonEntity dragon) {
		this.dragon = dragon;
	}
	
	private void initSlots() {
//		final int cellWidth = 18;
//		final int invRow = 9;
//		final int invWidth = cellWidth * invRow;
//		final int leftOffset = (width - invWidth) / 2;
//		final int dragonTopOffset = 10;
//		final int playerInvSize = 27 + 9; // Chop off equipment slots!
//		final int rowMargin = 2;
//		final int rowHMargin = 8 + PetGUI.GUI_TEX_TOGGLE_LENGTH;
//		final int toggleSize = PetGUI.GUI_TEX_TOGGLE_LENGTH;
//		final int rowIncr = cellWidth + toggleSize + rowMargin;
		
		int lastCount = 0;
		
		// Target slots
		NonNullList<ItemStack> slots = dragonInv.getTargetSpells();
		SpellSlot lastSlot = null;
		for (int i = 0; i < slots.size() && i < RedDragonSpellInventory.MaxSpellsPerCategory; i++) {
			SpellSlot slotIn = new SpellSlot(this, lastSlot, this.dragon, dragonInv, i,
					i + lastCount, leftOffset + offsetX + ((cellWidth + rowHMargin) * (i % invRow)), dragonTopOffset + offsetY);
			lastSlot = slotIn;
			container.addSheetSlot(slotIn);
		}
		
		lastCount += slots.size();
		
		// Self slots
		slots = dragonInv.getSelfSpells();
		lastSlot = null;
		for (int i = 0; i < slots.size(); i++) {
			SpellSlot slotIn = new SpellSlot(this, lastSlot, this.dragon, dragonInv, i,
						i + lastCount, leftOffset + offsetX + ((cellWidth + rowHMargin) * (i % invRow)), dragonTopOffset + offsetY + rowIncr);
			lastSlot = slotIn;
			container.addSheetSlot(slotIn);
		}
		lastCount += slots.size();
		
		// Ally slots
		slots = dragonInv.getAllySpells();
		lastSlot = null;
		for (int i = 0; i < slots.size(); i++) {
			SpellSlot slotIn = new SpellSlot(this, lastSlot, this.dragon, dragonInv, i,
					i + lastCount, leftOffset + offsetX + ((cellWidth + rowHMargin) * (i % invRow)), dragonTopOffset + offsetY + rowIncr + rowIncr);
			lastSlot = slotIn;
			container.addSheetSlot(slotIn);
		}
		lastCount += slots.size();

		final int playerTopOffset = dragonTopOffset + (rowIncr * 3) + 10;
		for (int i = 0; i < playerInvSize; i++) {
			Slot slotIn = new Slot(playerInv, (i + 9) % 36, leftOffset + offsetX + (cellWidth * (i % invRow)),
					(i < 27 ? 0 : 10) + playerTopOffset + offsetY + (cellWidth * (i / invRow)));
			container.addSheetSlot(slotIn);
		}
	}
	
	@Override
	public void showSheet(TameRedDragonEntity dragon, PlayerEntity player, IPetContainer<TameRedDragonEntity> container, int width, int height, int offsetX, int offsetY) {
		this.container = container;
		this.dragonInv = this.dragon.getSpellInventory();
		this.playerInv = player.inventory;
		this.width = width;
		this.height = height;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		
		leftOffset = (width - invWidth) / 2;
		initSlots();
		
		if (!player.level.isClientSide) {
			this.sendAllGambits(dragonInv.getAllGambits());
		}
	}

	@Override
	public void hideSheet(TameRedDragonEntity dragon, PlayerEntity player, IPetContainer<TameRedDragonEntity> container) {
		container.clearSlots();
	}
	
	private void drawCell(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int x, int y) {
		final int cellWidth = 18;
		matrixStackIn.pushPose();
		matrixStackIn.translate(x, y, 0);
		PetGUIRenderHelper.DrawSingleSlot(matrixStackIn, cellWidth, cellWidth);
		matrixStackIn.popPose();
	}
	
	private static @Nonnull ItemStack scrollShadow = ItemStack.EMPTY;
	
	private void drawNextCell(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int x, int y) {
		final int cellWidth = 18;
		
		if (scrollShadow.isEmpty()) {
			scrollShadow = new ItemStack(NostrumItems.spellScroll);
		}
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(x, y, 0);
		PetGUIRenderHelper.DrawSingleSlot(matrixStackIn, cellWidth, cellWidth);

		matrixStackIn.translate(1, 1, 0);
		RenderFuncs.RenderGUIItem(scrollShadow, matrixStackIn, 0, 0, -100);
			
		int color = 0x55FFFFFF;
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, 0, 1);
		RenderFuncs.drawRect(matrixStackIn, 
				0, 0,
				16, 16,
				color);
		matrixStackIn.popPose();
		matrixStackIn.popPose();
	}
	
	private void drawShadowCell(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int x, int y) {
		final int cellWidth = 18;
		matrixStackIn.pushPose();
		matrixStackIn.translate(x, y, 0);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.color4f(.7f, .71f, .7f, .4f);
		PetGUIRenderHelper.DrawSingleSlot(matrixStackIn, cellWidth, cellWidth);
		RenderSystem.color4f(1f, 1f, 1f, 1f);
		RenderSystem.disableBlend();
		matrixStackIn.popPose();
	}
	
	private void drawFadedCell(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int x, int y) {
		final int cellWidth = 18;
		matrixStackIn.pushPose();
		matrixStackIn.translate(x, y, 0);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.color4f(.2f, .2f, .2f, .4f);
		PetGUIRenderHelper.DrawSingleSlot(matrixStackIn, cellWidth, cellWidth);
		RenderSystem.color4f(1f, 1f, 1f, 1f);
		RenderSystem.disableBlend();
		matrixStackIn.popPose();
	}
	
	private void drawGambit(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int x, int y, DragonGambit gambit) {
		int texOffsetX = 0;
		int texOffsetY = 0;
		if (gambit != null) {
			texOffsetX = gambit.getTexOffsetX();
			texOffsetY = gambit.getTexOffsetY();
		}
		mc.getTextureManager().bind(DRAGON_ICON_TEXT);
		RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, x,
				y,
				GUI_TEX_TOGGLE_HOFFSET + texOffsetX,
				GUI_TEX_TOGGLE_VOFFSET + texOffsetY,
				toggleSize,	toggleSize, GUI_TEX_WIDTH, GUI_TEX_HEIGHT);
	}
	
	private void drawRow(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int x, int y, String title, NonNullList<ItemStack> slots, DragonGambit gambits[]) {
		
		final int usedCount = dragonInv.getUsedSlots();
		final int extraCount = Math.max(0, this.dragon.getMagicMemorySize() - usedCount);
		int count;
		int ghostCount;
		
		mc.font.draw(matrixStackIn, title, 5, y + 1 + (cellWidth - mc.font.lineHeight) / 2, 0xFFFFFFFF);
		
		count = 0;
		ghostCount = 0;
		for (int i = 0; i < slots.size(); i++) {
			if (!slots.get(i).isEmpty()) {
				count++;
				this.drawCell(matrixStackIn, mc, partialTicks, x + ((cellWidth + rowHMargin) * (i % invRow)), y);
				drawGambit(matrixStackIn, mc, partialTicks, x + ((cellWidth + rowHMargin) * (i % invRow)) + cellWidth, y + 1, gambits[i]);
			} else {
				// It's empty. The first empty should allow you to plae st uff, while the others are for show
				if (count < RedDragonSpellInventory.MaxSpellsPerCategory && ghostCount < extraCount) {
					if (ghostCount == 0) {
						this.drawNextCell(matrixStackIn, mc, partialTicks, x + ((cellWidth + rowHMargin) * (i % invRow)), y);
					} else {
						this.drawShadowCell(matrixStackIn, mc, partialTicks, x + ((cellWidth + rowHMargin) * (i % invRow)), y);
					}
					ghostCount++;
				} else if (count < RedDragonSpellInventory.MaxSpellsPerCategory) {
					drawFadedCell(matrixStackIn, mc, partialTicks, x + ((cellWidth + rowHMargin) * (i % invRow)), y);
				} else {
					break;
				}
			}
		}
	}

	@Override
	public void draw(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		// Draw sheet
		matrixStackIn.pushPose();
		{
//			final int cellWidth = 18;
//			final int invRow = 9;
//			final int invWidth = cellWidth * invRow;
//			final int leftOffset = (width - invWidth) / 2;
//			final int dragonTopOffset = 10;
//			final int playerInvSize = 27 + 9;
//			final int rowMargin = 2;
//			final int rowHMargin = 8 + PetGUI.GUI_TEX_TOGGLE_LENGTH;
//			final int toggleSize = PetGUI.GUI_TEX_TOGGLE_LENGTH;
//			final int rowIncr = cellWidth + toggleSize + rowMargin;
			
			// Target slots
			drawRow(matrixStackIn, mc, partialTicks, leftOffset - 1, dragonTopOffset - 1, "Enemy", dragonInv.getTargetSpells(), dragonInv.getTargetGambits());
			
			// Self slots
			drawRow(matrixStackIn, mc, partialTicks, leftOffset - 1, dragonTopOffset - 1 + rowIncr, "Self", dragonInv.getSelfSpells(), dragonInv.getSelfGambits());
			
			// Ally slots
			drawRow(matrixStackIn, mc, partialTicks, leftOffset - 1, dragonTopOffset - 1 + rowIncr + rowIncr, "Ally", dragonInv.getAllySpells(), dragonInv.getAllyGambits());
			
			final int playerTopOffset = dragonTopOffset + (rowIncr * 3) + 10;
			matrixStackIn.pushPose();
			matrixStackIn.translate(leftOffset - 1, playerTopOffset - 1, 0);
			// ... First 27
			PetGUIRenderHelper.DrawSlots(matrixStackIn, cellWidth, cellWidth, Math.min(27, playerInvSize), invRow);
			
			// Remaining (toolbar)
			final int yOffset = ((Math.min(27, playerInvSize) / invRow)) * cellWidth;
			matrixStackIn.translate(0, 10 + yOffset, 0);
			PetGUIRenderHelper.DrawSlots(matrixStackIn, cellWidth, cellWidth, Math.max(0, playerInvSize-27), invRow);
			
			matrixStackIn.popPose();
		}
		matrixStackIn.popPose();
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void overlay(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		
		// Draw spell icon overlays
		{
			/*
			 // Target slots
			drawRow(mc, partialTicks, leftOffset - 1, dragonTopOffset - 1, "Enemy", dragonInv.getTargetSpells(), dragonInv.getTargetGambits());
			
			// Self slots
			drawRow(mc, partialTicks, leftOffset - 1, dragonTopOffset - 1 + rowIncr, "Self", dragonInv.getSelfSpells(), dragonInv.getSelfGambits());
			
			// Ally slots
			drawRow(mc, partialTicks, leftOffset - 1, dragonTopOffset - 1 + rowIncr + rowIncr, "Ally", dragonInv.getAllySpells(), dragonInv.getAllyGambits());
			 */
			NonNullList<ItemStack> scrolls;
			int x;
			int y;
			final int innerCellWidth = RedDragonSpellSheet.cellWidth - 2;
			final long period = 2000;
			final float alpha = .85f + .1f * (float) Math.sin(Math.PI * 2 * (float) (System.currentTimeMillis() % period) / period);
			
			matrixStackIn.pushPose();
			matrixStackIn.translate(0, 0, 251);
			
			// Target
			scrolls = dragonInv.getTargetSpells();
			x = leftOffset;
			y = dragonTopOffset;
			for (int i = 0; i < scrolls.size(); i++) {
				ItemStack scroll = scrolls.get(i);
				if (scroll.isEmpty()) {
					break;
				}
				
				if (scroll.getItem() instanceof SpellScroll) {
					Spell spell = SpellScroll.GetSpell(scroll);
					if (spell != null) {
						final SpellIcon icon = SpellIcon.get(spell.getIconIndex());
						icon.render(mc, matrixStackIn, x + i * (rowHMargin + cellWidth), y, innerCellWidth, innerCellWidth, 1f, 1f, 1f, alpha);
					}
				}
			}
			
			// Self
			scrolls = dragonInv.getSelfSpells();
			x = leftOffset;
			y = dragonTopOffset + rowIncr;
			for (int i = 0; i < scrolls.size(); i++) {
				ItemStack scroll = scrolls.get(i);
				if (scroll.isEmpty()) {
					break;
				}
				
				if (scroll.getItem() instanceof SpellScroll) {
					Spell spell = SpellScroll.GetSpell(scroll);
					if (spell != null) {
						final SpellIcon icon = SpellIcon.get(spell.getIconIndex());
						icon.render(mc, matrixStackIn, x + i * (rowHMargin + cellWidth), y, innerCellWidth, innerCellWidth, 1f, 1f, 1f, alpha);
					}
				}
			}
			
			// Ally
			scrolls = dragonInv.getAllySpells();
			x = leftOffset;
			y = dragonTopOffset + rowIncr + rowIncr;
			for (int i = 0; i < scrolls.size(); i++) {
				ItemStack scroll = scrolls.get(i);
				if (scroll.isEmpty()) {
					break;
				}
				
				if (scroll.getItem() instanceof SpellScroll) {
					Spell spell = SpellScroll.GetSpell(scroll);
					if (spell != null) {
						final SpellIcon icon = SpellIcon.get(spell.getIconIndex());
						icon.render(mc, matrixStackIn, x + i * (rowHMargin + cellWidth), y, innerCellWidth, innerCellWidth, 1f, 1f, 1f, alpha);
					}
				}
			}
			
			matrixStackIn.popPose();
		}
		
		// Draw gambit overlay
		do {
			if (mouseY < dragonTopOffset - 1 || mouseY > dragonTopOffset - 1 + rowIncr + rowIncr + cellWidth) {
				break;
			}
			
			// Y checking
			int row = -1;
			int cellY = 0;
			for (int j = 0; j < 3; j++) {
				// Check each row to see if we match its y coords
				final int minY = dragonTopOffset + (j * rowIncr);
				final int maxY = minY + toggleSize;
				
				if (mouseY >= minY && mouseY <= maxY) {
					row = j;
					cellY = minY;
					break;
				}
			}
			
			if (row == -1) {
				break;
			}
			
			// X checking
			int col = -1;
			int cellX = 0;
			for (int i = 0; i < RedDragonSpellInventory.MaxSpellsPerCategory; i++) {
				// For each column, check if we hit the right x
				final int minX = leftOffset - 1 + ((cellWidth + rowHMargin) * (i % invRow)) + cellWidth;
				final int maxX = minX + toggleSize;
				
				if (mouseX >= minX && mouseX <= maxX) {
					col = i;
					cellX = minX;
					break;
				}
			}
			
			if (col == -1) {
				break;
			}
			
			int index = (row * RedDragonSpellInventory.MaxSpellsPerCategory) + col;
			if (dragonInv.getItem(index).isEmpty()) {
				break;
			}
			
			matrixStackIn.pushPose();
			matrixStackIn.translate(0, 0, 201);
			RenderFuncs.drawRect(matrixStackIn, cellX, cellY, cellX + toggleSize, cellY + toggleSize, 0x50FFFFFF);
			
			DragonGambit gambit = dragonInv.getAllGambits()[index];
			if (gambit != null) {
				GuiUtils.drawHoveringText(matrixStackIn, gambit.getDesc(), mouseX, mouseY, this.width, this.height, 150, mc.font);
			}
			
			matrixStackIn.popPose();
		} while (false);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		// Try to see if they're clicking on a gambit button
		
		// TODO pull out to a helper func so drawing and clicking cna use
		
		// Outer bounds
		if (mouseY < dragonTopOffset - 1 || mouseY > dragonTopOffset - 1 + rowIncr + rowIncr + cellWidth) {
			return false;
		}
		
		// Y checking
		int row = -1;
		for (int j = 0; j < 3; j++) {
			// Check each row to see if we match its y coords
			final int minY = dragonTopOffset + (j * rowIncr);
			final int maxY = minY + toggleSize;
			
			if (mouseY >= minY && mouseY <= maxY) {
				row = j;
				break;
			}
		}
		
		if (row == -1) {
			return false;
		}
		
		// X checking
		int col = -1;
		for (int i = 0; i < RedDragonSpellInventory.MaxSpellsPerCategory; i++) {
			// For each column, check if we hit the right x
			final int minX = leftOffset - 1 + ((cellWidth + rowHMargin) * (i % invRow)) + cellWidth;
			final int maxX = minX + toggleSize;
			
			if (mouseX >= minX && mouseX <= maxX) {
				col = i;
				break;
			}
		}
		
		if (col == -1) {
			return false;
		}
		
		int index = (row * RedDragonSpellInventory.MaxSpellsPerCategory) + col;
		if (dragonInv.getItem(index).isEmpty()) {
			return false;
		}
		
		sendGambitCycle(index, mouseButton == 0);
		NostrumMagicaSounds.UI_TICK.play(NostrumMagica.instance.proxy.getPlayer());
		return true;
	}
	
	private static enum SheetMessageType {
		REQ_GAMBITS,
		REV_GAMBIT,
		REV_ALL_GAMBITS,
		GAMBIT_CYCLE;
	}
	
//	private void sendGambitReq(boolean all, int index) {
//		CompoundNBT nbt = new CompoundNBT();
//		nbt.putString("type", SheetMessageType.REQ_GAMBITS.name());
//		nbt.putBoolean("all", all);
//		nbt.putInt("index", index);
//		this.container.sendSheetMessageToServer(nbt);
//	}
	
	private void sendGambitCycle(int index, boolean forward) {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putString("type", SheetMessageType.GAMBIT_CYCLE.name());
		nbt.putInt("index", index);
		nbt.putBoolean("forward", forward);
		this.container.sendSheetMessageToServer(nbt);
	}
	
	private void sendGambit(int index, DragonGambit gambit) {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putString("type", SheetMessageType.REV_GAMBIT.name());
		nbt.putInt("index", index);
		nbt.putString("gambit", gambit.name());
		this.container.sendSheetMessageToClient(nbt);
	}
	
	private void sendAllGambits(DragonGambit gambits[]) {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putString("type", SheetMessageType.REV_ALL_GAMBITS.name());
		
		ListNBT list = new ListNBT();
		for (int i = 0; i < gambits.length; i++) {
			StringNBT tag = StringNBT.valueOf(gambits[i].name());
			list.add(tag);
		}
		
		nbt.put("gambits", list);
		this.container.sendSheetMessageToClient(nbt);
	}
	
	// Client has requested information about 1 or all gambits
	private void receiveGambitReq(CompoundNBT nbt) {
		boolean all = nbt.getBoolean("all");
		
		if (all) {
			sendAllGambits(this.dragonInv.getAllGambits());
		} else {
			int index = nbt.getInt("index");
			sendGambit(index, this.dragonInv.getAllGambits()[index]);
		}
	}
	
	// Client has requested we cycle a gambit
	private void receiveGambitCycle(CompoundNBT nbt) {
		int index = nbt.getInt("index");
		boolean forward = nbt.getBoolean("forward");
		
		DragonGambit gambit = this.dragonInv.getAllGambits()[index];
		switch (gambit) {
		case ALWAYS:
			gambit = forward ? DragonGambit.HEALTH_CRITICAL : DragonGambit.FREQUENT;
			break;
		case HEALTH_CRITICAL:
			gambit = forward ? DragonGambit.HEALTH_LOW : DragonGambit.ALWAYS;
			break;
		case HEALTH_LOW:
			gambit = forward ? DragonGambit.MANA_LOW : DragonGambit.HEALTH_CRITICAL;
			break;
		case MANA_LOW:
			gambit = forward ? DragonGambit.OCCASIONAL : DragonGambit.HEALTH_LOW;
			break;
		case OCCASIONAL:
			gambit = forward ? DragonGambit.FREQUENT : DragonGambit.MANA_LOW;
			break;
		case FREQUENT:
			gambit = forward ? DragonGambit.ALWAYS : DragonGambit.OCCASIONAL;
			break;
		}
		this.dragonInv.setGambit(index, gambit);
		
		sendGambit(index, gambit);
	}
	
	// Server has sent info about a gambit
	private void receiveGambit(CompoundNBT nbt) {
		int index = nbt.getInt("index");
		String name = nbt.getString("gambit");
		DragonGambit gambit;
		try {
			gambit = DragonGambit.valueOf(name.toUpperCase());
		} catch (Exception e) {
			gambit = DragonGambit.ALWAYS;
		}
		
		this.dragonInv.setGambit(index, gambit);
	}
	
	// Server has sent information about all gambits
	private void receiveAllGambits(CompoundNBT nbt) {
		ListNBT list = nbt.getList("gambits", NBT.TAG_STRING);
		if (list != null) {
			for (int i = 0; i < dragonInv.getContainerSize() && i < list.size(); i++) {
				String name = list.getString(i);
				DragonGambit gambit;
				try {
					gambit = DragonGambit.valueOf(name.toUpperCase());
				} catch (Exception  e) {
					gambit = DragonGambit.ALWAYS;
				}
				
				dragonInv.setGambit(i, gambit);
			}
		}
	}

	@Override
	public void handleMessage(CompoundNBT data) {
		String typeKey = data.getString("type");
		SheetMessageType type;
		try {
			type = SheetMessageType.valueOf(typeKey.toUpperCase());
		} catch (Exception e) {
			return;
		}
		
		switch (type) {
		case GAMBIT_CYCLE:
			receiveGambitCycle(data);
			break;
		case REQ_GAMBITS:
			receiveGambitReq(data);
			break;
		case REV_ALL_GAMBITS:
			receiveAllGambits(data);
			break;
		case REV_GAMBIT:
			receiveGambit(data);
			break;
		}
	}

	@Override
	public String getButtonText() {
		return "Spells";
	}

	@Override
	public boolean shouldShow(TameRedDragonEntity dragon, IPetContainer<TameRedDragonEntity> container) {
		return this.dragon.canManageSpells();
	}
	
	private static final class SpellSlot extends Slot {
		
		private RedDragonSpellSheet sheet;
		private int subIndex;
		private TameRedDragonEntity dragon;
		private RedDragonSpellInventory inventory;
		private SpellSlot prev;
		
		public SpellSlot(RedDragonSpellSheet sheet, SpellSlot prev, TameRedDragonEntity dragon, RedDragonSpellInventory inventory, int subIndex, int globalIndex, int xPosition, int yPosition) {
			super(inventory, globalIndex, xPosition, yPosition);
			
			this.sheet = sheet;
			this.subIndex = subIndex;
			this.dragon = dragon;
			this.inventory = inventory;
			this.prev = prev;
		}
		
		@Override
		public boolean mayPlace(@Nonnull ItemStack stack) {
			if (!dragon.canManageSpells())
				return false;
			
			// Can always empty slot
			if (stack.isEmpty()) {
				return true;
			}
			
			// If somehow we're past the max spells in a category, do not allow anything in
			if (subIndex >= RedDragonSpellInventory.MaxSpellsPerCategory) {
				return false;
			}
			
			// Only scrolls with spells!
			if (!(stack.getItem() instanceof SpellScroll)) {
				return false;
			}
			
			if (SpellScroll.GetSpell(stack) == null) {
				return false;
			}
			
			// If our previous slow doesn't have something in it, can't put something in us!
			if (prev != null && !prev.hasItem()) {
				return false;
			}
			
			int capacity = dragon.getMagicMemorySize();
			int size = inventory.getUsedSlots();
			
			// If we already have too many spells, no slot can accept new ones
			if (size >= capacity) {
				return false;
			}
			
			return true;
		}
		
		@Override
		@OnlyIn(Dist.CLIENT)
		public boolean isActive() {
			if (subIndex >= RedDragonSpellInventory.MaxSpellsPerCategory) {
				return false;
			}
			
			if (this.hasItem()) {
				return true;
			}
			
			int capacity = dragon.getMagicMemorySize();
			int size = inventory.getUsedSlots();
			
			if (size >= capacity) {
				return false;
			}
			
			int leftover = capacity - size;
			for (; leftover > 0; leftover--) {
				if (prev == null) {
					return true;
				}
				if (prev.hasItem()) {
					return true;
				}
			}
			
			return false;
		}
		
		@Override
		public void setChanged() {
			super.setChanged();
			sheet.dragonInv.clean();
			
			if (!this.dragon.level.isClientSide) {
				sheet.sendAllGambits(sheet.dragonInv.getAllGambits());
			}
			sheet.container.clearSlots();
			sheet.initSlots();
		}
	}

}
