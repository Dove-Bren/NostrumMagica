package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.pet.PetInfo;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public interface IEntityPet extends IEntityTameable {

	public PetInfo getPetSummary();
	
	default public void onAttackCommand(EntityLivingBase target) { if (this instanceof EntityLiving) ((EntityLiving) this).setAttackTarget(target); };
	
	default public void onStopCommand() { if (this instanceof EntityLiving) ((EntityLiving) this).setAttackTarget(null); };
	
	public PetGUI.PetContainer<? extends IEntityPet> getGUIContainer(EntityPlayer player);
	
	public PetGUI.PetGUIStatAdapter<? extends IEntityPet> getGUIAdapter();
	
	public static final class SoulBoundLore implements ILoreTagged {
		
		private static SoulBoundLore instance = null;
		public static SoulBoundLore instance() {
			if (instance == null) {
				instance = new SoulBoundLore();
			}
			return instance;
		}

		@Override
		public String getLoreKey() {
			return "lore_generic_soulbound";
		}

		@Override
		public String getLoreDisplayName() {
			return "Soulbound Pets";
		}

		@Override
		public Lore getBasicLore() {
			return new Lore().add("You've soulbonded with one of your pets!", "This means that your pet trusted you enough to grant you access to its soul. For a powerful mage like you, this means you can bring the pet back from the dead should it die!", "Soulbonding usually produces a soul item linked to the pet. Keep this item safe, as it's the only way to get the pet back if it dies!");
		}

		@Override
		public Lore getDeepLore() {
			return new Lore().add("You've soulbonded with one of your pets!", "This means that your pet trusted you enough to grant you access to its soul. For a powerful mage like you, this means you can bring the pet back from the dead should it die!", "Soulbonding usually produces a soul item linked to the pet. Keep this item safe, as it's the only way to get the pet back if it dies!");
		}

		@Override
		public InfoScreenTabs getTab() {
			// Don't actually display! We're going to show our own page!
			return InfoScreenTabs.INFO_GUIDES;
		}
		
	}
	
}
