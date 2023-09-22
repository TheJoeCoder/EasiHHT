package uk.radialbog9.easitill.easihht.web;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class WebServer {
    public static void initiate(int port) throws Exception {
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setName("server");

        Server server = new Server(threadPool);

        ServerConnector connector = new ServerConnector(server, 1, 1, new HttpConnectionFactory());
        connector.setPort(port);

        server.addConnector(connector);

        server.setHandler(new WebHandler());

        server.start();
    }
}
