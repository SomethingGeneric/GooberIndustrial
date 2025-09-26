package cloud.goober.industrial;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;

import java.util.EnumSet;

public class MineOreGoal extends Goal {
    private final GoobotEntity goobotEntity;
    private final World world;
    private BlockPos targetOrePos;
    private int ticksAttempt = 0;

    public final Logger LOGGER = GooberIndustrial.LOGGER;

    private void log(String text) {
        LOGGER.info("GBT @ {}: {}", goobotEntity.getBlockPos(), text);
    }

    public MineOreGoal(GoobotEntity goobotEntity) {
        this.goobotEntity = goobotEntity;
        this.world = goobotEntity.getWorld();
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    public boolean correctItemInHand(ItemStack inHand) {
        return inHand.isOf(Items.STONE_PICKAXE) ||
                inHand.isOf(Items.IRON_PICKAXE) ||
                inHand.isOf(Items.GOLDEN_PICKAXE) ||
                inHand.isOf(Items.DIAMOND_PICKAXE);
    }

    public boolean targetThisBlock(BlockState block) {
        return block.isIn(BlockTags.NEEDS_STONE_TOOL) ||
                block.isOf(Blocks.COAL_ORE) ||
                block.isOf(Blocks.DEEPSLATE_COAL_ORE) ||
                block.isIn(BlockTags.NEEDS_IRON_TOOL) ||
                block.isIn(BlockTags.NEEDS_DIAMOND_TOOL);
    }

    @Override
    public boolean canStart() {
        // Check if the entity is holding a pickaxe in the main hand
        ItemStack mainHandItem = goobotEntity.getMainHandStack();
        if (!correctItemInHand(mainHandItem)) {
            log("Not starting to mine, wrong item in hand");
            return false;
        }

        log("Starting to mine. Looking for targets");

        // Find nearby ore
        BlockPos pos = goobotEntity.getBlockPos();
        int radius = 10; // Define the search radius for ores
        for (BlockPos targetPos : BlockPos.iterate(pos.add(-radius, -radius, -radius), pos.add(radius, radius, radius))) {
            if (targetThisBlock(world.getBlockState(targetPos))) {
                targetOrePos = targetPos;

                if (goobotEntity.manager != null) {
                    goobotEntity.manager.sendMessage(Text.of("We're going mining!"));
                }

                log("Target found!");
                return true;
            }
        }

        log("No targets found, giving up mining goal");
        return false;
    }

    @Override
    public void start() {
        // Move the entity to the target ore position
        if (targetOrePos != null) {
            log("Have a target position, moving to it");
            goobotEntity.getNavigation().startMovingTo(targetOrePos.getX(), targetOrePos.getY(), targetOrePos.getZ(), 1.0D);
        }
    }

    @Override
    public boolean shouldContinue() {
        // Continue if still holding a pickaxe and the ore block is still there
        boolean keepGoing = targetOrePos != null && targetThisBlock(world.getBlockState(targetOrePos)) &&
                correctItemInHand(goobotEntity.getMainHandStack());

        if (keepGoing) {
            log("Continuing mining goal");
        } else {
            log("Ending mining goal");
        }

        if (!keepGoing && goobotEntity.manager != null) {
            goobotEntity.manager.sendMessage(Text.of("No more mining. :C"));
        }

        return keepGoing;
    }

    @Override
    public void tick() {

        ticksAttempt += 1;

        if (ticksAttempt > 500) {
            log("Ending goal due to timeout");
            stop();
        }

        if (targetOrePos != null) {
            double distanceToOre = goobotEntity.squaredDistanceTo(targetOrePos.getX(), targetOrePos.getY(), targetOrePos.getZ());
            if (distanceToOre < 4.0D) {
                // Mine the ore
                world.breakBlock(targetOrePos, true);
                targetOrePos = null;
                stop();
            } else {
                goobotEntity.getNavigation().startMovingTo(targetOrePos.getX(), targetOrePos.getY(), targetOrePos.getZ(), 1.0D);
            }
        } else {
            stop();
        }
    }

    @Override
    public void stop() {
        log("Ending mining goal");
        targetOrePos = null;
    }
}
