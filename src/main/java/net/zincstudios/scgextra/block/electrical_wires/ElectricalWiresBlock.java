package net.zincstudios.scgextra.block.electrical_wires;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zincstudios.scgextra.block.ModBlockEntities;
import top.ribs.scguns.init.ModEffects;
import net.minecraft.world.level.block.Rotation;

@SuppressWarnings("deprecation")
public class ElectricalWiresBlock extends BaseEntityBlock{
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    protected static final int AABB_OFFSET = 1;
    protected static final VoxelShape COLLISION_SHAPE = Block.box((double)1.0F, (double)0.0F, (double)1.0F, (double)15.0F, (double)15.0F, (double)15.0F);
    protected static final VoxelShape OUTLINE_SHAPE = Block.box((double)1.0F, (double)0.0F, (double)1.0F, (double)15.0F, (double)16.0F, (double)15.0F);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private String tag = "HurtByBarbedWiresInt";

    public ElectricalWiresBlock(Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(LIT, false).setValue(FACING, Direction.NORTH));
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return (BlockState)this.defaultBlockState().setValue(LIT, false).setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
        builder.add(FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos arg0, BlockState arg1) {
        return new ElectricalWiresBlockEntity(arg0, arg1);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.ELECTRICAL_WIRES.get(), ElectricalWiresBlockEntity::tick);
    }
    public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if(!level.isClientSide()){
            ElectricalWiresBlockEntity ewbe = (ElectricalWiresBlockEntity)level.getBlockEntity(pos);
            if(level.getGameTime() % 20 == 0){
                if(ewbe.getEnergy()>=20){
                    entity.hurt(entity.damageSources().generic(), 4);
                }else{
                    entity.hurt(entity.damageSources().generic(), 2);
                }
                if(!entity.getPersistentData().contains(tag)){
                    entity.getPersistentData().putInt(tag, 0);
                }
                entity.getPersistentData().putInt(tag, entity.getPersistentData().getInt(tag)+1);
            }
            if(entity.getPersistentData().getInt(tag) ==3){
                if(ewbe.getEnergy()>=20){
                    if(entity instanceof LivingEntity le){
                        le.addEffect(new MobEffectInstance(ModEffects.LACERATED.get(), 60));
                        le.setSecondsOnFire(3);
                    }
                }
                entity.getPersistentData().putInt(tag, 0);
            }
            if(ewbe.getEnergy()>=20){
                ewbe.consumeEnergy(20);
            }
        }
        entity.makeStuckInBlock(state, new Vec3((double)0.25F, (double)0.05F, (double)0.25F));
    }
}