package com.mightyfilipns.screenshotgallery;

import java.time.LocalDate;
import java.util.List;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.NativeImage.Format;

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
	public static <T> T getlast(List<T> toget)
	{
		if(toget == null || toget.size() == 0)
		{
			return null;
		}
		return toget.get(toget.size()-1);
	}
	public static NativeImage whitesq(int x,int y, int argbcolor)
	{
		NativeImage ni = new NativeImage(Format.RGBA,x,y,false);
		for (int i = 0; i < x; i++)
		{
			for (int j = 0; j < y; j++)
			{
				ni.setPixelRGBA(i, j, argbcolor);
			}
		}
		return ni;
	}
}
