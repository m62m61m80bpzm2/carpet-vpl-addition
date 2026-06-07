package carpetvpladdition.settings;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Validator;
import net.minecraft.commands.CommandSourceStack;

public class RecipeRuleObserver extends Validator<Boolean> {
    @Override
    public Boolean validate(CommandSourceStack source, CarpetRule<Boolean> rule, Boolean newValue, String userInput) {
        Boolean oldValue = rule.value();
        if (oldValue != newValue) {
            onValueChange(source, rule, oldValue, newValue);
        }
        return newValue;
    }

    private void onValueChange(CommandSourceStack source, CarpetRule<Boolean> rule, Boolean oldValue, Boolean newValue) {
        if (source != null && source.getServer() != null) {
            source.getServer().reloadResources(source.getServer().getPackRepository().getSelectedIds());
        }
    }
}
