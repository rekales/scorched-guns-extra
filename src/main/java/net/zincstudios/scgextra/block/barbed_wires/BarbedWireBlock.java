package net.zincstudios.scgextra.block.barbed_wires;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import top.ribs.scguns.init.ModEffects;

@SuppressWarnings("deprecation")
public class BarbedWireBlock extends Block{
    Random random = new Random();
    private String tag = "HurtByBarbedWiresInt";
    protected static final int AABB_OFFSET = 1;
    protected static final VoxelShape COLLISION_SHAPE = Block.box((double)1.0F, (double)0.0F, (double)1.0F, (double)15.0F, (double)15.0F, (double)15.0F);
    protected static final VoxelShape OUTLINE_SHAPE = Block.box((double)1.0F, (double)0.0F, (double)1.0F, (double)15.0F, (double)16.0F, (double)15.0F);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public BarbedWireBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if(!level.isClientSide()){
            if(level.getGameTime() % 20 == 0){
                entity.hurt(entity.damageSources().generic(), 2);
                if(!entity.getPersistentData().contains(tag)){
                    entity.getPersistentData().putInt(tag, 0);
                }
                entity.getPersistentData().putInt(tag, entity.getPersistentData().getInt(tag)+1);
            }
            if(entity.getPersistentData().getInt(tag) ==3){
                if(entity instanceof LivingEntity le){
                    le.addEffect(new MobEffectInstance(ModEffects.LACERATED.get(), 60));
                }
                entity.getPersistentData().putInt(tag, 0);
            }
        }
        entity.makeStuckInBlock(state, new Vec3((double)0.25F, (double)0.05F, (double)0.25F));
    }

    public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE_SHAPE;
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }
}