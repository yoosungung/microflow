package io.berry.microflow;

import java.util.HashMap;
import java.util.Map;

import io.berry.microflow.service.FlowManagerService;
import io.berry.microflow.service.NodeManagerService;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.serviceproxy.ServiceBinder;

public class MainVerticle extends AbstractVerticle {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);
	
	JsonObject config;
	Map<String, MessageConsumer<JsonObject>> service_consumers = new HashMap<>();
	HttpServer server;
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		LOGGER.info("verticle: start");
		Future<Void> fur_config = loadConfig();
		fur_config.compose(v -> {
			Future<Void> jdbcFuture = creatDbPools(config.getJsonObject("jdbc_pools"));
			try {
				startService(config.getJsonObject("services"));
			} catch(Exception e) {
				LOGGER.error("service: ", e);
			}
			return jdbcFuture;
		}).compose(v -> {
			return startHttpServer(config.getJsonObject("httpserver"));
		});
	}
	
	private Future<Void> loadConfig() {
		
		Promise<Void> promise = Promise.promise();
		LOGGER.info("config: start");
		ConfigStoreOptions fileStore = new ConfigStoreOptions()
				.setType("file")
				.setFormat("json")
				.setOptional(true)
				.setConfig(new JsonObject().put("path", "flowconfig.json"));
		ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore);
		LOGGER.info("config: option="+options.toJson());
		ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
		retriever.getConfig(ar -> {
			if (ar.failed()) {
				LOGGER.error("config: ", ar.cause());
				promise.fail(ar.cause());
			} else {
			    config = ar.result();
			    LOGGER.info("config: read");
			    promise.complete();
			}
		});
		
		return promise.future();
	}

	private Future<Void> creatDbPools(JsonObject conf) {
		
		Promise<Void> promise = Promise.promise();
		
		conf.forEach( jdbc -> {
			LOGGER.info("dbpool: " + jdbc.getKey());
			SQLClient dbclient = JDBCClient.createShared(vertx, (JsonObject)(jdbc.getValue()));
			dbclient.getConnection(conn -> {
				if(conn.failed()) {
					LOGGER.error("dbpool: " + conn.cause());
					promise.fail(conn.cause());
				}
				conn.result().close();
				
				App.dbcpools.put(jdbc.getKey(), dbclient);
			});
		});
		promise.complete();
		
		return promise.future();
	}
	
	private void startService(JsonObject conf) {
		
		conf.forEach( service -> {
			LOGGER.info("service: " + service.getKey());
			JsonObject service_conf = (JsonObject)(service.getValue());

			String serviceAddress = service.getKey();
			ServiceBinder serviceBinder = new ServiceBinder(this.vertx);
			Object serviceInteface;
			MessageConsumer<JsonObject> consumer;
			
			switch(serviceAddress) {
			case "node_manager.app":
				serviceInteface = NodeManagerService.create(service_conf);
				consumer = serviceBinder
						.setAddress(serviceAddress)
						.register(NodeManagerService.class, 
								(NodeManagerService)serviceInteface);
				service_consumers.put(serviceAddress, consumer);
				LOGGER.info("service: bind=" + serviceAddress);
				break;
			case "flow_manager.app":
				serviceInteface = FlowManagerService.create(service_conf);
				consumer = serviceBinder
						.setAddress(serviceAddress)
						.register(FlowManagerService.class, 
								(FlowManagerService)serviceInteface);
				service_consumers.put(serviceAddress, consumer);
				LOGGER.info("service: bind=" + serviceAddress);
				break;
			}
		});
	}
	
	private Future<Void> startHttpServer(JsonObject conf) {
		
		Promise<Void> promise = Promise.promise();
	    
		LOGGER.info("openapi: start");
		OpenAPI3RouterFactory.create(this.vertx, "/openapi.json", api3ar -> {
	    	if (api3ar.succeeded()) {
	    		OpenAPI3RouterFactory routerFactory = api3ar.result();

				// Mount services on event bus based on extensions
				routerFactory.mountServicesFromExtensions();
				
				// Generate the router
				Router router = routerFactory.getRouter();
				router.get("/statics/*").handler(StaticHandler.create(conf.getString("statics", "./html")).setIndexPage(conf.getString("index_page", "index.html")));
		
				LOGGER.info("httpserver: start");
				server = vertx.createHttpServer(new HttpServerOptions().setPort(conf.getInteger("port", 8080)).setHost(conf.getString("host", "localhost")));
				
				server.requestHandler(router).listen(ar -> {
					if (ar.succeeded()) {
						LOGGER.info("httpserver: listen");
						promise.complete();
					}
					else {
						LOGGER.error("httpserver: ", ar.cause());
						promise.fail(ar.cause());
					}
				});
			} else {
				LOGGER.error("openapi: ", api3ar.cause());
				promise.fail(api3ar.cause());
			}
		});
	    
	    return promise.future();
	}

	@Override
	public void stop(Future<Void> stopFuture) throws Exception {
		service_consumers.forEach( (name, consumer) -> {
			consumer.unregister();
		});
		server.close();
	}
	
}
