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
	private final Double bestScoreSimulatedPrincipal;
	private final PositionChessboard positionOriginSimulated;
	private final PositionChessboard positionDestinySimulated;
	private final String pieceName;
	private final List<TrackMovement> listScoreLevelsCalculated = new ArrayList<>();
	private final PlayerMachineAI ai;

	public BestSimulationTurnAI(final PlayerMachineAI ai, final ChessboardModel model, final String pieceName,
			final Double bestScoreSimulated, final PositionChessboard positionOriginSimulated,
			final PositionChessboard positionDestinySimulated) {
		this.ai = ai;
		this.model = model;
		this.pieceName = pieceName;
		this.bestScoreSimulatedPrincipal = bestScoreSimulated;
		this.positionOriginSimulated = positionOriginSimulated;
		this.positionDestinySimulated = positionDestinySimulated;
		this.listScoreLevelsCalculated.add(new TrackMovement(bestScoreSimulated, positionOriginSimulated,
				positionDestinySimulated, pieceName, ""));
	}

	public Square[][] getSquaresChessboardSimulated() {
		return this.model.getSquares();
	}

	public String getPieceName() {
		return this.pieceName;
	}

	public Double getBestScoreSimulatedPrincipal() {
		return this.bestScoreSimulatedPrincipal;
	}

	public PositionChessboard getPositionOriginSimulated() {
		return this.positionOriginSimulated;
	}

	public PositionChessboard getPositionDestinySimulated() {
		return this.positionDestinySimulated;
	}

	public Double getLastScoreLevelsCalculated() {
		return this.listScoreLevelsCalculated.get(this.listScoreLevelsCalculated.size() - 1).getScore();
	}

	public void calculeNextBestMovementsOpponent(final Difficulty difficulty, final TypePlayer typePlayerAI) {

		if (difficulty.getLevelAI().getValue() >= 2) {

			final ChessMultiplayerAI chessPlayer = new ChessMultiplayerAI();
			final GameApplication game = chessPlayer.startChess(new Player(PieceHelper.negateTypePlayer(typePlayerAI)),
					new Player(typePlayerAI));
			game.getChessboard().setSquares(this.getSquaresChessboardSimulated());
			TypePlayer typeTurnCurrent = typePlayerAI;

			int cont = 2;
			while (cont <= difficulty.getLevelAI().getValue()) {

				// validate if player actual does checkmate or draw
				if (!this.isValidateNextTurnCheckmateOrDraw(game.getChessboard().getModel(), typeTurnCurrent)) {
					return;
				}
				typeTurnCurrent = PieceHelper.negateTypePlayer(typeTurnCurrent);

				// System.out.println("\n***Method
				// calculeNextBestMovementsOpponent() INIT level: " + cont + " -
				// TOTAL LEVELS: " +difficulty.getLevelAI().getValue()+
				// "***\n");

				final PlayerMachineAI buildPlayer = this.buildPlayerAISimulation(difficulty, typePlayerAI,
						typeTurnCurrent);
				final AIApplication AIApp = new AIApplication(game, buildPlayer);
				final Map<PositionChessboard, List<PositionChessboard>> mapPiecesAndListMovements = AIApp
						.getMapPiecesAndListMovements(game.getChessboard().getSquaresChessboard(), typeTurnCurrent);

				final List<PieceResultScoreModel> listPieceResultScoreMovementsTurnAI = AIApp
						.getListPieceResultScoreMovementsTurnAI(game.getChessboard().getSquaresChessboard(),
								mapPiecesAndListMovements, typeTurnCurrent);

				// Chessboard.printCloneDebugChessboard(game.getChessboard().getSquaresChessboard(),
				// "BEFORE TO MOVE");
				final AISimulation aiSimulation = new AISimulation(buildPlayer, game.getChessboard());
				aiSimulation.calculeSimulationBestTurn(listPieceResultScoreMovementsTurnAI);

				final BestMovementSimulation bestMovementSimulation = aiSimulation.getBestMovementSimulation();
				AIApp.executeBestMovementCalculated(bestMovementSimulation);

				// System.out.println("RESPONSE CLIENT TURN CURRENT: " +
				// typeTurnCurrent + " \t\t STATUS: " +
				// AIApp.getResponseClient().getStatus() );
				// Chessboard.printCloneDebugChessboard(game.getChessboard().getSquaresChessboard(),
				// "AFTER TO MOVE");

				final double score = PieceHelper.getTotalScoreChessboardPiecesByPlayer(
						game.getChessboard().getSquaresChessboard(), typePlayerAI);
				this.listScoreLevelsCalculated
						.add(new TrackMovement(score, bestMovementSimulation.getPositionOriginSimulated(),
								bestMovementSimulation.getPositionDestinySimulated(),
								bestMovementSimulation.getPieceName(), typeTurnCurrent.toString()));

				// System.out.println("\n*** NEXT SCORE AI: " +score+ " -
				// MOVEMENT EXECUTED: " +bestMovementSimulation
				// + "\t\t\n - TURN: " +typeTurnCurrent+ " ... level: " + cont +
				// " - TOTAL LEVELS: " +difficulty.getLevelAI().getValue()+
				// "***\n");

				// if(buildPlayer.getDifficulty().isDeductionLoop()){
				// System.out.println("\nDeduction loop List final of
				// BestSimulationTurnAI: " +
				// aiSimulation.getListBestSimulationTurnAI());
				// }

				// System.out.println("\n***Method
				// calculeNextBestMovementsOpponent() END level: " + cont + " -
				// TOTAL LEVELS: " +difficulty.getLevelAI().getValue()+
				// "***\n");
				cont++;
			}
		} else {
			// validate if player actual does checkmate or draw
			if (!this.isValidateNextTurnCheckmateOrDraw(this.getModel(), typePlayerAI)) {
				return;
			}
		}
	}

	/**
	 * Valida o turno do proximo jogador se eh recebeu um checkmate ou empate
	 * 
	 * @param typePlayerAIOwner
	 *            - player atual que realizou a jogada
	 * @return
	 */
	private boolean isValidateNextTurnCheckmateOrDraw(final ChessboardModel modelCurrent,
			final TypePlayer typePlayerAIOwner) {
		final Chessboard chessB = new Chessboard(modelCurrent);
		final PlayerMode playerNextTurnVerifyGameOver = (typePlayerAIOwner == chessB.getPlayer1().getTypePlayer()
				? chessB.getPlayer2() : chessB.getPlayer1());
		try {
			chessB.processValidateCheckmate(playerNextTurnVerifyGameOver);
			chessB.processValidateDraw(playerNextTurnVerifyGameOver);
		} catch (DrawStalemateException | Draw50MovementsException | Draw3PositionsException e) {
			System.out.println("SIMULATION GAMEOVER DRAW: ");
			chessB.printDebugChessboard(chessB, "GAMEOVER DRAW");
			this.listScoreLevelsCalculated.add(new TrackMovement(this.getLastScoreLevelsCalculated(),
					this.getPositionOriginSimulated(), this.getPositionDestinySimulated(),
					this.getPieceName() + "-doesdraw", typePlayerAIOwner.toString()));
			return false;
		} catch (final CheckmateException e) {
			System.out.println("SIMULATION GAMEOVER CHECKMATE: ");
			chessB.printDebugChessboard(chessB, "GAMEOVER CHECKMATE");
			final double score = this.getLastScoreLevelsCalculated() + (this.ai.getTypePlayer() == typePlayerAIOwner
					? TypePiece.KING.getValue() : -TypePiece.KING.getValue());
			this.listScoreLevelsCalculated
					.add(new TrackMovement(score, this.getPositionOriginSimulated(), this.getPositionDestinySimulated(),
							this.getPieceName() + "-doescheckmate", typePlayerAIOwner.toString()));
			return false;
		} catch (final CheckStateException e) {
			System.out.println("SIMULATION IN CHECK: ");
			chessB.printDebugChessboard(chessB, "SIMULATION IN CHECK");
		}
		return true;
	}

	private PlayerMachineAI buildPlayerAISimulation(final Difficulty difficulty, final TypePlayer typePieceAI,
			final TypePlayer typeTurnCurrent) {
		if (difficulty.isDeductionLoopAI() || typePieceAI == typeTurnCurrent) {
			return new PlayerMachineAI(typeTurnCurrent);
		}
		final Difficulty d = new Difficulty(Difficulty.Level.getEnum(difficulty.getLevelDeductionAI().getValue()),
				Difficulty.Deduction.LEVEL_DEDUCTION_1);
		d.setDeductionLoopAI(true);
		return new PlayerMachineAI(typeTurnCurrent, d);
	}

	public ChessboardModel getModel() {
		return this.model;
	}

	public void setModel(final ChessboardModel model) {
		this.model = model;
	}

	@Override
	public String toString() {
		return "\n" + this.getClass().getName() + " " + this.pieceName + " score: " + this.bestScoreSimulatedPrincipal
				+ " FROM: " + this.positionOriginSimulated + " TO: " + this.positionDestinySimulated
				+ " - List score leves:" + this.listScoreLevelsCalculated + " - FINAL LAST SCORE LEVEL: "
				+ this.getLastScoreLevelsCalculated();
	}
}
