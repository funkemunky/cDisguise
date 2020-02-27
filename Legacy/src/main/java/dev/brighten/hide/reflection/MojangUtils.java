package dev.brighten.hide.reflection;

import cc.funkemunky.api.reflections.Reflections;
import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.reflections.types.WrappedConstructor;
import cc.funkemunky.api.reflections.types.WrappedField;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import org.shanerx.mojang.PlayerProfile;

public class MojangUtils {

    private static WrappedClass property = Reflections.getClass(
            (ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_8) ? "net.minecraft.util" : "")
                    + "com.mojang.authlib.properties.Property");
    private static WrappedClass customProperty = new WrappedClass(PlayerProfile.Property.class);
    private static WrappedConstructor propertyCons = property.getConstructor(String.class, String.class, String.class);
    private static WrappedField mpname = property.getFieldByName("name"),
            mpvalue = property.getFieldByName("value"),
            mpsignature = property.getFieldByName("signature");
    private static WrappedField pname = customProperty.getFieldByName("name"),
            pvalue = customProperty.getFieldByName("value"), psignature = customProperty.getFieldByName("signature");


    public static <T> T toMojangProperty(PlayerProfile.Property property) {
        String sig = property.getSignature() == null ? "" : property.getSignature();
        return propertyCons.newInstance(property.getName(), property.getValue(), sig);
    }

    public static PlayerProfile.Property fromMojangProperty(Object property) {
        PlayerProfile.Property prop = new PlayerProfile.Property();

        String name = mpname.get(property), value = mpvalue.get(property), signature = mpsignature.get(property);

        pname.set(property, name);
        pvalue.set(property, value);
        psignature.set(propertyCons, signature);

        return prop;
    }
}
