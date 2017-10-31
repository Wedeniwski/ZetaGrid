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

package zeta.tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import zeta.ClientTask;
import zeta.TaskManager;
import zeta.WorkUnit;
import zeta.example.ZetaWorkUnitVerifier;
import zeta.server.tool.ConstantProperties;
import zeta.server.tool.GetData;
import zeta.util.StreamUtils;
import zeta.util.ThrowableHandler;

/**
 *  @version 1.9.4, August 27, 2004
**/
public class ZetaStatistic {
  public static void main(String[] args) {
    if (args.length == 3 && args[0].length() == 1) {
      if (args[0].charAt(0) == 'v') {
        verify(args[1], args[2]);
        return;
      } else if (args[0].charAt(0) == 'c') {
        check(Integer.parseInt(args[1]), args[2]);
        return;
      } else if (args[0].charAt(0) == 'e') {
        try {
          expandFileAndCheck(args[1], args[2], null);
        } catch (Exception e) {
          ThrowableHandler.handle(e);
        }
        return;
      }
    } else if (args.length == 2 && args[0].length() == 1 && args[0].charAt(0) == 'r') {
      reduceFilesize2(args[1]);
      return;
    } else if (args.length == 2 && args[0].length() == 1 && args[0].charAt(0) == 'g') {
      System.out.println(getStartN(Double.parseDouble(args[1])));
      return;
    } else if (args.length == 1 && args[0].length() == 1 && args[0].charAt(0) == '?') {
      System.out.println("USAGE: e(xpand) <input> <output>\n"
                       + "       r(educe) <input/output>"
                       + "       g(etStartN) <t>");
      return;
    }
    calc(-1);
  }

  public static void calc(int maxNumberToProceed) { // maxNumberToProceed = -1 means infinitely many
    clientTask = TaskManager.getInstance("zeta.tasks").getClientTask("zeta-zeros");
    clientTask.setResources("2");    // use more memory
    ZetaStatistic.maxNumberToProceed = maxNumberToProceed;
    PrintStream stdOut = System.out;
    PrintStream log = null;
    try {
      log = new PrintStream(new FileOutputStream("statistic.log", true), true);
      System.setOut(log);
      String[] lastError = new String[2];
      lastError[0] = lastError[1] = "";
      int anzahlError = 0;
      while (true) {
        String[] error = null;
        try {
          error = statistic();
        } catch (ZipException ze) {
          ThrowableHandler.handle(ze);
          List workUnits = clientTask.createWorkUnits(new String[] { ze.getMessage() });
          if (workUnits.size() > 0 && ((WorkUnit)workUnits.get(0)).isValid()) {
            WorkUnit workUnit = (WorkUnit)workUnits.get(0);
            if (recomputation(workUnit.getWorkUnitId(), workUnit.getSize())) {
              continue;
            } else {
              throw ze;
            }
          }
        }
        if (error == null) {
          break;
        }
        if (error.length == 0) {
          continue;
        }
        long[] workUnitIdSize = new long[2];
        workUnitIdSize[0] = Long.parseLong(error[1])-100;
        List workUnits = clientTask.createWorkUnits(new String[] { error[4] });
        long workUnitId = (workUnits.size() > 0 && ((WorkUnit)workUnits.get(0)).isValid())? ((WorkUnit)workUnits.get(0)).getWorkUnitId() : 0;
        if (workUnitIdSize[0] < workUnitId) {
          workUnitIdSize[0] = workUnitId;
        }
        if (!lastError[0].equals(error[0]) || !lastError[1].equals(error[1])) {
          anzahlError = 0;
        } else if (++anzahlError == 2) {
          break;
        }
        lastError = error;
        if (anzahlError == 0) {
          workUnitIdSize[1] = 200;
          zetaZerosExpand(workUnitIdSize[0], (int)workUnitIdSize[1], 0);
          if (updateData(workUnitIdSize, error[2], error[3], error[4])) continue;
        }

        // Check fast algorithm:
        String statisticTmp = fastCheck(workUnitId, error);
        int idx = statisticTmp.indexOf(';')+1;
        int idx2 = statisticTmp.indexOf(';', idx);
        if (idx2 == -1 || statisticTmp == error[5]) {
          // Faster at beginning:
          while (true) {
            statisticTmp = error[5];
            for (workUnitIdSize[1] = 300; workUnitIdSize[1] <= 1300; workUnitIdSize[1] += 500, workUnitIdSize[0] -= 100) {
              zetaZerosExpand(workUnitIdSize[0], (int)workUnitIdSize[1], 0);
              if (updateData(workUnitIdSize, error[2], null, null)) break;
            }
            if (workUnitIdSize[1] > 1300) {
              for (workUnitIdSize[1] = 10000; workUnitIdSize[1] <= 50000; workUnitIdSize[1] += 20000, workUnitIdSize[0] -= 100) {
                zetaZerosExpand(workUnitIdSize[0], (int)workUnitIdSize[1], 0);
                if (updateData(workUnitIdSize, error[2], null, null)) break;
              }
              if (workUnitIdSize[1] > 50000) break;
            }
            statisticTmp = statistic(statisticTmp, ConstantProperties.TEMP_DIR, true);
            idx = statisticTmp.indexOf(';')+1;
            idx2 = statisticTmp.indexOf(';', idx);
            if (idx2 != -1) break;
            workUnitIdSize[0] = Long.parseLong(statisticTmp.substring(idx))-100;
            if (workUnitIdSize[0] < workUnitId) workUnitIdSize[0] = workUnitId;
          }
        }
        reduceFilesize2(ConstantProperties.TEMP_DIR + "zeta_zeros.txt", error[4]);
        updateData(null, error[2], error[3], error[4]);
      }
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    } finally {
      StreamUtils.close(log);
    }
    System.setOut(stdOut);
  }

  private static String[] statistic() throws Exception { // return not null if an error occur
    File file = new File(ConstantProperties.FINAL_DIR + '/' + clientTask.getId());
    String[] list = file.list();
    if (list != null) {
      long maxSize = 0;
      File tmpFile = null;
      for (int i = 0; i < list.length; ++i) {
        String s = list[i];
        int l = s.length();
        if (s.endsWith(".tmp") && s.startsWith("zeta_zeros_0_") && l > 13 && Character.isDigit(s.charAt(13))) {
          long r = Character.digit(s.charAt(13), 10);
          for (int idx = 14; idx < l; ++idx) {
            int j = Character.digit(s.charAt(idx), 10);
            if (j == -1) {
              break;
            }
            r = r*10 + j;
          }
          if (r > maxSize) {
            maxSize = r;
            tmpFile = new File(ConstantProperties.FINAL_DIR + '/' + clientTask.getId() + '/' + list[i]);
          }
        }
      }
      maxSize += 2;
      List workUnits = clientTask.createWorkUnits(list);
      Collections.sort(workUnits);
      String statisticTmp = null;
      if (tmpFile != null) {
        if (!lastTmpFilename.equals(tmpFile.getName())) {
          lastTmpFilename = tmpFile.getName();
          System.out.println(lastTmpFilename);
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream((int)tmpFile.length());
        StreamUtils.writeData(new FileInputStream(tmpFile), buffer, true, true);
        statisticTmp = buffer.toString();
      }
      byte[] buffer = new byte[1000000];
      for (int i = 0, l = workUnits.size(); i < l; ++i) {
        WorkUnit workUnit = (WorkUnit)workUnits.get(i);
        if (workUnit.getWorkUnitId() <= maxSize && workUnit.getWorkUnitId()+workUnit.getSize() > maxSize) {
          String s = "zeta_zeros_" + workUnit.getWorkUnitId() + '_';
          File f = new File(ConstantProperties.FINAL_DIR + '/' + clientTask.getId() + '/' + workUnit.getWorkUnitFileName());
          System.out.println(f.getName());
          try {
            ZipInputStream zip = new ZipInputStream(new FileInputStream(f));
            for (int k = 0; k < 2; ++k) {
              ZipEntry zEntry = zip.getNextEntry();
              String filename = null;
              String name = zEntry.getName();
              if (name.endsWith(".txt")) {
                filename = "zeta_zeros.txt";
              } else if (name.endsWith(".log")) {
                filename = "zeta_zeros.log";
              } else if (name.endsWith(".$$$")) {
                zip.close();
                GetData.decrypt(workUnit, ConstantProperties.FINAL_DIR + '/' + clientTask.getId() + '/' + f.getName());
                return new String[0];
              }
              if (filename == null) {
                System.err.println("Fatal error reading '" + f.getName() + "'!");
                System.exit(1);
              }
              File tempFile = new File(ConstantProperties.TEMP_DIR + filename);
              if (!tempFile.delete() && tempFile.exists()) {
                System.err.println("Fatal error deleting '" + f.getName() + "'!");
                System.exit(1);
              }
              try {
                StreamUtils.writeData(zip, new FileOutputStream(tempFile), false, true);
              } catch (IOException ioe) {
                if (filename.equals("zeta_zeros.txt") && tempFile.length() > 50*1024*1024) { // 50 MB
                  ThrowableHandler.handle(ioe);
                  throw new ZipException("Too large");
                } else {
                  throw ioe;
                }
              }
            }
            zip.close();
          } catch (NullPointerException npe) {
            ThrowableHandler.handle(npe);
            throw new ZipException(workUnit.getWorkUnitFileName());
          } catch (EOFException ioe) {
            ThrowableHandler.handle(ioe);
            throw new ZipException(workUnit.getWorkUnitFileName());
          } catch (ZipException ze) {
            ThrowableHandler.handle(ze);
            throw new ZipException(workUnit.getWorkUnitFileName());
          }
          if (reduceFilesize2(ConstantProperties.TEMP_DIR + "zeta_zeros.txt", ConstantProperties.TEMP_DIR + f.getName().substring(0, f.getName().length()-3) + "txt")) {
            updateData(null, ConstantProperties.TEMP_DIR + "zeta_zeros.txt", ConstantProperties.TEMP_DIR + "zeta_zeros.log", f.getName());
            return new String[0];
          }
          long[] problems = expandFileAndCheck(ConstantProperties.TEMP_DIR + "zeta_zeros.txt", workUnit);
          if (problems != null && problems.length > 1) {
            if (workUnit.getWorkUnitId() == previousWorkUnitId && problems.length >= previousNumberProblems && previousFirstProblem == problems[0]) {
              System.err.println("Fatal error: Again " + (problems.length/2) + " problems (" + (previousNumberProblems/2) + ')');
              System.exit(1);
            }
            long sizeOfProblem = 0;
            for (int k = 1; k < problems.length; k += 2) {
              sizeOfProblem += problems[k];
            }
            System.out.println("Number of problems: " + (problems.length/2) + ", size=" + sizeOfProblem);
            if (problems.length > 200 || sizeOfProblem*5 > workUnit.getSize()) {  // too many errors -> recomputation of the work unit
              throw new ZipException(workUnit.getWorkUnitFileName());
            }
            for (int k = 0; k < problems.length; k += 2) {
              zetaZerosExpand(problems[k], (int)problems[k+1], 0);
            }
            updateData(problems, ConstantProperties.TEMP_DIR + "zeta_zeros.txt", ConstantProperties.TEMP_DIR + "zeta_zeros.log", f.getName());
            previousWorkUnitId = workUnit.getWorkUnitId();
            previousNumberProblems = problems.length;
            previousFirstProblem = problems[0];
            return new String[0];
          }
          previousNumberProblems = 0;
          //int lines = checkDuplicateZeros(ConstantProperties.TEMP_DIR + "zeta_zeros.txt");
          //boolean update = (workUnit.getWorkUnitId() != previousWorkUnitId && lines == -1 && removeDuplicateZeros(ConstantProperties.TEMP_DIR + "zeta_zeros.txt"));
          previousWorkUnitId = workUnit.getWorkUnitId();
          if (!checkLog(ConstantProperties.TEMP_DIR + "zeta_zeros.log") || !checkHeader.checkHeader(workUnit.getWorkUnitId(), workUnit.getSize(), ConstantProperties.TEMP_DIR + "zeta_zeros.log")) {
            throw new ZipException(workUnit.getWorkUnitFileName());
          }
          if (problems != null && problems.length == 1) {
            int lines = (int)problems[0];
            if (lines > 0 && lines+500 < workUnit.getSize()) {
              String nameTxt = f.getName().substring(0, f.getName().length()-3) + "txt";
              String nameLog = f.getName().substring(0, f.getName().length()-3) + "log";
              file = new File(nameTxt);
              file.delete();
              new File(ConstantProperties.TEMP_DIR + "zeta_zeros.txt").renameTo(file);
              if (!removeLastChar(ConstantProperties.TEMP_DIR + "zeta_zeros.log", nameLog)) {
                throw new Exception("Internal Error!");
              }
              zetaZerosExpand(workUnit.getWorkUnitId(), workUnit.getSize(), 0);
              file = new File(ConstantProperties.TEMP_DIR + "zeta_zeros.log");
              file.delete();
              new File(nameLog).renameTo(file);
              file = new File(ConstantProperties.TEMP_DIR + "zeta_zeros.txt");
              file.delete();
              new File(nameTxt).renameTo(file);
              updateData(null, ConstantProperties.TEMP_DIR + "zeta_zeros.txt", ConstantProperties.TEMP_DIR + "zeta_zeros.log", f.getName());
              return new String[0];
            }
          }
          String statisticTmpOld = statisticTmp;
          statisticTmp = statistic(statisticTmp, ConstantProperties.TEMP_DIR, false);
          int idx = statisticTmp.indexOf(';')+1;
          int idx2 = statisticTmp.indexOf(';', idx);
          if (idx2 == -1) {   // error occur
            final String[] result = { statisticTmp.substring(0, idx-1), statisticTmp.substring(idx), ConstantProperties.TEMP_DIR + "zeta_zeros.txt", ConstantProperties.TEMP_DIR + "zeta_zeros.log", f.getName(), statisticTmpOld };
            return result;
          }
          maxSize = Long.parseLong(statisticTmp.substring(idx, idx2));
          System.out.println("maxSize=" + maxSize + ", lastLine=" + statisticTmp.substring(0, idx-1));
          FileWriter writer = new FileWriter(ConstantProperties.FINAL_DIR + '/' + clientTask.getId() + '/' + "zeta_zeros_0_" + maxSize + ".tmp");
          writer.write(statisticTmp, 0, statisticTmp.length());
          writer.close();
          f = new File(ConstantProperties.TEMP_DIR + "zeta_zeros.txt");
          f.delete();
          f = new File(ConstantProperties.TEMP_DIR + "zeta_zeros.log");
          f.delete();

          if (maxNumberToProceed > 0 && --maxNumberToProceed == 0) return null; // terminate to generate CDs
        }
      }
    }
    return null;
  }

  private static boolean checkLog(String filename) {
    BufferedReader reader = null;
    try {
      int count = 0;
      reader = new BufferedReader(new FileReader(filename));
      while (true) {
        String line = reader.readLine();
        if (line == null) {
          break;
        }
        if (line.indexOf("search3b") >= 0) {
          System.out.println(filename + " used search3b!");
          //System.exit(1);
        }
        if (line.startsWith(".... We make a shift at")) {
          System.out.println(filename + " contains shifts!");
          System.err.println(filename + " contains shifts!");
          return false;
        }
        /*if (line.startsWith("1.") && line.length() > 2) {
          try {
            Double.parseDouble(line.substring(2));
            if (++count >= 100) {
              System.out.println(filename + " contains zeros!");
              System.err.println(filename + " contains zeros!");
              return false;
            }
          } catch (NumberFormatException nfe) {
          }
        }*/
      }
    } catch (IOException ioe) {
      ThrowableHandler.handle(ioe);
    } finally {
      StreamUtils.close(reader);
    }
    return true;
  }

  private static boolean updateData(long[] workUnitIdSize, String sourceTxt, String sourceLog, String destination) throws IOException {
    boolean found = (workUnitIdSize != null);
    boolean foundOne = false;
    boolean change = false;
    int lines = 0;
    if (found) {
      File txt = new File("zeta_zeros_" + workUnitIdSize[0] + '_' + workUnitIdSize[1] + ".txt");
      System.out.println(txt.getName());
      BufferedReader reader = new BufferedReader(new FileReader(txt));
      BufferedReader reader2 = new BufferedReader(new FileReader(sourceTxt));
      BufferedWriter writer = new BufferedWriter(new FileWriter(ConstantProperties.TEMP_DIR + "tmp.txt"));
      String line = reader.readLine();
      String lineFull = line;
      String line2Full = null;
      if (line == null || line.length() == 0) {
        return false;
      }
      String prefix = "";
      String prefix2 = "";
      int idxWorkUnitIdSize = 2;
      if (line.charAt(0) == '.') {
        prefix = line.substring(1);
        line = reader.readLine();
        int idx = line.indexOf('.')+1;
        lineFull = line.substring(0, idx) + prefix + line.substring(idx);
      }
      double lineValue = Double.parseDouble(lineFull.substring(lineFull.indexOf('.')+1));
      System.out.println("start line=" + lineFull);
      boolean first = true;
      found = false;
      while (true) {
        String line2 = reader2.readLine();
        if (line2 == null || line2.length() == 0) {
          break;
        }
        if (first) {
          first = false;
          if (line2.charAt(0) == '.') {
            prefix2 = line2.substring(1);
            line2 = reader2.readLine();
          }
        }
        int idx = line2.indexOf('.')+1;
        line2Full = line2.substring(0, idx) + prefix2 + repairDot(line2.substring(idx));
        if (equalLines(line2Full, lineFull) || lineValue > 0.0 && Double.parseDouble(line2Full.substring(idx)) > lineValue) {
          writer.write(lineFull);
          writer.newLine();
          ++lines;
          found = true;
          while (true) {
            line = reader.readLine();
            if (line == null) break;
            if (line.length() == 0) continue;
            idx = line.indexOf('.')+1;
            lineFull = line.substring(0, idx) + prefix + line.substring(idx);
            writer.write(lineFull);
            writer.newLine();
            change = true;
            ++lines;
          }
          lineValue = 0.0;
          double valueLineFull = Double.parseDouble(lineFull.substring(lineFull.indexOf('.')+1));
          while (line2Full != null && !equalLines(lineFull, line2Full)) {
            line2 = reader2.readLine();
            if (line2 == null) break;
            if (line2.length() == 0) throw new IOException("wrong last shift!");
            idx = line2.indexOf('.')+1;
            String sl = prefix2 + repairDot(line2.substring(idx));
            if (sl.length() == 0) continue;
            line2Full = line2.substring(0, idx) + sl;
            if (Double.parseDouble(sl) > valueLineFull) {
              System.out.println("error: " + lineFull + ',' + line2Full);
              found = false;
              break;
            }
          }
          if (found) {
            foundOne = true;
          }
          /*if (!found && idxWorkUnitIdSize == workUnitIdSize.length) {
            break;
          }*/
          System.out.println("end line=" + line2Full + ", found=" + found);
          line = null;
          lineValue = 0.0;
          while (idxWorkUnitIdSize < workUnitIdSize.length) {
            if (txt != null) {
              reader.close();
              txt.delete();
              File log = new File("zeta_zeros_" + workUnitIdSize[idxWorkUnitIdSize-2] + '_' + workUnitIdSize[idxWorkUnitIdSize-1] + ".log");
              log.delete();
            }
            idxWorkUnitIdSize += 2;
            if (workUnitIdSize[idxWorkUnitIdSize-2] > 0) {
              txt = new File("zeta_zeros_" + workUnitIdSize[idxWorkUnitIdSize-2] + '_' + workUnitIdSize[idxWorkUnitIdSize-1] + ".txt");
              System.out.println(txt.getName());
              reader = new BufferedReader(new FileReader(txt));
              lineFull = line = reader.readLine();
              if (line == null || line.length() == 0) throw new IOException("internal io error!");
              if (line.charAt(0) == '.') {
                prefix = line.substring(1);
                line = reader.readLine();
                idx = line.indexOf('.')+1;
                lineFull = line.substring(0, idx) + prefix + line.substring(idx);
              } else prefix = "";
              System.out.println("start line=" + lineFull);
              lineValue = Double.parseDouble(lineFull.substring(lineFull.indexOf('.')+1));
              found = false;
              break;
            }
            txt = null;
          }
        } else {
          writer.write(line2Full);
          writer.newLine();
          ++lines;
        }
      }
      writer.close();
      reader.close();
      reader2.close();
      if (workUnitIdSize.length > 500 && !foundOne) {
        System.out.println("Fatal error: update not successful!");
        System.exit(1);
      }
      for (int i = 0; i < workUnitIdSize.length; i += 2) {
        txt = new File("zeta_zeros_" + workUnitIdSize[i] + '_' + workUnitIdSize[i+1] + ".txt");
        txt.delete();
        File log = new File("zeta_zeros_" + workUnitIdSize[i] + '_' + workUnitIdSize[i+1] + ".log");
        log.delete();
      }
    } else {
      File f = new File(sourceTxt);
      File txt = new File(ConstantProperties.TEMP_DIR + "tmp.txt");
      txt.delete();
      f.renameTo(txt);
      foundOne = found = true;
    }
    if (foundOne || change && lines+50 >= workUnitIdSize[1]) {
      File f = new File(sourceTxt);
      f.delete();
      File txt = new File(ConstantProperties.TEMP_DIR + "tmp.txt");
      txt.renameTo(f);
      if (destination != null) {
        String workUnit = ConstantProperties.TEMP_DIR + destination.substring(0, destination.length()-3) + "txt";
        f = new File(workUnit);
        new File(sourceTxt).renameTo(f);
        reduceFilesize(workUnit);
        // generate a new zip file including corrections
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(destination));
        zipOut.setLevel(Deflater.BEST_COMPRESSION);
        zipOut.putNextEntry(new ZipEntry(destination.substring(0, destination.length()-3) + "txt"));
        StreamUtils.writeData(new FileInputStream(workUnit), zipOut, true, false);
        zipOut.putNextEntry(new ZipEntry(destination.substring(0, destination.length()-3) + "log"));
        String dir = ConstantProperties.FINAL_DIR + '/' + clientTask.getId() + '/';
        StreamUtils.writeData(new FileInputStream(sourceLog), zipOut, true, true);
        f.delete();
        new File(dir + "tmp").mkdir();
        f = new File(dir + destination);
        f.renameTo(new File(dir + "tmp/" + destination));
        f.delete();
        new File(destination).renameTo(f);
      }
    }
    return found;
  }

  private static boolean equalLines(String line1, String line2) {
    if (line1.equals(line2)) {
      return true;
    }
    int l = line2.length();
    if (l > 2 && line1.startsWith(line2.substring(0, l-1))) {
      int i1 = line1.indexOf('.')+1;
      int i2 = line2.indexOf('.')+1;
      if (i1 > 0 && i1 == i2) {
        if (Math.abs(Double.parseDouble(line1.substring(i1))-Double.parseDouble(line2.substring(i2))) <= 0.001) {
          return true;
        }
      }
    }
    return false;
  }

  private static String fastCheck(long nStart, String[] error) {
    BufferedReader reader = null;
    try {
      int idx = 0;
      long lastN = 0;
      int lastSize = 100;
      List problems = new ArrayList(20);
      reader = new BufferedReader(new FileReader(ConstantProperties.TEMP_DIR + "zeta_zeros.log"));
      long nPos = Long.parseLong(error[1]);
      while (true) {
        String line = reader.readLine();
        if (line == null) break;
        if (line.startsWith("This happened between ") || line.startsWith("Exit n=")) {
          long n = 0;
          int i = (line.charAt(0) == 'T')? 22 : 7;
          final int l = line.length();
          while (i < l && Character.isDigit(line.charAt(i))) { n *= 10; n += Character.digit(line.charAt(i), 10); ++i; }
          if (nPos < n && lastN < nPos) {
            if (lastN+lastSize+10 < nPos) {
              lastSize = 100;
              long n2 = nPos-50;
              if (n2 < nStart) n2 = nStart;
              final Object[] o = { new Long(n2), new Integer(lastSize) };
              problems.add(o);
            } else {
              lastSize = (int)(nPos-lastN)+150;
              nPos = ((Long)((Object[])problems.get(problems.size()-1))[0]).longValue();
              final Object[] o = { new Long(nPos), new Integer(lastSize) };
              problems.add(problems.size()-1, o);
            }
            lastN = nPos;
          }
          if (lastN < n) {
            if (lastN+lastSize+10 < n) {
              lastSize = 100;
              long n2 = n-50;
              if (n2 < nStart) n2 = nStart;
              final Object[] o = { new Long(n2), new Integer(lastSize) };
              problems.add(o);
            } else {
              lastSize = (int)(n-lastN)+150;
              n = ((Long)((Object[])problems.get(problems.size()-1))[0]).longValue();
              final Object[] o = { new Long(n), new Integer(lastSize) };
              problems.set(problems.size()-1, o);
            }
          }
          lastN = n;
        }
      }
      int j = 0;
      int l = problems.size();
      if (l == 0) {
        final Object[] o = { new Long(nPos), new Integer(100) };
        problems.add(o);
        l = 1;
      }
      l *= 2;
      long[] workUnitIdSize = new long[l];
      for (int i = 0; i < l; i += 2) {
        Object[] o = (Object[])problems.get(i/2);
        workUnitIdSize[i]   = ((Long)o[0]).longValue();
        workUnitIdSize[i+1] = ((Integer)o[1]).intValue();
      }
      int repeat = 0;
      do {
        for (int i = 0; i < l; i += 2) {
          long n2 = workUnitIdSize[i]-10;
          if (n2 < nStart) n2 = nStart;
          workUnitIdSize[i] = n2;
          workUnitIdSize[i+1] += 20;
          zetaZerosExpand(workUnitIdSize[i], (int)workUnitIdSize[i+1], 0);
        }
      } while (!updateData(workUnitIdSize, error[2], error[3], null) && ++repeat < 3);
      reader.close();
      reader = null;
      return statistic(error[5], ConstantProperties.TEMP_DIR, false);
    } catch (IOException ioe) {
      ThrowableHandler.handle(ioe);
      return error[5];
    } finally {
      StreamUtils.close(reader);
    }
  }

  private static void check(int gramBlockLength, String filename) {
    BufferedReader reader = null;
    BufferedReader reader2 = null;
    try {
      int blocks = 0;
      List workUnits = clientTask.createWorkUnits(new String[] { filename });
      if (workUnits.size() > 0 && ((WorkUnit)workUnits.get(0)).isValid()) {
        long workUnitId = ((WorkUnit)workUnits.get(0)).getWorkUnitId();
        reader = new BufferedReader(new FileReader(filename));
        while (true) {
          String line = reader.readLine();
          if (line == null) {
            break;
          }
          if (line.indexOf('.') == gramBlockLength) {
            boolean found = false;
            if (zetaZerosExpand(workUnitId-5, 20, 0) != 0) {
              throw new IOException();
            }
            File f = new File("zeta_zeros_" + (workUnitId-5) + "_20.txt");
            reader2 = new BufferedReader(new FileReader(f));
            while (true) {
              String line2 = reader2.readLine();
              if (line2 == null) {
                break;
              }
              if (line2.equals(line)) {
                found = true;
                break;
              }
            }
            reader2.close();
            if (!found) {
              System.out.println("Error: " + line);
              return;
            }
            f.delete();
            f = new File("zeta_zeros_" + (workUnitId-5) + "_20.log");
            f.delete();
            for (int i = 1; i < gramBlockLength; ++i) {
              reader.readLine();
            }
            
            workUnitId += gramBlockLength-1;
            ++blocks;
          }
          ++workUnitId;
        }
        reader.close();
      }
      System.out.println(blocks + " blocks are checked.");
    } catch (IOException ioe) {
      ThrowableHandler.handle(ioe);
    } finally {
      StreamUtils.close(reader);
      StreamUtils.close(reader2);
    }
  }

  static {
    try {
      System.loadLibrary("zeta_zeros");
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    }
  }

  static long[] expandFileAndCheck(String filename, WorkUnit workUnit) throws Exception {
    return expandFileAndCheck(filename, filename, workUnit);
  }

  static long[] expandFileAndCheck(String source, String destination, WorkUnit workUnit) throws Exception {
    long maxWorkUnitId = (workUnit == null)? 0 : workUnit.getWorkUnitId()+workUnit.getSize();
    List problems = new ArrayList(100);
    BufferedReader reader = null;
    BufferedWriter writer = null;
    boolean equal = false;
    if (source.equals(destination)) {
      equal = true;
      destination += ".tmp";
    }
    int lines = 0;
    int wrongLines = 0;
    try {
      reader = new BufferedReader(new FileReader(source));
      String line = reader.readLine();
      if (line == null || line.length() == 0) {
        throw new Exception("Internal Error!");
      }
      writer = new BufferedWriter(new FileWriter(destination));
      String prefix = "";
      String previousLineFull = null;
      if (line.charAt(0) == '.') {
        prefix = line.substring(1);
      } else if (Character.isDigit(line.charAt(0))) {
        previousLineFull = repairDot(line);
        writer.write(previousLineFull);
        writer.newLine();
        ++lines;
      } else {
        throw new ZipException(workUnit.getWorkUnitFileName());
      }
      int expandLines = 0;
      int plausibilityChecks = 0;
      long nStart = -1;
      long problemNStart = -1;
      long workUnitId = -1;
      double gram = 9.6669;
      double value = 0.0;
      double nextGramPoint = 0.0;
      while (true) {
        line = reader.readLine();
        if (line == null || line.length() == 0) {
          break;
        }
        if (Character.isDigit(line.charAt(line.length()-1))) {
          plausibilityChecks |= 2;
        }
        int idx = line.indexOf('.')+1;
        if (idx > 1) {
          plausibilityChecks |= 1;
        }
        String postfix = repairDot(line.substring(idx));
        if (problemNStart >= 0) {
          if (justDots(line, idx)) {
            continue;
          }
          try {
            nextGramPoint = Double.parseDouble(prefix + postfix);
          } catch (NumberFormatException nfe) {
            continue;
          }
          long nPos = Math.max(getStartN(nextGramPoint)-2, problemNStart);
          double d = nextGramPoint;
          do {
            d = Gram(++nPos, d);
//System.err.println("nextGramPoint="+nextGramPoint+", d="+d);
          } while (d < nextGramPoint);
          nextGramPoint = d;
          boolean contains = false;
          if (problems.size() > 0) {
            long[] o = (long[])problems.get(problems.size()-1);
            contains = (nPos <= o[0]+o[1] && problemNStart >= o[0]);
          }
//System.err.println("postfix="+postfix+", nPos="+nPos+", problemNStart="+problemNStart+", previousLineFull="+previousLineFull+", contains="+contains);
          if (!contains) {
            problems.add(new long[] { problemNStart, nPos-problemNStart+5 });
          }
          problemNStart = -1;
          nStart = nPos;
        }
        if (workUnitId > 0 && nStart-workUnitId > 10 && previousLineFull != null && (idx == line.length() && line.charAt(0) == '1' && idx == 2 || line.indexOf(".........................", idx) > 0)) {
          double d = Double.parseDouble(previousLineFull.substring(previousLineFull.indexOf('.')+1));
          if (value > 0.0 && d < value) {
            continue;
          }
          nStart = getStartN(d);
          problemNStart = nStart-5;
//System.err.println("postfix="+postfix+", problemNStart="+problemNStart+", previousLineFull="+previousLineFull);
          continue;
        }
        String lineFull = "";
        if (prefix.length() > 0 && (postfix.length() == 0 && nStart >= 0 || postfix.length() > 0 && postfix.charAt(0) == '.')) {
          boolean equalGram = (postfix.length() == 0);
          if (nStart >= 0 && postfix.length() > 0) {
            if (nextGramPoint > 0.0) {
//System.err.println("gram="+nextGramPoint);
              gram = nextGramPoint;
              nextGramPoint = 0.0;
            } else {
              nStart += postfix.length();
              gram = Gram(nStart, gram);
            }
          } else if (postfix.length() > 1) {
            try {
              workUnitId = nStart = Long.parseLong(postfix.substring(1));
            } catch (NumberFormatException nfe) {
              ThrowableHandler.handle(nfe);
              throw new ZipException(workUnit.getWorkUnitFileName());
            }
            gram = Gram(nStart, (nStart > 1)? 6.2831853*nStart/Math.log((double)nStart) : 9.6669);
          }
          postfix = String.valueOf((long)Math.floor((gram - Math.floor(gram))*1000.0+0.5));
          if (postfix.length() == 4) {
            if (!equalGram) {
              gram += 1.0;
            }
            postfix = String.valueOf(Integer.parseInt(postfix.substring(1)));
          }
          String s = ".";
          switch (postfix.length()) {
            case 0: s = "";
                    break;
            case 1: if (postfix.charAt(0) != '0') {
                      s = ".00";
                    } else {
                      postfix = s = "";
                    }
                    break;
            case 2: s = ".0";
                    if (postfix.charAt(1) == '0') {
                      postfix = String.valueOf(postfix.charAt(0));
                    }
                    break;
            case 3: if (postfix.charAt(2) == '0') {
                      postfix = (postfix.charAt(1) == '0')? String.valueOf(postfix.charAt(0)) : postfix.substring(0, 2);
                    }
                    break;
          }
          lineFull = line.substring(0, idx) + String.valueOf((long)Math.floor(gram)) + s + postfix;
          if (++expandLines >= 10000) {
            System.out.println("Number of lines which are continuously reduced: " + expandLines);
            throw new ZipException(workUnit.getWorkUnitFileName());
          }
        } else {
          lineFull = line.substring(0, idx) + prefix + postfix;
          expandLines = 0;
        }
        // check if a large gap occur
        double valueLine = 0.0;
        try {
          valueLine = Double.parseDouble(lineFull.substring(lineFull.indexOf('.')+1));
        } catch (NumberFormatException nfe) {
          System.out.println("format error line: " + lineFull + ", prev=" + previousLineFull);
          continue;
        }
        if (value > 0.0 && Math.abs(valueLine-value) > 20000.0 || valueLine < value /*|| lineFull.equals(previousLineFull)*/) {
//System.err.println("error valueLine="+valueLine+", previousLineFull="+previousLineFull+", lineFull="+lineFull);
          ++wrongLines;
          continue;
        }
        if (value > 0.0 && valueLine-value > 5.0) {
          if (justDots(line, idx)) {
            continue;
          }
//System.err.println("gap value="+value+", valueLine="+valueLine+", previousLineFull="+previousLineFull+", lineFull="+lineFull);
          nextGramPoint = valueLine;
          long nPos = Math.max(getStartN(nextGramPoint)-2, problemNStart);
          if (maxWorkUnitId > 0 && nPos > maxWorkUnitId) {
            nPos = maxWorkUnitId-2;
            nextGramPoint = valueLine = Gram(maxWorkUnitId, nextGramPoint);
          }
          double d = nextGramPoint;
          do {
            d = Gram(++nPos, d);
//System.err.println("nextGramPoint="+nextGramPoint+", d="+d);
          } while (d < nextGramPoint);
          nextGramPoint = d;
          long prevNPos = getStartN(value);
//System.err.println("nPos="+nPos+", prevNPos="+prevNPos+", maxWorkUnitId="+maxWorkUnitId);
          boolean contains = false;
          if (problems.size() > 0) {
            long[] o = (long[])problems.get(problems.size()-1);
            contains = (nPos <= o[0]+o[1] && prevNPos >= o[0]);
          }
          if (!contains) {
            problems.add(new long[] { prevNPos, nPos-prevNPos+5 });
          }
          nStart = nPos;
        }
        writer.write(lineFull);
        writer.newLine();
        ++lines;
        previousLineFull = lineFull;
        value = valueLine;
      }
      if (plausibilityChecks < 3) {
        System.out.println("Wrong file format: " + source);
        System.err.println("Wrong file format: " + source);
        throw new ZipException(workUnit.getWorkUnitFileName());
      }
    } catch (ZipException ze) {
      throw ze;
    } catch (IOException ioe) {
      ThrowableHandler.handle(ioe);
      throw new Exception("Internal Error!");
    } finally {
      StreamUtils.close(reader);
      try {
        if (writer != null) {
          writer.close();
        }
      } catch (IOException ioe) {
        throw new Exception("Internal Error!");
      }
    }
    if (workUnit != null && (wrongLines*5 > workUnit.getSize() || wrongLines > 0 && lines*3 < workUnit.getSize())) {  // too many errors -> recomputation of the work unit
      System.out.println("Number of wrong lines: " + wrongLines + " of " + lines + " (" + workUnit.getSize() + ')');
      throw new ZipException(workUnit.getWorkUnitFileName());
    }
    if (equal) {
      File f = new File(source);
      f.delete();
      if (/*!f.delete() ||*/ !new File(destination).renameTo(f)) {
        throw new Exception("Internal Error!");
      }
    }
    if (problems.size() > 0) {
      long[] result = new long[2*problems.size()];
      Iterator i = problems.iterator();
      for (int j = 0; i.hasNext(); j += 2) {
        long[] value = (long[])i.next();
        result[j] = value[0];
        result[j+1] = value[1];
      }
      return result;
    }
    return new long[] { lines };
  }

  private static boolean removeLastChar(String source, String destination) {
    FileInputStream in = null;
    FileOutputStream out = null;
    try {
      byte[] buffer = new byte[1000000];
      long size = new File(source).length();
      in = new FileInputStream(source);
      out = new FileOutputStream(destination);
      while (true) {
        int n = in.read(buffer);
        if (n <= 0) break;
        size -= n;
        if (size == 0) {
          out.write(buffer, 0, n-1);
          break;
        } else {
          out.write(buffer, 0, n);
        }
      }
    } catch (IOException ioe) {
      ThrowableHandler.handle(ioe);
      return false;
    } finally {
      StreamUtils.close(in);
      try {
        if (out != null) {
          out.close();
        }
      } catch (IOException ioe) {
        return false;
      }
    }
    return true;
  }

  private static boolean recomputation(long workUnitId, int size) throws IOException {
    String nameTxt = "zeta_zeros_" + workUnitId + '_'+ size + ".txt";
    String nameLog = "zeta_zeros_" + workUnitId + '_'+ size + ".log";
    String destination = "zeta_zeros_" + workUnitId + '_'+ size + ".zip";
    File fTxt = new File(nameTxt);
    File fLog = new File(nameLog);
    fTxt.delete();
    fLog.delete();
    if (zetaZeros(workUnitId, size, 0) == 0) {
      ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(destination));
      zipOut.setLevel(Deflater.BEST_COMPRESSION);
      zipOut.putNextEntry(new ZipEntry(nameTxt));
      StreamUtils.writeData(new FileInputStream(nameTxt), zipOut, true, false);
      zipOut.putNextEntry(new ZipEntry(nameLog));
      String dir = ConstantProperties.FINAL_DIR + '/' + clientTask.getId() + '/';
      StreamUtils.writeData(new FileInputStream(nameLog), zipOut, true, true);
      fTxt.delete();
      fLog.delete();
      File f = new File(dir + "tmp");
      f.mkdir();
      f = new File(dir + destination);
      f.renameTo(new File(dir + "tmp/" + destination));
      f.delete();
      return (new File(destination).renameTo(f));
    }
    return false;
  }

  private static String repairDot(String s) {
    final int i = s.indexOf(',');
    if (i >= 0 && s.indexOf(',', i+1) < 0 && s.indexOf('.') < 0) {
      StringBuffer buffer = new StringBuffer(s);
      buffer.setCharAt(i, '.');
      s = buffer.toString();
    }
    return s;
  }

  private static boolean justDots(String s, int i) {
    final int l = s.length();
    for (; i < l; ++i) {
      if (s.charAt(i) != '.') {
        return false;
      }
    }
    return true;
  }

  static double Gram(long n) {
    return Gram(n, (n > 1)? 6.2831853*n/Math.log((double)n) : 9.6669);
  }


  final static double PI = 3.1415926535897932384626433832795028841971693993751;
  final static double PI_INV = 1.0/PI;
  final static double TWO_PI = 2*PI;
  final static double TWO_PI_INV = 0.5*PI_INV;
  static double Gram(long n, double a) { // Using a as an initial approximation the nth Gram point is calculated and assigned to b.
    double t2,t1 = a*TWO_PI_INV;
    double d = ((double)n) + 0.125;
    do {
      t2 = (t1+d)/Math.log(t1);
      if (Math.abs(t1-t2) < t2*1e-13) return t2*TWO_PI;
      t1 = (t2+d)/Math.log(t2);
    } while (Math.abs(t1-t2) >= t1*1e-13);
    return t1*TWO_PI;
  }

  static long getStartN(double t) {
    long n2,n1 = (long)(t*0.83675);
    double g2,g1 = Gram(n1);
    if (g1 > t) {
      do {
        n2 = n1; g2 = g1;
        n1 >>= 1;
        g1 = Gram(n1);
      } while (g1 > t);
    } else {
      n2 = n1; g2 = g1;
      while (true) {
        n2 <<= 1;
        g2 = Gram(n2);
        if (g2 > t) break;
        g1 = g2; n1 = n2;
      }
    }
    while (n2-n1 > 1) {
      long n = (n1+n2) >> 1;
      double g = Gram(n);
      if (g > t) n2 = n;
      else n1 = n;
    }
    return n1;
  }

  static int zetaZerosExpand(long workUnitId, int size, int sleep) {
    int result = zetaZeros(workUnitId, size, sleep);
    try {
      expandFileAndCheck("zeta_zeros_" + workUnitId + '_' + size + ".txt", clientTask.createWorkUnit(workUnitId, size, false));
      if (StreamUtils.search(new String[] { "... Close pair of zeros between" }, new FileInputStream("zeta_zeros_" + workUnitId + '_' + size + ".log"), true)) {
        System.out.println("Close zero in 'zeta_zeros_" + workUnitId + '_' + size + ".log'");
        System.exit(1); // ToDo: transfer this line to the master log file
      }
    } catch (Exception e) {
      ThrowableHandler.handle(e);
      System.exit(1);
    }
    return result;
  }

  private static boolean reduceFilesize2(String filename) {
    return reduceFilesize2(filename, filename);
  }

  private static boolean reduceFilesize2(String filename1, String filename2) {
    if (!filename1.equals(filename2)) {
      File f1 = new File(filename1);
      File f2 = new File(filename2);
      f2.delete();
      if (!f1.renameTo(f2)) return false;
      boolean result = reduceFilesize(filename2);
      return (f2.renameTo(f1) && result);
    }
    return reduceFilesize(filename1);
  }


  private static int previousNumberProblems = 0;
  private static long previousFirstProblem = 0;
  private static long previousWorkUnitId = 0;
  private static int maxNumberToProceed = -1;
  private static String lastTmpFilename = "";
  private static ZetaWorkUnitVerifier checkHeader = new ZetaWorkUnitVerifier();
  private static ClientTask clientTask = null;

  static native boolean reduceFilesize(String filename);
  private static native String statistic(String tmpEntry, String directory, boolean removeLine);
  private static native void verify(String smallFile, String largeFile);
  static native int zetaZeros(long start, int size, int sleepN);
}
