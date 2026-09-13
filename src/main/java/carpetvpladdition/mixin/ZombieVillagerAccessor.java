package carpetvpladdition.mixin;

import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * ZombieVillager 访问器（b1.15.1.1）：gossips 字段无公开 getter，
 * tradedZombieVillagerPeace 规则需要读取它判断玩家交易记录。
 */
@Mixin(ZombieVillager.class)
public interface ZombieVillagerAccessor {

    @Accessor("gossips")
    GossipContainer vpl$getGossips();
}
