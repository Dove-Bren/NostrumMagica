package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.fluids.FluidPoisonWater;
import com.smanzana.nostrummagica.items.DragonArmor.DragonArmorMaterial;
import com.smanzana.nostrummagica.items.DragonArmor.DragonEquipmentSlot;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SpellRune.AlterationSpellRune;
import com.smanzana.nostrummagica.items.SpellRune.ElementSpellRune;
import com.smanzana.nostrummagica.items.SpellRune.ShapeSpellRune;
import com.smanzana.nostrummagica.items.SpellRune.TriggerSpellRune;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(NostrumMagica.MODID)
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
	@ObjectHolder(DragonArmor.ID_WINGS_GOLD) public static DragonArmor dragonArmorWingsGold;
	@ObjectHolder(DragonArmor.ID_WINGS_IRON) public static DragonArmor dragonArmorWingsIron;
	@ObjectHolder(DragonArmor.ID_WINGS_DIAMOND) public static DragonArmor dragonArmorWingsDiamond;
	@ObjectHolder(DragonArmor.ID_CREST_GOLD) public static DragonArmor dragonArmorCrestGold;
	@ObjectHolder(DragonArmor.ID_CREST_IRON) public static DragonArmor dragonArmorCrestIron;
	@ObjectHolder(DragonArmor.ID_CREST_DIAMOND) public static DragonArmor dragonArmorCrestDiamond;
	@ObjectHolder(DragonEgg.ID) public static DragonEgg dragonEgg;
	@ObjectHolder(DragonEggFragment.ID) public static DragonEggFragment dragonEggFragment;
	@ObjectHolder(DragonSoulItem.ID) public static DragonSoulItem dragonSoulItem;
	
	@ObjectHolder(EnchantedEarthArmor.ID_HELM_NOVICE) public static EnchantedEarthArmor enchantedArmorEarthHeadNovice;
	@ObjectHolder(EnchantedEarthArmor.ID_HELM_ADEPT) public static EnchantedEarthArmor enchantedArmorEarthHeadAdept;
	@ObjectHolder(EnchantedEarthArmor.ID_HELM_MASTER) public static EnchantedEarthArmor enchantedArmorEarthHeadMaster;
	@ObjectHolder(EnchantedEarthArmor.ID_HELM_TRUE) public static EnchantedEarthArmor enchantedArmorEarthHeadTrue;
	@ObjectHolder(EnchantedEarthArmor.ID_CHEST_NOVICE) public static EnchantedEarthArmor enchantedArmorEarthChestNovice;
	@ObjectHolder(EnchantedEarthArmor.ID_CHEST_ADEPT) public static EnchantedEarthArmor enchantedArmorEarthChestAdept;
	@ObjectHolder(EnchantedEarthArmor.ID_CHEST_MASTER) public static EnchantedEarthArmor enchantedArmorEarthChestMaster;
	@ObjectHolder(EnchantedEarthArmor.ID_CHEST_TRUE) public static EnchantedEarthArmor enchantedArmorEarthChestTrue;
	@ObjectHolder(EnchantedEarthArmor.ID_LEGS_NOVICE) public static EnchantedEarthArmor enchantedArmorEarthLegsNovice;
	@ObjectHolder(EnchantedEarthArmor.ID_LEGS_ADEPT) public static EnchantedEarthArmor enchantedArmorEarthLegsAdept;
	@ObjectHolder(EnchantedEarthArmor.ID_LEGS_MASTER) public static EnchantedEarthArmor enchantedArmorEarthLegsMaster;
	@ObjectHolder(EnchantedEarthArmor.ID_LEGS_TRUE) public static EnchantedEarthArmor enchantedArmorEarthLegsTrue;
	@ObjectHolder(EnchantedEarthArmor.ID_FEET_NOVICE) public static EnchantedEarthArmor enchantedArmorEarthFeetNovice;
	@ObjectHolder(EnchantedEarthArmor.ID_FEET_ADEPT) public static EnchantedEarthArmor enchantedArmorEarthFeetAdept;
	@ObjectHolder(EnchantedEarthArmor.ID_FEET_MASTER) public static EnchantedEarthArmor enchantedArmorEarthFeetMaster;
	@ObjectHolder(EnchantedEarthArmor.ID_FEET_TRUE) public static EnchantedEarthArmor enchantedArmorEarthFeetTrue;
	
	@ObjectHolder(EnchantedEnderArmor.ID_HELM_NOVICE) public static EnchantedEnderArmor enchantedArmorEnderHeadNovice;
	@ObjectHolder(EnchantedEnderArmor.ID_HELM_ADEPT) public static EnchantedEnderArmor enchantedArmorEnderHeadAdept;
	@ObjectHolder(EnchantedEnderArmor.ID_HELM_MASTER) public static EnchantedEnderArmor enchantedArmorEnderHeadMaster;
	@ObjectHolder(EnchantedEnderArmor.ID_HELM_TRUE) public static EnchantedEnderArmor enchantedArmorEnderHeadTrue;
	@ObjectHolder(EnchantedEnderArmor.ID_CHEST_NOVICE) public static EnchantedEnderArmor enchantedArmorEnderChestNovice;
	@ObjectHolder(EnchantedEnderArmor.ID_CHEST_ADEPT) public static EnchantedEnderArmor enchantedArmorEnderChestAdept;
	@ObjectHolder(EnchantedEnderArmor.ID_CHEST_MASTER) public static EnchantedEnderArmor enchantedArmorEnderChestMaster;
	@ObjectHolder(EnchantedEnderArmor.ID_CHEST_TRUE) public static EnchantedEnderArmor enchantedArmorEnderChestTrue;
	@ObjectHolder(EnchantedEnderArmor.ID_LEGS_NOVICE) public static EnchantedEnderArmor enchantedArmorEnderLegsNovice;
	@ObjectHolder(EnchantedEnderArmor.ID_LEGS_ADEPT) public static EnchantedEnderArmor enchantedArmorEnderLegsAdept;
	@ObjectHolder(EnchantedEnderArmor.ID_LEGS_MASTER) public static EnchantedEnderArmor enchantedArmorEnderLegsMaster;
	@ObjectHolder(EnchantedEnderArmor.ID_LEGS_TRUE) public static EnchantedEnderArmor enchantedArmorEnderLegsTrue;
	@ObjectHolder(EnchantedEnderArmor.ID_FEET_NOVICE) public static EnchantedEnderArmor enchantedArmorEnderFeetNovice;
	@ObjectHolder(EnchantedEnderArmor.ID_FEET_ADEPT) public static EnchantedEnderArmor enchantedArmorEnderFeetAdept;
	@ObjectHolder(EnchantedEnderArmor.ID_FEET_MASTER) public static EnchantedEnderArmor enchantedArmorEnderFeetMaster;
	@ObjectHolder(EnchantedEnderArmor.ID_FEET_TRUE) public static EnchantedEnderArmor enchantedArmorEnderFeetTrue;
	
	@ObjectHolder(EnchantedFireArmor.ID_HELM_NOVICE) public static EnchantedFireArmor enchantedArmorFireHeadNovice;
	@ObjectHolder(EnchantedFireArmor.ID_HELM_ADEPT) public static EnchantedFireArmor enchantedArmorFireHeadAdept;
	@ObjectHolder(EnchantedFireArmor.ID_HELM_MASTER) public static EnchantedFireArmor enchantedArmorFireHeadMaster;
	@ObjectHolder(EnchantedFireArmor.ID_HELM_TRUE) public static EnchantedFireArmor enchantedArmorFireHeadTrue;
	@ObjectHolder(EnchantedFireArmor.ID_CHEST_NOVICE) public static EnchantedFireArmor enchantedArmorFireChestNovice;
	@ObjectHolder(EnchantedFireArmor.ID_CHEST_ADEPT) public static EnchantedFireArmor enchantedArmorFireChestAdept;
	@ObjectHolder(EnchantedFireArmor.ID_CHEST_MASTER) public static EnchantedFireArmor enchantedArmorFireChestMaster;
	@ObjectHolder(EnchantedFireArmor.ID_CHEST_TRUE) public static EnchantedFireArmor enchantedArmorFireChestTrue;
	@ObjectHolder(EnchantedFireArmor.ID_LEGS_NOVICE) public static EnchantedFireArmor enchantedArmorFireLegsNovice;
	@ObjectHolder(EnchantedFireArmor.ID_LEGS_ADEPT) public static EnchantedFireArmor enchantedArmorFireLegsAdept;
	@ObjectHolder(EnchantedFireArmor.ID_LEGS_MASTER) public static EnchantedFireArmor enchantedArmorFireLegsMaster;
	@ObjectHolder(EnchantedFireArmor.ID_LEGS_TRUE) public static EnchantedFireArmor enchantedArmorFireLegsTrue;
	@ObjectHolder(EnchantedFireArmor.ID_FEET_NOVICE) public static EnchantedFireArmor enchantedArmorFireFeetNovice;
	@ObjectHolder(EnchantedFireArmor.ID_FEET_ADEPT) public static EnchantedFireArmor enchantedArmorFireFeetAdept;
	@ObjectHolder(EnchantedFireArmor.ID_FEET_MASTER) public static EnchantedFireArmor enchantedArmorFireFeetMaster;
	@ObjectHolder(EnchantedFireArmor.ID_FEET_TRUE) public static EnchantedFireArmor enchantedArmorFireFeetTrue;
	
	@ObjectHolder(EnchantedIceArmor.ID_HELM_NOVICE) public static EnchantedIceArmor enchantedArmorIceHeadNovice;
	@ObjectHolder(EnchantedIceArmor.ID_HELM_ADEPT) public static EnchantedIceArmor enchantedArmorIceHeadAdept;
	@ObjectHolder(EnchantedIceArmor.ID_HELM_MASTER) public static EnchantedIceArmor enchantedArmorIceHeadMaster;
	@ObjectHolder(EnchantedIceArmor.ID_HELM_TRUE) public static EnchantedIceArmor enchantedArmorIceHeadTrue;
	@ObjectHolder(EnchantedIceArmor.ID_CHEST_NOVICE) public static EnchantedIceArmor enchantedArmorIceChestNovice;
	@ObjectHolder(EnchantedIceArmor.ID_CHEST_ADEPT) public static EnchantedIceArmor enchantedArmorIceChestAdept;
	@ObjectHolder(EnchantedIceArmor.ID_CHEST_MASTER) public static EnchantedIceArmor enchantedArmorIceChestMaster;
	@ObjectHolder(EnchantedIceArmor.ID_CHEST_TRUE) public static EnchantedIceArmor enchantedArmorIceChestTrue;
	@ObjectHolder(EnchantedIceArmor.ID_LEGS_NOVICE) public static EnchantedIceArmor enchantedArmorIceLegsNovice;
	@ObjectHolder(EnchantedIceArmor.ID_LEGS_ADEPT) public static EnchantedIceArmor enchantedArmorIceLegsAdept;
	@ObjectHolder(EnchantedIceArmor.ID_LEGS_MASTER) public static EnchantedIceArmor enchantedArmorIceLegsMaster;
	@ObjectHolder(EnchantedIceArmor.ID_LEGS_TRUE) public static EnchantedIceArmor enchantedArmorIceLegsTrue;
	@ObjectHolder(EnchantedIceArmor.ID_FEET_NOVICE) public static EnchantedIceArmor enchantedArmorIceFeetNovice;
	@ObjectHolder(EnchantedIceArmor.ID_FEET_ADEPT) public static EnchantedIceArmor enchantedArmorIceFeetAdept;
	@ObjectHolder(EnchantedIceArmor.ID_FEET_MASTER) public static EnchantedIceArmor enchantedArmorIceFeetMaster;
	@ObjectHolder(EnchantedIceArmor.ID_FEET_TRUE) public static EnchantedIceArmor enchantedArmorIceFeetTrue;
	
	@ObjectHolder(EnchantedLightningArmor.ID_HELM_NOVICE) public static EnchantedLightningArmor enchantedArmorLightningHeadNovice;
	@ObjectHolder(EnchantedLightningArmor.ID_HELM_ADEPT) public static EnchantedLightningArmor enchantedArmorLightningHeadAdept;
	@ObjectHolder(EnchantedLightningArmor.ID_HELM_MASTER) public static EnchantedLightningArmor enchantedArmorLightningHeadMaster;
	@ObjectHolder(EnchantedLightningArmor.ID_HELM_TRUE) public static EnchantedLightningArmor enchantedArmorLightningHeadTrue;
	@ObjectHolder(EnchantedLightningArmor.ID_CHEST_NOVICE) public static EnchantedLightningArmor enchantedArmorLightningChestNovice;
	@ObjectHolder(EnchantedLightningArmor.ID_CHEST_ADEPT) public static EnchantedLightningArmor enchantedArmorLightningChestAdept;
	@ObjectHolder(EnchantedLightningArmor.ID_CHEST_MASTER) public static EnchantedLightningArmor enchantedArmorLightningChestMaster;
	@ObjectHolder(EnchantedLightningArmor.ID_CHEST_TRUE) public static EnchantedLightningArmor enchantedArmorLightningChestTrue;
	@ObjectHolder(EnchantedLightningArmor.ID_LEGS_NOVICE) public static EnchantedLightningArmor enchantedArmorLightningLegsNovice;
	@ObjectHolder(EnchantedLightningArmor.ID_LEGS_ADEPT) public static EnchantedLightningArmor enchantedArmorLightningLegsAdept;
	@ObjectHolder(EnchantedLightningArmor.ID_LEGS_MASTER) public static EnchantedLightningArmor enchantedArmorLightningLegsMaster;
	@ObjectHolder(EnchantedLightningArmor.ID_LEGS_TRUE) public static EnchantedLightningArmor enchantedArmorLightningLegsTrue;
	@ObjectHolder(EnchantedLightningArmor.ID_FEET_NOVICE) public static EnchantedLightningArmor enchantedArmorLightningFeetNovice;
	@ObjectHolder(EnchantedLightningArmor.ID_FEET_ADEPT) public static EnchantedLightningArmor enchantedArmorLightningFeetAdept;
	@ObjectHolder(EnchantedLightningArmor.ID_FEET_MASTER) public static EnchantedLightningArmor enchantedArmorLightningFeetMaster;
	@ObjectHolder(EnchantedLightningArmor.ID_FEET_TRUE) public static EnchantedLightningArmor enchantedArmorLightningFeetTrue;
	
	@ObjectHolder(EnchantedPhysicalArmor.ID_HELM_NOVICE) public static EnchantedPhysicalArmor enchantedArmorPhysicalHeadNovice;
	@ObjectHolder(EnchantedPhysicalArmor.ID_HELM_ADEPT) public static EnchantedPhysicalArmor enchantedArmorPhysicalHeadAdept;
	@ObjectHolder(EnchantedPhysicalArmor.ID_HELM_MASTER) public static EnchantedPhysicalArmor enchantedArmorPhysicalHeadMaster;
	@ObjectHolder(EnchantedPhysicalArmor.ID_HELM_TRUE) public static EnchantedPhysicalArmor enchantedArmorPhysicalHeadTrue;
	@ObjectHolder(EnchantedPhysicalArmor.ID_CHEST_NOVICE) public static EnchantedPhysicalArmor enchantedArmorPhysicalChestNovice;
	@ObjectHolder(EnchantedPhysicalArmor.ID_CHEST_ADEPT) public static EnchantedPhysicalArmor enchantedArmorPhysicalChestAdept;
	@ObjectHolder(EnchantedPhysicalArmor.ID_CHEST_MASTER) public static EnchantedPhysicalArmor enchantedArmorPhysicalChestMaster;
	@ObjectHolder(EnchantedPhysicalArmor.ID_CHEST_TRUE) public static EnchantedPhysicalArmor enchantedArmorPhysicalChestTrue;
	@ObjectHolder(EnchantedPhysicalArmor.ID_LEGS_NOVICE) public static EnchantedPhysicalArmor enchantedArmorPhysicalLegsNovice;
	@ObjectHolder(EnchantedPhysicalArmor.ID_LEGS_ADEPT) public static EnchantedPhysicalArmor enchantedArmorPhysicalLegsAdept;
	@ObjectHolder(EnchantedPhysicalArmor.ID_LEGS_MASTER) public static EnchantedPhysicalArmor enchantedArmorPhysicalLegsMaster;
	@ObjectHolder(EnchantedPhysicalArmor.ID_LEGS_TRUE) public static EnchantedPhysicalArmor enchantedArmorPhysicalLegsTrue;
	@ObjectHolder(EnchantedPhysicalArmor.ID_FEET_NOVICE) public static EnchantedPhysicalArmor enchantedArmorPhysicalFeetNovice;
	@ObjectHolder(EnchantedPhysicalArmor.ID_FEET_ADEPT) public static EnchantedPhysicalArmor enchantedArmorPhysicalFeetAdept;
	@ObjectHolder(EnchantedPhysicalArmor.ID_FEET_MASTER) public static EnchantedPhysicalArmor enchantedArmorPhysicalFeetMaster;
	@ObjectHolder(EnchantedPhysicalArmor.ID_FEET_TRUE) public static EnchantedPhysicalArmor enchantedArmorPhysicalFeetTrue;
	
	@ObjectHolder(EnchantedWindArmor.ID_HELM_NOVICE) public static EnchantedWindArmor enchantedArmorWindHeadNovice;
	@ObjectHolder(EnchantedWindArmor.ID_HELM_ADEPT) public static EnchantedWindArmor enchantedArmorWindHeadAdept;
	@ObjectHolder(EnchantedWindArmor.ID_HELM_MASTER) public static EnchantedWindArmor enchantedArmorWindHeadMaster;
	@ObjectHolder(EnchantedWindArmor.ID_HELM_TRUE) public static EnchantedWindArmor enchantedArmorWindHeadTrue;
	@ObjectHolder(EnchantedWindArmor.ID_CHEST_NOVICE) public static EnchantedWindArmor enchantedArmorWindChestNovice;
	@ObjectHolder(EnchantedWindArmor.ID_CHEST_ADEPT) public static EnchantedWindArmor enchantedArmorWindChestAdept;
	@ObjectHolder(EnchantedWindArmor.ID_CHEST_MASTER) public static EnchantedWindArmor enchantedArmorWindChestMaster;
	@ObjectHolder(EnchantedWindArmor.ID_CHEST_TRUE) public static EnchantedWindArmor enchantedArmorWindChestTrue;
	@ObjectHolder(EnchantedWindArmor.ID_LEGS_NOVICE) public static EnchantedWindArmor enchantedArmorWindLegsNovice;
	@ObjectHolder(EnchantedWindArmor.ID_LEGS_ADEPT) public static EnchantedWindArmor enchantedArmorWindLegsAdept;
	@ObjectHolder(EnchantedWindArmor.ID_LEGS_MASTER) public static EnchantedWindArmor enchantedArmorWindLegsMaster;
	@ObjectHolder(EnchantedWindArmor.ID_LEGS_TRUE) public static EnchantedWindArmor enchantedArmorWindLegsTrue;
	@ObjectHolder(EnchantedWindArmor.ID_FEET_NOVICE) public static EnchantedWindArmor enchantedArmorWindFeetNovice;
	@ObjectHolder(EnchantedWindArmor.ID_FEET_ADEPT) public static EnchantedWindArmor enchantedArmorWindFeetAdept;
	@ObjectHolder(EnchantedWindArmor.ID_FEET_MASTER) public static EnchantedWindArmor enchantedArmorWindFeetMaster;
	@ObjectHolder(EnchantedWindArmor.ID_FEET_TRUE) public static EnchantedWindArmor enchantedArmorWindFeetTrue;
	
	@ObjectHolder(EnchantedWeapon.ID_ICE_NOVICE) public static EnchantedWeapon enchantedWeaponIceNovice;
	@ObjectHolder(EnchantedWeapon.ID_ICE_ADEPT) public static EnchantedWeapon enchantedWeaponIceAdept;
	@ObjectHolder(EnchantedWeapon.ID_ICE_MASTER) public static EnchantedWeapon enchantedWeaponIceMaster;
	@ObjectHolder(EnchantedWeapon.ID_LIGHTNING_NOVICE) public static EnchantedWeapon enchantedWeaponLightningNovice;
	@ObjectHolder(EnchantedWeapon.ID_LIGHTNING_ADEPT) public static EnchantedWeapon enchantedWeaponLightningAdept;
	@ObjectHolder(EnchantedWeapon.ID_LIGHTNING_MASTER) public static EnchantedWeapon enchantedWeaponLightningMaster;
	@ObjectHolder(EnchantedWeapon.ID_WIND_NOVICE) public static EnchantedWeapon enchantedWeaponWindNovice;
	@ObjectHolder(EnchantedWeapon.ID_WIND_ADEPT) public static EnchantedWeapon enchantedWeaponWindAdept;
	@ObjectHolder(EnchantedWeapon.ID_WIND_MASTER) public static EnchantedWeapon enchantedWeaponWindMaster;
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_EARTH) public static EssenceItem essenceEarth;
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_FIRE) public static EssenceItem essenceFire;
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_ICE) public static EssenceItem essenceIce;
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_ENDER) public static EssenceItem essenceEnder;
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_LIGHTNING) public static EssenceItem essenceLightning;
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_PHYSICAL) public static EssenceItem essencePhysical;
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_WIND) public static EssenceItem essenceWind;
	@ObjectHolder(HookshotItem.ID_PREFIX + "weak") public static AltarItem hookshotWeak;
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
	@ObjectHolder(MirrorItem.ID) public static MirrorItem mirrorItem; // TODO could clean up; is just a BlockItem
	@ObjectHolder(MirrorShield.ID) public static MirrorShield mirrorShield;
	@ObjectHolder(MirrorShieldImproved.ID) public static MirrorShieldImproved mirrorShieldImproved;
	@ObjectHolder(NostrumGuide.ID) public static NostrumGuide nostrumGuide;
	@ObjectHolder(NostrumResourceItem.ID_TOKEN) public static NostrumResourceItem resourceToken;
	@ObjectHolder(NostrumResourceItem.ID_PENDANT_LEFT) public static NostrumResourceItem resourcePendantLeft;
	@ObjectHolder(NostrumResourceItem.ID_PENDANT_RIGHT) public static NostrumResourceItem resourcePendantRight;
	@ObjectHolder(NostrumResourceItem.ID_SLAB_FIERCE) public static NostrumResourceItem resourceSlabFierce;
	@ObjectHolder(NostrumResourceItem.ID_SLAB_KIND) public static NostrumResourceItem resourceSlabKind;
	@ObjectHolder(NostrumResourceItem.ID_SLAB_BALANCED) public static NostrumResourceItem resourceSlabBalanced;
	@ObjectHolder(NostrumResourceItem.ID_SPRITE_CORE) public static NostrumResourceItem resourceSpriteCore;
	@ObjectHolder(NostrumResourceItem.ID_ENDER_BRISTLE) public static NostrumResourceItem resourceEnderBristle;
	@ObjectHolder(NostrumResourceItem.ID_WISP_PEBBLE) public static NostrumResourceItem resourceWispPebble;
	@ObjectHolder(NostrumResourceItem.ID_MANA_LEAF) public static NostrumResourceItem resourceManaLeaf;
	@ObjectHolder(NostrumResourceItem.ID_EVIL_THISTLE) public static NostrumResourceItem resourceEvilThistle;
	@ObjectHolder(NostrumResourceItem.ID_DRAGON_WING) public static NostrumResourceItem resourceDragonWing;
	@ObjectHolder(NostrumResourceItem.ID_SEEKING_GEM) public static NostrumResourceItem resourceSeekingGem;
	@ObjectHolder(NostrumResourceCrystal.ID_CRYSTAL_SMALL) public static NostrumResourceCrystal crystalSmall;
	@ObjectHolder(NostrumResourceCrystal.ID_CRYSTAL_MEDIUM) public static NostrumResourceCrystal crystalMedium;
	@ObjectHolder(NostrumResourceCrystal.ID_CRYSTAL_LARGE) public static NostrumResourceCrystal crystalLarge;
	@ObjectHolder(NostrumRoseItem.ID_BLOOD_ROSE) public static NostrumRoseItem roseBlood;
	@ObjectHolder(NostrumRoseItem.ID_ELDRICH_ROSE) public static NostrumRoseItem roseEldrich;
	@ObjectHolder(NostrumRoseItem.ID_PALE_ROSE) public static NostrumRoseItem rosePale;
	@ObjectHolder(NostrumSkillItem.ID_SKILL_MIRROR) public static NostrumSkillItem.Mirror skillMirror;
	@ObjectHolder(NostrumSkillItem.ID_SKILL_OOZE) public static NostrumSkillItem.Ooze skillOoze;
	@ObjectHolder(NostrumSkillItem.ID_SKILL_PENDANT) public static NostrumSkillItem.Pendant skillPendant;
	@ObjectHolder(NostrumSkillItem.ID_SKILL_FLUTE) public static NostrumSkillItem.Flute skillFlute;
	@ObjectHolder(NostrumSkillItem.ID_SKILL_ENDER_PIN) public static NostrumSkillItem.EnderPin skillEnderPin;
	@ObjectHolder(NostrumSkillItem.ID_SKILL_SCROLL_SMALL) public static NostrumSkillItem.SmallScroll skillScrollSmall;
	@ObjectHolder(NostrumSkillItem.ID_SKILL_SCROLL_LARGE) public static NostrumSkillItem.LargeScroll skillScrollLarge;
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
	@ObjectHolder(SpellcraftGuide.ID) public static SpellcraftGuide spellcraftGuide;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_NOVICE) public static SpellPlate spellPlateNovice;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_ADVANCED) public static SpellPlate spellPlateAdvanced;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_COMBAT) public static SpellPlate spellPlateCombat;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_DEATH) public static SpellPlate spellPlateDeath;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_SPOOKY) public static SpellPlate spellPlateSpooky;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_MUTED) public static SpellPlate spellPlateMuted;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_LIVING) public static SpellPlate spellPlateLiving;
	@ObjectHolder(SpellScroll.ID) public static SpellScroll spellScroll;
	@ObjectHolder(SpellTableItem.ID) public static SpellTableItem spellTableItem;
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
	
	@ObjectHolder(FluidPoisonWater.ID_BREAKABLE + "_bucket") public static BucketItem poisonWaterBucket;
	@ObjectHolder(FluidPoisonWater.ID_UNBREAKABLE + "_bucket") public static BucketItem unbreakablePoisonWaterBucket;
	
	
	public NostrumItems() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
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
	
	public static Item.Properties PropEquipment() {
		return PropUnstackable()
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
    	register(registry, new DragonArmor(DragonEquipmentSlot.WINGS, DragonArmorMaterial.GOLD).setRegistryName(DragonArmor.ID_WINGS_GOLD));
    	register(registry, new DragonArmor(DragonEquipmentSlot.WINGS, DragonArmorMaterial.IRON).setRegistryName(DragonArmor.ID_WINGS_IRON));
    	register(registry, new DragonArmor(DragonEquipmentSlot.WINGS, DragonArmorMaterial.DIAMOND).setRegistryName(DragonArmor.ID_WINGS_DIAMOND));
    	register(registry, new DragonArmor(DragonEquipmentSlot.CREST, DragonArmorMaterial.GOLD).setRegistryName(DragonArmor.ID_CREST_GOLD));
    	register(registry, new DragonArmor(DragonEquipmentSlot.CREST, DragonArmorMaterial.IRON).setRegistryName(DragonArmor.ID_CREST_IRON));
    	register(registry, new DragonArmor(DragonEquipmentSlot.CREST, DragonArmorMaterial.DIAMOND).setRegistryName(DragonArmor.ID_CREST_DIAMOND));
    	register(registry, new DragonEgg().setRegistryName(DragonEgg.ID));
    	register(registry, new DragonEggFragment().setRegistryName(DragonEggFragment.ID));
    	register(registry, new DragonSoulItem().setRegistryName(DragonSoulItem.ID));
    	
    	register(registry, new EnchantedEarthArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedEarthArmor.ID_HELM_NOVICE));
    	register(registry, new EnchantedEarthArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedEarthArmor.ID_HELM_ADEPT));
    	register(registry, new EnchantedEarthArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedEarthArmor.ID_HELM_MASTER));
    	register(registry, new EnchantedEarthArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedEarthArmor.ID_HELM_TRUE));
    	register(registry, new EnchantedEarthArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedEarthArmor.ID_CHEST_NOVICE));
    	register(registry, new EnchantedEarthArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedEarthArmor.ID_CHEST_ADEPT));
    	register(registry, new EnchantedEarthArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedEarthArmor.ID_CHEST_MASTER));
    	register(registry, new EnchantedEarthArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedEarthArmor.ID_CHEST_TRUE));
    	register(registry, new EnchantedEarthArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedEarthArmor.ID_LEGS_NOVICE));
    	register(registry, new EnchantedEarthArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedEarthArmor.ID_LEGS_ADEPT));
    	register(registry, new EnchantedEarthArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedEarthArmor.ID_LEGS_MASTER));
    	register(registry, new EnchantedEarthArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedEarthArmor.ID_LEGS_TRUE));
    	register(registry, new EnchantedEarthArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedEarthArmor.ID_FEET_NOVICE));
    	register(registry, new EnchantedEarthArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedEarthArmor.ID_FEET_ADEPT));
    	register(registry, new EnchantedEarthArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedEarthArmor.ID_FEET_MASTER));
    	register(registry, new EnchantedEarthArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedEarthArmor.ID_FEET_TRUE));
    	
    	register(registry, new EnchantedEnderArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedEnderArmor.ID_HELM_NOVICE));
    	register(registry, new EnchantedEnderArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedEnderArmor.ID_HELM_ADEPT));
    	register(registry, new EnchantedEnderArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedEnderArmor.ID_HELM_MASTER));
    	register(registry, new EnchantedEnderArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedEnderArmor.ID_HELM_TRUE));
    	register(registry, new EnchantedEnderArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedEnderArmor.ID_CHEST_NOVICE));
    	register(registry, new EnchantedEnderArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedEnderArmor.ID_CHEST_ADEPT));
    	register(registry, new EnchantedEnderArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedEnderArmor.ID_CHEST_MASTER));
    	register(registry, new EnchantedEnderArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedEnderArmor.ID_CHEST_TRUE));
    	register(registry, new EnchantedEnderArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedEnderArmor.ID_LEGS_NOVICE));
    	register(registry, new EnchantedEnderArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedEnderArmor.ID_LEGS_ADEPT));
    	register(registry, new EnchantedEnderArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedEnderArmor.ID_LEGS_MASTER));
    	register(registry, new EnchantedEnderArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedEnderArmor.ID_LEGS_TRUE));
    	register(registry, new EnchantedEnderArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedEnderArmor.ID_FEET_NOVICE));
    	register(registry, new EnchantedEnderArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedEnderArmor.ID_FEET_ADEPT));
    	register(registry, new EnchantedEnderArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedEnderArmor.ID_FEET_MASTER));
    	register(registry, new EnchantedEnderArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedEnderArmor.ID_FEET_TRUE));
    	
    	register(registry, new EnchantedFireArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedFireArmor.ID_HELM_NOVICE));
    	register(registry, new EnchantedFireArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedFireArmor.ID_HELM_ADEPT));
    	register(registry, new EnchantedFireArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedFireArmor.ID_HELM_MASTER));
    	register(registry, new EnchantedFireArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedFireArmor.ID_HELM_TRUE));
    	register(registry, new EnchantedFireArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedFireArmor.ID_CHEST_NOVICE));
    	register(registry, new EnchantedFireArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedFireArmor.ID_CHEST_ADEPT));
    	register(registry, new EnchantedFireArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedFireArmor.ID_CHEST_MASTER));
    	register(registry, new EnchantedFireArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedFireArmor.ID_CHEST_TRUE));
    	register(registry, new EnchantedFireArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedFireArmor.ID_LEGS_NOVICE));
    	register(registry, new EnchantedFireArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedFireArmor.ID_LEGS_ADEPT));
    	register(registry, new EnchantedFireArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedFireArmor.ID_LEGS_MASTER));
    	register(registry, new EnchantedFireArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedFireArmor.ID_LEGS_TRUE));
    	register(registry, new EnchantedFireArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedFireArmor.ID_FEET_NOVICE));
    	register(registry, new EnchantedFireArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedFireArmor.ID_FEET_ADEPT));
    	register(registry, new EnchantedFireArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedFireArmor.ID_FEET_MASTER));
    	register(registry, new EnchantedFireArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedFireArmor.ID_FEET_TRUE));
    	
    	register(registry, new EnchantedIceArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_HELM_NOVICE));
    	register(registry, new EnchantedIceArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_HELM_ADEPT));
    	register(registry, new EnchantedIceArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_HELM_MASTER));
    	register(registry, new EnchantedIceArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_HELM_TRUE));
    	register(registry, new EnchantedIceArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_CHEST_NOVICE));
    	register(registry, new EnchantedIceArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_CHEST_ADEPT));
    	register(registry, new EnchantedIceArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_CHEST_MASTER));
    	register(registry, new EnchantedIceArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_CHEST_TRUE));
    	register(registry, new EnchantedIceArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_LEGS_NOVICE));
    	register(registry, new EnchantedIceArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_LEGS_ADEPT));
    	register(registry, new EnchantedIceArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_LEGS_MASTER));
    	register(registry, new EnchantedIceArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_LEGS_TRUE));
    	register(registry, new EnchantedIceArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_FEET_NOVICE));
    	register(registry, new EnchantedIceArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_FEET_ADEPT));
    	register(registry, new EnchantedIceArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_FEET_MASTER));
    	register(registry, new EnchantedIceArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedIceArmor.ID_FEET_TRUE));
    	
    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_HELM_NOVICE));
    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_HELM_ADEPT));
    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_HELM_MASTER));
    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_HELM_TRUE));
    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_CHEST_NOVICE));
    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_CHEST_ADEPT));
    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_CHEST_MASTER));
    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_CHEST_TRUE));
    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_LEGS_NOVICE));
    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_LEGS_ADEPT));
    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_LEGS_MASTER));
    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_LEGS_TRUE));
    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_FEET_NOVICE));
    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_FEET_ADEPT));
    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_FEET_MASTER));
    	register(registry, new EnchantedLightningArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedLightningArmor.ID_FEET_TRUE));
    	
    	register(registry, new EnchantedPhysicalArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedPhysicalArmor.ID_HELM_NOVICE));
    	register(registry, new EnchantedPhysicalArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedPhysicalArmor.ID_HELM_ADEPT));
    	register(registry, new EnchantedPhysicalArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedPhysicalArmor.ID_HELM_MASTER));
    	register(registry, new EnchantedPhysicalArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedPhysicalArmor.ID_HELM_TRUE));
    	register(registry, new EnchantedPhysicalArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedPhysicalArmor.ID_CHEST_NOVICE));
    	register(registry, new EnchantedPhysicalArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedPhysicalArmor.ID_CHEST_ADEPT));
    	register(registry, new EnchantedPhysicalArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedPhysicalArmor.ID_CHEST_MASTER));
    	register(registry, new EnchantedPhysicalArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedPhysicalArmor.ID_CHEST_TRUE));
    	register(registry, new EnchantedPhysicalArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedPhysicalArmor.ID_LEGS_NOVICE));
    	register(registry, new EnchantedPhysicalArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedPhysicalArmor.ID_LEGS_ADEPT));
    	register(registry, new EnchantedPhysicalArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedPhysicalArmor.ID_LEGS_MASTER));
    	register(registry, new EnchantedPhysicalArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedPhysicalArmor.ID_LEGS_TRUE));
    	register(registry, new EnchantedPhysicalArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedPhysicalArmor.ID_FEET_NOVICE));
    	register(registry, new EnchantedPhysicalArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedPhysicalArmor.ID_FEET_ADEPT));
    	register(registry, new EnchantedPhysicalArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedPhysicalArmor.ID_FEET_MASTER));
    	register(registry, new EnchantedPhysicalArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedPhysicalArmor.ID_FEET_TRUE));
    	
    	register(registry, new EnchantedWindArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_HELM_NOVICE));
    	register(registry, new EnchantedWindArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_HELM_ADEPT));
    	register(registry, new EnchantedWindArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_HELM_MASTER));
    	register(registry, new EnchantedWindArmor(EquipmentSlotType.HEAD, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_HELM_TRUE));
    	register(registry, new EnchantedWindArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_CHEST_NOVICE));
    	register(registry, new EnchantedWindArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_CHEST_ADEPT));
    	register(registry, new EnchantedWindArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_CHEST_MASTER));
    	register(registry, new EnchantedWindArmor(EquipmentSlotType.CHEST, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_CHEST_TRUE));
    	register(registry, new EnchantedWindArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_LEGS_NOVICE));
    	register(registry, new EnchantedWindArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_LEGS_ADEPT));
    	register(registry, new EnchantedWindArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_LEGS_MASTER));
    	register(registry, new EnchantedWindArmor(EquipmentSlotType.LEGS, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_LEGS_TRUE));
    	register(registry, new EnchantedWindArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.NOVICE, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_FEET_NOVICE));
    	register(registry, new EnchantedWindArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.ADEPT, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_FEET_ADEPT));
    	register(registry, new EnchantedWindArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.MASTER, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_FEET_MASTER));
    	register(registry, new EnchantedWindArmor(EquipmentSlotType.FEET, EnchantedArmor.Type.TRUE, PropEquipment()).setRegistryName(EnchantedWindArmor.ID_FEET_TRUE));
    	
    	register(registry, new EnchantedWeapon(EMagicElement.ICE, EnchantedWeapon.Type.NOVICE).setRegistryName(EnchantedWeapon.ID_ICE_NOVICE));
    	register(registry, new EnchantedWeapon(EMagicElement.ICE, EnchantedWeapon.Type.ADEPT).setRegistryName(EnchantedWeapon.ID_ICE_ADEPT));
    	register(registry, new EnchantedWeapon(EMagicElement.ICE, EnchantedWeapon.Type.MASTER).setRegistryName(EnchantedWeapon.ID_ICE_MASTER));
    	register(registry, new EnchantedWeapon(EMagicElement.LIGHTNING, EnchantedWeapon.Type.NOVICE).setRegistryName(EnchantedWeapon.ID_LIGHTNING_NOVICE));
    	register(registry, new EnchantedWeapon(EMagicElement.LIGHTNING, EnchantedWeapon.Type.ADEPT).setRegistryName(EnchantedWeapon.ID_LIGHTNING_ADEPT));
    	register(registry, new EnchantedWeapon(EMagicElement.LIGHTNING, EnchantedWeapon.Type.MASTER).setRegistryName(EnchantedWeapon.ID_LIGHTNING_MASTER));
    	register(registry, new EnchantedWeapon(EMagicElement.WIND, EnchantedWeapon.Type.NOVICE).setRegistryName(EnchantedWeapon.ID_WIND_NOVICE));
    	register(registry, new EnchantedWeapon(EMagicElement.WIND, EnchantedWeapon.Type.ADEPT).setRegistryName(EnchantedWeapon.ID_WIND_ADEPT));
    	register(registry, new EnchantedWeapon(EMagicElement.WIND, EnchantedWeapon.Type.MASTER).setRegistryName(EnchantedWeapon.ID_WIND_MASTER));
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
    	register(registry, new MirrorItem().setRegistryName(MirrorItem.ID));
    	register(registry, new MirrorShield().setRegistryName(MirrorShield.ID));
    	register(registry, new MirrorShieldImproved().setRegistryName(MirrorShieldImproved.ID));
    	register(registry, new NostrumGuide().setRegistryName(NostrumGuide.ID));
    	register(registry, new NostrumResourceItem().setRegistryName(NostrumResourceItem.ID_TOKEN));
    	register(registry, new NostrumResourceItem().setRegistryName(NostrumResourceItem.ID_PENDANT_LEFT));
    	register(registry, new NostrumResourceItem().setRegistryName(NostrumResourceItem.ID_PENDANT_RIGHT));
    	register(registry, new NostrumResourceItem().setRegistryName(NostrumResourceItem.ID_SLAB_FIERCE));
    	register(registry, new NostrumResourceItem().setRegistryName(NostrumResourceItem.ID_SLAB_KIND));
    	register(registry, new NostrumResourceItem().setRegistryName(NostrumResourceItem.ID_SLAB_BALANCED));
    	register(registry, new NostrumResourceItem().setRegistryName(NostrumResourceItem.ID_SPRITE_CORE));
    	register(registry, new NostrumResourceItem().setRegistryName(NostrumResourceItem.ID_ENDER_BRISTLE));
    	register(registry, new NostrumResourceItem().setRegistryName(NostrumResourceItem.ID_WISP_PEBBLE));
    	register(registry, new NostrumResourceItem().setRegistryName(NostrumResourceItem.ID_MANA_LEAF));
    	register(registry, new NostrumResourceItem().setRegistryName(NostrumResourceItem.ID_EVIL_THISTLE));
    	register(registry, new NostrumResourceItem().setRegistryName(NostrumResourceItem.ID_DRAGON_WING));
    	register(registry, new NostrumResourceItem().setRegistryName(NostrumResourceItem.ID_SEEKING_GEM));
    	register(registry, new NostrumResourceCrystal(NostrumBlocks.maniCrystalBlock, PropBase()).setRegistryName(NostrumResourceCrystal.ID_CRYSTAL_SMALL));
    	register(registry, new NostrumResourceCrystal(NostrumBlocks.kaniCrystalBlock, PropBase().rarity(Rarity.UNCOMMON)).setRegistryName(NostrumResourceCrystal.ID_CRYSTAL_MEDIUM));
    	register(registry, new NostrumResourceCrystal(NostrumBlocks.vaniCrystalBlock, PropBase().rarity(Rarity.RARE)).setRegistryName(NostrumResourceCrystal.ID_CRYSTAL_LARGE));
    	register(registry, new NostrumRoseItem().setRegistryName(NostrumRoseItem.ID_BLOOD_ROSE));
    	register(registry, new NostrumRoseItem().setRegistryName(NostrumRoseItem.ID_ELDRICH_ROSE));
    	register(registry, new NostrumRoseItem().setRegistryName(NostrumRoseItem.ID_PALE_ROSE));
    	register(registry, new NostrumSkillItem.Mirror().setRegistryName(NostrumSkillItem.ID_SKILL_MIRROR));
    	register(registry, new NostrumSkillItem.Ooze().setRegistryName(NostrumSkillItem.ID_SKILL_OOZE));
    	register(registry, new NostrumSkillItem.Pendant().setRegistryName(NostrumSkillItem.ID_SKILL_PENDANT));
    	register(registry, new NostrumSkillItem.Flute().setRegistryName(NostrumSkillItem.ID_SKILL_FLUTE));
    	register(registry, new NostrumSkillItem.EnderPin().setRegistryName(NostrumSkillItem.ID_SKILL_ENDER_PIN));
    	register(registry, new NostrumSkillItem.SmallScroll().setRegistryName(NostrumSkillItem.ID_SKILL_SCROLL_SMALL));
    	register(registry, new NostrumSkillItem.LargeScroll().setRegistryName(NostrumSkillItem.ID_SKILL_SCROLL_LARGE));
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
    	register(registry, new SpellcraftGuide().setRegistryName(SpellcraftGuide.ID));
    	register(registry, new SpellPlate(SpellTome.TomeStyle.NOVICE).setRegistryName(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_NOVICE));
    	register(registry, new SpellPlate(SpellTome.TomeStyle.ADVANCED).setRegistryName(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_ADVANCED));
    	register(registry, new SpellPlate(SpellTome.TomeStyle.COMBAT).setRegistryName(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_COMBAT));
    	register(registry, new SpellPlate(SpellTome.TomeStyle.DEATH).setRegistryName(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_DEATH));
    	register(registry, new SpellPlate(SpellTome.TomeStyle.SPOOKY).setRegistryName(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_SPOOKY));
    	register(registry, new SpellPlate(SpellTome.TomeStyle.MUTED).setRegistryName(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_MUTED));
    	register(registry, new SpellPlate(SpellTome.TomeStyle.LIVING).setRegistryName(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_LIVING));
    	//register(registry, new SpellRune().setRegistryName(SpellRune.ID));
    	register(registry, new SpellScroll().setRegistryName(SpellScroll.ID));
    	register(registry, new SpellTableItem().setRegistryName(SpellTableItem.ID));
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
	    	for (SpellTrigger type : SpellTrigger.getAllTriggers()) {
	    		registerRune(registry, new TriggerSpellRune(type));
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
