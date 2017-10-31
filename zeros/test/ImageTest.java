package test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.*;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;

public class ImageTest {
    protected static final float OFFICAL_MARK    =  1500000000.0f;
    protected static final float INOFFICIAL_MARK =  5600000000.0f;
    protected static final float TARGET_MARK     = 10000000000.0f;

    protected static final int imgWidth  = 600;
    protected static final int imgHeight = 250;

    protected static final Point2D.Float displacement = new Point2D.Float(-6.0f, 3.0f);
    protected static final Point2D.Float offset       = new Point2D.Float(-displacement.x, imgHeight - 1.0f);

    protected float maxX;
    protected float maxY;
    protected float scaleX;
    protected float scaleY;
    private Font font = null;

    protected void findMaxima(List data) {
        maxX = ((long[])data.get(data.size() - 1))[0] + 1;
        maxY = 0;
        for (int day = 0; day < data.size(); ++day) {
            maxY += ((long[])data.get(day))[1];
        }

        maxY = Math.max(maxY, TARGET_MARK);
    }

    protected void calcScale() {
        scaleX = (imgWidth - 1.0f - offset.x) / maxX;
        scaleY = (imgHeight - (imgHeight - offset.y) - 1.0f - displacement.y) / maxY;

        float f = offset.y - (TARGET_MARK * scaleY + displacement.y + 2.0f + 10.0f);
        if (f < 0.0f) {
            scaleY = (scaleY * maxY + f) / maxY;
        }
    }

    protected void paintFront(Graphics2D g2d, List data, Color lineColor, Color fillColor) {
        GeneralPath outlinePath = new GeneralPath();
        GeneralPath linePath    = new GeneralPath();
        int         vertices    = data.size();
        
        Point2D.Float lastVertex = new Point2D.Float(0.0f, 0.0f);
        outlinePath.moveTo(offset.x, offset.y);
        for (int i = 0; i < vertices; ++i) {
            long[] value = (long[])data.get(i);
            float x = (value[0] + 1) * scaleX;
            float y = lastVertex.y + value[1] * scaleY;

            outlinePath.lineTo(offset.x + x, offset.y - y);
            if (i < vertices - 1) {
                linePath.moveTo(offset.x + x, offset.y - y);
                linePath.lineTo(offset.x + x, offset.y);
            }

            lastVertex.x = x;
            lastVertex.y = y;
        }
        outlinePath.lineTo(offset.x + lastVertex.x, offset.y);
        outlinePath.closePath();
        
        g2d.setPaint(fillColor);
        g2d.fill(outlinePath);

        g2d.setPaint(lineColor);
        g2d.draw(linePath);

        g2d.setPaint(lineColor);
        g2d.draw(outlinePath);
    }

    protected void paintTop(Graphics2D g2d, List data, Color lineColor, Color fillColor) {
        GeneralPath outlinePath = new GeneralPath();
        GeneralPath linePath    = new GeneralPath();
        int         vertices    = data.size();
        
        Point2D.Float lastVertex = new Point2D.Float(0.0f, 0.0f);
        outlinePath.moveTo(offset.x, offset.y);
        for (int i = 0; i < vertices; ++i) {
            long[] value = (long[])data.get(i);
            float x = (value[0] + 1) * scaleX;
            float y = lastVertex.y + value[1] * scaleY;

            outlinePath.lineTo(offset.x + x, offset.y - y);
            if (i < vertices - 1) {
                linePath.moveTo(offset.x + x, offset.y - y);
                linePath.lineTo(offset.x + x + displacement.x, offset.y - (y + displacement.y));
            }

            lastVertex.x = x;
            lastVertex.y = y;
        }
        outlinePath.lineTo(offset.x + lastVertex.x + displacement.x, offset.y - (lastVertex.y + displacement.y));
        for (int i = vertices - 1; i > 0; --i) {
            long[] value = (long[])data.get(i-1);
            float x = (value[0] + 1) * scaleX;
            value = (long[])data.get(i);
            float y = lastVertex.y - value[1] * scaleY;

            outlinePath.lineTo(offset.x + x + displacement.x, offset.y - (y + displacement.y));

            lastVertex.x = x;
            lastVertex.y = y;
        }
        outlinePath.lineTo(offset.x + displacement.x, offset.y - (displacement.y));
        outlinePath.closePath();
        
        g2d.setPaint(fillColor);
        g2d.fill(outlinePath);

        g2d.setPaint(lineColor);
        g2d.draw(linePath);

        g2d.setPaint(lineColor);
        g2d.draw(outlinePath);
    }

    protected Point2D.Float calcIntersection(float mark, List data) {
        float x        = 0.0f;
        float y        = 0.0f;
        int   vertices = data.size();

        for (int i = 0; i < vertices; ++i) {
            long[] value = (long[])data.get(i);
            float nx = value[0] + 1;
            float ny = y + value[1];
            
            if (y <= mark && ny >= mark) {
                float d = (mark - y) / (ny - y);
                return new Point2D.Float(x + (nx - x) * d, mark);
            }

            x = nx;
            y = ny;
        }

        return null;
    }

    protected void paintMark(Graphics2D g2d, float mark, String description, List data, Color lineColor, Color fillColor, Color textColor) {
        Point2D.Float intersection = calcIntersection(mark, data);
        GeneralPath   path         = new GeneralPath();
        Line2D.Float  line         = null;

        if (intersection != null) {
            path.moveTo(offset.x, offset.y - mark * scaleY);
            path.lineTo(offset.x + intersection.x * scaleX,  offset.y - (intersection.y * scaleY));
            path.lineTo(offset.x + intersection.x * scaleX + displacement.x,  offset.y - (intersection.y * scaleY + displacement.y));
            path.lineTo(offset.x + displacement.x,  offset.y - (mark * scaleY + displacement.y));
            path.closePath();

            //line = new Line2D.Float(offset.x + intersection.x * scaleX,  offset.y - (intersection.y * scaleY),
            //                        offset.x + maxX * scaleX, offset.y - (intersection.y * scaleY));
        } else {
            path.moveTo(offset.x, offset.y - mark * scaleY);
            path.lineTo(offset.x + maxX * scaleX,  offset.y - (mark * scaleY));
            path.lineTo(offset.x + maxX * scaleX + displacement.x,  offset.y - (mark * scaleY + displacement.y));
            path.lineTo(offset.x + displacement.x,  offset.y - (mark * scaleY + displacement.y));
            path.closePath();
        }

        Stroke stroke = g2d.getStroke();
        g2d.setPaint(textColor);
        g2d.setStroke(new BasicStroke(1.0f));
        if (font == null) font = new java.awt.Font(g2d.getFont().getName(), java.awt.Font.PLAIN, 10);
        g2d.setFont(font);
        g2d.drawString(description, offset.x + displacement.x,  offset.y - (mark * scaleY + displacement.y + 2.0f));
        g2d.setStroke(stroke);

        g2d.setPaint(fillColor);
        g2d.fill(path);

        if (line != null) {
            g2d.setPaint(lineColor);
            g2d.draw(line);
        }

        g2d.setPaint(lineColor);
        g2d.draw(path);
    }

    public Image createImage() {
        List data = getData();
        if (data == null) return null;

        findMaxima(data);
        calcScale();

        BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(0.8f));
        
        paintFront(g2d, data, Color.white, new Color(0.0f, 0.0f, 0.6f));
        paintTop(g2d, data, Color.white, new Color(0.2f, 0.2f, 1.0f));
        paintMark(g2d,    OFFICAL_MARK,   "official record", data, Color.white, new Color(1.0f, 0.0f, 0.0f), Color.white);
        paintMark(g2d, INOFFICIAL_MARK, "inofficial record", data, Color.white, new Color(1.0f, 0.5f, 0.0f), Color.white);
        paintMark(g2d,     TARGET_MARK,      "first target", data, Color.white, new Color(0.0f, 1.0f, 0.0f), Color.white);

        g2d.dispose();
        
        try {
            FileOutputStream pngFile = null;
            try {
                com.sun.jimi.core.encoder.png.PNGEncoder encoder = new com.sun.jimi.core.encoder.png.PNGEncoder();
                pngFile = new FileOutputStream("test.png");
                encoder.encodeImage(com.sun.jimi.core.Jimi.createRasterImage(img.getSource()), pngFile);
            } catch (com.sun.jimi.core.JimiException ex) {
                ex.printStackTrace();
            } finally {
                if (pngFile != null) pngFile.close();
            }
            FileOutputStream out = new FileOutputStream("test.jpg");
            com.sun.image.codec.jpeg.JPEGEncodeParam encodeParam = com.sun.image.codec.jpeg.JPEGCodec.getDefaultJPEGEncodeParam(img);
            encodeParam.setQuality(1.0f, false);
            com.sun.image.codec.jpeg.JPEGCodec.createJPEGEncoder(out, encodeParam).encode(img);
            out.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

        return img;
    }

    private List getData() {
        Connection con = null;
        List data = new ArrayList();
        try {
            Class.forName("COM.ibm.db2.jdbc.app.DB2Driver");
            con             = DriverManager.getConnection("jdbc:db2:zetatest", "wedeniws", "");
            Statement  stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT a AS day, SUM(CAST (b AS DECIMAL(20, 0))) AS range       " +
                                             "FROM (SELECT DAYS(b.stop) - DAYS(a.a) AS a, b.range AS b               " +
                                             "      FROM (SELECT MIN(stop) AS a                               " +
                                             "            FROM zeta.computation                               " +
                                             "            WHERE stop IS NOT NULL) AS a, zeta.computation AS b " +
                                             "      WHERE b.stop IS NOT NULL) AS c                            " +
                                             "GROUP BY a                                                      " +
                                             "ORDER BY a                                                      ");
            while (rs.next()) {
                long[] row = new long[2];
                for (int i = 0; i < 2; ++i) {
                    row[i] = rs.getLong(i + 1);
                }
                data.add(row);
            }
        } catch(Throwable t) {
            t.printStackTrace();
            data = null;
        } finally {
            try {
                con.close();
            } catch(Exception e) {
            }
        }
        return data;
    }

    public static void main(String[] args) {
        Image image = new ImageTest().createImage();

        JFrame frame = new JFrame();
        frame.getContentPane().add(new JLabel(new ImageIcon(image)));
        frame.getContentPane().setBackground(new Color(0.1f, 0.1f, 0.0f));
        frame.pack();
        frame.setSize(frame.getPreferredSize().width + 20, frame.getPreferredSize().height + 20);
        frame.show();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
}
