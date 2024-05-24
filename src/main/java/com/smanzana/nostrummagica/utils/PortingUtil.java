package com.smanzana.nostrummagica.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.aetheria.AetheriaProxy;
import com.smanzana.nostrummagica.integration.curios.items.NostrumCurios;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.MagicCharm;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.armor.MagicArmor;
import com.smanzana.nostrummagica.items.equipment.AspectedWeapon;
import com.smanzana.nostrummagica.items.SpellPlate;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.tiles.NostrumTileEntities;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.EquipmentSlotType.Group;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants.NBT;

public class PortingUtil {
	
	public static final @Nullable SpellRune FindRuneFrom1_12_2(CompoundNBT tag) {
		tag = tag.getCompound("tag");
		
		SpellRune rune;
		final String oldType = tag.getString("type");
		String oldName= tag.getString("name");
		
		// trigger and shape used to be prefixed
		final String triggerPrefix = "trigger_";
		if (oldName.startsWith(triggerPrefix)) {
			oldName = oldName.substring(triggerPrefix.length());
		}
		
		final String shapePrefix = "shape_";
		if (oldName.startsWith(shapePrefix)) {
			oldName = oldName.substring(shapePrefix.length());
		}
		
		SpellComponentWrapper component = null;
		switch (oldType) {
		case "element": 
			EMagicElement elem = EMagicElement.valueOf(oldName.toUpperCase());
			component = new SpellComponentWrapper(elem);
			break;
		case "alteration":
			EAlteration alter = EAlteration.valueOf(oldName.toUpperCase());
			component = new SpellComponentWrapper(alter);
			break;
		case "shape":
			SpellShape shape = SpellShape.get(oldName);
			component = new SpellComponentWrapper(shape);
			break;
		case "trigger":
			SpellTrigger trigger = SpellTrigger.get(oldName);
			component = new SpellComponentWrapper(trigger);
			break;
		}
		
		if (component == null) {
			NostrumMagica.logger.warn("Failed to remap rune: " + oldType + "[" + oldName + "]");
			component = new SpellComponentWrapper(EMagicElement.PHYSICAL);
		}
		
		rune = SpellRune.GetRuneForType(component);
		
		return rune;
	}

	protected static Map<String, Map<Integer, Item>> ItemMap1_12_2 = null;
	
	private static final void AddNameTransform1_12_2(String old, Item newItem) {
		Map<Integer, Item> submap = new HashMap<>();
		submap.put(0, newItem);
		ItemMap1_12_2.put(old, submap);
	}
	
	private static final void ItemMap1_12_2_Init() {
		Map<Integer, Item> submap = new HashMap<>();
		
		String id = "nostrummagica:charm";
		{
			for (EMagicElement elem : EMagicElement.values()) {
				submap.put(elem.ordinal(), MagicCharm.getCharmItem(elem));
			}
		}
		ItemMap1_12_2.put(id, submap);
		
		submap = new HashMap<>();
		id = "nostrummagica:nostrum_resource";
		{
			submap.put(0, NostrumItems.resourceToken);
			submap.put(1, NostrumItems.crystalSmall);
			submap.put(2, NostrumItems.crystalMedium);
			submap.put(3, NostrumItems.crystalLarge);
			submap.put(4, NostrumItems.resourcePendantLeft);
			submap.put(5, NostrumItems.resourcePendantRight);
			submap.put(6, NostrumItems.resourceSlabFierce);
			submap.put(7, NostrumItems.resourceSlabKind);
			submap.put(8, NostrumItems.resourceSlabBalanced);
			submap.put(9, NostrumItems.resourceSpriteCore);
			submap.put(10, NostrumItems.resourceEnderBristle);
			submap.put(11, NostrumItems.resourceWispPebble);
			submap.put(12, NostrumItems.resourceManaLeaf);
			submap.put(13, NostrumItems.resourceEvilThistle);
		}
		ItemMap1_12_2.put(id, submap);
		
		submap = new HashMap<>();
		id = "nostrummagica:nostrum_gem";
		{
			for (EMagicElement elem : EMagicElement.values()) {
				submap.put(elem.ordinal() + 1, InfusedGemItem.getGemItem(elem));
			}
			// Physical was weird
			submap.put(0, InfusedGemItem.getGemItem(EMagicElement.PHYSICAL));
		}
		ItemMap1_12_2.put(id, submap);
		
		submap = new HashMap<>();
		id = "nostrummagica:nostrum_essence";
		{
			for (EMagicElement elem : EMagicElement.values()) {
				submap.put(elem.ordinal(), EssenceItem.getEssenceItem(elem));
			}
		}
		ItemMap1_12_2.put(id, submap);
		
		submap = new HashMap<>();
		id = "nostrummagica:skillitem";
		{
			submap.put(0, NostrumItems.skillMirror);
			submap.put(1, NostrumItems.skillOoze);
			submap.put(2, NostrumItems.skillPendant);
			submap.put(3, NostrumItems.skillFlute);
			submap.put(4, NostrumItems.resourceDragonWing);
			submap.put(5, NostrumItems.skillEnderPin);
			submap.put(6, NostrumItems.skillScrollSmall);
			submap.put(7, NostrumItems.skillScrollLarge);
		}
		ItemMap1_12_2.put(id, submap);
		
		submap = new HashMap<>();
		id = "nostrummagica:roseitem";
		{
			submap.put(0, NostrumItems.roseBlood);
			submap.put(1, NostrumItems.roseEldrich);
			submap.put(2, NostrumItems.rosePale);
		}
		ItemMap1_12_2.put(id, submap);
		
		submap = new HashMap<>();
		id = "nostrummagica:nostrum_reagent";
		{
			for (ReagentType type : ReagentType.values()) {
				submap.put(type.ordinal(), ReagentItem.GetItem(type));
			}
		}
		ItemMap1_12_2.put(id, submap);
		
		submap = new HashMap<>();
		id = "nostrummagica:spelltomeplate";
		{
			for (SpellTome.TomeStyle style : SpellTome.TomeStyle.values()) {
				submap.put(style.ordinal(), SpellPlate.GetPlateForStyle(style));
			}
		}
		ItemMap1_12_2.put(id, submap);
		
		submap = new HashMap<>();
		id = "nostrummagica:spelltome";
		{
			for (SpellTome.TomeStyle style : SpellTome.TomeStyle.values()) {
				submap.put(style.ordinal(), SpellTome.GetTomeForStyle(style));
			}
		}
		ItemMap1_12_2.put(id, submap);
		
		AddNameTransform1_12_2("nostrummagica:magichelmbase", NostrumItems.magicArmorBaseHelm);
		AddNameTransform1_12_2("nostrummagica:magicchestbase", NostrumItems.magicArmorBaseChest);
		AddNameTransform1_12_2("nostrummagica:magicleggingsbase", NostrumItems.magicArmorBaseLegs);
		AddNameTransform1_12_2("nostrummagica:magicfeetbase", NostrumItems.magicArmorBaseFeet);
		
		for (EMagicElement element : EMagicElement.values()) {
			for (EquipmentSlotType slot : EquipmentSlotType.values()) {
				if (slot.getSlotType() != Group.ARMOR) {
					continue;
				}
				
				for (int i = 0; i < 4; i++) {
					if (i < 3 && !MagicArmor.isArmorElement(element)) {
						continue;
					}
					
					id = "nostrummagica:armor_" + slot.name().toLowerCase() + "_" + element.name().toLowerCase() + (i + 1);
					final MagicArmor.Type type = MagicArmor.Type.values()[i];
					
					submap = new HashMap<>();
					submap.put(0, MagicArmor.get(element, slot, type));
					ItemMap1_12_2.put(id, submap);
				}
			}
		}
		
		for (EMagicElement element : EMagicElement.values()) {
			if (!AspectedWeapon.isWeaponElement(element)) {
				continue;
			}
			
			for (int i = 0; i < 3; i++) {
				id = "nostrummagica:sword_" + element.name().toLowerCase() + (i + 1);
				final AspectedWeapon.Type type = AspectedWeapon.Type.values()[i];
				
				submap = new HashMap<>();
				submap.put(0, AspectedWeapon.get(element, type));
				ItemMap1_12_2.put(id, submap);
			}
		}
		
		submap = new HashMap<>();
		id = "nostrummagica:ribbon";
		{
			submap.put(0, NostrumCurios.smallRibbon);
			submap.put(1, NostrumCurios.mediumRibbon);
			submap.put(2, NostrumCurios.largeRibbon);
			submap.put(3, NostrumCurios.fierceRibbon);
			submap.put(4, NostrumCurios.kindRibbon);
			submap.put(5, NostrumCurios.lightningBelt);
			submap.put(6, NostrumCurios.enderBelt);
			submap.put(7, NostrumCurios.ringGold);
			submap.put(8, NostrumCurios.ringTrueGold);
			submap.put(9, NostrumCurios.ringCorruptedGold);
			submap.put(10, NostrumCurios.ringSilver);
			submap.put(11, NostrumCurios.ringTrueSilver);
			submap.put(12, NostrumCurios.ringCorruptedSilver);
			submap.put(13, NostrumCurios.floatGuard);
			submap.put(14, AetheriaProxy.ringShieldSmall);
			submap.put(15, AetheriaProxy.ringShieldLarge);
			submap.put(16, AetheriaProxy.eludeCape);
			submap.put(17, NostrumCurios.dragonWingPendant);
		}
		ItemMap1_12_2.put(id, submap);
		
		AddNameTransform1_12_2("nostrummagica:pendant_whole", NostrumItems.thanoPendant);
	}
	
	protected static final @Nullable Item fixupItem1_12_2(String id, int damage) {
		if (ItemMap1_12_2 == null) {
			ItemMap1_12_2 = new HashMap<>();
			ItemMap1_12_2_Init();
		}
		
		Map<Integer, Item> submap = ItemMap1_12_2.get(id.toLowerCase());
		if (submap != null) {
			return submap.get(damage);
		}
		
		if (id.toLowerCase().contains("nostrum")) {
			NostrumMagica.logger.debug("Didn't find mapping for " + id);
		}
		
		return null;
	}
	
	public static final CompoundNBT fixupItemTag1_12_2(CompoundNBT itemTag) {
		final String id = itemTag.getString("id");
		final int damage = itemTag.getInt("Damage");
		
		// Special case for runes
		final Item item;
		if (id.equalsIgnoreCase("nostrummagica:nostrum_rune")) {
			item = FindRuneFrom1_12_2(itemTag);
		} else {
			item = fixupItem1_12_2(id, damage);
		}
		
		if (item != null) {
			// stamp in the new ID and remove the damage
			itemTag.remove("Damage");
			itemTag.putString("id", item.getRegistryName().toString());
			
			NostrumMagica.logger.debug("Replacing  " + id + "[" + damage + "] with " + item.getRegistryName().toString());
		}
		
		return itemTag;
	}
	
	public static final CompoundNBT fixupChest1_12_2(CompoundNBT chestTag) {
		ListNBT list = chestTag.getList("Items", NBT.TAG_COMPOUND);
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				fixupItemTag1_12_2(list.getCompound(i));
//				
//				final String id = list.getCompound(i).getString("id");
//				if (!Registry.ITEM.getValue(ResourceLocation.tryCreate(id)).isPresent()) {
//					NostrumMagica.logger.error("Can't deserialize item: " + id);
//				}
			}
		}
		
		return chestTag;
	}
	
	public static final CompoundNBT fixupAltar1_12_2(CompoundNBT altarTag) {
		if (altarTag.contains("item")) {
			altarTag.put("item", fixupItemTag1_12_2(altarTag.getCompound("item")));
		} else {
			// nothing to do
		}
		
		return altarTag;
	}
	
	// Returns null if no fixup is required
	protected static final @Nullable String fixupSpellComponentWrapperKey(String key) {
		// Trigger and shape had prefixes that are stripped off
		// trigger and shape used to be prefixed
		final String triggerPrefix = "trigger_";
		final String triggerKeyPrefix = "trigger:";
		if (key.startsWith(triggerKeyPrefix)) {
			// Part after has triggerPrefix on it
			String ending = key.substring(triggerKeyPrefix.length());
			ending = ending.substring(triggerPrefix.length());
			return triggerKeyPrefix + ending;
		}
		
		final String shapePrefix = "shape_";
		final String shapeKeyPrefix = "shape:";
		if (key.startsWith(shapeKeyPrefix)) {
			// Part after has shapePrefix on it
			String ending = key.substring(shapeKeyPrefix.length());
			ending = ending.substring(shapePrefix.length());
			return shapeKeyPrefix + ending;
		}
		
		return null;
	}
	
	public static final CompoundNBT fixupProgDoor1_12_2(CompoundNBT progDoorTag) {
		if (progDoorTag.contains("required_componenets")) { // Yup that typo is real
			ListNBT list = progDoorTag.getList("required_componenets", NBT.TAG_STRING);
			for (int i = 0; i < list.size(); i++) {
				final String keyString = list.getString(i);
				final @Nullable String fixupString = fixupSpellComponentWrapperKey(keyString);
				
				if (fixupString != null) {
					list.set(i, StringNBT.valueOf(fixupString));
				}
			}
		}
		
		return progDoorTag;
	}
	
	public static final CompoundNBT fixupSymbolEntity1_12_2(CompoundNBT symbolTag) {
		// Note: this is similar to how runes used to store this info, instead of
		// with a SpellComponentWrapper.
		final String oldName = symbolTag.getString("key");
		
		// trigger and shape used to be prefixed
		final String triggerPrefix = "trigger_";
		final String shapePrefix = "shape_";
		
		if (oldName.startsWith(triggerPrefix)) {
			symbolTag.putString("key", oldName.substring(triggerPrefix.length()));
		} else if (oldName.startsWith(shapePrefix)) {
			symbolTag.putString("key", oldName.substring(shapePrefix.length()));
		}
		
		return symbolTag;
	}
	
	public static final CompoundNBT fixupTileEntity_12_2(CompoundNBT teTag) {
		final String id = teTag.getString("id");
		
		// Chests and their inventory need a fixup
		if (id.equalsIgnoreCase("chest")) {
			return fixupChest1_12_2(teTag);
		}
		
		// Altars, Shrines, and progression doors do, too
		if (id.equalsIgnoreCase(NostrumTileEntities.AltarTileEntityType.getRegistryName().toString())) {
			return fixupAltar1_12_2(teTag);
		}
		if (id.equalsIgnoreCase(NostrumTileEntities.ProgressionDoorTileEntityType.getRegistryName().toString())) {
			return fixupProgDoor1_12_2(teTag);
		}
		if (id.equalsIgnoreCase(NostrumTileEntities.SymbolTileEntityType.getRegistryName().toString())) {
			return fixupSymbolEntity1_12_2(teTag);
		}
		
		// Else no fixup
		return teTag;
	}
	
	public static @Nullable UUID readNBTUUID(CompoundNBT nbt, String key) {
		if (nbt.contains(key)) {
			return nbt.getUniqueId(key);
		}
		
		return readNBTUUID_14_4(nbt, key);
		
	}
	
	public static @Nullable UUID readNBTUUID_14_4(CompoundNBT nbt, String key) {
		// Prior to 1.16, UUIDs were saved as two longs. It'd do like
		// "nbt.putUniqueID": nbt.putString(key + "_least", id.highbits); nbt.putString(key + "_most", ...)
		//
		// public void putUniqueId(String key, UUID value) {
		//   this.putLong(key + "Most", value.getMostSignificantBits());
		//   this.putLong(key + "Least", value.getLeastSignificantBits());
		// }
		final String leastKey = key + "Least";
		final String mostKey = key + "Most";
		if (nbt.contains(leastKey) && nbt.contains(mostKey)) {
			final long least = nbt.getLong(leastKey);
			final long most = nbt.getLong(mostKey);
			return new UUID(most, least);
		}
		
		return null;
	}
	
}
