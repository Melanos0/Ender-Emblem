package mel.enderemblem.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.authlib.GameProfile;
import mel.enderemblem.EnderEmblem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements EnderEmblem.PlayerEntityMixinAccess {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
    private float maxHealth = 25.0F;
    private float strength = 33.0F;
    private float magic = 33.0F;
    private float defense = 33.0F;
    private float resistance = 33.0F;
    private double speed = 40.0;
    private float skill = 20.0F;
    private float luck = 20.0F;

    @Shadow
    public abstract PlayerAbilities getAbilities();

    public double getSpeed(){return this.speed;}
    public void setSpeed(double value){this.speed = value;}
    @Inject(at = @At(value = "TAIL"), method = "writeCustomDataToNbt")
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo info) {
        nbt.putFloat("HP", this.maxHealth);
        nbt.putFloat("Str", this.strength);
        nbt.putFloat("Mag", this.magic);
        nbt.putFloat("Def", this.defense);
        nbt.putFloat("Res", this.resistance);
        nbt.putDouble("Spd", this.speed);
        nbt.putFloat("Skill", this.skill);
        nbt.putFloat("Luck", this.luck);
    }
    @Inject(at=@At(value = "TAIL"), method = "readCustomDataFromNbt")
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo info) {
        this.maxHealth = nbt.getFloat("HP");
        this.strength = nbt.getFloat("Str");
        this.magic = nbt.getFloat("Mag");
        this.defense = nbt.getFloat("Def");
        this.resistance = nbt.getFloat("Res");
        this.speed = nbt.getDouble("Spd");
        this.skill = nbt.getFloat("Skill");
        this.luck = nbt.getFloat("Luck");
        this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612*(this.speed/100.0 + 0.6));
    }
}
