package magicjinn.theblockkeepsticking.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import magicjinn.theblockkeepsticking.Constants;
import magicjinn.theblockkeepsticking.simulator.WorldSimulator;
import magicjinn.theblockkeepsticking.util.TickingObject;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** YACL config screen. Only referenced when YACL is installed. */
public final class ModConfigScreen {
    private static final String[] DUMMY_SETTINGS = { "Beehives", "Villager breeding", "Copper oxidizing" };

    public static Screen create(Screen parent) {
        ModConfig.HANDLER.load();
        ModConfig.ensureDefaultsPresent();

        OptionGroup.Builder generalBuilder = OptionGroup.createBuilder().name(Component.literal("General"));
        for (ConfigOptions.Option option : ConfigOptions.GENERAL) {
            generalBuilder.option(toYaclOption(option));
        }

        OptionGroup.Builder objectsGroupBuilder = OptionGroup.createBuilder()
                .name(Component.literal("Ticking Objects"))
                .description(OptionDescription.of(Component.literal(
                        "Enable or disable simulation for each supported block, block entity, and entity. Disabled entries are skipped when unloaded chunks catch up.")));

        for (TickingObject tickingObject : WorldSimulator.TickingObjectInstances) {
            String name = tickingObject.getName();
            String modId = tickingObject.getModId();
            Option<Boolean> objOpt = Option.<Boolean>createBuilder().name(Component.literal(name))
                    .description(OptionDescription.of(Component
                            .literal("Enables/disables simulation for " + name.toLowerCase()
                                    + ".\n\n\n")
                            .append(Component
                                    .literal("(setting added by mod: " + modId
                                            + ")")
                                    .withStyle(ChatFormatting.GRAY))))
                    .binding(ModConfig.getEnabledByName().getOrDefault(name, true),
                            () -> ModConfig.getEnabledByName().getOrDefault(name, true),
                            newVal -> ModConfig.setEnabled(name, newVal))
                    .controller(BooleanControllerBuilder::create).build();
            objectsGroupBuilder.option(objOpt);
            if ("Ageable Entities".equals(name)) {
                String eggKey = ModConfig.CHICKEN_EGG_LAYING_IN_UNLOADED;
                Option<Boolean> chickenEggOpt = Option.<Boolean>createBuilder()
                        .name(Component.literal(eggKey))
                        .description(OptionDescription.of(Component.literal(
                                "Enables/disables simulation for chicken egg laying.\n\n\n")
                                .append(Component.literal(
                                        "(setting added by mod: "
                                                + Constants.FABRIC_MOD_ID
                                                + ")")
                                        .withStyle(ChatFormatting.GRAY))))
                        .binding(ModConfig.getEnabledByName().getOrDefault(eggKey, true),
                                () -> ModConfig.getEnabledByName().getOrDefault(eggKey,
                                        true),
                                newVal -> ModConfig.setEnabled(eggKey, newVal))
                        .controller(BooleanControllerBuilder::create).build();
                objectsGroupBuilder.option(chickenEggOpt);
            }
        }

        for (String settingName : DUMMY_SETTINGS) {
            Option<String> dummyOpt = Option.<String>createBuilder()
                    .name(Component.literal(settingName))
                    .description(OptionDescription.of(Component.literal("Coming Soon!(?)")))
                    .binding("Coming Soon!(?)", () -> "Coming Soon!(?)", newVal -> {
                    }).available(false).controller(StringControllerBuilder::create)
                    .build();
            objectsGroupBuilder.option(dummyOpt);
        }

        return YetAnotherConfigLib.createBuilder().title(Component.literal(""))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("The Block Keeps Ticking"))
                        .group(generalBuilder.build()).group(objectsGroupBuilder.build()).build())
                .build().generateScreen(parent);
    }

    private static Option<?> toYaclOption(ConfigOptions.Option option) {
        return switch (option) {
            case ConfigOptions.BoolOption boolOption -> Option.<Boolean>createBuilder()
                    .name(Component.literal(boolOption.displayName()))
                    .description(OptionDescription.of(Component.literal(boolOption.description())))
                    .binding(boolOption.get(), boolOption::get, boolOption::set)
                    .controller(BooleanControllerBuilder::create)
                    .build();
            case ConfigOptions.IntOption intOption -> Option.<Integer>createBuilder()
                    .name(Component.literal(intOption.displayName()))
                    .description(OptionDescription.of(Component.literal(intOption.description())))
                    .binding(intOption.get(), intOption::get, intOption::set)
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                            .range(intOption.min(), intOption.max())
                            .step(intOption.step()))
                    .build();
            case ConfigOptions.EnumOption<?> enumOption -> enumYaclOption(enumOption);
        };
    }

    private static <E extends Enum<E>> Option<E> enumYaclOption(ConfigOptions.EnumOption<E> enumOption) {
        return Option.<E>createBuilder()
                .name(Component.literal(enumOption.displayName()))
                .description(OptionDescription.of(Component.literal(enumOption.description())))
                .binding(enumOption.get(), enumOption::get, enumOption::set)
                .controller(opt -> EnumControllerBuilder.create(opt).enumClass(enumOption.type()))
                .build();
    }
}
