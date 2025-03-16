package com.smanzana.nostrummagica.client.gui.container;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.BasicSpellCraftGui.BasicSpellCraftContainer;
import com.smanzana.nostrummagica.client.gui.container.BasicSpellCraftGui.BasicSpellCraftGuiContainer;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui.SpellCreationContainer;
import com.smanzana.nostrummagica.crafting.ISpellCraftingInventory;
import com.smanzana.nostrummagica.tile.BasicSpellTableTileEntity;
import com.smanzana.nostrummagica.util.ContainerUtil;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class RedwoodSpellCraftGui {
	
	public static class RedwoodContainer extends BasicSpellCraftContainer {
		
		public static final String ID = "redwoodspellcrafter";
		
		public RedwoodContainer(int windowId,
				Player crafter, Inventory playerInv, ISpellCraftingInventory tableInventory,
				BlockPos tablePos, @Nullable Container extraInventory) {
			this(NostrumContainers.SpellCreationRedwood, windowId, crafter, playerInv, tableInventory, tablePos, extraInventory);
		}

		protected RedwoodContainer(MenuType<? extends SpellCreationContainer> type, int windowId,
				Player crafter, Inventory playerInv, ISpellCraftingInventory tableInventory,
				BlockPos tablePos, @Nullable Container extraInventory) {
			super(type, windowId, crafter, playerInv, tableInventory, tablePos, extraInventory);
		}
		
		public static RedwoodContainer FromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf buffer) {
			final BasicSpellTableTileEntity te = ContainerUtil.GetPackedTE(buffer);
			final ISpellCraftingInventory tableInv = te.getSpellCraftingInventory();
			@Nullable Container extraInventory = te.getExtraInventory();
			return new RedwoodContainer(windowId, playerInv.player, playerInv, tableInv, te.getBlockPos(), extraInventory);
		}
		
		public static IPackedContainerProvider Make(BasicSpellTableTileEntity table) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new RedwoodContainer(windowId, player, playerInv, table.getSpellCraftingInventory(), table.getBlockPos(), table.getExtraInventory());
			}, (buffer) -> {
				ContainerUtil.PackTE(buffer, table);
			});
		}
	}
	
	public static class Gui extends BasicSpellCraftGuiContainer {
		
		private static final ResourceLocation TEXT = NostrumMagica.Loc("textures/gui/container/spell_create_red.png");
		
		public Gui(BasicSpellCraftContainer container, Inventory playerInv, Component name) {
			super(container, playerInv, name);
		}
		
		@Override
		protected ResourceLocation getBackgroundTexture() {
			return TEXT; 
		}
		
		@Override
		public void init() {
			super.init();
			
			if (this.extraInventoryWidget != null) {
				this.extraInventoryWidget.setColor(0xFF501516);
			}
		}
	}
	
}
