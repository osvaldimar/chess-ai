package com.dim.chess.ai.model;

import com.chess.core.enums.PositionChessboard;

public class TrackMovement {

	private Double score;
	private PositionChessboard origin;
	private PositionChessboard destiny;
	private String pieceName;
	private String turnPlayer;
	
	public TrackMovement(Double score, PositionChessboard origin, PositionChessboard destiny, String pieceName, String turnPlayer) {
		super();
		this.score = score;
		this.origin = origin;
		this.destiny = destiny;
		this.pieceName = pieceName;
		this.turnPlayer = turnPlayer;
	}
	
	public Double getScore() {
		return score;
	}
	public PositionChessboard getOrigin() {
		return origin;
	}
	public PositionChessboard getDestiny() {
		return destiny;
	}
	public String getPieceName() {
		return pieceName;
	}
	public String getTurnPlayer() {
		return turnPlayer;
	}

	@Override
	public String toString() {
		return "track{" +getScore()+ " from: " +getOrigin()+ " to: " +getDestiny()+ " " +getPieceName()+ "-" +getTurnPlayer()+ "}";
	}
}
