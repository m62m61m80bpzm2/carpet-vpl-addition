package carpetvpladdition;

import carpet.CarpetServer;
import net.fabricmc.api.ModInitializer;

public class CarpetVPLAdditionMod implements ModInitializer {
    @Override
    public void onInitialize() {
        CarpetServer.manageExtension(new CarpetVPLAdditionExtension());
    }
}
