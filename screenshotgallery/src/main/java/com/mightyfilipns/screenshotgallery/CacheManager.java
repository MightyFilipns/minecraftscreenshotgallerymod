package com.mightyfilipns.screenshotgallery;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;

public class CacheManager 
{
	public CacheManager() {}
	static File maindir = new File(Minecraft.getInstance().gameDirectory.getAbsolutePath() + "/screenshotgallerycache/");
	static File imgcache = new File(maindir.getAbsolutePath() + "/imgcache/");
	static File screenshootdir = new File(Minecraft.getInstance().gameDirectory.getAbsolutePath(), "/screenshots/");
	static FileFilter ff = file -> !file.isDirectory() && file.getName().endsWith(".png");
	static List<File> files = null;
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
		files = Arrays.asList(screenshootdir.listFiles(ff));
		for (File file : files) 
		{
			
			if(Files.exists(Paths.get(imgcache.getAbsolutePath()+"/"+file.getName())))
			{
				continue;
			}
			BufferedImage img;
			try {
				img = ImageIO.read(file);
			} catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			int xsize =200;
			int ysize = 112;;
			 
			final float ratio = xsize/(float)(ysize);
			
			int imgh =ysize;
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
		}
	}
}
