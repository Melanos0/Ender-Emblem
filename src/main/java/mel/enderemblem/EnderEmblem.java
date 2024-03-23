package mel.enderemblem;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.*;

public class EnderEmblem implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final String MOD_ID = "ender-emblem";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public interface PlayerEntityMixinAccess{
		double getSpeed();
		void setSpeed(double value);
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("setSpeed")
		.then(argument("value", IntegerArgumentType.integer()).executes(context -> {
			final int value = IntegerArgumentType.getInteger(context, "value");
			if(value < 0 || value > 100) {
				context.getSource().sendFeedback(() -> Text.literal("Invalid Int. Must be between 0-100."), false);
			}else {
				((PlayerEntityMixinAccess)context.getSource().getPlayer()).setSpeed((float)value);
				context.getSource().getPlayer().getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
						.setBaseValue(0.10000000149011612*(value/100.0 + 0.6));
				context.getSource().getPlayer().setMovementSpeed((float)(0.10000000149011612*(value/100.0 + 0.6)));
				context.getSource().sendFeedback(() -> Text.literal("Speed: %s".formatted(((PlayerEntityMixinAccess)context
					.getSource().getPlayer()).getSpeed())), false);
				context.getSource().sendFeedback(() -> Text.literal("Set your Speed Stat to %s".formatted(value)), false);
			}
			return 1;
		}))));
		LOGGER.info("Pissing all by yourself handsome?");
	}
}