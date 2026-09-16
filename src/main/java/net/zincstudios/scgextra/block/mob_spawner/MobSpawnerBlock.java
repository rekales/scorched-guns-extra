package net.zincstudios.scgextra.block.mob_spawner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.zincstudios.scgextra.entity.whaler.WhalerEntities;


//I KNOW THIS IS NOT A GOOD WAY TO DO IT BUT IT WORKS FOR NOW, WILL CHANGE
public class MobSpawnerBlock extends Block{
    public MobSpawnerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        level.scheduleTick(pos, this, 0);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        AABB box = new AABB(
            pos.getX()-5,
            pos.getY(),
            pos.getZ()-5,
            pos.getX()+5,
            pos.getY()+5,
            pos.getZ()+5
        );
        for(int i = 0; i < 5; i++){
            int n = random.nextInt(10);
            BlockPos spawnPos = new BlockPos(
                random.nextInt((int)box.minX, (int)box.maxX),
                random.nextInt((int)box.minY, (int)box.maxY),
                random.nextInt((int)box.minZ, (int)box.maxZ)
            );
            if(n >= 0 && n <= 8){
                WhalerEntities.FISH_FOLK.get().spawn(level, spawnPos, MobSpawnType.MOB_SUMMONED);
            }else{
                int nn = random.nextInt(3);
                switch (nn) {
                    case 0:
                        WhalerEntities.PUFFICUS.get().spawn(level, spawnPos, MobSpawnType.MOB_SUMMONED);
                        break;
                    case 1:
                        WhalerEntities.TENTACLIATOR.get().spawn(level, spawnPos, MobSpawnType.MOB_SUMMONED);
                        break;
                    case 2:
                        WhalerEntities.TURTLEMAN.get().spawn(level, spawnPos, MobSpawnType.MOB_SUMMONED);
                        break;
                    default:
                        break;
                }
            }
        }
        level.setBlock(pos, Blocks.WATER.defaultBlockState(), UPDATE_ALL);
    }
}
