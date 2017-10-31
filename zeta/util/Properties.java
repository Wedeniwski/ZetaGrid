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
     R. Auberger
     H. Haddorp
     S. Wedeniwski
--*/

package zeta.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *  The <code>Properties</code> class represents a persistent set of properties.
 *
 *  @version 1.9.3, May 29, 2004
**/
public class Properties {
  public final static String ZETA_CFG = "zeta.cfg";
  public final static String DEFAULT_CFG = "default.cfg";
  public final static String ZETA_TOOLS_CFG = "zeta_tools.cfg";

  /**
   *  @deprecated
  **/
  protected Properties() {
  }

  /**
   *  Creates a property list with the keys of the specified property file.
  **/
  public Properties(String propertiesFilename) throws IOException {
    this(propertiesFilename, null);
  }

  /**
   *  Creates a property list with the keys of the specified property file
   *  and the keys of the specified default property file if they are not defined in the first file.
  **/
  public Properties(String propertiesFilename, String defaultPropertiesFilename) throws IOException {
    this.propertiesFilename = propertiesFilename;
    this.defaultPropertiesFilename = defaultPropertiesFilename;
    load();
  }

  /**
   *  Reloads the property file.
  **/
  public void reload() throws IOException {
    properties = propertiesDefault = null;
    load();
  }

  /**
   *  Loads the property file.
  **/
  public void load() throws IOException {
    if (defaultPropertiesFilename != null) {
      File propertiesDefaultFile = new File(defaultPropertiesFilename);
      if (propertiesDefault == null && propertiesDefaultFile.canRead()) {
        FileInputStream propertyDefaultFileInputStream = null;
        try {
          propertiesDefault = new java.util.Properties();
          propertyDefaultFileInputStream = new FileInputStream(propertiesDefaultFile);
          propertiesDefault.load(propertyDefaultFileInputStream);
        } finally {
          StreamUtils.close(propertyDefaultFileInputStream);
        }
      }
    }
    FileInputStream propertyFileInputStream = null;
    try {
      File propertyFile = new File(propertiesFilename);
      if (properties == null && propertyFile.canRead()) {
        properties = new java.util.Properties(propertiesDefault);
        propertyFileInputStream = new FileInputStream(propertyFile);
        properties.load(propertyFileInputStream);
      }
    } finally {
      StreamUtils.close(propertyFileInputStream);
    }
  }

  /**
   * Searches for the property with the specified key in this property list.
   * If the key is not found in this property list, the default property list,
   * and its defaults, recursively, are then checked. The method returns
   * <code>null</code> if the property is not found.
   *
   * @param   key   the property key.
  **/
  public String get(String key) {
    String s = (properties != null)? (String)properties.get(key) : null;
    return (s != null || propertiesDefault == null)? s : (String)propertiesDefault.get(key);
  }

  /**
   *  Searches for the property with the specified key in this property list.
   *  If the key is not found in this property list, the default property list, and its defaults,
   *  recursively, are then checked. The method returns the default value argument if the property is not found.
   *
   *  @param   key   the property key.
   *  @param   defaultValue   a default value.
   *
   *  @return the value in this property list with the specified key value.
  **/
  public String get(String key, String defaultValue) {
    String s = get(key);
    return (s != null)? s : defaultValue;
  }

  /**
   *  Searches for the property with the specified key in this property list.
   *  If the key is not found in this property list, the default property list, and its defaults,
   *  recursively, are then checked. The method returns the default value argument if the property is not found
   *  or not an integer.
   *
   *  @param   key   the property key.
   *  @param   defaultValue   a default value.
   *
   *  @return the value in this property list with the specified key value.
  **/
  public int get(String key, int defaultValue) {
    try {
      String s = get(key);
      if (s != null) {
        return Integer.parseInt(s);
      }
    } catch (NumberFormatException e) {
    }
    return defaultValue;
  }

  protected String propertiesFilename = null;
  protected String defaultPropertiesFilename = null;

  /**
   *  A property list that contains default values for any keys not
   *  found in the main property list.
  **/
  private java.util.Properties propertiesDefault = null;

  /**
   *  The main property list.
  **/
  private java.util.Properties properties = null;
}
