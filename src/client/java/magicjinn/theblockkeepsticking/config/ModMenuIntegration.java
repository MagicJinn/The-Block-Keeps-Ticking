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
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModMenuIntegration::createScreen;
    }

    private static Screen createScreen(Screen parent) {
            // Ensure config is loaded from file before creating the screen
            ModConfig.HANDLER.load();
        ModConfig.ensureDefaultsPresent();

        // Time mode option
        Option<ModConfig.TimeMode> timeModeOpt = Option.<ModConfig.TimeMode>createBuilder()
                .name(Text.literal("Time Source"))
                .description(OptionDescription.of(Text.literal(
                                        "Choose whether to use world time or real time for simulation calculations. World time only passes when you are in your world. Real time passes at the same rate as the real world, even when you are not playing the game.\n(Real Time works well with lazy tax.)")))
                        .binding(ModConfig.getTimeMode(), () -> ModConfig.getTimeMode(),
                        newVal -> {
                                                ModConfig.setTimeMode(newVal);
                        })
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(ModConfig.TimeMode.class))
                .build();

        // Lazy tax option
        Option<Integer> lazyTaxOpt = Option.<Integer>createBuilder()
                .name(Text.literal("Lazy Tax (%)"))
                .description(OptionDescription.of(Text.literal(
                                        "Simulated ticks are reduced by this percentage. Increasing this value slows down simulation in unloaded chunks.")))
                        .binding(ModConfig.getLazyTaxPercent(), ModConfig::getLazyTaxPercent,
                        newVal -> {
                                                ModConfig.setLazyTaxPercent(newVal);
                        })
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 99).step(1))
                .build();

        // Debug logging option
        Option<Boolean> debugLoggingOpt = Option.<Boolean>createBuilder()
                        .name(Text.literal("Debug Logging"))
                        .description(OptionDescription.of(Text.literal(
                                        "Enables detailed logging when simulation occurs. Shows which blocks, block entities, and entities are being simulated.")))
                        .binding(ModConfig.isDebugLogging(), ModConfig::isDebugLogging, newVal -> {
                                ModConfig.setDebugLogging(newVal);
                        }).controller(BooleanControllerBuilder::create).build();

        OptionGroup general = OptionGroup.createBuilder().name(Text.literal("General"))
                        .option(timeModeOpt).option(lazyTaxOpt).option(debugLoggingOpt).build();

        // Per-object toggles
        OptionGroup.Builder objectsGroupBuilder =
                OptionGroup.createBuilder().name(Text.literal("Ticking Objects"));

        ModConfig.getEnabledByName().forEach((name, val) -> {
            Option<Boolean> objOpt = Option.<Boolean>createBuilder().name(Text.literal(name))
                                .description(OptionDescription.of(Text
                                                .literal("Enables/disables simulation for "
                                                                + name.toLowerCase() + ".\n\n\n")
                                                .append(Text.literal("(auto generated setting)")
                                                                .formatted(Formatting.GRAY))))
                    .binding(ModConfig.getEnabledByName().getOrDefault(name, true),
                            () -> ModConfig.getEnabledByName().getOrDefault(name, true), newVal -> {
                                ModConfig.getInstance().enabledByName.put(name, newVal);
                                ModConfig.HANDLER.save();
                            })
                    .controller(BooleanControllerBuilder::create).build();
            objectsGroupBuilder.option(objOpt);
        });

        // Dummy "Coming Soon" settings
        String[] dummySettings = {"Chicken eggs", "Beehives", "Villager breeding", "Leaf Decay",
                        "Copper oxidizing"};

        for (String settingName : dummySettings) {
                Option<String> dummyOpt = Option.<String>createBuilder()
                                .name(Text.literal(settingName))
                                .description(OptionDescription.of(Text.literal("Coming Soon!(?)")))
                                .binding("Coming Soon!(?)", () -> "Coming Soon!(?)", newVal -> {
                                }).available(false).controller(StringControllerBuilder::create)
                                .build();
                objectsGroupBuilder.option(dummyOpt);
        }

        return YetAnotherConfigLib.createBuilder().title(Text.literal("")) // does nothing
                .category(
                        ConfigCategory.createBuilder().name(Text.literal("The Block Keeps Ticking"))
                                .group(general).group(objectsGroupBuilder.build()).build())
                .build().generateScreen(parent);
    }
}
