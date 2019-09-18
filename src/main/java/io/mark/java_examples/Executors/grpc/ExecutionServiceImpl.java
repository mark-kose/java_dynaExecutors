package io.mark.java_examples.Executors.grpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import io.grpc.Status;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;


public class ExecutionServiceImpl extends  ExecutionServiceGrpc.ExecutionServiceImplBase {
	 public void execute(io.mark.java_examples.Executors.grpc.ExecutionRequest request,
			         io.grpc.stub.StreamObserver<io.mark.java_examples.Executors.grpc.ExecutionResponse> responseObserver) {
	  System.out.println(request);
	   ExecutionResponse.Builder resBuilder = ExecutionResponse.newBuilder();

	   try {
		   ClassLoader classLoader = ExecutionServiceImpl.class.getClassLoader();
		   Path jarPath = Paths.get(request.getJarLocation());
		   URLClassLoader urlClassLoader = new URLClassLoader(
				   new URL[]{jarPath.toUri().toURL()},
				   classLoader);
		   String[] tempArray=request.getMethod().split(":");
		   Class executableClass = urlClassLoader.loadClass(tempArray[0]);
		   Method method;
		   Object obj1 = executableClass.newInstance();
		   Object returnedObject = null;
		   Gson gson = new GsonBuilder()
				   .setLenient()
				   .create();
		   Method[] methods = executableClass.getDeclaredMethods();
		   String methodName = tempArray[1];
		   // System.out.println(methodName);
		   boolean foundMEthod = false;
		   for (Method m : methods) {
			   if (m.getName().equals(methodName)) {
				   foundMEthod = true;
				   Class[] pTypes = m.getParameterTypes();
				   if (pTypes.length == 1) {
					   Class pType = pTypes[0];
					   method = executableClass.getMethod(methodName, new Class[]{pType});
					   returnedObject = method.invoke(obj1, gson.fromJson(request.getData(), pType));
					   break;
				   } else if (pTypes.length == 0) {
					   method = executableClass.getMethod(methodName);
					   returnedObject = method.invoke(obj1);
					   break;
				   } else
					   continue;
			   }
		   }
		   if (foundMEthod) {
			   if (returnedObject == null) {
				   // resBuilder.setErrCode("0").build();
				   resBuilder.build();
			   } else {
				   resBuilder.setData(gson.toJson(returnedObject)).build();
			   }
			   responseObserver.onNext(resBuilder.build());
			   responseObserver.onCompleted();
		   } else {
			   responseObserver.onError(Status.UNKNOWN.augmentDescription("Could not find method").asRuntimeException());
		   }
	   }catch (JsonSyntaxException e) {
		   responseObserver.onError(Status.UNKNOWN.augmentDescription("Data provided can not be consumed by method").asRuntimeException());
		   e.printStackTrace();
	    }catch(ClassNotFoundException e){
		   responseObserver.onError(Status.UNKNOWN.augmentDescription("Could not find class").asRuntimeException());
		   e.printStackTrace();
	   }catch(NoSuchMethodException e){
		   responseObserver.onError(Status.UNKNOWN.augmentDescription("There is no such method").asRuntimeException());
		   e.printStackTrace();
	   }catch(IllegalAccessException e){
		   //resBuilder.setErrCode("Illegal access to the method");
	   }catch(InvocationTargetException e){
		   responseObserver.onError(Status.UNKNOWN.augmentDescription("Could not invoke method").asRuntimeException());
		   e.printStackTrace();
	 }catch(Exception e) {
		 responseObserver.onError(Status.UNKNOWN.augmentDescription("Exception occured").asRuntimeException());
		   e.printStackTrace();
	 }

 }

}
