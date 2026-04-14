package com.jodexindustries.donatecase.spigot.api.armorstand;

import com.jodexindustries.donatecase.api.armorstand.ArmorStandCreator;
import com.jodexindustries.donatecase.api.armorstand.ArmorStandEulerAngle;
import com.jodexindustries.donatecase.api.armorstand.EquipmentSlot;
import com.jodexindustries.donatecase.api.data.storage.CaseLocation;
import com.jodexindustries.donatecase.api.tools.DCTools;
import com.jodexindustries.donatecase.spigot.tools.BukkitUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class EntityArmorStandCreator implements ArmorStandCreator {

    private final UUID animationId;
    private final Location location;
    private final World world;

    private ArmorStand entity;

    // Pending properties (applied atomically on spawn)
    private boolean visible = true;
    private boolean small = false;
    private boolean marker = false;
    private boolean glowing = false;
    private boolean gravity = true;
    private boolean collidable = true;
    private boolean customNameVisible = false;
    private String customName = null;
    private ArmorStandEulerAngle pendingAngle = null;
    private Float pendingYaw = null;
    private Float pendingPitch = null;

    public EntityArmorStandCreator(UUID animationId, Location location) {
        this.animationId = animationId;
        this.location = location.clone();
        this.world = location.getWorld();
    }

    @Override
    public void spawn() {
        if (entity != null || world == null) return;

        entity = world.spawn(location, ArmorStand.class, stand -> {
            stand.setVisible(visible);
            stand.setSmall(small);
            stand.setMarker(marker);
            stand.setGlowing(glowing);
            stand.setGravity(gravity);
            stand.setCollidable(collidable);
            stand.setCustomNameVisible(customNameVisible);
            if (customName != null) stand.setCustomName(DCTools.rc(customName));
            if (pendingAngle != null) {
                stand.setHeadPose(BukkitUtils.toBukkit(pendingAngle.getHead()));
                stand.setBodyPose(BukkitUtils.toBukkit(pendingAngle.getBody()));
                stand.setLeftArmPose(BukkitUtils.toBukkit(pendingAngle.getLeftArm()));
                stand.setRightArmPose(BukkitUtils.toBukkit(pendingAngle.getRightArm()));
                stand.setLeftLegPose(BukkitUtils.toBukkit(pendingAngle.getLeftLeg()));
                stand.setRightLegPose(BukkitUtils.toBukkit(pendingAngle.getRightLeg()));
            }
            if (pendingYaw != null) stand.setRotation(pendingYaw, pendingPitch != null ? pendingPitch : 0f);
            // Refresh metadata after spawn for Bedrock compatibility
            stand.setMetadata("case", new FixedMetadataValue(BukkitUtils.getDonateCase(), "case"));
        });

        ArmorStandCreator.armorStands.put(entity.getEntityId(), this);
    }

    @Override
    public void updateMeta() {
        if (entity == null || !entity.isValid()) return;
        
        // Force metadata refresh by toggling custom name visibility
        // This ensures Bedrock clients receive the updated metadata
        boolean visible = entity.isCustomNameVisible();
        entity.setCustomNameVisible(!visible);
        entity.setCustomNameVisible(visible);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        EntityArmorStandCreator that = (EntityArmorStandCreator) object;
        return Objects.equals(entity, that.entity);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(entity);
    }

    @Override
    public void setVisible(boolean isVisible) {
        this.visible = isVisible;
        if (entity != null) entity.setVisible(isVisible);
    }

    @Override
    public void setSmall(boolean small) {
        this.small = small;
        if (entity != null) entity.setSmall(small);
    }

    @Override
    public void setMarker(boolean marker) {
        this.marker = marker;
        if (entity != null) entity.setMarker(marker);
    }

    @Override
    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
        if (entity != null) entity.setGlowing(glowing);
    }

    @Override
    public boolean isGlowing() {
        return entity != null ? entity.isGlowing() : glowing;
    }

    @Override
    public void setCollidable(boolean collidable) {
        this.collidable = collidable;
        if (entity != null) entity.setCollidable(collidable);
    }

    @Override
    public void setCustomNameVisible(boolean flag) {
        this.customNameVisible = flag;
        if (entity != null) entity.setCustomNameVisible(flag);
    }

    @Override
    public boolean isCustomNameVisible() {
        return entity != null ? entity.isCustomNameVisible() : customNameVisible;
    }

    @Override
    public void setCustomName(String displayName) {
        this.customName = displayName;
        if (entity != null) entity.setCustomName(displayName != null ? DCTools.rc(displayName) : null);
    }

    @Override
    public void teleport(CaseLocation location) {
        if (entity != null) entity.teleport(BukkitUtils.toBukkit(location));
    }

    @Override
    public void setEquipment(EquipmentSlot equipmentSlot, Object item) {
        if (entity == null) return;
        EntityEquipment equipment = entity.getEquipment();
        if (equipment != null)
            equipment.setItem(org.bukkit.inventory.EquipmentSlot.valueOf(equipmentSlot.name()), (ItemStack) item);
    }

    @Override
    public void setAngle(@NotNull ArmorStandEulerAngle angle) {
        this.pendingAngle = angle;
        if (entity != null) {
            entity.setHeadPose(BukkitUtils.toBukkit(angle.getHead()));
            entity.setBodyPose(BukkitUtils.toBukkit(angle.getBody()));
            entity.setLeftArmPose(BukkitUtils.toBukkit(angle.getLeftArm()));
            entity.setRightArmPose(BukkitUtils.toBukkit(angle.getRightArm()));
            entity.setLeftLegPose(BukkitUtils.toBukkit(angle.getLeftLeg()));
            entity.setRightLegPose(BukkitUtils.toBukkit(angle.getRightLeg()));
        }
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        this.pendingYaw = yaw;
        this.pendingPitch = pitch;
        if (entity != null) entity.setRotation(yaw, pitch);
    }

    @Override
    public CaseLocation getLocation() {
        return entity != null ? BukkitUtils.fromBukkit(entity.getLocation()) : BukkitUtils.fromBukkit(location);
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return entity != null ? entity.getUniqueId() : UUID.randomUUID();
    }

    @Override
    public UUID getAnimationId() {
        return animationId;
    }

    @Override
    public int getEntityId() {
        return entity != null ? entity.getEntityId() : -1;
    }

    @Override
    public void setGravity(boolean hasGravity) {
        this.gravity = hasGravity;
        if (entity != null) entity.setGravity(hasGravity);
    }

    @Override
    public void remove() {
        if (entity == null) return;
        ArmorStandCreator.armorStands.remove(entity.getEntityId());
        entity.remove();
    }
}
