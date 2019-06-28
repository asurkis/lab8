package client;

import javax.swing.*;
import java.util.Locale;

public class LangSelector extends JComboBox<Object> {
    private Main main;

    private static class LocaleObject {
        Locale locale;

        LocaleObject(String tag) {
            locale = Locale.forLanguageTag(tag);
        }

        @Override
        public String toString() {
            return locale.getDisplayLanguage(locale);
        }
    }

    public LangSelector(Main main) {
        super(new Object[] {
                new LocaleObject("ru-RU"),
                new LocaleObject("is-IS"),
                new LocaleObject("hr-HR"),
                new LocaleObject("en-AU"),
        });

        addItemListener(e -> main.updateLocale(((LocaleObject) e.getItem()).locale));
    }
}
