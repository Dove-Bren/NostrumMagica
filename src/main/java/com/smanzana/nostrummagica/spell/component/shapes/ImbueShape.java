package com.smanzana.nostrummagica.spell.component.shapes;

import java.util.ArrayList;

import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.item.equipment.SpellScroll;
import com.smanzana.nostrummagica.spell.ItemImbuement;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;

import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.Lazy;

/**
 * Shape that waits some time before proceeding
 * @author Skyler
 *
 */
public class ImbueShape extends InstantShape {

	private static final String ID = "imbue";
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.of(ItemStack.EMPTY, ReagentItem.CreateStack(ReagentType.GINSENG, 1)));
	
	protected ImbueShape(String key) {
		super(key);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
	}
	
	public ImbueShape() {
		this(ID);
	}
	
	@Override
	public NonNullList<ItemStack> getReagents() {
		return REAGENTS.get();
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.AMETHYST_SHARD);
	}

	@Override
	public int getManaCost(SpellShapeProperties properties) {
		return 5;
	}

	@Override
	public int getWeight(SpellShapeProperties properties) {
		return 1;
	}

	@Override
	public boolean shouldTrace(Player player, SpellShapeProperties params) {
		return false;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(true, true, false);
	}

	@Override
	public boolean supportsPreview(SpellShapeProperties params) {
		return false;
	}

	@Override
	protected TriggerData getTargetData(ISpellState state, LivingEntity entity, SpellLocation location, float pitch,
			float yaw, SpellShapeProperties params, SpellCharacteristics characteristics) {
		ItemStack stack = entity.getMainHandItem();
		if (stack.isEmpty() || stack.getItem() instanceof SpellScroll) {
			stack = entity.getOffhandItem();
		}
		
		if (!stack.isEmpty()) {
			ItemImbuement imbue = ItemImbuement.Make(state.getSpell().getSpellEffectParts(), 1f, state.getSpell().getIconIndex());
			ItemStack imbued = stack.getCount() > 1 ? stack.split(1) : stack;
			ItemImbuement.AttachToStack(imbued, imbue);
			if (imbued != stack) {
				// need to add to entity
				if (entity instanceof Player player) {
					player.addItem(imbued);
				} else {
					entity.setItemInHand(entity.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, imbued);
				}
			}
		}
		
		// Make the spell fizzle out in the immediate execution
		return new TriggerData(new ArrayList<>(), new ArrayList<>());
	}
}
