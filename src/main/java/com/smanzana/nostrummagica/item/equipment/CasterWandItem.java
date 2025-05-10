package com.smanzana.nostrummagica.item.equipment;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.listener.ClientPlayerListener;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.item.IRaytraceOverlay;
import com.smanzana.nostrummagica.item.ISpellCastingTool;
import com.smanzana.nostrummagica.item.ISpellContainerItem;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellCastEvent;
import com.smanzana.nostrummagica.spell.SpellCasting;
import com.smanzana.nostrummagica.spell.SpellCasting.SpellCastResult;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.util.ItemStacks;

import net.minecraft.ChatFormatting;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CasterWandItem extends ChargingSwordItem implements ILoreTagged, ISpellContainerItem, IRaytraceOverlay, ISpellCastingTool {

	public static final String ID = "caster_wand";
	
	protected static UUID WAND_POTENCY_UUID = UUID.fromString("d3589ae7-6abf-4c14-8b2a-502db550906b");
	
	private static final String NBT_SPELL = "spell";
	
	public CasterWandItem() {
		super(Tiers.WOOD, 2, -2.4F, NostrumItems.PropEquipment().rarity(Rarity.UNCOMMON).durability(300));
		MinecraftForge.EVENT_BUS.addListener(CasterWandItem::onSpellCast);
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		Multimap<Attribute, AttributeModifier> multimap = HashMultimap.<Attribute, AttributeModifier>create();
		ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		builder.putAll(multimap);

		if (equipmentSlot == EquipmentSlot.MAINHAND) {
            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 4, AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4000000953674316D, AttributeModifier.Operation.ADDITION));
		}
		
		if (equipmentSlot == EquipmentSlot.MAINHAND || equipmentSlot == EquipmentSlot.OFFHAND) {
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
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
		if (repair.isEmpty()) {
			return false;
		} else {
			return repair.is(NostrumTags.Items.WispPebble);
		}
    }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		final @Nullable Spell spell = getSpell(stack);
		if (spell != null) {
			tooltip.add(new TranslatableComponent("info.caster_wand.spell", spell.getName()).withStyle(ChatFormatting.GOLD));
		} else {
			tooltip.add(new TranslatableComponent("info.caster_wand.nospell"));
		}
		
		tooltip.add(TextComponent.EMPTY);
		tooltip.add(new TranslatableComponent("info.caster_wand.desc").withStyle(ChatFormatting.GRAY));
		tooltip.add(new TextComponent(" -2 Spell Weight").withStyle(ChatFormatting.DARK_GREEN));
	}
	
	protected void setSpell(ItemStack wand, @Nullable Spell spell) {
		CompoundTag tag = wand.getOrCreateTag();
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
			CompoundTag tag = wand.getTag();
			if (tag.contains(NBT_SPELL, Tag.TAG_INT)) {
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
	
	public static final class CasterWandColor implements ItemColor {
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
	public boolean shouldTrace(Level world, Player player, ItemStack stack) {
		@Nullable Spell spell = this.getSpell(stack);
		if (spell == null) {
			return false;
		}
		
		return spell.shouldTrace(player);
	}
	
	@Override
	public double getTraceRange(Level world, Player player, ItemStack stack) {
		@Nullable Spell spell = this.getSpell(stack);
		if (spell == null) {
			return 0;
		}
		
		return spell.getTraceRange(player);
	}
	
	@Override
	protected boolean canCharge(Level worldIn, Player playerIn, InteractionHand hand, ItemStack stack) {
		@Nullable Spell spell = this.getSpell(stack);
		return spell != null;
	}

	@Override
	protected boolean shouldAutoFire(ItemStack stack) {
		return false;
	}

	@Override
	protected int getTotalChargeTime(ItemStack stack) {
		return 20;
	}

	@Override
	protected void fireChargedWeapon(Level worldIn, LivingEntity playerIn, InteractionHand hand, ItemStack stack) {
		@Nullable Spell spell = this.getSpell(stack);
		if (spell != null) {
			if (worldIn.isClientSide()) {
				if (SpellCasting.CheckToolCast(spell, playerIn, stack).succeeded) {
					ItemStacks.damageItem(stack, playerIn, hand, 1);
				} else {
					for (int i = 0; i < 15; i++) {
						double offsetx = Math.cos(i * (2 * Math.PI / 15)) * 1.0;
						double offsetz = Math.sin(i * (2 * Math.PI / 15)) * 1.0;
						playerIn.level
							.addParticle(ParticleTypes.LARGE_SMOKE,
									playerIn.getX() + offsetx, playerIn.getY(), playerIn.getZ() + offsetz,
									0, -.5, 0);
						
					}
					
					NostrumMagicaSounds.CAST_FAIL.playClient(playerIn);
					((ClientPlayerListener) NostrumMagica.playerListener).doManaWiggle(2);
				}
			} else {
				if (SpellCasting.AttemptToolCast(spell, playerIn, stack, null).succeeded) {
					ItemStacks.damageItem(stack, playerIn, hand, 1);
				}
			}
		}
	}

	@Override
	public void onStartCastFromTool(LivingEntity caster, SpellCastSummary summary, ItemStack stack) {
		// Reduce weight by 2!
		summary.addWeightDiscount(2);
	}

	@Override
	public void onFinishCastFromTool(LivingEntity caster, SpellCastSummary summary, ItemStack stack) {
		if (caster instanceof Player) {
			NostrumMagica.instance.proxy.sendMana((Player) caster);
			((Player) caster).getCooldowns().addCooldown(stack.getItem(), 20);
		}
	}
	
	@SubscribeEvent
	public static void onSpellCast(SpellCastEvent.Post event) {
		// Notice and respond any time any spell is cast.
		// Note that our spell caster handler will have put in a smaller one already, so this will replace it.
		final SpellCastResult result = event.getCastResult();
		if (!event.isChecking && result.succeeded && result.caster != null && result.caster instanceof Player && NostrumMagica.getMagicWrapper(result.caster) != null && !result.caster.isDeadOrDying()) {
			if (!NostrumMagica.getMagicWrapper(result.caster).hasSkill(NostrumSkills.Spellcasting_ToolCooldown)) {
				final int cooldownTicks = SpellCasting.CalculateSpellCooldown(result);
				((Player) result.caster).getCooldowns().addCooldown(NostrumItems.casterWand, cooldownTicks);
			}
		}
	}

}
