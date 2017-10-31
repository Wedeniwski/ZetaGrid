#include "stdafx.h"
#include "resource.h"
#include "config.h"
#include <fstream>
#include <io.h>
#include "InstallDlg.h"
#include "registry.h"

using namespace std;

string Config::ZETAGRID_ROOT_PATH = Config::getZetaGridRootPath();

Config::Config(const string& file)
{
  name = "";
  eMail = "";
  messages = true;
  team = "";
  packageSize = 'm';
  numberOfPackages = 1;
  exit = 0;
  exitTime = 0;
  exitFilename = "";
  numberOfProcessors = 1;
  priority = "low";
  processorsUsage = 100;
  trustFilesFromUsers = "";
  resources = 0;
  activeAt = "";
  standardOutput = true;
  infoOutput = true;
  infoTimestamp = "";
  infoFilename = "info.log";
  infoEventLogFilename = "events.log";
  infoEventLogTimestamp = "yyyy/MM/dd HH:mm:ss";
  infoLogFileSize = "1MB";
  infoLogMaxBackupIndex = 0;
  exceptionOutput = true;
  javaVM = "java.exe";
  javaLib = "jvm.dll";
  rootPath = file.substr(0, file.find_last_of('\\'));
  filename = file;
  asynchronousTransfer = false;
  transferDetect = 0;
  task = "zeta-zeros";
  displayState = true;
  mode = screensaver;
  systemTray = true;
  port = 10000;
  proxyHost = proxyPort = "";
  proxyAuthenticationUsername = proxyAuthenticationPassword = "";
  encryptionUrl = true;
  checkConnectionToServer = false;
  checkBatteryMode = false;
  // configuration file
  ifstream fin(file.c_str());
  if (!fin) return;
  string s;
  while (!fin.eof()) {
    getline(fin, s);
    if (s.length() < 4) continue;
    if (s.compare(0, 5, "name=") == 0) name = s.substr(5);
    else if (s.compare(0, 6, "eMail=") == 0) eMail = s.substr(6);
    else if (s.compare(0, 9, "messages=") == 0) messages = (s.substr(9) == "true");
    else if (s.compare(0, 5, "team=") == 0) team = s.substr(5);
    else if (s.compare(0, 4, "lib=") == 0) javaLib = s.substr(4);
    else if (s.compare(0, 5, "call=") == 0) {
      call = s.substr(5);
      javaVM = (s[5] == '"')? s.substr(6, s.find('"', 6)-6) : s.substr(5, s.find(' ', 5)-5);

      const int l = s.length();
      int idx = s.find("-Dhttp.proxyHost=");
      if (idx != string::npos) {
        for (idx += 17; idx < l && s[idx] != ' '; ++idx) {
          proxyHost += s[idx];
        }
      }
      idx = s.find("-Dhttp.proxyPort=");
      if (idx != string::npos) {
        for (idx += 17; idx < l && s[idx] != ' '; ++idx) {
          proxyPort += s[idx];
        }
      }
    } else if (s.compare(0, 5, "prio=") == 0) {
      s = s.substr(5);
      if (s == "low" || s == "normal") priority = s;
    } else if (s.compare(0, 6, "sleep=") == 0) {
      processorsUsage = atoi(s.c_str()+6);
      if (processorsUsage <= 0 || processorsUsage > 12000) {
        processorsUsage = 100;
      } else {
        processorsUsage = (processorsUsage*100)/12001.0;
      }
    } else if (s.compare(0, 17, "processors.usage=") == 0) {
      processorsUsage = atoi(s.c_str()+17);
      if (processorsUsage < 0) {
        processorsUsage = 0;
      } else if (processorsUsage > 100) {
        processorsUsage = 100;
      }
    } else if (s.compare(0, 11, "work_units=") == 0) {
      numberOfPackages = atoi(s.c_str()+11);
    } else if (s.compare(0, 5, "exit=") == 0) {
      exit = atoi(s.c_str()+5);
    } else if (s.compare(0, 10, "exit.time=") == 0) {
      exitTime = atoi(s.c_str()+10);
    } else if (s.compare(0, 10, "exit.filename=") == 0) {
      exitFilename = s.substr(10);;
    } else if (s.compare(0, 11, "processors=") == 0) {
      numberOfProcessors = atoi(s.c_str()+11);
    } else if (s.compare(0, 10, "resources=") == 0) {
      resources = atoi(s.c_str()+10);
    } else if (s.compare(0, 15, "work_unit_size=") == 0) {
      if (s.length() > 15) {
        switch (s[15]) {
        case 't':
        case 's':
        case 'm':
        case 'l':
        case 'h':
          packageSize = s[15]; break;
        }
      }
    } else if (s.compare(0, 10, "active.at=") == 0) {
      activeAt = s.substr(10);
    } else if (s.compare(0, 16, "standard.output=") == 0) {
      standardOutput = (s.substr(16) == "true");
    } else if (s.compare(0, 12, "info.output=") == 0) {
      infoOutput = (s.substr(12) == "true");
    } else if (s.compare(0, 15, "info.timestamp=") == 0) {
      infoTimestamp = s.substr(15);
    } else if (s.compare(0, 14, "info.filename=") == 0) {
      infoFilename = s.substr(14);
    } else if (s.compare(0, 18, "info.log.filename=") == 0) {
      infoEventLogFilename = s.substr(18);
    } else if (s.compare(0, 19, "info.log.timestamp=") == 0) {
      infoEventLogTimestamp = s.substr(19);
    } else if (s.compare(0, 19, "info.log.file_size=") == 0) {
      infoLogFileSize = s.substr(19);
    } else if (s.compare(0, 26, "info.log.max_backup_index=") == 0) {
      infoLogMaxBackupIndex = atoi(s.c_str()+26);
    } else if (s.compare(0, 17, "exception.output=") == 0) {
      exceptionOutput = (s.substr(17) == "true");
    } else if (s.compare(0, 22, "transfer.asynchronous=") == 0) {
      asynchronousTransfer = (s.substr(22) == "true");
    } else if (s.compare(0, 16, "transfer.detect=") == 0) {
      transferDetect = atoi(s.c_str()+16);
    } else if (s.compare(0, 6, "state=") == 0) {
      displayState = (atoi(s.c_str()+6) == 1);
    } else if (s.compare(0, 5, "task=") == 0) {
      task = s.substr(5);
    } else if (s.compare(0, 23, "trust_files_from_users=") == 0) {
      trustFilesFromUsers = s.substr(23);
    } else if (s.compare(0, 5, "mode=") == 0) {
      const char* m = s.c_str()+5;
      if (strcmp(m, "screensaver") == 0) {
        mode = screensaver;
      } else if (strcmp(m, "service") == 0) {
        mode = service;
      } else if (strcmp(m, "commandline") == 0) {
        mode = commandline;
      } else {
        mode = screensaver;
      }
    } else if (s.compare(0, 11, "systemTray=") == 0) {
      systemTray = (atoi(s.c_str()+11) == 1);
    } else if (s.compare(0, 5, "port=") == 0) {
      port = atoi(s.c_str()+5);
    } else if (s.compare(0, 30, "proxy.authentication.username=") == 0) {
      proxyAuthenticationUsername = s.substr(30);
    } else if (s.compare(0, 30, "proxy.authentication.password=") == 0) {
      proxyAuthenticationPassword = s.substr(30);
    } else if (s.compare(0, 15, "encryption.url=") == 0) {
      encryptionUrl = (s.substr(15) == "true");
    } else if (s.compare(0, 17, "check.connection=") == 0) {
      checkConnectionToServer = (s.substr(17) == "true");;
    } else if (s.compare(0, 14, "check.battery=") == 0) {
      checkBatteryMode = (s.substr(14) == "true");
    }
  }
  fin.close();
  // convert:
  int i,l = javaVM.length();
  s = "";
  for (i = 0; i < l; ++i) {
    const char c = javaVM[i];
    if (c == '\\' && i+1 < l && javaVM[i+1] == '\\') ++i;
    s.append(1, c);
  }
  javaVM = s;
  l = call.length();
  s = "";
  for (i = 0; i < l; ++i) {
    const char c = call[i];
    if (c == '\\' && i+1 < l && call[i+1] == '\\') ++i;
    s.append(1, c);
  }
  call = s;
  l = javaLib.length();
  s = "";
  for (i = 0; i < l; ++i) {
    const char c = javaLib[i];
    if (c == '\\' && i+1 < l && javaLib[i+1] == '\\') ++i;
    s.append(1, c);
  }
  javaLib = s;
  // default configuration file
  hostName = "";
  hostPort = 80;
  int idx = file.find_last_of('\\');
  string defaultFile = "";
  if (idx != string::npos) {
    defaultFile = file.substr(0, idx+1);
  }
  defaultFile += "default.cfg";
  ifstream fin2(defaultFile.c_str());
  if (!fin2) return;
  while (!fin2.eof()) {
    getline(fin2, s);
    if (s.length() < 10) continue;
    if (s.compare(0, 10, "host.name=") == 0) hostName = s.substr(10);
    else if (s.compare(0, 10, "host.port=") == 0) hostPort = atoi(s.substr(10).c_str());
  }
}

void Config::save(bool saveCommand)
{
  ofstream fout(filename.c_str());
  fout << "# User identification\n";
  fout << "# Please note: We would very much appreciate to see your real name in the\n";
  fout << "#              statistics, but if you do not like to enter your real name for\n";
  fout << "#              some reason, please use the name 'anonymous' instead.\n";
  fout << "#              The e-mail address is optional. We guarantee you not to give your\n";
  fout << "#              e-mail address to any third parties. The e-mail is only used for\n";
  fout << "#              the location statistic and to send important ZetaGrid related\n";
  fout << "#              announcements. Do not worry, this will rarely happen.\n";
  fout << "#\n";
  fout << "#              All awards (see http://www.zetagrid.net/zeta/prizes.html) will\n";
  fout << "#              be offered only to correctly registered users. This means that\n";
  fout << "#              the user must provide a correct user name and a valid e-mail\n";
  fout << "#              address required for contact purposes.\n";
  fout << "#\n";
  fout << "#              These values must be changed at the server configuration also\n";
  fout << "#              if you change these values here in your local configuration.\n";
  fout << "#              Your server configuration is available at\n";
  fout << "#              http://www.zetagrid.net/servlet/service/producers\n";
  fout << "#              where you have to define your user name, e-mail and an\n";
  fout << "#              active work unit.\n";
  fout << "name=" << name.c_str();
  fout << "\neMail=" << eMail.c_str();
  fout << "\n\n# Receive important ZetaGrid related announcements";
  fout << "\n# You will receive important ZetaGrid related announcements if messages=true.";
  fout << "\n# Do not worry, this will rarely happen. Set messages=false if you do not want";
  fout << "\n# to receive announcements.";
  fout << "\n# The default value is messages=true.";
  fout << "\n#";
  fout << "\n# This value must be changed at the server configuration also if you change";
  fout << "\n# this value here in your local configuration.";
  fout << "\n# Your server configuration is available at";
  fout << "\n# http://www.zetagrid.net/servlet/service/producers";
  fout << "\n# where you have to define your user name, e-mail and an active work unit.";
  fout << "\nmessages=" << ((messages)? "true" : "false");
  fout << "\n\n# Team identification";
  fout << "\n# The team name is optional. You can be a member of a team and your name will be";
  fout << "\n# listed in further statistics. If you want to change your team membership";
  fout << "\n# you must supply a valid e-mail address in the appropriate field.";
  fout << "\n#";
  fout << "\n# This value must be changed at the server configuration also if you change";
  fout << "\n# this value here in your local configuration.";
  fout << "\n# Your server configuration is available at";
  fout << "\n# http://www.zetagrid.net/servlet/service/producers";
  fout << "\n# where you have to define your user name, e-mail and an active work unit.\n";
  if (team.length() == 0) {
    fout << '#';
  }
  fout << "team=" << team.c_str();
  fout << "\n\n# Size of the work units (estimates are made on Pentium IV 2 GHz, April 2003)";
  fout << "\n# Please note: After a time frame of about 7 days all incomplete work units";
  fout << "\n#              will be redistributed.";
  fout << "\n# t: tiny work unit ~  2 hours";
  fout << "\n# s: small work unit ~  4 hours";
  fout << "\n# m: medium work unit ~ 6 hours  (recommended)";
  fout << "\n# l: large work unit ~  8 hours";
  fout << "\n# h: huge work unit ~  12 hours";
  fout << "\n# Remark: The performance index will decrease about 0.3% per day because the";
  fout << "\n#         complexity to separate the zeros is growing continuously.";
  fout << "\n#         Therefore, the perfomance index will be 10% lower in about 30 days.";
  fout << "\nwork_unit_size=" << packageSize;
  fout << "\n\n# Number of work units";
  fout << "\n# The general limit of work units provided by the server is 5.";
  fout << "\n# Every work unit expires after 7 days.";
  fout << "\nwork_units=" << numberOfPackages;
  fout << "\n\n# Terminates the client";
  fout << "\n# The client will be terminated after calculating a number of work units.";
  fout << "\n# The default value 0 means that the client calculates infinitely many work";
  fout << "\n# units and never stops.";
  fout << "\nexit=" << exit;
  fout << "\n\n# Terminates the client in seconds";
  fout << "\n# The default value 0 means that the client will never be terminated.";
  fout << "\nexit.time=" << exitTime;
  fout << "\n\n# Terminates the client if the file exists";
  fout << "\n# The termination could have a delay up to 1 minute.";
  fout << "\nexit.filename=" << exitFilename.c_str();
  fout << "\n\n# Number of work units must be greater or equal than the number of processors";
  fout << "\n# The value 0 sequentially completes all local work units and terminates the";
  fout << "\n# process.";
  fout << "\nprocessors=" << numberOfProcessors;
  fout << "\n\n# Usage of the processor power (default value for all timeframes)";
  fout << "\n# The value >=100 means the usage of the complete CPU power and a";
  fout << "\n# small value (>0) reduce the intensity. This value maybe similar to";
  fout << "\n# the CPU usage, e.g. 50 means about 50% CPU usage.";
  fout << "\n# But this value will never be exact to the real CPU usage.";
  fout << "\nprocessors.usage=" << processorsUsage;
  fout << "\n\n# Transfer results asynchronous to the computation";
  fout << "\ntransfer.asynchronous=" << ((asynchronousTransfer)? "true" : "false");
  fout << "\n\n# Transfer results asynchronous to the computation";
  fout << "\n# if 'transfer.asynchronous=true' and a connection to the server is detected.";
  fout << "\n# The default value 0 deactivate this function.";
  fout << "\n# A value > 0 means in how many seconds a connection to the server will be";
  fout << "\n# checked. This value must be greater than or equal to 300 seconds.";
  fout << "\ntransfer.detect=" << transferDetect;
  fout << "\n\n# Set the task which should be computed";
  fout << "\n# the default is 'zeta-zeros': Verification of the Riemann Hypothesis";
  fout << "\ntask=" << task.c_str();
  fout << "\n\n# Trust in users that generated the download files";
  fout << "\n# Specify user names separated by comma.";
  fout << "\n# The default value is empty which means that you trust all users,";
  fout << "\n# i.e. download files.";
  fout << "\ntrust_files_from_users=" << trustFilesFromUsers.c_str();
  fout << "\n\n# Using resources";
  fout << "\n# 0: using about 37 MB main memory (default)";
  fout << "\n# 1: using about 45 MB main memory (~ 40% faster)";
  fout << "\n# 2: using about 51 MB main memory (~ 70% faster)";
  fout << "\n# 3: using about 85 MB main memory (~ 100% faster)";
  fout << "\nresources=" << resources;
  fout << "\n\n# Schedules the timeframes when the client can activate the computation";
  fout << "\n# Format: {<Day of Week><start hour>:<start minute>-<stop hour>:<stop minute>[,<CPU usage>]}*";
  fout << "\n# <Day of Week>: the name of the day, i.e. (Mo, Tu, We, Th, Fr, Sa, Su)";
  fout << "\n# <start hour>: controls what hour the computation will start, and is specified";
  fout << "\n#               in the 24 hour clock, values must be between 0 and 23";
  fout << "\n#               (0 is midnight)";
  fout << "\n# <start minute>: controls what minute of the hour the computation will start,";
  fout << "\n#                 value must be between 0 and 59";
  fout << "\n# <stop hour>: controls what hour the computation will start, and is specified";
  fout << "\n#              in the 24 hour clock, values must be between 0 and 23";
  fout << "\n#              (0 is midnight)";
  fout << "\n# <stop minute>: controls what minute of the hour the computation will stop,";
  fout << "\n#                value must be between 0 and 59";
  fout << "\n# <CPU usage>: usage of the processor power during the timeframe";
  fout << "\n#";
  fout << "\n# Example:";
  fout << "\n# Mo08:00-12:00,80Mo13:00-20:00Tu00:00-00:00We00:00-00:00Th00:00-00:00";
  fout << "\n#";
  fout << "\n# Conditions:";
  fout << "\n# 1. A timefame 0:00 to 0:00 means the whole day";
  fout << "\n# 2. Every timeframe less than 1 minute will be ignored";
  fout << "\n# 3. Timeframes which overlaps a previous timeframe will be ignored";
  fout << "\n# 4. The client activate the computation every time if this parameter is empty";
  fout << "\nactive.at=" << activeAt.c_str();
  fout << "\n\n# Put all logging data of the computation also on standard output stream";
  fout << "\nstandard.output=" << ((standardOutput)? "true" : "false");
  fout << "\n\n# Put all imformation also on standard output stream";
  fout << "\ninfo.output=" << ((infoOutput)? "true" : "false");
  fout << "\n\n# Appends a timestamp at the end of every information";
  if (infoTimestamp.length() == 0) {
    fout << "\n#info.timestamp=yyyy/MM/dd HH:mm:ss";
  } else {
    fout << "\ninfo.timestamp=" << infoTimestamp.c_str();
  }
  fout << "\n\n# Put all information also in a file that it can be monitored by external tools";
  fout << "\ninfo.filename=" << infoFilename.c_str();
  fout << "\n\n# Appends all information also in an event log file";
  fout << "\n# This file will be cleared if the client starts or the maximal number of lines";
  fout << "\n# was reached.";
  fout << "\ninfo.log.filename=" << infoEventLogFilename.c_str();
  fout << "\n\n# Timestamps at the beginning of every information in the event log file";
  fout << "\ninfo.log.timestamp=" << infoEventLogTimestamp.c_str();
  fout << "\n\n# The event log file will be rolled over when it reaches a specified size,";
  fout << "\n# e.g. 50kB, 2MB, or 1GB.";
  fout << "\ninfo.log.file_size=" << infoLogFileSize.c_str();
  fout << "\n\n# Keep backup files of the event log files";
  fout << "\n# When roll-over occurs, the event log file - e.g. 'events.log' - is";
  fout << "\n# automatically moved to 'events.log.1'";
  fout << "\ninfo.log.max_backup_index=" << infoLogMaxBackupIndex;
  fout << "\n\n# Put all exceptions on standard output stream";
  fout << "\nexception.output=" << ((exceptionOutput)? "true" : "false");
  fout << "\n\n# Proxy authentication";
  fout << "\n# Define username, password and the system properties";
  fout << "\n# proxyHost and proxyPort to enable authentication";
  fout << "\nproxy.authentication.username=" << proxyAuthenticationUsername.c_str();
  fout << "\nproxy.authentication.password=" << proxyAuthenticationPassword.c_str();
  fout << "\n\n# Encrypts the URL of every connection to a server, e.g. no e-mail address";
  fout << "\n# will be sent as plain text";
  fout << "\nencryption.url=" << ((encryptionUrl)? "true" : "false");
  fout << "\n\n# Port number which is used to avoid conflicts";
  fout << "\n# this port number must be changed if it is used by other applications,";
  fout << "\n# e.g. www.webmin.com";
  fout << "\nport=" << port;
  fout << "\n\n# Windows specific parameters";
  fout << "\nprio=" << priority.c_str();
  fout << "\nstate=" << int(displayState);
  if (mode == service) {
    fout << "\nmode=service";
  } else if (mode == commandline) {
    fout << "\nmode=commandline";
  } else {
    fout << "\nmode=screensaver";
  }
  fout << "\nsystemTray=" << int(systemTray);
  fout << "\ncheck.connection=" << ((checkConnectionToServer)? "true" : "false");
  fout << "\ncheck.battery=" << ((checkBatteryMode)? "true" : "false");

  // convert:
  int i,l = javaLib.length();
  string s = "";
  for (i = 0; i < l; ++i) {
    const char c = javaLib[i];
    if (c == '\\') s.append(1, '\\');
    s.append(1, c);
  }
  fout << "\nlib=" << s.c_str();

  fout << "\ncall=";
  bool b = (javaVM.find(' ') != string::npos);
  if (b) fout << '"';
  l = javaVM.length();
  call = javaVM;
  s = "";
  for (i = 0; i < l; ++i) {
    const char c = javaVM[i];
    if (c == '\\') s.append(1, '\\');
    s.append(1, c);
  }
  fout << s.c_str();
  if (b) fout << '"';
  if (proxyHost.length() > 0) {
    call += " -Dhttp.proxyHost=";
    fout << " -Dhttp.proxyHost=";
    call += proxyHost.c_str();
    fout << proxyHost.c_str();
  }
  if (proxyPort.length() > 0) {
    call += " -Dhttp.proxyPort=";
    fout << " -Dhttp.proxyPort=";
    call += proxyPort.c_str();
    fout << proxyPort.c_str();
  }
  call += " -Djava.library.path=.;";
  l = ZETAGRID_ROOT_PATH.length();
  s = (ZETAGRID_ROOT_PATH.find(' ') != string::npos)? "\"" : "";
  for (i = 0; i < l; ++i) {
    const char c = ZETAGRID_ROOT_PATH[i];
    if (c == '\\') s.append(1, '\\');
    s.append(1, c);
  }
  if (ZETAGRID_ROOT_PATH.find(' ') != string::npos) {
    s.append(1, '\"');
  }
  call += s.c_str();
  call += " -Dsun.net.inetaddr.ttl=0 -Dnetworkaddress.cache.ttl=0 -Dnetworkaddress.cache.negative.ttl=0 -Xmx128m -cp zeta.jar;zeta_client.jar zeta.ZetaClient";
  fout << " -Djava.library.path=.;";
  fout << s.c_str();
  fout << " -Dsun.net.inetaddr.ttl=0 -Dnetworkaddress.cache.ttl=0 -Dnetworkaddress.cache.negative.ttl=0 -Xmx128m -cp zeta.jar;zeta_client.jar zeta.ZetaClient\n";
  fout.close();
  // Change also InstallDlg!
  if (saveCommand && mode == commandline) {
    if (CInstallDlg::IsWindowsNT()) {
      string rootPath = ZETAGRID_ROOT_PATH;
      rootPath += "\\zeta.cmd";
      if (_access(rootPath.c_str(), 0) == 0) {
        CString s,s2;
        s.Format(IDS_OVERWRITE_WITH_NEW_CONFIG, rootPath.c_str());
        s2.LoadString(IDS_OVERWRITE_FILE);
        if (MessageBox(NULL, s, s2, MB_ICONQUESTION | MB_YESNO) == IDNO) {
          return;
        }
      }
      ofstream fout(rootPath.c_str());
      fout << "@echo off\n";
      fout << "rem ======================================================================\n";
      fout << "rem  zeta.cmd        Start script for the ZetaGrid client\n";
      fout << "rem ----------------------------------------------------------------------\n";
      fout << "rem\n";
      fout << "rem  This script sets the environment for the ZetaGrid client.\n";
      fout << "rem\n";
      fout << "rem  Please note:\n";
      fout << "rem\n";
      fout << "rem  - You must set the variable JAVA_HOME below to point at\n";
      fout << "rem    your Java development kit or runtime directory.\n";
      fout << "rem\n";
      fout << "rem  - If you have to access the Internet through a proxy server\n";
      fout << "rem    you must set the variable PROXY_HOST below to your proxy's address\n";
      fout << "rem\n";
      fout << "rem  Prerequisite: Java Runtime Environment 1.2.2 or higher,\n";
      fout << "rem                e.g. http://java.sun.com/j2se/1.3/download.html\n";
      fout << "rem\n";
      fout << "rem ======================================================================\n\n";
      fout << "rem ----------------------------------------------------------------------\n";
      fout << "rem  JAVA_HOME\n";
      fout << "rem  Must point at your Java Development Kit installation.\n";
      fout << "rem  Example: SET JAVA_HOME=C:\\program files\\ibm\\java13\n";
      fout << "rem ----------------------------------------------------------------------\n\n";
      fout << "SET JAVA_HOME=";
      int idx = javaVM.find_last_of("\\bin\\java");
      if (idx != string::npos && idx > 8) {
        fout << javaVM.substr(0, idx-8);
      }
      fout << "\n\n";
      fout << "rem ----------------------------------------------------------------------\n";
      fout << "rem  PROXY_HOST\n";
      fout << "rem  Must contain the proxy server address in case you must access the\n";
      fout << "rem  Internet through a proxy. The default value for the proxy port is 80.\n";
      fout << "rem  Example: SET PROXY_HOST=proxy.computer.com\n";
      fout << "rem           SET PROXY_PORT=80\n";
      fout << "rem ----------------------------------------------------------------------\n\n";
      fout << "SET PROXY_HOST=";
      if (proxyHost.length() > 0) {
        fout << proxyHost.c_str();
      }
      fout << "\nSET PROXY_PORT=";
      if (proxyPort.length() > 0) {
        fout << proxyPort.c_str();
      }
      fout << "\n\nrem ----------------------------------------------------------------------\n";
      fout << "rem  JAVA_COMMAND\n";
      fout << "rem  Creating command to call Java VM\n";
      fout << "rem ----------------------------------------------------------------------\n\n";
      fout << "SET JAVA_COMMAND=\"%JAVA_HOME%\\bin\\java\"\n\n";
      fout << "if not \"%JAVA_HOME%\" == \"\" goto javaHomeDefined\n";
      fout << "echo ========\n";
      fout << "echo WARNING: JAVA_HOME variable is not set\n";
      fout << "echo ========\n";
      fout << "echo This variable should point at your Java development kit installation.\n";
      fout << "SET JAVA_COMMAND=java\n";
      fout << ":javaHomeDefined\n\n\n";
      fout << "rem ----------------------------------------------------------------------\n";
      fout << "rem  JAVA_VM_OPTS\n";
      fout << "rem  Java runtime options used\n";
      fout << "rem ----------------------------------------------------------------------\n\n";
      fout << "SET JAVA_VM_OPTS=-Xmx128m\n\n";
      fout << "rem ----------------------------------------------------------------------\n";
      fout << "rem  ZETA_CLASSPATH\n";
      fout << "rem  Must contain the required libraries, separated by semicolons\n";
      fout << "rem ----------------------------------------------------------------------\n\n";
      fout << "SET ZETA_CLASSPATH=zeta.jar;zeta_client.jar\n\n";
      fout << "rem ----------------------------------------------------------------------\n";
      fout << "rem  ZETA_OPTIONS\n";
      fout << "rem\n";
      fout << "rem ----------------------------------------------------------------------\n\n";
      fout << "SET ZETA_OPTIONS=-Djava.library.path=.\n";
      fout << "SET ZETA_OPTIONS=%ZETA_OPTIONS% -Dsun.net.inetaddr.ttl=0\n";
      fout << "SET ZETA_OPTIONS=%ZETA_OPTIONS% -Dnetworkaddress.cache.ttl=0\n";
      fout << "SET ZETA_OPTIONS=%ZETA_OPTIONS% -Dnetworkaddress.cache.negative.ttl=0\n\n";
      fout << "if \"%PROXY_HOST%\" == \"\" goto noProxyHost\n";
      fout << "SET ZETA_OPTIONS=%ZETA_OPTIONS% -Dhttp.proxyHost=%PROXY_HOST%\n";
      fout << ":noProxyHost\n\n";
      fout << "if \"%PROXY_PORT%\" == \"\" goto noProxyPort\n";
      fout << "SET ZETA_OPTIONS=%ZETA_OPTIONS% -Dhttp.proxyPort=%PROXY_PORT%\n";
      fout << ":noProxyPort\n\n";
      fout << "rem ----------------------------------------------------------------------\n";
      fout << "rem Executing ZetaGrid client...\n";
      fout << "rem ----------------------------------------------------------------------\n\n";
      fout << "start";
      if (priority == "low") {
        fout << " /low";
      }
      fout << " \"ZetaGrid client\" %JAVA_COMMAND% %JAVA_VM_OPTS% -cp %ZETA_CLASSPATH% %ZETA_OPTIONS% zeta.ZetaClient\n";
      fout << "goto end\n\n";
      fout << "rem ----------------------------------------------------------------------\n";
      fout << "rem Exit\n";
      fout << "rem ----------------------------------------------------------------------\n";
      fout << ":error\n";
      fout << "echo You must set JAVA_HOME to point at your Java development kit installation\n";
      fout << ":end\n";
    } else {
      string rootPath = ZETAGRID_ROOT_PATH;
      rootPath += "\\zeta.bat";
      if (_access(rootPath.c_str(), 0) == 0) {
        CString s,s2;
        s.Format(IDS_OVERWRITE_WITH_NEW_CONFIG, rootPath.c_str());
        s2.LoadString(IDS_OVERWRITE_FILE);
        if (MessageBox(NULL, s, s2, MB_ICONQUESTION | MB_YESNO) == IDNO) {
          return;
        }
      }
      ofstream fout(rootPath.c_str());
      fout << "@echo off\n";
      fout << "rem ======================================================================\n";
      fout << "rem  zeta.bat        Start script for the ZetaGrid client\n";
      fout << "rem ----------------------------------------------------------------------\n";
      fout << "rem\n";
      fout << "rem  This script sets the environment for the ZetaGrid client.\n";
      fout << "rem\n";
      fout << "rem  Please note:\n";
      fout << "rem\n";
      fout << "rem  - You must set the variable JAVA_HOME below to point at\n";
      fout << "rem    your Java development kit or runtime directory.\n";
      fout << "rem\n";
      fout << "rem  - If you have to access the Internet through a proxy server\n";
      fout << "rem    you must set the variable PROXY_HOST below to your proxy's address\n";
      fout << "rem\n";
      fout << "rem  Prerequisite: Java Runtime Environment 1.2.2 or higher,\n";
      fout << "rem                e.g. http://java.sun.com/j2se/1.3/download.html\n";
      fout << "rem\n";
      fout << "rem ======================================================================\n\n";
      fout << "rem ----------------------------------------------------------------------\n";
      fout << "rem  JAVA_HOME\n";
      fout << "rem  Must point at your Java Development Kit installation.\n";
      fout << "rem  Example: SET JAVA_HOME=C:\\program files\\ibm\\java13\n";
      fout << "rem ----------------------------------------------------------------------\n\n";
      fout << "SET JAVA_HOME=";
      int idx = javaVM.find_last_of("\\bin\\java");
      if (idx != string::npos && idx > 8) {
        fout << javaVM.substr(0, idx-8);
      }
      fout << "rem ----------------------------------------------------------------------\n";
      fout << "rem  PROXY_HOST\n";
      fout << "rem  Must contain the proxy server address in case you must access the\n";
      fout << "rem  Internet through a proxy. The default value for the proxy port is 80.\n";
      fout << "rem  Example: SET PROXY_HOST=proxy.computer.com\n";
      fout << "rem           SET PROXY_PORT=80\n";
      fout << "rem ----------------------------------------------------------------------\n\n";
      fout << "SET PROXY_HOST=";
      if (proxyHost.length() > 0) {
        fout << proxyHost.c_str();
      }
      fout << "\nSET PROXY_PORT=";
      if (proxyPort.length() > 0) {
        fout << proxyPort.c_str();
      }
      fout << "\n\nrem ----------------------------------------------------------------------\n";
      fout << "rem  JAVA_COMMAND\n";
      fout << "rem  Creating command to call Java VM\n";
      fout << "rem ----------------------------------------------------------------------\n\n";
      fout << "SET JAVA_COMMAND=\"%JAVA_HOME%\\bin\\java\"\n\n";
      fout << "if not \"%JAVA_HOME%\" == \"\" goto javaHomeDefined\n";
      fout << "echo ========\n";
      fout << "echo WARNING: JAVA_HOME variable is not set\n";
      fout << "echo ========\n";
      fout << "echo This variable should point at your Java development kit installation.\n";
      fout << "SET JAVA_COMMAND=java\n";
      fout << ":javaHomeDefined\n\n\n";
      fout << "rem ----------------------------------------------------------------------\n";
      fout << "rem  JAVA_VM_OPTS\n";
      fout << "rem  Java runtime options used\n";
      fout << "rem ----------------------------------------------------------------------\n\n";
      fout << "SET JAVA_VM_OPTS=-Xmx128m\n\n";
      fout << "rem ----------------------------------------------------------------------\n";
      fout << "rem  ZETA_CLASSPATH\n";
      fout << "rem  Must contain the required libraries, separated by semicolons\n";
      fout << "rem ----------------------------------------------------------------------\n\n";
      fout << "SET ZETA_CLASSPATH=zeta.jar;zeta_client.jar\n\n";
      fout << "rem ----------------------------------------------------------------------\n";
      fout << "rem  ZETA_OPTIONS\n";
      fout << "rem\n";
      fout << "rem ----------------------------------------------------------------------\n\n";
      fout << "SET ZETA_OPTIONS=-Djava.library.path=.\n";
      fout << "SET ZETA_OPTIONS=%ZETA_OPTIONS% -Dsun.net.inetaddr.ttl=0\n";
      fout << "SET ZETA_OPTIONS=%ZETA_OPTIONS% -Dnetworkaddress.cache.ttl=0\n";
      fout << "SET ZETA_OPTIONS=%ZETA_OPTIONS% -Dnetworkaddress.cache.negative.ttl=0\n\n";
      fout << "if \"%PROXY_HOST%\" == \"\" goto noProxyHost\n";
      fout << "SET ZETA_OPTIONS=%ZETA_OPTIONS% -Dhttp.proxyHost=%PROXY_HOST%\n";
      fout << ":noProxyHost\n\n";
      fout << "if \"%PROXY_PORT%\" == \"\" goto noProxyPort\n";
      fout << "SET ZETA_OPTIONS=%ZETA_OPTIONS% -Dhttp.proxyPort=%PROXY_PORT%\n";
      fout << ":noProxyPort\n\n";
      fout << "rem ----------------------------------------------------------------------\n";
      fout << "rem Executing ZetaGrid client...\n";
      fout << "rem ----------------------------------------------------------------------\n\n";
      fout << "%JAVA_COMMAND% %JAVA_VM_OPTS% -cp %ZETA_CLASSPATH% %ZETA_OPTIONS% zeta.ZetaClient\n";
      fout << "goto end\n\n";
      fout << "rem ----------------------------------------------------------------------\n";
      fout << "rem Exit\n";
      fout << "rem ----------------------------------------------------------------------\n";
      fout << ":error\n";
      fout << "echo You must set JAVA_HOME to point at your Java development kit installation\n";
      fout << ":end\n";
    }
  }
}

void Config::numberOfLocalWorkunits(int& count, int& finished)
{
  count = finished = 0;
  _finddata_t fileinfo;
  string s = ZETAGRID_ROOT_PATH;
  s += "\\zeta_zeros_*.log";
  long handle = _findfirst(s.c_str(), &fileinfo);
  if (handle != -1) {
    do {
      s = ZETAGRID_ROOT_PATH.c_str();
      s += '\\';
      s += fileinfo.name;
      ifstream fin(s.c_str());
      if (!fin.eof()) {
        fin.seekg(0, ios::end);
        const int pos = fin.tellg();
        if (pos > 0) {
          fin.seekg(pos-1);
          char ch;
          fin.get(ch);
          if (ch == '@') {
            ++finished;
          }
        }
      }
      ++count;
    } while (_findnext(handle, &fileinfo) != -1);
    _findclose(handle);
  }
}

string Config::getZetaGridRootPath()
{
  DWORD len = 1024;
  char path[1024];
  if (getStringValue((unsigned char*)path, &len, HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\ZetaGrid", "InstallLocation") == 0) {
    return path;
  }
  return "c:\\zeta";
}

