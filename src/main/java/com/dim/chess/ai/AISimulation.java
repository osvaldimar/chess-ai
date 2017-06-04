package com.dim.chess.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

import com.chess.core.Chessboard;
import com.chess.core.enums.PositionChessboard;
import com.chess.core.exception.CheckMoveException;
import com.chess.core.exception.CheckStateException;
import com.chess.core.exception.CheckmateException;
import com.chess.core.model.Square;
import com.chess.core.util.ChessboardPieceFactory;
import com.dim.chess.ai.model.BestMovementSimulation;
import com.dim.chess.ai.model.BestSimulationTurnAI;
import com.dim.chess.ai.model.PieceResultScoreModel;

public class AISimulation {

	private PlayerMachineAI playerAI;
	private Chessboard chessboard;
	private List<BestSimulationTurnAI> listBestSimulationTurnAI;

	public AISimulation(PlayerMachineAI playerAI, Chessboard chessboard) {
		this.playerAI = playerAI;
		this.chessboard = chessboard;
		this.listBestSimulationTurnAI = new ArrayList<>();
	}
	
	public void calculeSimulationBestTurn(List<PieceResultScoreModel> listPieceResultScoreMovementsTurnAI) {
		
		listPieceResultScoreMovementsTurnAI.forEach(p -> p.sortedMapResultScoreMovementsOfPiece());
		System.out.println("\nSorted just map internal result score movements: \n" + listPieceResultScoreMovementsTurnAI);
		int totalResultScore = listPieceResultScoreMovementsTurnAI.stream().map(m -> m.getTotalResultScoreMovementsOfPiece()).reduce((a, b) -> a+b).orElse(0).intValue();
		System.out.println("Total movements of all pieces: " + totalResultScore);
		
		while(!listPieceResultScoreMovementsTurnAI.isEmpty() && totalResultScore != 0){
			BestSimulationTurnAI doBestSimulation = this.doBestSimulationTurnInList(listPieceResultScoreMovementsTurnAI);
			this.listBestSimulationTurnAI.add(doBestSimulation);

			//validate if player actual does checkmate or draw
			//TODO MODEL
			
			//validate list piece result score has movements yet into map
			if(listPieceResultScoreMovementsTurnAI.stream().map(m -> m.getTotalResultScoreMovementsOfPiece())
					.reduce((a, b) -> a+b).orElse(0).intValue() == 0 || this.playerAI.getDifficulty().getLevelAI().getValue() == 1){
				break;
			}
		}
		//TODO transaction thread exception here
		this.listBestSimulationTurnAI.stream().forEach(f ->  
				f.calculeNextBestMovementsOpponent(this.playerAI.getDifficulty(), this.playerAI.getTypePlayer()));
		
	}
	
	private BestSimulationTurnAI doBestSimulationTurnInList(List<PieceResultScoreModel> listPieceResultScoreMovementsTurnAI) {
		
		//valida list empty and map empty of list piece result score movements available
		
		PieceResultScoreModel pieceResult = this.findBestPieceResultScoreModel(listPieceResultScoreMovementsTurnAI);
		Entry<PositionChessboard, Double> entryDestiny = pieceResult.getMapResultScoreMovementsOfPiece().entrySet().stream().findFirst().orElse(null);
		//delete best position movement of pieceResult requested 
		listPieceResultScoreMovementsTurnAI.stream().filter(f -> f.getPositionPiece() == pieceResult.getPositionPiece())
					.findFirst().orElse(null).getMapResultScoreMovementsOfPiece()
					.remove(pieceResult.getMapResultScoreMovementsOfPiece().entrySet().stream().findFirst().orElse(null).getKey());
		System.out.println("Total movements yet available: " + listPieceResultScoreMovementsTurnAI.stream()
					.map(m -> m.getTotalResultScoreMovementsOfPiece()).reduce((a, b) -> a+b).orElse(0).intValue());
		
		Square[][] cloneReal = ChessboardPieceFactory.buildCloneSquares(this.chessboard.getSquaresChessboard());
		BestSimulationTurnAI bestSimulation = null;
		try {
			this.chessboard.movePieceInTheChessboard(
					pieceResult.getPositionPiece(), 
					entryDestiny.getKey(), 
					cloneReal[pieceResult.getPositionPiece().getLetter()][pieceResult.getPositionPiece().getNumber()].getPiece());
			
			bestSimulation = new BestSimulationTurnAI(this.chessboard.getSquaresChessboard(), 
					pieceResult.getPieceName(), entryDestiny.getValue(), 
					pieceResult.getPositionPiece(), entryDestiny.getKey());
					
			this.chessboard.setSquares(cloneReal);
		} catch (CheckMoveException | CheckStateException e) {
			e.printStackTrace();
		}		
		return bestSimulation;
	}
	
	private PieceResultScoreModel findBestPieceResultScoreModel(List<PieceResultScoreModel> listPieceResultScoreMovementsTurnAI) {
		
		//intelligence to find value score higher
		//List<PieceResultScoreModel> collectOrdered 
		listPieceResultScoreMovementsTurnAI = listPieceResultScoreMovementsTurnAI.stream().sorted(new Comparator<PieceResultScoreModel>() {
			public int compare(PieceResultScoreModel o1, PieceResultScoreModel o2) {
				Entry<PositionChessboard, Double> entry1 = o1.getMapResultScoreMovementsOfPiece().entrySet().stream().findFirst().orElse(null);
				Entry<PositionChessboard, Double> entry2 = o2.getMapResultScoreMovementsOfPiece().entrySet().stream().findFirst().orElse(null);
				Double d1 = entry1 != null ? entry1.getValue() : 0d;
				Double d2 = entry2 != null ? entry2.getValue() : 0d;
				return Double.compare(d1, d2) * -1;
			}
		}).collect(Collectors.toList());
		
		System.out.println("\nSorted list all pieces and result score movements: \n" + listPieceResultScoreMovementsTurnAI);
		
		//first double value score
		Entry<PositionChessboard, Double> entryFirst = listPieceResultScoreMovementsTurnAI.get(0).getMapResultScoreMovementsOfPiece()
															.entrySet().stream().findFirst().orElse(null);
		final Double scoreHigher = entryFirst != null ? entryFirst.getValue() : 0d;
		
		//collect list values score equals
		List<PieceResultScoreModel> collectListValuesEquals = listPieceResultScoreMovementsTurnAI.stream().filter(f -> {
			Entry<PositionChessboard, Double> entry = f.getMapResultScoreMovementsOfPiece().entrySet().stream().findFirst().orElse(null);
			Double value = entry != null ? entry.getValue() : 0d;
			if(scoreHigher.doubleValue() == value.doubleValue()) return true;
			return false;
		}).collect(Collectors.toList());
		
		//random list
		PieceResultScoreModel pieceBestMovement = collectListValuesEquals.get(new Random().nextInt(collectListValuesEquals.size()));		
		System.out.println("\nMethod findBestPieceResultScoreModel() piece choosed : " + pieceBestMovement);
		
		return pieceBestMovement;
	}

	public BestMovementSimulation getBestMovementSimulation() {
		this.listBestSimulationTurnAI = this.listBestSimulationTurnAI.stream().sorted(new Comparator<BestSimulationTurnAI>() {
			@Override
			public int compare(BestSimulationTurnAI o1, BestSimulationTurnAI o2) {
				//TODO pegar best score simulation opponent levels
				return Double.compare(o1.getLastScoreLevelsCalculated(), o2.getLastScoreLevelsCalculated()) * -1;
			}
		}).collect(Collectors.toList());
		
		BestSimulationTurnAI best = this.listBestSimulationTurnAI.get(0);
		System.out.println("\nList final of BestSimulationTurnAI: " + this.listBestSimulationTurnAI);
		
		return new BestMovementSimulation(best.getPieceName(), best.getBestScoreSimulatedPrincipal(), 
				best.getPositionOriginSimulated(), best.getPositionDestinySimulated());
	}

	public List<BestSimulationTurnAI> getListBestSimulationTurnAI() {
		return listBestSimulationTurnAI;
	}
}
