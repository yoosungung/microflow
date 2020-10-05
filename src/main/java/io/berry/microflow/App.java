package io.berry.microflow;

import java.util.HashMap;
import java.util.Map;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.sql.SQLClient;

public class App {
	
	public static Map<String, SQLClient> dbcpools = new HashMap<>();

	public static void main(String[] args) {
		VertxOptions vxOptions = new VertxOptions().setBlockedThreadCheckInterval(200000000);
		Vertx vertx = Vertx.vertx(vxOptions);
	    vertx.deployVerticle(MainVerticle.class.getName());
	}

}
