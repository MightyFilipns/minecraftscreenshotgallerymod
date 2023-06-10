package com.mightyfilipns.screenshotgallery.Widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
@SuppressWarnings("unchecked")
@OnlyIn(Dist.CLIENT)
public class Dropbox<T extends Enum<T>> extends AbstractWidget 
{
	private Enum<T> current = null;
	List<String> values = new ArrayList<String>();
	List<Button> tempbtns = new ArrayList<Button>();
	Button btn1 = null;
	List<GuiEventListener> btns = null;
	List<Object> objl = null;
	BiConsumer<AbstractWidget,Enum<T>> onchangee = null;
	BiConsumer<AbstractWidget,Integer> onchangei = null;
	boolean isopen = false;
	List<String> Values = null;
	boolean useenum = false;
	Screen scr = null;
	
	public Dropbox(int pX, int pY, int pWidth, int pHeight,Enum<T> defaultvalue,List<GuiEventListener> list,BiConsumer<AbstractWidget,Enum<T>> _onchange,Screen _scr) 
	{
		super(pX, pY, pWidth, pHeight, Component.empty());
		current = defaultvalue;
		scr = _scr;
		objl= Arrays.asList((Object[])defaultvalue.getDeclaringClass().getEnumConstants());
		objl.forEach((a)->{values.add(a.toString());});
		//btn1 = new Button(pX,pY, pWidth, pHeight,Component.literal(current.name()), (a)-> setupbuttons());
		btn1 = Button.builder(Component.literal(current.name()), (a)-> setupbuttons()).bounds(pX,pY, pWidth, pHeight).build();
		list.add(btn1);
		scr.renderables.add(btn1);
		btns = list;
		onchangee = _onchange;
		visible = true;
	}
	public Enum<T> getvalue()
	{
		return (Enum<T>)current;
	}
	// mouseClicked and onClick are required for some reason
	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) 
	{/*
		for (int i = 0; i < tempbtns.size(); i++) 
		{
			if(StaticFunctions.iswithin((int) pMouseX, getX(), getX()+width) && StaticFunctions.iswithin((int) pMouseY, getY()+((i+1)*height) , getY()+((i+2)*height)))
			{
				tempbtns.get(i).onPress();
				onchangee.accept(this,(Enum<T>)current);					
				isopen = false;
				return super.mouseClicked(pMouseX, pMouseY, pButton);
			}
		}
		//btns.removeAll(tempbtns);
		//scr.renderables.removeAll(tempbtns);
		tempbtns.removeAll(tempbtns);*/
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}
	@Override
	public void onClick(double pMouseX, double pMouseY) 
	{
		//btn1.onPress();
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
			scr.renderables.removeAll(tempbtns);
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
			btn = Button.builder(Component.literal(values.get(i)), (b)->{
				for (Object object : objl) 
				{
					if(object.toString().equals(b.getMessage().getString()))
					{
						current = (Enum<T>) object;
						btn1.setMessage(Component.literal(current.name()));
						onchangee.accept(this, current);
						break;
					}
				}
				btns.removeAll(tempbtns);
				scr.renderables.removeAll(tempbtns);
				tempbtns.removeAll(tempbtns);
				isopen = false;
			}).bounds(getX(), getY()+i2*height, width, height).build();
			/*btn = new Button(getX(), getY()+i2*height, width, height, Component.literal(values.get(i)), (b)->{
				for (Object object : objl) 
				{
					if(object.toString().equals(b.getMessage().getString()))
					{
						current = (Enum<T>) object;
						btn1.setMessage(new StringTextComponent(current.name()));
						break;
					}
				}
				btns.removeAll(tempbtns);
				tempbtns.removeAll(tempbtns);
			});*/
			btns.add(btn);
			scr.renderables.add(btn);
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
	@Override
	protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
		// TODO Auto-generated method stub
	}
}
