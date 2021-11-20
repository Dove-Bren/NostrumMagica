package com.smanzana.nostrummagica.rituals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.AltarBlock;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.blocks.ChalkBlock;
import com.smanzana.nostrummagica.blocks.tiles.AltarTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.CandleTileEntity;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenIndexed;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.rituals.outcomes.IItemRitualOutcome;
import com.smanzana.nostrummagica.rituals.outcomes.IRitualOutcome;
import com.smanzana.nostrummagica.rituals.requirements.IRitualRequirement;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class RitualRecipe implements InfoScreenIndexed {
	
	public static final class RitualMatchInfo {
		final boolean matched;
		@Nonnull final ItemStack center;
		@Nullable final NonNullList<ItemStack> extras;
		@Nonnull final ItemStack output;
		@Nullable final ReagentType reagents[];
		final EMagicElement element;
		
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
	private @Nonnull ItemStack centerItem;
	private NonNullList<ItemStack> extraItems;
	private IRitualOutcome hook;
	private IRitualRequirement req;
	private final String titleKey;
	
	private @Nonnull ItemStack icon;
	
	public static RitualRecipe createTier1(String titleKey,
			@Nonnull ItemStack icon,
			EMagicElement element,
			ReagentType reagent,
			IRitualRequirement requirement,
			IRitualOutcome outcome) {
		RitualRecipe recipe = new RitualRecipe(titleKey, element, 0);
		
		recipe.types[0] = reagent;
		recipe.hook = outcome;
		recipe.req = requirement;
		recipe.icon = icon;
		
		Validate.notNull(icon);
		
		return recipe;
	}
	
	public static RitualRecipe createTier2(String titleKey,
			@Nonnull ItemStack icon,
			EMagicElement element,
			ReagentType[] reagents,
			@Nonnull ItemStack center, 
			IRitualRequirement requirement,
			IRitualOutcome outcome) {
		if (center == null || center.isEmpty()) {
			throw new RuntimeException("Center item of tier 2 ritual cannot be empty!");
		}
		
		RitualRecipe recipe = new RitualRecipe(titleKey, element, 1);
		
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
	
	public static RitualRecipe createTier3(String titleKey,
			@Nonnull ItemStack icon,
			EMagicElement element,
			ReagentType[] reagents,
			@Nonnull ItemStack center,
			@Nonnull ItemStack extras[],
			IRitualRequirement requirement,
			IRitualOutcome outcome) {
		if (center == null || center.isEmpty()) {
			throw new RuntimeException("Center item of tier 3 ritual cannot be empty!");
		}
		
		RitualRecipe recipe = new RitualRecipe(titleKey, element, 2);
		
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
	
	private RitualRecipe(String nameKey, EMagicElement element, int tier) {
		this.tier = tier;
		this.element = element;
		this.titleKey = nameKey;
		if (tier == 0) {
			this.types = new ReagentType[1];
		} else {
			this.types = new ReagentType[4];
		}
		
		if (tier == 2) {
			this.extraItems = NonNullList.withSize(4, ItemStack.EMPTY);
		}
	}
	
	public RitualMatchInfo matches(EntityPlayer player, World world, BlockPos center, EMagicElement element) {
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
		
		if (this.req != null && !req.matches(player, attr))
			return RitualMatchInfo.Fail();
		
		if (tier > 0 && !(world.getBlockState(center).getBlock() instanceof AltarBlock))
			return RitualMatchInfo.Fail();
		
		if (tier == 0 && !(world.getBlockState(center).getBlock() instanceof Candle))
			return RitualMatchInfo.Fail();
		
		TileEntity centerTE = world.getTileEntity(center);
		if (centerTE == null)
			return RitualMatchInfo.Fail();
		
		ReagentType typesOut[] = null;
		ItemStack centerOut = ItemStack.EMPTY;
		NonNullList<ItemStack> extrasOut = null;
		if (tier == 0) {
			CandleTileEntity candle = (CandleTileEntity) centerTE;
			if (candle.getType() != types[0]) {
				return RitualMatchInfo.Fail();
			}
			
			typesOut = new ReagentType[] {candle.getType()};
		} else {
			// Check altars
			AltarTileEntity altar = (AltarTileEntity) centerTE;
			@Nonnull ItemStack stack = altar.getItem();
			if (stack.isEmpty())
				return RitualMatchInfo.Fail();
			if (!OreDictionary.itemMatches(centerItem, stack, false)) {
				return RitualMatchInfo.Fail();
			}
			centerOut = stack.copy();
			
			TileEntity te;
			
			if (tier == 2) {
				List<ItemStack> items = NonNullList.create();
				for (int x = -4; x <= 4; x+=4) {
					int diff = 4 - Math.abs(x);
					for (int z = -diff; z <= diff; z+=8) {
						te = world.getTileEntity(center.add(x, 0, z));
						if (te == null || !(te instanceof AltarTileEntity))
							return RitualMatchInfo.Fail();
						altar = (AltarTileEntity) te;
						if (!altar.getItem().isEmpty())
							items.add(altar.getItem());
					}
				}
				
				extrasOut = NonNullList.create();
				for (ItemStack req : extraItems) {
					if (req.isEmpty())
						continue;
					
					Iterator<ItemStack> it = items.iterator();
					boolean found = false;
					while (it.hasNext()) {
						ItemStack next = it.next();
						if (OreDictionary.itemMatches(req, next, false)) {
							extrasOut.add(next.copy());
							it.remove();
							found = true;
							break;
						}
					}
					if (!found)
						return RitualMatchInfo.Fail();
				}
				if (items.size() > 0)
					return RitualMatchInfo.Fail(); // More items on altars than in recipe
			}
			
			// get all candles. Must be a candle in all the spots.
			// then try to match reagent types with required ones
			List<ReagentType> reagents = new ArrayList<>(4);
			for (int x = -2; x <= 2; x += 4)
			for (int z = -2; z <= 2; z += 4) {
				te = world.getTileEntity(center.add(x, 0, z));
				if (te == null || !(te instanceof CandleTileEntity))
					return RitualMatchInfo.Fail();
				
				CandleTileEntity candle = (CandleTileEntity) te;
				reagents.add(candle.getType());
			}
			
			int i = 0;
			typesOut = new ReagentType[types.length];
			for (ReagentType req : types) {
				Iterator<ReagentType> it = reagents.iterator();
				boolean found = false;
				while (it.hasNext()) {
					ReagentType next = it.next();
					if (next == req) {
						it.remove();
						found = true;
						break;
					}
				}
				
				if (!found) {
					return RitualMatchInfo.Fail();
				} else {
					typesOut[i++] = req;
				}
			}
			
			if (reagents.size() > 0)
				return RitualMatchInfo.Fail();
		}
		
		// check chalk
		int[] xs = CHALK_XS[tier];
		int[] ys = CHALK_YS[tier];
		for (int index = 0; index < xs.length; index++) {
			IBlockState state = world.getBlockState(center.add(xs[index], 0, ys[index]));
			
			if (state == null || !(state.getBlock() instanceof ChalkBlock))
				return RitualMatchInfo.Fail();
		}
		
		final ItemStack outputOut = (this.getOutcome() instanceof IItemRitualOutcome
				? ((IItemRitualOutcome) this.getOutcome()).getResult().copy()
				: ItemStack.EMPTY
				);
		return new RitualMatchInfo(true, this.element, centerOut, extrasOut, outputOut, typesOut);
			
	}
	
	public void perform(World world, EntityPlayer player, BlockPos center) {
		
		if (world.isRemote)
			return;
		
		@Nonnull ItemStack centerItem = ItemStack.EMPTY;
		NonNullList<ItemStack> otherItems = null;
		
		// Do cleanup of altars and candles, etc
		if (tier == 0) {
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
				centerItem = ((AltarTileEntity) te).getItem();
				((AltarTileEntity) te).setItem(ItemStack.EMPTY);
			}
			
			if (tier == 2) {
				otherItems = NonNullList.create();
				for (int x = -4; x <= 4; x+=4) {
					int diff = 4 - Math.abs(x);
					for (int z = -diff; z <= diff; z+=8) {
						te = world.getTileEntity(center.add(x, 0, z));
						if (te == null || !(te instanceof AltarTileEntity))
							continue; // oh well, too late now!
						otherItems.add(((AltarTileEntity) te).getItem());
						((AltarTileEntity) te).setItem(ItemStack.EMPTY);
					}
				}
			}
		}

		if (hook != null)
			hook.perform(world, player, centerItem, otherItems, center, this);
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

	public @Nonnull ItemStack getCenterItem() {
		return centerItem;
	}

	public NonNullList<ItemStack> getExtraItems() {
		return extraItems;
	}
	
	public IRitualOutcome getOutcome() {
		return this.hook;
	}
	
	public IRitualRequirement getRequirement() {
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
