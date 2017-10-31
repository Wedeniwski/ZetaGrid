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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 *  @version 1.9.4, August 27, 2004
**/
public class ConnectionDataSource implements DataSource {
  public ConnectionDataSource(String driver, String url, String user, String password, int totalConnections, long connectionTimeout, long idleTimeout, long agedTimeout)
   throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
    this.url = url;
    this.connectionTimeout = connectionTimeout;
    this.idleTimeout = idleTimeout;
    this.agedTimeout = agedTimeout;
    this.driver = new ConnectionDriver(driver, url, user, password, totalConnections, connectionTimeout, idleTimeout, agedTimeout);
  }

  public Connection getConnection() throws SQLException {
    return driver.connect("jdbc:pool:", null);
  }

  public Connection getConnection(String username, String password) throws SQLException {
    ConnectionPool pool = new ConnectionPool(url, username, password, 1, connectionTimeout, idleTimeout, agedTimeout);
    return pool.getConnection();
  }

  public PrintWriter getLogWriter() throws SQLException {
    return logWriter;
  }

  public void setLogWriter(PrintWriter out) throws SQLException {
    logWriter = out;
  }

  public void setLoginTimeout(int seconds) throws SQLException {
    timeout = seconds;
  }

  public int getLoginTimeout() throws SQLException {
    return timeout;
  }

  public String getURL() {
    return driver.getURL();
  }

  public void setURL(String url) {
    driver.setURL(url);
  }


  private int timeout = 0;
  private PrintWriter logWriter = null;
  private ConnectionDriver driver;
  private String url;
  private long connectionTimeout;
  private long idleTimeout;
  private long agedTimeout;
}
