package com.civilizer.extra.tools;

import java.io.File;
import java.io.FileFilter;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;

import com.civilizer.utils.FsUtil;

public final class Launcher {
    
    private final Server server;
    
    public static void main(String[] args) {
        try {
            new Launcher(0).startServer();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private Launcher(int port) {
        assert 0 <= port && port <= 0xffff;
        server = new Server(port == 0 ? 8080 : port);
        assert server != null;
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
        if (! setupWebAppContextForDevelopment(waCtxt))
            if (! setupWebAppContextForProduction(waCtxt))
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
        server.join();
    }

}