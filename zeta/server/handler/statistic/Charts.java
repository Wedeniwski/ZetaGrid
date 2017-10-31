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
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import com.jrefinery.chart.ChartFactory;
import com.jrefinery.chart.CombinedXYPlot;
import com.jrefinery.chart.HorizontalDateAxis;
import com.jrefinery.chart.JFreeChart;
import com.jrefinery.chart.NumberTickUnit;
import com.jrefinery.chart.PiePlot;
import com.jrefinery.chart.ValueAxis;
import com.jrefinery.chart.VerticalNumberAxis;
import com.jrefinery.chart.VerticalXYBarRenderer;
import com.jrefinery.chart.XYPlot;
import com.jrefinery.chart.data.LinearPlotFitAlgorithm;
import com.jrefinery.chart.data.MovingAveragePlotFitAlgorithm;
import com.jrefinery.chart.data.PlotFit;
import com.jrefinery.data.BasicTimeSeries;
import com.jrefinery.data.CombinedDataset;
import com.jrefinery.data.Day;
import com.jrefinery.data.DefaultPieDataset;
import com.jrefinery.data.Hour;
import com.jrefinery.data.Range;
import com.jrefinery.data.SeriesDataset;
import com.jrefinery.data.SubSeriesDataset;
import com.jrefinery.data.TimeSeriesCollection;

import zeta.server.util.CachedQueries;
import zeta.server.util.DatabaseUtils;
import zeta.util.StreamUtils;
import zeta.util.ThrowableHandler;

/**
 *  Creates charts for the statistics by using the JFreeChart class library.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class Charts {
  /**
   *  Creates and returns a buffered image into which a pie chart with no title and not legend has been drawn.
   *  This chart is used in the statistic 'work load today'.
   *  @param imageWidth the width of the image
   *  @param imageHeight the height of the image
   *  @param names first items in the dataset for the chart
   *  @param values second items in the dataset for the chart
   *  @return a buffered image into which the chart has been drawn.
  **/
  public static BufferedImage generatePie(int imageWidth, int imageHeight, String[] names, Long[] values)
  {
    DefaultPieDataset d = new DefaultPieDataset();
    if (names != null && values != null) {
      for (int i = 0; i < names.length; ++i) {
        d.setValue(names[i], values[i]);
      }
    }
    JFreeChart chart = ChartFactory.createPieChart(null, d, true);
    chart.setLegend(null);
    chart.setBackgroundPaint(Color.white);
    PiePlot pie = (PiePlot)chart.getPlot();
    pie.setSectionLabelType(PiePlot.NAME_AND_PERCENT_LABELS);
    pie.setBackgroundPaint(Color.white);
    pie.setBackgroundAlpha(0.6f);
    pie.setForegroundAlpha(0.75f);
    return chart.createBufferedImage(imageWidth, imageHeight);
  }

  public static BufferedImage generateChart(int imageWidth, int imageHeight, String title, String timeAxisLabel, final String[] verticalAxisLabels, Paint[] colors, int[] weight, int[][] combination, BasicTimeSeries[] bts) {
    Locale.setDefault(Locale.ENGLISH);

    // make one shared horizintal axis
    ValueAxis timeAxis = new HorizontalDateAxis(timeAxisLabel);
    timeAxis.setCrosshairVisible(false);

    // make a vertically CombinedPlot that will contain the sub-plots
    CombinedXYPlot combinedPlot = new CombinedXYPlot(timeAxis, CombinedXYPlot.VERTICAL);

    // create master dataset
    TimeSeriesCollection tsc = new TimeSeriesCollection();
    for (int i = 0; i < bts.length; ++i) {
      bts[i].setName(verticalAxisLabels[i]);
      tsc.addSeries(bts[i]);
    }
    CombinedDataset data = new CombinedDataset();
    try {
      data.add(new PlotFit(tsc, new MovingAveragePlotFitAlgorithm()).getFit());
    } catch (IllegalArgumentException iae) {
      data.add(new PlotFit(tsc, new LinearPlotFitAlgorithm()).getFit());
    }
    for (int i = 0; i < combination.length; ++i) {
      // decompose data into its two dataset series
      // compose datasets for each sub-plot
      SeriesDataset[] sd = new SeriesDataset[combination[i].length];
      for (int j = 0; j < sd.length; ++j) {
        sd[j] = new SubSeriesDataset(data, combination[i][j]);
      }
      CombinedDataset combData = new CombinedDataset(sd);

      VerticalNumberAxis valueAxis = new VerticalNumberAxis("");
      valueAxis.setCrosshairVisible(false);
      valueAxis.setAutoRangeIncludesZero(false);

      // add a XY chart
      XYPlot subplot = null;
      if (i == 0) {
        subplot = new XYPlot(combData, null, valueAxis);
        Range size = subplot.getVerticalDataRange();
        if (size.getLength() > 10000000000.0) {
          valueAxis.setTickUnit(new NumberTickUnit(50000000000.0, new java.text.DecimalFormat("#,##0")));
        }
      } else {
        subplot = new XYPlot(combData, null, valueAxis, new VerticalXYBarRenderer(0.20));
      }
      for (int j = 0; j < sd.length; ++j) {
        subplot.setSeriesPaint(j, colors[combination[i][j]]);
      }
      combinedPlot.add(subplot, weight[i]);
    }
    // this should be called after all sub-plots have been added
    //combinedPlot.adjustPlots();

    // now make the top level JFreeChart that contains the CombinedPlot
    JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);
    chart.setBackgroundPaint(Color.white);
    //chart.getPlot().setSeriesPaint(colors);
    return chart.createBufferedImage(imageWidth, imageHeight);
  }

  public static BasicTimeSeries[] createNumberOfComputedZeros(int taskId, Connection con) throws SQLException {
    BasicTimeSeries[] t = { new BasicTimeSeries(""), new BasicTimeSeries(""), new BasicTimeSeries(""), new BasicTimeSeries(""), new BasicTimeSeries(""), new BasicTimeSeries(""), new BasicTimeSeries(""), new BasicTimeSeries("") };
    Statement stmt = null;
    try {
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT DAYS(res.stop),SUM(CAST(comp.size AS DECIMAL(20, 0))),"
                                       + "SUM(SQRT((comp.work_unit_id+comp.size)/12.566370614359)*148*1.245*1.246*comp.size)/1000000000000"
                                       + " FROM zeta.computation comp, zeta.result res"
                                       + " WHERE comp.task_id=res.task_id AND comp.task_id=" + taskId
                                       + " AND comp.work_unit_id=res.work_unit_id"
                                       + " GROUP BY DAYS(res.stop)"
                                       + " ORDER BY DAYS(res.stop)");
      Calendar cal = Calendar.getInstance();
      long lastDay = 0;
      long lastValue = 0;
      long lastFlops = 0;
      double sum = 0.0;
      Day d = null;
      while (rs.next()) {
        long day = DatabaseUtils.convertDaysToTimeMillis((long)rs.getInt(1));
        long value = rs.getLong(2);
        long flops = rs.getLong(3);
        if (lastDay > 0) {
          cal.setTime(new Date(lastDay));
          sum += lastValue;
          d = new Day(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR));
          t[0].add(d, sum);
          t[1].add(d, new Long(lastValue));
          t[2].add(d, new Long(lastFlops));
          if (t[3].getItemCount() == 0) {
            t[3].add(d, new Long(1500000000L));
            t[4].add(d, new Long(10000000000L));
            t[5].add(d, new Long(50000000000L));
            t[6].add(d, new Long(100000000000L));
            t[7].add(d, new Long(1000000000000L));
          }
        }
        lastDay = day; lastValue = value; lastFlops = flops;
      }
      if (d != null) {
        t[3].add(d, new Long(1500000000L));
        t[4].add(d, new Long(10000000000L));
        t[5].add(d, new Long(50000000000L));
        t[6].add(d, new Long(100000000000L));
        t[7].add(d, new Long(1000000000000L));
      }
      rs.close();
    } finally {
      DatabaseUtils.close(stmt);
    }
    return t;
  }

  public static BasicTimeSeries[] createNumberOfParticipants(int taskId, Connection con) throws SQLException {
    BasicTimeSeries[] t = { new BasicTimeSeries(""), new BasicTimeSeries(""), new BasicTimeSeries("") };
    Statement stmt = null;
    try {
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT MIN(DAYS(res.stop)) AS start_day,comp.server_id,comp.user_id"
                                     + " FROM zeta.computation comp, zeta.result res"
                                     + " WHERE res.task_id=comp.task_id AND comp.task_id=" + taskId
                                     + " AND res.work_unit_id=comp.work_unit_id"
                                     + " GROUP BY comp.server_id,comp.user_id ORDER BY start_day");
      Calendar cal = Calendar.getInstance();
      int count = 0;
      int sum = 0;
      long lastDay = 0;
      while (rs.next()) {
        long day = DatabaseUtils.convertDaysToTimeMillis((long)rs.getInt(1));
        if (lastDay > 0 && lastDay != day) {
          cal.setTime(new Date(lastDay));
          sum += count;
          Day d = new Day(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR));
          t[0].add(d, new Integer(sum));
          t[1].add(d, new Integer(count));
          count = 0;
        }
        lastDay = day; ++count;
      }
      cal.setTime(new Date(lastDay));
      sum += count;
      Day d = new Day(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR));
      t[0].add(d, new Integer(sum));
      t[1].add(d, new Integer(count));
      rs.close();
      rs = stmt.executeQuery("WITH a (day,server_id,user_id) AS"
                           + " (SELECT DISTINCT DAYS(res.stop),comp.server_id,comp.user_id"
                           + "  FROM zeta.computation comp, zeta.result res"
                           + "  WHERE res.task_id=comp.task_id AND comp.task_id=" + taskId + " AND res.work_unit_id=comp.work_unit_id)"
                           + " SELECT day,COUNT(*) FROM a GROUP BY day ORDER BY day");
      int lastValue = 0;
      lastDay = 0;
      while (rs.next()) {
        long day = DatabaseUtils.convertDaysToTimeMillis((long)rs.getInt(1));
        int value = rs.getInt(2);
        if (lastDay > 0) {
          cal.setTime(new Date(lastDay));
          t[2].add(new Day(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR)), new Integer(lastValue));
        }
        lastDay = day; lastValue = value;
      }
      /*cal.setTime(new Date(lastDay));
      sum += count;
      d = new Day(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR));
      t[0].add(d, new Integer(sum));
      t[1].add(d, new Integer(count));*/
      rs.close();
    } finally {
      DatabaseUtils.close(stmt);
    }
    return t;
  }

  public static BasicTimeSeries[] createNumberOfComputers(int taskId, Connection con) throws SQLException {
    BasicTimeSeries[] t = { new BasicTimeSeries(""), new BasicTimeSeries(""), new BasicTimeSeries("") };
    Statement stmt = null;
    try {
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT MIN(DAYS(res.stop)) AS start_day,comp.server_id,comp.workstation_id"
                                       + " FROM zeta.computation comp, zeta.result res"
                                       + " WHERE res.task_id=comp.task_id AND comp.task_id=" + taskId
                                       + " AND res.work_unit_id=comp.work_unit_id"
                                       + " GROUP BY comp.server_id,comp.workstation_id ORDER BY start_day");
      Calendar cal = Calendar.getInstance();
      int count = 0;
      int sum = 0;
      long lastDay = 0;
      while (rs.next()) {
        long day = DatabaseUtils.convertDaysToTimeMillis((long)rs.getInt(1));
        if (lastDay > 0 && lastDay != day) {
          cal.setTime(new Date(lastDay));
          sum += count;
          Day d = new Day(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR));
          t[0].add(d, new Integer(sum));
          t[1].add(d, new Integer(count));
          count = 0;
        }
        lastDay = day; ++count;
      }
      rs.close();
      sum += count;
      if (lastDay > 0) {
        cal.setTime(new Date(lastDay));
        Day d = new Day(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR));
        t[0].add(d, new Integer(sum));
        t[1].add(d, new Integer(count));
      }
      rs = stmt.executeQuery("WITH a (day,server_id,workstation_id) AS"
                           + " (SELECT DISTINCT DAYS(res.stop),comp.server_id,comp.workstation_id"
                           + "  FROM zeta.computation comp, zeta.result res"
                           + "  WHERE res.task_id=comp.task_id AND comp.task_id=" + taskId + " AND res.work_unit_id=comp.work_unit_id)"
                           + " SELECT day,COUNT(*) FROM a GROUP BY day ORDER BY day");
      int lastValue = 0;
      lastDay = 0;
      while (rs.next()) {
        long day = DatabaseUtils.convertDaysToTimeMillis((long)rs.getInt(1));
        int value = rs.getInt(2);
        if (lastDay > 0) {
          cal.setTime(new Date(lastDay));
          t[2].add(new Day(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR)), new Double(lastValue));
        }
        lastDay = day; lastValue = value;
      }
      rs.close();
    } finally {
      DatabaseUtils.close(stmt);
    }
    return t;
  }

  public static BasicTimeSeries[] createWorkLoad(Connection con, Map workloadSummary) {
    BasicTimeSeries[] t = { new BasicTimeSeries("", Hour.class), new BasicTimeSeries("", Hour.class), new BasicTimeSeries("", Hour.class), new BasicTimeSeries("", Hour.class) };
    int day = 0;
    Iterator i = workloadSummary.keySet().iterator();
    while (i.hasNext()) {
      day = Math.max(day, ((Integer)i.next()).intValue()/24);
    }
    int hours1 = workloadSummary.keySet().size();
    int hours2 = 0;
    i = workloadSummary.keySet().iterator();
    while (i.hasNext()) {
      if (((Integer)i.next()).intValue()/24 == day) {
        ++hours2;
      }
    }
    hours1 -= hours2;
    Calendar cal = Calendar.getInstance();
    cal.setTime(new java.util.Date());
    for (int j = 0; j <= 1; ++j, --day) {
      cal.set(Calendar.DAY_OF_YEAR, day);
      cal.add(Calendar.DAY_OF_YEAR, j);
      long sum = 0;
      for (int hour = 0; hour < 24; ++hour) {
        Long size = null;
        Object[] values = (Object[])workloadSummary.get(new Integer(day*24+hour));
        if (values == null) {
          if (j == 0 && hours2 > 0 || j == 1 && hours1 > 0) {
            size = new Long(0);
          } else {
            break;
          }
        } else {
          size = (Long)values[3];
          sum += size.longValue();
          if (j == 0) {
            --hours2;
          } else {
            --hours1;
          }
        }
        cal.set(Calendar.HOUR_OF_DAY, hour);
        Hour h = new Hour(cal.getTime());
        t[j].add(h, new Long(sum));
        t[2+j].add(h, size);
      }
    }
    return t;
  }

  public static BasicTimeSeries[] createNewReservedZeros(int taskId, Connection con) throws SQLException {
    BasicTimeSeries[] t = { new BasicTimeSeries("", Hour.class), new BasicTimeSeries("", Hour.class), new BasicTimeSeries("", Hour.class), new BasicTimeSeries("", Hour.class) };
    Statement stmt = null;
    try {
      stmt = con.createStatement();
      long day = CachedQueries.getMaxDays(taskId, stmt);
      ResultSet rs = stmt.executeQuery("SELECT HOUR(start),DAYS(start),SUM(size),COUNT(work_unit_id),MIN(start)"
                                     + " FROM zeta.computation WHERE DAYS(start) IN ("
                                     + day + ',' + (day-1)
                                     + ") AND task_id=" + taskId
                                     + " GROUP BY HOUR(start),DAYS(start) ORDER BY HOUR(start)");
      Calendar cal = Calendar.getInstance();
      Hour prevH = null;
      Long prevValue = null;
      Integer prevCount = null;
      while (rs.next()) {
        int activeDay = rs.getInt(2);
        long value = rs.getLong(3);
        int count = rs.getInt(4);
        Timestamp timestamp = rs.getTimestamp(5);
        cal.setTime(timestamp);
        if (activeDay == day) {
          if (prevH != null) {
            t[0].add(prevH, prevValue);
            t[2].add(prevH, prevCount);
          }
          prevH = new Hour(cal.getTime());
          prevValue = new Long(value);
          prevCount = new Integer(count);
        } else {
          cal.add(Calendar.DAY_OF_YEAR, 1);
          Hour h = new Hour(cal.getTime());
          t[1].add(h, new Long(value));
          t[3].add(h, new Integer(count));
        }
      }
      rs.close();
    } finally {
      DatabaseUtils.close(stmt);
    }
    return t;
  }
}
