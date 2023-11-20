package com.smanzana.nostrummagica.entity;

import java.util.Collection;
import java.util.List;

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

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.ZombiePigmanEntity;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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
		addSpawn(koidType, EntityClassification.MONSTER, 20, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.MAGICAL));
		addSpawn(koidType, EntityClassification.MONSTER, 20, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.FOREST));
		addSpawn(koidType, EntityClassification.MONSTER, 20, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.SNOWY));
		addSpawn(koidType, EntityClassification.MONSTER, 12, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.NETHER));
		addSpawn(koidType, EntityClassification.MONSTER, 20, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.SPOOKY));
		EntitySpawnPlacementRegistry.register(koidType, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canSpawnOn);
		
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
		addSpawn(tameDragonType, EntityClassification.MONSTER, 2, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.NETHER));
		EntitySpawnPlacementRegistry.register(tameDragonType, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ZombiePigmanEntity::func_223324_d);
		
		EntityType<EntityShadowDragonRed> shadowRedDragonType = EntityType.Builder.<EntityShadowDragonRed>create(EntityShadowDragonRed::new, EntityClassification.MONSTER)
				.setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(4F * .6F, 3F * .6F)
				.immuneToFire()
			.build("");
		shadowRedDragonType.setRegistryName(EntityShadowDragonRed.ID);
		registry.register(shadowRedDragonType);
		addSpawn(shadowRedDragonType, EntityClassification.MONSTER, 10, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.NETHER));
		EntitySpawnPlacementRegistry.register(shadowRedDragonType, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ZombiePigmanEntity::func_223324_d);
		
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
		registry.register(spriteType);
		addSpawn(spriteType, EntityClassification.MONSTER, 3, 1, 3, BiomeDictionary.getBiomes(BiomeDictionary.Type.NETHER));
		addSpawn(spriteType, EntityClassification.MONSTER, 10, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.MAGICAL));
		EntitySpawnPlacementRegistry.register(spriteType, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canSpawnOn);
		
		EntityType<EntityLux> luxType = EntityType.Builder.<EntityLux>create(EntityLux::new, EntityClassification.CREATURE)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(.5F, .5F)
			.build("");
		luxType.setRegistryName(EntityLux.ID);
		registry.register(luxType);
		addSpawn(luxType, EntityClassification.CREATURE, 20, 1, 3, BiomeDictionary.getBiomes(BiomeDictionary.Type.MAGICAL));
		addSpawn(luxType, EntityClassification.CREATURE, 15, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.FOREST));
		addSpawn(luxType, EntityClassification.CREATURE, 10, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.JUNGLE));
		
		EntityType<EntityWisp> wispType = EntityType.Builder.<EntityWisp>create(EntityWisp::new, EntityClassification.CREATURE)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(.75F, .75F)
			.build("");
		wispType.setRegistryName(EntityWisp.ID);
		registry.register(wispType);
		addSpawn(wispType, EntityClassification.AMBIENT, 1, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.MAGICAL));
		addSpawn(wispType, EntityClassification.AMBIENT, 1, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.FOREST));
		addSpawn(wispType, EntityClassification.AMBIENT, 1, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.SNOWY));
		addSpawn(wispType, EntityClassification.AMBIENT, 1, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.SPOOKY));
		addSpawn(wispType, EntityClassification.MONSTER, 5, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.NETHER));
		EntitySpawnPlacementRegistry.register(wispType, EntitySpawnPlacementRegistry.PlacementType.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntityWisp::canSpawnExtraCheck);
		
		EntityType<EntityWillo> willoType = EntityType.Builder.<EntityWillo>create(EntityWillo::new, EntityClassification.MONSTER)
				.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false)
				.size(.75F, .75F)
			.build("");
		willoType.setRegistryName(EntityWillo.ID);
		registry.register(willoType);
		addSpawn(willoType, EntityClassification.MONSTER, 18, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.MAGICAL));
		addSpawn(willoType, EntityClassification.MONSTER, 10, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.FOREST));
		addSpawn(willoType, EntityClassification.MONSTER, 8, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.SNOWY));
		addSpawn(willoType, EntityClassification.MONSTER, 14, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.SPOOKY));
		addSpawn(willoType, EntityClassification.MONSTER, 7, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.DRY));
		addSpawn(willoType, EntityClassification.MONSTER, 1, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.NETHER));
		
		EntitySpawnPlacementRegistry.register(willoType, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntityWillo::canSpawnExtraCheck);
		
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
	}
	
	private static void addSpawn(EntityType<? extends MobEntity> entityType, EntityClassification classification, int itemWeight, int minGroupCount, int maxGroupCount, Collection<Biome> biomes) {
		for (Biome biome : biomes) {
			List<Biome.SpawnListEntry> spawns = biome.getSpawns(classification);
			spawns.add(new Biome.SpawnListEntry(entityType, itemWeight, minGroupCount, maxGroupCount));
		}
	}
	
}
