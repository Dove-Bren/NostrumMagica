package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonRed;
import com.smanzana.nostrummagica.entity.golem.EntityGolemEarth;
import com.smanzana.nostrummagica.entity.golem.EntityGolemEnder;
import com.smanzana.nostrummagica.entity.golem.EntityGolemFire;
import com.smanzana.nostrummagica.entity.golem.EntityGolemIce;
import com.smanzana.nostrummagica.entity.golem.EntityGolemLightning;
import com.smanzana.nostrummagica.entity.golem.EntityGolemPhysical;
import com.smanzana.nostrummagica.entity.golem.EntityGolemWind;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBoss;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.tiles.SingleSpawnerTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;

public class NostrumSingleSpawner extends Block {
	
	public static enum Type implements IStringSerializable {
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
		public String getString() {
			return this.name().toLowerCase();
		}
		
		@Override
		public String toString() {
			return this.getString();
		}
	}
	
	public static final int SPAWN_DIST_SQ = 900; // 30^2 
	public static final EnumProperty<Type> MOB = EnumProperty.create("mob", Type.class);

	public static final String ID = "nostrum_spawner";
	
	public NostrumSingleSpawner() {
		super(Block.Properties.create(Material.ROCK)
				.hardnessAndResistance(-1.0F, 3600000.8F)
				.sound(SoundType.STONE)
				.harvestTool(ToolType.PICKAXE)
				.harvestLevel(4)
				.noDrops()
				.doesNotBlockMovement()
				);
		
		this.setDefaultState(this.stateContainer.getBaseState().with(MOB, Type.GOLEM_PHYSICAL));
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(MOB);
	}
	
	public BlockState getState(NostrumSingleSpawner.Type type) {
		return getDefaultState().with(MOB, type);
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
		if (!worldIn.isRemote())
		{
			for (PlayerEntity player : worldIn.getPlayers()) {
				if (!player.isSpectator() && !player.isCreative() && player.getDistanceSq(pos.getX() + .5, pos.getY(), pos.getZ() + .5) < SPAWN_DIST_SQ) {
					this.spawn(worldIn, pos, state, rand);
					worldIn.removeBlock(pos, false);
					return;
				}
			}
		}
	}
	
	public MobEntity spawn(World world, BlockPos pos, BlockState state, Random rand) {
		Type type = state.get(MOB);
		MobEntity entity = getEntity(type, world, pos);
		
		entity.enablePersistence();
		entity.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + .5);
		
		world.addEntity(entity);
		return entity;
	}
	
	protected static MobEntity getEntity(Type type, World world, BlockPos pos) {
		if (type == null)
			return null;
		
		MobEntity entity = null;
		
		switch (type) {
		case GOLEM_EARTH:
			entity = new EntityGolemEarth(NostrumEntityTypes.golemEarth, world);
			break;
		case GOLEM_ENDER:
			entity = new EntityGolemEnder(NostrumEntityTypes.golemEnder, world);
			break;
		case GOLEM_FIRE:
			entity = new EntityGolemFire(NostrumEntityTypes.golemFire, world);
			break;
		case GOLEM_ICE:
			entity = new EntityGolemIce(NostrumEntityTypes.golemIce, world);
			break;
		case GOLEM_LIGHTNING:
			entity = new EntityGolemLightning(NostrumEntityTypes.golemLightning, world);
			break;
		case GOLEM_PHYSICAL:
			entity = new EntityGolemPhysical(NostrumEntityTypes.golemPhysical, world);
			break;
		case GOLEM_WIND:
			entity = new EntityGolemWind(NostrumEntityTypes.golemWind, world);
			break;
		case DRAGON_RED:
			entity = new EntityDragonRed(NostrumEntityTypes.dragonRed, world);
			break;
		case PLANT_BOSS:
			entity = new EntityPlantBoss(NostrumEntityTypes.plantBoss, world);
			break;
		}
		
		return entity;
	}


	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new SingleSpawnerTileEntity();
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
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		if (worldIn.isRemote) {
			return ActionResultType.SUCCESS;
		}
		
		if (hand != Hand.MAIN_HAND) {
			return ActionResultType.SUCCESS;
		}
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof SingleSpawnerTileEntity)) {
			return ActionResultType.SUCCESS;
		}
		
		if (playerIn.isCreative()) {
			ItemStack heldItem = playerIn.getHeldItem(hand);
			if (heldItem.isEmpty()) {
				playerIn.sendMessage(new StringTextComponent("Currently set to " + state.get(MOB).getString()), Util.DUMMY_UUID);
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
				
				worldIn.setBlockState(pos, state.with(MOB, type));
			} else if (NostrumTags.Items.DragonWing.contains(heldItem.getItem())) {
				worldIn.setBlockState(pos, state.with(MOB, Type.DRAGON_RED));
			} else if (heldItem.getItem() == Items.SUGAR_CANE) {
				worldIn.setBlockState(pos, state.with(MOB, Type.PLANT_BOSS));
			}
			return ActionResultType.SUCCESS;
		}
		
		return ActionResultType.PASS;
	}
}
