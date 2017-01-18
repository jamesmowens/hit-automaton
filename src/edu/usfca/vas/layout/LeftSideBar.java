package edu.usfca.vas.layout;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by thoma on 1/17/2017.
 */
public class LeftSideBar extends JTabbedPane {


    public LeftSideBar(int layout) {
        super(layout);
    }

    int tabNo = 0;

    public void addTab(Component comp, String name) {
        addTab("<html> <img src=" + getClass().getResource(SettingsAccessor.getImgPath(name)).toString() +
                " height=25 width=25 hspace=10> <br> <p>" + name + "</p> </html>", comp);
        /*JLabel lbl = new JLabel(name);
        Icon icon = new ImageIcon(getClass().getResource(SettingsAccessor.getImgPath(name)));
        lbl.setIcon(icon);

        lbl.setIconTextGap(5);
        lbl.setHorizontalTextPosition(SwingConstants.SOUTH);

        setTabComponentAt(tabNo++, lbl);*/
    }

    private Image getScaledImage(Image srcImg, int w, int h){
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();

        return resizedImg;
    }

}
