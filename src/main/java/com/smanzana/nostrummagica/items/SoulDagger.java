package com.smanzana.nostrummagica.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.effects.ClientPredefinedEffect.PredefinedEffect;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.effects.NostrumEffects;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;
import com.smanzana.nostrummagica.entity.IStabbableEntity;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.utils.ItemStacks;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.SwordItem;
import net.minecraft.item.UseAction;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SoulDagger extends SwordItem implements ILoreTagged, ISpellArmor {

	public static final String ID = "soul_dagger";
	private static final int USE_DURATION = 30; // In ticks
	
	public SoulDagger() {
		super(ItemTier.IRON, 3, -2.4F, NostrumItems.PropEquipment().maxDamage(500));
		
		this.addPropertyOverride(new ResourceLocation("charge"), new IItemPropertyGetter() {
			@OnlyIn(Dist.CLIENT)
			public float call(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
				if (entityIn == null) {
					return 0.0F;
				} else {
					return !(entityIn.getActiveItemStack().getItem() instanceof SoulDagger) ? 0.0F : (float)(stack.getUseDuration() - entityIn.getItemInUseCount()) / USE_DURATION;
				}
			}
		});
		this.addPropertyOverride(new ResourceLocation("charging"), new IItemPropertyGetter() {
			@OnlyIn(Dist.CLIENT)
			public float call(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
				return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
			}
		});
	}
	
	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();

        if (equipmentSlot == EquipmentSlotType.MAINHAND)
        {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 3, AttributeModifier.Operation.ADDITION));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2D, AttributeModifier.Operation.ADDITION));
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
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return !repair.isEmpty() && NostrumItemTags.Items.CrystalSmall.contains(repair.getItem());
    }

	@Override
	public void apply(LivingEntity caster, SpellCastSummary summary, ItemStack stack) {
		// We provide -5% mana cost
		summary.addCostRate(-.05f);
//		stack.damageItem(1, caster);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
//		tooltip.add("Magic Potency Bonus: 20%");
		tooltip.add(new StringTextComponent("Mana Cost Reduction: 5%"));
		tooltip.add(new TranslationTextComponent("item.soul_dagger.desc"));
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		final ItemStack held = playerIn.getHeldItem(hand);
		if (playerIn.isSneaking()) {
			playerIn.setActiveHand(hand);
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, held);
		}
			
		return new ActionResult<ItemStack>(ActionResultType.PASS, held);
	}
	
	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}
	
	@Override
	public int getUseDuration(ItemStack stack) {
		return 270000;
	}
	
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
		
		// Only do something if enough time has passed
		final int duration = stack.getUseDuration() - timeLeft;
		if (worldIn.isRemote || duration < USE_DURATION) {
			return;
		}
		
		// Do forward attack
		//vfx
		{
			NostrumMagicaSounds.HEAVY_STRIKE.play(null, entityLiving.world, entityLiving.getPositionVector().add(0f, entityLiving.getEyeHeight(), 0).add(entityLiving.getLook(.5f)));
		}
		// actual effects
		{
			List<LivingEntity> targets = findStabTargets(worldIn, entityLiving, stack);
			for (LivingEntity target : targets) {
				// Just find and stab first
				if (target != null) {
					stabTarget(entityLiving, target, stack);
					ItemStacks.damageItem(stack, entityLiving, entityLiving.getHeldItemMainhand() == stack ? Hand.MAIN_HAND : Hand.OFF_HAND, 1);
					return;
				}
			}
		}
	}
	
	protected List<LivingEntity> findStabTargets(World worldIn, LivingEntity wielder, ItemStack dagger) {
		float extent = 3f;
		RayTraceResult mop = RayTrace.raytrace(wielder.world, wielder, wielder.getEyePosition(.5f), wielder.getLook(.5f), extent, new RayTrace.OtherLiving(wielder));
		if (mop == null || mop.getType() != RayTraceResult.Type.ENTITY) {
			return new ArrayList<>();
		} else {
			EntityRayTraceResult entResult = (EntityRayTraceResult) mop;
			LivingEntity living = NostrumMagica.resolveLivingEntity(entResult.getEntity());
			if (living == null) {
				return new ArrayList<>();
			} else {
				return Lists.newArrayList(living);
			}
		}
	}
	
	protected boolean doSpecialWolfStab(LivingEntity stabber, WolfEntity wolf, ItemStack dagger) {
		if (wolf.getActivePotionEffect(NostrumEffects.nostrumTransformation) != null
				&& stabber instanceof PlayerEntity
				&& wolf.isOwner(stabber)) {
			// Wolves get transformed into arcane wolves!
			wolf.playSound(SoundEvents.ENTITY_WOLF_HOWL, 1f, 1f);
			EntityArcaneWolf.TransformWolf(wolf, (PlayerEntity) stabber);
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
		
		if (target instanceof WolfEntity) {
			if (doSpecialWolfStab(attacker, (WolfEntity) target, dagger)) {
				return true;
			}
		}
		
		int durationTicks = (target instanceof PlayerEntity ? 20 : 60);
		
//		//TODO testing code; remove!
//		{
//			{
////				ClientEffect effect = new ClientEffectMirrored(Vec3d.ZERO,
////						new ClientEffectFormBasic(ClientEffectIcon.ARROW_SLASH, (-16f/24f), (-16f/24f), (-16f/24f)),
////						30, 5);
//				ClientEffect effect = new ClientEffectMirrored(Vec3d.ZERO,
//						new ClientEffectFormBasic(ClientEffectIcon.ARROW_SLASH, (-8f/24f), (8f/24f), (-12f/24f)),
//						durationTicks, 5);
//				
//					effect.modify(new ClientEffectModifierFollow(target));
//				
//				effect
//				.modify(new ClientEffectModifierColor(0xFF000000, 0xFF800000))
//				//.modify(new ClientEffectModifierTranslate(0, 0, 0))
//				.modify(new ClientEffectModifierMove(new Vec3d(2, 2, 0), new Vec3d(0, 0, 0), 0f, .1f))
//				.modify(new ClientEffectModifierGrow(2f, 0f, 2f, 1f, .05f))
//				.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .75f))
//				;
////				.modify(new ClientEffectModifierColor(element.getColor(), element.getColor()))
////				.modify(new ClientEffectModifierRotate(0f, .4f, 0f))
////				.modify(new ClientEffectModifierTranslate(0, 0, -1))
////				.modify(new ClientEffectModifierMove(new Vec3d(0, 1.5, 0), new Vec3d(0, .5, .7), .5f, 1f))
////				.modify(new ClientEffectModifierGrow(.1f, .3f, .2f, .8f, .5f))
////				;
//				//return effect;
//				
//				ClientEffectRenderer.instance().addEffect(effect);
//			}
//		}
		NostrumMagica.instance.proxy.playPredefinedEffect(PredefinedEffect.SOUL_DAGGER_STAB, durationTicks, target.world, target);
		
		float damage = 6.0f + EnchantmentHelper.getModifierForCreature(dagger, target.getCreatureAttribute());
		
		final boolean hit;
		if (attacker instanceof PlayerEntity) {
			hit = target.attackEntityFrom(DamageSource.causePlayerDamage((PlayerEntity)attacker), damage);
		} else {
			hit = target.attackEntityFrom(DamageSource.causeMobDamage(attacker), damage);
		}
		
		if (hit) {
			target.hurtResistantTime = 0;
			target.setInvulnerable(false);
			
			target.addPotionEffect(new EffectInstance(Effects.SLOWNESS, durationTicks, 6));
			// Effects:
			{
				NostrumParticles.GLOW_ORB.spawn(attacker.world, new SpawnParams(
						30, target.posX, target.posY + target.getHeight(), target.posZ, .5, 60, 20,
						new Vec3d(0, .05, 0), new Vec3d(.1, 0, .1)
						).color(.6f, .6f, 0f, 0f).dieOnTarget(true).gravity(.1f));
			}
			
			// Mana:
			{
				final int manaDrawn;
				INostrumMagic attr = NostrumMagica.getMagicWrapper(target);
				INostrumMagic attrSelf = NostrumMagica.getMagicWrapper(attacker);
				if (attrSelf == null || (attr == null && target instanceof PlayerEntity)) {
					manaDrawn = 0;
				} else if (attrSelf != null && attr == null && target instanceof MobEntity) {
					// Just fudge some mana to steal
					manaDrawn = NostrumMagica.rand.nextInt(50) + 50;
				} else {
					int manaCost = NostrumMagica.rand.nextInt(50) + 50;
					manaCost = Math.min(manaCost, attr.getMana());
					manaDrawn = Math.min(manaCost, attrSelf.getMaxMana() - attrSelf.getMana());
				}
				
				if (manaDrawn > 0) {
					if (attr != null) {
						attr.addMana(-manaDrawn);
					}
					attrSelf.addMana(manaDrawn);
					
					NostrumParticles.FILLED_ORB.spawn(attacker.world, new SpawnParams(
							50, target.posX, target.posY + target.getHeight(), target.posZ, .5, 60, 0,
							attacker.getEntityId()
							).color(1f, .4f, .8f, 1f).dieOnTarget(true));
				}
			}
		}
		
		return true;
	}

}
