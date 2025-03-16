package com.smanzana.nostrummagica.item.equipment;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.item.ISpellEquipment;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.armor.ElementalArmor;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.util.ItemStacks;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class AspectedPhysicalWeapon extends SwordItem implements ILoreTagged, ISpellEquipment {

	public static final String ID = "sword_physical";
	
	public AspectedPhysicalWeapon() {
		super(Tiers.DIAMOND, 6, -3.0F, NostrumItems.PropEquipment().durability(1240).addToolType(ToolType.AXE, 3));
	}
	
	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		if (getToolTypes(stack).stream().anyMatch(e -> state.isToolEffective(e))) return 8.0f; // diamond level
		return 1.0f;
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		return super.getDefaultAttributeModifiers(equipmentSlot);
    }
	
	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		if (enchantment.category == EnchantmentCategory.DIGGER) {
			return true;
		}
		
		return super.canApplyAtEnchantingTable(stack, enchantment);
	}
	
	@Override
	public String getLoreKey() {
		return "sword_physical";
	}

	@Override
	public String getLoreDisplayName() {
		return "Deep Metal Axe";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("This huge, magical metal axe boasts heavy damage in exchange for a slow swing rate.", "Additionally, it can be used to block. Using the energy stored within, it is able to apply the Rend status to an enemy if hit shortly after blocking a hit!");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("This huge, magical metal axe boasts heavy damage in exchange for a slow swing rate.", "Additionally, it can be used to block. Using the energy stored within, it is able to apply the Rend status to an enemy if hit shortly after blocking a hit!", "What's more, if the wielder is wearing a set of True Deep Metal armor, blocking grants the Steel Skin effect, reducing damage!");
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
			return NostrumTags.Items.InfusedGemVoid.contains(repair.getItem());
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
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		final ItemStack held = playerIn.getItemInHand(hand);
		playerIn.startUsingItem(hand);
		return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, held);
	}
	
	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BLOCK;
	}
	
	@Override
	public int getUseDuration(ItemStack stack) {
		return 270000;
	}
	
	@Override
	public boolean isShield(ItemStack stack, @Nullable LivingEntity entity) {
		return true;
	}
	
	@Override
	public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
		return true;
	}
	
	protected static void doBlockEffect(LivingEntity caster) {
		if (caster.level.isClientSide) {
			return;
		}
		
//		final Vector3d casterPos = caster.getPositionVec().add(0, caster.getEyeHeight(), 0);
//		final Vector3d targetPos = target.getPositionVec().add(0, target.getHeight()/2, 0); 
//		Vector3d diff = targetPos.subtract(casterPos);
//		
//		// Could go discrete increments, but just divide and stretch
//		final int intervals = 10;
//		for (int i = 0; i < intervals; i++) {
//			Vector3d offset = diff.scale((float) i/ (float) intervals);
//			final Vector3d pos = casterPos.add(offset);
//			NostrumParticles.GLOW_ORB.spawn(caster.world, new SpawnParams(
//					1,
//					pos.x, pos.y, pos.z, 0, 30, 5,
//					target.getEntityId()
//					).color(0xFFFF0000).dieOnTarget(true));
//		}
	}
	
	protected static void doBlock(LivingEntity blocker) {
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(blocker);
		final boolean hasBonus = ElementalArmor.GetSetCount(blocker, EMagicElement.PHYSICAL, ElementalArmor.Type.MASTER) == 4;
		final boolean hasSkill = attr != null && attr.hasSkill(NostrumSkills.Physical_Weapon);
		blocker.addEffect(new MobEffectInstance(NostrumEffects.rendStrike, 1 * 20, 0));
		
		if (hasBonus) {
			blocker.addEffect(new MobEffectInstance(NostrumEffects.steelSkin, 3 * 20, 0));
			if (hasSkill) {
				blocker.addEffect(new MobEffectInstance(NostrumEffects.magicShield, 10 * 20, 0));
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEntityHit(LivingAttackEvent event) {
		if (event.getEntityLiving().level.isClientSide()) {
			return;
		}
		
		// I really wish there was an item.onBlock() or even a LivingBlockEvent but there is neither.
		
		final LivingEntity ent = event.getEntityLiving();
		final DamageSource source = event.getSource();
		if (ent.isBlocking() && ent.getUseItem().getItem() instanceof AspectedPhysicalWeapon) {
			// This is based on LivingEntity#attackEntityFrom
			if (event.getAmount() > 0.0F
					// && ent.canBlockDamageSource(source)) { not visible
					&& !source.isBypassArmor()
					&& source.getEntity() != null
					) { 
				doBlock(ent);
				doBlockEffect(ent);
				// I want to disable the shield but that would make it not block
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static final float ModelBlocking(ItemStack stack, @Nullable Level worldIn, @Nullable LivingEntity entityIn) {
		return entityIn != null && entityIn.isUsingItem() && entityIn.getUseItem() == stack ? 1.0F : 0.0F;
	}

}
