package com.dim.chess.ai;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.chess.core.Chessboard;
import com.chess.core.enums.PositionChessboard;
import com.chess.core.enums.TypePlayer;
import com.chess.core.model.Difficulty;
import com.dim.chess.ai.client.ChessboardDTO;
import com.dim.chess.ai.client.PositionTurn;

public class PlayerConnectAI extends PlayerMachineAI {

	private static final long serialVersionUID = -4785649513452767268L;

	private static final String URL_ISLIVE = "http://localhost:8081/app-chess-ai/isLiveChessAI";
	private static final String URL_TURNAI = "http://localhost:8081/app-chess-ai/chessboard/turnAI";

	public PlayerConnectAI() {
	}

	public PlayerConnectAI(final TypePlayer typePlayer, final Difficulty difficulty) {
		super(typePlayer, difficulty);
	}

	@Override
	public ImmutablePair<PositionChessboard, PositionChessboard> play(final Chessboard chessboard) {

		System.out.println("Method play() of PlayerConnectAI.java - chessboard");

		final RestTemplate restTemplate = new RestTemplate();
		final ResponseEntity<String> response = restTemplate.getForEntity(URL_ISLIVE, String.class);
		System.out.println("Response app-chess-ai spring boot " + URL_ISLIVE + ": " + response.getBody());

		final ChessboardDTO dto = new ChessboardDTO(chessboard.getModel(),
				new PlayerMachineAI(this.getTypePlayer(), this.getDifficulty()));
		final PositionTurn result = restTemplate.postForObject(URL_TURNAI, dto, PositionTurn.class);
		System.out.println("Response app-chess-ai spring boot " + URL_TURNAI + " - response: " + result);

		return new ImmutablePair<PositionChessboard, PositionChessboard>(PositionChessboard.getEnum(result.getOrigin()),
				PositionChessboard.getEnum(result.getDestiny()));
	}
}
