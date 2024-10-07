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
    private int ticksAttempt = 0;

    public PickupNearbyItemsGoal(GoobotEntity goobotEntity) {
        this.goobotEntity = goobotEntity;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        Box searchBox = goobotEntity.getBoundingBox().expand(5.0D, 2.0D, 5.0D);
        List<ItemEntity> nearbyItems = goobotEntity.getEntityWorld().getEntitiesByClass(ItemEntity.class, searchBox, itemEntity -> !itemEntity.cannotPickup());

        if (!nearbyItems.isEmpty()) {
            targetItem = nearbyItems.getFirst();  // Corrected way to get the first element
            return targetItem != null;  // Ensure targetItem is not null
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
        return targetItem != null && targetItem.isAlive() && !targetItem.cannotPickup() &&
                goobotEntity.squaredDistanceTo(targetItem) > 2.0D;
    }

    @Override
    public void tick() {
        if (targetItem == null) {
            stop();
            return;
        }

        ticksAttempt++;

        if (ticksAttempt > 100) {
            stop();
            return;
        }

        // Check targetItem again before using it
        if (targetItem != null) {
            goobotEntity.getNavigation().startMovingTo(targetItem.getX(), targetItem.getY(), targetItem.getZ(), 1.0D);

            if (goobotEntity.squaredDistanceTo(targetItem) < 2.0D) {
                ItemStack itemStack = targetItem.getStack();

                if (goobotEntity.tryPickupItem(itemStack)) {
                    targetItem.discard();
                    targetItem = null;
                }
            }
        }
    }

    @Override
    public void stop() {
        targetItem = null;
        ticksAttempt = 0; // Reset attempt counter
    }
}
