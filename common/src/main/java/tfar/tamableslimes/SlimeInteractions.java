package tfar.tamableslimes;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.HashMap;
import java.util.Map;

public class SlimeInteractions {

    public static final Map<Item, EntityInteract> INTERACTIONS = new HashMap<>();

    public static void bootstrap() {
        INTERACTIONS.clear();
        INTERACTIONS.put(Items.SLIME_BALL, createHealingInteraction(2));
        INTERACTIONS.put(Items.SLIME_BLOCK,combine(createHealingInteraction(20),(tamableSlime, player, hand) -> {
            if (tamableSlime.canGrow()) {
                if (!tamableSlime.level().isClientSide) {
                    tamableSlime.setSize(tamableSlime.getSize() + 1, true);
                    if (!player.getAbilities().instabuild) {
                        ItemStack stack = player.getItemInHand(hand);
                        stack.shrink(1);
                    }
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }));
    }

    static EntityInteract combine(EntityInteract first,EntityInteract second) {
        return (tamableSlime, player, hand) -> {
         InteractionResult result1 = first.interact(tamableSlime, player, hand);
         if (result1.consumesAction()) return result1;
         return second.interact(tamableSlime, player, hand);
        };
    }

    static EntityInteract createHealingInteraction(float heal) {
        return (tamableSlime, player, hand) -> {
            if (tamableSlime.getHealth() < tamableSlime.getMaxHealth()) {
                if (!tamableSlime.level().isClientSide) {
                    ItemStack stack = player.getItemInHand(hand);
                    tamableSlime.heal(heal);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    tamableSlime.gameEvent(GameEvent.EAT, tamableSlime);
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        };
    }

    @FunctionalInterface
    public interface EntityInteract {
        InteractionResult interact(TamableSlime tamableSlime, Player player, InteractionHand hand);
    }

}
