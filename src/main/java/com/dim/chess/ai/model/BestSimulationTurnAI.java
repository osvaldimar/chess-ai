package com.dim.chess.ai.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chess.core.Chessboard;
import com.chess.core.GameApplication;
import com.chess.core.client.PlayerMode;
import com.chess.core.enums.PositionChessboard;
import com.chess.core.enums.TypePiece;
import com.chess.core.enums.TypePlayer;
import com.chess.core.exception.CheckStateException;
import com.chess.core.exception.CheckmateException;
import com.chess.core.exception.Draw3PositionsException;
import com.chess.core.exception.Draw50MovementsException;
import com.chess.core.exception.DrawStalemateException;
import com.chess.core.helper.PieceHelper;
import com.chess.core.model.ChessboardModel;
import com.chess.core.model.Difficulty;
import com.chess.core.model.Player;
import com.chess.core.model.Square;
import com.chess.core.service.ChessMultiplayerAI;
import com.dim.chess.ai.AIApplication;
import com.dim.chess.ai.AISimulation;
import com.dim.chess.ai.PlayerMachineAI;

public class BestSimulationTurnAI {

	private ChessboardModel model;
	private Double bestScoreSimulatedPrincipal;
	private PositionChessboard positionOriginSimulated;
	private PositionChessboard positionDestinySimulated;
	private String pieceName;
	private List<TrackMovement> listScoreLevelsCalculated = new ArrayList<>();
	private PlayerMachineAI ai;

	public BestSimulationTurnAI(PlayerMachineAI ai, ChessboardModel model, String pieceName, Double bestScoreSimulated, 
			PositionChessboard positionOriginSimulated, PositionChessboard positionDestinySimulated) {
		this.ai = ai;
		this.model = model;
		this.pieceName = pieceName;
		this.bestScoreSimulatedPrincipal = bestScoreSimulated;
		this.positionOriginSimulated = positionOriginSimulated;
		this.positionDestinySimulated = positionDestinySimulated;
		this.listScoreLevelsCalculated.add(new TrackMovement(bestScoreSimulated, positionOriginSimulated, positionDestinySimulated, pieceName, ""));
	}

	public Square[][] getSquaresChessboardSimulated() {
		return model.getSquares();
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

	public void calculeNextBestMovementsOpponent(Difficulty difficulty, TypePlayer typePlayerAI) {
		
		if(difficulty.getLevelAI().getValue() >= 2){
			
			ChessMultiplayerAI chessPlayer = new ChessMultiplayerAI();
			GameApplication game = chessPlayer.startChess(new Player(PieceHelper.negateTypePlayer(typePlayerAI)), new Player(typePlayerAI));
			game.getChessboard().setSquares(getSquaresChessboardSimulated());
			TypePlayer typeTurnCurrent = typePlayerAI;
			
			int cont = 2;
			while(cont <= difficulty.getLevelAI().getValue()){
				
				//validate if player actual does checkmate or draw
				if(!isValidateNextTurnCheckmateOrDraw(game.getChessboard().getModel(), typeTurnCurrent)) return;
				typeTurnCurrent = PieceHelper.negateTypePlayer(typeTurnCurrent);
				
				System.out.println("\n***Method calculeNextBestMovementsOpponent() INIT level: " + cont + " - TOTAL LEVELS: " +difficulty.getLevelAI().getValue()+ "***\n");
				
				PlayerMachineAI buildPlayer = buildPlayerAISimulation(difficulty, typePlayerAI, typeTurnCurrent);
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
				
				double score = PieceHelper.getTotalScoreChessboardPiecesByPlayer(game.getChessboard().getSquaresChessboard(), typePlayerAI);
				this.listScoreLevelsCalculated.add(new TrackMovement(score, bestMovementSimulation.getPositionOriginSimulated(), 
						bestMovementSimulation.getPositionDestinySimulated(), 
						bestMovementSimulation.getPieceName(), typeTurnCurrent.toString()));
				
				System.out.println("\n*** NEXT SCORE AI: " +score+ " - MOVEMENT EXECUTED: " +bestMovementSimulation
							+ "\t\t\n - TURN: " +typeTurnCurrent+ " ... level: " + cont + " - TOTAL LEVELS: " +difficulty.getLevelAI().getValue()+ "***\n");
				
				if(buildPlayer.getDifficulty().isDeductionLoop()){
					System.out.println("\nDeduction loop List final of BestSimulationTurnAI: " + aiSimulation.getListBestSimulationTurnAI());
				}
				
				System.out.println("\n***Method calculeNextBestMovementsOpponent() END level: " + cont + " - TOTAL LEVELS: " +difficulty.getLevelAI().getValue()+ "***\n");
				cont++;
			}
		}else{
			//validate if player actual does checkmate or draw
			if(!isValidateNextTurnCheckmateOrDraw(getModel(), typePlayerAI)) return;
		}
	}

	/**
	 * Valida o turno do proximo jogador se eh recebeu um checkmate ou empate
	 * @param typePlayerAIOwner - player atual que realizou a jogada
	 * @return
	 */
	private boolean isValidateNextTurnCheckmateOrDraw(ChessboardModel modelCurrent, TypePlayer typePlayerAIOwner) {
		Chessboard chessB = new Chessboard(modelCurrent);
		PlayerMode playerNextTurnVerifyGameOver = (typePlayerAIOwner == chessB.getPlayer1().getTypePlayer() ? chessB.getPlayer2() : chessB.getPlayer1()); 
		try {
			chessB.processValidateCheckmate(playerNextTurnVerifyGameOver);
			chessB.processValidateDraw(playerNextTurnVerifyGameOver);
		} catch (DrawStalemateException | Draw50MovementsException | Draw3PositionsException e) {
			System.out.println("SIMULATION GAMEOVER DRAW: ");
			chessB.printDebugChessboard(chessB, "GAMEOVER DRAW");
			this.listScoreLevelsCalculated.add(
					new TrackMovement(getLastScoreLevelsCalculated(), getPositionOriginSimulated(), 
					getPositionDestinySimulated(), 
					getPieceName() + "-doesdraw", typePlayerAIOwner.toString())
			);
			return false;
		} catch (CheckmateException e) {
			System.out.println("SIMULATION GAMEOVER CHECKMATE: ");
			chessB.printDebugChessboard(chessB, "GAMEOVER CHECKMATE");
			double score = getLastScoreLevelsCalculated() + (this.ai.getTypePlayer() == typePlayerAIOwner ? TypePiece.KING.getValue() : -TypePiece.KING.getValue());
			this.listScoreLevelsCalculated.add(
					new TrackMovement(score, getPositionOriginSimulated(), 
					getPositionDestinySimulated(), 
					getPieceName() + "-doescheckmate", typePlayerAIOwner.toString())
			);
			return false;
		} catch (CheckStateException e) {
			System.out.println("SIMULATION IN CHECK: ");
			chessB.printDebugChessboard(chessB, "SIMULATION IN CHECK");
		}
		return true;
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

	public ChessboardModel getModel() {
		return model;
	}

	public void setModel(ChessboardModel model) {
		this.model = model;
	}

	@Override
	public String toString() {
		return "\n" + this.getClass().getName() + " " + pieceName  + " score: " +  bestScoreSimulatedPrincipal 
				+ " FROM: " + positionOriginSimulated + " TO: " + positionDestinySimulated + " - List score leves:" + this.listScoreLevelsCalculated
				+ " - FINAL LAST SCORE LEVEL: " + getLastScoreLevelsCalculated();
	}
}
