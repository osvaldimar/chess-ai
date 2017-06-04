package com.dim.chess.machine;

import org.junit.Assert;
import org.junit.Test;

import com.chess.core.GameApplication;
import com.chess.core.ResponseChessboard.StatusResponse;
import com.chess.core.client.ChessServiceRemote;
import com.chess.core.client.ResponseClient;
import com.chess.core.enums.TypePlayer;
import com.chess.core.model.Player;
import com.chess.core.service.ChessMultiplayerAI;
import com.chess.core.service.ChessServiceImpl;
import com.dim.chess.ai.PlayerMachineAI;

public class AIApplicationTest {

	
	@Test
	public void testAIApplicationExecuteYourFistMovementBlack() throws InterruptedException{
		System.out.println("\n------------------------------------------------------------------------------");		
		System.out.println("\n-----------------testAIApplicationExecuteYourFistMovementBlack()--------------");
		//ambiente = tabuleiro
		//agente 1 = AI
		//agente 2 = human
		//percepcao = notas tabuleiro
		//acao = jogada com melhor nota
		
		ChessMultiplayerAI chessPlayer = new ChessMultiplayerAI();
		GameApplication game = chessPlayer.startChess(new Player(TypePlayer.W), new PlayerMachineAI(TypePlayer.B));		
		ChessServiceRemote service = new ChessServiceImpl();
		service.play(game);
		
		ResponseClient response = service.selectAndMovePiece("A2", "W");
		response = service.selectAndMovePiece("A4", "W");
		Assert.assertEquals(response.getStatus(), StatusResponse.MOVED.toString());
		
		service.printLayoutChessboard();
		response = service.verifyCheckmateTurn();
		Assert.assertEquals(response.getTurn(), TypePlayer.B.toString());
		
		Thread.sleep(3000);
	
		service.printLayoutChessboard();
		response = service.verifyCheckmateTurn();
		Assert.assertEquals(response.getTurn(), TypePlayer.W.toString());
	}
	
	@Test
	public void testAIApplicationExecuteYourFistMovementWhite() throws InterruptedException{
		System.out.println("\n------------------------------------------------------------------------------");		
		System.out.println("\n-----------------testAIApplicationExecuteYourFistMovementWhite()--------------");
		ChessMultiplayerAI chessPlayer = new ChessMultiplayerAI();
		GameApplication game = chessPlayer.startChess(new PlayerMachineAI(TypePlayer.W), new Player(TypePlayer.B));		
		ChessServiceRemote service = new ChessServiceImpl();
		service.play(game);
		
		//execute first movement by player 1 white
		Thread.sleep(3000);
		
		service.printLayoutChessboard();
		ResponseClient response = service.verifyCheckmateTurn();
		Assert.assertEquals(response.getTurn(), TypePlayer.B.toString());
		
		response = service.selectAndMovePiece("H7", "B");
		response = service.selectAndMovePiece("H5", "B");
		Assert.assertEquals(response.getStatus(), StatusResponse.MOVED.toString());
		
		service.printLayoutChessboard();
		response = service.verifyCheckmateTurn();
		Assert.assertEquals(response.getTurn(), TypePlayer.W.toString());
	}
}
