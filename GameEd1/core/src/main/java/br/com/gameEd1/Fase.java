package br.com.gameEd1;

// 1. IMPORTS
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

public class Fase extends ScreenAdapter {

    // Estados do teletransporte
    private enum EstadoTeleporte {
        NENHUM,       // jogando normalmente
        SUMINDO,      // animação de desaparecer (esquerda -> direita)
        ESCOLHENDO,   // mago invisível, jogador move a luz
        APARECENDO    // animação de aparecer (direita -> esquerda)
    }

    // 2. VARIÁVEIS

    private SpriteBatch batch;
    private Texture chao;
    private Texture background;

    // ---------- MAGO ----------
    private Texture magoSheet;

    private Animation<TextureRegion> animacaoAndando;    // linha 1 (8 quadros)
    private Animation<TextureRegion> animacaoPoder;      // linha 2 (11 quadros)
    private TextureRegion[] quadrosPulo;                 // linha 3 (8 quadros)
    private Animation<TextureRegion> animacaoSumindo;    // linha 4, da esquerda p/ direita
    private Animation<TextureRegion> animacaoAparecendo; // linha 4, da direita p/ esquerda

    // Tamanho de referência da imagem do mago (a imagem que você mandou no chat).
    // As coordenadas abaixo foram medidas nessa referência e são convertidas
    // automaticamente se o arquivo real tiver outra resolução (mesma proporção).
    private static final float REF_LARGURA = 1410f;
    private static final float REF_ALTURA = 768f;

    // Metade da largura de cada "célula" de quadro, na referência
    private static final float MEIA_CELULA = 56f;

    // Quantos pixels da tela vale 1 pixel da referência (controla o tamanho do mago)
    private static final float ESCALA_DESENHO = 0.7f;

    // Fator tela/pixel real da textura (calculado no show())
    private float escalaTela = 1f;

    // Controle das animações
    private float tempoAnimacao = 0;
    private float tempoPoder = 0;
    private float tempoPouso = 0;
    private boolean lancandoPoder = false;

    // Posição do MAGO (magoX = lado esquerdo da hitbox, magoY = pés)
    private float magoX = 100;
    private float magoY = 64;

    // Hitbox do MAGO
    private final float hitLargura = 36;
    private final float hitAltura = 72;

    // Velocidade
    private float velocidade = 200;

    // Controle do pulo
    private float velocidadePulo = 400;

    // Gravidade
    private float gravidade = 1000;

    // Velocidade vertical
    private float velocidadeY = 0;

    // Verificar se o mago está no chão
    private boolean estaNoChao = true;

    // Direção
    private boolean olhandoDireita = true;

    // ---------- TELETRANSPORTE ----------
    private static final int CARGAS_TELEPORTE = 2;
    private static final float VELOCIDADE_LUZ = 300f;

    private EstadoTeleporte estadoTP = EstadoTeleporte.NENHUM;
    private int teletransportes = 0;   // quantas vezes ainda pode teletransportar
    private float tempoTP = 0;

    // Posição da luz que mostra onde o mago vai aparecer (cursorY = pés)
    private float cursorX = 0;
    private float cursorY = 64;

    private Texture texBrilho;   // luz da escolha
    private Texture texBolinha;  // bolinhas roxas (tentativas)

    // ---------- ÍCONE DO TELETRANSPORTE (sai do baú) ----------
    private Texture teleporteSheet;
    private Animation<TextureRegion> animacaoItem;

    private final float itemTam = 64;     // tamanho desenhado
    private final float itemHit = 40;     // área para pegar o ícone
    private boolean itemSurgiu = false;
    private boolean itemColetado = false;
    private float tempoItem = 0;

    // ---------- BAÚ ----------
    private Texture bauSheet;
    private Animation<TextureRegion> animacaoBau;

    // Posição e tamanho do baú
    // bauY = 64 -> baú no chão (abre pulando em cima).
    // Para abrir por baixo, deixe o baú flutuando, ex: bauY = 150.
    private float bauX = 400;
    private float bauY = 64;
    private final float bauLargura = 64;
    private final float bauAltura = 64;

    // Estado do baú
    private boolean bauAbrindo = false;
    private float tempoBau = 0;


    // 3. CREATE
    @Override
    public void show() {

        batch = new SpriteBatch();

        // Carregar chão
        chao = new Texture("sprites/terra.png");

        // Carregar background
        background = new Texture("ceuAzul.png");

        // ---------- MAGO ----------
        carregarMago();

        // ---------- BAÚ ----------
        bauSheet = carregarSemMagenta("sprites/bau.jpg", false);
        bauSheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        int larguraQuadro = bauSheet.getWidth() / 4;
        int alturaQuadro = bauSheet.getHeight();

        TextureRegion[] quadrosBau = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            quadrosBau[i] = new TextureRegion(bauSheet, i * larguraQuadro, 0, larguraQuadro, alturaQuadro);
        }

        animacaoBau = new Animation<>(0.12f, quadrosBau);
        animacaoBau.setPlayMode(Animation.PlayMode.NORMAL);

        // ---------- ÍCONE DO TELETRANSPORTE ----------
        teleporteSheet = carregarSemMagenta("sprites/teleporte.jpg", true);
        teleporteSheet.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        int larguraIcone = teleporteSheet.getWidth() / 4;
        int alturaIcone = teleporteSheet.getHeight();

        TextureRegion[] quadrosItem = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            quadrosItem[i] = new TextureRegion(teleporteSheet, i * larguraIcone, 0, larguraIcone, alturaIcone);
        }

        animacaoItem = new Animation<>(0.15f, quadrosItem);
        animacaoItem.setPlayMode(Animation.PlayMode.LOOP);

        // ---------- LUZ E BOLINHAS ----------
        texBrilho = criarTexturaBrilho();
        texBolinha = criarTexturaBolinha();
    }


    // 4. RENDER
    @Override
    public void render(float delta) {

        boolean movendo = false;

        switch (estadoTP) {

            case NENHUM:
                movendo = atualizarMago(delta);
                verificarItem();
                break;

            case SUMINDO:
                tempoTP += delta;

                if (animacaoSumindo.isAnimationFinished(tempoTP)) {
                    estadoTP = EstadoTeleporte.ESCOLHENDO;
                    tempoTP = 0;

                    // a luz começa onde o mago estava
                    cursorX = magoX + hitLargura / 2;
                    cursorY = magoY;
                }
                break;

            case ESCOLHENDO:
                atualizarEscolha(delta);
                break;

            case APARECENDO:
                tempoTP += delta;

                if (animacaoAparecendo.isAnimationFinished(tempoTP)) {
                    estadoTP = EstadoTeleporte.NENHUM;
                }
                break;
        }

        // Atualizar animação do baú
        if (bauAbrindo) {
            tempoBau += delta;
        }

        // Quando o baú termina de abrir, o ícone surge
        if (bauAbrindo && !itemSurgiu && animacaoBau.isAnimationFinished(tempoBau)) {
            itemSurgiu = true;
            tempoItem = 0;
        }
        if (itemSurgiu && !itemColetado) {
            tempoItem += delta;
        }

        TextureRegion quadroMago = escolherQuadroMago(movendo);
        TextureRegion quadroBau = animacaoBau.getKeyFrame(tempoBau);

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

        // Desenhar o baú
        batch.draw(quadroBau, bauX, bauY, bauLargura, bauAltura);

        // Desenhar o ícone flutuando
        if (itemSurgiu && !itemColetado) {
            batch.draw(animacaoItem.getKeyFrame(tempoItem), itemX(), itemY(), itemTam, itemTam);
        }

        // Desenhar o MAGO (null = invisível, enquanto escolhe onde aparecer)
        if (quadroMago != null) {
            desenharMago(quadroMago);
        }

        // Desenhar a luz da escolha do teletransporte
        if (estadoTP == EstadoTeleporte.ESCOLHENDO) {
            desenharLuz();
        }

        // Desenhar as bolinhas roxas (tentativas restantes)
        for (int i = 0; i < teletransportes; i++) {
            batch.draw(texBolinha,
                20 + i * 28,
                Gdx.graphics.getHeight() - 44,
                22, 22
            );
        }

        batch.end();
    }


    // ---------- LÓGICA DO MAGO ----------

    // Entrada, física e colisões. Devolve true se o mago está andando.
    private boolean atualizarMago(float delta) {

        float magoXAnterior = magoX;
        float magoYAnterior = magoY;
        boolean estavaNoChao = estaNoChao;

        boolean movendo = false;

        // TECLA X: teletransporte (se tiver) ou animação de poder
        if (Gdx.input.isKeyJustPressed(Input.Keys.X) && !lancandoPoder) {

            if (teletransportes > 0) {

                iniciarTeleporte();

                return false;

            } else if (estaNoChao) {

                lancandoPoder = true;

                tempoPoder = 0;
            }
        }

        // Andar (o mago fica parado enquanto lança o poder)
        if (!lancandoPoder) {

            // Andar para direita
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {

                magoX += velocidade * delta;

                olhandoDireita = true;

                movendo = true;
            }

            // Andar para esquerda
            else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {

                magoX -= velocidade * delta;

                olhandoDireita = false;

                movendo = true;
            }
        }

        // Colisão lateral com o baú: ele é sólido
        if (colideComBau()) {
            magoX = magoXAnterior;
        }

        // PULAR COM A TECLA ESPAÇO
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && estaNoChao && !lancandoPoder) {

            velocidadeY = velocidadePulo;

            estaNoChao = false;
        }

        // APLICAR GRAVIDADE
        velocidadeY -= gravidade * delta;
        magoY += velocidadeY * delta;
        estaNoChao = false;

        // VERIFICAR SE O MAGO CHEGOU AO CHÃO
        if (magoY <= 64) {

            magoY = 64;

            velocidadeY = 0;

            estaNoChao = true;
        }

        // Colisão vertical com o baú
        if (colideComBau()) {

            float topoBau = bauY + bauAltura;

            if (velocidadeY > 0 && magoYAnterior + hitAltura <= bauY) {
                // Veio de BAIXO e bateu a cabeça no baú
                magoY = bauY - hitAltura;
                velocidadeY = 0;
                abrirBau();

            } else if (velocidadeY <= 0 && magoYAnterior >= topoBau) {
                // Veio de CIMA e pousou no baú
                magoY = topoBau;
                velocidadeY = 0;
                estaNoChao = true;
                abrirBau();
            }
        }

        // Acabou de pousar: toca os quadros de pouso por um instante
        if (estaNoChao && !estavaNoChao) {
            tempoPouso = 0.14f;
        }
        if (tempoPouso > 0) {
            tempoPouso -= delta;
        }

        return movendo;
    }

    // Escolhe o quadro do mago conforme o estado atual (null = não desenhar)
    private TextureRegion escolherQuadroMago(boolean movendo) {

        switch (estadoTP) {

            case SUMINDO:
                return animacaoSumindo.getKeyFrame(tempoTP);

            case ESCOLHENDO:
                return null;

            case APARECENDO:
                return animacaoAparecendo.getKeyFrame(tempoTP);

            default:
                break;
        }

        if (lancandoPoder) {

            tempoPoder += Gdx.graphics.getDeltaTime();

            TextureRegion quadro = animacaoPoder.getKeyFrame(tempoPoder);

            if (animacaoPoder.isAnimationFinished(tempoPoder)) {
                lancandoPoder = false;
            }

            return quadro;
        }

        if (!estaNoChao) {

            // Pulando: o quadro depende da velocidade vertical
            if (velocidadeY > 250) {
                return quadrosPulo[2]; // impulso
            } else if (velocidadeY > 80) {
                return quadrosPulo[3]; // subindo
            } else if (velocidadeY >= -80) {
                return quadrosPulo[4]; // topo do pulo
            } else {
                return quadrosPulo[5]; // caindo
            }
        }

        if (movendo) {

            tempoAnimacao += Gdx.graphics.getDeltaTime();

            return animacaoAndando.getKeyFrame(tempoAnimacao);
        }

        if (tempoPouso > 0) {

            return tempoPouso > 0.07f ? quadrosPulo[6] : quadrosPulo[7];
        }

        return animacaoAndando.getKeyFrames()[0];
    }

    // Desenha o mago centralizado na hitbox, com os pés em magoY
    private void desenharMago(TextureRegion quadro) {

        float largura = quadro.getRegionWidth() * escalaTela;
        float altura = quadro.getRegionHeight() * escalaTela;
        float centroX = magoX + hitLargura / 2;

        if (olhandoDireita) {
            batch.draw(quadro, centroX - largura / 2, magoY, largura, altura);
        } else {
            // largura negativa = espelhado
            batch.draw(quadro, centroX + largura / 2, magoY, -largura, altura);
        }
    }


    // ---------- TELETRANSPORTE ----------

    // Primeiro X: gasta uma tentativa e o mago começa a sumir
    private void iniciarTeleporte() {

        teletransportes--;

        estadoTP = EstadoTeleporte.SUMINDO;

        tempoTP = 0;

        velocidadeY = 0;
    }

    // Mago invisível: setas movem a luz, X confirma
    private void atualizarEscolha(float delta) {

        tempoTP += delta; // usado para a luz pulsar

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) cursorX += VELOCIDADE_LUZ * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  cursorX -= VELOCIDADE_LUZ * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.UP))    cursorY += VELOCIDADE_LUZ * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))  cursorY -= VELOCIDADE_LUZ * delta;

        // A luz nunca passa do chão nem sai da tela
        float metade = hitLargura / 2;
        cursorX = Math.max(metade, Math.min(Gdx.graphics.getWidth() - metade, cursorX));
        cursorY = Math.max(64, Math.min(Gdx.graphics.getHeight() - hitAltura, cursorY));

        // Segundo X: o mago aparece onde está a luz
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            confirmarTeleporte();
        }
    }

    private void confirmarTeleporte() {

        magoX = cursorX - hitLargura / 2;
        magoY = cursorY;

        // Se a luz estava dentro do baú, o mago aparece em cima dele
        if (colideComBau()) {
            magoY = bauY + bauAltura;
        }

        velocidadeY = 0;

        estaNoChao = magoY <= 64;

        estadoTP = EstadoTeleporte.APARECENDO;

        tempoTP = 0;
    }

    // Luz com brilho aditivo, pulsando
    private void desenharLuz() {

        float pulso = 1f + 0.15f * (float) Math.sin(tempoTP * 8f);

        float centroX = cursorX;
        float centroY = cursorY + hitAltura / 2;

        float grande = 70 * pulso;
        float nucleo = 26 * pulso;

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        batch.draw(texBrilho, centroX - grande / 2, centroY - grande / 2, grande, grande);
        batch.draw(texBrilho, centroX - nucleo / 2, centroY - nucleo / 2, nucleo, nucleo);

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }


    // ---------- ÍCONE DO TELETRANSPORTE ----------

    private float itemX() {
        return bauX + bauLargura / 2 - itemTam / 2;
    }

    // Sobe do baú até ficar flutuando acima dele
    private float itemY() {

        float inicio = bauY + bauAltura * 0.4f;
        float fim = bauY + bauAltura + 20;

        float subida = Math.min(1f, tempoItem / 0.5f);

        float y = inicio + (fim - inicio) * subida;

        if (subida >= 1f) {
            y += (float) Math.sin(tempoItem * 4f) * 6f;
        }

        return y;
    }

    // Mago encostou no ícone: ganha as tentativas
    private void verificarItem() {

        if (!itemSurgiu || itemColetado) return;

        float ix = itemX() + (itemTam - itemHit) / 2;
        float iy = itemY() + (itemTam - itemHit) / 2;

        boolean encostou = magoX < ix + itemHit
            && magoX + hitLargura > ix
            && magoY < iy + itemHit
            && magoY + hitAltura > iy;

        if (encostou) {
            itemColetado = true;
            teletransportes = CARGAS_TELEPORTE;
        }
    }


    // ---------- CARREGAMENTO DE IMAGENS ----------

    // Carrega o sprite sheet do mago, tira o fundo branco e fatia as animações
    private void carregarMago() {

        Pixmap original = new Pixmap(Gdx.files.internal("sprites/mago.jpg"));
        Pixmap pixmap = new Pixmap(original.getWidth(), original.getHeight(), Pixmap.Format.RGBA8888);
        pixmap.drawPixmap(original, 0, 0);
        original.dispose();

        pixmap.setBlending(Pixmap.Blending.None);

        // Fundo branco -> transparente (só o branco ligado às bordas; a barba do mago fica)
        removerFundoBranco(pixmap);

        float sx = pixmap.getWidth() / REF_LARGURA;
        float sy = pixmap.getHeight() / REF_ALTURA;

        // Apaga o texto "PULANDO", que encosta na área do pulo
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fillRectangle(
            Math.round(15 * sx), Math.round(392 * sy),
            Math.round(125 * sx), Math.round(38 * sy)
        );

        magoSheet = new Texture(pixmap);
        magoSheet.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Linha 1: andando (8 quadros)
        TextureRegion[] andando = fatiarLinha(magoSheet, pixmap, 75f, 113.3f, 8, 50f, 170f, sx, sy, true);

        // Linha 2: lançando poder (11 quadros)
        TextureRegion[] poder = fatiarLinha(magoSheet, pixmap, 75f, 113.7f, 11, 234f, 352f, sx, sy, true);

        // Linha 3: pulando (8 quadros)
        quadrosPulo = fatiarLinha(magoSheet, pixmap, 72f, 113.7f, 8, 388f, 555f, sx, sy, true);

        // Linha 4: desaparecendo (8 quadros). Todos os quadros usam a mesma altura,
        // senão as partículas "cairiam" até o chão.
        TextureRegion[] sumindo = fatiarLinha(magoSheet, pixmap, 75f, 113.7f, 8, 622f, 748f, sx, sy, false);

        pixmap.dispose();

        animacaoAndando = new Animation<>(0.1f, andando);
        animacaoAndando.setPlayMode(Animation.PlayMode.LOOP);

        animacaoPoder = new Animation<>(0.07f, poder);
        animacaoPoder.setPlayMode(Animation.PlayMode.NORMAL);

        // Sumir: esquerda -> direita
        animacaoSumindo = new Animation<>(0.08f, sumindo);
        animacaoSumindo.setPlayMode(Animation.PlayMode.NORMAL);

        // Aparecer: os mesmos quadros, da direita -> esquerda
        animacaoAparecendo = new Animation<>(0.08f, sumindo);
        animacaoAparecendo.setPlayMode(Animation.PlayMode.REVERSED);

        // pixels da tela por pixel real da textura
        escalaTela = ESCALA_DESENHO / sx;
    }

    // Corta uma linha do sprite sheet. Cada quadro tem largura fixa (centrada no
    // personagem). Se pesPorQuadro = true, a altura se ajusta a cada desenho (base nos pés);
    // se false, todos os quadros da linha usam a mesma altura.
    private TextureRegion[] fatiarLinha(Texture tex, Pixmap p, float primeiroCentro, float passo,
                                        int quantidade, float topo, float base, float sx, float sy,
                                        boolean pesPorQuadro) {

        int[] xs0 = new int[quantidade];
        int[] xs1 = new int[quantidade];
        int[] minYs = new int[quantidade];
        int[] maxYs = new int[quantidade];
        boolean[] valido = new boolean[quantidade];

        int y0 = Math.max(0, Math.round(topo * sy));
        int y1 = Math.min(p.getHeight(), Math.round(base * sy));

        for (int i = 0; i < quantidade; i++) {

            float centro = primeiroCentro + passo * i;

            int x0 = Math.max(0, Math.round((centro - MEIA_CELULA) * sx) + 1);
            int x1 = Math.min(p.getWidth(), Math.round((centro + MEIA_CELULA) * sx) + 1);

            // Procura a primeira e a última linha com pixels visíveis
            int minY = y1;
            int maxY = y0;

            for (int y = y0; y < y1; y++) {
                for (int x = x0; x < x1; x++) {
                    if ((p.getPixel(x, y) & 0xff) > 40) {
                        if (y < minY) minY = y;
                        if (y > maxY) maxY = y;
                    }
                }
            }

            valido[i] = maxY >= minY;

            if (!valido[i]) {
                minY = y0;
                maxY = y1 - 1;
            }

            xs0[i] = x0;
            xs1[i] = x1;
            minYs[i] = minY;
            maxYs[i] = maxY;
        }

        // Altura única para a linha toda
        if (!pesPorQuadro) {

            int menor = y1;
            int maior = y0;

            for (int i = 0; i < quantidade; i++) {
                if (!valido[i]) continue;
                if (minYs[i] < menor) menor = minYs[i];
                if (maxYs[i] > maior) maior = maxYs[i];
            }

            if (maior >= menor) {
                for (int i = 0; i < quantidade; i++) {
                    minYs[i] = menor;
                    maxYs[i] = maior;
                }
            }
        }

        TextureRegion[] quadros = new TextureRegion[quantidade];

        for (int i = 0; i < quantidade; i++) {
            quadros[i] = new TextureRegion(tex, xs0[i], minYs[i], xs1[i] - xs0[i], maxYs[i] - minYs[i] + 1);
        }

        return quadros;
    }

    // Torna transparente o fundo branco que está ligado às bordas da imagem
    private void removerFundoBranco(Pixmap p) {

        int w = p.getWidth();
        int h = p.getHeight();

        // Se o canto já é transparente, não precisa fazer nada
        if ((p.getPixel(0, 0) & 0xff) < 128) {
            return;
        }

        boolean[] marcado = new boolean[w * h];
        int[] pilha = new int[w * h];
        int topo = 0;

        for (int x = 0; x < w; x++) {
            topo = empilhar(p, marcado, pilha, topo, x, 0);
            topo = empilhar(p, marcado, pilha, topo, x, h - 1);
        }
        for (int y = 0; y < h; y++) {
            topo = empilhar(p, marcado, pilha, topo, 0, y);
            topo = empilhar(p, marcado, pilha, topo, w - 1, y);
        }

        while (topo > 0) {

            int i = pilha[--topo];
            int x = i % w;
            int y = i / w;

            p.drawPixel(x, y, 0);

            topo = empilhar(p, marcado, pilha, topo, x + 1, y);
            topo = empilhar(p, marcado, pilha, topo, x - 1, y);
            topo = empilhar(p, marcado, pilha, topo, x, y + 1);
            topo = empilhar(p, marcado, pilha, topo, x, y - 1);
        }
    }

    private int empilhar(Pixmap p, boolean[] marcado, int[] pilha, int topo, int x, int y) {

        if (x < 0 || y < 0 || x >= p.getWidth() || y >= p.getHeight()) return topo;

        int i = y * p.getWidth() + x;

        if (marcado[i]) return topo;

        int c = p.getPixel(x, y);
        int r = (c >>> 24) & 0xff;
        int g = (c >>> 16) & 0xff;
        int b = (c >>> 8) & 0xff;

        if (r < 232 || g < 232 || b < 232) return topo;

        marcado[i] = true;
        pilha[topo++] = i;
        return topo;
    }

    // Carrega uma imagem e troca o magenta (#FF00FF) por transparência.
    // recuperarBrilho = true: o brilho branco que se mistura com o magenta (rosa claro)
    // vira branco semitransparente, em vez de ficar como um disco rosa.
    private Texture carregarSemMagenta(String caminho, boolean recuperarBrilho) {
        Pixmap original = new Pixmap(Gdx.files.internal(caminho));
        Pixmap pixmap = new Pixmap(original.getWidth(), original.getHeight(), Pixmap.Format.RGBA8888);
        pixmap.drawPixmap(original, 0, 0);
        original.dispose();

        pixmap.setBlending(Pixmap.Blending.None);
        Color cor = new Color();

        for (int y = 0; y < pixmap.getHeight(); y++) {
            for (int x = 0; x < pixmap.getWidth(); x++) {
                Color.rgba8888ToColor(cor, pixmap.getPixel(x, y));

                if (recuperarBrilho && cor.r > 0.85f && cor.b > 0.85f) {

                    // pixel = mistura de magenta com branco; o verde diz quanto é branco
                    float alfa = cor.g < 0.06f ? 0f : cor.g;

                    pixmap.drawPixel(x, y, Color.rgba8888(1f, 1f, 1f, alfa));

                } else if (cor.r > 0.8f && cor.b > 0.8f && cor.g < 0.35f) {

                    pixmap.drawPixel(x, y, 0);
                }
            }
        }

        Texture textura = new Texture(pixmap);
        pixmap.dispose();
        return textura;
    }

    // Círculo de luz macio (transparente nas bordas)
    private Texture criarTexturaBrilho() {

        int tam = 64;

        Pixmap p = new Pixmap(tam, tam, Pixmap.Format.RGBA8888);
        p.setBlending(Pixmap.Blending.None);

        float c = (tam - 1) / 2f;

        for (int y = 0; y < tam; y++) {
            for (int x = 0; x < tam; x++) {

                float dx = (x - c) / (tam / 2f);
                float dy = (y - c) / (tam / 2f);
                float d = (float) Math.sqrt(dx * dx + dy * dy);

                float a = Math.max(0f, 1f - d);
                a = a * a;

                p.drawPixel(x, y, Color.rgba8888(0.8f, 0.65f, 1f, a));
            }
        }

        Texture t = new Texture(p);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        p.dispose();
        return t;
    }

    // Bolinha roxa das tentativas de teletransporte
    private Texture criarTexturaBolinha() {

        Pixmap p = new Pixmap(32, 32, Pixmap.Format.RGBA8888);

        p.setColor(0.30f, 0.08f, 0.50f, 1f);
        p.fillCircle(16, 16, 15);

        p.setColor(0.62f, 0.28f, 0.95f, 1f);
        p.fillCircle(16, 16, 12);

        p.setColor(0.85f, 0.65f, 1f, 1f);
        p.fillCircle(12, 12, 3);

        Texture t = new Texture(p);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        p.dispose();
        return t;
    }


    // ---------- BAÚ ----------

    // Começa a animação de abrir o baú (só acontece uma vez)
    private void abrirBau() {
        if (!bauAbrindo) {
            bauAbrindo = true;
            tempoBau = 0;
        }
    }

    // Retângulo do mago x retângulo do baú
    private boolean colideComBau() {
        return magoX < bauX + bauLargura
            && magoX + hitLargura > bauX
            && magoY < bauY + bauAltura
            && magoY + hitAltura > bauY;
    }


    // 5. DISPOSE
    @Override
    public void dispose() {

        batch.dispose();

        chao.dispose();

        background.dispose();

        magoSheet.dispose();

        bauSheet.dispose();

        teleporteSheet.dispose();

        texBrilho.dispose();

        texBolinha.dispose();
    }
}
