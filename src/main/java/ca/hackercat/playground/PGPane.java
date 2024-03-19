package ca.hackercat.playground;

import java.awt.Color;
import java.awt.Graphics2D;

public class PGPane implements PGObject, Updatable, Renderable {

    public static final int ORDER = 2_000_000_000;

    private int x;
    private int y;

    private int width = 320;
    private int height = 240;

    private boolean hidden;

    @Override
    public void update(double deltaTime) {

    }

    @Override
    public void render(Graphics2D g2, double t) {
        g2.setColor(Color.GREEN);
        g2.fillRect(x, y, width, height);

    }

    @Override
    public int getOrder() {
        return PGPane.ORDER;
    }
}
