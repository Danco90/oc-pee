package com.pino.project.ocpairprogramming.java8.ocp.chapter7.concurrency.workerthreads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Topic  : Waiting for the termination of some tasks, such as a never ending one, with a simple shutdown() and awaitTermination()
 * Details: ExecutorService will not be automatically destroyed when there is not task to process. It will stay alive and wait for new tasks to do.
 * 			Therefore, the ExecutorService interface provides 3 methods for controlling the termination of tasks submitted to executor: 
 * 			- void shutdown() initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks 
 * 			             will be accepted. This method does not wait for previously submitted tasks (but not started executing) to complete execution.
 * 			
 * 			- List<Runnable> shutdownNow(); attempts to stop all actively executing tasks, halts(blocks) the processing of waiting tasks, 
 * 			  and returns a list of the tasks that were awaiting execution.This method does not wait for actively executing tasks to terminate 
 * 			  and tries to stop them forcefully. There are no guarantees beyond best-effort attempts to stop processing actively executing tasks. 
 * 			  This implementation cancels tasks via Thread.interrupt(), so any task that fails to respond to interrupts may never terminate.
 * 			
 * 			- awaitTermination(long timeout, TimeUnit unit) blocks until all tasks have completed execution after a shutdown request, 
 * 			  or the timeout occurs, or the current thread is interrupted, whichever happens first.
 * @author matteodaniele
 *
 */
public class WaitingForNeverendingTaskToFinishWithShutDownAndAwaitTermination extends BaseShuttingDownUsecase{

	public static void main(String[] args) throws InterruptedException {
		ExecutorService service = null;
		try{
			service = Executors.newSingleThreadExecutor();
			// Add tasks to the thread executor
			//1- execute Runnable task
			service.execute(() -> System.out.println("Executing : Runnable-task-1"));//Submits and attemps to exec a Runnable task at some point in the future
			
			//2- submit Runnable task
			Future rawResult = service.submit(() -> System.out.println("Executing and returning: Runnable-task-2"));//Submits and attemps to exec a Runnable task in the future and returns a Future representing the task
			try { System.out.println("Runnable-task-2 result is : " + rawResult.get());
			} catch (ExecutionException e1) { e1.printStackTrace();}
			
			//3- submit callable task
			Future<?> genResult = service.submit(() -> "Executing and returning: Callable-task-1");//Submits and attempts to exec a Runnable task in the future and returns a Future representing the task
			try { System.out.println("Runnable-task-3 result is : " + (String) genResult.get()/*.toString()*/);
			} catch (ExecutionException e) { e.printStackTrace(); }
			
			//4- invoke All Callable tasks 
			List<Callable<String>> tasksList = new ArrayList<>();
        		tasksList.add(() -> "Exec & retur : Callable-task-2" );
        		tasksList.add(() -> "Exec & retur : Callable-task-3" );
        		tasksList.add(() -> "Exec & retur : Callable-task-4" );
			List<Future<String>> genResultList = service.invokeAll(tasksList);
			genResultList.forEach((Future<String> s) -> {//Consumer<T>
				try { System.out.println(s.get());//get() throws ExecutionException
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			});
			
			//5- invoke Any Callable tasks
			List<Callable<String>> tasksList2 = new ArrayList<>();
	    		tasksList2.add(() -> "Exec & retur : Callable-task-6" );
	    		tasksList2.add(() -> "Exec & retur : Callable-task-7" );
			try { String genResult2 = service.invokeAny(tasksList2);//it throws Execution Exception
					System.out.println(genResult2);
			} catch (ExecutionException e1) { e1.printStackTrace();
			}
			
			//6- invoke a never ending task Runnable tasks
			service.execute(() ->  {while(true);});//Submits and attemps to exec a Runnable task at some point in the future
			
		} finally {
			shutdown(service);//it starts the process of shutting down only the task which have started. If fails, it throws an InterruptedException
		}
		awaitTermination(service, 6, TimeUnit.SECONDS);
	}

}
