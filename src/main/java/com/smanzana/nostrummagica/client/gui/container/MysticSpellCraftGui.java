package com.smanzana.nostrummagica.client.gui.container;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.client.gui.container.SimpleInventoryWidget.SimpleInventoryContainerlet;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui.SpellCreationContainer;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui.SpellCreationContainer.RuneSlot;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui.SpellGui;
import com.smanzana.nostrummagica.crafting.ISpellCraftingInventory;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.SpellScroll;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.SpellCraftMessage;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spellcraft.SpellCraftContext;
import com.smanzana.nostrummagica.spellcraft.SpellCrafting;
import com.smanzana.nostrummagica.spellcraft.modifier.ISpellCraftModifier;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;
import com.smanzana.nostrummagica.tile.ISpellCraftingTileEntity;
import com.smanzana.nostrummagica.tile.MysticSpellTableEntity;
import com.smanzana.nostrummagica.util.ContainerUtil;
import com.smanzana.nostrummagica.util.RenderFuncs;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.renderer.Rectangle2d;
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

public class MysticSpellCraftGui {
	
	// Shared positioning variables. Out here because container needs size info to position slots. :(
	protected static final int POS_CONTAINER_WIDTH = 202;
	protected static final int POS_CONTAINER_HEIGHT = 177;
	
	protected static final int POS_SLOT_SCROLL_HOFFSET = 23;
	protected static final int POS_SLOT_SCROLL_VOFFSET = 16;
	
	protected static final int POS_SLOT_RUNES_VOFFSET = 42;
	protected static final int POS_SLOT_RUNES_WIDTH = 18;
	protected static final int POS_SLOT_RUNES_SPACER_WIDTH = 4;
	
	protected static final int POS_SLOT_PLAYERINV_HOFFSET = 21;
	protected static final int POS_SLOT_PLAYERINV_VOFFSET = 96;
	
	protected static final int POS_SLOT_HOTBAR_HOFFSET = 21;
	protected static final int POS_SLOT_HOTBAR_VOFFSET = 154;
	
	protected static final int POS_INFOPANEL_HOFFSET = POS_CONTAINER_WIDTH;
	protected static final int POS_INFOPANEL_VOFFSET = 0;
	protected static final int POS_INFOPANEL_WIDTH = 80;
	protected static final int POS_INFOPANEL_HEIGHT = 90;

	public static class MysticContainer extends SpellCreationContainer {
		
		public static final String ID = "mysticspellcrafter";
		
		private final SpellCraftPattern[] patternChoices; // Cached
		
		protected String name;
		protected int spellIcon;
		protected int patternIdx;
		protected @Nullable SimpleInventoryContainerlet extraInventory;
		
		public MysticContainer(int windowId,
				PlayerEntity crafter, PlayerInventory playerInv, ISpellCraftingInventory tableInventory,
				BlockPos tablePos, @Nullable IInventory extraInventory) {
			this(NostrumContainers.SpellCreationMystic, windowId, crafter, playerInv, tableInventory, tablePos, extraInventory);
		}

		protected MysticContainer(ContainerType<? extends SpellCreationContainer> type, int windowId,
				PlayerEntity crafter, PlayerInventory playerInv, ISpellCraftingInventory tableInventory,
				BlockPos tablePos, @Nullable IInventory extraInventory) {
			super(type, windowId, crafter, playerInv, tableInventory, tablePos);
			
			this.name = "";
			this.spellIcon = -1;
			this.patternIdx = 0;
			
			patternChoices = MysticSpellTableEntity.CalculatePatternChoices(crafter, tableInventory, tablePos);
			
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
						new StringTextComponent("Rune Library"));
			}
			
		}
		
		public static final MysticContainer FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buffer) {
			final MysticSpellTableEntity te = ContainerUtil.GetPackedTE(buffer);
			final ISpellCraftingInventory tableInv = te.getSpellCraftingInventory();
			final @Nullable IInventory extraInv = te.getExtraInventory();
			return new MysticContainer(windowId, playerInv.player, playerInv, tableInv, te.getPos(), extraInv);
		}
		
		public static IPackedContainerProvider Make(MysticSpellTableEntity table) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new MysticContainer(windowId, player, playerInv, ((ISpellCraftingTileEntity) table).getSpellCraftingInventory(), table.getPos(), table.getExtraInventory());
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
			return (this.patternChoices.length == 0 || this.patternIdx >= this.patternChoices.length)
					? null
					: patternChoices[this.patternIdx];
		}
		
		protected SpellCraftPattern[] getPatternChoices() {
			return this.patternChoices;
		}
	}
	
	public static class Gui extends SpellGui<MysticContainer> {
		
		private static final ResourceLocation TEXT = NostrumMagica.Loc("textures/gui/container/spell_create_mystic.png");
		
		private static final int TEX_WIDTH = 256;
		private static final int TEX_HEIGHT = 256;
		
		private static final int TEX_RUNESLOT_HOFFSET = 0;
		private static final int TEX_RUNESLOT_VOFFSET = 177;
		private static final int TEX_RUNESLOT_WIDTH = 18;
		private static final int TEX_RUNESLOT_HEIGHT = 18;
		
		private static final int TEX_RUNESPACER_DISABLED_HOFFSET = 18;
		private static final int TEX_RUNESPACER_DISABLED_VOFFSET = 177;
		private static final int TEX_RUNESPACER_DISABLED_WIDTH = 4;
		private static final int TEX_RUNESPACER_DISABLED_HEIGHT = 18;
		
		private static final int TEX_RUNESPACER_HOFFSET = TEX_RUNESPACER_DISABLED_HOFFSET + TEX_RUNESPACER_DISABLED_WIDTH;
		private static final int TEX_RUNESPACER_VOFFSET = TEX_RUNESPACER_DISABLED_VOFFSET;
		private static final int TEX_RUNESPACER_WIDTH = TEX_RUNESPACER_DISABLED_WIDTH;
		private static final int TEX_RUNESPACER_HEIGHT = TEX_RUNESPACER_DISABLED_HEIGHT;
		private static final int TEX_RUNESPACER_ANIM_COUNT = 4;
		
		private static final int TEX_RARROW_HOFFSET = 38;
		private static final int TEX_RARROW_VOFFSET = 177;
		private static final int TEX_RARROW_WIDTH = 18;
		private static final int TEX_RARROW_HEIGHT = 10;
		
		private static final int TEX_RARROW_HIGH_HOFFSET = TEX_RARROW_HOFFSET + TEX_RARROW_WIDTH;
		private static final int TEX_RARROW_HIGH_VOFFSET = TEX_RARROW_VOFFSET;
		private static final int TEX_RARROW_HIGH_WIDTH = TEX_RARROW_WIDTH;
		private static final int TEX_RARROW_HIGH_HEIGHT = TEX_RARROW_HEIGHT;
		
		private static final int TEX_LARROW_HOFFSET = 38;
		private static final int TEX_LARROW_VOFFSET = 187;
		private static final int TEX_LARROW_WIDTH = 18;
		private static final int TEX_LARROW_HEIGHT = 10;
		
		private static final int TEX_LARROW_HIGH_HOFFSET = TEX_LARROW_HOFFSET + TEX_LARROW_WIDTH;
		private static final int TEX_LARROW_HIGH_VOFFSET = TEX_LARROW_VOFFSET;
		private static final int TEX_LARROW_HIGH_WIDTH = TEX_LARROW_WIDTH;
		private static final int TEX_LARROW_HIGH_HEIGHT = TEX_LARROW_HEIGHT;
		
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
		private static final int POS_WEIGHTBAR_VOFFSET = POS_INFOPANEL_VOFFSET + 2;
		private static final int POS_WEIGHTBAR_WIDTH = 50;
		private static final int POS_WEIGHTBAR_HEIGHT = 16;
		
		private static final int POS_PATTERN_HOFFSET = 91;
		private static final int POS_PATTERN_VOFFSET = 65;
		private static final int POS_PATTERN_WIDTH = 20;
		private static final int POS_PATTERN_HEIGHT = 20;
		
		private static final int POS_LARROW_HOFFSET = 71;
		private static final int POS_LARROW_VOFFSET = 70;
		private static final int POS_LARROW_WIDTH = 18;
		private static final int POS_LARROW_HEIGHT = 10;
		
		private static final int POS_RARROW_HOFFSET = 113;
		private static final int POS_RARROW_VOFFSET = 70;
		private static final int POS_RARROW_WIDTH = 18;
		private static final int POS_RARROW_HEIGHT = 10;
		
		protected static class PatternChangeButton extends AbstractButton {
			private final Gui gui;
			private final boolean isLeft;
			
			public PatternChangeButton(Gui gui, boolean isLeft, int x, int y, int width, int height) {
				super(x, y, width, height, StringTextComponent.EMPTY);
				this.gui = gui;
				this.isLeft = isLeft;
			}

			@Override
			public void onPress() {
				gui.patternChangeButtonClicked(this, isLeft);
			}
			
			@Override
			public void renderToolTip(MatrixStack matrixStackIn, int mouseX, int mouseY) {
				;
			}
			
			@Override
			public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
				final Minecraft mc = Minecraft.getInstance();
				
				final int u, v, wu, hv;
				
				if (this.isHovered()) {
					if (this.isLeft) {
						u = TEX_LARROW_HIGH_HOFFSET;
						v = TEX_LARROW_HIGH_VOFFSET;
						wu = TEX_LARROW_HIGH_WIDTH;
						hv = TEX_LARROW_HIGH_HEIGHT;
					} else {
						u = TEX_RARROW_HIGH_HOFFSET;
						v = TEX_RARROW_HIGH_VOFFSET;
						wu = TEX_RARROW_HIGH_WIDTH;
						hv = TEX_RARROW_HIGH_HEIGHT;
					}
				} else {
					if (this.isLeft) {
						u = TEX_LARROW_HOFFSET;
						v = TEX_LARROW_VOFFSET;
						wu = TEX_LARROW_WIDTH;
						hv = TEX_LARROW_HEIGHT;
					} else {
						u = TEX_RARROW_HOFFSET;
						v = TEX_RARROW_VOFFSET;
						wu = TEX_RARROW_WIDTH;
						hv = TEX_RARROW_HEIGHT;
					}
				}

				mc.getTextureManager().bindTexture(TEXT);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, this.x, this.y,
						u, v, wu, hv,
						this.width, this.height, TEX_WIDTH, TEX_HEIGHT
						);
			}
		}
		
		private TextFieldWidget nameField;
		private @Nullable SimpleInventoryWidget extraInventoryWidget;
		private InfoPanel infoPanelWidget;
		private List<Rectangle2d> extraAreas;
		
		private Vector3i[] runeSlots;
		private Vector3i[] spacerSpots;
		private boolean modifierHovered;
		
		public Gui(MysticContainer container, PlayerInventory playerInv, ITextComponent name) {
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
			this.infoPanelWidget = new InfoPanel(horizontalMargin + POS_INFOPANEL_HOFFSET, verticalMargin + POS_INFOPANEL_VOFFSET, POS_INFOPANEL_WIDTH, POS_INFOPANEL_HEIGHT);
			this.infoPanelWidget.setContent(this::renderSpellPanel);
			this.addButton(infoPanelWidget);
			extraAreas.add(new Rectangle2d(horizontalMargin + POS_INFOPANEL_HOFFSET, verticalMargin + POS_INFOPANEL_VOFFSET, POS_INFOPANEL_WIDTH, POS_INFOPANEL_HEIGHT));
			{
				// Weight status
				infoPanelWidget.addChild(new WeightStatus(this,
						horizontalMargin + POS_WEIGHTBAR_HOFFSET + (POS_INFOPANEL_WIDTH-POS_WEIGHTBAR_WIDTH) / 4, verticalMargin + POS_WEIGHTBAR_VOFFSET,
						POS_WEIGHTBAR_WIDTH, POS_WEIGHTBAR_HEIGHT));
			}
			
			// Status icon
			this.addButton(new SpellStatusIcon(this, horizontalMargin + POS_STATUS_HOFFSET, verticalMargin + POS_STATUS_VOFFSET, POS_STATUS_WIDTH, POS_STATUS_HEIGHT));
			
			// Submit button
			this.addButton(new SubmitButton(this, horizontalMargin + POS_SUBMIT_HOFFSET, verticalMargin + POS_SUBMIT_VOFFSET, POS_SUBMIT_WIDTH, POS_SUBMIT_HEIGHT));
			
			// Pattern arrows
			this.addButton(new PatternChangeButton(this, true, horizontalMargin + POS_LARROW_HOFFSET, verticalMargin + POS_LARROW_VOFFSET, POS_LARROW_WIDTH, POS_LARROW_HEIGHT));
			this.addButton(new PatternChangeButton(this, false, horizontalMargin + POS_RARROW_HOFFSET, verticalMargin + POS_RARROW_VOFFSET, POS_RARROW_WIDTH, POS_RARROW_HEIGHT));
			
			// Pattern icon
			this.addButton(new PatternIcon(this, horizontalMargin + POS_PATTERN_HOFFSET, verticalMargin + POS_PATTERN_VOFFSET, POS_PATTERN_WIDTH, POS_PATTERN_HEIGHT));

			// Extra inventory
			if (this.getContainer().extraInventory != null) {
				final SimpleInventoryContainerlet extraContainer = this.getContainer().extraInventory;
				this.extraInventoryWidget = new SimpleInventoryWidget(this, extraContainer);
				this.extraInventoryWidget.setColor(0xFF263D5A);
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
			SpellPartBar partBarWidget = new SpellPartBar(this, belowSlots, POS_SLOT_RUNES_WIDTH, (part, matrix, mouseX, mouseY) -> {
				if (part == null) {
					infoPanelWidget.setContent(this::renderSpellPanel);
				} else {
					this.infoPanelWidget.setContent((matrixStackIn, width, height, partialTicks) -> {
						this.renderSpellPartPanel(part, matrixStackIn, width, height, partialTicks);
					});
				}
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
							getContainer().spellIcon,
							getContainer().getCraftPattern()
							));
					getContainer().name = "";
					this.nameField.setText("");
					getContainer().spellIcon = -1;
					// getContainer().patternIdx = 0; leave pattern selected?
				}
			} else {
				// Don't
			}
		}
		
		protected void patternChangeButtonClicked(PatternChangeButton button, boolean isLeft) {
			SpellCraftPattern[] choices = getContainer().getPatternChoices();
			
			// Safety check
			if (choices == null || choices.length == 0) {
				return;
			}
			
			MysticContainer container = getContainer();
			container.patternIdx = ((choices.length + container.patternIdx) + (isLeft ? -1 : 1)) % choices.length;
			container.validate();
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
		
		@Override
		protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			
			final @Nullable SpellCraftPattern pattern = getContainer().getCraftPattern();
			final SpellCraftContext context = getContainer().getCraftContext();
			
			mc.getTextureManager().bindTexture(TEXT);
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin, 0, 0, POS_CONTAINER_WIDTH, POS_CONTAINER_HEIGHT, TEX_WIDTH, TEX_HEIGHT);
			
			// Manually draw rune slots, since they're not baked onto the sheet
			final int filledRuneSlots = getContainer().getFilledRuneSlots();
			for (Vector3i spacerPos : spacerSpots) {
				matrixStackIn.push();
				matrixStackIn.translate(horizontalMargin + spacerPos.getX(), verticalMargin + spacerPos.getY(), 0);
				drawRuneSpacerBackground(matrixStackIn, POS_SLOT_RUNES_SPACER_WIDTH, POS_SLOT_RUNES_WIDTH, spacerPos.getZ() < filledRuneSlots-1);
				matrixStackIn.pop();
			}
			for (Vector3i slotPos : runeSlots) {
				final int runeSlotIdx = slotPos.getZ();
				final boolean isModified;
				if (pattern != null) {
					final @Nullable ISpellCraftModifier modifier;
					if (pattern.hasModifier(context, runeSlotIdx)) {
						modifier = pattern.getModifier(context, runeSlotIdx);
					} else {
						modifier = null;
					}
					isModified = modifier != null;
				} else {
					isModified = false;
				}
				
				matrixStackIn.push();
				matrixStackIn.translate(horizontalMargin + slotPos.getX(), verticalMargin + slotPos.getY(), 0);
				drawRuneCellBackground(matrixStackIn, POS_SLOT_RUNES_WIDTH, POS_SLOT_RUNES_WIDTH, isModified);
				matrixStackIn.pop();
			}
			
			// Decide whether to show summary, part info, or modifier info
			if (this.hoveredSlot != null && this.hoveredSlot instanceof RuneSlot) {
				int highlightedRuneIdx = ((RuneSlot) this.hoveredSlot).getSlotIndex() - getContainer().inventory.getRuneSlotStartingIndex();
				final @Nullable ISpellCraftModifier modifier;
				if (pattern != null && pattern.hasModifier(context, highlightedRuneIdx)) {
					modifier = pattern.getModifier(context, highlightedRuneIdx);
				} else {
					modifier = null;
				}
				this.infoPanelWidget.setContent((matrix, widthIn, heightIn, partialTicksIn) -> {
					this.renderModifierPanel(modifier, matrix, widthIn, heightIn, partialTicksIn);
				});
				modifierHovered = true;
			} else if (modifierHovered) {
				modifierHovered = false;
				this.infoPanelWidget.setContent(this::renderSpellPanel);
			}
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
			
			final @Nullable SpellCraftPattern pattern = getContainer().getCraftPattern();
			final SpellCraftContext context = getContainer().getCraftContext();
			
			for (Vector3i slotPos : runeSlots) {
				final int runeSlotIdx = slotPos.getZ();
				final boolean isModified;
				final boolean hasModifier;
				
				if (pattern != null) {
					if (pattern.hasModifier(context, runeSlotIdx)) {
						@Nullable ISpellCraftModifier modifier = pattern.getModifier(context, runeSlotIdx);
						if (modifier != null) {
							hasModifier = true;
							final ItemStack rune = getContainer().inventory.getRuneSlotContents(runeSlotIdx);
							if (!rune.isEmpty()) {
								isModified = modifier.canModify(context, SpellCrafting.MakeIngredient(rune));
							} else {
								isModified = false;
							}
						} else {
							hasModifier = isModified = false;
						}
					} else {
						hasModifier = isModified = false;
					}
				} else {
					hasModifier = isModified = false;
				}
				
				if (hasModifier) {
					final int color;
					if (isModified) {
						color = 0x60FFFF00;
					} else {
						color = 0x60FFFFFF;
					}
					matrixStackIn.push();
					matrixStackIn.translate(slotPos.getX(), slotPos.getY(), 300);
					RenderFuncs.drawGradientRect(matrixStackIn, 0, POS_SLOT_RUNES_WIDTH/4, POS_SLOT_RUNES_WIDTH, POS_SLOT_RUNES_WIDTH,
							0x00FFFFFF, 0x00FFFFFF, // top colors
							color, color // bottom colors
						);
					matrixStackIn.pop();
				}
			}
		}
		
		protected void drawRuneCellBackground(MatrixStack matrixStackIn, int width, int height, boolean isModified) {
			Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, 
					TEX_RUNESLOT_HOFFSET, TEX_RUNESLOT_VOFFSET, TEX_RUNESLOT_WIDTH, TEX_RUNESLOT_HEIGHT,
					width, height,
					TEX_WIDTH, TEX_HEIGHT
					);
		}
		
		protected void drawRuneSpacerBackground(MatrixStack matrixStackIn, int width, int height, boolean animate) {
			Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
			if (animate) {
				final int frameTimeMS = 500;
				final int animFrame = (int) (System.currentTimeMillis() % (frameTimeMS * TEX_RUNESPACER_ANIM_COUNT)) / frameTimeMS;
				
				final int u = TEX_RUNESPACER_HOFFSET + (animFrame * TEX_RUNESPACER_WIDTH);
			
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, 
						u, TEX_RUNESPACER_VOFFSET, TEX_RUNESPACER_WIDTH, TEX_RUNESPACER_HEIGHT,
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
	}
	
}
