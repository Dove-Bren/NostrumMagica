package com.smanzana.nostrummagica.blocks.tiles;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.ProgressionDoor;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants.NBT;

public class ProgressionDoorTileEntity extends TileEntity {
	
	private Set<SpellComponentWrapper> requiredComponents;
	private int requiredLevel;
	
	public ProgressionDoorTileEntity() {
		super();
		
		requiredComponents = new HashSet<>();
		requiredLevel = 0;
	}
	
	public ProgressionDoorTileEntity require(SpellComponentWrapper component) {
		requiredComponents.add(component);
		this.dirty();
		return this;
	}
	
	public ProgressionDoorTileEntity level(int level) {
		this.requiredLevel = level;
		return this;
	}
	
	public Set<SpellComponentWrapper> getRequiredComponents() {
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
			
			if (attr == null) {
				meets = false;
				if (missingDepStrings != null)
					missingDepStrings.add(new TextComponentTranslation("info.door.missing.error"));
			} else if (this.requiredLevel > 0 && !attr.isUnlocked()) {
				if (missingDepStrings != null)
					missingDepStrings.add(new TextComponentTranslation("info.door.missing.unlock"));
				meets = false;
			} else {
				if (this.requiredLevel > 0 && attr.getLevel() < this.requiredLevel) {
					if (missingDepStrings != null)
						missingDepStrings.add(new TextComponentTranslation("info.door.missing.level", this.requiredLevel));
					meets = false;
				}
				for (SpellComponentWrapper comp : this.requiredComponents) {
					if (comp.isShape()) {
						if (!attr.getShapes().contains(comp.getShape())) {
							if (missingDepStrings != null)
								missingDepStrings.add(new TextComponentTranslation("info.door.missing.shape", comp.getShape().getDisplayName()));
							meets = false;
						}
					} else if (comp.isTrigger()) {
						if (!attr.getTriggers().contains(comp.getTrigger())) {
							if (missingDepStrings != null)
								missingDepStrings.add(new TextComponentTranslation("info.door.missing.trigger", comp.getTrigger().getDisplayName()));
							meets = false;
						}
					} else if (comp.isElement()) {
						Boolean known = attr.getKnownElements().get(comp.getElement());
						if (known == null || !known) {
							if (missingDepStrings != null)
								missingDepStrings.add(new TextComponentTranslation("info.door.missing.element", comp.getElement().getName()));
							meets = false;
						}
					} else if (comp.isAlteration()) {
						Boolean known = attr.getAlterations().get(comp.getAlteration());
						if (known == null || !known) {
							if (missingDepStrings != null)
								missingDepStrings.add(new TextComponentTranslation("info.door.missing.alteration", comp.getAlteration().getName()));
							meets = false;
						}
					}
				}
			}
		}
		
		return meets;
	}
	
	private static final String NBT_LEVEL = "level_requirement";
	private static final String NBT_COMPS = "required_componenets";
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		
		this.requiredLevel = compound.getInteger(NBT_LEVEL);
		this.requiredComponents.clear();
		
		NBTTagList list = compound.getTagList(NBT_COMPS, NBT.TAG_STRING);
		int count = list.tagCount();
		for (int i = 0; i < count; i++) {
			this.requiredComponents.add(SpellComponentWrapper.fromKeyString(list.getStringTagAt(i)));
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound nbt = super.writeToNBT(compound);
		
		if (this.requiredLevel > 0)
			nbt.setInteger(NBT_LEVEL, this.requiredLevel);
		
		if (!this.requiredComponents.isEmpty()) {
			NBTTagList list = new NBTTagList();
			for (SpellComponentWrapper comp : this.requiredComponents) {
				list.appendTag(new NBTTagString(comp.getKeyString()));
			}
			nbt.setTag(NBT_COMPS, list);
		}
		
		return nbt;
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	protected void dirty() {
		world.markBlockRangeForRenderUpdate(pos, pos);
		world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
		world.scheduleBlockUpdate(pos, this.getBlockType(),0,0);
		markDirty();
	}
	
	private EnumFacing faceStash = null;
	public EnumFacing getFace() {
		if (faceStash == null) {
			IBlockState state = world.getBlockState(getPos());
			faceStash = EnumFacing.NORTH;
			if (state != null) {
				try {
					faceStash = state.getValue(ProgressionDoor.FACING);
				} catch (Exception e) {
					NostrumMagica.logger.warn("Failed to get face for progression tile entity");
				}
			}
		}
		
		return faceStash;
	}
	
	private BlockPos bottomStash = null;
	public BlockPos getBottomCenterPos() {
		if (bottomStash == null) {
			// Master is at TE's pos... but is it the bottom block? And is it in center?
			
			// Find bottom
			MutableBlockPos cursor = new MutableBlockPos(this.getPos());
			cursor.move(EnumFacing.DOWN, 1);
			
			while (cursor.getY() >= 0) {
				IBlockState state = world.getBlockState(cursor);
				if (state == null || !(state.getBlock() instanceof ProgressionDoor))
					break;
				
				cursor.move(EnumFacing.DOWN);
			}
			
			// Move back to last good position
			cursor.move(EnumFacing.UP);
			BlockPos bottomPos = new BlockPos(cursor);
			
			// Now discover left and right
			// Right:
			while (true) {
				cursor.move(getFace().rotateY());
				IBlockState state = world.getBlockState(cursor);
				if (state == null || !(state.getBlock() instanceof ProgressionDoor))
					break;
			}
			
			// Move back
			cursor.move(getFace().rotateYCCW());
			BlockPos rightPos = new BlockPos(cursor);
			cursor.setPos(bottomPos);
			
			// Left
			while (true) {
				cursor.move(getFace().rotateYCCW());
				IBlockState state = world.getBlockState(cursor);
				if (state == null || !(state.getBlock() instanceof ProgressionDoor))
					break;
			}
			
			// Move back
			cursor.move(getFace().rotateY());
			BlockPos leftPos = new BlockPos(cursor);
			
			bottomStash = new BlockPos(
					.5 * (rightPos.getX() + leftPos.getX()),
					bottomPos.getY(),
					.5 * (rightPos.getZ() + leftPos.getZ()));
		}
		
		return bottomStash;
	}
}