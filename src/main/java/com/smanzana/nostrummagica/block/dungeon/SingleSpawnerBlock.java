package com.smanzana.nostrummagica.block.dungeon;

import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.item.EssenceItem;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.tile.NostrumBlockEntities;
import com.smanzana.nostrummagica.tile.SingleSpawnerTileEntity;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;
import com.smanzana.nostrummagica.util.ShapeUtil;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SingleSpawnerBlock extends BaseEntityBlock {
	
	public static enum Type implements StringRepresentable {
		// Do not change order. Ordinals are used
		GOLEM_EARTH,
		GOLEM_ENDER,
		GOLEM_FIRE,
		GOLEM_ICE,
		GOLEM_LIGHTNING,
		GOLEM_NEUTRAL,
		GOLEM_WIND,
		DRAGON_RED,
		PLANT_BOSS,
		;

		@Override
		public String getSerializedName() {
			return this.name().toLowerCase();
		}
		
		@Override
		public String toString() {
			return this.getSerializedName();
		}
	}
	
	public static final EnumProperty<Type> MOB = EnumProperty.create("mob", Type.class);

	public static final String ID = "nostrum_spawner";
	
	public SingleSpawnerBlock() {
		super(Block.Properties.of(Material.STONE)
				.strength(-1.0F, 3600000.8F)
				.sound(SoundType.STONE)
				.noDrops()
				.noCollission()
				);
		
		this.registerDefaultState(this.stateDefinition.any().setValue(MOB, Type.GOLEM_NEUTRAL));
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(MOB);
	}
	
	public BlockState getState(SingleSpawnerBlock.Type type) {
		return defaultBlockState().setValue(MOB, type);
	}
	
	public SingleSpawnerBlock.Type getLegacySpawnType(BlockState state) {
		return state.getValue(MOB);
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
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
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {
		//super.tick(state, worldIn, pos, rand);
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SingleSpawnerTileEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return TickableBlockEntity.createTickerHelper(type, NostrumBlockEntities.SingleSpawner);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		if (worldIn.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.SUCCESS;
		}
		
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te == null || !(te instanceof SingleSpawnerTileEntity spawner)) {
			return InteractionResult.SUCCESS;
		}
		
		if (playerIn.isCreative()) {
			ItemStack heldItem = playerIn.getItemInHand(hand);
			if (heldItem.isEmpty()) {
				playerIn.sendMessage(new TextComponent("Currently set to " + spawner.getSpawnType().toString()), Util.NIL_UUID);
			} else if (heldItem.getItem() instanceof EssenceItem) {
				EntityType<?> type = null;
				switch (EssenceItem.findType(heldItem)) {
				case EARTH:
					type = NostrumEntityTypes.golemEarth;
					break;
				case ENDER:
					type = NostrumEntityTypes.golemEnder;
					break;
				case FIRE:
					type = NostrumEntityTypes.golemFire;
					break;
				case ICE:
					type = NostrumEntityTypes.golemIce;
					break;
				case LIGHTNING:
					type = NostrumEntityTypes.golemLightning;
					break;
				case NEUTRAL:
					type = NostrumEntityTypes.golemNeutral;
					break;
				case WIND:
					type = NostrumEntityTypes.golemWind;
					break;
				}
				
				spawner.setSpawnType(type);
			} else if (heldItem.is(NostrumTags.Items.DragonWing)) {
				spawner.setSpawnType(NostrumEntityTypes.dragonRed);
			} else if (heldItem.getItem() == Items.SUGAR_CANE) {
				spawner.setSpawnType(NostrumEntityTypes.plantBoss);
			} else if (heldItem.getItem() == NostrumBlocks.pushBlock.asItem()) {
				spawner.setSpawnType(NostrumEntityTypes.playerStatue);
			} else if (heldItem.getItem() == NostrumItems.crystalSmall) {
				spawner.setSpawnType(NostrumEntityTypes.primalMage);
			} else if (heldItem.getItem() == NostrumItems.crystalLarge) {
				spawner.setSpawnType(NostrumEntityTypes.shadowDragonBoss);
			} else if (heldItem.getItem() instanceof SpawnEggItem egg) {
				spawner.setSpawnType(egg.getType(heldItem.getTag()));
			}
			return InteractionResult.SUCCESS;
		}
		
		return InteractionResult.PASS;
	}
}
