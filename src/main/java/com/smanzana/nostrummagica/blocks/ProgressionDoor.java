package com.smanzana.nostrummagica.blocks;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class ProgressionDoor extends NostrumMagicDoor {

	public static final String ID = "progression_door";
	
	private static ProgressionDoor instance = null;
	public static ProgressionDoor instance() {
		if (instance == null)
			instance = new ProgressionDoor();
		
		return instance;
	}
	
	
	public ProgressionDoor() {
		super();
		this.setUnlocalizedName(ID);
		
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return this.isMaster(state);
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		if (!this.isMaster(state))
			return null;
		
		return new ProgressionDoorTileEntity();
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		BlockPos master = this.getMasterPos(worldIn, state, pos);
		if (master != null && worldIn.getTileEntity(master) != null) {
			List<ITextComponent> missingDepStrings = new LinkedList<>();
			if (!((ProgressionDoorTileEntity) worldIn.getTileEntity(master)).meetsRequirements(playerIn, missingDepStrings)) {
				// print stuff out!
			} else {
				this.clearDoor(worldIn, pos, state);
			}
		}
		
		return true;
	}
	
	public static class ProgressionDoorTileEntity extends TileEntity {
		
		private List<SpellComponentWrapper> requiredComponents;
		private int requiredLevel;
		
		public ProgressionDoorTileEntity() {
			super();
			
			requiredComponents = new LinkedList<>();
			requiredLevel = 0;
		}
		
		public ProgressionDoorTileEntity require(SpellComponentWrapper component) {
			requiredComponents.add(component);
			return this;
		}
		
		public ProgressionDoorTileEntity level(int level) {
			this.requiredLevel = level;
			return this;
		}
		
		public List<SpellComponentWrapper> getRequiredComponents() {
			return this.requiredComponents;
		}
		
		public int getRequiredLevel() {
			return this.requiredLevel;
		}
		
		public boolean meetsRequirements(EntityLivingBase entity, List<ITextComponent> missingDepStrings) {
			boolean meets = true;
			
			// Early out if no reqs, so we can have fluff doors!
			if (this.requiredComponents.isEmpty() && this.requiredLevel <= 0) {
				;
			} else {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
				
				if (attr == null || !attr.isUnlocked()) {
					missingDepStrings.add(new TextComponentTranslation("info.door.missing.unlock"));
					meets = false;
				} else {
					if (this.requiredLevel > 0 && attr.getLevel() < this.requiredLevel) {
						missingDepStrings.add(new TextComponentTranslation("info.door.missing.level", this.requiredLevel));
						meets = false;
					}
					for (SpellComponentWrapper comp : this.requiredComponents) {
						if (comp.isShape()) {
							if (!attr.getShapes().contains(comp.getShape())) {
								missingDepStrings.add(new TextComponentTranslation("info.door.missing.shape", comp.getShape().getDisplayName()));
								meets = false;
							}
						} else if (comp.isTrigger()) {
							if (!attr.getTriggers().contains(comp.getTrigger())) {
								missingDepStrings.add(new TextComponentTranslation("info.door.missing.trigger", comp.getTrigger().getDisplayName()));
								meets = false;
							}
						} else if (comp.isElement()) {
							Boolean known = attr.getKnownElements().get(comp.getElement());
							if (known == null || !known) {
								missingDepStrings.add(new TextComponentTranslation("info.door.missing.element", comp.getElement().getName()));
								meets = false;
							}
						} else if (comp.isAlteration()) {
							Boolean known = attr.getAlterations().get(comp.getAlteration());
							if (known == null || !known) {
								missingDepStrings.add(new TextComponentTranslation("info.door.missing.alteration", comp.getAlteration().getName()));
								meets = false;
							}
						}
					}
				}
			}
			
			return meets;
		}
	}
}
