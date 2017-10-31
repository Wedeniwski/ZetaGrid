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

import zeta.ZetaInfo;

/**
 *  @version 1.9.1, February 17, 2004
**/
public class KeyManager {
  public static Key getKey(String className) {
    if (className != null) {
      try {
        return (Key)Class.forName(className).newInstance();
      } catch (ClassNotFoundException e) {
        ZetaInfo.handle(e);
        return null;
      } catch (IllegalAccessException e) {
        ZetaInfo.handle(e);
        return null;
      } catch (InstantiationException e) {
        ZetaInfo.handle(e);
        return null;
      }
    }
    return new DefaultKey();
  }

  /**
   *  Returns a key which can be used to encrypt data
   *  via the key establishment protocol (half-certified Diffie-Hellman).
   *  @param keyClassData the bytes that make up the class data which contains the key for the encryption algorithm
   *  @param <code>null</code> if the encryption key is invalid
  **/
  public static Key getEncryptorKey(final byte[] keyClassData) {
    if (keyClassData != null) {
      try {
        final String className = getClassName(keyClassData);
        if (className != null) {
          Class clazz = new ClassLoader() {
            public Class findClass(String name) throws ClassNotFoundException {
              if (name != null && name.equals(className)) {
                return defineClass(className, keyClassData, 0, keyClassData.length);
              }
              throw new ClassNotFoundException(name);
            }
  
            public Class loadClass(String name) throws ClassNotFoundException {
              return loadClass(name, false);
            }
  
            protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
              if (name != null && name.equals(className)) {
                Class c = findClass(name);
                if (resolve) {
                  resolveClass(c);
                }
                return c;
              }
              return super.loadClass(name, resolve);
            }
          }.loadClass(className);
          return (Key)clazz.newInstance();
        }
      } catch (ClassNotFoundException e) {
        ZetaInfo.handle(e);
        return null;
      } catch (IllegalAccessException e) {
        ZetaInfo.handle(e);
        return null;
      } catch (InstantiationException e) {
        ZetaInfo.handle(e);
        return null;
      }
    }
    return new DefaultKeyEncrypt();
  }

  private static String getClassName(byte[] keyClassData) {
    // ToDo: get class name
    // This code may depend on the Java VM version.
    // This code was tested on the vesions 1.3.1 and 1.4.1
    try {
      // indexOf "java/lang/Object"
      byte[] target = "java/lang/Object".getBytes("UTF-8");
      int i = 0;
      int l = keyClassData.length-target.length;
      for (int j = 0; i <= l && j < target.length; ++i) {
        for (j = 0; j < target.length && keyClassData[i+j] == target[j]; ++j);
      }
      if (i <= l) {
        i -= 4;
        while (--i >= 0) {
          char c = (char)keyClassData[i];
          if (Character.isJavaIdentifierPart(c)) {
            StringBuffer buffer = new StringBuffer(100);
            buffer.append(c);
            while (--i >= 0) {
              c = (char)keyClassData[i];
              if (c == '/') {
                buffer.insert(0, '.');
              } else if (Character.isJavaIdentifierPart(c)) {
                buffer.insert(0, c);
              } else {
                break;
              }
            }
            return buffer.toString();
          }
        }
      }
    } catch (Exception e) {
      ZetaInfo.handle(e);
    }
    return null;
  }
}
