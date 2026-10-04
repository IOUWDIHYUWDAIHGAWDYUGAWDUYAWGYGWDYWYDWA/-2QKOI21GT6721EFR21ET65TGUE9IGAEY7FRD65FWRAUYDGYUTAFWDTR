package com.reallifeearth.entity.npc;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RealNpcEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> DATA_FIRST_NAME = SynchedEntityData.defineId(RealNpcEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_LAST_NAME = SynchedEntityData.defineId(RealNpcEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_GENDER = SynchedEntityData.defineId(RealNpcEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_COUNTRY = SynchedEntityData.defineId(RealNpcEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_PROFESSION = SynchedEntityData.defineId(RealNpcEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_PERSONALITY = SynchedEntityData.defineId(RealNpcEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_AGE = SynchedEntityData.defineId(RealNpcEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_HEIGHT_SCALE = SynchedEntityData.defineId(RealNpcEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_MARRIED = SynchedEntityData.defineId(RealNpcEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_SKIN_VARIANT = SynchedEntityData.defineId(RealNpcEntity.class, EntityDataSerializers.INT);

    private BlockPos homePos = null;
    private BlockPos workPos = null;
    private int ticksUntilNextChat = 0;

    public RealNpcEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(DATA_FIRST_NAME, "Alex");
        b.define(DATA_LAST_NAME, "Smith");
        b.define(DATA_GENDER, "male");
        b.define(DATA_COUNTRY, "DEFAULT");
        b.define(DATA_PROFESSION, NpcProfession.UNEMPLOYED.id);
        b.define(DATA_PERSONALITY, NpcPersonality.FRIENDLY.id);
        b.define(DATA_AGE, 25);
        b.define(DATA_HEIGHT_SCALE, 1.0f);
        b.define(DATA_MARRIED, false);
        b.define(DATA_SKIN_VARIANT, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.1));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8f));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance diff, MobSpawnType reason, @Nullable SpawnGroupData data) {
        data = super.finalizeSpawn(level, diff, reason, data);
        RandomSource r = level.getRandom();
        boolean male = r.nextBoolean();
        String country = pickCountry(r);
        int age = 18 + r.nextInt(52);
        NpcProfession prof = randomProfession(r, age);
        NpcPersonality pers = NpcPersonality.random(r);
        float hScale = NpcGenetics.heightScale(country, male, r);

        setGender(male ? "male" : "female");
        setCountry(country);
        setAge(age);
        setProfession(prof.id);
        setPersonality(pers.id);
        setHeightScale(hScale);
        setSkinVariant(r.nextInt(8));
        // Simple name pools
        String[] maleNames = {"Emir","Mehmet","Ali","Can","Eren","John","James","Hans","Kenji","Carlos"};
        String[] femaleNames = {"Ayse","Zeynep","Elif","Meryem","Linda","Emma","Sophie","Yuki","Maria","Anna"};
        String[] surnames = {"Yilmaz","Kaya","Smith","Johnson","Muller","Sato","Silva","Martin","Ivanov","Kim"};
        String first = male ? maleNames[r.nextInt(maleNames.length)] : femaleNames[r.nextInt(femaleNames.length)];
        setFirstName(first);
        setLastName(surnames[r.nextInt(surnames.length)]);
        this.homePos = this.blockPosition();
        refreshDimensions();
        return data;
    }

    private String pickCountry(RandomSource r) {
        String[] pool = {"TR","TR","US","DE","JP","GB","FR","BR","IN","CN","RU","DEFAULT"};
        return pool[r.nextInt(pool.length)];
    }
    private NpcProfession randomProfession(RandomSource r, int age) {
        if (age < 22) return r.nextFloat() < 0.6 ? NpcProfession.STUDENT : NpcProfession.UNEMPLOYED;
        if (age > 60) return r.nextFloat() < 0.7 ? NpcProfession.RETIRED : NpcProfession.UNEMPLOYED;
        var all = NpcProfession.values();
        return all[r.nextInt(all.length)];
    }

    // Getters/setters
    public String getFirstName(){return entityData.get(DATA_FIRST_NAME);}
    public void setFirstName(String v){entityData.set(DATA_FIRST_NAME,v);}
    public String getLastName(){return entityData.get(DATA_LAST_NAME);}
    public void setLastName(String v){entityData.set(DATA_LAST_NAME,v);}
    public String getGender(){return entityData.get(DATA_GENDER);}
    public void setGender(String v){entityData.set(DATA_GENDER,v);}
    public String getCountry(){return entityData.get(DATA_COUNTRY);}
    public void setCountry(String v){entityData.set(DATA_COUNTRY,v);}
    public String getProfessionId(){return entityData.get(DATA_PROFESSION);}
    public void setProfession(String v){entityData.set(DATA_PROFESSION,v);}
    public String getPersonalityId(){return entityData.get(DATA_PERSONALITY);}
    public void setPersonality(String v){entityData.set(DATA_PERSONALITY,v);}
    public int getNpcAge(){return entityData.get(DATA_AGE);}
    public void setAge(int v){entityData.set(DATA_AGE,v);}
    public float getHeightScale(){return entityData.get(DATA_HEIGHT_SCALE);}
    public void setHeightScale(float v){entityData.set(DATA_HEIGHT_SCALE,v);}
    public boolean isMarried(){return entityData.get(DATA_MARRIED);}
    public void setMarried(boolean v){entityData.set(DATA_MARRIED,v);}
    public int getSkinVariant(){return entityData.get(DATA_SKIN_VARIANT);}
    public void setSkinVariant(int v){entityData.set(DATA_SKIN_VARIANT,v);}

    public String getFullName(){ return getFirstName()+" "+getLastName(); }
    public NpcProfession getProfession(){ return NpcProfession.byId(getProfessionId()); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("FirstName", getFirstName());
        tag.putString("LastName", getLastName());
        tag.putString("Gender", getGender());
        tag.putString("Country", getCountry());
        tag.putString("Profession", getProfessionId());
        tag.putString("Personality", getPersonalityId());
        tag.putInt("NpcAge", getNpcAge());
        tag.putFloat("HeightScale", getHeightScale());
        tag.putBoolean("Married", isMarried());
        tag.putInt("SkinVariant", getSkinVariant());
        if (homePos != null) tag.putLong("HomePos", homePos.asLong());
        if (workPos != null) tag.putLong("WorkPos", workPos.asLong());
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("FirstName")) setFirstName(tag.getString("FirstName"));
        if (tag.contains("LastName")) setLastName(tag.getString("LastName"));
        if (tag.contains("Gender")) setGender(tag.getString("Gender"));
        if (tag.contains("Country")) setCountry(tag.getString("Country"));
        if (tag.contains("Profession")) setProfession(tag.getString("Profession"));
        if (tag.contains("Personality")) setPersonality(tag.getString("Personality"));
        if (tag.contains("NpcAge")) setAge(tag.getInt("NpcAge"));
        if (tag.contains("HeightScale")) setHeightScale(tag.getFloat("HeightScale"));
        if (tag.contains("Married")) setMarried(tag.getBoolean("Married"));
        if (tag.contains("SkinVariant")) setSkinVariant(tag.getInt("SkinVariant"));
        if (tag.contains("HomePos")) homePos = BlockPos.of(tag.getLong("HomePos"));
        if (tag.contains("WorkPos")) workPos = BlockPos.of(tag.getLong("WorkPos"));
        refreshDimensions();
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        float scale = getHeightScale();
        // base 0.6 x 1.8
        return super.getDefaultDimensions(pose).scale(0.6f * (0.95f + scale*0.05f), scale);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide) {
            // Open dialogue screen client-side via event
            return InteractionResult.SUCCESS;
        } else {
            // Server: send chat info, increase relationship a bit
            player.displayClientMessage(Component.literal(getFullName() + " (" + getProfessionId() + ", " + getCountry() + ") - Merhaba!"), false);
            // TODO: open DialogueScreen via payload
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && ticksUntilNextChat > 0) ticksUntilNextChat--;
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.VILLAGER_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource s) { return SoundEvents.VILLAGER_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.VILLAGER_DEATH; }

    public BlockPos getHomePos(){ return homePos; }
    public void setHomePos(BlockPos p){ this.homePos = p; }
    public BlockPos getWorkPos(){ return workPos; }
    public void setWorkPos(BlockPos p){ this.workPos = p; }
}
