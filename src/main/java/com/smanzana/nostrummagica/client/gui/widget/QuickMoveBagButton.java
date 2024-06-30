package com.smanzana.nostrummagica.client.gui.widget;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.ReagentAndRuneTransfer;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.QuickMoveBagMessage;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;

public class QuickMoveBagButton extends AbstractButton {
	
	protected static final ResourceLocation TEX = NostrumMagica.Loc("textures/gui/container/bag_button.png");
	protected static final int TEX_WIDTH = 32;
	protected static final int TEX_HEIGHT = 32;
	
	protected final PlayerEntity player;
	protected final ContainerScreen<?> screen;

	public QuickMoveBagButton(int x, int y, int width, int height, PlayerEntity player, ContainerScreen<?> screen) {
		super(x, y, width, height, StringTextComponent.EMPTY);
		this.player = player;
		this.screen = screen;
	}
	
	@Override
	public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		final float color[] = ColorUtil.ARGBToColor(shouldBeClickable() ? 0xFFFFFFFF : 0xFF808080);
		if (this.isHovered()) {
			color[0] *= .8f;
			color[1] *= .8f;
			color[2] *= .8f;
		}
		Minecraft.getInstance().getTextureManager().bindTexture(TEX);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, x, y, 0, 0, TEX_WIDTH, TEX_HEIGHT, width, height, TEX_WIDTH, TEX_HEIGHT, color[0], color[1], color[2], color[3]);
	}

	@Override
	public void onPress() {
		if (shouldBeClickable()) {
			NetworkHandler.sendToServer(new QuickMoveBagMessage(this.screen.getContainer()));
		}
	}
	
	/**
	 * Attempt to move to a good spot in the container
	 */
	public void handleLayout(ContainerScreen<?> screen) {
		// Guess based on top left player inventory slot seen?
		int minX = Integer.MAX_VALUE;
		int maxX = -1;
		int minY = Integer.MAX_VALUE;
		int maxY = -1;
		
		for (Slot slot : screen.getContainer().inventorySlots) {
			if (slot.inventory == this.player.inventory) {
				final int slotIdx = slot.getSlotIndex();
				if (PlayerInventory.isHotbar(slotIdx) || slotIdx >= 36) {
					continue; // hotbar or armor slot
				}
				
				minX = Math.min(minX, slot.xPos);
				maxX = Math.max(maxX, slot.xPos);
				minY = Math.min(minY, slot.yPos);
				maxY = Math.max(maxY, slot.yPos);
			}
		}
		
		if (maxX != -1) {
			// Prefer top right, then top center
			if (maxX + (18) + (width + 2) < screen.getXSize()) {
				this.x = screen.getGuiLeft() + maxX + (18) + (2);
				this.y = screen.getGuiTop() + minY;
			} else {
				this.x = screen.getGuiLeft() + ((minX+maxX)/2) + ((18 - width)/2);
				this.y = screen.getGuiTop() + minY - (2 + height);
			}
			
		}
	}
	
	protected boolean shouldBeClickable() {
		return ShouldBeClickable(player, screen);
	}
	
	protected static final boolean ShouldBeClickable(PlayerEntity player, ContainerScreen<?> screen) {
		boolean foundReagent = false;
		boolean foundRune = false;
		for (Slot slot : screen.getContainer().inventorySlots) {
			if (slot.inventory != player.inventory
					&& slot.getHasStack() && !slot.getStack().isEmpty()) {
				if (!foundReagent && slot.getStack().getItem() instanceof ReagentItem) {
					foundReagent = true;
				}
				if (!foundRune && slot.getStack().getItem() instanceof SpellRune) {
					foundRune = true;
				}
			}
		}
		
		if (!foundReagent && !foundRune) {
			return false;
		}
		
		// Return if player has appropriate bag
		if (foundReagent) {
			List<ItemStack> reagentBags = ReagentAndRuneTransfer.FindReagentBags(player);
			if (!reagentBags.isEmpty()) {
				return true;
			}
		}
		
		if (foundRune) {
			List<ItemStack> runeBags = ReagentAndRuneTransfer.FindRuneBags(player);
			if (!runeBags.isEmpty()) {
				return true;
			}
		}
		
		return false;
	}
	
	public static final void OnContainerScreenShow(InitGuiEvent.Post event) {
		if (event.getGui() instanceof ContainerScreen) {
			final Minecraft mc = Minecraft.getInstance();
			PlayerEntity player = mc.player;
			final ContainerScreen<?> screen = (ContainerScreen<?>) event.getGui();
			if (ReagentAndRuneTransfer.ShouldAddTo(player, screen.getContainer())) {
				// May have already added button.
				QuickMoveBagButton button = null;
				for (Widget w : event.getWidgetList()) {
					if (w instanceof QuickMoveBagButton) {
						button = (QuickMoveBagButton) w;
						break;
					}
				}
				
				if (button == null) {
					button = new QuickMoveBagButton(0, 0, 10, 10, player, screen);
					event.addWidget(button);
				}
				
				button.handleLayout(screen);
			}
			
		}
	}

}
