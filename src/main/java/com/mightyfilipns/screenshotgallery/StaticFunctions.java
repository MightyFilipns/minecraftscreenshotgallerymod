package com.mightyfilipns.screenshotgallery;

import java.time.LocalDate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;

public class StaticFunctions 
{
	public static void playDownSound() 
	{
		SoundManager pHandler = Minecraft.getInstance().getSoundManager();
		pHandler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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
	public static LocalDate LaterDate(LocalDate d1,LocalDate d2)
	{
		if (d1.isEqual(d2)) 
		{
			return d1;
		}
		else if(d2.isAfter(d1)) 
		{
			return d2;
		}
		else
		{
			return d1;
		}
	}
	public static LocalDate EarlierDate(LocalDate d1,LocalDate d2)
	{
		if (d1.isEqual(d2)) 
		{
			return d1;
		}
		else if(d2.isBefore(d1)) 
		{
			return d2;
		}
		else
		{
			return d1;
		}
	}
}
