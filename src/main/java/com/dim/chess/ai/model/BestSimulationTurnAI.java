package com.dim.chess.ai.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chess.core.Chessboard;
import com.chess.core.GameApplication;
import com.chess.core.enums.PositionChessboard;
import com.chess.core.enums.TypePlayer;
import com.chess.core.exception.CheckmateException;
import com.chess.core.helper.PieceHelper;
import com.chess.core.model.Difficulty;
import com.chess.core.model.Player;
import com.chess.core.model.Square;
import com.chess.core.service.ChessMultiplayerAI;
import com.dim.chess.ai.AIApplication;
import com.dim.chess.ai.AISimulation;
import com.dim.chess.ai.PlayerMachineAI;

public class BestSimulationTurnAI {

	private Square[][] squaresChessboardSimulated;
	private Double bestScoreSimulatedPrincipal;
	private PositionChessboard positionOriginSimulated;
	private PositionChessboard positionDestinySimulated;
	private String pieceName;
	private List<TrackMovement> listScoreLevelsCalculated = new ArrayList<>();

	public BestSimulationTurnAI(Square[][] squaresChessboardSimulated, String pieceName, Double bestScoreSimulated, 
			PositionChessboard positionOriginSimulated, PositionChessboard positionDestinySimulated) {
		this.squaresChessboardSimulated = squaresChessboardSimulated;
		this.pieceName = pieceName;
		this.bestScoreSimulatedPrincipal = bestScoreSimulated;
		this.positionOriginSimulated = positionOriginSimulated;
		this.positionDestinySimulated = positionDestinySimulated;
		this.listScoreLevelsCalculated.add(new TrackMovement(bestScoreSimulated, positionOriginSimulated, positionDestinySimulated, pieceName, ""));
	}

	public Square[][] getSquaresChessboardSimulated() {
		return squaresChessboardSimulated;
	}
	
	public String getPieceName() {
		return pieceName;
	}

	public Double getBestScoreSimulatedPrincipal() {
		return bestScoreSimulatedPrincipal;
	}

	public PositionChessboard getPositionOriginSimulated() {
		return positionOriginSimulated;
	}

	public PositionChessboard getPositionDestinySimulated() {
		return positionDestinySimulated;
	}
	
	
	public Double getLastScoreLevelsCalculated(){
		return this.listScoreLevelsCalculated.get(this.listScoreLevelsCalculated.size()-1).getScore();
	}

	public void calculeNextBestMovementsOpponent(Difficulty difficulty, TypePlayer typePieceAI) {
		
		if(difficulty.getLevelAI().getValue() >= 2){
			 
			TypePlayer typeTurnCurrent = PieceHelper.negateTypePlayer(typePieceAI);
			ChessMultiplayerAI chessPlayer = new ChessMultiplayerAI();
			GameApplication game = chessPlayer.startChess(new Player(typeTurnCurrent), new Player(typePieceAI));
			game.getChessboard().setSquares(getSquaresChessboardSimulated());
			
			int cont = 2;
			while(cont <= difficulty.getLevelAI().getValue()){
				System.out.println("\n***Method calculeNextBestMovementsOpponent() INIT level: " + cont + " - TOTAL LEVELS: " +difficulty.getLevelAI().getValue()+ "***\n");
				
				PlayerMachineAI buildPlayer = buildPlayerAISimulation(difficulty, typePieceAI, typeTurnCurrent);
				AIApplication AIApp = new AIApplication(game, buildPlayer);
				Map<PositionChessboard, List<PositionChessboard>> mapPiecesAndListMovements = 
						AIApp.getMapPiecesAndListMovements(game.getChessboard().getSquaresChessboard(), typeTurnCurrent);
				
				List<PieceResultScoreModel> listPieceResultScoreMovementsTurnAI = AIApp.getListPieceResultScoreMovementsTurnAI(
						game.getChessboard().getSquaresChessboard(), mapPiecesAndListMovements, typeTurnCurrent);
				
				Chessboard.printCloneDebugChessboard(game.getChessboard().getSquaresChessboard(), "BEFORE TO MOVE");
				AISimulation aiSimulation = new AISimulation(buildPlayer, game.getChessboard());
				aiSimulation.calculeSimulationBestTurn(listPieceResultScoreMovementsTurnAI);
				
				BestMovementSimulation bestMovementSimulation = aiSimulation.getBestMovementSimulation();
				AIApp.executeBestMovementCalculated(bestMovementSimulation);
				
				System.out.println("RESPONSE CLIENT TURN CURRENT: " + typeTurnCurrent + " \t\t STATUS: " + AIApp.getResponseClient().getStatus() );
				Chessboard.printCloneDebugChessboard(game.getChessboard().getSquaresChessboard(), "AFTER TO MOVE");
				
				double score = PieceHelper.getTotalScoreChessboardPiecesByPlayer(game.getChessboard().getSquaresChessboard(), typePieceAI);
				this.listScoreLevelsCalculated.add(new TrackMovement(score, bestMovementSimulation.getPositionOriginSimulated(), 
						bestMovementSimulation.getPositionDestinySimulated(), 
						bestMovementSimulation.getPieceName(), typeTurnCurrent.toString()));
				
				System.out.println("\n*** NEXT SCORE AI: " +score+ " - MOVEMENT EXECUTED: " +bestMovementSimulation
							+ "\t\t\n - TURN: " +typeTurnCurrent+ " ... level: " + cont + " - TOTAL LEVELS: " +difficulty.getLevelAI().getValue()+ "***\n");
				
				if(buildPlayer.getDifficulty().isDeductionLoop()){
					System.out.println("\nDeduction loop List final of BestSimulationTurnAI: " + aiSimulation.getListBestSimulationTurnAI());
				}
				
				System.out.println("\n***Method calculeNextBestMovementsOpponent() END level: " + cont + " - TOTAL LEVELS: " +difficulty.getLevelAI().getValue()+ "***\n");
				typeTurnCurrent = PieceHelper.negateTypePlayer(typeTurnCurrent);
				cont++;
			}
		}
	}
	
	private PlayerMachineAI buildPlayerAISimulation(Difficulty difficulty, TypePlayer typePieceAI, TypePlayer typeTurnCurrent) {
		if(difficulty.isDeductionLoop() || typePieceAI == typeTurnCurrent){
			return new PlayerMachineAI(typeTurnCurrent);			
		}		
		Difficulty d = new Difficulty(
				Difficulty.Level.getEnum(difficulty.getLevelDeduction().getValue()), 
				Difficulty.Deduction.LEVEL_DEDUCTION_1);
		d.setDeductionLoop(true);
		return new PlayerMachineAI(typeTurnCurrent, d);
	}

	@Override
	public String toString() {
		return "\n" + this.getClass().getName() + " " + pieceName  + " score: " +  bestScoreSimulatedPrincipal 
				+ " FROM: " + positionOriginSimulated + " TO: " + positionDestinySimulated + " - List score leves:" + this.listScoreLevelsCalculated
				+ " - FINAL LAST SCORE LEVEL: " + getLastScoreLevelsCalculated();
	}
}
