package com.smanzana.nostrummagica.client.render;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.item.IBlueprintHolder;
import com.smanzana.nostrummagica.util.RenderFuncs;
import com.smanzana.nostrummagica.world.blueprints.BlueprintBlock;
import com.smanzana.nostrummagica.world.blueprints.IBlueprint;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BlueprintRenderer {
	
	private IBlueprint cachedBlueprint = null;
	private VertexBuffer cachedRenderList = new VertexBuffer(DefaultVertexFormats.BLOCK);
	private boolean cachedRenderDirty = true;
	
	public BlueprintRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onHighlight(DrawHighlightEvent.HighlightBlock event) {
		if (event.getTarget().getType() == RayTraceResult.Type.BLOCK) {
			Minecraft mc = Minecraft.getInstance();
			ClientPlayerEntity player = mc.player;
			final MatrixStack matrixStackIn = event.getMatrix();
			final BlockRayTraceResult blockResult = (BlockRayTraceResult) event.getTarget();
			final BlockPos blockPos = blockResult.getPos().offset(blockResult.getFace());
			final ItemStack blueprintStack = this.findStackToRender(player, blockPos);
			if (!blueprintStack.isEmpty()) {
				IBlueprint blueprint = ((IBlueprintHolder) blueprintStack.getItem()).getBlueprint(player, blueprintStack, blockPos);
				if (cachedBlueprint != blueprint) {
					cachedBlueprint = blueprint;
					cachedRenderDirty = true;
				}
				
				if (cachedBlueprint != null) {
					final IRenderTypeBuffer.Impl bufferIn = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
					Vector3d center = event.getTarget().getHitVec();
					Direction face = Direction.getFacingFromVector((float) (center.x - player.getPosX()), 0f, (float) (center.z - player.getPosZ()));
					
					// Template is saved with some starting rotation. We want to render it such that the entry rotation is {face}.
					// The preview is with no rotation AKA if it was spawned with the same rotation is was catured with.
					// To render WITH the desired entry rotation, we have to figure out the diff and render with that.
					face = IBlueprint.GetModDir(cachedBlueprint.getEntry().getFacing(), face);
					
					renderBlueprintPreview(matrixStackIn, bufferIn, blockPos, cachedBlueprint.getPreview(), face, event.getPartialTicks());
					bufferIn.finish();
				}
			}
		}
	}
	
	protected ItemStack findStackToRender(PlayerEntity player, BlockPos pos) {
		ItemStack ret = ItemStack.EMPTY;
		for (ItemStack held : player.getHeldEquipment()) {
			if (!held.isEmpty() && held.getItem() instanceof IBlueprintHolder) {
				IBlueprintHolder holder = (IBlueprintHolder) held.getItem();
				if (holder.hasBlueprint(player, held) && holder.shouldDisplayBlueprint(player, held, pos)) {
					ret = held;
					break;
				}
			}
		}
		return ret;
	}
	
	@SuppressWarnings("deprecation")
	private void renderBlueprintPreview(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, BlockPos center, BlueprintBlock[][][] preview, Direction rotation, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		Vector3d playerPos = mc.gameRenderer.getActiveRenderInfo().getProjectedView();//player.getEyePosition(partialTicks).subtract(0, player.getEyeHeight(), 0);
		Vector3d offset = new Vector3d(center.getX() - playerPos.x,
				center.getY() - playerPos.y,
				center.getZ() - playerPos.z);
		
		// Compile drawlist if not present
		if (cachedRenderDirty) {
			cachedRenderDirty = false;
			//cachedRenderList.reset(); Reset by doing new upload
			
			final int width = preview.length;
			final int height = preview[0].length;
			final int depth = preview[0][0].length;
			BufferBuilder buffer = new BufferBuilder(4096);
			
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			
			for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
			for (int z = 0; z < depth; z++) {
				final BlueprintBlock block = preview[x][y][z];
				if (block == null) {
					continue;
				}
				
				final int xOff = x - (width/2);
				final int yOff = y - 1;
				final int zOff = z - (depth/2);
				
				BlockState state = block.getSpawnState(Direction.NORTH);
				
				if (state == null || state.getBlock() == Blocks.AIR) {
					continue;
				}
				
				IBakedModel model = null;
				if (state != null) {
					model = mc.getBlockRendererDispatcher().getModelForState(state);
				}
				
				if (model == null || model == mc.getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel()) {
					model = mc.getBlockRendererDispatcher().getModelForState(Blocks.STONE.getDefaultState());
				}
				
				final int fakeLight = 15728880;
				MatrixStack renderStack = new MatrixStack();
				renderStack.push();
				renderStack.translate(xOff, yOff, zOff);
				
				RenderFuncs.RenderModel(renderStack, buffer, model, fakeLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, .6f);
				
				renderStack.pop();
			}
			buffer.finishDrawing();
			cachedRenderList.upload(buffer); // Capture what we rendered
		}
		
		final float angle;
		final int rotX;
		final int rotZ;
		switch (rotation) {
		case NORTH:
		case UP:
		case DOWN:
		default:
			angle = 0;
			rotX = 0;
			rotZ = 0;
			break;
		case EAST:
			angle = 270;
			rotX = 1;
			rotZ = 0;
			break;
		case SOUTH:
			angle = 180;
			rotX = 1;
			rotZ = 1;
			break;
		case WEST:
			angle = 90;
			rotX = 0;
			rotZ = 1;
			break;
		}
		
		matrixStackIn.push();
		
		matrixStackIn.translate(offset.x + rotX, offset.y, offset.z + rotZ);
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(angle));
		
		mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		
		cachedRenderList.bindBuffer();
		DefaultVertexFormats.BLOCK.setupBufferState(0);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		cachedRenderList.draw(matrixStackIn.getLast().getMatrix(), GL11.GL_QUADS);
		RenderSystem.disableBlend();
		VertexBuffer.unbindBuffer();
        DefaultVertexFormats.BLOCK.clearBufferState();
		
		
		matrixStackIn.pop();
	}
	
}
