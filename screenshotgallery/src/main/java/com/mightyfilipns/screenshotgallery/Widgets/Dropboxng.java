package com.mightyfilipns.screenshotgallery.Widgets;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;


import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mightyfilipns.screenshotgallery.StaticFunctions;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

public class Dropboxng extends Widget 
{
	private int currentint = -1;
	List<String> values = new ArrayList<String>();
	List<Button> tempbtns = new ArrayList<Button>();
	Button btn1 = null;
	List<Widget> btns = null;
	List<Object> objl = null;
	List<IGuiEventListener> children = null;
	BiConsumer<Widget,Integer> onchangei = null;
	boolean isopen = false;
	int scroll = 0;
	Supplier<Boolean> prereq = null;
	
	public Dropboxng(int pX, int pY, int pWidth, int pHeight,int defaultvalue,List<String> _values,List<Widget> buttons,List<IGuiEventListener> _children, BiConsumer<Widget,Integer> _onchange) 
	{
		super(pX, pY, pWidth, pHeight, new StringTextComponent(""));
		values = _values;
		children = _children;
		btns = buttons;
		btn1 = new Button(pX,pY, pWidth, pHeight,new StringTextComponent(values.get(defaultvalue)), (a)-> setupbuttons());
		buttons.add(btn1);
		children.add(btn1);
		onchangei = _onchange;
		currentint = defaultvalue;
		visible = true;
	}
	public int getvalue()
	{
		return currentint;
	}
	public List<String> getvalues()
	{
		return values;
	}
	// mouseClicked and onClick are required for some reason
	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) 
	{
		System.out.println(values.size() + "dpng");
		isopen = false;
		btns.removeAll(tempbtns);
		tempbtns.removeAll(tempbtns);
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}
	@Override
	public void onClick(double pMouseX, double pMouseY) 
	{
		System.out.println(values.size() + "dpng");
		//btn1.onPress();
		super.onClick(pMouseX, pMouseY);
	}
	public void setvisiblity(boolean newstate)
	{
		btn1.visible = newstate;
		this.visible = newstate;
	}
	public void setopenprereq(Supplier<Boolean> a)
	{
		prereq = a;
	}
	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) 
	{
		System.out.println("scroll");
		int btnh = (values.size()-1)*20;
		int h1 = height-(y+20);
		int maxscroll = Math.max(btnh-h1,0);
		scroll += pDelta*10;
		scroll = Math.max(Math.min(scroll, maxscroll), 0);
		for (int i = 0; i < tempbtns.size(); i++) 
		{
			tempbtns.get(i).y = tempbtns.get(i).y + scroll; 
		}
		return super.mouseScrolled(pMouseX, pMouseY, pDelta);
	}
	String getcurrentvalue()
	{
		return values.get(currentint);
	}
	public void setupbuttons()
	{
		System.out.println(values.size() + "dpng");
		if(isopen)
		{
			btns.removeAll(tempbtns);
			children.removeAll(tempbtns);
			tempbtns.removeAll(tempbtns);
			isopen= false;
			return;
		}
		if(prereq != null && !prereq.get())
		{
			return;
		}
		int i2 = 1;
		for (int i = 0; i < values.size(); i++) 
		{
			if(currentint == i)
			{
				continue;
			}
			Button btn= null;
			btn = new Button(x, y+i2*height, width, height, new StringTextComponent(values.get(i)), (b)->{
				System.out.println(currentint);
				currentint = values.indexOf(b.getMessage().getString());
				System.out.println(currentint);
				values.forEach((a)-> System.out.println(a));
				btn1.setMessage(new StringTextComponent(getcurrentvalue()));
				btns.removeAll(tempbtns);
				children.removeAll(tempbtns);
				tempbtns.removeAll(tempbtns);
				onchangei.accept(this, currentint);
				isopen = false;
			});
			btns.add(btn);
			children.add(btn);
			tempbtns.add(btn);
			i2++;
		}
		isopen = true;
	}
	public boolean getIsopen() 
	{
		return isopen;
	}
	public boolean setto(int toset)
	{
		if(isopen)
		{
			return false;
		}
		currentint = toset;
		return true;
	}
}
