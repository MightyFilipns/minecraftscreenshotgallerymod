 package com.mightyfilipns.screenshotgallery;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.mightyfilipns.screenshotgallery.Widgets.DatePicker;
import com.mightyfilipns.screenshotgallery.Widgets.Dropbox;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
@OnlyIn(Dist.CLIENT)
public class GalleryGUI extends Screen {
	private static final Minecraft INSTANCE = Minecraft.getInstance();
	TextureManager tm = INSTANCE.getTextureManager();
	public static DynamicTexture whiteimg = new DynamicTexture(StaticFunctions.whitesq(10, 10,0xFF_FF_FF_FF));
	public static DynamicTexture graybar = new DynamicTexture(StaticFunctions.whitesq(10, 10,0x0F_FF_FF_FF));
	ResourceLocation rlwimng = tm.register("whiteimg", whiteimg);
	List<DynamicTexture> dym = new ArrayList<>();
	List<Integer> torender = new ArrayList<>();
	List<Integer> renderd = new ArrayList<>();
	List<Integer> notdis = new ArrayList<>();
	ResourceLocation rlmainimg = null;
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
	List<String> disdata = new ArrayList<>();
	final int charcoeff = 5;
	int dw = 0;
	int dh = 100;
	Dropbox<sortdir> sortdbox = null;
	Dropbox<sorttype> sortboxtype = null;
	boolean notfirsts = false;
	DatePicker dp1 = null;
	DatePicker dp2 = null;
	enum sortdir
	{
		Ascending,
		Descending
	}
	enum sorttype
	{
		CreatedTime,
		filesize,
		width,
		height,
	}

	protected GalleryGUI() {
		super(Component.literal("Screenshot gallery"));
		ins = this;
	}

	private void calcscroll()
	{
		maxheight = (int) ((margin + screenshotysize) * Math.ceil(((float)files.length / (float)perrow)))+20;
		scrollmaxvalue = Math.max(maxheight - height, 0);
		scroll = Math.max(Math.min(scroll, scrollmaxvalue), 0);
	}

	public void filterdate()
	{
		if(dp1.getcurrentdate().toEpochDay() < dp2.getcurrentdate().toEpochDay())
		{
			List<File> a = CacheManager.getfiles(dp1.getcurrentdate(), dp2.getcurrentdate());
			files= a.toArray(new File[0]);			
		}
		else
		{
			files = new File[0];
		}
		scroll = 0;
		dym.clear();
		renderd.clear();
		resort();
		calcscroll();
		updateimgs();
	}

	public boolean issortinghovered()
	{
		if(sortdbox.isHoveredOrFocused() || sortboxtype.isHoveredOrFocused() || dp1.isHoveredOrFocused() || dp2.isHoveredOrFocused())
		{
			return true;
		}
		return false;
	}
	private boolean issortingopen()
	{
		if(sortdbox.getIsopen() || sortboxtype.getIsopen() || dp1.isopen() || dp2.isopen())
		{
			return true;
		}
		return false;
	}
	private void togglesortvisibility(boolean newstate)
	{
		sortdbox.setvisiblity(newstate);
		sortboxtype.setvisiblity(newstate);
		dp1.setvisiblity(newstate);
		dp2.setvisiblity(newstate);
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
					//long str = System.nanoTime();
					BasicFileAttributes bf1 = Files.readAttributes(o1.toPath(), BasicFileAttributes.class);
					BasicFileAttributes bf2 = Files.readAttributes(o2.toPath(), BasicFileAttributes.class);
					//long str2 = System.nanoTime();
					//System.out.println(str2-str);
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
						case CreatedTime:
							v1 = bf1.lastModifiedTime().to(TimeUnit.SECONDS);
							v2 = bf2.lastModifiedTime().to(TimeUnit.SECONDS);
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
				} catch (NoSuchFileException e) {
					if(!o1.exists()) 
					{
						
					}
				}
				catch (IOException e) 
				{
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
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		for (GuiEventListener child : children()) {
			child.mouseScrolled(pMouseX, pMouseY, pDelta);
		}
		if(!editmode ^ (dp1.isopen() || dp2.isopen()))
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
		return true;//super.mouseReleased(pMouseX, pMouseY, pButton);
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
		if(pMouseX > width-15 || notfirsts && !editmode && pMouseY+50 > y && y > pMouseY-30 && !sortdbox.getIsopen())
		{
			notfirsts = true;
			scroll = (int) (scrollmaxvalue*(pMouseY/height));
			updateimgs();
		}
		return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
	}
	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
	{
		
		if(lasthoverover > -1 && !editmode && files[lasthoverover] != null && !issortingopen() && !issortinghovered())
		{
			chosen = loadimg(files[lasthoverover]);
			if(chosen == null)
			{
				return super.mouseClicked(pMouseX, pMouseY, pButton);
			}
			editmode = true;
			rlmainimg = tm.register("mainimg", chosen);
			filexp = Button.builder(Component.literal("Open in default image viewer"), (a) -> {
				Util.getPlatform().openFile(files[lasthoverover]);
		     }).bounds(this.width / 2 - 150, this.height-20, 150, 20).build();
			this.addRenderableWidget(filexp);
			
			details = Button.builder(Component.literal("More details"), (a) ->
			{
				detailsopen = true;
		     }).bounds(this.width / 2, this.height-20, 150, 20).build();
			this.addRenderableWidget(details);
			
			try {
				attr = Files.readAttributes(files[lasthoverover].toPath(), BasicFileAttributes.class);
				disdata.removeAll(disdata);
				disdata.add("Created: " + attr.creationTime().toString());
				disdata.add("File Name: " + files[lasthoverover].getName());
				disdata.add("Width: " + chosen.getPixels().getWidth());
				disdata.add("Height: "+ chosen.getPixels().getHeight());
				disdata.add("File Size:"+ files[lasthoverover].length()+ " Bytes");
					for (String element : disdata) {
						dw = Math.max(charcoeff*element.length(), dw);
					}

			} catch (IOException e) {
				details.setMessage(Component.literal("Error: " + e.getMessage()));
				e.printStackTrace();
			}
			togglesortvisibility(false);
		}
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}
	
	
	
	@Override
	public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
	{
		perrow = Math.max(1, Math.floorDiv(width, screenshotxsize));
		leftover = width % screenshotxsize;
		margin = (int) Math.floor(leftover / (perrow + 1));
		this.renderBackground(pMatrixStack);
		int pos1 = 0;
		int pos2 = 0;
		
		if(files == null || files.length == 0)
		{
			if(dp1.getcurrentdate().toEpochDay() >= dp2.getcurrentdate().toEpochDay())
			{
				drawCenteredString(pMatrixStack, this.font, "Invalid Date Filter", width/2, height/2, 0x00_FF_00_00);
			}
			else
			{
				drawCenteredString(pMatrixStack, this.font, "No Screenshots Found", width/2, height/2, 0xff_ff_ff_ff);
				togglesortvisibility(false);
			}
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

			RenderSystem.setShaderTexture(0, rlmainimg);
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
		//updateimgs();
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
		if(!editmode && files != null && files.length != 0)
		{
			lasthoverover = -1;
			int tosw = 125;
			if(width < 450)
			{
				int lefo = width-212;
				tosw = lefo/2;
			}
			drawCenteredString(pMatrixStack, font,"To", tosw, 5, 0xFF_FF_FF_FF);
		}
		if(!editmode && !CacheManager.iscach.get())
		{
			for (DynamicTexture item : dym)
			{

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
					ResourceLocation rl = tm.register("empty", item);
					RenderSystem.setShaderTexture(0, rl);
					blit(pMatrixStack,x,y,imgw, imgh, 0, 0, 1, 1, 1, 1);
					if(StaticFunctions.iswithin(pMouseY, y, y+imgh) && StaticFunctions.iswithin(pMouseX, x, x+imgw) && !issortingopen() && !issortinghovered())
					{
						RenderSystem.setShaderTexture(0, rlwimng);
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
						drawCenteredString(pMatrixStack, this.font, "Error Encountered" , x+(screenshotxsize/2), y+(screenshotysize/2)-10, 0xFF_FF_FF_FF);
						drawCenteredString(pMatrixStack, this.font, "Try running /galleryrefreshcache" , x+(screenshotxsize/2), y+(screenshotysize/2)+10, 0xFF_FF_FF_FF);
					}
				}
				pos1++;
			}
		}
		if(scrollmaxvalue > 0 && !editmode)
		{
			RenderSystem.setShaderTexture(0, sliders);
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
			removeWidget(details);
			details.active = false;
			removeWidget(filexp);
			filexp.active = false;
			editmode = false;
			togglesortvisibility(true);
			return false;
		}
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
		//files = screenshootdir.listFiles(ff);
		int dpw = 100;
		if(width < 450)
		{
			int lefto = width-212;
			int one5 = lefto/5;
			dpw = one5*2;
		}
		@SuppressWarnings("unchecked")
		List<GuiEventListener> lst = (List<GuiEventListener>) children();
 		sortdbox = new Dropbox<sortdir>(width-100-12, 0, 100, 20, sortdir.Ascending, lst, (a,b) -> {
			resort();
		},this);
		sortboxtype = new Dropbox<>(width-200-12, 0, 100, 20, sorttype.CreatedTime, lst, (a,b) -> {
			resort();
		},this);
		dp1 = new DatePicker(0, 0, dpw, 20, CacheManager.getearliestdate(), CacheManager.getlatestdate(),CacheManager.getearliestdate(),lst,this);
		dp2 = new DatePicker((int)(dpw*1.5f), 0, dpw, 20, CacheManager.getearliestdate(), CacheManager.getlatestdate(), CacheManager.getlatestdate(),lst,this);
		this.addRenderableWidget(sortdbox);
		this.addRenderableWidget(sortboxtype);
		dp1.setOnchange((a)->{
			filterdate();
		});
		dp2.setOnchange((a)->{
			filterdate();
		});
		filterdate();
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
			filexp.setPosition(this.width / 2-150, this.height-20);
			details.setPosition(this.width / 2, this.height-20);
			this.addWidget(filexp);
			this.addWidget(details);
		}
	}
	boolean sttime= true;
	private void updateimgs() {
		torender.removeAll(torender);
		notdis.removeAll(notdis);
		int a = screenshotysize + margin;
		int v1 = (int) Math.ceil((double) height / (double) a);
		int visible = v1 * perrow;
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
					dym.add(CacheManager.gethumbnail((files[torender.get(i)]).getName()));
				}
				renderd = new ArrayList<>(torender);
				//System.out.println("initial");
				//System.out.println(str-System.nanoTime());
				return;
			}
			if (torender.get(0).equals(renderd.get(0))) {
				//System.out.println("same");
				return;
			}
			else
			{
				renderd.clear();
				dym.clear();
				int tol = Math.min(files.length, torender.size());
				//System.out.println(torender.size()+ " " +files.length);
				for (int i = 0; i < tol; i++)
				{
					if(notdis.contains(torender.get(i)))
					{
						dym.add(null);
						continue;
					}
					dym.add(CacheManager.gethumbnail((files[torender.get(i)].getName())));
				}
				renderd = new ArrayList<>(torender);
				//System.out.println((System.nanoTime()-str)/1000000f);
				return;
			}/*
			// scroll down
			if (torender.get(0).equals(renderd.get(0 + perrow))) {
				
				renderd.clear();
				dym.clear();
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
				renderd = new ArrayList<>(torender);
				//System.out.println("initial");
				
				
				
				
				
				
				for (int i = 0; i < perrow; i++) {
					dym.remove(i);
					//System.out.println("removal "+ renderd.get(i));
				}
				for (int i = 0; i < perrow; i++)
				{
					int fi = Math.abs(i-perrow);
					if(notdis.contains(torender.get(torender.size() - fi - (sttime ? 0: perrow))))
					{
						dym.add(null);
						continue;
					}
					dym.add(loadimgresized(files[torender.get(torender.size() - fi-(sttime ? 0: perrow))]));
				}
				for (int i = 0; i < perrow; i++)
				{
					dym.set(i, loadimgresized(files[torender.get(i)])); // i couldn't figure out what's wrong with the code above, so i added this to "fix" it
				}
				renderd = new ArrayList<>(torender);
				//System.out.println("scroll down");
				if(sttime)
				{
					sttime = false;
				}
				System.out.println((System.nanoTime()-str)/1000000f);
				return;
			}
			//scroll up
			if (torender.get(0 + perrow).equals(renderd.get(0)))
			{
				renderd.clear();
				dym.clear();
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
				renderd = new ArrayList<>(torender);
				//System.out.println("initial");
				
				
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
				List<DynamicTexture> ndym = new ArrayList<>();
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
				renderd = new ArrayList<>(torender);
				//System.out.println("scroll up");
				// scroll up
				System.out.println((System.nanoTime()-str)/1000000f);
				return;
			}*/
			//System.out.println("no ifs fulfilled "+ torender.get(0)+" "+renderd.get(0 + perrow) +" : "+ torender.get(0).equals(renderd.get(0 + perrow)));
		}
		catch (IndexOutOfBoundsException e)
		{
			dym.removeAll(dym);
			renderd.removeAll(renderd);
			scroll = 0;
			calcscroll();
			//e.printStackTrace();
		}

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

	@Override
	public boolean isPauseScreen() {
		return true;
	}
}