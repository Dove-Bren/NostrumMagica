package com.smanzana.nostrummagica.entity;

import java.util.Random;
import java.util.Set;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.boss.plantboss.PlantBossBrambleEntity;
import com.smanzana.nostrummagica.entity.boss.plantboss.PlantBossEntity;
import com.smanzana.nostrummagica.entity.boss.playerstatue.PlayerStatueEntity;
import com.smanzana.nostrummagica.entity.boss.primalmage.PrimalMageEntity;
import com.smanzana.nostrummagica.entity.boss.reddragon.RedDragonEntity;
import com.smanzana.nostrummagica.entity.boss.shadowdragon.ShadowDragonEntity;
import com.smanzana.nostrummagica.entity.dragon.DragonEggEntity;
import com.smanzana.nostrummagica.entity.dragon.ShadowRedDragonEntity;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity;
import com.smanzana.nostrummagica.entity.golem.MagicEarthGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicEnderGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicFireGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicIceGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicLightningGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicNeutralGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicWindGolemEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumEntityTypes {

	@ObjectHolder(MagicNeutralGolemEntity.ID) public static EntityType<MagicNeutralGolemEntity> golemNeutral;
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
	@ObjectHolder(SeekerSpellSaucerEntity.ID) public static EntityType<SeekerSpellSaucerEntity> seekerSpellSaucer;
	@ObjectHolder(CyclerSpellSaucerEntity.ID) public static EntityType<CyclerSpellSaucerEntity> cyclerSpellSaucer;
	@ObjectHolder(SpellBoulderEntity.ID) public static EntityType<SpellBoulderEntity> boulder;
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
	@ObjectHolder(ArrowShardProjectile.ID) public static EntityType<ArrowShardProjectile> arrowShard;
	@ObjectHolder(CursedGlassTriggerEntity.ID) public static EntityType<CursedGlassTriggerEntity> cursedGlassTrigger;
	@ObjectHolder(PlayerStatueEntity.ID) public static EntityType<PlayerStatueEntity> playerStatue;
	@ObjectHolder(WhirlwindEntity.ID) public static EntityType<WhirlwindEntity> whirlwind;
	@ObjectHolder(PrimalMageEntity.ID) public static EntityType<PrimalMageEntity> primalMage;
	@ObjectHolder(ShadowDragonEntity.ID) public static EntityType<ShadowDragonEntity> shadowDragonBoss;
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<EntityType<?>> event) {
		final IForgeRegistry<EntityType<?>> registry = event.getRegistry();
    	registry.register(EntityType.Builder.<MagicNeutralGolemEntity>of(MagicNeutralGolemEntity::new, MobCategory.MISC)
				.sized(0.8F, 1.6F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(MagicNeutralGolemEntity.ID));
		registry.register(EntityType.Builder.<MagicLightningGolemEntity>of(MagicLightningGolemEntity::new, MobCategory.MISC)
				.sized(0.8F, 1.6F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(MagicLightningGolemEntity.ID));
		registry.register(EntityType.Builder.<MagicFireGolemEntity>of(MagicFireGolemEntity::new, MobCategory.MISC)
				.sized(0.8F, 1.6F)
				.fireImmune()
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(MagicFireGolemEntity.ID));
		registry.register(EntityType.Builder.<MagicEarthGolemEntity>of(MagicEarthGolemEntity::new, MobCategory.MISC)
				.sized(0.8F, 1.6F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(MagicEarthGolemEntity.ID));
		registry.register(EntityType.Builder.<MagicIceGolemEntity>of(MagicIceGolemEntity::new, MobCategory.MISC)
				.sized(0.8F, 1.6F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(MagicIceGolemEntity.ID));
		registry.register(EntityType.Builder.<MagicWindGolemEntity>of(MagicWindGolemEntity::new, MobCategory.MISC)
				.sized(0.8F, 1.6F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(MagicWindGolemEntity.ID));
		registry.register(EntityType.Builder.<MagicEnderGolemEntity>of(MagicEnderGolemEntity::new, MobCategory.MISC)
				.sized(0.8F, 1.6F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(MagicEnderGolemEntity.ID));
		
		EntityType<KoidEntity> koidType = EntityType.Builder.<KoidEntity>of(KoidEntity::new, MobCategory.MONSTER)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.sized(0.8F, 1F)
			.build("");
		koidType.setRegistryName(KoidEntity.ID);
		registry.register(koidType);
		
		registry.register(EntityType.Builder.<RedDragonEntity>of(RedDragonEntity::new, MobCategory.MISC)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.sized(RedDragonEntity.GetBodyWidth(), RedDragonEntity.GetBodyHeight())
				.fireImmune()
			.build("").setRegistryName(RedDragonEntity.ID));
		registry.register(EntityType.Builder.<RedDragonEntity.DragonBodyPart>of(RedDragonEntity.DragonBodyPart::new, MobCategory.MISC)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.sized(RedDragonEntity.GetBodyWidth(), RedDragonEntity.GetBodyHeight())
				.fireImmune()
			.build("").setRegistryName(RedDragonEntity.DragonBodyPart.ID));
		
		EntityType<TameRedDragonEntity> tameDragonType = EntityType.Builder.<TameRedDragonEntity>of(TameRedDragonEntity::new, MobCategory.MONSTER)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.sized(6F * .4F, 4.6F * .6F)
				.fireImmune()
			.build("");
		tameDragonType.setRegistryName(TameRedDragonEntity.ID);
		registry.register(tameDragonType);
		
		EntityType<ShadowRedDragonEntity> shadowRedDragonType = EntityType.Builder.<ShadowRedDragonEntity>of(ShadowRedDragonEntity::new, MobCategory.MONSTER)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.sized(4F * .6F, 3F * .6F)
				.fireImmune()
			.build("");
		shadowRedDragonType.setRegistryName(ShadowRedDragonEntity.ID);
		registry.register(shadowRedDragonType);
		
		registry.register(EntityType.Builder.<DragonEggEntity>of(DragonEggEntity::new, MobCategory.MISC)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.sized(.45f, .5f)
			.build("").setRegistryName(DragonEggEntity.ID));
		registry.register(EntityType.Builder.<PlantBossEntity>of(PlantBossEntity::new, MobCategory.MISC)
				.sized(7, 4)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(PlantBossEntity.ID));
		registry.register(EntityType.Builder.<PlantBossEntity.PlantBossBody>of(PlantBossEntity.PlantBossBody::new, MobCategory.MISC)
				.sized(4.25f /*width 3, but rotates. sqrt(3^2+3^2) = 4.24*/, 4)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(PlantBossEntity.PlantBossBody.ID));
		registry.register(EntityType.Builder.<PlantBossEntity.PlantBossLeafLimb>of(PlantBossEntity.PlantBossLeafLimb::new, MobCategory.MISC)
				.sized(4, 4)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(PlantBossEntity.PlantBossLeafLimb.ID));
		registry.register(EntityType.Builder.<PlantBossBrambleEntity>of(PlantBossBrambleEntity::new, MobCategory.MISC)
				.sized(.5f, .75f)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(PlantBossBrambleEntity.ID));
		
		EntityType<SpriteEntity> spriteType = EntityType.Builder.<SpriteEntity>of(SpriteEntity::new, MobCategory.MONSTER)
				.sized(1F, 1.75F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("");
		spriteType.setRegistryName(SpriteEntity.ID);
		
		EntityType<LuxEntity> luxType = EntityType.Builder.<LuxEntity>of(LuxEntity::new, MobCategory.CREATURE)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.sized(.5F, .5F)
			.build("");
		luxType.setRegistryName(LuxEntity.ID);
		registry.register(luxType);
		
		EntityType<WispEntity> wispType = EntityType.Builder.<WispEntity>of(WispEntity::new, MobCategory.CREATURE)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.sized(.75F, .75F)
			.build("");
		wispType.setRegistryName(WispEntity.ID);
		registry.register(wispType);
		registry.register(spriteType);
		
		EntityType<WilloEntity> willoType = EntityType.Builder.<WilloEntity>of(WilloEntity::new, MobCategory.MONSTER)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.sized(.75F, .75F)
			.build("");
		willoType.setRegistryName(WilloEntity.ID);
		registry.register(willoType);
		
		registry.register(EntityType.Builder.<ArcaneWolfEntity>of(ArcaneWolfEntity::new, MobCategory.MISC)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.sized(0.7F, 0.95F)
			.build("").setRegistryName(ArcaneWolfEntity.ID));

		
    	registry.register(EntityType.Builder.<SpellProjectileEntity>of(SpellProjectileEntity::new, MobCategory.MISC)
    			.sized(0.3125F, 0.3125F)
    			.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
    		.build("").setRegistryName(SpellProjectileEntity.ID));
		registry.register(EntityType.Builder.<ChakramSpellSaucerEntity>of(ChakramSpellSaucerEntity::new, MobCategory.MISC)
				.sized(1F, .2F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(ChakramSpellSaucerEntity.ID));
		registry.register(EntityType.Builder.<CyclerSpellSaucerEntity>of(CyclerSpellSaucerEntity::new, MobCategory.MISC)
				.sized(1F, .2F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(CyclerSpellSaucerEntity.ID));
		registry.register(EntityType.Builder.<SeekerSpellSaucerEntity>of(SeekerSpellSaucerEntity::new, MobCategory.MISC)
				.sized(.5F, .125F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(SeekerSpellSaucerEntity.ID));
		registry.register(EntityType.Builder.<MagicDamageProjectileEntity>of(MagicDamageProjectileEntity::new, MobCategory.MISC)
				.sized(.3F, .3F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(MagicDamageProjectileEntity.ID));
		registry.register(EntityType.Builder.<ArrowShardProjectile>of(ArrowShardProjectile::new, MobCategory.MISC)
				.sized(.3F, .3F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(ArrowShardProjectile.ID));
		registry.register(EntityType.Builder.<SpellBoulderEntity>of(SpellBoulderEntity::new, MobCategory.MISC)
				.sized(2.5F, 2.5F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(SpellBoulderEntity.ID));
		registry.register(EntityType.Builder.<SwitchTriggerEntity>of(SwitchTriggerEntity::new, MobCategory.MISC)
				.sized(.8f, 1.8f)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(SwitchTriggerEntity.ID));
		registry.register(EntityType.Builder.<KeySwitchTriggerEntity>of(KeySwitchTriggerEntity::new, MobCategory.MISC)
				.sized(.8f, 1.8f)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(KeySwitchTriggerEntity.ID));
		registry.register(EntityType.Builder.<ShrineTriggerEntity.Element>of(ShrineTriggerEntity.Element::new, MobCategory.MISC)
				.sized(.8f, .8f)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(ShrineTriggerEntity.Element.ID));
		registry.register(EntityType.Builder.<ShrineTriggerEntity.Shape>of(ShrineTriggerEntity.Shape::new, MobCategory.MISC)
				.sized(.8f, .8f)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(ShrineTriggerEntity.Shape.ID));
		registry.register(EntityType.Builder.<ShrineTriggerEntity.Alteration>of(ShrineTriggerEntity.Alteration::new, MobCategory.MISC)
				.sized(.8f, .8f)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(ShrineTriggerEntity.Alteration.ID));
		registry.register(EntityType.Builder.<ShrineTriggerEntity.Tier>of(ShrineTriggerEntity.Tier::new, MobCategory.MISC)
				.sized(.8f, .8f)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(ShrineTriggerEntity.Tier.ID));
		registry.register(EntityType.Builder.<CursedGlassTriggerEntity>of(CursedGlassTriggerEntity::new, MobCategory.MISC)
				.sized(3.2f, 3.2f)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(CursedGlassTriggerEntity.ID));
		registry.register(EntityType.Builder.<TameLightning>of(TameLightning::new, MobCategory.MISC)
				.sized(0, 0)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(TameLightning.ID));
		registry.register(EntityType.Builder.<HookShotEntity>of(HookShotEntity::new, MobCategory.MISC)
				.sized(.2f, .2f)
				.fireImmune()
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(HookShotEntity.ID));
		registry.register(EntityType.Builder.<SpellBulletEntity>of(SpellBulletEntity::new, MobCategory.MISC)
				.sized(0.3125F, 0.3125F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(SpellBulletEntity.ID));
		registry.register(EntityType.Builder.<SpellMortarEntity>of(SpellMortarEntity::new, MobCategory.MISC)
				.sized(0.75F, 0.75F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(SpellMortarEntity.ID));
		registry.register(EntityType.Builder.<AreaEffectEntity>of(AreaEffectEntity::new, MobCategory.MISC)
				.sized(1f, .25f)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(AreaEffectEntity.ID));
		registry.register(EntityType.Builder.<EnderRodBallEntity>of(EnderRodBallEntity::new, MobCategory.MISC)
				.sized(1.5f, 1.5f)
				.setTrackingRange(128).setUpdateInterval(5).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(EnderRodBallEntity.ID));
		registry.register(EntityType.Builder.<SpellBubbleEntity>of(SpellBubbleEntity::new, MobCategory.MISC)
    			.sized(0.125F, 0.125F)
    			.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
    		.build("").setRegistryName(SpellBubbleEntity.ID));
		
		registry.register(EntityType.Builder.<PlayerStatueEntity>of(PlayerStatueEntity::new, MobCategory.MONSTER)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.sized(1, 2)
			.build("").setRegistryName(PlayerStatueEntity.ID));
		registry.register(EntityType.Builder.<PrimalMageEntity>of(PrimalMageEntity::new, MobCategory.MONSTER)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.sized(0.6F, 1.95F)
			.build("").setRegistryName(PrimalMageEntity.ID));
		registry.register(EntityType.Builder.<ShadowDragonEntity>of(ShadowDragonEntity::new, MobCategory.MONSTER)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.sized(1.5F, 3F)
				.fireImmune()
			.build("").setRegistryName(ShadowDragonEntity.ID));
		
		registry.register(EntityType.Builder.<WhirlwindEntity>of(WhirlwindEntity::new, MobCategory.MISC)
    			.sized(0.5F, 0.5F)
    			.setTrackingRange(64).setUpdateInterval(5).setShouldReceiveVelocityUpdates(false)
    		.build("").setRegistryName(WhirlwindEntity.ID));
	}
	
	private static final boolean netherMobGroundSpawnTest(EntityType<?> type, LevelAccessor world, MobSpawnType reason, BlockPos pos, Random rand) {
		return world.getDifficulty() != Difficulty.PEACEFUL && world.getBlockState(pos.below()).getBlock() != Blocks.NETHER_WART_BLOCK;
	}
	
	@SubscribeEvent
	public static void registerEntityPlacement(FMLCommonSetupEvent event) {
		SpawnPlacements.register(koid, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules);
		SpawnPlacements.register(tameDragonRed, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NostrumEntityTypes::netherMobGroundSpawnTest);
		SpawnPlacements.register(shadowDragonRed, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NostrumEntityTypes::netherMobGroundSpawnTest);
		SpawnPlacements.register(sprite, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules);
		SpawnPlacements.register(wisp, SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WispEntity::canSpawnExtraCheck);
		SpawnPlacements.register(willo, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WilloEntity::canSpawnExtraCheck);
		SpawnPlacements.register(lux, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Animal::checkAnimalSpawnRules);
		
		// Can't mix buses, so manually register spawn handling to the game bus
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, NostrumEntityTypes::registerSpawns);
	}
	
	//@SubscribeEvent registered in #registerEntityPlacement above
	public static void registerSpawns(BiomeLoadingEvent event) {
		final Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(ResourceKey.create(Registry.BIOME_REGISTRY, event.getName()));
		
		final boolean magical = types.contains(BiomeDictionary.Type.MAGICAL);
		final boolean forest = types.contains(BiomeDictionary.Type.FOREST);
		final boolean snowy = types.contains(BiomeDictionary.Type.SNOWY);
		final boolean nether = types.contains(BiomeDictionary.Type.NETHER);
		final boolean spooky = types.contains(BiomeDictionary.Type.SPOOKY);
		final boolean jungle = types.contains(BiomeDictionary.Type.JUNGLE);
		final boolean dry = types.contains(BiomeDictionary.Type.DRY);
		
		// koid
		if (magical || forest || snowy || nether || spooky) {
			event.getSpawns().getSpawner(MobCategory.MONSTER).add(new MobSpawnSettings.SpawnerData(koid, nether ? 12 : 20, 1, 1));
		}
		
		// tameable and shadow dragon natural spawns
		if (nether) {
			event.getSpawns().getSpawner(MobCategory.MONSTER).add(new MobSpawnSettings.SpawnerData(tameDragonRed, 2, 1, 1));
			event.getSpawns().getSpawner(MobCategory.MONSTER).add(new MobSpawnSettings.SpawnerData(shadowDragonRed, 15, 1, 2));
		}
		
		// sprite
		if (nether || magical) {
			event.getSpawns().getSpawner(MobCategory.MONSTER).add(new MobSpawnSettings.SpawnerData(sprite, nether ? 15 : 10, 1, 3));
		}
		
		// lux
		if (magical || forest || jungle) {
			final int weight = magical ? 20 : (forest ? 15 : 10);
			event.getSpawns().getSpawner(MobCategory.CREATURE).add(new MobSpawnSettings.SpawnerData(lux, weight, 1, 3));
		}
		
		// wisp
		if (magical || forest || snowy || spooky || nether) {
			event.getSpawns().getSpawner(MobCategory.AMBIENT).add(new MobSpawnSettings.SpawnerData(wisp, nether ? 4 : 1, 1, 1));
		}
		
		// willo
		if (magical || forest || snowy || spooky || dry || nether) {
			final int weight = magical ? 18
					: forest ? 10
					: snowy ? 8
					: spooky ? 14
					: dry ? 7
					: 1;
			event.getSpawns().getSpawner(MobCategory.MONSTER).add(new MobSpawnSettings.SpawnerData(willo, weight, 1, 3));
		}
	}
	
	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(golemNeutral, MagicNeutralGolemEntity.BuildAttributes().build());
		event.put(golemLightning, MagicLightningGolemEntity.BuildAttributes().build());
		event.put(golemFire, MagicFireGolemEntity.BuildAttributes().build());
		event.put(golemEarth, MagicEarthGolemEntity.BuildAttributes().build());
		event.put(golemIce, MagicIceGolemEntity.BuildAttributes().build());
		event.put(golemWind, MagicWindGolemEntity.BuildAttributes().build());
		event.put(golemEnder, MagicEnderGolemEntity.BuildAttributes().build());
		event.put(koid, KoidEntity.BuildAttributes().build());
		event.put(dragonRed, RedDragonEntity.BuildAttributes().build());
		// No attributes event.put(dragonRedBodyPart, EntityDragonRed.DragonBodyPart.BuildAttributes().create());
		event.put(tameDragonRed, TameRedDragonEntity.BuildAttributes().build());
		event.put(shadowDragonRed, ShadowRedDragonEntity.BuildAttributes().build());
		event.put(dragonEgg, DragonEggEntity.BuildAttributes().build());
		event.put(plantBoss, PlantBossEntity.BuildAttributes().build());
		// No attributes event.put(plantBossBody, EntityPlantBoss.PlantBossBody.BuildAttributes().create());
		// No attributes event.put(plantBossLeaf, EntityPlantBoss.PlantBossLeafLimb.BuildAttributes().create());
		// No attributes event.put(plantBossBramble, EntityPlantBossBramble.BuildAttributes().create());
		event.put(sprite, SpriteEntity.BuildAttributes().build());
		event.put(lux, LuxEntity.BuildAttributes().build());
		event.put(wisp, WispEntity.BuildAttributes().build());
		event.put(willo, WilloEntity.BuildAttributes().build());
		event.put(arcaneWolf, ArcaneWolfEntity.BuildAttributes().build());
		// No attributes event.put(spellProjectile, EntitySpellProjectile.BuildAttributes().create());
		// No attributes event.put(chakramSpellSaucer, EntityChakramSpellSaucer.BuildAttributes().create());
		// No attributes event.put(cyclerSpellSaucer, EntityCyclerSpellSaucer.BuildAttributes().create());
		event.put(switchTrigger, SwitchTriggerEntity.BuildAttributes().build());
		event.put(elementShrine, ShrineTriggerEntity.Element.BuildAttributes().build());
		event.put(shapeShrine, ShrineTriggerEntity.Shape.BuildAttributes().build());
		event.put(alterationShrine, ShrineTriggerEntity.Alteration.BuildAttributes().build());
		event.put(tierShrine, ShrineTriggerEntity.Tier.BuildAttributes().build());
		// No attributes event.put(tameLightning, NostrumTameLightning.BuildAttributes().create());
		// No attributes event.put(hookShot, EntityHookShot.BuildAttributes().create());
		// No attributes event.put(spellBullet, EntitySpellBullet.BuildAttributes().create());
		// No attributes event.put(spellMortar, EntitySpellMortar.BuildAttributes().create());
		// No attributes event.put(areaEffect, EntityAreaEffect.BuildAttributes().create());
		event.put(keySwitchTrigger, KeySwitchTriggerEntity.BuildKeySwitchAttributes().build());
		// No attributes event.put(enderRodBall, EntityEnderRodBall.BuildAttributes().create());
		event.put(cursedGlassTrigger, CursedGlassTriggerEntity.BuildAttributes().build());
		event.put(playerStatue, PlayerStatueEntity.BuildAttributes().build());
		event.put(primalMage, PrimalMageEntity.BuildAttributes().build());
		event.put(shadowDragonBoss, ShadowDragonEntity.BuildAttributes().build());
		
	}
	
}
