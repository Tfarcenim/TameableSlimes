package tfar.tamableslimes.client;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import tfar.tamableslimes.init.ModEntityTypes;

public class TameableSlimesClient {

    public static void entityRenderers() {
        EntityRenderers.register(ModEntityTypes.TAMABLE_SLIME, SlimeRenderer::new);
    }

}
