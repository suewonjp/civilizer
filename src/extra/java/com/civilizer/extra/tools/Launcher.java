package com.civilizer.extra.tools;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;

import com.civilizer.utils.FsUtil;
import com.civilizer.utils.Pair;

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

    public Launcher(int port) {
        assert 0 <= port && port <= 0xffff;
        server = new Server(port == 0 ? 8080 : port);
        assert server != null;
    }
    
    private Pair<File, File> findAppFiles() {
        final File usrDir = new File(System.getProperty("user.dir"));
        Pair<File, File> output = findAppFiles(usrDir);
        if (output != null)
            return output;
        final File tgtDir = new File(FsUtil.concatPath(usrDir.getAbsolutePath(), "target"));
        if (tgtDir.isDirectory())
            output = findAppFiles(tgtDir);
        return output;
    }
    
    private Pair<File, File> findAppFiles(File dir) {
        assert dir.isDirectory();
        final File[] pkgs = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return (pathname.isDirectory() && pathname.getName().startsWith("civilizer"));
            }
        });
        for (File pkg : pkgs) {
            final File f = new File(FsUtil.concatPath(pkg.getAbsolutePath(), "WEB-INF", "web.xml"));
            if (f.isFile())
                return new Pair<>(pkg, f);
        }
        return null;
    }
    
    void startServer() throws Exception {
        assert server != null;
        final Pair<File, File> appFiles = findAppFiles();
        if (appFiles == null) {
            throw new FileNotFoundException("Can't find the Civilizer application folder!");
        }

        final WebAppContext waCtxt = new WebAppContext();
        waCtxt.setContextPath("/civilizer");
//        waCtxt.setResourceBase(appFiles.getFirst().getAbsolutePath());
//        waCtxt.setWar(appFiles.getFirst().getAbsolutePath());
//        waCtxt.setDescriptor(appFiles.getSecond().getAbsolutePath());
        waCtxt.setBaseResource(new ResourceCollection(
                new String[] { "src/main/webapp", "target" }
                ));
        
        waCtxt.setResourceAlias("/WEB-INF/classes/", "/classes/");
        waCtxt.setDescriptor("src/main/webapp/WEB-INF/web.xml");
        
        server.setHandler(waCtxt);
        server.setStopAtShutdown(true);        
        server.start();        
        server.join();
    }

}