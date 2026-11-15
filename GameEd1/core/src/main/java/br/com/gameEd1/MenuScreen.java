package br.com.gameEd1;

import br.com.gameEd1.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/** Aqui é o menu principal com algumas opções */
public class MenuScreen extends ScreenAdapter {
    private Main game;
    private Stage stage;
    private Skin skin;

    private Texture backgroundTexture;

    public MenuScreen(Main game) {
        this.game = game;

        // Cria a interface
        stage = new Stage(new ScreenViewport());

        //Pegando a imagem do Background
        backgroundTexture = new Texture(
            Gdx.files.internal("ceuAzul.png")
        );

        // Cria os estilos dos componentes
        skin = criarSkin();

        // Monta o menu
        criarMenu();
    }

    private Skin criarSkin() {
        Skin skin = new Skin();

        // Fonte padrão
        BitmapFont fonte = new BitmapFont();
        fonte.getData().setScale(2);

        skin.add("default", fonte);

        // Estilo do Label
        Label.LabelStyle estiloLabel = new Label.LabelStyle();

        estiloLabel.font = fonte;
        estiloLabel.fontColor = Color.WHITE;

        skin.add("default", estiloLabel);

        // Texturas dos botões
        Texture normal = criarTextura(
            new Color(0.12f, 0.15f, 0.22f, 1)
        );

        Texture hover = criarTextura(
            new Color(0.20f, 0.35f, 0.55f, 1)
        );

        Texture pressionado = criarTextura(
            new Color(0.08f, 0.10f, 0.16f, 1)
        );

        skin.add("normal", normal);
        skin.add("hover", hover);
        skin.add("pressionado", pressionado);

        // Estilo dos botões
        TextButton.TextButtonStyle estilo =
            new TextButton.TextButtonStyle();

        estilo.up = new TextureRegionDrawable(
            new TextureRegion(normal)
        );

        estilo.over = new TextureRegionDrawable(
            new TextureRegion(hover)
        );

        estilo.down = new TextureRegionDrawable(
            new TextureRegion(pressionado)
        );

        estilo.font = fonte;
        estilo.fontColor = Color.WHITE;

        skin.add("default", estilo);

        return skin;
    }

    private Texture criarTextura(Color cor) {
        Pixmap pixmap = new Pixmap(
            1, 1, Pixmap.Format.RGBA8888
        );

        pixmap.setColor(cor);
        pixmap.fill();

        Texture textura = new Texture(pixmap);

        pixmap.dispose();

        return textura;
    }

    private void criarMenu() {

        // Imagem de fundo
        Image background = new Image(backgroundTexture);
        background.setFillParent(true);
        background.setScaling(Scaling.stretch);

        stage.addActor(background);

        // Organiza os elementos
        Table tabela = new Table();

        tabela.setFillParent(true);

        stage.addActor(tabela);

        // Título
        Label titulo = new Label(
            "MEU JOGO",
            skin
        );

        titulo.setFontScale(3);

        TextButton jogar = new TextButton("JOGAR", skin);

        // Evento do botão JOGAR
        jogar.addListener(
            new com.badlogic.gdx.scenes.scene2d.utils
                .ChangeListener() {

                @Override
                public void changed(
                    ChangeEvent event,
                    com.badlogic.gdx.scenes.scene2d.Actor actor
                ) {

                    // Abre a fase do jogo
                    game.setScreen(new Fase());

                }
            }
        );



        TextButton opcoes = new TextButton(
            "OPCOES", skin
        );

        TextButton sair = new TextButton(
            "SAIR", skin
        );

        // Espaçamento e organização
        tabela.add(titulo).padBottom(70);
        tabela.row();

        tabela.add(jogar)
            .width(300)
            .height(65)
            .padBottom(20);

        tabela.row();

        tabela.add(opcoes)
            .width(300)
            .height(65)
            .padBottom(20);

        tabela.row();

        tabela.add(sair)
            .width(300)
            .height(65);

        // Evento do botão JOGAR
        jogar.addListener(
            new com.badlogic.gdx.scenes.scene2d.utils
                .ChangeListener() {

                @Override
                public void changed(
                    ChangeEvent event,
                    com.badlogic.gdx.scenes.scene2d.Actor actor
                ) {
                    Gdx.app.log("MENU", "Iniciar jogo");
                }
            }
        );

        // Evento do botão OPCOES
        opcoes.addListener(
            new com.badlogic.gdx.scenes.scene2d.utils
                .ChangeListener() {

                @Override
                public void changed(
                    ChangeEvent event,
                    com.badlogic.gdx.scenes.scene2d.Actor actor
                ) {
                    Gdx.app.log("MENU", "Abrir opcoes");
                }
            }
        );

        // Evento do botão SAIR
        sair.addListener(
            new com.badlogic.gdx.scenes.scene2d.utils
                .ChangeListener() {

                @Override
                public void changed(
                    ChangeEvent event,
                    com.badlogic.gdx.scenes.scene2d.Actor actor
                ) {
                    Gdx.app.exit();
                }
            }
        );

    }

    @Override
    public void show() {
        // Permite que o Stage receba os cliques
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Cor do fundo
        ScreenUtils.clear(
            0.025f, 0.035f, 0.07f, 1
        );

        // Atualiza e desenha a interface
        stage.act(delta);
        stage.draw();
    }

    // Pega o tamanho da tela definidp
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(
            width, height, true
        );
    }

    // Liberacão de recursos ao sair
    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        backgroundTexture.dispose();
    }
}
