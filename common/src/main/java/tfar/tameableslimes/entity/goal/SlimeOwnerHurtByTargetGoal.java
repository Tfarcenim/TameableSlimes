package tfar.tameableslimes.entity.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import tfar.tameableslimes.TameableSlime;

import java.util.EnumSet;

public class SlimeOwnerHurtByTargetGoal extends TargetGoal {
    private final TameableSlime tameAnimal;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public SlimeOwnerHurtByTargetGoal(TameableSlime $$0) {
        super($$0, false);
        this.tameAnimal = $$0;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public boolean canUse() {
        if (this.tameAnimal.isTame() && !this.tameAnimal.isOrderedToSit()) {
            LivingEntity owner = this.tameAnimal.getOwner();
            if (owner == null) {
                return false;
            } else {
                this.ownerLastHurtBy = owner.getLastHurtByMob();
                int $$1 = owner.getLastHurtByMobTimestamp();
                return $$1 != this.timestamp && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT) && this.tameAnimal.wantsToAttack(this.ownerLastHurtBy, owner);
            }
        } else {
            return false;
        }
    }

    public void start() {
        this.mob.setTarget(this.ownerLastHurtBy);
        LivingEntity $$0 = this.tameAnimal.getOwner();
        if ($$0 != null) {
            this.timestamp = $$0.getLastHurtByMobTimestamp();
        }

        super.start();
    }
}
