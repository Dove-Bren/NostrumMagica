package com.smanzana.nostrummagica.client.gui.container;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.client.gui.container.SimpleInventoryWidget.SimpleInventoryContainerlet;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui.SpellCreationContainer;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui.SpellGui;
import com.smanzana.nostrummagica.crafting.ISpellCraftingInventory;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.SpellCraftMessage;
import com.smanzana.nostrummagica.spellcraft.SpellCrafting.SpellPartSummary;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.SpellAction;
import com.smanzana.nostrummagica.spells.components.SpellAction.SpellActionProperties;
import com.smanzana.nostrummagica.spells.components.SpellEffectPart;
import com.smanzana.nostrummagica.spells.components.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.tiles.BasicSpellTableEntity;
import com.smanzana.nostrummagica.utils.ContainerUtil;
import com.smanzana.nostrummagica.utils.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class BasicSpellCraftGui {
	
	// Shared positioning variables. Out here because container needs size info to position slots. :(
	protected static final int POS_CONTAINER_WIDTH = 202;
	protected static final int POS_CONTAINER_HEIGHT = 166;
	
	protected static final int POS_SLOT_SCROLL_HOFFSET = 23;
	protected static final int POS_SLOT_SCROLL_VOFFSET = 16;
	
	protected static final int POS_SLOT_RUNES_VOFFSET = 47;
	protected static final int POS_SLOT_RUNES_WIDTH = 18;
	protected static final int POS_SLOT_RUNES_SPACER_WIDTH = 18;
	
	protected static final int POS_SLOT_PLAYERINV_HOFFSET = 21;
	protected static final int POS_SLOT_PLAYERINV_VOFFSET = 85;
	
	protected static final int POS_SLOT_HOTBAR_HOFFSET = 21;
	protected static final int POS_SLOT_HOTBAR_VOFFSET = 143;

	protected static final int POS_INFOPANEL_HOFFSET = POS_CONTAINER_WIDTH;
	protected static final int POS_INFOPANEL_VOFFSET = 0;
	protected static final int POS_INFOPANEL_WIDTH = 62;
	protected static final int POS_INFOPANEL_HEIGHT = 79;

	public static class BasicSpellCraftContainer extends SpellCreationContainer {
		
		public static final String ID = "basicspellcrafter";
		
		protected String name;
		protected int spellIcon;
		protected @Nullable SimpleInventoryContainerlet extraInventory;
		
		public BasicSpellCraftContainer(int windowId,
				PlayerEntity crafter, PlayerInventory playerInv, ISpellCraftingInventory tableInventory,
				BlockPos tablePos, @Nullable IInventory extraInventory) {
			this(NostrumContainers.SpellCreationBasic, windowId, crafter, playerInv, tableInventory, tablePos, extraInventory);
		}

		protected BasicSpellCraftContainer(ContainerType<? extends SpellCreationContainer> type, int windowId,
				PlayerEntity crafter, PlayerInventory playerInv, ISpellCraftingInventory tableInventory,
				BlockPos tablePos, @Nullable IInventory extraInventory) {
			super(type, windowId, crafter, playerInv, tableInventory, tablePos);
			
			this.name = "";
			this.spellIcon = -1;
			
			// Scroll slot
			this.addSlot(new ScrollSlot(this, tableInventory, tableInventory.getScrollSlotIndex(), POS_SLOT_SCROLL_HOFFSET, POS_SLOT_SCROLL_VOFFSET));
			
			// Player main inventory
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 9; x++) {
					this.addSlot(new Slot(playerInv, x + y * 9 + 9, POS_SLOT_PLAYERINV_HOFFSET + (x * 18), POS_SLOT_PLAYERINV_VOFFSET + (y * 18)));
				}
			}
			// Player hotbar
			for (int x = 0; x < 9; x++) {
				this.addSlot(new Slot(playerInv, x, POS_SLOT_HOTBAR_HOFFSET + x * 18, POS_SLOT_HOTBAR_VOFFSET));
			}
			
			// Rune slots
			final int runeSlots = tableInventory.getRuneSlotCount();
			final int totalWidth = (runeSlots * POS_SLOT_RUNES_WIDTH) + ((runeSlots - 1) * POS_SLOT_RUNES_SPACER_WIDTH);
			final int runeSlotXOffset = (POS_CONTAINER_WIDTH - totalWidth) / 2;
			@Nullable RuneSlot prev = null;
			for (int i = 0; i < runeSlots; i++) {
				final RuneSlot slot = new RuneSlot(this, prev, tableInventory, tableInventory.getRuneSlotStartingIndex() + i,
						runeSlotXOffset + (i * POS_SLOT_RUNES_WIDTH) + (i * POS_SLOT_RUNES_SPACER_WIDTH), POS_SLOT_RUNES_VOFFSET);
				this.addSlot(slot);
				if (prev != null) {
					prev.setNext(slot);
				}
				prev = slot;
			}
			
			// Extra inventory, if tile entity has one
			if (extraInventory != null) {
				this.extraInventory = new SimpleInventoryContainerlet(this::addSlot, extraInventory, HideableSlot::new,
						POS_CONTAINER_WIDTH, POS_INFOPANEL_VOFFSET + POS_INFOPANEL_HEIGHT, POS_INFOPANEL_WIDTH, POS_CONTAINER_HEIGHT - (POS_INFOPANEL_VOFFSET + POS_INFOPANEL_HEIGHT),
						new StringTextComponent("Extra"));
			}
			
		}
		
		public static BasicSpellCraftContainer FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buffer) {
			final BasicSpellTableEntity te = ContainerUtil.GetPackedTE(buffer);
			final ISpellCraftingInventory tableInv = te.getSpellCraftingInventory();
			final @Nullable IInventory extraInv = te.getExtraInventory();
			return new BasicSpellCraftContainer(windowId, playerInv.player, playerInv, tableInv, te.getPos(), extraInv);
		}
		
		public static IPackedContainerProvider Make(BasicSpellTableEntity table) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new BasicSpellCraftContainer(windowId, player, playerInv, table.getSpellCraftingInventory(), table.getPos(), table.getExtraInventory());
			}, (buffer) -> {
				ContainerUtil.PackTE(buffer, table);
			});
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public int getSpellIcon() {
			return this.spellIcon;
		}
		
		@Override
		public @Nullable SpellCraftPattern getCraftPattern() {
			return null; // Table doesn't support patterns.
		}
	}
	
	public static class BasicSpellCraftGuiContainer extends SpellGui<BasicSpellCraftContainer> {
		
		private static final ResourceLocation TEXT = NostrumMagica.Loc("textures/gui/container/spell_create_basic.png");
		
		private static final int TEX_WIDTH = 256;
		private static final int TEX_HEIGHT = 256;
		
		private static final int TEX_RUNESLOT_HOFFSET = 0;
		private static final int TEX_RUNESLOT_VOFFSET = 166;
		private static final int TEX_RUNESLOT_WIDTH = 18;
		private static final int TEX_RUNESLOT_HEIGHT = 18;
		
		private static final int TEX_RUNESPACER_DISABLED_HOFFSET = 18;
		private static final int TEX_RUNESPACER_DISABLED_VOFFSET = 166;
		private static final int TEX_RUNESPACER_DISABLED_WIDTH = 18;
		private static final int TEX_RUNESPACER_DISABLED_HEIGHT = 18;
		
		private static final int TEX_RUNESPACER_HOFFSET = TEX_RUNESPACER_DISABLED_HOFFSET;
		private static final int TEX_RUNESPACER_VOFFSET = TEX_RUNESPACER_DISABLED_VOFFSET + TEX_RUNESPACER_DISABLED_HEIGHT;
		private static final int TEX_RUNESPACER_WIDTH = TEX_RUNESPACER_DISABLED_WIDTH;
		private static final int TEX_RUNESPACER_HEIGHT = TEX_RUNESPACER_DISABLED_HEIGHT;
		private static final int TEX_RUNESPACER_ANIM_COUNT = 4;
		
		private static final int TEX_INFOPANEL_HOFFSET = 36;
		private static final int TEX_INFOPANEL_VOFFSET = 166;
		private static final int TEX_INFOPANEL_WIDTH = 62;
		private static final int TEX_INFOPANEL_HEIGHT = 79;
		
		private static final int POS_NAME_HOFFSET = 47;
		private static final int POS_NAME_VOFFSET = 18;
		private static final int POS_NAME_WIDTH = 116;
		private static final int POS_NAME_HEIGHT = 12;
		
		private static final int POS_ICON_BUTTON_WIDTH = 16;
		private static final int POS_ICON_BUTTON_HEIGHT = 16;
		
		private static final int POS_STATUS_HOFFSET = 4;
		private static final int POS_STATUS_VOFFSET = 4;
		private static final int POS_STATUS_WIDTH = 10;
		private static final int POS_STATUS_HEIGHT = 10;
		
		private static final int POS_SUBMIT_HOFFSET = 172;
		private static final int POS_SUBMIT_VOFFSET = 19;
		private static final int POS_SUBMIT_WIDTH = 18;
		private static final int POS_SUBMIT_HEIGHT = 10;
		
		private static final int POS_WEIGHTBAR_HOFFSET = POS_INFOPANEL_HOFFSET + 6;
		private static final int POS_WEIGHTBAR_VOFFSET = POS_INFOPANEL_VOFFSET + 3;
		private static final int POS_WEIGHTBAR_WIDTH = 50;
		private static final int POS_WEIGHTBAR_HEIGHT = 16;
		
		protected TextFieldWidget nameField;
		protected @Nullable SimpleInventoryWidget extraInventoryWidget;
		protected SpellPartBar partBarWidget;
		protected List<Rectangle2d> extraAreas;
		
		private Vector3i[] runeSlots;
		private Vector3i[] spacerSpots;
		private @Nullable SpellPartSummary hoveredPart;
		
		public BasicSpellCraftGuiContainer(BasicSpellCraftContainer container, PlayerInventory playerInv, ITextComponent name) {
			super(container, playerInv, name);
			
			this.xSize = POS_CONTAINER_WIDTH;
			this.ySize = POS_CONTAINER_HEIGHT;
			
			final int runeSlotCount = getContainer().inventory.getRuneSlotCount();
			final int totalWidth = (runeSlotCount * POS_SLOT_RUNES_WIDTH) + ((runeSlotCount - 1) * POS_SLOT_RUNES_SPACER_WIDTH);
			final int runeSlotXOffset = (POS_CONTAINER_WIDTH - totalWidth) / 2;
			this.runeSlots = new Vector3i[runeSlotCount];
			this.spacerSpots = new Vector3i[Math.max(0, runeSlotCount-1)];
			for (int i = 0; i < runeSlotCount; i++) {
				runeSlots[i] = new Vector3i(-1 + runeSlotXOffset + (i * POS_SLOT_RUNES_WIDTH) + (i * POS_SLOT_RUNES_SPACER_WIDTH), -1 + POS_SLOT_RUNES_VOFFSET, i);
				if (i > 0) {
					spacerSpots[i-1] = new Vector3i(runeSlotXOffset + (i * POS_SLOT_RUNES_WIDTH) + ((i-1) * POS_SLOT_RUNES_SPACER_WIDTH), POS_SLOT_RUNES_VOFFSET, i-1);
				}
			}
		}
		
		@Override
		public void init() {
			super.init(); // Clears children
			
			final Minecraft mc = Minecraft.getInstance();
			final int horizontalMargin = ((width - xSize) / 2);
			final int verticalMargin = (height - ySize) / 2;
			
			// Name input field
			this.nameField = new TextFieldWidget(mc.fontRenderer, horizontalMargin + POS_NAME_HOFFSET, verticalMargin + POS_NAME_VOFFSET, POS_NAME_WIDTH, POS_NAME_HEIGHT, StringTextComponent.EMPTY);
			this.nameField.setMaxStringLength(SpellCreationGui.MaxNameLength);
			this.nameField.setResponder((s) -> {
				getContainer().name = s;
				getContainer().validate();
			});
			this.nameField.setValidator((s) -> {
				// do this better? If it ends up sucking. Otherwise this is probably fine
				return s.codePoints().allMatch(SpellGui::isValidChar);
			});
			this.nameField.setText(getContainer().getName());
			this.addButton(this.nameField);
			
			
			// Spell icon buttons
			int extraMargin = 3;
			final int spaceWidth = horizontalMargin - (2 * extraMargin); // amount of space to draw in
			final int perRow = spaceWidth / POS_ICON_BUTTON_WIDTH;
			extraMargin += (spaceWidth % POS_ICON_BUTTON_WIDTH) / 2; // Center by adding remainder / 2
			
			extraAreas = new ArrayList<>(2);
			extraAreas.add(new Rectangle2d(extraMargin, verticalMargin, horizontalMargin - extraMargin, ((SpellIcon.numIcons / perRow) + 1) * POS_ICON_BUTTON_HEIGHT));
			for (int i = 0; i < SpellIcon.numIcons; i++) {
				SpellIconButton button = new SpellIconButton(
						extraMargin + (i % perRow) * POS_ICON_BUTTON_WIDTH,
						verticalMargin + (i / perRow) * POS_ICON_BUTTON_HEIGHT,
						POS_ICON_BUTTON_WIDTH,
						POS_ICON_BUTTON_HEIGHT,
						i,
						this);
				
				this.addButton(button);
			}
			
			// Info panel
			// this.addButton(); Not actually a widget
			extraAreas.add(new Rectangle2d(horizontalMargin + POS_INFOPANEL_HOFFSET, verticalMargin + POS_INFOPANEL_VOFFSET, POS_INFOPANEL_WIDTH, POS_INFOPANEL_HEIGHT));
			
			// Status icon
			this.addButton(new SpellStatusIcon(this, horizontalMargin + POS_STATUS_HOFFSET, verticalMargin + POS_STATUS_VOFFSET, POS_STATUS_WIDTH, POS_STATUS_HEIGHT));
			
			// Submit button
			this.addButton(new SubmitButton(this, horizontalMargin + POS_SUBMIT_HOFFSET, verticalMargin + POS_SUBMIT_VOFFSET, POS_SUBMIT_WIDTH, POS_SUBMIT_HEIGHT));
			
			// Weight status
			this.addButton(new WeightStatus(this, horizontalMargin + POS_WEIGHTBAR_HOFFSET, verticalMargin + POS_WEIGHTBAR_VOFFSET, POS_WEIGHTBAR_WIDTH, POS_WEIGHTBAR_HEIGHT));
			
			// Extra inventory
			if (this.getContainer().extraInventory != null) {
				final SimpleInventoryContainerlet extraContainer = this.getContainer().extraInventory;
				this.extraInventoryWidget = new SimpleInventoryWidget(this, extraContainer);
				this.addButton(this.extraInventoryWidget);
				extraAreas.add(new Rectangle2d(horizontalMargin + extraContainer.x, verticalMargin + this.getContainer().extraInventory.y, this.getContainer().extraInventory.width, this.getContainer().extraInventory.height));
			}
			
			Vector3i[] belowSlots = new Vector3i[runeSlots.length];
			for (int i = 0; i < runeSlots.length; i++) {
				belowSlots[i] = new Vector3i(
						runeSlots[i].getX(),
						runeSlots[i].getY() + POS_SLOT_RUNES_WIDTH + 1,
						runeSlots[i].getZ()
					);
			}
			this.partBarWidget = new SpellPartBar(this, belowSlots, POS_SLOT_RUNES_WIDTH, (part, matrix, mouseX, mouseY) -> {
				this.hoveredPart = part;
			});
			this.addButton(partBarWidget);

			this.getContainer().validate();
		}

		@Override
		protected void onIconSelected(int icon) {
			this.getContainer().spellIcon = icon;
		}

		@Override
		protected void onSubmit() {
			// clicked on submit button
			getContainer().validate();
			if (getContainer().spellValid) {
				// whoo make spell
				Spell spell = getContainer().makeSpell(true);
				if (spell != null) {
					// All of this happens again and is synced back to client
					// But in the mean, might as well do it here for the
					// smoothest feel
					ItemStack scroll = new ItemStack(NostrumItems.spellScroll, 1);
					SpellScroll.setSpell(scroll, spell);
					//getContainer().setScroll(scroll);
					getContainer().inventory.setScrollSlotContents(scroll);
					//NostrumMagicaSounds.AMBIENT_WOOSH.play(Minecraft.getInstance().thePlayer);
					
					NetworkHandler.sendToServer(new SpellCraftMessage(
							getContainer().name.toString(),
							getContainer().pos,
							getContainer().spellIcon
							));
					getContainer().name = "";
					this.nameField.setText("");
					getContainer().spellIcon = -1;
				}
			} else {
				// Don't
			}
		}
		
		@Override
		public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
			if (p_keyPressed_1_ == 256) {
				this.mc.player.closeScreen();
			}

			// Copied from AnvilScreen
			return !this.nameField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) && !this.nameField.canWrite() ? super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) : true;
		}
		
		@Override
		public List<Rectangle2d> getGuiExtraAreas() {
			return extraAreas;
		}
		
		protected ResourceLocation getBackgroundTexture() {
			return TEXT; 
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			
			mc.getTextureManager().bindTexture(getBackgroundTexture());
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin, 0, 0, POS_CONTAINER_WIDTH, POS_CONTAINER_HEIGHT, TEX_WIDTH, TEX_HEIGHT);
			
			// Manually draw rune slots, since they're not baked onto the sheet
			final int filledRuneSlots = getContainer().getFilledRuneSlots();
			for (Vector3i spacerPos : spacerSpots) {
				matrixStackIn.push();
				matrixStackIn.translate(horizontalMargin + spacerPos.getX(), verticalMargin + spacerPos.getY(), 0);
				drawRuneSpacerBackground(matrixStackIn, POS_SLOT_RUNES_SPACER_WIDTH, POS_SLOT_RUNES_SPACER_WIDTH, spacerPos.getZ() < filledRuneSlots-1);
				matrixStackIn.pop();
			}
			for (Vector3i slotPos : runeSlots) {
				matrixStackIn.push();
				matrixStackIn.translate(horizontalMargin + slotPos.getX(), verticalMargin + slotPos.getY(), 0);
				drawRuneCellBackground(matrixStackIn, POS_SLOT_RUNES_WIDTH, POS_SLOT_RUNES_WIDTH);
				matrixStackIn.pop();
			}
			
			// Draw info panel since it's not a widget
			matrixStackIn.push();
			matrixStackIn.translate(horizontalMargin + POS_INFOPANEL_HOFFSET, verticalMargin + POS_INFOPANEL_VOFFSET, 0);
			drawInfoPanelBackground(matrixStackIn);
			matrixStackIn.pop();
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(MatrixStack matrixStackIn, int mouseX, int mouseY) {
			if (!getContainer().hasScroll()) {
				Minecraft mc = Minecraft.getInstance();
				final int width = 150;
				final int height = 50;
				matrixStackIn.push();
				matrixStackIn.translate(this.xSize / 2, (height+20) / 2, 300);
				SpellGui.drawScrollMessage(matrixStackIn, width, height, mc.fontRenderer);
				matrixStackIn.pop();
			}
		}
		
		protected void drawInfoPanelBackground(MatrixStack matrixStackIn) {
			final Minecraft mc = Minecraft.getInstance();
			FontRenderer fontRenderer = mc.fontRenderer;
			
			// Background
			mc.getTextureManager().bindTexture(getBackgroundTexture());
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, 
					TEX_INFOPANEL_HOFFSET, TEX_INFOPANEL_VOFFSET, TEX_INFOPANEL_WIDTH, TEX_INFOPANEL_HEIGHT,
					POS_INFOPANEL_WIDTH, POS_INFOPANEL_HEIGHT,
					TEX_WIDTH, TEX_HEIGHT
					);
			
			// info
			final int xOffset = 4;
			final int yOffset = (POS_WEIGHTBAR_VOFFSET - POS_INFOPANEL_VOFFSET) + POS_WEIGHTBAR_HEIGHT;
			matrixStackIn.push();
			matrixStackIn.translate(xOffset, yOffset, 0);
			
			if (this.hoveredPart != null) {
				final String titleText = hoveredPart.isError() ? "Error" : hoveredPart.isShape() ? "Shape" : "Effect";
				final int titleTextWidth = fontRenderer.getStringWidth(titleText);
				fontRenderer.drawString(matrixStackIn, titleText, ((POS_INFOPANEL_WIDTH-8) - titleTextWidth)/2, 0, 0xFF000000);
				matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
				
				if (!hoveredPart.isError()) {
					matrixStackIn.scale(.5f, .5f, 1f);
					matrixStackIn.push();
					
					// Mana cost
					fontRenderer.drawString(matrixStackIn, "Mana Cost: " + hoveredPart.getMana(), 0, 0, 0xFF000000);
					matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
					
					// Weight
					fontRenderer.drawString(matrixStackIn, "Weight: " + hoveredPart.getWeight(), 0, 0, 0xFF000000);
					matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
					
					if (!hoveredPart.isShape()) {
						final SpellEffectPart effect = hoveredPart.getEffect();
						final int subWidth = (POS_INFOPANEL_WIDTH-8) * 2;
						final String name;
						final String desc;
						@Nullable SpellAction action = SpellGui.getKnownActionForPart(effect);
						if (action == null) {
							name = "Unknown Effect";
							desc = "You haven't seen this effect before. Make a spell with it to find out what it does!";
						} else {
							final String suffix = effect.getElementCount() <= 1 ? ""
									: effect.getElementCount() <= 2 ? " II"
									: " III";
							
							name = I18n.format("effect." + action.getName() + ".name", (Object[]) null) + suffix;
							desc = I18n.format("effect." + action.getName() + ".desc", (Object[]) null);
						}
						
						int len = fontRenderer.getStringWidth(name);
						fontRenderer.drawString(matrixStackIn, name,
								(subWidth - len) / 2,
								0,
								0xFFFFFFFF);
						matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
						
						if (action != null) {
							final SpellActionProperties props = action.getProperties();
							final int iconWidth = 12;
							final int iconHeight = 12;
							matrixStackIn.push();
							matrixStackIn.translate(subWidth / 2, 0, 0);
							matrixStackIn.translate(-(4 + iconWidth), 0, 0);
							
							float color[] = {1f, 1f, 1f, 1f};
							if (!props.affectsEntity) {
								color = new float[] {.3f, .3f, .3f, .4f};
							}
							drawAffectEntity(matrixStackIn, iconWidth, iconHeight, color);
							
							matrixStackIn.translate(12 + 4, 0, 0);
							if (props.affectsBlock) {
								color = new float[] {1f, 1f, 1f, 1f};
							} else {
								color = new float[] {.3f, .3f, .3f, .4f};
							}
							drawAffectBlock(matrixStackIn, iconWidth, iconHeight, color);
							matrixStackIn.pop();
							
							matrixStackIn.translate(0, iconHeight + 2, 0);
						}
						
						int yUsed = RenderFuncs.drawSplitString(matrixStackIn, fontRenderer, desc,
								0,
								0,
								subWidth,
								0xFFA0A0A0);
						matrixStackIn.translate(0, yUsed, 0);
					}
					matrixStackIn.pop();
				}
			} else {
				final BasicSpellCraftContainer container = getContainer();
				final String summaryText = "Summary";
				final int summaryTextWidth = fontRenderer.getStringWidth(summaryText);
				fontRenderer.drawString(matrixStackIn, summaryText, ((POS_INFOPANEL_WIDTH-8) - summaryTextWidth)/2, 0, 0xFF000000);
				matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
				
				matrixStackIn.scale(.5f, .5f, 1f);
				
				// Mana cost
				fontRenderer.drawString(matrixStackIn, "Mana Cost: " + container.getManaCost(), 0, 0, 0xFF000000);
				matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
				
				// Weight
				fontRenderer.drawString(matrixStackIn, "Weight: " + container.getCurrentWeight(), 0, 0, 0xFF000000);
				matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
				
				// Reagents
				if (!container.getReagentStrings().isEmpty()) {
					fontRenderer.drawString(matrixStackIn, "Reagents:", 0, 0, 0xFF000000);
					matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
					for (ITextComponent string : container.getReagentStrings()) {
						fontRenderer.func_243248_b(matrixStackIn, string, 4, 0, 0xFF000000); //drawTextComponent()
						matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
					}
				}
			}
			matrixStackIn.pop();
		}
		
		protected void drawRuneCellBackground(MatrixStack matrixStackIn, int width, int height) {
			Minecraft.getInstance().getTextureManager().bindTexture(getBackgroundTexture());
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, 
					TEX_RUNESLOT_HOFFSET, TEX_RUNESLOT_VOFFSET, TEX_RUNESLOT_WIDTH, TEX_RUNESLOT_HEIGHT,
					width, height,
					TEX_WIDTH, TEX_HEIGHT
					);
		}
		
		protected void drawRuneSpacerBackground(MatrixStack matrixStackIn, int width, int height, boolean animate) {
			Minecraft.getInstance().getTextureManager().bindTexture(getBackgroundTexture());
//			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, 
//					TEX_RUNESPACER_HOFFSET, TEX_RUNESPACER_VOFFSET, TEX_RUNESPACER_WIDTH, TEX_RUNESPACER_HEIGHT,
//					width, height,
//					TEX_WIDTH, TEX_HEIGHT
//					);
			
			if (animate) {
				final int frameTimeMS = 500;
				final int animFrame = (int) (System.currentTimeMillis() % (frameTimeMS * TEX_RUNESPACER_ANIM_COUNT)) / frameTimeMS;
				
				final int v = TEX_RUNESPACER_VOFFSET + (animFrame * TEX_RUNESPACER_HEIGHT);
			
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, 
						TEX_RUNESPACER_HOFFSET, v, TEX_RUNESPACER_WIDTH, TEX_RUNESPACER_HEIGHT,
						width, height,
						TEX_WIDTH, TEX_HEIGHT
						);
			} else {
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, 
						TEX_RUNESPACER_DISABLED_HOFFSET, TEX_RUNESPACER_DISABLED_VOFFSET, TEX_RUNESPACER_DISABLED_WIDTH, TEX_RUNESPACER_DISABLED_HEIGHT,
						width, height,
						TEX_WIDTH, TEX_HEIGHT
						);
			}
		}
		
		protected void drawAffectEntity(MatrixStack matrixStackIn, int width, int height, float[] color) {
			SpellComponentIcon.get(NostrumSpellShapes.AtFeet)
				.draw(this, matrixStackIn, this.font, 0, 0, width, height, color[0], color[1], color[2], color[3]);
		}
		
		protected void drawAffectBlock(MatrixStack matrixStackIn, int width, int height, float[] color) {
			SpellComponentIcon.get(NostrumSpellShapes.Proximity)
				.draw(this, matrixStackIn, this.font, 0, 0, width, height, color[0], color[1], color[2], color[3]);
		}
	}
	
}
