package com.smanzana.nostrummagica.ritual;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.Candle;
import com.smanzana.nostrummagica.block.ChalkBlock;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenIndexed;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.progression.requirement.IRequirement;
import com.smanzana.nostrummagica.ritual.outcome.IItemRitualOutcome;
import com.smanzana.nostrummagica.ritual.outcome.IRitualOutcome;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.tile.AltarTileEntity;
import com.smanzana.nostrummagica.tile.CandleTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RitualRecipe /*extends ForgeRegistryEntry<RitualRecipe>*/ implements InfoScreenIndexed {
	
	public static final class RitualMatchInfo {
		public boolean matched;
		@Nonnull public final ItemStack center;
		@Nullable public final NonNullList<ItemStack> extras;
		@Nonnull public final ItemStack output;
		@Nullable public final ReagentType reagents[];
		public final EMagicElement element;
		
		public RitualMatchInfo(boolean matched, EMagicElement element, 
				ItemStack center, NonNullList<ItemStack> extras, ItemStack output,
				ReagentType[] reagents) {
			super();
			this.matched = matched;
			this.center = center;
			this.extras = extras;
			this.output = output;
			this.reagents = reagents;
			this.element = element;
		}
		
		public static RitualMatchInfo Fail() {
			return new RitualMatchInfo(false, EMagicElement.PHYSICAL, ItemStack.EMPTY, null, ItemStack.EMPTY, null);
		}
	}
	
	private static final int CHALK_XS[][] = new int[][] {
		new int[]{-1, 0, 1, -1, 1, -1, 0, 1},
		new int[]{0, -1, 1, -2, 2, -1, 1, 0},
		new int[]{-2, -1, 1, 2, -3, -1, 0, 1, 3, -3, -2, -1, 1, 2, 3, -2, 2, -3, -2, -1, 1, 2, 3, -3, -1, 0, 1, 3, -2, -1, 1, 2}
	};
	private static final int CHALK_YS[][] = new int[][] {
		new int[]{-1, -1, -1, 0, 0, 1, 1, 1},
		new int[]{-2, -1, -1, 0, 0, 1, 1, 2},
		new int[]{-3, -3, -3, -3, -2, -2, -2, -2, -2, -1, -1, -1, -1, -1, -1, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3}
	};

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
				Ingredient.fromStacks(center),
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
			extraTags[i] = extras[i].isEmpty() ? Ingredient.EMPTY : Ingredient.fromStacks(extras[i]);
		}
		
		return createTier3(titleKey,
				icon,
				element,
				reagents,
				Ingredient.fromStacks(center),
				extraTags,
				requirement,
				outcome
				);
	}
	
	private RitualRecipe(String registryName, String nameKey, EMagicElement element, int tier) {
		this.tier = tier;
		this.element = element;
		this.titleKey = nameKey;
		this.centerItem = Ingredient.EMPTY;
		if (tier == 0) {
			this.types = new ReagentType[1];
		} else {
			this.types = new ReagentType[4];
		}
		
		if (tier == 2) {
			this.extraItems = NonNullList.withSize(4, Ingredient.EMPTY);
		}
		//this.setRegistryName(registryName);
	}
	
	protected static RitualMatchInfo Capture(World world, BlockPos center, RitualRecipe recipe) {
		final ItemStack output;
		final ItemStack centerItem;
		final ReagentType[] reagents;
		final NonNullList<ItemStack> extras;
		
		final TileEntity centerTE = world.getTileEntity(center);
		
		if (recipe.tier == 0) {
			centerItem = ItemStack.EMPTY;
			extras = null;
			
			ReagentType type = null;
			if (world.getBlockState(center).getBlock() instanceof Candle
					&& centerTE != null
					&& centerTE instanceof CandleTileEntity) {
				CandleTileEntity candle = (CandleTileEntity) centerTE;
				type = candle.getReagentType();
			}
			
			reagents = (type == null ? null : new ReagentType[] {type});
		} else {
			// Tier 2 and 3 common: center + candles
			{
				// Center item
				if (centerTE != null && centerTE instanceof AltarTileEntity) {
					centerItem = ((AltarTileEntity) centerTE).getItem().copy();
				} else {
					centerItem = ItemStack.EMPTY;
				}
				
				// get all candles. Must be a candle in all the spots.
				// then try to match reagent types with required ones
				TileEntity te;
				List<ReagentType> reagentList = new ArrayList<>(4);
				for (int x = -2; x <= 2; x += 4)
				for (int z = -2; z <= 2; z += 4) {
					te = world.getTileEntity(center.add(x, 0, z));
					if (te == null || !(te instanceof CandleTileEntity))
						return RitualMatchInfo.Fail();
					
					CandleTileEntity candle = (CandleTileEntity) te;
					reagentList.add(candle.getReagentType());
				}
				
				if (reagentList.size() == 4) {
					reagents = new ReagentType[4];
					for (int i = 0; i < reagentList.size(); i++) {
						reagents[i] = reagentList.get(i);
					}
				} else {
					reagents = null;
				}
			}
			
			if (recipe.tier == 2) {
				// Get extra altars
				NonNullList<ItemStack> items = NonNullList.create();
				TileEntity te;
				boolean foundNonEmpty = false;
				for (int x = -4; x <= 4; x+=4) {
					int diff = 4 - Math.abs(x);
					for (int z = -diff; z <= diff; z+=8) {
						te = world.getTileEntity(center.add(x, 0, z));
						if (te == null || !(te instanceof AltarTileEntity))
							return RitualMatchInfo.Fail();
						AltarTileEntity altar = (AltarTileEntity) te;
						items.add(altar.getItem());
						if (!altar.getItem().isEmpty()) {
							foundNonEmpty = true;
						}
					}
				}
				if (!foundNonEmpty) {
					extras = null;
				} else {
					extras = items;
				}
			} else {
				extras = null;
			}
		}
		
		output = (recipe.getOutcome() instanceof IItemRitualOutcome
				? ((IItemRitualOutcome) recipe.getOutcome()).getResult().copy()
				: ItemStack.EMPTY
				);
		return new RitualMatchInfo(false, recipe.element, centerItem, extras, output, reagents);
	}
	
	protected static void ClearRitual(World world, BlockPos center, RitualRecipe recipe) {
		// Do cleanup of altars and candles, etc
		if (recipe.tier == 0) {
			// candle in center. extinguish
			Candle.extinguish(world, center, world.getBlockState(center));
		} else {
			// candles at spots. extinguish.
			for (int x = -2; x <= 2; x += 4)
			for (int z = -2; z <= 2; z += 4) {
				BlockPos pos = center.add(x, 0, z);
				Candle.extinguish(world, pos, world.getBlockState(pos));
			}
			
			// Clear off altars also
			TileEntity te;
			te = world.getTileEntity(center);
			if (te != null && te instanceof AltarTileEntity) {
				((AltarTileEntity) te).setItem(ItemStack.EMPTY);
			}
			
			if (recipe.tier == 2) {
				for (int x = -4; x <= 4; x+=4) {
					int diff = 4 - Math.abs(x);
					for (int z = -diff; z <= diff; z+=8) {
						te = world.getTileEntity(center.add(x, 0, z));
						if (te == null || !(te instanceof AltarTileEntity))
							continue; // oh well, too late now!
						((AltarTileEntity) te).setItem(ItemStack.EMPTY);
					}
				}
			}
		}
	}
	
	public RitualMatchInfo matches(PlayerEntity player, World world, BlockPos center, EMagicElement element) {
		if (element == null) {
			element = EMagicElement.PHYSICAL;
		}
		
		// Do null matching with physical
		if (element == EMagicElement.PHYSICAL) {
			if (this.element != null && this.element != EMagicElement.PHYSICAL) {
				return RitualMatchInfo.Fail();
			}
		} else if (element != this.element) {
			return RitualMatchInfo.Fail();
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return RitualMatchInfo.Fail();
		
		if (!attr.getCompletedResearches().contains("rituals"))
			return RitualMatchInfo.Fail();
		
		if (this.req != null && !req.matches(player))
			return RitualMatchInfo.Fail();
		
		// check chalk
		int[] xs = CHALK_XS[tier];
		int[] ys = CHALK_YS[tier];
		for (int index = 0; index < xs.length; index++) {
			BlockState state = world.getBlockState(center.add(xs[index], 0, ys[index]));
			
			if (state == null || !(state.getBlock() instanceof ChalkBlock))
				return RitualMatchInfo.Fail();
		}
		
		
		//// END early fail cases ////
		final RitualMatchInfo capture = Capture(world, center, this);
		if (tier == 0) {
			capture.matched = (capture.reagents != null && capture.reagents.length == 1 && capture.reagents[0] == types[0]);
		} else if (tier == 1) {
			capture.matched = (
					capture.reagents != null && capture.reagents.length == this.types.length
					&& !capture.center.isEmpty()
					// && capture.extras == null 
				);
			if (capture.matched) {
				// Deep check reagents
				List<ReagentType> typePool = Lists.newArrayList(capture.reagents);
				for (int i = 0; i < this.types.length; i++) {
					ReagentType req = this.types[i];
					if (!typePool.remove(req)) {
						capture.matched = false;
						break;
					}
				}
				if (capture.matched && !typePool.isEmpty()) {
					capture.matched = false;
				}
			}
			if (capture.matched) {
				// Deep check center
				capture.matched = this.centerItem.test(capture.center);
			}
		} else if (tier == 2) {
			capture.matched = (
					capture.reagents != null && capture.reagents.length == this.types.length
					&& !capture.center.isEmpty()
					&& capture.extras != null// && capture.extras.size() == 4
				);
			if (capture.matched) {
				// Deep check reagents
				List<ReagentType> typePool = Lists.newArrayList(capture.reagents);
				for (int i = 0; i < this.types.length; i++) {
					ReagentType req = this.types[i];
					if (!typePool.remove(req)) {
						capture.matched = false;
						break;
					}
				}
				if (capture.matched && !typePool.isEmpty()) {
					capture.matched = false;
				}
			}
			if (capture.matched) {
				// Deep check center
				capture.matched = this.centerItem.test(capture.center);
			}
			if (capture.matched) {
				// Deep check extras
				NonNullList<ItemStack> items = NonNullList.create();
				items.addAll(capture.extras);
				items.removeIf((stack) -> {return stack.isEmpty();});
				for (Ingredient req : this.extraItems) {
					if (req == null || req == Ingredient.EMPTY) {
						continue;
					}
					
					boolean found = false;
					Iterator<ItemStack> it = items.iterator();
					while (it.hasNext()) {
						ItemStack avail = it.next();
						if (req.test(avail)) {
							it.remove();
							found = true;
							break;
						}
					}
					
					if (!found) {
						capture.matched = false;
						break;
					}
				}
				
				if (capture.matched && !items.isEmpty()) {
					capture.matched = false;
				}
			}
		}
		
		return capture;
			
	}
	
	/**
	 * Attempt to perform the ritual.
	 * Returns whether the ritual was performed or not.
	 * @param world
	 * @param player
	 * @param center
	 * @return
	 */
	public boolean perform(World world, PlayerEntity player, BlockPos center) {
		
		if (world.isRemote)
			return true;
		
		final RitualMatchInfo ingredients = Capture(world, center, this);
		
		if (hook != null && !hook.canPerform(world, player, center, ingredients)) {
			return false;
		}
		
		ClearRitual(world, center, this);

		if (hook != null)
			hook.perform(world, player, ingredients.center, ingredients.extras, center, this);
		
		return true;
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
