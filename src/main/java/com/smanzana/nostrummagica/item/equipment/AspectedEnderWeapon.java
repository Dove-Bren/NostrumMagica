package com.smanzana.nostrummagica.item.equipment;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.NostrumMagica.NostrumTeleportEvent;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.EnderRodBallEntity;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.item.ISpellEquipment;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.armor.ElementalArmor;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellDamage;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.util.Entities;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AspectedEnderWeapon extends ChargingSwordItem implements ILoreTagged, ISpellEquipment {

	public static final String ID = "sword_ender";
	private static final int USE_DURATION = 20; // In ticks
	private static final float MAX_BALL_DIST = 30;
	
	public AspectedEnderWeapon() {
		super(Tiers.GOLD, 5, -2.6F, NostrumItems.PropEquipment().durability(1240));
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		return super.getDefaultAttributeModifiers(equipmentSlot);
    }
	
	@Override
	public String getLoreKey() {
		return "sword_ender";
	}

	@Override
	public String getLoreDisplayName() {
		return "Ender Rod";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Charging up and using this rod casts a ball of ender energy at the spot the caster was looking at. Hitting an entity with the rod while a charge point is nearby will teleport the entity to it!");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Charging up and using this rod casts a ball of ender energy at the spot the caster was looking at. Hitting an entity with the rod while a charge point is nearby will teleport the entity to it!", "With the True Ender set, the wielder can instead teleport themselves to the target spot. Additionally, they inflict a heavy movement penalty to mobs when they teleport them with the rod, and deal damage any time a charge point is used.");
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
			return NostrumTags.Items.InfusedGemEnder.contains(repair.getItem());
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
	
	protected void doCastEffect(LivingEntity target, Vec3 startPos, Vec3 endPos) {
		if (target.level.isClientSide) {
			return;
		}
		
		target.level.playSound(null, endPos.x(), endPos.y(), endPos.z(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1f, 1f);
		target.level.playSound(null, startPos.x(), startPos.y(), startPos.z(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1f, 1f);
		
		for(int i = 0; i < 32; ++i) {
			target.level.addParticle(ParticleTypes.PORTAL, endPos.x(), endPos.y() + NostrumMagica.rand.nextDouble() * 2.0D, endPos.z(), NostrumMagica.rand.nextGaussian(), 0.0D, NostrumMagica.rand.nextGaussian());
		}
		
		Vec3 diff = endPos.subtract(startPos);
		
		// Could go discrete increments, but just divide and stretch
		final int intervals = 10;
		for (int i = 0; i < intervals; i++) {
			Vec3 offset = diff.scale((float) i/ (float) intervals);
			final Vec3 pos = startPos.add(offset);
			NostrumParticles.GLOW_ORB.spawn(target.level, new SpawnParams(
					1,
					pos.x, pos.y, pos.z, 0, 30, 5,
					target.getId()
					).color(EMagicElement.ENDER.getColor()).dieOnTarget(true));
		}
	}
	
	protected Vec3 getCastPosition(LivingEntity caster) {
		HitResult result = RayTrace.raytrace(caster.level, caster, caster.position().add(0, caster.getEyeHeight(), 0),
				caster.getXRot(), caster.getYRot(), MAX_BALL_DIST, (ent) -> {
					return false; // Don't want entities
				});
		
		if (result.getType() == HitResult.Type.MISS) {
			return caster.position().add(0, caster.getEyeHeight(), 0).add(
					caster.getLookAngle().scale(MAX_BALL_DIST)
					);
		} else {
			BlockHitResult blockRes = (BlockHitResult) result;
			BlockPos pos = RayTrace.blockPosFromResult(result);
			if (!caster.level.isEmptyBlock(pos)) {
				pos = pos.relative(blockRes.getDirection());
			}
			return new Vec3(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
		}
	}
	
	protected @Nullable EnderRodBallEntity findNearestBall(LivingEntity caster) {
		ServerLevel world = (ServerLevel) caster.level;
		List<? extends EnderRodBallEntity> balls = world.getEntities(NostrumEntityTypes.enderRodBall, (e) -> {
			return e != null
					&& e instanceof EnderRodBallEntity
					&& caster.equals(((EnderRodBallEntity) e).getOwner());
		});
		
		EnderRodBallEntity closest = null;
		double minDistSq = -1;
		for (Entity ball : balls) {
			if (closest == null || ball.distanceToSqr(caster) < minDistSq) {
				closest = (EnderRodBallEntity) ball;
				minDistSq = ball.distanceToSqr(caster);
			}
		}
		return closest;
	}
	
	protected void doConsumeDamage(LivingEntity caster, LivingEntity target) {
		SpellDamage.DamageEntity(target, EMagicElement.ENDER, 2, caster);
	}
	
	protected void consumeBall(LivingEntity caster, EnderRodBallEntity ball) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
		final boolean hasBonus = ElementalArmor.GetSetCount(caster, EMagicElement.ENDER, ElementalArmor.Type.MASTER) == 4;
		final boolean hasShield = attr != null && attr.hasSkill(NostrumSkills.Ender_Weapon);
		if (hasBonus) {
			int hurtCount = 0;
			for (LivingEntity ent : Entities.GetEntities((ServerLevel) caster.level, (e) -> {
				return e != null
						&& !NostrumMagica.IsSameTeam(e, caster)
						&& e.distanceTo(ball) <= 5;
			})) {
				doConsumeDamage(caster, ent);
				hurtCount++;
			}
			
			NostrumParticles.GLOW_ORB.spawn(ball.level, new SpawnParams(
					50,
					ball.getX(), ball.getY() + ball.getBbHeight() / 2, ball.getZ(), .25, 50, 20,
					new Vec3(0, .1, 0), new Vec3(.25, .05, .25)
					).color(EMagicElement.ENDER.getColor()).gravity(true));
			
			if (hasShield && hurtCount > 0) {
				// Apply effects if not present
				final MobEffectInstance activePhysical = caster.getEffect(NostrumEffects.physicalShield);
				final MobEffectInstance activeMagic = caster.getEffect(NostrumEffects.magicShield);
				if (activePhysical == null || activePhysical.getDuration() < 15 * 20) {
					caster.addEffect(new MobEffectInstance(NostrumEffects.physicalShield, 15 * 20, 0));
				}
				if (activeMagic == null || activeMagic.getDuration() < 15 * 20) {
					caster.addEffect(new MobEffectInstance(NostrumEffects.magicShield, 15 * 20, 0));
				}
				
				// Actually set amount
				final float shieldAmt = 2f * hurtCount;
				NostrumMagica.magicEffectProxy.applyPhysicalShield(caster, shieldAmt);
				NostrumMagica.magicEffectProxy.applyMagicalShield(caster, shieldAmt);
			}
		}
		
		ball.discard();
	}
	
	protected void teleportEntity(LivingEntity caster, LivingEntity entity, Vec3 pos) {
		final Vec3 startPos = entity.position();
		NostrumTeleportEvent event = NostrumMagica.fireTeleportAttemptEvent(entity, pos.x(), pos.y(), pos.z(), caster);
		if (!event.isCanceled()) {
			entity.teleportTo(event.getTargetX(), event.getTargetY(), event.getTargetZ());
			NostrumMagica.fireTeleprotedOtherEvent(event.getEntity(), caster, event.getPrev(), event.getTarget());
		}
		doCastEffect(entity, startPos, pos);
	}
	
	protected boolean dislocateEntity(LivingEntity caster, LivingEntity target) {
		@Nullable EnderRodBallEntity ball = findNearestBall(caster);
		if (ball != null) {
			final boolean hasBonus = ElementalArmor.GetSetCount(caster, EMagicElement.ENDER, ElementalArmor.Type.MASTER) == 4;
			
			teleportEntity(caster, target, ball.position());
			consumeBall(caster, ball);
			
			if (hasBonus && !NostrumMagica.IsSameTeam(target, caster)) {
				target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 3, 3));
			}
			return true;
		} else {
			return false;
		}
	}
	
	protected boolean dislocateCaster(LivingEntity caster) {
		@Nullable EnderRodBallEntity ball = findNearestBall(caster);
		if (ball != null) {
			teleportEntity(caster, caster, ball.position());
			consumeBall(caster, ball);
			return true;
		} else {
			return false;
		}
	}
	
	protected boolean castRod(Level worldIn, LivingEntity caster) {
		// Find existing ball and consume it
		@Nullable EnderRodBallEntity ball = this.findNearestBall(caster);
		if (ball != null) {
			this.consumeBall(caster, ball);
		}
		
		// Create a new ball where caster is looking
		Vec3 pos = this.getCastPosition(caster);
		ball = new EnderRodBallEntity(NostrumEntityTypes.enderRodBall, worldIn, caster);
		ball.setPos(pos.x, pos.y, pos.z);
		worldIn.addFreshEntity(ball);
		
		return true;
	}
	
	protected boolean castOnEntity(LivingEntity caster, LivingEntity target) {
		return dislocateEntity(caster, target);
	}
	
	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity target, InteractionHand hand) {
		if (!playerIn.level.isClientSide()) {
			if (castOnEntity(playerIn, target)) {
				ItemStacks.damageItem(stack, playerIn, playerIn.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, 1);
			}
		}
		
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		boolean ret = super.hurtEnemy(stack, target, attacker);
		if (ret) {
			castOnEntity(attacker, target);
		}
		return ret;
	}
	
	public static boolean AttemptCasterTeleport(LivingEntity caster, ItemStack stack) {
		if (!stack.isEmpty() && stack.getItem() instanceof AspectedEnderWeapon) {
			return ((AspectedEnderWeapon) stack.getItem()).dislocateCaster(caster);
		}
		return false;
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
			ItemStacks.damageItem(stack, playerIn	, hand, 1);
		}
	}
}
