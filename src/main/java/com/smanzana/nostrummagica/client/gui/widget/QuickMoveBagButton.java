package com.smanzana.nostrummagica.client.gui.widget;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.ReagentAndRuneTransfer;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.QuickMoveBagMessage;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;

public class QuickMoveBagButton extends AbstractButton {
	
	protected static final ResourceLocation TEX = NostrumMagica.Loc("textures/gui/container/bag_button.png");
	protected static final int TEX_WIDTH = 32;
	protected static final int TEX_HEIGHT = 32;
	
	protected final Player player;
	protected final AbstractContainerScreen<?> screen;

	public QuickMoveBagButton(int x, int y, int width, int height, Player player, AbstractContainerScreen<?> screen) {
		super(x, y, width, height, TextComponent.EMPTY);
		this.player = player;
		this.screen = screen;
	}
	
	@Override
	public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		final float color[] = ColorUtil.ARGBToColor(shouldBeClickable() ? 0xFFFFFFFF : 0xFF808080);
		if (this.isHovered()) {
			color[0] *= .8f;
			color[1] *= .8f;
			color[2] *= .8f;
		}
		Minecraft.getInstance().getTextureManager().bind(TEX);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, x, y, 0, 0, TEX_WIDTH, TEX_HEIGHT, width, height, TEX_WIDTH, TEX_HEIGHT, color[0], color[1], color[2], color[3]);
	}

	@Override
	public void onPress() {
		if (shouldBeClickable()) {
			NetworkHandler.sendToServer(new QuickMoveBagMessage(this.screen.getMenu()));
		}
	}
	
	/**
	 * Attempt to move to a good spot in the container
	 */
	public void handleLayout(AbstractContainerScreen<?> screen) {
		// Guess based on top left player inventory slot seen?
		int minX = Integer.MAX_VALUE;
		int maxX = -1;
		int minY = Integer.MAX_VALUE;
		int maxY = -1;
		
		for (Slot slot : screen.getMenu().slots) {
			if (slot.container == this.player.inventory) {
				final int slotIdx = slot.getSlotIndex();
				if (Inventory.isHotbarSlot(slotIdx) || slotIdx >= 36) {
					continue; // hotbar or armor slot
				}
				
				minX = Math.min(minX, slot.x);
				maxX = Math.max(maxX, slot.x);
				minY = Math.min(minY, slot.y);
				maxY = Math.max(maxY, slot.y);
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
	
	protected static final boolean ShouldBeClickable(Player player, AbstractContainerScreen<?> screen) {
		boolean foundReagent = false;
		boolean foundRune = false;
		for (Slot slot : screen.getMenu().slots) {
			if (slot.container != player.inventory
					&& slot.hasItem() && !slot.getItem().isEmpty()) {
				if (!foundReagent && slot.getItem().getItem() instanceof ReagentItem) {
					foundReagent = true;
				}
				if (!foundRune && slot.getItem().getItem() instanceof SpellRune) {
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
		if (event.getGui() instanceof AbstractContainerScreen) {
			final Minecraft mc = Minecraft.getInstance();
			Player player = mc.player;
			final AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) event.getGui();
			if (ReagentAndRuneTransfer.ShouldAddTo(player, screen.getMenu())) {
				// May have already added button.
				QuickMoveBagButton button = null;
				for (AbstractWidget w : event.getWidgetList()) {
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
