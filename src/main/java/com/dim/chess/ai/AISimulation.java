package com.dim.chess.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.chess.core.Chessboard;
import com.chess.core.enums.PositionChessboard;
import com.chess.core.exception.CheckMoveException;
import com.chess.core.exception.CheckStateException;
import com.chess.core.model.ChessboardModel;
import com.chess.core.model.Piece;
import com.chess.core.model.Queen;
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
	
	public <A extends T, T, R> Function<? super A, Integer> pega(Function<? super T, ? extends Integer> f){
		return (t) -> f.apply(t);
	}
	
	public void calculeSimulationBestTurn(List<PieceResultScoreModel> listPieceResultScoreMovementsTurnAI) {
		
		listPieceResultScoreMovementsTurnAI.forEach(p -> p.sortedMapResultScoreMovementsOfPiece());
		//System.out.println("\nSorted just map internal result score movements: \n" + listPieceResultScoreMovementsTurnAI);
		int totalResultScore = listPieceResultScoreMovementsTurnAI.stream().map(t -> this.pega(PieceResultScoreModel::getTotalResultScoreMovementsOfPiece).apply(t)).reduce((a, b) -> a+b).orElse(0).intValue();
		//System.out.println("Total movements of all pieces: " + totalResultScore);
		
		while(!listPieceResultScoreMovementsTurnAI.isEmpty() && totalResultScore != 0){
			BestSimulationTurnAI doBestSimulation = this.doBestSimulationTurnInList(listPieceResultScoreMovementsTurnAI);
			this.listBestSimulationTurnAI.add(doBestSimulation);

			//validate list piece result score has movements yet into map
			if(listPieceResultScoreMovementsTurnAI.stream().map(m -> m.getTotalResultScoreMovementsOfPiece())
					.reduce((a, b) -> a+b).orElse(0).intValue() == 0 || this.playerAI.getDifficulty().getLevelAI().getValue() == 1){
				break;
			}
		}
		
		AIPoolThreadExecutorService threadService = new AIPoolThreadExecutorService();
		threadService.calculeList(this.listBestSimulationTurnAI, this.playerAI.getDifficulty(), this.playerAI.getTypePlayer());
		
	}
	
	private BestSimulationTurnAI doBestSimulationTurnInList(List<PieceResultScoreModel> listPieceResultScoreMovementsTurnAI) {
		
		PieceResultScoreModel pieceResult;
		Entry<PositionChessboard, Double> entryDestiny;
		//valida list empty and map empty of list piece result score movements available		
		pieceResult = this.findBestPieceResultScoreModel(listPieceResultScoreMovementsTurnAI);
		entryDestiny = pieceResult.getMapResultScoreMovementsOfPiece().entrySet().stream().findFirst().orElse(null);
		//delete best position movement of pieceResult requested 
		teste(listPieceResultScoreMovementsTurnAI, pieceResult)
					.findFirst().orElse(null).getMapResultScoreMovementsOfPiece()
					.remove(pieceResult.getMapResultScoreMovementsOfPiece().entrySet().stream().findFirst().orElse(null).getKey());
		
		ChessboardModel modelReal = ChessboardPieceFactory.cloneDeepGeneric(this.chessboard.getModel());
		
		BestSimulationTurnAI bestSimulation = null;
		try {
			this.chessboard.movePieceInTheChessboard(
					pieceResult.getPositionPiece(), 
					entryDestiny.getKey(), 
					this.chessboard.getSquareChessboard(pieceResult.getPositionPiece()).getPiece());
			
			//validate promotion pawn
			if(this.chessboard.getPositionPromotionPawn() != null){
				Piece pawn = this.chessboard.getSquareChessboard(this.chessboard.getPositionPromotionPawn()).getPiece();
				this.chessboard.processPromotionOfPawn(this.chessboard.getPositionPromotionPawn(), new Queen(pawn.getColor(), pawn.getPlayer()));
			}
			
			bestSimulation = new BestSimulationTurnAI(playerAI, this.chessboard.getModel(), 
					pieceResult.getPieceName(), entryDestiny.getValue(), 
					pieceResult.getPositionPiece(), entryDestiny.getKey());
					
			this.chessboard.setModel(modelReal);
		} catch (CheckMoveException | CheckStateException e) {
			e.printStackTrace();
		}		
		return bestSimulation;
	}

	private Stream<PieceResultScoreModel> teste(List<PieceResultScoreModel> listPieceResultScoreMovementsTurnAI,
			PieceResultScoreModel pieceResult) {
		return listPieceResultScoreMovementsTurnAI.stream().filter(f -> f.getPositionPiece() == pieceResult.getPositionPiece());
	}
	
	private PieceResultScoreModel findBestPieceResultScoreModel(List<PieceResultScoreModel> listPieceResultScoreMovementsTurnAI) {
		
		//intelligence to find value score higher
		listPieceResultScoreMovementsTurnAI = listPieceResultScoreMovementsTurnAI.stream().sorted(new Comparator<PieceResultScoreModel>() {
			public int compare(PieceResultScoreModel o1, PieceResultScoreModel o2) {
				Entry<PositionChessboard, Double> entry1 = o1.getMapResultScoreMovementsOfPiece().entrySet().stream().findFirst().orElse(null);
				Entry<PositionChessboard, Double> entry2 = o2.getMapResultScoreMovementsOfPiece().entrySet().stream().findFirst().orElse(null);
				Double d1 = entry1 != null ? entry1.getValue() : 0d;
				Double d2 = entry2 != null ? entry2.getValue() : 0d;
				return Double.compare(d1, d2) * -1;
			}
		}).collect(Collectors.toList());
		
		//System.out.println("\nSorted list all pieces and result score movements: \n" + listPieceResultScoreMovementsTurnAI);
		
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
		//System.out.println("\nMethod findBestPieceResultScoreModel() piece choosed : " + pieceBestMovement);
		
		return pieceBestMovement;
	}

	public BestMovementSimulation getBestMovementSimulation() {
		this.listBestSimulationTurnAI = this.listBestSimulationTurnAI.stream().sorted(new Comparator<BestSimulationTurnAI>() {
			@Override
			public int compare(BestSimulationTurnAI o1, BestSimulationTurnAI o2) {
				//pegar best score simulation opponent levels
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
