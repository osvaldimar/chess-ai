package com.dim.chess.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.chess.core.Chessboard;
import com.chess.core.GameApplication;
import com.chess.core.ResponseChessboard;
import com.chess.core.client.ChessServiceRemote;
import com.chess.core.client.ResponseClient;
import com.chess.core.enums.PositionChessboard;
import com.chess.core.enums.TypePiece;
import com.chess.core.enums.TypePlayer;
import com.chess.core.exception.CheckMoveException;
import com.chess.core.exception.CheckStateException;
import com.chess.core.helper.PieceHelper;
import com.chess.core.model.ChessboardModel;
import com.chess.core.model.Piece;
import com.chess.core.model.Player;
import com.chess.core.model.Queen;
import com.chess.core.model.Square;
import com.chess.core.service.ChessMultiplayerOnline;
import com.chess.core.service.ChessServiceImpl;
import com.chess.core.util.ChessboardPieceFactory;
import com.dim.chess.ai.model.BestMovementSimulation;
import com.dim.chess.ai.model.PieceResultScoreModel;

public class AIApplication {
	
	private ChessServiceRemote service = new ChessServiceImpl();
	private GameApplication game;
	private ResponseClient responseClient;
	private PlayerMachineAI playerAI;
	
	public AIApplication(GameApplication game, PlayerMachineAI playerAI){
		this.game = game;
		this.playerAI = playerAI;
		this.service.play(game);
	}
	
	public AIApplication(Chessboard chessboard, PlayerMachineAI playerAI) {
		ChessMultiplayerOnline chessOn = new ChessMultiplayerOnline();
		GameApplication game = chessOn.startChess(
				new Player(chessboard.getPlayer1().getTypePlayer()), 
				new Player(chessboard.getPlayer2().getTypePlayer())
		);
		game.getChessboard().setModel(ChessboardPieceFactory.cloneDeepGeneric(chessboard.getModel()));
		game.setTurnPlayer(playerAI);
		this.game = game;
		this.playerAI = playerAI;
		this.service.play(game);
	}

	public ImmutablePair<PositionChessboard, PositionChessboard> playAI(){
		
		System.out.println("playAI ...");
		Map<PositionChessboard, List<PositionChessboard>> mapPiecesAndListMovements = 
				getMapPiecesAndListMovements(game.getChessboard().getSquaresChessboard(), playerAI.getTypePlayer());
		
		List<PieceResultScoreModel> listPieceResultScoreMovementsTurnAI = getListPieceResultScoreMovementsTurnAI(
				game.getChessboard().getSquaresChessboard(), mapPiecesAndListMovements, playerAI.getTypePlayer());
		
		AISimulation aiSimulation = new AISimulation(playerAI, game.getChessboard());
		aiSimulation.calculeSimulationBestTurn(listPieceResultScoreMovementsTurnAI);
		
		BestMovementSimulation bestMovementSimulation = aiSimulation.getBestMovementSimulation();
		//executeBestMovementCalculated(bestMovementSimulation);
		
		return ImmutablePair.of(bestMovementSimulation.getPositionOriginSimulated(), bestMovementSimulation.getPositionDestinySimulated());
	}
	
	public void executeBestMovementCalculated(BestMovementSimulation bestMovementSimulation) {
		System.out.println("\nMethod executeBestMovementCalculated(): " + bestMovementSimulation);
		this.service.selectAndMovePiece(bestMovementSimulation.getPositionOriginSimulated().toString(), 
				this.playerAI.getTypePlayer().toString());
		this.responseClient = this.service.selectAndMovePiece(bestMovementSimulation.getPositionDestinySimulated().toString(), 
				this.playerAI.getTypePlayer().toString());
		//validate promotion pawn
		if(ResponseChessboard.StatusResponse.PAWN_PROMOTION.toString().equals(this.responseClient.getStatus())){
			this.service.choosePromotion(TypePiece.QUEEN.toString(), this.playerAI.getTypePlayer().toString());
		}
	}
	
	public ResponseClient getResponseClient() {
		return responseClient;
	}

	public Map<PositionChessboard, List<PositionChessboard>> getMapPiecesAndListMovements(
			Square[][] squares, TypePlayer type, Set<PositionChessboard> setMovementsAllowedToSimulation) {
		Map<PositionChessboard, List<PositionChessboard>> mapPiecesAndListMovements = this.getMapPiecesAndListMovements(squares, type);
		mapPiecesAndListMovements.entrySet().stream().forEach(entry -> {
			entry.getValue().removeIf(f -> !setMovementsAllowedToSimulation.contains(f));
		});
		return mapPiecesAndListMovements;
	}
	
	public Map<PositionChessboard, List<PositionChessboard>> getMapPiecesAndListMovements(
			Square[][] squares, TypePlayer type) {
		
		List<Square> listSquareOfAllMyPieces = new ArrayList<>();
		for(Square[] s : squares){
			listSquareOfAllMyPieces.addAll(Arrays.stream(s).filter(f -> !f.isAvailable())
			.filter(p -> p.getPiece().getPlayer()==type).collect(Collectors.toList()));
		}
		
		Map<PositionChessboard, List<PositionChessboard>> mapPiecesAndListMovements = new HashMap<>();
		listSquareOfAllMyPieces.forEach(s -> {			
			List<PositionChessboard> list = new ArrayList<>();
			list.addAll(s.getPiece().movementAvailable(s.getPosition(), squares));
			list.addAll(s.getPiece().movementAvailableToTakePieces(s.getPosition(), squares));
			mapPiecesAndListMovements.put(s.getPosition(), list);
		});		
		return mapPiecesAndListMovements;
	}
	
	//public 

	public List<PieceResultScoreModel> getListPieceResultScoreMovementsTurnAI(Square[][] squares,
			Map<PositionChessboard, List<PositionChessboard>> mapPiecesAndListMovements, TypePlayer type) {
		
		//Map<PositionChessboard, Map<PositionChessboard, Double>> mapResultScoreAllPieces = new HashMap<>();	
		List<PieceResultScoreModel> listPieceResultScoreModel = new ArrayList<>();
		
		this.game.getChessboard().setSquares(squares);
		ChessboardModel modelReal = ChessboardPieceFactory.cloneDeepGeneric(this.game.getChessboard().getModel());
		
		//my pieces position origin
		for(PositionChessboard keyOrigin : mapPiecesAndListMovements.keySet()){
			Map<PositionChessboard, Double> mapResultScoreMovementsOfPiece = new HashMap<>();
			mapPiecesAndListMovements.get(keyOrigin).forEach(destiny -> {
				ChessboardModel modelClone = ChessboardPieceFactory.cloneDeepGeneric(modelReal);
				this.game.getChessboard().setModel(modelClone);
				try {
					this.game.getChessboard().movePieceInTheChessboard(keyOrigin, destiny, this.game.getChessboard().getSquareChessboard(keyOrigin).getPiece());
					Double value = PieceHelper.getTotalScoreChessboardPiecesByPlayer(modelClone.getSquares(), type);
					mapResultScoreMovementsOfPiece.put(destiny, value);
					
				} catch (CheckMoveException e) {
					System.out.println("IGNORED keyOrigin: " + keyOrigin + " - destiny: " + destiny + " - piece: " 
							+ this.game.getChessboard().getSquareChessboard(keyOrigin).getPiece().getTypePiece());
				} catch (CheckStateException s){
					System.out.println("IGNORED: keyOrigin: " + keyOrigin + " - destiny: " + destiny + " - piece: " 
							+ this.game.getChessboard().getSquareChessboard(keyOrigin).getPiece().getTypePiece());
				}
			});
			PieceResultScoreModel pieceResultScoreModel = new PieceResultScoreModel(
					squares[keyOrigin.getLetter()][keyOrigin.getNumber()].getPiece().getTypePiece().toString(), 
					keyOrigin, mapResultScoreMovementsOfPiece);
			listPieceResultScoreModel.add(pieceResultScoreModel);
		}
		
		this.game.getChessboard().setModel(modelReal);		
		return listPieceResultScoreModel;
	}	
	
	public PlayerMachineAI getPlayerAI() {
		return playerAI;
	}
}
