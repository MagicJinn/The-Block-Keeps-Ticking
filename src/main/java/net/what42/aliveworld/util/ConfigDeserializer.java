package net.what42.aliveworld.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.what42.aliveworld.Config;

import java.lang.reflect.Type;

public class ConfigDeserializer implements JsonDeserializer<Config> {
    @Override
    public Config deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        Config config = Config.getInstance();
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            if (obj.has("furnacesEnabled")) {
                config.setFurnacesEnabled(obj.get("furnacesEnabled").getAsBoolean());
            }
            if (obj.has("brewingStandsEnabled")) {
                config.setBrewingStandsEnabled(obj.get("brewingStandsEnabled").getAsBoolean());
            }
            if (obj.has("campfiresEnabled")) {
                config.setCampfiresEnabled(obj.get("campfiresEnabled").getAsBoolean());
            }
            if (obj.has("mobGrowingEnabled")) {
                config.setMobGrowingEnabled(obj.get("mobGrowingEnabled").getAsBoolean());
            }
            if (obj.has("chickenEggLayingEnabled")) {
                config.setChickenEggLayingEnabled(obj.get("chickenEggLayingEnabled").getAsBoolean());
            }
            if (obj.has("cropsGrowingEnabled")) {
                config.setCropsGrowingEnabled(obj.get("cropsGrowingEnabled").getAsBoolean());
            }
        }
        return config;
    }
}
