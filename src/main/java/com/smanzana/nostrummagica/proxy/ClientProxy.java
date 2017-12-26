package com.smanzana.nostrummagica.proxy;

import org.lwjgl.input.Keyboard;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.client.overlay.OverlayRenderer;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ClientCastMessage;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class ClientProxy extends CommonProxy {
	
	private KeyBinding bindingCast;
	private OverlayRenderer overlayRenderer;

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
	
	@Override
	public void postinit() {
		this.overlayRenderer = new OverlayRenderer();
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
			
			Minecraft.getMinecraft().thePlayer.sendChatMessage("Mana: " + mana);
			
			if (mana < cost) {
				EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
				player.sendChatMessage("Not enough mana");
				for (int i = 0; i < 15; i++) {
					double offsetx = Math.cos(i * (2 * Math.PI / 15)) * 1.0;
					double offsetz = Math.sin(i * (2 * Math.PI / 15)) * 1.0;
					player.worldObj
						.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
								player.posX + offsetx, player.posY, player.posZ + offsetz,
								0, -.5, 0);
				}
				overlayRenderer.startManaWiggle(2);
				return;
			}
			
			NostrumMagica.getMagicWrapper(Minecraft.getMinecraft().thePlayer)
				.addMana(-cost);
			
			NetworkHandler.getSyncChannel().sendToServer(
	    			new ClientCastMessage(spell));
		}
	}
	
	@Override
	public void syncPlayer(EntityPlayerMP player) {
		if (player.worldObj.isRemote)
			return;
		
		super.syncPlayer(player);
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
		
		if (existing == null)
			return; // Mana got here before we attached
		
		existing.copy(overrides);
		
		overrides = null;
	}
	
	@Override
	public boolean isServer() {
		return false;
	}
	
	@Override
	public void openBook(EntityPlayer player, GuiBook book, Object userdata) {
		Minecraft.getMinecraft().displayGuiScreen(book.getScreen(userdata));
	}
}
