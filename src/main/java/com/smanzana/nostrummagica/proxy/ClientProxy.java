package com.smanzana.nostrummagica.proxy;

import org.lwjgl.input.Keyboard;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ClientCastMessage;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class ClientProxy extends CommonProxy {
	
	private KeyBinding bindingCast;

	public ClientProxy() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void preinit() {
		super.preinit();
		
		bindingCast = new KeyBinding("key.cast.desc", Keyboard.KEY_LCONTROL, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingCast);
	}
	
	@Override
	public void init() {
		super.init();
		
		registerModel(SpellTome.instance(), 0, SpellTome.id);
	}
	
	private static void registerModel(Item item, int meta, String modelName) {
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
    	.register(item, meta,
    			new ModelResourceLocation(NostrumMagica.MODID + ":" + modelName, "inventory"));
	}
	
	@SubscribeEvent
	public void onKey(KeyInputEvent event) {
		if (bindingCast.isPressed()) {
			Spell spell = NostrumMagica.getCurrentSpell(Minecraft.getMinecraft().thePlayer);
			if (spell == null)
				return;
			
			// Do mana check here (it's also done on server)
			// to stop redundant checks and get mana looking good
			// on client side immediately
			int mana = NostrumMagica.getMagicWrapper(Minecraft.getMinecraft().thePlayer).getMana();
			int cost = spell.getManaCost();
			
			if (mana < cost)
				return;
			
			NostrumMagica.getMagicWrapper(Minecraft.getMinecraft().thePlayer)
				.addMana(-cost);
			
			NetworkHandler.getSyncChannel().sendToServer(
	    			new ClientCastMessage(spell));
		}
	}
	
	@Override
	public void syncPlayer(EntityPlayerMP player) {
		; // do nothing; we're a client
	}
	
	@Override
	public EntityPlayer getPlayer() {
		return Minecraft.getMinecraft().thePlayer;
	}
	
	private INostrumMagic overrides = null;
	@Override
	public void receiveStatOverrides(INostrumMagic override) {
		// If we can look up stats, apply them.
		// Otherwise, stash them for loading when we apply attributes
		INostrumMagic existing = NostrumMagica.getMagicWrapper(Minecraft.getMinecraft().thePlayer);
		if (existing != null) {
			// Stash them
			existing.copy(override);
		} else {
			// apply them
			overrides = override;
		}
	}
	
	@Override
	public void applyOverride() {
		if (overrides == null)
			return;
		
		INostrumMagic existing = NostrumMagica.getMagicWrapper(Minecraft.getMinecraft().thePlayer);
		existing.copy(overrides);
	}
	
	@Override
	public boolean isServer() {
		return false;
	}
}
