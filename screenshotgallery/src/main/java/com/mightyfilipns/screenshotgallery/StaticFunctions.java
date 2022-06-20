package com.mightyfilipns.screenshotgallery;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.SoundEvents;

public class StaticFunctions 
{
	public static void playDownSound() 
	{
		SoundHandler pHandler = Minecraft.getInstance().getSoundManager();
		pHandler.play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}
	public static boolean iswithin(int value,int lowerlimit,int upperlimit)
	{
		if(value >= lowerlimit && value <= upperlimit)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
