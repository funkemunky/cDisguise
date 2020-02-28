package dev.brighten.hide.utils.menu.mask;

import dev.brighten.hide.utils.menu.Menu;
import dev.brighten.hide.utils.menu.button.Button;

/**
 * @author Missionary (missionarymc@gmail.com)
 * @since 5/17/2018
 */
public interface Mask {

    Mask setButton(char key, Button button);

    Mask setMaskPattern(String... maskPattern);

    void applyTo(Menu menu);
}
