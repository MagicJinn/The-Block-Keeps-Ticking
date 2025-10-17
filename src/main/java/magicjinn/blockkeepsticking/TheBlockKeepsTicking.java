package magicjinn.blockkeepsticking;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.registry.FabricRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.util.Identifier;

import net.minecraft.server.world.ServerWorld;

public class TheBlockKeepsTicking implements ModInitializer
{
	public static final String MOD_ID = "the-block-keeps-ticking";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize ()
	{
		AttachmentRegistry.createPersistent(new Identifier(MOD_ID, ""))

		LOGGER.info("The Block Keeps Ticking is initializing!");
	}
}