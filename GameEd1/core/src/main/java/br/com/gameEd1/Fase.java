
package br.com.gameEd1;

// 1. IMPORTS
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;

public class Fase extends ScreenAdapter {

    // 2. VARIÁVEIS

    private SpriteBatch batch;
    private Texture chao;
    private Texture background;

    // Imagem do BOB
    private Texture bobSheet;

    // Animação de caminhada
    private Animation<TextureRegion> animacaoAndando;

    // Controle da animação
    private float tempoAnimacao = 0;

    // Posição do BOB
    private float bobX = 100;
    private float bobY = 64;

    // Velocidade
    private float velocidade = 200;

    // Direção
    private boolean olhandoDireita = true;


    // 3. CREATE
    @Override
    public void show() {

        batch = new SpriteBatch();

        // Carregar chão
        chao = new Texture("sprites/terra.png");

        // Carregar Sprite Sheet do BOB
        bobSheet = new Texture("sprites/bob.png");

        // Carregar background
        background = new Texture("ceuAzul.png");

        // Separar os quadros da animação
        TextureRegion[] quadros = new TextureRegion[4];

        quadros[0] = new TextureRegion(bobSheet, 60, 8, 32, 36);
        quadros[1] = new TextureRegion(bobSheet, 109, 8, 32, 36);
        quadros[2] = new TextureRegion(bobSheet, 158, 8, 32, 36);
        quadros[3] = new TextureRegion(bobSheet, 208, 8, 32, 36);

        // Criar animação
        animacaoAndando = new Animation<>(0.12f, quadros);

        animacaoAndando.setPlayMode(Animation.PlayMode.LOOP);
    }


    // 4. RENDER
    @Override
    public void render(float delta) {


        boolean movendo = false;

        // Andar para direita
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {

            bobX += velocidade * delta;

            olhandoDireita = true;

            movendo = true;
        }

        // Andar para esquerda
        else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {

            bobX -= velocidade * delta;

            olhandoDireita = false;

            movendo = true;
        }

        // Escolher quadro do BOB
        TextureRegion quadroAtual;

        if (movendo) {

            tempoAnimacao += delta;

            quadroAtual = new TextureRegion(
                animacaoAndando.getKeyFrame(tempoAnimacao)
            );

        } else {

            quadroAtual = new TextureRegion(
                animacaoAndando.getKeyFrames()[0]
            );
        }

        // Virar o BOB para esquerda
        if (!olhandoDireita) {

            quadroAtual.flip(true, false);
        }

        ScreenUtils.clear(0, 0, 0, 1);

        ScreenUtils.clear(0, 0, 0, 1);

        batch.begin();

        // Desenhar background
        batch.draw(background, 0, 0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        // Desenhar o chão
        for (int x = 0; x < Gdx.graphics.getWidth(); x += 64) {

            batch.draw(chao, x, 0, 64, 64);
        }

        // Desenhar o BOB
        batch.draw(quadroAtual, bobX, bobY, 64, 72);

        batch.end();
    }


    // 5. DISPOSE
    @Override
    public void dispose() {

        batch.dispose();

        chao.dispose();

        bobSheet.dispose();
    }
}
