package com.smanzana.nostrummagica.entity;

import java.util.UUID;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.gui.petgui.IPetGUISheet;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.pet.PetInfo;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public interface IEntityPet extends ITameableEntity {

	public PetInfo getPetSummary();
	
	default public void onAttackCommand(LivingEntity target) { if (this instanceof MobEntity) ((MobEntity) this).setAttackTarget(target); };
	
	default public void onStopCommand() { if (this instanceof MobEntity) ((MobEntity) this).setAttackTarget(null); };
	
	public UUID getPetID();
	
	public IPetGUISheet<? extends IEntityPet>[] getContainerSheets(PlayerEntity player);
	
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
