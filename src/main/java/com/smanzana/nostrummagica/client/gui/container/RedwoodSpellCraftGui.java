package com.smanzana.nostrummagica.client.gui.container;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.BasicSpellCraftGui.BasicSpellCraftContainer;
import com.smanzana.nostrummagica.client.gui.container.BasicSpellCraftGui.BasicSpellCraftGuiContainer;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui.SpellCreationContainer;
import com.smanzana.nostrummagica.crafting.ISpellCraftingInventory;
import com.smanzana.nostrummagica.tiles.BasicSpellTableEntity;
import com.smanzana.nostrummagica.utils.ContainerUtil;
import com.smanzana.nostrummagica.utils.ContainerUtil.IPackedContainerProvider;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public class RedwoodSpellCraftGui {
	
	public static class RedwoodContainer extends BasicSpellCraftContainer {
		
		public static final String ID = "redwoodspellcrafter";
		
		public RedwoodContainer(int windowId,
				PlayerEntity crafter, PlayerInventory playerInv, ISpellCraftingInventory tableInventory,
				BlockPos tablePos) {
			this(NostrumContainers.SpellCreationRedwood, windowId, crafter, playerInv, tableInventory, tablePos);
		}

		protected RedwoodContainer(ContainerType<? extends SpellCreationContainer> type, int windowId,
				PlayerEntity crafter, PlayerInventory playerInv, ISpellCraftingInventory tableInventory,
				BlockPos tablePos) {
			super(type, windowId, crafter, playerInv, tableInventory, tablePos);
		}
		
		public static RedwoodContainer FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buffer) {
			final BasicSpellTableEntity te = ContainerUtil.GetPackedTE(buffer);
			final ISpellCraftingInventory tableInv = te.getSpellCraftingInventory();
			return new RedwoodContainer(windowId, playerInv.player, playerInv, tableInv, te.getPos());
		}
		
		public static IPackedContainerProvider Make(BasicSpellTableEntity table) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new RedwoodContainer(windowId, player, playerInv, table.getSpellCraftingInventory(), table.getPos());
			}, (buffer) -> {
				ContainerUtil.PackTE(buffer, table);
			});
		}
	}
	
	public static class Gui extends BasicSpellCraftGuiContainer {
		
		private static final ResourceLocation TEXT = NostrumMagica.Loc("textures/gui/container/spell_create_red.png");
		
		public Gui(BasicSpellCraftContainer container, PlayerInventory playerInv, ITextComponent name) {
			super(container, playerInv, name);
		}
		
		protected ResourceLocation getBackgroundTexture() {
			return TEXT; 
		}
	}
	
}
