package com.smanzana.nostrummagica.entity;


import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.EMagicTier;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.tile.ShrineTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class ShrineTriggerEntity<E extends ShrineTileEntity<?>> extends TileProxyTriggerEntity<E> {
	
	protected static final String ID_BASE = "entity_shrine_ent_";
	
	protected ShrineTriggerEntity(EntityType<? extends ShrineTriggerEntity<E>> type, Level worldIn) {
		super(type, worldIn);
	}
	
	@Override
	protected BlockPos getCheckPos() {
		return this.blockPosition().below();
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
	}

	@Override
	protected boolean canBeHitBy(LivingEntity attacker) {
		return attacker instanceof Player && !isInvisibleTo((Player) attacker);
	}
	
	@Override
	public boolean isInvisibleTo(Player player) {
		if (this.getLinkedTileEntity() == null) {
			return true;
		}
		@Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		
		return !player.isCreative() && !this.getLinkedTileEntity().canPlayerSee(attr, player);
	}
	
	public int getHitCount() {
		if (this.getLinkedTileEntity() != null) {
			return this.getLinkedTileEntity().getHitCount();
		}
		return 0;
	}
	
	public int getMaxHitCount() {
		if (this.getLinkedTileEntity() != null) {
			return this.getLinkedTileEntity().getMaxHitCount();
		}
		return 1;
	}
	
	public static class Element extends ShrineTriggerEntity<ShrineTileEntity.Element> {
		public static final String ID = ID_BASE + "element";
		
		public Element(EntityType<? extends ShrineTriggerEntity<ShrineTileEntity.Element>> type, Level worldIn) {
			super(type, worldIn);
		}
		
		public EMagicElement getElement() {
			if (this.getLinkedTileEntity() != null) {
				return this.getLinkedTileEntity().getElement();
			}
			return EMagicElement.PHYSICAL;
		}
	}
	
	public static class Shape extends ShrineTriggerEntity<ShrineTileEntity.Shape> {
		public static final String ID = ID_BASE + "shape";
		
		public Shape(EntityType<? extends ShrineTriggerEntity<ShrineTileEntity.Shape>> type, Level worldIn) {
			super(type, worldIn);
		}
		
		protected final SpellShape getDefaultShape() {
			return SpellShape.getAllShapes().iterator().next();
		}
		
		public SpellShape getShape() {
			if (this.getLinkedTileEntity() != null) {
				return this.getLinkedTileEntity().getShape();
			}
			return getDefaultShape();
		}
	}
	
	public static class Alteration extends ShrineTriggerEntity<ShrineTileEntity.Alteration> {
		public static final String ID = ID_BASE + "alteration";
		
		public Alteration(EntityType<? extends ShrineTriggerEntity<ShrineTileEntity.Alteration>> type, Level worldIn) {
			super(type, worldIn);
		}
		
		public EAlteration getAlteration() {
			if (this.getLinkedTileEntity() != null) {
				return this.getLinkedTileEntity().getAlteration();
			}
			return EAlteration.INFLICT;
		}
	}
	
	public static class Tier extends ShrineTriggerEntity<ShrineTileEntity.Tier> {
		public static final String ID = ID_BASE + "tier";
		
		public Tier(EntityType<? extends ShrineTriggerEntity<ShrineTileEntity.Tier>> type, Level worldIn) {
			super(type, worldIn);
		}
		
		public EMagicTier getTier() {
			if (this.getLinkedTileEntity() != null) {
				return this.getLinkedTileEntity().getTier();
			}
			return EMagicTier.MANI;
		}
	}
}
