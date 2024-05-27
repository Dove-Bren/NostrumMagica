package com.smanzana.nostrummagica.items.equipment;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.effects.NostrumEffects;
import com.smanzana.nostrummagica.items.IRaytraceOverlay;
import com.smanzana.nostrummagica.items.ISpellEquipment;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.armor.MagicArmor;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.shapes.SeekingBulletShape;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.utils.ItemStacks;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AspectedFireWeapon extends ChargingSwordItem implements ILoreTagged, ISpellEquipment, IRaytraceOverlay {

	public static final String ID = "sword_fire";
	private static final int USE_DURATION = 20; // In ticks
	private static final float CAST_RANGE = SeekingBulletShape.MAX_DIST;
	
	public AspectedFireWeapon() {
		super(ItemTier.GOLD, 5, -2.6F, NostrumItems.PropEquipment().maxDamage(1240));
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
		return super.getAttributeModifiers(equipmentSlot);
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
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		if (repair.isEmpty()) {
			return false;
		} else {
			return NostrumTags.Items.InfusedGemFire.contains(repair.getItem());
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
	
	protected void doCastEffect(LivingEntity caster, LivingEntity target) {
		if (caster.world.isRemote || caster.world != target.world) {
			return;
		}
		
		final Vector3d casterPos = caster.getPositionVec().add(0, caster.getEyeHeight(), 0);
		final Vector3d targetPos = target.getPositionVec().add(0, target.getHeight()/2, 0); 
		Vector3d diff = targetPos.subtract(casterPos);
		
		// Could go discrete increments, but just divide and stretch
		final int intervals = 10;
		for (int i = 0; i < intervals; i++) {
			Vector3d offset = diff.scale((float) i/ (float) intervals);
			final Vector3d pos = casterPos.add(offset);
			NostrumParticles.GLOW_ORB.spawn(caster.world, new SpawnParams(
					1,
					pos.x, pos.y, pos.z, 0, 30, 5,
					target.getEntityId()
					).color(0xFFFF0000).dieOnTarget(true));
		}
	}
	
	protected @Nullable LivingEntity getCastTarget(LivingEntity caster) {
		// We have a target?
		RayTraceResult result = RayTrace.raytraceApprox(caster.world, caster, caster.getPositionVec().add(0, caster.getEyeHeight(), 0),
				caster.rotationPitch, caster.rotationYaw, CAST_RANGE, (ent) -> {
					return ent != null
							&& ent != caster
							&& ent instanceof LivingEntity
							&& !NostrumMagica.IsSameTeam((LivingEntity) ent, caster);
				}, .5);
		
		
		@Nullable Entity ent = RayTrace.entFromRaytrace(result);
		return ent == null ? null : (LivingEntity) RayTrace.entFromRaytrace(result);
	}
	
	protected boolean castOn(LivingEntity caster, LivingEntity target) {
		
		final boolean hasBonus = MagicArmor.GetSetCount(caster, EMagicElement.FIRE, MagicArmor.Type.TRUE) == 4;
		
		target.addPotionEffect(new EffectInstance(NostrumEffects.soulDrain, 20 * 5, 0));
		if (hasBonus) {
			caster.addPotionEffect(new EffectInstance(NostrumEffects.soulVampire, 20 * 5, 0));
			target.setFire(1);
			target.forceFireTicks(105);
		}
		
		doCastEffect(caster, target);
		return true;
	}
	
	protected boolean castRod(World worldIn, LivingEntity caster) {
		@Nullable LivingEntity target = getCastTarget(caster);
		if (target != null) {
			return castOn(caster, target);
		}
		
		return false;
	}
	
	@Override
	public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
		if (!playerIn.world.isRemote()) {
			if (castOn(playerIn, target)) {
				ItemStacks.damageItem(stack, playerIn, playerIn.getHeldItemMainhand() == stack ? Hand.MAIN_HAND : Hand.OFF_HAND, 1);
			}
		}
		
		return ActionResultType.SUCCESS;
	}

	@Override
	public boolean shouldTrace(World world, PlayerEntity player, ItemStack stack) {
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
	protected void fireChargedWeapon(World worldIn, LivingEntity playerIn, Hand hand, ItemStack stack) {
		if (!worldIn.isRemote() && castRod(worldIn, playerIn)) {
			ItemStacks.damageItem(stack, playerIn, hand, 1);
		}
	}

	@Override
	public double getTraceRange(World world, PlayerEntity player, ItemStack stack) {
		return CAST_RANGE;
	}

}
