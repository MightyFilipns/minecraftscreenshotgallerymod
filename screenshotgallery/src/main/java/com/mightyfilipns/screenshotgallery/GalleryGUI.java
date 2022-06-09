 package com.mightyfilipns.screenshotgallery;

import java.awt.Desktop;
import java.awt.Point;
import java.awt.geom.Arc2D.Double;
import java.io.Console;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.NativeImage.PixelFormat;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;

public class GalleryGUI extends Screen {
	private static final Minecraft INSTANCE = Minecraft.getInstance();
	public static DynamicTexture whiteimg = new DynamicTexture(whitesq(10, 10));
	List<DynamicTexture> dym = new ArrayList<DynamicTexture>();
	List<Integer> torender = new ArrayList<Integer>();
	List<Integer> renderd = new ArrayList<Integer>();
	List<Integer> notdis = new ArrayList<Integer>();
	final static int screenshotxsize = 200;
	File[] files;
	File screenshootdir = new File(INSTANCE.gameDirectory.getAbsolutePath(), "/screenshots/");
	FileFilter ff = file -> !file.isDirectory() && file.getName().endsWith(".png");
	int scroll = 0;
	int perrow = 0;
	int leftover = 0;
	int margin = 0;
	int scrollmaxvalue;
	int maxheight;
	static GalleryGUI ins = null;
	static int hoverborder = 10; 
	int lasthoverover = -1;
	boolean editmode = false;
	DynamicTexture chosen = null;
	Button filexp = new Button(this.width / 2 - 100, this.height-20, 200, 20, new StringTextComponent("Open in file explorer"), (a) -> {
        System.out.println("button");
     });
	Button details = null;
	public void stop()
	{
		int aa = 0;
	}
	protected GalleryGUI() {
		super(new StringTextComponent("Gallery Gui"));
		ins = this;
	}
	
	private void calcscroll()
	{
		maxheight = (int) ((margin + 122) * Math.ceil(((float)files.length / (float)perrow)));
		scrollmaxvalue = Math.max(maxheight - height, 0);
		scroll = Math.max(Math.min(scroll, scrollmaxvalue), 0);
	}
	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		scroll -= pDelta * 10;
		calcscroll();
		updateimgs();
		return super.mouseScrolled(pMouseX, pMouseY, pDelta);
	}
	
	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) 
	{
		if(lasthoverover > -1 && !editmode)
		{
			editmode = true;
			chosen = loadimg(files[lasthoverover]);	
			this.addButton(new Button(this.width / 2-150, this.height-20, 150, 20, new StringTextComponent("Open in file explorer"), (a) -> 
			{
				//check lasthoverover
				Util.getPlatform().openFile(files[lasthoverover]);
				System.out.println("file");
		        System.out.println("button");
		     }));
			details = new Button(this.width / 2, this.height-20, 150, 20, new StringTextComponent("More details"), (a) -> 
			{
		        System.out.println("details");
		     });
			this.addButton(details);
		}
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}
	
	@Override
	public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
		perrow = Math.max(1, Math.floorDiv(width, screenshotxsize));
		leftover = width % screenshotxsize;
		margin = (int) Math.floor(leftover / (perrow + 1));
		this.renderBackground(pMatrixStack);
		int pos1 = 0;
		int pos2 = 0;
		if(editmode)
		{
			int m2 = 30;
			chosen.bind();
			blit(pMatrixStack,m2,m2,width-(2*m2),height-(2*m2),0 , 0, 1, 1, 1, 1);
		}
		
		calcscroll();
		pos2 = renderd.get(0)/perrow;
		lasthoverover = -1;
		for (DynamicTexture item : dym) 
		{
			if(editmode)
			{
				break;
			}
			if (pos1 == perrow) {
				pos1 = 0;
				pos2++;
			}
			if(item != null)
			{
				int x = (margin * (pos1 + 1) + (pos1 * screenshotxsize));
				int y = (margin * (pos2 + 1) + (112 * pos2) - scroll);
				item.bind();
				blit(pMatrixStack,x,y,screenshotxsize, 112, 0, 0, 1, 1, 1, 1);
				if(iswithin(pMouseY, y, y+112) && iswithin(pMouseX, x, x+screenshotxsize))
				{
					whiteimg.bind();
					blit(pMatrixStack,x,y,hoverborder,112,0 , 0, 1, 1, 1, 1);
					blit(pMatrixStack,x,y,screenshotxsize,hoverborder,0 , 0, 1, 1, 1, 1);
					blit(pMatrixStack,x+screenshotxsize-hoverborder,y,hoverborder,112,0 , 0, 1, 1, 1, 1);
					blit(pMatrixStack,x,y+112-hoverborder,screenshotxsize,hoverborder,0 , 0, 1, 1, 1, 1);
					lasthoverover = pos2*perrow+pos1;

				}
			}
			pos1++;
		}
		super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
	}
	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) 
	{
		if(pKeyCode == 256)
		{
			//editmode = false;
		}
		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}
	@Override
	public boolean shouldCloseOnEsc() 
	{
		if(editmode)
		{
			editmode = false;
			return false;
		}
		return true;
	}
	
	@Override
	public boolean isPauseScreen() {
		return true;
	}

	@Override
	protected void init() {
		super.init();
		if (!screenshootdir.exists()) 
		{
			return;
		}
		perrow = Math.max(1, Math.floorDiv(width, screenshotxsize));
		leftover = width % screenshotxsize;
		margin = (int) Math.floor(leftover / (perrow + 1));
		files = screenshootdir.listFiles(ff);
		//System.out.println(files.length);
		updateimgs();

	}
	@Override
	public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
		dym.removeAll(dym);
		renderd.removeAll(renderd);
		scroll = 0;
		super.resize(pMinecraft, pWidth, pHeight);
	}
	private void updateimgs() {
		torender.removeAll(torender);
		notdis.removeAll(notdis);
		int a = 112 + margin;
		int v1 = (int) Math.ceil((double) height / (double) a);
		int visible = (int) v1 * perrow;
		int scrolleff = (int) Math.floor(scroll / a);
		//System.out.println((double)scroll/(double)a);
		if((double)scroll%(double)a <= 10)
		{
			visible+=perrow;
		}
		for (int i = 0; i < visible; i++) 
		{
			if(i+(scrolleff*perrow) >= files.length)
			{
				notdis.add(i+(scrolleff*perrow));
			}
			torender.add(i + (scrolleff * perrow));
		}
		
		//System.out.println(String.format("Visible:%s V1:%s scroleff:%S perrow:%s torender0:%s rendered0:%S",visible,v1,scrolleff,perrow,(torender.size() != 0 ? torender.get(0) : -1),(renderd.size() != 0 ? renderd.get(0) : -1)));
		//System.out.println(torender);
		if (renderd.size() == 0) 
		{
			int tol = Math.min(files.length, torender.size());
			//System.out.println(torender.size()+ " " +files.length);
			for (int i = 0; i < tol; i++) 
			{
				if(notdis.contains(torender.get(i)))
				{
					dym.add(null);
					continue;
				}
				dym.add(loadimgresized(files[torender.get(i)]));
			}
			renderd = new ArrayList<Integer>(torender);
			//System.out.println("initial");
			return;
		}
		if (torender.get(0) == renderd.get(0)) {
			//System.out.println("same");
			return;
		}
		if (torender.get(0) == renderd.get(0 + perrow)) {
			for (int i = 0; i < perrow; i++) {
				dym.remove(i);
				//System.out.println("removal "+ renderd.get(i));
			}
			for (int i = 0; i < perrow; i++) 
			{
				int fi = Math.abs(i-perrow);
				if(notdis.contains(torender.get(torender.size() - fi)))
				{
					dym.add(null);
					continue;
				}
				dym.add(loadimgresized(files[torender.get(torender.size() - fi)]));
			}
			for (int i = 0; i < perrow; i++) 
			{
				dym.set(i, loadimgresized(files[torender.get(i)])); // i couldn't figure out what's wrong with the code above, so i added this to "fix" it 				
			}
			renderd = new ArrayList<Integer>(torender);
			//System.out.println("scroll down");
			// scroll down
		}
		//fix
		if (torender.get(0 + perrow) == renderd.get(0))
		{
			if(getlast(renderd) != getlast(torender))
			{
				if(notdis.size() !=0)
				{
					//last row special
					for (int i = 0; i < perrow-notdis.size(); i++) 
					{
						dym.remove((dym.size() - 1) - i);
					}
				}
				else
				{
					for (int i = 0; i < perrow; i++) 
					{
						dym.remove(dym.size() - 1);
					}		
				}		
			}
			List<DynamicTexture> ndym = new ArrayList<DynamicTexture>();
			for (int i = 0; i < perrow; i++) {
				ndym.add(loadimgresized(files[torender.get(i)]));
			}
			dym.addAll(0, ndym);
			for (int i = 0; i < perrow; i++) 
			{
				if(dym.get(dym.size()-i-1) == null)
				{
					continue;
				}			
			}
			renderd = new ArrayList<Integer>(torender);
			//System.out.println("scroll up");
			// scroll up
		}

	}

	public DynamicTexture loadimgresized(File img) {
		NativeImage nativeimage = null;
		try (InputStream inputstream = new FileInputStream(img.getAbsoluteFile())) {
			nativeimage = NativeImage.read(inputstream);
			nativeimage = resize(screenshotxsize, 112, nativeimage);
			return new DynamicTexture(nativeimage);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public DynamicTexture loadimg(File img) {
		NativeImage nativeimage = null;
		try (InputStream inputstream = new FileInputStream(img.getAbsoluteFile())) {
			nativeimage = NativeImage.read(inputstream);
			return new DynamicTexture(nativeimage);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	private static NativeImage resize(int width, int height, NativeImage org) {
		NativeImage img = new NativeImage(width, height, false);
		double x_ratio = org.getWidth() / (double) width;
		double y_ratio = org.getHeight() / (double) height;
		double px, py;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				px = Math.floor(j * x_ratio);
				py = Math.floor(i * y_ratio);
				img.setPixelRGBA(j, i, org.getPixelRGBA((int) px, (int) py));
			}
		}
		return img;
	}
	static boolean iswithin(int value,int lowerlimit,int upperlimit)
	{
		if(value >= lowerlimit && value <= upperlimit)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	static <T> T getlast(List<T> toget)
	{
		if(toget == null || toget.size() == 0)
		{
			return null;
		}
		return toget.get(toget.size()-1);
	}
	static NativeImage whitesq(int x,int y)
	{
		NativeImage ni = new NativeImage(PixelFormat.RGBA,x,y,false);
		for (int i = 0; i < x; i++) 
		{
			for (int j = 0; j < y; j++) 
			{
				ni.setPixelRGBA(i, j, -1);
			}
		}
		return ni;
	}
}