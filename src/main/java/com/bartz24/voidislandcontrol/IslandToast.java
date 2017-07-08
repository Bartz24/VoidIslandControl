package com.bartz24.voidislandcontrol;

import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;

public class IslandToast implements IToast {
	private IslandToast.Type type;
	private String title;
	private String subtitle;
	private IToast.Visibility visibility = IToast.Visibility.SHOW;

	public IslandToast(ITextComponent titleComponent, ITextComponent subtitleComponent) {
		this.title = titleComponent.getUnformattedText();
		this.subtitle = subtitleComponent == null ? null : subtitleComponent.getUnformattedText();
		type = Type.Island;
	}

	public IToast.Visibility draw(GuiToast toastGui, long delta) {
		toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
		GlStateManager.color(1.0F, 1.0F, 1.0F);
		toastGui.drawTexturedModalRect(0, 0, 0, 96, 160, 32);

		if (this.subtitle == null) {
			toastGui.getMinecraft().fontRenderer.drawString(this.title, 30, 12, -11534256);
		} else {
			toastGui.getMinecraft().fontRenderer.drawString(this.title, 30, 7, -11534256);
			toastGui.getMinecraft().fontRenderer.drawString(this.subtitle, 30, 18, -16777216);
		}

		return this.visibility;
	}

	public void hide() {
		this.visibility = IToast.Visibility.HIDE;

	}

	public IslandToast.Type getType() {
		return this.type;
	}

	public static enum Type {
		Island;
	}
}
