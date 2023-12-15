package com.smanzana.nostrummagica.items;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.effects.NostrumEffects;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.SwordItem;
import net.minecraft.item.UseAction;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class AspectedPhysicalWeapon extends SwordItem implements ILoreTagged, ISpellArmor {

	public static final String ID = "sword_physical";
	
	public AspectedPhysicalWeapon() {
		super(ItemTier.DIAMOND, 6, -3.0F, NostrumItems.PropEquipment().maxDamage(1240).addToolType(ToolType.AXE, 3));
		
		this.addPropertyOverride(new ResourceLocation("blocking"), new IItemPropertyGetter() {
			@OnlyIn(Dist.CLIENT)
			public float call(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
				return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
			}
		});
	}
	
	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		if (getToolTypes(stack).stream().anyMatch(e -> state.isToolEffective(e))) return 8.0f; // diamond level
		return 1.0f;
	}
	
	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
		return super.getAttributeModifiers(equipmentSlot);
    }
	
	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		if (enchantment.type == EnchantmentType.DIGGER) {
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
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		if (repair.isEmpty()) {
			return false;
		} else {
			return NostrumTags.Items.InfusedGemVoid.contains(repair.getItem());
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
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		final ItemStack held = playerIn.getHeldItem(hand);
		playerIn.setActiveHand(hand);
		return new ActionResult<ItemStack>(ActionResultType.SUCCESS, held);
	}
	
	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BLOCK;
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
		if (caster.world.isRemote) {
			return;
		}
		
//		final Vec3d casterPos = caster.getPositionVec().add(0, caster.getEyeHeight(), 0);
//		final Vec3d targetPos = target.getPositionVec().add(0, target.getHeight()/2, 0); 
//		Vec3d diff = targetPos.subtract(casterPos);
//		
//		// Could go discrete increments, but just divide and stretch
//		final int intervals = 10;
//		for (int i = 0; i < intervals; i++) {
//			Vec3d offset = diff.scale((float) i/ (float) intervals);
//			final Vec3d pos = casterPos.add(offset);
//			NostrumParticles.GLOW_ORB.spawn(caster.world, new SpawnParams(
//					1,
//					pos.x, pos.y, pos.z, 0, 30, 5,
//					target.getEntityId()
//					).color(0xFFFF0000).dieOnTarget(true));
//		}
	}
	
	protected static void doBlock(LivingEntity blocker) {
		final boolean hasBonus = MagicArmor.GetSetCount(blocker, EMagicElement.PHYSICAL, MagicArmor.Type.TRUE) == 4;
		blocker.addPotionEffect(new EffectInstance(NostrumEffects.rendStrike, 1 * 20, 0));
		
		if (hasBonus) {
			blocker.addPotionEffect(new EffectInstance(NostrumEffects.steelSkin, 3 * 20, 0));
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEntityHit(LivingAttackEvent event) {
		if (event.getEntityLiving().world.isRemote()) {
			return;
		}
		
		// I really wish there was an item.onBlock() or even a LivingBlockEvent but there is neither.
		
		final LivingEntity ent = event.getEntityLiving();
		final DamageSource source = event.getSource();
		if (ent.isActiveItemStackBlocking() && ent.getActiveItemStack().getItem() instanceof AspectedPhysicalWeapon) {
			// This is based on LivingEntity#attackEntityFrom
			if (event.getAmount() > 0.0F
					// && ent.canBlockDamageSource(source)) { not visible
					&& !source.isUnblockable()
					&& source.getTrueSource() != null
					) { 
				doBlock(ent);
				doBlockEffect(ent);
				// I want to disable the shield but that would make it not block
			}
		}
	}

}
