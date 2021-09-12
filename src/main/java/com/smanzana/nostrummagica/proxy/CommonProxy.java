package com.smanzana.nostrummagica.proxy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.ActiveHopper;
import com.smanzana.nostrummagica.blocks.AltarBlock;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.blocks.ChalkBlock;
import com.smanzana.nostrummagica.blocks.CropEssence;
import com.smanzana.nostrummagica.blocks.CropGinseng;
import com.smanzana.nostrummagica.blocks.CropMandrakeRoot;
import com.smanzana.nostrummagica.blocks.CursedIce;
import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.blocks.EssenceOre;
import com.smanzana.nostrummagica.blocks.ItemDuct;
import com.smanzana.nostrummagica.blocks.LogicDoor;
import com.smanzana.nostrummagica.blocks.LoreTable;
import com.smanzana.nostrummagica.blocks.MagicDirt;
import com.smanzana.nostrummagica.blocks.MagicWall;
import com.smanzana.nostrummagica.blocks.ManiCrystal;
import com.smanzana.nostrummagica.blocks.ManiOre;
import com.smanzana.nostrummagica.blocks.MimicBlock;
import com.smanzana.nostrummagica.blocks.ModificationTable;
import com.smanzana.nostrummagica.blocks.NostrumMagicaFlower;
import com.smanzana.nostrummagica.blocks.NostrumMirrorBlock;
import com.smanzana.nostrummagica.blocks.NostrumObelisk;
import com.smanzana.nostrummagica.blocks.NostrumSingleSpawner;
import com.smanzana.nostrummagica.blocks.NostrumSpawnAndTrigger;
import com.smanzana.nostrummagica.blocks.ObeliskPortal;
import com.smanzana.nostrummagica.blocks.ProgressionDoor;
import com.smanzana.nostrummagica.blocks.PutterBlock;
import com.smanzana.nostrummagica.blocks.ShrineBlock;
import com.smanzana.nostrummagica.blocks.SorceryPortal;
import com.smanzana.nostrummagica.blocks.SorceryPortalSpawner;
import com.smanzana.nostrummagica.blocks.SpellTable;
import com.smanzana.nostrummagica.blocks.SwitchBlock;
import com.smanzana.nostrummagica.blocks.SymbolBlock;
import com.smanzana.nostrummagica.blocks.TeleportRune;
import com.smanzana.nostrummagica.blocks.TeleportationPortal;
import com.smanzana.nostrummagica.blocks.TemporaryTeleportationPortal;
import com.smanzana.nostrummagica.blocks.tiles.ActiveHopperTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.AltarTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.CandleTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.ItemDuctTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.LoreTableEntity;
import com.smanzana.nostrummagica.blocks.tiles.ModificationTableEntity;
import com.smanzana.nostrummagica.blocks.tiles.NostrumObeliskEntity;
import com.smanzana.nostrummagica.blocks.tiles.ObeliskPortalTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.ProgressionDoorTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.PutterBlockTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.SingleSpawnerTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.SorceryPortalTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.SpawnerTriggerTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.SpellTableEntity;
import com.smanzana.nostrummagica.blocks.tiles.SwitchBlockTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.SymbolTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.TeleportRuneTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.TeleportationPortalTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.TemporaryPortalTileEntity;
import com.smanzana.nostrummagica.capabilities.CapabilityHandler;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagicStorage;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.client.gui.dragongui.TamedDragonGUI.DragonContainer;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.config.network.ServerConfigMessage;
import com.smanzana.nostrummagica.enchantments.EnchantmentManaRecovery;
import com.smanzana.nostrummagica.entity.EntityAreaEffect;
import com.smanzana.nostrummagica.entity.EntityChakramSpellSaucer;
import com.smanzana.nostrummagica.entity.EntityCyclerSpellSaucer;
import com.smanzana.nostrummagica.entity.EntityHookShot;
import com.smanzana.nostrummagica.entity.EntityKoid;
import com.smanzana.nostrummagica.entity.EntityLux;
import com.smanzana.nostrummagica.entity.EntitySpellBullet;
import com.smanzana.nostrummagica.entity.EntitySpellProjectile;
import com.smanzana.nostrummagica.entity.EntitySprite;
import com.smanzana.nostrummagica.entity.EntitySwitchTrigger;
import com.smanzana.nostrummagica.entity.EntityWisp;
import com.smanzana.nostrummagica.entity.NostrumTameLightning;
import com.smanzana.nostrummagica.entity.PetInfo;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonEgg;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonRed;
import com.smanzana.nostrummagica.entity.dragon.EntityShadowDragonRed;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;
import com.smanzana.nostrummagica.entity.dragon.ITameDragon;
import com.smanzana.nostrummagica.entity.golem.EntityGolemEarth;
import com.smanzana.nostrummagica.entity.golem.EntityGolemEnder;
import com.smanzana.nostrummagica.entity.golem.EntityGolemFire;
import com.smanzana.nostrummagica.entity.golem.EntityGolemIce;
import com.smanzana.nostrummagica.entity.golem.EntityGolemLightning;
import com.smanzana.nostrummagica.entity.golem.EntityGolemPhysical;
import com.smanzana.nostrummagica.entity.golem.EntityGolemWind;
import com.smanzana.nostrummagica.items.AltarItem;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.ChalkItem;
import com.smanzana.nostrummagica.items.DragonArmor;
import com.smanzana.nostrummagica.items.DragonEgg;
import com.smanzana.nostrummagica.items.DragonEggFragment;
import com.smanzana.nostrummagica.items.EnchantedArmor;
import com.smanzana.nostrummagica.items.EnchantedWeapon;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.HookshotItem;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.MageStaff;
import com.smanzana.nostrummagica.items.MagicArmorBase;
import com.smanzana.nostrummagica.items.MagicCharm;
import com.smanzana.nostrummagica.items.MagicSwordBase;
import com.smanzana.nostrummagica.items.MasteryOrb;
import com.smanzana.nostrummagica.items.MirrorItem;
import com.smanzana.nostrummagica.items.MirrorShield;
import com.smanzana.nostrummagica.items.MirrorShieldImproved;
import com.smanzana.nostrummagica.items.NostrumGuide;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumRoseItem;
import com.smanzana.nostrummagica.items.NostrumSkillItem;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.items.PositionToken;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentSeed;
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
import com.smanzana.nostrummagica.items.WarlockSword;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ClientEffectRenderMessage;
import com.smanzana.nostrummagica.network.messages.MagicEffectUpdate;
import com.smanzana.nostrummagica.network.messages.ManaMessage;
import com.smanzana.nostrummagica.network.messages.SpellDebugMessage;
import com.smanzana.nostrummagica.network.messages.SpellRequestReplyMessage;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;
import com.smanzana.nostrummagica.network.messages.TamedDragonGUIOpenMessage;
import com.smanzana.nostrummagica.potions.FamiliarPotion;
import com.smanzana.nostrummagica.potions.FrostbitePotion;
import com.smanzana.nostrummagica.potions.LightningAttackPotion;
import com.smanzana.nostrummagica.potions.LightningChargePotion;
import com.smanzana.nostrummagica.potions.MagicBoostPotion;
import com.smanzana.nostrummagica.potions.MagicBuffPotion;
import com.smanzana.nostrummagica.potions.MagicResistPotion;
import com.smanzana.nostrummagica.potions.MagicShieldPotion;
import com.smanzana.nostrummagica.potions.PhysicalShieldPotion;
import com.smanzana.nostrummagica.potions.RootedPotion;
import com.smanzana.nostrummagica.quests.NostrumQuest;
import com.smanzana.nostrummagica.research.NostrumResearch;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.ChainShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.BeamTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.DamagedTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.DelayTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.FoodTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.HealthTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MagicCutterTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MagicCyclerTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ManaTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.OtherTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProximityTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SeekingBulletTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.TouchTrigger;
import com.smanzana.nostrummagica.world.NostrumDungeonGenerator;
import com.smanzana.nostrummagica.world.NostrumFlowerGenerator;
import com.smanzana.nostrummagica.world.NostrumOreGenerator;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class CommonProxy {
	
	public CapabilityHandler capabilityHandler;
	
	public void preinit() {
		MinecraftForge.EVENT_BUS.register(this);
		
		CapabilityManager.INSTANCE.register(INostrumMagic.class, new NostrumMagicStorage(), NostrumMagic::new);
		capabilityHandler = new CapabilityHandler();
		NetworkHandler.getInstance();
		
		PetInfo.PetAction.Init();
		
    	registerShapes();
    	registerTriggers();
    	
    	EntityTameDragonRed.init();
	}
	
	public void init() {
    	// Moved here because this loads NostrumDungeonGenerator which needs to be after DungeonRoomRegistry is initted
    	// BUT we can't just move that to before we register blocks or all the sudden all the blueprints are lying
    	ShrineSeekingGem.init();
    	
    	GameRegistry.registerWorldGenerator(new NostrumOreGenerator(), 0);
    	GameRegistry.registerWorldGenerator(new NostrumFlowerGenerator(), 0);
    	GameRegistry.registerWorldGenerator(new NostrumDungeonGenerator(), 0);
    	
    	NetworkRegistry.INSTANCE.registerGuiHandler(NostrumMagica.instance, new NostrumGui());
    	
    	LoreRegistry.instance();
	}
	
	public void postinit() {
		for (Biome biome : Biome.REGISTRY) {
			biome.addFlower(NostrumMagicaFlower.instance().getState(NostrumMagicaFlower.Type.MIDNIGHT_IRIS), 8);
			biome.addFlower(NostrumMagicaFlower.instance().getState(NostrumMagicaFlower.Type.CRYSTABLOOM), 7);
		}
		
		NostrumQuest.Validate();
		NostrumResearch.Validate();
	}
    
    private void registerShapes() {
    	SpellShape.register(SingleShape.instance());
    	SpellShape.register(AoEShape.instance());
    	SpellShape.register(ChainShape.instance());
    }
    
    private void registerTriggers() {
    	SpellTrigger.register(SelfTrigger.instance());
    	SpellTrigger.register(TouchTrigger.instance());
    	SpellTrigger.register(AITargetTrigger.instance());
    	SpellTrigger.register(ProjectileTrigger.instance());
    	SpellTrigger.register(BeamTrigger.instance());
    	SpellTrigger.register(DelayTrigger.instance());
    	SpellTrigger.register(ProximityTrigger.instance());
    	SpellTrigger.register(HealthTrigger.instance());
    	SpellTrigger.register(FoodTrigger.instance());
    	SpellTrigger.register(ManaTrigger.instance());
    	SpellTrigger.register(DamagedTrigger.instance());
    	SpellTrigger.register(OtherTrigger.instance());
    	SpellTrigger.register(MagicCutterTrigger.instance());
    	SpellTrigger.register(MagicCyclerTrigger.instance());
    	SpellTrigger.register(SeekingBulletTrigger.instance());
    }
    
    @SubscribeEvent
    private void registerPotions(RegistryEvent.Register<Potion> event) {
    	final IForgeRegistry<Potion> registry = event.getRegistry();
    	
    	registry.register(RootedPotion.instance());
    	registry.register(MagicResistPotion.instance());
    	registry.register(PhysicalShieldPotion.instance());
    	registry.register(MagicShieldPotion.instance());
    	registry.register(FrostbitePotion.instance());
    	registry.register(MagicBoostPotion.instance());
    	registry.register(MagicBuffPotion.instance());
    	registry.register(FamiliarPotion.instance());
    	registry.register(LightningChargePotion.instance());
    	registry.register(LightningAttackPotion.instance());
    }
    
    private List<Item> blockItemsToRegister = new ArrayList<>();
    
    @SubscribeEvent
    private void registerItems(RegistryEvent.Register<Item> event) {
    	final IForgeRegistry<Item> registry = event.getRegistry();
    	
    	NostrumGuide.instance().setRegistryName(NostrumMagica.MODID, NostrumGuide.id);
    	registry.register(NostrumGuide.instance());
    	NostrumGuide.init();
    	
    	SpellcraftGuide.instance().setRegistryName(NostrumMagica.MODID, SpellcraftGuide.id);
    	registry.register(SpellcraftGuide.instance());
    	SpellcraftGuide.init();
    	
    	SpellTome.instance().setRegistryName(NostrumMagica.MODID, SpellTome.id);
    	registry.register(SpellTome.instance());
    	SpellPlate.instance().setRegistryName(NostrumMagica.MODID, SpellPlate.id);
    	registry.register(SpellPlate.instance());
    	BlankScroll.instance().setRegistryName(NostrumMagica.MODID, BlankScroll.id);
    	registry.register(BlankScroll.instance());
    	BlankScroll.init();
    	
    	SpellScroll.instance().setRegistryName(NostrumMagica.MODID, SpellScroll.id);
    	registry.register(SpellScroll.instance());
    	SpellScroll.init();
    	
    	SpellTableItem.instance().setRegistryName(NostrumMagica.MODID, SpellTableItem.ID);
    	registry.register(SpellTableItem.instance());
    	SpellTableItem.init();
    	
    	MirrorItem.instance().setRegistryName(NostrumMagica.MODID, MirrorItem.ID);
    	registry.register(MirrorItem.instance());
    	MirrorItem.init();
    	
    	MagicSwordBase.init();
    	MagicArmorBase.init();
    	EnchantedWeapon.registerWeapons();
    	EnchantedArmor.registerArmors();
    	DragonArmor.registerArmors();
    	
    	MirrorShield.instance().setRegistryName(NostrumMagica.MODID, MirrorShield.id);
    	registry.register(MirrorShield.instance());
    	
    	MirrorShieldImproved.instance().setRegistryName(NostrumMagica.MODID, MirrorShieldImproved.id);
    	registry.register(MirrorShieldImproved.instance());
    	
    	ReagentItem.instance().setRegistryName(NostrumMagica.MODID, ReagentItem.ID);
    	registry.register(ReagentItem.instance());
    	ReagentItem.init();
    	InfusedGemItem.instance().setRegistryName(NostrumMagica.MODID, InfusedGemItem.ID);
    	registry.register(InfusedGemItem.instance());
    	InfusedGemItem.init();
    	SpellRune.instance().setRegistryName(NostrumMagica.MODID, SpellRune.ID);
    	registry.register(SpellRune.instance());
    	SpellRune.init();
    	
    	NostrumResourceItem.instance().setRegistryName(NostrumMagica.MODID, NostrumResourceItem.ID);
    	registry.register(NostrumResourceItem.instance());
    	NostrumResourceItem.init();
    	
    	ReagentBag.instance().setRegistryName(NostrumMagica.MODID, ReagentBag.id);
    	registry.register(ReagentBag.instance());
    	ReagentBag.init();
    	
    	SeekerIdol.instance().setRegistryName(NostrumMagica.MODID, SeekerIdol.id);
    	registry.register(SeekerIdol.instance());
    	SeekerIdol.init();
    	
    	ShrineSeekingGem.instance().setRegistryName(NostrumMagica.MODID, ShrineSeekingGem.id);
    	registry.register(ShrineSeekingGem.instance());
    	
    	ChalkItem.instance().setRegistryName(NostrumMagica.MODID, ChalkItem.ID);
    	registry.register(ChalkItem.instance());
    	ChalkItem.init();
    	
    	AltarItem.instance().setRegistryName(NostrumMagica.MODID, AltarItem.ID);
    	registry.register(AltarItem.instance());
    	AltarItem.init();
    	
    	PositionCrystal.instance().setRegistryName(NostrumMagica.MODID, PositionCrystal.ID);
    	registry.register(PositionCrystal.instance());
    	PositionCrystal.init();
    	
    	PositionToken.instance().setRegistryName(NostrumMagica.MODID, PositionToken.ID);
    	registry.register(PositionToken.instance());
    	PositionToken.init();
    	
    	SpellTomePage.instance().setRegistryName(NostrumMagica.MODID, SpellTomePage.id);
    	registry.register(SpellTomePage.instance());
    	SpellTomePage.init();
    	
    	EssenceItem.instance().setRegistryName(NostrumMagica.MODID, EssenceItem.ID);
    	registry.register(EssenceItem.instance());
    	EssenceItem.init();
    	
    	MageStaff.instance().setRegistryName(NostrumMagica.MODID, MageStaff.ID);
    	registry.register(MageStaff.instance());
    	MageStaff.init();
    	
    	ThanoPendant.instance().setRegistryName(NostrumMagica.MODID, ThanoPendant.ID);
    	registry.register(ThanoPendant.instance());
    	
    	ThanosStaff.instance().setRegistryName(NostrumMagica.MODID, ThanosStaff.ID);
    	registry.register(ThanosStaff.instance());
    	ThanosStaff.init();
    	
    	MagicCharm.instance().setRegistryName(NostrumMagica.MODID, MagicCharm.ID);
    	registry.register(MagicCharm.instance());
    	
    	RuneBag.instance().setRegistryName(NostrumMagica.MODID, RuneBag.id);
    	registry.register(RuneBag.instance());
    	RuneBag.init();
    	
    	DragonEggFragment.instance().setRegistryName(NostrumMagica.MODID, DragonEggFragment.id);
    	registry.register(DragonEggFragment.instance());
    	DragonEggFragment.init();
    	
    	DragonEgg.instance().setRegistryName(NostrumMagica.MODID, DragonEgg.ID);
    	registry.register(DragonEgg.instance());
    	DragonEgg.init();
    	
    	NostrumSkillItem.instance().setRegistryName(NostrumMagica.MODID, NostrumSkillItem.ID);
    	registry.register(NostrumSkillItem.instance());
    	//NostrumSkillItem.init();
    	
    	NostrumRoseItem.instance().setRegistryName(NostrumMagica.MODID, NostrumRoseItem.ID);
    	registry.register(NostrumRoseItem.instance());
    	
    	WarlockSword.instance().setRegistryName(NostrumMagica.MODID, WarlockSword.ID);
    	registry.register(WarlockSword.instance());
    	WarlockSword.init();
    	
    	HookshotItem.instance().setRegistryName(NostrumMagica.MODID, HookshotItem.ID);
    	registry.register(HookshotItem.instance());
    	HookshotItem.init();
    	
    	ReagentSeed.mandrake.setRegistryName(NostrumMagica.MODID, ReagentSeed.mandrake.getItemID());
    	registry.register(ReagentSeed.mandrake);
    	ReagentSeed.ginseng.setRegistryName(NostrumMagica.MODID, ReagentSeed.ginseng.getItemID());
    	registry.register(ReagentSeed.ginseng);
    	ReagentSeed.essence.setRegistryName(NostrumMagica.MODID, ReagentSeed.essence.getItemID());
    	registry.register(ReagentSeed.essence);
    	
    	MasteryOrb.instance().setRegistryName(MasteryOrb.id);
    	registry.register(MasteryOrb.instance());
    	
    	for (Item item : blockItemsToRegister) {
    		registry.register(item);
    	}
    	
    	String[] variants = new String[DungeonBlock.Type.values().length];
    	for (DungeonBlock.Type type : DungeonBlock.Type.values()) {
    		variants[type.ordinal()] = type.getName().toLowerCase();
    	}
    	registry.register(
    			(new ItemMultiTexture(DungeonBlock.instance(), DungeonBlock.instance(), variants))
    			.setRegistryName(DungeonBlock.ID)
    		.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(DungeonBlock.ID));
    }
    
    private void registerBlock(Block block, String registryName, IForgeRegistry<Block> registry) {
    	block.setRegistryName(registryName);
    	registry.register(block);
    }
    
    private void registerBlockAndItemBlock(Block block, String registryName, @Nullable CreativeTabs tab, IForgeRegistry<Block> registry) {
    	registerBlock(block, registryName, registry);
    	
    	ItemBlock item = new ItemBlock(block);
    	item.setRegistryName(registryName);
    	item.setUnlocalizedName(registryName);
    	item.setCreativeTab(tab == null ? NostrumMagica.creativeTab : tab);
    	blockItemsToRegister.add(item);
    }
    
    private void registerBlockAndItemBlock(Block block, String registryName, IForgeRegistry<Block> registry) {
    	registerBlockAndItemBlock(block, registryName, null, registry);
    }
    
    @SubscribeEvent
    private void registerBlocks(RegistryEvent.Register<Block> event) {
    	final IForgeRegistry<Block> registry = event.getRegistry();
    	
    	registerBlockAndItemBlock(MagicWall.instance(), MagicWall.ID, registry);
    	registerBlockAndItemBlock(CursedIce.instance(), CursedIce.ID, registry);
    	registerBlockAndItemBlock(ManiOre.instance(), ManiOre.ID, registry);
    	registerBlockAndItemBlock(MagicDirt.instance(), MagicDirt.ID, registry);
    	registerBlock(SpellTable.instance(), SpellTable.ID, registry);
    	registerBlock(NostrumMagicaFlower.instance(), NostrumMagicaFlower.ID, registry);
    	registerBlock(CropMandrakeRoot.instance(), CropMandrakeRoot.ID, registry);
    	registerBlock(CropGinseng.instance(), CropGinseng.ID, registry);
    	registerBlockAndItemBlock(NostrumSingleSpawner.instance(), NostrumSingleSpawner.ID, registry);
    	registerBlockAndItemBlock(NostrumSpawnAndTrigger.instance(), NostrumSpawnAndTrigger.ID, registry);
    	
    	// DungeonBlock item variants registered by hand in item register method
    	registerBlock(DungeonBlock.instance(), DungeonBlock.ID, registry);
    	
    	registerBlock(SymbolBlock.instance(), SymbolBlock.ID, registry);
    	registerBlock(ShrineBlock.instance(), ShrineBlock.ID, registry);
    	registerBlock(NostrumMirrorBlock.instance(), NostrumMirrorBlock.ID, registry);
    	registerBlock(ChalkBlock.instance(), ChalkBlock.ID, registry);
    	registerBlock(AltarBlock.instance(), AltarBlock.ID, registry);
    	registerBlockAndItemBlock(Candle.instance(), Candle.ID, registry);
    	registerBlock(NostrumObelisk.instance(), NostrumObelisk.ID, registry);
    	registerBlock(ObeliskPortal.instance(), ObeliskPortal.ID, registry);
    	registerBlockAndItemBlock(EssenceOre.instance(), EssenceOre.ID, registry);
    	registerBlockAndItemBlock(ModificationTable.instance(), ModificationTable.ID, registry);
    	registerBlockAndItemBlock(LoreTable.instance(), LoreTable.ID, registry);
    	registerBlockAndItemBlock(SorceryPortal.instance(), SorceryPortal.ID, registry);
    	registerBlock(TeleportationPortal.instance(), TeleportationPortal.ID, registry);
    	registerBlock(TemporaryTeleportationPortal.instance(), TemporaryTeleportationPortal.ID, registry);
    	registerBlockAndItemBlock(ProgressionDoor.instance(), ProgressionDoor.ID, registry);
    	registerBlockAndItemBlock(LogicDoor.instance(), LogicDoor.ID, registry);
    	registerBlockAndItemBlock(SwitchBlock.instance(), SwitchBlock.ID, registry);
    	registerBlock(SorceryPortalSpawner.instance(), SorceryPortalSpawner.ID, registry);
    	registerBlock(ManiCrystal.instance(), ManiCrystal.ID, registry);
    	registerBlockAndItemBlock(MimicBlock.door(), MimicBlock.ID_DOOR, registry);
    	registerBlockAndItemBlock(MimicBlock.facade(), MimicBlock.ID_FACADE, registry);
    	registerBlockAndItemBlock(TeleportRune.instance(), TeleportRune.ID, registry);
    	registerBlockAndItemBlock(PutterBlock.instance(), PutterBlock.ID, registry);
    	registerBlock(CropEssence.instance(), CropEssence.ID, registry);
    	registerBlockAndItemBlock(ActiveHopper.instance, ActiveHopper.ID, registry);
    	registerBlockAndItemBlock(ItemDuct.instance, ItemDuct.ID, registry);
    	
    	MagicDirt.init();
    	Candle.init();
    	
    	registerTileEntities();
    	

    	
    	// These ItemBlocks were setting setHasSubtypes. I think it's useless tho and can beignored? Confirm. #TODO DONOTCHECKIN
//    	registry.register(MimicBlock.door(),
//    			new ResourceLocation(NostrumMagica.MODID, MimicBlock.ID_DOOR));
//    	registry.register(
//    			(new ItemBlock(MimicBlock.door()).setRegistryName(MimicBlock.ID_DOOR)
//    					.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(MimicBlock.ID_DOOR).setHasSubtypes(true))
//    			);
//    	
//    	registry.register(MimicBlock.facade(),
//    			new ResourceLocation(NostrumMagica.MODID, MimicBlock.ID_FACADE));
//    	registry.register(
//    			(new ItemBlock(MimicBlock.facade()).setRegistryName(MimicBlock.ID_FACADE)
//    					.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(MimicBlock.ID_FACADE).setHasSubtypes(true))
//    			);
    }
    
    private void registerTileEntities() {
    	GameRegistry.registerTileEntity(SpellTableEntity.class, new ResourceLocation(NostrumMagica.MODID, "spell_table"));
    	GameRegistry.registerTileEntity(SingleSpawnerTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "nostrum_mob_spawner_te"));
    	GameRegistry.registerTileEntity(SpawnerTriggerTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "nostrum_mob_spawner_trigger_te"));
    	GameRegistry.registerTileEntity(SymbolTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "nostrum_symbol_te"));
    	GameRegistry.registerTileEntity(AltarTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "nostrum_altar_te"));
    	GameRegistry.registerTileEntity(CandleTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "nostrum_candle_te"));
    	GameRegistry.registerTileEntity(NostrumObeliskEntity.class, new ResourceLocation(NostrumMagica.MODID, "nostrum_obelisk"));
    	GameRegistry.registerTileEntity(ObeliskPortalTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "obelisk_portal"));
    	GameRegistry.registerTileEntity(ModificationTableEntity.class, new ResourceLocation(NostrumMagica.MODID, "modification_table"));
    	GameRegistry.registerTileEntity(LoreTableEntity.class, new ResourceLocation(NostrumMagica.MODID, "lore_table"));
    	GameRegistry.registerTileEntity(SorceryPortalTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "sorcery_portal"));
    	GameRegistry.registerTileEntity(TeleportationPortalTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "teleportation_portal"));
    	GameRegistry.registerTileEntity(TemporaryPortalTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "limited_teleportation_portal"));
    	GameRegistry.registerTileEntity(ProgressionDoorTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "progression_door"));
    	GameRegistry.registerTileEntity(SwitchBlockTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "switch_block_tile_entity"));
    	GameRegistry.registerTileEntity(TeleportRuneTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "teleport_rune"));
    	GameRegistry.registerTileEntity(PutterBlockTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "putter_entity"));
    	GameRegistry.registerTileEntity(ActiveHopperTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "active_hopper_te"));
    	GameRegistry.registerTileEntity(ItemDuctTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "item_duct_te"));
    }
    
    @SubscribeEvent
    private void registerEntities(RegistryEvent.Register<EntityEntry> event) {
    	int entityID = 0;
    	final IForgeRegistry<EntityEntry> registry = event.getRegistry();
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntitySpellProjectile.class)
    			.id("spell_projectile", entityID++)
    			.name("spell_projectile")
    			.tracker(64, 1, true)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityGolemPhysical.class)
    			.id("physical_golem", entityID++)
    			.name("physical_golem")
    			.tracker(64, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityGolemLightning.class)
    			.id("lightning_golem", entityID++)
    			.name("lightning_golem")
    			.tracker(64, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityGolemFire.class)
    			.id("fire_golem", entityID++)
    			.name("fire_golem")
    			.tracker(64, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityGolemEarth.class)
    			.id("earth_golem", entityID++)
    			.name("earth_golem")
    			.tracker(64, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityGolemIce.class)
    			.id("ice_golem", entityID++)
    			.name("ice_golem")
    			.tracker(64, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityGolemWind.class)
    			.id("wind_golem", entityID++)
    			.name("wind_golem")
    			.tracker(64, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityGolemEnder.class)
    			.id("ender_golem", entityID++)
    			.name("ender_golem")
    			.tracker(64, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityKoid.class)
    			.id("entity_koid", entityID++)
    			.name("entity_koid")
    			.tracker(64, 1, false)
    			.spawn(EnumCreatureType.MONSTER, 20, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.MAGICAL))
    			.spawn(EnumCreatureType.MONSTER, 20, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.FOREST))
				.spawn(EnumCreatureType.MONSTER, 20, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.SNOWY))
				.spawn(EnumCreatureType.MONSTER, 20, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.NETHER))
				.spawn(EnumCreatureType.MONSTER, 20, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.SPOOKY))
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityDragonRed.class)
    			.id("entity_dragon_red", entityID++)
    			.name("entity_dragon_red")
    			.tracker(128, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityTameDragonRed.class)
    			.id("entity_tame_dragon_red", entityID++)
    			.name("entity_tame_dragon_red")
    			.tracker(128, 1, false)
    			.spawn(EnumCreatureType.MONSTER, 2, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.NETHER))
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityShadowDragonRed.class)
    			.id("entity_shadow_dragon_red", entityID++)
    			.name("entity_shadow_dragon_red")
    			.tracker(128, 1, false)
    			.spawn(EnumCreatureType.MONSTER, 15, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.NETHER))
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntitySprite.class)
    			.id("entity_sprite", entityID++)
    			.name("entity_sprite")
    			.tracker(64, 1, false)
    			.spawn(EnumCreatureType.MONSTER, 15, 1, 3, ForgeRegistries.BIOMES.getValuesCollection().stream().filter( (biome) -> {
    				return !BiomeDictionary.hasType(biome, BiomeDictionary.Type.END)
    						&& BiomeDictionary.hasType(biome, BiomeDictionary.Type.NETHER);
    			}).collect(Collectors.toList()))
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntitySpellBullet.class)
    			.id("spell_bullet", entityID++)
    			.name("spell_bullet")
    			.tracker(64, 1, true)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityLux.class)
    			.id("entity_lux", entityID++)
    			.name("entity_lux")
    			.tracker(64, 1, false)
    			.spawn(EnumCreatureType.CREATURE, 6, 1, 3, BiomeDictionary.getBiomes(BiomeDictionary.Type.MAGICAL))
    			.spawn(EnumCreatureType.CREATURE, 6, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.FOREST))
				.spawn(EnumCreatureType.CREATURE, 4, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.JUNGLE))
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityDragonEgg.class)
    			.id("entity_dragon_egg", entityID++)
    			.name("entity_dragon_egg")
    			.tracker(64, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityChakramSpellSaucer.class)
    			.id("entity_internal_spellsaucer_chakram", entityID++)
    			.name("entity_internal_spellsaucer_chakram")
    			.tracker(64, 1, true)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityCyclerSpellSaucer.class)
    			.id("entity_internal_spellsaucer_cycler", entityID++)
    			.name("entity_internal_spellsaucer_cycler")
    			.tracker(64, 1, true)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntitySwitchTrigger.class)
    			.id("entity_switch_trigger", entityID++)
    			.name("entity_switch_trigger")
    			.tracker(128, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(NostrumTameLightning.class)
    			.id("nostrum_lightning", entityID++)
    			.name("nostrum_lightning")
    			.tracker(128, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityHookShot.class)
    			.id("nostrum_hookshot", entityID++)
    			.name("nostrum_hookshot")
    			.tracker(128, 1, true)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityWisp.class)
    			.id("entity_wisp", entityID++)
    			.name("entity_wisp")
    			.tracker(64, 1, false)
    			.spawn(EnumCreatureType.AMBIENT, 1, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.MAGICAL))
    			.spawn(EnumCreatureType.AMBIENT, 1, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.FOREST))
    			.spawn(EnumCreatureType.AMBIENT, 1, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.SNOWY))
    			.spawn(EnumCreatureType.AMBIENT, 1, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.SPOOKY))
    			.spawn(EnumCreatureType.MONSTER, 13, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.NETHER))
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityAreaEffect.class)
    			.id("entity_effect_cloud", entityID++)
    			.name("entity_effect_cloud")
    			.tracker(64, 1, false)
    		.build());
    }
    
    @SubscribeEvent
    private void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
    	event.getRegistry().register(EnchantmentManaRecovery.instance());
    }
    
    @SubscribeEvent
    private void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		for (NostrumMagicaSounds sound : NostrumMagicaSounds.values()) {
			event.getRegistry().register(sound.getEvent());
		}
    }
    
    public void syncPlayer(EntityPlayerMP player) {
    	INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
    	attr.refresh(player);
    	NetworkHandler.getSyncChannel().sendTo(
    			new StatSyncMessage(NostrumMagica.getMagicWrapper(player)),
    			player);
    	NetworkHandler.getSyncChannel().sendTo(
    			new SpellRequestReplyMessage(NostrumMagica.getSpellRegistry().getAllSpells(), true),
    			player);
    }
    
    public void updatePlayerEffect(EntityPlayerMP player, SpecialEffect effectType, EffectData data) {
    	NetworkHandler.getSyncChannel().sendTo(
    			new MagicEffectUpdate(effectType, data),
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
	
	public void openDragonGUI(EntityPlayer player, ITameDragon dragon) {
		// This code is largely taken from FMLNetworkHandler's openGui method, but we use our own message to open the GUI on the client
		EntityPlayerMP mpPlayer = (EntityPlayerMP) player;
		DragonContainer container = dragon.getGUIContainer(player);
		mpPlayer.getNextWindowId();
		mpPlayer.closeContainer();
        int windowId = mpPlayer.currentWindowId;
        mpPlayer.openContainer = container;
        mpPlayer.openContainer.windowId = windowId;
        mpPlayer.openContainer.addListener(mpPlayer);
        
        // Open GUI on client
        TamedDragonGUIOpenMessage message = new TamedDragonGUIOpenMessage(dragon, windowId, container.getContainerID(), container.getSheetCount());
        NetworkHandler.getSyncChannel().sendTo(message, mpPlayer);
	}

	public void sendServerConfig(EntityPlayerMP player) {
		ModConfig.channel.sendTo(new ServerConfigMessage(ModConfig.config), player);
	}
	
	public void sendSpellDebug(EntityPlayer player, ITextComponent comp) {
		NetworkHandler.getSyncChannel().sendTo(new SpellDebugMessage(comp), (EntityPlayerMP) player);
	}
	
	public String getTranslation(String key) {
		return key; // This is the server, silly!
	}
	
	public void requestObeliskTransportation(BlockPos origin, BlockPos target) {
		; // server does nothing
	}
	
	public void setObeliskIndex(BlockPos obeliskPos, int index) {
		; // server does nothing
	}
	
	public void requestStats(EntityLivingBase entity) {
		;
	}
	
	/**
	 * Spawns an client-rendered effect.
	 * @param world Only needed if caster and target are null
	 * @param comp
	 * @param caster
	 * @param casterPos
	 * @param target
	 * @param targetPos
	 * @param flavor Optional component used to flavor the effect.
	 * @param negative whether the effect should be considered 'negative'/harmful
	 * @param param optional extra float param for display
	 */
	public void spawnEffect(World world, SpellComponentWrapper comp,
			EntityLivingBase caster, Vec3d casterPos,
			EntityLivingBase target, Vec3d targetPos,
			SpellComponentWrapper flavor, boolean isNegative, float compParam) {
		if (world == null) {
			if (caster == null)
				world = target.world; // If you NPE here you suck. Supply a world!
			else
				world = caster.world;
		}
		
		final double MAX_RANGE_SQR = 2500.0;
		
		Set<EntityPlayer> players = new HashSet<>();
		
		if (caster != null) {
			//caster.addTrackingPlayer(player);
			players.addAll(((WorldServer) world).getEntityTracker()
				.getTrackingPlayers(caster));
		}
		
		if (target != null) {
			//caster.addTrackingPlayer(player);
			players.addAll(((WorldServer) world).getEntityTracker()
				.getTrackingPlayers(target));
		}
		
		if (caster != null && caster == target && caster instanceof EntityPlayer) {
			// Very specific case here
			players.add((EntityPlayer) caster);
		}
		
		if (players.isEmpty()) {
			// Fall back to distance check against locations
			if (casterPos != null) {
				for (EntityPlayer player : world.playerEntities) {
					if (player.getDistanceSq(casterPos.x, casterPos.y, casterPos.z) <= MAX_RANGE_SQR)
						players.add(player);
				}
			}
			
			if (targetPos != null) {
				for (EntityPlayer player : world.playerEntities) {
					if (player.getDistanceSq(targetPos.x, targetPos.y, targetPos.z) <= MAX_RANGE_SQR)
						players.add(player);
				}
			}
		}
		
		if (!players.isEmpty()) {
			ClientEffectRenderMessage message = new ClientEffectRenderMessage(
					caster, casterPos,
					target, targetPos,
					comp, flavor,
					isNegative, compParam);
			for (EntityPlayer player : players) {
				NetworkHandler.getSyncChannel().sendTo(message, (EntityPlayerMP) player);
			}
		}
	}
	
	public void sendMana(EntityPlayer player) {
		EntityTracker tracker = ((WorldServer) player.world).getEntityTracker();
		if (tracker == null)
			return;
		
		INostrumMagic stats = NostrumMagica.getMagicWrapper(player);
		final int mana;
		if (stats == null) {
			mana = 0;
		} else {
			mana = stats.getMana();
		}
		
		tracker.sendToTrackingAndSelf(player, NetworkHandler.getSyncChannel()
				.getPacketFrom(new ManaMessage(player, mana)));
	}
}
