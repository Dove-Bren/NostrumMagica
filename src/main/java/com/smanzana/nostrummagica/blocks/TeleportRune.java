package com.smanzana.nostrummagica.blocks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.curios.items.NostrumCurios;
import com.smanzana.nostrummagica.items.EnchantedArmor;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.tiles.TeleportRuneTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TeleportRune extends ContainerBlock  {
	
	public static final String ID = "teleport_rune";
	protected static final VoxelShape RUNE_AABB = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2D, 16.0D);
	
	public TeleportRune() {
		super(Block.Properties.create(Material.CARPET)
				.hardnessAndResistance(0.5f, 5.0f)
				.sound(SoundType.STONE)
				.tickRandomly()
				);
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return true;
    }
	
//	@Override
//	public boolean isFullCube(BlockState state) {
//		return false;
//	}
//	
//	@Override
//	public boolean isOpaqueCube(BlockState state) {
//		return false;
//	}
//	
//	@Override
//	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
//        return false;
//    }
//	
//	@Override
//	public int getLightValue(BlockState state, IBlockAccess world, BlockPos pos) {
//		return 0;
//	}
//	
//	@Override
//	public int getLightOpacity(BlockState state, IBlockAccess world, BlockPos pos) {
//		return 0;
//	}
//	
//	@Override
//	public boolean isSideSolid(BlockState state, IBlockAccess worldIn, BlockPos pos, Direction side) {
//		return false;
//	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.empty();
		//return super.getCollisionBoundingBox(blockState, worldIn, pos);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return RUNE_AABB;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
//	@Override
//	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) { broke();
//		super.breakBlock(world, pos, state);
//	}
	
//	@Override
//	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
//		return super.canPlaceBlockAt(worldIn, pos);
//	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}
	
	protected void teleportEntity(World worldIn, BlockPos portalPos, Entity entityIn) {
		TileEntity te = worldIn.getTileEntity(portalPos);
		if (te == null || !(te instanceof TeleportRuneTileEntity)) {
			return;
		}
		
		TeleportRuneTileEntity ent = (TeleportRuneTileEntity) te;
		BlockPos offset = ent.getOffset();
		if (offset == null) {
			return;
		}
		
		BlockPos target = portalPos.add(offset);
		entityIn.lastTickPosX = entityIn.prevPosX = target.getX() + .5;
		entityIn.lastTickPosY = entityIn.prevPosY = target.getY() + .005;
		entityIn.lastTickPosZ = entityIn.prevPosZ = target.getZ() + .5;
		
		if (!worldIn.isRemote) {
			NostrumMagica.playerListener.registerTimer((type, entity, data) -> {
				//Event type, LivingEntity entity, T data
				entityIn.setPositionAndUpdate(target.getX() + .5, target.getY() + .005, target.getZ() + .5);
	
				double dx = target.getX() + .5;
				double dy = target.getY() + 1;
				double dz = target.getZ() + .5;
				for (int i = 0; i < 10; i++) {
					
					((ServerWorld) worldIn).spawnParticle(ParticleTypes.DRAGON_BREATH,
							dx,
							dy,
							dz,
							10,
							.25,
							.6,
							.25,
							.1
							);
				}
				NostrumMagicaSounds.DAMAGE_ENDER.play(worldIn, dx, dy, dz);
				return true;
			}, 1, 0);
		}
	}
	
	// Note: Just doing a dumb little static map cause we don't really care to persist portal cooldowns. Just
	// There to be nice to players.
	private static final Map<UUID, Integer> EntityTeleportCharge = new HashMap<>();
	
	// How long entities must wait in the teleporation block before they teleport
	public static final int TELEPORT_CHARGE_TIME = 2;
	public static final int TELEPORT_RANGE = 64;
	
	private static boolean DumbIntegratedGuard = false;
	
	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		Integer charge = EntityTeleportCharge.get(entityIn.getUniqueID());
		if (charge == null) {
			charge = 0;
		}
		
		if (worldIn.isRemote && entityIn == NostrumMagica.instance.proxy.getPlayer() && ((!DumbIntegratedGuard && charge == 0) || (DumbIntegratedGuard && charge == 2))) {
			entityIn.playSound(SoundEvents.BLOCK_PORTAL_TRIGGER, 1f, (4f / (float) TELEPORT_CHARGE_TIME));
		}
		
		if (!DumbIntegratedGuard) {
			charge += 2;
			DumbIntegratedGuard = true;
		}
		
		
		if (charge > TELEPORT_CHARGE_TIME * 20) {
			EntityTeleportCharge.put(entityIn.getUniqueID(), -(TELEPORT_CHARGE_TIME * 20));
			if (!worldIn.isRemote) {
				this.teleportEntity(worldIn, pos, entityIn);
			}
		} else {
			EntityTeleportCharge.put(entityIn.getUniqueID(), charge);
			if (worldIn.isRemote && charge >= 0) {
				int count = (charge / 20) / TELEPORT_CHARGE_TIME;
				for (int i = 0; i < count + 1; i++) {
					double dx = pos.getX() + .5;
					double dy = pos.getY() + .5;
					double dz = pos.getZ() + .5;
					
					double mx = .25 * (NostrumMagica.rand.nextFloat() - .5f);
					double my = .5 * (NostrumMagica.rand.nextFloat() - .5f);
					double mz = .25 * (NostrumMagica.rand.nextFloat() - .5f);
					worldIn.addParticle(ParticleTypes.DRAGON_BREATH, dx + mx, dy, dz + mz, mx / 3, my, mz / 3);
				}
			}
		}
	}
	
	public static void tick() {
		Iterator<UUID> it = EntityTeleportCharge.keySet().iterator();
		while (it.hasNext()) {
			UUID key = it.next();
			Integer charge = EntityTeleportCharge.get(key);
			if (charge != null && charge > 0) {
				charge--;
			}
			
			if (charge == null || charge == 0) {
				it.remove();
			} else {
				EntityTeleportCharge.put(key, charge);
			}
		}
		DumbIntegratedGuard = false;
	}
	
	public static void resetTimers() {
		EntityTeleportCharge.clear();
	}
	
	public static int getRemainingCharge(Entity ent) {
		Integer charge = EntityTeleportCharge.get(ent.getUniqueID());
		return TELEPORT_CHARGE_TIME - (charge == null ? 0 : charge) * 20; 
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		if (worldIn.isRemote) {
			return true;
		}
		
		ItemStack heldItem = playerIn.getHeldItem(hand);
		
		if (heldItem.isEmpty() || !(heldItem.getItem() instanceof PositionCrystal)) {
			return false;
		}
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof TeleportRuneTileEntity)) {
			return true;
		}
		
		TeleportRuneTileEntity ent = (TeleportRuneTileEntity) te;
		BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
		if (heldPos == null) {
			return true;
		}
		
		if (!playerIn.isCreative()) {
			// 1) has to be another teleport rune there, and 2) has to be within X blocks
			if (!NostrumMagica.isBlockLoaded(worldIn, heldPos)) {
				playerIn.sendMessage(new TranslationTextComponent("info.teleportrune.unloaded"));
				return true;
			}
			
			BlockState targetState = worldIn.getBlockState(heldPos);
			if (targetState == null || !(targetState.getBlock() instanceof TeleportRune)) {
				playerIn.sendMessage(new TranslationTextComponent("info.teleportrune.norune"));
				return true;
			}
			
			int dist = Math.abs(heldPos.getX() - pos.getX())
					+ Math.abs(heldPos.getY() - pos.getY())
					+ Math.abs(heldPos.getZ() - pos.getZ());
			
			boolean hasEnderBelt = false;
			// Look for lightning belt
			IInventory baubles = NostrumMagica.instance.curios.getCurios(playerIn);
			if (baubles != null) {
				for (int i = 0; i < baubles.getSizeInventory(); i++) {
					ItemStack stack = baubles.getStackInSlot(i);
					if (stack.isEmpty() || stack.getItem() != NostrumCurios.enderBelt) {
						continue;
					}
					
					hasEnderBelt = true;
					break;
				}
			}
			final boolean hasEnderSet = EnchantedArmor.GetSetCount(playerIn, EMagicElement.ENDER, EnchantedArmor.Type.TRUE) == 4;
			final double range = TELEPORT_RANGE * (hasEnderBelt ? 2 : 1) * (hasEnderSet ? 2 : 1);
			
			if (dist > range) {
				playerIn.sendMessage(new TranslationTextComponent("info.teleportrune.toofar"));
				return true;
			}
		}
		
		BlockState targetState = worldIn.getBlockState(heldPos);
		if (targetState != null && targetState.getBlock() instanceof TeleportRune) {
			;
		} else {
			heldPos = heldPos.up();
		}
		
		ent.setTargetPosition(heldPos);
		playerIn.sendMessage(new TranslationTextComponent("info.generic.block_linked"));
		
		// If creative, can target tele tiles that are pointing to other ones. But, if it's not pointing anywhere, we'll conveniently hook them up.
		// Non-creative placement forces them to be linked to eachother, though.
		boolean shouldLink = true;
		if (playerIn.isCreative()) {
			TileEntity otherTE = worldIn.getTileEntity(heldPos);
			if (otherTE != null && otherTE instanceof TeleportRuneTileEntity) {
				TeleportRuneTileEntity otherEnt = (TeleportRuneTileEntity) otherTE;
				shouldLink = (otherEnt.getOffset() == null);
			}
		}
		
		if (shouldLink) {
			BlockPos oldOffset = null;
			TileEntity otherTE = worldIn.getTileEntity(heldPos);
			if (otherTE != null && otherTE instanceof TeleportRuneTileEntity) {
				TeleportRuneTileEntity otherEnt = (TeleportRuneTileEntity) otherTE;
				oldOffset = otherEnt.getOffset();
				otherEnt.setTargetPosition(pos);
			}
			
			if (oldOffset != null && !playerIn.isCreative()) {
				// Unlink old one, too!
				otherTE = worldIn.getTileEntity(heldPos.add(oldOffset));
				if (otherTE != null && otherTE instanceof TeleportRuneTileEntity) {
					TeleportRuneTileEntity otherEnt = (TeleportRuneTileEntity) otherTE;
					otherEnt.setTargetPosition(null);
				}
			}
		}
		
		return true;
	}

	@Override
	public boolean hasTileEntity() {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new TeleportRuneTileEntity();
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof TeleportRuneTileEntity)) {
			return;
		}
		
		TeleportRuneTileEntity ent = (TeleportRuneTileEntity) te;
		if (ent.getOffset() != null) {
			double dx = pos.getX() + .5;
			double dy = pos.getY() + .1;
			double dz = pos.getZ() + .5;
			
			double mx = 1 * (rand.nextFloat() - .5f);
			double mz = 1 * (rand.nextFloat() - .5f);
			
			worldIn.addParticle(ParticleTypes.PORTAL, dx + mx, dy, dz + mz, mx / 3, 0.0D, mz / 3);
		}
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		// TODO Auto-generated method stub
		return null;
	}
}
