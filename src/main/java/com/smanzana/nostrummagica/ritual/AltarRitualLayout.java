package com.smanzana.nostrummagica.ritual;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.CandleBlock;
import com.smanzana.nostrummagica.block.ChalkBlock;
import com.smanzana.nostrummagica.criteria.RitualCriteriaTrigger;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.ritual.RitualRecipe.RitualResult;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.tile.AltarTileEntity;
import com.smanzana.nostrummagica.tile.CandleTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Ritual input based on itemstack center, extras, and reagent type reagents from candles and altars.
 * Wants exact matches on extras and reagents. That means if a recipe wants only 3 extras and we have 4, we will reject it.
 */
public class AltarRitualLayout implements IRitualLayout {
	
	protected static final AltarRitualLayout EMPTY = new AltarRitualLayout(0, EMagicElement.PHYSICAL, ItemStack.EMPTY, null, null); 

	protected final int tier;
	protected final EMagicElement element;
	protected final ItemStack centerItem;
	protected final List<ItemStack> extraItems;
	protected final List<ItemStack> reagentItems;
	protected final ReagentType[] reagentTypes;
	
	public AltarRitualLayout(int tier, EMagicElement element, ItemStack centerItem, List<ItemStack> extraItems, List<ItemStack> reagents) {
		this.tier = tier;
		this.element = element;
		this.centerItem = centerItem;
		this.extraItems = extraItems == null ? new ArrayList<>() : extraItems;
		this.reagentItems = reagents == null ? new ArrayList<>() : reagents;
		this.reagentTypes = reagents == null ? new ReagentType[0] 
				: reagents.stream().map(stack -> ReagentItem.FindType(stack)).collect(Collectors.toList()).toArray(new ReagentType[0]);
	}

	@Override
	public boolean hasTierBlocks(int tier) {
		return this.tier >= tier;
	}

	@Override
	public boolean hasCenterItem(Ingredient ingredient) {
		return ingredient.test(this.centerItem);
	}

	@Override
	public boolean hasReagents(Iterable<ReagentType> reagents) {
		List<ReagentType> available = Lists.newArrayList(this.reagentTypes);
		
		for (ReagentType req : reagents) {
			if (!available.remove(req)) {
				return false;
			}
		}
		
		return available.isEmpty();
	}

	@Override
	public boolean hasExtraItems(Iterable<Ingredient> ingredients) {
		List<ItemStack> available = this.extraItems.stream().map(i -> i.copy()).collect(Collectors.toList());
		
		for (Ingredient req : ingredients) {
			if (req == null || req == Ingredient.EMPTY) {
				continue;
			}
			
			// want to use .removeIf but that removes all
			Iterator<ItemStack> it = available.iterator();
			boolean found = false;
			while (it.hasNext()) {
				final ItemStack remaining = it.next();
				if (req.test(remaining)) {
					it.remove();
					found = true;
					break;
				}
			}
			
			if (!found) {
				return false;
			}
		}
		
		return available.isEmpty();
	}
	
	@Override
	public boolean hasElement(EMagicElement element) {
		return this.element == element;
	}

	@Override
	public ItemStack getCenterItem(World world, BlockPos center) {
		return this.centerItem;
	}
	
	@Override
	public List<ItemStack> getExtraItems(World world, BlockPos center) {
		return this.extraItems;
	}

	@Override
	public List<ItemStack> getReagentItems(World world, BlockPos center) {
		return this.reagentItems;
	}
	
	@Override
	public void clearIngredients(World world, BlockPos center, RitualRecipe recipePerformed) {
		ClearIngredients(world, center, recipePerformed.getTier());
	}
	
	@Override
	public void setOutputItems(World world, BlockPos center, Iterable<ItemStack> outputs) {
		DistributeOutputs(world, center, outputs);
	}
	
	public static final AltarRitualLayout Capture(World world, BlockPos center, EMagicElement element) {
		final TileEntity centerTE = world.getTileEntity(center);
		
		// Center may be candle or may be altar. It may not be null.
		if (centerTE == null) {
			return EMPTY;
		}
		
		if (centerTE instanceof CandleTileEntity) {
			// Candle center means it can only be tier 1
			if (CheckChalkTier1(world, center)) {
				ItemStack reagent = GetReagent(world, center);
				if (!reagent.isEmpty()) {
					return new AltarRitualLayout(0, element, ItemStack.EMPTY, null, Lists.newArrayList(reagent));
				}
			}
			
			return EMPTY;
		}
		
		// Remaining two tiers require altar center
		if (!(centerTE instanceof AltarTileEntity)) {
			return EMPTY;
		}
		
		// Check chalk to see up to what tier it even can be
		if (!CheckChalkTier2(world, center)) {
			return EMPTY;
		}
		
		// At least tier 1, so from here we'll always return a non-empty ingredient
		final ItemStack centerItem = GetAltarItem(world, center);
		final List<ItemStack> reagents = CollectTier2Reagents(world, center);
		final List<ItemStack> extras;
		final int tier;
		
		// If tier 3, will have extras to collect
		if (CheckChalkTier3(world, center)) {
			tier = 2;
			extras = CollectTier3Extras(world, center);
		} else {
			extras = new ArrayList<>();
			tier = 1;
		}
		
		return new AltarRitualLayout(tier, element, centerItem, extras, reagents);
	}
	
	protected static final ItemStack GetAltarItem(World world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te != null && te instanceof AltarTileEntity) {
			return ((AltarTileEntity) te).getItem();
		}
		
		return ItemStack.EMPTY;
	}
	
	protected static final ItemStack GetReagent(World world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te != null && te instanceof CandleTileEntity) {
			return ReagentItem.CreateStack(((CandleTileEntity) te).getReagentType(), 1);
		}
		
		return ItemStack.EMPTY;
	}
	
	protected static final boolean CheckChalkBlock(World world, BlockPos pos, BlockState state) {
		return state.getBlock() instanceof ChalkBlock;
	}
	
	private static final boolean CheckChalk(World world, BlockPos center, int[] xCoords, int[] yCoords) {
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		for (int i = 0; i < xCoords.length; i++) {
			cursor.setPos(center.getX() + xCoords[i], center.getY(), center.getZ() + yCoords[i]);
			if (!CheckChalkBlock(world, cursor, world.getBlockState(cursor))) {
				return false;
			}
		}
		
		return true;
	}
	
	protected static final boolean CheckChalkTier1(World world, BlockPos center) {
		final int CHALK_XS[] = {-1, 0, 1, -1, 1, -1, 0, 1};
		final int CHALK_YS[] = {-1, -1, -1, 0, 0, 1, 1, 1};
		return CheckChalk(world, center, CHALK_XS, CHALK_YS);
	}
	
	protected static final boolean CheckChalkTier2(World world, BlockPos center) {
		final int CHALK_XS[] = {0, -1, 1, -2, 2, -1, 1, 0};
		final int CHALK_YS[] = {-2, -1, -1, 0, 0, 1, 1, 2};
		return CheckChalk(world, center, CHALK_XS, CHALK_YS);
	}
	
	protected static final boolean CheckChalkTier3(World world, BlockPos center) {
		final int CHALK_XS[] = {-2, -1, 1, 2, -3, -1, 0, 1, 3, -3, -2, -1, 1, 2, 3, -2, 2, -3, -2, -1, 1, 2, 3, -3, -1, 0, 1, 3, -2, -1, 1, 2};
		final int CHALK_YS[] = {-3, -3, -3, -3, -2, -2, -2, -2, -2, -1, -1, -1, -1, -1, -1, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3};
		return CheckChalk(world, center, CHALK_XS, CHALK_YS);
	}
	
	private static final void VisitTier2Candles(World world, BlockPos center, BiConsumer<World, BlockPos> visitor) {
		final int CANDLE_XS[] = {-2, -2, 2, 2};
		final int CANDLE_YS[] = {-2, 2, -2, 2};
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		for (int i = 0; i < CANDLE_XS.length; i++) {
			cursor.setPos(center.getX() + CANDLE_XS[i], center.getY(), center.getZ() + CANDLE_YS[i]);
			visitor.accept(world, cursor);
		}
	}
	
	protected static final List<ItemStack> CollectTier2Reagents(World world, BlockPos center) {
		final List<ItemStack> reagents = new ArrayList<>(4);
		VisitTier2Candles(world, center, (w, pos) -> {
			ItemStack reagent = GetReagent(world, pos);
			if (!reagent.isEmpty()) {
				reagents.add(reagent);
			}
		});
		return reagents;
	}
	
	private static final void VisitTier3Extras(World world, BlockPos center, BiConsumer<World, BlockPos> visitor) {
		final int ALTAR_XS[] = {-4, 0, 0, 4};
		final int ALTAR_YS[] = {0, -4, 4, 0};
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		for (int i = 0; i < ALTAR_XS.length; i++) {
			cursor.setPos(center.getX() + ALTAR_XS[i], center.getY(), center.getZ() + ALTAR_YS[i]);
			visitor.accept(world, cursor);
		}
	}
	
	protected static final List<ItemStack> CollectTier3Extras(World world, BlockPos center) {
		final List<ItemStack> extras = new ArrayList<>(4);
		VisitTier3Extras(world, center, (w, pos) -> {
			ItemStack extra = GetAltarItem(world, pos);
			if (!extra.isEmpty()) {
				extras.add(extra);
			}
		});
		return extras;
	}
	
	protected static final void ClearCandle(World world, BlockPos pos) {
		CandleBlock.extinguish(world, pos, world.getBlockState(pos));
	}
	
	protected static final void ClearAltar(World world, BlockPos pos) {
		TileEntity te;
		te = world.getTileEntity(pos);
		if (te != null && te instanceof AltarTileEntity) {
			((AltarTileEntity) te).setItem(ItemStack.EMPTY);
		}
	}
	
	protected static final void ClearIngredients(World world, BlockPos center, int tier) {
		// This is safe to do without actually taking in what was used to match the recipe because we do exact matching.
		// So a dangerous situation could be if a recipe just wants 1/2/3 extra items even though we have up to 4.
		// If we didn't do exact matching, we'd possibly incorrectly remove whatever the items were that weren't required.
		if (tier == 0) {
			ClearCandle(world, center);
		} else {
			ClearAltar(world, center); // clear center altar
			VisitTier2Candles(world, center, AltarRitualLayout::ClearCandle);
			
			if (tier >= 2) {
				VisitTier3Extras(world, center, AltarRitualLayout::ClearAltar);
			}
		}
	}
	
	protected static final void DistributeOutputs(World world, BlockPos center, Iterable<ItemStack> outputs) {
		// If center is candle, just drop items in the world.
		// Otherwise, try to put in known altar locations. Any leftover, just put in world.
		List<ItemStack> leftover = Lists.newArrayList(outputs);
		
		if (!leftover.isEmpty()) {
			TileEntity te = world.getTileEntity(center);
			if (te != null && te instanceof AltarTileEntity) {
				Iterator<ItemStack> it = leftover.iterator();
				
				// Sort of special case for first item
				if (((AltarTileEntity) te).getItem().isEmpty()) {
					((AltarTileEntity) te).setItem(it.next());
					it.remove();
				}
				
				VisitTier3Extras(world, center, (w, pos) -> {
					if (it.hasNext()) {
						TileEntity posTE = w.getTileEntity(pos);
						if (posTE != null && posTE instanceof AltarTileEntity) {
							if (((AltarTileEntity) posTE).getItem().isEmpty()) {
								((AltarTileEntity) posTE).setItem(it.next());
								it.remove();
							}
						}
					}
				});
			}
		}
		
		// Drop anything left in the world
		for (ItemStack stack : leftover) {
			ItemEntity item = new ItemEntity(world, center.getX() + .5, center.getY() + 1.5, center.getZ() + .5, stack);
			world.addEntity(item);
		}
	}
	
	public static final boolean AttemptRitual(World world, BlockPos pos, PlayerEntity player, EMagicElement element) {
		AltarRitualLayout layout = Capture(world, pos, element);
		
		for (RitualRecipe ritual : RitualRegistry.instance().getRegisteredRituals()) {
			if (ritual.matches(player, world, pos, layout)) {
				// Try to take first match
				RitualResult result = ritual.perform(world, player, pos, layout);
				if (result.performed) {
					RitualRegistry.instance().fireRitualPerformed(ritual, world, player, pos);
					
					NostrumMagicaSounds.AMBIENT_WOOSH2.play(world, pos.getX(), pos.getY(), pos.getZ());
	
					NostrumMagica.instance.proxy.playRitualEffect(world, pos, result.element == null ? EMagicElement.PHYSICAL : result.element,
							result.centerItem, result.extraItems, result.reagentItems, result.output);
					
					if (player instanceof ServerPlayerEntity) {
						RitualCriteriaTrigger.Instance.trigger((ServerPlayerEntity) player, ritual.getTitleKey());
					}
					
					return true;
				}
			}
		}
		
		return false;
	}
}
