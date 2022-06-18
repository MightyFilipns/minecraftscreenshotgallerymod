package com.mightyfilipns.screenshotgallery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

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
	public Dropbox(int pX, int pY, int pWidth, int pHeight,Class<T>  Enum,Enum<T> defaultvalue,List<Widget> buttons) throws Throwable  
	{
		super(pX, pY, pWidth, pHeight, new StringTextComponent("dropbox"));
		if(defaultvalue.getClass().getTypeName() != Enum.getTypeName())
		{
			throw new Exception("defaultvalue's type is "+ defaultvalue.getClass().getTypeName()+ " ,instead of "+Enum.getTypeName());
		}
		current = defaultvalue;
		objl= Arrays.asList(Enum.getEnumConstants());
		objl.forEach((a)->{values.add(a.toString());});
		btn1 = new Button(pX,pY, pWidth, pHeight,new StringTextComponent(current.name()), (a)-> setupbuttons());
		buttons.add(btn1);
		btns = buttons;
	}
	public Enum<T> getvalue()
	{
		return (T)current;
	}
	@Override
	public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
		// TODO Auto-generated method stub
		//super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
	}
	public void setupbuttons()
	{
		System.out.println("cccc");
		tempbtns.removeAll(tempbtns);
		int i2 = 1;
		for (int i = 0; i < values.size(); i++) 
		{
			if(values.get(i)==current.name())
			{
				System.out.println("aa");
				continue;
			}
			System.out.println("bb");
			Button btn= null;
			btn = new Button(x, y+i2*height, width, height, new StringTextComponent(values.get(i)), (b)->{
				for (Object object : objl) 
				{
					if(object.toString() == current.name())
					{
						current = (java.lang.Enum) object;
						break;
					}
				}
				btns.removeAll(tempbtns);
			});
			btns.add(btn);
			tempbtns.add(btn);
			i2++;
		}
		System.out.println(values.size());
	}
}
