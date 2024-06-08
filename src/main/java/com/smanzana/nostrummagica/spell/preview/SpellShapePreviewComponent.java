package com.smanzana.nostrummagica.spell.preview;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.util.Curves.ICurve3d;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public abstract class SpellShapePreviewComponent {
	
	public static final class Type<T extends SpellShapePreviewComponent> {
		protected final ResourceLocation name;
		public Type(ResourceLocation name) {
			this.name = name;
		}
	}
	
	public static final Type<Position> BLOCKPOS = new Type<>(NostrumMagica.Loc("spellshape.preview.blockpos"));
	public static final Type<Ent> ENTITY = new Type<>(NostrumMagica.Loc("spellshape.preview.entity"));
	public static final Type<Line> LINE = new Type<>(NostrumMagica.Loc("spellshape.preview.line"));
	public static final Type<AoELine> AOE_LINE = new Type<>(NostrumMagica.Loc("spellshape.preview.aoeline"));
	public static final Type<Curve> CURVE = new Type<>(NostrumMagica.Loc("spellshape.preview.curve"));
	public static final Type<Disk> DISK = new Type<>(NostrumMagica.Loc("spellshape.preview.disk"));
	
	protected final Type<? extends SpellShapePreviewComponent> type;
	
	public SpellShapePreviewComponent(Type<? extends SpellShapePreviewComponent> type) {
		this.type = type;
	}
	
	public Type<? extends SpellShapePreviewComponent> getType() {
		return this.type;
	}
	
	public static class Position extends SpellShapePreviewComponent {

		protected final BlockPos blockPos;
		
		protected Position(Type<? extends Position> type, BlockPos pos) {
			super(type);
			this.blockPos = pos.toImmutable();
		}
		
		public Position(BlockPos pos) {
			this(BLOCKPOS, pos);
		}
		
		public BlockPos getPos() {
			return this.blockPos;
		}
	}
	
	public static class Ent extends SpellShapePreviewComponent {
		protected final Entity entity;
		protected Ent(Type<? extends Ent> type, Entity entity) {
			super(type);
			this.entity = entity;
		}
		
		public Ent(Entity entity) {
			this(ENTITY, entity);
		}
		
		public Entity getEntity() {
			return this.entity;
		}
	}
	
	public abstract static class Span extends SpellShapePreviewComponent {

		protected final Vector3d start;
		protected final Vector3d end;
		
		protected Span(Type<? extends Span> type, Vector3d start, Vector3d end) {
			super(type);
			this.start = start;
			this.end = end;
		}
		
		public Vector3d getStart() {
			return this.start;
		}
		
		public Vector3d getEnd() {
			return this.end;
		}
	}
	
	public static class Line extends Span {
		
		protected Line(Type<? extends Line> type, Vector3d start, Vector3d end) {
			super(type, start, end);
		}
		
		public Line(Vector3d start, Vector3d end) {
			this(LINE, start, end);
		}
	}
	
	public static class AoELine extends Line {
		
		protected final float width;
		
		protected AoELine(Type<? extends AoELine> type, Vector3d start, Vector3d end, float width) {
			super(type, start, end);
			this.width = width;
		}
		
		public AoELine(Vector3d start, Vector3d end, float width) {
			this(AOE_LINE, start, end, width);
		}
		
		public float getWidth() {
			return this.width;
		}
	}
	
	public static class Curve extends Line {
		
		protected final ICurve3d curve;
		
		protected Curve(Type<? extends Curve> type, Vector3d start, Vector3d end, ICurve3d curve) {
			super(type, start, end);
			this.curve = curve;
		}
		
		public Curve(Vector3d start, Vector3d end, ICurve3d curve) {
			this(CURVE, start, end, curve);
		}
		
		public ICurve3d getCurve() {
			return this.curve;
		}
	}
	
	public static class Disk extends SpellShapePreviewComponent {

		protected final Vector3d start;
		protected final float radius;
		
		protected Disk(Type<? extends Disk> type, Vector3d start, float radius) {
			super(type);
			this.start = start;
			this.radius = radius;
		}
		
		public Disk(Vector3d start, float radius) {
			this(DISK, start, radius);
		}
		
		public Vector3d getStart() {
			return this.start;
		}
		
		public float getRadius() {
			return this.radius;
		}
	}
}