package tfar.tameableslimes.client;

import net.minecraftforge.client.event.EntityRenderersEvent;

public class TameableSlimesClientForge {

    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        TameableSlimesClient.entityRenderers();
    }

}
