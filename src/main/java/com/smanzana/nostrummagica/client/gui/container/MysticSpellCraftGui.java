package com.smanzana.nostrummagica.client.gui.container;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui.SpellCreationContainer;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui.SpellCreationContainer.RuneSlot;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui.SpellGui;
import com.smanzana.nostrummagica.crafting.ISpellCraftingInventory;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.SpellCraftMessage;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spellcraft.SpellCraftContext;
import com.smanzana.nostrummagica.spellcraft.SpellCrafting;
import com.smanzana.nostrummagica.spellcraft.modifier.ISpellCraftModifier;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;
import com.smanzana.nostrummagica.tile.ISpellCraftingTileEntity;
import com.smanzana.nostrummagica.tile.MysticSpellTableTileEntity;
import com.smanzana.nostrummagica.util.ContainerUtil;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

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
				Player crafter, Inventory playerInv, ISpellCraftingInventory tableInventory,
				BlockPos tablePos, @Nullable Container extraInventory) {
			this(NostrumContainers.SpellCreationMystic, windowId, crafter, playerInv, tableInventory, tablePos, extraInventory);
		}

		protected MysticContainer(MenuType<? extends SpellCreationContainer> type, int windowId,
				Player crafter, Inventory playerInv, ISpellCraftingInventory tableInventory,
				BlockPos tablePos, @Nullable Container extraInventory) {
			super(type, windowId, crafter, playerInv, tableInventory, tablePos);
			
			this.name = "";
			this.spellIcon = -1;
			this.patternIdx = 0;
			
			patternChoices = MysticSpellTableTileEntity.CalculatePatternChoices(crafter, tableInventory, tablePos);
			
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
						new TextComponent("Rune Library"));
			}
			
		}
		
		public static final MysticContainer FromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf buffer) {
			final MysticSpellTableTileEntity te = ContainerUtil.GetPackedTE(buffer);
			final ISpellCraftingInventory tableInv = te.getSpellCraftingInventory();
			final @Nullable Container extraInv = te.getExtraInventory();
			return new MysticContainer(windowId, playerInv.player, playerInv, tableInv, te.getBlockPos(), extraInv);
		}
		
		public static IPackedContainerProvider Make(MysticSpellTableTileEntity table) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new MysticContainer(windowId, player, playerInv, ((ISpellCraftingTileEntity) table).getSpellCraftingInventory(), table.getBlockPos(), table.getExtraInventory());
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
				super(x, y, width, height, TextComponent.EMPTY);
				this.gui = gui;
				this.isLeft = isLeft;
			}

			@Override
			public void onPress() {
				gui.patternChangeButtonClicked(this, isLeft);
			}
			
			@Override
			public void renderToolTip(PoseStack matrixStackIn, int mouseX, int mouseY) {
				;
			}
			
			@Override
			public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
				final int u, v, wu, hv;
				
				if (this.isHoveredOrFocused()) {
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

				RenderSystem.setShaderTexture(0, TEXT);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, this.x, this.y,
						u, v, wu, hv,
						this.width, this.height, TEX_WIDTH, TEX_HEIGHT
						);
			}

			@Override
			public void updateNarration(NarrationElementOutput p_169152_) {
				this.defaultButtonNarrationText(p_169152_);
			}
		}
		
		private EditBox nameField;
		private @Nullable SimpleInventoryWidget extraInventoryWidget;
		private @Nullable InfoPanel infoPanelWidget;
		private List<Rect2i> extraAreas;
		
		private Vec3i[] runeSlots;
		private Vec3i[] spacerSpots;
		private boolean modifierHovered;
		
		public Gui(MysticContainer container, Inventory playerInv, Component name) {
			super(container, playerInv, name);
			
			this.imageWidth = POS_CONTAINER_WIDTH;
			this.imageHeight = POS_CONTAINER_HEIGHT;
			
			final int runeSlotCount = getMenu().inventory.getRuneSlotCount();
			final int totalWidth = (runeSlotCount * POS_SLOT_RUNES_WIDTH) + ((runeSlotCount - 1) * POS_SLOT_RUNES_SPACER_WIDTH);
			final int runeSlotXOffset = (POS_CONTAINER_WIDTH - totalWidth) / 2;
			this.runeSlots = new Vec3i[runeSlotCount];
			this.spacerSpots = new Vec3i[Math.max(0, runeSlotCount-1)];
			for (int i = 0; i < runeSlotCount; i++) {
				runeSlots[i] = new Vec3i(-1 + runeSlotXOffset + (i * POS_SLOT_RUNES_WIDTH) + (i * POS_SLOT_RUNES_SPACER_WIDTH), -1 + POS_SLOT_RUNES_VOFFSET, i);
				if (i > 0) {
					spacerSpots[i-1] = new Vec3i(runeSlotXOffset + (i * POS_SLOT_RUNES_WIDTH) + ((i-1) * POS_SLOT_RUNES_SPACER_WIDTH), POS_SLOT_RUNES_VOFFSET, i-1);
				}
			}
		}
		
		@Override
		public void init() {
			super.init(); // Clears children
			
			final Minecraft mc = Minecraft.getInstance();
			final int horizontalMargin = ((width - imageWidth) / 2);
			final int verticalMargin = (height - imageHeight) / 2;
			
			// Name input field
			this.nameField = new EditBox(mc.font, horizontalMargin + POS_NAME_HOFFSET, verticalMargin + POS_NAME_VOFFSET, POS_NAME_WIDTH, POS_NAME_HEIGHT, TextComponent.EMPTY);
			this.nameField.setMaxLength(SpellCreationGui.MaxNameLength);
			this.nameField.setResponder((s) -> {
				getMenu().name = s;
				getMenu().validate();
			});
			this.nameField.setFilter((s) -> {
				// do this better? If it ends up sucking. Otherwise this is probably fine
				return s.codePoints().allMatch(SpellGui::isValidChar);
			});
			this.nameField.setValue(getMenu().getName());
			this.addRenderableWidget(this.nameField);
			
			
			// Spell icon buttons
			int extraMargin = 3;
			final int spaceWidth = horizontalMargin - (2 * extraMargin); // amount of space to draw in
			final int perRow = spaceWidth / POS_ICON_BUTTON_WIDTH;
			extraMargin += (spaceWidth % POS_ICON_BUTTON_WIDTH) / 2; // Center by adding remainder / 2
			
			extraAreas = new ArrayList<>(2);
			extraAreas.add(new Rect2i(extraMargin, verticalMargin, horizontalMargin - extraMargin, ((SpellIcon.numIcons / perRow) + 1) * POS_ICON_BUTTON_HEIGHT));
			for (int i = 0; i < SpellIcon.numIcons; i++) {
				SpellIconButton button = new SpellIconButton(
						extraMargin + (i % perRow) * POS_ICON_BUTTON_WIDTH,
						verticalMargin + (i / perRow) * POS_ICON_BUTTON_HEIGHT,
						POS_ICON_BUTTON_WIDTH,
						POS_ICON_BUTTON_HEIGHT,
						i,
						this);
				
				this.addRenderableWidget(button);
			}
			
			// Info panel
			if (NostrumMagica.getMagicWrapper(getMenu().player).hasSkill(NostrumSkills.Spellcraft_Infopanel)) {
				this.infoPanelWidget = new InfoPanel(horizontalMargin + POS_INFOPANEL_HOFFSET, verticalMargin + POS_INFOPANEL_VOFFSET, POS_INFOPANEL_WIDTH, POS_INFOPANEL_HEIGHT);
				this.infoPanelWidget.setContent(this::renderSpellPanel);
				this.addRenderableWidget(infoPanelWidget);
				extraAreas.add(new Rect2i(horizontalMargin + POS_INFOPANEL_HOFFSET, verticalMargin + POS_INFOPANEL_VOFFSET, POS_INFOPANEL_WIDTH, POS_INFOPANEL_HEIGHT));
				{
					// Weight status
					infoPanelWidget.addChild(new WeightStatus(this,
							horizontalMargin + POS_WEIGHTBAR_HOFFSET + (POS_INFOPANEL_WIDTH-POS_WEIGHTBAR_WIDTH) / 4, verticalMargin + POS_WEIGHTBAR_VOFFSET,
							POS_WEIGHTBAR_WIDTH, POS_WEIGHTBAR_HEIGHT));
				}
			}
			
			// Status icon
			this.addRenderableWidget(new SpellStatusIcon(this, horizontalMargin + POS_STATUS_HOFFSET, verticalMargin + POS_STATUS_VOFFSET, POS_STATUS_WIDTH, POS_STATUS_HEIGHT));
			
			// Submit button
			this.addRenderableWidget(new SubmitButton(this, horizontalMargin + POS_SUBMIT_HOFFSET, verticalMargin + POS_SUBMIT_VOFFSET, POS_SUBMIT_WIDTH, POS_SUBMIT_HEIGHT));
			
			// Pattern arrows
			this.addRenderableWidget(new PatternChangeButton(this, true, horizontalMargin + POS_LARROW_HOFFSET, verticalMargin + POS_LARROW_VOFFSET, POS_LARROW_WIDTH, POS_LARROW_HEIGHT));
			this.addRenderableWidget(new PatternChangeButton(this, false, horizontalMargin + POS_RARROW_HOFFSET, verticalMargin + POS_RARROW_VOFFSET, POS_RARROW_WIDTH, POS_RARROW_HEIGHT));
			
			// Pattern icon
			this.addRenderableWidget(new PatternIcon(this, horizontalMargin + POS_PATTERN_HOFFSET, verticalMargin + POS_PATTERN_VOFFSET, POS_PATTERN_WIDTH, POS_PATTERN_HEIGHT));

			// Extra inventory
			if (this.getMenu().extraInventory != null) {
				final SimpleInventoryContainerlet extraContainer = this.getMenu().extraInventory;
				this.extraInventoryWidget = new SimpleInventoryWidget(this, extraContainer);
				this.extraInventoryWidget.setColor(0xFF263D5A);
				this.addRenderableWidget(this.extraInventoryWidget);
				extraAreas.add(new Rect2i(horizontalMargin + extraContainer.x, verticalMargin + this.getMenu().extraInventory.y, this.getMenu().extraInventory.width, this.getMenu().extraInventory.height));
			}
			
			if (infoPanelWidget != null) {
				Vec3i[] belowSlots = new Vec3i[runeSlots.length];
				for (int i = 0; i < runeSlots.length; i++) {
					belowSlots[i] = new Vec3i(
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
				this.addRenderableWidget(partBarWidget);
			}

			this.getMenu().validate();
		}

		@Override
		protected void onIconSelected(int icon) {
			this.getMenu().spellIcon = icon;
		}

		@Override
		protected void onSubmit() {
			// clicked on submit button
			getMenu().validate();
			if (getMenu().spellValid) {
				// whoo make spell
				Spell spell = getMenu().makeSpell(true);
				if (spell != null) {
					// All of this happens again and is synced back to client
					// But in the mean, might as well do it here for the
					// smoothest feel
					ItemStack scroll = new ItemStack(NostrumItems.spellScroll, 1);
					//SpellScroll.setSpell(scroll, spell);
					//getContainer().setScroll(scroll);
					getMenu().inventory.setScrollSlotContents(scroll);
					//NostrumMagicaSounds.AMBIENT_WOOSH.play(Minecraft.getInstance().thePlayer);
					
					NetworkHandler.sendToServer(new SpellCraftMessage(
							getMenu().name.toString(),
							getMenu().pos,
							getMenu().spellIcon,
							getMenu().getCraftPattern()
							));
					getMenu().name = "";
					this.nameField.setValue("");
					getMenu().spellIcon = -1;
					// getContainer().patternIdx = 0; leave pattern selected?
				}
			} else {
				// Don't
			}
		}
		
		protected void patternChangeButtonClicked(PatternChangeButton button, boolean isLeft) {
			SpellCraftPattern[] choices = getMenu().getPatternChoices();
			
			// Safety check
			if (choices == null || choices.length == 0) {
				return;
			}
			
			MysticContainer container = getMenu();
			container.patternIdx = ((choices.length + container.patternIdx) + (isLeft ? -1 : 1)) % choices.length;
			container.validate();
		}
		
		@Override
		public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
			if (p_keyPressed_1_ == 256) {
				this.mc.player.closeContainer();
			}

			// Copied from AnvilScreen
			return !this.nameField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) && !this.nameField.canConsumeInput() ? super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) : true;
		}
		
		@Override
		public List<Rect2i> getGuiExtraAreas() {
			return extraAreas;
		}
		
		@Override
		protected void renderBg(PoseStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			int horizontalMargin = (width - imageWidth) / 2;
			int verticalMargin = (height - imageHeight) / 2;
			
			final @Nullable SpellCraftPattern pattern = getMenu().getCraftPattern();
			final SpellCraftContext context = getMenu().getCraftContext();
			
			RenderSystem.setShaderTexture(0, TEXT);
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin, 0, 0, POS_CONTAINER_WIDTH, POS_CONTAINER_HEIGHT, TEX_WIDTH, TEX_HEIGHT);
			
			// Manually draw rune slots, since they're not baked onto the sheet
			final int filledRuneSlots = getMenu().getFilledRuneSlots();
			for (Vec3i spacerPos : spacerSpots) {
				matrixStackIn.pushPose();
				matrixStackIn.translate(horizontalMargin + spacerPos.getX(), verticalMargin + spacerPos.getY(), 0);
				drawRuneSpacerBackground(matrixStackIn, POS_SLOT_RUNES_SPACER_WIDTH, POS_SLOT_RUNES_WIDTH, spacerPos.getZ() < filledRuneSlots-1);
				matrixStackIn.popPose();
			}
			for (Vec3i slotPos : runeSlots) {
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
				
				matrixStackIn.pushPose();
				matrixStackIn.translate(horizontalMargin + slotPos.getX(), verticalMargin + slotPos.getY(), 0);
				drawRuneCellBackground(matrixStackIn, POS_SLOT_RUNES_WIDTH, POS_SLOT_RUNES_WIDTH, isModified);
				matrixStackIn.popPose();
			}
			
			// Decide whether to show summary, part info, or modifier info
			if (this.hoveredSlot != null && this.hoveredSlot instanceof RuneSlot && infoPanelWidget != null) {
				int highlightedRuneIdx = ((RuneSlot) this.hoveredSlot).getSlotIndex() - getMenu().inventory.getRuneSlotStartingIndex();
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
		protected void renderLabels(PoseStack matrixStackIn, int mouseX, int mouseY) {
			if (!getMenu().hasScroll()) {
				Minecraft mc = Minecraft.getInstance();
				final int width = 150;
				final int height = 50;
				matrixStackIn.pushPose();
				matrixStackIn.translate(this.imageWidth / 2, (height+20) / 2, 300);
				SpellGui.drawScrollMessage(matrixStackIn, width, height, mc.font);
				matrixStackIn.popPose();
			}
			
			final @Nullable SpellCraftPattern pattern = getMenu().getCraftPattern();
			final SpellCraftContext context = getMenu().getCraftContext();
			
			for (Vec3i slotPos : runeSlots) {
				final int runeSlotIdx = slotPos.getZ();
				final boolean isModified;
				final boolean hasModifier;
				
				if (pattern != null) {
					if (pattern.hasModifier(context, runeSlotIdx)) {
						@Nullable ISpellCraftModifier modifier = pattern.getModifier(context, runeSlotIdx);
						if (modifier != null) {
							hasModifier = true;
							final ItemStack rune = getMenu().inventory.getRuneSlotContents(runeSlotIdx);
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
					matrixStackIn.pushPose();
					matrixStackIn.translate(slotPos.getX(), slotPos.getY(), 300);
					RenderFuncs.drawGradientRect(matrixStackIn, 0, POS_SLOT_RUNES_WIDTH/4, POS_SLOT_RUNES_WIDTH, POS_SLOT_RUNES_WIDTH,
							0x00FFFFFF, 0x00FFFFFF, // top colors
							color, color // bottom colors
						);
					matrixStackIn.popPose();
				}
			}
		}
		
		protected void drawRuneCellBackground(PoseStack matrixStackIn, int width, int height, boolean isModified) {
			RenderSystem.setShaderTexture(0, TEXT);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, 
					TEX_RUNESLOT_HOFFSET, TEX_RUNESLOT_VOFFSET, TEX_RUNESLOT_WIDTH, TEX_RUNESLOT_HEIGHT,
					width, height,
					TEX_WIDTH, TEX_HEIGHT
					);
		}
		
		protected void drawRuneSpacerBackground(PoseStack matrixStackIn, int width, int height, boolean animate) {
			RenderSystem.setShaderTexture(0, TEXT);
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
