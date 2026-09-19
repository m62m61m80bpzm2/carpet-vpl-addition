package carpetvpladdition.mixin;

import carpetvpladdition.settings.CarpetVPLAdditionSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/**
 * 命名矿车区块加载器（b1.15.1.1 新增，b1.15.1.4 重写，规则 namedMinecartChunkLoader）。
 *
 * 在铁砧中把矿车命名为加载器名字（支持 6 语言等价名，见 LOADER_NAMES），放置后该矿车
 * 即成为区块加载器：每 20 游戏刻为以它为中心的 3×3 区块各续期一次加载票据。
 *
 * === 26.x 票据等级语义（javap 核实，与直觉相反，务必看清） ===
 * 票据等级 = ChunkLevel.FULL_CHUNK_LEVEL(33) - radius，且**数值越低权限越高**：
 *   ChunkLevel.isEntityTicking(l) -> l <= 31
 *   ChunkLevel.isBlockTicking(l)  -> l <= 32
 *   fullStatus(l)                 -> l<=31 实体 tick，l<=32 方块 tick，l<=33 仅加载，l>33 不可达
 * 而 FullChunkStatus 是包含关系（实体 tick 的区块必然也跑方块实体），
 * 所以 radius 2（等级 31）的源点区块上漏斗、熔炉、作物照常运行。
 * 票据向邻格扩散时等级每次 +1（权限递减，33 即视为未被覆盖）。
 *
 * === 为什么必须改成 3×3（旧实现只贴锚点单格） ===
 * 旧实现只在矿车所在区块贴一张 radius 2 的票，于是只有锚点格是实体 tick（31），
 * 邻格被扩散成 32 = 只跑方块、**不跑实体**。矿车一跨出锚点格就落入不 tick 实体的区块
 * → 本 Mixin 不再被调用 → 无人续期 → 票据过期后整片卸载，矿车永久冻结在区块边界且无法自愈。
 * 现改为显式给 3×3 九个区块各贴一张源点票：九个区块都直接是等级 31 的源点，
 * 不依赖扩散推断，矿车跨一格仍在可 tick 区域内，下一跳照常续期。
 *
 * === 为什么把末影珍珠票据换成传送门票据 ===
 *  - ENDER_PEARL：timeout 仅 40gt（2 秒），flags=14 不含 FLAG_PERSIST，
 *    而 TicketStorage.packTickets 只序列化 persist() 为真的票据 → 重启服务器后票据全部丢失；
 *  - PORTAL：timeout 正好 300gt（15 秒），flags=15 含 FLAG_PERSIST → 随世界存档保存、读档恢复；
 *    遍历 26.1.2 全部 10682 个原版类，仅 15 个类引用 TicketType，且无任何代码移除 PORTAL 票据，
 *    借用不会被传送门逻辑误清。
 */
@Mixin(AbstractMinecart.class)
public abstract class NamedMinecartChunkLoaderMixin {

    /** 关键字：归一化后的矿车名包含它即视为加载器，允许玩家加前后缀区分多台加载器 */
    private static final String LOADER_KEYWORD = "chunkloader";

    /** 各语言等价名（须为 isLoaderName 归一化后的形式：小写、无空白与下划线） */
    private static final Set<String> LOADER_NAMES = Set.of(
        "区块加载器", "區塊載入器", "cargadordechunks", "chargeurdechunks", "carregadordechunks"
    );

    /** 源点票半径：2 → 等级 31，即实体 tick（同时含方块 tick 与加载） */
    private static final int TICKET_RADIUS = 2;

    /** 加载范围：以矿车区块为中心的 (2*1+1)^2 = 3×3 区块 */
    private static final int COVER_RADIUS_IN_CHUNKS = 1;

    @Inject(method = "tick", at = @At("HEAD"))
    private void vplKeepChunkLoaded(CallbackInfo ci) {
        if (!CarpetVPLAdditionSettings.namedMinecartChunkLoader) {
            return;
        }
        AbstractMinecart cart = (AbstractMinecart) (Object) this;
        // 顺序优化：先按 20 刻节流，每矿车 19/20 的调用在此直接返回
        if (cart.tickCount % 20 != 0
            || cart.level().isClientSide()
            || !(cart.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Component customName = cart.getCustomName();
        if (customName == null || !isLoaderName(customName.getString())) {
            return;
        }
        ChunkPos center = cart.chunkPosition();
        // 显式覆盖 3×3：每张都是源点票，同 type 同 level 由原版 addTicket 去重（仅重置倒计时），
        // 故每秒 9 次调用不产生累积，代价仅为 9 个区块保持运行——这本即区块加载器的用途。
        for (int dx = -COVER_RADIUS_IN_CHUNKS; dx <= COVER_RADIUS_IN_CHUNKS; dx++) {
            for (int dz = -COVER_RADIUS_IN_CHUNKS; dz <= COVER_RADIUS_IN_CHUNKS; dz++) {
                serverLevel.getChunkSource().addTicketWithRadius(
                    TicketType.PORTAL, new ChunkPos(center.x() + dx, center.z() + dz), TICKET_RADIUS);
            }
        }
    }

    /**
     * 名字是否被认定为加载器。归一化 = 转小写并剔除空白与下划线，
     * 使 "Chunk Loader"、"chunk_loader"、"CHUNKLOADER" 视为同一写法。
     */
    private static boolean isLoaderName(String raw) {
        StringBuilder normalized = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (!Character.isWhitespace(c) && c != '_') {
                normalized.append(Character.toLowerCase(c));
            }
        }
        String name = normalized.toString();
        return name.contains(LOADER_KEYWORD) || LOADER_NAMES.contains(name);
    }
}
