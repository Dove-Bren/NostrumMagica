package com.smanzana.nostrummagica.items;

import java.util.HashSet;
import java.util.List;

import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.blocks.MineBlock;
import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.ToolItem;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

public class AspectedEarthWeapon extends ToolItem implements ILoreTagged, ISpellArmor {

	public static final String ID = "sword_earth";
	
	private static final float AttackDamage = 3.0f;
	private static final float AttackSpeed = -1.8f;
	private static final int HarvestLevel = 3;
	
	public AspectedEarthWeapon() {
		super(AttackDamage, AttackSpeed, ItemTier.DIAMOND, new HashSet<>(), NostrumItems.PropEquipment().maxDamage(1440).addToolType(ToolType.PICKAXE, HarvestLevel).addToolType(ToolType.SHOVEL, HarvestLevel));
	}
	
	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		stack.damageItem(1, attacker, (p) -> {
			p.sendBreakAnimation(EquipmentSlotType.MAINHAND);
		});
		return true;
	}
	
	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(equipmentSlot);
//		if (equipmentSlot == EquipmentSlotType.MAINHAND) {
//			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", (double)attackDamage, AttributeModifier.Operation.ADDITION));
//			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", (double)attackSpeed, AttributeModifier.Operation.ADDITION));
//		}

		return multimap;
	}
	
	/**
	 * Check whether this Item can harvest the given Block
	 */
	@Override
	public boolean canHarvestBlock(BlockState blockIn) {
		Block block = blockIn.getBlock();
		final int harvestLevel = this.getHarvestLevel(ItemStack.EMPTY, blockIn.getHarvestTool(), null, blockIn);
		if (harvestLevel != -1) {
			return blockIn.getHarvestLevel() <= harvestLevel;
		}
		
		// Copied from pickaxe and shovel
		Material material = blockIn.getMaterial();
		return material == Material.ROCK || material == Material.IRON || material == Material.ANVIL
				|| block == Blocks.SNOW || block == Blocks.SNOW_BLOCK;
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		// Copied from pickaxe
		Material material = state.getMaterial();
		return material != Material.IRON && material != Material.ANVIL && material != Material.ROCK ? super.getDestroySpeed(stack, state) : this.efficiency;
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
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		if (repair.isEmpty()) {
			return false;
		} else {
			return NostrumTags.Items.InfusedGemEarth.contains(repair.getItem());
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
	public ActionResultType onItemUse(ItemUseContext context) {
		final ItemStack held = context.getItem();
		final boolean hasBonus = MagicArmor.GetSetCount(context.getPlayer(), EMagicElement.EARTH, MagicArmor.Type.TRUE) == 4;
		
		if (context.getPlayer().isSneaking() && hasBonus) {
			if (!context.getWorld().isRemote()) {
				BlockPos pos = context.getPos().offset(context.getFace());
				if (context.getWorld().isAirBlock(pos)) {
					if (makeMine(context.getWorld(), context.getPlayer(), pos, context.getFace().getOpposite())) {
						ItemStacks.damageItem(held, context.getPlayer(), context.getHand(), 1);
						doMineEffect(context.getPlayer(), context.getWorld(), context.getPos());
					}
				}
			}
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}
	
	protected static void doMineEffect(PlayerEntity caster, World world, BlockPos pos) {
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
	
	protected static boolean makeMine(World world, PlayerEntity playerIn, BlockPos pos, Direction face) {
		BlockState stateToPlace = NostrumBlocks.mineBlock.getDefaultState().with(MineBlock.FACING, face);
		if (NostrumBlocks.mineBlock.isValidPosition(stateToPlace, world, pos)) {
			world.setBlockState(pos, stateToPlace, 3);
			return true;
		} else {
			return false;
		}
	}
}
