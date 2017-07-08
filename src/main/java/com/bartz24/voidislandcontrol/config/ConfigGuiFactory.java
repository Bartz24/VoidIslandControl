package com.bartz24.voidislandcontrol.config;

import java.util.Set;

import com.bartz24.voidislandcontrol.References;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;

public class ConfigGuiFactory implements IModGuiFactory {

	@Override
	public void initialize(Minecraft minecraftInstance) {
		// NO-OP
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

	public static class ConfigGui extends GuiConfig {

		public ConfigGui(GuiScreen parentScreen) {

			super(parentScreen, ConfigOptions.getConfigElements(), References.ModID, false, false,
					GuiConfig.getAbridgedConfigPath(ConfigOptions.config.toString()));
		}

		@Override
		public void onGuiClosed() {
			super.onGuiClosed();
			ConfigOptions.config.save();
		}
	}

	@Override
	public boolean hasConfigGui() {
		return true;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		return new ConfigGui(parentScreen);
	}

}
