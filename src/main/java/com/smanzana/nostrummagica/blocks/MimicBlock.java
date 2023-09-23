package com.smanzana.nostrummagica.blocks;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.MimicBlockTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;

@SuppressWarnings("deprecation")
public class MimicBlock extends DirectionalBlock implements ITileEntityProvider {
	
	public static class MimicBlockData {
		public BlockState mimicState;
		
		public BlockState getBlockState() {
			return mimicState;
		}
	}
	
//	public static IUnlistedProperty<BlockState> NESTED_STATE = new IUnlistedProperty<BlockState>() {
//
//		@Override
//		public String getName() {
//			return "Mimic::NestedState";
//		}
//
//		@Override
//		public boolean isValid(BlockState value) {
//			return value != null;
//		}
//
//		@Override
//		public Class<BlockState> getType() {
//			return BlockState.class;
//		}
//
//		@Override
//		public String valueToString(BlockState value) {
//			return value.toString();
//		}
//		
//	};
	
	public static BooleanProperty UNBREAKABLE = BooleanProperty.create("unbreakable");

	public static final String ID_DOOR = "mimic_door";
	public static final String ID_FACADE = "mimic_facade";
	
	private final boolean isDoor;
	
	public MimicBlock(boolean isDoor) {
		super(Block.Properties.create(Material.GLASS)
				.hardnessAndResistance(1.0f)
				.variableOpacity()
				);
		
		this.isDoor = isDoor;
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING).add(UNBREAKABLE);
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
	
	@Override
	public float getBlockHardness(BlockState blockState, IBlockReader worldIn, BlockPos pos) {
		return blockState.get(UNBREAKABLE) ? -1f : super.getBlockHardness(blockState, worldIn, pos);
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		boolean solid = false;
		
		if (!isDoor) {
			solid = false;
		} else if (context.getEntity() != null) {
			final Entity entityIn = context.getEntity();
			final AxisAlignedBB entityBox = entityIn.getCollisionBoundingBox();
			Direction side = state.get(FACING);
			// cant use getCenter cause it's client-side only
			//Vec3d center = entityBox.getCenter();
			Vec3d center = new Vec3d(entityBox.minX + (entityBox.maxX - entityBox.minX) * 0.5D, entityBox.minY + (entityBox.maxY - entityBox.minY) * 0.5D, entityBox.minZ + (entityBox.maxZ - entityBox.minZ) * 0.5D);
			
			// XZ motion isn't stored on the server and is handled client-side
			// Server also resets lastPos in an inconvenient way.
			final double dx;
			final double dz;
			if (entityIn instanceof PlayerEntity) {
				dx = entityIn.world.isRemote()
						? (entityIn.getMotion().x)
						: (entityIn.posX - NostrumMagica.playerListener.getLastTickPos(entityIn).x);
				dz = entityIn.world.isRemote()
						? (entityIn.getMotion().z)
						: (entityIn.posZ - NostrumMagica.playerListener.getLastTickPos(entityIn).z);
			} else {
				dx = entityIn.getMotion().x;
				dz = entityIn.getMotion().z;
			}
			
//			final double dx = worldIn.isRemote
//					? (entityIn.getMotion().x)
//					: (entityIn.posX - NostrumMagica.playerListener.getLastTickPos(entityIn).x);
//			final double dz = worldIn.isRemote
//					? (entityIn.getMotion().z)
//					: (entityIn.posZ - NostrumMagica.playerListener.getLastTickPos(entityIn).z);
			
			// Offset center back to old position to prevent sneaking back inside!
			center = center.add(-dx, 0, -dz);
			
			switch (side) {
			case DOWN:
				solid = center.y < pos.getY() && entityIn.getMotion().y >= 0;
				break;
			case EAST:
				solid = center.x > pos.getX() + 1 && dx <= 0;
				break;
			case NORTH:
				solid = center.z < pos.getZ() && dz >= 0;
				break;
			case SOUTH:
				solid = center.z > pos.getZ() + 1 && dz <= 0;
				break;
			case UP:
			default:
				solid = center.y > pos.getY() + 1 && entityIn.getMotion().y <= 0;
				break;
			case WEST:
				solid = center.x < pos.getX() && dx >= 0;
				break;
			}
		}
		
		if (solid) {
			return VoxelShapes.fullCube();
		} else {
			return VoxelShapes.empty();
		}
    }
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		//Direction enumfacing = Direction.getHorizontal(MathHelper.floor_double((double)(placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3).getOpposite();
		return this.getDefaultState()
				.with(FACING,context.getNearestLookingDirection())
				;
	}
	
	@Override
	public boolean isReplaceable(BlockState state, BlockItemUseContext context) {
        return false;
    }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
		return side != state.get(FACING);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onBlockHighlight(DrawBlockHighlightEvent event) {
		if (event.getTarget().getType() == RayTraceResult.Type.BLOCK) {
			BlockPos pos = new BlockPos(event.getTarget().getHitVec());
			BlockState hit = event.getInfo().getRenderViewEntity().world.getBlockState(pos);
			if (hit != null && hit.getBlock() == this) {
				Direction face = hit.get(FACING);
				boolean outside = false;
				
				switch (face) {
				case DOWN:
					outside = event.getInfo().getProjectedView().y < pos.getY();
					break;
				case EAST:
					outside = event.getInfo().getProjectedView().x > pos.getX() + 1;
					break;
				case NORTH:
					outside = event.getInfo().getProjectedView().z < pos.getZ();
					break;
				case SOUTH:
					outside = event.getInfo().getProjectedView().z > pos.getZ() + 1;
					break;
				case UP:
				default:
					outside =  event.getInfo().getProjectedView().y > pos.getY() + 1;
					break;
				case WEST:
					outside = event.getInfo().getProjectedView().x < pos.getX();
					break;
				}
				
				if (!outside) {
					event.setCanceled(true);
				}
				return;
			}
		}
	}
	
	// Mimiced block attributes
	@Override
	public int getOpacity(BlockState state, IBlockReader world, BlockPos pos) {
		return getValue(world, pos, BlockState::getOpacity, () -> super.getOpacity(state, world, pos));
	}

	@Override
	public float func_220080_a(BlockState state, IBlockReader world, BlockPos pos) {
		return getValue(world, pos, BlockState::func_215703_d, () -> super.func_220080_a(state, world, pos));
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader world, BlockPos pos) {
		return getValue(world, pos, BlockState::propagatesSkylightDown, () -> super.propagatesSkylightDown(state, world, pos));
	}

	@Override
	public boolean isNormalCube(BlockState state, IBlockReader world, BlockPos pos) {
		return getValue(world, pos, BlockState::isNormalCube, () -> super.isNormalCube(state, world, pos));
	}

	@Override
	public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
		return true;
	}
	
	@Override
    public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
        return getValue(world, pos, (mirror, reader, pos1) -> mirror.getSoundType(reader, pos1, entity), () -> super.getSoundType(state, world, pos, entity));
    }

    @Override
    public int getPackedLightmapCoords(BlockState state, IEnviromentBlockReader world, BlockPos pos) {
        return getValue(world, pos, BlockState::getPackedLightmapCoords, () -> super.getPackedLightmapCoords(state, world, pos));
    }

    @Nullable
    @Override
    public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos, @Nullable MobEntity entity) {
        return getValue(world, pos, (mirror, reader, pos1) -> mirror.getAiPathNodeType(reader, pos1, entity), () -> super.getAiPathNodeType(state, world, pos, entity));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return getValue(worldIn, pos, (mirror, reader, pos1) -> mirror.getShape(reader, pos1, context), () -> super.getShape(state, worldIn, pos, context));
    }

	@Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return getValue(worldIn, pos, BlockState::getRenderShape, () -> super.getRenderShape(state, worldIn, pos));
    }

    @Override
    public VoxelShape getRaytraceShape(BlockState state, IBlockReader world, BlockPos pos) {
        return getValue(world, pos, BlockState::getRaytraceShape, () -> super.getRaytraceShape(state, world, pos));
    }

    @Nullable
    @Override
    public RayTraceResult getRayTraceResult(BlockState state, World world, BlockPos pos, Vec3d start, Vec3d end, RayTraceResult original) {
        return getValue(world, pos, (mirror, reader, pos1) -> mirror.getBlock().getRayTraceResult(mirror, world, pos, start, end, original), () -> super.getRayTraceResult(state, world, pos, start, end, original));
    }

    @Override
    public int getLightValue(BlockState state, IEnviromentBlockReader world, BlockPos pos) {
        int result = getValue(world, pos, BlockState::getLightValue, () -> super.getLightValue(state, world, pos));
        //This is needed so we can control AO. Try to remove this asap
        if ("net.minecraft.client.renderer.BlockModelRenderer".equals(Thread.currentThread().getStackTrace()[3].getClassName())) {
            Optional<BlockState> mirrorState = getMirrorState(world, pos);
            if(mirrorState.isPresent()) {
                Boolean isAoModel = DistExecutor.callWhenOn(Dist.CLIENT, () -> () ->
                    Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(mirrorState.get()).isAmbientOcclusion());
                if(isAoModel != null) {
                    return result == 0 && isAoModel ? 0 : 1;
                }
            }
        }
        return result;
    }
    
  //Entity#createRunningParticles
    @Override
    public boolean addRunningEffects(BlockState state, World world, BlockPos pos, Entity entity) {
        Optional<BlockState> mirrorState = getMirrorState(world, pos);
        if(mirrorState.isPresent()) {
            BlockState blockstate = mirrorState.get();
            if (blockstate.getRenderType() != BlockRenderType.INVISIBLE) {
                Vec3d vec3d = entity.getMotion();
                world.addParticle(new BlockParticleData(ParticleTypes.BLOCK, blockstate),
                        entity.posX + (world.rand.nextFloat() - 0.5D) * entity.getWidth(),
                        entity.posY + 0.1D,
                        entity.posZ + (world.rand.nextFloat() - 0.5D) * entity.getWidth(),

                        vec3d.x * -4.0D, 1.5D, vec3d.z * -4.0D);
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
            Optional<BlockState> mirrorState = getMirrorState(world, pos);
            if(mirrorState.isPresent()) {
                BlockState blockstate = mirrorState.get();
                Direction side = ((BlockRayTraceResult) target).getFace();
                if (blockstate.getRenderType() != BlockRenderType.INVISIBLE) {
                    int x = pos.getX();
                    int y = pos.getY();
                    int z = pos.getZ();
                    AxisAlignedBB axisalignedbb = blockstate.getShape(world, pos).getBoundingBox();
                    double xPos = x + world.rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.2F) + 0.1F + axisalignedbb.minX;
                    double yPos = y + world.rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.2F) + 0.1F + axisalignedbb.minY;
                    double zPos = z + world.rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.2F) + 0.1F + axisalignedbb.minZ;

                    switch (side) {
                        case UP: yPos = y + axisalignedbb.maxY + 0.1F; break;
                        case DOWN: yPos = y + axisalignedbb.minY - 0.1F; break;
                        case NORTH: zPos = z + axisalignedbb.minZ - 0.1F; break;
                        case SOUTH: zPos = z + axisalignedbb.maxZ + 0.1F; break;
                        case WEST: xPos = x + axisalignedbb.minX - 0.1F; break;
                        case EAST: xPos = x + axisalignedbb.maxX + 0.1F; break;
                    }

                    final Minecraft mc = Minecraft.getInstance();
                    mc.particles.addEffect(
                            new DiggingParticle(world, xPos, yPos, zPos, 0.0D, 0.0D, 0.0D, blockstate)
                                    .setBlockPos(pos)
                                    .multiplyVelocity(0.2F)
                                    .multipleParticleScaleBy(0.6F)
                    );
                }
            }
        }
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addDestroyEffects(BlockState stateIn, World world, BlockPos pos, ParticleManager manager) {
        Optional<BlockState> mirrorState = getMirrorState(world, pos);
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
                                    new DiggingParticle(world,
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
    public boolean addLandingEffects(BlockState state1, ServerWorld worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        Optional<BlockState> mirrorState = getMirrorState(worldserver, pos);
        if(mirrorState.isPresent()) {
            worldserver.spawnParticle(new BlockParticleData(ParticleTypes.BLOCK, mirrorState.get()), entity.posX, entity.posY, entity.posZ, numberOfParticles, 0.0D, 0.0D, 0.0D, 0.15F);
        }
        return true;
    }
	
	
	
	public static <T, W extends IBlockReader> T getValue(W reader, BlockPos pos, StateFunction<T, W> function, Supplier<T> defaultValue) {
        return getMirrorState(reader, pos).map(mirror -> function.getValue(mirror, reader, pos)).orElseGet(defaultValue);
    }

    public static Optional<BlockState> getMirrorState(IBlockReader world, BlockPos pos) {
        return getMirrorData(world, pos).map(MimicBlockData::getBlockState);
    }

    public static Optional<MimicBlockData> getMirrorData(IBlockReader world, BlockPos pos) {
        if(world == null || pos == null) {
            return Optional.empty();
        }
        TileEntity te = world.getTileEntity(pos);
        return world.getBlockState(pos).getBlock() instanceof MimicBlock && te instanceof MimicBlockTileEntity ?
            Optional.of(((MimicBlockTileEntity) te).getData()) : Optional.empty();
    }

    public static void requestModelRefresh(IBlockReader world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity != null) {
            tileEntity.requestModelDataUpdate();
        }
    }

    private interface StateFunction<T, W extends IBlockReader> {
        T getValue(BlockState mirror, W reader, BlockPos pos);
    }
}
