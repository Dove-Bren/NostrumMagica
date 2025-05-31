package com.smanzana.nostrummagica.item.equipment;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMana;
import com.smanzana.nostrummagica.client.effects.ClientPredefinedEffect.PredefinedEffect;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;
import com.smanzana.nostrummagica.entity.IStabbableEntity;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.api.IRaytraceOverlay;
import com.smanzana.nostrummagica.item.api.ISpellEquipment;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.util.ItemStacks;
import com.smanzana.nostrummagica.util.RayTrace;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SoulDagger extends ChargingSwordItem implements ILoreTagged, ISpellEquipment, IRaytraceOverlay {

	public static final String ID = "soul_dagger";
	private static final int USE_DURATION = 30; // In ticks
	private static final float STAB_RANGE = 3f;
	
	public SoulDagger() {
		super(Tiers.IRON, 3, -2.4F, NostrumItems.PropEquipment().durability(500));
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.<Attribute, AttributeModifier>create();

        if (equipmentSlot == EquipmentSlot.MAINHAND)
        {
        	ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
			builder.putAll(multimap);
            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 3, AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2D, AttributeModifier.Operation.ADDITION));
            multimap = builder.build();
        }

        return multimap;
    }
	
	@Override
	public String getLoreKey() {
		return "nostrum_soul_dagger";
	}

	@Override
	public String getLoreDisplayName() {
		return "Soul Daggers";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("This strange dagger is able to deal moderate damage to the flesh and tremendous damage to the soul.", "The dagger can be slashed like a normal weapon. Additionally, you can sneak and 'use' the item (right-click) to prepare a stab attack.", "The stab attack, once charged up, deals good damage, stuns the target for a few moments, and drains some of their mana.", "Something tells you there's more you can do with the dagger, but you're not quite sure yet...");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("This strange dagger is able to deal moderate damage to the flesh and tremendous damage to the soul.", "The dagger can be slashed like a normal weapon. Additionally, you can sneak and 'use' the item (right-click) to prepare a stab attack.", "The stab attack, once charged up, deals good damage, stuns the target for a few moments, and drains some of their mana.", "You've gathered that the soul dagger can be used to cleanly peel away the flesh from around a beings soul as long as they are at total peace with you. This means pets that have fully bonded with you can grant you a piece of their soul.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return !repair.isEmpty() && repair.is(NostrumTags.Items.CrystalSmall);
    }

	@Override
	public void apply(LivingEntity caster, Spell spell, SpellCastSummary summary, ItemStack stack) {
		// We provide -5% mana cost
		summary.addCostRate(-.05f);
//		stack.damageItem(1, caster);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
//		tooltip.add("Magic Potency Bonus: 20%");
		tooltip.add(new TextComponent("Mana Cost Reduction: 5%"));
		tooltip.add(new TranslatableComponent("item.nostrummagica.soul_dagger.desc"));
	}
	
	@Override
	protected void fireChargedWeapon(Level worldIn, LivingEntity entityLiving, InteractionHand hand, ItemStack stack) {
		if (worldIn.isClientSide()) {
			return;
		}
		
		// Do forward attack
		//vfx
		{
			NostrumMagicaSounds.HEAVY_STRIKE.play(null, entityLiving.level, entityLiving.position().add(0f, entityLiving.getEyeHeight(), 0).add(entityLiving.getViewVector(.5f)));
		}
		// actual effects
		{
			List<LivingEntity> targets = findStabTargets(worldIn, entityLiving, stack);
			for (LivingEntity target : targets) {
				// Just find and stab first
				if (target != null) {
					stabTarget(entityLiving, target, stack);
					ItemStacks.damageItem(stack, entityLiving, hand, 1);
					return;
				}
			}
		}
	}
	
	protected List<LivingEntity> findStabTargets(Level worldIn, LivingEntity wielder, ItemStack dagger) {
		HitResult mop = RayTrace.raytrace(wielder.level, wielder, wielder.getEyePosition(.5f), wielder.getViewVector(.5f), STAB_RANGE, new RayTrace.OtherLiving(wielder));
		if (mop == null || mop.getType() != HitResult.Type.ENTITY) {
			return new ArrayList<>();
		} else {
			EntityHitResult entResult = (EntityHitResult) mop;
			LivingEntity living = NostrumMagica.resolveLivingEntity(entResult.getEntity());
			if (living == null) {
				return new ArrayList<>();
			} else {
				return Lists.newArrayList(living);
			}
		}
	}
	
	protected boolean doSpecialWolfStab(LivingEntity stabber, Wolf wolf, ItemStack dagger) {
		if (wolf.getEffect(NostrumEffects.nostrumTransformation) != null
				&& stabber instanceof Player
				&& wolf.isOwnedBy(stabber)) {
			// Wolves get transformed into arcane wolves!
			wolf.playSound(SoundEvents.WOLF_HOWL, 1f, 1f);
			ArcaneWolfEntity.TransformWolf(wolf, (Player) stabber);
			return true;
		}
		
		return false;
	}
	
	protected boolean stabTarget(LivingEntity attacker, LivingEntity target, ItemStack dagger) {
		
		// First, check if its a special-stabbable entity
		if (target instanceof IStabbableEntity) {
			if (((IStabbableEntity) target).onSoulStab(attacker, dagger)) {
				return true;
			}
		}
		
		if (target instanceof Wolf) {
			if (doSpecialWolfStab(attacker, (Wolf) target, dagger)) {
				return true;
			}
		}
		
		int durationTicks = (target instanceof Player ? 20 : 60);
		
		NostrumMagica.Proxy.playPredefinedEffect(PredefinedEffect.SOUL_DAGGER_STAB, durationTicks, target.level, target);
		
		float damage = 6.0f + EnchantmentHelper.getDamageBonus(dagger, target.getMobType());
		
		final boolean hit;
		if (attacker instanceof Player) {
			hit = target.hurt(DamageSource.playerAttack((Player)attacker), damage);
		} else {
			hit = target.hurt(DamageSource.mobAttack(attacker), damage);
		}
		
		if (hit) {
			target.invulnerableTime = 0;
			target.setInvulnerable(false);
			
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, durationTicks, 6));
			// Effects:
			{
				NostrumParticles.GLOW_ORB.spawn(attacker.level, new SpawnParams(
						30, target.getX(), target.getY() + target.getBbHeight(), target.getZ(), .5, 60, 20,
						new Vec3(0, .05, 0), new Vec3(.1, 0, .1)
						).color(.6f, .6f, 0f, 0f).gravity(.1f));
			}
			
			// Mana:
			{
				final int manaDrawn;
				INostrumMana attr = NostrumMagica.getManaWrapper(target);
				INostrumMana attrSelf = NostrumMagica.getManaWrapper(attacker);
				if (attrSelf == null || (attr == null && target instanceof Player)) {
					manaDrawn = 0;
				} else if (attrSelf != null && attr == null && target instanceof Mob) {
					// Just fudge some mana to steal
					manaDrawn = NostrumMagica.rand.nextInt(50) + 50;
				} else {
					int manaCost = NostrumMagica.rand.nextInt(50) + 50;
					manaCost = Math.min(manaCost, attr.getMana());
					attr.addMana(-manaCost);
					manaDrawn = Math.min(manaCost, attrSelf.getMaxMana() - attrSelf.getMana());
				}
				
				if (manaDrawn > 0) {
					if (attr != null) {
						attr.addMana(-manaDrawn);
					}
					attrSelf.addMana(manaDrawn);
					
					NostrumParticles.FILLED_ORB.spawn(attacker.level, new SpawnParams(
							50, target.getX(), target.getY() + target.getBbHeight(), target.getZ(), .5, 60, 0,
							new TargetLocation(attacker, true)
							).setTargetBehavior(new ParticleTargetBehavior().orbitMode(true).dieWithTarget()).color(1f, .4f, .8f, 1f));
				}
			}
			
			MobEffectInstance effect = attacker.getEffect(NostrumEffects.soulVampire);
			if (effect != null && effect.getDuration() > 0) {
				attacker.heal(damage);
			}
		}
		
		return true;
	}
	
	@Override
	protected boolean shouldAutoFire(ItemStack stack) {
		return false;
	}

	@Override
	protected int getTotalChargeTime(ItemStack stack) {
		return USE_DURATION;
	}
	
	@Override
	public boolean shouldTrace(Level world, Player player, ItemStack stack) {
		return true;
	}

	@Override
	public double getTraceRange(Level world, Player player, ItemStack stack) {
		return STAB_RANGE;
	}
}
