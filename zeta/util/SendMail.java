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

package zeta.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *  @version 1.9.3, May 29, 2004
**/
public class SendMail extends Thread {

  public SendMail(String smtpHostname, int smtpPort, String sendFrom, String sendTo, String title, String message) {
    this(smtpHostname, smtpPort, null, null, sendFrom, new String[] { sendTo }, title, message);
  }

  public SendMail(String smtpHostname, int smtpPort, String loginName, String loginPassword, String sendFrom, String sendTo, String title, String message) {
    this(smtpHostname, smtpPort, loginName, loginPassword, sendFrom, new String[] { sendTo }, title, message);
  }

  public SendMail(String smtpHostname, int smtpPort, String loginName, String loginPassword, String sendFrom, String[] sendTo, String title, String message) {
    this.smtpHostname = smtpHostname;
    this.smtpPort = smtpPort;
    this.loginName = loginName;
    this.loginPassword = loginPassword;
    this.realNameFrom = this.sendFrom = sendFrom;
    this.realNameTo = new String[sendTo.length];
    this.sendTo = new String[sendTo.length];
    System.arraycopy(sendTo, 0, this.realNameTo, 0, sendTo.length);
    System.arraycopy(sendTo, 0, this.sendTo, 0, sendTo.length);
    this.title = title;
    this.message = message;
  }

  public void setRealNameFrom(String realNameFrom) {
    this.realNameFrom = realNameFrom;
  }

  public void setRealNameTo(String realNameTo) {
    setRealNameTo(0, realNameTo);
  }

  public void setRealNameTo(int idx, String realNameTo) {
    this.realNameTo[idx] = realNameTo;
  }

  /**
   *  @param pop3Port -1 means no authentication over POP3 protocol
  **/
  public void setPop3Authentication(int pop3Port) {
    this.pop3Port = pop3Port;
  }

  public synchronized void run() {
    for (int i = 0; i < sendTo.length; ++i) {
      if (!isValidEmailAddressSyntax(sendTo[i])) {
        if (verbose) {
          System.out.println("E-mail address '" + sendTo[i] + "' is invalid!");
        }
        successfully = false;
        return;
      }
    }
    successfully = true;
    Socket socket = null;
    try {
      if (pop3Port >= 0 && loginName != null && loginPassword != null) {
        socket = new Socket(smtpHostname, pop3Port);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "8859_1"));
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
        String s = new String(decoder.decodeBuffer(loginName));
        successfully &= sendline(in, out, "USER " + s);
        s = new String(decoder.decodeBuffer(loginPassword));
        successfully &= sendline(in, out, "PASS " + s);
        socket.close();
        socket = null;
      }
      socket = new Socket(smtpHostname, smtpPort);
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "8859_1"));
      PrintWriter out = new PrintWriter(socket.getOutputStream());
      successfully &= sendline(in, out, "HELO " + InetAddress.getLocalHost().getHostName());
      if (pop3Port < 0 && loginName != null && loginPassword != null) {
        successfully &= sendline(in, out, "AUTH LOGIN");
        successfully &= sendline(in, out, loginName);
        successfully &= sendline(in, out, loginPassword);
      }

      for (int i = 0; successfully && i < sendTo.length; ++i) {
        if (verbose) {
          System.out.print("send to " + sendTo[i]);
          System.out.flush();
        }
        successfully &= sendline(in, out, "MAIL FROM: " + sendFrom);
        successfully &= sendline(in, out, "RCPT TO: " + sendTo[i]);
        lastSendTo = sendTo[i];
        successfully &= sendline(in, out, "DATA");
        successfully &= sendline(in, out, "From: " + realNameFrom + "\nTo: " + realNameTo[i] + "\nSubject: " + title + '\n' + message + "\n.");
        if (verbose) {
          if (successfully) {
            System.out.println(" successfully.");
          } else {
            System.out.println(" error occurred!");
          }
        }
      }

      successfully &= sendline(in, out, "QUIT");
    } catch (IOException ioe) {
      ThrowableHandler.handle(ioe);
    } finally {
      if (socket != null) {
        try {
          socket.close();
        } catch (IOException ioe) {
        }
      }
    }
  }

  public synchronized boolean isSuccessfully() {
    return successfully;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  /**
   *  Validates an email address using syntax check.
   *  The method checks that only the characters a-z, A-Z, 0-9, ., _, - are in the address.
   *  It checks that at least 67 characters or less following the last "@"
   *  and also that the entire address is at least 6 characters.
   *  It checks that the address contains at least one character following the "@",
   *  followed by a dot ("."), followed by by at least two characters.
   *  It checks the invalid patterns "..", "-.", ".-", "@." and "@-" and the address not ending with the characters ., _, -
  **/
  public static boolean isValidEmailAddressSyntax(String emailAddress) {
    int idxAt = -1;
    int idxDot = -1;
    int l = emailAddress.length();
    for (int i = 0; i < l; ++i) {
      char c = emailAddress.charAt(i);
      if (!Character.isLetterOrDigit(c)) {
        if (c == '@') {
          idxAt = i;
          if (i+1 == l) {
            return false;
          }
          c = emailAddress.charAt(i+1);
          if (c == '.' || c == '-') {
            return false;
          }
        } else if (c == '-') {
          if (i+1 == l || emailAddress.charAt(i+1) == '.') {
            return false;
          }
        } else if (c == '.') {
          idxDot = i;
          if (i+1 == l || emailAddress.charAt(i+1) == '-' || emailAddress.charAt(i+1) == '.') {
            return false;
          }
        } else if (c == '_') {
          if (i+1 == l) {
            return false;
          }
        } else {
          return false;
        }
      } else if (((int)c) >= 128) {
        return false;
      }
    }
    return (l >= 6 && idxAt > 0 && idxAt < idxDot && idxDot+2 < l && l-idxAt <= 68);
  }

  private boolean sendline(BufferedReader in, PrintWriter out, String data) throws IOException {
    if (debug) {
      System.out.println(data);
    }
    out.print(data + '\n');
    out.flush();
    data = in.readLine();
    if (debug) {
      System.out.println(">" + data);
    }
    return (data != null && (data.startsWith("2") || data.startsWith("3") || data.startsWith("+OK")));
  }

  public static void main(String args[]) {
    if (args.length == 8) {
      String sendFrom = args[4];
      String sendFromEmail = args[4];
      int i = sendFrom.lastIndexOf(' ', sendFrom.indexOf('@'));
      if (i > 0) {
        sendFromEmail = sendFrom.substring(i+1);
        sendFrom = "\"" + sendFrom.substring(0, i) + "\"" + sendFrom.substring(i);
      }
      sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
      FileInputStream in = null;
      try {
        in = new FileInputStream(args[7]);
        ByteArrayOutputStream out = new ByteArrayOutputStream(10000);
        StreamUtils.writeData(in, out, true, true);
        in = null;
        String message = out.toString();
        in = new FileInputStream(args[5]);
        out.reset();
        StreamUtils.writeData(in, out, true, true);
        StringTokenizer sendTo = new StringTokenizer(out.toString());
        List sendToEmail = new ArrayList(sendTo.countTokens());
        while (sendTo.hasMoreTokens()) {
          String s = sendTo.nextToken().trim();
          if (isValidEmailAddressSyntax(s)) {
            sendToEmail.add(s);
          }
        }
        String[] sendToEmailArray = new String[sendToEmail.size()];
        for (int j = 0; j < sendToEmailArray.length; ++j) {
          sendToEmailArray[j] = (String)sendToEmail.get(j);
        }
        while (true) {
          SendMail sendMail = new SendMail(args[0], Integer.parseInt(args[1]), encoder.encode(args[2].getBytes()), encoder.encode(args[3].getBytes()), sendFromEmail, sendToEmailArray, args[6], message);
          sendMail.setRealNameFrom(sendFrom);
          sendMail.setPop3Authentication(110);
          sendMail.setVerbose(true);
          sendMail.run();
          if (sendMail.isSuccessfully()) {
            break;
          }
          int j = sendToEmailArray.length;
          while (j > 0 && !sendToEmailArray[--j].equals(sendMail.lastSendTo));
          if (j+1 >= sendToEmailArray.length) {
            break;
          }
          String[] sendToEmailArray2 = new String[sendToEmailArray.length-j-1];
          for (int k = 0; ++j < sendToEmailArray.length; ++k) {
            sendToEmailArray2[k] = sendToEmailArray[j];
          }
          sendToEmailArray = sendToEmailArray2;
        }
      } catch (IOException ioe) {
        ThrowableHandler.handle(ioe);
      } finally {
        StreamUtils.close(in);
      }
    } else {
      System.out.println("USAGE: <SMTP hostname> <SMTP port> <login name> <login password> <send from> <filename of send to names> <title> <filename of message>");
    }
  }

  private String smtpHostname;
  private int smtpPort;
  private String loginName;
  private String loginPassword;
  private String sendFrom;
  private String realNameFrom;
  private String[] sendTo;
  private String lastSendTo;
  private String[] realNameTo;
  private String title;
  private String message;
  private boolean debug = false;
  private boolean verbose = false;
  private int pop3Port = -1;
  private boolean successfully = false;
}
