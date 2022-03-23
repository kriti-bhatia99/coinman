package com.kriti.coinman;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Random;

public class CoinMan extends ApplicationAdapter {
	SpriteBatch batch; Random random;
	int score = 0, gameState = 0;
	BitmapFont scoreFont, gameOverFont, startFont;
	Texture background;

	Music backgroundMusic;
	Sound coinSound, gameOverSound;

	Texture[] man; Texture dizzyMan;
	int manState = 0, pause = 0, manY = 0, manX = 0;
	float gravity = 0.2f, velocity = 0;
	Rectangle manRectangle;

	ArrayList<Integer> coinXs = new ArrayList<>();
	ArrayList<Integer> coinYs = new ArrayList<>();
	ArrayList<Rectangle> coinRectangles = new ArrayList<>();
	Texture coin; int coinCount;

	ArrayList<Integer> bombXs = new ArrayList<>();
	ArrayList<Integer> bombYs = new ArrayList<>();
	ArrayList<Rectangle> bombRectangles = new ArrayList<>();
	Texture bomb; int bombCount;

	@Override
	public void create () {
		batch = new SpriteBatch();
		background = new Texture("bg.png");

		backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("spinning.mp3"));
		backgroundMusic.setLooping(true);
		backgroundMusic.setVolume(0.5f);
		backgroundMusic.play();

		coinSound = Gdx.audio.newSound(Gdx.files.internal("coin.wav"));
		gameOverSound = Gdx.audio.newSound(Gdx.files.internal("game-over.wav"));

		scoreFont = new BitmapFont();
		scoreFont.setColor(Color.WHITE);
		scoreFont.getData().setScale(5);

		gameOverFont = new BitmapFont();
		gameOverFont.setColor(Color.WHITE);
		gameOverFont.getData().setScale(10);

		startFont = new BitmapFont();
		startFont.setColor(Color.WHITE);
		startFont.getData().setScale(5);

		man = new Texture[4];
		man[0] = new Texture("frame-1.png");
		man[1] = new Texture("frame-2.png");
		man[2] = new Texture("frame-3.png");
		man[3] = new Texture("frame-4.png");

		dizzyMan = new Texture("dizzy-1.png");
		manRectangle = new Rectangle();
		manY = Gdx.graphics.getHeight()/2;

		coin = new Texture("coin.png");
		random = new Random();
		bomb = new Texture("bomb.png");
	}

	public void makeCoin()
	{
		float height = random.nextFloat() * Gdx.graphics.getHeight();
		coinYs.add((int) height);
		coinXs.add(Gdx.graphics.getWidth());
	}

	public void makeBomb()
	{
		float height = random.nextFloat() * Gdx.graphics.getHeight();
		bombYs.add((int) height);
		bombXs.add(Gdx.graphics.getWidth());
	}

	@Override
	public void render () {
		batch.begin();
		batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		if (gameState == 1)
		{
			// Game is live
			// Draw the bombs

			if (bombCount < 150)
			{
				bombCount++;
			}
			else
			{
				bombCount = 0;
				makeBomb();
			}

			bombRectangles.clear();
			for (int i = 0; i < bombXs.size(); i++)
			{
				batch.draw(bomb, bombXs.get(i), bombYs.get(i));
				bombXs.set(i, bombXs.get(i) - 8);
				bombRectangles.add(new Rectangle(bombXs.get(i), bombYs.get(i), bomb.getWidth(), bomb.getHeight()));
			}

			// Draw the coins
			if (coinCount < 100)
			{
				coinCount++;
			}
			else
			{
				coinCount = 0;
				makeCoin();
			}

			coinRectangles.clear();
			for (int i = 0; i < coinXs.size(); i++)
			{
				batch.draw(coin, coinXs.get(i), coinYs.get(i));
				coinXs.set(i, coinXs.get(i) - 4);
				coinRectangles.add(new Rectangle(coinXs.get(i), coinYs.get(i), coin.getWidth(), coin.getHeight()));
			}

			// Jumping
			if (Gdx.input.justTouched())
				velocity = -10;

			// Changing man frames
			if (pause < 8)
			{
				pause++;
			}
			else
			{
				pause = 0;
				if (manState < 3)
					manState++;
				else
					manState = 0;
			}

			velocity += gravity;
			manY -= velocity;

			if (manY <= 0)
				manY = 0;

		}
		else if (gameState == 0)
		{
			// Waiting to start
			startFont.draw(batch, "Click to start", 80, 200);
			if (Gdx.input.justTouched())
			{
				gameState = 1;
			}
		}
		else if (gameState == 2)
		{
			// Game over
			gameOverFont.draw(batch, "Game Over", 200, Gdx.graphics.getHeight()/2);
			// backgroundMusic.stop();

			if (Gdx.input.justTouched())
			{
				gameState = 1;
				manY = Gdx.graphics.getHeight() / 2;
				score = 0;
				velocity = 0;
				coinXs.clear();
				coinYs.clear();
				coinRectangles.clear();
				coinCount = 0;
			}

		}

		manX = Gdx.graphics.getWidth()/2 - man[manState].getWidth()/2;

		// Draw running man if game is live else draw the dizzy man
		if (gameState == 2)
		{
			batch.draw(dizzyMan, manX, 0);
		}
		else
		{
			batch.draw(man[manState], manX, manY);
		}

		// Check for collisions
		manRectangle = new Rectangle(manX, manY, man[manState].getWidth(), man[manState].getHeight());

		for (int i = 0; i < coinRectangles.size(); i++)
		{
			if (Intersector.overlaps(manRectangle, coinRectangles.get(i)))
			{
				Gdx.app.log("Coin", "Collision!");
				coinSound.play(0.3f);
				score++;

				coinRectangles.remove(i);
				coinXs.remove(i);
				coinYs.remove(i);
				break;
			}
		}

		for (int i = 0; i < bombRectangles.size(); i++)
		{
			if (Intersector.overlaps(manRectangle, bombRectangles.get(i)))
			{
				Gdx.app.log("Bomb", "Collision!");
				gameOverSound.play(0.3f);
				bombXs.clear();
				bombYs.clear();
				bombRectangles.clear();
				bombCount = 0;
				gameState = 2;
			}
		}

		scoreFont.draw(batch, "Score: " + String.valueOf(score), 80, Gdx.graphics.getHeight() - 80);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		backgroundMusic.dispose();
		coinSound.dispose();
		gameOverSound.dispose();
	}
}
