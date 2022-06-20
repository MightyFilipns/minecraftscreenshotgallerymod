package com.mightyfilipns.screenshotgallery.Widgets;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;
public class DatePicker extends Widget 
{
	List<Widget> buttons = null;
	List<IGuiEventListener> children =null;
	LocalDate initaldate = null;
	LocalDate _mindate = null;
	LocalDate _maxdate = null;
	Dropboxng day = null;
	Dropboxng month = null;
	Dropboxng year = null;
	List<String> days = new ArrayList<String>();
	List<String> months = new ArrayList<String>();
	List<String> years = new ArrayList<String>();
	public DatePicker(int pX, int pY, int pWidth, int pHeight,LocalDate mindate,LocalDate maxdate,LocalDate currentdate,List<Widget> btns,List<IGuiEventListener> _children) 
	{
		super(pX, pY, pWidth, pHeight, new StringTextComponent(""));
		_mindate = mindate;
		_maxdate = maxdate;
		initaldate = currentdate;
		buttons = btns;
		children = _children;
		if(_mindate.toEpochDay() >_maxdate.toEpochDay())
		{
			setMessage(new StringTextComponent("Invalid Parameters"));
			return;
		}
		recalcdate();
		System.out.println("days: "+ days.size()+" months: " +months.size()+ "years: " +years.size());
		day = new Dropboxng(pX, pY, pWidth, pHeight, initaldate.getDayOfMonth()-1, days, buttons,children, (a,b)->{
			System.out.println("day changed to " + days.get(b));
			recalcdate();
		});
		month = new Dropboxng(pX+pWidth, pY, pWidth, pHeight, initaldate.getMonthValue()-1, months, buttons,children, (a,b)->{
			System.out.println("month changed to " + days.get(b));
			recalcdate();
		});
		year = new Dropboxng(pX+2*pWidth, pY, pWidth, pHeight, initaldate.minusYears(_mindate.getYear()).getYear(), years, buttons,children, (a,b)->{
			System.out.println("year changed to " + days.get(b));
			recalcdate();
		});
		buttons.add(day);
		children.add(day);
		day.setopenprereq(() -> {return areallclosed();});
		buttons.add(month);
		children.add(month);
		month.setopenprereq(() -> {return areallclosed();});
		buttons.add(year);
		children.add(year);
		year.setopenprereq(() -> {return areallclosed();});
	}
	public void recalcdate()
	{
		days.removeAll(days);
		months.removeAll(months);
		years.removeAll(years);
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
					dayc = _mindate.getDayOfMonth()-_mindate.lengthOfMonth()+1;
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
					days.add((_mindate.lengthOfMonth()+i)+"");
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
				days.add((initaldate.getDayOfMonth()+i)+"");
			}
		}
		else if(initaldate.getYear() == _maxdate.getYear())
		{
			monthsc = 12-_maxdate.getMonthValue();
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
}