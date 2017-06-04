package com.dim.chess.ai.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.chess.core.enums.PositionChessboard;

public class PieceResultScoreModel {

	private String pieceName;
	private PositionChessboard positionPiece;
	private Map<PositionChessboard, Double> mapResultScoreMovementsOfPiece;

	public PieceResultScoreModel(String pieceName, PositionChessboard positionPiece,
			Map<PositionChessboard, Double> mapResultScoreMovementsOfPiece) {
		this.pieceName = pieceName;
		this.positionPiece = positionPiece;
		this.mapResultScoreMovementsOfPiece = mapResultScoreMovementsOfPiece;
	}

	public String getPieceName() {
		return pieceName;
	}

	public PositionChessboard getPositionPiece() {
		return positionPiece;
	}

	public Map<PositionChessboard, Double> getMapResultScoreMovementsOfPiece() {
		return mapResultScoreMovementsOfPiece;
	}
	
	public int getTotalResultScoreMovementsOfPiece() {
		return mapResultScoreMovementsOfPiece.size();
	}

	@Override
	public String toString() {
		return "\n" + pieceName + " origin: " + positionPiece + " mapScore: " + mapResultScoreMovementsOfPiece.toString();
	}

	public void sortedMapResultScoreMovementsOfPiece() {
		mapResultScoreMovementsOfPiece = mapResultScoreMovementsOfPiece.entrySet().stream()
				.sorted(Map.Entry.<PositionChessboard, Double>comparingByValue().reversed())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}
}
