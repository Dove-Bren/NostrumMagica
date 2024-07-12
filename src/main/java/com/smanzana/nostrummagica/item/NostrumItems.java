package com.smanzana.nostrummagica.item;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.capabilities.EMagicTier;
import com.smanzana.nostrummagica.fluid.PoisonWaterFluid;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.item.SpellRune.AlterationSpellRune;
import com.smanzana.nostrummagica.item.SpellRune.ElementSpellRune;
import com.smanzana.nostrummagica.item.SpellRune.ShapeSpellRune;
import com.smanzana.nostrummagica.item.armor.DragonArmor;
import com.smanzana.nostrummagica.item.armor.DragonArmor.DragonArmorMaterial;
import com.smanzana.nostrummagica.item.armor.DragonArmor.DragonEquipmentSlot;
import com.smanzana.nostrummagica.item.armor.MagicArmor;
import com.smanzana.nostrummagica.item.armor.MagicArmorBase;
import com.smanzana.nostrummagica.item.armor.MagicEarthArmor;
import com.smanzana.nostrummagica.item.armor.MagicEnderArmor;
import com.smanzana.nostrummagica.item.armor.MagicFireArmor;
import com.smanzana.nostrummagica.item.armor.MagicIceArmor;
import com.smanzana.nostrummagica.item.armor.MagicLightningArmor;
import com.smanzana.nostrummagica.item.armor.MagicPhysicalArmor;
import com.smanzana.nostrummagica.item.armor.MagicWindArmor;
import com.smanzana.nostrummagica.item.equipment.AspectedEarthWeapon;
import com.smanzana.nostrummagica.item.equipment.AspectedEnderWeapon;
import com.smanzana.nostrummagica.item.equipment.AspectedFireWeapon;
import com.smanzana.nostrummagica.item.equipment.AspectedPhysicalWeapon;
import com.smanzana.nostrummagica.item.equipment.AspectedWeapon;
import com.smanzana.nostrummagica.item.equipment.CasterWandItem;
import com.smanzana.nostrummagica.item.equipment.HookshotItem;
import com.smanzana.nostrummagica.item.equipment.MageBlade;
import com.smanzana.nostrummagica.item.equipment.MageStaff;
import com.smanzana.nostrummagica.item.equipment.MagicSwordBase;
import com.smanzana.nostrummagica.item.equipment.MirrorShield;
import com.smanzana.nostrummagica.item.equipment.MirrorShieldImproved;
import com.smanzana.nostrummagica.item.equipment.ReagentBag;
import com.smanzana.nostrummagica.item.equipment.RuneBag;
import com.smanzana.nostrummagica.item.equipment.SoulDagger;
import com.smanzana.nostrummagica.item.equipment.ThanoPendant;
import com.smanzana.nostrummagica.item.equipment.ThanosStaff;
import com.smanzana.nostrummagica.item.equipment.WarlockSword;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.SpellComponentWrapper;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.block.Blocks;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumItems {
	
	// Not sure where to put these...
	protected static final String ID_EARTH = "earth";
	protected static final String ID_ENDER = "ender";
	protected static final String ID_FIRE = "fire";
	protected static final String ID_ICE = "ice";
	protected static final String ID_LIGHTNING = "lightning";
	protected static final String ID_PHYSICAL = "physical";
	protected static final String ID_WIND = "wind";
	
	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem; // TODO could clean up; is just a BlockItem
	@ObjectHolder(ArcaneWolfSoulItem.ID) public static ArcaneWolfSoulItem arcaneWolfSoulItem;
	@ObjectHolder(BlankScroll.ID) public static BlankScroll blankScroll;
	@ObjectHolder(ChalkItem.ID) public static ChalkItem chalkItem;
	@ObjectHolder(DragonArmor.ID_HELM_GOLD) public static DragonArmor dragonArmorHelmGold;
	@ObjectHolder(DragonArmor.ID_HELM_IRON) public static DragonArmor dragonArmorHelmIron;
	@ObjectHolder(DragonArmor.ID_HELM_DIAMOND) public static DragonArmor dragonArmorHelmDiamond;
	@ObjectHolder(DragonArmor.ID_BODY_GOLD) public static DragonArmor dragonArmorBodyGold;
	@ObjectHolder(DragonArmor.ID_BODY_IRON) public static DragonArmor dragonArmorBodyIron;
	@ObjectHolder(DragonArmor.ID_BODY_DIAMOND) public static DragonArmor dragonArmorBodyDiamond;
//	@ObjectHolder(DragonArmor.ID_WINGS_GOLD) public static DragonArmor dragonArmorWingsGold;
//	@ObjectHolder(DragonArmor.ID_WINGS_IRON) public static DragonArmor dragonArmorWingsIron;
//	@ObjectHolder(DragonArmor.ID_WINGS_DIAMOND) public static DragonArmor dragonArmorWingsDiamond;
//	@ObjectHolder(DragonArmor.ID_CREST_GOLD) public static DragonArmor dragonArmorCrestGold;
//	@ObjectHolder(DragonArmor.ID_CREST_IRON) public static DragonArmor dragonArmorCrestIron;
//	@ObjectHolder(DragonArmor.ID_CREST_DIAMOND) public static DragonArmor dragonArmorCrestDiamond;
	@ObjectHolder(DragonEgg.ID) public static DragonEgg dragonEgg;
	@ObjectHolder(DragonEggFragment.ID) public static DragonEggFragment dragonEggFragment;
	@ObjectHolder(DragonSoulItem.ID) public static DragonSoulItem dragonSoulItem;
	
	@ObjectHolder(MagicEarthArmor.ID_HELM_NOVICE) public static MagicEarthArmor enchantedArmorEarthHeadNovice;
	@ObjectHolder(MagicEarthArmor.ID_HELM_ADEPT) public static MagicEarthArmor enchantedArmorEarthHeadAdept;
	@ObjectHolder(MagicEarthArmor.ID_HELM_MASTER) public static MagicEarthArmor enchantedArmorEarthHeadMaster;
	@ObjectHolder(MagicEarthArmor.ID_HELM_TRUE) public static MagicEarthArmor enchantedArmorEarthHeadTrue;
	@ObjectHolder(MagicEarthArmor.ID_CHEST_NOVICE) public static MagicEarthArmor enchantedArmorEarthChestNovice;
	@ObjectHolder(MagicEarthArmor.ID_CHEST_ADEPT) public static MagicEarthArmor enchantedArmorEarthChestAdept;
	@ObjectHolder(MagicEarthArmor.ID_CHEST_MASTER) public static MagicEarthArmor enchantedArmorEarthChestMaster;
	@ObjectHolder(MagicEarthArmor.ID_CHEST_TRUE) public static MagicEarthArmor enchantedArmorEarthChestTrue;
	@ObjectHolder(MagicEarthArmor.ID_LEGS_NOVICE) public static MagicEarthArmor enchantedArmorEarthLegsNovice;
	@ObjectHolder(MagicEarthArmor.ID_LEGS_ADEPT) public static MagicEarthArmor enchantedArmorEarthLegsAdept;
	@ObjectHolder(MagicEarthArmor.ID_LEGS_MASTER) public static MagicEarthArmor enchantedArmorEarthLegsMaster;
	@ObjectHolder(MagicEarthArmor.ID_LEGS_TRUE) public static MagicEarthArmor enchantedArmorEarthLegsTrue;
	@ObjectHolder(MagicEarthArmor.ID_FEET_NOVICE) public static MagicEarthArmor enchantedArmorEarthFeetNovice;
	@ObjectHolder(MagicEarthArmor.ID_FEET_ADEPT) public static MagicEarthArmor enchantedArmorEarthFeetAdept;
	@ObjectHolder(MagicEarthArmor.ID_FEET_MASTER) public static MagicEarthArmor enchantedArmorEarthFeetMaster;
	@ObjectHolder(MagicEarthArmor.ID_FEET_TRUE) public static MagicEarthArmor enchantedArmorEarthFeetTrue;
	
	@ObjectHolder(MagicEnderArmor.ID_HELM_NOVICE) public static MagicEnderArmor enchantedArmorEnderHeadNovice;
	@ObjectHolder(MagicEnderArmor.ID_HELM_ADEPT) public static MagicEnderArmor enchantedArmorEnderHeadAdept;
	@ObjectHolder(MagicEnderArmor.ID_HELM_MASTER) public static MagicEnderArmor enchantedArmorEnderHeadMaster;
	@ObjectHolder(MagicEnderArmor.ID_HELM_TRUE) public static MagicEnderArmor enchantedArmorEnderHeadTrue;
	@ObjectHolder(MagicEnderArmor.ID_CHEST_NOVICE) public static MagicEnderArmor enchantedArmorEnderChestNovice;
	@ObjectHolder(MagicEnderArmor.ID_CHEST_ADEPT) public static MagicEnderArmor enchantedArmorEnderChestAdept;
	@ObjectHolder(MagicEnderArmor.ID_CHEST_MASTER) public static MagicEnderArmor enchantedArmorEnderChestMaster;
	@ObjectHolder(MagicEnderArmor.ID_CHEST_TRUE) public static MagicEnderArmor enchantedArmorEnderChestTrue;
	@ObjectHolder(MagicEnderArmor.ID_LEGS_NOVICE) public static MagicEnderArmor enchantedArmorEnderLegsNovice;
	@ObjectHolder(MagicEnderArmor.ID_LEGS_ADEPT) public static MagicEnderArmor enchantedArmorEnderLegsAdept;
	@ObjectHolder(MagicEnderArmor.ID_LEGS_MASTER) public static MagicEnderArmor enchantedArmorEnderLegsMaster;
	@ObjectHolder(MagicEnderArmor.ID_LEGS_TRUE) public static MagicEnderArmor enchantedArmorEnderLegsTrue;
	@ObjectHolder(MagicEnderArmor.ID_FEET_NOVICE) public static MagicEnderArmor enchantedArmorEnderFeetNovice;
	@ObjectHolder(MagicEnderArmor.ID_FEET_ADEPT) public static MagicEnderArmor enchantedArmorEnderFeetAdept;
	@ObjectHolder(MagicEnderArmor.ID_FEET_MASTER) public static MagicEnderArmor enchantedArmorEnderFeetMaster;
	@ObjectHolder(MagicEnderArmor.ID_FEET_TRUE) public static MagicEnderArmor enchantedArmorEnderFeetTrue;
	
	@ObjectHolder(MagicFireArmor.ID_HELM_NOVICE) public static MagicFireArmor enchantedArmorFireHeadNovice;
	@ObjectHolder(MagicFireArmor.ID_HELM_ADEPT) public static MagicFireArmor enchantedArmorFireHeadAdept;
	@ObjectHolder(MagicFireArmor.ID_HELM_MASTER) public static MagicFireArmor enchantedArmorFireHeadMaster;
	@ObjectHolder(MagicFireArmor.ID_HELM_TRUE) public static MagicFireArmor enchantedArmorFireHeadTrue;
	@ObjectHolder(MagicFireArmor.ID_CHEST_NOVICE) public static MagicFireArmor enchantedArmorFireChestNovice;
	@ObjectHolder(MagicFireArmor.ID_CHEST_ADEPT) public static MagicFireArmor enchantedArmorFireChestAdept;
	@ObjectHolder(MagicFireArmor.ID_CHEST_MASTER) public static MagicFireArmor enchantedArmorFireChestMaster;
	@ObjectHolder(MagicFireArmor.ID_CHEST_TRUE) public static MagicFireArmor enchantedArmorFireChestTrue;
	@ObjectHolder(MagicFireArmor.ID_LEGS_NOVICE) public static MagicFireArmor enchantedArmorFireLegsNovice;
	@ObjectHolder(MagicFireArmor.ID_LEGS_ADEPT) public static MagicFireArmor enchantedArmorFireLegsAdept;
	@ObjectHolder(MagicFireArmor.ID_LEGS_MASTER) public static MagicFireArmor enchantedArmorFireLegsMaster;
	@ObjectHolder(MagicFireArmor.ID_LEGS_TRUE) public static MagicFireArmor enchantedArmorFireLegsTrue;
	@ObjectHolder(MagicFireArmor.ID_FEET_NOVICE) public static MagicFireArmor enchantedArmorFireFeetNovice;
	@ObjectHolder(MagicFireArmor.ID_FEET_ADEPT) public static MagicFireArmor enchantedArmorFireFeetAdept;
	@ObjectHolder(MagicFireArmor.ID_FEET_MASTER) public static MagicFireArmor enchantedArmorFireFeetMaster;
	@ObjectHolder(MagicFireArmor.ID_FEET_TRUE) public static MagicFireArmor enchantedArmorFireFeetTrue;
	
//	@ObjectHolder(EnchantedIceArmor.ID_HELM_NOVICE) public static EnchantedIceArmor enchantedArmorIceHeadNovice;
//	@ObjectHolder(EnchantedIceArmor.ID_HELM_ADEPT) public static EnchantedIceArmor enchantedArmorIceHeadAdept;
//	@ObjectHolder(EnchantedIceArmor.ID_HELM_MASTER) public static EnchantedIceArmor enchantedArmorIceHeadMaster;
	@ObjectHolder(MagicIceArmor.ID_HELM_TRUE) public static MagicIceArmor enchantedArmorIceHeadTrue;
//	@ObjectHolder(EnchantedIceArmor.ID_CHEST_NOVICE) public static EnchantedIceArmor enchantedArmorIceChestNovice;
//	@ObjectHolder(EnchantedIceArmor.ID_CHEST_ADEPT) public static EnchantedIceArmor enchantedArmorIceChestAdept;
//	@ObjectHolder(EnchantedIceArmor.ID_CHEST_MASTER) public static EnchantedIceArmor enchantedArmorIceChestMaster;
	@ObjectHolder(MagicIceArmor.ID_CHEST_TRUE) public static MagicIceArmor enchantedArmorIceChestTrue;
//	@ObjectHolder(EnchantedIceArmor.ID_LEGS_NOVICE) public static EnchantedIceArmor enchantedArmorIceLegsNovice;
//	@ObjectHolder(EnchantedIceArmor.ID_LEGS_ADEPT) public static EnchantedIceArmor enchantedArmorIceLegsAdept;
//	@ObjectHolder(EnchantedIceArmor.ID_LEGS_MASTER) public static EnchantedIceArmor enchantedArmorIceLegsMaster;
	@ObjectHolder(MagicIceArmor.ID_LEGS_TRUE) public static MagicIceArmor enchantedArmorIceLegsTrue;
//	@ObjectHolder(EnchantedIceArmor.ID_FEET_NOVICE) public static EnchantedIceArmor enchantedArmorIceFeetNovice;
//	@ObjectHolder(EnchantedIceArmor.ID_FEET_ADEPT) public static EnchantedIceArmor enchantedArmorIceFeetAdept;
//	@ObjectHolder(EnchantedIceArmor.ID_FEET_MASTER) public static EnchantedIceArmor enchantedArmorIceFeetMaster;
	@ObjectHolder(MagicIceArmor.ID_FEET_TRUE) public static MagicIceArmor enchantedArmorIceFeetTrue;
	
//	@ObjectHolder(EnchantedLightningArmor.ID_HELM_NOVICE) public static EnchantedLightningArmor enchantedArmorLightningHeadNovice;
//	@ObjectHolder(EnchantedLightningArmor.ID_HELM_ADEPT) public static EnchantedLightningArmor enchantedArmorLightningHeadAdept;
//	@ObjectHolder(EnchantedLightningArmor.ID_HELM_MASTER) public static EnchantedLightningArmor enchantedArmorLightningHeadMaster;
	@ObjectHolder(MagicLightningArmor.ID_HELM_TRUE) public static MagicLightningArmor enchantedArmorLightningHeadTrue;
//	@ObjectHolder(EnchantedLightningArmor.ID_CHEST_NOVICE) public static EnchantedLightningArmor enchantedArmorLightningChestNovice;
//	@ObjectHolder(EnchantedLightningArmor.ID_CHEST_ADEPT) public static EnchantedLightningArmor enchantedArmorLightningChestAdept;
//	@ObjectHolder(EnchantedLightningArmor.ID_CHEST_MASTER) public static EnchantedLightningArmor enchantedArmorLightningChestMaster;
	@ObjectHolder(MagicLightningArmor.ID_CHEST_TRUE) public static MagicLightningArmor enchantedArmorLightningChestTrue;
//	@ObjectHolder(EnchantedLightningArmor.ID_LEGS_NOVICE) public static EnchantedLightningArmor enchantedArmorLightningLegsNovice;
//	@ObjectHolder(EnchantedLightningArmor.ID_LEGS_ADEPT) public static EnchantedLightningArmor enchantedArmorLightningLegsAdept;
//	@ObjectHolder(EnchantedLightningArmor.ID_LEGS_MASTER) public static EnchantedLightningArmor enchantedArmorLightningLegsMaster;
	@ObjectHolder(MagicLightningArmor.ID_LEGS_TRUE) public static MagicLightningArmor enchantedArmorLightningLegsTrue;
//	@ObjectHolder(EnchantedLightningArmor.ID_FEET_NOVICE) public static EnchantedLightningArmor enchantedArmorLightningFeetNovice;
//	@ObjectHolder(EnchantedLightningArmor.ID_FEET_ADEPT) public static EnchantedLightningArmor enchantedArmorLightningFeetAdept;
//	@ObjectHolder(EnchantedLightningArmor.ID_FEET_MASTER) public static EnchantedLightningArmor enchantedArmorLightningFeetMaster;
	@ObjectHolder(MagicLightningArmor.ID_FEET_TRUE) public static MagicLightningArmor enchantedArmorLightningFeetTrue;
	
	@ObjectHolder(MagicPhysicalArmor.ID_HELM_NOVICE) public static MagicPhysicalArmor enchantedArmorPhysicalHeadNovice;
	@ObjectHolder(MagicPhysicalArmor.ID_HELM_ADEPT) public static MagicPhysicalArmor enchantedArmorPhysicalHeadAdept;
	@ObjectHolder(MagicPhysicalArmor.ID_HELM_MASTER) public static MagicPhysicalArmor enchantedArmorPhysicalHeadMaster;
	@ObjectHolder(MagicPhysicalArmor.ID_HELM_TRUE) public static MagicPhysicalArmor enchantedArmorPhysicalHeadTrue;
	@ObjectHolder(MagicPhysicalArmor.ID_CHEST_NOVICE) public static MagicPhysicalArmor enchantedArmorPhysicalChestNovice;
	@ObjectHolder(MagicPhysicalArmor.ID_CHEST_ADEPT) public static MagicPhysicalArmor enchantedArmorPhysicalChestAdept;
	@ObjectHolder(MagicPhysicalArmor.ID_CHEST_MASTER) public static MagicPhysicalArmor enchantedArmorPhysicalChestMaster;
	@ObjectHolder(MagicPhysicalArmor.ID_CHEST_TRUE) public static MagicPhysicalArmor enchantedArmorPhysicalChestTrue;
	@ObjectHolder(MagicPhysicalArmor.ID_LEGS_NOVICE) public static MagicPhysicalArmor enchantedArmorPhysicalLegsNovice;
	@ObjectHolder(MagicPhysicalArmor.ID_LEGS_ADEPT) public static MagicPhysicalArmor enchantedArmorPhysicalLegsAdept;
	@ObjectHolder(MagicPhysicalArmor.ID_LEGS_MASTER) public static MagicPhysicalArmor enchantedArmorPhysicalLegsMaster;
	@ObjectHolder(MagicPhysicalArmor.ID_LEGS_TRUE) public static MagicPhysicalArmor enchantedArmorPhysicalLegsTrue;
	@ObjectHolder(MagicPhysicalArmor.ID_FEET_NOVICE) public static MagicPhysicalArmor enchantedArmorPhysicalFeetNovice;
	@ObjectHolder(MagicPhysicalArmor.ID_FEET_ADEPT) public static MagicPhysicalArmor enchantedArmorPhysicalFeetAdept;
	@ObjectHolder(MagicPhysicalArmor.ID_FEET_MASTER) public static MagicPhysicalArmor enchantedArmorPhysicalFeetMaster;
	@ObjectHolder(MagicPhysicalArmor.ID_FEET_TRUE) public static MagicPhysicalArmor enchantedArmorPhysicalFeetTrue;
	
//	@ObjectHolder(EnchantedWindArmor.ID_HELM_NOVICE) public static EnchantedWindArmor enchantedArmorWindHeadNovice;
//	@ObjectHolder(EnchantedWindArmor.ID_HELM_ADEPT) public static EnchantedWindArmor enchantedArmorWindHeadAdept;
//	@ObjectHolder(EnchantedWindArmor.ID_HELM_MASTER) public static EnchantedWindArmor enchantedArmorWindHeadMaster;
	@ObjectHolder(MagicWindArmor.ID_HELM_TRUE) public static MagicWindArmor enchantedArmorWindHeadTrue;
//	@ObjectHolder(EnchantedWindArmor.ID_CHEST_NOVICE) public static EnchantedWindArmor enchantedArmorWindChestNovice;
//	@ObjectHolder(EnchantedWindArmor.ID_CHEST_ADEPT) public static EnchantedWindArmor enchantedArmorWindChestAdept;
//	@ObjectHolder(EnchantedWindArmor.ID_CHEST_MASTER) public static EnchantedWindArmor enchantedArmorWindChestMaster;
	@ObjectHolder(MagicWindArmor.ID_CHEST_TRUE) public static MagicWindArmor enchantedArmorWindChestTrue;
//	@ObjectHolder(EnchantedWindArmor.ID_LEGS_NOVICE) public static EnchantedWindArmor enchantedArmorWindLegsNovice;
//	@ObjectHolder(EnchantedWindArmor.ID_LEGS_ADEPT) public static EnchantedWindArmor enchantedArmorWindLegsAdept;
//	@ObjectHolder(EnchantedWindArmor.ID_LEGS_MASTER) public static EnchantedWindArmor enchantedArmorWindLegsMaster;
	@ObjectHolder(MagicWindArmor.ID_LEGS_TRUE) public static MagicWindArmor enchantedArmorWindLegsTrue;
//	@ObjectHolder(EnchantedWindArmor.ID_FEET_NOVICE) public static EnchantedWindArmor enchantedArmorWindFeetNovice;
//	@ObjectHolder(EnchantedWindArmor.ID_FEET_ADEPT) public static EnchantedWindArmor enchantedArmorWindFeetAdept;
//	@ObjectHolder(EnchantedWindArmor.ID_FEET_MASTER) public static EnchantedWindArmor enchantedArmorWindFeetMaster;
	@ObjectHolder(MagicWindArmor.ID_FEET_TRUE) public static MagicWindArmor enchantedArmorWindFeetTrue;
	
	@ObjectHolder(AspectedWeapon.ID_ICE_NOVICE) public static AspectedWeapon enchantedWeaponIceNovice;
	@ObjectHolder(AspectedWeapon.ID_ICE_ADEPT) public static AspectedWeapon enchantedWeaponIceAdept;
	@ObjectHolder(AspectedWeapon.ID_ICE_MASTER) public static AspectedWeapon enchantedWeaponIceMaster;
	@ObjectHolder(AspectedWeapon.ID_LIGHTNING_NOVICE) public static AspectedWeapon enchantedWeaponLightningNovice;
	@ObjectHolder(AspectedWeapon.ID_LIGHTNING_ADEPT) public static AspectedWeapon enchantedWeaponLightningAdept;
	@ObjectHolder(AspectedWeapon.ID_LIGHTNING_MASTER) public static AspectedWeapon enchantedWeaponLightningMaster;
	@ObjectHolder(AspectedWeapon.ID_WIND_NOVICE) public static AspectedWeapon enchantedWeaponWindNovice;
	@ObjectHolder(AspectedWeapon.ID_WIND_ADEPT) public static AspectedWeapon enchantedWeaponWindAdept;
	@ObjectHolder(AspectedWeapon.ID_WIND_MASTER) public static AspectedWeapon enchantedWeaponWindMaster;
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_EARTH) public static EssenceItem essenceEarth;
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_FIRE) public static EssenceItem essenceFire;
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_ICE) public static EssenceItem essenceIce;
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_ENDER) public static EssenceItem essenceEnder;
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_LIGHTNING) public static EssenceItem essenceLightning;
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_PHYSICAL) public static EssenceItem essencePhysical;
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_WIND) public static EssenceItem essenceWind;
	@ObjectHolder(HookshotItem.ID_PREFIX + "weak") public static HookshotItem hookshotWeak;
	@ObjectHolder(HookshotItem.ID_PREFIX + "medium") public static HookshotItem hookshotMedium;
	@ObjectHolder(HookshotItem.ID_PREFIX + "strong") public static HookshotItem hookshotStrong;
	@ObjectHolder(HookshotItem.ID_PREFIX + "claw") public static HookshotItem hookshotClaw;
	@ObjectHolder(InfusedGemItem.ID_PREFIX + "unattuned") public static InfusedGemItem infusedGemUnattuned;
	@ObjectHolder(InfusedGemItem.ID_PREFIX + ID_EARTH) public static InfusedGemItem infusedGemEarth;
	@ObjectHolder(InfusedGemItem.ID_PREFIX + ID_ENDER) public static InfusedGemItem infusedGemEnder;
	@ObjectHolder(InfusedGemItem.ID_PREFIX + ID_FIRE) public static InfusedGemItem infusedGemFire;
	@ObjectHolder(InfusedGemItem.ID_PREFIX + ID_ICE) public static InfusedGemItem infusedGemIce;
	@ObjectHolder(InfusedGemItem.ID_PREFIX + ID_LIGHTNING) public static InfusedGemItem infusedGemLightning;
	@ObjectHolder(InfusedGemItem.ID_PREFIX + ID_WIND) public static InfusedGemItem infusedGemWind;
	@ObjectHolder(MagicArmorBase.ID_HELM) public static MagicArmorBase magicArmorBaseHelm;
	@ObjectHolder(MagicArmorBase.ID_CHEST) public static MagicArmorBase magicArmorBaseChest;
	@ObjectHolder(MagicArmorBase.ID_LEGS) public static MagicArmorBase magicArmorBaseLegs;
	@ObjectHolder(MagicArmorBase.ID_FEET) public static MagicArmorBase magicArmorBaseFeet;
	@ObjectHolder(MageStaff.ID) public static MageStaff mageStaff;
	@ObjectHolder(MagicCharm.ID_PREFIX + ID_EARTH) public static MagicCharm magicCharmEarth;
	@ObjectHolder(MagicCharm.ID_PREFIX + ID_ENDER) public static MagicCharm magicCharmEnder;
	@ObjectHolder(MagicCharm.ID_PREFIX + ID_FIRE) public static MagicCharm magicCharmFire;
	@ObjectHolder(MagicCharm.ID_PREFIX + ID_ICE) public static MagicCharm magicCharmIce;
	@ObjectHolder(MagicCharm.ID_PREFIX + ID_LIGHTNING) public static MagicCharm magicCharmLightning;
	@ObjectHolder(MagicCharm.ID_PREFIX + ID_PHYSICAL) public static MagicCharm magicCharmPhysical;
	@ObjectHolder(MagicCharm.ID_PREFIX + ID_WIND) public static MagicCharm magicCharmWind;
	@ObjectHolder(MagicSwordBase.ID) public static MagicSwordBase magicSwordBase;
	@ObjectHolder(MasteryOrb.ID) public static MasteryOrb masteryOrb;
	@ObjectHolder(MirrorShield.ID) public static MirrorShield mirrorShield;
	@ObjectHolder(MirrorShieldImproved.ID) public static MirrorShieldImproved mirrorShieldImproved;
	@ObjectHolder(ResourceItem.ID_TOKEN) public static ResourceItem resourceToken;
	@ObjectHolder(ResourceItem.ID_PENDANT_LEFT) public static ResourceItem resourcePendantLeft;
	@ObjectHolder(ResourceItem.ID_PENDANT_RIGHT) public static ResourceItem resourcePendantRight;
	@ObjectHolder(ResourceItem.ID_SLAB_FIERCE) public static ResourceItem resourceSlabFierce;
	@ObjectHolder(ResourceItem.ID_SLAB_KIND) public static ResourceItem resourceSlabKind;
	@ObjectHolder(ResourceItem.ID_SLAB_BALANCED) public static ResourceItem resourceSlabBalanced;
	@ObjectHolder(ResourceItem.ID_SPRITE_CORE) public static ResourceItem resourceSpriteCore;
	@ObjectHolder(ResourceItem.ID_ENDER_BRISTLE) public static ResourceItem resourceEnderBristle;
	@ObjectHolder(ResourceItem.ID_WISP_PEBBLE) public static ResourceItem resourceWispPebble;
	@ObjectHolder(ResourceItem.ID_MANA_LEAF) public static ResourceItem resourceManaLeaf;
	@ObjectHolder(ResourceItem.ID_EVIL_THISTLE) public static ResourceItem resourceEvilThistle;
	@ObjectHolder(ResourceItem.ID_DRAGON_WING) public static ResourceItem resourceDragonWing;
	@ObjectHolder(ResourceItem.ID_SKILL_OOZE) public static ResourceItem resourceSkillOoze;
	@ObjectHolder(ResourceItem.ID_SKILL_PENDANT) public static ResourceItem resourceSkillPendant;
	@ObjectHolder(ResourceItem.ID_SKILL_FLUTE) public static ResourceItem resourceSkillFlute;
	@ObjectHolder(ResourceCrystal.ID_CRYSTAL_SMALL) public static ResourceCrystal crystalSmall;
	@ObjectHolder(ResourceCrystal.ID_CRYSTAL_MEDIUM) public static ResourceCrystal crystalMedium;
	@ObjectHolder(ResourceCrystal.ID_CRYSTAL_LARGE) public static ResourceCrystal crystalLarge;
	@ObjectHolder(RoseItem.ID_BLOOD_ROSE) public static RoseItem roseBlood;
	@ObjectHolder(RoseItem.ID_ELDRICH_ROSE) public static RoseItem roseEldrich;
	@ObjectHolder(RoseItem.ID_PALE_ROSE) public static RoseItem rosePale;
	@ObjectHolder(SkillItem.ID_SKILL_MIRROR) public static SkillItem.Mirror skillMirror;
	@ObjectHolder(SkillItem.ID_SKILL_ENDER_PIN) public static SkillItem.EnderPin skillEnderPin;
	@ObjectHolder(SkillItem.ID_SKILL_SCROLL_SMALL) public static SkillItem.SmallScroll skillScrollSmall;
	@ObjectHolder(SkillItem.ID_SKILL_SCROLL_LARGE) public static SkillItem.LargeScroll skillScrollLarge;
	@ObjectHolder(PositionCrystal.ID) public static PositionCrystal positionCrystal;
	@ObjectHolder(PositionToken.ID) public static PositionToken positionToken;
	@ObjectHolder(ReagentItem.ID_MANDRAKE_ROOT) public static ReagentItem reagentMandrakeRoot;
	@ObjectHolder(ReagentItem.ID_SPIDER_SILK) public static ReagentItem reagentSpiderSilk;
	@ObjectHolder(ReagentItem.ID_BLACK_PEARL) public static ReagentItem reagentBlackPearl;
	@ObjectHolder(ReagentItem.ID_SKY_ASH) public static ReagentItem reagentSkyAsh;
	@ObjectHolder(ReagentItem.ID_GINSENG) public static ReagentItem reagentGinseng;
	@ObjectHolder(ReagentItem.ID_GRAVE_DUST) public static ReagentItem reagentGraveDust;
	@ObjectHolder(ReagentItem.ID_CRYSTABLOOM) public static ReagentItem reagentCrystabloom;
	@ObjectHolder(ReagentItem.ID_MANI_DUST) public static ReagentItem reagentManiDust;
	@ObjectHolder(ReagentBag.ID) public static ReagentBag reagentBag;
	@ObjectHolder(ReagentSeed.ID_MANDRAKE_SEED) public static ReagentSeed reagentSeedMandrake;
	@ObjectHolder(ReagentSeed.ID_GINSENG_SEED) public static ReagentSeed reagentSeedGinseng;
	@ObjectHolder(ReagentSeed.ID_ESSENCE_SEED) public static ReagentSeed reagentSeedEssence;
	@ObjectHolder(RuneBag.ID) public static RuneBag runeBag;
	@ObjectHolder(SoulDagger.ID) public static SoulDagger soulDagger;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_NOVICE) public static SpellPlate spellPlateNovice;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_ADVANCED) public static SpellPlate spellPlateAdvanced;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_COMBAT) public static SpellPlate spellPlateCombat;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_DEATH) public static SpellPlate spellPlateDeath;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_SPOOKY) public static SpellPlate spellPlateSpooky;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_MUTED) public static SpellPlate spellPlateMuted;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_LIVING) public static SpellPlate spellPlateLiving;
	@ObjectHolder(SpellScroll.ID) public static SpellScroll spellScroll;
	@ObjectHolder(SpellTome.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_NOVICE) public static SpellTome spellTomeNovice;
	@ObjectHolder(SpellTome.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_ADVANCED) public static SpellTome spellTomeAdvanced;
	@ObjectHolder(SpellTome.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_COMBAT) public static SpellTome spellTomeCombat;
	@ObjectHolder(SpellTome.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_DEATH) public static SpellTome spellTomeDeath;
	@ObjectHolder(SpellTome.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_SPOOKY) public static SpellTome spellTomeSpooky;
	@ObjectHolder(SpellTome.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_MUTED) public static SpellTome spellTomeMuted;
	@ObjectHolder(SpellTome.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_LIVING) public static SpellTome spellTomeLiving;
	@ObjectHolder(SpellTomePage.ID) public static SpellTomePage spellTomePage;
	@ObjectHolder(ThanoPendant.ID) public static ThanoPendant thanoPendant;
	@ObjectHolder(ThanosStaff.ID) public static ThanosStaff thanosStaff;
	@ObjectHolder(WarlockSword.ID) public static WarlockSword warlockSword;
	@ObjectHolder(WorldKeyItem.ID) public static WorldKeyItem worldKey;
	@ObjectHolder(FillItem.ID_AIR_ALL) public static FillItem fillDungeonAir;
	@ObjectHolder(FillItem.ID_WATER_ALL) public static FillItem fillWater;
	@ObjectHolder(FillItem.ID_WATER_DOWN) public static FillItem fillWaterLevel;
	@ObjectHolder(MageBlade.ID) public static MageBlade mageBlade;
	@ObjectHolder(AspectedFireWeapon.ID) public static AspectedFireWeapon flameRod;
	@ObjectHolder(AspectedPhysicalWeapon.ID) public static AspectedPhysicalWeapon deepMetalAxe;
	@ObjectHolder(AspectedEarthWeapon.ID) public static AspectedEarthWeapon earthPike;
	@ObjectHolder(AspectedEnderWeapon.ID) public static AspectedEnderWeapon enderRod;
	@ObjectHolder(CasterWandItem.ID) public static CasterWandItem casterWand;
	@ObjectHolder(SpellPatternTome.ID) public static SpellPatternTome spellPatternTome;
	@ObjectHolder(CopyWandItem.ID) public static CopyWandItem copyWand;
	@ObjectHolder(ResearchTranscriptItem.ID) public static ResearchTranscriptItem researchTranscript;
	@ObjectHolder(SeekingGem.ID) public static SeekingGem seekingGem;
	
	@ObjectHolder(PoisonWaterFluid.ID_BREAKABLE + "_bucket") public static BucketItem poisonWaterBucket;
	@ObjectHolder(PoisonWaterFluid.ID_UNBREAKABLE + "_bucket") public static BucketItem unbreakablePoisonWaterBucket;
	
	
	public static Item.Properties PropBase() {
		return new Item.Properties()
				.group(NostrumMagica.creativeTab)
				;
	}
	
	public static Item.Properties PropLowStack() {
		return PropBase()
				.maxStackSize(16);
	}
	
	public static Item.Properties PropUnstackable() {
		return PropBase()
				.maxStackSize(1);
	}
	
	public static Item.Properties PropEquipmentBase() {
		return new Item.Properties()
				.group(NostrumMagica.equipmentTab);
	}
	
	public static Item.Properties PropEquipment() {
		return PropEquipmentBase()
				.maxStackSize(1)
				;
	}
	
	public static Item.Properties PropTomeBase() {
		return new Item.Properties()
				.group(NostrumMagica.enhancementTab);
	}
	
	public static Item.Properties PropTomeUnstackable() {
		return PropTomeBase()
				.maxStackSize(1);
	}
	
	public static Item.Properties PropRuneBase() {
		return new Item.Properties()
				.group(NostrumMagica.runeTab);
	}
	
	public static Item.Properties PropDungeonBase() {
		return new Item.Properties()
				.group(NostrumMagica.dungeonTab);
	}
	
	public static Item.Properties PropDungeonUnstackable() {
		return PropDungeonBase()
				.maxStackSize(1);
	}
	
	private static final void register(IForgeRegistry<Item> registry, Item item) {
		registry.register(item);
		
		if (item instanceof ILoreTagged) {
			LoreRegistry.instance().register((ILoreTagged) item);
		}
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();
		register(registry, new AltarItem().setRegistryName(AltarItem.ID));
    	register(registry, new ArcaneWolfSoulItem().setRegistryName(ArcaneWolfSoulItem.ID));
    	register(registry, new BlankScroll().setRegistryName(BlankScroll.ID));
    	register(registry, new ChalkItem().setRegistryName(ChalkItem.ID));
    	register(registry, new DragonArmor(DragonEquipmentSlot.HELM, DragonArmorMaterial.GOLD).setRegistryName(DragonArmor.ID_HELM_GOLD));
    	register(registry, new DragonArmor(DragonEquipmentSlot.HELM, DragonArmorMaterial.IRON).setRegistryName(DragonArmor.ID_HELM_IRON));
    	register(registry, new DragonArmor(DragonEquipmentSlot.HELM, DragonArmorMaterial.DIAMOND).setRegistryName(DragonArmor.ID_HELM_DIAMOND));
    	register(registry, new DragonArmor(DragonEquipmentSlot.BODY, DragonArmorMaterial.GOLD).setRegistryName(DragonArmor.ID_BODY_GOLD));
    	register(registry, new DragonArmor(DragonEquipmentSlot.BODY, DragonArmorMaterial.IRON).setRegistryName(DragonArmor.ID_BODY_IRON));
    	register(registry, new DragonArmor(DragonEquipmentSlot.BODY, DragonArmorMaterial.DIAMOND).setRegistryName(DragonArmor.ID_BODY_DIAMOND));
//    	register(registry, new DragonArmor(DragonEquipmentSlot.WINGS, DragonArmorMaterial.GOLD).setRegistryName(DragonArmor.ID_WINGS_GOLD));
//    	register(registry, new DragonArmor(DragonEquipmentSlot.WINGS, DragonArmorMaterial.IRON).setRegistryName(DragonArmor.ID_WINGS_IRON));
//    	register(registry, new DragonArmor(DragonEquipmentSlot.WINGS, DragonArmorMaterial.DIAMOND).setRegistryName(DragonArmor.ID_WINGS_DIAMOND));
//    	register(registry, new DragonArmor(DragonEquipmentSlot.CREST, DragonArmorMaterial.GOLD).setRegistryName(DragonArmor.ID_CREST_GOLD));
//    	register(registry, new DragonArmor(DragonEquipmentSlot.CREST, DragonArmorMaterial.IRON).setRegistryName(DragonArmor.ID_CREST_IRON));
//    	register(registry, new DragonArmor(DragonEquipmentSlot.CREST, DragonArmorMaterial.DIAMOND).setRegistryName(DragonArmor.ID_CREST_DIAMOND));
    	register(registry, new DragonEgg().setRegistryName(DragonEgg.ID));
    	register(registry, new DragonEggFragment().setRegistryName(DragonEggFragment.ID));
    	register(registry, new DragonSoulItem().setRegistryName(DragonSoulItem.ID));
    	
    	register(registry, new MagicEarthArmor(EquipmentSlotType.HEAD, MagicArmor.Type.NOVICE, PropEquipment()).setRegistryName(MagicEarthArmor.ID_HELM_NOVICE));
    	register(registry, new MagicEarthArmor(EquipmentSlotType.HEAD, MagicArmor.Type.ADEPT, PropEquipment()).setRegistryName(MagicEarthArmor.ID_HELM_ADEPT));
    	register(registry, new MagicEarthArmor(EquipmentSlotType.HEAD, MagicArmor.Type.MASTER, PropEquipment()).setRegistryName(MagicEarthArmor.ID_HELM_MASTER));
    	register(registry, new MagicEarthArmor(EquipmentSlotType.HEAD, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicEarthArmor.ID_HELM_TRUE));
    	register(registry, new MagicEarthArmor(EquipmentSlotType.CHEST, MagicArmor.Type.NOVICE, PropEquipment()).setRegistryName(MagicEarthArmor.ID_CHEST_NOVICE));
    	register(registry, new MagicEarthArmor(EquipmentSlotType.CHEST, MagicArmor.Type.ADEPT, PropEquipment()).setRegistryName(MagicEarthArmor.ID_CHEST_ADEPT));
    	register(registry, new MagicEarthArmor(EquipmentSlotType.CHEST, MagicArmor.Type.MASTER, PropEquipment()).setRegistryName(MagicEarthArmor.ID_CHEST_MASTER));
    	register(registry, new MagicEarthArmor(EquipmentSlotType.CHEST, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicEarthArmor.ID_CHEST_TRUE));
    	register(registry, new MagicEarthArmor(EquipmentSlotType.LEGS, MagicArmor.Type.NOVICE, PropEquipment()).setRegistryName(MagicEarthArmor.ID_LEGS_NOVICE));
    	register(registry, new MagicEarthArmor(EquipmentSlotType.LEGS, MagicArmor.Type.ADEPT, PropEquipment()).setRegistryName(MagicEarthArmor.ID_LEGS_ADEPT));
    	register(registry, new MagicEarthArmor(EquipmentSlotType.LEGS, MagicArmor.Type.MASTER, PropEquipment()).setRegistryName(MagicEarthArmor.ID_LEGS_MASTER));
    	register(registry, new MagicEarthArmor(EquipmentSlotType.LEGS, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicEarthArmor.ID_LEGS_TRUE));
    	register(registry, new MagicEarthArmor(EquipmentSlotType.FEET, MagicArmor.Type.NOVICE, PropEquipment()).setRegistryName(MagicEarthArmor.ID_FEET_NOVICE));
    	register(registry, new MagicEarthArmor(EquipmentSlotType.FEET, MagicArmor.Type.ADEPT, PropEquipment()).setRegistryName(MagicEarthArmor.ID_FEET_ADEPT));
    	register(registry, new MagicEarthArmor(EquipmentSlotType.FEET, MagicArmor.Type.MASTER, PropEquipment()).setRegistryName(MagicEarthArmor.ID_FEET_MASTER));
    	register(registry, new MagicEarthArmor(EquipmentSlotType.FEET, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicEarthArmor.ID_FEET_TRUE));
    	
    	register(registry, new MagicEnderArmor(EquipmentSlotType.HEAD, MagicArmor.Type.NOVICE, PropEquipment()).setRegistryName(MagicEnderArmor.ID_HELM_NOVICE));
    	register(registry, new MagicEnderArmor(EquipmentSlotType.HEAD, MagicArmor.Type.ADEPT, PropEquipment()).setRegistryName(MagicEnderArmor.ID_HELM_ADEPT));
    	register(registry, new MagicEnderArmor(EquipmentSlotType.HEAD, MagicArmor.Type.MASTER, PropEquipment()).setRegistryName(MagicEnderArmor.ID_HELM_MASTER));
    	register(registry, new MagicEnderArmor(EquipmentSlotType.HEAD, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicEnderArmor.ID_HELM_TRUE));
    	register(registry, new MagicEnderArmor(EquipmentSlotType.CHEST, MagicArmor.Type.NOVICE, PropEquipment()).setRegistryName(MagicEnderArmor.ID_CHEST_NOVICE));
    	register(registry, new MagicEnderArmor(EquipmentSlotType.CHEST, MagicArmor.Type.ADEPT, PropEquipment()).setRegistryName(MagicEnderArmor.ID_CHEST_ADEPT));
    	register(registry, new MagicEnderArmor(EquipmentSlotType.CHEST, MagicArmor.Type.MASTER, PropEquipment()).setRegistryName(MagicEnderArmor.ID_CHEST_MASTER));
    	register(registry, new MagicEnderArmor(EquipmentSlotType.CHEST, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicEnderArmor.ID_CHEST_TRUE));
    	register(registry, new MagicEnderArmor(EquipmentSlotType.LEGS, MagicArmor.Type.NOVICE, PropEquipment()).setRegistryName(MagicEnderArmor.ID_LEGS_NOVICE));
    	register(registry, new MagicEnderArmor(EquipmentSlotType.LEGS, MagicArmor.Type.ADEPT, PropEquipment()).setRegistryName(MagicEnderArmor.ID_LEGS_ADEPT));
    	register(registry, new MagicEnderArmor(EquipmentSlotType.LEGS, MagicArmor.Type.MASTER, PropEquipment()).setRegistryName(MagicEnderArmor.ID_LEGS_MASTER));
    	register(registry, new MagicEnderArmor(EquipmentSlotType.LEGS, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicEnderArmor.ID_LEGS_TRUE));
    	register(registry, new MagicEnderArmor(EquipmentSlotType.FEET, MagicArmor.Type.NOVICE, PropEquipment()).setRegistryName(MagicEnderArmor.ID_FEET_NOVICE));
    	register(registry, new MagicEnderArmor(EquipmentSlotType.FEET, MagicArmor.Type.ADEPT, PropEquipment()).setRegistryName(MagicEnderArmor.ID_FEET_ADEPT));
    	register(registry, new MagicEnderArmor(EquipmentSlotType.FEET, MagicArmor.Type.MASTER, PropEquipment()).setRegistryName(MagicEnderArmor.ID_FEET_MASTER));
    	register(registry, new MagicEnderArmor(EquipmentSlotType.FEET, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicEnderArmor.ID_FEET_TRUE));
    	
    	register(registry, new MagicFireArmor(EquipmentSlotType.HEAD, MagicArmor.Type.NOVICE, PropEquipment()).setRegistryName(MagicFireArmor.ID_HELM_NOVICE));
    	register(registry, new MagicFireArmor(EquipmentSlotType.HEAD, MagicArmor.Type.ADEPT, PropEquipment()).setRegistryName(MagicFireArmor.ID_HELM_ADEPT));
    	register(registry, new MagicFireArmor(EquipmentSlotType.HEAD, MagicArmor.Type.MASTER, PropEquipment()).setRegistryName(MagicFireArmor.ID_HELM_MASTER));
    	register(registry, new MagicFireArmor(EquipmentSlotType.HEAD, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicFireArmor.ID_HELM_TRUE));
    	register(registry, new MagicFireArmor(EquipmentSlotType.CHEST, MagicArmor.Type.NOVICE, PropEquipment()).setRegistryName(MagicFireArmor.ID_CHEST_NOVICE));
    	register(registry, new MagicFireArmor(EquipmentSlotType.CHEST, MagicArmor.Type.ADEPT, PropEquipment()).setRegistryName(MagicFireArmor.ID_CHEST_ADEPT));
    	register(registry, new MagicFireArmor(EquipmentSlotType.CHEST, MagicArmor.Type.MASTER, PropEquipment()).setRegistryName(MagicFireArmor.ID_CHEST_MASTER));
    	register(registry, new MagicFireArmor(EquipmentSlotType.CHEST, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicFireArmor.ID_CHEST_TRUE));
    	register(registry, new MagicFireArmor(EquipmentSlotType.LEGS, MagicArmor.Type.NOVICE, PropEquipment()).setRegistryName(MagicFireArmor.ID_LEGS_NOVICE));
    	register(registry, new MagicFireArmor(EquipmentSlotType.LEGS, MagicArmor.Type.ADEPT, PropEquipment()).setRegistryName(MagicFireArmor.ID_LEGS_ADEPT));
    	register(registry, new MagicFireArmor(EquipmentSlotType.LEGS, MagicArmor.Type.MASTER, PropEquipment()).setRegistryName(MagicFireArmor.ID_LEGS_MASTER));
    	register(registry, new MagicFireArmor(EquipmentSlotType.LEGS, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicFireArmor.ID_LEGS_TRUE));
    	register(registry, new MagicFireArmor(EquipmentSlotType.FEET, MagicArmor.Type.NOVICE, PropEquipment()).setRegistryName(MagicFireArmor.ID_FEET_NOVICE));
    	register(registry, new MagicFireArmor(EquipmentSlotType.FEET, MagicArmor.Type.ADEPT, PropEquipment()).setRegistryName(MagicFireArmor.ID_FEET_ADEPT));
    	register(registry, new MagicFireArmor(EquipmentSlotType.FEET, MagicArmor.Type.MASTER, PropEquipment()).setRegistryName(MagicFireArmor.ID_FEET_MASTER));
    	register(registry, new MagicFireArmor(EquipmentSlotType.FEET, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicFireArmor.ID_FEET_TRUE));
    	
//    	register(registry, new EnchantedIceArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_HELM_NOVICE));
//    	register(registry, new EnchantedIceArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_HELM_ADEPT));
//    	register(registry, new EnchantedIceArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_HELM_MASTER));
    	register(registry, new MagicIceArmor(EquipmentSlotType.HEAD, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicIceArmor.ID_HELM_TRUE));
//    	register(registry, new EnchantedIceArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_CHEST_NOVICE));
//    	register(registry, new EnchantedIceArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_CHEST_ADEPT));
//    	register(registry, new EnchantedIceArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_CHEST_MASTER));
    	register(registry, new MagicIceArmor(EquipmentSlotType.CHEST, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicIceArmor.ID_CHEST_TRUE));
//    	register(registry, new EnchantedIceArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_LEGS_NOVICE));
//    	register(registry, new EnchantedIceArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_LEGS_ADEPT));
//    	register(registry, new EnchantedIceArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_LEGS_MASTER));
    	register(registry, new MagicIceArmor(EquipmentSlotType.LEGS, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicIceArmor.ID_LEGS_TRUE));
//    	register(registry, new EnchantedIceArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_FEET_NOVICE));
//    	register(registry, new EnchantedIceArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_FEET_ADEPT));
//    	register(registry, new EnchantedIceArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_FEET_MASTER));
    	register(registry, new MagicIceArmor(EquipmentSlotType.FEET, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicIceArmor.ID_FEET_TRUE));
    	
//    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_HELM_NOVICE));
//    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_HELM_ADEPT));
//    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_HELM_MASTER));
    	register(registry, new MagicLightningArmor(EquipmentSlotType.HEAD, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicLightningArmor.ID_HELM_TRUE));
//    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_CHEST_NOVICE));
//    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_CHEST_ADEPT));
//    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_CHEST_MASTER));
    	register(registry, new MagicLightningArmor(EquipmentSlotType.CHEST, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicLightningArmor.ID_CHEST_TRUE));
//    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_LEGS_NOVICE));
//    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_LEGS_ADEPT));
//    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_LEGS_MASTER));
    	register(registry, new MagicLightningArmor(EquipmentSlotType.LEGS, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicLightningArmor.ID_LEGS_TRUE));
//    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_FEET_NOVICE));
//    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_FEET_ADEPT));
//    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_FEET_MASTER));
    	register(registry, new MagicLightningArmor(EquipmentSlotType.FEET, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicLightningArmor.ID_FEET_TRUE));
    	
    	register(registry, new MagicPhysicalArmor(EquipmentSlotType.HEAD, MagicArmor.Type.NOVICE, PropEquipment()).setRegistryName(MagicPhysicalArmor.ID_HELM_NOVICE));
    	register(registry, new MagicPhysicalArmor(EquipmentSlotType.HEAD, MagicArmor.Type.ADEPT, PropEquipment()).setRegistryName(MagicPhysicalArmor.ID_HELM_ADEPT));
    	register(registry, new MagicPhysicalArmor(EquipmentSlotType.HEAD, MagicArmor.Type.MASTER, PropEquipment()).setRegistryName(MagicPhysicalArmor.ID_HELM_MASTER));
    	register(registry, new MagicPhysicalArmor(EquipmentSlotType.HEAD, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicPhysicalArmor.ID_HELM_TRUE));
    	register(registry, new MagicPhysicalArmor(EquipmentSlotType.CHEST, MagicArmor.Type.NOVICE, PropEquipment()).setRegistryName(MagicPhysicalArmor.ID_CHEST_NOVICE));
    	register(registry, new MagicPhysicalArmor(EquipmentSlotType.CHEST, MagicArmor.Type.ADEPT, PropEquipment()).setRegistryName(MagicPhysicalArmor.ID_CHEST_ADEPT));
    	register(registry, new MagicPhysicalArmor(EquipmentSlotType.CHEST, MagicArmor.Type.MASTER, PropEquipment()).setRegistryName(MagicPhysicalArmor.ID_CHEST_MASTER));
    	register(registry, new MagicPhysicalArmor(EquipmentSlotType.CHEST, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicPhysicalArmor.ID_CHEST_TRUE));
    	register(registry, new MagicPhysicalArmor(EquipmentSlotType.LEGS, MagicArmor.Type.NOVICE, PropEquipment()).setRegistryName(MagicPhysicalArmor.ID_LEGS_NOVICE));
    	register(registry, new MagicPhysicalArmor(EquipmentSlotType.LEGS, MagicArmor.Type.ADEPT, PropEquipment()).setRegistryName(MagicPhysicalArmor.ID_LEGS_ADEPT));
    	register(registry, new MagicPhysicalArmor(EquipmentSlotType.LEGS, MagicArmor.Type.MASTER, PropEquipment()).setRegistryName(MagicPhysicalArmor.ID_LEGS_MASTER));
    	register(registry, new MagicPhysicalArmor(EquipmentSlotType.LEGS, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicPhysicalArmor.ID_LEGS_TRUE));
    	register(registry, new MagicPhysicalArmor(EquipmentSlotType.FEET, MagicArmor.Type.NOVICE, PropEquipment()).setRegistryName(MagicPhysicalArmor.ID_FEET_NOVICE));
    	register(registry, new MagicPhysicalArmor(EquipmentSlotType.FEET, MagicArmor.Type.ADEPT, PropEquipment()).setRegistryName(MagicPhysicalArmor.ID_FEET_ADEPT));
    	register(registry, new MagicPhysicalArmor(EquipmentSlotType.FEET, MagicArmor.Type.MASTER, PropEquipment()).setRegistryName(MagicPhysicalArmor.ID_FEET_MASTER));
    	register(registry, new MagicPhysicalArmor(EquipmentSlotType.FEET, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicPhysicalArmor.ID_FEET_TRUE));
    	
//    	register(registry, new EnchantedWindArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_HELM_NOVICE));
//    	register(registry, new EnchantedWindArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_HELM_ADEPT));
//    	register(registry, new EnchantedWindArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_HELM_MASTER));
    	register(registry, new MagicWindArmor(EquipmentSlotType.HEAD, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicWindArmor.ID_HELM_TRUE));
//    	register(registry, new EnchantedWindArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_CHEST_NOVICE));
//    	register(registry, new EnchantedWindArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_CHEST_ADEPT));
//    	register(registry, new EnchantedWindArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_CHEST_MASTER));
    	register(registry, new MagicWindArmor(EquipmentSlotType.CHEST, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicWindArmor.ID_CHEST_TRUE));
//    	register(registry, new EnchantedWindArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_LEGS_NOVICE));
//    	register(registry, new EnchantedWindArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_LEGS_ADEPT));
//    	register(registry, new EnchantedWindArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_LEGS_MASTER));
    	register(registry, new MagicWindArmor(EquipmentSlotType.LEGS, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicWindArmor.ID_LEGS_TRUE));
//    	register(registry, new EnchantedWindArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_FEET_NOVICE));
//    	register(registry, new EnchantedWindArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_FEET_ADEPT));
//    	register(registry, new EnchantedWindArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_FEET_MASTER));
    	register(registry, new MagicWindArmor(EquipmentSlotType.FEET, MagicArmor.Type.TRUE, PropEquipment()).setRegistryName(MagicWindArmor.ID_FEET_TRUE));
    	
    	register(registry, new AspectedWeapon(EMagicElement.ICE, AspectedWeapon.Type.NOVICE).setRegistryName(AspectedWeapon.ID_ICE_NOVICE));
    	register(registry, new AspectedWeapon(EMagicElement.ICE, AspectedWeapon.Type.ADEPT).setRegistryName(AspectedWeapon.ID_ICE_ADEPT));
    	register(registry, new AspectedWeapon(EMagicElement.ICE, AspectedWeapon.Type.MASTER).setRegistryName(AspectedWeapon.ID_ICE_MASTER));
    	register(registry, new AspectedWeapon(EMagicElement.LIGHTNING, AspectedWeapon.Type.NOVICE).setRegistryName(AspectedWeapon.ID_LIGHTNING_NOVICE));
    	register(registry, new AspectedWeapon(EMagicElement.LIGHTNING, AspectedWeapon.Type.ADEPT).setRegistryName(AspectedWeapon.ID_LIGHTNING_ADEPT));
    	register(registry, new AspectedWeapon(EMagicElement.LIGHTNING, AspectedWeapon.Type.MASTER).setRegistryName(AspectedWeapon.ID_LIGHTNING_MASTER));
    	register(registry, new AspectedWeapon(EMagicElement.WIND, AspectedWeapon.Type.NOVICE).setRegistryName(AspectedWeapon.ID_WIND_NOVICE));
    	register(registry, new AspectedWeapon(EMagicElement.WIND, AspectedWeapon.Type.ADEPT).setRegistryName(AspectedWeapon.ID_WIND_ADEPT));
    	register(registry, new AspectedWeapon(EMagicElement.WIND, AspectedWeapon.Type.MASTER).setRegistryName(AspectedWeapon.ID_WIND_MASTER));
    	register(registry, new EssenceItem(EMagicElement.EARTH).setRegistryName(EssenceItem.ID_PREFIX + ID_EARTH));
    	register(registry, new EssenceItem(EMagicElement.FIRE).setRegistryName(EssenceItem.ID_PREFIX + ID_FIRE));
    	register(registry, new EssenceItem(EMagicElement.ICE).setRegistryName(EssenceItem.ID_PREFIX + ID_ICE));
    	register(registry, new EssenceItem(EMagicElement.ENDER).setRegistryName(EssenceItem.ID_PREFIX + ID_ENDER));
    	register(registry, new EssenceItem(EMagicElement.LIGHTNING).setRegistryName(EssenceItem.ID_PREFIX + ID_LIGHTNING));
    	register(registry, new EssenceItem(EMagicElement.PHYSICAL).setRegistryName(EssenceItem.ID_PREFIX + ID_PHYSICAL));
    	register(registry, new EssenceItem(EMagicElement.WIND).setRegistryName(EssenceItem.ID_PREFIX + ID_WIND));
    	register(registry, new HookshotItem(HookshotItem.HookshotType.WEAK).setRegistryName(HookshotItem.ID_PREFIX + "weak"));
    	register(registry, new HookshotItem(HookshotItem.HookshotType.MEDIUM).setRegistryName(HookshotItem.ID_PREFIX + "medium"));
    	register(registry, new HookshotItem(HookshotItem.HookshotType.STRONG).setRegistryName(HookshotItem.ID_PREFIX + "strong"));
    	register(registry, new HookshotItem(HookshotItem.HookshotType.CLAW).setRegistryName(HookshotItem.ID_PREFIX + "claw"));
    	register(registry, new InfusedGemItem(EMagicElement.PHYSICAL).setRegistryName(InfusedGemItem.ID_PREFIX + "unattuned"));
    	register(registry, new InfusedGemItem(EMagicElement.EARTH).setRegistryName(InfusedGemItem.ID_PREFIX + ID_EARTH));
    	register(registry, new InfusedGemItem(EMagicElement.ENDER).setRegistryName(InfusedGemItem.ID_PREFIX + ID_ENDER));
    	register(registry, new InfusedGemItem(EMagicElement.FIRE).setRegistryName(InfusedGemItem.ID_PREFIX + ID_FIRE));
    	register(registry, new InfusedGemItem(EMagicElement.ICE).setRegistryName(InfusedGemItem.ID_PREFIX + ID_ICE));
    	register(registry, new InfusedGemItem(EMagicElement.LIGHTNING).setRegistryName(InfusedGemItem.ID_PREFIX + ID_LIGHTNING));
    	register(registry, new InfusedGemItem(EMagicElement.WIND).setRegistryName(InfusedGemItem.ID_PREFIX + ID_WIND));
    	register(registry, new MagicArmorBase(EquipmentSlotType.HEAD, PropEquipment()).setRegistryName(MagicArmorBase.ID_HELM));
    	register(registry, new MagicArmorBase(EquipmentSlotType.CHEST, PropEquipment()).setRegistryName(MagicArmorBase.ID_CHEST));
    	register(registry, new MagicArmorBase(EquipmentSlotType.LEGS, PropEquipment()).setRegistryName(MagicArmorBase.ID_LEGS));
    	register(registry, new MagicArmorBase(EquipmentSlotType.FEET, PropEquipment()).setRegistryName(MagicArmorBase.ID_FEET));
    	register(registry, new MageStaff().setRegistryName(MageStaff.ID));
    	register(registry, new MagicCharm(EMagicElement.EARTH).setRegistryName(MagicCharm.ID_PREFIX + ID_EARTH));
    	register(registry, new MagicCharm(EMagicElement.ENDER).setRegistryName(MagicCharm.ID_PREFIX + ID_ENDER));
    	register(registry, new MagicCharm(EMagicElement.FIRE).setRegistryName(MagicCharm.ID_PREFIX + ID_FIRE));
    	register(registry, new MagicCharm(EMagicElement.ICE).setRegistryName(MagicCharm.ID_PREFIX + ID_ICE));
    	register(registry, new MagicCharm(EMagicElement.LIGHTNING).setRegistryName(MagicCharm.ID_PREFIX + ID_LIGHTNING));
    	register(registry, new MagicCharm(EMagicElement.PHYSICAL).setRegistryName(MagicCharm.ID_PREFIX + ID_PHYSICAL));
    	register(registry, new MagicCharm(EMagicElement.WIND).setRegistryName(MagicCharm.ID_PREFIX + ID_WIND));
    	register(registry, new MagicSwordBase().setRegistryName(MagicSwordBase.ID));
    	register(registry, new MasteryOrb().setRegistryName(MasteryOrb.ID));
    	register(registry, new MirrorShield().setRegistryName(MirrorShield.ID));
    	register(registry, new MirrorShieldImproved().setRegistryName(MirrorShieldImproved.ID));
    	register(registry, new ResourceItem().setRegistryName(ResourceItem.ID_TOKEN));
    	register(registry, new ResourceItem().setRegistryName(ResourceItem.ID_PENDANT_LEFT));
    	register(registry, new ResourceItem().setRegistryName(ResourceItem.ID_PENDANT_RIGHT));
    	register(registry, new ResourceItem().setRegistryName(ResourceItem.ID_SLAB_FIERCE));
    	register(registry, new ResourceItem().setRegistryName(ResourceItem.ID_SLAB_KIND));
    	register(registry, new ResourceItem().setRegistryName(ResourceItem.ID_SLAB_BALANCED));
    	register(registry, new ResourceItem().setRegistryName(ResourceItem.ID_SPRITE_CORE));
    	register(registry, new ResourceItem().setRegistryName(ResourceItem.ID_ENDER_BRISTLE));
    	register(registry, new ResourceItem().setRegistryName(ResourceItem.ID_WISP_PEBBLE));
    	register(registry, new ResourceItem().setRegistryName(ResourceItem.ID_MANA_LEAF));
    	register(registry, new ResourceItem().setRegistryName(ResourceItem.ID_EVIL_THISTLE));
    	register(registry, new ResourceItem().setRegistryName(ResourceItem.ID_DRAGON_WING));
    	register(registry, new SeekingGem(NostrumItems.PropEquipment()).setRegistryName(SeekingGem.ID));
    	register(registry, new ResourceItem(NostrumItems.PropBase().rarity(Rarity.RARE)).setRegistryName(ResourceItem.ID_SKILL_OOZE));
    	register(registry, new ResourceItem(NostrumItems.PropBase().rarity(Rarity.RARE)).setRegistryName(ResourceItem.ID_SKILL_PENDANT));
    	register(registry, new ResourceItem(NostrumItems.PropBase().rarity(Rarity.RARE)).setRegistryName(ResourceItem.ID_SKILL_FLUTE));
    	register(registry, new ResourceCrystal(NostrumBlocks.maniCrystalBlock, PropBase(), EMagicTier.MANI).setRegistryName(ResourceCrystal.ID_CRYSTAL_SMALL));
    	register(registry, new ResourceCrystal(NostrumBlocks.kaniCrystalBlock, PropBase().rarity(Rarity.UNCOMMON), EMagicTier.KANI).setRegistryName(ResourceCrystal.ID_CRYSTAL_MEDIUM));
    	register(registry, new ResourceCrystal(NostrumBlocks.vaniCrystalBlock, PropBase().rarity(Rarity.RARE), EMagicTier.VANI).setRegistryName(ResourceCrystal.ID_CRYSTAL_LARGE));
    	register(registry, new RoseItem().setRegistryName(RoseItem.ID_BLOOD_ROSE));
    	register(registry, new RoseItem().setRegistryName(RoseItem.ID_ELDRICH_ROSE));
    	register(registry, new RoseItem().setRegistryName(RoseItem.ID_PALE_ROSE));
    	register(registry, new SkillItem.Mirror().setRegistryName(SkillItem.ID_SKILL_MIRROR));
    	register(registry, new SkillItem.EnderPin().setRegistryName(SkillItem.ID_SKILL_ENDER_PIN));
    	register(registry, new SkillItem.SmallScroll().setRegistryName(SkillItem.ID_SKILL_SCROLL_SMALL));
    	register(registry, new SkillItem.LargeScroll().setRegistryName(SkillItem.ID_SKILL_SCROLL_LARGE));
    	register(registry, new PositionCrystal().setRegistryName(PositionCrystal.ID));
    	register(registry, new PositionToken().setRegistryName(PositionToken.ID));
    	register(registry, new ReagentItem(ReagentType.MANDRAKE_ROOT).setRegistryName(ReagentItem.ID_MANDRAKE_ROOT));
    	register(registry, new ReagentItem(ReagentType.SPIDER_SILK).setRegistryName(ReagentItem.ID_SPIDER_SILK));
    	register(registry, new ReagentItem(ReagentType.BLACK_PEARL).setRegistryName(ReagentItem.ID_BLACK_PEARL));
    	register(registry, new ReagentItem(ReagentType.SKY_ASH).setRegistryName(ReagentItem.ID_SKY_ASH));
    	register(registry, new ReagentItem(ReagentType.GINSENG).setRegistryName(ReagentItem.ID_GINSENG));
    	register(registry, new ReagentItem(ReagentType.GRAVE_DUST).setRegistryName(ReagentItem.ID_GRAVE_DUST));
    	register(registry, new ReagentItem(ReagentType.CRYSTABLOOM).setRegistryName(ReagentItem.ID_CRYSTABLOOM));
    	register(registry, new ReagentItem(ReagentType.MANI_DUST).setRegistryName(ReagentItem.ID_MANI_DUST));
    	register(registry, new ReagentBag().setRegistryName(ReagentBag.ID));
    	register(registry, new ReagentSeed(NostrumBlocks.mandrakeCrop, PropBase()).setRegistryName(ReagentSeed.ID_MANDRAKE_SEED));
    	register(registry, new ReagentSeed(NostrumBlocks.ginsengCrop, PropBase()).setRegistryName(ReagentSeed.ID_GINSENG_SEED));
    	register(registry, new ReagentSeed(NostrumBlocks.essenceCrop, PropBase()).setRegistryName(ReagentSeed.ID_ESSENCE_SEED));
    	register(registry, new RuneBag().setRegistryName(RuneBag.ID));
    	register(registry, new SoulDagger().setRegistryName(SoulDagger.ID));
    	register(registry, new SpellPlate(SpellTome.TomeStyle.NOVICE).setRegistryName(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_NOVICE));
    	register(registry, new SpellPlate(SpellTome.TomeStyle.ADVANCED).setRegistryName(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_ADVANCED));
    	register(registry, new SpellPlate(SpellTome.TomeStyle.COMBAT).setRegistryName(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_COMBAT));
    	register(registry, new SpellPlate(SpellTome.TomeStyle.DEATH).setRegistryName(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_DEATH));
    	register(registry, new SpellPlate(SpellTome.TomeStyle.SPOOKY).setRegistryName(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_SPOOKY));
    	register(registry, new SpellPlate(SpellTome.TomeStyle.MUTED).setRegistryName(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_MUTED));
    	register(registry, new SpellPlate(SpellTome.TomeStyle.LIVING).setRegistryName(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_LIVING));
    	//register(registry, new SpellRune().setRegistryName(SpellRune.ID));
    	register(registry, new SpellScroll().setRegistryName(SpellScroll.ID));
    	register(registry, new SpellTome().setRegistryName(SpellTome.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_NOVICE));
    	register(registry, new SpellTome().setRegistryName(SpellTome.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_ADVANCED));
    	register(registry, new SpellTome().setRegistryName(SpellTome.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_COMBAT));
    	register(registry, new SpellTome().setRegistryName(SpellTome.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_DEATH));
    	register(registry, new SpellTome().setRegistryName(SpellTome.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_SPOOKY));
    	register(registry, new SpellTome().setRegistryName(SpellTome.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_MUTED));
    	register(registry, new SpellTome().setRegistryName(SpellTome.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_LIVING));
    	register(registry, new SpellTomePage().setRegistryName(SpellTomePage.ID));
    	register(registry, new ThanoPendant().setRegistryName(ThanoPendant.ID));
    	register(registry, new ThanosStaff().setRegistryName(ThanosStaff.ID));
    	register(registry, new WarlockSword().setRegistryName(WarlockSword.ID));
    	register(registry, new WorldKeyItem().setRegistryName(WorldKeyItem.ID));
    	register(registry, new FillItem(() -> NostrumBlocks.dungeonAir.getDefaultState(), false).setRegistryName(FillItem.ID_AIR_ALL));
    	register(registry, new FillItem(() -> Blocks.WATER.getDefaultState(), false).setRegistryName(FillItem.ID_WATER_ALL));
    	register(registry, new FillItem(() -> Blocks.WATER.getDefaultState(), true).setRegistryName(FillItem.ID_WATER_DOWN));
    	register(registry, new MageBlade().setRegistryName(MageBlade.ID));
    	register(registry, new AspectedFireWeapon().setRegistryName(AspectedFireWeapon.ID));
    	register(registry, new AspectedPhysicalWeapon().setRegistryName(AspectedPhysicalWeapon.ID));
    	register(registry, new AspectedEarthWeapon().setRegistryName(AspectedEarthWeapon.ID));
    	register(registry, new AspectedEnderWeapon().setRegistryName(AspectedEnderWeapon.ID));
    	register(registry, new CasterWandItem().setRegistryName(CasterWandItem.ID));
    	register(registry, new SpellPatternTome(PropUnstackable().rarity(Rarity.EPIC)).setRegistryName(SpellPatternTome.ID));
    	register(registry, new CopyWandItem().setRegistryName(CopyWandItem.ID));
    	register(registry, new ResearchTranscriptItem(PropBase().rarity(Rarity.UNCOMMON)).setRegistryName(ResearchTranscriptItem.ID));
    	
    	// Generate and register spell runes
    	{
	    	for (EMagicElement type : EMagicElement.values()) {
	    		registerRune(registry, new ElementSpellRune(type));
	    	}
	    	for (EAlteration type : EAlteration.values()) {
	    		registerRune(registry, new AlterationSpellRune(type));
	    	}
	    	for (SpellShape type : SpellShape.getAllShapes()) {
	    		registerRune(registry, new ShapeSpellRune(type));
	    	}
    	}
	}
	
	private static void registerRune(IForgeRegistry<Item> registry, SpellRune rune) {
		rune.setRegistryName(rune.makeRegistryName());
		register(registry, rune); // Register item and lore
		SpellRune.SetRuneForType(rune.getComponent(), rune);
	}
	
	public static SpellRune GetRune(SpellComponentWrapper type) {
		return SpellRune.GetRuneForType(type);
	}
}
