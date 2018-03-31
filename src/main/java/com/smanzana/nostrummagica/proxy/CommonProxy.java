package com.smanzana.nostrummagica.proxy;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.AltarBlock;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.blocks.ChalkBlock;
import com.smanzana.nostrummagica.blocks.CropGinseng;
import com.smanzana.nostrummagica.blocks.CropMandrakeRoot;
import com.smanzana.nostrummagica.blocks.CursedIce;
import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.blocks.MagicWall;
import com.smanzana.nostrummagica.blocks.ManiOre;
import com.smanzana.nostrummagica.blocks.NostrumMagicaFlower;
import com.smanzana.nostrummagica.blocks.NostrumMirrorBlock;
import com.smanzana.nostrummagica.blocks.NostrumObelisk;
import com.smanzana.nostrummagica.blocks.NostrumSingleSpawner;
import com.smanzana.nostrummagica.blocks.ShrineBlock;
import com.smanzana.nostrummagica.blocks.SpellTable;
import com.smanzana.nostrummagica.blocks.SymbolBlock;
import com.smanzana.nostrummagica.capabilities.CapabilityHandler;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagicStorage;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.config.network.ServerConfigMessage;
import com.smanzana.nostrummagica.enchantments.EnchantmentManaRecovery;
import com.smanzana.nostrummagica.entity.EntityGolemEarth;
import com.smanzana.nostrummagica.entity.EntityGolemEnder;
import com.smanzana.nostrummagica.entity.EntityGolemFire;
import com.smanzana.nostrummagica.entity.EntityGolemIce;
import com.smanzana.nostrummagica.entity.EntityGolemLightning;
import com.smanzana.nostrummagica.entity.EntityGolemPhysical;
import com.smanzana.nostrummagica.entity.EntityGolemWind;
import com.smanzana.nostrummagica.entity.EntityKoid;
import com.smanzana.nostrummagica.entity.EntitySpellProjectile;
import com.smanzana.nostrummagica.items.AltarItem;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.ChalkItem;
import com.smanzana.nostrummagica.items.EnchantedArmor;
import com.smanzana.nostrummagica.items.EnchantedWeapon;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.MagicArmorBase;
import com.smanzana.nostrummagica.items.MagicSwordBase;
import com.smanzana.nostrummagica.items.MirrorItem;
import com.smanzana.nostrummagica.items.NostrumGuide;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.items.PositionToken;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.SeekerIdol;
import com.smanzana.nostrummagica.items.ShrineSeekingGem;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTableItem;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.items.SpellTomePage;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.SpellDebugMessage;
import com.smanzana.nostrummagica.network.messages.SpellRequestReplyMessage;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;
import com.smanzana.nostrummagica.potions.FrostbitePotion;
import com.smanzana.nostrummagica.potions.MagicBoostPotion;
import com.smanzana.nostrummagica.potions.MagicResistPotion;
import com.smanzana.nostrummagica.potions.MagicShieldPotion;
import com.smanzana.nostrummagica.potions.PhysicalShieldPotion;
import com.smanzana.nostrummagica.potions.RootedPotion;
import com.smanzana.nostrummagica.quests.NostrumQuest;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
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
import com.smanzana.nostrummagica.spells.components.triggers.ManaTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.OtherTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProximityTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.TouchTrigger;
import com.smanzana.nostrummagica.world.NostrumFlowerGenerator;
import com.smanzana.nostrummagica.world.NostrumOreGenerator;
import com.smanzana.nostrummagica.world.NostrumShrineGenerator;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.network.NetworkRegistry;
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
    	EntityRegistry.registerModEntity(EntityKoid.class, "entity_koid",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			false);
    	EntityRegistry.addSpawn(EntityKoid.class, 4, 1, 1, EnumCreatureType.MONSTER, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.MAGICAL));
    	EntityRegistry.addSpawn(EntityKoid.class, 4, 1, 1, EnumCreatureType.MONSTER, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.HOT));
    	EntityRegistry.addSpawn(EntityKoid.class, 4, 1, 1, EnumCreatureType.MONSTER, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.WET));
    	EntityRegistry.addSpawn(EntityKoid.class, 4, 1, 1, EnumCreatureType.MONSTER, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.NETHER));
	}
	
	public void init() {
    	registerPotions();
    	registerItems();
    	registerBlocks();
    	
    	GameRegistry.register(EnchantmentManaRecovery.instance(),
    			new ResourceLocation(NostrumMagica.MODID, EnchantmentManaRecovery.ID));
    	
    	GameRegistry.registerWorldGenerator(new NostrumOreGenerator(), 0);
    	GameRegistry.registerWorldGenerator(new NostrumFlowerGenerator(), 0);
    	GameRegistry.registerWorldGenerator(new NostrumShrineGenerator(), 0);
    	
    	NetworkRegistry.INSTANCE.registerGuiHandler(NostrumMagica.instance, new NostrumGui());
	}
	
	public void postinit() {
		for (Biome biome : Biome.REGISTRY) {
			biome.addFlower(NostrumMagicaFlower.instance().getState(NostrumMagicaFlower.Type.MIDNIGHT_IRIS), 8);
			biome.addFlower(NostrumMagicaFlower.instance().getState(NostrumMagicaFlower.Type.CRYSTABLOOM), 7);
		}
		
		NostrumQuest.Validate();
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
    	
    	NostrumGuide.instance().setRegistryName(NostrumMagica.MODID, NostrumGuide.id);
    	GameRegistry.register(NostrumGuide.instance());
    	NostrumGuide.init();
    	
    	BlankScroll.instance().setRegistryName(NostrumMagica.MODID, BlankScroll.id);
    	GameRegistry.register(BlankScroll.instance());
    	BlankScroll.init();
    	
    	SpellScroll.instance().setRegistryName(NostrumMagica.MODID, SpellScroll.id);
    	GameRegistry.register(SpellScroll.instance());
    	
    	SpellTableItem.instance().setRegistryName(NostrumMagica.MODID, SpellTableItem.ID);
    	GameRegistry.register(SpellTableItem.instance());
    	SpellTableItem.init();
    	
    	MirrorItem.instance().setRegistryName(NostrumMagica.MODID, MirrorItem.ID);
    	GameRegistry.register(MirrorItem.instance());
    	MirrorItem.init();
    	
    	MagicSwordBase.init();
    	MagicArmorBase.init();
    	EnchantedWeapon.registerWeapons();
    	EnchantedArmor.registerArmors();
    	
    	ReagentItem.instance().setRegistryName(NostrumMagica.MODID, ReagentItem.ID);
    	GameRegistry.register(ReagentItem.instance());
    	ReagentItem.init();
    	InfusedGemItem.instance().setRegistryName(NostrumMagica.MODID, InfusedGemItem.ID);
    	GameRegistry.register(InfusedGemItem.instance());
    	InfusedGemItem.init();
    	SpellRune.instance().setRegistryName(NostrumMagica.MODID, SpellRune.ID);
    	GameRegistry.register(SpellRune.instance());
    	SpellRune.init();
    	
    	NostrumResourceItem.instance().setRegistryName(NostrumMagica.MODID, NostrumResourceItem.ID);
    	GameRegistry.register(NostrumResourceItem.instance());
    	NostrumResourceItem.init();
    	
    	ReagentBag.instance().setRegistryName(NostrumMagica.MODID, ReagentBag.id);
    	GameRegistry.register(ReagentBag.instance());
    	ReagentBag.init();
    	
    	SeekerIdol.instance().setRegistryName(NostrumMagica.MODID, SeekerIdol.id);
    	GameRegistry.register(SeekerIdol.instance());
    	SeekerIdol.init();
    	
    	ShrineSeekingGem.instance().setRegistryName(NostrumMagica.MODID, ShrineSeekingGem.id);
    	GameRegistry.register(ShrineSeekingGem.instance());
    	ShrineSeekingGem.init();
    	
    	ChalkItem.instance().setRegistryName(NostrumMagica.MODID, ChalkItem.ID);
    	GameRegistry.register(ChalkItem.instance());
    	ChalkItem.init();
    	
    	AltarItem.instance().setRegistryName(NostrumMagica.MODID, AltarItem.ID);
    	GameRegistry.register(AltarItem.instance());
    	AltarItem.init();
    	
    	PositionCrystal.instance().setRegistryName(NostrumMagica.MODID, PositionCrystal.ID);
    	GameRegistry.register(PositionCrystal.instance());
    	PositionCrystal.init();
    	
    	PositionToken.instance().setRegistryName(NostrumMagica.MODID, PositionToken.ID);
    	GameRegistry.register(PositionToken.instance());
    	PositionToken.init();
    	
    	SpellTomePage.instance().setRegistryName(NostrumMagica.MODID, SpellTomePage.id);
    	GameRegistry.register(SpellTomePage.instance());
    	
    	EssenceItem.instance().setRegistryName(NostrumMagica.MODID, EssenceItem.ID);
    	GameRegistry.register(EssenceItem.instance());
    }
    
    private void registerBlocks() {
    	GameRegistry.register(MagicWall.instance(),
    			new ResourceLocation(NostrumMagica.MODID, MagicWall.ID));
    	GameRegistry.register(
    			(new ItemBlock(MagicWall.instance())).setRegistryName(MagicWall.ID)
    		.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(MagicWall.ID));

    	GameRegistry.register(CursedIce.instance(),
    			new ResourceLocation(NostrumMagica.MODID, CursedIce.ID));
    	GameRegistry.register(
    			(new ItemBlock(CursedIce.instance())).setRegistryName(CursedIce.ID)
    		.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(CursedIce.ID));
    	
    	GameRegistry.register(ManiOre.instance(),
    			new ResourceLocation(NostrumMagica.MODID, ManiOre.ID));
    	GameRegistry.register(
    			(new ItemBlock(ManiOre.instance())).setRegistryName(ManiOre.ID)
    		.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(ManiOre.ID));
    	
    	GameRegistry.register(SpellTable.instance(),
    			new ResourceLocation(NostrumMagica.MODID, SpellTable.ID));
    	SpellTable.init();
    	
    	NostrumMagicaFlower.init();
    	
    	GameRegistry.register(CropMandrakeRoot.instance(),
    			new ResourceLocation(NostrumMagica.MODID, CropMandrakeRoot.ID));
    	
    	GameRegistry.register(CropGinseng.instance(),
    			new ResourceLocation(NostrumMagica.MODID, CropGinseng.ID));
    	
    	GameRegistry.register(NostrumSingleSpawner.instance(),
    			new ResourceLocation(NostrumMagica.MODID, NostrumSingleSpawner.ID));
    	GameRegistry.register(
    			(new ItemBlock(NostrumSingleSpawner.instance())).setRegistryName(NostrumSingleSpawner.ID)
    		.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(NostrumSingleSpawner.ID));
    	NostrumSingleSpawner.init();
    	
    	GameRegistry.register(DungeonBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, DungeonBlock.ID));
    	GameRegistry.register(
    			(new ItemBlock(DungeonBlock.instance())).setRegistryName(DungeonBlock.ID)
    		.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(DungeonBlock.ID));
    	
    	GameRegistry.register(SymbolBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, SymbolBlock.ID));
    	SymbolBlock.init();

    	GameRegistry.register(ShrineBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, ShrineBlock.ID));
    	
    	GameRegistry.register(NostrumMirrorBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, NostrumMirrorBlock.ID));
    	
    	GameRegistry.register(ChalkBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, ChalkBlock.ID));
    	
    	GameRegistry.register(AltarBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, AltarBlock.ID));
    	AltarBlock.init();
    	
    	GameRegistry.register(Candle.instance(),
    			new ResourceLocation(NostrumMagica.MODID, Candle.ID));
    	GameRegistry.register(
    			(new ItemBlock(Candle.instance())).setRegistryName(Candle.ID)
    		.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(Candle.ID));
    	Candle.init();
    	
    	GameRegistry.register(NostrumObelisk.instance(),
    			new ResourceLocation(NostrumMagica.MODID, NostrumObelisk.ID));
    	NostrumObelisk.init();
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
}
