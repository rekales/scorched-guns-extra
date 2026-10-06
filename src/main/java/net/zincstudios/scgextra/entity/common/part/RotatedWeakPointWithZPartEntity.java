package net.zincstudios.scgextra.entity.common.part;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RotatedWeakPointWithZPartEntity<T extends LivingEntity> extends RotatedWeakPointPartEntity<T>{

    private final float x;
    private final float y;
    private final float z;

    public RotatedWeakPointWithZPartEntity(T parent, Vec3 offset, float width, float height, float z) {
        super(parent, offset, width, height);
        this.x = width;
        this.y = height;
        this.z = z;
    }
    
    public RotatedWeakPointWithZPartEntity(T parent, Vec3 offset, float width, float height, float z, boolean collision) {
        super(parent, offset, width, height, collision);
        this.x = width;
        this.y = height;
        this.z = z;
    }
    @Override
    protected AABB getBoundingBoxForPose(Pose pPose) {
        float fx = this.x / 2;
        float fy = this.y;
        float fz = this.z;
        return new AABB(
            this.getX() - (double)fx, 
            this.getY(), 
            this.getZ() - (double)fz, 
            this.getX() + (double)fx, 
            this.getY() + (double)fy, 
            this.getZ() + (double)fz
        );
    }
    @Override
    protected AABB makeBoundingBox() {
        float fx = this.x / 2;
        float fy = this.y;
        float fz = this.z;
        return new AABB(
            this.getX() - (double)fx, 
            this.getY(), 
            this.getZ() - (double)fz, 
            this.getX() + (double)fx, 
            this.getY() + (double)fy, 
            this.getZ() + (double)fz
        );
    }
    @Override
    public void tick() {
        super.tick();
        this.setYBodyRot(this.getParent().yBodyRot);
        // this.setYRot(this.getParent().getYRot());
        // this.setYHeadRot(this.getParent().getYHeadRot());
    }
}
