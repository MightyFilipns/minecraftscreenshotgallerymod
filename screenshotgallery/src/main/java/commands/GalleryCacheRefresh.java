package commands;

import com.mightyfilipns.screenshotgallery.CacheManager;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class GalleryCacheRefresh {

	public GalleryCacheRefresh(CommandDispatcher<CommandSource> dis)
	{
		dis.register(Commands.literal("galleryrefreshcache").executes((cmd)->{
			CacheManager.buildcache(false);
			return 0;
		}).then(Commands.literal("--full").executes((cmd)->{
			CacheManager.CleanCache();
			CacheManager.buildcache(true);
			return 0;
		})));
	}

}
