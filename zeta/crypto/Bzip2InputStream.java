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

import java.io.InputStream;
import java.io.IOException;

/**
 * An input stream that decompresses from the BZip2 format (without the file
 * header chars) to be read as any other stream.
 *
 *  @version 1.9.3, May 29, 2004
**/
public class Bzip2InputStream extends InputStream implements Bzip2Constants {
  public Bzip2InputStream(InputStream in) throws IOException {
    this.in = in;
    bsLive = 0;
    bsBuff = 0;
    char magic3 = (char)read(8);
    char magic4 = (char)read(8);
    if (magic3 != 'h' || magic4 < '1' || magic4 > '9') {
      close();
      return;
    }
    blockSize100k = Character.digit(magic4, 10);
    if (blockSize100k <= 0 || blockSize100k > 9) {
      throw new IOException("Invalid block size");
    }
    int n = BASE_BLOCK_SIZE * blockSize100k;
    ll8 = new short[n];
    tt = new int[n];
    combinedCRC = 0;
    initBlock();
  }

  public int read() {
    if (in == null) {
      return -1;
    } else {
      int retChar = currentChar;
      switch (currentState) {
        case START_BLOCK_STATE:
          break;
        case RAND_PART_A_STATE:
          break;
        case RAND_PART_B_STATE:
          setupRandPartB();
          break;
        case RAND_PART_C_STATE:
          setupRandPartC();
          break;
        case NO_RAND_PART_A_STATE:
          break;
        case NO_RAND_PART_B_STATE:
          setupNoRandPartB();
          break;
        case NO_RAND_PART_C_STATE:
          setupNoRandPartC();
          break;
        default:
          break;
      }
      return retChar;
    }
  }

  public void close() {
    if (in != null && in != System.in) {
      try {
        in.close();
      } catch (IOException ioe) {
        //ignore
      } finally {
        in = null;
      }
    }
  }

  private int read(int n) {
    int i = bsLive;
    int v = bsBuff;
    if (n == 1) {
      if (i <= 0) {
        try {
          int ch = in.read();
          if (ch == -1) {
            throw new IllegalStateException("compressed stream EOF");
          }
          bsBuff = v = (v << 8) | (ch & 0xff);
        } catch (IOException e) {
          throw new IllegalStateException("compressed stream EOF");
        }
        i += 8;
      }
      bsLive = i-1;
      return (v >> (i-1)) & 1;
    } else {
      while (i < n) {
        try {
          int ch = in.read();
          if (ch == -1) {
            throw new IllegalStateException("compressed stream EOF");
          }
          v = (v << 8) | (ch & 0xff);
        } catch (IOException e) {
          throw new IllegalStateException("compressed stream EOF");
        }
        i += 8;
      }
      bsBuff = v;
      bsLive = i-n;
      return (v >> (i-n)) & ((1 << n) - 1);
    }
  }

  private int readInt() {
    return (((((read(8) << 8) | read(8)) << 8) | read(8)) << 8) | read(8);
  }

  private void initBlock() {
    char magic1 = (char)read(8);
    char magic2 = (char)read(8);
    char magic3 = (char)read(8);
    char magic4 = (char)read(8);
    char magic5 = (char)read(8);
    char magic6 = (char)read(8);
    if (magic1 == 0x17 && magic2 == 0x72 && magic3 == 0x45 && magic4 == 0x38 && magic5 == 0x50 && magic6 == 0x90) {
      if (readInt() != combinedCRC) {
        close();
        throw new IllegalStateException("CRC error");
      }
      close();
      return;
    }
    if (magic1 != 0x31 || magic2 != 0x41 || magic3 != 0x59 || magic4 != 0x26 || magic5 != 0x53 || magic6 != 0x59) {
      close();
      throw new IllegalStateException("bad block header");
    }
    storedBlockCRC = readInt();
    boolean blockRandomised = (read(1) == 1);
    final short RUNA = 0;
    final short RUNB = 1;
    int limitLast = BASE_BLOCK_SIZE * blockSize100k;
    int origPtr = read(24);
    boolean[] inUse16 = new boolean[16];
    for (int i = 0; i < 16; ++i) {
      inUse16[i] = (read(1) == 1);
    }
    short alphaSize = 2;
    short[] seqToUnseq = new short[256];
    for (short i = 0, k = 0; i < 16; ++i, k += 16) {
      if (inUse16[i]) {
        for (short j = 0; j < 16; ++j) {
          if (read(1) == 1) {
            seqToUnseq[alphaSize-2] = (short)(k+j);
            ++alphaSize;
          }
        }
      }
    }
    int nGroups = read(3);
    if (nGroups > 127) {  // normally <= 6
      throw new IllegalStateException("Groups value is too high");
    }
    byte[] selectorMtf = new byte[read(15)];
    for (int i = 0; i < selectorMtf.length; ++i) {
      byte j = 0;
      while (read(1) == 1) {
        ++j;
      }
      selectorMtf[i] = j;
    }
    byte[] pos = new byte[nGroups];
    for (byte v = 0; v < nGroups; ++v) {
      pos[v] = v;
    }
    byte[] selector = new byte[selectorMtf.length];
    for (int i = 0; i < selector.length; ++i) {
      byte v = selectorMtf[i];
      byte tmp = pos[v];
      while (v > 0) {
        pos[v] = pos[v-1];
        --v;
      }
      pos[0] = tmp;
      selector[i] = tmp;
    }
    short len[][] = new short[nGroups][alphaSize];
    short[] minLens = new short[nGroups];
    short[] maxLens = new short[nGroups];
    short maxL = alphaSize;  // <= 258
    for (int t = 0; t < nGroups; ++t) {
      short minLen = 32;
      short maxLen = 0;
      short curr = (short)read(5);
      for (short i = 0; i < alphaSize; ++i) {
        while (read(1) == 1) {
          if (read(1) == 0) {
            ++curr;
          } else {
            --curr;
          }
        }
        len[t][i] = curr;
        if (curr > maxLen) {
          maxLen = curr;
        }
        if (curr < minLen) {
          minLen = curr;
        }
      }
      minLens[t] = minLen;
      maxLens[t] = maxLen;
      if (maxLen > maxL) {
        maxL = maxLen;
      }
    }
    int[][] limit = new int[nGroups][alphaSize];
    int[][] base = new int[nGroups][maxL+2];
    short[][] perm = new short[nGroups][alphaSize];
    for (int t = 0; t < nGroups; ++t) {
      short minLen = minLens[t];
      short maxLen = maxLens[t];
      int k = 0;
      for (short i = minLen; i <= maxLen; ++i) {
        for (short j = 0; j < alphaSize; ++j) {
          if (len[t][j] == i) {
            perm[t][k++] = j;
          }
        }
      }
      for (short i = 0; i < maxL; ++i) {
        base[t][i] = 0;
        limit[t][i] = 0;
      }
      for (short i = 0; i < alphaSize; ++i) {
        ++base[t][len[t][i]+1];
      }
      for (short i = 1; i < maxL; ++i) {
        base[t][i] += base[t][i-1];
      }
      k = 0;
      for (short i = minLen; i <= maxLen; ++i) {
        k += base[t][i+1] - base[t][i];
        limit[t][i] = k-1;
        k <<= 1;
      }
      for (short i = (short)(minLen+1); i <= maxLen; ++i) {
        base[t][i] = ((limit[t][i-1]+1) << 1) - base[t][i];
      }
    }
    int[] unzftab = new int[256];
    short[] yy = new short[256];
    for (short i = 0; i <= 255; ++i) {
      unzftab[i] = 0;
      yy[i] = i;
    }
    last = -1;
    int groupNo = 0;
    int groupPos = G_SIZE-1;
    int zt = selector[0];
    int zn = minLens[zt];
    int zvec = read(zn);
    while (zvec > limit[zt][zn]) {
      ++zn;
      zvec = (zvec << 1) | read(1);
    }
    short nextSym = perm[zt][zvec - base[zt][zn]];
    while (true) {
      if (nextSym == alphaSize-1) {
        break;
      }
      if (nextSym == RUNA || nextSym == RUNB) {
        int s = -1;
        int N = 1;
        do {
          if (nextSym == RUNA) {
            s += N;
          } else if (nextSym == RUNB) {
            s += N << 1;
          }
          N <<= 1;
          if (--groupPos < 0) {
            ++groupNo;
            groupPos = G_SIZE-1;
          }
          zt = selector[groupNo];
          zn = minLens[zt];
          zvec = read(zn);
          while (zvec > limit[zt][zn]) {
            ++zn;
            zvec = (zvec << 1) | read(1);
          }
          nextSym = perm[zt][zvec - base[zt][zn]];
        } while (nextSym == RUNA || nextSym == RUNB);
        short ch = seqToUnseq[yy[0]];
        unzftab[ch] += ++s;
        if (s > 3) {
          do {
            ll8[last+1] = ll8[last+2] = ll8[last+3] = ll8[last+4] = ch;
            last += 4;
            s -= 4;
          } while (s > 3);
        }
        if (s > 0) {
          do {
            ll8[++last] = ch;
          } while (--s > 0);
        }
        if (last >= limitLast) {
          throw new IllegalStateException("block overrun");
        }
      } else {
        if (++last >= limitLast) {
          throw new IllegalStateException("block overrun");
        }
        int j = nextSym-1;
        short tmp = yy[j];
        ++unzftab[seqToUnseq[tmp]];
        ll8[last] = seqToUnseq[tmp];
        for (; j > 3; j -= 4) {
          yy[j]   = yy[j-1];
          yy[j-1] = yy[j-2];
          yy[j-2] = yy[j-3];
          yy[j-3] = yy[j-4];
        }
        for (; j > 0; --j) {
          yy[j] = yy[j-1];
        }
        yy[0] = tmp;
        if (--groupPos < 0) {
          ++groupNo;
          groupPos = G_SIZE-1;
        }
        zt = selector[groupNo];
        zn = minLens[zt];
        zvec = read(zn);
        while (zvec > limit[zt][zn]) {
          ++zn;
          zvec = (zvec << 1) | read(1);
        }
        nextSym = perm[zt][zvec - base[zt][zn]];
      }
    }
    crc.init();
    int[] cftab = new int[257];
    cftab[0] = 0;
    for (int i = 1, k = 0; i <= 256; ++i) {
      cftab[i] = k += unzftab[i-1];
    }
    for (int i = 0; i <= last; ++i) {
      short ch = ll8[i];
      tt[cftab[ch]] = i;
      ++cftab[ch];
    }
    cftab = null;
    tPos = tt[origPtr];
    count = 0;
    countPartA = 0;
    chPartA = 256;
    currentState = START_BLOCK_STATE;
    if (blockRandomised) {
      rNToGo = 0;
      rTPos = 0;
      setupRandPartA();
    } else {
      setupNoRandPartA();
    }
  }

  private void endBlock() {
    int c = crc.get();
    if (storedBlockCRC != c) {
      throw new IllegalStateException("CRC error");
    }
    combinedCRC = ((combinedCRC << 1) | (combinedCRC >>> 31)) ^ c;
  }

  private void setupRandPartA() {
    if (countPartA <= last) {
      chPrev = chPartA;
      chPartA = ll8[tPos];
      tPos = tt[tPos];
      if (rNToGo == 0) {
        rNToGo = randomNumbers[rTPos];
        if (++rTPos == 512) {
          rTPos = 0;
        }
      }
      chPartA ^= (int)((--rNToGo == 1)? 1 : 0);
      ++countPartA;
      currentChar = chPartA;
      currentState = RAND_PART_B_STATE;
      crc.update(chPartA, 1);
    } else {
      endBlock();
      initBlock();
    }
  }

  private void setupNoRandPartA() {
    if (countPartA <= last) {
      chPrev = chPartA;
      chPartA = ll8[tPos];
      tPos = tt[tPos];
      ++countPartA;
      currentChar = chPartA;
      currentState = NO_RAND_PART_B_STATE;
      crc.update(chPartA, 1);
    } else {
      endBlock();
      initBlock();
    }
  }

  private void setupRandPartB() {
    if (chPartA != chPrev) {
      count = 1;
      currentState = RAND_PART_A_STATE;
      setupRandPartA();
    } else {
      if (++count >= 4) {
        lastPartC = ll8[tPos];
        tPos = tt[tPos];
        if (rNToGo == 0) {
          rNToGo = randomNumbers[rTPos];
          if (++rTPos == 512) {
            rTPos = 0;
          }
        }
        lastPartC ^= ((--rNToGo == 1)? 1 : 0);
        countPartC = 0;
        currentState = RAND_PART_C_STATE;
        setupRandPartC();
      } else {
        currentState = RAND_PART_A_STATE;
        setupRandPartA();
      }
    }
  }

  private void setupNoRandPartB() {
    if (chPartA != chPrev) {
      count = 1;
      currentState = NO_RAND_PART_A_STATE;
      setupNoRandPartA();
    } else {
      if (++count >= 4) {
        lastPartC = ll8[tPos];
        tPos = tt[tPos];
        countPartC = 0;
        currentState = NO_RAND_PART_C_STATE;
        setupNoRandPartC();
      } else {
        currentState = NO_RAND_PART_A_STATE;
        setupNoRandPartA();
      }
    }
  }

  private void setupRandPartC() {
    if (countPartC < lastPartC) {
      currentChar = chPartA;
      crc.update(chPartA, 1);
      ++countPartC;
    } else {
      ++countPartA;
      count = 0;
      currentState = RAND_PART_A_STATE;
      setupRandPartA();
    }
  }

  private void setupNoRandPartC() {
    if (countPartC < lastPartC) {
      currentChar = chPartA;
      crc.update(chPartA, 1);
      ++countPartC;
    } else {
      ++countPartA;
      count = 0;
      currentState = NO_RAND_PART_A_STATE;
      setupNoRandPartA();
    }
  }

  private InputStream in = null;
  private int blockSize100k;
  private Bzip2CRC crc = new Bzip2CRC();
  private int bsBuff;
  private int bsLive;
  private int currentChar = -1;
  private int last;

  private static final int START_BLOCK_STATE = 1;
  private static final int RAND_PART_A_STATE = 2;
  private static final int RAND_PART_B_STATE = 3;
  private static final int RAND_PART_C_STATE = 4;
  private static final int NO_RAND_PART_A_STATE = 5;
  private static final int NO_RAND_PART_B_STATE = 6;
  private static final int NO_RAND_PART_C_STATE = 7;
  private int currentState = START_BLOCK_STATE;
  private int storedBlockCRC;
  private int combinedCRC;

  private int[] tt;
  private short[] ll8;
  private int countPartA, countPartC, lastPartC, count, chPrev, chPartA, tPos;
  private int rNToGo = 0;
  private int rTPos = 0;
}
