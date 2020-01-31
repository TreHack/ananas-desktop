package org.ananas.server;

import static spark.Spark.*;

import org.ananas.runner.core.extension.LocalExtensionRepository;
import org.ananas.runner.misc.BackgroundApiService;
import org.ananas.runner.misc.HomeManager;

/** REST API Routes */
public class RestApiRoutes {

  public static void initRestApi(String[] args) {
    int maxThreads = 8;
    int minThreads = 2;
    int timeOutMillis = 30000;
    threadPool(maxThreads, minThreads, timeOutMillis);

    String address = "127.0.0.1";
    int port = 3003;
    String extensionRepo = HomeManager.getHomeExtensionPath();
    if (args.length != 0) {
      address = args[0];
      if (args.length >= 2) {
        port = Integer.valueOf(args[1]);
      }
      // load extensions
      if (args.length >= 3) {
        extensionRepo = args[3];
      }
    }

    // set default repository for the server
    // NOTE: only do it once! Use LocalExtensionRepository.getDefault() to ref it
    LocalExtensionRepository.setDefaultRepository(extensionRepo);
    LocalExtensionRepository.getDefault().load();

    ipAddress(address);
    port(port);

    // CORS
    options(
        "/*",
        (request, response) -> {
          String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
          if (accessControlRequestHeaders != null) {
            response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
          }

          String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
          if (accessControlRequestMethod != null) {
            response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
          }

          return "OK";
        });

    before(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Request-Method", "POST");
          response.header("Access-Control-Allow-Headers", "");
          // Note: this may or may not be necessary in your particular application
          response.type("application/json");
        });

    // Endpoints
    post("/v1/:id/paginate", HttpHandler.paginateStep);

    post("/v1/:projectid/dag/test", HttpHandler.testDag);
    post("/v1/:projectid/dag/run", HttpHandler.runDag);

    get("/v1/jobs/:jobid/poll", HttpHandler.pollJob);
    get("/v1/jobs/", HttpHandler.listJobs);
    get("/v1/goal/:goalid/jobs", HttpHandler.getJobsByGoal);
    post("/v1/jobs/:id/cancel", HttpHandler.cancelPipeline);

    get("/v1/data/:jobid/:stepid", HttpHandler.dataView);

    // Exception handler
    exception(Exception.class, HttpHandler.error);

    // background services
    BackgroundApiService backgroundService = new BackgroundApiService();
    Thread deamonthread = new Thread(backgroundService);
    deamonthread.setDaemon(true);
    deamonthread.start();

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                backgroundService.doStop();
                backgroundService.cancelRunningJobs();
              }
            });
  }
}