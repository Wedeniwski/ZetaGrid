#if !defined(_CONFIG_INCLUDED_)
#define _CONFIG_INCLUDED_

#include <string>

using namespace std;

struct Config {
  static string ZETAGRID_ROOT_PATH;

  string name;
  string eMail;
  bool messages;
  string team;
  char packageSize;
  int numberOfPackages;
  int exit;
  int exitTime;
  string exitFilename;
  int numberOfProcessors;
  string priority;
  int processorsUsage;
  string trustFilesFromUsers;
  int resources;
  string activeAt;
  bool standardOutput;
  bool infoOutput;
  string infoTimestamp;
  string infoFilename;
  string infoEventLogFilename;
  string infoEventLogTimestamp;
  string infoLogFileSize;
  int infoLogMaxBackupIndex;
  bool exceptionOutput;
  string javaVM;
  string call;
  string javaLib;
  string rootPath;
  string filename;
  bool asynchronousTransfer;
  int transferDetect;
  bool displayState;
  string task;
  enum Mode { screensaver, service, commandline } mode;
  bool systemTray;
  int port;
  string proxyHost;
  string proxyPort;
  string proxyAuthenticationUsername;
  string proxyAuthenticationPassword;
  bool encryptionUrl;
  string hostName;
  int hostPort;
  bool checkConnectionToServer;
  bool checkBatteryMode;

  Config(const string& file = ZETAGRID_ROOT_PATH + "\\zeta.cfg");
  void save(bool saveCommand);

  static void numberOfLocalWorkunits(int& count, int& finished);
  static string getZetaGridRootPath();
};

#endif // !defined(_CONFIG_INCLUDED_)
