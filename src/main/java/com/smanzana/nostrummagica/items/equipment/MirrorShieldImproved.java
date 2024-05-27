package com.smanzana.nostrummagica.items.equipment;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.NostrumAttributes;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.listeners.PlayerListener.Event;
import com.smanzana.nostrummagica.listeners.PlayerListener.SpellActionListenerData;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// Dumb name to keep it sorted. Worth?
public class MirrorShieldImproved extends MirrorShield {

	public static final String ID = "true_mirror_shield";
	private static final String NBT_CHARGED = "charged";
	
	public static final float CHARGE_CHANCE = 0.25f;
	
	public MirrorShieldImproved() {
		super(NostrumItems.PropEquipment().rarity(Rarity.UNCOMMON).maxDamage(1250));
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
		Multimap<Attribute, AttributeModifier> multimap = HashMultimap.<Attribute, AttributeModifier>create();

		if (equipmentSlot == EquipmentSlotType.OFFHAND) {
			ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
			builder.putAll(multimap);
			builder.put(NostrumAttributes.magicResist, new AttributeModifier(MOD_RESIST_UUID, "Magic Shield Resist", 20, AttributeModifier.Operation.ADDITION));
			multimap = builder.build();
		}

		return multimap;
	}
	
	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000; // Maybe make longer/shorter?
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		return super.onItemRightClick(worldIn, playerIn, hand);
	}

	@Override
	public boolean onEvent(Event type, LivingEntity entity, SpellActionListenerData data) {
		
		if (type == Event.MAGIC_EFFECT) {
			if (entity.isActiveItemStackBlocking() && entity.getActiveItemStack().getItem() instanceof MirrorShieldImproved) {
				ItemStacks.damageItem(entity.getActiveItemStack(), entity, entity.getActiveHand(), NostrumMagica.rand.nextInt(2) + 1);
				//entity.getActiveItemStack().damageItem(NostrumMagica.rand.nextInt(2) + 1, entity);
				
				float reduc = 0.3f;
				
				if (getBlockCharged(entity.getActiveItemStack())) {
					markBlockCharged(entity.getActiveItemStack(), false);
					reduc = 1f;
				}
				
				// If there was a caster, reflect part of the spell back
				if (data.caster != null && data.caster != entity) {
					data.summary.getAction().apply(entity, data.caster, reduc);
				}
				
				// If fully blocked, cancel. Otherwise, reduce
				if (reduc >= 1f)
					data.summary.cancel();
				else
					data.summary.addEfficiency(-reduc);
			} else {
				if (!entity.getHeldItemOffhand().isEmpty() && entity.getHeldItemOffhand().getItem() instanceof MirrorShieldImproved) {
					// If holding mirror shield in offhand but not actively blocking, have chance of charging
					if (!getBlockCharged(entity.getHeldItemOffhand()) && random.nextFloat() < CHARGE_CHANCE)
						markBlockCharged(entity.getHeldItemOffhand(), true);
				}
			}
		}
		
		return false;
	}

	@Override
	public String getLoreKey() {
		return "true_mirror_shield";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("A Mirror Shield further refined to block and reflect magic damage better.", "This shield blocks and reflects more damage than the regular Mirror Shield!", "However, it appears to have lost its defensive bonus.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("A Mirror Shield further refined to block and reflect magic damage better.", "This shield passively blocks 20% of magic damage, and reduces+reflects an additional 30% while blocking.", "Additionally, it charges off of the magic it absorbs when not blocking.", "Once charged, it can completely reflect spell effects when used to block a spell!");
	}
	
	// TODO another shield that absorbs mana instead of reflecting?

	public static boolean getBlockCharged(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof MirrorShieldImproved)) {
			return false;
		}
		
		CompoundNBT nbt = stack.getTag();
		if (nbt != null && nbt.contains(NBT_CHARGED)) {
			return nbt.getBoolean(NBT_CHARGED);
		}
		
		return false;
	}
	
	public static void markBlockCharged(ItemStack stack, boolean charged) {
		if (stack.isEmpty() || !(stack.getItem() instanceof MirrorShieldImproved)) {
			return;
		}
		
		CompoundNBT nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundNBT();
		}
		
		nbt.putBoolean(NBT_CHARGED, charged);
		stack.setTag(nbt);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static final float ModelCharged(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
		return getBlockCharged(stack) ? 1.0F : 0.0F;
	}

}
