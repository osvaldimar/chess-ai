package com.dim.chess.ai.model;

import com.chess.core.enums.PositionChessboard;

public class BestMovementSimulation {

	private Double bestScoreSimulated;
	private PositionChessboard positionOriginSimulated;
	private PositionChessboard positionDestinySimulated;
	private String pieceName;

	public BestMovementSimulation(String pieceName, Double bestScoreSimulated, PositionChessboard positionOriginSimulated,
			PositionChessboard positionDestinySimulated) {
		this.pieceName = pieceName;
		this.bestScoreSimulated = bestScoreSimulated;
		this.positionOriginSimulated = positionOriginSimulated;
		this.positionDestinySimulated = positionDestinySimulated;
	}

	public String getPieceName() {
		return pieceName;
	}
	
	public Double getBestScoreSimulated() {
		return bestScoreSimulated;
	}

	public PositionChessboard getPositionOriginSimulated() {
		return positionOriginSimulated;
	}

	public PositionChessboard getPositionDestinySimulated() {
		return positionDestinySimulated;
	}
	
	@Override
	public String toString() {
		return "\n" + this.getClass().getName() + " " + pieceName + " score: " + bestScoreSimulated + 
				" FROM: " + positionOriginSimulated + " TO: " + positionDestinySimulated;
	}
}
