package commands;


import org.apache.logging.log4j.LogManager;

import com.mightyfilipns.screenshotgallery.CacheManager;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class GalleryCacheRefresh {

	Minecraft INS = Minecraft.getInstance();
	public GalleryCacheRefresh(CommandDispatcher<CommandSourceStack> dis)
	{
		dis.register(Commands.literal("galleryrefreshcache").executes((cmd)->{
			if(INS.level.isClientSide)
			{
				CacheManager.buildcache(false,false);				
			}
			else
			{
				LogManager.getLogger().info("This command can be run on servers");
			}
			return 0;
		}).then(Commands.literal("--full").executes((cmd)->{
			if(INS.level.isClientSide)
			{
				CacheManager.CleanCache();
				CacheManager.buildcache(true,false);		
			}
			else
			{
				LogManager.getLogger().info("This command can be run on servers");
			}
			return 0;
		})));
	}

}
