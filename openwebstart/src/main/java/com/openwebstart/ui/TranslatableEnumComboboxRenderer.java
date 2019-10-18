package com.openwebstart.ui;

import net.adoptopenjdk.icedteaweb.i18n.Translator;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.awt.Component;

public class TranslatableEnumComboboxRenderer<T extends Enum<T> & Translatable> extends JLabel implements ListCellRenderer<T> {

    private final Translator translator;

    public TranslatableEnumComboboxRenderer() {
        this.translator = Translator.getInstance();
        setOpaque(true);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
    }


    @Override
    public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected, boolean cellHasFocus) {
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setText(translator.translate(value.getTranslationKey()));

        return this;
    }
}
