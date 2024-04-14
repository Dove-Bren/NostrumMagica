package com.smanzana.nostrummagica.entity;

import java.util.Random;
import java.util.Set;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonEgg;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonRed;
import com.smanzana.nostrummagica.entity.dragon.EntityShadowDragonRed;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;
import com.smanzana.nostrummagica.entity.golem.EntityGolemEarth;
import com.smanzana.nostrummagica.entity.golem.EntityGolemEnder;
import com.smanzana.nostrummagica.entity.golem.EntityGolemFire;
import com.smanzana.nostrummagica.entity.golem.EntityGolemIce;
import com.smanzana.nostrummagica.entity.golem.EntityGolemLightning;
import com.smanzana.nostrummagica.entity.golem.EntityGolemPhysical;
import com.smanzana.nostrummagica.entity.golem.EntityGolemWind;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBoss;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBossBramble;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.common.BiomeDictionary;
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

	@ObjectHolder(EntityGolemPhysical.ID) public static EntityType<EntityGolemPhysical> golemPhysical;
	@ObjectHolder(EntityGolemLightning.ID) public static EntityType<EntityGolemLightning> golemLightning;
	@ObjectHolder(EntityGolemFire.ID) public static EntityType<EntityGolemFire> golemFire;
	@ObjectHolder(EntityGolemEarth.ID) public static EntityType<EntityGolemEarth> golemEarth;
	@ObjectHolder(EntityGolemIce.ID) public static EntityType<EntityGolemIce> golemIce;
	@ObjectHolder(EntityGolemWind.ID) public static EntityType<EntityGolemWind> golemWind;
	@ObjectHolder(EntityGolemEnder.ID) public static EntityType<EntityGolemEnder> golemEnder;
	@ObjectHolder(EntityKoid.ID) public static EntityType<EntityKoid> koid;
	@ObjectHolder(EntityDragonRed.ID) public static EntityType<EntityDragonRed> dragonRed;
	@ObjectHolder(EntityDragonRed.DragonBodyPart.ID) public static EntityType<EntityDragonRed.DragonBodyPart> dragonRedBodyPart;
	@ObjectHolder(EntityTameDragonRed.ID) public static EntityType<EntityTameDragonRed> tameDragonRed;
	@ObjectHolder(EntityShadowDragonRed.ID) public static EntityType<EntityShadowDragonRed> shadowDragonRed;
	@ObjectHolder(EntityDragonEgg.ID) public static EntityType<EntityDragonEgg> dragonEgg;
	@ObjectHolder(EntityPlantBoss.ID) public static EntityType<EntityPlantBoss> plantBoss;
	@ObjectHolder(EntityPlantBoss.PlantBossBody.ID) public static EntityType<EntityPlantBoss.PlantBossBody> plantBossBody;
	@ObjectHolder(EntityPlantBoss.PlantBossLeafLimb.ID) public static EntityType<EntityPlantBoss.PlantBossLeafLimb> plantBossLeaf;
	@ObjectHolder(EntityPlantBossBramble.ID) public static EntityType<EntityPlantBossBramble> plantBossBramble;
	@ObjectHolder(EntitySprite.ID) public static EntityType<EntitySprite> sprite;
	@ObjectHolder(EntityLux.ID) public static EntityType<EntityLux> lux;
	@ObjectHolder(EntityWisp.ID) public static EntityType<EntityWisp> wisp;
	@ObjectHolder(EntityWillo.ID) public static EntityType<EntityWillo> willo;
	@ObjectHolder(EntityArcaneWolf.ID) public static EntityType<EntityArcaneWolf> arcaneWolf;
	@ObjectHolder(EntitySpellProjectile.ID) public static EntityType<EntitySpellProjectile> spellProjectile;
	@ObjectHolder(EntityChakramSpellSaucer.ID) public static EntityType<EntityChakramSpellSaucer> chakramSpellSaucer;
	@ObjectHolder(EntityCyclerSpellSaucer.ID) public static EntityType<EntityCyclerSpellSaucer> cyclerSpellSaucer;
	@ObjectHolder(EntitySwitchTrigger.ID) public static EntityType<EntitySwitchTrigger> switchTrigger;
	@ObjectHolder(NostrumTameLightning.ID) public static EntityType<NostrumTameLightning> tameLightning;
	@ObjectHolder(EntityHookShot.ID) public static EntityType<EntityHookShot> hookShot;
	@ObjectHolder(EntitySpellBullet.ID) public static EntityType<EntitySpellBullet> spellBullet;
	@ObjectHolder(EntitySpellMortar.ID) public static EntityType<EntitySpellMortar> spellMortar;
	@ObjectHolder(EntityAreaEffect.ID) public static EntityType<EntityAreaEffect> areaEffect;
	@ObjectHolder(EntityKeySwitchTrigger.ID) public static EntityType<EntityKeySwitchTrigger> keySwitchTrigger;
	@ObjectHolder(EntityEnderRodBall.ID) public static EntityType<EntityEnderRodBall> enderRodBall;
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<EntityType<?>> event) {
		final IForgeRegistry<EntityType<?>> registry = event.getRegistry();
    	registry.register(EntityType.Builder.<EntityGolemPhysical>create(EntityGolemPhysical::new, EntityClassification.MISC)
				.size(0.8F, 1.6F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(EntityGolemPhysical.ID));
		registry.register(EntityType.Builder.<EntityGolemLightning>create(EntityGolemLightning::new, EntityClassification.MISC)
				.size(0.8F, 1.6F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(EntityGolemLightning.ID));
		registry.register(EntityType.Builder.<EntityGolemFire>create(EntityGolemFire::new, EntityClassification.MISC)
				.size(0.8F, 1.6F)
				.immuneToFire()
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(EntityGolemFire.ID));
		registry.register(EntityType.Builder.<EntityGolemEarth>create(EntityGolemEarth::new, EntityClassification.MISC)
				.size(0.8F, 1.6F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(EntityGolemEarth.ID));
		registry.register(EntityType.Builder.<EntityGolemIce>create(EntityGolemIce::new, EntityClassification.MISC)
				.size(0.8F, 1.6F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(EntityGolemIce.ID));
		registry.register(EntityType.Builder.<EntityGolemWind>create(EntityGolemWind::new, EntityClassification.MISC)
				.size(0.8F, 1.6F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(EntityGolemWind.ID));
		registry.register(EntityType.Builder.<EntityGolemEnder>create(EntityGolemEnder::new, EntityClassification.MISC)
				.size(0.8F, 1.6F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(EntityGolemEnder.ID));
		
		EntityType<EntityKoid> koidType = EntityType.Builder.<EntityKoid>create(EntityKoid::new, EntityClassification.MONSTER)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(0.8F, 1F)
			.build("");
		koidType.setRegistryName(EntityKoid.ID);
		registry.register(koidType);
		
		registry.register(EntityType.Builder.<EntityDragonRed>create(EntityDragonRed::new, EntityClassification.MISC)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(EntityDragonRed.GetBodyWidth(), EntityDragonRed.GetBodyHeight())
				.immuneToFire()
			.build("").setRegistryName(EntityDragonRed.ID));
		registry.register(EntityType.Builder.<EntityDragonRed.DragonBodyPart>create(EntityDragonRed.DragonBodyPart::new, EntityClassification.MISC)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(EntityDragonRed.GetBodyWidth(), EntityDragonRed.GetBodyHeight())
				.immuneToFire()
			.build("").setRegistryName(EntityDragonRed.DragonBodyPart.ID));
		
		EntityType<EntityTameDragonRed> tameDragonType = EntityType.Builder.<EntityTameDragonRed>create(EntityTameDragonRed::new, EntityClassification.MONSTER)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(6F * .4F, 4.6F * .6F)
				.immuneToFire()
			.build("");
		tameDragonType.setRegistryName(EntityTameDragonRed.ID);
		registry.register(tameDragonType);
		
		EntityType<EntityShadowDragonRed> shadowRedDragonType = EntityType.Builder.<EntityShadowDragonRed>create(EntityShadowDragonRed::new, EntityClassification.MONSTER)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(4F * .6F, 3F * .6F)
				.immuneToFire()
			.build("");
		shadowRedDragonType.setRegistryName(EntityShadowDragonRed.ID);
		registry.register(shadowRedDragonType);
		
		registry.register(EntityType.Builder.<EntityDragonEgg>create(EntityDragonEgg::new, EntityClassification.MISC)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(.45f, .5f)
			.build("").setRegistryName(EntityDragonEgg.ID));
		registry.register(EntityType.Builder.<EntityPlantBoss>create(EntityPlantBoss::new, EntityClassification.MISC)
				.size(7, 4)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(EntityPlantBoss.ID));
		registry.register(EntityType.Builder.<EntityPlantBoss.PlantBossBody>create(EntityPlantBoss.PlantBossBody::new, EntityClassification.MISC)
				.size(4.25f /*width 3, but rotates. sqrt(3^2+3^2) = 4.24*/, 4)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(EntityPlantBoss.PlantBossBody.ID));
		registry.register(EntityType.Builder.<EntityPlantBoss.PlantBossLeafLimb>create(EntityPlantBoss.PlantBossLeafLimb::new, EntityClassification.MISC)
				.size(4, 4)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(EntityPlantBoss.PlantBossLeafLimb.ID));
		registry.register(EntityType.Builder.<EntityPlantBossBramble>create(EntityPlantBossBramble::new, EntityClassification.MISC)
				.size(.5f, .75f)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(EntityPlantBossBramble.ID));
		
		EntityType<EntitySprite> spriteType = EntityType.Builder.<EntitySprite>create(EntitySprite::new, EntityClassification.MONSTER)
				.size(1F, 1.75F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("");
		spriteType.setRegistryName(EntitySprite.ID);
		
		EntityType<EntityLux> luxType = EntityType.Builder.<EntityLux>create(EntityLux::new, EntityClassification.CREATURE)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(.5F, .5F)
			.build("");
		luxType.setRegistryName(EntityLux.ID);
		registry.register(luxType);
		
		EntityType<EntityWisp> wispType = EntityType.Builder.<EntityWisp>create(EntityWisp::new, EntityClassification.CREATURE)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(.75F, .75F)
			.build("");
		wispType.setRegistryName(EntityWisp.ID);
		registry.register(wispType);
		registry.register(spriteType);
		
		EntityType<EntityWillo> willoType = EntityType.Builder.<EntityWillo>create(EntityWillo::new, EntityClassification.MONSTER)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(.75F, .75F)
			.build("");
		willoType.setRegistryName(EntityWillo.ID);
		registry.register(willoType);
		
		registry.register(EntityType.Builder.<EntityArcaneWolf>create(EntityArcaneWolf::new, EntityClassification.MISC)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(0.7F, 0.95F)
			.build("").setRegistryName(EntityArcaneWolf.ID));

		
    	registry.register(EntityType.Builder.<EntitySpellProjectile>create(EntitySpellProjectile::new, EntityClassification.MISC)
    			.size(0.3125F, 0.3125F)
    			.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
    		.build("").setRegistryName(EntitySpellProjectile.ID));
		registry.register(EntityType.Builder.<EntityChakramSpellSaucer>create(EntityChakramSpellSaucer::new, EntityClassification.MISC)
				.size(1F, .2F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(EntityChakramSpellSaucer.ID));
		registry.register(EntityType.Builder.<EntityCyclerSpellSaucer>create(EntityCyclerSpellSaucer::new, EntityClassification.MISC)
				.size(1F, .2F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(EntityCyclerSpellSaucer.ID));
		registry.register(EntityType.Builder.<EntitySwitchTrigger>create(EntitySwitchTrigger::new, EntityClassification.MISC)
				.size(.8f, 1.8f)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(EntitySwitchTrigger.ID));
		registry.register(EntityType.Builder.<EntityKeySwitchTrigger>create(EntityKeySwitchTrigger::new, EntityClassification.MISC)
				.size(.8f, 1.8f)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(EntityKeySwitchTrigger.ID));
		registry.register(EntityType.Builder.<NostrumTameLightning>create(NostrumTameLightning::new, EntityClassification.MISC)
				.size(0, 0)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(NostrumTameLightning.ID));
		registry.register(EntityType.Builder.<EntityHookShot>create(EntityHookShot::new, EntityClassification.MISC)
				.size(.2f, .2f)
				.immuneToFire()
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(EntityHookShot.ID));
		registry.register(EntityType.Builder.<EntitySpellBullet>create(EntitySpellBullet::new, EntityClassification.MISC)
				.size(0.3125F, 0.3125F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(EntitySpellBullet.ID));
		registry.register(EntityType.Builder.<EntitySpellMortar>create(EntitySpellMortar::new, EntityClassification.MISC)
				.size(0.75F, 0.75F)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
			.build("").setRegistryName(EntitySpellMortar.ID));
		registry.register(EntityType.Builder.<EntityAreaEffect>create(EntityAreaEffect::new, EntityClassification.MISC)
				.size(1f, .25f)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(EntityAreaEffect.ID));
		registry.register(EntityType.Builder.<EntityEnderRodBall>create(EntityEnderRodBall::new, EntityClassification.MISC)
				.size(1.5f, 1.5f)
				.setTrackingRange(128).setUpdateInterval(5).setShouldReceiveVelocityUpdates(false)
			.build("").setRegistryName(EntityEnderRodBall.ID));
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
		EntitySpawnPlacementRegistry.register(wisp, EntitySpawnPlacementRegistry.PlacementType.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntityWisp::canSpawnExtraCheck);
		EntitySpawnPlacementRegistry.register(willo, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntityWillo::canSpawnExtraCheck);
	}
	
	@SubscribeEvent
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
			event.getSpawns().getSpawner(EntityClassification.MONSTER).add(new MobSpawnInfo.Spawners(tameDragonRed, 4, 1, 1));
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
			event.getSpawns().getSpawner(EntityClassification.AMBIENT).add(new MobSpawnInfo.Spawners(wisp, nether ? 5 : 1, 1, 1));
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
		event.put(golemPhysical, EntityGolemPhysical.BuildAttributes().create());
		event.put(golemLightning, EntityGolemLightning.BuildAttributes().create());
		event.put(golemFire, EntityGolemFire.BuildAttributes().create());
		event.put(golemEarth, EntityGolemEarth.BuildAttributes().create());
		event.put(golemIce, EntityGolemIce.BuildAttributes().create());
		event.put(golemWind, EntityGolemWind.BuildAttributes().create());
		event.put(golemEnder, EntityGolemEnder.BuildAttributes().create());
		event.put(koid, EntityKoid.BuildAttributes().create());
		event.put(dragonRed, EntityDragonRed.BuildAttributes().create());
		// No attributes event.put(dragonRedBodyPart, EntityDragonRed.DragonBodyPart.BuildAttributes().create());
		event.put(tameDragonRed, EntityTameDragonRed.BuildAttributes().create());
		event.put(shadowDragonRed, EntityShadowDragonRed.BuildAttributes().create());
		event.put(dragonEgg, EntityDragonEgg.BuildAttributes().create());
		event.put(plantBoss, EntityPlantBoss.BuildAttributes().create());
		// No attributes event.put(plantBossBody, EntityPlantBoss.PlantBossBody.BuildAttributes().create());
		// No attributes event.put(plantBossLeaf, EntityPlantBoss.PlantBossLeafLimb.BuildAttributes().create());
		// No attributes event.put(plantBossBramble, EntityPlantBossBramble.BuildAttributes().create());
		event.put(sprite, EntitySprite.BuildAttributes().create());
		event.put(lux, EntityLux.BuildAttributes().create());
		event.put(wisp, EntityWisp.BuildAttributes().create());
		event.put(willo, EntityWillo.BuildAttributes().create());
		event.put(arcaneWolf, EntityArcaneWolf.BuildAttributes().create());
		// No attributes event.put(spellProjectile, EntitySpellProjectile.BuildAttributes().create());
		// No attributes event.put(chakramSpellSaucer, EntityChakramSpellSaucer.BuildAttributes().create());
		// No attributes event.put(cyclerSpellSaucer, EntityCyclerSpellSaucer.BuildAttributes().create());
		event.put(switchTrigger, EntitySwitchTrigger.BuildAttributes().create());
		// No attributes event.put(tameLightning, NostrumTameLightning.BuildAttributes().create());
		// No attributes event.put(hookShot, EntityHookShot.BuildAttributes().create());
		// No attributes event.put(spellBullet, EntitySpellBullet.BuildAttributes().create());
		// No attributes event.put(spellMortar, EntitySpellMortar.BuildAttributes().create());
		// No attributes event.put(areaEffect, EntityAreaEffect.BuildAttributes().create());
		event.put(keySwitchTrigger, EntityKeySwitchTrigger.BuildKeySwitchAttributes().create());
		// No attributes event.put(enderRodBall, EntityEnderRodBall.BuildAttributes().create());
		
	}
	
}
