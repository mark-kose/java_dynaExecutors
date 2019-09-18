package io.mark.java_examples.Executors.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class JavaExecutorGRPCClient
{
	    public static void main( String[] args ) throws Exception
		        {
				  final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8080")
					          .usePlaintext(true)
						          .build();

				   try {
					   ExecutionServiceGrpc.ExecutionServiceBlockingStub stub = ExecutionServiceGrpc.newBlockingStub(channel);

					   ExecutionRequest request =
							   ExecutionRequest.newBuilder()
									   .setJarLocation(args[0])
									   .setMethod(args[1])
									   .setData(args[2])
									   .build();
					   ExecutionResponse response =
							   stub.execute(request);

					   System.out.println(response);
				   }catch (io.grpc.StatusRuntimeException e) {
				   	System.out.println("Error occured. Status:"+e.getStatus().getCode()+" . Description:"+e.getStatus().getDescription());


				   }

						  channel.shutdownNow();
						      }
}
