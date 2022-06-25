package commands;

import java.io.IOException;

import com.mightyfilipns.screenshotgallery.CacheManager;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class GalleryCacheRefresh {

	public GalleryCacheRefresh(CommandDispatcher<CommandSource> dis) 
	{
		dis.register(Commands.literal("galleryrefreshcache").executes((cmd)->{
			try {
				CacheManager.buildcache();
			} catch (IOException e) {
				e.printStackTrace();
				return 1;
			}
			return 0;
		}).then(Commands.literal("--full").executes((cmd)->{
			CacheManager.CleanCache();
			try {
				CacheManager.buildcache();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		})));
	}

}
