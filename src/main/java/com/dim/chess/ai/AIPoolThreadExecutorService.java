package com.dim.chess.ai;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.chess.core.enums.TypePlayer;
import com.chess.core.model.Difficulty;
import com.dim.chess.ai.model.BestSimulationTurnAI;

public class AIPoolThreadExecutorService {

	public class BestSimulation{
		
		private AtomicInteger atomicInt = new AtomicInteger(0);
		private List<BestSimulationTurnAI> listBestSimulationTurnAI;

		public BestSimulation(List<BestSimulationTurnAI> listBestSimulationTurnAI) {
			this.listBestSimulationTurnAI = listBestSimulationTurnAI;
		}
		
		public synchronized BestSimulationTurnAI getNextSimulationInList(){
			return this.listBestSimulationTurnAI.get(atomicInt.getAndIncrement());
		}
		
		public AtomicInteger getAtomicInt() {
			return atomicInt;
		}
	}
	
	public void calculeList(List<BestSimulationTurnAI> listBestSimulationTurnAI, Difficulty difficulty, TypePlayer typePlayerAI) {
		BestSimulation best = new BestSimulation(listBestSimulationTurnAI);
		
//		ExecutorService executor = Executors.newFixedThreadPool(15);
//		Callable<String> task = () -> {
			while(best.getAtomicInt().intValue() < listBestSimulationTurnAI.size()) {
				BestSimulationTurnAI next = best.getNextSimulationInList();
				next.calculeNextBestMovementsOpponent(difficulty, typePlayerAI);
				System.out.println("\nNivel 8 hard Thread name: " +Thread.currentThread().getName()
							+ " - Task executed - piece: " + next.getPieceName() 
							+ " - position original: "+ next.getPositionOriginSimulated());
			}
//			return "\nTask ok";
//		};
//		
//		Future<String> submit = executor.submit(task);
//		try {
//			System.out.println("Future submit 15 threads: " + submit.get());
//			executor.shutdown();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		} finally{
//			executor.shutdownNow();
//		}
	}

}
