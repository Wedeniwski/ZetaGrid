/*--
  This file is a part of ZetaGrid, a program and
  library for seperation of zeros of the Riemann zeta function.

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

#include <jni.h>
#include "zeta_zeros.h"
#include "statistic.h"
#include "check.h"
#include "gram.h"

extern "C" {

void exitCalculation2(int terminate);


JNIEXPORT jint JNICALL Java_zeta_example_ZetaTask_zetaZeros(JNIEnv* pJNIEnv, jclass cl, jlong LASTN, jint NRANGE, jint sleepN)
{
  return ZetaZeros(startType(LASTN), int(NRANGE), int(sleepN)).getResult();
}

JNIEXPORT void JNICALL Java_zeta_example_ZetaTask_setCoutLog(JNIEnv* pJNIEnv, jclass cl, jboolean coutLog)
{
  setCoutLog(coutLog);
}

JNIEXPORT jstring JNICALL Java_zeta_example_ZetaTask_getZetaVersion(JNIEnv* pJNIEnv, jclass cl)
{
  return pJNIEnv->NewStringUTF(VERSION);
}

JNIEXPORT void JNICALL Java_zeta_example_ZetaTask_setZetaResources(JNIEnv* pJNIEnv, jclass cl, jint resourceId)
{
  EvalZeta::setFastSumZMethod(resourceId);
}

JNIEXPORT void JNICALL Java_zeta_example_ZetaTask_zetaExit(JNIEnv*, jclass)
{
  exitCalculation2(0);
}


JNIEXPORT jstring JNICALL Java_zeta_ZetaCalc_getVersion(JNIEnv* pJNIEnv, jclass cl)
{
  return Java_zeta_example_ZetaTask_getZetaVersion(pJNIEnv, cl);
}

JNIEXPORT void JNICALL Java_zeta_ZetaCalc_setResources(JNIEnv* pJNIEnv, jclass cl, jint resourceId)
{
  Java_zeta_example_ZetaTask_setZetaResources(pJNIEnv, cl, resourceId);
}

JNIEXPORT jint JNICALL Java_zeta_ZetaCalc_zetaZeros(JNIEnv* pJNIEnv, jclass cl, jlong LASTN, jint NRANGE, jint sleepN)
{
  return Java_zeta_example_ZetaTask_zetaZeros(pJNIEnv, cl, LASTN, NRANGE, sleepN);
}

JNIEXPORT void JNICALL Java_zeta_ZetaCalc_setCoutLog(JNIEnv* pJNIEnv, jclass cl, jboolean coutLog)
{
  Java_zeta_example_ZetaTask_setCoutLog(pJNIEnv, cl, coutLog);
}

JNIEXPORT jstring JNICALL Java_zeta_ZetaTask_getZetaVersion(JNIEnv* pJNIEnv, jclass cl)
{
  return Java_zeta_example_ZetaTask_getZetaVersion(pJNIEnv, cl);
}

JNIEXPORT void JNICALL Java_zeta_ZetaTask_setZetaResources(JNIEnv* pJNIEnv, jclass cl, jint resourceId)
{
  Java_zeta_example_ZetaTask_setZetaResources(pJNIEnv, cl, resourceId);
}

JNIEXPORT void JNICALL Java_zeta_ZetaTask_zetaExit(JNIEnv* pJNIEnv, jclass cl)
{
  Java_zeta_example_ZetaTask_zetaExit(pJNIEnv, cl);
}

JNIEXPORT jint JNICALL Java_zeta_ZetaTask_zetaZeros(JNIEnv* pJNIEnv, jclass cl, jlong LASTN, jint NRANGE, jint sleepN)
{
  return Java_zeta_example_ZetaTask_zetaZeros(pJNIEnv, cl, LASTN, NRANGE, sleepN);
}

JNIEXPORT void JNICALL Java_zeta_ZetaTask_setCoutLog(JNIEnv* pJNIEnv, jclass cl, jboolean coutLog)
{
  Java_zeta_example_ZetaTask_setCoutLog(pJNIEnv, cl, coutLog);
}

JNIEXPORT jstring JNICALL Java_zeta_tool_ZetaStatistic_getVersion(JNIEnv* pJNIEnv, jclass cl)
{
  return Java_zeta_example_ZetaTask_getZetaVersion(pJNIEnv, cl);
}

JNIEXPORT jlong JNICALL Java_zeta_tool_ZetaStatistic_getStartN(JNIEnv* pJNIEnv, jclass cl, jdouble t)
{
  return getStartN(t);
}

JNIEXPORT jstring JNICALL Java_zeta_tool_ZetaStatistic_statistic(JNIEnv* pJNIEnv, jclass, jstring tmp, jstring directory, jboolean removeLine)
{
  if (tmp) {
    const char* c1 = pJNIEnv->GetStringUTFChars(tmp, 0);
    const char* c2 = pJNIEnv->GetStringUTFChars(directory, 0);
    jstring newTmp = pJNIEnv->NewStringUTF(statistic(c1, c2, removeLine).c_str());
    pJNIEnv->ReleaseStringUTFChars(directory, c2);
    pJNIEnv->ReleaseStringUTFChars(tmp, c1);
    return newTmp;
  }
  if (directory) {
    const char* c2 = pJNIEnv->GetStringUTFChars(directory, 0);
    jstring newTmp = pJNIEnv->NewStringUTF(statistic(0, c2, removeLine).c_str());
    pJNIEnv->ReleaseStringUTFChars(directory, c2);
    return newTmp;
  }
  return pJNIEnv->NewStringUTF(statistic(0, "", removeLine).c_str());
}

JNIEXPORT jint JNICALL Java_zeta_tool_ZetaStatistic_zetaZeros(JNIEnv* pJNIEnv, jclass cl, jlong LASTN, jint NRANGE, jint sleepN)
{
  return Java_zeta_ZetaCalc_zetaZeros(pJNIEnv, cl, LASTN, NRANGE, sleepN);
}

JNIEXPORT void JNICALL Java_zeta_tool_ZetaStatistic_verify(JNIEnv* pJNIEnv, jclass, jstring smallFile, jstring largeFile)
{
  const char* c1 = pJNIEnv->GetStringUTFChars(smallFile, 0);
  const char* c2 = pJNIEnv->GetStringUTFChars(largeFile, 0);
  verify(c1, c2);
  pJNIEnv->ReleaseStringUTFChars(smallFile, c1);
  pJNIEnv->ReleaseStringUTFChars(largeFile, c2);
}

/*
 * Class:     ZetaStatistic
 * Method:    reduceFilesize
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_zeta_tool_ZetaStatistic_reduceFilesize(JNIEnv* pJNIEnv, jclass, jstring filename)
{
  const char* c = pJNIEnv->GetStringUTFChars(filename, 0);
  jboolean result = (reduceFilesize(0, c) == 0);
  pJNIEnv->ReleaseStringUTFChars(filename, c);
  return result;
}

}
