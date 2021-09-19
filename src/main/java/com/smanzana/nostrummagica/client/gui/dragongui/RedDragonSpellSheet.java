package com.smanzana.nostrummagica.client.gui.dragongui;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.client.gui.dragongui.TamedDragonGUI.DragonContainer;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonGambit;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed.RedDragonSpellInventory;
import com.smanzana.nostrummagica.entity.dragon.ITameDragon;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RedDragonSpellSheet implements IDragonGUISheet {
	
	// Position stuff.
	private static final int cellWidth = 18;
	private static final int invRow = 9;
	private static final int invWidth = cellWidth * invRow;
	private static int leftOffset;
	private static final int dragonTopOffset = 10;
	private static final int playerInvSize = 27 + 9;
	private static final int rowMargin = 2;
	private static final int rowHMargin = 8 + TamedDragonGUI.GUI_TEX_TOGGLE_LENGTH;
	private static final int toggleSize = TamedDragonGUI.GUI_TEX_TOGGLE_LENGTH;
	private static final int rowIncr = cellWidth + toggleSize + rowMargin;
	
	private EntityTameDragonRed dragon;
	private RedDragonSpellInventory dragonInv;
	private IInventory playerInv;
	private DragonContainer container;
	private int width;
	private int height;
	private int offsetX;
	private int offsetY;
	
	public RedDragonSpellSheet(EntityTameDragonRed dragon) {
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
//		final int rowHMargin = 8 + TamedDragonGUI.GUI_TEX_TOGGLE_LENGTH;
//		final int toggleSize = TamedDragonGUI.GUI_TEX_TOGGLE_LENGTH;
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
	public void showSheet(ITameDragon dragon, EntityPlayer player, DragonContainer container, int width, int height, int offsetX, int offsetY) {
		this.container = container;
		this.dragonInv = this.dragon.getSpellInventory();
		this.playerInv = player.inventory;
		this.width = width;
		this.height = height;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		
		leftOffset = (width - invWidth) / 2;
		initSlots();
		
		if (!player.world.isRemote) {
			this.sendAllGambits(dragonInv.getAllGambits());
		}
	}

	@Override
	public void hideSheet(ITameDragon dragon, EntityPlayer player, DragonContainer container) {
		container.clearSlots();
	}
	
	private void drawCell(Minecraft mc, float partialTicks, int x, int y) {
		final int cellWidth = 18;
		GlStateManager.color(1f, 1f, 1f, 1f);
		Gui.drawModalRectWithCustomSizedTexture(x, y,
				TamedDragonGUI.GUI_TEX_CELL_HOFFSET, TamedDragonGUI.GUI_TEX_CELL_VOFFSET,
				cellWidth, cellWidth,
				256, 256);
	}
	
	private static @Nonnull ItemStack scrollShadow = ItemStack.EMPTY;
	
	private void drawNextCell(Minecraft mc, float partialTicks, int x, int y) {
		final int cellWidth = 18;
		
		if (scrollShadow.isEmpty()) {
			scrollShadow = new ItemStack(SpellScroll.instance());
		}
		
		GlStateManager.color(1f, 1f, 1f, 1f);
		Gui.drawModalRectWithCustomSizedTexture(x, y,
				TamedDragonGUI.GUI_TEX_CELL_HOFFSET + cellWidth, TamedDragonGUI.GUI_TEX_CELL_VOFFSET,
				cellWidth, cellWidth,
				256, 256);
		
		mc.getTextureManager().bindTexture(TamedDragonGUI.DragonGUI.TEXT);
	}
	
	private void drawShadowCell(Minecraft mc, float partialTicks, int x, int y) {
		final int cellWidth = 18;
		GlStateManager.color(.7f, .71f, .7f, .4f);
		Gui.drawModalRectWithCustomSizedTexture(x, y,
				TamedDragonGUI.GUI_TEX_CELL_HOFFSET, TamedDragonGUI.GUI_TEX_CELL_VOFFSET,
				cellWidth, cellWidth,
				256, 256);
	}
	
	private void drawFadedCell(Minecraft mc, float partialTicks, int x, int y) {
		final int cellWidth = 18;
		GlStateManager.color(.2f, .2f, .2f, .4f);
		Gui.drawModalRectWithCustomSizedTexture(x, y,
				TamedDragonGUI.GUI_TEX_CELL_HOFFSET, TamedDragonGUI.GUI_TEX_CELL_VOFFSET,
				cellWidth, cellWidth,
				256, 256);
	}
	
	private void drawGambit(Minecraft mc, float partialTicks, int x, int y, EntityDragonGambit gambit) {
		int texOffset = 0;
		if (gambit != null) {
			texOffset = gambit.getTexOffsetX();
		}
		mc.getTextureManager().bindTexture(TamedDragonGUI.DragonGUI.TEXT);
		GlStateManager.color(1f, 1f, 1f, 1f);
		Gui.drawModalRectWithCustomSizedTexture(x, y,
				TamedDragonGUI.GUI_TEX_TOGGLE_HOFFSET + texOffset,
				TamedDragonGUI.GUI_TEX_TOGGLE_VOFFSET,
				toggleSize, toggleSize,
				256, 256);
	}
	
	private void drawRow(Minecraft mc, float partialTicks, int x, int y, String title, NonNullList<ItemStack> slots, EntityDragonGambit gambits[]) {
		
		final int usedCount = dragonInv.getUsedSlots();
		final int extraCount = Math.max(0, this.dragon.getMagicMemorySize() - usedCount);
		int count;
		int ghostCount;
		
		mc.fontRenderer.drawString(title, 5, y + 1 + (cellWidth - mc.fontRenderer.FONT_HEIGHT) / 2, 0xFFFFFFFF);
		mc.getTextureManager().bindTexture(TamedDragonGUI.DragonGUI.TEXT);
		
		count = 0;
		ghostCount = 0;
		for (int i = 0; i < slots.size(); i++) {
			if (!slots.get(i).isEmpty()) {
				count++;
				this.drawCell(mc, partialTicks, x + ((cellWidth + rowHMargin) * (i % invRow)), y);
				drawGambit(mc, partialTicks, x + ((cellWidth + rowHMargin) * (i % invRow)) + cellWidth, y + 1, gambits[i]);
			} else {
				// It's empty. The first empty should allow you to plae st uff, while the others are for show
				if (count < RedDragonSpellInventory.MaxSpellsPerCategory && ghostCount < extraCount) {
					if (ghostCount == 0) {
						this.drawNextCell(mc, partialTicks, x + ((cellWidth + rowHMargin) * (i % invRow)), y);
					} else {
						this.drawShadowCell(mc, partialTicks, x + ((cellWidth + rowHMargin) * (i % invRow)), y);
					}
					ghostCount++;
				} else if (count < RedDragonSpellInventory.MaxSpellsPerCategory) {
					drawFadedCell(mc, partialTicks, x + ((cellWidth + rowHMargin) * (i % invRow)), y);
				} else {
					break;
				}
			}
		}
	}

	@Override
	public void draw(Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
		
		// Draw sheet
		GlStateManager.pushMatrix();
		{
//			final int cellWidth = 18;
//			final int invRow = 9;
//			final int invWidth = cellWidth * invRow;
//			final int leftOffset = (width - invWidth) / 2;
//			final int dragonTopOffset = 10;
//			final int playerInvSize = 27 + 9;
//			final int rowMargin = 2;
//			final int rowHMargin = 8 + TamedDragonGUI.GUI_TEX_TOGGLE_LENGTH;
//			final int toggleSize = TamedDragonGUI.GUI_TEX_TOGGLE_LENGTH;
//			final int rowIncr = cellWidth + toggleSize + rowMargin;
			
			// Target slots
			drawRow(mc, partialTicks, leftOffset - 1, dragonTopOffset - 1, "Enemy", dragonInv.getTargetSpells(), dragonInv.getTargetGambits());
			
			// Self slots
			drawRow(mc, partialTicks, leftOffset - 1, dragonTopOffset - 1 + rowIncr, "Self", dragonInv.getSelfSpells(), dragonInv.getSelfGambits());
			
			// Ally slots
			drawRow(mc, partialTicks, leftOffset - 1, dragonTopOffset - 1 + rowIncr + rowIncr, "Ally", dragonInv.getAllySpells(), dragonInv.getAllyGambits());
			
			final int playerTopOffset = dragonTopOffset + (rowIncr * 3) + 10;
			for (int i = 0; i < playerInvSize; i++) {
				GlStateManager.color(1f, 1f, 1f, 1f);
				Gui.drawModalRectWithCustomSizedTexture(leftOffset - 1 + (cellWidth * (i % invRow)), (i < 27 ? 0 : 10) + playerTopOffset - 1 + (cellWidth * (i / invRow)),
						TamedDragonGUI.GUI_TEX_CELL_HOFFSET, TamedDragonGUI.GUI_TEX_CELL_VOFFSET,
						cellWidth, cellWidth,
						256, 256);
			}
			
			GlStateManager.popMatrix();
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void overlay(Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		
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
			final float alpha = .85f + .1f * (float) Math.sin(Math.PI * 2 * (float) (Minecraft.getSystemTime() % period) / period);
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 251);
			
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
					Spell spell = SpellScroll.getSpell(scroll);
					if (spell != null) {
						final SpellIcon icon = SpellIcon.get(spell.getIconIndex());
						GlStateManager.color(1f, 1f, 1f, alpha);
						icon.render(mc, x + i * (rowHMargin + cellWidth), y, innerCellWidth, innerCellWidth);
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
					Spell spell = SpellScroll.getSpell(scroll);
					if (spell != null) {
						final SpellIcon icon = SpellIcon.get(spell.getIconIndex());
						GlStateManager.color(1f, 1f, 1f, alpha);
						icon.render(mc, x + i * (rowHMargin + cellWidth), y, innerCellWidth, innerCellWidth);
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
					Spell spell = SpellScroll.getSpell(scroll);
					if (spell != null) {
						final SpellIcon icon = SpellIcon.get(spell.getIconIndex());
						GlStateManager.color(1f, 1f, 1f, alpha);
						icon.render(mc, x + i * (rowHMargin + cellWidth), y, innerCellWidth, innerCellWidth);
					}
				}
			}
			
			GlStateManager.popMatrix();
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
			if (dragonInv.getStackInSlot(index).isEmpty()) {
				break;
			}
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 201);
			Gui.drawRect(cellX, cellY, cellX + toggleSize, cellY + toggleSize, 0x50FFFFFF);
			
			EntityDragonGambit gambit = dragonInv.getAllGambits()[index];
			if (gambit != null) {
				GuiUtils.drawHoveringText(gambit.getDesc(), mouseX, mouseY, this.width, this.height, 150, mc.fontRenderer);
			}
			
			GlStateManager.popMatrix();
		} while (false);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		// Try to see if they're clicking on a gambit button
		
		// TODO pull out to a helper func so drawing and clicking cna use
		
		// Outer bounds
		if (mouseY < dragonTopOffset - 1 || mouseY > dragonTopOffset - 1 + rowIncr + rowIncr + cellWidth) {
			return;
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
			return;
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
			return;
		}
		
		int index = (row * RedDragonSpellInventory.MaxSpellsPerCategory) + col;
		if (dragonInv.getStackInSlot(index).isEmpty()) {
			return;
		}
		
		sendGambitCycle(index, mouseButton == 0);
		NostrumMagicaSounds.UI_TICK.play(NostrumMagica.proxy.getPlayer());
	}
	
	private static enum SheetMessageType {
		REQ_GAMBITS,
		REV_GAMBIT,
		REV_ALL_GAMBITS,
		GAMBIT_CYCLE;
	}
	
//	private void sendGambitReq(boolean all, int index) {
//		NBTTagCompound nbt = new NBTTagCompound();
//		nbt.setString("type", SheetMessageType.REQ_GAMBITS.name());
//		nbt.setBoolean("all", all);
//		nbt.setInteger("index", index);
//		this.container.sendSheetMessageToServer(nbt);
//	}
	
	private void sendGambitCycle(int index, boolean forward) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("type", SheetMessageType.GAMBIT_CYCLE.name());
		nbt.setInteger("index", index);
		nbt.setBoolean("forward", forward);
		this.container.sendSheetMessageToServer(nbt);
	}
	
	private void sendGambit(int index, EntityDragonGambit gambit) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("type", SheetMessageType.REV_GAMBIT.name());
		nbt.setInteger("index", index);
		nbt.setString("gambit", gambit.name());
		this.container.sendSheetMessageToClient(nbt);
	}
	
	private void sendAllGambits(EntityDragonGambit gambits[]) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("type", SheetMessageType.REV_ALL_GAMBITS.name());
		
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < gambits.length; i++) {
			NBTTagString tag = new NBTTagString(gambits[i].name());
			list.appendTag(tag);
		}
		
		nbt.setTag("gambits", list);
		this.container.sendSheetMessageToClient(nbt);
	}
	
	// Client has requested information about 1 or all gambits
	private void receiveGambitReq(NBTTagCompound nbt) {
		boolean all = nbt.getBoolean("all");
		
		if (all) {
			sendAllGambits(this.dragonInv.getAllGambits());
		} else {
			int index = nbt.getInteger("index");
			sendGambit(index, this.dragonInv.getAllGambits()[index]);
		}
	}
	
	// Client has requested we cycle a gambit
	private void receiveGambitCycle(NBTTagCompound nbt) {
		int index = nbt.getInteger("index");
		boolean forward = nbt.getBoolean("forward");
		
		EntityDragonGambit gambit = this.dragonInv.getAllGambits()[index];
		switch (gambit) {
		case ALWAYS:
			gambit = forward ? EntityDragonGambit.HEALTH_CRITICAL : EntityDragonGambit.FREQUENT;
			break;
		case HEALTH_CRITICAL:
			gambit = forward ? EntityDragonGambit.HEALTH_LOW : EntityDragonGambit.ALWAYS;
			break;
		case HEALTH_LOW:
			gambit = forward ? EntityDragonGambit.MANA_LOW : EntityDragonGambit.HEALTH_CRITICAL;
			break;
		case MANA_LOW:
			gambit = forward ? EntityDragonGambit.OCCASIONAL : EntityDragonGambit.HEALTH_LOW;
			break;
		case OCCASIONAL:
			gambit = forward ? EntityDragonGambit.FREQUENT : EntityDragonGambit.MANA_LOW;
			break;
		case FREQUENT:
			gambit = forward ? EntityDragonGambit.ALWAYS : EntityDragonGambit.OCCASIONAL;
			break;
		}
		this.dragonInv.setGambit(index, gambit);
		
		sendGambit(index, gambit);
	}
	
	// Server has sent info about a gambit
	private void receiveGambit(NBTTagCompound nbt) {
		int index = nbt.getInteger("index");
		String name = nbt.getString("gambit");
		EntityDragonGambit gambit;
		try {
			gambit = EntityDragonGambit.valueOf(name.toUpperCase());
		} catch (Exception e) {
			gambit = EntityDragonGambit.ALWAYS;
		}
		
		this.dragonInv.setGambit(index, gambit);
	}
	
	// Server has sent information about all gambits
	private void receiveAllGambits(NBTTagCompound nbt) {
		NBTTagList list = nbt.getTagList("gambits", NBT.TAG_STRING);
		if (list != null) {
			for (int i = 0; i < dragonInv.getSizeInventory() && i < list.tagCount(); i++) {
				String name = list.getStringTagAt(i);
				EntityDragonGambit gambit;
				try {
					gambit = EntityDragonGambit.valueOf(name.toUpperCase());
				} catch (Exception  e) {
					gambit = EntityDragonGambit.ALWAYS;
				}
				
				dragonInv.setGambit(i, gambit);
			}
		}
	}

	@Override
	public void handleMessage(NBTTagCompound data) {
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
	public boolean shouldShow(ITameDragon dragon, DragonContainer container) {
		return this.dragon.canManageSpells();
	}
	
	private static final class SpellSlot extends Slot {
		
		private RedDragonSpellSheet sheet;
		private int subIndex;
		private EntityTameDragonRed dragon;
		private RedDragonSpellInventory inventory;
		private SpellSlot prev;
		
		public SpellSlot(RedDragonSpellSheet sheet, SpellSlot prev, EntityTameDragonRed dragon, RedDragonSpellInventory inventory, int subIndex, int globalIndex, int xPosition, int yPosition) {
			super(inventory, globalIndex, xPosition, yPosition);
			
			this.sheet = sheet;
			this.subIndex = subIndex;
			this.dragon = dragon;
			this.inventory = inventory;
			this.prev = prev;
		}
		
		@Override
		public boolean isItemValid(@Nonnull ItemStack stack) {
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
			
			if (SpellScroll.getSpell(stack) == null) {
				return false;
			}
			
			// If our previous slow doesn't have something in it, can't put something in us!
			if (prev != null && !prev.getHasStack()) {
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
		@SideOnly(Side.CLIENT)
		public boolean isEnabled() {
			if (subIndex >= RedDragonSpellInventory.MaxSpellsPerCategory) {
				return false;
			}
			
			if (this.getHasStack()) {
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
				if (prev.getHasStack()) {
					return true;
				}
			}
			
			return false;
		}
		
		@Override
		public void onSlotChanged() {
			super.onSlotChanged();
			sheet.dragonInv.clean();
			
			if (!this.dragon.world.isRemote) {
				sheet.sendAllGambits(sheet.dragonInv.getAllGambits());
			}
			sheet.container.clearSlots();
			sheet.initSlots();
		}
	}

}
