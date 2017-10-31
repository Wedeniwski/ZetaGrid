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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import zeta.server.tool.ConstantProperties;
import zeta.util.StreamUtils;
import zeta.util.StringUtils;
import zeta.util.ThrowableHandler;


/**
 *  @version 1.9.4, August 27, 2004
**/
public class ZetaStatisticSummary {
  public static void main(String[] args) {
    if (args.length == 2) {
      ZetaStatisticSummary stat = new ZetaStatisticSummary();
      if (args[0].equals("p")) {
        stat.print(args[1]);
        return;
      }
      if (args[0].equals("c")) {
        stat.createSummary(args[1]);
        return;
      }
      if (stat.checkType(Long.parseLong(args[0]), args[1])) {
        System.out.println("OK!");
      } else {
        System.out.println("Error!");
      }
    } else if (args.length == 3 && args[0].equals("u")) {
      try {
        ZetaStatisticSummary source = new ZetaStatisticSummary();
        ZetaStatisticSummary delta = new ZetaStatisticSummary();
        ZetaStatisticSummary destination = new ZetaStatisticSummary();
        source.read(new File(args[1]));
        delta.read(new File(ConstantProperties.FINAL_DIR + "/1/" + args[1]));  // ToDo: task id
        destination.read(new File(args[2]));
        destination.substract(delta);
        destination.add(source);
        File newFile = new File(args[2] + ".tmp");
        destination.write(newFile);
      } catch (IOException ioe) {
        ThrowableHandler.handle(ioe);
      }
    } else if (args.length == 1 && args[0].equals("z")) {
      ZetaStatisticSummary.zip(new File("summary/tmp"));
    } else if (args.length == 0) {
      ZetaStatisticSummary stat = new ZetaStatisticSummary();
      stat.checkMax();
    } else {
      System.err.println("USAGE: [<n> <Rosser block type>]\n"
                       + "       p <filename>\n"
                       + "       c <filename>\n"
                       + "       u <source> <destination>\n"
                       + "       z(ip)");
    }
  }

  static void zip(File path) {
    String[] filenames = path.list();
    if (filenames != null) {
      System.out.println("Number of files: " + filenames.length);
      Arrays.sort(filenames, new Comparator() {
        public int compare(Object o1, Object o2) {
          long r1 = getSize((String)o1);
          long r2 = getSize((String)o2);
          if (r1 == 0) {
            return (r2 == 0)? 0 : -1;
          }
          if (r1 == r2) {
            return 0;
          }
          return (r1 < r2)? -1 : 1;
        }

        public boolean equals(Object obj) {
          return true;
        }
      });
      int start = -1;
      for (int i = 0; i < filenames.length; ++i) {
        if (filenames[i].endsWith(".tmp")) {
          start = i;
        } else if (filenames[i].endsWith(".zip")) {
          if (start >= 0) {
            System.err.println("Error: " + filenames[i] + " contains " + filenames[start]);
            return;
          }
        } else {
          System.err.println("Wrong file format : " + filenames[i]);
          return;
        }
      }
      try {
        for (start = 0; start < filenames.length && !filenames[start].endsWith(".tmp"); ++start);
        for (int i = start+1000; i < filenames.length; i += 1000) {
          String zipFilename = filenames[i-1].substring(0, filenames[i-1].length()-3) + "zip";
          ZipOutputStream out = null;
          System.out.print(zipFilename);
          System.out.flush();
          try {
            out = new ZipOutputStream(new FileOutputStream(path.getAbsolutePath() + '/' + zipFilename));
            out.setLevel(Deflater.BEST_COMPRESSION);
            for (int j = start, k = 0; j < i; ++j, ++k) {
              out.putNextEntry(new ZipEntry(filenames[j]));
              StreamUtils.writeData(new FileInputStream(path.getAbsolutePath() + '/' + filenames[j]), out, true, false);
              if (k%100 == 0) {
                System.out.print('.');
                System.out.flush();
              }
            }
          } finally {
            StreamUtils.close(out);
            while (start < i) {
              new File(path.getAbsolutePath() + '/' + filenames[start]).delete();
              ++start;
            }
          }
          System.out.println("successfully created.");
        }
      } catch (IOException ioe) {
        ThrowableHandler.handle(ioe);
      }
    }
  }

  void substract(ZetaStatisticSummary stat) {
    // Number of Rosser blocks of given length
    if (!anzAll.equals(stat.anzAll)) {
      throw new IllegalArgumentException("Not equal list: Number of Rosser blocks of given length");
    }
    int l = stat.anz.size();
    if (anz.size() < l) {
      throw new IllegalArgumentException("Delta contains too many values");
    }
    for (int i = 0; i < l; ++i) {
      long a = ((Long)anz.get(i)).longValue();
      long b = ((Long)stat.anz.get(i)).longValue();
      if (a < b) {
        throw new IllegalArgumentException("Delta contains a too large value (" + b + ')');
      }
      anz.set(i, new Long(a-b));
    }
    // Number of Gram intervals containing excatly m zeros
    if (!zeros.equals(stat.zeros)) {
      throw new IllegalArgumentException("Not equal list: Number of Gram intervals containing excatly m zeros");
    }
    zeros.clear();
    l = stat.zero.size();
    if (zero.size() < l) {
      throw new IllegalArgumentException("Delta contains too many values");
    }
    for (int i = 0; i < l; ++i) {
      long a = ((Long)zero.get(i)).longValue();
      long b = ((Long)stat.zero.get(i)).longValue();
      if (a < b) {
        throw new IllegalArgumentException("Delta contains a too large value (" + b + ')');
      }
      zero.set(i, new Long(a-b));
    }
    // First occurrences and number of Rosser blocks of various types
    Iterator iter = stat.types.keySet().iterator();
    while (iter.hasNext()) {
      String type = (String)iter.next();
      long count = ((Long)stat.typesCount.get(type)).longValue();
      long firstOccurrence = ((Long)stat.types.get(type)).longValue();
      Long count2 = (Long)typesCount.get(type);
      Long firstOccurrence2 = (Long)types.get(type);
      if (count2 == null || firstOccurrence2 == null) {
        throw new IllegalArgumentException("Type '" + type + "' is not defined in destination");
      }
      if (count2.longValue() < count) {
        throw new IllegalArgumentException("The number of type '" + type + "' is too small in destination");
      }
      if (firstOccurrence2.longValue() != firstOccurrence) {
        throw new IllegalArgumentException("The first occurrence of type '" + type + "' is not equal");
      }
      typesCount.put(type, new Long(count2.longValue()-count));
    }
  }

  void add(ZetaStatisticSummary stat) {
    // Number of Rosser blocks of given length
    if (!anzAll.equals(stat.anzAll)) {
      throw new IllegalArgumentException("Not equal list: Number of Rosser blocks of given length");
    }
    int l1 = anz.size();
    int l2 = stat.anz.size();
    for (int i = 0; i < l1 || i < l2; ++i) {
      if (i < l1) {
        long b = (i < l2)? ((Long)stat.anz.get(i)).longValue() : 0;
        anz.set(i, new Long(((Long)anz.get(i)).longValue()+b));
      } else {
        anz.add(stat.anz.get(i));
      }
    }
    // Number of Gram intervals containing excatly m zeros
    if (zeros.size() > 0) {
      throw new IllegalArgumentException("List not empty: Number of Gram intervals containing excatly m zeros");
    }
    zeros = stat.zeros;
    l1 = zero.size();
    l2 = stat.zero.size();
    for (int i = 0; i < l1 || i < l2; ++i) {
      if (i < l1) {
        long b = (i < l2)? ((Long)stat.zero.get(i)).longValue() : 0;
        zero.set(i, new Long(((Long)zero.get(i)).longValue()+b));
      } else {
        zero.add(stat.zero.get(i));
      }
    }
    // First occurrences and number of Rosser blocks of various types
    Iterator iter = stat.types.keySet().iterator();
    while (iter.hasNext()) {
      String type = (String)iter.next();
      long count = ((Long)stat.typesCount.get(type)).longValue();
      long firstOccurrence = ((Long)stat.types.get(type)).longValue();
      Long count2 = (Long)typesCount.get(type);
      Long firstOccurrence2 = (Long)types.get(type);
      if (count2 == null || firstOccurrence2 == null) {
        typesCount.put(type, new Long(count));
        types.put(type, new Long(firstOccurrence));
      } else if (firstOccurrence2.longValue() < firstOccurrence) {
        throw new IllegalArgumentException("The first occurrence of type '" + type + "' is wrong");
      } else {
        typesCount.put(type, new Long(count2.longValue()+count));
        types.put(type, new Long(firstOccurrence));
      }
    }
  }

  boolean createSummary(String filename) {
    BufferedReader reader = null;
    ZipInputStream zip = null;
    try {
      zip = new ZipInputStream(new FileInputStream(filename));
      for (int k = 0; k < 2; ++k) {
        ZipEntry zEntry = zip.getNextEntry();
        if (zEntry == null) {
          throw new IOException(filename + " wrong entry!");
        }
        String s = zEntry.getName();
        if (s.endsWith(".txt")) {
          break;
        }
      }
      reader = new BufferedReader(new InputStreamReader(zip));
      String firstLine = reader.readLine();
      if (firstLine == null || firstLine.length() < 2 || firstLine.charAt(0) != '.') {
        System.err.println("work unit should start with the prefix");
        return false;
      }
      String secondLine = reader.readLine();
      if (secondLine == null || secondLine.length() < 4 || secondLine.charAt(0) != '1' || secondLine.charAt(1) != '.' || secondLine.charAt(2) != '.') {
        System.err.println("the second line in the work unit should contain the work unit id");
        return false;
      }
      id = Long.parseLong(secondLine.substring(3));
      double gram = ZetaStatistic.Gram(id, (id > 1)? 6.2831853*id/Math.log((double)id) : 9.6669);
      String postfix = String.valueOf((long)Math.floor((gram - Math.floor(gram))*1000.0+0.5));
      if (postfix.length() == 4) {
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
      lastLine = "1." + String.valueOf((long)Math.floor(gram)) + s + postfix;

      /*for (long n = Long.parseLong(secondLine.substring(3));; ++n) {
        String line = reader.readLine();
        if (line == null) {
          System.err.println("no synchronization point in the work unit");
          return false;
        }
        int idx = line.indexOf('.');
        if (idx+1 < line.length() && Character.isDigit(line.charAt(idx+1))) {
          lastLine = line.substring(0, idx+1) + firstLine.substring(1) + line.substring(idx+1);
          id = n+2;
          break;
        }
        if (!line.equals("1..")) {
          System.err.println("too difficult to find a synchronization point in the work unit");
          return false;
        }
      }*/
    } catch (IOException ioe) {
      ThrowableHandler.handle(ioe);
      return false;
    } finally {
      StreamUtils.close(zip);
      StreamUtils.close(reader);
    }
    System.out.println("the synchronization point is " + lastLine + " at " + id);
    types.clear();
    typesCount.clear();
    zeros.clear();
    zero.clear();
    anz.clear();
    anzAll.clear();
    List list = new ArrayList(1);
    list.add(new Long(id+1));
    anzAll.add(list);
    anz.add(new Long(0));
    anz.add(new Long(id+1));
    list = new ArrayList(2);
    list.add(new Long(0));
    list.add(new Long(id+1));
    zeros.add(list);
    zero.add(new Long(0));
    zero.add(new Long(id+1));
    String type = "                             1";
    types.put(type, new Long(-1));
    typesCount.put(type, new Long(id+1));
    File newFile = new File(ConstantProperties.FINAL_DIR + "/1/zeta_zeros_0_" + id + ".tmp");
    try {
      write(newFile);
    } catch (IOException ioe) {
      ThrowableHandler.handle(ioe);
      return false;
    }
    return true;
  }

  void checkMax() {
    File file = new File(ConstantProperties.FINAL_DIR + "/1");
    File[] list = file.listFiles();
    if (list != null) {
      long maxSize = 0;
      File statisticFile = null;
      for (int i = 0; i < list.length; ++i) {
        String s = list[i].getName();
        if (s.endsWith(".tmp")) {
          long r = getSize(s);
          if (r > maxSize) {
            maxSize = r;
            statisticFile = list[i];
          }
        }
      }
      if (statisticFile != null) {
        System.out.println(statisticFile.getName());
        try {
          read(statisticFile);
          correct();
          check(maxSize);
          File newFile = new File(statisticFile.getPath() + ".tmp");
          write(newFile);
          //statisticFile.delete();
          //newFile.renameTo(statisticFile);
        } catch (IOException ioe) {
          ThrowableHandler.handle(ioe);
        }
      }
    }
  }

  void print(String filename) {
    try {
      File statisticFile = new File(filename);
      read(statisticFile);
      correct();
      System.out.println(toString());
    } catch (IOException ioe) {
      ThrowableHandler.handle(ioe);
    }
  }

  public String toString() {
    String newLine = System.getProperty("line.separator");
    StringBuffer buffer = new StringBuffer(50000);
    buffer.append("Number of Rosser blocks of given length");
    buffer.append(newLine);
    long n = 100;
    long step = 100;
    Iterator iter = anzAll.iterator();
    while (iter.hasNext()) {
      StringUtils.format(false, ' ', 19, Long.toString(n), buffer);
      buffer.append(':');
      Iterator iter2 = ((List)iter.next()).iterator();
      while (iter2.hasNext()) {
        StringUtils.format(false, ' ', 20, ((Long)iter2.next()).toString(), buffer);
      }
      buffer.append(newLine);
      n += step;
      if (step*10 == n) {
        step *= 10;
      }
    }
    StringUtils.format(false, ' ', 19, Long.toString(id), buffer);
    buffer.append(':');
    iter = anz.iterator();
    if (iter.hasNext()) {
      iter.next();
    }
    while (iter.hasNext()) {
      StringUtils.format(false, ' ', 20, ((Long)iter.next()).toString(), buffer);
    }
    buffer.append(newLine);
    buffer.append(newLine);
    buffer.append("Number of Gram intervals containing excatly m zeros");
    buffer.append(newLine);
    n = 100;
    step = 100;
    iter = zeros.iterator();
    while (iter.hasNext()) {
      StringUtils.format(false, ' ', 20, Long.toString(n), buffer);
      Iterator iter2 = ((List)iter.next()).iterator();
      while (iter2.hasNext()) {
        StringUtils.format(false, ' ', 20, ((Long)iter2.next()).toString(), buffer);
      }
      buffer.append(newLine);
      n += step;
      if (step*10 == n) {
        step *= 10;
      }
    }
    StringUtils.format(false, ' ', 20, Long.toString(id), buffer);
    iter = zero.iterator();
    while (iter.hasNext()) {
      StringUtils.format(false, ' ', 20, ((Long)iter.next()).toString(), buffer);
    }
    buffer.append(newLine);
    buffer.append(newLine);
    buffer.append("First occurrences and number of Rosser blocks of various types");
    buffer.append(newLine);
    int countZeros = 0;
    iter = types.keySet().iterator();
    while (iter.hasNext()) {
      String type = (String)iter.next();
      Long count = (Long)typesCount.get(type);
      Long firstOccurrence = (Long)types.get(type);
      buffer.append(type);
      StringUtils.format(false, ' ', 20, firstOccurrence.toString(), buffer);
      StringUtils.format(false, ' ', 20, count.toString(), buffer);
      buffer.append(newLine);
    }
    return buffer.toString();
  }

  /**
   *  Reads the statistic summary file.
  **/
  private void read(File file) throws IOException {
    System.out.println("read " + file.getAbsolutePath());
    types.clear();
    typesCount.clear();
    zeros.clear();
    zero.clear();
    anz.clear();
    anzAll.clear();
    char[] tmp = new char[(int)file.length()];
    int n = 0;
    FileReader reader = new FileReader(file);
    try {
      while (n < tmp.length) {
        int i = reader.read(tmp, n, tmp.length-n);
        if (i <= 0) {
          break;
        }
        n += i;
      }
    } finally {
      reader.close();
    }
    for (n = 0; tmp[n] != ';'; ++n);
    lastLine = new String(tmp, 0, n);
    id = 0;
    while (tmp[++n] != ';') {
      id = id*10 + Character.digit(tmp[n], 10);
    }
    ++n;
    while (true) {
      List z = new ArrayList(20);
      while (tmp[n] != ':' && tmp[n] != ';') {
        long k = 0;
        while (tmp[n] != ',' && tmp[n] != ':' && tmp[n] != ';') {
          k = k*10 + Character.digit(tmp[n], 10);
          ++n;
        }
        z.add(new Long(k));
        if (tmp[n] == ',') ++n;
      }
      zeros.add(z);
      if (tmp[n] == ';') {
        break;
      }
      ++n;
    }
    ++n;
    while (true) {
      long k = 0;
      while (tmp[n] != ',' && tmp[n] != ';') {
        k = k*10 + Character.digit(tmp[n], 10);
        ++n;
      }
      zero.add(new Long(k));
      if (tmp[n] == ';') {
        break;
      }
      ++n;
    }
    ++n;
    while (true) {
      long k = 0;
      while (tmp[n] != ',' && tmp[n] != ';') {
        k = k*10 + Character.digit(tmp[n], 10);
        ++n;
      }
      anz.add(new Long(k));
      if (tmp[n] == ';') {
        break;
      }
      ++n;
    }
    ++n;
    while (true) {
      StringBuffer s = new StringBuffer(100);
      while (tmp[n] != ',') {
        s.append(tmp[n]);
        ++n;
      }
      long k = 0;
      boolean minus = false;
      if (tmp[n+1] == '-') {
        minus = true;
        ++n;
      }
      while (tmp[++n] != ':' && tmp[n] != ';') {
        k = k*10 + Character.digit(tmp[n], 10);
      }
      if (minus) {
        k = -k;
      }
      types.put(s.toString(), new Long(k));
      if (tmp[n] == ';') {
        break;
      }
      ++n;
    }
    ++n;
    while (true) {
      StringBuffer s = new StringBuffer(100);
      while (tmp[n] != ',') {
        s.append(tmp[n]);
        ++n;
      }
      long k = 0;
      while (++n < tmp.length && tmp[n] != ':') {
        k = k*10 + Character.digit(tmp[n], 10);
      }
      typesCount.put(s.toString(), new Long(k));
      if (n == tmp.length) {
        break;
      }
      ++n;
    }
    reader = new FileReader("rosser_blocks.txt");
    try {
      BufferedReader bReader = new BufferedReader(reader);
      while (true) {
        String line = bReader.readLine();
        if (line == null) {
          break;
        }
        n = line.indexOf(':');
        if (n >= 0) {
          List rosserBlocks = new ArrayList(20);
          final int l = line.length();
          ++n;
          while (n < l) {
            int i = n;
            while (i < l && line.charAt(i) == ' ') {
              ++i;
            }
            int j = i;
            while (j < l && line.charAt(j) != ' ') {
              ++j;
            }
            rosserBlocks.add(new Long(line.substring(i, j)));
            n = j;
          }
          anzAll.add(rosserBlocks);
        }
      }
      bReader.close();
    } finally {
      reader.close();
    }
  }

  /**
   *  Writes a statistic summary file.
  **/
  private void write(File file) throws IOException {
    StringBuffer buffer = new StringBuffer(20000);
    buffer.append(lastLine);
    buffer.append(';');
    buffer.append(Long.toString(id));
    buffer.append(';');
    int i;
    for (i = 0; i < zeros.size(); ++i) {
      List l = (List)zeros.get(i);
      if (l.size() > 0) {
        buffer.append(((Long)l.get(0)).toString());
        for (int j = 1; j < l.size(); ++j) {
          buffer.append(',');
          buffer.append(((Long)l.get(j)).toString());
        }
      }
      if (i+1 < zeros.size()) {
        buffer.append(':');
      }
    }
    buffer.append(';');
    if (zero.size() > 0) {
      buffer.append(((Long)zero.get(0)).toString());
      for (int j = 1; j < zero.size(); ++j) {
        buffer.append(',');
        buffer.append(((Long)zero.get(j)).toString());
      }
    }
    buffer.append(';');
    if (anz.size() > 0) {
      buffer.append(((Long)anz.get(0)).toString());
      for (int j = 1; j < anz.size(); ++j) {
        buffer.append(',');
        buffer.append(((Long)anz.get(j)).toString());
      }
    }
    buffer.append(';');
    Iterator iter = types.keySet().iterator();
    if (iter.hasNext()) {
      String type = (String)iter.next();
      buffer.append(type);
      buffer.append(',');
      buffer.append(((Long)types.get(type)).toString());
      while (iter.hasNext()) {
        type = (String)iter.next();
        buffer.append(':');
        buffer.append(type);
        buffer.append(',');
        buffer.append(((Long)types.get(type)).toString());
      }
    }
    buffer.append(';');
    iter = typesCount.keySet().iterator();
    if (iter.hasNext()) {
      String type = (String)iter.next();
      buffer.append(type);
      buffer.append(',');
      buffer.append(((Long)typesCount.get(type)).toString());
      while (iter.hasNext()) {
        type = (String)iter.next();
        buffer.append(':');
        buffer.append(type);
        buffer.append(',');
        buffer.append(((Long)typesCount.get(type)).toString());
      }
    }
    FileWriter writer = new FileWriter(file);
    writer.write(buffer.toString());
    writer.close();
  }

  private void check(long size) {
    // Checks the number of zero-patterns with the size of the size (number of Gram points)
    Iterator i = typesCount.keySet().iterator();
    long count = -1;
    while (i.hasNext()) {
      String type = (String)i.next();
      count += ((Long)typesCount.get(type)).longValue()*StringUtils.numberOfDigits(type);
    }
    if (count != size) {
      System.out.println("size=" + size + ", count=" + count);
      throw new RuntimeException("invalid count");
    }
    // ToDo: Checks
    /*i = zeros.iterator();
    while (i.hasNext()) {
      List list = (List)i.next();
      count = 0;
      for (int j = 1; j < list.size(); ++j) {
        count += j*((Long)list.get(j)).longValue();
      }
      System.out.println(count);
    }*/
    // Check first occurences
    i = types.keySet().iterator();
    while (i.hasNext()) {
      String type = (String)i.next();
      long start = ((Long)types.get(type)).longValue();
      if (start > 0) {
        if (!checkType(start, type)) {
          System.err.println("Error: type '" + type.trim() + "' did not occur at " + start + '!');
          return;
        }
      }
    }
  }

  private void correct() {
    // executed on 'zeta_zeros_0_126239529702.tmp'
    // error found at summary/tmp/zeta_zeros_0_8131070000.tmp  (01110,3) -> (21110,1)
    correct(8130814896L, new String[] { "01110", "3" }, new String[] { "21110", "1" }, 17111714889L);
    // error found at summary/tmp/zeta_zeros_0_9302931000.tmp (21130,00) -> (21110,1,1)
    correct(9302708401L, new String[] { "21130", "00" }, new String[] { "21110", "1", "1" }, 10386883653L);
    // error found at summary/tmp/zeta_zeros_0_11994084402.tmp (21310,00) -> (21110,1,1)
    // error found at summary/tmp/zeta_zeros_0_14824707298.tmp (21310,00) -> (21110,1,1)
    // error found at summary/tmp/zeta_zeros_0_15746718909.tmp (21310,00) -> (21110,1,1)
    // error found at summary/tmp/zeta_zeros_0_18371281000.tmp (21310,00) -> (21110,1,1)
    // error found at summary/tmp/zeta_zeros_0_20599013598.tmp (21310,00) -> (21110,1,1)
    // error found at summary/tmp/zeta_zeros_0_22853358098.tmp (21310,00) -> (21110,1,1)
    // error found at summary/tmp/zeta_zeros_0_24272477404.tmp (21310,00) -> (21110,1,1)
    correct(11993665779L, new String[] { "21310", "00" }, new String[] { "21110", "1", "1" }, 14824201450L);
    correct(14824201450L, new String[] { "21310", "00" }, new String[] { "21110", "1", "1" }, 15746219663L);
    correct(15746219663L, new String[] { "21310", "00" }, new String[] { "21110", "1", "1" }, 18370782525L);
    correct(18370782525L, new String[] { "21310", "00" }, new String[] { "21110", "1", "1" }, 20598814223L);
    correct(20598814223L, new String[] { "21310", "00" }, new String[] { "21110", "1", "1" }, 22852859280L);
    correct(22852859280L, new String[] { "21310", "00" }, new String[] { "21110", "1", "1" }, 24271981140L);
    correct(24271981140L, new String[] { "21310", "00" }, new String[] { "21110", "1", "1" }, 26375964557L);
    // error found at summary/tmp/zeta_zeros_0_15935880106.tmp (23110,00) -> (21110,1,1)
    // error found at summary/tmp/zeta_zeros_0_16749912598.tmp (23110,00) -> (21110,1,1)
    // error found at summary/tmp/zeta_zeros_0_16804101401.tmp (23110,00) -> (21110,1,1)
    // error found at summary/tmp/zeta_zeros_0_17753506702.tmp (23110,00) -> (21110,1,1)
    // error found at summary/tmp/zeta_zeros_0_20483037108.tmp (23110,00) -> (21110,1,1)
    // error found at summary/tmp/zeta_zeros_0_20907450604.tmp (23110,00) -> (21110,1,1)
    // error found at summary/tmp/zeta_zeros_0_24217788506.tmp (23110,00) -> (21110,1,1)
    // error found at summary/tmp/zeta_zeros_0_26290892303.tmp (23110,00) -> (21110,1,1)
    // error found at summary/tmp/zeta_zeros_0_26984064908.tmp (23110,00) -> (21110,1,1)
    // error found at summary/tmp/zeta_zeros_0_28098253105.tmp (23110,00) -> (21110,1,1)
    // error found at summary/tmp/zeta_zeros_0_30900924605.tmp (23110,00) -> (21110,1,1)
    // error found at summary/tmp/zeta_zeros_0_68324225501.tmp (23110,00) -> (21110,1,1)
    correct(15935841803L, new String[] { "23110", "00" }, new String[] { "21110", "1", "1" }, 16749418217L);
    correct(16749418217L, new String[] { "23110", "00" }, new String[] { "21110", "1", "1" }, 16803602231L);
    correct(16803602231L, new String[] { "23110", "00" }, new String[] { "21110", "1", "1" }, 17753007263L);
    correct(17753007263L, new String[] { "23110", "00" }, new String[] { "21110", "1", "1" }, 20482537801L);
    correct(20482537801L, new String[] { "23110", "00" }, new String[] { "21110", "1", "1" }, 20906951768L);
    correct(20906951768L, new String[] { "23110", "00" }, new String[] { "21110", "1", "1" }, 24217292623L);
    correct(24217292623L, new String[] { "23110", "00" }, new String[] { "21110", "1", "1" }, 26290392923L);
    correct(26290392923L, new String[] { "23110", "00" }, new String[] { "21110", "1", "1" }, 26983566636L);
    correct(26983566636L, new String[] { "23110", "00" }, new String[] { "21110", "1", "1" }, 28097753827L);
    correct(28097753827L, new String[] { "23110", "00" }, new String[] { "21110", "1", "1" }, 30899929053L);
    correct(30899929053L, new String[] { "23110", "00" }, new String[] { "21110", "1", "1" }, 68323726035L);
    correct(68323726035L, new String[] { "23110", "00" }, new String[] { "21110", "1", "1" }, 76818475555L);
    // error found at summary/tmp/zeta_zeros_0_24317068898.tmp (211130,00,02) -> (211110,00,22)
    correct(24317005112L, new String[] { "211130", "00", "02" }, new String[] { "211110", "00", "22" }, 0);
    // error found at summary/tmp/zeta_zeros_0_25297378302.tmp (211310,00,1) -> (211110,00,3)
    correct(25297162824L, new String[] { "211310", "00", "1" }, new String[] { "211110", "00", "3" }, 0);
    // error found at summary/tmp/zeta_zeros_0_17509956002.tmp (211310,00,1) -> (211110,00,3)
    // error found at summary/tmp/zeta_zeros_0_25983748998.tmp (211310,00,1) -> (211110,00,3)
    correct(17509921902L, new String[] { "213110", "00", "1" }, new String[] { "211110", "00", "3" }, 25983083344L);
    correct(25983083344L, new String[] { "213110", "00", "1" }, new String[] { "211110", "00", "3" }, 0);  
    // error found at summary/tmp/zeta_zeros_0_10747637700.tmp (231110,00,1) -> (211110,00,3)
    // error found at summary/tmp/zeta_zeros_0_12706740098.tmp (231110,00,1) -> (211110,00,3)
    correct(10747518961L, new String[] { "231110", "00", "1" }, new String[] { "211110", "00", "3" }, 12706554093L);
    correct(12706554093L, new String[] { "231110", "00", "1" }, new String[] { "211110", "00", "3" }, 0);
    // error (2131110,00,1) -> (2111110,00,3)
    correct(18993705653L, new String[] { "2131110", "00", "1" }, new String[] { "2111110", "00", "3" }, 0);
  }

  private void correct(long wrongPos, String[] wrongPattern, String[] correctPattern, long correctPosForWrongPattern) {
    // correct "First occurrences and number of Rosser blocks of various types"
    String type = StringUtils.format(false, ' ', 30, wrongPattern[0]);
    Long count = (Long)typesCount.get(type);
    Long firstOccurrence = (Long)types.get(type);
    if (firstOccurrence != null && firstOccurrence.longValue() == wrongPos && id >= correctPosForWrongPattern && (count.longValue() > 1 || count.longValue() == 1 && correctPosForWrongPattern == 0)) {
      if (correctPosForWrongPattern == 0) {
        types.remove(type);
      } else {
        types.put(type, new Long(correctPosForWrongPattern));
      }
      int[] z = new int[10];
      int[] a = new int[30];
      for (int i = 0; i < wrongPattern.length; ++i) {
        type = StringUtils.format(false, ' ', 30, wrongPattern[i]);
        long value = ((Long)typesCount.get(type)).longValue();
        if (value == 1) {
          typesCount.remove(type);
        } else {
          typesCount.put(type, new Long(value-1));
        }
        type = wrongPattern[i];
        int l = type.length();
        --a[l];
        for (int j = 0; j < l; ++j) {
          --z[Character.digit(type.charAt(j), 10)];
        }
      }
      for (int i = 0; i < correctPattern.length; ++i) {
        type = StringUtils.format(false, ' ', 30, correctPattern[i]);
        typesCount.put(type, new Long(((Long)typesCount.get(type)).longValue()+1));
        type = correctPattern[i];
        int l = type.length();
        ++a[l];
        for (int j = 0; j < l; ++j) {
          ++z[Character.digit(type.charAt(j), 10)];
        }
      }
      // correct "Number of Gram intervals containing excatly m zeros" 
      int max = z.length;
      while (max > 0 && z[--max] == 0);
      long n = 100;
      long step = 100;
      Iterator iter = zeros.iterator();
      while (iter.hasNext()) {
        List list = (List)iter.next();
        if (n >= wrongPos) {
          final int l = list.size();
          if (l > max) {
            for (int i = 0; i <= max; ++i) {
              list.set(i, new Long(((Long)list.get(i)).longValue()+z[i]));
            }
          }
        }
        n += step;
        if (step*10 == n) {
          step *= 10;
        }
      }
      // correct "Number of Rosser blocks of given length"
      int l = anz.size();
      for (int i = 1; i < l; ++i) {
        anz.set(i, new Long(((Long)anz.get(i)).longValue()+a[i]));
      }
      n = 100;
      step = 100;
      iter = anzAll.iterator();
      while (iter.hasNext()) {
        List list = (List)iter.next();
        if (n >= wrongPos) {
          for (int i = 0; i < list.size(); ++i) {
            list.set(i, new Long(((Long)list.get(i)).longValue()+a[i+1]));
          }
        }
        n += step;
        if (step*10 == n) {
          step *= 10;
        }
      }
    }
  }

  private boolean checkType(long start, String type) {
    type = type.trim();
    int size = type.length()+21;   // since exceptions to Rosser's rule
    start -= 10;
    int ignoreLines = 12;
    boolean typeOccur = false;
    boolean again;
    boolean rosserException = false;
    do {
      again = false;
      ZetaStatistic.zetaZerosExpand(start, size, 0);
      BufferedReader reader = null;
      try {
        reader = new BufferedReader(new FileReader("zeta_zeros_" + start + '_' + size + ".log"));
        while (true) {
          String line = reader.readLine();
          if (line == null) {
            break;
          }
          if (line.startsWith("LASTN is decreased by 1 due to the bad initial Gram point ")) {
            ++ignoreLines;
            again = true;
            break;
          }
        }
        reader.close();  
        if (!again) {
          reader = new BufferedReader(new FileReader("zeta_zeros_" + start + '_' + size + ".txt"));
          int checkLines = type.length();
          while (true) {
            String line = reader.readLine();
            if (line == null) {
              break;
            }
            if (rosserException && (line.startsWith("3.") || line.startsWith("5."))) {
              ++ignoreLines;
            }
            if (--ignoreLines <= 0) {
              if (ignoreLines == 0 && !line.startsWith(type + '.')) {
                break;
              }
              if (--checkLines == 0) {
                typeOccur = true;
                break;
              }
            }
            rosserException = (line.startsWith("3.") || line.startsWith("5."));
          }
        }
      } catch (IOException ioe) {
        ThrowableHandler.handle(ioe);
      } finally {
        StreamUtils.close(reader);
        if (again || typeOccur) {
          new File("zeta_zeros_" + start + '_' + size + ".log").delete();
          new File("zeta_zeros_" + start + '_' + size + ".txt").delete();
        }
        --start; ++size;
      }
    } while (again);
    return typeOccur;
  }

  private static long getSize(String filename) {
    int l = filename.length();
    if (filename.startsWith("zeta_zeros_0_") && l > 13 && Character.isDigit(filename.charAt(13))) {
      int idx = 13;
      while (++idx < l && Character.isDigit(filename.charAt(idx)));
      return Long.parseLong(filename.substring(13, idx));
    }
    return 0;
  }

  private String lastLine = null;
  private long id = 0;
  private Map types = new TreeMap();
  private Map typesCount = new TreeMap();
  private List zeros = new ArrayList(1000);
  private List zero = new ArrayList(1000);
  private List anz = new ArrayList(1000);
  private List anzAll = new ArrayList(500);
}
