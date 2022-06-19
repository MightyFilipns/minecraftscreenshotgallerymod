package com.mightyfilipns.screenshotgallery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mightyfilipns.screenshotgallery.GalleryGUI.sortdir;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.java.games.input.Mouse;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.util.text.StringTextComponent;

public class Dropbox<T extends Enum<T>> extends Widget 
{
	private Enum current = null;
	List<String> values = new ArrayList<String>();
	List<Button> tempbtns = new ArrayList<Button>();
	Button btn1 = null;
	List<Widget> btns = null;
	List<Object> objl = null;
	BiConsumer<Widget,Enum<T>> onchange = null;
	boolean isopen = false;
	public Dropbox(int pX, int pY, int pWidth, int pHeight,Enum<T> defaultvalue,List<Widget> buttons,BiConsumer<Widget,Enum<T>> _onchange) 
	{
		super(pX, pY, pWidth, pHeight, new StringTextComponent(""));
		current = defaultvalue;
		objl= Arrays.asList(defaultvalue.getDeclaringClass().getEnumConstants());
		objl.forEach((a)->{values.add(a.toString());});
		btn1 = new Button(pX,pY, pWidth, pHeight,new StringTextComponent(current.name()), (a)-> setupbuttons());
		buttons.add(btn1);
		btns = buttons;
		onchange = _onchange;
		visible = true;
	}
	public Enum<T> getvalue()
	{
		return (T)current;
	}
	// mouseClicked and onClick are required for some reason
	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) 
	{
		for (int i = 0; i < tempbtns.size(); i++) 
		{
			if(GalleryGUI.iswithin((int) pMouseX, x, x+width) && GalleryGUI.iswithin((int) pMouseY, y+((i+1)*height) , y+((i+2)*height)))
			{
				tempbtns.get(i).onPress();
				onchange.accept(this,(Enum<T>)current);
				isopen = false;
				//StaticFunctions.playDownSound();
				return super.mouseClicked(pMouseX, pMouseY, pButton);
			}
		}
		btns.removeAll(tempbtns);
		tempbtns.removeAll(tempbtns);
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}
	@Override
	public void onClick(double pMouseX, double pMouseY) 
	{
		btn1.onPress();
		super.onClick(pMouseX, pMouseY);
	}
	public void setvisiblity(boolean newstate)
	{
		btn1.visible = newstate;
		this.visible = newstate;
	}
	public void setupbuttons()
	{
		if(isopen)
		{
			btns.removeAll(tempbtns);
			tempbtns.removeAll(tempbtns);
			isopen= false;
			return;
		}
		int i2 = 1;
		for (int i = 0; i < values.size(); i++) 
		{
			if(values.get(i)==current.name())
			{
				continue;
			}
			Button btn= null;
			btn = new Button(x, y+i2*height, width, height, new StringTextComponent(values.get(i)), (b)->{
				for (Object object : objl) 
				{
					if(object.toString().equals(b.getMessage().getString()))
					{
						current = (java.lang.Enum<T>) object;
						btn1.setMessage(new StringTextComponent(current.name()));
						break;
					}
				}
				btns.removeAll(tempbtns);
				tempbtns.removeAll(tempbtns);
			});
			btns.add(btn);
			tempbtns.add(btn);
			i2++;
		}
		isopen = true;
	}
	public boolean getIsopen() 
	{
		return isopen;
	}
	public boolean setto(Enum<T> toset)
	{
		if(isopen)
		{
			return false;
		}
		current = toset;
		return true;
	}
}