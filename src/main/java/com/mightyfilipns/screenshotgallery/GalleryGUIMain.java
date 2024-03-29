package com.mightyfilipns.screenshotgallery;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.command.ConfigCommand;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import commands.GalleryCacheRefresh;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(GalleryGUIMain.modid)
public class GalleryGUIMain
{
    private static final Minecraft INSTANCE = Minecraft.getInstance();
    public final static String modid = "screenshotgallery";
	// Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public GalleryGUIMain() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }
    KeyMapping kb =  new KeyMapping("Open Gallery", 71 , "Gallery Mod");
    private void setup(final FMLCommonSetupEvent event)
    {

    }
    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        //LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().options);
    	LOGGER.info("Building cache");
    	CacheManager.buildcache(false,true);
    	CacheManager.checkcache();
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        //InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        /*LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));*/
    }
    @SubscribeEvent
    public void Cmdreg(RegisterCommandsEvent event)
    {
    	new GalleryCacheRefresh(event.getDispatcher());
    	ConfigCommand.register(event.getDispatcher());
    }
    @SubscribeEvent
    public void screenshot(ScreenshotEvent event)
    {
    	CacheManager.addsc(event);
    }
    
    @SubscribeEvent
    public void kbreg(RegisterKeyMappingsEvent event)
    {
    	event.register(kb);
    }
    
    @SubscribeEvent
    public void tick(ClientTickEvent event)
    {
    	if(kb.isDown() && INSTANCE.screen == null)
    	{
    		INSTANCE.setScreen(new GalleryGUI());
    	}
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
    	
    }
}
