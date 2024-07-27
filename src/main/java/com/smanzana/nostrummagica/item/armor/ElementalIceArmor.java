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

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.vector.Vector3d;
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
		
	public ElementalIceArmor(EquipmentSlotType slot, Type type, Item.Properties properties) {
		super(EMagicElement.ICE, slot, type, properties);
		if (slot == EquipmentSlotType.CHEST) {
			MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::onEntityDamage);
		}
	}
	
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (this.isInGroup(group)) {
			items.add(new ItemStack(this));
			
			// Add an upgraded copy of true chestplates
			if (this.slot == EquipmentSlotType.CHEST && this.getType() == Type.MASTER) {
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
		
		if (event.getSource().getImmediateSource() instanceof SnowballEntity
				&& event.getSource().getTrueSource() != null
				&& event.getSource().getTrueSource() instanceof LivingEntity) {
			// If shooter has full blizzard set...
			final LivingEntity thrower = (LivingEntity) event.getSource().getTrueSource();
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
						if (wolf.isTamed() && wolf.getOwner() == thrower) {
							wolf.addBond(1f);
						}
					}
					
					if (attr != null && attr.hasSkill(NostrumSkills.Ice_Adept)) {
						if (NostrumMagica.rand.nextBoolean()) {
							entity.addPotionEffect(new EffectInstance(NostrumEffects.magicShield, (int)((20 * 15) * 1), 0));
						}
					}
					event.getSource().getImmediateSource().remove();
					event.setCanceled(true);
					
					NostrumParticles.FILLED_ORB.spawn(entity.world, new SpawnParams(
							10, entity.getPosX(), entity.getPosY() + entity.getHeight()/2, entity.getPosZ(), 1,
							40, 10,
							entity.getEntityId()
							).gravity(true).color(EMagicElement.ICE.getColor()));
				} else {
					SpellDamage.DamageEntity(entity, EMagicElement.ICE, 1f, thrower);
					entity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 20 * 15, 3));
					
					NostrumParticles.FILLED_ORB.spawn(entity.world, new SpawnParams(
							10, entity.getPosX(), entity.getPosY() + entity.getHeight()/2, entity.getPosZ(), 0,
							40, 10,
							new Vector3d(0, .1, 0), new Vector3d(.1, .05, .1)
							).gravity(true).color(EMagicElement.ICE.getColor()));
				}
			}
		}
	}
	
}
