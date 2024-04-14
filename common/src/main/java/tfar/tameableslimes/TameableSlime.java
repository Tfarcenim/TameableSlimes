package tfar.tameableslimes;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;
import tfar.tameableslimes.entity.goal.SlimeFollowOwnerGoal;
import tfar.tameableslimes.entity.goal.SlimeOwnerHurtByTargetGoal;
import tfar.tameableslimes.entity.goal.SlimeOwnerHurtTargetGoal;
import tfar.tameableslimes.entity.goal.SlimeSitWhenOrderedToSitGoal;

import java.util.Optional;
import java.util.UUID;

public class TameableSlime extends Slime implements OwnableEntity {

    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(TameableSlime.class, EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(TameableSlime.class, EntityDataSerializers.BYTE);
    private boolean orderedToSit;

    public static final TagKey<Item> TAMING_ITEM = TagKey.create(Registries.ITEM, new ResourceLocation(TameableSlimes.MOD_ID, "taming_item"));

    public static final Object2IntMap<Item> HEALING_ITEMS = new Object2IntOpenHashMap<>();

    static {
        HEALING_ITEMS.put(Items.SLIME_BALL, 1);
        HEALING_ITEMS.put(Items.SLIME_BLOCK, 10);
    }

    public TameableSlime(EntityType<? extends Slime> $$0, Level $$1) {
        super($$0, $$1);
        this.reassessTameGoals();
    }

    @Override
    protected void registerGoals() {

        this.goalSelector.addGoal(2, new SlimeSitWhenOrderedToSitGoal(this));
        this.goalSelector.addGoal(2, new Slime.SlimeAttackGoal(this));
        this.goalSelector.addGoal(6, new SlimeFollowOwnerGoal(this, 1.0D, 7, 4, false));

        this.targetSelector.addGoal(1, new SlimeOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new SlimeOwnerHurtTargetGoal(this));

    }

    public boolean wantsToAttack(LivingEntity pTarget, LivingEntity pOwner) {
        if (pTarget == pOwner) {//don't attack the owner
            return false;
        } else if (pTarget instanceof Creeper || pTarget instanceof Ghast) {
            return false;//don't attack creepers or ghasts
        }
        return true;
    }

    @Override
    public boolean isDealsDamage() {
        return super.isDealsDamage();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.ATTACK_DAMAGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
        this.entityData.define(DATA_FLAGS_ID, (byte) 0);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID pUuid) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(pUuid));
    }

    public boolean isTame() {
        return (this.entityData.get(DATA_FLAGS_ID) & TAME) != 0;
    }

    static final byte TAME = 0b100;

    public void setTame(boolean pTamed) {
        byte flags = this.entityData.get(DATA_FLAGS_ID);
        if (pTamed) {
            this.entityData.set(DATA_FLAGS_ID, (byte) (flags | TAME));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte) (flags & ~TAME));
        }

        this.reassessTameGoals();
    }

    @Override
    public void push(Entity pEntity) {
        super.push(pEntity);
        if (pEntity instanceof LivingEntity living) {
            if (this.isDealsDamage() && getTarget() == living) {
                this.dealDamage(living);
            }
        }
    }

    @Override
    protected void dealDamage(LivingEntity entity) {
        if (entity instanceof IronGolem) {
            //super will damage the iron golem
        } else {
            super.dealDamage(entity);
        }
    }

    @Override
    public void playerTouch(Player player) {
        //super.playerTouch(player);
    }

    @Override
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        Item item = itemstack.getItem();
        if (this.level().isClientSide) {
            boolean flag = this.isOwnedBy(pPlayer) || this.isTame() || itemstack.is(TAMING_ITEM) && !this.isTame();
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else if (this.isTame()) {
            if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                this.heal(HEALING_ITEMS.getInt(itemstack.getItem()));
                if (!pPlayer.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }

                this.gameEvent(GameEvent.EAT, this);
                return InteractionResult.SUCCESS;
            } else {

                if (item == Items.SLIME_BLOCK) {
                    if (isOwnedBy(pPlayer) && canGrow()) {
                        setSize(getSize() + 1, true);
                        if (!pPlayer.getAbilities().instabuild) {
                            itemstack.shrink(1);
                        }
                        return InteractionResult.SUCCESS;
                    }
                    return super.mobInteract(pPlayer, pHand);
                }

            /*    if (item instanceof DyeItem) {
                    DyeItem dyeitem = (DyeItem)item;
                    if (this.isOwnedBy(pPlayer)) {
                        DyeColor dyecolor = dyeitem.getDyeColor();
                        if (dyecolor != this.getCollarColor()) {
                            this.setCollarColor(dyecolor);
                            if (!pPlayer.getAbilities().instabuild) {
                                itemstack.shrink(1);
                            }

                            return InteractionResult.SUCCESS;
                        }

                        return super.mobInteract(pPlayer, pHand);
                    }
                }*/

                InteractionResult interactionresult = super.mobInteract(pPlayer, pHand);
                if ((!interactionresult.consumesAction() || this.isBaby()) && this.isOwnedBy(pPlayer)) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                    this.jumping = false;
                    this.navigation.stop();
                    this.setTarget(null);
                    return InteractionResult.SUCCESS;
                } else {
                    return interactionresult;
                }
            }
        } else if (itemstack.is(TAMING_ITEM)) {
            if (!pPlayer.getAbilities().instabuild) {
                itemstack.shrink(1);
            }

            if (this.random.nextInt(3) == 0 /*&& !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, pPlayer)*/) {//todo
                this.tame(pPlayer);
                this.navigation.stop();
                this.setTarget(null);
                this.setOrderedToSit(true);
                this.level().broadcastEntityEvent(this, (byte) 0b111);
            } else {
                this.level().broadcastEntityEvent(this, (byte) 0b110);
            }

            return InteractionResult.SUCCESS;
        } else {
            return super.mobInteract(pPlayer, pHand);
        }
    }

    private boolean isFood(ItemStack itemstack) {
        return HEALING_ITEMS.containsKey(itemstack.getItem());
    }

    boolean canGrow() {
        return getSize() < 100;
    }


    protected void reassessTameGoals() {
    }

    public boolean isInSittingPose() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setInSittingPose(boolean pSitting) {
        byte b0 = this.entityData.get(DATA_FLAGS_ID);
        if (pSitting) {
            this.entityData.set(DATA_FLAGS_ID, (byte) (b0 | 1));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte) (b0 & -2));
        }

    }

    public void tame(Player pPlayer) {
        this.setTame(true);
        this.setOwnerUUID(pPlayer.getUUID());
        //   if (pPlayer instanceof ServerPlayer) {
        //       CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayer)pPlayer, this);
        //     }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        if (this.getOwnerUUID() != null) {
            pCompound.putUUID("Owner", this.getOwnerUUID());
        }

        pCompound.putBoolean("Sitting", this.orderedToSit);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        UUID uuid;
        if (pCompound.hasUUID("Owner")) {
            uuid = pCompound.getUUID("Owner");
        } else {
            String s = pCompound.getString("Owner");
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
        }

        if (uuid != null) {
            try {
                this.setOwnerUUID(uuid);
                this.setTame(true);
            } catch (Throwable throwable) {
                this.setTame(false);
            }
        }

        this.orderedToSit = pCompound.getBoolean("Sitting");
        this.setInSittingPose(this.orderedToSit);
    }


    public boolean isOrderedToSit() {
        return this.orderedToSit;
    }

    public void setOrderedToSit(boolean pOrderedToSit) {
        this.orderedToSit = pOrderedToSit;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    public boolean isOwnedBy(LivingEntity pEntity) {
        return pEntity == this.getOwner();
    }

}
