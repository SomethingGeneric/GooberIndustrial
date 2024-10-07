package cloud.goober.industrial;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.util.Identifier;

public class GoobotEntityRenderer extends MobEntityRenderer<GoobotEntity, PlayerEntityModel<GoobotEntity>> {
    public GoobotEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.5f);

        // Get the BakedModelManager from MinecraftClient
        BakedModelManager bakedModelManager = MinecraftClient.getInstance().getBakedModelManager();

        // Add the armor feature to render armor on the entity
        /*this.addFeature(new ArmorFeatureRenderer<>(this,
                new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER_INNER_ARMOR), false),
                new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR), false),
                bakedModelManager
        ));*/

        // Add the held item feature to render held items on the entity
        this.addFeature(new HeldItemFeatureRenderer<>(this, context.getHeldItemRenderer()));
    }

    @Override
    public Identifier getTexture(GoobotEntity entity) {
        return Identifier.of(GooberIndustrial.MOD_ID, "textures/entity/steve.png");
    }
}
