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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//import javax.swing.ImageIcon;
//import javax.swing.JMenuItem;
//import javax.swing.JPopupMenu;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;

import com.civilizer.config.Configurator;
import com.civilizer.utils.FsUtil;

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
    
    public static void main(String[] args) {
        try {
            System.setProperty(STATUS, STARTING);
            System.setProperty(PORT, "");
            
            setupSystemTray();
            
            int port = 8080;
            Arrays.sort(args);
            final int iii = Arrays.binarySearch(args, "--port");
            if (-1 < iii && iii < args.length-1) {
                try {
                    port = Integer.parseInt(args[iii + 1]);
                    if (port <= 0)
                        throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    l(LogType.ERROR, "'" + port + "' is not a valid port number. exiting...");
                    System.exit(1);
                }
            }
            new Launcher(port).startServer();
        } catch (Exception e) {
            l(LogType.ERROR, "An unhandled exception triggered. exiting...");
            e.printStackTrace();
            System.exit(1);
        }
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
    
    private static String getFullJarPath(final Pattern pattern){
        for(final String element : System.getProperty("java.class.path", ".").split("[:;]")){
            if (pattern.matcher(element).matches())
                return element;
        }
        return null;
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
    
    private static Font createFont() {
        final String tgtJarPath = getFullJarPath(Pattern.compile(".*primefaces.*\\.jar"));
        final String fontPath = getResourcePathFromJarFile(new File(tgtJarPath), Pattern.compile(".*/fontawesome-webfont\\.ttf"));
        assert fontPath.isEmpty() == false;
        final InputStream is = Launcher.class.getClassLoader().getResourceAsStream(fontPath);
        assert is != null;
        Font font;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (FontFormatException e) {
            e.printStackTrace();
            throw new Error(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new Error(e);
        }
        return font.deriveFont(Font.PLAIN, 24f);
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
    
    private static void setupSystemTray() {
        if (SystemTray.isSupported() == false)
            return;
        
        fontForIcon = createFont();
        final Image img = createFontIcon(fontForIcon, "\uf19c", new Color(0xff, 0x0, 0x0));
        
        EventQueue.invokeLater(
            new Runnable() {
                public void run() {
                    final SystemTray tray = SystemTray.getSystemTray();
                    assert tray != null;
                    assert img != null;
                    
//                    final TrayIcon trayIcon = new TrayIcon(img, STARTING, null);
//                    trayIcon.setImageAutoSize(true);
//
//                    final JPopupMenu jpopup = new JPopupMenu();
//
//                    JMenuItem javaCupMI = new JMenuItem("Example", new ImageIcon("javacup.gif"));
//                    jpopup.add(javaCupMI);
//
//                    jpopup.addSeparator();
//
//                    JMenuItem exitMI = new JMenuItem("Exit");
//                    jpopup.add(exitMI);
//                    
//                    trayIcon.addMouseListener(new MouseAdapter() {
//                        public void mouseReleased(MouseEvent e) {
//                            if (e.getButton() == MouseEvent.BUTTON1) {
//                                jpopup.setLocation(e.getX(), e.getY());
//                                jpopup.setInvoker(null);
//                                jpopup.setVisible(true);
//                            }
//                        }
//                    });
                    
                    PopupMenu popup = new PopupMenu();
                    
                    final TrayIcon trayIcon = new TrayIcon(img, STARTING, popup);
                    trayIcon.setImageAutoSize(true);

                    MenuItem item;
                    
                    item = new MenuItem("\u2716 Shutdown");
                    item.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            tray.remove(trayIcon);
                            System.exit(0);
                        }
                    });
                    popup.add(item);
                    
                    try {
                        tray.add(trayIcon);
                    } catch (AWTException e) {
                        e.printStackTrace();
                        throw new Error(e);
                    }
                }
            }
        ); // EventQueue.invokeLater()
    }
    
    private static void updateSystemTray() {
        // We change appearance of the system tray icon to notify that the server is ready.
        // Also add extra menus.
        if (SystemTray.isSupported() == false)
            return;
        for (TrayIcon icon : SystemTray.getSystemTray().getTrayIcons()) {
            if (icon.getToolTip().equals(STARTING)) {
                assert fontForIcon != null;
                final Image img = createFontIcon(fontForIcon, "\uf19c", new Color(0xf0, 0xff, 0xff));
                icon.setImage(img);
                icon.setToolTip(RUNNING);

                MenuItem item = new MenuItem("\u270d Browse");
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        openBrowser();
                    }
                });
                icon.getPopupMenu().insert(item, 0);
                
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

    private Launcher(int port) {
        assert 0 < port && port <= 0xffff;
        server = new Server(port);
        assert server != null;
        this.port = port;
    }
    
    private boolean setupWebAppContextForDevelopment(WebAppContext waCtxt) {
        if (!FsUtil.exists("pom.xml"))
            return false;
        final String webAppDir = FsUtil.concatPath("src", "main", "webapp");
        final String tgtDir = "target";
        final String webXmlFile = FsUtil.concatPath(webAppDir, "WEB-INF", "web.xml");
        if (!FsUtil.exists(webAppDir) || !FsUtil.exists(tgtDir) || !FsUtil.exists(webXmlFile))
            return false;
        waCtxt.setBaseResource(new ResourceCollection(
                new String[] { webAppDir, tgtDir }
                ));
        waCtxt.setResourceAlias("/WEB-INF/classes/", "/classes/");
        waCtxt.setDescriptor(webXmlFile);
        l(LogType.INFO, "Running under the development environment...");
        return true;
    }

    private boolean setupWebAppContextForProduction(WebAppContext waCtxt) {
        final File usrDir = new File(System.getProperty("user.dir"));
        final File[] pkgs = usrDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return (pathname.isDirectory() && pathname.getName().startsWith("civilizer"));
            }
        });
        for (File pkg : pkgs) {
            final String pkgPath = pkg.getAbsolutePath();
            if (FsUtil.exists(pkgPath, "WEB-INF", "web.xml") == false
                    || FsUtil.exists(pkgPath, "WEB-INF", "classes") == false
                    || FsUtil.exists(pkgPath, "WEB-INF", "lib") == false
                    )
                continue;
            waCtxt.setWar(pkgPath);
            return true;
        }
        return false;
    }
    
    private WebAppContext setupWebAppContext() {
        final WebAppContext waCtxt = new WebAppContext();
        waCtxt.setContextPath("/civilizer");
        if (! setupWebAppContextForProduction(waCtxt))
            if (! setupWebAppContextForDevelopment(waCtxt))
                return null;
        return waCtxt;
    }
    
    private void startServer() throws Exception {
        assert server != null;
        
        final WebAppContext waCtxt = setupWebAppContext();
        assert waCtxt != null;
        server.setHandler(waCtxt);
        server.setStopAtShutdown(true);        
        server.start();
        System.setProperty(STATUS, RUNNING);
        System.setProperty(PORT, new Integer(port).toString());
        assert System.getProperty(PORT).equals(new Integer(port).toString());
        l(LogType.INFO, "Civilizer is running... access to " + getCvzUrl());
        updateSystemTray();
        if (Configurator.isTrue(BROWSE_AT_STARTUP)) {
            openBrowser();
        }
        server.join();
    }

}