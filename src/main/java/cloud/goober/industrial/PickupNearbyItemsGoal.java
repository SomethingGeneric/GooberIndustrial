package cloud.goober.industrial;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;

import java.util.EnumSet;
import java.util.List;

public class PickupNearbyItemsGoal extends Goal {
    private final GoobotEntity goobotEntity;
    private ItemEntity targetItem;

    public PickupNearbyItemsGoal(GoobotEntity goobotEntity) {
        this.goobotEntity = goobotEntity;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        // Define the radius for searching nearby items
        Box searchBox = goobotEntity.getBoundingBox().expand(5.0D, 2.0D, 5.0D); // A 5x2x5 radius around the entity
        List<ItemEntity> nearbyItems = goobotEntity.getEntityWorld().getEntitiesByClass(ItemEntity.class, searchBox, itemEntity -> !itemEntity.cannotPickup());

        if (!nearbyItems.isEmpty()) {
            // Set the first item found as the target
            targetItem = nearbyItems.getFirst();
            return true;
        }
        return false;
    }

    @Override
    public void start() {
        if (targetItem != null) {
            goobotEntity.getNavigation().startMovingTo(targetItem.getX(), targetItem.getY(), targetItem.getZ(), 1.0D);
        }
    }

    @Override
    public boolean shouldContinue() {
        // Continue if the item is still valid and exists
        return targetItem != null && targetItem.isAlive() && !targetItem.cannotPickup() &&
                goobotEntity.squaredDistanceTo(targetItem) > 1.5D;
    }

    @Override
    public void tick() {
        if (targetItem != null) {
            // Move towards the item
            goobotEntity.getNavigation().startMovingTo(targetItem.getX(), targetItem.getY(), targetItem.getZ(), 1.0D);

            // If the entity is close enough, pick up the item
            if (goobotEntity.squaredDistanceTo(targetItem) < 1.5D) {
                ItemStack itemStack = targetItem.getStack();
                // Handle picking up the item (similar to how PlayerEntity picks up items)

                if (goobotEntity.tryPickupItem(itemStack)) {
                    targetItem.discard();
                }

                targetItem = null; // Clear the target once the item is picked up
            }
        }
    }

    @Override
    public void stop() {
        targetItem = null; // Clear the target item when the goal is stopped
    }
}
