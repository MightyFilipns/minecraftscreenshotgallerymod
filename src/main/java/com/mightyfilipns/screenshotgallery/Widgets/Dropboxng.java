package com.mightyfilipns.screenshotgallery.Widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
@OnlyIn(Dist.CLIENT)
public class Dropboxng extends AbstractWidget
{
	private int currentint = -1;
	List<String> values = new ArrayList<>();
	List<Button> tempbtns = new ArrayList<>();
	Button btn1 = null;
	//List<AbstractWidget> btns = null;
	List<Object> objl = null;
	List<GuiEventListener> children = null;
	BiConsumer<AbstractWidget,Integer> onchangei = null;
	Consumer<Dropboxng> onopen = null;
	boolean isopen = false;
	int scroll = 0;
	Function<Dropboxng, Boolean> prereq = null;
	Minecraft INSTANCE = Minecraft.getInstance();
	Screen scr = null;
	boolean remove = false;
	public Dropboxng(int pX, int pY, int pWidth, int pHeight,int defaultvalue,List<String> _values,List<GuiEventListener> _children, BiConsumer<AbstractWidget,Integer> _onchange,Screen _scr)
	{
		super(pX, pY, pWidth, pHeight, Component.empty());
		values = _values;
		children = _children;
		//btns = buttons;
		//btn1 = new Button(pX,pY, pWidth, pHeight,Component.literal(values.get(defaultvalue)), (a)-> setupbuttons());
		btn1 = Button.builder(Component.literal(values.get(defaultvalue)), (a)-> setupbuttons()).bounds(pX, pY, pWidth, pHeight).build();
		//buttons.add(btn1);
		scr = _scr;
		children.add(btn1);
		scr.renderables.add(btn1);
		onchangei = _onchange;
		currentint = defaultvalue;
		visible = true;
		if(values.size() == 1)
		{
			btn1.active = false;
		}
	}
	public int getvalue()
	{
		return currentint;
	}
	public List<String> getvalues()
	{
		return values;
	}
	public void close()
	{
		//btns.removeAll(tempbtns);
		children.removeAll(tempbtns);
		scr.renderables.removeAll(tempbtns);
		tempbtns.removeAll(tempbtns);
		isopen= false;
	}
	@Override
	public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
		
		if(remove)
		{
			children.removeAll(tempbtns);
			//btns.removeAll(tempbtns);
			scr.renderables.removeAll(tempbtns);
			tempbtns.clear();
			isopen = false;
			remove = false;
		}
		super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
	}
	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
	{
		if(pMouseY< getY()|| getY()+(height*values.size())< pMouseY && pMouseX< getX() || getX()+width < pMouseX)
		{
			remove = true;
		}
		//System.out.println(values.size() + "dpng");
		//children.removeAll(tempbtns);
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}
	@Override
	public void onClick(double pMouseX, double pMouseY)
	{
		//System.out.println(values.size() + "dpng");
		super.onClick(pMouseX, pMouseY);
	}
	public void setvisiblity(boolean newstate)
	{
		btn1.visible = newstate;
		this.visible = newstate;
	}
	public void setopenprereq(Function<Dropboxng, Boolean> a)
	{
		prereq = a;
	}
	public void setonopen(Consumer<Dropboxng> _onopen)
	{
		onopen = _onopen;
	}
	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta)
	{
		if(!isopen || (INSTANCE.screen == null))
		{
			return false;
		}
		int btnh = (values.size()-1)*20;
		int h1 = INSTANCE.screen.height -(getY()+20);
		int maxscroll = Math.max(btnh-h1,0);
		scroll -= pDelta*10;
		scroll = Math.max(Math.min(scroll, maxscroll), 0);
		//int oldscroll = scroll;
		//int deltatoadd = oldscroll-scroll;
		for (int i = 0; i < tempbtns.size(); i++)
		{
			tempbtns.get(i).setY((getY()+((i+1)*20)) -scroll);
		}
		return super.mouseScrolled(pMouseX, pMouseY, pDelta);
	}
	public boolean setstring(String input)
	{
		int ind = values.indexOf(input);
		if(ind == -1)
		{
			return false;
		}
		else
		{
			currentint = ind;
			btn1.setMessage(Component.literal(values.get(ind)));
			return true;
		}
	}
	String getcurrentvalue()
	{
		return values.get(currentint);
	}
	public void setupbuttons()
	{
		if(values.size() == 1)
		{
			return;
		}
		//btns.removeAll(tempbtns);
		children.removeAll(tempbtns);
		scr.renderables.removeAll(tempbtns);
		tempbtns.removeAll(tempbtns);
		//System.out.println(values.size() + "dpng");
		if(isopen)
		{
			//btns.removeAll(tempbtns);
			children.removeAll(tempbtns);
			scr.renderables.removeAll(tempbtns);
			tempbtns.removeAll(tempbtns);
			isopen= false;
			return;
		}
		if(prereq != null && !prereq.apply(this))
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
			/*btn = new Button(getX(), getY()+i2*height, width, height,Component.literal(values.get(i)), (b)->{
				//System.out.println(currentint+ ":"+ b.getMessage().getString());
				if(!isopen)
				{
					return;
				}
				currentint = values.indexOf(b.getMessage().getString());
				//System.out.println(currentint);
				//values.forEach((a)-> System.out.println(a));
				btn1.setMessage(new StringTextComponent(getcurrentvalue()));
				btns.removeAll(tempbtns);
				children.removeAll(tempbtns);
				tempbtns.removeAll(tempbtns);
				onchangei.accept(this, currentint);
				isopen = false;
			});*/
			btn = Button.builder(Component.literal(values.get(i)),(b)->{
				//System.out.println(currentint+ ":"+ b.getMessage().getString());
				if(!isopen)
				{
					return;
				}
				currentint = values.indexOf(b.getMessage().getString());
				//System.out.println(currentint);
				//values.forEach((a)-> System.out.println(a));
				btn1.setMessage(Component.literal(getcurrentvalue()));
				//btns.removeAll(tempbtns);
				children.removeAll(tempbtns);
				scr.renderables.removeAll(tempbtns);
				tempbtns.removeAll(tempbtns);
				onchangei.accept(this, currentint);
				isopen = false;
			}).bounds(getX(), getY()+i2*height, width, height).build();
			//btns.add(btn);
			children.add(btn);
			scr.renderables.add(btn);
			tempbtns.add(btn);
			i2++;
		}
		if(onopen != null)
		{
			onopen.accept(this);
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
	@Override
	protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
		// TODO Auto-generated method stub
		
	}
}
