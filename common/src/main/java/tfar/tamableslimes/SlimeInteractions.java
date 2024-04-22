package tfar.tamableslimes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.HashMap;
import java.util.Map;

public class SlimeInteractions {

    public static final Map<Item, EntityInteract> INTERACTIONS = new HashMap<>();
    public static final Map<String,EntityEffect> EFFECT_MAP = new HashMap<>();

    public static void bootstrap() {
        INTERACTIONS.clear();
        EFFECT_MAP.clear();
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
        INTERACTIONS.put(Items.SHEARS,(tamableSlime, player, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
           if (!tamableSlime.level().isClientSide) {
               tamableSlime.kill();
               tamableSlime.gameEvent(GameEvent.SHEAR, player);
               stack.hurtAndBreak(1, player, player1 -> player1.broadcastBreakEvent(hand));
               return InteractionResult.SUCCESS;
           } else {
               return InteractionResult.CONSUME;
           }
        });
        addEffect(Items.MAGMA_CREAM,(slime, target) -> target.setSecondsOnFire(2));
        addEffect(Items.NETHER_STAR,((slime, target) -> target.addEffect(new MobEffectInstance(MobEffects.WITHER,100))));
    }

    static void addEffect(Item item,EntityEffect effect) {
        String name = BuiltInRegistries.ITEM.getKey(item).getPath();
        INTERACTIONS.put(item,(tamableSlime, player, hand) -> {
            boolean b = tamableSlime.addSlimeEffect(name);
            if (!b) return InteractionResult.FAIL;
            if (!tamableSlime.level().isClientSide) {
                if (!player.getAbilities().instabuild) {
                    ItemStack stack = player.getItemInHand(hand);
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.CONSUME;
        });
        EFFECT_MAP.put(name,effect);
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
    public interface EntityEffect {
        void apply(TamableSlime slime, LivingEntity target);
    }

    @FunctionalInterface
    public interface EntityInteract {
        InteractionResult interact(TamableSlime tamableSlime, Player player, InteractionHand hand);
    }

}
