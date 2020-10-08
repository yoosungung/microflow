package io.berry.microflow.service;

import io.berry.microflow.service.impl.UserManagerServiceImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
public interface  UserManagerService {

	static UserManagerService create(JsonObject conf) {
		return new UserManagerServiceImpl(conf);
	}
				
	void getUserList(String username,
			OperationRequest context, 
		    Handler<AsyncResult<OperationResponse>> resultHandler);
			
	void createUser(String username, 
			JsonObject body,
			OperationRequest context, 
		    Handler<AsyncResult<OperationResponse>> resultHandler);
	
	void getUser(String username,
			OperationRequest context, 
		    Handler<AsyncResult<OperationResponse>> resultHandler);
	
	void updateUser(String username, 
			JsonObject body,
			OperationRequest context, 
		    Handler<AsyncResult<OperationResponse>> resultHandler);
	
	void deleteUser(String username,
			OperationRequest context, 
		    Handler<AsyncResult<OperationResponse>> resultHandler);
	
}
