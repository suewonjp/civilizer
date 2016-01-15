package com.civilizer.extra.tools;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
//import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.webapp.WebAppContext;

// [IMPORTANT!]
// To run this embedded Jetty server on Eclipse, the following procedure is required.
//
// 1. First, Build the target WAR package.
//      - run 'mvn clean package' from the command line.
//      - Then, refresh the project. (press F5 with the project selected)
// 2. On Eclipse, go to 'Run > Debug Configurations... > Java Application' 
//    and click the toolbar button for 'New launch configuration'.
//      - For Main tab > Main class input box:
//          Add 'com.civilizer.extra.tools.Launcher'
//      - (Optional) For Arguments tab > VM Arguments text box:
//          add ' -ea -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog -Dorg.eclipse.jetty.LEVEL=INFO '
//      - For Classpath tab:
//          Remove all classpath except Bootstrap Entries.
//          And then add the path 'extra/lib/jetty-runner.jar'
//          Also add the path 'target/extra'
//      - For Source tab:
//          Click Add > Java Project, then add 'civilizer'
// 3. Note that this server launches the webapp located at 'target/civilizer-1.0.0.CI-SNAPSHOT'.
//    If you modified other parts of the source than this class,
//    then run 'mvn clean package -Dmaven.test.skip=true' to confirm the effect of the modification.
//    This embedded server will ignore any outcome ( other than this class ) built by Eclipse.

public final class Launcher {
    
    private static final String PORT              = "civilizer.port";
    private static final String STATUS            = "civilizer.status";
    private static final String BROWSE_AT_STARTUP = "civilizer.browse_at_startup";
    private static final String STARTING          = "Starting Civilizer...";
    private static final String RUNNING           = "Civilizer is running...";
    
    private static Font fontForIcon;
    
    private final Server server;
    private final int port;
    
    private enum LogType {
        INFO,
        WARN,
        ERROR,
    }
    
    public static final String[] plusConfigurationClasses = new String[] {
        org.eclipse.jetty.webapp.WebInfConfiguration.class.getCanonicalName(),
        org.eclipse.jetty.webapp.WebXmlConfiguration.class.getCanonicalName(),
        org.eclipse.jetty.webapp.MetaInfConfiguration.class.getCanonicalName(),
        org.eclipse.jetty.webapp.FragmentConfiguration.class.getCanonicalName(),
        org.eclipse.jetty.plus.webapp.EnvConfiguration.class.getCanonicalName(),
        org.eclipse.jetty.plus.webapp.PlusConfiguration.class.getCanonicalName(),
        org.eclipse.jetty.annotations.AnnotationConfiguration.class.getCanonicalName(),
        org.eclipse.jetty.webapp.JettyWebXmlConfiguration.class.getCanonicalName()
        };
    
    public static void main(String[] args) {
        try {
            System.setProperty(STATUS, STARTING);
            System.setProperty(PORT, "");
            
            final File warFolder = findWarFolder();
            assert warFolder != null && warFolder.isDirectory();
            
            setupSystemTray(warFolder);
            
            String home = "";
            int port = 8080;
            for (int i=0;i<args.length; ++i) {
                switch (args[i]) {
                case "--port":
                    try {
                        port = Integer.parseInt(args[++i]);
                        if (port <= 0)
                            throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        l(LogType.ERROR, "'" + port + "' is not a valid port number. exiting...");
                        new Error(e);
                    }
                    break;
                case "--home":
                    if (i < args.length - 1)
                        home = args[++i];
                    break;
                }
            }
            
            new Launcher(port, home).startServer(warFolder);
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
            l(LogType.ERROR, "An unhandled exception triggered. exiting...");
            new Error(e);
        }
    }

    private Launcher(int port, String home) {
        assert 0 < port && port <= 0xffff;
        server = new Server();
        assert server != null;
        this.port = port;
        System.setProperty("civilizer.private_home_path", home);
    }
    
    private static String getCvzUrl() {
        final String portStr = System.getProperty(PORT);
        assert portStr.isEmpty() == false;
        int port = 0;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            throw new Error(e);
        }
        return "http://localhost:" + port + "/civilizer/app/home";
    }
    
    private static File getFileOrFolderFromPattern(String folderToSearch, String pattern, boolean targetIsFolder) {
        if (folderToSearch == null || folderToSearch.isEmpty())
            folderToSearch = System.getProperty("user.dir");
        final File parentFolder = new File(folderToSearch);
        if (parentFolder.isDirectory() == false)
            return null;
        final Pattern ptn = Pattern.compile(pattern);
        for (File f : parentFolder.listFiles()) {
            if (targetIsFolder && !f.isDirectory())
                continue;
            if (!targetIsFolder && !f.isFile())
                continue;
            if (ptn.matcher(f.getName()).matches())
                return f;
        }
        return null;
    }
    
    private static File findWarFolder() {
        File output = null;
        // [NOTE] We need to care of this when we are no longer dependent to Maven...
        if (exists("pom.xml") && exists("target")) {
            // The current working directory is the root of the source package.
            // This instance might've been launched from Eclipse. (development situation)
            output = getFileOrFolderFromPattern("target", "civilizer-.*", true);
        }
        else {
            // This instance might've been launched from a command line. (production situation)
            output = getFileOrFolderFromPattern(null, "civilizer", true);
        }
        
        return output;
    }
    
    private void prependHandler (Handler handler, HandlerCollection handlers) {
        if (handler == null || handlers == null)
            return;

       Handler[] existing = handlers.getChildHandlers();
       Handler[] children = new Handler[existing.length + 1];
       children[0] = handler;
       System.arraycopy(existing, 0, children, 1, existing.length);
       handlers.setHandlers(children);
    }

    private WebAppContext setupWebAppContext(File warFolder) {
        if (warFolder.isDirectory() == false)
            return null;
        final String absWarPath = warFolder.getAbsolutePath();
        if (exists(absWarPath + "/WEB-INF/web.xml") == false
                || exists(absWarPath + "/WEB-INF/classes") == false
                || exists(absWarPath + "/WEB-INF/lib") == false
                )
            return null;
        
        HandlerCollection handlers = (HandlerCollection) server.getChildHandlerByClass(HandlerCollection.class);
        if (handlers == null) {
            handlers = new HandlerCollection();
            server.setHandler(handlers);
        }

        ContextHandlerCollection contexts = (ContextHandlerCollection) handlers.getChildHandlerByClass(ContextHandlerCollection.class);
        if (contexts == null) {
            contexts = new ContextHandlerCollection();
            prependHandler(contexts, handlers);
        }
        
        //ensure a DefaultHandler is present
        if (handlers.getChildHandlerByClass(DefaultHandler.class) == null) {
            handlers.addHandler(new DefaultHandler());
        }

        //ensure a log handler is present
        RequestLogHandler logHandler = (RequestLogHandler) handlers.getChildHandlerByClass(RequestLogHandler.class);
        if (logHandler == null) {
            logHandler = new RequestLogHandler();
            handlers.addHandler(logHandler);
        }
        
        //check a connector is configured to listen on
        Connector[] connectors = server.getConnectors();
        if (connectors == null || connectors.length == 0) {
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(port);
            server.addConnector(connector);
        } 
        
        final WebAppContext waCtxt = new WebAppContext(contexts, warFolder.toString(), "/civilizer");
        
        waCtxt.setConfigurationClasses(plusConfigurationClasses);
        
        return waCtxt;
    }
    
    private static String getResourcePathFromJarFile(final File file, final Pattern pattern){
        try (ZipFile zf = new ZipFile(file);){
            final Enumeration<?> e = zf.entries();
            while(e.hasMoreElements()){
                final ZipEntry ze = (ZipEntry) e.nextElement();
                final String fileName = ze.getName();
                if(pattern.matcher(fileName).matches())
                    return fileName;
            }
        } catch(final IOException e){
            e.printStackTrace();
            throw new Error(e);
        }
        return "";
    }
    
    private static Font createFont(File warFolder) {
        l(LogType.INFO, warFolder.toString());
        final File tgtJar = getFileOrFolderFromPattern(warFolder+"/WEB-INF/lib", ".*primefaces.*\\.jar", false);
        l(LogType.INFO, tgtJar.toString());
        assert tgtJar != null && tgtJar.isFile();
        final String fontPath = getResourcePathFromJarFile(tgtJar, Pattern.compile(".*/fontawesome-webfont\\.ttf"));
        assert fontPath.isEmpty() == false;

        URL[] urls = null;
        try {
            urls = new URL[]{ tgtJar.toURI().toURL() };
        } catch (MalformedURLException e) {
            e.printStackTrace();
            assert false;
            throw new Error(e);
        }
        
        try (final URLClassLoader cl = new URLClassLoader(urls);) {
            final InputStream is = cl.getResourceAsStream(fontPath);
            assert is != null;
            Font font;
            try {
                font = Font.createFont(Font.TRUETYPE_FONT, is);
            } catch (FontFormatException e) {
                e.printStackTrace();
                assert false;
                throw new Error(e);
            } catch (IOException e) {
                e.printStackTrace();
                assert false;
                throw new Error(e);
            }
            return font.deriveFont(Font.PLAIN, 24f);
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
            throw new Error(e);
        }
    }

    // Copied from https://github.com/roysix/font-awesome-to-image
    private static Point calcDrawPoint(Font font, String icon, int size, Graphics2D graphics) {
        int center = size / 2; // Center X and Center Y are the same
        Rectangle stringBounds = graphics.getFontMetrics().getStringBounds(icon, graphics).getBounds();
        Rectangle visualBounds = font.createGlyphVector(graphics.getFontRenderContext(), icon).getVisualBounds().getBounds();
        return new Point(center - stringBounds.width / 2, center - visualBounds.height / 2 - visualBounds.y);
    }

    private static Image createFontIcon(Font font, String code, Color clr) {
        final int iconSize = 24;
        final BufferedImage img = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = img.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setColor(clr);
        graphics.setFont(font);
        final String icon = code;
        final Point pt = calcDrawPoint(font, icon, iconSize, graphics);
        graphics.drawString(icon, pt.x, pt.y);
        graphics.dispose();
        return img;
    }
    
    private static boolean systemTraySupported() {
        // [Note] Java's standard system tray is not well supported on some of Linux systems;
        // (at least, on Linux Mint as of 2015/09/20)
        // So we don't display the system tray icon on Linux (for now).
        return (SystemTray.isSupported() && !System.getProperty("os.name").toLowerCase().contains("linux"));
    }
    
    private static void setupSystemTray(File warFolder) {
        if (systemTraySupported() == false)
            return;
        
        fontForIcon = createFont(warFolder);
        final Image img = createFontIcon(fontForIcon, "\uf19c", new Color(0xff, 0x0, 0x0));
        
        EventQueue.invokeLater(
                new Runnable() {
                    public void run() {
                        final SystemTray tray = SystemTray.getSystemTray();
                        assert tray != null;
                        assert img != null;
                        
                        PopupMenu popup = new PopupMenu();
                        
                        final TrayIcon trayIcon = new TrayIcon(img, STARTING, popup);
                        trayIcon.setImageAutoSize(true);
                        
                        MenuItem item;
                        
                        item = new MenuItem("Shutdown");
                        item.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                tray.remove(trayIcon);
                                System.exit(0);
                            }
                        });
                        popup.add(item);
                        
                        try {
                            tray.add(trayIcon);
                            
                            trayIcon.displayMessage("Staring Civilizer...", "Please wait...", MessageType.INFO);
                        } catch (AWTException e) {
                            e.printStackTrace();
                            throw new Error(e);
                        }
                    }
                }
                ); // EventQueue.invokeLater()
    }

    private static void updateSystemTray() {
        if (systemTraySupported() == false)
            return;
        
        // We change appearance of the system tray icon to notify that the server is ready.
        // Also add extra menus.
        for (TrayIcon icon : SystemTray.getSystemTray().getTrayIcons()) {
            if (icon.getToolTip().equals(STARTING)) {
                assert fontForIcon != null;
                final Image img = createFontIcon(fontForIcon, "\uf19c", new Color(255,222,173));
                icon.setImage(img);
                icon.setToolTip(RUNNING);

                MenuItem item = new MenuItem("Browse");
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        openBrowser();
                    }
                });
                icon.getPopupMenu().insert(item, 0);
                

                icon.displayMessage(null, "Civilizer is ready...", MessageType.NONE);
                
                break;
            }
        }
    }
    
    private static void openBrowser() {
        if (System.getProperty(PORT).isEmpty() || ! System.getProperty(STATUS).equals(RUNNING))
            return;
        final String url = getCvzUrl();
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }
    }
    
    private static void l(LogType type, String msg) {
        System.out.println(MessageFormat.format("{0} : [{1}] {2}", Launcher.class.getSimpleName(), type.toString(), msg));
    }
    
    private static boolean exists(String path) {
        return new File(path).exists();
    }
    
    private static boolean isSysPropTrue(String key) {
        String v = System.getProperty(key);
        if (v == null)
            return false;
        v = v.toLowerCase();
        return v.equals("true") || v.equals("yes") || v.equals("on");
    }
    
    private void startServer(File warFolder) throws Exception {
        assert server != null;
        
        org.eclipse.jetty.util.resource.Resource.setDefaultUseCaches(false);
        
        final WebAppContext waCtxt = setupWebAppContext(warFolder);
        assert waCtxt != null;
        server.setHandler(waCtxt);
        server.setStopAtShutdown(true);
        server.start();
        
        System.setProperty(STATUS, RUNNING);
        System.setProperty(PORT, new Integer(port).toString());
        assert System.getProperty(PORT).equals(new Integer(port).toString());
        l(LogType.INFO, "Civilizer is running... access to " + getCvzUrl());
        updateSystemTray();
        if (isSysPropTrue(BROWSE_AT_STARTUP)) {
            openBrowser();
        }
        
        server.join();
    }

}