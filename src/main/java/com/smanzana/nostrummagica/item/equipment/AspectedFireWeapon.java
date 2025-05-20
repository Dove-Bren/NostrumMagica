package com.smanzana.nostrummagica.item.equipment;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.item.IRaytraceOverlay;
import com.smanzana.nostrummagica.item.ISpellEquipment;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.armor.ElementalArmor;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.shapes.SeekingBulletShape;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.util.ItemStacks;
import com.smanzana.nostrummagica.util.RayTrace;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AspectedFireWeapon extends ChargingSwordItem implements ILoreTagged, ISpellEquipment, IRaytraceOverlay {

	public static final String ID = "sword_fire";
	private static final int USE_DURATION = 20; // In ticks
	private static final float CAST_RANGE = SeekingBulletShape.MAX_DIST;
	
	public AspectedFireWeapon() {
		super(Tiers.GOLD, 5, -2.6F, NostrumItems.PropEquipment().durability(1240));
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		return super.getDefaultAttributeModifiers(equipmentSlot);
    }
	
	@Override
	public String getLoreKey() {
		return "sword_fire";
	}

	@Override
	public String getLoreDisplayName() {
		return "Flame Rod";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("This magic rod channels flame energy to inflict enemies with a deadly Soul Drain, damaging their health and lowering their invulnerability period after getting hit.");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("This magic rod channels flame energy to inflict enemies with a deadly soul drain, damaging their health and lowering their invulnerability period after getting hit.", "When combined with a True Lava set of armor, the wearer is granted the Soul Vampire effect, gaining health from enemies afflicted with Soul Drain!");
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
			return repair.is(NostrumTags.Items.InfusedGemFire);
		}
    }

	@Override
	public void apply(LivingEntity caster, Spell spell, SpellCastSummary summary, ItemStack stack) {
		// We provide -10% mana cost reduct
		summary.addCostRate(-.1f);
		ItemStacks.damageItem(stack, caster, caster.getItemInHand(InteractionHand.MAIN_HAND) == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, 1);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(new TextComponent("Mana Cost Discount: 10%"));
	}
	
	protected void doCastEffect(LivingEntity caster, LivingEntity target) {
		if (caster.level.isClientSide || caster.level != target.level) {
			return;
		}
		
		final Vec3 casterPos = caster.position().add(0, caster.getEyeHeight(), 0);
		final Vec3 targetPos = target.position().add(0, target.getBbHeight()/2, 0); 
		Vec3 diff = targetPos.subtract(casterPos);
		
		// Could go discrete increments, but just divide and stretch
		final int intervals = 10;
		for (int i = 0; i < intervals; i++) {
			Vec3 offset = diff.scale((float) i/ (float) intervals);
			final Vec3 pos = casterPos.add(offset);
			NostrumParticles.GLOW_ORB.spawn(caster.level, new SpawnParams(
					1,
					pos.x, pos.y, pos.z, 0, 30, 5,
					target.getId()
					).color(0xFFFF0000).dieWithTarget(true));
		}
	}
	
	protected @Nullable LivingEntity getCastTarget(LivingEntity caster) {
		// We have a target?
		HitResult result = RayTrace.raytraceApprox(caster.level, caster, caster.position().add(0, caster.getEyeHeight(), 0),
				caster.getXRot(), caster.getYRot(), CAST_RANGE, (ent) -> {
					return ent != null
							&& ent != caster
							&& ent instanceof LivingEntity
							&& !NostrumMagica.IsSameTeam((LivingEntity) ent, caster);
				}, .5);
		
		
		@Nullable Entity ent = RayTrace.entFromRaytrace(result);
		return ent == null ? null : (LivingEntity) RayTrace.entFromRaytrace(result);
	}
	
	protected boolean castOn(LivingEntity caster, LivingEntity target) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
		final boolean hasBonus = ElementalArmor.GetSetCount(caster, EMagicElement.FIRE, ElementalArmor.Type.MASTER) == 4;
		final boolean hasSkill = attr.hasSkill(NostrumSkills.Fire_Weapon);
		
		if (hasBonus) {
			caster.addEffect(new MobEffectInstance(NostrumEffects.soulVampire, 20 * 5, 0));
		}
		
		final List<LivingEntity> targets;
		if (hasSkill) {
			targets = target.getCommandSenderWorld().getEntities((Entity) null, target.getBoundingBox().inflate(5), (e) -> e instanceof LivingEntity && !NostrumMagica.IsSameTeam((LivingEntity) e, caster))
					.stream().map((e) -> (LivingEntity) e).collect(Collectors.toList());
					
		} else {
			targets = Lists.newArrayList(target);
		}
		
		for (LivingEntity targ : targets) {
			targ.addEffect(new MobEffectInstance(NostrumEffects.soulDrain, 20 * 5, 0));
			if (hasBonus) {
				targ.setSecondsOnFire(1);
				targ.setRemainingFireTicks(105);
			}
		}
		
		// Only do line effect to initial target even with AoE skill
		doCastEffect(caster, target);
		return true;
	}
	
	protected boolean castRod(Level worldIn, LivingEntity caster) {
		@Nullable LivingEntity target = getCastTarget(caster);
		if (target != null) {
			return castOn(caster, target);
		}
		
		return false;
	}
	
	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity target, InteractionHand hand) {
		if (!playerIn.level.isClientSide()) {
			if (castOn(playerIn, target)) {
				ItemStacks.damageItem(stack, playerIn, playerIn.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, 1);
			}
		}
		
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean shouldTrace(Level world, Player player, ItemStack stack) {
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
	protected void fireChargedWeapon(Level worldIn, LivingEntity playerIn, InteractionHand hand, ItemStack stack) {
		if (!worldIn.isClientSide() && castRod(worldIn, playerIn)) {
			ItemStacks.damageItem(stack, playerIn, hand, 1);
		}
	}

	@Override
	public double getTraceRange(Level world, Player player, ItemStack stack) {
		return CAST_RANGE;
	}

}
