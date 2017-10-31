/*--
  This file is a part of ZetaGrid, a simple and secure Grid Computing
  kernel.

  Copyright (c) 2001-2004 Sebastian Wedeniwski.  All rights reserved.

  Use in source and binary forms, with or without modification,
  are permitted provided that the following conditions are met:

  1. The source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.

  2. The origin of this software must not be misrepresented; you must
     not claim that you wrote the original software.  If you plan to
     use this software in a product, please contact the author.

  3. Altered source versions must be plainly marked as such, and must
     not be misrepresented as being the original software. The author
     must be informed about these changes.

  4. The name of the author may not be used to endorse or promote
     products derived from this software without specific prior written
     permission.

  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS
  OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

  This program is based on the work of:
     S. Wedeniwski
--*/

package zeta.server.handler.statistic;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Label;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.servlet.ServletException;

import zeta.server.DispatcherServlet;
import zeta.server.util.CachedQueries;
import zeta.server.util.DatabaseUtils;
import zeta.server.util.QueryWithSum;
import zeta.util.Graphics;
import zeta.util.Table;
import zeta.util.ThrowableHandler;

/**
 *  Handles a GET request for the statistic 'locations'.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class LocationsHandler extends AbstractHandler {
  /**
   *  @param servlet  servlet which owns this handler.
  **/
  public LocationsHandler(DispatcherServlet servlet) throws SQLException, ServletException {
    super(servlet);
  }

  /**
   *  Creates HTML page with the content of the statistic 'locations'.
   *  @param taskId task identifier
   *  @param  con  connection to the back-end database
   *  @return HTML page with the content of the statistic 'locations'.
  **/
  public String createPage(int taskId, Connection con) throws SQLException, ServletException {
    Map locations = new TreeMap();
    synchronized (countryPos) {
      if (isoCountryCode.size() == 0) {
        Statement stmt = null;
        try {
          stmt = con.createStatement();
          ResultSet rs = stmt.executeQuery("SELECT code,country,x,y FROM zeta.iso_country_code ORDER BY code");
          while (rs.next()) {
            isoCountryCode.add(rs.getString(1).substring(1));
            Object[] o = new Object[3];
            o[0] = rs.getString(2);
            int x = rs.getInt(3);
            int y = rs.getInt(4);
            if (x == 0 && y == 0) {
              o[1] = o[2] = null;
            } else {
              o[1] = new Integer(x);
              o[2] = new Integer(y);
            }
            countryData.add(o);
          }
          rs.close();
        } catch (SQLException e) {
          throw e;
        } finally {
          DatabaseUtils.close(stmt);
        }
      }
      countryPos.clear();
      computers.clear();
      Table table = CachedQueries.getWorkstationTable(taskId);
      if (table != null) {
        for (int i = 0, l = table.getRowCount(); i < l; ++i) {
          String location = "Unresolved/Unknown";
          String name = ((String)table.getValue(i, 0)).toLowerCase();
          int idx2 = name.lastIndexOf(',');
          int idx1 = name.lastIndexOf(',', Math.max(0, idx2-1));
          if (idx1 < idx2 && idx1 >= 0) {
            idx1 = name.indexOf('@', idx1+1);
            int idx3 = name.indexOf('.', idx1);
            if (idx1 < idx3 && idx3 < idx2) {
              idx1 = isoCountryCode.indexOf(name.substring(idx1+1, idx3));
              if (idx1 < 0 || idx3+1 == idx2 || name.indexOf('.', idx3+1) < 0) { // e.g. x@sd.com -> is not 'Sudan'
                idx3 = name.lastIndexOf('.', idx2);
                if (idx3+1 < idx2) {
                  idx1 = isoCountryCode.indexOf(name.substring(idx3+1, idx2));
                }
              }
              if (idx1 >= 0) {
                location = (String)((Object[])countryData.get(idx1))[0];
              }
            }
          }
          Set users = (Set)locations.get(location);
          if (users == null) {
            users = new HashSet(100);
            locations.put(location, users);
            if (idx1 >= 0) {
              Object[] o = (Object[])countryData.get(idx1);
              if (o[1] != null && o[2] != null) {
                countryPos.add(o);
              }
            }
          }
          if (idx2 > 0) {
            name = name.substring(0, idx2);
          }
          if (!users.contains(name)) {
            users.add(name);
            Integer count = (Integer)computers.get(location);
            computers.put(location, new Integer(CachedQueries.getMaxComputersUsed(taskId, name) + ((count == null)? 0 : count.intValue())));
          }
        }
      }
    }
    HtmlTableGenerator generator = new HtmlTableGeneratorWithSum(servlet);
    Table table = new Table(3);
    table.setColumnName(0, "country");
    table.setType(0, Types.VARCHAR);
    table.setAlignment(0, Table.LEFT);
    table.setColumnName(1, "users");
    table.setType(1, Types.INTEGER);
    table.setAlignment(1, Table.RIGHT);
    table.setColumnName(2, "computers");
    table.setType(2, Types.INTEGER);
    table.setAlignment(2, Table.RIGHT);
    Iterator i = locations.keySet().iterator();
    for (int row = 0; i.hasNext();) {
      String country = (String)i.next();
      Integer count = (Integer)computers.get(country);
      if (count != null) {
        table.addRow();
        table.setValue(row, 0, country);
        table.setValue(row, 1, new Integer(((Collection)locations.get(country)).size()));
        table.setValue(row, 2, count);
        ++row;
      }
    }
    QueryWithSum.addSum(table);

    StringBuffer buffer = new StringBuffer(70*1024);
    buffer.append("<tr><td><p><center><img src=\"");
    buffer.append(servlet.getRootPath());
    buffer.append(servlet.getHandlerPath(getClass()));
    buffer.append("?task=");
    buffer.append(URLEncoder.encode(servlet.getTaskManager().getServerTask(taskId).getName()));
    buffer.append("&image=map\" height=");
    buffer.append(WorldmapBuffer.HEIGHT);
    buffer.append(" width=");
    buffer.append(WorldmapBuffer.WIDTH);
    buffer.append("></center></td></tr>\n");
    buffer.append("<tr><td height=\"30pt\" class=\"second-head-gray\"><center>Locations:</center></td></tr>");
    buffer.append("<tr><td><br><center>");
    buffer.append(generator.generate(table));
    buffer.append("</center></td></tr>");
    return buffer.toString();
  }

  /**
   *  Creates PNG image for a specified name which is defined in the HTML page.
   *  @param taskId task identifier
   *  @param  con  connection to the back-end database
   *  @param  imageName name of the image
   *  @return PNG image for a specified name which is defined in the HTML page.
  **/
  public BufferedImage createImage(int taskId, Connection con, String imageName) throws SQLException, ServletException {
    if (isoCountryCode.size() == 0) {
      createPage(taskId, con);
    }
    Color notFillColor = new Color(255, 255, 255);
    Image image = Toolkit.getDefaultToolkit().createImage(WorldmapBuffer.getImage());
    MediaTracker tracker = new MediaTracker(new Label());
    tracker.addImage(image, 0);
    try {
      tracker.waitForID(0);
      tracker.removeImage(image);
      int maxValue = 0;
      List coordinates = null;
      synchronized (countryPos) {
        final int l = countryPos.size();
        for (int i = 0; i < l; ++i) {
          Object[] o = (Object[])countryPos.get(i);
          Integer count = (Integer)computers.get(o[0]);
          if (count != null) {
            int c = count.intValue();
            if (maxValue < c) {
              maxValue = c;
            }
          }
        }
        coordinates = new ArrayList(l);
        for (int value = 1; value <= maxValue; value *= 10) {
          int valueHigh = value*10;
          for (int i = 0; i < l; ++i) {
            Object[] o = (Object[])countryPos.get(i);
            Integer count = (Integer)computers.get(o[0]);
            if (count != null) {
              int c = count.intValue();
              if (c >= value && c < valueHigh) {
                coordinates.add(new Object[] { o[1], o[2], getColor(c) });
              }
            }
          }
        }
      }
      image = Graphics.floodFill(image, coordinates, notFillColor);
      BufferedImage bImage = new BufferedImage(WorldmapBuffer.WIDTH, WorldmapBuffer.HEIGHT, BufferedImage.TYPE_INT_RGB);
      Graphics2D g = bImage.createGraphics();
      g.drawImage(image, 0, 0, WorldmapBuffer.WIDTH, WorldmapBuffer.HEIGHT, null);
      int sz = coordinates.size();
      for (int i = 0; i < sz; ++i) {
        Object[] o = (Object[])coordinates.get(i);
        int x = ((Integer)o[0]).intValue();
        int y = ((Integer)o[1]).intValue();
        g.setPaint((Color)o[2]);
        g.fillArc(x-3, y-3, 8, 8, 0, 360);
      }
      g.setFont(new Font(g.getFont().getName(), Font.PLAIN, 10));
      for (int value = 1, pos = WorldmapBuffer.HEIGHT+15-18*((int)Math.round(0.5+Math.log(maxValue)*0.43429448190325182765112891891661)); value <= maxValue; pos += 15) {
        g.setPaint(getColor(value));
        g.fillArc(5, pos-4, 8, 8, 0, 360);
        int nextValue = value*10;
        g.drawString("between " + value + " and " + (nextValue-1) + " computers", 15,  pos+3);
        value = nextValue;
      }
      g.dispose();
      return bImage;
    } catch (InterruptedException e) {
      return null;
    }
  }

  /**
   *  Returns same color for values inside a range.
   *  @param value value
   *  @return same color for values inside a range.
  **/
  private static Color getColor(int value) {
    if (value >= 10000) {
      return new Color(255, 255, 0);
    } else if (value >= 1000) {
      return new Color(255, 240, 0);
    } else if (value >= 100) {
      return new Color(255, 160, 0);
    } else if (value >= 10) {
      return new Color(200, 80, 0);
    }
    return new Color(255, 0, 0);
  }

  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("USAGE: <name> <password>");
      return;
    }
    /*try {
      byte[] buffer = zeta.util.StreamUtils.getFile("images/worldmap.gif", false, false);
      for (int i = 0; i < buffer.length; ++i) {
        String s = "     " + Byte.toString(buffer[i]) + ((i == buffer.length-1)? "" : ", ");
        if (i > 0 && i%25 == 24) {
          System.out.println(s.substring(s.length()-6, s.length()-1));
        } else {
          System.out.print(s.substring(s.length()-6));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }*/
    try {
      Connection con = zeta.server.tool.Database.getConnection(args[0], args[1]);
      LocationsHandler handler = new LocationsHandler(null);
      BufferedImage bImage = handler.createImage(1, con, "map");
      ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream(32*1024);
      com.sun.jimi.core.encoder.png.PNGEncoder encoder = new com.sun.jimi.core.encoder.png.PNGEncoder();
      encoder.encodeImage(com.sun.jimi.core.Jimi.createRasterImage(bImage.getSource()), imageBuffer);
      imageBuffer.close();
      zeta.util.StreamUtils.writeData(new java.io.ByteArrayInputStream(imageBuffer.toByteArray()), new java.io.FileOutputStream("map.png"), true, true);
      Iterator i = handler.computers.keySet().iterator();
      while (i.hasNext()) {
        String location = (String)i.next();
        final int l = handler.countryData.size();
        int idx = -1;
        while (++idx < l && !((Object[])handler.countryData.get(idx))[0].equals(location));
        if (idx == l || ((Object[])handler.countryData.get(idx))[1] == null) {
          System.out.println(location + " has no valid position");
        }
      }
    } catch (Exception ex) {
      ThrowableHandler.handle(ex);
    }
    System.exit(1);
  }

  private Map computers = new HashMap(150); // ToDo: change to map of map (depends on task ID)
  private List isoCountryCode = new ArrayList(300);
  private List countryData = new ArrayList(300);
  private List countryPos = new ArrayList(100);
}
