package com.smanzana.nostrummagica.rituals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.AltarBlock;
import com.smanzana.nostrummagica.blocks.AltarBlock.AltarTileEntity;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.blocks.Candle.CandleTileEntity;
import com.smanzana.nostrummagica.blocks.ChalkBlock;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.rituals.outcomes.IRitualOutcome;
import com.smanzana.nostrummagica.rituals.requirements.IRitualRequirement;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class RitualRecipe {
	
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

	private EMagicElement element;
	private int tier;
	private ReagentType types[];
	private ItemStack centerItem;
	private ItemStack extraItems[];
	private IRitualOutcome hook;
	private IRitualRequirement req;
	private String titleKey;
	
	private ItemStack icon;
	
	public static RitualRecipe createTier1(String titleKey,
			ItemStack icon,
			EMagicElement element,
			ReagentType reagent,
			IRitualRequirement requirement,
			IRitualOutcome outcome) {
		RitualRecipe recipe = new RitualRecipe(titleKey, element, 0);
		
		recipe.types[0] = reagent;
		recipe.hook = outcome;
		recipe.req = requirement;
		recipe.icon = icon;
		
		return recipe;
	}
	
	public static RitualRecipe createTier2(String titleKey,
			ItemStack icon,
			EMagicElement element,
			ReagentType[] reagents,
			ItemStack center, 
			IRitualRequirement requirement,
			IRitualOutcome outcome) {
		RitualRecipe recipe = new RitualRecipe(titleKey, element, 1);
		
		for (int i = 0; i < 4 && i < reagents.length; i++) {
			recipe.types[i] = reagents[i];
		}
		
		recipe.centerItem = center;
		recipe.hook = outcome;
		recipe.req = requirement;
		recipe.icon = icon;
		
		return recipe;
	}
	
	public static RitualRecipe createTier3(String titleKey,
			ItemStack icon,
			EMagicElement element,
			ReagentType[] reagents,
			ItemStack center,
			ItemStack extras[],
			IRitualRequirement requirement,
			IRitualOutcome outcome) {
		RitualRecipe recipe = new RitualRecipe(titleKey, element, 2);
		
		for (int i = 0; i < 4 && i < reagents.length; i++) {
			recipe.types[i] = reagents[i];
		}
		recipe.centerItem = center;
		
		for (int i = 0; i < 4 && i < extras.length; i++) {
			recipe.extraItems[i] = extras[i];
		}

		recipe.hook = outcome;
		recipe.req = requirement;
		recipe.icon = icon;
		
		return recipe;
	}
	
	private RitualRecipe(String nameKey, EMagicElement element, int tier) {
		this.tier = tier;
		this.element = element;
		this.titleKey = nameKey;
		if (tier == 0)
			this.types = new ReagentType[1];
		else
			this.types = new ReagentType[4];
		if (tier == 2)
			this.extraItems = new ItemStack[4];
	}
	
	public boolean matches(EntityPlayer player, World world, BlockPos center, EMagicElement element) {
		if (element != this.element)
			return false;
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return false;
		
		if (this.req != null && !req.matches(player, attr))
			return false;
		
		if (tier > 0 && !(world.getBlockState(center).getBlock() instanceof AltarBlock))
			return false;
		
		if (tier == 0 && !(world.getBlockState(center).getBlock() instanceof Candle))
			return false;
		
		TileEntity centerTE = world.getTileEntity(center);
		if (centerTE == null)
			return false;
		
		if (tier == 0) {
			CandleTileEntity candle = (CandleTileEntity) centerTE;
			if (candle.getType() != types[0])
				return false;
		} else {
			// Check altars
			AltarTileEntity altar = (AltarTileEntity) centerTE;
			ItemStack stack = altar.getItem();
			if (stack == null)
				return false;
			if (!OreDictionary.itemMatches(centerItem, stack, false)) {
				return false;
			}
			TileEntity te;
			
			if (tier == 2) {
				List<ItemStack> items = new ArrayList<>(4);
				for (int x = -4; x <= 4; x+=4) {
					int diff = 4 - Math.abs(x);
					for (int z = -diff; z <= diff; z+=8) {
						te = world.getTileEntity(center.add(x, 0, z));
						if (te == null || !(te instanceof AltarTileEntity))
							return false;
						altar = (AltarTileEntity) te;
						if (altar.getItem() != null)
							items.add(altar.getItem());
					}
				}
				
				for (ItemStack req : extraItems) {
					if (req == null)
						continue;
					
					Iterator<ItemStack> it = items.iterator();
					boolean found = false;
					while (it.hasNext()) {
						ItemStack next = it.next();
						if (OreDictionary.itemMatches(req, next, false)) {
							it.remove();
							found = true;
							break;
						}
					}
					if (!found)
						return false;
				}
				if (items.size() > 0)
					return false; // More items on altars than in recipe
			}
			
			// get all candles. Must be a candle in all the spots.
			// then try to match reagent types with required ones
			List<ReagentType> reagents = new ArrayList<>(4);
			for (int x = -2; x <= 2; x += 4)
			for (int z = -2; z <= 2; z += 4) {
				te = world.getTileEntity(center.add(x, 0, z));
				if (te == null || !(te instanceof CandleTileEntity))
					return false;
				
				CandleTileEntity candle = (CandleTileEntity) te;
				reagents.add(candle.getType());
			}
			
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
				
				if (!found)
					return false;
			}
			
			if (reagents.size() > 0)
				return false;
		}
		
		// check chalk
		int[] xs = CHALK_XS[tier];
		int[] ys = CHALK_YS[tier];
		for (int index = 0; index < xs.length; index++) {
			IBlockState state = world.getBlockState(center.add(xs[index], 0, ys[index]));
			
			if (state == null || !(state.getBlock() instanceof ChalkBlock))
				return false;
		}
		
		return true;
			
	}
	
	public void perform(World world, EntityPlayer player, BlockPos center) {
		
		if (world.isRemote)
			return;
		
		ItemStack centerItem = null;
		ItemStack otherItems[] = null;
		
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
				((AltarTileEntity) te).setItem(null);
			}
			
			if (tier == 2) {
				otherItems = new ItemStack[4];
				int i = 0;
				for (int x = -4; x <= 4; x+=4) {
					int diff = 4 - Math.abs(x);
					for (int z = -diff; z <= diff; z+=8) {
						te = world.getTileEntity(center.add(x, 0, z));
						if (te == null || !(te instanceof AltarTileEntity))
							continue; // oh well, too late now!
						otherItems[i++] = ((AltarTileEntity) te).getItem();
						((AltarTileEntity) te).setItem(null);
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

	public ItemStack getCenterItem() {
		return centerItem;
	}

	public ItemStack[] getExtraItems() {
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

	public ItemStack getIcon() {
		return icon;
	}
	
}
