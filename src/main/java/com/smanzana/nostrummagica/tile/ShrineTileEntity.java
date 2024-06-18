package com.smanzana.nostrummagica.tile;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.block.dungeon.ShrineBlock;
import com.smanzana.nostrummagica.entity.EntityShrineTrigger;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class ShrineTileEntity<E extends EntityShrineTrigger<?>> extends TileEntity implements ITickableTileEntity {

	private E triggerEntity;
	
	protected ShrineTileEntity(TileEntityType<? extends ShrineTileEntity<E>> type) {
		super(type);
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.pos, 3, this.getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return this.write(new CompoundNBT());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(this.getBlockState(), pkt.getNbtCompound());
	}
	
	protected void dirty() {
		world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
		markDirty();
	}
	
	protected abstract E makeTriggerEntity(World world, double x, double y, double z);
	
	public @Nullable E getTriggerEntity() {
		return this.triggerEntity;
	}
	
	public void trigger(LivingEntity entity) {
		if (entity instanceof PlayerEntity) {
			// Just defer to block
			((ShrineBlock<?>) this.getBlockState().getBlock()).handleRelease(this.getWorld(), this.getPos(), this.getBlockState(), (PlayerEntity) entity);
		}
	}
	
	@Override
	public void tick() {
		if (world.isRemote) {
			return;
		}
		
		// Create entity here if it doesn't exist
		BlockPos blockUp = pos.up();
		if (triggerEntity == null || !triggerEntity.isAlive() || triggerEntity.world != this.world
				|| triggerEntity.getDistanceSq(blockUp.getX() + .5, blockUp.getY() + 1, blockUp.getZ() + .5) > 1.5) {
			// Entity is dead OR is too far away
			if (triggerEntity != null && !triggerEntity.isAlive()) {
				triggerEntity.remove();
			}
			
			triggerEntity = makeTriggerEntity(this.getWorld(), pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
			world.addEntity(triggerEntity);
		}
	}
	
	public static class Element extends ShrineTileEntity<EntityShrineTrigger.Element> {
		
		private static final String NBT_ELEMENT = "element";
		
		private EMagicElement element;
		
		public Element() {
			super(NostrumTileEntities.ElementShrineTileType);
			this.element = EMagicElement.PHYSICAL;
		}
		
		public EMagicElement getElement() {
			return this.element;
		}
		
		public void setElement(EMagicElement element) {
			this.element = element;
			this.dirty();
		}
		
		@Override
		public CompoundNBT write(CompoundNBT nbt) {
			nbt = super.write(nbt);
			
			nbt.put(NBT_ELEMENT, element.toNBT());
			
			return nbt;
		}
		
		@Override
		public void read(BlockState state, CompoundNBT nbt) {
			super.read(state, nbt);
			
			if (nbt.contains(NBT_ELEMENT)) {
				this.element = EMagicElement.FromNBT(nbt.get(NBT_ELEMENT));
			}
		}

		@Override
		protected EntityShrineTrigger.Element makeTriggerEntity(World world, double x, double y, double z) {
			EntityShrineTrigger.Element ent = new EntityShrineTrigger.Element(NostrumEntityTypes.elementShrine, world);
			ent.setPosition(x, y, z);
			return ent;
		}
	}
	
	public static class Alteration extends ShrineTileEntity<EntityShrineTrigger.Alteration> {
		
		private static final String NBT_ALTERATION = "alteration";
		
		private EAlteration alteration;
		
		public Alteration() {
			super(NostrumTileEntities.AlterationShrineTileType);
			this.alteration = EAlteration.INFLICT;
		}
		
		public EAlteration getAlteration() {
			return this.alteration;
		}
		
		public void setAlteration(EAlteration alteration) {
			this.alteration = alteration;
			this.dirty();
		}
		
		@Override
		public CompoundNBT write(CompoundNBT nbt) {
			nbt = super.write(nbt);
			
			nbt.put(NBT_ALTERATION, alteration.toNBT());
			
			return nbt;
		}
		
		@Override
		public void read(BlockState state, CompoundNBT nbt) {
			super.read(state, nbt);
			
			if (nbt.contains(NBT_ALTERATION)) {
				this.alteration = EAlteration.FromNBT(nbt.get(NBT_ALTERATION));
			}
		}

		@Override
		protected EntityShrineTrigger.Alteration makeTriggerEntity(World world, double x, double y, double z) {
			EntityShrineTrigger.Alteration ent = new EntityShrineTrigger.Alteration(NostrumEntityTypes.alterationShrine, world);
			ent.setPosition(x, y, z);
			return ent;
		}
	}
	
	public static class Shape extends ShrineTileEntity<EntityShrineTrigger.Shape> {
		
		private static final String NBT_SHAPE = "shape";
		
		private SpellShape shape;
		
		public Shape() {
			super(NostrumTileEntities.ShapeShrineTileType);
			this.shape = null;
		}
		
		public SpellShape getShape() {
			if (this.shape == null) {
				this.shape = SpellShape.getAllShapes().iterator().next();
			}
			return this.shape;
		}
		
		public void setShape(SpellShape shape) {
			this.shape = shape;
			this.dirty();
		}
		
		@Override
		public CompoundNBT write(CompoundNBT nbt) {
			nbt = super.write(nbt);
			
			if (getShape() != null) {
				nbt.putString(NBT_SHAPE, getShape().getShapeKey());
			}
			
			return nbt;
		}
		
		@Override
		public void read(BlockState state, CompoundNBT nbt) {
			super.read(state, nbt);
			
			if (nbt.contains(NBT_SHAPE)) {
				this.shape = SpellShape.get(nbt.getString(NBT_SHAPE));
			}
		}

		@Override
		protected EntityShrineTrigger.Shape makeTriggerEntity(World world, double x, double y, double z) {
			EntityShrineTrigger.Shape ent = new EntityShrineTrigger.Shape(NostrumEntityTypes.shapeShrine, world);
			ent.setPosition(x, y, z);
			return ent;
		}
	}
	
}