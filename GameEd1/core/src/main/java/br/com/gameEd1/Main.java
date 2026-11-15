package br.com.gameEd1;

import com.badlogic.gdx.Game;
import br.com.gameEd1.MenuScreen;

/** Essa main ela serve para inicializar o game e controlar as telas*/
public class Main extends Game {

    @Override
    public void create() {
        // Inicializa o menu principal
        setScreen(new MenuScreen(this));

    }

    @Override
    public void dispose() {
        // Libera os recursos da tela atual
        super.dispose();

    }
}
