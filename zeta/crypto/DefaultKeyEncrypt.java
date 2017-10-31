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

import java.math.BigInteger;

/**
 *  @version 1.9.0, February 8, 2004
**/
public class DefaultKeyEncrypt implements Key {
  public DefaultKeyEncrypt() {
    //public keys:
    p = new BigInteger("12LBK5TMOB99C131JB3Q9UMVS18SFU7OOKL25K3OH51PL1H6GH3HDV6OIKL2LQI86KCB4LGMASDFLAPLJG5NJUV05ELC2PKLG2KG2SCB2", 32);
    p = p.shiftLeft(500);
    p = p.or(new BigInteger("SGJLA1K936HBH0JVKKL4BQBL76G57KO1KH90345BILT4850I9ML5RG9Q8053KJAGR7N8CJFLQI9LV0O15UV9INHUFMIII3LL5J53", 32));

    g = new BigInteger("BLKT883FAB6V1BCF64G4PDUNMH9B9D317ECVHEAOK3OHPOS2E4D7CI8PK26EP36MH1GJS1NG8B74D10G6E3KKJNI7QMFGR4Q8J7FQ8B8", 32);
    g = g.shiftLeft(500);
    g = g.or(new BigInteger("C2NHRVQULGBJ8H4F2UU1GS0ATBJ5FDEIB7HGIN5U1K1ES0EB7NP8NQAPR41B5PQ1TBOUS4PJ5AAC2GRSJSEMKBEN49IGHOA8ORFA", 32));

    A = new BigInteger("U53EO946TT0EQ0QHGS6GCBBUVVDRF9U50QVB3795MIP3OH18S91AAFQV54N5RFBL3I7UIPAUM4R67PUQEV3JQH9PNPEJ84IU6L3FVM7L", 32);
    A = A.shiftLeft(500);
    A = A.or(new BigInteger("9VU1F4R6TDV9HQH0RAKL7VQEI3G87M4F86CRQKQ9P5C892F2QPU1M6UCSEIDL2VP8UDK9808UHHGVVPVFBVD6AE7NC8DJHSUKIEG", 32));
  }

  public BigInteger getBase() {
    return A;
  }

  public BigInteger getModulo() {
    return p;
  }

  public BigInteger getGenerator() {
    return g;
  }

  private BigInteger p,g,A;
}
