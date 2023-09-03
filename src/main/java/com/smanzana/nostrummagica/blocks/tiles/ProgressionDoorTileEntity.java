package com.smanzana.nostrummagica.blocks.tiles;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.ProgressionDoor;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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
	
	public boolean meetsRequirements(LivingEntity entity, List<ITextComponent> missingDepStrings) {
		boolean meets = true;
		
		if (!entity.world.isRemote) {
			NostrumMagica.logger.info("Checking requirements: lvl [" + this.requiredLevel + "], components: " + this.requiredComponents.size());
		}
		
		// Early out if no reqs, so we can have fluff doors!
		if (this.requiredComponents.isEmpty() && this.requiredLevel <= 0) {
			;
		} else {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
			
			if (attr == null) {
				meets = false;
				if (missingDepStrings != null)
					missingDepStrings.add(new TranslationTextComponent("info.door.missing.error"));
			} else if (this.requiredLevel > 0 && !attr.isUnlocked()) {
				if (missingDepStrings != null)
					missingDepStrings.add(new TranslationTextComponent("info.door.missing.unlock"));
				meets = false;
			} else {
				if (this.requiredLevel > 0 && attr.getLevel() < this.requiredLevel) {
					if (missingDepStrings != null)
						missingDepStrings.add(new TranslationTextComponent("info.door.missing.level", this.requiredLevel));
					meets = false;
				}
				for (SpellComponentWrapper comp : this.requiredComponents) {
					if (comp.isShape()) {
						if (!attr.getShapes().contains(comp.getShape())) {
							if (missingDepStrings != null)
								missingDepStrings.add(new TranslationTextComponent("info.door.missing.shape", comp.getShape().getDisplayName()));
							meets = false;
						}
					} else if (comp.isTrigger()) {
						if (!attr.getTriggers().contains(comp.getTrigger())) {
							if (missingDepStrings != null)
								missingDepStrings.add(new TranslationTextComponent("info.door.missing.trigger", comp.getTrigger().getDisplayName()));
							meets = false;
						}
					} else if (comp.isElement()) {
						Boolean known = attr.getKnownElements().get(comp.getElement());
						if (known == null || !known) {
							if (missingDepStrings != null)
								missingDepStrings.add(new TranslationTextComponent("info.door.missing.element", comp.getElement().getName()));
							meets = false;
						}
					} else if (comp.isAlteration()) {
						Boolean known = attr.getAlterations().get(comp.getAlteration());
						if (known == null || !known) {
							if (missingDepStrings != null)
								missingDepStrings.add(new TranslationTextComponent("info.door.missing.alteration", comp.getAlteration().getName()));
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
	public void readFromNBT(CompoundNBT compound) {
		super.readFromNBT(compound);
		
		this.requiredLevel = compound.getInt(NBT_LEVEL);
		this.requiredComponents.clear();
		
		ListNBT list = compound.getList(NBT_COMPS, NBT.TAG_STRING);
		int count = list.size();
		for (int i = 0; i < count; i++) {
			this.requiredComponents.add(SpellComponentWrapper.fromKeyString(list.getString(i)));
		}
	}
	
	@Override
	public CompoundNBT writeToNBT(CompoundNBT compound) {
		CompoundNBT nbt = super.writeToNBT(compound);
		
		if (this.requiredLevel > 0)
			nbt.putInt(NBT_LEVEL, this.requiredLevel);
		
		if (!this.requiredComponents.isEmpty()) {
			ListNBT list = new ListNBT();
			for (SpellComponentWrapper comp : this.requiredComponents) {
				list.add(new StringNBT(comp.getKeyString()));
			}
			nbt.put(NBT_COMPS, list);
		}
		
		return nbt;
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return this.writeToNBT(new CompoundNBT());
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
	
	private Direction faceStash = null;
	public Direction getFace() {
		if (faceStash == null) {
			BlockState state = world.getBlockState(getPos());
			faceStash = Direction.NORTH;
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
			cursor.move(Direction.DOWN, 1);
			
			while (cursor.getY() >= 0) {
				BlockState state = world.getBlockState(cursor);
				if (state == null || !(state.getBlock() instanceof ProgressionDoor))
					break;
				
				cursor.move(Direction.DOWN);
			}
			
			// Move back to last good position
			cursor.move(Direction.UP);
			BlockPos bottomPos = new BlockPos(cursor);
			
			// Now discover left and right
			// Right:
			while (true) {
				cursor.move(getFace().rotateY());
				BlockState state = world.getBlockState(cursor);
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
				BlockState state = world.getBlockState(cursor);
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