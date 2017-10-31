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

package zeta.crypto;

import java.io.IOException;
import java.io.OutputStream;

/**
 *  @version 1.9.3, May 29, 2004
**/
public class Bzip2OutputStream extends OutputStream implements Bzip2Constants {
  public Bzip2OutputStream(OutputStream out) throws IOException {
    this(out, 9);
  }

  public Bzip2OutputStream(OutputStream out, int blockSize100k) throws IOException {
    this.out = out;
    bsLive = 0;
    bsBuff = 0;
    if (blockSize100k > 9) {
      blockSize100k = 9;
    } else if (blockSize100k < 1) {
      blockSize100k = 1;
    }
    this.blockSize100k = blockSize100k;
    allowableBlockSize = BASE_BLOCK_SIZE * blockSize100k - 20;
    int n = BASE_BLOCK_SIZE * blockSize100k;
    block = new char[(n + 1 + NUM_OVERSHOOT_BYTES)];
    quadrant = new int[(n + NUM_OVERSHOOT_BYTES)];
    map = new int[n];
    smap = new short[2*n];
    nBlocksRandomised = 0;
    write(8, 'h');
    write(8, '0' + blockSize100k);
    initBlock();
  }

  public void write(int b) throws IOException {
    b = (b+256)&0xff;
    if (currentChar != -1) {
      if (currentChar != b) {
        writeRun();
        currentChar = b;
        runLength = 1;
      } else if (++runLength >= 255) {
        writeRun();
        currentChar = -1;
        runLength = 0;
      }
    } else {
      currentChar = b;
      ++runLength;
    }
  }

  public void flush() throws IOException {
    super.flush();
    out.flush();
  }

  public void close() throws IOException {
    if (closed) {
      return;
    }
    if (runLength > 0) {
      writeRun();
    }
    currentChar = -1;
    sortBlock();
    write(8, 0x17);
    write(8, 0x72);
    write(8, 0x45);
    write(8, 0x38);
    write(8, 0x50);
    write(8, 0x90);
    writeInt(combinedCRC);
    while (bsLive > 0) {
      out.write(bsBuff >> 24); // write 8-bit
      bsBuff <<= 8;
      bsLive -= 8;
    }
    try {
      super.close();
      out.close();
    } finally {
      closed = true;
    }
  }

  protected void finalize() throws Throwable {
    close();
  }

  private void writeRun() throws IOException {
    if (last >= allowableBlockSize) {
      sortBlock();
      initBlock();
    }
    crc.update(currentChar, runLength);
    char ch = (char)currentChar;
    inUse[ch] = true;
    switch (runLength) {
      case 1:
        block[last+2] = ch;
        ++last;
        break;
      case 2:
        block[last+2] = block[last+3] = ch;
        last += 2;
        break;
      case 3:
        block[last+2] = block[last+3] = block[last+4] = ch;
        last += 3;
        break;
      default:
        inUse[runLength-4] = true;
        block[last+2] = block[last+3] = block[last+4] = block[last+5] = ch;
        block[last+6] = (char)(runLength-4);
        last += 5;
        break;
    }
  }

  private void initBlock() {
    crc.init();
    last = -1;
    for (int i = 255; i >= 0; --i) {
      inUse[i] = false;
    }
  }

  private void sortBlock() throws IOException {
    int blockCRC = crc.get();
    combinedCRC = ((combinedCRC << 1) | (combinedCRC >>> 31)) ^ blockCRC;
    budget = 50 * last;
    blockRandomised = false;
    firstBudget = true;
    sort();
    if (firstBudget && budget < 0) {
      int b = 0;
      int c = 0;
      for (int i = 255; i >= 0; --i) {
        inUse[i] = false;
      }
      for (int i = 0; i <= last; ++i) {
        if (b == 0) {
          b = (char)randomNumbers[c];
          if (++c == 512) {
            c = 0;
          }
        }
        --b;
        char a = (char)((block[i+1] ^ ((b == 1)? 1 : 0)) & 0xff);
        block[i+1] = a;
        inUse[a] = true;
      }
      budget = 0;
      blockRandomised = true;
      firstBudget = false;
      sort();
    }
    int origPtr = -1;
    for (int i = 0; i <= last; i++) {
      if (map[i] == 0) {
        origPtr = i;
        break;
      }
    }
    if (origPtr == -1) {
      throw new IllegalStateException("pointer not found");
    }
    write(8, 0x31);
    write(8, 0x41);
    write(8, 0x59);
    write(8, 0x26);
    write(8, 0x53);
    write(8, 0x59);
    writeInt(blockCRC);
    if (blockRandomised) {
      write(1, 1);
      ++nBlocksRandomised;
    } else {
      write(1, 0);
    }
    write(24, origPtr);
    sendValues(generateValues());
  }

  private void write(int n, int v) throws IOException {
    int b = bsBuff;
    int a = bsLive;
    if (a >= 24) {
      out.write(b >> 24);           // write 8-bit
      out.write((b >> 16) & 0xff);  // write 8-bit
      out.write((b >> 8) & 0xff);   // write 8-bit
      b <<= 24;
      a -= 24;
    } else if (a >= 16) {
      out.write(b >> 24);           // write 8-bit
      out.write((b >> 16) & 0xff);  // write 8-bit
      b <<= 16;
      a -= 16;
    } else if (a >= 8) {
      out.write(b >> 24);           // write 8-bit
      b <<= 8;
      a -= 8;
    }
    bsBuff = b | (v << (32-a-n));
    bsLive = a+n;
  }

  private void writeInt(int u) throws IOException {
    write(8, (u >> 24) & 0xff);
    write(8, (u >> 16) & 0xff);
    write(8, (u >> 8) & 0xff);
    write(8, u & 0xff);
  }

  private int generateValues() {
    final short RUNA = 0;
    final short RUNB = 1;
    char[] unseqToSeq = new char[256];
    char[] yy = new char[256];
    int nInUse = 0;
    int wr = 0;
    int x = 0;
    for (int i = 0; i < 256; ++i) {
      if (inUse[i]) {
        unseqToSeq[i] = (char)nInUse;
        ++nInUse;
      }
    }
    mtfFreq = new int[nInUse+2];
    mtfFreq[nInUse+1] = mtfFreq[nInUse] = 0;
    for (int i = nInUse-1; i >= 0; --i) {
      mtfFreq[i] = 0;
      yy[i] = (char)i;
    }
    for (int i = 0; i <= last; ++i) {
      int j = 0;
      char t = yy[j];
      for (char c = unseqToSeq[block[map[i]]]; c != t; ) {
        char t2 = t;
        t = yy[++j];
        yy[j] = t2;
      }
      if (j == 0) {
        ++x;
      } else {
        yy[0] = t;
        if (x > 0) {
          --x;
          while (true) {
            short s = ((x&1) == 0)? RUNA : RUNB;
            smap[wr++] = s;
            ++mtfFreq[s];
            if (x <= 1) {
              break;
            }
            x = (x-2)/2;
          }
          x = 0;
        }
        smap[wr++] = (short)(j+1);
        ++mtfFreq[j+1];
      }
    }
    if (--x >= 0) {
      while (true) {
        short s = ((x&1) == 0)? RUNA : RUNB;
        smap[wr++] = s;
        ++mtfFreq[s];
        if (x <= 1) {
          break;
        }
        x = (x-2)/2;
      }
    }
    smap[wr] = (short)(nInUse+1);
    ++mtfFreq[nInUse+1];
    nMTF = ++wr;
    return nInUse;
  }

  private int getNumberOfGroups() {
    int nGroups = 6;
    if (nMTF <= 0) {
      throw new IllegalStateException("wrong coding table");
    }
    if (nMTF < 200) {
      nGroups = 2;
    } else if (nMTF < 600) {
      nGroups = 3;
    } else if (nMTF < 1200) {
      nGroups = 4;
    } else if (nMTF < 2400) {
      nGroups = 5;
    }
    return nGroups;
  }

  private char[][] prepareValues(int nInUse) {
    final int N_ITERS = 4;
    final char GREATER_ICOST = 15;
    final char LESSER_ICOST = 0;
    int alphaSize = nInUse+2;
    int nGroups = getNumberOfGroups();
    char[][] len = new char[nGroups][alphaSize];
    int[][] rfreq = new int[nGroups][alphaSize];
    int[] fave = new int[nGroups];
    short[] cost = new short[nGroups];
    int remF = nMTF;
    int gs = 0;
    nSelectors = 0;
    for (int i = nGroups; i > 0; --i) {
      int ge = gs-1;
      int aFreq = 0;
      for (int tFreq = remF / i; aFreq < tFreq && ge < alphaSize-1; aFreq += mtfFreq[++ge]);
      if (ge > gs && i != nGroups && i != 1 && ((nGroups-i)&1) == 1) {
        aFreq -= mtfFreq[ge--];
      }
      for (int v = 0; v < alphaSize; ++v) {
        len[i-1][v] = (v >= gs && v <= ge)? LESSER_ICOST : GREATER_ICOST;
      }
      gs = ge+1;
      remF -= aFreq;
    }
    for (int iter = 0; iter < N_ITERS; ++iter) {
      for (int t = nGroups-1; t >= 0; --t) {
        fave[t] = 0;
        for (int v = alphaSize-1; v >= 0; --v) {
          rfreq[t][v] = 0;
        }
      }
      nSelectors = 0;
      int totalCost = 0;
      gs = 0;
      selector = new byte[((nMTF-gs)/G_SIZE)+1];
      while (gs < nMTF) {
        int ge = gs+G_SIZE-1;
        if (ge >= nMTF) {
          ge = nMTF-1;
        }
        for (int t = nGroups-1; t >= 0; --t) {
          cost[t] = 0;
        }
        if (nGroups == 6 && gs <= ge) {
          cost6(cost, smap, len, gs, ge);
        } else {
          for (int i = gs; i <= ge; ++i) {
            short icv = smap[i];
            for (int t = nGroups-1; t >= 0; --t) {
              cost[t] += len[t][icv];
            }
          }
        }
        int bc = 999999999;
        byte bt = -1;
        for (int t = 0; t < nGroups; ++t) {
          if (cost[t] < bc) {
            bc = cost[t];
            bt = (byte)t;
          }
        }
        totalCost += bc;
        ++fave[bt];
        selector[nSelectors++] = bt;
        for (int i = gs; i <= ge; ++i) {
          ++rfreq[bt][smap[i]];
        }
        gs = ge+1;
      }
      for (int t = 0; t < nGroups; ++t) {
        makeCodeLengths(len[t], rfreq[t], alphaSize, 20);
      }
    }
    return len;
  }

  private void sendValues(int nInUse) throws IOException {
    char[][] len = prepareValues(nInUse);
    int alphaSize = nInUse+2;
    int nGroups = getNumberOfGroups();
    byte[] selectorMtf = new byte[nSelectors];
    byte[] pos = new byte[nGroups];
    for (byte i = (byte)(nGroups-1); i >= 0; --i) {
      pos[i] = i;
    }
    for (int i = 0; i < nSelectors; ++i) {
      int j = 0;
      byte t1 = pos[j];
      for (byte c = selector[i]; c != t1;) {
        byte t2 = t1;
        t1 = pos[++j];
        pos[j] = t2;
      }
      pos[0] = t1;
      selectorMtf[i] = (byte)j;
    }
    int[][] code = new int[nGroups][alphaSize];
    for (int t = 0; t < nGroups; ++t) {
      char minLen = 32;
      char maxLen = 0;
      for (int i = alphaSize-1; i >= 0; --i) {
        char c = len[t][i];
        if (c > maxLen) {
          maxLen = c;
        }
        if (c < minLen) {
          minLen = c;
        }
      }
      if (maxLen > 20 || minLen < 1) {
        throw new IllegalStateException("wrong length");
      }
      int vec = 0;
      for (int n = minLen; n <= maxLen; ++n) {
        for (int i = 0; i < alphaSize; ++i) {
          if (len[t][i] == n) {
            code[t][i] = vec++;
          }
        }
        vec <<= 1;
      }
    }
    boolean[] inUse16 = new boolean[16];
    for (int i = 0; i < 16; ++i) {
      inUse16[i] = false;
      for (int j = 15; j >= 0; --j) {
        if (inUse[16*i+j]) {
          inUse16[i] = true;
          break;
        }
      }
      write(1, (inUse16[i])? 1 : 0);
    }
    for (int i = 0; i < 16; ++i) {
      if (inUse16[i]) {
        for (int j = 0, k = 16*i; j < 16; ++j) {
          write(1, (inUse[k+j])? 1 : 0);
        }
      }
    }
    write(3, nGroups);
    write(15, nSelectors);
    for (int i = 0; i < nSelectors; ++i) {
      for (int j = selectorMtf[i]; j > 0; --j) {
        write(1, 1);
      }
      write(1, 0);
    }
    for (int t = 0; t < nGroups; ++t) {
      int curr = len[t][0];
      write(5, curr);
      for (int i = 0; i < alphaSize; ++i) {
        int j = len[t][i];
        while (curr < j) {
          write(2, 2);
          ++curr;
        }
        while (curr > j) {
          write(2, 3);
          --curr;
        }
        write(1, 0);
      }
    }
    int selCtr = 0;
    for (int gs = 0; gs < nMTF; ++selCtr) {
      int ge = gs+G_SIZE-1;
      if (ge >= nMTF) {
        ge = nMTF-1;
      }
      for (int i = gs; i <= ge; ++i) {
        int j = selector[selCtr];
        int k = smap[i];
        write(len[j][k], code[j][k]);
      }
      gs = ge+1;
    }
    if (selCtr != nSelectors) {
      throw new IllegalStateException("internal fatal error");
    }
  }

  private static void cost6(short[] cost, short[] smap, char[][] len, int gs, int ge) {
    // gs <= ge
    short icv = smap[gs];
    char cost0 = len[0][icv];
    char cost1 = len[1][icv];
    char cost2 = len[2][icv];
    char cost3 = len[3][icv];
    char cost4 = len[4][icv];
    char cost5 = len[5][icv];
    for (int i = gs+1; i <= ge; ++i) {
      icv = smap[i];
      cost0 += len[0][icv];
      cost1 += len[1][icv];
      cost2 += len[2][icv];
      cost3 += len[3][icv];
      cost4 += len[4][icv];
      cost5 += len[5][icv];
    }
    cost[0] = (short)cost0;
    cost[1] = (short)cost1;
    cost[2] = (short)cost2;
    cost[3] = (short)cost3;
    cost[4] = (short)cost4;
    cost[5] = (short)cost5;
  }

  private static char med3(char a, char b, char c) {
  	return ((a < b)? ((b < c)? b : (a<c)? c : a) : ((b > c)? b : (a > c)? c : a));
  }

  private static void swap(int[] map, int p1, int p2, int n) {
    if (n > 0) {
      do {
        int t = map[p1];
        map[p1++] = map[p2];
        map[p2++] = t;
      } while (--n > 0);
    }
  }

  private final static int[] incs = { 1, 4, 13, 40, 121, 364, 1093, 3280, 9841, 29524, 88573, 265720, 797161, 2391484 };
  private void simpleSort(int lo, int hi, int d) {
    if (hi <= lo) {
      return;
    }
    int hp = 0;
    for (int n = hi-lo+1; incs[hp] < n; ++hp);
    while (--hp >= 0) {
      int h = incs[hp];
      for (int i = lo+h; i <= hi; ++i) {
        int j = i;
        int v = map[i]+d;
        for (int k = lo+h; j >= k && mainGtU(map[j-h]+d, v); j -= h) {
          map[j] = map[j-h];
        }
        map[j] = v-d;
        if (++i > hi) {
          break;
        }
        j = i; v = map[i]+d;
        for (int k = lo+h; j >= k && mainGtU(map[j-h]+d, v); j -= h) {
          map[j] = map[j-h];
        }
        map[j] = v-d;
        if (++i > hi) {
          break;
        }
        j = i; v = map[i]+d;
        for (int k = lo+h; j >= k && mainGtU(map[j-h]+d, v); j -= h) {
          map[j] = map[j-h];
        }
        map[j] = v-d;
        if (firstBudget && budget < 0) {
          return;
        }
      }
    }
  }

  private boolean mainGtU(int i1, int i2) {
    char c1 = block[i1+1];
    char c2 = block[i2+1];
    if (c1 != c2) {
      return (c1 > c2);
    }
    c1 = block[i1+2]; c2 = block[i2+2]; // 2
    if (c1 != c2) {
      return (c1 > c2);
    }
    c1 = block[i1+3]; c2 = block[i2+3]; // 3
    if (c1 != c2) {
      return (c1 > c2);
    }
    c1 = block[i1+4]; c2 = block[i2+4]; // 4
    if (c1 != c2) {
      return (c1 > c2);
    }
    c1 = block[i1+5]; c2 = block[i2+5]; // 5
    if (c1 != c2) {
      return (c1 > c2);
    }
    c1 = block[i1+6]; c2 = block[i2+6]; // 6
    if (c1 != c2) {
      return (c1 > c2);
    }
    i1 += 6;
    i2 += 6;
    int k = last+1;
    do {
      c1 = block[i1+1]; c2 = block[i2+1];
      if (c1 != c2) {
        return (c1 > c2);
      }
      int s1 = quadrant[i1];
      int s2 = quadrant[i2];
      if (s1 != s2) {
        return (s1 > s2);
      }
      c1 = block[i1+2]; c2 = block[i2+2];
      if (c1 != c2) {
        return (c1 > c2);
      }
      s1 = quadrant[i1+1]; s2 = quadrant[i2+1];
      if (s1 != s2) {
        return (s1 > s2);
      }
      c1 = block[i1+3]; c2 = block[i2+3];
      if (c1 != c2) {
        return (c1 > c2);
      }
      s1 = quadrant[i1+2]; s2 = quadrant[i2+2];
      if (s1 != s2) {
        return (s1 > s2);
      }
      c1 = block[i1+4]; c2 = block[i2+4];
      if (c1 != c2) {
        return (c1 > c2);
      }
      s1 = quadrant[i1+3]; s2 = quadrant[i2+3];
      if (s1 != s2) {
        return (s1 > s2);
      }
      i1 += 4;
      if (i1 > last) {
        i1 -= last+1;
      }
      i2 += 4;
      if (i2 > last) {
        i2 -= last+1;
      }
      --budget;
      k -= 4;
    } while (k >= 0);
    return false;
  }

  private static void makeCodeLengths(char[] len, int[] freq, int alphaSize, int maxLen) {
    int[] heap = new int[alphaSize+2];
    int[] weight = new int[2*alphaSize];
    int[] parent = new int[2*alphaSize];
    for (int i = 0; i < alphaSize; ++i) {
      weight[i+1] = ((freq[i] == 0)? 1 : freq[i]) << 8;
    }
    while (true) {
      int nNodes = alphaSize;
      int nHeap = 0;
      heap[0] = 0;
      weight[0] = 0;
      parent[0] = -2;
      for (int i = 1; i <= alphaSize; ++i) {
        parent[i] = -1;
        heap[++nHeap] = i;
        int z = nHeap;
        int t = heap[z];
        for (int w = weight[t], zz = z >> 1; w < weight[heap[zz]]; zz >>= 1) {
          heap[z] = heap[zz];
          z = zz;
        }
        heap[z] = t;
      }
      while (nHeap > 1) {
        int n1 = heap[1];
        heap(--nHeap, heap, weight);
        int n2 = heap[1];
        heap(nHeap-1, heap, weight);
        parent[n1] = parent[n2] = ++nNodes;
        weight[nNodes] = ((weight[n1]&0xffffff00) + (weight[n2]&0xffffff00)) | (1 + Math.max(weight[n1]&255, weight[n2]&255));
        parent[nNodes] = -1;
        heap[nHeap] = nNodes;
        int z = nHeap;
        int t = heap[z];
        for (int w = weight[t], zz = z >> 1; w < weight[heap[zz]]; zz >>= 1) {
          heap[z] = heap[zz];
          z = zz;
        }
        heap[z] = t;
      }
      boolean tooLong = false;
      for (int i = 1; i <= alphaSize; ++i) {
        char j = 0;
        for (int k = i; parent[k] >= 0; k = parent[k]) {
          ++j;
        }
        len[i-1] = j;
        if (j > maxLen) {
          tooLong = true;
        }
      }
      if (!tooLong) {
        break;
      }
      for (int i = 1; i < alphaSize; ++i) {
        weight[i] = ((weight[i] >> 9) + 1) << 8;
      }
    }
  }

  private static void heap(int nHeap, int[] heap, int[] weight) {
    int z = 1;
    int t = heap[1] = heap[nHeap+1];
    for (int zz = z << 1, w = weight[t]; zz <= nHeap; zz = z << 1) {
      if (zz < nHeap && weight[heap[zz+1]] < weight[heap[zz]]) {
        ++zz;
      }
      if (w < weight[heap[zz]]) {
        break;
      }
      heap[z] = heap[zz];
      z = zz;
    }
    heap[z] = t;
  }

  private void quicksort(int lo, int hi, int d) {
    int[] stack = new int[300];
    int sp = 0;
    stack[0] = lo;
    stack[1] = hi;
    stack[2] = d;
    while ((sp += 3) > 0) {
      sp -= 3;
      lo = stack[sp];
      hi = stack[sp+1];
      d = stack[sp+2];
      if (hi-lo < 100 || d > 14) {  // d depends on NUM_OVERSHOOT_BYTES
        simpleSort(lo, hi, d);
        if (firstBudget && budget < 0) {
          return;
        }
        sp -= 3;
      } else {
        int med = med3(block[map[lo]+d+1], block[map[hi]+d+1], block[map[(lo+hi) >> 1]+d+1]);
        int unLo = lo;
        int ltLo = lo;
        int unHi = hi;
        int gtHi = hi;
        while (true) {
          for (int n = 0; unLo <= unHi; ++unLo) {
            n = ((int)block[map[unLo]+d+1])-med;
            if (n == 0) {
              int t = map[unLo];
              map[unLo] = map[ltLo];
              map[ltLo++] = t;
            } else if (n > 0) {
              break;
            }
          }
          for (int n = 0; unLo <= unHi; --unHi) {
            n = ((int)block[map[unHi]+d+1])-med;
            if (n == 0) {
              int t = map[unHi];
              map[unHi] = map[gtHi];
              map[gtHi--] = t;
            } else if (n < 0) {
              break;
            }
          }
          if (unLo > unHi) {
            break;
          }
          int t = map[unLo];
          map[unLo++] = map[unHi];
          map[unHi--] = t;
        }
        if (gtHi < ltLo) {
          stack[sp] = lo;
          stack[sp+1] = hi;
          stack[sp+2] = d+1;
        } else {
          int n = Math.min(ltLo-lo, unLo-ltLo);
          swap(map, lo, unLo-n, n);
          int m = Math.min(hi-gtHi, gtHi-unHi);
          swap(map, unLo, hi-m+1, m);
          n = lo+unLo-ltLo-1;
          m = hi-gtHi+unHi+1;
          stack[sp] = lo;
          stack[sp+1] = n;
          stack[sp+2] = d;
          stack[sp+3] = n+1;
          stack[sp+4] = m-1;
          stack[sp+5] = d+1;
          stack[sp+6] = m;
          stack[sp+7] = hi;
          stack[sp+8] = d;
          if (stack[sp+1]-stack[sp] < stack[sp+4]-stack[sp+3]) {
            int t = stack[sp];
            stack[sp] = stack[sp+3]; stack[sp+3] = t;
            t = stack[sp+1]; stack[sp+1] = stack[sp+4]; stack[sp+4] = t;
            t = stack[sp+2]; stack[sp+2] = stack[sp+5]; stack[sp+5] = t;
          }
          if (stack[sp+4]-stack[sp+3] < stack[sp+7]-stack[sp+6]) {
            int t = stack[sp+3];
            stack[sp+3] = stack[sp+6]; stack[sp+6] = t;
            t = stack[sp+4]; stack[sp+4] = stack[sp+7]; stack[sp+7] = t;
            t = stack[sp+5]; stack[sp+5] = stack[sp+8]; stack[sp+8] = t;
            if (stack[sp+1]-stack[sp] < stack[sp+4]-stack[sp+3]) {
              t = stack[sp]; stack[sp] = stack[sp+3]; stack[sp+3] = t;
              t = stack[sp+1]; stack[sp+1] = stack[sp+4]; stack[sp+4] = t;
              t = stack[sp+2]; stack[sp+2] = stack[sp+5]; stack[sp+5] = t;
            }
          }
          sp += 6;
        }
      }
    }
  }

  private void sort() {
    for (int i = 0; i < NUM_OVERSHOOT_BYTES; ++i) {
      block[last+i+2] = block[(i%(last+1))+1];
    }
    for (int i = last+NUM_OVERSHOOT_BYTES; i >= 0; --i) {
      quadrant[i] = 0;
    }
    block[0] = block[last+1];
    if (last < 5000) {
      for (int i = last; i >= 0; --i) {
        map[i] = i;
      }
      firstBudget = false;
      budget = 0;
      simpleSort(0, last, 0);
    } else {
      int[] runningOrder = new int[256];
      int[] copy = new int[256];
      int[] ftab = new int[65537];
      boolean[] bigDone = new boolean[256];
      int numSorted = 0;
      for (int i = 255; i >= 0; --i) {
        bigDone[i] = false;
        runningOrder[i] = i;
      }
      for (int i = ftab.length-1; i >= 0; --i) {
        ftab[i] = 0;
      }
      int c1 = block[0];
      for (int i = 0; i <= last; ++i) {
        int c2 = block[i+1];
        ++ftab[(c1 << 8) + c2];
        c1 = c2;
      }
      for (int i = 1; i < ftab.length; ++i) {
        ftab[i] += ftab[i-1];
      }
      c1 = block[1];
      for (int i = 0; i < last; ++i) {
        int c2 = block[i+2];
        int j = --ftab[(c1 << 8) + c2];
        map[j] = i;
        c1 = c2;
      }
      map[--ftab[((block[last + 1]) << 8) + (block[1])]] = last;
      int h = 364;
      do {
        h /= 3;
        for (int i = h; i <= 255; ++i) {
          int vv = runningOrder[i];
          int j = i;
          while (j >= h && ftab[((runningOrder[j-h])+1) << 8] - ftab[(runningOrder[j-h]) << 8] > ftab[(vv+1) << 8] - ftab[vv << 8]) {
            runningOrder[j] = runningOrder[j-h];
            j -= h;
          }
          runningOrder[j] = vv;
        }
      } while (h != 1);
      for (int i = 0; i <= 255; ++i) {
        final int s = runningOrder[i];
        for (int j = 0; j <= 255; ++j) {
          int t = (s << 8) + j;
          if ((ftab[t]&SETMASK) != SETMASK) {
            int lo = ftab[t] & CLEARMASK;
            int hi = (ftab[t+1] & CLEARMASK)-1;
            if (hi > lo) {
              quicksort(lo, hi, 2);
              numSorted += (hi-lo+1);
              if (firstBudget && budget < 0) {
                return;
              }
            }
            ftab[t] |= SETMASK;
          }
        }
        bigDone[s] = true;
        if (i < 255) {
          int bbStart = ftab[s << 8] & CLEARMASK;
          int bbSize = (ftab[(s+1) << 8] & CLEARMASK) - bbStart;
          int shifts = 0;
          while ((bbSize >> shifts) > 65534) {
            ++shifts;
          }
          for (int j = 0; j < bbSize; ++j) {
            int k = map[bbStart+j];
            int v = (j >> shifts);
            quadrant[k] = v;
            if (k < NUM_OVERSHOOT_BYTES) {
              quadrant[k+last+1] = v;
            }
          }
          if (((bbSize - 1) >> shifts) > 65535) {
            throw new IllegalStateException("internal size error");
          }
        }
        for (int j = 255; j >= 0; --j) {
          if (!bigDone[j]) {
            copy[j] = ftab[(j << 8) + s] & CLEARMASK;
          }
        }
        for (int j = ftab[s << 8] & CLEARMASK, k = (ftab[(s+1) << 8] & CLEARMASK); j < k; ++j) {
          c1 = block[map[j]];
          if (!bigDone[c1]) {
            map[copy[c1]] = (map[j] == 0)? last : map[j]-1;
            ++copy[c1];
          }
        }
        for (int j = s, k = 65536+s; j < k; j += 256) {
          ftab[j] |= SETMASK;
        }
      }
    }
  }

  private OutputStream out;
  private boolean closed = false;
  private int blockSize100k;
  private int allowableBlockSize;
  private Bzip2CRC crc = new Bzip2CRC();
  private int combinedCRC = 0;
  private int bsBuff;
  private int bsLive;
  private int currentChar = -1;
  private int runLength = 0;
  private int last;
  private char[] block;
  private int[] quadrant;
  private int[] map;
  private short[] smap;
  private int budget;
  private boolean firstBudget;
  private boolean blockRandomised;
  private int nBlocksRandomised;
  private boolean[] inUse = new boolean[256];
  private int nSelectors = 0;
  private byte[] selector = null;
  private int nMTF;
  private int[] mtfFreq = null;

  private final static int SETMASK = (1 << 21);
  private final static int CLEARMASK = (~SETMASK);
  private final static int NUM_OVERSHOOT_BYTES = 34;
}
