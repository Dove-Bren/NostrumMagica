package com.smanzana.nostrummagica.client.gui;

import com.smanzana.nostrummagica.blocks.NostrumObeliskEntity;
import com.smanzana.nostrummagica.blocks.SpellTable.SpellTableEntity;
import com.smanzana.nostrummagica.client.gui.container.ReagentBagGui;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui;
import com.smanzana.nostrummagica.items.ReagentBag;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class NostrumGui implements IGuiHandler {

	public static final int reagentBagID = 0;
	public static final int spellTableID = 1;
	public static final int mirrorID = 2;
	public static final int obeliskID = 3;
	
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
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof SpellTableEntity) {
				return new SpellCreationGui.SpellCreationContainer(
						player.inventory,
						(SpellTableEntity) ent,
						new BlockPos(x, y, z)); // should be tile inventory
			}
		}
		
		if (ID == mirrorID) {
			// Not checked on server side; just return
			return null;
		}
		
		if (ID == obeliskID) {
			return null;
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
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof SpellTableEntity) {
				return new SpellCreationGui.SpellGui(new SpellCreationGui.SpellCreationContainer(
						player.inventory,
						(SpellTableEntity) ent,
						new BlockPos(x, y, z))); // should be tile inventory
			}
		}
		
		if (ID == mirrorID) {
			return new MirrorGui(player);
		}
		
		if (ID == obeliskID) {
			TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
			if (te != null && te instanceof NostrumObeliskEntity) {
				return new ObeliskScreen((NostrumObeliskEntity) te);
			}
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
