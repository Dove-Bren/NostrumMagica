package com.smanzana.nostrummagica.client.gui.container;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.SimpleInventoryWidget.SimpleInventoryContainerlet;
import com.smanzana.nostrummagica.client.gui.widget.FixedWidget;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.RuneShaperMessage;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.tile.RuneShaperTileEntity;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.ContainerUtil;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.util.ContainerUtil.NoisySlot;
import com.smanzana.nostrummagica.util.ItemStacks;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RuneShaperGui {
	
	private static final int GUI_WIDTH = 202;
	private static final int GUI_HEIGHT = 221;
	private static final int POS_PLAYER_INV_HOFFSET = 23;
	private static final int POS_PLAYER_INV_VOFFSET = 140;
	
	private static final int POS_SLOT_INPUT_HOFFSET = 95;
	private static final int POS_SLOT_INPUT_VOFFSET = 8;
	
	private static final int POS_SLOT_INGREDIENT_HOFFSET = 156;
	private static final int POS_SLOT_INGREDIENT_VOFFSET = 111;
	
	public static class RuneShaperContainer extends Container {
		
		public static final String ID = "rune_shaper";
		
		// Actual container variables as well as a couple for keeping track
		// of crafting state
		protected final RuneShaperTileEntity shaper;
		protected final Inventory modItemInventory;
		protected final SimpleInventoryContainerlet extraInv;
		
		private final HideableSlot ingredientSlot;
		
		public RuneShaperContainer(int windowId, PlayerEntity player, IInventory playerInv, RuneShaperTileEntity tableInventory, BlockPos pos, @Nullable IInventory extraInventory) {
			super(NostrumContainers.RuneShaper, windowId);
			this.shaper = tableInventory;
			this.modItemInventory = new Inventory(1);
			
			// Rune slot
			this.addSlot(new NoisySlot(shaper, 0, POS_SLOT_INPUT_HOFFSET, POS_SLOT_INPUT_VOFFSET, this::runeChanged));
			ingredientSlot = new HideableSlot(modItemInventory, 0, POS_SLOT_INGREDIENT_HOFFSET, POS_SLOT_INGREDIENT_VOFFSET);
			this.addSlot(ingredientSlot);
			
			// Construct player inventory
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 9; x++) {
					this.addSlot(new Slot(playerInv, x + y * 9 + 9, POS_PLAYER_INV_HOFFSET + (x * 18), POS_PLAYER_INV_VOFFSET + (y * 18)));
				}
			}
			// Construct player hotbar
			for (int x = 0; x < 9; x++) {
				this.addSlot(new Slot(playerInv, x, POS_PLAYER_INV_HOFFSET + x * 18, 58 + (POS_PLAYER_INV_VOFFSET)));
			}
			
			if (extraInventory != null) {
				final int height = 88;
				this.extraInv = new SimpleInventoryContainerlet(this::addSlot, extraInventory, HideableSlot::new, GUI_WIDTH, GUI_HEIGHT	- height, 100, height, new StringTextComponent("Chest"));
			} else {
				this.extraInv = null;
			}
		}
		
		public static final RuneShaperContainer FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
			RuneShaperTileEntity te = ContainerUtil.GetPackedTE(buf);
			return new RuneShaperContainer(windowId, playerInv.player, playerInv, te, buf.readBlockPos(), te.getExtraInventory());
		}
		
		public static final IPackedContainerProvider Make(RuneShaperTileEntity table) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new RuneShaperContainer(windowId, player, playerInv, table, table.getPos(), table.getExtraInventory());
			}, (buffer) -> {
				ContainerUtil.PackTE(buffer, table);
				buffer.writeBlockPos(table.getPos());
			});
		}
		
		@Override
		public @Nonnull ItemStack transferStackInSlot(PlayerEntity playerIn, int fromSlot) {
			Slot slot = (Slot) this.inventorySlots.get(fromSlot);
			
			if (slot != null && slot.getHasStack()) {
				ItemStack cur = slot.getStack();
				
				if (slot.inventory == this.shaper) {
					// Trying to take our rune
					if (playerIn.inventory.addItemStackToInventory(cur)) {
						slot.putStack(ItemStack.EMPTY);
						cur = slot.onTake(playerIn, cur);
					}
				} else {
					// Trying to add an item
					if (shaper.canShapeRune(cur)) {
						// Rune piece, so only care about first slot
						ItemStack existingStack = shaper.getRuneSlot();
						
						if (existingStack.isEmpty() || ItemStacks.stacksMatch(existingStack, cur)) {
							int room = cur.getMaxStackSize();
							if (!existingStack.isEmpty()) {
								room -= existingStack.getCount();
							}
							
							if (room > 0) {
								final ItemStack taken;
								final ItemStack remaining;
								if (existingStack.isEmpty()) {
									shaper.setInventorySlotContents(0, cur);
									taken = cur;
									remaining = ItemStack.EMPTY;
								} else {
									final int amt = room >= cur.getCount() ? cur.getCount() : room;
									existingStack.setCount(existingStack.getCount() + amt);
									shaper.setInventorySlotContents(0, existingStack); // generate change event
									taken = cur.split(amt);
									remaining = cur;
								}
								
								slot.putStack(remaining);
								slot.onTake(playerIn, taken);
							}
						}
					}
				}
				
				if (cur.isEmpty() || cur.getCount() <= 0) {
					slot.putStack(ItemStack.EMPTY);
				}
			}
			
			return ItemStack.EMPTY;
		}
		
		@Override
		public boolean canDragIntoSlot(Slot slotIn) {
			return true;
		}
		
		@Override
		public boolean canInteractWith(PlayerEntity playerIn) {
			return true;
		}
		
		@Override
		public void onContainerClosed(PlayerEntity playerIn) {
			super.onContainerClosed(playerIn);
			this.clearContainer(playerIn, playerIn.world, this.modItemInventory);
		}
		
		protected void setAcceptingInput(boolean accepting) {
			this.ingredientSlot.setHidden(!accepting);
		}
		
		protected @Nullable SpellShape getRuneShape() {
			return SpellRune.getShape(this.shaper.getRuneSlot());
		}
		
		protected @Nullable SpellShapeProperties getRuneProperties() {
			return SpellRune.GetPieceShapeParam(this.shaper.getRuneSlot());
		}
		
		protected void runeChanged(NoisySlot changedSlot) {
			;
		}
		
		protected ItemStack getRune() {
			return this.shaper.getRuneSlot();
		}
		
		protected ItemStack getIngredient() {
			return this.modItemInventory.getStackInSlot(0);
		}
		
		protected <T> SpellShapeProperties setOnProperties(SpellShapeProperties props, SpellShapeProperty<T> property, int valueIdx) {
			return props.setValue(property, property.getPossibleValues()[valueIdx]);
		}

		public boolean handleSubmitAttempt(SpellShape shape, SpellShapeProperty<?> property, int propertyValueIdx) {
			// Validate shape, property, and index
			final SpellShape curShape = this.getRuneShape();
			if (shape != curShape) {
				NostrumMagica.logger.warn("Rejecting runeshaper submit: wrong shape");
				return false;
			}
			
			if (!shape.getDefaultProperties().hasValue(property)) {
				NostrumMagica.logger.warn("Rejecting runeshaper submit: inexistent property");
				return false;
			}
			
			final Object[] values = property.getPossibleValues();
			if (values.length <= propertyValueIdx) {
				NostrumMagica.logger.warn("Rejecting runeshaper submit: too high idx");
				return false;
			}
			
			// Check required input
			ItemStack required = null;
			final NonNullList<ItemStack> requirements = shape.supportedFloatCosts(property);
			if (requirements != null) {
				required = requirements.get(propertyValueIdx);
				if (!required.isEmpty()) {
					if (!ItemStack.areItemsEqual(required, getIngredient())) {
						NostrumMagica.logger.warn("Rejecting runeshaper submit: missing ingredient");
						return false;
					}
				}
			}
			
			// Apply the change to the rune
			SpellRune.setPieceParam(getRune(), setOnProperties(getRuneProperties(), property, propertyValueIdx));
			
			// Consume the ingredient
			if (required != null && !required.isEmpty()) {
				getIngredient().split(1);
			}
			return true;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class RuneShaperGuiContainer extends AutoGuiContainer<RuneShaperContainer> implements IJEIAwareGuiContainer {
		
		public static class SubmitButton extends AbstractButton {
			
			private final RuneShaperGuiContainer gui;
			
			public SubmitButton(RuneShaperGuiContainer gui, int x, int y, int width, int height) {
				super(x, y, width, height, StringTextComponent.EMPTY);
				this.gui = gui;
			}

			@Override
			public void onPress() {
				if (canSubmit()) {
					NetworkHandler.sendToServer(new RuneShaperMessage(gui.container.shaper.getPos(), 
							gui.container.getRuneShape(),
							gui.selectedProperty,
							gui.propertyValueIdx
							));
					
				}
			}
			
			protected boolean canSubmit() {
				return gui.canSubmit();
			}
			
			@Override
			public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
				final int u;
				final int v;
				final int uw;
				final int vh;
				if (canSubmit()) {
					u = TEX_SUBMIT_VALID_HOFFSET;
					v = TEX_SUBMIT_VALID_VOFFSET;
					uw = TEX_SUBMIT_VALID_WIDTH;
					vh = TEX_SUBMIT_VALID_HEIGHT;
				} else {
					u = TEX_SUBMIT_HOFFSET;
					v = TEX_SUBMIT_VOFFSET;
					uw = TEX_SUBMIT_WIDTH;
					vh = TEX_SUBMIT_HEIGHT;
				}
				
				final float[] color = ColorUtil.ARGBToColor(this.isHovered() ? 0xFFAAAAAA : 0xFFFFFFFF);

				gui.mc.getTextureManager().bindTexture(TEXT);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, this.x, this.y,
						u, v, uw, vh,
						this.width, this.height,
						TEX_WIDTH, TEX_HEIGHT,
						color[0], color[1], color[2], color[3]);
			}
		}
		
		public static class IngredientSlotWidget extends FixedWidget {
			
			private final RuneShaperGuiContainer gui;
			
			public IngredientSlotWidget(RuneShaperGuiContainer gui, int x, int y, int width, int height) {
				super(x, y, width, height, StringTextComponent.EMPTY);
				this.gui = gui;
			}
			
			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				return false;
			}
			
			protected boolean validItem() {
				return gui.ingredientValid();
			}
			
			@Override
			public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
				if (!gui.container.ingredientSlot.isEnabled()) {
					return;
				}
				
				gui.mc.getTextureManager().bindTexture(TEXT);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, this.x, this.y,
						TEX_INGSLOT_HOFFSET, TEX_INGSLOT_VOFFSET, TEX_INGSLOT_WIDTH, TEX_INGSLOT_HEIGHT,
						this.width, this.height,
						TEX_WIDTH, TEX_HEIGHT);
				
				if (!validItem()) {
					matrixStackIn.push();
					matrixStackIn.translate(x, y, 0);
					if (gui.container.getIngredient().isEmpty()) {
						ItemStack needed = gui.container.getRuneShape().supportedFloatCosts(gui.selectedProperty).get(gui.propertyValueIdx);
						if (needed != null && !needed.isEmpty()) { 
							matrixStackIn.push();
							matrixStackIn.translate(0, 0, -50);
							RenderFuncs.RenderGUIItem(needed, matrixStackIn, 1, 1);
							matrixStackIn.pop();
						}
					}
					
					matrixStackIn.translate(0, 0, 60);
					RenderFuncs.drawRect(matrixStackIn, 1, 1, width - 1, height - 1, 0x30FF0000);
					matrixStackIn.pop();
				}
			}
		}

		public static class PropertyWidget extends AbstractButton {
			
			private final RuneShaperGuiContainer gui;
			private final SpellShapeProperty<?> property;
			
			public PropertyWidget(RuneShaperGuiContainer gui, SpellShapeProperty<?> property, ITextComponent label, int x, int y, int width, int height) {
				super(x, y, width, height, label);
				this.gui = gui;
				this.property = property;
			}

			@Override
			public void onPress() {
				gui.setSelectedProperty(this.property);
			}
			
			protected boolean isSelected() {
				return gui.selectedProperty == this.property;
			}
			
			@Override
			public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
				int color = 0xFFFFFFFF;
				if (isSelected()) {
					color -= 0x004F4F4F;
				}
				if (isHovered()) {
					color -= 0x00202020;
				}
				
				final float colors[] = ColorUtil.ARGBToColor(color);
				
				gui.mc.getTextureManager().bindTexture(TEXT);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, this.x, this.y,
						TEX_PROPTILE_HOFFSET, TEX_PROPTILE_VOFFSET, TEX_PROPTILE_WIDTH, TEX_PROPTILE_HEIGHT,
						this.width, this.height,
						TEX_WIDTH, TEX_HEIGHT,
						colors[0], colors[1], colors[2], colors[3]);
				
				matrixStackIn.push();
				matrixStackIn.translate(this.x + width/2, this.y + (this.height / 2), 0);
				matrixStackIn.scale(.75f, .75f, .75f);
				drawCenteredString(matrixStackIn, gui.font, this.getMessage(), 0, -gui.font.FONT_HEIGHT/2, 0xFFFFFFFF);
				matrixStackIn.pop();
			}
		}

		public static class PropertyValueWidget extends AbstractButton {
			
			private final RuneShaperGuiContainer gui;
			private final int valueIdx;
			
			public PropertyValueWidget(RuneShaperGuiContainer gui, int valueIdx, ITextComponent label, int x, int y, int width, int height) {
				super(x, y, width, height, label);
				this.gui = gui;
				this.valueIdx = valueIdx;
			}

			@Override
			public void onPress() {
				gui.setSelectedValue(this.valueIdx);
			}
			
			protected boolean isSelected() {
				return gui.propertyValueIdx == this.valueIdx;
			}
			
			@Override
			public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
				final int u;
				final int v;
				final int uw;
				final int vh;
				if (!isSelected()) {
					u = TEX_VALUE_VALID_HOFFSET;
					v = TEX_VALUE_VALID_VOFFSET;
					uw = TEX_VALUE_VALID_WIDTH;
					vh = TEX_VALUE_VALID_HEIGHT;
				} else {
					u = TEX_VALUE_HOFFSET;
					v = TEX_VALUE_VOFFSET;
					uw = TEX_VALUE_WIDTH;
					vh = TEX_VALUE_HEIGHT;
				}
				
				int color = 0xFFFFFFFF;
				if (isHovered()) {
					color -= 0x00202020;
				}
				
				final float colors[] = ColorUtil.ARGBToColor(color);
				
				gui.mc.getTextureManager().bindTexture(TEXT);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, this.x, this.y,
						u, v, uw, vh,
						this.width, this.height,
						TEX_WIDTH, TEX_HEIGHT,
						colors[0], colors[1], colors[2], colors[3]);
				
				// Guess at scale based on message bare string representation
				final int guessedLen = this.getMessage().getString().length();
				final float scale;
				if (guessedLen >= 4) {
					scale = .75f / ((float) guessedLen / 3f);
				} else {
					scale = .75f;
				}
				matrixStackIn.push();
				matrixStackIn.translate(this.x + width/2, this.y + (this.height / 2), 0);
				matrixStackIn.scale(scale, scale, 1f);
				drawCenteredString(matrixStackIn, gui.font, this.getMessage(), 0, -gui.font.FONT_HEIGHT/2, 0xFFFFFFFF);
				matrixStackIn.pop();
			}
		}
		
		private static final int POS_PANEL_HOFFSET = 6;
		private static final int POS_PANEL_VOFFSET = 36;
		private static final int POS_PANEL_WIDTH = 60;
		//private static final int POS_PANEL_HEIGHT = 92;
		
		private static final int POS_PROPTILE_WIDTH = POS_PANEL_WIDTH;
		private static final int POS_PROPTILE_HEIGHT = 14;
		
		private static final int POS_SUBMIT_HOFFSET = GUI_WIDTH - (20 + 5);
		private static final int POS_SUBMIT_VOFFSET = 134 - (10 + 5);
		private static final int POS_SUBMIT_WIDTH = 20;
		private static final int POS_SUBMIT_HEIGHT = 10;
		
		private static final int POS_VALUE_HOFFSET = 85;
		private static final int POS_VALUE_VOFFSET = 60;
		private static final int POS_VALUE_WIDTH = 20;
		private static final int POS_VALUE_HEIGHT = 10;
		
		
		private static final ResourceLocation TEXT = NostrumMagica.Loc("textures/gui/container/rune_shaper.png");
		private static final int TEX_WIDTH = 256;
		private static final int TEX_HEIGHT = 256;

		private static final int TEX_SUBMIT_HOFFSET = 0;
		private static final int TEX_SUBMIT_VOFFSET = 221;
		private static final int TEX_SUBMIT_WIDTH = 20;
		private static final int TEX_SUBMIT_HEIGHT = 10;
		private static final int TEX_SUBMIT_VALID_HOFFSET = TEX_SUBMIT_HOFFSET;
		private static final int TEX_SUBMIT_VALID_VOFFSET = TEX_SUBMIT_VOFFSET + TEX_SUBMIT_HEIGHT;
		private static final int TEX_SUBMIT_VALID_WIDTH = TEX_SUBMIT_WIDTH;
		private static final int TEX_SUBMIT_VALID_HEIGHT = TEX_SUBMIT_HEIGHT;

		private static final int TEX_INGSLOT_HOFFSET = 20;
		private static final int TEX_INGSLOT_VOFFSET = 221;
		private static final int TEX_INGSLOT_WIDTH = 18;
		private static final int TEX_INGSLOT_HEIGHT = 18;

		private static final int TEX_PROPTILE_HOFFSET = 38;
		private static final int TEX_PROPTILE_VOFFSET = 221;
		private static final int TEX_PROPTILE_WIDTH = 60;
		private static final int TEX_PROPTILE_HEIGHT = 10;

		private static final int TEX_VALUE_HOFFSET = 0;
		private static final int TEX_VALUE_VOFFSET = 241;
		private static final int TEX_VALUE_WIDTH = 20;
		private static final int TEX_VALUE_HEIGHT = 10;
		private static final int TEX_VALUE_VALID_HOFFSET = TEX_VALUE_HOFFSET + TEX_VALUE_WIDTH;
		private static final int TEX_VALUE_VALID_VOFFSET = TEX_VALUE_VOFFSET;
		private static final int TEX_VALUE_VALID_WIDTH = TEX_VALUE_WIDTH;
		private static final int TEX_VALUE_VALID_HEIGHT = TEX_VALUE_HEIGHT;

		private final RuneShaperContainer container;
		
		protected final List<Rectangle2d> extraAreas;
		protected @Nullable SimpleInventoryWidget extraInventoryWidget;
		protected SubmitButton submitButton;
		
		private final List<SpellShapeProperty<?>> properties;
		private @Nullable SpellShapeProperty<?> selectedProperty = null;
		private int propertyValueIdx = -1;
		private @Nullable ITextComponent description = null;
		private ItemStack lastViewedRune = ItemStack.EMPTY;
		
		public RuneShaperGuiContainer(RuneShaperContainer container, PlayerInventory playerInv, ITextComponent name) {
			super(container, playerInv, name);
			this.container = container;
			this.xSize = GUI_WIDTH;
			this.ySize = GUI_HEIGHT;
			this.extraAreas = new ArrayList<>(1);
			this.properties = new ArrayList<>();
		}
		
		@Override
		public void init() {
			super.init();
			
			if (container.extraInv != null) {
				this.extraInventoryWidget = new SimpleInventoryWidget(this, container.extraInv);
				this.extraInventoryWidget.setColor(0xFF221F23);
				// this.addButton(extraInventoryWidget); done later so it can be repeated
				extraAreas.add(new Rectangle2d(this.getGuiLeft() + container.extraInv.x, this.getGuiTop() + this.container.extraInv.y, this.container.extraInv.width, this.container.extraInv.height));
			}
			
			submitButton = new SubmitButton(this, this.getGuiLeft() + POS_SUBMIT_HOFFSET, this.getGuiTop() + POS_SUBMIT_VOFFSET, POS_SUBMIT_WIDTH, POS_SUBMIT_HEIGHT);
			
			this.refreshWidgets();
		}
		
		protected void addBaseWidgets() {
			if (this.extraInventoryWidget != null) {
				this.addButton(this.extraInventoryWidget);
			}
			this.addButton(submitButton);
			
			this.addButton(new IngredientSlotWidget(this, this.getGuiLeft() + POS_SLOT_INGREDIENT_HOFFSET - 1, this.getGuiTop() + POS_SLOT_INGREDIENT_VOFFSET - 1, 
					18, 18));
		}
		
		protected void refreshWidgets() {
			properties.clear();
			this.children.clear();
			this.buttons.clear();
			
			// Add property widgets for selecting properties
			final SpellShape shape = container.getRuneShape();
			if (shape != null) {
				properties.addAll(shape.getDefaultProperties().getProperties());
			}
			
			for (int i = 0; i < properties.size(); i++) {
				this.addButton(makePropertyWidget(shape, properties.get(i), i));
			}
			
			
			// Add value widgets for configuring the selected property
			if (shape != null && this.selectedProperty != null) {
				final ITextComponent[] values = getValuesForProperty(shape, this.selectedProperty);
				final int width = POS_VALUE_WIDTH;
				final int height = POS_VALUE_HEIGHT;
				final int margin = 20;
				final int maxCol = ((GUI_WIDTH - POS_VALUE_HOFFSET) - (2 * margin)) / width;
				final int xOffset;
				if (values.length < maxCol) {
					xOffset = (((GUI_WIDTH - POS_VALUE_HOFFSET) - (2 * margin)) - (values.length * width)) / 2;
				} else {
					xOffset = (((GUI_WIDTH - POS_VALUE_HOFFSET) - (2 * margin)) - (maxCol * width)) / 2;
				}
				for (int i = 0; i < values.length; i++) {
					final int x = getGuiLeft() + xOffset + POS_VALUE_HOFFSET + (i % maxCol) * (width + width/2);
					final int y = getGuiTop() + POS_VALUE_VOFFSET + (i / maxCol) * (height + height/2);
					this.addButton(new PropertyValueWidget(this, i, values[i], x, y, width, height));
				}
			}
			
			addBaseWidgets();
		}
		
		protected PropertyWidget makePropertyWidget(SpellShape shape, SpellShapeProperty<?> property, int idx) {
			final int x = this.getGuiLeft() + POS_PANEL_HOFFSET;
			final int y = this.getGuiTop() + POS_PANEL_VOFFSET + (idx * POS_PROPTILE_HEIGHT);
			final int w = POS_PROPTILE_WIDTH;
			final int h = POS_PROPTILE_HEIGHT;
			return new PropertyWidget(this, property, getPropertyName(shape, property), x, y, w, h);
		}
		
		protected <T> ITextComponent[] getValuesForProperty(SpellShape shape, SpellShapeProperty<T> property) {
			T[] values = property.getPossibleValues();
			ITextComponent[] ret = new ITextComponent[values.length];
			
			for (int i = 0; i < values.length; i++) {
				ret[i] = property.getDisplayValue(shape, values[i]);
			}
			return ret;
		}
		
		protected <T> ITextComponent getPropertyName(SpellShape shape, SpellShapeProperty<T> property) {
			return property.getDisplayName(shape);
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			
			mc.getTextureManager().bindTexture(TEXT);
			
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin,0, 0, GUI_WIDTH, GUI_HEIGHT, TEX_WIDTH, TEX_HEIGHT);
			
			if (!container.getRune().isEmpty() && this.description != null) {
				final int xOffset = POS_PANEL_HOFFSET + POS_PANEL_WIDTH + 4;
				final float scale = .75f;
				final int areaWidth = GUI_WIDTH - (xOffset + 4);
				matrixStackIn.push();
				matrixStackIn.translate(horizontalMargin + xOffset, verticalMargin + POS_PANEL_VOFFSET, 0);
				matrixStackIn.scale(scale, scale, 1f);
				RenderFuncs.drawSplitString(matrixStackIn, font, this.description.getString(), 0, 0, (int) ((float) areaWidth / scale), 0xFFFFFFFF);
				matrixStackIn.pop();
			}
			
			checkForChanges();
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(MatrixStack matrixStackIn, int mouseX, int mouseY) {
			
		}
		
		protected void checkForChanges() {
			if (!ItemStack.areItemStacksEqual(lastViewedRune, container.getRune())) {
				selectedProperty = null;
				propertyValueIdx = -1;
				description = null;
				lastViewedRune = container.getRune().copy();
				this.refreshWidgets();
			}
		}
		
		private boolean hasChange() {
			if (!container.getRune().isEmpty() && this.selectedProperty != null) {
				final int setValueIdx = getMatchingPropertyValueIdx(container.getRuneProperties(), this.selectedProperty);
				return setValueIdx != this.propertyValueIdx;
			}
			return false;
		}
		
		protected boolean canSubmit() {
			if (!container.getRune().isEmpty() && this.selectedProperty != null) {
				return hasChange() && ingredientValid();
			}
			return false;
		}
		
		protected boolean ingredientValid() {
			if (hasChange()) {
				return isPropertyValid(container.getRuneShape(), container.getRuneProperties(), this.selectedProperty, this.propertyValueIdx, this.container.getIngredient());
			} else {
				return this.container.getIngredient().isEmpty();
			}
		}
		
		protected <T> int getMatchingPropertyValueIdx(SpellShapeProperties properties, SpellShapeProperty<T> property) {
			T current = properties.getValue(property);
			T[] values = property.getPossibleValues();
			for (int i = 0; i < values.length; i++) {
				if (current.equals(values[i])) {
					return i;
				}
			}
			return -1;
		}
		
		protected boolean isPropertyValid(SpellShape shape, SpellShapeProperties base, SpellShapeProperty<?> property, int choice, ItemStack input) {
			NonNullList<ItemStack> costs = shape.supportedFloatCosts(property);
			if (costs == null) {
				return input.isEmpty();
			}
			
			if (choice >= costs.size()) {
				return false; // wrong choice index
			}
			
			final ItemStack required = costs.get(choice);
			if (input.isEmpty()) {
				return required.isEmpty();
			}
			
			// Doesn't work if both are empty stacks
			return required.isItemEqual(input);
		}
		
		protected void setSelectedProperty(SpellShapeProperty<?> property) {
			if (property != this.selectedProperty) {
				this.selectedProperty = property;
				this.propertyValueIdx = getMatchingPropertyValueIdx(container.getRuneProperties(), this.selectedProperty);
				this.description = property.getDisplayDescription(container.getRuneShape());
				
				container.setAcceptingInput(container.getRuneShape().supportedFloatCosts(property) != null);
				refreshWidgets();
			}
		}
		
		protected void setSelectedValue(int index) {
			if (selectedProperty != null && index != this.propertyValueIdx) {
				this.propertyValueIdx = index;
			}
		}

		@Override
		public List<Rectangle2d> getGuiExtraAreas() {
			return extraAreas;
		}
	}
}