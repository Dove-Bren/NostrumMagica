package com.smanzana.nostrummagica.item.equipment;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.NostrumMagica.NostrumTeleportEvent;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.entity.EntityEnderRodBall;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.item.ISpellEquipment;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.armor.MagicArmor;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.MagicDamageSource;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.util.Entities;
import com.smanzana.nostrummagica.util.ItemStacks;
import com.smanzana.nostrummagica.util.RayTrace;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AspectedEnderWeapon extends ChargingSwordItem implements ILoreTagged, ISpellEquipment {

	public static final String ID = "sword_ender";
	private static final int USE_DURATION = 20; // In ticks
	private static final float MAX_BALL_DIST = 30;
	
	public AspectedEnderWeapon() {
		super(ItemTier.GOLD, 5, -2.6F, NostrumItems.PropEquipment().maxDamage(1240));
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
		return super.getAttributeModifiers(equipmentSlot);
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
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		if (repair.isEmpty()) {
			return false;
		} else {
			return NostrumTags.Items.InfusedGemEnder.contains(repair.getItem());
		}
    }

	@Override
	public void apply(LivingEntity caster, SpellCastSummary summary, ItemStack stack) {
		// We provide -10% mana cost reduct
		summary.addCostRate(-.1f);
		ItemStacks.damageItem(stack, caster, caster.getHeldItem(Hand.MAIN_HAND) == stack ? Hand.MAIN_HAND : Hand.OFF_HAND, 1);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(new StringTextComponent("Mana Cost Discount: 10%"));
	}
	
	protected void doCastEffect(LivingEntity target, Vector3d startPos, Vector3d endPos) {
		if (target.world.isRemote) {
			return;
		}
		
		target.world.playSound(null, endPos.getX(), endPos.getY(), endPos.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.NEUTRAL, 1f, 1f);
		target.world.playSound(null, startPos.getX(), startPos.getY(), startPos.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.NEUTRAL, 1f, 1f);
		
		for(int i = 0; i < 32; ++i) {
			target.world.addParticle(ParticleTypes.PORTAL, endPos.getX(), endPos.getY() + NostrumMagica.rand.nextDouble() * 2.0D, endPos.getZ(), NostrumMagica.rand.nextGaussian(), 0.0D, NostrumMagica.rand.nextGaussian());
		}
		
		Vector3d diff = endPos.subtract(startPos);
		
		// Could go discrete increments, but just divide and stretch
		final int intervals = 10;
		for (int i = 0; i < intervals; i++) {
			Vector3d offset = diff.scale((float) i/ (float) intervals);
			final Vector3d pos = startPos.add(offset);
			NostrumParticles.GLOW_ORB.spawn(target.world, new SpawnParams(
					1,
					pos.x, pos.y, pos.z, 0, 30, 5,
					target.getEntityId()
					).color(EMagicElement.ENDER.getColor()).dieOnTarget(true));
		}
	}
	
	protected Vector3d getCastPosition(LivingEntity caster) {
		RayTraceResult result = RayTrace.raytrace(caster.world, caster, caster.getPositionVec().add(0, caster.getEyeHeight(), 0),
				caster.rotationPitch, caster.rotationYaw, MAX_BALL_DIST, (ent) -> {
					return false; // Don't want entities
				});
		
		if (result.getType() == RayTraceResult.Type.MISS) {
			return caster.getPositionVec().add(0, caster.getEyeHeight(), 0).add(
					caster.getLookVec().scale(MAX_BALL_DIST)
					);
		} else {
			BlockRayTraceResult blockRes = (BlockRayTraceResult) result;
			BlockPos pos = RayTrace.blockPosFromResult(result);
			if (!caster.world.isAirBlock(pos)) {
				pos = pos.offset(blockRes.getFace());
			}
			return new Vector3d(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
		}
	}
	
	protected @Nullable EntityEnderRodBall findNearestBall(LivingEntity caster) {
		ServerWorld world = (ServerWorld) caster.world;
		List<Entity> balls = world.getEntities(NostrumEntityTypes.enderRodBall, (e) -> {
			return e != null
					&& e instanceof EntityEnderRodBall
					&& caster.equals(((EntityEnderRodBall) e).getOwner());
		});
		
		EntityEnderRodBall closest = null;
		double minDistSq = -1;
		for (Entity ball : balls) {
			if (closest == null || ball.getDistanceSq(caster) < minDistSq) {
				closest = (EntityEnderRodBall) ball;
				minDistSq = ball.getDistanceSq(caster);
			}
		}
		return closest;
	}
	
	protected void doConsumeDamage(LivingEntity caster, LivingEntity target) {
		target.attackEntityFrom(new MagicDamageSource(caster, EMagicElement.ENDER), 2);
	}
	
	protected void consumeBall(LivingEntity caster, EntityEnderRodBall ball) {
		final boolean hasBonus = MagicArmor.GetSetCount(caster, EMagicElement.ENDER, MagicArmor.Type.TRUE) == 4;
		if (hasBonus) {
			for (LivingEntity ent : Entities.GetEntities((ServerWorld) caster.world, (e) -> {
				return e != null
						&& !NostrumMagica.IsSameTeam(e, caster)
						&& e.getDistance(ball) <= 5;
			})) {
				doConsumeDamage(caster, ent);
			}
			
			NostrumParticles.GLOW_ORB.spawn(ball.world, new SpawnParams(
					50,
					ball.getPosX(), ball.getPosY() + ball.getHeight() / 2, ball.getPosZ(), .25, 50, 20,
					new Vector3d(0, .1, 0), new Vector3d(.25, .05, .25)
					).color(EMagicElement.ENDER.getColor()).gravity(true));
		}
		
		ball.remove();
	}
	
	protected void teleportEntity(LivingEntity caster, LivingEntity entity, Vector3d pos) {
		final Vector3d startPos = entity.getPositionVec();
		NostrumTeleportEvent event = NostrumMagica.fireTeleportAttemptEvent(entity, pos.getX(), pos.getY(), pos.getZ(), caster);
		if (!event.isCanceled()) {
			entity.setPositionAndUpdate(event.getTargetX(), event.getTargetY(), event.getTargetZ());
			NostrumMagica.fireTeleprotedOtherEvent(event.getEntity(), caster, event.getPrev(), event.getTarget());
		}
		doCastEffect(entity, startPos, pos);
	}
	
	protected boolean dislocateEntity(LivingEntity caster, LivingEntity target) {
		@Nullable EntityEnderRodBall ball = findNearestBall(caster);
		if (ball != null) {
			final boolean hasBonus = MagicArmor.GetSetCount(caster, EMagicElement.ENDER, MagicArmor.Type.TRUE) == 4;
			
			teleportEntity(caster, target, ball.getPositionVec());
			consumeBall(caster, ball);
			
			if (hasBonus && !NostrumMagica.IsSameTeam(target, caster)) {
				target.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 20 * 3, 3));
			}
			return true;
		} else {
			return false;
		}
	}
	
	protected boolean dislocateCaster(LivingEntity caster) {
		@Nullable EntityEnderRodBall ball = findNearestBall(caster);
		if (ball != null) {
			teleportEntity(caster, caster, ball.getPositionVec());
			consumeBall(caster, ball);
			return true;
		} else {
			return false;
		}
	}
	
	protected boolean castRod(World worldIn, LivingEntity caster) {
		// Find existing ball and consume it
		@Nullable EntityEnderRodBall ball = this.findNearestBall(caster);
		if (ball != null) {
			this.consumeBall(caster, ball);
		}
		
		// Create a new ball where caster is looking
		Vector3d pos = this.getCastPosition(caster);
		ball = new EntityEnderRodBall(NostrumEntityTypes.enderRodBall, worldIn, caster);
		ball.setPosition(pos.x, pos.y, pos.z);
		worldIn.addEntity(ball);
		
		return true;
	}
	
	protected boolean castOnEntity(LivingEntity caster, LivingEntity target) {
		return dislocateEntity(caster, target);
	}
	
	@Override
	public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
		if (!playerIn.world.isRemote()) {
			if (castOnEntity(playerIn, target)) {
				ItemStacks.damageItem(stack, playerIn, playerIn.getHeldItemMainhand() == stack ? Hand.MAIN_HAND : Hand.OFF_HAND, 1);
			}
		}
		
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		boolean ret = super.hitEntity(stack, target, attacker);
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
	protected void fireChargedWeapon(World worldIn, LivingEntity playerIn, Hand hand, ItemStack stack) {
		if (!worldIn.isRemote() && castRod(worldIn, playerIn)) {
			ItemStacks.damageItem(stack, playerIn	, hand, 1);
		}
	}
}
