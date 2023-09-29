package com.smanzana.nostrummagica.items;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.blocks.AltarBlock;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.rituals.RitualRegistry;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.tiles.AltarTileEntity;
import com.smanzana.nostrummagica.tiles.CandleTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * One for each element, except physical
 * @author Skyler
 *
 */
public class InfusedGemItem extends Item implements ILoreTagged {

	public static final String ID_PREFIX = "nostrum_gem_";
	public static final String MakeID(EMagicElement element) {
		return ID_PREFIX + element.getName().toLowerCase();
	}
	
	protected final EMagicElement element;
	
	public InfusedGemItem(EMagicElement element) {
		super(NostrumItems.PropLowStack());
		this.element = element;
	}
	
	/**
	 * Returns an itemstack containing the gem type specefied.
	 * If element is null, returns a basic gem.
	 * @param element
	 * @param count
	 * @return
	 */
	public static ItemStack getGem(EMagicElement element, int count) {
		if (element == null) {
			element = EMagicElement.PHYSICAL;
		}
		
		InfusedGemItem gem = null;
		switch (element) {
		case EARTH:
			gem = NostrumItems.infusedGemEarth;
			break;
		case ENDER:
			gem = NostrumItems.infusedGemEnder;
			break;
		case FIRE:
			gem = NostrumItems.infusedGemFire;
			break;
		case ICE:
			gem = NostrumItems.infusedGemIce;
			break;
		case LIGHTNING:
			gem = NostrumItems.infusedGemLightning;
			break;
		case PHYSICAL:
			gem = NostrumItems.infusedGemUnattuned;
			break;
		case WIND:
			gem = NostrumItems.infusedGemWind;
			break;
		}
		
		return new ItemStack(gem, count);
	}
	
//    public String getNameFromMeta(int meta) {
//    	String suffix = "basic";
//		
//    	EMagicElement type = getTypeFromMeta(meta);
//    	if (type != null)
//    		suffix = type.name().toLowerCase();
//    	
//		return suffix;
//    }
    
    @Override
	public String getLoreKey() {
		return "nostrum_infused_gem";
	}

	@Override
	public String getLoreDisplayName() {
		return "Infused Gems";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("By combining magical reagents with an enderpearl, you can create a Void Gem.", "The gem lacks any elemental affinity and otherwise seems useless.");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("By combining magical reagents with an enderpearl, you can create a Void Gem.", "Void gems alone are not very useful.", "In order to use them, they must be inbued with the power of an element.", "Perhaps an Alter rune would do the trick...");
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		//if (worldIn.isRemote)
			//return ActionResultType.SUCCESS;
		
		final PlayerEntity playerIn = context.getPlayer();
		final BlockPos pos = context.getPos();
		final @Nonnull ItemStack stack = context.getItem();
		final World worldIn = context.getWorld();
		BlockState state = worldIn.getBlockState(pos);
		if (state.getBlock() == null)
			return ActionResultType.PASS;
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (state.getBlock() instanceof Candle) {
			if (!(te instanceof CandleTileEntity))
				return ActionResultType.PASS;
			
 			if (RitualRegistry.attemptRitual(worldIn, pos, playerIn, element)) {
 				stack.shrink(1);
 			}
 			
			return ActionResultType.SUCCESS;
		} else if (state.getBlock() instanceof AltarBlock) {
			if (!(te instanceof AltarTileEntity))
				return ActionResultType.PASS;
			
			if (RitualRegistry.attemptRitual(worldIn, pos, playerIn, element)) {
				stack.shrink(1);
			}
			
			return ActionResultType.SUCCESS;
		}
		
        return ActionResultType.PASS;
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
}
