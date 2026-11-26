package com.crepsman.ultimate_furnace.screen;

import com.crepsman.ultimate_furnace.UltimateFurnaceMod;
import com.crepsman.ultimate_furnace.blocks.entity.UltimateFurnaceBlockEntity;
import com.crepsman.ultimate_furnace.util.FurnaceConfig;
import net.minecraft.ChatFormatting;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else if >=1.20 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?} else {
/*import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
*///?}
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
//? if >=1.21.2 {
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
//?}
//? if >=1.21.6 {
import net.minecraft.client.renderer.RenderPipelines;
//?} else if >=1.21.2 {
/*import net.minecraft.client.renderer.RenderType;
*///?}
import net.minecraft.network.chat.Component;
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
//? if >=1.21.2 {
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeBookCategories;
//?}

import java.util.ArrayList;
import java.util.List;

public class UltimateFurnaceScreen extends AbstractFurnaceScreen<UltimateFurnaceScreenHandler> {
	//? if >=1.21.11 {
	private static final Identifier LIT_PROGRESS_TEXTURE = Identifier.withDefaultNamespace("container/furnace/lit_progress");
	private static final Identifier BURN_PROGRESS_TEXTURE = Identifier.withDefaultNamespace("container/furnace/burn_progress");
	private static final Identifier LIT_HOT = UltimateFurnaceMod.id("container/ultimate_furnace/lit_hot");
	private static final Identifier ULTIMATE_BAR = UltimateFurnaceMod.id("container/ultimate_furnace/ultimate_bar");
	private static final Identifier TEXTURE = UltimateFurnaceMod.id("textures/gui/container/ultimate_furnace.png");
	//?} else if >=1.21 {
	/*private static final ResourceLocation LIT_PROGRESS_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "container/furnace/lit_progress");
	private static final ResourceLocation BURN_PROGRESS_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "container/furnace/burn_progress");
	private static final ResourceLocation LIT_HOT = UltimateFurnaceMod.id("container/ultimate_furnace/lit_hot");
	private static final ResourceLocation ULTIMATE_BAR = UltimateFurnaceMod.id("container/ultimate_furnace/ultimate_bar");
	private static final ResourceLocation TEXTURE = UltimateFurnaceMod.id("textures/gui/container/ultimate_furnace.png");
	*///?} else if >=1.20.2 {
	/*private static final ResourceLocation LIT_PROGRESS_TEXTURE = new ResourceLocation("minecraft", "container/furnace/lit_progress");
	private static final ResourceLocation BURN_PROGRESS_TEXTURE = new ResourceLocation("minecraft", "container/furnace/burn_progress");
	private static final ResourceLocation LIT_HOT = UltimateFurnaceMod.id("container/ultimate_furnace/lit_hot");
	private static final ResourceLocation ULTIMATE_BAR = UltimateFurnaceMod.id("container/ultimate_furnace/ultimate_bar");
	private static final ResourceLocation TEXTURE = UltimateFurnaceMod.id("textures/gui/container/ultimate_furnace.png");
	*///?} else {
	/*private static final ResourceLocation LIT_HOT = UltimateFurnaceMod.id("textures/gui/sprites/container/ultimate_furnace/lit_hot.png");
	private static final ResourceLocation ULTIMATE_BAR = UltimateFurnaceMod.id("textures/gui/sprites/container/ultimate_furnace/ultimate_bar.png");
	private static final ResourceLocation TEXTURE = UltimateFurnaceMod.id("textures/gui/container/ultimate_furnace.png");
	*///?}
	//? if >=1.21.2 {
	private static final Component TOGGLE_SMELTABLE_TEXT = Component.translatable("gui.recipebook.toggleRecipes.smeltable");
	private static final List<RecipeBookComponent.TabInfo> TABS = List.of(
		new RecipeBookComponent.TabInfo(SearchRecipeBookCategory.FURNACE),
		new RecipeBookComponent.TabInfo(Items.PORKCHOP, RecipeBookCategories.FURNACE_FOOD),
		new RecipeBookComponent.TabInfo(Items.STONE, RecipeBookCategories.FURNACE_BLOCKS),
		new RecipeBookComponent.TabInfo(Items.LAVA_BUCKET, Items.EMERALD, RecipeBookCategories.FURNACE_MISC)
	);
	//?}

	public UltimateFurnaceScreen(UltimateFurnaceScreenHandler handler, Inventory inventory, Component title) {
		//? if >=1.21.2 {
		super(handler, inventory, title, TOGGLE_SMELTABLE_TEXT, TEXTURE, LIT_PROGRESS_TEXTURE, BURN_PROGRESS_TEXTURE, TABS);
		//?} else if >=1.20.2 {
		/*super(handler, new UltimateFurnaceRecipeBookScreen(), inventory, title, TEXTURE, LIT_PROGRESS_TEXTURE, BURN_PROGRESS_TEXTURE);
		*///?} else {
		/*super(handler, new UltimateFurnaceRecipeBookScreen(), inventory, title, TEXTURE);
		*///?}
	}

	private List<FormattedCharSequence> buildPowerTooltip() {
		List<FormattedCharSequence> lines = new ArrayList<>();
		int level = this.menu.getLevel();
		int rawStored = this.menu.getRawStoredPower();
		int storedPercent = this.menu.getStoredPower();
		int maxPower = UltimateFurnaceBlockEntity.getMaxStoredPower(level);
		int sunPercent = this.menu.getSunlightPercent();

		if (maxPower > 0) {
			lines.add(Component.translatable("tooltip.ultimate_furnace.gui.power", rawStored, maxPower, storedPercent).withStyle(ChatFormatting.GOLD).getVisualOrderText());
		} else {
			lines.add(Component.translatable("tooltip.ultimate_furnace.gui.no_storage").withStyle(ChatFormatting.GRAY).getVisualOrderText());
		}

		lines.add(Component.translatable("tooltip.ultimate_furnace.gui.sunlight", sunPercent)
			.withStyle(sunPercent > 0 ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY).getVisualOrderText());

		Component status;
		if (sunPercent > 0) {
			if (maxPower > 0 && rawStored < maxPower) {
				status = Component.translatable("tooltip.ultimate_furnace.gui.status.charging").withStyle(ChatFormatting.GREEN);
			} else if (sunPercent < 100 && rawStored == 0) {
				status = Component.translatable("tooltip.ultimate_furnace.gui.status.slow").withStyle(ChatFormatting.GOLD);
			} else {
				status = Component.translatable("tooltip.ultimate_furnace.gui.status.solar").withStyle(ChatFormatting.GREEN);
			}
		} else if (rawStored > 0) {
			status = Component.translatable("tooltip.ultimate_furnace.gui.status.battery").withStyle(ChatFormatting.GOLD);
		} else {
			status = Component.translatable("tooltip.ultimate_furnace.gui.status.blocked").withStyle(ChatFormatting.RED);
		}
		lines.add(status.getVisualOrderText());
		return lines;
	}

	private List<FormattedCharSequence> buildLevelTooltip() {
		int smeltCount = this.menu.getSmeltCount();
		int currentLevel = this.menu.getLevel();
		int maxLevel = Math.max(1, FurnaceConfig.getMaxLevel());
		List<FormattedCharSequence> lines = new ArrayList<>();
		if (currentLevel < maxLevel) {
			int maxSmeltCount = this.menu.getMaxSmeltCountForLevel();
			lines.add(Component.translatable("tooltip.ultimate_furnace.gui.level", currentLevel, maxLevel)
				.withStyle(ChatFormatting.AQUA).getVisualOrderText());
			lines.add(Component.translatable("tooltip.ultimate_furnace.gui.next_level", smeltCount, maxSmeltCount)
				.withStyle(ChatFormatting.GRAY).getVisualOrderText());
		} else {
			lines.add(Component.translatable("tooltip.ultimate_furnace.gui.level_max", maxLevel, maxLevel)
				.withStyle(ChatFormatting.GOLD).getVisualOrderText());
			lines.add(Component.translatable("tooltip.ultimate_furnace.gui.total_smelted", smeltCount)
				.withStyle(ChatFormatting.GRAY).getVisualOrderText());
		}
		return lines;
	}

	//? if >=26.1 {
	@Override
	public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		int i = this.leftPos;
		int j = this.topPos;
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

		// Draw the burn item fire icon
		if (this.menu.isLit()) {
			context.blitSprite(RenderPipelines.GUI_TEXTURED, LIT_PROGRESS_TEXTURE, 14, 14, 0, 0, i + 56, j + 36, 14, 14);
		}

		// Draw the smelting progress arrow
		int progressPercentage = this.menu.getCookingProgress();
		int l = Mth.ceil(progressPercentage * 24.0F / 100.0F);
		context.blitSprite(RenderPipelines.GUI_TEXTURED, BURN_PROGRESS_TEXTURE, 24, 16, 0, 0, i + 79, j + 34, l, 16);

		// Draw the ultimate bar
		int smeltCountProgress = this.menu.getSmeltCountProgress();
		if (this.menu.getLevel() >= FurnaceConfig.getMaxLevel()) {
			smeltCountProgress = 100;
		}
		if (smeltCountProgress > 0) {
			int scaledSmeltCountWidth = smeltCountProgress * 162 / 100;
			context.blitSprite(RenderPipelines.GUI_TEXTURED, ULTIMATE_BAR, 162, 5, 0, 0, this.leftPos + 7, this.topPos + this.imageHeight - 101, scaledSmeltCountWidth, 5);
		}

		// Draw the stored power bar
		int storedPowerPercentage = this.menu.getStoredPower();
		int scaledPowerWidth = Mth.ceil(storedPowerPercentage * 18.0F / 100.0F);
		context.blitSprite(RenderPipelines.GUI_TEXTURED, LIT_HOT, 18, 5, 0, 0, this.leftPos + 55, this.topPos + 52, scaledPowerWidth, 5);
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY) {
		super.extractTooltip(context, mouseX, mouseY);

		if (mouseX >= this.leftPos + 55 && mouseX <= this.leftPos + 73 && mouseY >= this.topPos + 52 && mouseY <= this.topPos + 57) {
			context.setTooltipForNextFrame(this.font, buildPowerTooltip(), mouseX, mouseY);
		}

		if (mouseX >= this.leftPos + 7 && mouseX <= this.leftPos + 7 + 162 && mouseY >= this.topPos + 65 && mouseY <= this.topPos + 70) {
			List<FormattedCharSequence> lines = buildLevelTooltip();
			if (!lines.isEmpty()) {
				context.setTooltipForNextFrame(this.font, lines, mouseX, mouseY);
			}
		}
	}
	//?} else if >=1.20 {
	/*@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		//? if >=1.20.2 {
		this.renderBackground(context, mouseX, mouseY, delta);
		//?} else {
		/^this.renderBackground(context);
		^///?}
		super.render(context, mouseX, mouseY, delta);
		this.renderTooltip(context, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
		int i = this.leftPos;
		int j = this.topPos;
		//? if >=1.21.6 {
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

		if (this.menu.isLit()) {
			context.blitSprite(RenderPipelines.GUI_TEXTURED, LIT_PROGRESS_TEXTURE, 14, 14, 0, 0, i + 56, j + 36, 14, 14);
		}

		int progressPercentage = this.menu.getCookingProgress();
		int l = Mth.ceil(progressPercentage * 24.0F / 100.0F);
		context.blitSprite(RenderPipelines.GUI_TEXTURED, BURN_PROGRESS_TEXTURE, 24, 16, 0, 0, i + 79, j + 34, l, 16);

		int smeltCountProgress = this.menu.getSmeltCountProgress();
		if (this.menu.getLevel() >= FurnaceConfig.getMaxLevel()) {
			smeltCountProgress = 100;
		}
		if (smeltCountProgress > 0) {
			int scaledSmeltCountWidth = smeltCountProgress * 162 / 100;
			context.blitSprite(RenderPipelines.GUI_TEXTURED, ULTIMATE_BAR, 162, 5, 0, 0, this.leftPos + 7, this.topPos + this.imageHeight - 101, scaledSmeltCountWidth, 5);
		}

		int storedPowerPercentage = this.menu.getStoredPower();
		int scaledPowerWidth = Mth.ceil(storedPowerPercentage * 18.0F / 100.0F);
		context.blitSprite(RenderPipelines.GUI_TEXTURED, LIT_HOT, 18, 5, 0, 0, this.leftPos + 55, this.topPos + 52, scaledPowerWidth, 5);
		//?} else if >=1.21.2 {
		/^context.blit(RenderType::guiTextured, TEXTURE, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

		if (this.menu.isLit()) {
			context.blitSprite(RenderType::guiTextured, LIT_PROGRESS_TEXTURE, 14, 14, 0, 0, i + 56, j + 36, 14, 14);
		}

		int progressPercentage = this.menu.getCookingProgress();
		int l = Mth.ceil(progressPercentage * 24.0F / 100.0F);
		context.blitSprite(RenderType::guiTextured, BURN_PROGRESS_TEXTURE, 24, 16, 0, 0, i + 79, j + 34, l, 16);

		int smeltCountProgress = this.menu.getSmeltCountProgress();
		if (this.menu.getLevel() >= FurnaceConfig.getMaxLevel()) {
			smeltCountProgress = 100;
		}
		if (smeltCountProgress > 0) {
			int scaledSmeltCountWidth = smeltCountProgress * 162 / 100;
			context.blitSprite(RenderType::guiTextured, ULTIMATE_BAR, 162, 5, 0, 0, this.leftPos + 7, this.topPos + this.imageHeight - 101, scaledSmeltCountWidth, 5);
		}

		int storedPowerPercentage = this.menu.getStoredPower();
		int scaledPowerWidth = Mth.ceil(storedPowerPercentage * 18.0F / 100.0F);
		context.blitSprite(RenderType::guiTextured, LIT_HOT, 18, 5, 0, 0, this.leftPos + 55, this.topPos + 52, scaledPowerWidth, 5);
		^///?} else if >=1.20.2 {
		/^context.blit(TEXTURE, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

		if (this.menu.isLit()) {
			context.blitSprite(LIT_PROGRESS_TEXTURE, 14, 14, 0, 0, i + 56, j + 36, 14, 14);
		}

		int progressPercentage = this.menu.getCookingProgress();
		int l = Mth.ceil(progressPercentage * 24.0F / 100.0F);
		context.blitSprite(BURN_PROGRESS_TEXTURE, 24, 16, 0, 0, i + 79, j + 34, l, 16);

		int smeltCountProgress = this.menu.getSmeltCountProgress();
		if (this.menu.getLevel() >= FurnaceConfig.getMaxLevel()) {
			smeltCountProgress = 100;
		}
		if (smeltCountProgress > 0) {
			int scaledSmeltCountWidth = smeltCountProgress * 162 / 100;
			context.blitSprite(ULTIMATE_BAR, 162, 5, 0, 0, this.leftPos + 7, this.topPos + this.imageHeight - 101, scaledSmeltCountWidth, 5);
		}

		int storedPowerPercentage = this.menu.getStoredPower();
		int scaledPowerWidth = Mth.ceil(storedPowerPercentage * 18.0F / 100.0F);
		context.blitSprite(LIT_HOT, 18, 5, 0, 0, this.leftPos + 55, this.topPos + 52, scaledPowerWidth, 5);
		^///?} else {
		/^context.blit(TEXTURE, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

		if (this.menu.isLit()) {
			context.blit(TEXTURE, i + 56, j + 36, 176, 0, 14, 14);
		}

		int progressPercentage = this.menu.getCookingProgress();
		int l = Mth.ceil(progressPercentage * 24.0F / 100.0F);
		context.blit(TEXTURE, i + 79, j + 34, 176, 14, l, 16);

		int smeltCountProgress = this.menu.getSmeltCountProgress();
		if (this.menu.getLevel() >= FurnaceConfig.getMaxLevel()) {
			smeltCountProgress = 100;
		}
		if (smeltCountProgress > 0) {
			int scaledSmeltCountWidth = smeltCountProgress * 162 / 100;
			context.blit(ULTIMATE_BAR, this.leftPos + 7, this.topPos + this.imageHeight - 101, 0.0F, 0.0F, scaledSmeltCountWidth, 5, 162, 5);
		}

		int storedPowerPercentage = this.menu.getStoredPower();
		int scaledPowerWidth = Mth.ceil(storedPowerPercentage * 18.0F / 100.0F);
		context.blit(LIT_HOT, this.leftPos + 55, this.topPos + 52, 0.0F, 0.0F, scaledPowerWidth, 5, 18, 5);
		^///?}
	}

	@Override
	protected void renderTooltip(GuiGraphics context, int mouseX, int mouseY) {
		super.renderTooltip(context, mouseX, mouseY);

		if (mouseX >= this.leftPos + 55 && mouseX <= this.leftPos + 73 && mouseY >= this.topPos + 52 && mouseY <= this.topPos + 57) {
			//? if >=1.21.6 {
			context.setTooltipForNextFrame(this.font, buildPowerTooltip(), mouseX, mouseY);
			//?} else {
			/^context.renderTooltip(this.font, buildPowerTooltip(), mouseX, mouseY);
			^///?}
		}

		if (mouseX >= this.leftPos + 7 && mouseX <= this.leftPos + 7 + 162 && mouseY >= this.topPos + 65 && mouseY <= this.topPos + 70) {
			List<FormattedCharSequence> lines = buildLevelTooltip();
			if (!lines.isEmpty()) {
				//? if >=1.21.6 {
				context.setTooltipForNextFrame(this.font, lines, mouseX, mouseY);
				//?} else {
				/^context.renderTooltip(this.font, lines, mouseX, mouseY);
				^///?}
			}
		}
	}
	*///?} else {
	/*@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		this.renderTooltip(matrices, mouseX, mouseY);
	}

	@Override
	protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);

		int i = this.leftPos;
		int j = this.topPos;
		this.blit(matrices, i, j, 0, 0, this.imageWidth, this.imageHeight);

		if (this.menu.isLit()) {
			this.blit(matrices, i + 56, j + 36, 176, 0, 14, 14);
		}

		int progressPercentage = this.menu.getCookingProgress();
		int l = Mth.ceil(progressPercentage * 24.0F / 100.0F);
		this.blit(matrices, i + 79, j + 34, 176, 14, l, 16);

		int smeltCountProgress = this.menu.getSmeltCountProgress();
		if (this.menu.getLevel() >= FurnaceConfig.getMaxLevel()) {
			smeltCountProgress = 100;
		}
		if (smeltCountProgress > 0) {
			int scaledSmeltCountWidth = smeltCountProgress * 162 / 100;
			RenderSystem.setShaderTexture(0, ULTIMATE_BAR);
			GuiComponent.blit(matrices, this.leftPos + 7, this.topPos + this.imageHeight - 101, 0.0F, 0.0F, scaledSmeltCountWidth, 5, 162, 5);
		}

		int storedPowerPercentage = this.menu.getStoredPower();
		int scaledPowerWidth = Mth.ceil(storedPowerPercentage * 18.0F / 100.0F);
		RenderSystem.setShaderTexture(0, LIT_HOT);
		GuiComponent.blit(matrices, this.leftPos + 55, this.topPos + 52, 0.0F, 0.0F, scaledPowerWidth, 5, 18, 5);
	}

	@Override
	protected void renderTooltip(PoseStack matrices, int mouseX, int mouseY) {
		super.renderTooltip(matrices, mouseX, mouseY);

		if (mouseX >= this.leftPos + 55 && mouseX <= this.leftPos + 73 && mouseY >= this.topPos + 52 && mouseY <= this.topPos + 57) {
			this.renderTooltip(matrices, buildPowerTooltip(), mouseX, mouseY);
		}

		if (mouseX >= this.leftPos + 7 && mouseX <= this.leftPos + 7 + 162 && mouseY >= this.topPos + 65 && mouseY <= this.topPos + 70) {
			List<FormattedCharSequence> lines = buildLevelTooltip();
			if (!lines.isEmpty()) {
				this.renderTooltip(matrices, lines, mouseX, mouseY);
			}
		}
	}
	*///?}
}
