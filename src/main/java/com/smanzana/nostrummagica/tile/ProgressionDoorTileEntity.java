package com.smanzana.nostrummagica.tile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.dungeon.ProgressionDoorBlock;
import com.smanzana.nostrummagica.capabilities.EMagicTier;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.component.SpellComponentWrapper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ProgressionDoorTileEntity extends BlockEntity {
	
	private Set<SpellComponentWrapper> requiredComponents;
	private int requiredLevel;
	private EMagicTier requiredTier;
	
	public ProgressionDoorTileEntity(BlockPos pos, BlockState state) {
		super(NostrumTileEntities.ProgressionDoorTileEntityType, pos, state);
		
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
	
	public boolean meetsRequirements(LivingEntity entity, List<Component> missingDepStrings) {
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
					missingDepStrings.add(new TranslatableComponent("info.door.missing.error"));
			} else if (this.requiredLevel > 0 && !attr.isUnlocked()) {
				if (missingDepStrings != null)
					missingDepStrings.add(new TranslatableComponent("info.door.missing.unlock"));
				meets = false;
			} else {
				if (this.requiredLevel > 0 && attr.getLevel() < this.requiredLevel) {
					if (missingDepStrings != null)
						missingDepStrings.add(new TranslatableComponent("info.door.missing.level", this.requiredLevel));
					meets = false;
				}
				if (this.requiredTier != EMagicTier.LOCKED && !attr.getTier().isGreaterOrEqual(this.requiredTier)) {
					if (missingDepStrings != null)
						missingDepStrings.add(new TranslatableComponent("info.door.missing.tier", this.requiredTier.getName()));
					meets = false;
				}
				for (SpellComponentWrapper comp : this.requiredComponents) {
					if (comp.isShape()) {
						if (!attr.getShapes().contains(comp.getShape())) {
							if (missingDepStrings != null)
								missingDepStrings.add(new TranslatableComponent("info.door.missing.shape", comp.getShape().getDisplayName()));
							meets = false;
						}
					} else if (comp.isElement()) {
						Boolean known = attr.getKnownElements().get(comp.getElement());
						if (known == null || !known) {
							if (missingDepStrings != null)
								missingDepStrings.add(new TranslatableComponent("info.door.missing.element", comp.getElement().getDisplayName()));
							meets = false;
						}
					} else if (comp.isAlteration()) {
						Boolean known = attr.getAlterations().get(comp.getAlteration());
						if (known == null || !known) {
							if (missingDepStrings != null)
								missingDepStrings.add(new TranslatableComponent("info.door.missing.alteration", comp.getAlteration().getDisplayName()));
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
	public void load(CompoundTag compound) {
		super.load(compound);
		
		this.requiredLevel = compound.getInt(NBT_LEVEL);
		this.requiredTier = EMagicTier.FromNBT(compound.get(NBT_TIER));
		this.requiredComponents.clear();
		
		ListTag list = compound.getList(NBT_COMPS, Tag.TAG_STRING);
		int count = list.size();
		for (int i = 0; i < count; i++) {
			this.requiredComponents.add(SpellComponentWrapper.fromKeyString(list.getString(i)));
		}
	}
	
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		if (this.requiredLevel > 0)
			nbt.putInt(NBT_LEVEL, this.requiredLevel);
		
		nbt.put(NBT_TIER, this.requiredTier.toNBT());
		
		if (!this.requiredComponents.isEmpty()) {
			ListTag list = new ListTag();
			for (SpellComponentWrapper comp : this.requiredComponents) {
				list.add(StringTag.valueOf(comp.getKeyString()));
			}
			nbt.put(NBT_COMPS, list);
		}
	}
	
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
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