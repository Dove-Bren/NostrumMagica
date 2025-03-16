package com.smanzana.nostrummagica.item.equipment;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.MineBlock;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.crafting.NostrumTags;
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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public class AspectedEarthWeapon extends DiggerItem implements ILoreTagged, ISpellEquipment {

	public static final String ID = "sword_earth";
	
	private static final float AttackDamage = 3.0f;
	private static final float AttackSpeed = -1.8f;
	
	public AspectedEarthWeapon() {
		super(AttackDamage, AttackSpeed, Tiers.DIAMOND, BlockTags.MINEABLE_WITH_PICKAXE, NostrumItems.PropEquipment().durability(1440));
	}
	
	protected final boolean toolMatches(ItemStack stack, BlockState state) {
		return state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
	}
	
	@Override
	public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
		// Only allow axe mining; Don't strip with special axe
		return ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(toolAction)
				|| ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(toolAction);
	}
	
	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		// Simulate pick and shovel
		if (toolMatches(stack, state)) {
			return this.speed;
		}
		return 1.0f;
	}
	
	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		stack.hurtAndBreak(1, attacker, (p) -> {
			p.broadcastBreakEvent(EquipmentSlot.MAINHAND);
		});
		return true;
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		Multimap<Attribute, AttributeModifier> multimap = super.getDefaultAttributeModifiers(equipmentSlot);
//		if (equipmentSlot == EquipmentSlotType.MAINHAND) {
//			multimap.put(Attributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", (double)attackDamage, AttributeModifier.Operation.ADDITION));
//			multimap.put(Attributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", (double)attackSpeed, AttributeModifier.Operation.ADDITION));
//		}

		return multimap;
	}
	
	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		if (enchantment.category == EnchantmentCategory.WEAPON) {
			return true;
		}
		
		return super.canApplyAtEnchantingTable(stack, enchantment);
	}
	
	/**
	 * Check whether this Item can harvest the given Block
	 */
	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState blockIn) {
		if (toolMatches(stack, blockIn)) {
			return TierSortingRegistry.isCorrectTierForDrops(getTier(), blockIn);
		}
		
		return false;
	}

	@Override
	public String getLoreKey() {
		return "sword_earth";
	}

	@Override
	public String getLoreDisplayName() {
		return "Earth Pike";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("The Earth Pike doubles as a weapon and a tool. As a weapon, it has the same attack strength as a diamond sword. As a tool, it functions as a shovel and a pickaxe.");
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("The Earth Pike doubles as a weapon and a tool. As a weapon, it has the same attack strength as a diamond sword. As a tool, it functions as a shovel and a pickaxe.", "When wearing the True Tremor set, the pike can also place down mines on the top of most types of stone and ore. When stepped on, the mines do damage and inflict a status that causes mobs to drop more items. When mined with the pike, the wielder vein mines!");
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
			return NostrumTags.Items.InfusedGemEarth.contains(repair.getItem());
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
	public InteractionResult useOn(UseOnContext context) {
		final @Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(context.getPlayer());
		final ItemStack held = context.getItemInHand();
		final boolean hasBonus = ElementalArmor.GetSetCount(context.getPlayer(), EMagicElement.EARTH, ElementalArmor.Type.MASTER) == 4;
		final boolean canUpgrade = attr != null && attr.hasSkill(NostrumSkills.Earth_Weapon);
		final int manaCost = 20;
		
		if (context.getPlayer().isShiftKeyDown() && hasBonus && attr.getMana() >= manaCost) {
			if (!context.getLevel().isClientSide()) {
				if (canUpgrade && context.getLevel().getBlockState(context.getClickedPos()).getBlock() instanceof MineBlock) {
					BlockState state = context.getLevel().getBlockState(context.getClickedPos());
					if (state.getValue(MineBlock.LEVEL) < 3) {
						context.getLevel().setBlockAndUpdate(context.getClickedPos(), state.setValue(MineBlock.LEVEL, state.getValue(MineBlock.LEVEL) + 1));
						doMineEffect(context.getPlayer(), context.getLevel(), context.getClickedPos());
						attr.addMana(-manaCost);
						NostrumMagica.instance.proxy.sendMana(context.getPlayer());
					}
				} else if (context.getLevel().isEmptyBlock(context.getClickedPos().relative(context.getClickedFace()))) {
				BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
					if (makeMine(context.getLevel(), context.getPlayer(), pos, context.getClickedFace().getOpposite())) {
						ItemStacks.damageItem(held, context.getPlayer(), context.getHand(), 1);
						doMineEffect(context.getPlayer(), context.getLevel(), context.getClickedPos());
						attr.addMana(-manaCost);
						NostrumMagica.instance.proxy.sendMana(context.getPlayer());
					}
				}
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
	
	protected static void doMineEffect(Player caster, Level world, BlockPos pos) {
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
	
	protected static boolean makeMine(Level world, Player playerIn, BlockPos pos, Direction face) {
		BlockState stateToPlace = NostrumBlocks.mineBlock.defaultBlockState().setValue(MineBlock.FACING, face);
		if (NostrumBlocks.mineBlock.canSurvive(stateToPlace, world, pos)) {
			world.setBlock(pos, stateToPlace, 3);
			return true;
		} else {
			return false;
		}
	}
}
