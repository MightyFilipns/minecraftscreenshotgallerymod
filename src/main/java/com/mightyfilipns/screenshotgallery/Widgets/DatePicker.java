package com.mightyfilipns.screenshotgallery.Widgets;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.mightyfilipns.screenshotgallery.StaticFunctions;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
@OnlyIn(Dist.CLIENT)
public class DatePicker extends AbstractWidget
{
	//List<Widget> buttons = null;
	List<GuiEventListener> children =null;
	LocalDate initaldate = null;
	LocalDate _mindate = null;
	LocalDate _maxdate = null;
	Dropboxng day = null;
	Dropboxng month = null;
	Dropboxng year = null;
	List<String> days = new ArrayList<>();
	List<String> months = new ArrayList<>();
	List<String> years = new ArrayList<>();
	List<Dropboxng> dngs = new ArrayList<>();
	Consumer<DatePicker> onchange = null;
	public DatePicker(int pX, int pY, int pWidth, int pHeight,LocalDate mindate,LocalDate maxdate,LocalDate currentdate,/*List<Widget> btns,*/List<GuiEventListener> lst,Screen scr)
	{
		super(pX, pY, pWidth, pHeight,Component.literal("") );
		
		_mindate = mindate;
		_maxdate = maxdate;
		initaldate = currentdate;
		//buttons = btns;
		children = lst;
		if(_mindate.toEpochDay() >_maxdate.toEpochDay())
		{
			setMessage(Component.literal("Invalid Parameters"));
			return;
		}
		if(maxdate == mindate)
		{
			return;
		}
		//System.out.println(mindate);
		recalcdate();
		//System.out.println("days: "+ days.size()+" months: " +months.size()+ "years: " +years.size());
		day = new Dropboxng(pX, pY, pWidth/3, pHeight, initaldate.getDayOfMonth()-Integer.parseInt(days.get(0)), days,children, (a,b)->{
			//System.out.println("day changed to " + days.get(b));
			initaldate = LocalDate.of(initaldate.getYear(),initaldate.getMonth(), Integer.parseInt(days.get(b)));
			onchange();
		},scr);
		month = new Dropboxng(pX+(pWidth/3), pY, pWidth/3, pHeight, initaldate.getMonthValue()-Integer.parseInt(months.get(0)), months,children, (a,b)->{
			//System.out.println("month changed to " + months.get(b));
			initaldate = LocalDate.of(initaldate.getYear(),Integer.parseInt(months.get(b)), initaldate.getDayOfMonth());
			onchange();
		},scr);
		year = new Dropboxng(pX+((pWidth/3)*2), pY, pWidth/3, pHeight, initaldate.minusYears(_mindate.getYear()).getYear(), years,children, (a,b)->{
			//System.out.println("year changed to " + years.get(b));
			initaldate = LocalDate.of(Integer.parseInt(years.get(b)),initaldate.getMonth(), initaldate.getDayOfMonth());
			onchange();
		},scr);
		dngs.add(day);
		dngs.add(month);
		dngs.add(year);
		for (Dropboxng widget : dngs)
		{
			widget.prereq = (a)->{
				return areallclosed();
			};
			//buttons.add(widget);
			//children.add((AbstractWidget)null);
			//lst.add(widget);
		}
		//visible = false;
	}
	public void setOnchange(Consumer<DatePicker> onchange)
	{
		this.onchange = onchange;
	}
	private void onchange()
	{
		checkdates();
		recalcdate();
		updatedates();
		if(onchange != null)
		{
			onchange.accept(this);
		}
	}
	public boolean isopen()
	{
		for (Dropboxng widget : dngs)
		{
			if(widget.getIsopen())
			{
				return true;
			}
		}
		return false;
	}
	public void closeall(Dropboxng exept)
	{
		for (Dropboxng widget : dngs)
		{
			if(widget != exept)
			{
				widget.close();
			}
		}
	}
	public LocalDate getcurrentdate()
	{
		return initaldate;
	}
	public void setvisiblity(boolean newstate)
	{
		dngs.forEach((a)->{
			a.setvisiblity(newstate);
		});
	}
	public void updatedates()
	{
		day.setstring(initaldate.getDayOfMonth()+"");
		month.setstring(initaldate.getMonthValue()+"");
		year.setstring(initaldate.getYear()+"");
	}
	public void recalcdate()
	{
		days.removeAll(days);
		months.removeAll(months);
		years.removeAll(years);
		
		List<LocalDate> ydl = _mindate.datesUntil(_maxdate, Period.ofYears(1)).collect(Collectors.toList());
		
		for (LocalDate ld : ydl) 
		{
			years.add(Integer.toString(ld.getYear()));
		}
		
		LocalDate mmin1 = StaticFunctions.LaterDate(_mindate, LocalDate.of(initaldate.getYear(), 1, 1));
		LocalDate mmin2 = StaticFunctions.EarlierDate(_maxdate, LocalDate.of(initaldate.getYear(), 12, 31));
		List<LocalDate> mdl = null;
		//mmin1.datesUntil(mmin2, Period.ofMonths(1)).collect(Collectors.toList());
		
		if (mmin2.getMonthValue() == mmin1.getMonthValue()) 
		{
			mdl = mmin1.datesUntil(mmin2, Period.ofMonths(1)).collect(Collectors.toList());
		}
		else 
		{
			mdl = mmin1.datesUntil(mmin2.plusMonths(1), Period.ofMonths(1)).collect(Collectors.toList());
		}
		
		for (LocalDate ld : mdl) 
		{
			months.add(Integer.toString(ld.getMonthValue()));
		}
		
		
		LocalDate dmin1 = StaticFunctions.LaterDate(_mindate, LocalDate.of(initaldate.getYear(), initaldate.getMonthValue(), 1));
		LocalDate dmin2 = StaticFunctions.EarlierDate(_maxdate, LocalDate.of(initaldate.getYear(), initaldate.getMonthValue(), initaldate.lengthOfMonth()));
		List<LocalDate> ddl = null;
		//dmin1.datesUntil(mmin2, Period.ofDays(1)).collect(Collectors.toList());
		
		if (dmin2.getDayOfMonth() == dmin1.getDayOfMonth()) 
		{
			ddl = dmin1.datesUntil(dmin2, Period.ofDays(1)).collect(Collectors.toList());
		}
		else 
		{
			ddl = dmin1.datesUntil(dmin2.plusDays(1), Period.ofDays(1)).collect(Collectors.toList());
		}
		
		for (LocalDate ld : ddl) 
		{
			days.add(Integer.toString(ld.getDayOfMonth()));
		}
		
		
		
		
		/*
		int yearsc = _maxdate.minusYears(_mindate.getYear()).getYear()+1;
		for (int i = 0; i < yearsc; i++)
		{
			years.add((_mindate.getYear()+i)+"");
		}
		//month
		int monthsc = 0;
		int dayc;
		if(yearsc == 1)
		{
			monthsc = _maxdate.getMonthValue()-_mindate.getMonthValue()+1;
			for (int i = 0; i < monthsc; i++)
			{
				months.add((_mindate.getMonthValue()+i)+"");
			}
			if(monthsc == 1)
			{
				dayc =  _maxdate.getDayOfMonth()-_mindate.getDayOfMonth()+1;
				for (int i = 0; i < dayc; i++)
				{
					days.add((_mindate.getDayOfMonth()+i)+"");
				}
			}
			else
			{
				if(initaldate.getMonth() == _mindate.getMonth())
				{
					dayc = Math.abs(_mindate.getDayOfMonth()-_mindate.lengthOfMonth()+1);
					for (int i = 0; i < dayc; i++)
					{
						days.add((_mindate.getDayOfMonth()+1)+"");
					}
				}
				else if(initaldate.getMonth() == _maxdate.getMonth())
				{
					dayc = _maxdate.getDayOfMonth();
					for (int i = 0; i < dayc; i++)
					{
						days.add((i+1)+"");
					}
				}
				else
				{
					dayc = initaldate.lengthOfMonth();
					for (int i = 0; i < dayc; i++)
					{
						days.add((i+1)+"");
					}
				}
			}
		}
		else if(initaldate.getYear() == _mindate.getYear())
		{
			monthsc = 12-_mindate.getMonthValue()+1;
			for (int i = 0; i < monthsc; i++)
			{
				months.add((_mindate.getMonthValue()+i)+"");
			}

			if(initaldate.getMonth() == _mindate.getMonth())
			{
				dayc = _mindate.lengthOfMonth()-_mindate.getDayOfMonth()+1;
				for (int i = 0; i < dayc; i++)
				{
					days.add((_mindate.getDayOfMonth()+i)+"");
				}
			}
			else
			{
				dayc = _mindate.lengthOfMonth();
				for (int i = 0; i < dayc; i++)
				{
					days.add((i+1)+"");
				}
			}
			dayc = initaldate.lengthOfMonth()-initaldate.getDayOfMonth()+1;
			for (int i = 0; i < dayc; i++)
			{
				//days.add((initaldate.getDayOfMonth()+i)+"");
			}
		}
		else if(initaldate.getYear() == _maxdate.getYear())
		{
			monthsc = _maxdate.getMonthValue();
			for (int i = 0; i < monthsc; i++)
			{
				months.add((i+1)+"");
			}
			if(initaldate.getMonth() == _maxdate.getMonth())
			{
				dayc = _maxdate.getDayOfMonth();
			}
			else
			{
				dayc = initaldate.lengthOfMonth();
			}
			for (int i = 0; i < dayc; i++)
			{
				days.add((i+1)+"");
			}
		}
		else
		{
			for (int i = 0; i < 12; i++)
			{
				months.add((i+1)+"");
			}
			dayc = initaldate.lengthOfMonth();
			for (int i = 0; i < dayc; i++)
			{
				days.add((i+1)+"");
			}
		}*/
	}
	public void checkdates()
	{
		if(_mindate.toEpochDay()> initaldate.toEpochDay())
		{
			initaldate = _mindate.plusDays(0);
		}
		if(initaldate.toEpochDay() > _maxdate.toEpochDay())
		{
			initaldate = _maxdate.plusDays(0);
		}
	}
	public boolean areallclosed()
	{
		if(day.isopen || month.isopen || year.isopen)
		{
			return false;
		}
		return true;
	}
	@Override
	protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
		// TODO Auto-generated method stub
		
	}
}
