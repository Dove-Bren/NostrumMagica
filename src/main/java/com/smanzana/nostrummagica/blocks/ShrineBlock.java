package com.smanzana.nostrummagica.blocks;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ShrineBlock extends SymbolBlock {
	
	private static interface ElementTrial {
		
		public boolean canTake(EntityPlayer entityPlayer, INostrumMagic attr);
		
		public void start(EntityPlayer entityPlayer, INostrumMagic attr);
		
	}
	
	public static final String ID = "shrine_block";
	
	private static ShrineBlock instance = null;
	public static ShrineBlock instance() {
		if (instance == null)
			instance = new ShrineBlock();
		
		return instance;
	}
	
	public ShrineBlock() {
		super();
	}
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return false;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		SymbolTileEntity ent = new SymbolTileEntity(1.0f);
		
		return ent;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof SymbolTileEntity))
			return false;
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
		if (attr == null)
			return false;
		
		SymbolTileEntity tile = (SymbolTileEntity) te;
		SpellComponentWrapper component = tile.getComponent();
		
		if (component.isElement()) {
			// Elements either grant knowledge (if the player hasn't unlocked
			// magic yet) OR start a trial (if the player has unlocked and the
			// specific trial requirements have been met)
			if (!attr.isUnlocked()) {
				attr.learnElement(component.getElement());
				return true;
			}
			
			if (!elementTrials.containsKey(component.getElement())) {
				NostrumMagica.logger.error("No trial found for element " + component.getElement().name());
				return false;
			} else {
				if (elementTrials.get(component.getElement()).canTake(playerIn, attr))
					elementTrials.get(component.getElement()).start(playerIn, attr);
				
				return true;
			}
		}
		
		
		
		return false;
	}
	
	private static Map<EMagicElement, ElementTrial> elementTrials = new EnumMap<>(EMagicElement.class);
}
