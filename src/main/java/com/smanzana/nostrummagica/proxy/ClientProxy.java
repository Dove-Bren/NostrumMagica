package com.smanzana.nostrummagica.proxy;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.input.Keyboard;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.CursedIce;
import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.blocks.MagicWall;
import com.smanzana.nostrummagica.blocks.ManiOre;
import com.smanzana.nostrummagica.blocks.NostrumMagicaFlower;
import com.smanzana.nostrummagica.blocks.NostrumMirrorBlock;
import com.smanzana.nostrummagica.blocks.NostrumSingleSpawner;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.client.overlay.OverlayRenderer;
import com.smanzana.nostrummagica.client.render.TileEntitySymbolRenderer;
import com.smanzana.nostrummagica.entity.EntityGolem;
import com.smanzana.nostrummagica.entity.renderer.ModelGolem;
import com.smanzana.nostrummagica.entity.renderer.RenderGolem;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.EnchantedArmor;
import com.smanzana.nostrummagica.items.EnchantedWeapon;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.MagicArmorBase;
import com.smanzana.nostrummagica.items.MagicSwordBase;
import com.smanzana.nostrummagica.items.MirrorItem;
import com.smanzana.nostrummagica.items.NostrumGuide;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SeekerIdol;
import com.smanzana.nostrummagica.items.ShrineSeekingGem;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTableItem;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ClientCastMessage;
import com.smanzana.nostrummagica.network.messages.SpellTomeIncrementMessage;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.model.obj.OBJLoader;
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
		
		variants = new ResourceLocation[EMagicElement.values().length];
		i = 0;
		for (EMagicElement type : EMagicElement.values()) {
			if (type == EMagicElement.PHYSICAL)
				continue;
			variants[i++] = new ResourceLocation(NostrumMagica.MODID,
					"gem_" + InfusedGemItem.instance().getNameFromMeta(InfusedGemItem.instance()
							.getMetaFromElement(type)));
		}
		variants[i++] = new ResourceLocation(NostrumMagica.MODID,
				"gem_basic");
		ModelBakery.registerItemVariants(InfusedGemItem.instance(), variants);
		
		List<ResourceLocation> list = new LinkedList<>();
		for (EMagicElement type : EMagicElement.values()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, "rune_" + type.name().toLowerCase()));
    	}
    	for (EAlteration type : EAlteration.values()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, "rune_" + type.name().toLowerCase()));
    	}
    	for (SpellShape type : SpellShape.getAllShapes()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, "rune_" + type.getShapeKey()));
    	}
    	for (SpellTrigger type : SpellTrigger.getAllTriggers()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, "rune_" + type.getTriggerKey()));
    	}
    	
    	variants = list.toArray(new ResourceLocation[0]);
    	ModelBakery.registerItemVariants(SpellRune.instance(), variants);
    	
    	TileEntitySymbolRenderer.init();
    	
    	OBJLoader.INSTANCE.addDomain(NostrumMagica.MODID);
	}
	
	@Override
	public void init() {
		super.init();
		
		registerModel(SpellTome.instance(), 0, SpellTome.id);
		registerModel(NostrumGuide.instance(), 0, NostrumGuide.id);
		registerModel(SpellScroll.instance(), 0, SpellScroll.id);
		registerModel(BlankScroll.instance(), 0, BlankScroll.id);
		registerModel(ReagentBag.instance(), 0, ReagentBag.id);
		registerModel(SeekerIdol.instance(), 0, SeekerIdol.id);
		registerModel(ShrineSeekingGem.instance(), 0, ShrineSeekingGem.id);
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
		
		InfusedGemItem gem = InfusedGemItem.instance();
		
		registerModel(gem, 0, "gem_basic");
		
		for (EMagicElement type : EMagicElement.values()) {
			if (type == EMagicElement.PHYSICAL)
				continue;
			int meta = gem.getMetaFromElement(type);
			registerModel(gem, meta,
					"gem_" + gem.getNameFromMeta(meta));
		}
		
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
			.register(SpellRune.instance(), new SpellRune.ModelMesher());
		
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
		registerModel(Item.getItemFromBlock(NostrumSingleSpawner.instance()),
				0,
				NostrumSingleSpawner.ID);
		registerModel(Item.getItemFromBlock(CursedIce.instance()),
				0,
				CursedIce.ID);
		registerModel(Item.getItemFromBlock(ManiOre.instance()),
				0,
				ManiOre.ID);
		registerModel(SpellTableItem.instance(),
				0,
				SpellTableItem.ID);
		registerModel(MirrorItem.instance(),
				0,
				MirrorItem.ID);
		
		registerModel(new ItemBlock(DungeonBlock.instance()), 
				DungeonBlock.Type.DARK.ordinal(),
				DungeonBlock.Type.DARK.getName()
				);
		registerModel(new ItemBlock(DungeonBlock.instance()), 
				DungeonBlock.Type.LIGHT.ordinal(),
				DungeonBlock.Type.LIGHT.getName()
				);
		
		registerModel(new ItemBlock(NostrumMirrorBlock.instance()),
				0,
				NostrumMirrorBlock.ID);
	}
	
	@Override
	public void postinit() {
		this.overlayRenderer = new OverlayRenderer();
		
		super.postinit();
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
					int index = SpellTome.incrementIndex(tome, wheel);
					if (index != -1) {
						NetworkHandler.getSyncChannel()
							.sendToServer(new SpellTomeIncrementMessage(index));
					}
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
				EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
				if (mana < cost) {
					
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
				
				Map<ReagentType, Integer> reagents = spell.getRequiredReagents();
				for (Entry<ReagentType, Integer> row : reagents.entrySet()) {
					int count = NostrumMagica.getReagentCount(player, row.getKey());
					if (count < row.getValue()) {
						player.addChatMessage(new TextComponentString("Not enough "
								+ row.getKey().prettyName()));
						return;
					}
				}
				// actually deduct
				for (Entry<ReagentType, Integer> row : reagents.entrySet()) {
					NostrumMagica.removeReagents(player, row.getKey(), row.getValue());
				}
				
				NostrumMagica.getMagicWrapper(Minecraft.getMinecraft().thePlayer)
					.addMana(-cost);
			}
			
			NetworkHandler.getSyncChannel().sendToServer(
	    			new ClientCastMessage(spell, false));
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
	
	@Override
	public void sendServerConfig(EntityPlayerMP player) {
		; //do nothing on client side
	}
	
	@Override
	public void sendSpellDebug(EntityPlayer player, ITextComponent comp) {
		; 
	}
}
