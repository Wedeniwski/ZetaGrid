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

import java.util.Enumeration;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/**
 *  @version 1.9.4, August 27, 2004
**/
public class ConnectionDataSourceFactory implements ObjectFactory {
  public Object getObjectInstance(Object obj, Name namingName, Context nameCtx, Hashtable environment) throws NamingException {
    String driver = null;
    String url = null;
    String user = null;
    String password = null;
    int totalConnections = 50;
    long connectionTimeout = 1000*1000;
    long idleTimeout = 2000*1000;
    long agedTimeout = 12*3600*1000;
    try {
      Reference ref = (Reference) obj;
      Enumeration addrs = ref.getAll();
      while (addrs.hasMoreElements()) {
        RefAddr addr = (RefAddr)addrs.nextElement();
        String name = addr.getType();
        String value = (String)addr.getContent();
        if (name.equals("database.connection.driver")) {
          driver = value;
        } else if (name.equals("database.connection.url")) {
          url = value;
        } else if (name.equals("database.connection.username")) {
          user = value;
        } else if (name.equals("database.connection.password")) {
          password = value;
        } else if (name.equals("database.connection.total.connections")) {
          totalConnections = Integer.parseInt(value);
        } else if (name.equals("database.connection.timeout")) {
          connectionTimeout = Long.parseLong(value);
        } else if (name.equals("database.connection.idle.timeout")) {
          idleTimeout = Long.parseLong(value);
        } else if (name.equals("database.connection.aged.timeout")) {
          agedTimeout = Long.parseLong(value);
        }
      }
      return new ConnectionDataSource(driver, url, user, password, totalConnections, connectionTimeout, idleTimeout, agedTimeout);
    } catch (Exception e) {
      NamingException ex = new NamingException();
      ex.setRootCause(e);
      throw ex;
    }
  }
}
