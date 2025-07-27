package com.smanzana.nostrummagica.item.armor;

import java.util.UUID;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.client.model.NostrumModelLayers;
import com.smanzana.nostrummagica.client.model.WitchHatModel;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;

public class MageArmor extends ArmorItem implements ILoreTagged {
	
	private static final UUID[] ARMOR_MODIFIERS = new UUID[]{UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")};

	private static final String ID_PREFIX = "magearmor_";
	public static final String ID_HELM = ID_PREFIX + "helm";
	public static final String ID_CHEST = ID_PREFIX + "chest";
	public static final String ID_LEGS = ID_PREFIX + "legs";
	public static final String ID_FEET = ID_PREFIX + "feet";
	
	protected Multimap<Attribute, AttributeModifier> attributes;

	public MageArmor(EquipmentSlot slot, Item.Properties properties) {
		super(ArmorMaterials.LEATHER, slot, properties.durability(250));
	}
	
	protected Multimap<Attribute, AttributeModifier> makeAttributes() {
		final int armor;
		final float potency = 2.5f;
		switch (slot) {
		case CHEST:
			armor = 6;
			break;
		case FEET:
			armor = 3;
			break;
		case HEAD:
			armor = 3;
			break;
		case LEGS:
			armor = 5;
			break;
		case MAINHAND:
		case OFFHAND:
		default:
			armor = 0;
			break;
		
		}
		
		ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		builder.put(Attributes.ARMOR, new AttributeModifier(ARMOR_MODIFIERS[this.slot.getIndex()],
				"Armor modifier", (double) armor, AttributeModifier.Operation.ADDITION));
		builder.put(NostrumAttributes.magicPotency, new AttributeModifier(ARMOR_MODIFIERS[this.slot.getIndex()],
				"Magic Potency", (double) potency, AttributeModifier.Operation.ADDITION));
		return builder.build();
	}
	
//	public String getModelID() {
//		return id;
//	}
	
	@Override
	public int getEnchantmentValue() {
		return 20; // not as much as gold but very good otherwise
	}
	
	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
		return !repair.isEmpty() && repair.is(NostrumTags.Items.CrystalSmall);
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		if (equipmentSlot == this.slot) {
			if (attributes == null) {
				attributes = this.makeAttributes();
			}
			return attributes;
		}
		
		return ImmutableMultimap.of();
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_mage_armor";
	}

	@Override
	public String getLoreDisplayName() {
		return "Mage Armor";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("By modifying leather armor, you've made armor that seems must more suitable to be used for mages!");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("By modifying leather armor, you've made armor that seems must more suitable to be used for mages!");
	}

	@Override
	public ELoreCategory getCategory() {
		return ELoreCategory.ITEM;
	}
	
	public static MageArmor get(EquipmentSlot slot) {
		switch (slot) {
		case CHEST:
			return NostrumItems.mageArmorChest;
		case FEET:
			return NostrumItems.mageArmorFeet;
		case HEAD:
			return NostrumItems.mageArmorHelm;
		case LEGS:
			return NostrumItems.mageArmorLegs;
		default:
			break;
		}
		
		return null;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		// Make this render the helm invisible, since I can't figure out how to make helmet just not render
		final boolean isLegSlot = slot == EquipmentSlot.LEGS;
		return String.format("%s:textures/models/armor/mage_layer_%d%s.png", NostrumMagica.MODID, (isLegSlot ? 2 : 1), type == null ? "" : String.format("_%s", type));
	}

	private static WitchHatModel<?> model;
	
	@Override
	public void initializeClient(Consumer<IItemRenderProperties> props) {
		super.initializeClient(props);
		props.accept(new IItemRenderProperties() {
			@Override
			public HumanoidModel<?> getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				if (armorSlot == EquipmentSlot.HEAD && MageArmor.this.slot == armorSlot) {
					if (model == null) {
						model = new WitchHatModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(NostrumModelLayers.WitchHat));
					}
					return model;
				}
				return _default;
			}
		});
	}
	

}
