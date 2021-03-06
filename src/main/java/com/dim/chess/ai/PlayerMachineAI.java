package com.dim.chess.ai;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.chess.core.Chessboard;
import com.chess.core.client.PlayerMode;
import com.chess.core.enums.PositionChessboard;
import com.chess.core.enums.TypePlayer;
import com.chess.core.model.Difficulty;

public class PlayerMachineAI extends PlayerMode {

	private static final long serialVersionUID = 6512889892985044671L;

	private Difficulty difficulty;

	public PlayerMachineAI() {
	}

	public PlayerMachineAI(final TypePlayer typePlayer) {
		super(typePlayer);
		this.difficulty = new Difficulty(Difficulty.Level.LEVEL_AI_1, Difficulty.Deduction.LEVEL_DEDUCTION_1);
	}

	public PlayerMachineAI(final TypePlayer typePlayer, final Difficulty difficulty) {
		super(typePlayer);
		this.difficulty = difficulty;
	}

	@Override
	public boolean isAI() {
		return true;
	}

	@Override
	public ImmutablePair<PositionChessboard, PositionChessboard> play(final Chessboard chessboard) {
		System.out.println("Method play() of PlayerMachineAI.java - chessboard");
		final ImmutablePair<PositionChessboard, PositionChessboard> pairPositions = new AIApplication(chessboard, this)
				.playAI();
		return pairPositions;
	}

	public Difficulty getDifficulty() {
		return this.difficulty;
	}
}
