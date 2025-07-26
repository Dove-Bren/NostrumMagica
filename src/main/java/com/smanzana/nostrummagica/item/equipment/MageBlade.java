package com.smanzana.nostrummagica.item.equipment;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.effect.ElementalEnchantEffect;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.api.ICrystalEnchantableItem;
import com.smanzana.nostrummagica.item.api.ISpellEquipment;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellDamage;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.util.ItemStacks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MageBlade extends SwordItem implements ILoreTagged, ISpellEquipment, ICrystalEnchantableItem {

	public static final String ID = "mage_blade";
	
	protected static UUID MAGEBLADE_POTENCY_UUID = UUID.fromString("83088ef2-c0cc-401d-999c-0aeaf9e511b5");
	private static final String NBT_ELEMENT = "element";
	private static final String NBT_CHARGES = "charges";
	private static final String NBT_DAMAGE = "elemdamage";
	
	public MageBlade() {
		super(Tiers.DIAMOND, 3, -2.0F, NostrumItems.PropEquipment());
	}
	
	public @Nullable EMagicElement getElement(ItemStack stack) {
		EMagicElement stored = null;
		
		if (stack.hasTag() && stack.getTag().contains(NBT_ELEMENT)) {
			try {	
				stored = EMagicElement.parse(stack.getTag().getString(NBT_ELEMENT));
			} catch (Exception e) {
				stored = null;
			}
		}
		
		return stored;
	}
	
	public void setElement(ItemStack stack, @Nullable EMagicElement element) {
		if (stack.isEmpty() || !(stack.getItem() instanceof MageBlade)) {
			return;
		}
		
		CompoundTag tag = stack.getTag();
		if (tag == null) {
			tag = new CompoundTag();
		}
		
		if (element == null) {
			tag.remove(NBT_ELEMENT);
		} else {
			tag.putString(NBT_ELEMENT, element.name());
		}
		
		stack.setTag(tag);
	}
	
	public int getCharges(ItemStack stack) {
		int stored = 0;
		
		if (stack.hasTag()) {
			stored = stack.getTag().getInt(NBT_CHARGES);
		}
		
		return stored;
	}
	
	public void setCharges(ItemStack stack, int charges) {
		if (stack.isEmpty() || !(stack.getItem() instanceof MageBlade)) {
			return;
		}
		
		CompoundTag tag = stack.getTag();
		if (tag == null) {
			tag = new CompoundTag();
		}
		
		if (charges <= 0) {
			tag.remove(NBT_CHARGES);
		} else {
			tag.putInt(NBT_CHARGES, charges);
		}
		
		stack.setTag(tag);
	}
	
	public float getElementalDamage(ItemStack stack) {
		float stored = 0;
		
		if (stack.hasTag()) {
			stored = stack.getTag().getFloat(NBT_DAMAGE);
		}
		
		return stored;
	}
	
	public void setElementalDamage(ItemStack stack, float damage) {
		if (stack.isEmpty() || !(stack.getItem() instanceof MageBlade)) {
			return;
		}
		
		CompoundTag tag = stack.getTag();
		if (tag == null) {
			tag = new CompoundTag();
		}
		
		if (damage <= 0) {
			tag.remove(NBT_DAMAGE);
		} else {
			tag.putFloat(NBT_DAMAGE, damage);
		}
		
		stack.setTag(tag);
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		Multimap<Attribute, AttributeModifier> multimap = super.getDefaultAttributeModifiers(equipmentSlot);//HashMultimap.<String, AttributeModifier>create();

		if (equipmentSlot == EquipmentSlot.MAINHAND || equipmentSlot == EquipmentSlot.OFFHAND) {
			ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
			builder.putAll(multimap);
			builder.put(NostrumAttributes.magicPotency, new AttributeModifier(MAGEBLADE_POTENCY_UUID, "Potency modifier", 10, AttributeModifier.Operation.ADDITION));
			multimap = builder.build();
		}

        return multimap;
    }
	
	@Override
	public String getLoreKey() {
		return "nostrum_mage_blade";
	}

	@Override
	public String getLoreDisplayName() {
		return "Mage Blades";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("An enhanced blade that's sharp on it's own, but seems to be a good conductor of magical elements as well!");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("An enhanced blade that's sharp on it's own, but seems to be a good conductor of magical elements as well!", "Absorbs power when held by enchanted entities, and can be further enchanted at an elemental stone.");
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
			return repair.is(NostrumTags.Items.CrystalSmall);
		}
    }

	@Override
	public void apply(LivingEntity caster, Spell spell, SpellCastSummary summary, ItemStack stack) {
		// We provide -10% reagent cost, +20% potency
		summary.addReagentCost(-.1f);
		//summary.addEfficiency(.2f);
		ItemStacks.damageItem(stack, caster, caster.getItemInHand(InteractionHand.MAIN_HAND) == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, 1);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		//tooltip.add("Magic Potency Bonus: 20%");
		tooltip.add(new TextComponent("Reagent Cost Discount: 10%"));
	}
	
	protected void doEffect(LivingEntity entity, EMagicElement element) {
		NostrumParticles.GLOW_ORB.spawn(entity.level, new SpawnParams(
				3,
				entity.getX(), entity.getY() + entity.getBbHeight(), entity.getZ(), 1, 30, 5,
				new Vec3(0, -0.05, 0), null
				).color(0x80000000 | (0x00FFFFFF & element.getColor())));
		NostrumMagicaSounds.DAMAGE_FIRE.play(entity);
	}
	
	protected void spendCharge(ItemStack stack) {
		int charges = this.getCharges(stack) - 1;
		this.setCharges(stack, charges);
		if (charges <= 0) {
			this.setElement(stack, null);
			this.setElementalDamage(stack, 0);
		}
	}
	
	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		// Add magic damage, but only if weapon cooldown is recovered
		final EMagicElement elem = this.getElement(stack);
		if (elem != null) {
			// Want to make sure you're not spam clicking it, but hitEntity is called after regular hit stuff including cooldown
			//if (!(attacker instanceof PlayerEntity) || ((PlayerEntity)attacker).getCooledAttackStrength(0.5F) > .95)
			{
				final float damage = this.getElementalDamage(stack);
				target.setInvulnerable(false);
				target.invulnerableTime = 0;
				SpellDamage.DamageEntity(target, elem, damage, attacker);
				
				if (!target.level.isClientSide()) {
					doEffect(target, elem);
					spendCharge(stack);
					
					if (this.getCharges(stack) <= 0) {
						NostrumMagicaSounds.HOOKSHOT_TICK.play(attacker);
					}
				}
			}
		}
		
		return super.hurtEnemy(stack, target, attacker);
	}

	@Override
	public boolean canEnchant(ItemStack stack, EMagicElement element) {
		return this.getElement(stack) == null || this.getElement(stack) != element;
	}

	@Override
	public Result attemptEnchant(ItemStack stack, EMagicElement element) {
		final int charges = 20;
		
		MageBlade blade = (MageBlade) stack.getItem();
		blade.setElement(stack, element);
		blade.setCharges(stack, charges);
		blade.setElementalDamage(stack, 4f);
		return new Result(true, stack);
	}
	
	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean isSelected) {
		super.inventoryTick(stack, world, entity, slot, isSelected);
		
		if (isSelected && !world.isClientSide() && entity instanceof LivingEntity living) {
			if (this.getElement(stack) == null) {
				for (EMagicElement elem : EMagicElement.values()) {
					final MobEffectInstance effect = living.getEffect(ElementalEnchantEffect.GetForElement(elem));
					if (effect != null && effect.getDuration() > 0) {
						doAbsorbFromEntity(living, effect, elem, stack);
						return;
					}
				}
			}
		}
	}
	
	protected void doAbsorbFromEntity(LivingEntity holder, MobEffectInstance effect, EMagicElement element, ItemStack stack) {
		final int charges = 1;
		final float damage = 2 + effect.getAmplifier();
		
		MageBlade blade = (MageBlade) stack.getItem();
		blade.setElement(stack, element);
		blade.setCharges(stack, charges);
		blade.setElementalDamage(stack, damage);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static final float ModelElement(ItemStack stack, @Nullable Level worldIn, @Nullable LivingEntity entityIn, int entID) {
		final EMagicElement elem = ((MageBlade) stack.getItem()).getElement(stack);
		if (elem == null) {
			return 0;
		} else {
			return elem.ordinal() + 1;
		}
	}
	
}
