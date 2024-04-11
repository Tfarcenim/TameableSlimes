package tfar.tameableslimes.client;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import tfar.tameableslimes.init.ModEntityTypes;

public class TameableSlimesClient {

    public static void entityRenderers() {
        EntityRenderers.register(ModEntityTypes.TAMEABLE_SLIME, SlimeRenderer::new);
    }

}
