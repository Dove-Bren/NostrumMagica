package com.smanzana.nostrummagica.proxy;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.capabilities.CapabilityHandler;
import com.smanzana.nostrummagica.capabilities.IManaArmor;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.ManaArmor;
import com.smanzana.nostrummagica.capabilities.ManaArmorStorage;
import com.smanzana.nostrummagica.capabilities.NostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagicStorage;
import com.smanzana.nostrummagica.client.effects.ClientPredefinedEffect.PredefinedEffect;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI;
import com.smanzana.nostrummagica.command.CommandAllQuests;
import com.smanzana.nostrummagica.command.CommandAllResearch;
import com.smanzana.nostrummagica.command.CommandCreateGeotoken;
import com.smanzana.nostrummagica.command.CommandDebugEffect;
import com.smanzana.nostrummagica.command.CommandEnhanceTome;
import com.smanzana.nostrummagica.command.CommandForceBind;
import com.smanzana.nostrummagica.command.CommandGiveResearchpoint;
import com.smanzana.nostrummagica.command.CommandGiveSkillpoint;
import com.smanzana.nostrummagica.command.CommandRandomSpell;
import com.smanzana.nostrummagica.command.CommandReadRoom;
import com.smanzana.nostrummagica.command.CommandReloadResearch;
import com.smanzana.nostrummagica.command.CommandSetDimension;
import com.smanzana.nostrummagica.command.CommandSetLevel;
import com.smanzana.nostrummagica.command.CommandSetManaArmor;
import com.smanzana.nostrummagica.command.CommandSpawnDungeon;
import com.smanzana.nostrummagica.command.CommandSpawnObelisk;
import com.smanzana.nostrummagica.command.CommandTestConfig;
import com.smanzana.nostrummagica.command.CommandUnlock;
import com.smanzana.nostrummagica.command.CommandUnlockAll;
import com.smanzana.nostrummagica.command.CommandWriteRoom;
import com.smanzana.nostrummagica.enchantments.EnchantmentManaRecovery;
import com.smanzana.nostrummagica.entity.IEntityPet;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ClientEffectRenderMessage;
import com.smanzana.nostrummagica.network.messages.MagicEffectUpdate;
import com.smanzana.nostrummagica.network.messages.ManaArmorSyncMessage;
import com.smanzana.nostrummagica.network.messages.ManaMessage;
import com.smanzana.nostrummagica.network.messages.SpawnNostrumRitualEffectMessage;
import com.smanzana.nostrummagica.network.messages.SpawnPredefinedEffectMessage;
import com.smanzana.nostrummagica.network.messages.SpellDebugMessage;
import com.smanzana.nostrummagica.network.messages.SpellRequestReplyMessage;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;
import com.smanzana.nostrummagica.quests.NostrumQuest;
import com.smanzana.nostrummagica.research.NostrumResearch;
import com.smanzana.nostrummagica.serializers.ArcaneWolfElementalTypeSerializer;
import com.smanzana.nostrummagica.serializers.DragonArmorMaterialSerializer;
import com.smanzana.nostrummagica.serializers.FloatArraySerializer;
import com.smanzana.nostrummagica.serializers.HookshotTypeDataSerializer;
import com.smanzana.nostrummagica.serializers.MagicElementDataSerializer;
import com.smanzana.nostrummagica.serializers.OptionalDragonArmorMaterialSerializer;
import com.smanzana.nostrummagica.serializers.OptionalMagicElementDataSerializer;
import com.smanzana.nostrummagica.serializers.OptionalParticleDataSerializer;
import com.smanzana.nostrummagica.serializers.PetJobSerializer;
import com.smanzana.nostrummagica.serializers.PlantBossTreeTypeSerializer;
import com.smanzana.nostrummagica.serializers.WilloStatusSerializer;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.ChainShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.AtFeetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.AuraTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.BeamTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.CasterTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.DamagedTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.DelayTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.FieldTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.FoodTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.HealthTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MagicCutterTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MagicCyclerTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ManaTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MortarTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.OtherTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProximityTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SeekingBulletTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.TouchTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.WallTrigger;
import com.smanzana.nostrummagica.utils.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructure.NostrumDungeonConfig;
import com.smanzana.nostrummagica.world.gen.NostrumFeatures;

import net.minecraft.command.CommandSource;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class CommonProxy {
	
	public CapabilityHandler capabilityHandler;
	
	public CommonProxy() {
		//MinecraftForge.EVENT_BUS.register(this);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}
	
	public void preinit() {
		//MinecraftForge.EVENT_BUS.register(this);
		
		NetworkHandler.getInstance();
		
		// Fluids registered with blocks
		//registerFluids();
		
    	registerShapes();
    	registerTriggers();
    	
    	EntityTameDragonRed.init();
    	
    	new NostrumItems();
    	new NostrumBlocks();
	}
	
	public void init() {
    	LoreRegistry.instance();
    	
    	CapabilityManager.INSTANCE.register(INostrumMagic.class, new NostrumMagicStorage(), NostrumMagic::new);
		CapabilityManager.INSTANCE.register(IManaArmor.class, new ManaArmorStorage(), ManaArmor::new);
		capabilityHandler = new CapabilityHandler();
	}
	
	public void postinit() {
		NostrumQuest.Validate();
		NostrumResearch.Validate();
	}
	
	@SubscribeEvent
	public void startup(FMLServerStartingEvent event) {
		final CommandDispatcher<CommandSource> dispatcher = event.getCommandDispatcher();
		
		CommandTestConfig.register(dispatcher);
		CommandTestConfig.register(dispatcher);
		CommandSpawnObelisk.register(dispatcher);
		CommandEnhanceTome.register(dispatcher);
		CommandSetLevel.register(dispatcher);
		CommandUnlock.register(dispatcher);
		CommandGiveSkillpoint.register(dispatcher);
		CommandAllQuests.register(dispatcher);
		CommandAllResearch.register(dispatcher);
		CommandCreateGeotoken.register(dispatcher);
		CommandForceBind.register(dispatcher);
		CommandSpawnDungeon.register(dispatcher);
		CommandUnlockAll.register(dispatcher);
		CommandSetDimension.register(dispatcher);
		CommandWriteRoom.register(dispatcher);
		CommandReadRoom.register(dispatcher);
		CommandGiveResearchpoint.register(dispatcher);
		CommandReloadResearch.register(dispatcher);
		CommandRandomSpell.register(dispatcher);
		CommandDebugEffect.register(dispatcher);
		CommandSetManaArmor.register(dispatcher);
	}
	
	public void registerWorldGen() {
		// Note: features registered in NostrumFeatures
		for(Biome biome : ForgeRegistries.BIOMES) {
			// Filter this list maybe?
			biome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(NostrumFeatures.flowers, IFeatureConfig.NO_FEATURE_CONFIG, Placement.COUNT_HEIGHTMAP_32, new FrequencyConfig(1)));
			biome.addFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, Biome.createDecoratedFeature(NostrumFeatures.dungeons, new NostrumDungeonConfig(), Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG));
			
			biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Biome.createDecoratedFeature(Feature.ORE, new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE, NostrumBlocks.maniOre.getDefaultState(), 9), Placement.COUNT_RANGE, new CountRangeConfig(20, 0, 0, 240)));
			biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Biome.createDecoratedFeature(Feature.ORE, new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE, NostrumBlocks.essenceOre.getDefaultState(), 3), Placement.COUNT_RANGE, new CountRangeConfig(10, 20, 20, 60)));
		}
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
    	SpellTrigger.register(WallTrigger.instance());
    	SpellTrigger.register(MortarTrigger.instance());
    	SpellTrigger.register(FieldTrigger.instance());
    	SpellTrigger.register(AtFeetTrigger.instance());
    	SpellTrigger.register(AuraTrigger.instance());
    	SpellTrigger.register(CasterTrigger.instance());
    }
    
    @SubscribeEvent
    public void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
    	event.getRegistry().register(EnchantmentManaRecovery.instance());
    }
    
    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		for (NostrumMagicaSounds sound : NostrumMagicaSounds.values()) {
			event.getRegistry().register(sound.getEvent());
		}
    }
    
    // Just have data files now.
//    @SubscribeEvent
//    public void registerCustomRecipes(RegistryEvent.Register<IRecipe> event) {
//    	RecipeProvider p;
//    	
//    	final IForgeRegistry<IRecipe> registry = event.getRegistry();
//    	
//    	registry.register(new SpellRune.RuneRecipe());
//    	registry.register(new SpellTomePageCombineRecipe());
//    }
    
    @SubscribeEvent
    public void registerDataSerializers(RegistryEvent.Register<DataSerializerEntry> event) {
    	final IForgeRegistry<DataSerializerEntry> registry = event.getRegistry();
    	
    	registry.register(new DataSerializerEntry(DragonArmorMaterialSerializer.instance).setRegistryName("nostrum.serial.dragon_armor"));
    	registry.register(new DataSerializerEntry(OptionalDragonArmorMaterialSerializer.instance).setRegistryName("nostrum.serial.dragon_armor_opt"));
    	registry.register(new DataSerializerEntry(MagicElementDataSerializer.instance).setRegistryName("nostrum.serial.element"));
    	registry.register(new DataSerializerEntry(HookshotTypeDataSerializer.instance).setRegistryName("nostrum.serial.hookshot_type"));
    	registry.register(new DataSerializerEntry(PetJobSerializer.instance).setRegistryName("nostrum.serial.pet_job"));
    	registry.register(new DataSerializerEntry(WilloStatusSerializer.instance).setRegistryName("nostrum.serial.willo_status"));
    	registry.register(new DataSerializerEntry(ArcaneWolfElementalTypeSerializer.instance).setRegistryName("nostrum.serial.arcane_wolf_type"));
    	registry.register(new DataSerializerEntry(FloatArraySerializer.instance).setRegistryName("nostrum.serial.float_array"));
    	registry.register(new DataSerializerEntry(OptionalMagicElementDataSerializer.instance).setRegistryName("nostrum.serial.element_opt"));
    	registry.register(new DataSerializerEntry(PlantBossTreeTypeSerializer.instance).setRegistryName("nostrum.serial.plantboss_tree_type"));
    	registry.register(new DataSerializerEntry(OptionalParticleDataSerializer.instance).setRegistryName("nostrum.serial.particle_opt"));
    }
    
//    @SubscribeEvent
//    public void registerFluids(RegistryEvent.Register<Fluid> event) {
//    	for (NostrumFluids fluid : NostrumFluids.values()) {
//    		FluidRegistry.registerFluid(fluid.getFluid());
//    	}
//    	
//    	NostrumFluids.POISON_WATER.getFluid().setBlock(new FluidPoisonWaterBlock((FluidPoisonWater) NostrumFluids.POISON_WATER.getFluid(), new MaterialLiquid(MapColor.GREEN_STAINED_HARDENED_CLAY) {
//			@Override
//			public boolean isReplaceable() {
//				return true;
//			}
//			
//			@Override
//			public boolean blocksMovement() {
//				return true; // so our liquids are not replaced by water
//			}
//		}, false));
//    	NostrumFluids.POISON_WATER_UNBREAKABLE.getFluid().setBlock(new FluidPoisonWaterBlock((FluidPoisonWater) NostrumFluids.POISON_WATER_UNBREAKABLE.getFluid(), new MaterialLiquid(MapColor.GREEN_STAINED_HARDENED_CLAY) {
//			@Override
//			public boolean isReplaceable() {
//				return false;
//			}
//			
//			@Override
//			public boolean blocksMovement() {
//				return true; // so our liquids are not replaced by water
//			}
//		}, true));
//    }
    
    public void syncPlayer(ServerPlayerEntity player) {
    	INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
    	attr.refresh(player);
    	NetworkHandler.sendTo(
    			new StatSyncMessage(NostrumMagica.getMagicWrapper(player)),
    			player);
    	NetworkHandler.sendTo(
    			new SpellRequestReplyMessage(NostrumMagica.instance.getSpellRegistry().getAllSpells(), true),
    			player);
    	NetworkHandler.sendTo(
    			new ManaArmorSyncMessage(player, NostrumMagica.getManaArmor(player)),
    			player);
    }
    
    public void updateEntityEffect(ServerPlayerEntity player, LivingEntity entity, SpecialEffect effectType, EffectData data) {
    	NetworkHandler.sendTo(
    			new MagicEffectUpdate(entity, effectType, data),
    			player);
    }

	public PlayerEntity getPlayer() {
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
	
	public void openBook(PlayerEntity player, GuiBook book, Object userdata) {
		; // Server does nothing
	}
	
	public void openContainer(PlayerEntity player, IPackedContainerProvider provider) {
		if (!player.world.isRemote() && player instanceof ServerPlayerEntity) {
			NetworkHooks.openGui((ServerPlayerEntity) player, provider, provider.getData());
		}
	}
	
	public <T extends IEntityPet> void openPetGUI(PlayerEntity player, T pet) {
		if (!player.world.isRemote()) {
			this.openContainer(player, PetGUI.PetContainer.Make(pet, player));
		}
	}
	
	public void openSpellScreen(Spell spell) {
		; // Nothing on server side
	}

	public void sendSpellDebug(PlayerEntity player, ITextComponent comp) {
		NetworkHandler.sendTo(new SpellDebugMessage(comp), (ServerPlayerEntity) player);
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
	
	public void requestStats(LivingEntity entity) {
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
			LivingEntity caster, Vec3d casterPos,
			LivingEntity target, Vec3d targetPos,
			SpellComponentWrapper flavor, boolean isNegative, float compParam) {
		if (world == null) {
			if (caster == null)
				world = target.world; // If you NPE here you suck. Supply a world!
			else
				world = caster.world;
		}
		
		final double MAX_RANGE = 50.0;
		
		ClientEffectRenderMessage message = new ClientEffectRenderMessage(
				caster, casterPos,
				target, targetPos,
				comp, flavor,
				isNegative, compParam);
		if (target != null) {
			NetworkHandler.sendToAllTracking(message, target);
		} else {
			NetworkHandler.sendToAllAround(message, new TargetPoint(targetPos.x, targetPos.y, targetPos.z, MAX_RANGE, world.getDimension().getType()));
		}
		
//		if (caster != null) {
//			//caster.addTrackingPlayer(player);
//			players.addAll(((ServerWorld) world).getEntityTracker()
//				.getTrackingPlayers(caster));
//		}
//		
//		if (target != null) {
//			//caster.addTrackingPlayer(player);
//			players.addAll(((ServerWorld) world).getEntityTracker()
//				.getTrackingPlayers(target));
//		}
//		
//		if (caster != null && caster == target && caster instanceof PlayerEntity) {
//			// Very specific case here
//			players.add((PlayerEntity) caster);
//		}
//		
//		if (players.isEmpty()) {
//			// Fall back to distance check against locations
//			if (casterPos != null) {
//				for (PlayerEntity player : world.playerEntities) {
//					if (player.getDistanceSq(casterPos.x, casterPos.y, casterPos.z) <= MAX_RANGE_SQR)
//						players.add(player);
//				}
//			}
//			
//			if (targetPos != null) {
//				for (PlayerEntity player : world.playerEntities) {
//					if (player.getDistanceSq(targetPos.x, targetPos.y, targetPos.z) <= MAX_RANGE_SQR)
//						players.add(player);
//				}
//			}
//		}
	}
	
	public void sendMana(PlayerEntity player) {
		INostrumMagic stats = NostrumMagica.getMagicWrapper(player);
		final int mana;
		if (stats == null) {
			mana = 0;
		} else {
			mana = stats.getMana();
		}
		
		NetworkHandler.sendToAllTracking(new ManaMessage(player, mana), player);
	}
	
	public void sendManaArmorCapability(PlayerEntity player) {
		IManaArmor stats = NostrumMagica.getManaArmor(player);
		NetworkHandler.sendToAllTracking(new ManaArmorSyncMessage(player, stats), player);
	}
	
	public void receiveManaArmorOverride(@Nonnull Entity ent, IManaArmor override) {
		; // Nothing to do on server
	}
	
	public void playRitualEffect(World world, BlockPos pos, EMagicElement element,
			ItemStack center, @Nullable NonNullList<ItemStack> extras, ReagentType[] types, ItemStack output) {
		Set<PlayerEntity> players = new HashSet<>();
		final double MAX_RANGE_SQR = 2500.0;
		if (pos != null) {
			for (PlayerEntity player : ((ServerWorld) world).getPlayers()) {
				if (player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) <= MAX_RANGE_SQR)
					players.add(player);
			}
		}
		
		if (!players.isEmpty()) {
			SpawnNostrumRitualEffectMessage message = new SpawnNostrumRitualEffectMessage(
					//int dimension, BlockPos pos, ReagentType[] reagents, ItemStack center, @Nullable NonNullList<ItemStack> extras, ItemStack output
					world.getDimension().getType(),
					pos, element, types, center, extras, output
					);
			for (PlayerEntity player : players) {
				NetworkHandler.sendTo(message, (ServerPlayerEntity) player);
			}
		}
	}
	
	public void playPredefinedEffect(PredefinedEffect type, int duration, World world, Vec3d position) {
		playPredefinedEffect(new SpawnPredefinedEffectMessage(type, duration, world.getDimension().getType(), position), world, position);
	}
	
	public void playPredefinedEffect(PredefinedEffect type, int duration, World world, Entity entity) {
		playPredefinedEffect(new SpawnPredefinedEffectMessage(type, duration, world.getDimension().getType(), entity.getEntityId()), world, entity.getPositionVector());
	}
	
	private void playPredefinedEffect(SpawnPredefinedEffectMessage message, World world, Vec3d center) {
		final double MAX_RANGE = 50.0;
		NetworkHandler.sendToAllAround(message, new TargetPoint(center.x, center.y, center.z, MAX_RANGE, world.getDimension().getType()));
	}
}
