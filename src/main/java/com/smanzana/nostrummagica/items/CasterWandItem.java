package com.smanzana.nostrummagica.items;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.NostrumAttributes;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

public class CasterWandItem extends ChargingSwordItem implements ILoreTagged, ISpellContainerItem, IRaytraceOverlay {

	public static final String ID = "caster_wand";
	
	protected static UUID WAND_POTENCY_UUID = UUID.fromString("d3589ae7-6abf-4c14-8b2a-502db550906b");
	
	private static final String NBT_SPELL = "spell";
	
	public CasterWandItem() {
		super(ItemTier.WOOD, 2, -2.4F, NostrumItems.PropEquipment().rarity(Rarity.UNCOMMON).maxDamage(300));
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
		Multimap<Attribute, AttributeModifier> multimap = HashMultimap.<Attribute, AttributeModifier>create();
		ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		builder.putAll(multimap);

		if (equipmentSlot == EquipmentSlotType.MAINHAND) {
            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 1, AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, AttributeModifier.Operation.ADDITION));
		}
		
		if (equipmentSlot == EquipmentSlotType.MAINHAND || equipmentSlot == EquipmentSlotType.OFFHAND) {
			builder.put(NostrumAttributes.magicPotency, new AttributeModifier(WAND_POTENCY_UUID, "Potency modifier", 20, AttributeModifier.Operation.ADDITION));
		}

        return builder.build();
    }
	
	@Override
	public String getLoreKey() {
		return "nostrum_caster_wand";
	}

	@Override
	public String getLoreDisplayName() {
		return "Caster Wands";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		if (repair.isEmpty()) {
			return false;
		} else {
			return NostrumTags.Items.WispPebble.contains(repair.getItem());
		}
    }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(new StringTextComponent("weight -1 lol"));
		
		final @Nullable Spell spell = getSpell(stack);
		if (spell != null) {
			tooltip.add(new StringTextComponent("Spell"));
		} else {
			tooltip.add(new StringTextComponent("No Bound Spell"));
		}
	}
	
	protected void setSpell(ItemStack wand, @Nullable Spell spell) {
		CompoundNBT tag = wand.getOrCreateTag();
		if (spell == null) {
			tag.remove(NBT_SPELL);
		} else {
			tag.putInt(NBT_SPELL, spell.getRegistryID());
		}
		wand.setTag(tag);
	}
	
	public static final void SetSpell(ItemStack wand, @Nullable Spell spell) {
		if (!wand.isEmpty() && wand.getItem() instanceof CasterWandItem) {
			((CasterWandItem) wand.getItem()).setSpell(wand, spell);
		}
	}
	
	@Override
	public @Nullable Spell getSpell(ItemStack wand) {
		if (wand.hasTag()) {
			CompoundNBT tag = wand.getTag();
			if (tag.contains(NBT_SPELL, NBT.TAG_INT)) {
				final int id = tag.getInt(NBT_SPELL);
				return NostrumMagica.instance.getSpellRegistry().lookup(id);
			}
		}
		return null;
	}
	
	public static final @Nullable Spell GetSpell(ItemStack wand) {
		if (!wand.isEmpty() && wand.getItem() instanceof CasterWandItem) {
			return ((CasterWandItem) wand.getItem()).getSpell(wand);
		}
		
		return null;
	}
	
	protected boolean canStoreSpell(ItemStack wand, @Nonnull Spell spell) {
		// No real limits on spell?
		return true;
	}
	
	public static final boolean CanStoreSpell(ItemStack wand, @Nonnull Spell spell) {
		if (!wand.isEmpty() && wand.getItem() instanceof CasterWandItem) {
			return ((CasterWandItem) wand.getItem()).canStoreSpell(wand, spell);
		}
		
		return false;
	}
	
	public static final class CasterWandColor implements IItemColor {
		@Override
		public int getColor(ItemStack stack, int layer) {
			if (layer == 1) {
				// Color based on spell
				final @Nullable Spell spell = GetSpell(stack);
				if (spell != null) {
					return 0xFF000000 | spell.getPrimaryElement().getColor();
				} else {
					return 0xFFFFFFFF; // No alpha
				}
			} else {
				return 0xFFFFFFFF;
			}
		}
	}

	@Override
	public boolean shouldTrace(World world, PlayerEntity player, ItemStack stack) {
		@Nullable Spell spell = this.getSpell(stack);
		if (spell == null) {
			return false;
		}
		
		return spell.shouldTrace();
	}
	
	@Override
	public double getTraceRange(World world, PlayerEntity player, ItemStack stack) {
		@Nullable Spell spell = this.getSpell(stack);
		if (spell == null) {
			return 0;
		}
		
		return spell.getTraceRange();
	}
	
	@Override
	protected boolean canCharge(World worldIn, PlayerEntity playerIn, Hand hand, ItemStack stack) {
		@Nullable Spell spell = this.getSpell(stack);
		return spell != null;
	}

	@Override
	protected boolean shouldAutoFire(ItemStack stack) {
		return true;
	}

	@Override
	protected int getTotalChargeTime(ItemStack stack) {
		return 20;
	}

	@Override
	protected void fireChargedWeapon(World worldIn, LivingEntity playerIn, Hand hand, ItemStack stack) {
		;
	}

}
