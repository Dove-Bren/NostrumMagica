package com.smanzana.nostrummagica.client.gui;

import com.smanzana.nostrummagica.client.gui.container.ReagentBagGui;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui;
import com.smanzana.nostrummagica.items.ReagentBag;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class NostrumGui implements IGuiHandler {

	public static final int reagentBagID = 0;
	public static final int spellTableID = 1;
	
	private IInventory test;
	private IInventory testInv() {
		if (test == null)
			test = new InventoryBasic("test inv", false, 10);
		
		return test;
	}
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//		TileEntity activatedEntity = world.getTileEntity(new BlockPos(x,y,z));
//		if (activatedEntity != null) {
//			// Check for the GUI type
//			if (ID == Armory.Gui_Type.BRAZIER.ordinal()) {
//				return new Brazier.BrazierContainer(player.inventory, (Brazier.BrazierTileEntity) world.getTileEntity(new BlockPos(x,y,z)));
//			} else if (ID == Armory.Gui_Type.FORGE.ordinal()) {
//				return new Forge.ForgeContainer(player.inventory, (Forge.ForgeTileEntity) world.getTileEntity(new BlockPos(x,y,z)));
//			}
//		} else 
		
		if (ID == spellTableID) {
			// TODO get spell table pointed at, etc etc
			return new SpellCreationGui.SpellCreationContainer(
					player.inventory,
					testInv()); // should be tile inventory
		}
		
		
		// Item based
		int pos = player.inventory.currentItem + 27;
		ItemStack inHand = player.getHeldItemMainhand();
		if (inHand == null) {
			inHand = player.getHeldItemOffhand();
			pos = 40;
		}
		
		if (inHand != null) {
			if (ID == reagentBagID && inHand.getItem() instanceof ReagentBag) {
				
				return new ReagentBagGui.BagContainer(
						player.inventory,
						ReagentBag.instance(),
						inHand,
						pos
						);
			}
			// Else other item stuff
		}
		return null;
	}

	
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		
		if (ID == spellTableID) {
			// TODO get spell table pointed at, etc etc
			return new SpellCreationGui.SpellGui(new SpellCreationGui.SpellCreationContainer(
					player.inventory,
					testInv())); // should be tile inventory
		}
		
		// Item based
		int pos = player.inventory.currentItem + 27;
		ItemStack inHand = player.getHeldItemMainhand();
		if (inHand == null) {
			inHand = player.getHeldItemOffhand();
			pos = 40;
		}
		
		if (inHand != null) {
			if (ID == reagentBagID && inHand.getItem() instanceof ReagentBag) {
				
				return new ReagentBagGui.BagGui(new ReagentBagGui.BagContainer(
						player.inventory,
						ReagentBag.instance(),
						inHand,
						pos
						));
			}
			// Else other item stuff
		}
		
		return null;
	}
	
}
