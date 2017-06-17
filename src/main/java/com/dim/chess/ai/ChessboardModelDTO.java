package com.dim.chess.ai;

import java.io.Serializable;

import com.chess.core.model.ChessboardModel;

public class ChessboardModelDTO implements Serializable {

	private static final long serialVersionUID = 3212570184134876555L;

	private ChessboardModel chessboardModel;
	private PlayerMachineAI playerAI;

	public ChessboardModelDTO(final ChessboardModel model, final PlayerMachineAI playerAI) {
		this.chessboardModel = model;
		this.playerAI = playerAI;
	}

	public ChessboardModel getChessboardModel() {
		return this.chessboardModel;
	}

	public void setChessboardModel(final ChessboardModel chessboardModel) {
		this.chessboardModel = chessboardModel;
	}

	public PlayerMachineAI getPlayerAI() {
		return this.playerAI;
	}

	public void setPlayerAI(final PlayerMachineAI playerAI) {
		this.playerAI = playerAI;
	}

}
