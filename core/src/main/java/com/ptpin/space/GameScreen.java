package com.ptpin.space;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import com.badlogic.gdx.Input.Keys;

public class GameScreen implements Screen {

    final SpaceGame game;
    OrthographicCamera camera;

    Texture shipImage;
    Rectangle ship;

    Texture crystalImage;
    Texture crystalGoldImage;
    // En lugar de Rectangle genérico, usamos un pequeño envoltorio (wrapper)
    // para saber si es oro o no.
    class CrystalDrop {
        Rectangle rect;
        boolean isGold;
    }
    
    Array<CrystalDrop> crystals;
    long lastDropTime;
    int score;
    int fallCount; 
    boolean gameOver;
    // Dificultad
    float baseSpeed;
    long spawnInterval;

    Sound dropSound;

    public GameScreen(final SpaceGame game) {
        this.game = game;

        // Cargar imagen de la nave
        shipImage = new Texture(Gdx.files.internal("ship.png"));
        // Cargar imagen del cristal
        crystalImage = new Texture(Gdx.files.internal("crystal.png"));
        crystalGoldImage = new Texture(Gdx.files.internal("crystal_gold.png"));

        // Definir el rectángulo de colisión de la nave
        ship = new Rectangle();
        ship.x = 800 / 2 - 64 / 2;
        ship.y = 20;
        ship.width = 64;
        ship.height = 64;

        // Cargar sonidos
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));

        // Inicializamos los puntos y el estado del juego
        score = 0;
        fallCount = 0;
        gameOver = false;
        
        // Dificultad inicial
        baseSpeed = 200f; // 200 pixeles por segundo
        spawnInterval = 1000000000L; // 1 segundo

        crystals = new Array<>();
        spawnCrystal(); // Crearemos este método enseguida para generar el primer cristal

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.1f, 1); // Fondo del espacio
        camera.update();
        
        // 1. DIBUJAR
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        
        if (!gameOver) {
            game.font.draw(game.batch, "Puntos: " + score + " | Perdidos: " + fallCount + "/3", 10, 470);
            game.batch.draw(shipImage, ship.x, ship.y);
            for (CrystalDrop drop : crystals) {
                if(drop.isGold) {
                    game.batch.draw(crystalGoldImage, drop.rect.x, drop.rect.y);
                } else {
                    game.batch.draw(crystalImage, drop.rect.x, drop.rect.y);
                }
            }
        } else {
            // Lógica de dibujar cuando el juego termina
            game.font.draw(game.batch, "¡FIN DEL JUEGO!", 300, 280);
            game.font.draw(game.batch, "Puntuación Final: " + score, 280, 230);
            game.font.draw(game.batch, "Toca la pantalla para volver a jugar", 200, 180);
        }
        
        game.batch.end();

        // Si el juego ha terminado, solo esperamos un click para reiniciar
        if (gameOver) {
            if (Gdx.input.isTouched()) {
                game.setScreen(new GameScreen(game));
                dispose();
            }
            return; // Detenemos la función render aquí para que no se mueva nada
        }

        // 2. LÓGICA / MOVIMIENTO (Solo si no estamos en Game Over)
        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            ship.x -= 400 * delta;
        }
        if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            ship.x += 400 * delta;
        }

        // 3. LIMITES DE PANTALLA
        if (ship.x < 0) { ship.x = 0; }
        if (ship.x > 800 - 64) { ship.x = 800 - 64; }

        // 4. LÓGICA DE CRISTALES (Generación y colisiones)
        
        // Comprobar si debemos generar otro basándonos en el spawnInterval en lugar de 1 segundo estático
        if (TimeUtils.nanoTime() - lastDropTime > spawnInterval) {
            spawnCrystal();
        }

        // Iterar los cristales: moverlos hacia abajo y comprobar si los atrapamos
        for (int i = 0; i < crystals.size; i++) {
            CrystalDrop drop = crystals.get(i);
            Rectangle rect = drop.rect;
            
            // Los dorados bajan un 50% más rápido que los normales
            float currentSpeed = drop.isGold ? baseSpeed * 1.5f : baseSpeed;
            rect.y -= currentSpeed * delta;

            // Si cae por debajo del borde de la pantalla, desaparece y penaliza
            if (rect.y + 64 < 0) {
                fallCount++; // Sumamos a la lista de "perdidos"
                crystals.removeIndex(i);
                i--;
                
                // Si caen 3 cristales, perdemos el juego
                if (fallCount >= 3) {
                    gameOver = true;
                }
            } 
            // Si nuestro rectángulo de la nave (AABB) choca con el rectángulo del cristal
            else if (rect.overlaps(ship)) {
                // Ganamos 1 punto por el normal, 3 por el dorado
                score += drop.isGold ? 3 : 1; 
                dropSound.play();
                crystals.removeIndex(i); 
                i--; 
                
                // === DIFICULTAD PROGRESIVA ===
                baseSpeed += 5f; 
                if (spawnInterval > 400000000L) { // Límite máximo: 0.4 segundos
                    spawnInterval -= 15000000L;
                }
            }
        }
    }

    // Método de utilidad para crear un nuevo cristal aleatorio
    private void spawnCrystal() {
        CrystalDrop drop = new CrystalDrop();
        drop.rect = new Rectangle();
        drop.rect.x = MathUtils.random(0, 800 - 64);
        drop.rect.y = 480;
        drop.rect.width = 64;
        drop.rect.height = 64;
        
        // 20% de probabilidad (1 de cada 5) de que sea dorado
        drop.isGold = MathUtils.random(1, 5) == 1;
        
        crystals.add(drop);
        lastDropTime = TimeUtils.nanoTime(); 
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override 
    public void dispose() {
        shipImage.dispose();
        crystalImage.dispose();
        crystalGoldImage.dispose();
        dropSound.dispose();
    }
}
