package com.smanzana.nostrummagica.ritual;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenIndexed;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.progression.requirement.IRequirement;
import com.smanzana.nostrummagica.ritual.outcome.IItemRitualOutcome;
import com.smanzana.nostrummagica.ritual.outcome.IRitualOutcome;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.MagicCapability;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class RitualRecipe /*extends ForgeRegistryEntry<RitualRecipe>*/ implements InfoScreenIndexed {
	
	public static final class RitualResult {
		public boolean performed;
		public final @Nonnull EMagicElement element;
		public final ItemStack output;
		public final ItemStack centerItem;
		public final List<ItemStack> extraItems;
		public final List<ItemStack> reagentItems;
		
		public RitualResult(boolean performed, EMagicElement element, ItemStack output, ItemStack centerItem, List<ItemStack> extraItems, List<ItemStack> reagentTypes) {
			this.element = element == null ? EMagicElement.NEUTRAL : element;
			this.performed = performed;
			this.output = output;
			this.centerItem = centerItem;
			this.extraItems = extraItems == null ? new ArrayList<>() : extraItems;
			this.reagentItems = reagentTypes == null ? new ArrayList<>() : reagentTypes;
		}

		public static RitualResult Fail() {
			return new RitualResult(false, null, ItemStack.EMPTY, ItemStack.EMPTY, null, null);
		}
	}
	
	private final EMagicElement element;
	private final int tier;
	private ReagentType types[];
	private @Nonnull Ingredient centerItem;
	private NonNullList<Ingredient> extraItems;
	private IRitualOutcome hook;
	private IRequirement req;
	private final String titleKey;
	
	private @Nonnull ItemStack icon;
	
	public static RitualRecipe createTier1(String registryName,
			String titleKey,
			@Nonnull ItemStack icon,
			EMagicElement element,
			ReagentType reagent,
			IRequirement requirement,
			IRitualOutcome outcome) {
		RitualRecipe recipe = new RitualRecipe(registryName, titleKey, element, 0);
		
		recipe.types[0] = reagent;
		recipe.hook = outcome;
		recipe.req = requirement;
		recipe.icon = icon;
		
		Validate.notNull(icon);
		
		return recipe;
	}
	
	public static RitualRecipe createTier1(String titleKey,
			@Nonnull ItemStack icon,
			EMagicElement element,
			ReagentType reagent,
			IRequirement requirement,
			IRitualOutcome outcome) {
		return createTier1(titleKey, titleKey, icon, element, reagent, requirement, outcome);
	}
	
	public static RitualRecipe createTier2(String registryName,
			String titleKey,
			@Nonnull ItemStack icon,
			EMagicElement element,
			ReagentType[] reagents,
			@Nonnull Ingredient center, 
			IRequirement requirement,
			IRitualOutcome outcome) {
		if (center == null || center == Ingredient.EMPTY) {
			throw new RuntimeException("Center item of tier 2 ritual cannot be empty!");
		}
		
		RitualRecipe recipe = new RitualRecipe(registryName, titleKey, element, 1);
		
		for (int i = 0; i < 4 && i < reagents.length; i++) {
			recipe.types[i] = reagents[i];
		}
		
		recipe.centerItem = center;
		recipe.hook = outcome;
		recipe.req = requirement;
		recipe.icon = icon;
		
		Validate.notNull(icon);
		Validate.notNull(center);
		
		return recipe;
	}
	
	public static RitualRecipe createTier2(String titleKey,
			@Nonnull ItemStack icon,
			EMagicElement element,
			ReagentType[] reagents,
			@Nonnull Ingredient center, 
			IRequirement requirement,
			IRitualOutcome outcome) {
		return createTier2(titleKey, titleKey, icon, element, reagents, center, requirement, outcome);
	}

	@Deprecated
	public static RitualRecipe createTier2(String titleKey,
			@Nonnull ItemStack icon,
			EMagicElement element,
			ReagentType[] reagents,
			@Nonnull ItemStack center, 
			IRequirement requirement,
			IRitualOutcome outcome) {
		return createTier2(titleKey,
				icon,
				element,
				reagents,
				Ingredient.of(center),
				requirement,
				outcome
				);
	}
	
	public static RitualRecipe createTier3(String registryName,
			String titleKey,
			@Nonnull ItemStack icon,
			EMagicElement element,
			ReagentType[] reagents,
			@Nonnull Ingredient center,
			@Nonnull Ingredient extras[],
			IRequirement requirement,
			IRitualOutcome outcome) {
		if (center == null || center == Ingredient.EMPTY) {
			throw new RuntimeException("Center item of tier 3 ritual cannot be empty!");
		}
		
		RitualRecipe recipe = new RitualRecipe(registryName, titleKey, element, 2);
		
		for (int i = 0; i < 4 && i < reagents.length; i++) {
			recipe.types[i] = reagents[i];
		}
		recipe.centerItem = center;
		
		for (int i = 0; i < 4 && i < extras.length; i++) {
			if (extras[i] == null) {
				throw new RuntimeException(String.format("Extra item %d of tier 3 ritual cannot be null!", i));
			}
			recipe.extraItems.set(i, extras[i]);
		}

		recipe.hook = outcome;
		recipe.req = requirement;
		recipe.icon = icon;
		
		Validate.notNull(icon);
		
		return recipe;
	}
	
	public static RitualRecipe createTier3(String titleKey,
			@Nonnull ItemStack icon,
			EMagicElement element,
			ReagentType[] reagents,
			@Nonnull Ingredient center,
			@Nonnull Ingredient extras[],
			IRequirement requirement,
			IRitualOutcome outcome) {
		return createTier3(titleKey, titleKey, icon, element, reagents, center, extras, requirement, outcome);
	}
	
	@Deprecated
	public static RitualRecipe createTier3(String titleKey,
			@Nonnull ItemStack icon,
			EMagicElement element,
			ReagentType[] reagents,
			@Nonnull ItemStack center,
			@Nonnull ItemStack extras[],
			IRequirement requirement,
			IRitualOutcome outcome) {
		
		Ingredient[] extraTags;
		extraTags = new Ingredient[extras.length];
		for (int i = 0; i < extras.length; i++) {
			extraTags[i] = extras[i].isEmpty() ? Ingredient.EMPTY : Ingredient.of(extras[i]);
		}
		
		return createTier3(titleKey,
				icon,
				element,
				reagents,
				Ingredient.of(center),
				extraTags,
				requirement,
				outcome
				);
	}
	
	private RitualRecipe(String registryName, String nameKey, EMagicElement element, int tier) {
		this.tier = tier;
		this.element = element == null ? EMagicElement.NEUTRAL : element;
		this.titleKey = nameKey;
		this.centerItem = Ingredient.EMPTY;
		if (tier == 0) {
			this.types = new ReagentType[1];
		} else {
			this.types = new ReagentType[4];
		}
		
		if (tier == 2) {
			this.extraItems = NonNullList.withSize(4, Ingredient.EMPTY);
		} else {
			this.extraItems = NonNullList.create();
		}
		//this.setRegistryName(registryName);
	}
	
	public boolean matches(Player player, Level world, BlockPos center, IRitualIngredients ingredients) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return false;
		
		if (!MagicCapability.RITUAL_ENABLED.matches(attr))
			return false;
		
		if (this.req != null && !req.matches(player))
			return false;
		
		// Check tier requirement
		if (!ingredients.hasTierBlocks(getTier())) {
			return false;
		}
		
		// Check element
		if (!ingredients.hasElement(this.getElement())) {
			return false;
		}
		
		// This relies on tier 1 always having a reagent array of size 1 else size 4
		// and centerItem and extraItems being non-null
		if (!ingredients.hasReagents(Lists.newArrayList(getTypes()))) {
			return false;
		}
		if (!ingredients.hasCenterItem(getCenterItem())) {
			return false; 
		}
		if (!ingredients.hasExtraItems(this.getExtraItems())) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Attempt to perform the ritual.
	 * Returns whether the ritual was performed or not.
	 * @param world
	 * @param player
	 * @param center
	 * @return
	 */
	public RitualResult perform(Level world, Player player, BlockPos center, IRitualLayout ingredients) {
		
		if (world.isClientSide) {
			return new RitualResult(true, this.element, ItemStack.EMPTY, ItemStack.EMPTY, null, null);
		}
		
		if (hook != null && !hook.canPerform(world, player, center, ingredients)) {
			return RitualResult.Fail();
		}
		
		// Going to succeed. So capture result outputs not before clearing
		final ItemStack output = (getOutcome() instanceof IItemRitualOutcome
				? ((IItemRitualOutcome) getOutcome()).getResult().copy()
				: ItemStack.EMPTY
				);
		final RitualResult result = new RitualResult(true, this.element, output,
				ingredients.getCenterItem(world, center),
				ingredients.getExtraItems(world, center),
				ingredients.getReagentItems(world, center));
		
		ingredients.clearIngredients(world, center, this);

		if (hook != null) {
			hook.perform(world, player, center, ingredients, this);
		}
		
		return result;
	}

	public EMagicElement getElement() {
		return element;
	}

	public int getTier() {
		return tier;
	}

	public ReagentType[] getTypes() {
		return types;
	}

	public @Nonnull Ingredient getCenterItem() {
		return centerItem;
	}

	public NonNullList<Ingredient> getExtraItems() {
		return extraItems;
	}
	
	public IRitualOutcome getOutcome() {
		return this.hook;
	}
	
	public IRequirement getRequirement() {
		return this.req;
	}
	
	public String getTitleKey() {
		return titleKey;
	}

	public @Nonnull ItemStack getIcon() {
		return icon;
	}

	@Override
	public String getInfoScreenKey() {
		return "ritual::" + titleKey;
	}
	
}
