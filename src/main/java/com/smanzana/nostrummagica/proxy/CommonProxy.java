package com.smanzana.nostrummagica.proxy;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.CapabilityHandler;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagicStorage;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.entity.EntityGolemEarth;
import com.smanzana.nostrummagica.entity.EntityGolemEnder;
import com.smanzana.nostrummagica.entity.EntityGolemFire;
import com.smanzana.nostrummagica.entity.EntityGolemIce;
import com.smanzana.nostrummagica.entity.EntityGolemLightning;
import com.smanzana.nostrummagica.entity.EntityGolemPhysical;
import com.smanzana.nostrummagica.entity.EntityGolemWind;
import com.smanzana.nostrummagica.entity.EntitySpellProjectile;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.SpellRequestReplyMessage;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;
import com.smanzana.nostrummagica.potions.FrostbitePotion;
import com.smanzana.nostrummagica.potions.MagicBoostPotion;
import com.smanzana.nostrummagica.potions.MagicResistPotion;
import com.smanzana.nostrummagica.potions.MagicShieldPotion;
import com.smanzana.nostrummagica.potions.PhysicalShieldPotion;
import com.smanzana.nostrummagica.potions.RootedPotion;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.TouchTrigger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {
	
	public CapabilityHandler capabilityHandler;
	
	public void preinit() {
		CapabilityManager.INSTANCE.register(INostrumMagic.class, new NostrumMagicStorage(), NostrumMagic.class);
		capabilityHandler = new CapabilityHandler();
		NetworkHandler.getInstance();
		NostrumMagicaSounds.registerSounds();
		
    	registerShapes();
    	registerTriggers();
    	
    	int entityID = 0;
    	EntityRegistry.registerModEntity(EntitySpellProjectile.class, "spell_projectile",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			true
    			);
    	EntityRegistry.registerModEntity(EntityGolemPhysical.class, "physical_golem",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityGolemLightning.class, "lightning_golem",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityGolemFire.class, "fire_golem",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityGolemEarth.class, "earth_golem",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityGolemIce.class, "ice_golem",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityGolemWind.class, "wind_golem",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityGolemEnder.class, "ender_golem",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			false
    			);
	}
	
	public void init() {
    	registerPotions();
    	registerItems();
	}
	
	public void postinit() {
		
	}
    
    private void registerShapes() {
    	SpellShape.register(SingleShape.instance());
    	SpellShape.register(AoEShape.instance());
    }
    
    private void registerTriggers() {
    	SpellTrigger.register(SelfTrigger.instance());
    	SpellTrigger.register(TouchTrigger.instance());
    	SpellTrigger.register(AITargetTrigger.instance());
    	SpellTrigger.register(ProjectileTrigger.instance());
    }
    
    private void registerPotions() {
    	RootedPotion.instance();
    	MagicResistPotion.instance();
    	PhysicalShieldPotion.instance();
    	MagicShieldPotion.instance();
    	FrostbitePotion.instance();
    	MagicBoostPotion.instance();
    }
    
    private void registerItems() {
    	SpellTome.instance().setRegistryName(NostrumMagica.MODID, SpellTome.id);
    	GameRegistry.register(SpellTome.instance());
    }
    
    public void syncPlayer(EntityPlayerMP player) {
    	System.out.println("Sending sync to client");
    	NetworkHandler.getSyncChannel().sendTo(
    			new StatSyncMessage(NostrumMagica.getMagicWrapper(player)),
    			player);
    	NetworkHandler.getSyncChannel().sendTo(
    			new SpellRequestReplyMessage(NostrumMagica.spellRegistry.getAllSpells(), true),
    			player);
    }

	public EntityPlayer getPlayer() {
		return null; // Doesn't mean anything on the server
	}
	
	public void receiveStatOverrides(INostrumMagic override) {
		return; // Server side doesn't do anything
	}
	
	public void applyOverride() {
		; // do nothing
	}

	public boolean isServer() {
		return true;
	}
	
	public void openBook(EntityPlayer player, GuiBook book, Object userdata) {
		; // Server does nothing
	}
}
