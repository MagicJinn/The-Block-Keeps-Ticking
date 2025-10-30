package magicjinn.theblockkeepsticking.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModMenuIntegration::createScreen;
    }

    private static Screen createScreen(Screen parent) {
        ModConfig.ensureDefaultsPresent();

        // Time mode option
        Option<ModConfig.TimeMode> timeModeOpt = Option.<ModConfig.TimeMode>createBuilder()
                .name(Text.literal("Time Source"))
                .description(OptionDescription.of(Text.literal(
                        "Choose whether to use world time or real time for simulation calculations")))
                .binding(ModConfig.getInstance().timeMode, () -> ModConfig.getTimeMode(),
                        newVal -> {
                            ModConfig.getInstance().timeMode = newVal;
                            ModConfig.HANDLER.save();
                        })
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(ModConfig.TimeMode.class))
                .build();

        // Lazy tax option
        Option<Integer> lazyTaxOpt = Option.<Integer>createBuilder()
                .name(Text.literal("Lazy Tax (%)"))
                .description(OptionDescription.of(Text.literal(
                        "Reduce simulated ticks by percentage. Higher values reduce simulation speed in unloaded chunks.")))
                .binding(ModConfig.getInstance().lazyTaxPercent, ModConfig::getLazyTaxPercent,
                        newVal -> {
                            ModConfig.getInstance().lazyTaxPercent = newVal;
                            ModConfig.HANDLER.save();
                        })
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 99).step(1))
                .build();

        OptionGroup general = OptionGroup.createBuilder().name(Text.literal("General"))
                .option(timeModeOpt).option(lazyTaxOpt).build();

        // Per-object toggles
        OptionGroup.Builder objectsGroupBuilder =
                OptionGroup.createBuilder().name(Text.literal("Ticking Objects"));

        ModConfig.getEnabledByName().forEach((name, val) -> {
            Option<Boolean> objOpt = Option.<Boolean>createBuilder().name(Text.literal(name))
                    .description(OptionDescription.of(Text.literal(
                            "Enables/disables simulation for " + name.toLowerCase() + ".")))
                    .binding(ModConfig.getEnabledByName().getOrDefault(name, true),
                            () -> ModConfig.getEnabledByName().getOrDefault(name, true), newVal -> {
                                ModConfig.getInstance().enabledByName.put(name, newVal);
                                ModConfig.HANDLER.save();
                            })
                    .controller(BooleanControllerBuilder::create).build();
            objectsGroupBuilder.option(objOpt);
        });

        return YetAnotherConfigLib.createBuilder().title(Text.literal("")) // does nothing
                .category(
                        ConfigCategory.createBuilder().name(Text.literal("The Block Keeps Ticking"))
                                .group(general).group(objectsGroupBuilder.build()).build())
                .build().generateScreen(parent);
    }
}
