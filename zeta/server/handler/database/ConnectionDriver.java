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

package zeta.server.handler.database;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

/**
 *  @version 1.9.4, August 27, 2004
**/
public class ConnectionDriver implements Driver {
  public ConnectionDriver(String driver, String url, String user, String password, int totalConnections, long connectionTimeout, long idleTimeout, long agedTimeout) 
   throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
    this.driver = driver;
    DriverManager.registerDriver(this);
    Class.forName(driver).newInstance();
    pool = new ConnectionPool(url, user, password, totalConnections, connectionTimeout, idleTimeout, agedTimeout);
  }

  public Connection connect(String url, Properties props) throws SQLException {
    return (url.startsWith(URL_PREFIX))? pool.getConnection() : null;
  }

  public boolean acceptsURL(String url) {
    return url.startsWith(URL_PREFIX);
  }

  public int getMajorVersion() {
    return 1;
  }

  public int getMinorVersion() {
    return 0;
  }

  public DriverPropertyInfo[] getPropertyInfo(String str, Properties props) {
    return new DriverPropertyInfo[0];
  }

  public boolean jdbcCompliant() {
    return false;
  }

  public String getURL() {
    return pool.getURL();
  }

  public void setURL(String url) {
    try {
      Class.forName(driver).newInstance();
    } catch (ClassNotFoundException cnf) {
    } catch (InstantiationException ie) {
    } catch (IllegalAccessException iae) {
    }
    pool.setURL(url);
  }

  public int getPoolsize() {
    return pool.getPoolsize();
  }

  public static final String URL_PREFIX = "jdbc:pool:";
  private ConnectionPool pool;
  private String driver;
}
