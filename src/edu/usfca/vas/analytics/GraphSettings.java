package edu.usfca.vas.analytics;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by Thomas Schweich on 2/9/2017.
 *
 * Class representing the settings associated with a graph such as labels and colors. Auto-handles input errors.
 */
public class GraphSettings {
    private String title = "";
    private String xLabel = "";
    private String yLabel = "";
    private String background = "#FFFFFF";
    private String lineColor = "#0000FF";
    private String shape="circle";
    private String shapeSize = "5";
    private String gridColor = "#000000";

    /**
     * Uses reflection to set all members of this class to the values who's keys correspond to the members' names in
     * settings. Settings does not have to contain an entry for every member of this class.
     * @param settings Map from String to String of any desired settings
     */
    public GraphSettings(Map<String, String> settings) {
        for(Field f : getClass().getDeclaredFields()) {
            final String name = f.getName();
            if(settings.containsKey(f.getName())) {
                try {
                    f.set(this, settings.get(f.getName()));
                } catch (IllegalAccessException e) {
                    System.err.println("Couldn't access the desired setting \"" + name + "\"");
                    //Non-fatal -- field will simply be default value
                }
            }
        }
    }

    /**
     * Initialize a GraphSettings to defaults
     */
    public GraphSettings() {}

    public String getTitle() {
        return title;
    }

    public String getxLabel() {
        return xLabel;
    }

    public String getyLabel() {
        return yLabel;
    }

    public Color getBackground() {
        try {
            return Color.decode(background);
        } catch (NumberFormatException n) {
            System.err.println("Invalid input for for color \"" + background + "\". Defaulting to white.");
        }
        return Color.WHITE;
    }

    public Color getLineColor() {
        try {
            return Color.decode(lineColor);
        } catch (NumberFormatException n) {
            System.err.println("Invalid input for for color \"" + lineColor + "\". Defaulting to blue.");
        }
        return Color.BLUE;
    }

    public Color getGridColor() {
        try {
            return Color.decode(gridColor);
        } catch (NumberFormatException n) {
            System.err.println("Invalid input for for color \"" + gridColor + "\". Defaulting to black.");
        }
        return Color.BLACK;
    }

    public Shape getShape() {
        double size;
        try {
            size = Double.parseDouble(shapeSize);
        } catch (NumberFormatException n) {
            System.err.println("Couldn't parse input as double \"" + shapeSize + "\". Defaulting to 5.");
            size = 5;
        }
        switch (shape) {
            case "circle":
                return new Ellipse2D.Double(-size, -size, size*2, size*2);
            case "square":
                return new Rectangle2D.Double(-size, -size, size*2, size*2);
            default:
                return new Ellipse2D.Double(-size, -size, size, size);
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setxLabel(String xLabel) {
        this.xLabel = xLabel;
    }

    public void setyLabel(String yLabel) {
        this.yLabel = yLabel;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public void setLineColor(String lineColor) {
        this.lineColor = lineColor;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public void setShapeSize(String shapeSize) {
        this.shapeSize = shapeSize;
    }

    public void setGridColor(String gridColor) {
        this.gridColor = gridColor;
    }
}
