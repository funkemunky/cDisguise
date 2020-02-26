package dev.brighten.hide.conf;

import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;

@Init
public class Mongo {

    @ConfigSetting(path = "database.mongo", name = "enabled")
    public static boolean enabled = false;

    @ConfigSetting(path = "database.mongo", name = "ip")
    public static String ip = "127.0.0.1";

    @ConfigSetting(path = "database.mongo", name = "port")
    public static int port = 27107;

    @ConfigSetting(path = "database.mongo", name = "database")
    public static String database = "cdisguise";

    @ConfigSetting(path = "database.mongo", name = "username")
    public static String username = "";

    @ConfigSetting(path = "database.mongo", name = "password", hide = true)
    public static String password = "";

    @ConfigSetting(path = "database.mongo", name = "authDB")
    public static String authDB = "admin";
}
