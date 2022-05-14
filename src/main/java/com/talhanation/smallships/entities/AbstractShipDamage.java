package com.talhanation.smallships.entities;

import com.talhanation.smallships.DamageSourceShip;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

public abstract class AbstractShipDamage extends AbstractBannerUser {
    private static final DataParameter<Float> DAMAGE = EntityDataManager.defineId(AbstractShipDamage.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> SUNKEN = EntityDataManager.defineId(AbstractShipDamage.class, DataSerializers.BOOLEAN);

    public AbstractShipDamage(EntityType<? extends AbstractShipDamage> type, World world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DAMAGE, 0F);
        this.entityData.define(SUNKEN, false);
    }

    ////////////////////////////////////TICK////////////////////////////////////

    @Override
    public void tick() {
        super.tick();
        if (isInLava()) {
            setShipDamage(getShipDamage() + 1.5F);
        }

        if (isOnFire()) {
            setShipDamage(getShipDamage() + 0.5F);
        }

        //if(isOverBubble)

        if (getShipDamage() >= 100){
            setSunken(true);
            this.setDeltaMovement(0, -0.2D,0);
        }

    }

    ////////////////////////////////////SAVE////////////////////////////////////

    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putFloat("Damage", getShipDamage());
        nbt.putBoolean("Sunken", getSunken());
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        setShipDamage(nbt.getFloat("Damage"));
        setSunken(nbt.getBoolean("Sunken"));
    }

    ////////////////////////////////////GET////////////////////////////////////

    public boolean getSunken() {
        return entityData.get(SUNKEN);
    }

    public float getShipDamage() {
        return entityData.get(DAMAGE);
    }

    public abstract double getShipDefense(); // how much incoming dmg gets reduced in % e.g: 30
    //public double getShipHealth(){


    ////////////////////////////////////SET////////////////////////////////////

    public void setSunken(boolean sunken) {
        if (sunken != getSunken()){
            //particels
            if (this.level.isClientSide) {
                for (int i = 0; i < 600; ++i) {
                    double d0 = this.random.nextGaussian() * 0.04D;
                    double d1 = this.random.nextGaussian() * 0.02D;
                    double d2 = this.random.nextGaussian() * 0.04D;
                    double d3 = 45.0D;
                    this.level.addParticle(ParticleTypes.POOF, this.getX(1.0D) - d0 * d3, this.getRandomY() - d1 * d3 + i * 0.0001, this.getRandomZ(2.0D) - d2 * d3, d0, d1, d2);
                    //this.level.addParticle(ParticleTypes.BUBBLE, this.getX(2.0D) - d0 * d3, this.getRandomY() - d1 * d3, this.getRandomZ(2.0D) - d2 * d3, d0, d1, d2);
                }
            }
            entityData.set(SUNKEN, sunken);
        }
    }

    public void setShipDamage(float damage) {
        entityData.set(DAMAGE, damage);
    }

    ////////////////////////////////////ON FUNCTIONS////////////////////////////////////

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerable()) {
            return false;
        }

        if (level.isClientSide || !isAlive()) {
            return false;
        }

        if (source.getDirectEntity() instanceof PlayerEntity){
            PlayerEntity player = (PlayerEntity) source.getDirectEntity();
            if (player == null) {
                return false;
            }

            if (hasPassenger(player)) {
                return false;
            }

            if (player.abilities.instabuild) {
                if (player.isShiftKeyDown()) {
                    destroyShip(source);
                    return true;
                }
            }
            if (amount >= 2) damageShip(amount);
            this.markHurt();
            return false;
        }
        if (source.isProjectile()){
            if (amount >= 2) damageShip(amount);
            this.markHurt();
        }
        if (getShipDamage() >= 100) destroyShip(source);
        if (amount >= 2) damageShip(amount);
         return false;
    }

    public abstract ResourceLocation getLootTable();

    public void destroyShip(DamageSource source) {
        super.destroyShip(source);
        kill();
    }

    ////////////////////////////////////OTHER FUNCTIONS////////////////////////////////////

    public void damageShip(double damage) {
        setShipDamage((float) (((getShipDamage()) + (damage - (damage * getShipDefense()/100)))));
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        if (entity instanceof LivingEntity && !getPassengers().contains(entity)) {
            if (entity.getBoundingBox().intersects(getBoundingBox())) {
                double speed = getDeltaMovement().length();
                if (speed > 0.35F) {
                    float damage = Math.min((float) (speed * 10D), 15F);
                    entity.hurt(DamageSourceShip.DAMAGE_SHIP, damage);
                }

            }
        }
        return super.canCollideWith(entity);
    }


    public void onInteractionWitAxe(PlayerEntity player){
        PlayerInventory playerInventory = player.inventory;
        int healBonus = this.random.nextInt(7) + 5;

        this.level.playSound(null, this.getX(), this.getY() + 4, this.getZ(), SoundEvents.WOOD_PLACE, this.getSoundSource(), 10.0F, 0.8F + 0.4F * this.random.nextFloat());


        setShipDamage(this.getShipDamage() - healBonus);
        handleItemsOnRepair(playerInventory);
    }
    
    public boolean hasPlanks(PlayerInventory inventory){

        for(int i = 0; i < inventory.getContainerSize(); i++){
            ItemStack itemStack = inventory.getItem(i);

            if (itemStack.getItem() == Items.OAK_PLANKS ||
                    itemStack.getItem() == Items.SPRUCE_PLANKS ||
                    itemStack.getItem() == Items.ACACIA_PLANKS ||
                    itemStack.getItem() == Items.DARK_OAK_PLANKS ||
                    itemStack.getItem() == Items.BIRCH_PLANKS ||
                    itemStack.getItem() == Items.JUNGLE_PLANKS){

                return true;
            }
        }
        return false;
    }

    public boolean hasIronNugget(PlayerInventory inventory){

        for(int i = 0; i < inventory.getContainerSize(); i++){
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.getItem() == Items.IRON_NUGGET){

                return true;
            }
        }
        return false;
    }

    public void handleItemsOnRepair(PlayerInventory inventory){
        for(int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.getItem() == Items.IRON_NUGGET) {
                itemStack.shrink(1);
            }
        }

        for(int i = 0; i < inventory.getContainerSize(); i++){
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.getItem() == Items.OAK_PLANKS ||
                    itemStack.getItem() == Items.SPRUCE_PLANKS ||
                    itemStack.getItem() == Items.ACACIA_PLANKS ||
                    itemStack.getItem() == Items.DARK_OAK_PLANKS ||
                    itemStack.getItem() == Items.BIRCH_PLANKS ||
                    itemStack.getItem() == Items.JUNGLE_PLANKS){
                itemStack.shrink(1);
            }
        }
    }
}