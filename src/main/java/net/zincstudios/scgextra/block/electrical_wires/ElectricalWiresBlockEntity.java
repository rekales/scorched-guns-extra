package net.zincstudios.scgextra.block.electrical_wires;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.zincstudios.scgextra.block.ModBlockEntities;

public class ElectricalWiresBlockEntity extends BlockEntity{
    private final EnergyStorage energyStorage = new EnergyStorage(2000) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (!simulate && received > 0) {
                setChanged();
                sync();
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (!simulate && extracted > 0) {
                setChanged();
                sync();
            }
            return extracted;
        }
        @Override
        public boolean canReceive() {
            if(this.getEnergyStored() < 2000){
                return true;
            }
            return false;
        };
    };
    private final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);
    public ElectricalWiresBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ELECTRICAL_WIRES.get(), pos, blockState);
    }
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Energy", energyStorage.serializeNBT());
    }
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energyStorage.deserializeNBT(tag.get("Energy"));
    }
    private void sync() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energy.cast();
        }
        return super.getCapability(cap, side);
    }
    public static void tick(Level level, BlockPos pos, BlockState state, ElectricalWiresBlockEntity blockEntity) {
        boolean wasLit = state.getValue(ElectricalWiresBlock.LIT);
        boolean isLit = false;
        if (!level.isClientSide) {
            if (blockEntity.hasEnoughEnergy(200)) {
                isLit = true;
            }

            if (wasLit != isLit) {
                level.setBlock(pos, state.setValue(ElectricalWiresBlock.LIT, isLit), 3);
            }
            for (Direction direction : Direction.values()) {
                BlockEntity adjacentEntity = level.getBlockEntity(pos.relative(direction));
                if (adjacentEntity != null) {
                    if(!(adjacentEntity instanceof ElectricalWiresBlockEntity ewbe)){
                        continue;
                    }
                    ewbe.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(handler -> {
                        if(ewbe.getEnergy()+200<=blockEntity.getEnergy()){
                            if (handler.canReceive()) {
                                int extracted = blockEntity.energyStorage.extractEnergy(200, true);
                                int accepted = handler.receiveEnergy(extracted, false);
                                blockEntity.energyStorage.extractEnergy(accepted, false);
                                blockEntity.setChanged();
                                blockEntity.sync();
                            }
                        }
                    });
                }
            }
        }
    }
    private boolean hasEnoughEnergy(int amount) {
        return energyStorage.getEnergyStored() >= amount;
    }

    public void consumeEnergy(int amount) {
        energyStorage.extractEnergy(amount, false);
        setChanged();
        sync();
    }

    public void addEnergy(int amount) {
        energyStorage.receiveEnergy(amount, false);
        setChanged();
        sync();
    }
    public int getEnergy(){
        return energyStorage.getEnergyStored();
    }
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energy.invalidate();
    }
}