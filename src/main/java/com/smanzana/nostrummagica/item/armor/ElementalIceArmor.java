package com.smanzana.nostrummagica.item.armor;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellDamage;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.NonNullList;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;

public class ElementalIceArmor extends ElementalArmor {

	public static final String ID_PREFIX = "armor_ice_";
	public static final String ID_HELM_NOVICE = ID_PREFIX + "helm_novice";
	public static final String ID_HELM_ADEPT = ID_PREFIX + "helm_adept";
	public static final String ID_HELM_MASTER = ID_PREFIX + "helm_master";
		
	public static final String ID_CHEST_NOVICE = ID_PREFIX + "chest_novice";
	public static final String ID_CHEST_ADEPT = ID_PREFIX + "chest_adept";
	public static final String ID_CHEST_MASTER = ID_PREFIX + "chest_master";
		
	public static final String ID_LEGS_NOVICE = ID_PREFIX + "legs_novice";
	public static final String ID_LEGS_ADEPT = ID_PREFIX + "legs_adept";
	public static final String ID_LEGS_MASTER = ID_PREFIX + "legs_master";
		
	public static final String ID_FEET_NOVICE = ID_PREFIX + "feet_novice";
	public static final String ID_FEET_ADEPT = ID_PREFIX + "feet_adept";
	public static final String ID_FEET_MASTER = ID_PREFIX + "feet_master";
		
	public ElementalIceArmor(EquipmentSlot slot, Type type, Item.Properties properties) {
		super(EMagicElement.ICE, slot, type, properties);
		if (slot == EquipmentSlot.CHEST) {
			MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::onEntityDamage);
		}
	}
	
	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		if (this.allowdedIn(group)) {
			items.add(new ItemStack(this));
			
			// Add an upgraded copy of true chestplates
			if (this.slot == EquipmentSlot.CHEST && this.getType() == Type.MASTER) {
				ItemStack stack = new ItemStack(this);
				ElementalArmor.SetHasWingUpgrade(stack, true);
				items.add(stack);
			}
		}
	}
	
	public void onEntityDamage(LivingAttackEvent event) {
		if (event.isCanceled()) {
			return;
		}
		
		if (event.getSource().getDirectEntity() instanceof Snowball
				&& event.getSource().getEntity() != null
				&& event.getSource().getEntity() instanceof LivingEntity) {
			// If shooter has full blizzard set...
			final LivingEntity thrower = (LivingEntity) event.getSource().getEntity();
			final int blizzardCount = ElementalArmor.GetSetCount(thrower, EMagicElement.ICE, Type.MASTER);
			if (blizzardCount == 4) {
				// Either 'freeze' enemy, or heal ally
				@Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(thrower);
				final LivingEntity entity = event.getEntityLiving();
				if (NostrumMagica.IsSameTeam(event.getEntityLiving(), thrower)) {
					int amt = 4;
					if (attr != null && attr.hasSkill(NostrumSkills.Ice_Master)) {
						amt *= 2;
					}
					
					entity.heal(amt);
					if (entity instanceof TameRedDragonEntity) {
						TameRedDragonEntity dragon = (TameRedDragonEntity) entity;
						if (dragon.isTamed() && dragon.getOwner() == thrower) {
							dragon.addBond(1f);
						}
					} else if (entity instanceof ArcaneWolfEntity) {
						ArcaneWolfEntity wolf = (ArcaneWolfEntity) entity;
						if (wolf.isTame() && wolf.getOwner() == thrower) {
							wolf.addBond(1f);
						}
					}
					
					if (attr != null && attr.hasSkill(NostrumSkills.Ice_Adept)) {
						if (NostrumMagica.rand.nextBoolean()) {
							entity.addEffect(new MobEffectInstance(NostrumEffects.magicShield, (int)((20 * 15) * 1), 0));
						}
					}
					event.getSource().getDirectEntity().remove();
					event.setCanceled(true);
					
					NostrumParticles.FILLED_ORB.spawn(entity.level, new SpawnParams(
							10, entity.getX(), entity.getY() + entity.getBbHeight()/2, entity.getZ(), 1,
							40, 10,
							entity.getId()
							).gravity(true).color(EMagicElement.ICE.getColor()));
				} else {
					SpellDamage.DamageEntity(entity, EMagicElement.ICE, 1f, thrower);
					entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 15, 3));
					
					NostrumParticles.FILLED_ORB.spawn(entity.level, new SpawnParams(
							10, entity.getX(), entity.getY() + entity.getBbHeight()/2, entity.getZ(), 0,
							40, 10,
							new Vec3(0, .1, 0), new Vec3(.1, .05, .1)
							).gravity(true).color(EMagicElement.ICE.getColor()));
				}
			}
		}
	}
	
}
