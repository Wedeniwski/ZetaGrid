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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

import zeta.server.util.DatabaseUtils;

/**
 *  A JDBC connection in the connection pool, and is essentially a
 *  wrapper around a real JDBC connection.
 *
 *  @see java.sql.Connection
 *  @version 1.9.4, August 27, 2004
**/
public class ConnectionPool {

  public ConnectionPool(String url, String user, String password, int totalConnections, long connectionTimeout, long idleTimeout, long agedTimeout) {
    this.url = url;
    this.user = user;
    this.password = password;
    this.totalConnections = totalConnections;
    this.connectionTimeout = connectionTimeout*1000;
    this.idleTimeout = idleTimeout*1000;
    this.agedTimeout = agedTimeout*1000;
    connections = new LinkedList();
    reaper = new ConnectionReaper(this);
    reaper.start();
  }

  public synchronized String getURL() {
    return url;
  }

  public synchronized void setURL(String url) {
    this.url = url;
    closeConnections();
  }

  public synchronized void reapConnections() {
    long currentTime = System.currentTimeMillis();
    Iterator i = connections.iterator();
    while (i.hasNext()) {
      PooledConnection con = (PooledConnection)i.next();
      if (con.inUse()) {
        if (con.getLastUse()+connectionTimeout < currentTime) {
          removeConnection(con);
          i = connections.iterator();
        }
      } else {
        if (con.getLastUse()+idleTimeout < currentTime) {
          removeConnection(con);
          i = connections.iterator();
        }
      }
    }
  }

  public synchronized void closeConnections() {
    while (!connections.isEmpty()) {
      removeConnection((PooledConnection)connections.getFirst());
    }
  }

  public synchronized Connection getConnection() throws SQLException {
    Iterator i = connections.iterator();
    while (i.hasNext()) {
      PooledConnection con = (PooledConnection)i.next();
      if (con.lease()) {
        if (!con.isClosed()) {
          return con;
        }
        removeConnection(con);
        i = connections.iterator();
      }
    }
    if (connections.size() < totalConnections) {
      PooledConnection con = new PooledConnection(DriverManager.getConnection(url, user, password), this);
      con.lease();
      connections.add(con);
      return con;
    }
    throw new SQLException("Too many connections");
  } 

  public synchronized void returnConnection(PooledConnection con) {
    con.expireLease();
    try {
      if (!connections.contains(con)) {
        DatabaseUtils.close(con.getConnection());
      } else if (con.isClosed() || con.getCreatedTimestamp()+agedTimeout < System.currentTimeMillis()) {
        removeConnection(con);
      }
    } catch (SQLException se) {
      removeConnection(con);
    }
  }

  public synchronized int getPoolsize() {
    return connections.size();
  }

  protected void finalize() {
    closeConnections();
  }

  private synchronized void removeConnection(PooledConnection con) {
    connections.remove(con);
    DatabaseUtils.close(con.getConnection());
  }

  private LinkedList connections;
  private String url, user, password;
  private int totalConnections;
  private ConnectionReaper reaper;
  private long connectionTimeout = 1000*1000;
  private long idleTimeout = 2000*1000;
  private long agedTimeout = 12*3600*1000;
}

class ConnectionReaper extends Thread {
  ConnectionReaper(ConnectionPool pool) {
    this.pool = pool;
  }

  public void run() {
    while (true) {
      try {
        sleep(reapTime);
      } catch (InterruptedException e) {
      }
      pool.reapConnections();
    }
  }

  private ConnectionPool pool;
  private final long reapTime = 180*1000;
}
