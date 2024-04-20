package tfar.tamableslimes.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tfar.tamableslimes.TamableSlimes;

@Mixin(Slime.class)//todo, neoforge adds MobSplitEvent in 1.20.4
public class SlimeMixin {

    @Inject(method = "remove",at = @At(value = "INVOKE",target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z")
            ,locals = LocalCapture.CAPTURE_FAILHARD)
    private void onSlimeSplit(Entity.RemovalReason removalReason, CallbackInfo ci, int $$1, Component $$2, boolean $$3,
                              float $$4, int $$5, int $$6, int $$7, float $$8, float $$9, Slime slime) {
        TamableSlimes.onSlimeSplit((Slime) (Object)this,slime);
    }

}
