package com.mightyfilipns.screenshotgallery;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraftforge.client.event.ScreenshotEvent;

public class CacheManager
{
	public CacheManager() {}
	static File maindir = new File(Minecraft.getInstance().gameDirectory.getAbsolutePath() + "/screenshotgallerycache/");
	static File imgcache = new File(maindir.getAbsolutePath() + "/imgcache/");
	static File datacache = new File(maindir.getAbsolutePath() + "/datacache/");
	static File screenshootdir = new File(Minecraft.getInstance().gameDirectory.getAbsolutePath(), "/screenshots/");
	static FileFilter ff = file -> !file.isDirectory() && file.getName().endsWith(".png");
	static List<File> files = null;
	static List<imgdata> imgd = new ArrayList<>();
	private static final Minecraft INSTANCE = Minecraft.getInstance();
	public static volatile boolean iscaching = false;
	public static AtomicBoolean iscach = new AtomicBoolean();
	private static int getdim(File img,boolean height)
	{
		try(ImageInputStream in = ImageIO.createImageInputStream(img)){
		    final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
		    if (readers.hasNext()) {
		        ImageReader reader = readers.next();
		        try {
		            reader.setInput(in);
		            if(height)
		            {
		            	return reader.getHeight(0);
		            }
		            else
		            {
		            	return reader.getWidth(0);
		            }
		        } finally {
		            reader.dispose();
		        }
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	public static LocalDate getearliestdate()
	{
		if(imgd.size() == 0)
		{
			return LocalDate.now();
		}
		return imgd.get(0).createdtime;
	}
	public static void buildcache(boolean onlyaddnew,boolean initial)
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					iscaching = true;
					iscach.set(true);
					internalbuildcache(onlyaddnew,initial);
					iscaching = false;
					iscach.set(false);
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		},"BuildGalleryCacheThread").start();;

	}
	
	private static void internalbuildcache(boolean onlyaddnew,boolean inital) throws IOException, InterruptedException
	{
		iscaching = true;
		if(!maindir.exists())
		{
			Files.createDirectory(Paths.get(maindir.getAbsolutePath()));
		}
		if(!imgcache.exists())
		{
			Files.createDirectory(Paths.get(imgcache.getAbsolutePath()));
		}
		if(!screenshootdir.exists())
		{
			Files.createDirectory(Paths.get(screenshootdir.getAbsolutePath()));
		}
		files = Arrays.asList(screenshootdir.listFiles(ff));
		int i = 1;
		Thread.sleep(100);
		imgd.clear();
		for (File file : files)
		{
			if(Files.exists(Paths.get(imgcache.getAbsolutePath()+"/"+file.getName())))
			{
				if(INSTANCE.player != null && !inital)
				{
					INSTANCE.player.displayClientMessage(Component.literal("Quick Cached "+files.size()+"/"+i), false);
				}
				BasicFileAttributes bf = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
				imgd.add(new imgdata(file,getdim(file, true),getdim(file, false),LocalDate.from(ZonedDateTime.ofInstant(bf.lastModifiedTime().toInstant(), ZoneId.systemDefault())),file.length()));
				i++;
			}
			else
			{
				
				try {
					cacheimg(file);
					if(INSTANCE.player != null && !inital)
					{
						INSTANCE.player.displayClientMessage(Component.literal("Cached "+files.size()+"/"+i), false);
					}
					i++;
				} catch (Throwable e) {
					if(INSTANCE.player != null && !inital)
					{
						INSTANCE.player.displayClientMessage(Component.literal("Caching failed for "+files.size()+"/"+i + ":" +file.getName()), false);
					}
					i++;
					e.printStackTrace();
				}
			}
		}
		resort();
		if(INSTANCE.player != null && !inital)
		{
			INSTANCE.player.displayClientMessage(Component.literal("Cache Building Finished"), false);
		}
		iscaching = false;
	}
	private static void cacheimg(File file) throws Throwable
	{
		BufferedImage img = null;
		img = ImageIO.read(file);
		int xsize =200;
		int ysize = 112;

		final float ratio = xsize/(float)(ysize);

		int imgh = ysize;
		int imgw = xsize;


		int chosenw = img.getWidth();
		int chosenh = img.getHeight();

		int min = Math.min(imgw, imgh);

		if(chosenw < chosenh)
		{
			imgw = (int) (imgh/ratio);
			if(imgw > xsize)
			{
				imgw = xsize;
				imgh = (int) (imgw*ratio);
			}
		}
		if(chosenw > chosenh)
		{
			imgh = (int) (imgw/ratio);
			if(imgh > ysize)
			{
				imgh = ysize;
				imgw = (int) (imgh*ratio);
			}
		}
		if(chosenw == chosenh)
		{
			imgh = min;
			imgw = min;
		}

		final AffineTransform at = AffineTransform.getScaleInstance(imgw/(double)chosenw, imgh/(double)chosenh);
		final AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
		BufferedImage img2 = null;
		img2 = ato.filter(img, img2);
		File newimg = new File(imgcache.getAbsolutePath()+"/"+file.getName());
		ImageIO.write(img2, "png", newimg);
		BasicFileAttributes bf = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
		imgd.add(new imgdata(file,img.getWidth(),img.getHeight(),LocalDate.from(ZonedDateTime.ofInstant(bf.lastModifiedTime().toInstant(), ZoneId.systemDefault())),file.length()));
	}
	private static void resort()
	{
		imgd.sort(new Comparator<imgdata>() {

			@Override
			public int compare(imgdata o1, imgdata o2) {
				try {
					BasicFileAttributes bf1 = Files.readAttributes(o1.img.toPath(), BasicFileAttributes.class);
					BasicFileAttributes bf2 = Files.readAttributes(o2.img.toPath(), BasicFileAttributes.class);
					long v1 = 0;
					long v2 = 0;
					v1 = bf1.lastModifiedTime().to(TimeUnit.DAYS);
					v2 = bf2.lastModifiedTime().to(TimeUnit.DAYS);
					if(v1 > v2)
					{
						return 1;
					}
					if(v1 < v2)
					{
						return -1;
					}
					if(v1 == v2)
					{
						return 0;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return 0;
			}
		});
		files.sort(new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				try {
					BasicFileAttributes bf1 = Files.readAttributes(o1.toPath(), BasicFileAttributes.class);
					BasicFileAttributes bf2 = Files.readAttributes(o2.toPath(), BasicFileAttributes.class);
					long v1 = 0;
					long v2 = 0;
					v1 = bf1.lastModifiedTime().to(TimeUnit.DAYS);
					v2 = bf2.lastModifiedTime().to(TimeUnit.DAYS);
					if(v1 > v2)
					{
						return 1;
					}
					if(v1 < v2)
					{
						return -1;
					}
					if(v1 == v2)
					{
						return 0;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return 0;
			}
		});
	}
	public static void CleanCache()
	{
		new Thread(new Runnable() {
			@Override
			public void run() 
			{	
				iscaching = true;
				internalcleancache();
				iscaching = false;
			}
		},"CleanGalleryCache").start();
		

	}
	
	private static void internalcleancache()
	{
		iscaching = true;
		imgd.removeAll(imgd);
		File[] thf = imgcache.listFiles(ff);
		List<File> lthf = Arrays.asList(thf);
		lthf.forEach((a)->{
			try {
				Files.delete(Paths.get(a.getAbsolutePath()));
			} catch (IOException e) {
				if(INSTANCE.player != null)
				{
					INSTANCE.player.displayClientMessage(Component.literal("Cache Cleaning Falied"), false);
					//INSTANCE.player.sendMessage(new StringTextComponent("Cache Cleaning Falied"),null);
				}
				e.printStackTrace();
			}
		});
		if(INSTANCE.player != null)
		{
			INSTANCE.player.displayClientMessage(Component.literal("Cache Cleaning Finished"), false);
			//INSTANCE.player.sendMessage(new StringTextComponent("Cache Cleaning Finished"), null);
		}
		iscaching = false;
	}
	//delay is necessary because the event is called before the file is written
	public static void addsc(ScreenshotEvent event)
	{//fix using event.getImage
		Timer t = new Timer();
		t.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					cacheimg(event.getScreenshotFile());
				} catch (Throwable e) {
					if(INSTANCE.player != null)
					{
						INSTANCE.player.displayClientMessage(Component.literal("Caching Falied"), false);
						//INSTANCE.player.sendMessage(new StringTextComponent("Caching Falied"),null);
					}
					e.printStackTrace();
				}
				//imgd.add(new imgdata(event.getScreenshotFile(), event.getImage().getHeight(), event.getImage().getWidth(), LocalDate.now(), event.getScreenshotFile().length()));
				files = Arrays.asList(screenshootdir.listFiles(ff));
				resort();

			}
		}, 2000);

	}
	public static List<File> getfiles(LocalDate min,LocalDate max)
	{
		if(imgd.size() == 0)
		{
			return new ArrayList<>();		
		}
		//System.out.println(imgd.size());
		int i1 = getclosestvalue(min);
		int i2 = getclosestvalue(max);
		
		while((i2+1) < imgd.size() && imgd.get(i2+1).createdtime.toEpochDay() == max.toEpochDay())
		{
			i2++;
		}
		while((i1-1) >= 0 && imgd.get(i1-1).createdtime.toEpochDay() == min.toEpochDay())
		{
			i1--;
		}
		if(i2 == imgd.size())
		{
			i2 = imgd.size()-1;
		}
		//System.out.println(i1 +":"+ i2 + " : "+min.toEpochDay() + " : "+max.toEpochDay());
		List<imgdata> nd = imgd.subList(i1, i2+1);
		List<File> nf = nd.stream().map(imgdata::getFile).collect(Collectors.toList());
		return nf;
	}
	static int getclosestvalue(LocalDate date)
	{/*
		int i = 0;/
		while(imgd.get(i).createdtime.toEpochDay() > date.toEpochDay())
		{
			System.out.println(imgd.get(i).createdtime + " :: "+date);
			if(i+1 >= imgd.size())
			{
				return i;
			}
			i++;
		}
		return i;*/
		if(imgd.size() == 0)
		{
			return -1;
		}
		int first = 0;
	    int last = imgd.size() - 1;
	    int mid = 0;
	    do
	    {
	        mid = first + (last - first) / 2;
	        if (date.toEpochDay() > imgd.get(mid).createdtime.toEpochDay())
	            first = mid + 1;
	        else
	            last = mid - 1;/*
	        if (imgd.get(mid).createdtime.toEpochDay() == date.toEpochDay())
	            return mid;*/
	    } while (first <= last);
	    return mid;
	}
	public static DynamicTexture gethumbnail(String imgname)
	{
		try(InputStream str = new FileInputStream(imgcache.getAbsolutePath()+"/"+imgname))
		{
			return new DynamicTexture(NativeImage.read(str));
		} catch (FileNotFoundException e) {
			// TODO return a img that says the cahsing failed
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
class imgdata
{

	public imgdata(File img, int height, int width, LocalDate createdtime, long filesize)
	{
		this.img = img;
		this.height = height;
		this.width = width;
		this.createdtime = createdtime;
		this.filesize = filesize;
	}
	public File getFile()
	{
		return img;
	}
	File img;
	int height;
	int width;
	LocalDate createdtime;
	long filesize;
}
