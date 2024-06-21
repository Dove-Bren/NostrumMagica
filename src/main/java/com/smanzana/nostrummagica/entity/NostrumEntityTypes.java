package com.smanzana.nostrummagica.entity;

import java.util.Random;
import java.util.Set;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.dragon.DragonEggEntity;
import com.smanzana.nostrummagica.entity.dragon.RedDragonEntity;
import com.smanzana.nostrummagica.entity.dragon.ShadowRedDragonEntity;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity;
import com.smanzana.nostrummagica.entity.golem.MagicEarthGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicEnderGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicFireGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicIceGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicLightningGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicPhysicalGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicWindGolemEntity;
import com.smanzana.nostrummagica.entity.plantboss.PlantBossEntity;
import com.smanzana.nostrummagica.entity.plantboss.PlantBossBrambleEntity;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumEntityTypes {

	@ObjectHolder(MagicPhysicalGolemEntity.ID) public static EntityType<MagicPhysicalGolemEntity> golemPhysical;
	@ObjectHolder(MagicLightningGolemEntity.ID) public static EntityType<MagicLightningGolemEntity> golemLightning;
	@ObjectHolder(MagicFireGolemEntity.ID) public static EntityType<MagicFireGolemEntity> golemFire;
	@ObjectHolder(MagicEarthGolemEntity.ID) public static EntityType<MagicEarthGolemEntity> golemEarth;
	@ObjectHolder(MagicIceGolemEntity.ID) public static EntityType<MagicIceGolemEntity> golemIce;
	@ObjectHolder(MagicWindGolemEntity.ID) public static EntityType<MagicWindGolemEntity> golemWind;
	@ObjectHolder(MagicEnderGolemEntity.ID) public static EntityType<MagicEnderGolemEntity> golemEnder;
	@ObjectHolder(KoidEntity.ID) public static EntityType<KoidEntity> koid;
	@ObjectHolder(RedDragonEntity.ID) public static EntityType<RedDragonEntity> dragonRed;
	@ObjectHolder(RedDragonEntity.DragonBodyPart.ID) public static EntityType<RedDragonEntity.DragonBodyPart> dragonRedBodyPart;
	@ObjectHolder(TameRedDragonEntity.ID) public static EntityType<TameRedDragonEntity> tameDragonRed;
	@ObjectHolder(ShadowRedDragonEntity.ID) public static EntityType<ShadowRedDragonEntity> shadowDragonRed;
	@ObjectHolder(DragonEggEntity.ID) public static EntityType<DragonEggEntity> dragonEgg;
	@ObjectHolder(PlantBossEntity.ID) public static EntityType<PlantBossEntity> plantBoss;
	@ObjectHolder(PlantBossEntity.PlantBossBody.ID) public static EntityType<PlantBossEntity.PlantBossBody> plantBossBody;
	@ObjectHolder(PlantBossEntity.PlantBossLeafLimb.ID) public static EntityType<PlantBossEntity.PlantBossLeafLimb> plantBossLeaf;
	@ObjectHolder(PlantBossBrambleEntity.ID) public static EntityType<PlantBossBrambleEntity> plantBossBramble;
	@ObjectHolder(SpriteEntity.ID) public static EntityType<SpriteEntity> sprite;
	@ObjectHolder(LuxEntity.ID) public static EntityType<LuxEntity> lux;
	@ObjectHolder(WispEntity.ID) public static EntityType<WispEntity> wisp;
	@ObjectHolder(WilloEntity.ID) public static EntityType<WilloEntity> willo;
	@ObjectHolder(ArcaneWolfEntity.ID) public static EntityType<ArcaneWolfEntity> arcaneWolf;
	@ObjectHolder(SpellProjectileEntity.ID) public static EntityType<SpellProjectileEntity> spellProjectile;
	@ObjectHolder(ChakramSpellSaucerEntity.ID) public static EntityType<ChakramSpellSaucerEntity> chakramSpellSaucer;
	@ObjectHolder(CyclerSpellSaucerEntity.ID) public static EntityType<CyclerSpellSaucerEntity> cyclerSpellSaucer;
	@ObjectHolder(SwitchTriggerEntity.ID) public static EntityType<SwitchTriggerEntity> switchTrigger;
	@ObjectHolder(TameLightning.ID) public static EntityType<TameLightning> tameLightning;
	@ObjectHolder(HookShotEntity.ID) public static EntityType<HookShotEntity> hookShot;
	@ObjectHolder(SpellBulletEntity.ID) public static EntityType<SpellBulletEntity> spellBullet;
	@ObjectHolder(SpellMortarEntity.ID) public static EntityType<SpellMortarEntity> spellMortar;
	@ObjectHolder(AreaEffectEntity.ID) public static EntityType<AreaEffectEntity> areaEffect;
	@ObjectHolder(KeySwitchTriggerEntity.ID) public static EntityType<KeySwitchTriggerEntity> keySwitchTrigger;
	@ObjectHolder(EnderRodBallEntity.ID) public static EntityType<EnderRodBallEntity> enderRodBall;
	@ObjectHolder(SpellBubbleEntity.ID) public static EntityType<SpellBubbleEntity> spellBubble;
	@ObjectHolder(ShrineTriggerEntity.Element.ID) public static EntityType<ShrineTriggerEntity.Element> elementShrine;
	@ObjectHolder(ShrineTriggerEntity.Shape.ID) public static EntityType<ShrineTriggerEntity.Shape> shapeShrine;
	@ObjectHolder(ShrineTriggerEntity.Alteration.ID) public static EntityType<ShrineTriggerEntity.Alteration> alterationShrine;
	@ObjectHolder(ShrineTriggerEntity.Tier.ID) public static EntityType<ShrineTriggerEntity.Tier> tierShrine;
	@ObjectHolder(MagicDamageProjectileEntity.ID) public static EntityType<MagicDamageProjectileEntity> magicDamageProjectile;
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<EntityType<?>> event) {
		final IForgeRegistry<EntityType<?>> registry = event.getRegistry();
    	registry.register(EntityType.Builder.<MagicPhysicalGolemEntity>create(MagicPhysicalGolemEntity::new, EntityClassification.MISC)
				.size(0.8F, 1.6F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(MagicPhysicalGolemEntity.ID));
		registry.register(EntityType.Builder.<MagicLightningGolemEntity>create(MagicLightningGolemEntity::new, EntityClassification.MISC)
				.size(0.8F, 1.6F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(MagicLightningGolemEntity.ID));
		registry.register(EntityType.Builder.<MagicFireGolemEntity>create(MagicFireGolemEntity::new, EntityClassification.MISC)
				.size(0.8F, 1.6F)
				.immuneToFire()
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(MagicFireGolemEntity.ID));
		registry.register(EntityType.Builder.<MagicEarthGolemEntity>create(MagicEarthGolemEntity::new, EntityClassification.MISC)
				.size(0.8F, 1.6F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(MagicEarthGolemEntity.ID));
		registry.register(EntityType.Builder.<MagicIceGolemEntity>create(MagicIceGolemEntity::new, EntityClassification.MISC)
				.size(0.8F, 1.6F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(MagicIceGolemEntity.ID));
		registry.register(EntityType.Builder.<MagicWindGolemEntity>create(MagicWindGolemEntity::new, EntityClassification.MISC)
				.size(0.8F, 1.6F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(MagicWindGolemEntity.ID));
		registry.register(EntityType.Builder.<MagicEnderGolemEntity>create(MagicEnderGolemEntity::new, EntityClassification.MISC)
				.size(0.8F, 1.6F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(MagicEnderGolemEntity.ID));
		
		EntityType<KoidEntity> koidType = EntityType.Builder.<KoidEntity>create(KoidEntity::new, EntityClassification.MONSTER)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(0.8F, 1F)
			.build("");
		koidType.setRegistryName(KoidEntity.ID);
		registry.register(koidType);
		
		registry.register(EntityType.Builder.<RedDragonEntity>create(RedDragonEntity::new, EntityClassification.MISC)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(RedDragonEntity.GetBodyWidth(), RedDragonEntity.GetBodyHeight())
				.immuneToFire()
			.build("").setRegistryName(RedDragonEntity.ID));
		registry.register(EntityType.Builder.<RedDragonEntity.DragonBodyPart>create(RedDragonEntity.DragonBodyPart::new, EntityClassification.MISC)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(RedDragonEntity.GetBodyWidth(), RedDragonEntity.GetBodyHeight())
				.immuneToFire()
			.build("").setRegistryName(RedDragonEntity.DragonBodyPart.ID));
		
		EntityType<TameRedDragonEntity> tameDragonType = EntityType.Builder.<TameRedDragonEntity>create(TameRedDragonEntity::new, EntityClassification.MONSTER)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(6F * .4F, 4.6F * .6F)
				.immuneToFire()
			.build("");
		tameDragonType.setRegistryName(TameRedDragonEntity.ID);
		registry.register(tameDragonType);
		
		EntityType<ShadowRedDragonEntity> shadowRedDragonType = EntityType.Builder.<ShadowRedDragonEntity>create(ShadowRedDragonEntity::new, EntityClassification.MONSTER)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(4F * .6F, 3F * .6F)
				.immuneToFire()
			.build("");
		shadowRedDragonType.setRegistryName(ShadowRedDragonEntity.ID);
		registry.register(shadowRedDragonType);
		
		registry.register(EntityType.Builder.<DragonEggEntity>create(DragonEggEntity::new, EntityClassification.MISC)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(.45f, .5f)
			.build("").setRegistryName(DragonEggEntity.ID));
		registry.register(EntityType.Builder.<PlantBossEntity>create(PlantBossEntity::new, EntityClassification.MISC)
				.size(7, 4)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(PlantBossEntity.ID));
		registry.register(EntityType.Builder.<PlantBossEntity.PlantBossBody>create(PlantBossEntity.PlantBossBody::new, EntityClassification.MISC)
				.size(4.25f /*width 3, but rotates. sqrt(3^2+3^2) = 4.24*/, 4)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(PlantBossEntity.PlantBossBody.ID));
		registry.register(EntityType.Builder.<PlantBossEntity.PlantBossLeafLimb>create(PlantBossEntity.PlantBossLeafLimb::new, EntityClassification.MISC)
				.size(4, 4)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(PlantBossEntity.PlantBossLeafLimb.ID));
		registry.register(EntityType.Builder.<PlantBossBrambleEntity>create(PlantBossBrambleEntity::new, EntityClassification.MISC)
				.size(.5f, .75f)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(PlantBossBrambleEntity.ID));
		
		EntityType<SpriteEntity> spriteType = EntityType.Builder.<SpriteEntity>create(SpriteEntity::new, EntityClassification.MONSTER)
				.size(1F, 1.75F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("");
		spriteType.setRegistryName(SpriteEntity.ID);
		
		EntityType<LuxEntity> luxType = EntityType.Builder.<LuxEntity>create(LuxEntity::new, EntityClassification.CREATURE)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(.5F, .5F)
			.build("");
		luxType.setRegistryName(LuxEntity.ID);
		registry.register(luxType);
		
		EntityType<WispEntity> wispType = EntityType.Builder.<WispEntity>create(WispEntity::new, EntityClassification.CREATURE)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(.75F, .75F)
			.build("");
		wispType.setRegistryName(WispEntity.ID);
		registry.register(wispType);
		registry.register(spriteType);
		
		EntityType<WilloEntity> willoType = EntityType.Builder.<WilloEntity>create(WilloEntity::new, EntityClassification.MONSTER)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(.75F, .75F)
			.build("");
		willoType.setRegistryName(WilloEntity.ID);
		registry.register(willoType);
		
		registry.register(EntityType.Builder.<ArcaneWolfEntity>create(ArcaneWolfEntity::new, EntityClassification.MISC)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(0.7F, 0.95F)
			.build("").setRegistryName(ArcaneWolfEntity.ID));

		
    	registry.register(EntityType.Builder.<SpellProjectileEntity>create(SpellProjectileEntity::new, EntityClassification.MISC)
    			.size(0.3125F, 0.3125F)
    			.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
    		.build("").setRegistryName(SpellProjectileEntity.ID));
		registry.register(EntityType.Builder.<ChakramSpellSaucerEntity>create(ChakramSpellSaucerEntity::new, EntityClassification.MISC)
				.size(1F, .2F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(ChakramSpellSaucerEntity.ID));
		registry.register(EntityType.Builder.<CyclerSpellSaucerEntity>create(CyclerSpellSaucerEntity::new, EntityClassification.MISC)
				.size(1F, .2F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(CyclerSpellSaucerEntity.ID));
		registry.register(EntityType.Builder.<MagicDamageProjectileEntity>create(MagicDamageProjectileEntity::new, EntityClassification.MISC)
				.size(.3F, .3F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(MagicDamageProjectileEntity.ID));
		registry.register(EntityType.Builder.<SwitchTriggerEntity>create(SwitchTriggerEntity::new, EntityClassification.MISC)
				.size(.8f, 1.8f)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(SwitchTriggerEntity.ID));
		registry.register(EntityType.Builder.<KeySwitchTriggerEntity>create(KeySwitchTriggerEntity::new, EntityClassification.MISC)
				.size(.8f, 1.8f)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(KeySwitchTriggerEntity.ID));
		registry.register(EntityType.Builder.<ShrineTriggerEntity.Element>create(ShrineTriggerEntity.Element::new, EntityClassification.MISC)
				.size(.8f, .8f)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(ShrineTriggerEntity.Element.ID));
		registry.register(EntityType.Builder.<ShrineTriggerEntity.Shape>create(ShrineTriggerEntity.Shape::new, EntityClassification.MISC)
				.size(.8f, .8f)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(ShrineTriggerEntity.Shape.ID));
		registry.register(EntityType.Builder.<ShrineTriggerEntity.Alteration>create(ShrineTriggerEntity.Alteration::new, EntityClassification.MISC)
				.size(.8f, .8f)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(ShrineTriggerEntity.Alteration.ID));
		registry.register(EntityType.Builder.<ShrineTriggerEntity.Tier>create(ShrineTriggerEntity.Tier::new, EntityClassification.MISC)
				.size(.8f, .8f)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(ShrineTriggerEntity.Tier.ID));
		registry.register(EntityType.Builder.<TameLightning>create(TameLightning::new, EntityClassification.MISC)
				.size(0, 0)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(TameLightning.ID));
		registry.register(EntityType.Builder.<HookShotEntity>create(HookShotEntity::new, EntityClassification.MISC)
				.size(.2f, .2f)
				.immuneToFire()
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(HookShotEntity.ID));
		registry.register(EntityType.Builder.<SpellBulletEntity>create(SpellBulletEntity::new, EntityClassification.MISC)
				.size(0.3125F, 0.3125F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(SpellBulletEntity.ID));
		registry.register(EntityType.Builder.<SpellMortarEntity>create(SpellMortarEntity::new, EntityClassification.MISC)
				.size(0.75F, 0.75F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(SpellMortarEntity.ID));
		registry.register(EntityType.Builder.<AreaEffectEntity>create(AreaEffectEntity::new, EntityClassification.MISC)
				.size(1f, .25f)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(AreaEffectEntity.ID));
		registry.register(EntityType.Builder.<EnderRodBallEntity>create(EnderRodBallEntity::new, EntityClassification.MISC)
				.size(1.5f, 1.5f)
				.setTrackingRange(128).setUpdateInterval(5).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(EnderRodBallEntity.ID));
		registry.register(EntityType.Builder.<SpellBubbleEntity>create(SpellBubbleEntity::new, EntityClassification.MISC)
    			.size(0.125F, 0.125F)
    			.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
    		.build("").setRegistryName(SpellBubbleEntity.ID));
	}
	
	private static final boolean netherMobGroundSpawnTest(EntityType<?> type, IWorld world, SpawnReason reason, BlockPos pos, Random rand) {
		return world.getDifficulty() != Difficulty.PEACEFUL && world.getBlockState(pos.down()).getBlock() != Blocks.NETHER_WART_BLOCK;
	}
	
	@SubscribeEvent
	public static void registerEntityPlacement(FMLCommonSetupEvent event) {
		EntitySpawnPlacementRegistry.register(koid, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canSpawnOn);
		EntitySpawnPlacementRegistry.register(tameDragonRed, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, NostrumEntityTypes::netherMobGroundSpawnTest);
		EntitySpawnPlacementRegistry.register(shadowDragonRed, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, NostrumEntityTypes::netherMobGroundSpawnTest);
		EntitySpawnPlacementRegistry.register(sprite, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canSpawnOn);
		EntitySpawnPlacementRegistry.register(wisp, EntitySpawnPlacementRegistry.PlacementType.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, WispEntity::canSpawnExtraCheck);
		EntitySpawnPlacementRegistry.register(willo, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, WilloEntity::canSpawnExtraCheck);
		EntitySpawnPlacementRegistry.register(lux, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING, AnimalEntity::canAnimalSpawn);
		
		// Can't mix buses, so manually register spawn handling to the game bus
		MinecraftForge.EVENT_BUS.addListener(NostrumEntityTypes::registerSpawns);
	}
	
	//@SubscribeEvent registered in #registerEntityPlacement above
	public static void registerSpawns(BiomeLoadingEvent event) {
		final Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(RegistryKey.getOrCreateKey(Registry.BIOME_KEY, event.getName()));
		
		final boolean magical = types.contains(BiomeDictionary.Type.MAGICAL);
		final boolean forest = types.contains(BiomeDictionary.Type.FOREST);
		final boolean snowy = types.contains(BiomeDictionary.Type.SNOWY);
		final boolean nether = types.contains(BiomeDictionary.Type.NETHER);
		final boolean spooky = types.contains(BiomeDictionary.Type.SPOOKY);
		final boolean jungle = types.contains(BiomeDictionary.Type.JUNGLE);
		final boolean dry = types.contains(BiomeDictionary.Type.DRY);
		
		// koid
		if (magical || forest || snowy || nether || spooky) {
			event.getSpawns().getSpawner(EntityClassification.MONSTER).add(new MobSpawnInfo.Spawners(koid, nether ? 12 : 20, 1, 1));
		}
		
		// tameable and shadow dragon natural spawns
		if (nether) {
			event.getSpawns().getSpawner(EntityClassification.MONSTER).add(new MobSpawnInfo.Spawners(tameDragonRed, 2, 1, 1));
			event.getSpawns().getSpawner(EntityClassification.MONSTER).add(new MobSpawnInfo.Spawners(shadowDragonRed, 15, 1, 2));
		}
		
		// sprite
		if (nether || magical) {
			event.getSpawns().getSpawner(EntityClassification.MONSTER).add(new MobSpawnInfo.Spawners(sprite, nether ? 15 : 10, 1, 3));
		}
		
		// lux
		if (magical || forest || jungle) {
			final int weight = magical ? 20 : (forest ? 15 : 10);
			event.getSpawns().getSpawner(EntityClassification.CREATURE).add(new MobSpawnInfo.Spawners(lux, weight, 1, 3));
		}
		
		// wisp
		if (magical || forest || snowy || spooky || nether) {
			event.getSpawns().getSpawner(EntityClassification.AMBIENT).add(new MobSpawnInfo.Spawners(wisp, nether ? 4 : 1, 1, 1));
		}
		
		// willo
		if (magical || forest || snowy || spooky || dry || nether) {
			final int weight = magical ? 18
					: forest ? 10
					: snowy ? 8
					: spooky ? 14
					: dry ? 7
					: 1;
			event.getSpawns().getSpawner(EntityClassification.MONSTER).add(new MobSpawnInfo.Spawners(willo, weight, 1, 3));
		}
	}
	
	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(golemPhysical, MagicPhysicalGolemEntity.BuildAttributes().create());
		event.put(golemLightning, MagicLightningGolemEntity.BuildAttributes().create());
		event.put(golemFire, MagicFireGolemEntity.BuildAttributes().create());
		event.put(golemEarth, MagicEarthGolemEntity.BuildAttributes().create());
		event.put(golemIce, MagicIceGolemEntity.BuildAttributes().create());
		event.put(golemWind, MagicWindGolemEntity.BuildAttributes().create());
		event.put(golemEnder, MagicEnderGolemEntity.BuildAttributes().create());
		event.put(koid, KoidEntity.BuildAttributes().create());
		event.put(dragonRed, RedDragonEntity.BuildAttributes().create());
		// No attributes event.put(dragonRedBodyPart, EntityDragonRed.DragonBodyPart.BuildAttributes().create());
		event.put(tameDragonRed, TameRedDragonEntity.BuildAttributes().create());
		event.put(shadowDragonRed, ShadowRedDragonEntity.BuildAttributes().create());
		event.put(dragonEgg, DragonEggEntity.BuildAttributes().create());
		event.put(plantBoss, PlantBossEntity.BuildAttributes().create());
		// No attributes event.put(plantBossBody, EntityPlantBoss.PlantBossBody.BuildAttributes().create());
		// No attributes event.put(plantBossLeaf, EntityPlantBoss.PlantBossLeafLimb.BuildAttributes().create());
		// No attributes event.put(plantBossBramble, EntityPlantBossBramble.BuildAttributes().create());
		event.put(sprite, SpriteEntity.BuildAttributes().create());
		event.put(lux, LuxEntity.BuildAttributes().create());
		event.put(wisp, WispEntity.BuildAttributes().create());
		event.put(willo, WilloEntity.BuildAttributes().create());
		event.put(arcaneWolf, ArcaneWolfEntity.BuildAttributes().create());
		// No attributes event.put(spellProjectile, EntitySpellProjectile.BuildAttributes().create());
		// No attributes event.put(chakramSpellSaucer, EntityChakramSpellSaucer.BuildAttributes().create());
		// No attributes event.put(cyclerSpellSaucer, EntityCyclerSpellSaucer.BuildAttributes().create());
		event.put(switchTrigger, SwitchTriggerEntity.BuildAttributes().create());
		event.put(elementShrine, ShrineTriggerEntity.Element.BuildAttributes().create());
		event.put(shapeShrine, ShrineTriggerEntity.Shape.BuildAttributes().create());
		event.put(alterationShrine, ShrineTriggerEntity.Alteration.BuildAttributes().create());
		event.put(tierShrine, ShrineTriggerEntity.Tier.BuildAttributes().create());
		// No attributes event.put(tameLightning, NostrumTameLightning.BuildAttributes().create());
		// No attributes event.put(hookShot, EntityHookShot.BuildAttributes().create());
		// No attributes event.put(spellBullet, EntitySpellBullet.BuildAttributes().create());
		// No attributes event.put(spellMortar, EntitySpellMortar.BuildAttributes().create());
		// No attributes event.put(areaEffect, EntityAreaEffect.BuildAttributes().create());
		event.put(keySwitchTrigger, KeySwitchTriggerEntity.BuildKeySwitchAttributes().create());
		// No attributes event.put(enderRodBall, EntityEnderRodBall.BuildAttributes().create());
		
	}
	
}
