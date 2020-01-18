package com.jona.schiffeversenken;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class GameOverFragment extends Fragment {

	TextView tvPointsOpponent, tvPointsPlayer, winOrLose;

	int pointsOpponent, pointsPlayer;
	boolean winner;

	public GameOverFragment(int pointsplayer, int pointsopponent, boolean winner) {
		this.pointsOpponent = pointsopponent;
		this.pointsPlayer = pointsplayer;
		this.winner = winner;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_game_over, container, false);

		tvPointsOpponent = (TextView) rootView.findViewById(R.id.tv_points_opponent);
		tvPointsPlayer = (TextView) rootView.findViewById(R.id.tv_points_player);
		winOrLose = (TextView) rootView.findViewById(R.id.tv_win_or_lose);

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		tvPointsOpponent.setText("" + pointsOpponent);
		tvPointsPlayer.setText("" + pointsPlayer);

		if (winner) {
			winOrLose.setText(getResources().getString(R.string.you_win));
		} else {
			winOrLose.setText(getResources().getString(R.string.you_lose));
		}
	}

}
