/**
 * %HEADER%
 */
package net.sf.genomeview.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.jannot.source.Locator;

import org.apache.commons.io.FileUtils;
import org.java.plugin.ObjectFactory;
import org.java.plugin.Plugin;
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.PluginManager;
import org.java.plugin.PluginManager.PluginLocation;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Identity;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.standard.StandardPluginLocation;
import org.java.plugin.util.ExtendedProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.abeel.concurrency.DaemonThread;
import be.abeel.net.URIFactory;

/**
 * 
 * @author Thomas Abeel
 * @author thpar
 * 
 */
public class PluginLoader {

	private static Logger log = LoggerFactory.getLogger(PluginLoader.class.getCanonicalName());

	public static PluginManager pluginManager = null;

	private static boolean pluginLock = false;
	
	private static boolean corePluginLoaded = false;

	private static Model model;

	/**
	 * Sets up the plugin manager and loads the core and default plugins
	 * 
	 * @param model
	 */
	public static void init(Model model){
		PluginLoader.model = model;
		log.info("Shadow folder: " + System.getProperty("java.io.tmpdir") + "/.jpf-shadow");
		
		ExtendedProperties ep = new ExtendedProperties();
		
		ep.put("org.java.plugin.PathResolver", "org.java.plugin.standard.ShadingPathResolver");
		ep.put("unpackMode", "always");
		ep.put("org.java.plugin.standard.ShadingPathResolver.unpackMode", "always");
		
		pluginManager = ObjectFactory.newInstance(ep).createManager(
				ObjectFactory.newInstance(ep).createRegistry(),
				ObjectFactory.newInstance(ep).createPathResolver());
		
		loadCorePlugin();
		File[] defaultPlugins = gatherDefaultPlugins();
		loadPlugins(defaultPlugins);		
	}

	/** 
	 * Tries to lock the plugin loader so it can be used by one single thread.
	 * The lock can only be obtained if unlocked and if the core plugin is already loaded.
	 * 
	 * @return true when the lock was obtained, false when lock is in use
	 */
	synchronized public static boolean lockPluginLoader(){
		if (pluginLock || !corePluginLoaded){
			return false;
		} else {
			model.getGUIManager().startPluginLoading();
			pluginLock = true;
			return true;
		}
	}
	/** 
	 * Release lock on {@link PluginLoader}
	 */
	synchronized public static void unlockPluginLoader(){
		pluginLock = false;
		model.getGUIManager().finishPluginLoading();
	}
	
	/**
	 * Reads the default plugin directory as described in {@link Configuration} and detects all
	 * zip and jar files there.
	 * 
	 * @return array of plugin files.
	 */
	private static File[] gatherDefaultPlugins() {
		File pluginsDir = Configuration.getPluginDirectory();
		File[] plugins = pluginsDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".jar");
			}
		});
		return plugins;
	}
	
	/**
	 * Registers and loads the GenomeView core plugin.
	 */
	private static void loadCorePlugin(){
		DaemonThread dt = new DaemonThread(new Runnable() {
			public void run() {
				try {
					/* Load core plugin */
					PluginLocation coreLocation;
					URL manifest = PluginLoader.class.getResource("/plugin.xml");
					System.err.println("Core plugin manifest: " + manifest);
					String jar;

					if (manifest.toString().startsWith("jar")) { // normal
						// usage
						jar = manifest.toString().substring(4, manifest.toString().lastIndexOf('/') - 1);
					} else
						// developer usage
						jar = manifest.toString().substring(0, manifest.toString().lastIndexOf('/'));
					URL context = URIFactory.url(jar);
					System.err.println("Core plugin context: " + context);
					coreLocation = new StandardPluginLocation(context, manifest);
					pluginManager.publishPlugins(new PluginLocation[] { coreLocation });
					corePluginLoaded = true;
				} catch (Exception e) {
					log.error( "Plugin loading exception", e);
				}
			}
		});
		dt.start();
	}
	
	/**
	 * Convenience method to load a single plugin file. Runs {@link #loadPlugins(File[])}.
	 * 
	 * @param pluginFile single plugin file to register and start.
	 */
	public static void loadPlugin(File pluginFile){
		File[] pluginFiles = new File[1];
		pluginFiles[0] = pluginFile;
		loadPlugins(pluginFiles);
	}
	
	
	/**
	 * Registers a list of plugin files (jar or zip) with the current {@link PluginManager} and activates them, including
	 * extentions 
	 * ({@link IPlugin#init(Model)} and {@link Plugin#doStart()} methods of the plugin will be executed.
	 * 
	 * @param pluginFiles array of plugin files
	 */
	public static void loadPlugins(final File[] pluginFiles){
		DaemonThread dt = new DaemonThread(new Runnable() {

			public void run() {				
				while (!lockPluginLoader()){
					//keep trying... you're bound to get in at some point
				}

				try {
					//publish the plugins with the pluginManager
					Collection<Identity> newIds;
					try {
						PluginLocation[] locations = new PluginLocation[pluginFiles.length];
						
						int i=0;
						for (File plugin : pluginFiles) {
							locations[i++] = StandardPluginLocation.create(plugin);
						}
						
						//get a list of plugins that are already registered
						Collection<PluginDescriptor> oldDescriptors = pluginManager.getRegistry().getPluginDescriptors();
						Set<String> oldIds = new HashSet<String>();
						for (PluginDescriptor oldDesc :oldDescriptors){
							oldIds.add(oldDesc.getId());
						}
						
						//publish given plugins
						Map<String, Identity> newPlugins = pluginManager.publishPlugins(locations);
						newIds = newPlugins.values();
												
						//filter out plugins that were already registered
						Set<Identity> overlap = new HashSet<Identity>();
						for (Identity id : newIds){
							if (oldIds.contains(id.getId())){
								overlap.add(id);
							}
						}
						newIds.removeAll(overlap);
						
					} catch (Exception e) {
						unlockPluginLoader();
						throw new RuntimeException(e);
					}

					StringBuffer errorMessage = new StringBuffer();
					
					for (Identity pluginId : newIds) {
						PluginDescriptor pd = (PluginDescriptor)pluginId;
						try {
							log.info("Loading plugin " + pd);
							pluginManager.activatePlugin(pd.getId());

							Iterator<Extension> it = pd.getExtensions().iterator();
							while (it.hasNext()) {
								Extension ext = (Extension) it.next();
								ClassLoader classLoader = pluginManager.getPluginClassLoader(ext.getDeclaringPluginDescriptor());

								Class<?> toolCls = classLoader.loadClass(ext.getParameter("class").valueAsString());

								IPlugin tool = (IPlugin) toolCls.newInstance();

								tool.init(model);
							}
						} catch (PluginLifecycleException e) {
							String name = pd.getPluginClassName();
							name = name.substring(name.lastIndexOf('.') + 1);
							if (e.getMessage().contains("incompatible version")) {
								errorMessage.append(MessageManager.formatMessage("pluginloader.incompatible_plugin", new Object[]{name}));
							}
							if (e.getMessage().contains("can't start")) {
								errorMessage.append(MessageManager.formatMessage("pluginloader.cannot_start_plugin", new Object[]{name}));
							}
							log.error("Cannot load " + pd + " " + e.getMessage());
						} catch (InstantiationException e) {
							unlockPluginLoader();
							throw new RuntimeException(e);
						} catch (IllegalAccessException e) {
							unlockPluginLoader();
							throw new RuntimeException(e);
						} catch (ClassNotFoundException e) {
							//class not found == just carry on???
							e.printStackTrace();
						}
					}
					if (errorMessage.length() > 0) {
						errorMessage.append(MessageManager.getString("pluginloader.fix_plugins"));
						JOptionPane.showMessageDialog(model.getGUIManager().getParent(), errorMessage, 
								MessageManager.getString("pluginloader.plugin_error"),
								JOptionPane.ERROR_MESSAGE);
					}
				} catch(Exception e){
					log.error( "Plugin loading exception", e);
				} finally{
					//don't forget to release the lock
					unlockPluginLoader();					
				}
			}
		});
		dt.start();
	}

	/**
	 * Gets a plugin location, copies the plugin to the given destination directory and registers/activates the new plugin with 
	 * {@link #loadPlugin(File)}.
	 * If the plugin already existed in the target folder, it will not be overwritten.
	 * 
	 * @param pluginLocation place download the jar/zip file from 
	 * @param pluginDirectory place to store the plugin. This will be eiter the default plugin directory or the sessions plugin dir.
	 */
	public static void installPlugin(Locator pluginLocation, File pluginDirectory) throws IOException, URISyntaxException{
		if (pluginLocation.isURL()){
			installPlugin(pluginLocation.url(), pluginDirectory);
		} else {
			installPlugin(pluginLocation.file(), pluginDirectory);
		}
	}
	/**
	 * Gets a plugin location, copies the plugin to the given destination directory and registers/activates the new plugin with 
	 * {@link #loadPlugin(File)}.
	 * If the plugin already existed in the target folder, it will not be overwritten.
	 * 
	 * @param pluginLocation place download the jar/zip file from 
	 * @param pluginDirectory place to store the plugin. This will be eiter the default plugin directory or the sessions plugin dir.
	 */
	public static void installPlugin(URL pluginLocation, File pluginDirectory) throws IOException{
		String[] path = pluginLocation.getPath().split("/");
		String jarName = pluginLocation.getPath().split("/")[path.length-1];
		File dest = new File(pluginDirectory, jarName);
				
		if (dest.exists()){
			loadPlugin(dest);			
		} else {
			if (confirmPluginInstall(jarName)){
				FileUtils.copyURLToFile(pluginLocation, dest);
				loadPlugin(dest);
			}
		}
		
	}
	/**
	 * Gets a plugin location, copies the plugin to the given destination directory and registers/activates the new plugin with 
	 * {@link #loadPlugin(File)}.
	 * If the plugin already existed in the target folder, it will not be overwritten.
	 * 
	 * @param pluginLocation place to copy the jar/zip file from 
	 * @param pluginDirectory place to store the plugin. This will be eiter the default plugin directory or the sessions plugin dir.
	 */
	public static void installPlugin(File pluginLocation, File pluginDirectory) throws IOException{
		String[] path = pluginLocation.getPath().split("/");
		String jarName = pluginLocation.getPath().split("/")[path.length-1];
		File dest = new File(pluginDirectory, jarName);
				
		if (dest.exists()){
			loadPlugin(dest);			
		} else {
			if (confirmPluginInstall(jarName)){
				FileUtils.copyFile(pluginLocation, dest);
				loadPlugin(dest);
			}
		}
		
	}
	
	/**
	 * Warn the user that extra code will be loaded and used and asks for confirmation.
	 * 
	 * @return true if the user accepts the plugin for the first time.
	 */
	private static boolean confirmPluginInstall(String jarName){
		int answer = JOptionPane.showConfirmDialog(model.getGUIManager().getParent(), 
				MessageManager.formatMessage("pluginloader.confirm_install", new Object[]{jarName}),
				MessageManager.getString("pluginloader.confirm_install_title"),
				JOptionPane.YES_NO_OPTION);
		return answer==JOptionPane.YES_OPTION;
	}

}
