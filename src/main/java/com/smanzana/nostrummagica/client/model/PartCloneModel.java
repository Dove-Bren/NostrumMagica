package com.smanzana.nostrummagica.client.model;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Stream;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Dupe of vanilla's ModelPart but not final
 */
public class PartCloneModel {
	public float x;
	   public float y;
	   public float z;
	   public float xRot;
	   public float yRot;
	   public float zRot;
	   public boolean visible = true;
	   private final List<PartCloneModel.Cube> cubes;
	   private final Map<String, PartCloneModel> children;

	   public PartCloneModel(List<PartCloneModel.Cube> p_171306_, Map<String, PartCloneModel> p_171307_) {
		  this.cubes = p_171306_;
		  this.children = p_171307_;
	   }

	   public PartPose storePose() {
		  return PartPose.offsetAndRotation(this.x, this.y, this.z, this.xRot, this.yRot, this.zRot);
	   }

	   public void loadPose(PartPose p_171323_) {
		  this.x = p_171323_.x;
		  this.y = p_171323_.y;
		  this.z = p_171323_.z;
		  this.xRot = p_171323_.xRot;
		  this.yRot = p_171323_.yRot;
		  this.zRot = p_171323_.zRot;
	   }

	   public void copyFrom(PartCloneModel p_104316_) {
		  this.xRot = p_104316_.xRot;
		  this.yRot = p_104316_.yRot;
		  this.zRot = p_104316_.zRot;
		  this.x = p_104316_.x;
		  this.y = p_104316_.y;
		  this.z = p_104316_.z;
	   }
	   
	   // Begin Skyler Change
	   public void copyFrom(ModelPart p_104316_) {
		  this.xRot = p_104316_.xRot;
		  this.yRot = p_104316_.yRot;
		  this.zRot = p_104316_.zRot;
		  this.x = p_104316_.x;
		  this.y = p_104316_.y;
		  this.z = p_104316_.z;
	   }
	   // End Skyler Change

	   public PartCloneModel getChild(String p_171325_) {
		  PartCloneModel ModelPartClone = this.children.get(p_171325_);
		  if (ModelPartClone == null) {
			 throw new NoSuchElementException("Can't find part " + p_171325_);
		  } else {
			 return ModelPartClone;
		  }
	   }

	   public void setPos(float p_104228_, float p_104229_, float p_104230_) {
		  this.x = p_104228_;
		  this.y = p_104229_;
		  this.z = p_104230_;
	   }

	   public void setRotation(float p_171328_, float p_171329_, float p_171330_) {
		  this.xRot = p_171328_;
		  this.yRot = p_171329_;
		  this.zRot = p_171330_;
	   }

	   public void render(PoseStack p_104302_, VertexConsumer p_104303_, int p_104304_, int p_104305_) {
		  this.render(p_104302_, p_104303_, p_104304_, p_104305_, 1.0F, 1.0F, 1.0F, 1.0F);
	   }

	   public void render(PoseStack p_104307_, VertexConsumer p_104308_, int p_104309_, int p_104310_, float p_104311_, float p_104312_, float p_104313_, float p_104314_) {
		  if (this.visible) {
			 if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
				p_104307_.pushPose();
				this.translateAndRotate(p_104307_);
				this.compile(p_104307_.last(), p_104308_, p_104309_, p_104310_, p_104311_, p_104312_, p_104313_, p_104314_);

				for(PartCloneModel ModelPartClone : this.children.values()) {
				   ModelPartClone.render(p_104307_, p_104308_, p_104309_, p_104310_, p_104311_, p_104312_, p_104313_, p_104314_);
				}

				p_104307_.popPose();
			 }
		  }
	   }

	   public void visit(PoseStack p_171310_, PartCloneModel.Visitor p_171311_) {
		  this.visit(p_171310_, p_171311_, "");
	   }

	   private void visit(PoseStack p_171313_, PartCloneModel.Visitor p_171314_, String p_171315_) {
		  if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
			 p_171313_.pushPose();
			 this.translateAndRotate(p_171313_);
			 PoseStack.Pose posestack$pose = p_171313_.last();

			 for(int i = 0; i < this.cubes.size(); ++i) {
				p_171314_.visit(posestack$pose, p_171315_, i, this.cubes.get(i));
			 }

			 String s = p_171315_ + "/";
			 this.children.forEach((p_171320_, p_171321_) -> {
				p_171321_.visit(p_171313_, p_171314_, s + p_171320_);
			 });
			 p_171313_.popPose();
		  }
	   }

	   public void translateAndRotate(PoseStack p_104300_) {
		  p_104300_.translate((double)(this.x / 16.0F), (double)(this.y / 16.0F), (double)(this.z / 16.0F));
		  if (this.zRot != 0.0F) {
			 p_104300_.mulPose(Vector3f.ZP.rotation(this.zRot));
		  }

		  if (this.yRot != 0.0F) {
			 p_104300_.mulPose(Vector3f.YP.rotation(this.yRot));
		  }

		  if (this.xRot != 0.0F) {
			 p_104300_.mulPose(Vector3f.XP.rotation(this.xRot));
		  }

	   }

	   public void compile(PoseStack.Pose p_104291_, VertexConsumer p_104292_, int p_104293_, int p_104294_, float p_104295_, float p_104296_, float p_104297_, float p_104298_) {
		  for(PartCloneModel.Cube ModelPartClone$cube : this.cubes) {
			 ModelPartClone$cube.compile(p_104291_, p_104292_, p_104293_, p_104294_, p_104295_, p_104296_, p_104297_, p_104298_);
		  }

	   }

	   public PartCloneModel.Cube getRandomCube(Random p_104329_) {
		  return this.cubes.get(p_104329_.nextInt(this.cubes.size()));
	   }

	   public boolean isEmpty() {
		  return this.cubes.isEmpty();
	   }

	   public Stream<PartCloneModel> getAllParts() {
		  return Stream.concat(Stream.of(this), this.children.values().stream().flatMap(PartCloneModel::getAllParts));
	   }

	   @OnlyIn(Dist.CLIENT)
	   public static class Cube {
		  private final PartCloneModel.Polygon[] polygons;
		  public final float minX;
		  public final float minY;
		  public final float minZ;
		  public final float maxX;
		  public final float maxY;
		  public final float maxZ;

		  public Cube(int u, int v, float x, float y, float z, float wx, float wy, float wz, float growX, float growY, float growZ, boolean mirror, float p_104355_, float p_104356_) {
			 this.minX = x;
			 this.minY = y;
			 this.minZ = z;
			 this.maxX = x + wx;
			 this.maxY = y + wy;
			 this.maxZ = z + wz;
			 this.polygons = new PartCloneModel.Polygon[6];
			 float f = x + wx;
			 float f1 = y + wy;
			 float f2 = z + wz;
			 x = x - growX;
			 y = y - growY;
			 z = z - growZ;
			 f = f + growX;
			 f1 = f1 + growY;
			 f2 = f2 + growZ;
			 if (mirror) {
				float f3 = f;
				f = x;
				x = f3;
			 }

			 PartCloneModel.Vertex ModelPartClone$vertex7 = new PartCloneModel.Vertex(x, y, z, 0.0F, 0.0F);
			 PartCloneModel.Vertex ModelPartClone$vertex = new PartCloneModel.Vertex(f, y, z, 0.0F, 8.0F);
			 PartCloneModel.Vertex ModelPartClone$vertex1 = new PartCloneModel.Vertex(f, f1, z, 8.0F, 8.0F);
			 PartCloneModel.Vertex ModelPartClone$vertex2 = new PartCloneModel.Vertex(x, f1, z, 8.0F, 0.0F);
			 PartCloneModel.Vertex ModelPartClone$vertex3 = new PartCloneModel.Vertex(x, y, f2, 0.0F, 0.0F);
			 PartCloneModel.Vertex ModelPartClone$vertex4 = new PartCloneModel.Vertex(f, y, f2, 0.0F, 8.0F);
			 PartCloneModel.Vertex ModelPartClone$vertex5 = new PartCloneModel.Vertex(f, f1, f2, 8.0F, 8.0F);
			 PartCloneModel.Vertex ModelPartClone$vertex6 = new PartCloneModel.Vertex(x, f1, f2, 8.0F, 0.0F);
			 float f4 = (float)u;
			 float f5 = (float)u + wz;
			 float f6 = (float)u + wz + wx;
			 float f7 = (float)u + wz + wx + wx;
			 float f8 = (float)u + wz + wx + wz;
			 float f9 = (float)u + wz + wx + wz + wx;
			 float f10 = (float)v;
			 float f11 = (float)v + wz;
			 float f12 = (float)v + wz + wy;
			 this.polygons[2] = new PartCloneModel.Polygon(new PartCloneModel.Vertex[]{ModelPartClone$vertex4, ModelPartClone$vertex3, ModelPartClone$vertex7, ModelPartClone$vertex}, f5, f10, f6, f11, p_104355_, p_104356_, mirror, Direction.DOWN);
			 this.polygons[3] = new PartCloneModel.Polygon(new PartCloneModel.Vertex[]{ModelPartClone$vertex1, ModelPartClone$vertex2, ModelPartClone$vertex6, ModelPartClone$vertex5}, f6, f11, f7, f10, p_104355_, p_104356_, mirror, Direction.UP);
			 this.polygons[1] = new PartCloneModel.Polygon(new PartCloneModel.Vertex[]{ModelPartClone$vertex7, ModelPartClone$vertex3, ModelPartClone$vertex6, ModelPartClone$vertex2}, f4, f11, f5, f12, p_104355_, p_104356_, mirror, Direction.WEST);
			 this.polygons[4] = new PartCloneModel.Polygon(new PartCloneModel.Vertex[]{ModelPartClone$vertex, ModelPartClone$vertex7, ModelPartClone$vertex2, ModelPartClone$vertex1}, f5, f11, f6, f12, p_104355_, p_104356_, mirror, Direction.NORTH);
			 this.polygons[0] = new PartCloneModel.Polygon(new PartCloneModel.Vertex[]{ModelPartClone$vertex4, ModelPartClone$vertex, ModelPartClone$vertex1, ModelPartClone$vertex5}, f6, f11, f8, f12, p_104355_, p_104356_, mirror, Direction.EAST);
			 this.polygons[5] = new PartCloneModel.Polygon(new PartCloneModel.Vertex[]{ModelPartClone$vertex3, ModelPartClone$vertex4, ModelPartClone$vertex5, ModelPartClone$vertex6}, f8, f11, f9, f12, p_104355_, p_104356_, mirror, Direction.SOUTH);
		  }

		  public void compile(PoseStack.Pose p_171333_, VertexConsumer p_171334_, int p_171335_, int p_171336_, float p_171337_, float p_171338_, float p_171339_, float p_171340_) {
			 Matrix4f matrix4f = p_171333_.pose();
			 Matrix3f matrix3f = p_171333_.normal();

			 for(PartCloneModel.Polygon ModelPartClone$polygon : this.polygons) {
				Vector3f vector3f = ModelPartClone$polygon.normal.copy();
				vector3f.transform(matrix3f);
				float f = vector3f.x();
				float f1 = vector3f.y();
				float f2 = vector3f.z();

				for(PartCloneModel.Vertex ModelPartClone$vertex : ModelPartClone$polygon.vertices) {
				   float f3 = ModelPartClone$vertex.pos.x() / 16.0F;
				   float f4 = ModelPartClone$vertex.pos.y() / 16.0F;
				   float f5 = ModelPartClone$vertex.pos.z() / 16.0F;
				   Vector4f vector4f = new Vector4f(f3, f4, f5, 1.0F);
				   vector4f.transform(matrix4f);
				   p_171334_.vertex(vector4f.x(), vector4f.y(), vector4f.z(), p_171337_, p_171338_, p_171339_, p_171340_, ModelPartClone$vertex.u, ModelPartClone$vertex.v, p_171336_, p_171335_, f, f1, f2);
				}
			 }

		  }
	   }

	   @OnlyIn(Dist.CLIENT)
	   static class Polygon {
		  public final PartCloneModel.Vertex[] vertices;
		  public final Vector3f normal;

		  public Polygon(PartCloneModel.Vertex[] p_104362_, float p_104363_, float p_104364_, float p_104365_, float p_104366_, float p_104367_, float p_104368_, boolean p_104369_, Direction p_104370_) {
			 this.vertices = p_104362_;
			 float f = 0.0F / p_104367_;
			 float f1 = 0.0F / p_104368_;
			 p_104362_[0] = p_104362_[0].remap(p_104365_ / p_104367_ - f, p_104364_ / p_104368_ + f1);
			 p_104362_[1] = p_104362_[1].remap(p_104363_ / p_104367_ + f, p_104364_ / p_104368_ + f1);
			 p_104362_[2] = p_104362_[2].remap(p_104363_ / p_104367_ + f, p_104366_ / p_104368_ - f1);
			 p_104362_[3] = p_104362_[3].remap(p_104365_ / p_104367_ - f, p_104366_ / p_104368_ - f1);
			 if (p_104369_) {
				int i = p_104362_.length;

				for(int j = 0; j < i / 2; ++j) {
				   PartCloneModel.Vertex ModelPartClone$vertex = p_104362_[j];
				   p_104362_[j] = p_104362_[i - 1 - j];
				   p_104362_[i - 1 - j] = ModelPartClone$vertex;
				}
			 }

			 this.normal = p_104370_.step();
			 if (p_104369_) {
				this.normal.mul(-1.0F, 1.0F, 1.0F);
			 }

		  }
	   }

	   @OnlyIn(Dist.CLIENT)
	   static class Vertex {
		  public final Vector3f pos;
		  public final float u;
		  public final float v;

		  public Vertex(float p_104375_, float p_104376_, float p_104377_, float p_104378_, float p_104379_) {
			 this(new Vector3f(p_104375_, p_104376_, p_104377_), p_104378_, p_104379_);
		  }

		  public PartCloneModel.Vertex remap(float p_104385_, float p_104386_) {
			 return new PartCloneModel.Vertex(this.pos, p_104385_, p_104386_);
		  }

		  public Vertex(Vector3f p_104381_, float p_104382_, float p_104383_) {
			 this.pos = p_104381_;
			 this.u = p_104382_;
			 this.v = p_104383_;
		  }
	   }

	   @FunctionalInterface
	   @OnlyIn(Dist.CLIENT)
	   public interface Visitor {
		  void visit(PoseStack.Pose p_171342_, String p_171343_, int p_171344_, PartCloneModel.Cube p_171345_);
	   }
	   
	   
	   //// Begin Skyler Changes
	   public void addChild(String name, PartCloneModel child) {
		   if (null != this.children.putIfAbsent(name, child)) {
			   throw new RuntimeException("Replacing child element");
		   }
	   }
}
