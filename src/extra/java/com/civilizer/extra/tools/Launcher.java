package com.civilizer.extra.tools;

import java.io.File;
import java.io.FileFilter;
import java.text.MessageFormat;
import java.util.Arrays;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;

import com.civilizer.utils.FsUtil;

public final class Launcher {
    
    private final Server server;
    private final int port;
    
    private enum LogType {
        INFO,
        WARN,
        ERROR,
    }
    
    public static void main(String[] args) {
        try {
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
        l(LogType.INFO, "Civilizer is running... acess to http://localhost:"+port+"/civilizer/app/home");
        server.join();
    }

}