package com.smanzana.nostrummagica.block.dungeon;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.world.blueprints.Blueprint;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.autodungeons.world.blueprints.IBlueprint;
import com.smanzana.nostrummagica.block.ITriggeredBlock;
import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.item.mapmaking.CopyWandItem;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.TemplateStamperBlockEntity;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.ShapeUtil;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TemplateStamperBlock extends BaseEntityBlock implements ITriggeredBlock {
	
	public static final String ID = "template_stamper";

	public TemplateStamperBlock() {
		super(Block.Properties.of(Material.BARRIER)
				.strength(-1.0F, 3600000.8F)
				.noDrops()
				.noCollission()
				);
	}
	
	// GetHowMuchLightGoesThrough?? Not sure.
	@Override
	@OnlyIn(Dist.CLIENT)
	public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 1.0F;
	}
	
	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
		return true;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE; 
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		// Render/particle code calls with dummy sometimes and crashes if you return an empty cube
		if (context != CollisionContext.empty() && context instanceof EntityCollisionContext) {
			final @Nullable Entity entity = ((EntityCollisionContext) context).getEntity();
			if (entity != null && entity instanceof Player && ((Player) entity).isCreative()) {
				return Shapes.block();
			}
		}
		
		return ShapeUtil.EMPTY_NOCRASH;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TemplateStamperBlockEntity(pos, state);
	}

	@Override
	public void trigger(Level world, BlockPos blockPos, BlockState state, BlockPos triggerPos) {
		BlockEntity te = world.getBlockEntity(blockPos);
		if (te instanceof TemplateStamperBlockEntity ent) {
			ent.trigger(triggerPos);
		}
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		if (worldIn.isClientSide() || !playerIn.isCreative()) {
			return playerIn.isCreative() ? InteractionResult.SUCCESS : InteractionResult.FAIL; // in creative, we still want the client to think we ate the interact
		}
		
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te == null) {
			return InteractionResult.FAIL;
		}
		
		TemplateStamperBlockEntity ent = (TemplateStamperBlockEntity) te;
		
		ItemStack heldItem = playerIn.getItemInHand(hand);
		
		if (heldItem.isEmpty()) {
			ent.trigger(pos);
			// Display info
//			List<BlockPos> offsets = ent.getOffsets();
//			playerIn.sendMessage(new TextComponent("Holding " + offsets.size() + " offsets"), Util.NIL_UUID);
//			if (playerIn.isShiftKeyDown()) {
//				for (BlockPos offset : offsets) {
//					final BlockPos worldPosition = pos.offset(offset);
//					playerIn.sendMessage(new TextComponent(" > " + offset), Util.NIL_UUID);
//					NostrumParticles.GLOW_TRAIL.spawn(worldIn, new SpawnParams(1, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
//							0, 300, 0, new TargetLocation(Vec3.atCenterOf(worldPosition))
//							).setTargetBehavior(new ParticleTargetBehavior().joinMode(true)).color(1f, .8f, 1f, .3f));
//				}
//			}
//			if (ent.getTriggerRequirement() > 1) {
//				playerIn.sendMessage(new TextComponent("Triggered %d out of %d times ".formatted(ent.getCurrentTriggerCount(), ent.getTriggerRequirement())), Util.NIL_UUID);
//			}
		} else if (heldItem.getItem() instanceof PositionCrystal) {
			BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
			if (heldPos != null && DimensionUtils.DimEquals(PositionCrystal.getDimension(heldItem), worldIn.dimension())) {
				ent.setSpawnPoint(heldPos, false);
				playerIn.sendMessage(new TextComponent("Offset to " + heldPos), Util.NIL_UUID);
				NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
		} else if (heldItem.getItem() instanceof CopyWandItem copyWand) {
			IBlueprint blueprint = copyWand.getBlueprint(playerIn, heldItem, pos);
			if (blueprint != null) {
				ent.setBlueprint((Blueprint) blueprint);
				playerIn.sendMessage(new TextComponent("Overwrote blueprint"), Util.NIL_UUID);
				NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			} else {
				BlockPos anchor = copyWand.getAnchor(playerIn, heldItem);
				BlockPos boundingPos = copyWand.getBoundingPos(playerIn, heldItem);
				if (anchor != null && boundingPos != null) {
					// make anchor min and bounding max
					{
						final int minX = Math.min(anchor.getX(), boundingPos.getX());
						final int minY = Math.min(anchor.getY(), boundingPos.getY());
						final int minZ = Math.min(anchor.getZ(), boundingPos.getZ());
						final int maxX = Math.max(anchor.getX(), boundingPos.getX());
						final int maxY = Math.max(anchor.getY(), boundingPos.getY());
						final int maxZ = Math.max(anchor.getZ(), boundingPos.getZ());
						anchor = new BlockPos(minX, minY, minZ);
						boundingPos = new BlockPos(maxX, maxY, maxZ);
					}
					
					
					// Want block that's in center of area, but closest to our Y
					BlockPos captureOrigin = new BlockPos(
							anchor.getX() + ((boundingPos.getX() - anchor.getX()) / 2),
							Math.max(anchor.getY(), Math.min(boundingPos.getY(), pos.getY())),
							anchor.getZ() + ((boundingPos.getZ() - anchor.getZ()) / 2)
							);
					
					playerIn.sendMessage(new TextComponent("Capturing %s to %s with center %s".formatted(anchor, boundingPos, captureOrigin)), Util.NIL_UUID);
					NostrumMagicaSounds.STATUS_BUFF2.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
					
					Blueprint newBlueprint = Blueprint.Capture(worldIn, anchor, boundingPos, new BlueprintLocation(captureOrigin.subtract(anchor), Direction.NORTH));
					ent.setBlueprint(newBlueprint);
					ent.setSpawnPoint(captureOrigin, Direction.NORTH, false);
				}
			}
		} else if (heldItem.getItem() == Items.LEVER) {
			final boolean newOneTime = !ent.isOneTimeOnly();
			playerIn.sendMessage(new TextComponent("%s one time only".formatted(newOneTime ? "Is" : "Is NOT")), Util.NIL_UUID);
			NostrumMagicaSounds.STATUS_BUFF2.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			ent.setOneTimeOnly(newOneTime);
			
			if (ent.hasBeenTriggered()) {
				ent.resetTriggered();
				playerIn.sendMessage(new TextComponent("Reset triggered status"), Util.NIL_UUID);
			}
		} else if (heldItem.getItem() == Items.PAPER) {
			final boolean newShowHint = !ent.showsHint();
			playerIn.sendMessage(new TextComponent("%s show hint".formatted(newShowHint ? "Will" : "Will NOT")), Util.NIL_UUID);
			NostrumMagicaSounds.STATUS_BUFF2.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			ent.setShowHint(newShowHint);
		}
		
		return InteractionResult.SUCCESS;
	}

}
