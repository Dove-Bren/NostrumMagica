package com.smanzana.nostrummagica.integration.curios.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.item.api.ISpellEquipment;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.StatSyncMessage;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import top.theillusivec4.curios.api.SlotContext;

public class NostrumCurio extends Item implements INostrumCurio, ILoreTagged, ISpellEquipment {
	
	public static final String ID_RIBBON_SMALL = "ribbon_small";
	public static final String ID_RIBBON_MEDIUM = "ribbon_medium";
	public static final String ID_RIBBON_LARGE = "ribbon_large";
	public static final String ID_RIBBON_FIERCE = "ribbon_fierce";
	public static final String ID_RIBBON_KIND = "ribbon_kind";
	public static final String ID_BELT_LIGHTNING = "belt_lightning";
	public static final String ID_BELT_ENDER = "belt_ender";
	public static final String ID_RING_GOLD = "ring_gold";
	public static final String ID_RING_GOLD_TRUE = "ring_gold_true";
	public static final String ID_RING_GOLD_CORRUPTED = "ring_gold_corrupted";
	public static final String ID_RING_SILVER = "ring_silver";
	public static final String ID_RING_SILVER_TRUE = "ring_silver_true";
	public static final String ID_RING_SILVER_CORRUPTED = "ring_silver_corrupted";
	public static final String ID_RING_MYSTIC = "ring_mystic";
	public static final String ID_RING_MAGE = "ring_mage";
	public static final String ID_RING_KOID = "ring_koid";
	public static final String ID_NECK_KOID = "necklace_koid";
	public static final String ID_BELT_GOLEM = "belt_golem";
	public static final String ID_RING_GOLEM = "ring_golem";

	private boolean requiresMagic; // To equip
	
	private int manaBonus;
	private float manaRegenModifier;
	private float manaCostModifier;
	private float castEfficiency;
	private UUID attribID;
	private final Map<Supplier<Attribute>, AttributeModifier> modifiers;
	
	private final String desckey;
	
	public NostrumCurio(Item.Properties builder, String descKey) {
		super(builder);
		this.desckey = descKey;
		this.modifiers = new HashMap<>();
	}
	
	public NostrumCurio requiresMagic() {
		this.requiresMagic = true;
		return this;
	}
	
	public NostrumCurio attrID(UUID id) {
		this.attribID = id;
		return this;
	}

	public NostrumCurio manaBonus(int manaBonus) {
		Validate.notNull(this.attribID);
		this.manaBonus = manaBonus;
		return this;
	}

	public NostrumCurio manaRegenModifier(float manaRegenModifier) {
		Validate.notNull(this.attribID);
		this.manaRegenModifier = manaRegenModifier;
		return this;
	}

	public NostrumCurio manaCostModifier(float manaCostModifier) {
		Validate.notNull(this.attribID);
		this.manaCostModifier = manaCostModifier;
		return this;
	}

	public NostrumCurio castEfficiency(float castEfficiency) {
		this.castEfficiency = castEfficiency;
		return this;
	}
	
	public NostrumCurio attribute(Supplier<Attribute> attribute, AttributeModifier modifier) {
		this.modifiers.put(attribute, modifier);
		return this;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		
		if (this.desckey == null) {
			return;
		}
		
		final String trans = this.getOrCreateDescriptionId() + ".desc";
		
		if (!I18n.exists(trans)) {
			return;
		}
		
		// Format with placeholders for blue and red formatting
		String translation = I18n.get(trans, ChatFormatting.GRAY, ChatFormatting.BLUE, ChatFormatting.DARK_RED);
		if (translation.trim().isEmpty())
			return;
		String lines[] = translation.split("\\|");
		for (String line : lines) {
			tooltip.add(new TextComponent(line));
		}
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return INostrumCurio.initCapabilities(stack, nbt);
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_baubles";
	}

	@Override
	public String getLoreDisplayName() {
		return "Magic Baubles";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("By imbuing raw materials with magical reagents, you've discovered a way to created small baubles that enhance your magical powers!");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("By imbuing raw materials with magical reagents, you've discovered a way to created small baubles that enhance your magical powers!", "Cloth and precious metals seem to be especially willing to be imbued. While leather hasn't proven the same, you're sure you can affix a crystal or two to make it work!");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	public void onWornTick(ItemStack stack, SlotContext slot) {
		;
	}

	@Override
	public void onEquipped(ItemStack stack, SlotContext slot) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(slot.entity());
		if (attr == null) {
			return;
		}
		
		if (this.manaBonus != 0) attr.addManaBonus(this.attribID, this.manaBonus);
		
		if (slot.entity() instanceof ServerPlayer player) {
			NetworkHandler.sendTo(
					new StatSyncMessage(attr), player);
		}
		
	}

	@Override
	public void onUnequipped(ItemStack stack, SlotContext slot) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(slot.entity());
		if (attr == null) {
			return;
		}
		
		attr.removeManaBonus(this.attribID);
		
		if (slot.entity() instanceof ServerPlayer player) {
			NetworkHandler.sendTo(
					new StatSyncMessage(attr), player);
		}
	}

	@Override
	public boolean canEquip(ItemStack stack, SlotContext slot) {
		final LivingEntity entity = slot.entity();
		if (entity.level.isClientSide && entity != NostrumMagica.Proxy.getPlayer()) {
			return true; // Auto allow for other entities on client side when the server says they have them
		}
		
		if (this.requiresMagic) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
			if (attr == null || !attr.isUnlocked()) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public void apply(LivingEntity caster, Spell spell, SpellCastSummary summary, ItemStack stack) {
		;
	}

	@Override
	public Multimap<Attribute,AttributeModifier> getEquippedAttributeModifiers(ItemStack stack) {
		ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		
		for (Entry<Supplier<Attribute>, AttributeModifier> entry : modifiers.entrySet()) {
			final Attribute attribute = entry.getKey().get();
			if (attribute != null) {
				builder.put(attribute, entry.getValue());
			}
		}

		if (manaCostModifier != 0f) {
			builder.put(NostrumAttributes.manaCost, new AttributeModifier(this.attribID, "Mana Cost Reduc (Curio)", -manaCostModifier * 100, AttributeModifier.Operation.ADDITION));
		}
		
		if (manaRegenModifier != 0f) {
			builder.put(NostrumAttributes.manaRegen, new AttributeModifier(this.attribID, "Mana Regen (Curio)", manaRegenModifier * 100, AttributeModifier.Operation.ADDITION));
		}

		if (castEfficiency != 0f) {
			builder.put(NostrumAttributes.magicPotency, new AttributeModifier(this.attribID, "Potency (Curio)", this.castEfficiency * 100, AttributeModifier.Operation.ADDITION));
		}
		
		return builder.build();
	}
}
