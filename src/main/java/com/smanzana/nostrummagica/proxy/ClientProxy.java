package com.smanzana.nostrummagica.proxy;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.input.Keyboard;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.blocks.CursedIce;
import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.blocks.EssenceOre;
import com.smanzana.nostrummagica.blocks.LoreTable;
import com.smanzana.nostrummagica.blocks.MagicWall;
import com.smanzana.nostrummagica.blocks.ManiOre;
import com.smanzana.nostrummagica.blocks.ModificationTable;
import com.smanzana.nostrummagica.blocks.NostrumMagicaFlower;
import com.smanzana.nostrummagica.blocks.NostrumMirrorBlock;
import com.smanzana.nostrummagica.blocks.NostrumSingleSpawner;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.effects.ClientEffect;
import com.smanzana.nostrummagica.client.effects.ClientEffectBeam;
import com.smanzana.nostrummagica.client.effects.ClientEffectEchoed;
import com.smanzana.nostrummagica.client.effects.ClientEffectFormBasic;
import com.smanzana.nostrummagica.client.effects.ClientEffectIcon;
import com.smanzana.nostrummagica.client.effects.ClientEffectMirrored;
import com.smanzana.nostrummagica.client.effects.ClientEffectRenderer;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierColor;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierFollow;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierGrow;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierMove;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierRotate;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierShrink;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierTranslate;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.client.gui.MirrorGui;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.client.overlay.OverlayRenderer;
import com.smanzana.nostrummagica.client.render.TileEntityAltarRenderer;
import com.smanzana.nostrummagica.client.render.TileEntityCandleRenderer;
import com.smanzana.nostrummagica.client.render.TileEntityObeliskRenderer;
import com.smanzana.nostrummagica.client.render.TileEntitySymbolRenderer;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.entity.EntityDragon;
import com.smanzana.nostrummagica.entity.EntityDragonRed;
import com.smanzana.nostrummagica.entity.EntityGolem;
import com.smanzana.nostrummagica.entity.EntityKoid;
import com.smanzana.nostrummagica.entity.EntityShadowDragonRed;
import com.smanzana.nostrummagica.entity.EntityTameDragonRed;
import com.smanzana.nostrummagica.entity.ITameDragon;
import com.smanzana.nostrummagica.entity.renderer.ModelGolem;
import com.smanzana.nostrummagica.entity.renderer.RenderDragonRed;
import com.smanzana.nostrummagica.entity.renderer.RenderGolem;
import com.smanzana.nostrummagica.entity.renderer.RenderKoid;
import com.smanzana.nostrummagica.entity.renderer.RenderShadowDragonRed;
import com.smanzana.nostrummagica.entity.renderer.RenderTameDragonRed;
import com.smanzana.nostrummagica.items.AltarItem;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.ChalkItem;
import com.smanzana.nostrummagica.items.DragonEgg;
import com.smanzana.nostrummagica.items.DragonEggFragment;
import com.smanzana.nostrummagica.items.EnchantedArmor;
import com.smanzana.nostrummagica.items.EnchantedWeapon;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.ISpellArmor;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.MageStaff;
import com.smanzana.nostrummagica.items.MagicArmorBase;
import com.smanzana.nostrummagica.items.MagicCharm;
import com.smanzana.nostrummagica.items.MagicSwordBase;
import com.smanzana.nostrummagica.items.MasteryOrb;
import com.smanzana.nostrummagica.items.MirrorItem;
import com.smanzana.nostrummagica.items.NostrumGuide;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.NostrumRoseItem;
import com.smanzana.nostrummagica.items.NostrumRoseItem.RoseType;
import com.smanzana.nostrummagica.items.NostrumSkillItem;
import com.smanzana.nostrummagica.items.NostrumSkillItem.SkillItemType;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.items.PositionToken;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.RuneBag;
import com.smanzana.nostrummagica.items.SeekerIdol;
import com.smanzana.nostrummagica.items.ShrineSeekingGem;
import com.smanzana.nostrummagica.items.SpellPlate;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTableItem;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.items.SpellTomePage;
import com.smanzana.nostrummagica.items.SpellcraftGuide;
import com.smanzana.nostrummagica.items.ThanoPendant;
import com.smanzana.nostrummagica.items.ThanosStaff;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ClientCastMessage;
import com.smanzana.nostrummagica.network.messages.ObeliskTeleportationRequestMessage;
import com.smanzana.nostrummagica.network.messages.SpellTomeIncrementMessage;
import com.smanzana.nostrummagica.network.messages.StatRequestMessage;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.BeamTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.FoodTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.HealthTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ManaTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.OtherTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProximityTrigger;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancementWrapper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class ClientProxy extends CommonProxy {
	
	private KeyBinding bindingCast;
	private KeyBinding bindingScroll;
	private KeyBinding bindingInfo;
	private OverlayRenderer overlayRenderer;
	private ClientEffectRenderer effectRenderer;

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
		bindingInfo = new KeyBinding("key.infoscreen.desc", Keyboard.KEY_HOME, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingInfo);
		
		RenderingRegistry.registerEntityRenderingHandler(EntityGolem.class, new IRenderFactory<EntityGolem>() {
			@Override
			public Render<? super EntityGolem> createRenderFor(RenderManager manager) {
				return new RenderGolem(manager, new ModelGolem(), .8f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityKoid.class, new IRenderFactory<EntityKoid>() {
			@Override
			public Render<? super EntityKoid> createRenderFor(RenderManager manager) {
				return new RenderKoid(manager, .3f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityDragonRed.class, new IRenderFactory<EntityDragonRed>() {
			@Override
			public Render<? super EntityDragonRed> createRenderFor(RenderManager manager) {
				return new RenderDragonRed(manager, 5);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityTameDragonRed.class, new IRenderFactory<EntityTameDragonRed>() {
			@Override
			public Render<? super EntityTameDragonRed> createRenderFor(RenderManager manager) {
				return new RenderTameDragonRed(manager, 2);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityShadowDragonRed.class, new IRenderFactory<EntityShadowDragonRed>() {
			@Override
			public Render<? super EntityShadowDragonRed> createRenderFor(RenderManager manager) {
				return new RenderShadowDragonRed(manager, 2);
			}
		});
		
		ResourceLocation variants[] = new ResourceLocation[ReagentType.values().length];
		int i = 0;
		for (ReagentType type : ReagentType.values()) {
			variants[i++] = new ResourceLocation(NostrumMagica.MODID,
					ReagentItem.getNameFromMeta(type.getMeta()));
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
    	
    	variants = new ResourceLocation[EMagicElement.values().length];
		i = 0;
		for (EMagicElement type : EMagicElement.values()) {
			variants[i++] = new ResourceLocation(NostrumMagica.MODID,
					"charm_" + MagicCharm.getNameFromMeta(MagicCharm
							.getMetaFromType(type)));
		}
		ModelBakery.registerItemVariants(MagicCharm.instance(), variants);
    	
    	list = new LinkedList<>();
    	List<ResourceLocation> list2 = new LinkedList<>();
    	for (i = 1; i <= SpellTome.MAX_TOME_COUNT; i++) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, SpellTome.id + i));
    		list2.add(new ResourceLocation(NostrumMagica.MODID, SpellPlate.id + i));
    	}
    	
    	variants = list.toArray(new ResourceLocation[0]);
    	ModelBakery.registerItemVariants(SpellTome.instance(), variants);
    	variants = list2.toArray(new ResourceLocation[0]);
    	ModelBakery.registerItemVariants(SpellPlate.instance(), variants);
    	
    	list = new LinkedList<>();
    	for (ResourceType type : ResourceType.values()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, type.getUnlocalizedKey()));
    	}
    	
    	variants = list.toArray(new ResourceLocation[0]);
    	ModelBakery.registerItemVariants(NostrumResourceItem.instance(), variants);
    	
    	list = new LinkedList<>();
    	for (SkillItemType type : SkillItemType.values()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, type.getUnlocalizedKey()));
    	}
    	
    	variants = list.toArray(new ResourceLocation[0]);
    	ModelBakery.registerItemVariants(NostrumSkillItem.instance(), variants);
    	
    	list = new LinkedList<>();
    	for (RoseType type : RoseType.values()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, type.getUnlocalizedKey()));
    	}
    	
    	variants = list.toArray(new ResourceLocation[0]);
    	ModelBakery.registerItemVariants(NostrumRoseItem.instance(), variants);
    	
    	ModelBakery.registerItemVariants(ThanosStaff.instance(),
    			new ResourceLocation(NostrumMagica.MODID, ThanosStaff.ID),
    			new ResourceLocation(NostrumMagica.MODID, ThanosStaff.ID + "_activated"));
    	
    	TileEntitySymbolRenderer.init();
    	TileEntityCandleRenderer.init();
    	TileEntityAltarRenderer.init();
    	TileEntityObeliskRenderer.init();
    	
    	OBJLoader.INSTANCE.addDomain(NostrumMagica.MODID);
    	
    	MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void init() {
		super.init();
		
		//registerModel(SpellTome.instance(), 0, SpellTome.id);
		registerModel(NostrumGuide.instance(), 0, NostrumGuide.id);
		registerModel(SpellcraftGuide.instance(), 0, SpellcraftGuide.id);
		registerModel(SpellScroll.instance(), 0, SpellScroll.id);
		registerModel(SpellScroll.instance(), 1, SpellScroll.id);
		registerModel(SpellScroll.instance(), 2, SpellScroll.id);
		registerModel(BlankScroll.instance(), 0, BlankScroll.id);
		registerModel(DragonEggFragment.instance(), 0, DragonEggFragment.id);
		registerModel(DragonEgg.instance(), 0, DragonEgg.ID);
		registerModel(ReagentBag.instance(), 0, ReagentBag.id);
		registerModel(RuneBag.instance(), 0, RuneBag.id);
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
					ReagentItem.getNameFromMeta(type.getMeta()));
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
		
		for (EMagicElement type : EMagicElement.values()) {
			int meta = MagicCharm.getMetaFromType(type);
			registerModel(MagicCharm.instance(), meta,
					"charm_" + MagicCharm.getNameFromMeta(meta));
		}
		
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
			.register(SpellRune.instance(), new SpellRune.ModelMesher());
		
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
			.register(ThanosStaff.instance(), new ThanosStaff.ModelMesher());
		
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
		registerModel(Item.getItemFromBlock(Candle.instance()),
				0,
				Candle.ID);
		registerModel(Item.getItemFromBlock(ManiOre.instance()),
				0,
				ManiOre.ID);
		registerModel(Item.getItemFromBlock(EssenceOre.instance()),
				0,
				EssenceOre.ID);
		registerModel(SpellTableItem.instance(),
				0,
				SpellTableItem.ID);
		registerModel(MirrorItem.instance(),
				0,
				MirrorItem.ID);
		registerModel(Item.getItemFromBlock(ModificationTable.instance()),
				0,
				ModificationTable.ID);
		registerModel(Item.getItemFromBlock(LoreTable.instance()),
				0,
				LoreTable.ID);
		
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
		
		registerModel(ChalkItem.instance(),
				0,
				ChalkItem.ID);
		
		registerModel(AltarItem.instance(),
				0,
				AltarItem.ID);
		
		for (ResourceType type : ResourceType.values()) {
			registerModel(NostrumResourceItem.instance(),
					NostrumResourceItem.getMetaFromType(type),
					type.getUnlocalizedKey());
		}
		
		for (SkillItemType type : SkillItemType.values()) {
			registerModel(NostrumSkillItem.instance(),
					NostrumSkillItem.getMetaFromType(type),
					type.getUnlocalizedKey());
		}
		
		for (RoseType type : RoseType.values()) {
			registerModel(NostrumRoseItem.instance(),
					NostrumRoseItem.getMetaFromType(type),
					type.getUnlocalizedKey());
		}
		
		registerModel(PositionCrystal.instance(),
				0,
				PositionCrystal.ID);
		
		registerModel(PositionToken.instance(),
				0,
				PositionToken.ID);
		
		registerModel(SpellTomePage.instance(),
				0,
				SpellTomePage.id);
		registerModel(MasteryOrb.instance(),
				0,
				MasteryOrb.id);
		
    	for (int i = 1; i <= SpellTome.MAX_TOME_COUNT; i++) {
    		registerModel(SpellTome.instance(), i - 1, SpellTome.id + i);
    		registerModel(SpellPlate.instance(), i - 1, SpellPlate.id + i);
    	}
    	
    	registerModel(MageStaff.instance(),
    			0,
    			MageStaff.ID);
    	
    	registerModel(ThanoPendant.instance(),
    			0,
    			ThanoPendant.ID);
		
		for (EMagicElement element : EMagicElement.values()) {
			registerModel(EssenceItem.instance(),
					element.ordinal(),
					EssenceItem.ID);
		}
		IItemColor tinter = new IItemColor() {

			@Override
			public int getColorFromItemstack(ItemStack stack, int tintIndex) {
				EMagicElement element = EssenceItem.findType(stack);
				return element.getColor();
			}
			
		};
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(tinter,
				EssenceItem.instance()
				);
		
	}
	
	@Override
	public void postinit() {
		this.overlayRenderer = new OverlayRenderer();
		this.effectRenderer = ClientEffectRenderer.instance();
		
		initDefaultEffects(this.effectRenderer);
		
		super.postinit();
	}
	
	public static void registerModel(Item item, int meta, String modelName) {
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
		if (bindingCast.isPressed())
			doCast();
		else if (bindingInfo.isPressed()) {
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			player.openGui(NostrumMagica.instance,
					NostrumGui.infoscreenID, player.worldObj, 0, 0, 0);
		} else if (Minecraft.getMinecraft().gameSettings.keyBindJump.isPressed()) {
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			if (player.isRiding() && player.getRidingEntity() instanceof EntityTameDragonRed) {
				((EntityDragon) player.getRidingEntity()).dragonJump();
			}
		}
	}
	
	private void doCast() {
		
		Spell spell = NostrumMagica.getCurrentSpell(Minecraft.getMinecraft().thePlayer);
		if (spell == null)
			return;
		
		// Do mana check here (it's also done on server)
		// to stop redundant checks and get mana looking good
		// on client side immediately
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		INostrumMagic att = NostrumMagica.getMagicWrapper(player);
		int mana = att.getMana();
		int cost = spell.getManaCost();
		SpellCastSummary summary = new SpellCastSummary(cost, 0);
		
		// Add the player's personal bonuses
		summary.addCostRate(-att.getManaCostModifier());
		
		// Find the tome this was cast from, if any
		ItemStack tome = NostrumMagica.getCurrentTome(player); 
		if (tome != null && tome.getItem() instanceof SpellTome) {
			// Casting from a tome.
			
			// Make sure it isn't too hard for the tome
			int cap = SpellTome.getMaxMana(tome);
			if (cap < cost) {
				player.addChatMessage(new TextComponentTranslation(
						"info.spell.tome_weak", new Object[0]));
				NostrumMagicaSounds.CAST_FAIL.play(player);
				return;
			}
			
			List<SpellTomeEnhancementWrapper> enhancements = SpellTome.getEnhancements(tome);
			if (enhancements != null && !enhancements.isEmpty())
			for (SpellTomeEnhancementWrapper enhance : enhancements) {
				enhance.getEnhancement().onCast(
						enhance.getLevel(), summary, player, att);
			}
		}
		
		// Cap enhancements at 80% LRC
		{
			float lrc = summary.getReagentCost();
			if (lrc < .2f)
				summary.addCostRate(.2f - lrc); // Add however much we need to get to 1
		}
		
		// Visit an equipped spell armor
		for (ItemStack equip : player.getEquipmentAndArmor()) {
			if (equip == null)
				continue;
			if (equip.getItem() instanceof ISpellArmor) {
				ISpellArmor armor = (ISpellArmor) equip.getItem();
				armor.apply(player, summary, equip);
			}
		}
		
		// Possible use baubles
		IInventory baubles = NostrumMagica.baubles.getBaubles(player);
		if (baubles != null) {
			for (int i = 0; i < baubles.getSizeInventory(); i++) {
				ItemStack equip = baubles.getStackInSlot(i);
				if (equip == null) {
					continue;
				}
				
				if (equip.getItem() instanceof ISpellArmor) {
					ISpellArmor armor = (ISpellArmor) equip.getItem();
					armor.apply(player, summary, equip);
				}
			}
		}
		
		cost = summary.getFinalCost();
		
		// Add dragon mana pool
		Collection<ITameDragon> dragons = NostrumMagica.getNearbyTamedDragons(player, 24, true);
		if (dragons != null && !dragons.isEmpty()) {
			for (ITameDragon dragon : dragons) {
				if (dragon.sharesMana(Minecraft.getMinecraft().thePlayer)) {
					mana += dragon.getMana();
				}
			}
		}
		
		if (!Minecraft.getMinecraft().thePlayer.isCreative()) {
			// Check mana
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
			
			// Check attributes
			int maxComps = 2 * (att.getTech() + 1);
			int maxTriggers = 1 + (att.getFinesse());
			int maxElems = 1 + (3 * att.getControl());
			if (spell.getComponentCount() > maxComps) {
				player.addChatMessage(new TextComponentTranslation(
						"info.spell.low_tech", new Object[0]));
				NostrumMagicaSounds.CAST_FAIL.play(player);
				return;
			} else if (spell.getElementCount() > maxElems) {
				player.addChatMessage(new TextComponentTranslation(
						"info.spell.low_control", new Object[0]));
				NostrumMagicaSounds.CAST_FAIL.play(player);
				return;
			} else if (spell.getTriggerCount() > maxTriggers) {
				player.addChatMessage(new TextComponentTranslation(
						"info.spell.low_finesse", new Object[0]));
				NostrumMagicaSounds.CAST_FAIL.play(player);
				return;
			}
			
			// Check elemental mastery
			for (SpellPart part : spell.getSpellParts()) {
	    		if (part.isTrigger())
	    			continue;
	    		EMagicElement elem = part.getElement();
	    		if (elem == null)
	    			elem = EMagicElement.PHYSICAL;
	    		int level = part.getElementCount();
	    		
	    		if (level == 1) {
	    			Boolean know = att.getKnownElements().get(elem);
	    			if (know == null || !know) {
	    				player.addChatMessage(new TextComponentTranslation(
								"info.spell.no_mastery", new Object[] {elem.getName()}));
						NostrumMagicaSounds.CAST_FAIL.play(player);
						return;
	    			}
				} else {
		    		Integer mast = att.getElementMastery().get(elem);
		    		int mastery = (mast == null ? 0 : mast);
		    		if (mastery < level)
		    			player.addChatMessage(new TextComponentTranslation(
								"info.spell.low_mastery", new Object[] {elem.getName(), level, mastery}));
						NostrumMagicaSounds.CAST_FAIL.play(player);
						return;
				}
	    	}
			
			// Check reagents
			// Skip check if there's a server-side chance of it still working anyways
			if (summary.getReagentCost() >= 1f) {
				Map<ReagentType, Integer> reagents = spell.getRequiredReagents();
				for (Entry<ReagentType, Integer> row : reagents.entrySet()) {
					int count = NostrumMagica.getReagentCount(player, row.getKey());
					if (count < row.getValue()) {
						player.addChatMessage(new TextComponentTranslation("info.spell.bad_reagent", row.getKey().prettyName()));
						return;
					}
				}
				
				// Don't actually deduct on client.
				// Response from server will result in deduct if it goes through
			}
			
			NostrumMagica.getMagicWrapper(Minecraft.getMinecraft().thePlayer)
				.addMana(-cost);
		}
		
		NetworkHandler.getSyncChannel().sendToServer(
    			new ClientCastMessage(spell, false, SpellTome.getTomeID(tome)));
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
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		INostrumMagic existing = NostrumMagica.getMagicWrapper(player);
		if (existing != null && !player.isDead) {
			// apply them
			existing.copy(override);
			
			// If we're on a screen that cares, refresh it
			if (Minecraft.getMinecraft().currentScreen instanceof MirrorGui) {
				((MirrorGui) Minecraft.getMinecraft().currentScreen).refresh();
			}
		} else {
			// Stash them
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
	public void openDragonGUI(EntityPlayer player, ITameDragon dragon) {
		// Integrated clients still need to open the gui...
		//if (!player.worldObj.isRemote) {
//			DragonContainer container = dragon.getGUIContainer();
//			DragonGUI gui = new DragonGUI(container);
//			FMLCommonHandler.instance().showGuiScreen(gui);
			super.openDragonGUI(player, dragon);
		//}
	}
	
	@Override
	public void sendServerConfig(EntityPlayerMP player) {
		; //do nothing on client side
	}
	
	@Override
	public void sendSpellDebug(EntityPlayer player, ITextComponent comp) {
		if (!player.worldObj.isRemote) {
			super.sendSpellDebug(player, comp);
		}
		;
	}
	
	@Override
	public String getTranslation(String key) {
		return I18n.format(key, new Object[0]).trim();
	}
	
	@Override
	public void requestObeliskTransportation(BlockPos origin, BlockPos target) {
		// Send a request to the server
		NetworkHandler.getSyncChannel().sendToServer(
				new ObeliskTeleportationRequestMessage(origin, target)
				);
	}
	
	@Override
	public void requestStats(EntityLivingBase entity) {
		NetworkHandler.getSyncChannel().sendToServer(
				new StatRequestMessage()
				);
	}
	
	@SubscribeEvent
	public void stitchEventPre(TextureStitchEvent.Pre event) {
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "entity/koid"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "entity/golem_ender"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "entity/dragon_C"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "entity/dragon_TR"));
	}
	
	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
    	for (ClientEffectIcon icon: ClientEffectIcon.values()) {
    		IModel model;
			try {
				model = ModelLoaderRegistry.getModel(new ResourceLocation(
						NostrumMagica.MODID, "effect/" + icon.getModelKey()
						));
				IBakedModel bakedModel = model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, 
	    				(location) -> {return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());});
	    		event.getModelRegistry().putObject(
	    				new ModelResourceLocation(NostrumMagica.MODID + ":effects/" + icon.getKey(), "normal"),
	    				bakedModel);
			} catch (Exception e) {
				e.printStackTrace();
				NostrumMagica.logger.warn("Failed to load effect " + icon.getKey());
			}
    		
    	}
	}
	
	private static void initDefaultEffects(ClientEffectRenderer renderer) {
		
		// elements 
		for (EMagicElement element : EMagicElement.values()) {
			renderer.registerEffect(new SpellComponentWrapper(element),
					(source, sourcePos, target, targetPos, flavor) -> {
						ClientEffect effect = new ClientEffectMirrored(target == null ? targetPos : new Vec3d(0, 0, 0),
								new ClientEffectFormBasic(ClientEffectIcon.TING1, 0, 0, 0),
								500L, 5);
						
						if (target != null)
							effect.modify(new ClientEffectModifierFollow(target));
						
						effect
						.modify(new ClientEffectModifierColor(element.getColor(), element.getColor()))
						.modify(new ClientEffectModifierRotate(0f, .4f, 0f))
						.modify(new ClientEffectModifierTranslate(0, 0, -1))
						.modify(new ClientEffectModifierMove(new Vec3d(0, 1.5, 0), new Vec3d(0, .5, .7), .5f, 1f))
						.modify(new ClientEffectModifierGrow(.1f, .3f, .2f, .8f, .5f))
						;
						return effect;
					});
		}
		
		// triggers (that have them)
		renderer.registerEffect(new SpellComponentWrapper(BeamTrigger.instance()),
				(source, sourcePos, target, targetPos, flavor) -> {
					ClientEffect effect = new ClientEffectBeam(sourcePos == null ? source.getPositionVector() : sourcePos,
							targetPos == null ? target.getPositionVector() : targetPos,
							500L);
					
					//if (target != null)
					//	effect.modify(new ClientEffectModifierFollow(target));
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
					effect
					.modify(new ClientEffectModifierGrow(1f, .2f, 1f, 1f, .4f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, .2f, .6f))
					;
					return effect;
				});
		
//		renderer.registerEffect(new SpellComponentWrapper(SelfTrigger.instance()),
//				(source, sourcePos, target, targetPos, flavor) -> {
//					ClientEffect effect = new ClientEffect(source == null ? sourcePos : source.getPositionVector(),
//							new ClientEffectFormBasic(ClientEffectIcon.TING2, 0, 0, 0),
//							10);
//					
//					if (target != null)
//						effect.modify(new ClientEffectModifierFollow(target));
//					
//					if (flavor != null && flavor.isElement()) {
//						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
//					}
//					
//					effect
//					.modify(new ClientEffectModifierGrow(1f, .2f, 1f, 1f, .4f))
//					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, .2f, .6f))
//					;
//					return effect;
//				});
		// Can't think of a cool one for self. Oh well
		
		renderer.registerEffect(new SpellComponentWrapper(OtherTrigger.instance()),
				(source, sourcePos, target, targetPos, flavor) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
							new ClientEffectFormBasic(ClientEffectIcon.TING3, 0, 0, 0),
							500L, 6);
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
					effect
					.modify(new ClientEffectModifierTranslate(0, 1, -1))
					.modify(new ClientEffectModifierRotate(.4f, 0f, 1.2f))
					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, 1f, .3f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, .2f, .8f))
					;
					return effect;
				});
		
		renderer.registerEffect(new SpellComponentWrapper(HealthTrigger.instance()),
				(source, sourcePos, target, targetPos, flavor) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
							new ClientEffectFormBasic(ClientEffectIcon.TING5, 0, 0, 0),
							1000L, 4);
					
					if (target != null)
						effect.modify(new ClientEffectModifierFollow(target));
					
					effect
					.modify(new ClientEffectModifierColor(0xFFA50500, 0xFFA50500))
					.modify(new ClientEffectModifierTranslate(0, 2, -1))
					.modify(new ClientEffectModifierRotate(0f, .5f, 0f))
					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, 1f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .8f))
					;
					return effect;
				});
		
		renderer.registerEffect(new SpellComponentWrapper(ManaTrigger.instance()),
				(source, sourcePos, target, targetPos, flavor) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
							new ClientEffectFormBasic(ClientEffectIcon.TING5, 0, 0, 0),
							1000L, 4);
					
					if (target != null)
						effect.modify(new ClientEffectModifierFollow(target));
					
					effect
					.modify(new ClientEffectModifierColor(0xFF0005A5, 0xFF0005A5))
					.modify(new ClientEffectModifierTranslate(0, 2, -1))
					.modify(new ClientEffectModifierRotate(0f, .5f, 0f))
					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, 1f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .8f))
					;
					return effect;
				});
		
		renderer.registerEffect(new SpellComponentWrapper(FoodTrigger.instance()),
				(source, sourcePos, target, targetPos, flavor) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
							new ClientEffectFormBasic(ClientEffectIcon.TING5, 0, 0, 0),
							1000L, 4);
					
					if (target != null)
						effect.modify(new ClientEffectModifierFollow(target));
					
					effect
					.modify(new ClientEffectModifierColor(0xFFC6CC30, 0xFFC6CC30))
					.modify(new ClientEffectModifierTranslate(0, 2, -1))
					.modify(new ClientEffectModifierRotate(0f, .5f, 0f))
					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, 1f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .8f))
					;
					return effect;
				});
		
		renderer.registerEffect(new SpellComponentWrapper(ProximityTrigger.instance()),
				(source, sourcePos, target, targetPos, flavor) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
							new ClientEffectFormBasic(ClientEffectIcon.TING4, 0, 0, 0),
							2L * 1000L, 5);
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
					effect
					.modify(new ClientEffectModifierRotate(0f, .5f, 0f))
					.modify(new ClientEffectModifierTranslate(0, .2f, .5f))
					.modify(new ClientEffectModifierGrow(.2f, .2f, .4f, .5f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .4f))
					;
					return effect;
				});
		
		// Alterations
		renderer.registerEffect(new SpellComponentWrapper(EAlteration.INFLICT),
				(source, sourcePos, target, targetPos, flavor) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
							new ClientEffectFormBasic(ClientEffectIcon.ARROWD, 0, 0, 0),
							3L * 500L, 6);
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
					
					effect
					.modify(new ClientEffectModifierRotate(0f, .5f, 0f))
					.modify(new ClientEffectModifierTranslate(0, 1.5f, -1.5f))
					.modify(new ClientEffectModifierMove(new Vec3d(0, 0, 0), new Vec3d(0, -2, 0), .3f, 1f))
					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, 1f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .8f))
					;
					return effect;
				});

		renderer.registerEffect(new SpellComponentWrapper(EAlteration.RESIST),
				(source, sourcePos, target, targetPos, flavor) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
							new ClientEffectFormBasic(ClientEffectIcon.ARROWU, 0, 0, 0),
							3L * 500L, 6);
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
					
					effect
					.modify(new ClientEffectModifierRotate(0f, .5f, 0f))
					.modify(new ClientEffectModifierTranslate(0, 0f, -1.5f))
					.modify(new ClientEffectModifierMove(new Vec3d(0, 0, 0), new Vec3d(0, 1.5, 0), 0f, .7f))
					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, 1f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .8f))
					;
					return effect;
				});

		renderer.registerEffect(new SpellComponentWrapper(EAlteration.GROWTH),
				(source, sourcePos, target, targetPos, flavor) -> {
					ClientEffect effect = new ClientEffectEchoed(new Vec3d(0,0,0), 
							new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
							new ClientEffectFormBasic(ClientEffectIcon.TING3, 0, 0, 0),
							2L * 1000L, 4), 2L * 1000L, 5, .2f);
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
					
					effect
					.modify(new ClientEffectModifierTranslate(0, 1, 0))
					.modify(new ClientEffectModifierRotate(1f, 2f, 0f))
					.modify(new ClientEffectModifierTranslate(.5f, 0f, -1.2f))
					.modify(new ClientEffectModifierGrow(.2f, .2f, .4f, .6f, .5f))
					;
					return effect;
				});

		renderer.registerEffect(new SpellComponentWrapper(EAlteration.SUPPORT),
				(source, sourcePos, target, targetPos, flavor) -> {
					ClientEffect effect;
					boolean isShield = false;
					if (flavor != null && flavor.isElement() && 
							(flavor.getElement() == EMagicElement.EARTH || flavor.getElement() == EMagicElement.ICE)) {
						// Special ones for shields!
						effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
								new ClientEffectFormBasic(ClientEffectIcon.SHIELD, 0, 0, 0),
								3L * 500L, 5);
						isShield = true;
					} else {
						effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
								new ClientEffectFormBasic(ClientEffectIcon.TING5, 0, 0, 0),
								3L * 500L, 10);
					}
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
					
					if (!isShield)
						effect.modify(new ClientEffectModifierRotate(0f, -.5f, 0f));
					
					effect.modify(new ClientEffectModifierTranslate(0f, 1f, -1.2f));
					
					if (isShield) {
						effect.modify(new ClientEffectModifierRotate(0f, -.5f, 0f))
							.modify(new ClientEffectModifierGrow(1f, .2f, 1f, .8f, .5f));
					} else {
						effect
						.modify(new ClientEffectModifierGrow(.2f, .2f, .4f, .6f, .5f))
						.modify(new ClientEffectModifierShrink(1f, 1f, 0f, 0f, .5f))
					;
					}
					return effect;
				});

		renderer.registerEffect(new SpellComponentWrapper(EAlteration.ENCHANT),
				(source, sourcePos, target, targetPos, flavor) -> {
					ClientEffect effect = new ClientEffectMirrored((targetPos == null ? target.getPositionVector() : targetPos).addVector(0, 1, 0),
							new ClientEffectFormBasic(ClientEffectIcon.TING4, 0, 0, 0),
							3L * 500L, 6, new Vec3d(1, 0, 0));
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
					
					effect
					.modify(new ClientEffectModifierRotate(1f, 0f, 1f))
					.modify(new ClientEffectModifierTranslate(-.5f, 0f, 1f))
					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, .6f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .6f))
					;
					return effect;
				});

		renderer.registerEffect(new SpellComponentWrapper(EAlteration.CONJURE),
				(source, sourcePos, target, targetPos, flavor) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
							new ClientEffectFormBasic(ClientEffectIcon.TING4, 0, 0, 0),
							1L * 500L, 6);
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
										
					effect
					.modify(new ClientEffectModifierTranslate(0f, 1f, 0f))
					.modify(new ClientEffectModifierMove(new Vec3d(0, 0, 0), new Vec3d(0, 1.5, 0), 0f, .3f))
					.modify(new ClientEffectModifierMove(new Vec3d(0, 0, 0), new Vec3d(0, 0, 1.5)))
					.modify(new ClientEffectModifierMove(new Vec3d(0, 0, 0), new Vec3d(0, -2, 0), 0f, 1f))
					.modify(new ClientEffectModifierGrow(.6f, .2f, .7f, .6f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, .5f, 0f, .6f))
					;
					return effect;
				});

		renderer.registerEffect(new SpellComponentWrapper(EAlteration.SUMMON),
				(source, sourcePos, target, targetPos, flavor) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
							new ClientEffectFormBasic(ClientEffectIcon.TING1, 0, 0, 0),
							1L * 500L, 6);
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
					
					effect
					.modify(new ClientEffectModifierRotate(0f, -.5f, 0f))
					.modify(new ClientEffectModifierTranslate(0f, 1f, 0f))
					.modify(new ClientEffectModifierMove(new Vec3d(0, 0, 0), new Vec3d(0, 1.5, 0), 0f, .3f))
					.modify(new ClientEffectModifierMove(new Vec3d(0, 0, 0), new Vec3d(0, 0, 1.5)))
					.modify(new ClientEffectModifierMove(new Vec3d(0, 0, 0), new Vec3d(0, -2, 0), 0f, 1f))
					.modify(new ClientEffectModifierRotate(1f, 0f, 0f))
					.modify(new ClientEffectModifierGrow(.6f, .2f, .7f, .6f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, .5f, 0f, .6f))
					;
					return effect;
				});

		renderer.registerEffect(new SpellComponentWrapper(EAlteration.ALTER),
				(source, sourcePos, target, targetPos, flavor) -> {
					ClientEffect effect = new ClientEffectMirrored((targetPos == null ? target.getPositionVector() : targetPos).addVector(0, 1, 0),
							new ClientEffectFormBasic(ClientEffectIcon.TING4, 0, 0, 0),
							2L * 500L, 6, new Vec3d(.5, .5, 0));
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
					
					effect
					.modify(new ClientEffectModifierTranslate(0f, 0f, .5f))
					.modify(new ClientEffectModifierRotate(1f, 0f, 1f))
					.modify(new ClientEffectModifierTranslate(-.5f, 0f, 1f))
					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, .6f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .6f))
					;
					return effect;
				});
	}
	
	@Override
	public void spawnEffect(World world, SpellComponentWrapper comp,
			EntityLivingBase caster, Vec3d casterPos,
			EntityLivingBase target, Vec3d targetPos,
			SpellComponentWrapper flavor) {
		if (world != null) {
			if (!world.isRemote) {
				super.spawnEffect(world, comp, caster, casterPos, target, targetPos, flavor);
				return;
			}
		}
		if (targetPos == null)
//			if (target != null)
//				targetPos = target.getPositionVector();
//			else
				targetPos = new Vec3d(0, 0, 0);
		
		this.effectRenderer.spawnEffect(comp, caster, casterPos, target, targetPos, flavor);
	}
	
	private static boolean shownText = false;
	@SubscribeEvent
	public void onClientConnect(EntityJoinWorldEvent event) {
		if (ClientProxy.shownText == false && ModConfig.config.displayLoginText()
				&& event.getEntity() == Minecraft.getMinecraft().thePlayer) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(
					new TextComponentTranslation("info.nostrumwelcome.text", new Object[]{
							this.bindingInfo.getDisplayName()
					}));
			ClientProxy.shownText = true;
		}
	}
}
