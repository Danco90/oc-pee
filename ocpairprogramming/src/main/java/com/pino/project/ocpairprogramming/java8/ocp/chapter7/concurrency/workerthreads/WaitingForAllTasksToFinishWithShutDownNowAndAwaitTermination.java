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
 * Usecase : Waiting for the termination of some tasks, with shutdownNow() and awaitTermination()
 * Details : ExecutorService will not be automatically destroyed when there is not task to process. It will stay alive and wait for new tasks to do.
 * 			 Therefore, the ExecutorService interface provides 3 methods for controlling the termination of tasks submitted to executor: 
 * 			
 * 			 - void shutdown() initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks 
 * 			             will be accepted. This method does not wait for previously submitted tasks (but not started executing) to complete execution.
 * 			
 * 			 - List<Runnable> shutdownNow(); attempts to stop all actively executing tasks, halts(blocks) the processing of waiting tasks, 
 * 			  and returns a list of the tasks that were awaiting execution.This method does not wait for actively executing tasks to terminate 
 * 			  and tries to stop them forcefully. There are no guarantees beyond best-effort attempts to stop processing actively executing tasks. 
 * 			  This implementation cancels tasks via Thread.interrupt(), so any task that fails to respond to interrupts may never terminate.
 * 			
 * 			 - awaitTermination(long timeout, TimeUnit unit) blocks until all tasks have completed execution after a shutdown request, 
 * 			  or the timeout occurs, or the current thread is interrupted, whichever happens first.
 * 
 * @author : matteodaniele
 *
 */
public class WaitingForAllTasksToFinishWithShutDownNowAndAwaitTermination extends BaseShuttingDownUsecase {

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
			
			//3- submit Callable task
			Future<?> genResult = service.submit(() -> "Executing and returning: Callable-task-1");//Submits and attempts to exec a Runnable task in the future and returns a Future representing the task
			try { System.out.println("Callable-task-1 result is : " + (String) genResult.get()/*.toString()*/);
			} catch (ExecutionException e) { e.printStackTrace(); }
			
			//4- invoke All Callable tasks 
			List<Callable<String>> tasksList = new ArrayList<>();
        		tasksList.add(() -> "Exec. & retur  Callable-task-2 (synchronously)" );
        		tasksList.add(() -> "Exec. & retur  Callable-task-3 (synchronously)" );
        		tasksList.add(() -> "Exec. & retur  Callable-task-4 (synchronously)" );
			List<Future<String>> genResultList = service.invokeAll(tasksList);
			genResultList.forEach((Future<String> s) -> {//Consumer<T>
				try { System.out.println(s.get());//get() throws ExecutionException
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			});
			
			//5- invoke Any Callable tasks
			List<Callable<String>> tasksList2 = new ArrayList<>();
	    		tasksList2.add(() -> "Exec. retur  Callable-task-6 (asynchronously)" );
	    		tasksList2.add(() -> "Exec. retur  Callable-task-7 (asynchronously)" );
	    		tasksList2.add(() -> "Exec. retur  Callable-task-7 (asynchronously)" );
			try { String genResult2 = service.invokeAny(tasksList2);//it throws Execution Exception
				  System.out.println(genResult2);
			} catch (ExecutionException e1) { e1.printStackTrace(); }
			
			//6- invoke a Runnable task which lasts 5 seconds
			service.execute(() ->  { try{Thread.sleep(5000);} catch(InterruptedException e) { } });
		} finally {
				List<Runnable> taskList = shutdownNow(service);//attempts to stop all executing tasks via Thread.interrupt(), blocks the process of waiting for the tasks
				//and return a list of the only RUNNABLE tasks that were supposed to be executed, but eventually they didn't started.
				//NB: any task that fails to respond to interrupts may never terminate.
				System.out.println(taskList.size()+ " Tasks not started " +taskList);	
		}
		awaitTermination(service, 6, TimeUnit.SECONDS);//it blocks until one of the following condition happens first
		
	}

}
