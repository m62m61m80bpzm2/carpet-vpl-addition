package carpetvpladdition;

import carpet.CarpetServer;
import net.fabricmc.api.ModInitializer;

/**
 * 模组入口：注册 Carpet 扩展。
 * （b1.15.1.1 起所有新功能均以 Mixin + 规则实现，无全局事件注册）
 */
public class CarpetVPLAdditionMod implements ModInitializer {
    @Override
    public void onInitialize() {
        CarpetServer.manageExtension(new CarpetVPLAdditionExtension());
    }
}
