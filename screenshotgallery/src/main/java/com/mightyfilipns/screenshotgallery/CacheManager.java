package com.mightyfilipns.screenshotgallery;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.util.text.StringTextComponent;
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
	static List<imgdata> imgd = new ArrayList<imgdata>();
	private static final Minecraft INSTANCE = Minecraft.getInstance();
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
		return imgd.get(0).createdtime;
	}
	public static void buildcache() throws IOException
	{
		if(!maindir.exists())
		{
			Files.createDirectory(Paths.get(maindir.getAbsolutePath()));
		}
		if(!imgcache.exists())
		{
			Files.createDirectory(Paths.get(imgcache.getAbsolutePath()));
		}
		if(!datacache.exists())
		{
			Files.createDirectory(Paths.get(datacache.getAbsolutePath()));
		}
		files = Arrays.asList(screenshootdir.listFiles(ff));
		for (File file : files) 
		{
			if(Files.exists(Paths.get(imgcache.getAbsolutePath()+"/"+file.getName())))
			{			
				BasicFileAttributes bf = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
				imgd.add(new imgdata(file,getdim(file, true),getdim(file, false),LocalDate.ofInstant(bf.lastModifiedTime().toInstant(), ZoneId.systemDefault()),file.length()));
			}
			else
			{
				cacheimg(file);
			}
		}
		resort();
	}
	private static void cacheimg(File file) throws IOException
	{
		BufferedImage img = null;
		try {
			img = ImageIO.read(file);
		} catch (IOException e) 
		{
			e.printStackTrace();
			return;
		}
		int xsize =200;
		int ysize = 112;;
		 
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
		imgd.add(new imgdata(file,img.getWidth(),img.getHeight(),LocalDate.ofInstant(bf.lastModifiedTime().toInstant(), ZoneId.systemDefault()),file.length()));
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
		imgd.clear();
		File[] thf = imgcache.listFiles(ff);
		List<File> lthf = Arrays.asList(thf);
		lthf.forEach((a)->{
			try {
				Files.delete(Paths.get(a.getAbsolutePath()));
			} catch (IOException e) {
				if(INSTANCE.player != null)
				{
					INSTANCE.player.sendMessage(new StringTextComponent("Cache Cleaning Falied"),null);					
				}
				e.printStackTrace();
			}
		});
	}
	//delay is necessary because the event is called before the file is written
	public static void addsc(ScreenshotEvent event)
	{
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			
			@Override
			public void run() {
				try {
					cacheimg(event.getScreenshotFile());
				} catch (IOException e) {
					if(INSTANCE.player != null)
					{
						INSTANCE.player.sendMessage(new StringTextComponent("Caching Falied"),null);					
					}
					e.printStackTrace();
				}
				imgd.add(new imgdata(event.getScreenshotFile(), event.getImage().getHeight(), event.getImage().getWidth(), LocalDate.now(), event.getScreenshotFile().length()));
				files = Arrays.asList(screenshootdir.listFiles(ff));
				resort();
				
			}
		}, 2000);

	}
	public static List<File> getfiles(LocalDate min,LocalDate max)
	{
		//System.out.println(imgd.size());
		int i1 = getclosestvalue(min);
		int i2 = getclosestvalue(max)+1;
		if(i2 == imgd.size())
		{
			i2 = imgd.size()-1;
		}
		System.out.println(i1 +":"+ i2 + " : "+min.toEpochDay() + " : "+max.toEpochDay());
		List<imgdata> nd = imgd.subList(i1, i2);
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
