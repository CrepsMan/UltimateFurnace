package com.crepsman.ultimate_furnace;

import com.crepsman.ultimate_furnace.blocks.entity.UltimateFurnaceBlockEntity;
import com.crepsman.ultimate_furnace.registry.ModBlocks;
import com.crepsman.ultimate_furnace.registry.ModScreenHandlers;
import com.crepsman.ultimate_furnace.screen.UltimateFurnaceScreen;
import com.crepsman.ultimate_furnace.util.ClientConfig;
import com.crepsman.ultimate_furnace.util.FurnaceConfig;
import net.minecraft.ChatFormatting;
//? if !neoforge {
import net.minecraft.client.gui.screens.MenuScreens;
//?} else if <1.20.4 {
/*import net.minecraft.client.gui.screens.MenuScreens;
*///?}
//? if >=1.20.5 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
//?}
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
//? if fabric {
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
//?}
//? if forge {
/*import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
//? if >=1.21.6 {
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
//?} else {
/^import net.minecraftforge.eventbus.api.SubscribeEvent;
^///?}
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
*///?}
//? if neoforge {
/*import net.neoforged.bus.api.IEventBus;
//? if >=1.20.4 {
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
//?}
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
*///?}

import java.util.List;

//? if forge {
/*@Mod.EventBusSubscriber(modid = UltimateFurnaceMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class UltimateFurnaceModClient {
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() ->
			MenuScreens.register(ModScreenHandlers.ULTIMATE_FURNACE_SCREEN_HANDLER, UltimateFurnaceScreen::new)
		);
		ClientConfig.load();
	}

	@Mod.EventBusSubscriber(modid = UltimateFurnaceMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
	public static class ForgeClientEvents {
		@SubscribeEvent
		public static void onItemTooltip(ItemTooltipEvent event) {
			appendFurnaceTooltip(event.getItemStack(), event.getToolTip());
		}
	}
*///?}

//? if neoforge {
/*public class UltimateFurnaceModClient {
	public static void init(IEventBus modEventBus) {
		//? if >=1.20.4 {
		modEventBus.addListener(UltimateFurnaceModClient::onRegisterMenuScreens);
		//?}
		modEventBus.addListener(UltimateFurnaceModClient::onClientSetup);
		NeoForge.EVENT_BUS.addListener(UltimateFurnaceModClient::onItemTooltip);
	}

	//? if >=1.20.4 {
	private static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
		event.register(ModScreenHandlers.ULTIMATE_FURNACE_SCREEN_HANDLER, UltimateFurnaceScreen::new);
	}
	//?}

	private static void onClientSetup(FMLClientSetupEvent event) {
		//? if <1.20.4 {
		/^event.enqueueWork(() ->
			MenuScreens.register(ModScreenHandlers.ULTIMATE_FURNACE_SCREEN_HANDLER, UltimateFurnaceScreen::new)
		);
		^///?}
		ClientConfig.load();
	}

	private static void onItemTooltip(ItemTooltipEvent event) {
		appendFurnaceTooltip(event.getItemStack(), event.getToolTip());
	}
*///?}

//? if fabric {
public class UltimateFurnaceModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModScreenHandlers.ULTIMATE_FURNACE_SCREEN_HANDLER, UltimateFurnaceScreen::new);
		ClientConfig.load();

		//? if >=1.20.5 {
		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> appendFurnaceTooltip(stack, lines));
		//?} else {
		/*ItemTooltipCallback.EVENT.register((stack, context, lines) -> appendFurnaceTooltip(stack, lines));
		*///?}
	}
//?}

	private static void appendFurnaceTooltip(ItemStack stack, List<Component> lines) {
		if (stack.getItem() == ModBlocks.ULTIMATE_FURNACE.asItem()) {
			//? if >=1.20.5 {
			CustomData data = stack.get(DataComponents.CUSTOM_DATA);
			CompoundTag tag = data != null ? data.copyTag() : null;
			//?} else {
			/*CompoundTag tag = stack.getTag();
			*///?}
			if (tag != null) {
				//? if >=1.21.5 {
				int level = tag.getIntOr("Level", 1);
				int smelts = tag.getIntOr("SmeltCount", 0);
				int stored = tag.getIntOr("StoredPower", 0);
				//?} else {
				/*int level = tag.getInt("Level");
				int smelts = tag.getInt("SmeltCount");
				int stored = tag.getInt("StoredPower");
				*///?}
				int maxStored = UltimateFurnaceBlockEntity.getMaxStoredPower(level);
				lines.add(Component.translatable("tooltip.ultimate_furnace.level", level).withStyle(ChatFormatting.GRAY));
				int itemsPerLevel = FurnaceConfig.getItemsPerLevel();
				int threshold = Math.max(1, itemsPerLevel * Math.max(1, level));
				lines.add(Component.translatable("tooltip.ultimate_furnace.smelted", smelts).append(Component.literal(" / ")).append(Component.literal(String.valueOf(threshold))).withStyle(ChatFormatting.DARK_GRAY));
				lines.add(Component.translatable("tooltip.ultimate_furnace.power", stored, maxStored).withStyle(ChatFormatting.GOLD));
				if (maxStored > 0) {
					boolean unicode = ClientConfig.useUnicodeBar();
					ChatFormatting color = safeColor(ClientConfig.barColor());
					int barLen = 20;
					double pct = Math.min(1.0, stored / (double) maxStored);
					int filled = (int) Math.round(barLen * pct);
					StringBuilder bar = new StringBuilder();
					char filledChar = unicode ? '█' : '|';
					char emptyChar = unicode ? '░' : '.';
					for (int i = 0; i < barLen; i++) bar.append(i < filled ? filledChar : emptyChar);
					lines.add(Component.literal(bar.toString()).withStyle(color));
				}
			}
		}
	}

	private static ChatFormatting safeColor(String name) {
		try { return ChatFormatting.valueOf(name.toUpperCase()); } catch (Exception e) { return ChatFormatting.DARK_GREEN; }
	}
}
