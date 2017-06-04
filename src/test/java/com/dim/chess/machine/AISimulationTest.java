package com.dim.chess.machine;

import static com.chess.core.enums.PositionChessboard.B8;
import static com.chess.core.enums.PositionChessboard.H8;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Test;

import com.chess.core.GameApplication;
import com.chess.core.ResponseChessboard;
import com.chess.core.ResponseChessboard.StatusResponse;
import com.chess.core.client.ChessServiceRemote;
import com.chess.core.client.ResponseClient;
import com.chess.core.enums.PositionChessboard;
import com.chess.core.enums.TypePiece;
import com.chess.core.enums.TypePlayer;
import com.chess.core.helper.PieceHelper;
import com.chess.core.model.Difficulty;
import com.chess.core.model.Player;
import com.chess.core.service.ChessMultiplayerAI;
import com.chess.core.service.ChessServiceImpl;
import com.dim.chess.ai.AIApplication;
import com.dim.chess.ai.AISimulation;
import com.dim.chess.ai.PlayerMachineAI;
import com.dim.chess.ai.model.BestMovementSimulation;
import com.dim.chess.ai.model.BestSimulationTurnAI;
import com.dim.chess.ai.model.PieceResultScoreModel;

public class AISimulationTest {

	@Test
	public void testAISimulationManualProcessLevel_1() throws InterruptedException{
		System.out.println("\n------------------------------------------------------------------------------");		
		System.out.println("\n-----------------testAISimulationManualProcessLevel_1()-----------------------");
		ChessMultiplayerAI chessPlayer = new ChessMultiplayerAI();
		GameApplication game = chessPlayer.startChess(new Player(TypePlayer.W), new Player(TypePlayer.B));		
		ChessServiceRemote service = new ChessServiceImpl();
		service.play(game);
		ResponseClient response = service.selectAndMovePiece("C2", "W");
		response = service.selectAndMovePiece("C3", "W");
		Assert.assertEquals(response.getStatus(), StatusResponse.MOVED.toString());
		
		response = service.selectAndMovePiece("H7", "B");
		response = service.selectAndMovePiece("H6", "B");
		response = service.selectAndMovePiece("D1", "W");
		response = service.selectAndMovePiece("B3", "W");
		
		response = service.selectAndMovePiece("H6", "B");
		response = service.selectAndMovePiece("H5", "B");
		response = service.selectAndMovePiece("B3", "W");
		response = service.selectAndMovePiece("B7", "W");
		
		AIApplication AIApp = new AIApplication(game, new PlayerMachineAI(TypePlayer.B));
		
		Double scoreChessboardAI = PieceHelper.getTotalScoreChessboardPiecesByPlayer(
				game.getChessboard().getSquaresChessboard(), AIApp.getPlayerAI().getTypePlayer());
		Assert.assertEquals(new Double(PieceHelper.getTotalScorePiecesGameStart()-TypePiece.PAWN.getValue()), scoreChessboardAI);
		
		Double scoreChessboardPlayer = PieceHelper.getTotalScoreChessboardPiecesByPlayer(
				game.getChessboard().getSquaresChessboard(), PieceHelper.negateTypePlayer(AIApp.getPlayerAI().getTypePlayer()));
		Assert.assertEquals(new Double(PieceHelper.getTotalScorePiecesGameStart()+TypePiece.PAWN.getValue()), scoreChessboardPlayer);
		
		Map<PositionChessboard, List<PositionChessboard>> mapPiecesAndListMovements = 
				AIApp.getMapPiecesAndListMovements(game.getChessboard().getSquaresChessboard(), TypePlayer.B);
		Assert.assertEquals(mapPiecesAndListMovements.size(), 15);
		
		List<PieceResultScoreModel> listPieceResultScoreMovementsTurnAI = AIApp.getListPieceResultScoreMovementsTurnAI(
				game.getChessboard().getSquaresChessboard(), mapPiecesAndListMovements, TypePlayer.B);
		System.out.println("listPieceResultScoreMovementsTurnAI: \n" + listPieceResultScoreMovementsTurnAI);
		
		AISimulation aiSimulation = new AISimulation(AIApp.getPlayerAI(), game.getChessboard());
		
		aiSimulation.calculeSimulationBestTurn(listPieceResultScoreMovementsTurnAI);

		BestMovementSimulation bestMovementSimulation = aiSimulation.getBestMovementSimulation();
		Assert.assertNotNull(bestMovementSimulation.getBestScoreSimulated());
		Assert.assertNotNull(bestMovementSimulation.getPositionOriginSimulated());
		Assert.assertNotNull(bestMovementSimulation.getPositionDestinySimulated());
		Assert.assertEquals(bestMovementSimulation.getPieceName(), TypePiece.BISHOP.toString());
		
		game.getChessboard().printDebugChessboard(game.getChessboard(), "BEFORE FINISH");
		Assert.assertEquals(response.getTurn(), TypePlayer.B.toString());
		ImmutablePair<PositionChessboard, PositionChessboard> pairPositions = 
				ImmutablePair.of(bestMovementSimulation.getPositionOriginSimulated(), 
						bestMovementSimulation.getPositionDestinySimulated());
		response = service.selectAndMovePiece(pairPositions.getLeft().toString(), TypePlayer.B.toString());
		response = service.selectAndMovePiece(pairPositions.getRight().toString(), TypePlayer.B.toString());
		
		game.getChessboard().printDebugChessboard(game.getChessboard(), "FINISH");
		Assert.assertEquals(response.getStatus(), ResponseChessboard.StatusResponse.MOVED.toString());
		Assert.assertEquals(response.getTurn(), TypePlayer.W.toString());
	}
	
	@Test
	public void testAISimulationManualProcessLevel_2() throws InterruptedException{
		System.out.println("\n------------------------------------------------------------------------------");		
		System.out.println("\n-----------------testAISimulationManualProcessLevel_2()-----------------------");
		ChessMultiplayerAI chessPlayer = new ChessMultiplayerAI();
		GameApplication game = chessPlayer.startChess(new Player(TypePlayer.W), new Player(TypePlayer.B));		
		ChessServiceRemote service = new ChessServiceImpl();
		service.play(game);
		ResponseClient response = service.selectAndMovePiece("A2", "W");
		response = service.selectAndMovePiece("A4", "W");
		
		response = service.selectAndMovePiece("H7", "B");
		response = service.selectAndMovePiece("H6", "B");
		response = service.selectAndMovePiece("A1", "W");
		response = service.selectAndMovePiece("A3", "W");
		
		response = service.selectAndMovePiece("H6", "B");
		response = service.selectAndMovePiece("H5", "B");
		response = service.selectAndMovePiece("A3", "W");
		response = service.selectAndMovePiece("C3", "W");
		
		response = service.selectAndMovePiece("H5", "B");
		response = service.selectAndMovePiece("H4", "B");
		response = service.selectAndMovePiece("C3", "W");
		response = service.selectAndMovePiece("C7", "W");
		
		response = service.selectAndMovePiece("H4", "B");
		response = service.selectAndMovePiece("H3", "B");
		response = service.selectAndMovePiece("B1", "W");
		response = service.selectAndMovePiece("C3", "W");
		
		response = service.selectAndMovePiece("A7", "B");
		response = service.selectAndMovePiece("A6", "B");
		response = service.selectAndMovePiece("C3", "W");
		response = service.selectAndMovePiece("B5", "W");
		Assert.assertEquals(response.getStatus(), StatusResponse.MOVED.toString());
		Assert.assertEquals(response.getTurn(), TypePlayer.B.toString());
		game.getChessboard().printDebugChessboard(game.getChessboard(), "rook and knigh white attach, two pawn black take knight or pawn");
		
		Difficulty difficulty = new Difficulty(Difficulty.Level.LEVEL_AI_2, Difficulty.Deduction.LEVEL_DEDUCTION_1);
		AIApplication AIApp = new AIApplication(game, new PlayerMachineAI(TypePlayer.B, difficulty));
		
		Double scoreChessboardAI = PieceHelper.getTotalScoreChessboardPiecesByPlayer(
				game.getChessboard().getSquaresChessboard(), AIApp.getPlayerAI().getTypePlayer());
		Assert.assertEquals(new Double(PieceHelper.getTotalScorePiecesGameStart()-TypePiece.PAWN.getValue()), scoreChessboardAI);
		
		Double scoreChessboardPlayer = PieceHelper.getTotalScoreChessboardPiecesByPlayer(
				game.getChessboard().getSquaresChessboard(), PieceHelper.negateTypePlayer(AIApp.getPlayerAI().getTypePlayer()));
		Assert.assertEquals(new Double(PieceHelper.getTotalScorePiecesGameStart()+TypePiece.PAWN.getValue()), scoreChessboardPlayer);
		
		Map<PositionChessboard, List<PositionChessboard>> mapPiecesAndListMovements = 
				AIApp.getMapPiecesAndListMovements(game.getChessboard().getSquaresChessboard(), TypePlayer.B);
		Assert.assertEquals(mapPiecesAndListMovements.size(), 15);
		
		List<PieceResultScoreModel> listPieceResultScoreMovementsTurnAI = AIApp.getListPieceResultScoreMovementsTurnAI(
				game.getChessboard().getSquaresChessboard(), mapPiecesAndListMovements, TypePlayer.B);
		System.out.println("listPieceResultScoreMovementsTurnAI: \n" + listPieceResultScoreMovementsTurnAI);
		
		AISimulation aiSimulation = new AISimulation(AIApp.getPlayerAI(), game.getChessboard());
		
		aiSimulation.calculeSimulationBestTurn(listPieceResultScoreMovementsTurnAI);
		
		BestMovementSimulation bestMovementSimulation = aiSimulation.getBestMovementSimulation();
		
		game.getChessboard().printDebugChessboard(game.getChessboard(), "BEFORE FINISH");
		Assert.assertEquals(response.getTurn(), TypePlayer.B.toString());
		ImmutablePair<PositionChessboard, PositionChessboard> pairPositions = 
				ImmutablePair.of(bestMovementSimulation.getPositionOriginSimulated(), 
						bestMovementSimulation.getPositionDestinySimulated());
		response = service.selectAndMovePiece(pairPositions.getLeft().toString(), TypePlayer.B.toString());
		response = service.selectAndMovePiece(pairPositions.getRight().toString(), TypePlayer.B.toString());
		
		game.getChessboard().printDebugChessboard(game.getChessboard(), "FINISH");
		Assert.assertEquals(response.getStatus(), ResponseChessboard.StatusResponse.MOVED.toString());
		Assert.assertEquals(response.getTurn(), TypePlayer.W.toString());
		
		Assert.assertNotNull(bestMovementSimulation.getBestScoreSimulated());
		Assert.assertNotNull(bestMovementSimulation.getPositionOriginSimulated());
		Assert.assertNotNull(bestMovementSimulation.getPositionDestinySimulated());
		Assert.assertEquals(bestMovementSimulation.getPieceName(), TypePiece.PAWN.toString());
	}
	
	@Test
	public void testAISimulationManualProcessLevel_3() throws InterruptedException{
		System.out.println("\n------------------------------------------------------------------------------");		
		System.out.println("\n-----------------testAISimulationManualProcessLevel_3()-----------------------");
		ChessMultiplayerAI chessPlayer = new ChessMultiplayerAI();
		GameApplication game = chessPlayer.startChess(new Player(TypePlayer.W), new Player(TypePlayer.B));		
		ChessServiceRemote service = new ChessServiceImpl();
		service.play(game);
		ResponseClient response = service.selectAndMovePiece("A2", "W");
		response = service.selectAndMovePiece("A4", "W");
		
		response = service.selectAndMovePiece("H7", "B");
		response = service.selectAndMovePiece("H6", "B");
		response = service.selectAndMovePiece("A1", "W");
		response = service.selectAndMovePiece("A3", "W");
		
		response = service.selectAndMovePiece("H6", "B");
		response = service.selectAndMovePiece("H5", "B");
		response = service.selectAndMovePiece("A3", "W");
		response = service.selectAndMovePiece("C3", "W");
		
		response = service.selectAndMovePiece("H5", "B");
		response = service.selectAndMovePiece("H4", "B");
		response = service.selectAndMovePiece("C3", "W");
		response = service.selectAndMovePiece("C7", "W");
		
		response = service.selectAndMovePiece("A7", "B");
		response = service.selectAndMovePiece("A6", "B");
		response = service.selectAndMovePiece("B1", "W");
		response = service.selectAndMovePiece("C3", "W");
		
		response = service.selectAndMovePiece("A6", "B");
		response = service.selectAndMovePiece("A5", "B");
		response = service.selectAndMovePiece("C3", "W");
		response = service.selectAndMovePiece("D5", "W");
		Assert.assertEquals(response.getStatus(), StatusResponse.MOVED.toString());
		Assert.assertEquals(response.getTurn(), TypePlayer.B.toString());
		game.getChessboard().printDebugChessboard(game.getChessboard(), "rook and knigh white attach, hard to black");
		
		Difficulty difficulty = new Difficulty(Difficulty.Level.LEVEL_AI_3, Difficulty.Deduction.LEVEL_DEDUCTION_1);
		AIApplication AIApp = new AIApplication(game, new PlayerMachineAI(TypePlayer.B, difficulty));
		
		Double scoreChessboardAI = PieceHelper.getTotalScoreChessboardPiecesByPlayer(
				game.getChessboard().getSquaresChessboard(), AIApp.getPlayerAI().getTypePlayer());
		Assert.assertEquals(new Double(PieceHelper.getTotalScorePiecesGameStart()-TypePiece.PAWN.getValue()), scoreChessboardAI);
		
		Double scoreChessboardPlayer = PieceHelper.getTotalScoreChessboardPiecesByPlayer(
				game.getChessboard().getSquaresChessboard(), PieceHelper.negateTypePlayer(AIApp.getPlayerAI().getTypePlayer()));
		Assert.assertEquals(new Double(PieceHelper.getTotalScorePiecesGameStart()+TypePiece.PAWN.getValue()), scoreChessboardPlayer);
		
		Map<PositionChessboard, List<PositionChessboard>> mapPiecesAndListMovements = 
				AIApp.getMapPiecesAndListMovements(game.getChessboard().getSquaresChessboard(), TypePlayer.B);
		Assert.assertEquals(mapPiecesAndListMovements.size(), 15);
		
		List<PieceResultScoreModel> listPieceResultScoreMovementsTurnAI = AIApp.getListPieceResultScoreMovementsTurnAI(
				game.getChessboard().getSquaresChessboard(), mapPiecesAndListMovements, TypePlayer.B);
		System.out.println("listPieceResultScoreMovementsTurnAI: \n" + listPieceResultScoreMovementsTurnAI);
		
		AISimulation aiSimulation = new AISimulation(AIApp.getPlayerAI(), game.getChessboard());
		
		aiSimulation.calculeSimulationBestTurn(listPieceResultScoreMovementsTurnAI);
		
		BestSimulationTurnAI bestSimulationTurnAI = aiSimulation.getListBestSimulationTurnAI().get(0);
		Assert.assertEquals(bestSimulationTurnAI.getPieceName(), TypePiece.QUEEN.toString());
		
		BestMovementSimulation bestMovementSimulation = aiSimulation.getBestMovementSimulation();
		
		game.getChessboard().printDebugChessboard(game.getChessboard(), "BEFORE FINISH");
		Assert.assertEquals(response.getTurn(), TypePlayer.B.toString());
		ImmutablePair<PositionChessboard, PositionChessboard> pairPositions = 
				ImmutablePair.of(bestMovementSimulation.getPositionOriginSimulated(), 
						bestMovementSimulation.getPositionDestinySimulated());
		response = service.selectAndMovePiece(pairPositions.getLeft().toString(), TypePlayer.B.toString());
		response = service.selectAndMovePiece(pairPositions.getRight().toString(), TypePlayer.B.toString());
		
		game.getChessboard().printDebugChessboard(game.getChessboard(), "FINISH");
		Assert.assertEquals(response.getStatus(), ResponseChessboard.StatusResponse.MOVED.toString());
		Assert.assertEquals(response.getTurn(), TypePlayer.W.toString());
		
		Assert.assertNotNull(bestMovementSimulation.getBestScoreSimulated());
		Assert.assertNotNull(bestMovementSimulation.getPositionOriginSimulated());
		Assert.assertNotNull(bestMovementSimulation.getPositionDestinySimulated());
	}
	
	@Test
	public void testAISimulationManualProcessLevel_Difficulty_3And2() throws InterruptedException{
		System.out.println("\n------------------------------------------------------------------------------");		
		System.out.println("\n-----------------testAISimulationManualProcessLevel_Difficulty_3And2()-----------------------");
		ChessMultiplayerAI chessPlayer = new ChessMultiplayerAI();
		GameApplication game = chessPlayer.startChess(new Player(TypePlayer.W), new Player(TypePlayer.B));
		
		List<PositionChessboard> asList = Arrays.asList(H8, B8);
		game.getChessboard().removePiece(asList);
		
		ChessServiceRemote service = new ChessServiceImpl();
		service.play(game);
		ResponseClient response = service.selectAndMovePiece("A2", "W");
		response = service.selectAndMovePiece("A4", "W");
		
		response = service.selectAndMovePiece("H7", "B");
		response = service.selectAndMovePiece("H6", "B");
		response = service.selectAndMovePiece("A1", "W");
		response = service.selectAndMovePiece("A3", "W");
		
		response = service.selectAndMovePiece("H6", "B");
		response = service.selectAndMovePiece("H5", "B");
		response = service.selectAndMovePiece("A3", "W");
		response = service.selectAndMovePiece("C3", "W");
		
		response = service.selectAndMovePiece("H5", "B");
		response = service.selectAndMovePiece("H4", "B");
		response = service.selectAndMovePiece("C3", "W");
		response = service.selectAndMovePiece("C7", "W");
		
		response = service.selectAndMovePiece("A7", "B");
		response = service.selectAndMovePiece("A6", "B");
		response = service.selectAndMovePiece("B1", "W");
		response = service.selectAndMovePiece("C3", "W");
		
		response = service.selectAndMovePiece("A6", "B");
		response = service.selectAndMovePiece("A5", "B");
		response = service.selectAndMovePiece("C3", "W");
		response = service.selectAndMovePiece("D5", "W");
		Assert.assertEquals(response.getStatus(), StatusResponse.MOVED.toString());
		Assert.assertEquals(response.getTurn(), TypePlayer.B.toString());
		game.getChessboard().printDebugChessboard(game.getChessboard(), "Difficulty level AI 3 and deduction 2, best piece pawn E7 to E6");
		
		Difficulty difficulty = new Difficulty(Difficulty.Level.LEVEL_AI_3, Difficulty.Deduction.LEVEL_DEDUCTION_2);
		AIApplication AIApp = new AIApplication(game, new PlayerMachineAI(TypePlayer.B, difficulty));
		
		Double scoreChessboardAI = PieceHelper.getTotalScoreChessboardPiecesByPlayer(
				game.getChessboard().getSquaresChessboard(), AIApp.getPlayerAI().getTypePlayer());
		Assert.assertEquals(new Double(PieceHelper.getTotalScorePiecesGameStart()
				-TypePiece.PAWN.getValue()-TypePiece.ROOK.getValue()-TypePiece.KNIGHT.getValue()), scoreChessboardAI);
		
		Double scoreChessboardPlayer = PieceHelper.getTotalScoreChessboardPiecesByPlayer(
				game.getChessboard().getSquaresChessboard(), PieceHelper.negateTypePlayer(AIApp.getPlayerAI().getTypePlayer()));
		Assert.assertEquals(new Double(PieceHelper.getTotalScorePiecesGameStart()
				+TypePiece.PAWN.getValue()+TypePiece.ROOK.getValue()+TypePiece.KNIGHT.getValue()), scoreChessboardPlayer);
		
		Map<PositionChessboard, List<PositionChessboard>> mapPiecesAndListMovements = 
				AIApp.getMapPiecesAndListMovements(game.getChessboard().getSquaresChessboard(), TypePlayer.B);
		Assert.assertEquals(mapPiecesAndListMovements.size(), 13);
		
		List<PieceResultScoreModel> listPieceResultScoreMovementsTurnAI = AIApp.getListPieceResultScoreMovementsTurnAI(
				game.getChessboard().getSquaresChessboard(), mapPiecesAndListMovements, TypePlayer.B);
		System.out.println("listPieceResultScoreMovementsTurnAI: \n" + listPieceResultScoreMovementsTurnAI);
		
		AISimulation aiSimulation = new AISimulation(AIApp.getPlayerAI(), game.getChessboard());
		
		aiSimulation.calculeSimulationBestTurn(listPieceResultScoreMovementsTurnAI);
		
		BestMovementSimulation bestMovementSimulation = aiSimulation.getBestMovementSimulation();
		
		game.getChessboard().printDebugChessboard(game.getChessboard(), "BEFORE FINISH");
		Assert.assertEquals(response.getTurn(), TypePlayer.B.toString());
		ImmutablePair<PositionChessboard, PositionChessboard> pairPositions = 
				ImmutablePair.of(bestMovementSimulation.getPositionOriginSimulated(), 
						bestMovementSimulation.getPositionDestinySimulated());
		response = service.selectAndMovePiece(pairPositions.getLeft().toString(), TypePlayer.B.toString());
		response = service.selectAndMovePiece(pairPositions.getRight().toString(), TypePlayer.B.toString());
		
		game.getChessboard().printDebugChessboard(game.getChessboard(), "FINISH");
		Assert.assertEquals(response.getStatus(), ResponseChessboard.StatusResponse.MOVED.toString());
		Assert.assertEquals(response.getTurn(), TypePlayer.W.toString());
		
		Assert.assertNotNull(bestMovementSimulation.getBestScoreSimulated());
		Assert.assertEquals(bestMovementSimulation.getPositionOriginSimulated(), PositionChessboard.E7);
		Assert.assertEquals(bestMovementSimulation.getPositionDestinySimulated(), PositionChessboard.E6);
		Assert.assertEquals(bestMovementSimulation.getPieceName(), TypePiece.PAWN.toString());
	}
	
}
