package cloud.goober.industrial;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

public class GoobotEntityRenderer extends MobEntityRenderer<GoobotEntity, PlayerEntityModel<GoobotEntity>> {
    public GoobotEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public Identifier getTexture(GoobotEntity entity) {
        return Identifier.of(GooberIndustrial.MOD_ID, "textures/entity/steve.png");
    }
}