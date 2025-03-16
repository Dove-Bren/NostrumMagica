package com.smanzana.nostrummagica.block.dungeon;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.MimicBlockTileEntity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelProperty;

@SuppressWarnings("deprecation")
public abstract class MimicBlock extends Block implements EntityBlock {
	
	public static class MimicBlockData {
		public BlockState mimicState;
		
		public BlockState getBlockState() {
			return mimicState;
		}
	}
	
	public static final ModelProperty<MimicBlockData> MIMIC_MODEL_PROPERTY = new ModelProperty<>();
	
	public abstract @Nonnull BlockState getMimickedState(BlockState mimicBlockState, Level world, BlockPos myPos);
	
	public MimicBlock(Block.Properties builder) {
		super(builder.noOcclusion());
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockGetter world) {
		return new MimicBlockTileEntity();
	}
	
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		return newBlockEntity(world);
	}
	
//	@Override
//	public float getBlockHardness(BlockState blockState, IBlockReader worldIn, BlockPos pos) {
//		return blockState.get(UNBREAKABLE) ? -1f : super.getBlockHardness(blockState, worldIn, pos);
//	}
	
//	@Override
//	public BlockState getStateForPlacement(BlockItemUseContext context) {
//		return this.getDefaultState(context);
//	}
	
	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return false;
    }
	
	protected boolean shouldRefreshFromNeighbor(BlockState state, Level worldIn, BlockPos myPos, BlockPos fromPos) {
		return myPos.equals(fromPos.above());
	}
	
	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
		
		if (shouldRefreshFromNeighbor(state, worldIn, pos, fromPos)) {
			// Block below changed, so refresh tile entity
			MimicBlockTileEntity te = (MimicBlockTileEntity) worldIn.getBlockEntity(pos);
			te.updateBlock();
		}
	}
	
	// Mimiced block attributes
	@Override
	public int getLightBlock(BlockState state, BlockGetter world, BlockPos pos) {
		return getValue(state, world, pos, BlockState::getLightBlock, () -> super.getLightBlock(state, world, pos));
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return getValue(state, world, pos, BlockState::getCollisionShape, () -> super.getCollisionShape(state, world, pos, context));
    }

	@Override
	public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
		return getValue(state, world, pos, BlockState::getShadeBrightness, () -> super.getShadeBrightness(state, world, pos));
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos) {
		return getValue(state, world, pos, BlockState::propagatesSkylightDown, () -> super.propagatesSkylightDown(state, world, pos));
	}

//	@Override
//	public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
//		return true; // MimicBlockBakedModel checks wrapped state when rendered
//	}
	
	@Override
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity entity) {
        return getValue(state, world, pos, (mirror, reader, pos1) -> mirror.getSoundType(reader, pos1, entity), () -> super.getSoundType(state, world, pos, entity));
    }

//    @Override
//    public int getPackedLightmapCoords(BlockState state, IBlockDisplayReader world, BlockPos pos) {
//        return getValue(state, world, pos, BlockState::getPackedLightmapCoords, () -> super.getPackedLightmapCoords(state, world, pos));
//    }

    @Nullable
    @Override
    public BlockPathTypes getAiPathNodeType(BlockState state, BlockGetter world, BlockPos pos, @Nullable Mob entity) {
        return getValue(state, world, pos, (mirror, reader, pos1) -> mirror.getAiPathNodeType(reader, pos1, entity), () -> super.getAiPathNodeType(state, world, pos, entity));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return getValue(state, worldIn, pos, (mirror, reader, pos1) -> mirror.getShape(reader, pos1, context), () -> super.getShape(state, worldIn, pos, context));
    }

	@Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return getValue(state, worldIn, pos, BlockState::getBlockSupportShape, () -> super.getOcclusionShape(state, worldIn, pos));
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return getValue(state, world, pos, BlockState::getInteractionShape, () -> super.getInteractionShape(state, world, pos));
    }

//    @Nullable
//    @Override
//    public RayTraceResult getRayTraceResult(BlockState state, World world, BlockPos pos, Vector3d start, Vector3d end, RayTraceResult original) {
//        return getValue(state, world, pos, (mirror, reader, pos1) -> mirror.getBlock().getRayTraceResult(mirror, world, pos, start, end, original), () -> super.getRayTraceResult(state, world, pos, start, end, original));
//    }

    @Override
    public int getLightValue(BlockState state, BlockGetter world, BlockPos pos) {
        int result = getValue(state, world, pos, BlockState::getLightValue, () -> super.getLightValue(state, world, pos));
        // Copied from secret rooms mod, b ut it's SO SLOW because getting the stacktrace on a thread is not fast. Kills performance!
//        //This is needed so we can control AO. Try to remove this asap
//        if ("net.minecraft.client.renderer.BlockModelRenderer".equals(Thread.currentThread().getStackTrace()[3].getClassName())) {
//            Optional<BlockState> mirrorState = getMirrorState(state, world, pos);
//            if(mirrorState.isPresent()) {
//                Boolean isAoModel = DistExecutor.callWhenOn(Dist.CLIENT, () -> () ->
//                    Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(mirrorState.get()).isAmbientOcclusion());
//                if(isAoModel != null) {
//                    return result == 0 && isAoModel ? 0 : 1;
//                }
//            }
//        }
        return result;
    }
    
  //Entity#createRunningParticles
    @Override
    public boolean addRunningEffects(BlockState state, Level world, BlockPos pos, Entity entity) {
        Optional<BlockState> mirrorState = getMirrorState(state, world, pos);
        if(mirrorState.isPresent()) {
            BlockState blockstate = mirrorState.get();
            if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                Vec3 Vector3d = entity.getDeltaMovement();
                world.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate),
                        entity.getX() + (world.random.nextFloat() - 0.5D) * entity.getBbWidth(),
                        entity.getY() + 0.1D,
                        entity.getZ() + (world.random.nextFloat() - 0.5D) * entity.getBbWidth(),

                        Vector3d.x * -4.0D, 1.5D, Vector3d.z * -4.0D);
            }
        }
        return true;
    }
    //ParticleManager#addBlockHitEffects

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addHitEffects(BlockState state, Level world, HitResult target, ParticleEngine manager) {
        if(target instanceof BlockHitResult) {
            BlockPos pos = ((BlockHitResult) target).getBlockPos();
            Optional<BlockState> mirrorState = getMirrorState(state, world, pos);
            if(mirrorState.isPresent()) {
                BlockState blockstate = mirrorState.get();
                Direction side = ((BlockHitResult) target).getDirection();
                if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                    int x = pos.getX();
                    int y = pos.getY();
                    int z = pos.getZ();
                    AABB bb = blockstate.getShape(world, pos).bounds();
                    double xPos = x + world.random.nextDouble() * (bb.maxX - bb.minX - 0.2F) + 0.1F + bb.minX;
                    double yPos = y + world.random.nextDouble() * (bb.maxY - bb.minY - 0.2F) + 0.1F + bb.minY;
                    double zPos = z + world.random.nextDouble() * (bb.maxZ - bb.minZ - 0.2F) + 0.1F + bb.minZ;

                    switch (side) {
                        case UP: yPos = y + bb.maxY + 0.1F; break;
                        case DOWN: yPos = y + bb.minY - 0.1F; break;
                        case NORTH: zPos = z + bb.minZ - 0.1F; break;
                        case SOUTH: zPos = z + bb.maxZ + 0.1F; break;
                        case WEST: xPos = x + bb.minX - 0.1F; break;
                        case EAST: xPos = x + bb.maxX + 0.1F; break;
                    }

                    final Minecraft mc = Minecraft.getInstance();
                    mc.particleEngine.add(
                            new TerrainParticle((ClientLevel) world, xPos, yPos, zPos, 0.0D, 0.0D, 0.0D, blockstate)
                                    .init(pos)
                                    .setPower(0.2F)
                                    .scale(0.6F)
                    );
                }
            }
        }
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addDestroyEffects(BlockState stateIn, Level world, BlockPos pos, ParticleEngine manager) {
        Optional<BlockState> mirrorState = getMirrorState(stateIn, world, pos);
        if (mirrorState.isPresent()) {
            if(mirrorState.get().isAir(world, pos)) {
                return false;
            }
            BlockState state = mirrorState.get();
            VoxelShape voxelshape = state.getShape(world, pos);
            voxelshape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
                double xDelta = Math.min(1.0D, x2 - x1);
                double yDelta = Math.min(1.0D, y2 - y1);
                double zDelta = Math.min(1.0D, z2 - z1);
                int xAmount = Math.max(2, Mth.ceil( xDelta / 0.25D));
                int yAmount = Math.max(2, Mth.ceil( yDelta / 0.25D));
                int zAmount = Math.max(2, Mth.ceil( zDelta / 0.25D));

                for(int x = 0; x < xAmount; ++x) {
                    for(int y = 0; y < yAmount; ++y) {
                        for(int z = 0; z < zAmount; ++z) {
                            double dx = (x + 0.5D) / xAmount;
                            double dy = (y + 0.5D) / yAmount;
                            double dz = (z + 0.5D) / zAmount;
                            double xPos = dx * xDelta + x1;
                            double yPos = dy * yDelta + y1;
                            double zPos = dz * zDelta + z1;
                            
                            final Minecraft mc = Minecraft.getInstance();
                            mc.particleEngine.add(
                                    new TerrainParticle((ClientLevel) world,
                                            pos.getX() + xPos,pos.getY() + yPos, pos.getZ() + zPos,
                                            dx - 0.5D, dy - 0.5D,dz - 0.5D, state)
                                            .init(pos)
                            );
                        }
                    }
                }
            });
        }
        return true;
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel ServerWorld, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        Optional<BlockState> mirrorState = getMirrorState(state2, ServerWorld, pos);
        if(mirrorState.isPresent()) {
            ServerWorld.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, mirrorState.get()), entity.getX(), entity.getY(), entity.getZ(), numberOfParticles, 0.0D, 0.0D, 0.0D, 0.15F);
        }
        return true;
    }
	
	
	
	public static <T, W extends BlockGetter> T getValue(@Nullable BlockState state, W reader, BlockPos pos, StateFunction<T, W> function, Supplier<T> defaultValue) {
        return getMirrorState(state, reader, pos).map(mirror -> function.getValue(mirror, reader, pos)).orElseGet(defaultValue);
    }

    public static Optional<BlockState> getMirrorState(@Nullable BlockState state, BlockGetter world, BlockPos pos) {
        return getMirrorData(state, world, pos).map(MimicBlockData::getBlockState);
    }

    public static Optional<MimicBlockData> getMirrorData(@Nullable BlockState state, BlockGetter world, BlockPos pos) {
        if(world == null || pos == null) {
            return Optional.empty();
        }
        
        if (state == null) { // Important: state should be passed in during world gen to avoid a lookup that stalls.
        	state = world.getBlockState(pos);
        }
        
        BlockEntity te = world.getBlockEntity(pos);
        if (state.getBlock() instanceof MimicBlock && te instanceof MimicBlockTileEntity) {
        	
        	MimicBlockData data = ((MimicBlockTileEntity) te).getData();
        	if (data.getBlockState() != null && !(data.getBlockState().getBlock() instanceof MimicBlock)) {
        		return Optional.of(data);
        	} else {
        		if (data.getBlockState() != null) {
        			NostrumMagica.logger.warn("Mimic block is mimicking another mimic block? Cascade did not work.");
        		}
        		return Optional.empty();
        	}
        } else {
        	return Optional.empty();
        }
    }

    private interface StateFunction<T, W extends BlockGetter> {
        T getValue(BlockState mirror, W reader, BlockPos pos);
    }
}
