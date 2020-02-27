package dev.brighten.hide.handler;

import dev.brighten.db.db.Database;
import dev.brighten.db.db.MongoDatabase;
import dev.brighten.db.db.MySQLDatabase;
import dev.brighten.db.db.StructureSet;
import dev.brighten.hide.Disguise;
import dev.brighten.hide.conf.Mongo;
import dev.brighten.hide.conf.MySQL;
import dev.brighten.hide.disguise.DisguiseObject;
import dev.brighten.hide.user.User;

import java.io.IOException;

public class SyncHandler {

    private Database database;
    private boolean enabled;
    public SyncHandler() {
        if(Mongo.enabled) {
            Disguise.bc("&7Loading in Mongo database from configuration...");
            database = new MongoDatabase(Mongo.database);

            if(Mongo.username.length() > 0 || Mongo.password.length() > 0) {
                database.connect(Mongo.ip, String.valueOf(Mongo.port), Mongo.database,
                        Mongo.username, Mongo.password, Mongo.authDB);
            } else database.connect(Mongo.ip, String.valueOf(Mongo.port), Mongo.database);
            enabled = true;
        } else if(MySQL.enabled) {
            Disguise.bc("&7Loading in MySQL database from configuration...");
            database = new MySQLDatabase(MySQL.name);
            database.connect(MySQL.ip,
                    String.valueOf(MySQL.port),
                    MySQL.database,
                    String.valueOf(MySQL.ssl),
                    MySQL.username,
                    MySQL.password);

            enabled = true;
        } else {
            enabled = false;
            Disguise.bc("&cMust have either Mongo and MySQL configured and enabled for sync functionality.");
        }
    }

    public void upSync(User user) throws IOException {
        if(enabled) {
            StructureSet set;
            if(database.contains(user.uuid.toString())) {
                set = database.get(user.uuid.toString()).get(0);
            } else set = database.create(user.uuid.toString());

            set.input("uuid", user.uuid.toString());
            set.input("disguised", user.disguised);
            set.input("disguise", user.disguise.toJson());
            set.input("group", user.getOriginalGroup());

            set.save(database);
        }
    }

    public void updateUser(User user) {
        if(enabled) {
            if(database.contains(user.uuid.toString())) {
                StructureSet set = database.get(user.uuid.toString()).get(0);

                user.disguise = DisguiseObject.fromJson(set.getObject("disguise"));
                user.setOriginalGroup(set.getObject("group"));
                user.disguised = set.getObject("disguised");
            }
        }
    }
}
