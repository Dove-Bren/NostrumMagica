package com.smanzana.nostrummagica.block.dungeon;

import java.util.Random;

import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.boss.plantboss.PlantBossEntity;
import com.smanzana.nostrummagica.entity.boss.reddragon.RedDragonEntity;
import com.smanzana.nostrummagica.entity.golem.MagicEarthGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicEnderGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicFireGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicIceGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicLightningGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicPhysicalGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicWindGolemEntity;
import com.smanzana.nostrummagica.item.EssenceItem;
import com.smanzana.nostrummagica.tile.NostrumBlockEntities;
import com.smanzana.nostrummagica.tile.SingleSpawnerTileEntity;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

public class SingleSpawnerBlock extends BaseEntityBlock {
	
	public static enum Type implements StringRepresentable {
		// Do not change order. Ordinals are used
		GOLEM_EARTH,
		GOLEM_ENDER,
		GOLEM_FIRE,
		GOLEM_ICE,
		GOLEM_LIGHTNING,
		GOLEM_PHYSICAL,
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
	
	public static final int SPAWN_DIST_SQ = 900; // 30^2 
	public static final EnumProperty<Type> MOB = EnumProperty.create("mob", Type.class);

	public static final String ID = "nostrum_spawner";
	
	public SingleSpawnerBlock() {
		super(Block.Properties.of(Material.STONE)
				.strength(-1.0F, 3600000.8F)
				.sound(SoundType.STONE)
				.noDrops()
				.noCollission()
				);
		
		this.registerDefaultState(this.stateDefinition.any().setValue(MOB, Type.GOLEM_PHYSICAL));
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(MOB);
	}
	
	public BlockState getState(SingleSpawnerBlock.Type type) {
		return defaultBlockState().setValue(MOB, type);
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {
		if (!worldIn.isClientSide())
		{
			for (Player player : worldIn.players()) {
				if (!player.isSpectator() && !player.isCreative() && player.distanceToSqr(pos.getX() + .5, pos.getY(), pos.getZ() + .5) < SPAWN_DIST_SQ) {
					this.spawn(worldIn, pos, state, rand);
					worldIn.removeBlock(pos, false);
					return;
				}
			}
		}
	}
	
	public Mob spawn(Level world, BlockPos pos, BlockState state, Random rand) {
		Type type = state.getValue(MOB);
		Mob entity = getEntity(type, world, pos);
		
		entity.setPersistenceRequired();
		entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + .5);
		
		world.addFreshEntity(entity);
		return entity;
	}
	
	protected static Mob getEntity(Type type, Level world, BlockPos pos) {
		if (type == null)
			return null;
		
		Mob entity = null;
		
		switch (type) {
		case GOLEM_EARTH:
			entity = new MagicEarthGolemEntity(NostrumEntityTypes.golemEarth, world);
			break;
		case GOLEM_ENDER:
			entity = new MagicEnderGolemEntity(NostrumEntityTypes.golemEnder, world);
			break;
		case GOLEM_FIRE:
			entity = new MagicFireGolemEntity(NostrumEntityTypes.golemFire, world);
			break;
		case GOLEM_ICE:
			entity = new MagicIceGolemEntity(NostrumEntityTypes.golemIce, world);
			break;
		case GOLEM_LIGHTNING:
			entity = new MagicLightningGolemEntity(NostrumEntityTypes.golemLightning, world);
			break;
		case GOLEM_PHYSICAL:
			entity = new MagicPhysicalGolemEntity(NostrumEntityTypes.golemPhysical, world);
			break;
		case GOLEM_WIND:
			entity = new MagicWindGolemEntity(NostrumEntityTypes.golemWind, world);
			break;
		case DRAGON_RED:
			entity = new RedDragonEntity(NostrumEntityTypes.dragonRed, world);
			break;
		case PLANT_BOSS:
			entity = new PlantBossEntity(NostrumEntityTypes.plantBoss, world);
			break;
		}
		
		return entity;
	}


	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SingleSpawnerTileEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return TickableBlockEntity.createTickerHelper(type, NostrumBlockEntities.SingleSpawner);
	}
	
//	@Override
//	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
//		if (state.getBlock() != newState.getBlock()) {
//        	world.removeTileEntity(pos);
//		}
//	}
	
//	@SuppressWarnings("deprecation")
//	@Override
//	public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int eventID, int eventParam) {
//		super.eventReceived(state, worldIn, pos, eventID, eventParam);
//		TileEntity tileentity = worldIn.getTileEntity(pos);
//        return tileentity == null ? false : tileentity.receiveClientEvent(eventID, eventParam);
//	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		if (worldIn.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.SUCCESS;
		}
		
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te == null || !(te instanceof SingleSpawnerTileEntity)) {
			return InteractionResult.SUCCESS;
		}
		
		if (playerIn.isCreative()) {
			ItemStack heldItem = playerIn.getItemInHand(hand);
			if (heldItem.isEmpty()) {
				playerIn.sendMessage(new TextComponent("Currently set to " + state.getValue(MOB).getSerializedName()), Util.NIL_UUID);
			} else if (heldItem.getItem() instanceof EssenceItem) {
				Type type = null;
				switch (EssenceItem.findType(heldItem)) {
				case EARTH:
					type = Type.GOLEM_EARTH;
					break;
				case ENDER:
					type = Type.GOLEM_ENDER;
					break;
				case FIRE:
					type = Type.GOLEM_FIRE;
					break;
				case ICE:
					type = Type.GOLEM_ICE;
					break;
				case LIGHTNING:
					type = Type.GOLEM_LIGHTNING;
					break;
				case PHYSICAL:
					type = Type.GOLEM_PHYSICAL;
					break;
				case WIND:
					type = Type.GOLEM_WIND;
					break;
				}
				
				worldIn.setBlockAndUpdate(pos, state.setValue(MOB, type));
			} else if (heldItem.is(NostrumTags.Items.DragonWing)) {
				worldIn.setBlockAndUpdate(pos, state.setValue(MOB, Type.DRAGON_RED));
			} else if (heldItem.getItem() == Items.SUGAR_CANE) {
				worldIn.setBlockAndUpdate(pos, state.setValue(MOB, Type.PLANT_BOSS));
			}
			return InteractionResult.SUCCESS;
		}
		
		return InteractionResult.PASS;
	}
}
