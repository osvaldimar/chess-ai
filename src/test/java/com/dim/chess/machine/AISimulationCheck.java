package com.dim.chess.machine;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Test;

import com.chess.core.Chessboard;
import com.chess.core.GameApplication;
import com.chess.core.ResponseChessboard.StatusResponse;
import com.chess.core.client.ChessServiceRemote;
import com.chess.core.client.ResponseClient;
import com.chess.core.enums.PositionChessboard;
import com.chess.core.enums.TypeColor;
import com.chess.core.enums.TypePlayer;
import com.chess.core.model.Difficulty;
import com.chess.core.model.King;
import com.chess.core.model.Player;
import com.chess.core.model.Rook;
import com.chess.core.service.ChessServiceImpl;
import com.dim.chess.ai.AIApplication;
import com.dim.chess.ai.AISimulation;
import com.dim.chess.ai.PlayerMachineAI;
import com.dim.chess.ai.model.BestMovementSimulation;
import com.dim.chess.ai.model.PieceResultScoreModel;

public class AISimulationCheck {

	@Test
	public void testAISimulationManualCheckmateBlack(){
		System.out.println("\n------------------------------------------------------------------------------");		
		System.out.println("\n-----------------testAISimulationManualCheckmateBlack()-----------------------");
		Chessboard chessboard = new Chessboard(new Player(TypePlayer.W), new Player(TypePlayer.B));		
		King kingW = new King(TypeColor.WHITE, TypePlayer.W);
		King kingB = new King(TypeColor.BLACK, TypePlayer.B);
		Rook rook1 = new Rook(TypeColor.BLACK, TypePlayer.B);
		Rook rook2 = new Rook(TypeColor.BLACK, TypePlayer.B);
		chessboard.addPiece(PositionChessboard.E1, kingW);
		chessboard.addPiece(PositionChessboard.E8, kingB);
		chessboard.addPiece(PositionChessboard.H2, rook1);
		chessboard.addPiece(PositionChessboard.G8, rook2);
		
		GameApplication game = new GameApplication(chessboard);	
		chessboard.printDebugChessboard(game.getChessboard(), "start chessboard custom");
		ChessServiceRemote service = new ChessServiceImpl();
		service.play(game);
		
		ResponseClient response = service.selectAndMovePiece("E1", "W");
		Assert.assertEquals(response.getStatus(), StatusResponse.CLICKED.toString());
		response = service.selectAndMovePiece("D1", "W");
		Assert.assertEquals(response.getStatus(), StatusResponse.MOVED.toString());
		Assert.assertEquals(response.getTurn(), TypePlayer.B.toString());
		
		game.getChessboard().printDebugChessboard(game.getChessboard(), "testAISimulationManualCheckmateBlack");
		
		Difficulty difficulty = new Difficulty(Difficulty.Level.LEVEL_AI_3, Difficulty.Deduction.LEVEL_DEDUCTION_1);
		AIApplication AIApp = new AIApplication(game, new PlayerMachineAI(TypePlayer.B, difficulty));
		
		Map<PositionChessboard, List<PositionChessboard>> mapPiecesAndListMovements = 
				AIApp.getMapPiecesAndListMovements(game.getChessboard().getSquaresChessboard(), AIApp.getPlayerAI().getTypePlayer());
		
		List<PieceResultScoreModel> listPieceResultScoreMovementsTurnAI = AIApp.getListPieceResultScoreMovementsTurnAI(
				game.getChessboard().getSquaresChessboard(), mapPiecesAndListMovements, AIApp.getPlayerAI().getTypePlayer());
		
		AISimulation aiSimulation = new AISimulation(AIApp.getPlayerAI(), game.getChessboard());
		aiSimulation.calculeSimulationBestTurn(listPieceResultScoreMovementsTurnAI);
		
		BestMovementSimulation bestMovementSimulation = aiSimulation.getBestMovementSimulation();		
		ImmutablePair<PositionChessboard, PositionChessboard> pair = 
				ImmutablePair.of(bestMovementSimulation.getPositionOriginSimulated(), 
				bestMovementSimulation.getPositionDestinySimulated());
		
		response = service.selectAndMovePiece(pair.getLeft().toString(), AIApp.getPlayerAI().getTypePlayer().toString());
		response = service.selectAndMovePiece(pair.getRight().toString(), AIApp.getPlayerAI().getTypePlayer().toString());
		Assert.assertEquals(response.getStatus(), StatusResponse.MOVED.toString());
		chessboard.printDebugChessboard(game.getChessboard(), "final chessboard custom");
		
	}
}
