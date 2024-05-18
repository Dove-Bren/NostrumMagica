package com.smanzana.nostrummagica.client.gui;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.SpellTomeSlotModifyMessage;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class TomeWorkshopScreen extends Screen {
	
	private static final ResourceLocation TEXT = NostrumMagica.Loc("textures/gui/tome_workshop.png");
	private static final int TEXT_WIDTH = 256;
	private static final int TEXT_HEIGHT = 256;
	
	// Background texture
	private static final int TEXT_GUI_HOFFSET = 0;
	private static final int TEXT_GUI_VOFFSET = 0;
	private static final int TEXT_GUI_WIDTH = 202;
	private static final int TEXT_GUI_HEIGHT = 227;
	
	// Library panel texture
	private static final int TEXT_LIBPANEL_HOFFSET = 158;
	private static final int TEXT_LIBPANEL_VOFFSET = 227;
	private static final int TEXT_LIBPANEL_WIDTH = 79;
	private static final int TEXT_LIBPANEL_HEIGHT = 20;
	
	// Spellslot empty texture
	private static final int TEXT_SLOTEMPTY_HOFFSET = 0;
	private static final int TEXT_SLOTEMPTY_VOFFSET = 227;
	private static final int TEXT_SLOTEMPTY_WIDTH = 79;
	private static final int TEXT_SLOTEMPTY_HEIGHT = 20;

	// Spellslot full texture
	private static final int TEXT_SLOTFULL_HOFFSET = TEXT_SLOTEMPTY_HOFFSET + TEXT_SLOTEMPTY_WIDTH;
	private static final int TEXT_SLOTFULL_VOFFSET = TEXT_SLOTEMPTY_VOFFSET;
	private static final int TEXT_SLOTFULL_WIDTH = TEXT_SLOTEMPTY_WIDTH;
	private static final int TEXT_SLOTFULL_HEIGHT = TEXT_SLOTEMPTY_HEIGHT;
	
	// Scrollbar texture
	private static final int TEXT_SCROLLBAR_HOFFSET = 202;
	private static final int TEXT_SCROLLBAR_VOFFSET = 24;
	private static final int TEXT_SCROLLBAR_WIDTH = 6;
	private static final int TEXT_SCROLLBAR_HEIGHT = 14;
	
	private static final int TEXT_SCROLLBAR_HIGH_HOFFSET = 208;
	private static final int TEXT_SCROLLBAR_HIGH_VOFFSET = 24;
	private static final int TEXT_SCROLLBAR_HIGH_WIDTH = TEXT_SCROLLBAR_WIDTH;
	private static final int TEXT_SCROLLBAR_HIGH_HEIGHT = TEXT_SCROLLBAR_HEIGHT;
	
	// Page arrows texture
	private static final int TEXT_RARROW_HOFFSET = 202;
	private static final int TEXT_RARROW_VOFFSET = 0;
	private static final int TEXT_RARROW_WIDTH = 18;
	private static final int TEXT_RARROW_HEIGHT = 10;

	private static final int TEXT_RARROW_HIGH_HOFFSET = 225;
	private static final int TEXT_RARROW_HIGH_VOFFSET = 0;
	private static final int TEXT_RARROW_HIGH_WIDTH = TEXT_RARROW_WIDTH;
	private static final int TEXT_RARROW_HIGH_HEIGHT = TEXT_RARROW_HEIGHT;

	private static final int TEXT_LARROW_HOFFSET = 202;
	private static final int TEXT_LARROW_VOFFSET = 13;
	private static final int TEXT_LARROW_WIDTH = 18;
	private static final int TEXT_LARROW_HEIGHT = 10;

	private static final int TEXT_LARROW_HIGH_HOFFSET = 225;
	private static final int TEXT_LARROW_HIGH_VOFFSET = 13;
	private static final int TEXT_LARROW_HIGH_WIDTH = TEXT_LARROW_WIDTH;
	private static final int TEXT_LARROW_HIGH_HEIGHT = TEXT_LARROW_HEIGHT;
	
	private static final int TEXT_BACKGROUND_COLOR = 0xFF272A2A;
	
	private final PlayerInventory playerInv;
	
	// Positioning variables
	private int gui_width;
	private int gui_height;
	
	private int playerSlotSelected = -1;
	private ItemStack playerStackSelected = ItemStack.EMPTY;
	
	private int librarySlotSelected = -1;
	private int pageSlotSelected = -1;
	
	private int pageIdx = 0;
	private float scrollReal; // In 'cells'
	private float scrollDisplay; // In 'Cells'
	private boolean scrollClicked;
	
	private List<SpellSlotPane> spellSlotWidgets;
	private List<SpellLibraryPane> spellLibraryWidgets;
	private List<PretendInventorySlot> inventoryWidgets;
	private Scrollbar scrollWidget;
	private PageFlipButton leftFlip;
	private PageFlipButton rightFlip;

	public TomeWorkshopScreen(PlayerEntity player) {
		super(new StringTextComponent("Tome Workshop"));
		this.playerInv = player.inventory;
		this.spellSlotWidgets = new ArrayList<>();
		this.spellLibraryWidgets = new ArrayList<>();
		this.inventoryWidgets = new ArrayList<>();
	}
	
	@Override	
	public void tick() {
		if (playerSlotSelected != -1) {
			// Make sure same tome is in place. Otherwise, reset
			ItemStack inSlot = playerInv.getStackInSlot(playerSlotSelected);
			if (!ItemStack.areItemsEqual(playerStackSelected, inSlot)) {
				resetInventorySelection();
				return;
			}
		}
		
		// Tick scroll
		if (updateScroll()) {
			this.refreshAfterScroll();
		}
	}
	
	@Override
	public void init() {
		// For now, just always use the same size as the background
		gui_width = TEXT_GUI_WIDTH;
		gui_height = TEXT_GUI_HEIGHT;
		
		// Don't reset any selections in case we just got resized
		
		// Refresh the tome screen
		this.children.clear();
		this.buttons.clear();
		refreshTomeScreen();
		this.inventoryWidgets.clear();
		setupInventorySlots();
		scrollWidget = new Scrollbar(this, getGuiLeft() + POS_SCROLLBAR_HOFFSET, getGuiTop() + POS_SCROLLBAR_VOFFSET, POS_SCROLLBAR_WIDTH, POS_SCROLLBAR_HEIGHT);
		this.addButton(scrollWidget);
		leftFlip = new PageFlipButton(this, true, getGuiLeft() + POS_LARROW_HOFFSET, getGuiTop() + POS_LARROW_VOFFSET, POS_LARROW_WIDTH, POS_LARROW_HEIGHT);
		this.addButton(leftFlip);
		rightFlip = new PageFlipButton(this, false, getGuiLeft() + POS_RARROW_HOFFSET, getGuiTop() + POS_RARROW_VOFFSET, POS_RARROW_WIDTH, POS_RARROW_HEIGHT);
		this.addButton(rightFlip);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int state) {
		if (this.scrollClicked) {
			this.scrollClicked = false;
			this.scrollWidget.setPressed(false);
			calcScroll(mouseY, true);
			return true;
		}
		
		return super.mouseReleased(mouseX, mouseY, state);
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dx, double dy) {
		if (scrollClicked) {
			calcScroll(mouseY, false);
			scrollDisplay = scrollReal; // Snap to when using the slider
			
			final int yMin = getGuiTop() + POS_SCROLLBAR_VOFFSET;
			final int yMax = yMin + POS_SCROLLBAR_TRACK_HEIGHT;
			this.scrollWidget.y = Math.min(yMax, Math.max(yMin, (int) mouseY - (POS_SCROLLBAR_HEIGHT/2)));
			this.refreshAfterScroll();
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, clickedMouseButton, dx, dy);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double dx) {
		final int spilloverRows = this.getTotalScrollRows();
		if (spilloverRows <= 0) {
			return super.mouseScrolled(mouseX, mouseY, dx);
		}
		
		final float amt = (float) -dx;
		this.scrollReal = Math.min(spilloverRows, Math.max(0, scrollReal + amt));
		this.scrollDisplay = scrollReal;
		this.refreshAfterScroll();
		
		return true;
	}
	
	protected void removeWidget(Widget widget) {
		// This is dumb
		this.children.remove(widget);
		this.buttons.remove(widget);
	}
	
	protected int getGuiLeft() {
		return (this.width - this.gui_width) / 2;
	}
	
	protected int getGuiTop() {
		return (this.height - this.gui_height) / 2;
	}
	
	private static final int POS_LIBRARY_HOFFSET = 12;
	private static final int POS_LIBRARY_VOFFSET = 22;
	private static final int POS_LIBRARY_WIDTH = TEXT_LIBPANEL_WIDTH + 2;
	private static final int POS_LIBRARY_HEIGHT = 102;
	
	private static final int POS_SLOTS_HOFFSET = 110;
	private static final int POS_SLOTS_VOFFSET = 21;
	private static final int POS_SLOTS_WIDTH = TEXT_SLOTEMPTY_WIDTH + 2;
	private static final int POS_SLOTS_HEIGHT = 102;
	
	private static final int POS_PLAYERINV_HOFFSET = 23;
	private static final int POS_PLAYERINV_VOFFSET = 146;
	private static final int POS_HOTBAR_HOFFSET = 23;
	private static final int POS_HOTBAR_VOFFSET = 204;
	
	private static final int POS_SCROLLBAR_HOFFSET = 90;
	private static final int POS_SCROLLBAR_VOFFSET = 21;
	private static final int POS_SCROLLBAR_WIDTH = 6;
	private static final int POS_SCROLLBAR_HEIGHT = 14;
	private static final int POS_SCROLLBAR_TRACK_HEIGHT = 100 - (POS_SCROLLBAR_HEIGHT - 2);
	
	private static final int POS_LARROW_HOFFSET = 120;
	private static final int POS_LARROW_VOFFSET = 125;
	private static final int POS_LARROW_WIDTH = 18;
	private static final int POS_LARROW_HEIGHT = 10;
	
	private static final int POS_RARROW_HOFFSET = 163;
	private static final int POS_RARROW_VOFFSET = 125;
	private static final int POS_RARROW_WIDTH = 18;
	private static final int POS_RARROW_HEIGHT = 10;
	
	private static final int POS_PAGENUM_HOFFSET = (POS_LARROW_HOFFSET + POS_LARROW_WIDTH) + ((POS_RARROW_HOFFSET - (POS_LARROW_HOFFSET + POS_LARROW_WIDTH)) / 2);
	private static final int POS_PAGENUM_VOFFSET = POS_RARROW_VOFFSET + 2;
	
	protected void resetInventorySelection() {
		setInventorySelection(-1);
	}
	
	protected void setInventorySelection(int slotIdx) {
		if (slotIdx == playerSlotSelected || slotIdx == -1) {
			playerSlotSelected = -1;
			playerStackSelected = ItemStack.EMPTY;
		} else {
			playerSlotSelected = slotIdx;
			playerStackSelected = playerInv.getStackInSlot(slotIdx);
		}
		for (PretendInventorySlot slot : this.inventoryWidgets) {
			slot.setSelected(slot.slotIdx == playerSlotSelected);
		}
		refreshTomeScreen();
	}
	
	protected boolean isValidSelection(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof SpellTome;
	}
	
	protected void addSpellSlots() {
		// Force spell slots to match current tome and page
		final int pageSlots = SpellTome.getSlots(playerStackSelected);
		final int x = getGuiLeft() + POS_SLOTS_HOFFSET + 1;
		final int yAnchor = getGuiTop() + POS_SLOTS_VOFFSET + 1;
		final int buttonWidth = 79;
		final int buttonHeight = 20;
		for (int i = 0; i < pageSlots; i++) {
			int y = yAnchor + (i * buttonHeight);
			SpellSlotPane slot = new SpellSlotPane(this, i, x, y, buttonWidth, buttonHeight);
			slot.setSpell(getSpellForSlot(i));
			this.addButton(slot);
			this.spellSlotWidgets.add(slot);
		}
	}
	
	protected void addLibrary() {
		List<Spell> librarySpells = SpellTome.getSpellLibrary(playerStackSelected);
		final int x = getGuiLeft() + POS_LIBRARY_HOFFSET;
		final int yAnchor = getGuiTop() + POS_LIBRARY_VOFFSET;
		final int buttonWidth = 79;
		final int buttonHeight = 20;
		for (int i = 0; i < librarySpells.size(); i++) {
			final Spell spell = librarySpells.get(i);
			final int y = yAnchor + (i * buttonHeight);
			
			SpellLibraryPane pane = new SpellLibraryPane(this, spell, i, x, y, buttonWidth, buttonHeight);
			this.addButton(pane);
			spellLibraryWidgets.add(pane);
		}
	}
	
	protected void refreshTomeScreen() {
		for (Widget widget : spellSlotWidgets) {
			this.removeWidget(widget);
		}
		for (Widget widget : spellLibraryWidgets) {
			this.removeWidget(widget);
		}
		this.spellSlotWidgets.clear();
		this.spellLibraryWidgets.clear();
		this.librarySlotSelected = -1;
		this.pageSlotSelected = -1;
		
		if (isValidSelection(playerStackSelected)) {
			addSpellSlots();
			addLibrary();
			//addPageTurnButtons();
		} else {
			// Reset page for next tome
			pageIdx = 0;
			
			// Don't set up any children
		}
	}
	
	protected void refreshAfterScroll() {
		final int librarySlotHeight = 20;
		final int scrollOffset = (int) (scrollDisplay * librarySlotHeight); // !! Hardcoded button height
		for (SpellLibraryPane pane : this.spellLibraryWidgets) {
			pane.y = pane.getBaseY() + -scrollOffset;
			pane.visible = (pane.y > getGuiTop() + (POS_LIBRARY_VOFFSET - librarySlotHeight)
					&& pane.y < getGuiTop() + (POS_LIBRARY_VOFFSET + POS_LIBRARY_HEIGHT + librarySlotHeight));
		}
		
		if (!this.scrollClicked) {
			final int yOffset = getGuiTop() + POS_SCROLLBAR_VOFFSET;
			final int ticks = this.getTotalScrollRows();
			this.scrollWidget.y = yOffset + (int) (POS_SCROLLBAR_TRACK_HEIGHT * (this.scrollDisplay / ticks));
		}
	}
	
	protected boolean updateScroll() {
		final int hiddenRows = getTotalScrollRows();
		this.scrollWidget.visible = hiddenRows > 0;
		if (hiddenRows <= 0) {
			scrollReal = 0;
			scrollDisplay = 0;
			return true;
		} else if (this.scrollDisplay != this.scrollReal) {
			final float speedAdj = 0.075f; // cells/tick
			// final double speedAdj = speedMult  / spilloverRows; Could scroll faster if list is long...
			
			final float diff = scrollReal - scrollDisplay;
			if (diff != 0) {
				if (Math.abs(diff) < speedAdj) {
					scrollDisplay = scrollReal;
				} else {
					scrollDisplay += Math.signum(diff) * speedAdj;
				}
			}
			return true;
		} else {
			// Nothing to change
			return false;
		}
	}
	
	// Calculate the number of library spells that don't fit in the window
	protected int getTotalScrollRows() {
		if (this.spellLibraryWidgets.isEmpty()) {
			return 0;
		}
		
		final int room = POS_LIBRARY_HEIGHT / spellLibraryWidgets.get(0).getHeightRealms();
		return Math.max(0, spellLibraryWidgets.size() - room);
	}
	
	private void calcScroll(double mouseY, boolean finalize) {
		if (getTotalScrollRows() <= 0) {
			this.scrollReal = 0f;
			this.scrollDisplay = 0f;
			return;
		}
		
		final int topOffset = this.getGuiTop();
		final int topY = topOffset + POS_SCROLLBAR_VOFFSET;
		final int bottomY = topOffset + POS_SCROLLBAR_VOFFSET + POS_SCROLLBAR_TRACK_HEIGHT;
		mouseY = Math.min(Math.max(topY, mouseY), bottomY);
		mouseY -= topY;
		double scrollProg = mouseY / (double) POS_SCROLLBAR_TRACK_HEIGHT;
		
		final int spilloverRows = getTotalScrollRows();
		this.scrollReal = (float) (scrollProg * spilloverRows);
		
		if (finalize) {
			// Round scroll to an even increment
			this.scrollReal = Math.round(scrollReal);
		}
	}
	
	protected void handleScrollbarClick(Scrollbar bar, double mouseY) {
		if (this.getTotalScrollRows() > 0) {
			this.scrollClicked = true;
			bar.setPressed(true);
		}
	}
	
	protected void handleInventorySelect(PretendInventorySlot slot) {
		setInventorySelection(slot.slotIdx);
	}
	
	protected void handleLibrarySelect(SpellLibraryPane pane) {
		if (this.librarySlotSelected == pane.idx) {
			this.librarySlotSelected = -1;
		} else {
			this.librarySlotSelected = pane.idx;
			if (attemptAssignment()) {
				// Would make it selected, but we just did an assignment and want to unselect everything
				return;
			}
		}
		
		for (SpellLibraryPane slot: this.spellLibraryWidgets) {
			slot.setSelected(slot.idx == this.librarySlotSelected);
		}
	}
	
	protected void handleSlotSelect(SpellSlotPane pane) {
		if (this.pageSlotSelected == pane.idx) {
			this.pageSlotSelected = -1;
		} else {
			this.pageSlotSelected = pane.idx;
			if (attemptAssignment()) {
				// Would make it selected, but we just did an assignment and want to unselect everything
				return;
			}
		}
		
		for (SpellSlotPane slot : this.spellSlotWidgets) {
			slot.setSelected(slot.idx == this.pageSlotSelected);
		}
	}
	
	protected void handleSlotDrop(SpellSlotPane pane) {
		// Deselect other slot selected if there is one. Otherwise, drop
		if (this.pageSlotSelected != -1) {
			this.pageSlotSelected = -1;
			
			for (SpellSlotPane slot : this.spellSlotWidgets) {
				slot.setSelected(false);
			}
		} else {
			attemptDrop(pane.idx);
		}
	}
	
	protected void handlePageFlip(PageFlipButton button, boolean isLeft) {
		if (!this.isValidSelection(playerStackSelected)) {
			return;
		}
		
		final int oldIdx = this.pageIdx;
		this.pageIdx = Math.max(0, Math.min(SpellTome.getPageCount(playerStackSelected) - 1,
				pageIdx + (isLeft ? -1 : 1)));
		if (this.pageIdx != oldIdx) {
			this.refreshTomeScreen();
		}
	}
	
	protected boolean attemptAssignment() {
		if (this.pageSlotSelected != -1 && this.librarySlotSelected != -1 && !this.playerStackSelected.isEmpty()) {
			// Assign from library into slot
			Spell spell = null;
			for (SpellLibraryPane pane : this.spellLibraryWidgets) {
				if (pane.idx == this.librarySlotSelected) {
					spell = pane.spell;
					break;
				}
			}
			// assert(spell != null);
			
			// Do locally to see things quicker
			SpellTome.setSpellInSlot(playerStackSelected, this.pageIdx, this.pageSlotSelected, spell);
			
			NetworkHandler.sendToServer(new SpellTomeSlotModifyMessage(
					SpellTome.getTomeID(playerStackSelected),
					this.pageIdx,
					this.pageSlotSelected,
					spell.getRegistryID()
					));
			
			// Recreate the whole thing
			this.refreshTomeScreen();
			return true;
		} else {
			return false;
		}
	}
	
	protected boolean attemptDrop(int idx) {
		// Do locally to see things quicker
		SpellTome.setSpellInSlot(playerStackSelected, this.pageIdx, idx, null);
		
		NetworkHandler.sendToServer(new SpellTomeSlotModifyMessage(
				SpellTome.getTomeID(playerStackSelected),
				this.pageIdx,
				idx,
				-1
				));

		// Recreate the whole thing
		this.refreshTomeScreen();
		return true;
	}
	
	protected @Nullable Spell getSpellForSlot(int idx) {
		if (playerStackSelected.isEmpty() || SpellTome.getSlots(playerStackSelected) <= idx) {
			return null;
		}
		
		return SpellTome.getSpellInSlot(playerStackSelected, this.pageIdx, idx);
	}
	
	protected void setupInventorySlots() {
		
		// Construct player inventory
		int xOffset = getGuiLeft() + POS_PLAYERINV_HOFFSET;
		int yOffset = getGuiTop() + POS_PLAYERINV_VOFFSET;
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				final int idx = x + y * 9 + 9;
				PretendInventorySlot slot = new PretendInventorySlot(this, this.playerInv, idx,
						xOffset + (x * 18), yOffset + (y * 18),
						18, 18);
				this.addButton(slot);
				this.inventoryWidgets.add(slot);
			}
		}
		// Construct player hotbar
		xOffset = getGuiLeft() + POS_HOTBAR_HOFFSET;
		yOffset = getGuiTop() + POS_HOTBAR_VOFFSET;
		for (int x = 0; x < 9; x++) {
			final int idx = x;
			PretendInventorySlot slot = new PretendInventorySlot(this, this.playerInv, idx,
					xOffset + (x * 18), yOffset,
					18, 18);
			this.addButton(slot);
			this.inventoryWidgets.add(slot);
		}
	}
	
	public void render(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStackIn);
		
		final Minecraft mc = Minecraft.getInstance();
		
		// Render library buttons first so they go under the gui
		matrixStackIn.push();
		matrixStackIn.translate(getGuiLeft(), getGuiTop(), 0);
		RenderFuncs.drawRect(matrixStackIn, POS_LIBRARY_HOFFSET - 1, POS_LIBRARY_VOFFSET - 1,
				POS_LIBRARY_HOFFSET - 1 + POS_LIBRARY_WIDTH, POS_LIBRARY_VOFFSET - 1 + POS_LIBRARY_HEIGHT,
				0xFF303030);
		matrixStackIn.pop();
		for (SpellLibraryPane pane : this.spellLibraryWidgets) {
			pane.render(matrixStackIn, mouseX, mouseY, partialTicks);
		}
		
		// Render background
		matrixStackIn.push();
		matrixStackIn.translate(getGuiLeft(), getGuiTop(), 0);
		{
			mc.textureManager.bindTexture(TEXT);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, TEXT_GUI_HOFFSET, TEXT_GUI_VOFFSET, TEXT_GUI_WIDTH, TEXT_GUI_HEIGHT, TEXT_GUI_WIDTH, TEXT_GUI_HEIGHT, TEXT_WIDTH, TEXT_HEIGHT);
			
			if (this.isValidSelection(playerStackSelected)) {
				// Library background rendered before so it's under
				
				// Slot background
				RenderFuncs.drawRect(matrixStackIn, POS_SLOTS_HOFFSET, POS_SLOTS_VOFFSET,
						POS_SLOTS_HOFFSET + POS_SLOTS_WIDTH, POS_SLOTS_VOFFSET + POS_SLOTS_HEIGHT,
						0xFF606060);
				
				if (this.spellLibraryWidgets.isEmpty()) {
					final String message = "No Spells";
					final int messageLen = mc.fontRenderer.getStringWidth(message);
					final int x = POS_LIBRARY_HOFFSET + (POS_LIBRARY_WIDTH/2) + (-messageLen / 2);
					final int y = POS_LIBRARY_VOFFSET + (POS_LIBRARY_HEIGHT/2) + (-mc.fontRenderer.FONT_HEIGHT/2);
					mc.fontRenderer.drawString(matrixStackIn, message, x, y, 0xFFFFFFFF);
				}
				
				if (this.spellSlotWidgets.isEmpty()) {
					final String message = "No Slots";
					final int messageLen = mc.fontRenderer.getStringWidth(message);
					final int x = POS_SLOTS_HOFFSET + (POS_SLOTS_WIDTH/2) + (-messageLen / 2);
					final int y = POS_SLOTS_VOFFSET + (POS_SLOTS_HEIGHT/2) + (-mc.fontRenderer.FONT_HEIGHT/2);
					mc.fontRenderer.drawString(matrixStackIn, message, x, y, 0xFFFFFFFF);
				}
				
				// Draw labels
				String label = "Spell Library";
				int labelLen = mc.fontRenderer.getStringWidth(label);
				mc.fontRenderer.drawString(matrixStackIn, label, POS_LIBRARY_HOFFSET + (POS_LIBRARY_WIDTH/2) + (-labelLen/2), POS_LIBRARY_VOFFSET - (2 + mc.fontRenderer.FONT_HEIGHT), 0xFFDDDDDD);
				
				label = "Spell Pages";
				labelLen = mc.fontRenderer.getStringWidth(label);
				mc.fontRenderer.drawString(matrixStackIn, label, POS_SLOTS_HOFFSET + (POS_SLOTS_WIDTH/2) + (-labelLen/2), POS_SLOTS_VOFFSET - (2 + mc.fontRenderer.FONT_HEIGHT), 0xFFDDDDDD);
			} else {
				// Patch texture hole where library goes
				RenderFuncs.drawRect(matrixStackIn, POS_LIBRARY_HOFFSET - 1, POS_LIBRARY_VOFFSET - 1,
						POS_LIBRARY_HOFFSET + POS_LIBRARY_WIDTH + 5, POS_LIBRARY_VOFFSET + POS_LIBRARY_HEIGHT,
						TEXT_BACKGROUND_COLOR);
				
				final String message = "Select A Tome";
				final int messageLen = mc.fontRenderer.getStringWidth(message);
				int x = (this.gui_width / 2) - 70;
				int y = (60 + mc.fontRenderer.FONT_HEIGHT/2) - 20;
				RenderFuncs.drawRect(matrixStackIn, x, y, x + 140, y + 40, 0xFF303030);
				
				x = (this.gui_width / 2) - (messageLen / 2);
				y = 60;//(this.gui_height / 2) - (mc.fontRenderer.FONT_HEIGHT/2);
				mc.fontRenderer.drawString(matrixStackIn, message, x, y, 0xFFFFFFFF);
			}
		}
		matrixStackIn.pop();
		
		// Render remaining buttons
		for (PretendInventorySlot slot: this.inventoryWidgets) {
			slot.render(matrixStackIn, mouseX, mouseY, partialTicks);
		}
		for (SpellSlotPane slot : this.spellSlotWidgets) {
			slot.render(matrixStackIn, mouseX, mouseY, partialTicks);
		}
		if (this.isValidSelection(playerStackSelected)) {
			this.scrollWidget.render(matrixStackIn, mouseX, mouseY, partialTicks);
			this.leftFlip.render(matrixStackIn, mouseX, mouseY, partialTicks);
			this.rightFlip.render(matrixStackIn, mouseX, mouseY, partialTicks);
			
			final String pageNum = (this.pageIdx+1) + "";
			final int pageNumLen = mc.fontRenderer.getStringWidth(pageNum);
			mc.fontRenderer.drawString(matrixStackIn, pageNum, getGuiLeft() + POS_PAGENUM_HOFFSET - (pageNumLen / 2), getGuiTop() + POS_PAGENUM_VOFFSET, 0xFFAAAAAA);
		}
		
		// Render foregrounds
		for (PretendInventorySlot slot: this.inventoryWidgets) {
			slot.renderForeground(matrixStackIn, mouseX, mouseY, partialTicks);
		}
		for (SpellLibraryPane librarySlot : this.spellLibraryWidgets) {
			librarySlot.renderForeground(matrixStackIn, mouseX, mouseY, partialTicks);
		}
		for (SpellSlotPane slot : this.spellSlotWidgets) {
			slot.renderForeground(matrixStackIn, mouseX, mouseY, partialTicks);
		}
		
		// Super renders buttons, but we want to split up buttons
		//super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
	
	protected void renderSpellSlide(MatrixStack matrixStackIn, Spell spell, int width, int height, float partialTicks) {
		final Minecraft mc = getMinecraft();
		final int iconLen = Math.min(32, height-2);
		final int yMargin = Math.max(0, (iconLen+2) - height) / 2; // Get how much vertical space that means we have
		
		// Draw background
		RenderFuncs.drawRect(matrixStackIn, 0, 0, iconLen + 2, iconLen + 2, 0xFF000000);
		RenderFuncs.drawRect(matrixStackIn, 1, 1, iconLen + 1, iconLen + 1, 0xFF404040);
		SpellIcon.get(spell.getIconIndex()).render(mc, matrixStackIn, 0, 1 + yMargin, iconLen, iconLen);
		
		String name = spell.getName();
		if (name.length() > 17) {
			name = name.substring(0, 15) + "...";
		}
		
		// Draw info
		int y = 0;
		matrixStackIn.push();
		matrixStackIn.translate(iconLen + 3, 1, 0);
		matrixStackIn.scale(.5f, .5f, 1f);
		mc.fontRenderer.drawString(matrixStackIn, TextFormatting.BOLD + name, 0, y, 0xFF000000);
		y += mc.fontRenderer.FONT_HEIGHT;
		mc.fontRenderer.drawString(matrixStackIn, "Mana: " + spell.getManaCost(), 0, y, 0xFF202020);
		y += mc.fontRenderer.FONT_HEIGHT;
		mc.fontRenderer.drawString(matrixStackIn, "Weight: " + spell.getWeight(), 0, y, 0xFF202020);
		matrixStackIn.pop();
	}
	
	protected void renderItemTooltip(MatrixStack matrixStackIn, ItemStack stack, int x, int y) {
		this.renderTooltip(matrixStackIn, stack, x, y);
	}
	
	protected static class SpellLibraryPane extends AbstractButton {
		
		private final TomeWorkshopScreen screen;
		private final Spell spell;
		private final int idx;
		private final int baseY;
		protected boolean selected;
		
		public SpellLibraryPane(TomeWorkshopScreen screen, Spell spell, int idx, int x, int y, int width, int height) {
			//int x, int y, int width, int height, ITextComponent title
			super(x, y, width, height, StringTextComponent.EMPTY);
			this.screen = screen;
			this.spell = spell;
			this.idx = idx;
			this.selected = false;
			
			this.baseY = y; // Remember Y
		}

		@Override
		public void onPress() {
			screen.handleLibrarySelect(this);
		}
		
		public void setSelected(boolean selected) {
			this.selected = selected;
		}
		
		public int getBaseY() {
			return this.baseY;
		}
		
		@Override
		public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			final Minecraft mc = Minecraft.getInstance();
			matrixStackIn.push();
			matrixStackIn.translate(x, y, 0);
			
			if (selected) {
				
			}
			
			// Draw background
			mc.textureManager.bindTexture(TEXT);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0,
					TEXT_LIBPANEL_HOFFSET, TEXT_LIBPANEL_VOFFSET,
					TEXT_LIBPANEL_WIDTH, TEXT_LIBPANEL_HEIGHT,
					width, height,
					TEXT_WIDTH, TEXT_HEIGHT);
			
			// Draw spell slide
			matrixStackIn.translate(1, 1, 0);
			screen.renderSpellSlide(matrixStackIn, spell, width - (2), height - (2), partialTicks);
			matrixStackIn.translate(-1, -1, 0);
			
			if (this.selected) {
				matrixStackIn.translate(0, 0, 1);
				RenderFuncs.drawRect(matrixStackIn, 1, 1, this.width - 1, this.height - 1, 0x3088FF55);
			}
			
			if (this.isHovered()) {
				final int slotHighlightColor = -2130706433; // copied from ContainerScreen
				RenderFuncs.drawRect(matrixStackIn, 1, 1, this.width - 1 , this.height - 1, slotHighlightColor);
			}
			
			matrixStackIn.pop();
		}
		
		public void renderForeground(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			
		}
	}
	
	protected static class SpellSlotPane extends AbstractButton {
		
		private final TomeWorkshopScreen screen;
		private final int idx;
		private @Nullable Spell spell;
		protected boolean selected;
		
		public SpellSlotPane(TomeWorkshopScreen screen, int idx, int x, int y, int width, int height) {
			//int x, int y, int width, int height, ITextComponent title
			super(x, y, width, height, StringTextComponent.EMPTY);
			this.screen = screen;
			this.idx = idx;
			this.selected = false;
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			// Dupe what's in Widget to handle right-clicks.
			// button==0 is left click
			if (button == 1 && this.clicked(mouseX, mouseY)) {
				this.playDownSound(Minecraft.getInstance().getSoundHandler());
				this.onRightClick(mouseX, mouseY);
				return true;
			} else {
				return super.mouseClicked(mouseX, mouseY, button);
			}
		}

		@Override
		public void onPress() {
			screen.handleSlotSelect(this);
		}
		
		public void onRightClick(double mouseX, double mouseY) {
			screen.handleSlotDrop(this);
		}
		
		public void setSelected(boolean selected) {
			this.selected = selected;
		}
		
		public void setSpell(@Nullable Spell spell) {
			this.spell = spell;
		}
		
		@Override
		public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			final Minecraft mc = Minecraft.getInstance();
			
			matrixStackIn.push();
			matrixStackIn.translate(x, y, 0);
			
			// Draw background
			mc.textureManager.bindTexture(TEXT);
			if (this.spell == null) {
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0,
						TEXT_SLOTEMPTY_HOFFSET, TEXT_SLOTEMPTY_VOFFSET,
						TEXT_SLOTEMPTY_WIDTH, TEXT_SLOTEMPTY_HEIGHT,
						width, height,
						TEXT_WIDTH, TEXT_HEIGHT);
			} else {
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0,
						TEXT_SLOTFULL_HOFFSET, TEXT_SLOTFULL_VOFFSET,
						TEXT_SLOTFULL_WIDTH, TEXT_SLOTFULL_HEIGHT,
						width, height,
						TEXT_WIDTH, TEXT_HEIGHT);
				
				// Draw spell slide
				matrixStackIn.translate(1, 1, 0);
				screen.renderSpellSlide(matrixStackIn, spell, width - (2), height - (2), partialTicks);
				matrixStackIn.translate(-1, -1, 0);
			}
			
			if (this.selected) {
				matrixStackIn.translate(0, 0, 1);
				RenderFuncs.drawRect(matrixStackIn, 1, 1, this.width - 1, this.height - 1, 0x3088FF55);
			}
			
			if (this.isHovered()) {
				final int slotHighlightColor = -2130706433; // copied from ContainerScreen
				RenderFuncs.drawRect(matrixStackIn, 1, 1, this.width - 1 , this.height - 1, slotHighlightColor);
			}
			
			matrixStackIn.pop();
		}
		
		public void renderForeground(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			
		}
	}
	
	protected static class PretendInventorySlot extends AbstractButton {
		
		private final TomeWorkshopScreen screen;
		private final PlayerInventory inventory;
		private final int slotIdx;
		private boolean selected;
		
		public PretendInventorySlot(TomeWorkshopScreen screen, PlayerInventory inventory, int slotIdx, int x, int y, int width, int height) {
			super(x, y, width, height, StringTextComponent.EMPTY);
			this.screen = screen;
			this.inventory = inventory;
			this.slotIdx = slotIdx;
		}
		
		@Override
		public void onPress() {
			screen.handleInventorySelect(this);
		}
		
		public void setSelected(boolean selected) {
			this.selected = selected;
		}
		
		@Override
		public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			matrixStackIn.push();
			matrixStackIn.translate(this.x, this.y, 0);
			ItemStack slotStack = this.inventory.getStackInSlot(this.slotIdx);
			if (!slotStack.isEmpty()) {
				RenderFuncs.RenderGUIItem(slotStack, matrixStackIn);
			}
			
			matrixStackIn.translate(0, 0, 150);
			if (this.selected) {
				final int color;
				if (screen.isValidSelection(slotStack)) {
					color = 0x3088FF55;
				} else {
					color = 0x30FF5588;
				}
				RenderFuncs.drawRect(matrixStackIn, 0, 0, this.width - 2, this.height - 2, color);
				matrixStackIn.translate(0, 0, 10);
			}
			 
			if (this.isHovered()) {
				final int slotHighlightColor = -2130706433; // copied from ContainerScreen
				RenderFuncs.drawRect(matrixStackIn, 0, 0, this.width -2 , this.height - 2, slotHighlightColor);
			}
			matrixStackIn.pop();
		}
		
		public void renderForeground(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			if (this.isHovered) {
				ItemStack slotStack = this.inventory.getStackInSlot(this.slotIdx);
				if (!slotStack.isEmpty()) {
					screen.renderItemTooltip(matrixStackIn, slotStack, mouseX, mouseY);
				}
			}
		}
		
	}
	
	protected static class Scrollbar extends AbstractButton {
		
		private final TomeWorkshopScreen screen;
		private boolean pressed;
		
		public Scrollbar(TomeWorkshopScreen screen, int x, int y, int width, int height) {
			super(x, y, width, height, StringTextComponent.EMPTY);
			this.screen = screen;
		}
		
		@Override
		public void onClick(double mouseX, double mouseY) {
			screen.handleScrollbarClick(this, mouseY);
		}

		@Override
		public void onPress() {
			; // Don't have to do anything, as onClick is overriden to not call this
		}
		
		public void setPressed(boolean pressed) {
			this.pressed = pressed;
		}
		
		@Override
		public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			final Minecraft mc = Minecraft.getInstance();
			
			matrixStackIn.push();
			matrixStackIn.translate(x, y, 0);
			
			// Draw background
			mc.textureManager.bindTexture(TEXT);
			if (this.isHovered() || pressed) {
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0,
					TEXT_SCROLLBAR_HIGH_HOFFSET, TEXT_SCROLLBAR_HIGH_VOFFSET,
					TEXT_SCROLLBAR_HIGH_WIDTH, TEXT_SCROLLBAR_HIGH_HEIGHT,
					width, height,
					TEXT_WIDTH, TEXT_HEIGHT);
			} else {
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0,
						TEXT_SCROLLBAR_HOFFSET, TEXT_SCROLLBAR_VOFFSET,
						TEXT_SCROLLBAR_WIDTH, TEXT_SCROLLBAR_HEIGHT,
						width, height,
						TEXT_WIDTH, TEXT_HEIGHT);
			}
			
			matrixStackIn.pop();
		}
	}
	
	protected static class PageFlipButton extends AbstractButton {
		private final TomeWorkshopScreen screen;
		private final boolean isLeft;
		
		public PageFlipButton(TomeWorkshopScreen screen, boolean isLeft, int x, int y, int width, int height) {
			super(x, y, width, height, StringTextComponent.EMPTY);
			this.screen = screen;
			this.isLeft = isLeft;
		}
		
		@Override
		public void onPress() {
			screen.handlePageFlip(this, isLeft);
		}
		
		@Override
		public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			final Minecraft mc = Minecraft.getInstance();
			
			matrixStackIn.push();
			matrixStackIn.translate(x, y, 0);
			
			// Draw background
			mc.textureManager.bindTexture(TEXT);
			if (this.isHovered()) {
				if (this.isLeft) {
					RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0,
						TEXT_LARROW_HIGH_HOFFSET, TEXT_LARROW_HIGH_VOFFSET,
						TEXT_LARROW_HIGH_WIDTH, TEXT_LARROW_HIGH_HEIGHT,
						width, height,
						TEXT_WIDTH, TEXT_HEIGHT);
				} else {
					RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0,
							TEXT_RARROW_HIGH_HOFFSET, TEXT_RARROW_HIGH_VOFFSET,
							TEXT_RARROW_HIGH_WIDTH, TEXT_RARROW_HIGH_HEIGHT,
							width, height,
							TEXT_WIDTH, TEXT_HEIGHT);
				}
			} else {
				if (this.isLeft) {
					RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0,
							TEXT_LARROW_HOFFSET, TEXT_LARROW_VOFFSET,
							TEXT_LARROW_WIDTH, TEXT_LARROW_HEIGHT,
							width, height,
							TEXT_WIDTH, TEXT_HEIGHT);
				} else {
					RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0,
							TEXT_RARROW_HOFFSET, TEXT_RARROW_VOFFSET,
							TEXT_RARROW_WIDTH, TEXT_RARROW_HEIGHT,
							width, height,
							TEXT_WIDTH, TEXT_HEIGHT);
				}
			}
			
			matrixStackIn.pop();
		}
	}
	
}
