package mel.enderemblem;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import mel.enderemblem.mixin.PlayerEntityMixin;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Math.floor;
import static net.minecraft.server.command.CommandManager.*;

public class EnderEmblem implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final String MOD_ID = "ender-emblem";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public interface PlayerEntityMixinAccess{
		double getHealthStat();
		void setHealthStat(double value);
		double getStrength();
		void setStrength(double value);
		double getSpeed();
		void setSpeed(double value);
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		//Keep stats after death & dimension changes
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			EntityAttributeInstance oldPlayerMhealth = oldPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
			EntityAttributeInstance newPlayerMhealth = newPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
			EntityAttributeInstance oldPlayerStrength = oldPlayer.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
			EntityAttributeInstance newPlayerStrength = newPlayer.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
			EntityAttributeInstance oldPlayerMspeed = oldPlayer.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
			EntityAttributeInstance newPlayerMspeed = newPlayer.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
			((PlayerEntityMixinAccess)newPlayer).setHealthStat(((PlayerEntityMixinAccess)oldPlayer).getHealthStat());
			newPlayerMhealth.setBaseValue(oldPlayerMhealth.getBaseValue());
			newPlayer.setHealth(oldPlayer.getHealth());
			newPlayerStrength.setBaseValue(oldPlayerStrength.getBaseValue());
			((PlayerEntityMixinAccess)newPlayer).setStrength(((PlayerEntityMixinAccess)oldPlayer).getStrength());
			((PlayerEntityMixinAccess)newPlayer).setSpeed(((PlayerEntityMixinAccess)oldPlayer).getSpeed());
			newPlayerMspeed.setBaseValue(oldPlayerMspeed.getBaseValue());
			newPlayer.getAbilities().setWalkSpeed(oldPlayer.getAbilities().getWalkSpeed());
			newPlayer.sendAbilitiesUpdate();
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			((PlayerEntityMixinAccess)newPlayer).setHealthStat(((PlayerEntityMixinAccess)oldPlayer).getHealthStat());
			newPlayer.setHealth((float)((PlayerEntityMixinAccess)oldPlayer).getHealthStat());
			newPlayer.getAbilities().setWalkSpeed(oldPlayer.getAbilities().getWalkSpeed());
			newPlayer.sendAbilitiesUpdate();
		});

		//Set & Get Stat Commands
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("setHealth")
				.then(argument("value", IntegerArgumentType.integer()).executes(context -> {
					final int value = IntegerArgumentType.getInteger(context, "value");
					if(value < 0 || value > 100) {
						context.getSource().sendFeedback(() -> Text.literal("Invalid Int. Must be between 0-100."), false);
					}else {
						((PlayerEntityMixinAccess)context.getSource().getPlayer()).setHealthStat(value);
						context.getSource().getPlayer().getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
								.setBaseValue(floor(40*(value/100.0)));
						context.getSource().getPlayer().setHealth((float)floor(40*(value/100.0)));
						context.getSource().sendFeedback(() -> Text.literal("Set your Max Health Stat to %s".formatted(value)), false);
					}
					return 1;
				}))));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("getHealth")
				.executes(context -> {
					context.getSource().sendFeedback(() -> Text.literal("Max Health: %s, GMax Health: %s".formatted(
							((PlayerEntityMixinAccess)context.getSource().getPlayer()).getHealthStat(),
							context.getSource().getPlayer().getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
									.getBaseValue())), false);
					return 1;
				})));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("setStrength")
				.then(argument("value", IntegerArgumentType.integer()).executes(context -> {
					final int value = IntegerArgumentType.getInteger(context, "value");
					if(value < 0 || value > 100) {
						context.getSource().sendFeedback(() -> Text.literal("Invalid Int. Must be between 0-100."), false);
					}else {
						((PlayerEntityMixinAccess)context.getSource().getPlayer()).setStrength(value);
						context.getSource().getPlayer().getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
								.setBaseValue(3*(value/100.0));
						context.getSource().sendFeedback(() -> Text.literal("Set your Strength Stat to %s".formatted(value)), false);
					}
					return 1;
				}))));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("getStrength")
				.executes(context -> {
					context.getSource().sendFeedback(() -> Text.literal("Strength: %s, GStrength: %s".formatted(
							((PlayerEntityMixinAccess)context.getSource().getPlayer()).getStrength(),
							context.getSource().getPlayer().getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
									.getBaseValue())), false);
					return 1;
				})));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("setSpeed")
		.then(argument("value", IntegerArgumentType.integer()).executes(context -> {
			final int value = IntegerArgumentType.getInteger(context, "value");
			if(value < 0 || value > 100) {
				context.getSource().sendFeedback(() -> Text.literal("Invalid Int. Must be between 0-100."), false);
			}else {
				((PlayerEntityMixinAccess)context.getSource().getPlayer()).setSpeed(value);
				context.getSource().getPlayer().getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
						.setBaseValue(0.10000000149011612*(value/100.0 + 0.6));
				context.getSource().getPlayer().getAbilities().setWalkSpeed((float)context.getSource().getPlayer()
						.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).getBaseValue());
				context.getSource().getPlayer().sendAbilitiesUpdate();
				context.getSource().sendFeedback(() -> Text.literal("Set your Speed Stat to %s".formatted(value)), false);
			}
			return 1;
		}))));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("getSpeed")
				.executes(context -> {
					context.getSource().sendFeedback(() -> Text.literal("Speed: %s, GMSpeed: %s, WSpeed: %s".formatted(
							((PlayerEntityMixinAccess)context.getSource().getPlayer()).getSpeed(),
							context.getSource().getPlayer().getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
									.getBaseValue(), context.getSource().getPlayer().getAbilities().getWalkSpeed())), false);
					return 1;
				})));

		LOGGER.info("Pissing all by yourself handsome?");
	}
}