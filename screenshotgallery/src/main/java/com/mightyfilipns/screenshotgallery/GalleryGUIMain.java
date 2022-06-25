package com.mightyfilipns.screenshotgallery;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent.KeyboardKeyEvent;
import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.command.ConfigCommand;

import java.io.IOException;

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
    KeyBinding kb =  new KeyBinding("Open Gallery", 71 , "Gallery Mod");
    private void setup(final FMLCommonSetupEvent event)
    {
    	LOGGER.info("Building cache");
    	try {
			CacheManager.buildcache();
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.info("Building cache falied");
		}
    }
    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        //LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().options);
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
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts

    }
    @SubscribeEvent
    public void screenshot(ScreenshotEvent event)
    {
    	CacheManager.addsc(event);
    }
    
    @SubscribeEvent
    public void keyb(KeyboardKeyEvent e)
    {
    	if(e.getKeyCode() == 65 && INSTANCE.screen instanceof GalleryGUI)
    	{
    		GalleryGUI.ins.stop();	
    	}
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
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            //LOGGER.info("HELLO from Register Block");
        }
    }
}
