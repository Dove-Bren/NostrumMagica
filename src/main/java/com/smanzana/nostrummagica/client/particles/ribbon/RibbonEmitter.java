package com.smanzana.nostrummagica.client.particles.ribbon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.util.Color;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

// Emitter that creates segments as it lives and draws quads between them, forming a ribbon.
public class RibbonEmitter<T> {
	
	public static final class EmitterData {
		public Vec3 emitterPos;
		public float emitterAgeProgress; // 0-1f
		public int emitterAge; // Ticks alive
		public int emitterLifetime; // Ticks emitter will live for
		public Color emitterColor;
		
		public float partialTicks;
		
		public int segmentSpawnCount; // Number of segments spawned for duration of emitter
		public int segmentAliveCount; // Number of segments currently alive
		
		public double ribbonSpawnLength; // Total length of ribbon segment spawning locations. Does not account for segment motion!
		public double ribbonVisibleLength; // Total length of all segments still visible in the ribbon 
	}
	
	public static final class SegmentData {
		public Vector3f segmentLength; // Vector span of this segment. Also works as 'distance to next segment'
		public int segmentIdxFromTail; // Index relative to the tail of the ribbon. The last in the ribbon is idx 0 and head is [segmentAliveCount-1]
		
		public double ribbonLengthFromHead; // 'Length' in block units of the ribbon from the emitter to this segment
	}
	
	public static interface ISegment {
		public Vec3 getPosition(Camera camera, EmitterData emitter);
		public Vector3f getSpanDirection(Camera camera, EmitterData emitter, SegmentData ribbonData);
		public Color getColor(Camera camera, EmitterData emitter, SegmentData ribbonData);
		public float getWidth(Camera camera, EmitterData emitter, SegmentData ribbonData);
		public float getV(Camera camera, EmitterData emitter, SegmentData ribbonData);
		public float getU(Camera camera, EmitterData emitter, SegmentData ribbonData, boolean leftEdge);
		public default boolean canRemove(EmitterData emitter) {return false;}
	}
	
	@FunctionalInterface
	public static interface ISegmentSpawner<T> {
		public @Nullable ISegment makeSegment(T particle, ClientLevel worldIn, Vec3 spawnPos, EmitterData emitter, double distanceFromLast, float ticksFromLast);
	}

	protected final int fixedRandom;
	
	protected final T spawnerData;
	protected final ISegmentSpawner<T> spawner;
	protected final List<ISegment> segments;
	protected Vec3 lastSegmentPos;
	protected int lastSegmentTicks;
	protected float totalSegmentLength;

	protected final int lifetime;
	protected int age;
	protected final ClientLevel level;
	protected double x, xo;
	protected double y, yo;
	protected double z, zo;
	
	protected boolean active;

	// Stashing on one object instead of constantly calling new
	protected final EmitterData dataStorage;
	protected final SegmentData segmentStorage;
	
	public RibbonEmitter(ClientLevel level, double x, double y, double z, float red, float green, float blue, float alpha, int lifetime, final int fixedRandom, ISegmentSpawner<T> spawner, T spawnerData) {
		this.spawner = spawner;
		this.spawnerData = spawnerData;
		this.lifetime = lifetime;
		this.segments = new ArrayList<>();
		this.lastSegmentPos = new Vec3(x, y, z);
		this.lastSegmentTicks = 0;
		this.dataStorage = new EmitterData();
		this.dataStorage.emitterColor = new Color(red, green, blue, alpha);
		this.segmentStorage = new SegmentData();
		this.fixedRandom = fixedRandom;
		this.x = this.xo = x;
		this.y = this.yo = y;
		this.z = this.zo = z;
		this.level = level;
		this.active = true;
	}
	
	public RibbonEmitter(ClientLevel worldIn, double x, double y, double z, float red, float green, float blue, float alpha, int lifetime, ISegmentSpawner<T> spawner, T spawnerData) {
		this(worldIn, x, y, z, red, green, blue, alpha, lifetime, NostrumMagica.rand.nextInt(), spawner, spawnerData);
	}
	
	public void disable() {
		this.active = false;
	}
	
	protected void updateEmitterTickData() {
		updateEmitterSubtickData(0f);
	}
	
	protected void updateEmitterSubtickData(float partialTicks) {
		this.dataStorage.emitterPos = new Vec3(Mth.lerp(partialTicks, this.xo, this.x), Mth.lerp(partialTicks, this.yo, this.y), Mth.lerp(partialTicks, this.zo, this.z));
		this.dataStorage.emitterAgeProgress = Mth.clamp((this.age + partialTicks) / (float) this.lifetime, 0, 1);
		this.dataStorage.emitterAge = this.age;
		this.dataStorage.emitterLifetime = this.lifetime;
		// color is stored on storage directly and doesn't need to be copied
		// spawnCount ""
		
		// AliveCount is updated in render func directly
		this.dataStorage.ribbonSpawnLength = this.totalSegmentLength;
		
		// ribbonVisibleLength is updated in render func directly
		
		this.dataStorage.partialTicks = partialTicks;
	}
	
	public void tick(double x, double y, double z, double oldX, double oldY, double oldZ) {
		age++;
		
		this.xo = oldX;
		this.x = x;
		this.yo = oldY;
		this.y = y;
		this.zo = oldZ;
		this.z = z;
		
		updateEmitterTickData();
		cleanSegments();
	}
	
	public void tick() {
		tick(x, y, z, xo, yo, zo);
	}
	
	public boolean isAlive() {
		return !this.segments.isEmpty();
	}
	
	protected void cleanSegments() {
		Iterator<ISegment> it = this.segments.iterator();
		while (it.hasNext()) {
			ISegment seg = it.next();
			if (seg.canRemove(dataStorage)) {
				it.remove();
				dataStorage.segmentAliveCount--;
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	protected int getLightAt(ClientLevel level, Vec3 pos) {
		// Copied from particle#getLight
		BlockPos blockpos = new BlockPos(pos.x, pos.y, pos.z);
		return level.hasChunkAt(blockpos) ? LevelRenderer.getLightColor(level, blockpos) : 0;
	}
	
	protected void attemptNewSegment(float partialTicks) {
		final float renderAge = (this.age + partialTicks);
		final Vec3 pos = new Vec3(Mth.lerp(partialTicks, this.xo, this.x),
				Mth.lerp(partialTicks, this.yo, this.y),
				Mth.lerp(partialTicks, this.zo, this.z)
				);
		ISegment seg = spawner.makeSegment(this.spawnerData, level, pos, dataStorage, pos.distanceTo(lastSegmentPos), renderAge - lastSegmentTicks);
		if (seg != null) {
			this.segments.add(seg);
			this.totalSegmentLength += pos.distanceTo(lastSegmentPos);
			this.lastSegmentPos = pos;
			this.lastSegmentTicks = age;
			this.dataStorage.segmentSpawnCount++;
			this.dataStorage.segmentAliveCount++;
		}
	}
	
	public void render(@Nullable PoseStack matrixStack, VertexConsumer buffer, Camera camera, float partialTicks) {
		updateEmitterSubtickData(partialTicks);
		if (this.active) {
			// Adding in render so it's faster than 1/20th a second
			attemptNewSegment(partialTicks);
		}
		
		// Allow matrix stack to be passed in, OR take care of the entire particle rendering offset here
		if (matrixStack == null) {
			matrixStack = new PoseStack();
			
			Vec3 cameraPos = camera.getPosition();
			
			double offX = (float) -cameraPos.x();
			double offY = (float) -cameraPos.y();
			double offZ = (float) -cameraPos.z();
			matrixStack.translate(offX, offY, offZ);
		}
		
		matrixStack.pushPose();
		renderSegments(level, matrixStack, buffer, camera, partialTicks);
		matrixStack.popPose();
	}
	
	protected void renderSegments(ClientLevel level, PoseStack matrixStack, VertexConsumer buffer, Camera camera, float partialTicks) {
		if (this.segments.size() > 1) {
			Vec3 lastPos = dataStorage.emitterPos;
			double visibleLength = 0; // Calculate, then set to avoid giving partial info to render calls
			
			for (int i = segments.size() - 1; i >= 0; i--) {
				ISegment segment = segments.get(i);
				final Vec3 pos = segment.getPosition(camera, dataStorage);
				final Vector3f segmentLength = new Vector3f(pos.subtract(lastPos));
				final float segmentMagnitude = Mth.sqrt(Mth.square(segmentLength.x()) + Mth.square(segmentLength.y()) + Mth.square(segmentLength.z()));
				this.segmentStorage.ribbonLengthFromHead = visibleLength + (segmentMagnitude);
				this.segmentStorage.segmentLength = segmentLength;
				this.segmentStorage.segmentIdxFromTail = i;
				
				final int light = getLightAt(level, pos);
				
				matrixStack.pushPose();
				matrixStack.translate(pos.x, pos.y, pos.z);
				renderSegment(segment, this.segmentStorage, matrixStack, buffer, camera, partialTicks, light, i != 0, i < segments.size() - 1);
				matrixStack.popPose();
				
				if (lastPos != null) {
					visibleLength += segmentMagnitude;
				}
				lastPos = pos;
			}
			
			this.dataStorage.ribbonVisibleLength = visibleLength;
		}
	}
	
	protected void renderSegment(ISegment segment, SegmentData ribbonData, PoseStack matrixStack, VertexConsumer buffer, Camera camera, float partialTicks, int light, boolean start, boolean end) {
		
		final float v = segment.getV(camera, dataStorage, ribbonData);
		final float lowU = segment.getU(camera, dataStorage, ribbonData, true);
		final float highU = segment.getU(camera, dataStorage, ribbonData, false);
		final float width = segment.getWidth(camera, dataStorage, ribbonData);
		final Color color = segment.getColor(camera, dataStorage, ribbonData);
		final Vector3f bandVec = segment.getSpanDirection(camera, dataStorage, ribbonData);
		
		// mutate diff Vec to be normal
		final Vector3f normalVec = ribbonData.segmentLength.copy();
		normalVec.normalize();
		
		// For checkerboard testing, replace color with
		// ribbonData.segmentIdxFromTail % 2 == 0 ? new Color(1f, 1f, 1f, 1f) : new Color(0f, 0f, 0f, 1f) (or %2 == 1 for start)
		//
//		matrixStack.pushPose();
//		//matrixStack.mulPose(rotation);
//		if (end)
//			renderSegmentRaw(matrixStack, buffer, ribbonData.segmentIdxFromTail % 2 == 0 ? new Color(1f, 1f, 1f, 1f) : new Color(0f, 0f, 0f, 1f), lowU, highU, v, OverlayTexture.NO_OVERLAY, light, bandVec, normalVec, width, false);
//		if (start)
//			renderSegmentRaw(matrixStack, buffer, ribbonData.segmentIdxFromTail % 2 == 1 ? new Color(1f, 1f, 1f, 1f) : new Color(0f, 0f, 0f, 1f), lowU, highU, v, OverlayTexture.NO_OVERLAY, light, bandVec, normalVec, width, true);
//		
//		
//		matrixStack.popPose();
		
		matrixStack.pushPose();
		//matrixStack.mulPose(rotation);
		if (end)
			renderSegmentRaw(matrixStack, buffer, color, lowU, highU, v, OverlayTexture.NO_OVERLAY, light, bandVec, normalVec, width, false);
		if (start)
			renderSegmentRaw(matrixStack, buffer, color, lowU, highU, v, OverlayTexture.NO_OVERLAY, light, bandVec, normalVec, width, true);
		
		
		matrixStack.popPose();
	}
	
	// Assumes matrix stack is handling rotation
	protected void renderSegmentRaw(PoseStack matrixStack, VertexConsumer buffer, Color color, float lowU, float highU, float v, int overlay, int light, Vector3f bandDirection, Vector3f normalVec, float radius, boolean reverse) {
		final Matrix4f transform = matrixStack.last().pose();
		final Matrix3f normal = matrixStack.last().normal();
		bandDirection = bandDirection.copy();
		bandDirection.mul(radius);
		
		if (reverse) {
			buffer.vertex(transform, bandDirection.x(), bandDirection.y(), bandDirection.z()).color(color.red, color.green, color.blue, color.alpha).uv(highU, v).overlayCoords(overlay).uv2(light).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).endVertex();
			buffer.vertex(transform, -bandDirection.x(), -bandDirection.y(), -bandDirection.z()).color(color.red, color.green, color.blue, color.alpha).uv(lowU, v).overlayCoords(overlay).uv2(light).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).endVertex();
		} else {
			buffer.vertex(transform, -bandDirection.x(), -bandDirection.y(), -bandDirection.z()).color(color.red, color.green, color.blue, color.alpha).uv(lowU, v).overlayCoords(overlay).uv2(light).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).endVertex();
			buffer.vertex(transform, bandDirection.x(), bandDirection.y(), bandDirection.z()).color(color.red, color.green, color.blue, color.alpha).uv(highU, v).overlayCoords(overlay).uv2(light).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).endVertex();
		}
	}
}
