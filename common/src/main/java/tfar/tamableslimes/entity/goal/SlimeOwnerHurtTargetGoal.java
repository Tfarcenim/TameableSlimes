package tfar.tamableslimes.entity.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import tfar.tamableslimes.TamableSlime;

import java.util.EnumSet;

public class SlimeOwnerHurtTargetGoal extends TargetGoal {
    private final TamableSlime slime;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public SlimeOwnerHurtTargetGoal(TamableSlime pTameAnimal) {
        super(pTameAnimal, false);
        this.slime = pTameAnimal;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        if (this.slime.isTame() && !this.slime.isOrderedToSit()) {
            LivingEntity livingentity = this.slime.getOwner();
            if (livingentity == null) {
                return false;
            } else {
                this.ownerLastHurt = livingentity.getLastHurtMob();
                int i = livingentity.getLastHurtMobTimestamp();
                return i != this.timestamp && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT) && this.slime.wantsToAttack(this.ownerLastHurt, livingentity);
            }
        } else {
            return false;
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        this.mob.setTarget(this.ownerLastHurt);
        LivingEntity livingentity = this.slime.getOwner();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtMobTimestamp();
        }

        super.start();
    }
}
