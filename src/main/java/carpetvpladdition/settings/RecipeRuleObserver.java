package carpetvpladdition.settings;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Validator;
import net.minecraft.commands.CommandSourceStack;

public class RecipeRuleObserver extends Validator<Boolean> {
    // 防重复执行标志
    private static boolean pendingReload = false;

    @Override
    public Boolean validate(CommandSourceStack source, CarpetRule<Boolean> rule, Boolean newValue, String userInput) {
        Boolean oldValue = rule.value();
        if (oldValue != newValue) {
            onValueChange(source);
        }
        return newValue;
    }

    private void onValueChange(CommandSourceStack source) {
        // 异步重载配方，避免在主线程同步执行完整 reloadResources
        if (source != null && source.getServer() != null && !pendingReload) {
            pendingReload = true;
            source.getServer().executeIfPossible(() -> {
                try {
                    source.getServer().reloadResources(source.getServer().getPackRepository().getSelectedIds());
                } finally {
                    pendingReload = false;
                }
            });
        }
    }
}
