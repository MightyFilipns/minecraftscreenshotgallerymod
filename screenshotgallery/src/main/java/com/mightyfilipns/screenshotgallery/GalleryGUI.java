 package com.mightyfilipns.screenshotgallery;

import java.io.Console;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.NativeImage.PixelFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;

public class GalleryGUI extends Screen {
	private static final Minecraft INSTANCE = Minecraft.getInstance();
	public static DynamicTexture whiteimg = new DynamicTexture(whitesq(10, 10));
	List<DynamicTexture> dym = new ArrayList<DynamicTexture>();
	List<Integer> torender = new ArrayList<Integer>();
	List<Integer> renderd = new ArrayList<Integer>();
	List<Integer> notdis = new ArrayList<Integer>();
	final static int screenshotxsize = 200;
	final static int screenshotysize = 112;
	File[] files;
	File screenshootdir = new File(INSTANCE.gameDirectory.getAbsolutePath(), "/screenshots/");
	ResourceLocation sliders = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
	FileFilter ff = file -> !file.isDirectory() && file.getName().endsWith(".png");
	int scroll = 0;
	int perrow = 0;
	int leftover = 0;
	int margin = 0;
	int scrollmaxvalue;
	int maxheight;
	static GalleryGUI ins = null;
	int hoverborder = 5; 
	int lasthoverover = -1;
	boolean editmode = false;
	DynamicTexture chosen = null;
	
	Button filexp = null;
	Button details = null;
	Button aspectratiob = null;
	
	boolean respectaspectration = true;
	boolean detailsopen = false;
	BasicFileAttributes attr = null;
	List<String> disdata = new ArrayList<String>();
	final int charcoeff = 5;
	int dw = 0;
	int dh = 100;
	Dropbox<sortdir> sortdbox = null;
	Dropbox<sorttype> sortboxtype = null;
	boolean notfirsts = false;
	enum sortdir
	{
		Ascending,
		Descending
	}
	enum sorttype
	{
		lastModifiedTime,
		lastCreatedTime,
		filesize,
		width,
		height,
	}
	
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
		maxheight = (int) ((margin + screenshotysize) * Math.ceil(((float)files.length / (float)perrow)));
		scrollmaxvalue = Math.max(maxheight - height, 0);
		scroll = Math.max(Math.min(scroll, scrollmaxvalue), 0);
	}
	
	public boolean issortinghovered()
	{
		if(sortdbox.isHovered() || sortboxtype.isHovered())
		{
			return true;
		}
		return false;
	}
	private boolean issortingopen()
	{
		if(sortdbox.isopen || sortboxtype.isopen)
		{
			return true;
		}
		return false;
	}
	private void togglesortvisibility(boolean newstate)
	{
		sortdbox.setvisiblity(newstate);
		sortboxtype.setvisiblity(newstate);
	}
	public void resort()
	{
		List<File> tf = Arrays.asList(files);
		tf.sort(new Comparator<File>() 
		{
			int postivevalue = -1;
			int negativevalue = 1;
			@Override
			public int compare(File o1, File o2) 
			{
				if(sortdbox.getvalue() == sortdir.Ascending)
				{
					postivevalue = 1;
					negativevalue = -1;
				}
				try 
				{
					BasicFileAttributes bf1 = Files.readAttributes(o1.toPath(), BasicFileAttributes.class);
					BasicFileAttributes bf2 = Files.readAttributes(o2.toPath(), BasicFileAttributes.class);
					
					long v1 = 0;
					long v2 = 0;
					sorttype st = (sorttype) sortboxtype.getvalue();
					switch (st) 
					{
						case filesize:
							v1 = o1.length();
							v2 = o2.length();
							break;
						case height:
							v1 = getdim(o1, true);
							v2 = getdim(o2, true);
							break;
						case lastCreatedTime:
							v1 = bf1.creationTime().to(TimeUnit.SECONDS);
							v2 = bf2.creationTime().to(TimeUnit.SECONDS);
							break;
						case lastModifiedTime:
							v1 = bf1.lastModifiedTime().to(TimeUnit.SECONDS);
							v2 =  bf2.lastModifiedTime().to(TimeUnit.SECONDS);
							break;
						case width:
							v1 = getdim(o1, false);
							v2 = getdim(o2, false);
							break;
						default:
							
							break;
						
					}

					if(v1 > v2)
					{
						return postivevalue;
					}
					if(v1 < v2)
					{
						return negativevalue;
					}
					if(v1 == v2)
					{
						return 0;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return 0;
			}
		});
		files = tf.toArray(new File[0]);
	}
	private int getdim(File img,boolean height)
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
	
	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		if(!editmode) 
		{
			scroll -= pDelta * 10;
			calcscroll();
			updateimgs();			
		}
		return super.mouseScrolled(pMouseX, pMouseY, pDelta);
	}
	@Override
	public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) 
	{
		notfirsts = false;
		if(!editmode)
		{
			dym.removeAll(dym);
			renderd.removeAll(renderd);			
		}
		calcscroll();
		updateimgs();
		return super.mouseReleased(pMouseX, pMouseY, pButton);
	}
	@Override
	public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) 
	{
		if(pMouseX < 0 || pMouseY <0 || pMouseX > width || pDragY > height)
		{
			return false;
		}
		float scrollp = (float)scroll/((float)scrollmaxvalue);
		int y = (int) (scrollp*(height-15));
		if(pMouseX > width-15 || notfirsts && !editmode && pMouseY+50 > y && y > pMouseY-30)
		{
			notfirsts = true;
			scroll = (int) (scrollmaxvalue*(pMouseY/(float)height));
			updateimgs();
		}
		return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
	}
	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) 
	{
		if(lasthoverover > -1 && !editmode && files[lasthoverover] != null && !issortingopen())
		{
			editmode = true;
			chosen = loadimg(files[lasthoverover]);	
			filexp = new Button(this.width / 2 - 150, this.height-20, 150, 20, new StringTextComponent("Open in default image viewer"), (a) -> {
				Util.getPlatform().openFile(files[lasthoverover]);
		     });
			this.addButton(filexp);
			details = new Button(this.width / 2, this.height-20, 150, 20, new StringTextComponent("More details"), (a) -> 
			{
				detailsopen = true;
		     });
			this.addButton(details);
			try {
				attr = Files.readAttributes(files[lasthoverover].toPath(), BasicFileAttributes.class);
				disdata.removeAll(disdata);
				disdata.add("Created: " + attr.creationTime().toString());
				disdata.add("File Name: " + files[lasthoverover].getName());
				disdata.add("Width: " + chosen.getPixels().getWidth());
				disdata.add("Height: "+ chosen.getPixels().getHeight());
				disdata.add("File Size:"+files[lasthoverover].length()+ " Bytes");
				for (int i = 0; i < disdata.size(); i++) 
				{
					dw = Math.max(charcoeff*disdata.get(i).length(), dw);
				}
				
			} catch (IOException e) {
				details.setMessage(new StringTextComponent("Error: " + e.getMessage()));
				e.printStackTrace();
			}
			togglesortvisibility(false);
		}
		StaticFunctions.playDownSound();
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}
	@Override
	public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) 
	{
		perrow = Math.max(1, Math.floorDiv(width, screenshotxsize));
		leftover = width % screenshotxsize;
		margin = (int) Math.floor(leftover / (perrow + 1));
		this.renderBackground(pMatrixStack);
		int pos1 = 0;
		int pos2 = 0;
		if(files == null && files.length == 0)
		{
			drawCenteredString(pMatrixStack, this.font, new StringTextComponent("No Screenshots found"), width/2, height/2, 0xff_ff_ff_ff);
		}
		if(editmode)
		{
			int m2 = 30;
			int m3 = m2;
			
			int imgw = width-(2*m2);
			int imgh = height-(2*m3);
			
			int min = Math.min(imgw, imgh);
			
			int chosenh = chosen.getPixels().getHeight();
			int chosenw = chosen.getPixels().getWidth();
			
			float ratio = (float)chosenw/(float)chosenh;
			if(respectaspectration)
			{				
				if(chosenw < chosenh)
				{
					imgw = (int) (imgh*ratio);
					if(imgw > width-m2*2)
					{
						imgw = width-m2*2;
						imgh = (int) (imgw/ratio);
					}
				}
				if(chosenw > chosenh)
				{
					imgh = (int) (imgw/ratio);
					if(imgh > height-m2*2)
					{
						imgh = height-m2*2;
						imgw = (int) (imgh*ratio);
					}
				}
				if(chosenw == chosenh)
				{
					imgh = min;
					imgw = min;
				}
				m2 = width/2-imgw/2;
				m3 = height/2-imgh/2;
			}
			
			
			chosen.bind();
			blit(pMatrixStack,m2,m3,imgw,imgh, 0, 0,1,1,1,1);
			if(attr != null && detailsopen)
			{
				int x1 = width/2-dw/2;
				int y1 = height/2-dh/2;
				int x2 = width/2+dw/2;
				int y2 = height/2+dh/2;
				int bordercolor = this.minecraft.options.getBackgroundColor(1f);
				fill(pMatrixStack, x1-10, y1-10, x1, y2+10,bordercolor);
				fill(pMatrixStack, x1, y1, x2, y1-10,bordercolor);
				fill(pMatrixStack, x2, y1-10, x2+10, y2+10,bordercolor);
				fill(pMatrixStack, x1, y2, x2, y2+10,bordercolor);
				
				fill(pMatrixStack, x1, y1, x2, y2,this.minecraft.options.getBackgroundColor(0.7f));
				
				for (int i = 0; i < disdata.size(); i++) 
				{
					drawString(pMatrixStack, this.font, disdata.get(i), x1, y1+(i*10), 0xff_ff_ff_ff);
				}
			}
		}
		
		calcscroll();
		if(renderd.size() != 0)
		{
			pos2 = renderd.get(0)/perrow;			
		}
		else
		{
			updateimgs();
			pos2 = renderd.get(0)/perrow;
		}
		if(!editmode)
		{
			lasthoverover = -1;			
		}
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
				final float ratio = screenshotxsize/(float)(screenshotysize);
				
				int imgh =screenshotysize;
				int imgw = screenshotxsize;
				
				NativeImage chosen = item.getPixels();
				
				int chosenw = chosen.getWidth();
				int chosenh = chosen.getHeight();
				
				int min = Math.min(imgw, imgh);
				
				if(chosenw < chosenh)
				{
					imgw = (int) (imgh/ratio);
					if(imgw > screenshotxsize)
					{
						imgw = screenshotxsize;
						imgh = (int) (imgw*ratio);
					}
				}
				if(chosenw > chosenh)
				{
					imgh = (int) (imgw/ratio);
					if(imgh > 112)
					{
						imgh = 112;
						imgw = (int) (imgh*ratio);
					}
				}
				if(chosenw == chosenh)
				{
					imgh = min;
					imgw = min;
				}
				
				int x = (margin * (pos1 + 1) + (pos1 * screenshotxsize))+Math.max(0, screenshotxsize-imgw)/2;
				int y = (margin * (pos2 + 1) + (screenshotysize * pos2) - scroll)+20;
				item.bind();
				blit(pMatrixStack,x,y,imgw, imgh, 0, 0, 1, 1, 1, 1);
				//System.out.println("blit " + imgw+" " + imgh+" "+x+" "+y);
				if(iswithin(pMouseY, y, y+imgh) && iswithin(pMouseX, x, x+imgw) && !issortingopen())
				{
					whiteimg.bind();
					blit(pMatrixStack,x,y,hoverborder,imgh,0 , 0, 1, 1, 1, 1);
					blit(pMatrixStack,x,y,imgw,hoverborder,0 , 0, 1, 1, 1, 1);
					blit(pMatrixStack,x+imgw-hoverborder,y,hoverborder,imgh,0 , 0, 1, 1, 1, 1);
					blit(pMatrixStack,x,y+imgh-hoverborder,imgw,hoverborder,0 , 0, 1, 1, 1, 1);
					lasthoverover = pos2*perrow+pos1;
				}
			}
			else
			{
				if(pos2*perrow+pos1 <= files.length-1)
				{
					int x = (margin * (pos1 + 1) + (pos1 * screenshotxsize));
					int y = (margin * (pos2 + 1) + (screenshotysize * pos2) - scroll);
					drawString(pMatrixStack, this.font, "Error Encountered" , x, y, 0xFF_FF_FF_FF);					
				}
			}
			pos1++;
		}
		if(scrollmaxvalue > 0 && !editmode)
		{
			this.minecraft.getTextureManager().bind(sliders);
			int x = width-12;
			float scrollp = (float)scroll/((float)scrollmaxvalue);
			int y = (int) (scrollp*(height-15));
			blit(pMatrixStack,x,y,232,0,12,15);
		}
		super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
	}
	@Override
	public boolean shouldCloseOnEsc() 
	{
		if(detailsopen)
		{
			detailsopen = false;
			return false;
		}
		if(editmode)
		{
			buttons.remove(details);
			buttons.remove(filexp);
			editmode = false;
			togglesortvisibility(true);
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
		sortdbox = new Dropbox<sortdir>(width-100, 0, 100, 20, sortdir.Descending, buttons, (a,b) -> {
			resort();
		});
		sortboxtype = new Dropbox<sorttype>(width-200, 0, 100, 20, sorttype.lastModifiedTime, buttons, (a,b) -> {
			resort();
		});
		this.addButton(sortdbox);
		this.addButton(sortboxtype);
		resort();
		updateimgs();

	}
	@Override
	public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
		dym.removeAll(dym);
		renderd.removeAll(renderd);
		scroll = 0;
		super.resize(pMinecraft, pWidth, pHeight);
		if(editmode)
		{
			filexp.x = this.width / 2-150;
			details.x = this.width / 2;
			filexp.y =this.height-20;
			details.y =this.height-20;
			this.addButton(filexp);
			this.addButton(details);
		}
	}
	private void updateimgs() {
		//System.out.println("called");
		torender.removeAll(torender);
		notdis.removeAll(notdis);
		int a = screenshotysize + margin;
		int v1 = (int) Math.ceil((double) height / (double) a);
		int visible = (int) v1 * perrow;
		int scrolleff = (int) Math.floor(scroll / a);
		
		try 
		{
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
			//== can't be used because java
			if (torender.get(0).equals(renderd.get(0 + perrow))) {
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
				return;
			}
			//fix
			if (torender.get(0 + perrow).equals(renderd.get(0)))
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
				return;
			}
			//System.out.println("no ifs fulfilled "+ torender.get(0)+" "+renderd.get(0 + perrow) +" : "+ torender.get(0).equals(renderd.get(0 + perrow)));
		}
		catch (IndexOutOfBoundsException e) 
		{
			dym.removeAll(dym);
			renderd.removeAll(renderd);
			scroll = 0;
			calcscroll();
			e.printStackTrace();
		}

	}

	public DynamicTexture loadimgresized(File img) {
		NativeImage nativeimage = null;
		//long start = System.currentTimeMillis();
		try (InputStream inputstream = new FileInputStream(img.getAbsoluteFile())) {
			nativeimage = NativeImage.read(inputstream);
			//System.out.println(System.currentTimeMillis()-start+" load");
			int x = width/perrow;
			int y = x;
			if((float)nativeimage.getWidth()/(float)nativeimage.getHeight() != 0)
			{
				y /= (float)nativeimage.getWidth()/(float)nativeimage.getHeight(); 				
			}
			
			
			nativeimage = resize(x, y, nativeimage);
			//System.out.println(System.currentTimeMillis()-start+" full");
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