package net.what42.aliveworld;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.what42.aliveworld.util.ConfigDeserializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
	private static final Gson GSON = new GsonBuilder()
		.setPrettyPrinting()
		.registerTypeAdapter(Config.class, new ConfigDeserializer())
		.create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("alive_world.json");
	
	private boolean furnacesEnabled = true;
	private boolean brewingStandsEnabled = true;
	private boolean campfiresEnabled = true;
	private boolean mobGrowingEnabled = true;
	private boolean chickenEggLayingEnabled = true;
	private boolean cropsGrowingEnabled = true;
	
	private static Config INSTANCE;
	
	private Config() {}
	
	public static Config getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Config();
			INSTANCE.load();
		}
		return INSTANCE;
	}
	
	private void load() {
		try {
			if (Files.exists(CONFIG_PATH)) {
				String jsonStr = Files.readString(CONFIG_PATH);
				GSON.fromJson(jsonStr, Config.class);
			}
			save();
		} catch (IOException e) {
			AliveWorld.LOGGER.error("Failed to load Alive World config: {}", e.getMessage());
			save();  // Guardar config por defecto si falla la carga
		}
	}
	
	private void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, GSON.toJson(this));
		} catch (IOException e) {
			AliveWorld.LOGGER.error("Failed to save Alive World config: {}", e.getMessage());
		}
	}
	
	public boolean isFurnacesEnabled() {
		return furnacesEnabled;
	}
	
	public void setFurnacesEnabled(boolean furnacesEnabled) {
		this.furnacesEnabled = furnacesEnabled;
	}
	
	public boolean isBrewingStandsEnabled() {
		return brewingStandsEnabled;
	}
	
	public void setBrewingStandsEnabled(boolean brewingStandsEnabled) {
		this.brewingStandsEnabled = brewingStandsEnabled;
	}
	
	public boolean isCampfiresEnabled() {
		return campfiresEnabled;
	}
	
	public void setCampfiresEnabled(boolean campfiresEnabled) {
		this.campfiresEnabled = campfiresEnabled;
	}
	
	public boolean isMobGrowingEnabled() {
		return mobGrowingEnabled;
	}
	
	public void setMobGrowingEnabled(boolean mobGrowingEnabled) {
		this.mobGrowingEnabled = mobGrowingEnabled;
	}
	
	public boolean isChickenEggLayingEnabled() {
		return chickenEggLayingEnabled;
	}
	
	public void setChickenEggLayingEnabled(boolean chickenEggLayingEnabled) {
		this.chickenEggLayingEnabled = chickenEggLayingEnabled;
	}
	
	public boolean isCropsGrowingEnabled() {
		return cropsGrowingEnabled;
	}
	
	public void setCropsGrowingEnabled(boolean cropsGrowingEnabled) {
		this.cropsGrowingEnabled = cropsGrowingEnabled;
	}
}
