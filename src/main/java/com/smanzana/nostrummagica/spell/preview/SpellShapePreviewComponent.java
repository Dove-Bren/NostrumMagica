package com.smanzana.nostrummagica.spell.preview;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.util.Curves.ICurve3d;

import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;

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
	public static final Type<Box> BOX = new Type<>(NostrumMagica.Loc("spellshape.preview.box"));
	
	protected final Type<? extends SpellShapePreviewComponent> type;
	
	public SpellShapePreviewComponent(Type<? extends SpellShapePreviewComponent> type) {
		this.type = type;
	}
	
	public Type<? extends SpellShapePreviewComponent> getType() {
		return this.type;
	}
	
	public static class Position extends SpellShapePreviewComponent {

		protected final SpellLocation location;
		
		protected Position(Type<? extends Position> type, SpellLocation location) {
			super(type);
			this.location = location;
		}
		
		public Position(SpellLocation location) {
			this(BLOCKPOS, location);
		}
		
		public SpellLocation getLocation() {
			return this.location;
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

		protected final Vec3 start;
		protected final Vec3 end;
		
		protected Span(Type<? extends Span> type, Vec3 start, Vec3 end) {
			super(type);
			this.start = start;
			this.end = end;
		}
		
		public Vec3 getStart() {
			return this.start;
		}
		
		public Vec3 getEnd() {
			return this.end;
		}
	}
	
	public static class Line extends Span {
		
		protected Line(Type<? extends Line> type, Vec3 start, Vec3 end) {
			super(type, start, end);
		}
		
		public Line(Vec3 start, Vec3 end) {
			this(LINE, start, end);
		}
	}
	
	public static class AoELine extends Line {
		
		protected final float width;
		
		protected AoELine(Type<? extends AoELine> type, Vec3 start, Vec3 end, float width) {
			super(type, start, end);
			this.width = width;
		}
		
		public AoELine(Vec3 start, Vec3 end, float width) {
			this(AOE_LINE, start, end, width);
		}
		
		public float getWidth() {
			return this.width;
		}
	}
	
	public static class Curve extends Line {
		
		protected final ICurve3d curve;
		
		protected Curve(Type<? extends Curve> type, Vec3 start, Vec3 end, ICurve3d curve) {
			super(type, start, end);
			this.curve = curve;
		}
		
		public Curve(Vec3 start, Vec3 end, ICurve3d curve) {
			this(CURVE, start, end, curve);
		}
		
		public ICurve3d getCurve() {
			return this.curve;
		}
	}
	
	public static class Disk extends SpellShapePreviewComponent {

		protected final Vec3 start;
		protected final float radius;
		
		protected Disk(Type<? extends Disk> type, Vec3 start, float radius) {
			super(type);
			this.start = start;
			this.radius = radius;
		}
		
		public Disk(Vec3 start, float radius) {
			this(DISK, start, radius);
		}
		
		public Vec3 getStart() {
			return this.start;
		}
		
		public float getRadius() {
			return this.radius;
		}
	}
	
	public static class Box extends Span {

		protected Box(Type<? extends Span> type, Vec3 boundsMin, Vec3 boundsMax) {
			super(type, boundsMin, boundsMax);
		}

		protected Box(Type<? extends Span> type, BoundingBox bounds) {
			super(type, new Vec3(bounds.minX(), bounds.minY(), bounds.minZ()), new Vec3(bounds.maxX(), bounds.maxY(), bounds.maxZ()));
		}
		
		public Box(Vec3 boundsMin, Vec3 boundsMax) {
			this(BOX, boundsMin, boundsMax);
		}
		
		public Box(BoundingBox bounds) {
			this(BOX, bounds);
		}
	}
}