package com.smanzana.nostrummagica.client.gui;

import java.util.List;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.container.ActiveHopperGui;
import com.smanzana.nostrummagica.client.gui.container.LoreTableGui;
import com.smanzana.nostrummagica.client.gui.container.ModificationTableGui;
import com.smanzana.nostrummagica.client.gui.container.PutterBlockGui;
import com.smanzana.nostrummagica.client.gui.container.ReagentBagGui;
import com.smanzana.nostrummagica.client.gui.container.RuneBagGui;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui;
import com.smanzana.nostrummagica.client.gui.container.WispBlockGui;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreen;
import com.smanzana.nostrummagica.entity.dragon.ITameDragon;
import com.smanzana.nostrummagica.integration.aetheria.blocks.WispBlock.WispBlockTileEntity;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.RuneBag;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.tiles.ActiveHopperTileEntity;
import com.smanzana.nostrummagica.tiles.LoreTableEntity;
import com.smanzana.nostrummagica.tiles.ModificationTableEntity;
import com.smanzana.nostrummagica.tiles.NostrumObeliskEntity;
import com.smanzana.nostrummagica.tiles.PutterBlockTileEntity;
import com.smanzana.nostrummagica.tiles.SpellTableEntity;

import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.PlayerEntity;
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
	public static final int infoscreenID = 4;
	public static final int modtableID = 5;
	public static final int loretableID = 6;
	public static final int runeBagID = 7;
	public static final int scrollID = 8;
	public static final int dragonID = 9;
	public static final int wispblockID = 10;
	public static final int putterBlockID = 11;
	public static final int activeHopperID = 11;
	
	@Override
	public Object getServerGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z) {
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
						player,
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
		
		if (ID == infoscreenID) {
			return null;
		}
		
		if (ID == scrollID) {
			return null;
		}
		
		if (ID == modtableID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof ModificationTableEntity) {
				return new ModificationTableGui.ModificationTableContainer(
						player,
						player.inventory,
						(ModificationTableEntity) ent,
						new BlockPos(x, y, z)); // should be tile inventory
			}
		}
		
		if (ID == loretableID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof LoreTableEntity) {
				return new LoreTableGui.LoreTableContainer(
						player,
						player.inventory,
						(LoreTableEntity) ent,
						new BlockPos(x, y, z)); // should be tile inventory
			}
		}
		
		if (ID == wispblockID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof WispBlockTileEntity) {
				return new WispBlockGui.WispBlockContainer(
						player,
						player.inventory,
						(WispBlockTileEntity) ent,
						new BlockPos(x, y, z)); // should be tile inventory
			}
		}
		
		if (ID == putterBlockID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof PutterBlockTileEntity) {
				return new PutterBlockGui.PutterBlockContainer(
						player.inventory,
						(PutterBlockTileEntity) ent); // should be tile inventory
			}
		}
		
		if (ID == activeHopperID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof ActiveHopperTileEntity) {
				return new ActiveHopperGui.ActiveHopperContainer(
						player.inventory,
						(ActiveHopperTileEntity) ent); // should be tile inventory
			}
		}
		
		if (ID == dragonID) {
			// Dumb that this interface doens't let us pass the dragon in directly!!!
			
			List<EntityDragon> list = world.getEntities(EntityDragon.class, new Predicate<EntityDragon>() {
				@Override
				public boolean apply(EntityDragon input) {
					if (input != null && input instanceof ITameDragon) {
						if (input.getDistanceSq(x, y, z) < 1) {
							return true;
						}
					}
					return false;
				}
			});
			
			if (list != null && !list.isEmpty()) {
				ITameDragon dragon = (ITameDragon) list.get(0);
				return dragon.getGUIContainer(player);
			}
			
			return null;
		}
		
		// Item based
		int pos = player.inventory.currentItem + 27;
		ItemStack inHand = player.getHeldItemMainhand();
		if (inHand.isEmpty()) {
			inHand = player.getHeldItemOffhand();
			pos = 40;
		}
		
		if (!inHand.isEmpty()) {
			if (ID == reagentBagID && inHand.getItem() instanceof ReagentBag) {
				return new ReagentBagGui.BagContainer(
						player.inventory,
						ReagentBag.instance(),
						inHand,
						pos
						);
			}
			if (ID == runeBagID && inHand.getItem() instanceof RuneBag) {
				return new RuneBagGui.BagContainer(
						player.inventory,
						RuneBag.instance(),
						inHand,
						pos
						);
			}
			// Else other item stuff
		}
		return null;
	}

	
	@Override
	public Object getClientGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z) {
		
		if (ID == spellTableID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof SpellTableEntity) {
				return new SpellCreationGui.SpellGui(new SpellCreationGui.SpellCreationContainer(
						player,
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
		
		if (ID == infoscreenID) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return null;
			return new InfoScreen(attr, (String) null);
		}
		
		if (ID == modtableID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof ModificationTableEntity) {
				return new ModificationTableGui.ModificationGui(new ModificationTableGui.ModificationTableContainer(
						player,
						player.inventory,
						(ModificationTableEntity) ent,
						new BlockPos(x, y, z))); // should be tile inventory
			}
		}
		
		if (ID == loretableID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof LoreTableEntity) {
				return new LoreTableGui.LoreTableGuiContainer(new LoreTableGui.LoreTableContainer(
						player,
						player.inventory,
						(LoreTableEntity) ent,
						new BlockPos(x, y, z))); // should be tile inventory
			}
		}
		
		if (ID == wispblockID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof WispBlockTileEntity) {
				return new WispBlockGui.WispBlockGuiContainer(new WispBlockGui.WispBlockContainer(
						player,
						player.inventory,
						(WispBlockTileEntity) ent,
						new BlockPos(x, y, z))); // should be tile inventory
			}
		}
		
		if (ID == putterBlockID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof PutterBlockTileEntity) {
				return new PutterBlockGui.PutterBlockGuiContainer(new PutterBlockGui.PutterBlockContainer(
						player.inventory,
						(PutterBlockTileEntity) ent)); // should be tile inventory
			}
		}
		
		if (ID == activeHopperID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof ActiveHopperTileEntity) {
				return new ActiveHopperGui.ActiveHopperGuiContainer(new ActiveHopperGui.ActiveHopperContainer(
						player.inventory,
						(ActiveHopperTileEntity) ent)); // should be tile inventory
			}
		}
		
		if (ID == dragonID) {
			// Clients don't open something with the FML message. Instead we use our own custom message.
			return null;
		}
		
		if (ID == scrollID) {
			Spell spell = NostrumMagica.instance.getSpellRegistry().lookup(x);
			return new ScrollScreen(spell);
		}
		
		// Item based
		int pos = player.inventory.currentItem + 27;
		ItemStack inHand = player.getHeldItemMainhand();
		if (inHand.isEmpty()) {
			inHand = player.getHeldItemOffhand();
			pos = 40;
		}
		
		if (!inHand.isEmpty()) {
			if (ID == reagentBagID && inHand.getItem() instanceof ReagentBag) {
				
				return new ReagentBagGui.BagGui(new ReagentBagGui.BagContainer(
						player.inventory,
						ReagentBag.instance(),
						inHand,
						pos
						));
			}
			if (ID == runeBagID && inHand.getItem() instanceof RuneBag) {
				return new RuneBagGui.BagGui(new RuneBagGui.BagContainer(
						player.inventory,
						RuneBag.instance(),
						inHand,
						pos));
			}
			// Else other item stuff
		}
		
		return null;
	}
	
}
