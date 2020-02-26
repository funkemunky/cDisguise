package dev.brighten.hide.conf;

import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;

@Init
public class MySQL {

    @ConfigSetting(path = "database.mysql", name = "enabled")
    public static boolean enabled = false;

    @ConfigSetting(path = "database.mysql", name = "ip")
    public static String ip = "127.0.0.1";

    @ConfigSetting(path = "database.mysql", name = "port")
    public static int port = 3306;

    @ConfigSetting(path = "database.mysql", name = "name")
    public static String name = "cdisguise";

    @ConfigSetting(path = "database.mysql", name = "database")
    public static String database = "database";

    @ConfigSetting(path = "database.mysql", name = "username")
    public static String username = "root";

    @ConfigSetting(path = "database.mysql", name = "password", hide = true)
    public static String password = "password";

    @ConfigSetting(path = "database.mysql", name = "ssh")
    public static boolean ssl = true;
}
