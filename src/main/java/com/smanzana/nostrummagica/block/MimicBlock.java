package com.smanzana.nostrummagica.block;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.MimicBlockTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelProperty;

@SuppressWarnings("deprecation")
public abstract class MimicBlock extends Block implements ITileEntityProvider {
	
	public static class MimicBlockData {
		public BlockState mimicState;
		
		public BlockState getBlockState() {
			return mimicState;
		}
	}
	
	public static final ModelProperty<MimicBlockData> MIMIC_MODEL_PROPERTY = new ModelProperty<>();
	
	public abstract @Nonnull BlockState getMimickedState(BlockState mimicBlockState, World world, BlockPos myPos);
	
	public MimicBlock(Block.Properties builder) {
		super(builder.notSolid());
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createNewTileEntity(IBlockReader world) {
		return new MimicBlockTileEntity();
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return createNewTileEntity(world);
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
	public boolean isReplaceable(BlockState state, BlockItemUseContext context) {
        return false;
    }
	
	protected boolean shouldRefreshFromNeighbor(BlockState state, World worldIn, BlockPos myPos, BlockPos fromPos) {
		return myPos.equals(fromPos.up());
	}
	
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
		
		if (shouldRefreshFromNeighbor(state, worldIn, pos, fromPos)) {
			// Block below changed, so refresh tile entity
			MimicBlockTileEntity te = (MimicBlockTileEntity) worldIn.getTileEntity(pos);
			te.updateBlock();
		}
	}
	
	// Mimiced block attributes
	@Override
	public int getOpacity(BlockState state, IBlockReader world, BlockPos pos) {
		return getValue(state, world, pos, BlockState::getOpacity, () -> super.getOpacity(state, world, pos));
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
		return getValue(state, world, pos, BlockState::getCollisionShape, () -> super.getCollisionShape(state, world, pos, context));
    }

	@Override
	public float getAmbientOcclusionLightValue(BlockState state, IBlockReader world, BlockPos pos) {
		return getValue(state, world, pos, BlockState::getAmbientOcclusionLightValue, () -> super.getAmbientOcclusionLightValue(state, world, pos));
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader world, BlockPos pos) {
		return getValue(state, world, pos, BlockState::propagatesSkylightDown, () -> super.propagatesSkylightDown(state, world, pos));
	}

//	@Override
//	public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
//		return true; // MimicBlockBakedModel checks wrapped state when rendered
//	}
	
	@Override
    public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
        return getValue(state, world, pos, (mirror, reader, pos1) -> mirror.getSoundType(reader, pos1, entity), () -> super.getSoundType(state, world, pos, entity));
    }

//    @Override
//    public int getPackedLightmapCoords(BlockState state, IBlockDisplayReader world, BlockPos pos) {
//        return getValue(state, world, pos, BlockState::getPackedLightmapCoords, () -> super.getPackedLightmapCoords(state, world, pos));
//    }

    @Nullable
    @Override
    public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos, @Nullable MobEntity entity) {
        return getValue(state, world, pos, (mirror, reader, pos1) -> mirror.getAiPathNodeType(reader, pos1, entity), () -> super.getAiPathNodeType(state, world, pos, entity));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return getValue(state, worldIn, pos, (mirror, reader, pos1) -> mirror.getShape(reader, pos1, context), () -> super.getShape(state, worldIn, pos, context));
    }

	@Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return getValue(state, worldIn, pos, BlockState::getRenderShape, () -> super.getRenderShape(state, worldIn, pos));
    }

    @Override
    public VoxelShape getRaytraceShape(BlockState state, IBlockReader world, BlockPos pos) {
        return getValue(state, world, pos, BlockState::getRayTraceShape, () -> super.getRaytraceShape(state, world, pos));
    }

//    @Nullable
//    @Override
//    public RayTraceResult getRayTraceResult(BlockState state, World world, BlockPos pos, Vector3d start, Vector3d end, RayTraceResult original) {
//        return getValue(state, world, pos, (mirror, reader, pos1) -> mirror.getBlock().getRayTraceResult(mirror, world, pos, start, end, original), () -> super.getRayTraceResult(state, world, pos, start, end, original));
//    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
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
    public boolean addRunningEffects(BlockState state, World world, BlockPos pos, Entity entity) {
        Optional<BlockState> mirrorState = getMirrorState(state, world, pos);
        if(mirrorState.isPresent()) {
            BlockState blockstate = mirrorState.get();
            if (blockstate.getRenderType() != BlockRenderType.INVISIBLE) {
                Vector3d Vector3d = entity.getMotion();
                world.addParticle(new BlockParticleData(ParticleTypes.BLOCK, blockstate),
                        entity.getPosX() + (world.rand.nextFloat() - 0.5D) * entity.getWidth(),
                        entity.getPosY() + 0.1D,
                        entity.getPosZ() + (world.rand.nextFloat() - 0.5D) * entity.getWidth(),

                        Vector3d.x * -4.0D, 1.5D, Vector3d.z * -4.0D);
            }
        }
        return true;
    }
    //ParticleManager#addBlockHitEffects

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addHitEffects(BlockState state, World world, RayTraceResult target, ParticleManager manager) {
        if(target instanceof BlockRayTraceResult) {
            BlockPos pos = ((BlockRayTraceResult) target).getPos();
            Optional<BlockState> mirrorState = getMirrorState(state, world, pos);
            if(mirrorState.isPresent()) {
                BlockState blockstate = mirrorState.get();
                Direction side = ((BlockRayTraceResult) target).getFace();
                if (blockstate.getRenderType() != BlockRenderType.INVISIBLE) {
                    int x = pos.getX();
                    int y = pos.getY();
                    int z = pos.getZ();
                    AxisAlignedBB bb = blockstate.getShape(world, pos).getBoundingBox();
                    double xPos = x + world.rand.nextDouble() * (bb.maxX - bb.minX - 0.2F) + 0.1F + bb.minX;
                    double yPos = y + world.rand.nextDouble() * (bb.maxY - bb.minY - 0.2F) + 0.1F + bb.minY;
                    double zPos = z + world.rand.nextDouble() * (bb.maxZ - bb.minZ - 0.2F) + 0.1F + bb.minZ;

                    switch (side) {
                        case UP: yPos = y + bb.maxY + 0.1F; break;
                        case DOWN: yPos = y + bb.minY - 0.1F; break;
                        case NORTH: zPos = z + bb.minZ - 0.1F; break;
                        case SOUTH: zPos = z + bb.maxZ + 0.1F; break;
                        case WEST: xPos = x + bb.minX - 0.1F; break;
                        case EAST: xPos = x + bb.maxX + 0.1F; break;
                    }

                    final Minecraft mc = Minecraft.getInstance();
                    mc.particles.addEffect(
                            new DiggingParticle((ClientWorld) world, xPos, yPos, zPos, 0.0D, 0.0D, 0.0D, blockstate)
                                    .setBlockPos(pos)
                                    .multiplyVelocity(0.2F)
                                    .multiplyParticleScaleBy(0.6F)
                    );
                }
            }
        }
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addDestroyEffects(BlockState stateIn, World world, BlockPos pos, ParticleManager manager) {
        Optional<BlockState> mirrorState = getMirrorState(stateIn, world, pos);
        if (mirrorState.isPresent()) {
            if(mirrorState.get().isAir(world, pos)) {
                return false;
            }
            BlockState state = mirrorState.get();
            VoxelShape voxelshape = state.getShape(world, pos);
            voxelshape.forEachBox((x1, y1, z1, x2, y2, z2) -> {
                double xDelta = Math.min(1.0D, x2 - x1);
                double yDelta = Math.min(1.0D, y2 - y1);
                double zDelta = Math.min(1.0D, z2 - z1);
                int xAmount = Math.max(2, MathHelper.ceil( xDelta / 0.25D));
                int yAmount = Math.max(2, MathHelper.ceil( yDelta / 0.25D));
                int zAmount = Math.max(2, MathHelper.ceil( zDelta / 0.25D));

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
                            mc.particles.addEffect(
                                    new DiggingParticle((ClientWorld) world,
                                            pos.getX() + xPos,pos.getY() + yPos, pos.getZ() + zPos,
                                            dx - 0.5D, dy - 0.5D,dz - 0.5D, state)
                                            .setBlockPos(pos)
                            );
                        }
                    }
                }
            });
        }
        return true;
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerWorld ServerWorld, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        Optional<BlockState> mirrorState = getMirrorState(state2, ServerWorld, pos);
        if(mirrorState.isPresent()) {
            ServerWorld.spawnParticle(new BlockParticleData(ParticleTypes.BLOCK, mirrorState.get()), entity.getPosX(), entity.getPosY(), entity.getPosZ(), numberOfParticles, 0.0D, 0.0D, 0.0D, 0.15F);
        }
        return true;
    }
	
	
	
	public static <T, W extends IBlockReader> T getValue(@Nullable BlockState state, W reader, BlockPos pos, StateFunction<T, W> function, Supplier<T> defaultValue) {
        return getMirrorState(state, reader, pos).map(mirror -> function.getValue(mirror, reader, pos)).orElseGet(defaultValue);
    }

    public static Optional<BlockState> getMirrorState(@Nullable BlockState state, IBlockReader world, BlockPos pos) {
        return getMirrorData(state, world, pos).map(MimicBlockData::getBlockState);
    }

    public static Optional<MimicBlockData> getMirrorData(@Nullable BlockState state, IBlockReader world, BlockPos pos) {
        if(world == null || pos == null) {
            return Optional.empty();
        }
        
        if (state == null) { // Important: state should be passed in during world gen to avoid a lookup that stalls.
        	state = world.getBlockState(pos);
        }
        
        TileEntity te = world.getTileEntity(pos);
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

    private interface StateFunction<T, W extends IBlockReader> {
        T getValue(BlockState mirror, W reader, BlockPos pos);
    }
}
