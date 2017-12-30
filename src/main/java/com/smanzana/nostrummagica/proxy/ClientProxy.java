package com.smanzana.nostrummagica.proxy;

import org.lwjgl.input.Keyboard;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.CursedIce;
import com.smanzana.nostrummagica.blocks.MagicWall;
import com.smanzana.nostrummagica.blocks.NostrumMagicaFlower;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.client.overlay.OverlayRenderer;
import com.smanzana.nostrummagica.entity.EntityGolem;
import com.smanzana.nostrummagica.entity.renderer.ModelGolem;
import com.smanzana.nostrummagica.entity.renderer.RenderGolem;
import com.smanzana.nostrummagica.items.EnchantedArmor;
import com.smanzana.nostrummagica.items.EnchantedWeapon;
import com.smanzana.nostrummagica.items.MagicArmorBase;
import com.smanzana.nostrummagica.items.MagicSwordBase;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ClientCastMessage;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class ClientProxy extends CommonProxy {
	
	private KeyBinding bindingCast;
	private KeyBinding bindingScroll;
	private OverlayRenderer overlayRenderer;

	public ClientProxy() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void preinit() {
		super.preinit();
		
		bindingCast = new KeyBinding("key.cast.desc", Keyboard.KEY_LCONTROL, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingCast);
		bindingScroll = new KeyBinding("key.spellscroll.desc", Keyboard.KEY_LSHIFT, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingScroll);
		
		RenderingRegistry.registerEntityRenderingHandler(EntityGolem.class, new IRenderFactory<EntityGolem>() {
			@Override
			public Render<? super EntityGolem> createRenderFor(RenderManager manager) {
				return new RenderGolem(manager, new ModelGolem(), .8f);
			}
		});
		
		ResourceLocation variants[] = new ResourceLocation[ReagentType.values().length];
		int i = 0;
		for (ReagentType type : ReagentType.values()) {
			variants[i++] = new ResourceLocation(NostrumMagica.MODID,
					ReagentItem.instance().getNameFromMeta(type.getMeta()));
		}
		ModelBakery.registerItemVariants(ReagentItem.instance(), variants);
		
	}
	
	@Override
	public void init() {
		super.init();
		
		registerModel(SpellTome.instance(), 0, SpellTome.id);
		for (EnchantedWeapon weapon : EnchantedWeapon.getAll()) {
			registerModel(weapon, 0, weapon.getModelID());
		}
		for (EnchantedArmor armor : EnchantedArmor.getAll()) {
			registerModel(armor, 0, armor.getModelID());
		}
		
		registerModel(MagicSwordBase.instance(), 0, MagicSwordBase.instance().getModelID());
		registerModel(MagicArmorBase.helm, 0, MagicArmorBase.helm.getModelID());
		registerModel(MagicArmorBase.chest, 0, MagicArmorBase.chest.getModelID());
		registerModel(MagicArmorBase.legs, 0, MagicArmorBase.legs.getModelID());
		registerModel(MagicArmorBase.feet, 0, MagicArmorBase.feet.getModelID());
		
		for (ReagentItem.ReagentType type : ReagentItem.ReagentType.values()) {
			registerModel(ReagentItem.instance(), type.getMeta(),
					ReagentItem.instance().getNameFromMeta(type.getMeta()));
		}
		
		registerModel(new ItemBlock(NostrumMagicaFlower.instance()), 
				NostrumMagicaFlower.Type.CRYSTABLOOM.getMeta(),
				NostrumMagicaFlower.Type.CRYSTABLOOM.getName()
				);
		registerModel(new ItemBlock(NostrumMagicaFlower.instance()), 
				NostrumMagicaFlower.Type.MIDNIGHT_IRIS.getMeta(),
				NostrumMagicaFlower.Type.MIDNIGHT_IRIS.getName()
				);
		
		registerModel(Item.getItemFromBlock(MagicWall.instance()),
				0,
				MagicWall.ID);
		registerModel(Item.getItemFromBlock(CursedIce.instance()),
				0,
				CursedIce.ID);
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
	public void onMouse(MouseEvent event) {
		int wheel = event.getDwheel();
		if (wheel != 0) {
			if (!NostrumMagica.getMagicWrapper(Minecraft.getMinecraft().thePlayer)
					.isUnlocked())
				return;
			ItemStack tome = NostrumMagica.getCurrentTome(Minecraft.getMinecraft().thePlayer);
			if (tome != null) {
				if (bindingScroll.isKeyDown()) {
					wheel = (wheel > 0 ? -1 : 1);
					SpellTome.incrementIndex(tome, wheel);
					event.setCanceled(true);
				}
			}
		}
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
			
			if (!Minecraft.getMinecraft().thePlayer.isCreative()) {
				if (mana < cost) {
					EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
					for (int i = 0; i < 15; i++) {
						double offsetx = Math.cos(i * (2 * Math.PI / 15)) * 1.0;
						double offsetz = Math.sin(i * (2 * Math.PI / 15)) * 1.0;
						player.worldObj
							.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
									player.posX + offsetx, player.posY, player.posZ + offsetz,
									0, -.5, 0);
						
						NostrumMagicaSounds.CAST_FAIL.play(player);
					}
					overlayRenderer.startManaWiggle(2);
					return;
				}
				
				NostrumMagica.getMagicWrapper(Minecraft.getMinecraft().thePlayer)
					.addMana(-cost);
			}
			
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
