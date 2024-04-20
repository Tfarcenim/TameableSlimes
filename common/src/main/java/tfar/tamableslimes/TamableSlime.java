package tfar.tamableslimes;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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
import tfar.tamableslimes.entity.goal.SlimeFollowOwnerGoal;
import tfar.tamableslimes.entity.goal.SlimeOwnerHurtByTargetGoal;
import tfar.tamableslimes.entity.goal.SlimeOwnerHurtTargetGoal;
import tfar.tamableslimes.entity.goal.SlimeSitWhenOrderedToSitGoal;

import java.util.Optional;
import java.util.UUID;

public class TamableSlime extends Slime implements OwnableEntity {

    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(TamableSlime.class, EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(TamableSlime.class, EntityDataSerializers.BYTE);
    private boolean orderedToSit;

    public static final TagKey<Item> TAMING_ITEM = TagKey.create(Registries.ITEM, new ResourceLocation(TamableSlimes.MOD_ID, "taming_item"));

    public TamableSlime(EntityType<? extends Slime> $$0, Level $$1) {
        super($$0, $$1);
        this.reassessTameGoals();
    }

    @Override
    protected void registerGoals() {

        this.goalSelector.addGoal(2, new SlimeSitWhenOrderedToSitGoal(this));
        this.goalSelector.addGoal(2, new Slime.SlimeAttackGoal(this));
        this.goalSelector.addGoal(6, new SlimeFollowOwnerGoal(this, 1.0D, 7, 4, false));
        this.goalSelector.addGoal(5, new JumpUnlessTameGoal(this));

        this.targetSelector.addGoal(1, new SlimeOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new SlimeOwnerHurtTargetGoal(this));


    }

    public static class JumpUnlessTameGoal extends SlimeKeepOnJumpingGoal {

        protected final TamableSlime tamableSlime;
        public JumpUnlessTameGoal(TamableSlime $$0) {
            super($$0);
            this.tamableSlime = $$0;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !tamableSlime.isOrderedToSit() && tamableSlime.getTarget() != null;
        }
    }

    public boolean wantsToAttack(LivingEntity pTarget, LivingEntity pOwner) {
        if (pTarget == pOwner) {//don't attack the owner
            return false;
        } else return !(pTarget instanceof Creeper) && !(pTarget instanceof Ghast);//don't attack creepers or ghasts
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
    static final byte SITTING = 0b1;

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
    public InteractionResult mobInteract(Player player, InteractionHand pHand) {
        ItemStack itemstack = player.getItemInHand(pHand);
        Item item = itemstack.getItem();
        boolean client = level().isClientSide;
        boolean tame = isTame();
        if (!tame) {
            if (itemstack.is(TAMING_ITEM)) {
                if (!client) {
                    tryTame(player,itemstack);
                }
                return InteractionResult.SUCCESS;
            }
        } else {
            if (isOwnedBy(player)) {
                if (!itemstack.isEmpty()) {
                    SlimeInteractions.EntityInteract entityInteract = SlimeInteractions.SLIME_INTERACTIONS.get(item);
                    return entityInteract == null ? InteractionResult.PASS : entityInteract.interact(this, player, pHand);
                } else {
                    return emptyRightClick(player,pHand);
                }
            }
        }
        return super.mobInteract(player, pHand);
    }

    InteractionResult emptyRightClick(Player player,InteractionHand hand) {
        InteractionResult interactionresult = super.mobInteract(player, hand);
        if ((!interactionresult.consumesAction() || this.isBaby()) && this.isOwnedBy(player)) {
            if (player.isCrouching()) {
                this.level().broadcastEntityEvent(this, EntityEvent.IN_LOVE_HEARTS);
                this.playSound(this.getSquishSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) / 0.8F);
            } else {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.jumping = false;
                this.navigation.stop();
                this.setTarget(null);
            }
            return InteractionResult.SUCCESS;
        } else {
            return interactionresult;
        }
    }

    void tryTame(Player player, ItemStack itemstack) {
        if (!player.getAbilities().instabuild) {
            itemstack.shrink(1);
        }

        if (this.random.nextInt(3) == 0 /*&& !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, pPlayer)*/) {//todo
            this.tame(player);
            this.navigation.stop();
            this.setTarget(null);
            this.setOrderedToSit(true);
            this.level().broadcastEntityEvent(this, EntityEvent.TAMING_SUCCEEDED);
        } else {
            this.level().broadcastEntityEvent(this,EntityEvent.TAMING_FAILED);
        }
    }


    @Override
    public void handleEntityEvent(byte pId) {
        switch (pId) {
            case EntityEvent.TAMING_SUCCEEDED -> this.spawnTamingParticles(true);
            case EntityEvent.TAMING_FAILED -> this.spawnTamingParticles(false);
            case EntityEvent.IN_LOVE_HEARTS -> {
                for (int i = 0; i < 7; ++i) {
                    double d0 = this.random.nextGaussian() * 0.02D;
                    double d1 = this.random.nextGaussian() * 0.02D;
                    double d2 = this.random.nextGaussian() * 0.02D;
                    this.level().addParticle(ParticleTypes.HEART, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
                }
            }
            default -> super.handleEntityEvent(pId);
        }
    }

    /**
     * Play the taming effect, will either be hearts or smoke depending on status
     */
    protected void spawnTamingParticles(boolean pTamed) {
        ParticleOptions particleoptions = ParticleTypes.HEART;
        if (!pTamed) {
            particleoptions = ParticleTypes.SMOKE;
        }

        for(int i = 0; i < 7; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            this.level().addParticle(particleoptions, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
        }

    }

    @Override
    public boolean removeWhenFarAway(double $$0) {
        return !isTame();
    }

    boolean canGrow() {
        return getSize() < 64;
    }


    protected void reassessTameGoals() {
    }

    public boolean isInSittingPose() {
        return (this.entityData.get(DATA_FLAGS_ID) & SITTING) != 0;
    }

    public void setInSittingPose(boolean pSitting) {
        byte b0 = this.entityData.get(DATA_FLAGS_ID);
        if (pSitting) {
            this.entityData.set(DATA_FLAGS_ID, (byte) (b0 | SITTING));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte) (b0 & ~SITTING));
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
