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
import magicjinn.theblockkeepsticking.simulator.WorldSimulator;
import magicjinn.theblockkeepsticking.util.TickingObject;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
                .name(Component.literal("Time Source"))
                .description(OptionDescription.of(Component.literal(
                        "Choose whether to use world time or real time for simulation calculations. World time only passes when you are in your level. Real time passes at the same rate as the real world, even when you are not playing the game.\n(Real Time works well with lazy tax.)")))
                .binding(ModConfig.getTimeMode(), () -> ModConfig.getTimeMode(),
                        newVal -> {
                            ModConfig.setTimeMode(newVal);
                        })
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(ModConfig.TimeMode.class))
                .build();

        // Lazy tax option
        Option<Integer> lazyTaxOpt = Option.<Integer>createBuilder()
                .name(Component.literal("Lazy Tax (%)"))
                .description(OptionDescription.of(Component.literal(
                        "Simulated ticks are reduced by this percentage. Increasing this value slows down simulation in unloaded chunks.")))
                .binding(ModConfig.getLazyTaxPercent(), ModConfig::getLazyTaxPercent,
                        newVal -> {
                            ModConfig.setLazyTaxPercent(newVal);
                        })
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 99).step(1))
                .build();

        // Simulate chunks when sleeping option
        Option<Boolean> simulateChunksWhenSleepingOpt = Option.<Boolean>createBuilder()
                .name(Component.literal("Simulate After Sleeping"))
                .description(OptionDescription.of(Component.literal(
                        "When enabled, time skips (e.g. from sleeping) will simulate loaded chunks for the duration of the time skip.")))
                .binding(ModConfig.isSimulateChunksWhenSleeping(), ModConfig::isSimulateChunksWhenSleeping,
                        ModConfig::setSimulateChunksWhenSleeping)
                .controller(BooleanControllerBuilder::create)
                .build();

        // Debug logging option
        Option<Boolean> debugLoggingOpt = Option.<Boolean>createBuilder()
                .name(Component.literal("Debug Logging"))
                .description(OptionDescription.of(Component.literal(
                        "Enables detailed logging when simulation occurs. Shows which blocks, block entities, and entities are being simulated.")))
                .binding(ModConfig.isDebugLogging(), ModConfig::isDebugLogging, newVal -> {
                    ModConfig.setDebugLogging(newVal);
                }).controller(BooleanControllerBuilder::create).build();

        OptionGroup general = OptionGroup.createBuilder().name(Component.literal("General"))
                .option(timeModeOpt).option(lazyTaxOpt).option(simulateChunksWhenSleepingOpt)
                .option(debugLoggingOpt).build();

        // Per-object toggles
        OptionGroup.Builder objectsGroupBuilder = OptionGroup.createBuilder()
                .name(Component.literal("Ticking Objects"));

        for (TickingObject tickingObject : WorldSimulator.TickingObjectInstances) {
            String name = tickingObject.getName();
            String modId = tickingObject.getModId();
            Option<Boolean> objOpt = Option.<Boolean>createBuilder().name(Component.literal(name))
                    .description(OptionDescription.of(Component
                            .literal("Enables/disables simulation for "
                                    + name.toLowerCase() + ".\n\n\n")
                            .append(Component.literal(
                                    "(setting added by mod: " + modId + ")")
                                    .withStyle(ChatFormatting.GRAY))))
                    .binding(ModConfig.getEnabledByName().getOrDefault(name, true),
                            () -> ModConfig.getEnabledByName().getOrDefault(name, true), newVal -> {
                                ModConfig.getInstance().enabledByName.put(name, newVal);
                                ModConfig.HANDLER.save();
                            })
                    .controller(BooleanControllerBuilder::create).build();
            objectsGroupBuilder.option(objOpt);
            if ("Ageable Entities".equals(name)) {
                String eggKey = ModConfig.CHICKEN_EGG_LAYING_IN_UNLOADED;
                Option<Boolean> chickenEggOpt = Option.<Boolean>createBuilder()
                        .name(Component.literal(eggKey))
                        .description(OptionDescription.of(Component.literal(
                                "Enables/disables simulation for chicken egg laying.\n\n\n")
                                .append(Component.literal(
                                        "(setting added by mod: the-block-keeps-ticking)")
                                        .withStyle(ChatFormatting.GRAY))))
                        .binding(ModConfig.getEnabledByName().getOrDefault(eggKey, true),
                                () -> ModConfig.getEnabledByName().getOrDefault(eggKey, true),
                                newVal -> {
                                    ModConfig.getInstance().enabledByName.put(eggKey, newVal);
                                    ModConfig.HANDLER.save();
                                })
                        .controller(BooleanControllerBuilder::create).build();
                objectsGroupBuilder.option(chickenEggOpt);
            }
    }

    // Dummy "Coming Soon" settings
    String[] dummySettings = { "Beehives", "Villager breeding", "Leaf Decay",
            "Copper oxidizing" };

    for (String settingName : dummySettings) {
        Option<String> dummyOpt = Option.<String>createBuilder()
                .name(Component.literal(settingName))
                .description(OptionDescription.of(Component.literal("Coming Soon!(?)")))
                .binding("Coming Soon!(?)", () -> "Coming Soon!(?)", newVal -> {
                }).available(false).controller(StringControllerBuilder::create)
                .build();
        objectsGroupBuilder.option(dummyOpt);
    }

    return YetAnotherConfigLib.createBuilder().title(Component.literal("")) // does nothing
            .category(
                    ConfigCategory.createBuilder()
                            .name(Component.literal("The Block Keeps Ticking"))
                            .group(general).group(objectsGroupBuilder.build()).build())
            .build().generateScreen(parent);
}
}
