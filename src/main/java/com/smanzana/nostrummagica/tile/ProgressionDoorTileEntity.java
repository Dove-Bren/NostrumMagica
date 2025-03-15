package com.smanzana.nostrummagica.tile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.dungeon.ProgressionDoorBlock;
import com.smanzana.nostrummagica.capabilities.EMagicTier;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.component.SpellComponentWrapper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants.NBT;

public class ProgressionDoorTileEntity extends TileEntity {
	
	private Set<SpellComponentWrapper> requiredComponents;
	private int requiredLevel;
	private EMagicTier requiredTier;
	
	public ProgressionDoorTileEntity() {
		super(NostrumTileEntities.ProgressionDoorTileEntityType);
		
		requiredComponents = new HashSet<>();
		requiredLevel = 0;
		this.requiredTier = EMagicTier.LOCKED;
	}
	
	public ProgressionDoorTileEntity require(SpellComponentWrapper component) {
		requiredComponents.add(component);
		this.dirty();
		return this;
	}
	
	public ProgressionDoorTileEntity level(int level) {
		this.requiredLevel = level;
		this.dirty();
		return this;
	}
	
	public ProgressionDoorTileEntity tier(EMagicTier tier) {
		this.requiredTier = tier;
		this.dirty();
		return this;
	}
	
	public Set<SpellComponentWrapper> getRequiredComponents() {
		return this.requiredComponents;
	}
	
	public int getRequiredLevel() {
		return this.requiredLevel;
	}
	
	public EMagicTier getRequiredTier() {
		return this.requiredTier;
	}
	
	public boolean meetsRequirements(LivingEntity entity, List<ITextComponent> missingDepStrings) {
		boolean meets = true;
		
		if (!entity.level.isClientSide) {
			NostrumMagica.logger.info("Checking requirements: lvl [" + this.requiredLevel + "], tier [" + requiredTier + "], components: " + this.requiredComponents.size());
		}
		
		// Early out if no reqs, so we can have fluff doors!
		if (this.requiredComponents.isEmpty() && this.requiredLevel <= 0 && this.requiredTier == EMagicTier.LOCKED) {
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
				if (this.requiredTier != EMagicTier.LOCKED && !attr.getTier().isGreaterOrEqual(this.requiredTier)) {
					if (missingDepStrings != null)
						missingDepStrings.add(new TranslationTextComponent("info.door.missing.tier", this.requiredTier.getName()));
					meets = false;
				}
				for (SpellComponentWrapper comp : this.requiredComponents) {
					if (comp.isShape()) {
						if (!attr.getShapes().contains(comp.getShape())) {
							if (missingDepStrings != null)
								missingDepStrings.add(new TranslationTextComponent("info.door.missing.shape", comp.getShape().getDisplayName()));
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
	private static final String NBT_TIER = "tier_requirement";
	private static final String NBT_COMPS = "required_componenets";
	
	@Override
	public void load(BlockState state, CompoundNBT compound) {
		super.load(state, compound);
		
		this.requiredLevel = compound.getInt(NBT_LEVEL);
		this.requiredTier = EMagicTier.FromNBT(compound.get(NBT_TIER));
		this.requiredComponents.clear();
		
		ListNBT list = compound.getList(NBT_COMPS, NBT.TAG_STRING);
		int count = list.size();
		for (int i = 0; i < count; i++) {
			this.requiredComponents.add(SpellComponentWrapper.fromKeyString(list.getString(i)));
		}
	}
	
	@Override
	public CompoundNBT save(CompoundNBT compound) {
		CompoundNBT nbt = super.save(compound);
		
		if (this.requiredLevel > 0)
			nbt.putInt(NBT_LEVEL, this.requiredLevel);
		
		nbt.put(NBT_TIER, this.requiredTier.toNBT());
		
		if (!this.requiredComponents.isEmpty()) {
			ListNBT list = new ListNBT();
			for (SpellComponentWrapper comp : this.requiredComponents) {
				list.add(StringNBT.valueOf(comp.getKeyString()));
			}
			nbt.put(NBT_COMPS, list);
		}
		
		return nbt;
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.worldPosition, 3, this.getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return this.save(new CompoundNBT());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(this.getBlockState(), pkt.getTag());
	}
	
	protected void dirty() {
		level.sendBlockUpdated(worldPosition, this.level.getBlockState(worldPosition), this.level.getBlockState(worldPosition), 3);
		setChanged();
	}
	
	private Direction faceStash = null;
	public Direction getFace() {
		if (faceStash == null) {
			BlockState state = level.getBlockState(getBlockPos());
			faceStash = Direction.NORTH;
			if (state != null) {
				try {
					faceStash = state.getValue(ProgressionDoorBlock.FACING);
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
			bottomStash = ProgressionDoorBlock.FindBottomCenterPos(getLevel(), getBlockPos());
		}
		
		return bottomStash;
	}
}