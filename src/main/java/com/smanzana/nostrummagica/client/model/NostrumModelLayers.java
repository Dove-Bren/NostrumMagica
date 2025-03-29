package com.smanzana.nostrummagica.client.model;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NostrumModelLayers {

	public static final ModelLayerLocation ArcaneWolf = make("arcane_wolf");
	public static final ModelLayerLocation Lux = make("lux");
	public static final ModelLayerLocation DragonEgg = make("dragon_egg");
	public static final ModelLayerLocation FlightWings = make("flight_wings");
	public static final ModelLayerLocation Golem = make("golem");
	public static final ModelLayerLocation HookshotAnchor = make("hookshot_anchor");
	public static final ModelLayerLocation Plantboss = make("plantboss");
	public static final ModelLayerLocation PlantbossBramble = make("plantboss_bramble");
	public static final ModelLayerLocation PlantbossLeaf = make("plantboss_leaf");
	public static final ModelLayerLocation Willo = make("willo");
	public static final ModelLayerLocation WitchHat = make("witchhat");
	
	public static final ModelLayerLocation ElemArmor_0 = make("elemarmor_0");
	public static final ModelLayerLocation ElemArmor_1 = make("elemarmor_1");
	public static final ModelLayerLocation ElemArmor_2 = make("elemarmor_2");
	public static final ModelLayerLocation ElemArmor_3 = make("elemarmor_3");
	public static final ModelLayerLocation ElemArmor_4 = make("elemarmor_4");
	
	
	
	private static final ModelLayerLocation make(String name) {
		return make(name, "main");
	}
	
	private static final ModelLayerLocation make(String name, String layer) {
		return new ModelLayerLocation(NostrumMagica.Loc(name), layer);
	}
	
	@SubscribeEvent
	public static void registerModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(Lux, ModelLux::createLayer);
		event.registerLayerDefinition(DragonEgg, ModelDragonEgg::createLayer);
		event.registerLayerDefinition(FlightWings, ModelDragonFlightWings::createLayer);
		event.registerLayerDefinition(Golem, ModelGolem::createLayer);
		event.registerLayerDefinition(HookshotAnchor, ModelHookShot::createLayer);
		event.registerLayerDefinition(Plantboss, ModelPlantBoss::createLayer);
		event.registerLayerDefinition(PlantbossBramble, ModelPlantBossBramble::createLayer);
		event.registerLayerDefinition(PlantbossLeaf, ModelPlantBossLeaf::createLayer);
		event.registerLayerDefinition(Willo, ModelWillo::createLayer);
		event.registerLayerDefinition(WitchHat, ModelWitchHat::createLayer);
		event.registerLayerDefinition(ArcaneWolf, ModelArcaneWolf::createLayer);

		event.registerLayerDefinition(ElemArmor_0, () -> ModelEnchantedArmorBase.createLayer(0));
		event.registerLayerDefinition(ElemArmor_1, () -> ModelEnchantedArmorBase.createLayer(1));
		event.registerLayerDefinition(ElemArmor_2, () -> ModelEnchantedArmorBase.createLayer(2));
		event.registerLayerDefinition(ElemArmor_3, () -> ModelEnchantedArmorBase.createLayer(3));
		event.registerLayerDefinition(ElemArmor_4, () -> ModelEnchantedArmorBase.createLayer(4));
	}
}
