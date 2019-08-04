package com.smanzana.nostrummagica.blocks;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.MasteryOrb;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.ChainShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.trials.ShrineTrial;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ShrineBlock extends SymbolBlock {
	
	public static final String ID = "shrine_block";
	private static final PropertyBool EXHAUSTED = PropertyBool.create("exhausted");
	
	private static ShrineBlock instance = null;
	public static ShrineBlock instance() {
		if (instance == null)
			instance = new ShrineBlock();
		
		return instance;
	}
	
	public ShrineBlock() {
		super();
		this.setDefaultState(this.blockState.getBaseState().withProperty(EXHAUSTED, false));
	}
	
	public boolean isExhausted(IBlockState state) {
		return state.getValue(EXHAUSTED);
	}
	
	public IBlockState getExhaustedState(boolean exhausted) {
		return this.getDefaultState().withProperty(EXHAUSTED, true);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, EXHAUSTED);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		boolean bool = ((meta & 0x1) == 1);
		return getDefaultState().withProperty(EXHAUSTED, bool);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(EXHAUSTED) ? 1 : 0);
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
	@SideOnly(Side.CLIENT)
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		if (hand != EnumHand.MAIN_HAND) {
			return true;
		}
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof SymbolTileEntity))
			return false;
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
		if (attr == null)
			return false;
		
		
		
		SymbolTileEntity tile = (SymbolTileEntity) te;
		SpellComponentWrapper component = tile.getComponent();
		
		// Check for binding first
		if (attr.isBinding()) {
			if (attr.getBindingComponent().equals(component)) {
				attr.completeBinding(null);
				return true;
			}
		}
		
		if (component.isElement()) {
			// Elements either grant knowledge (if the player hasn't unlocked
			// magic yet) OR start a trial/advance mastery
			attr.learnElement(component.getElement());
			if (!attr.isUnlocked()) {
				return true;
			}

			// Make sure we have an orb first
			if (heldItem == null || !(heldItem.getItem() instanceof MasteryOrb)) {
				return false;
			}
			
			ShrineTrial trial = ShrineTrial.getTrial(component.getElement());
			if (trial == null) {
				NostrumMagica.logger.error("No trial found for element " + component.getElement().name());
				return false;
			} else {
				if (trial.canTake(playerIn, attr)) {
					trial.start(playerIn, attr);
					heldItem.splitStack(1);
				}
				
				return true;
			}
		}
		
		if (component.isTrigger()) {
			if (attr.getTriggers().contains(component.getTrigger()))
				return false;
			
			if (isExhausted(state)) {
				if (playerIn.worldObj.isRemote) {
					playerIn.addChatComponentMessage(new TextComponentTranslation("info.shrine.exhausted", new Object[0]));
				}
				return true;
			}
			
			attr.addTrigger(component.getTrigger());
			worldIn.setBlockState(pos, getExhaustedState(true));
			SymbolTileEntity ent = (SymbolTileEntity) worldIn.getTileEntity(pos);
			ent.setComponent(component);
			
			if (playerIn.worldObj.isRemote) {
				playerIn.addChatComponentMessage(new TextComponentTranslation("info.shrine.trigger", new Object[] {component.getTrigger().getDisplayName()}));
			}
		}
		
		if (component.isShape()) {
			boolean pass = false;
			if (component.getShape() instanceof SingleShape) {
				pass = true;
			}
			
			if (heldItem != null && heldItem.getItem() instanceof SpellScroll) {
				Spell spell = SpellScroll.getSpell(heldItem);
				if (spell != null) {
					// What we require depends on the shape
					if (component.getShape() instanceof AoEShape) {
						boolean speed, leap;
						speed = leap = false;
						for (SpellPart part : spell.getSpellParts()) {
							if (part.isTrigger())
								continue;
							if (!(part.getShape() instanceof SingleShape))
								continue;
							
							if (part.getAlteration() == null)
								continue;
							
							if (!speed
									&& part.getElement() == EMagicElement.WIND
									&& part.getAlteration() == EAlteration.SUPPORT) {
								speed = true;
								continue;
							}
							
							if (!leap
									&& part.getElement() == EMagicElement.LIGHTNING
									&& part.getAlteration() == EAlteration.GROWTH) {
								leap = true;
								continue;
							}
						}
						
						if (speed && leap)
							pass = true;
					} else if (component.getShape() instanceof ChainShape) {
						boolean lightning, blind;
						lightning = blind = false;
						for (SpellPart part : spell.getSpellParts()) {
							if (part.isTrigger())
								continue;
							if (!(part.getShape() instanceof SingleShape))
								continue;
							
							if (!lightning
									&& part.getElement() == EMagicElement.LIGHTNING
									&& part.getAlteration() == null
									&& part.getElementCount() >= 2) {
								lightning = true;
								continue;
							}
							
							if (!blind
									&& part.getElement() == EMagicElement.ENDER
									&& part.getAlteration() == EAlteration.INFLICT) {
								blind = true;
								continue;
							}
						}
						
						if (lightning && blind )
							pass = true;
					}
				}
			}
			
			if (pass && !attr.getShapes().contains(component.getShape())) {
				attr.addShape(component.getShape());
				if (playerIn.worldObj.isRemote) {
					playerIn.addChatComponentMessage(new TextComponentTranslation("info.shrine.shape", new Object[] {component.getShape().getDisplayName()}));
				}
				
				if (!(component.getShape() instanceof SingleShape)) {
					playerIn.setHeldItem(hand, null);
				}
			} else if (!pass) {
				// Shape that we haven't correctly unlocked yet
				if (playerIn.worldObj.isRemote) {
					String suffix = "";
					if (component.getShape() instanceof AoEShape) {
						suffix = "aoe";
					} else if (component.getShape() instanceof ChainShape) {
						suffix = "chain";
					}
					
					TextComponentTranslation trans = new TextComponentTranslation("info.shapehint.preamble", new Object[0]);
					trans.getStyle().setColor(TextFormatting.DARK_GRAY);
					playerIn.addChatComponentMessage(trans);
					
					trans = new TextComponentTranslation("info.shapehint." + suffix, new Object[0]);
					trans.getStyle().setColor(TextFormatting.LIGHT_PURPLE);
					playerIn.addChatComponentMessage(trans);
				}
			}
		}
		
		return false;
	}
}
