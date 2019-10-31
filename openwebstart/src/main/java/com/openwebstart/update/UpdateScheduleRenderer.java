package com.openwebstart.update;

import com.install4j.api.update.UpdateSchedule;
import net.adoptopenjdk.icedteaweb.i18n.Translator;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.Component;

public class UpdateScheduleRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
        final Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if(value instanceof UpdateSchedule) {
            UpdateSchedule updateSchedule = (UpdateSchedule) value;
            if(result instanceof JLabel) {
                JLabel label = (JLabel) result;
                label.setText(Translator.getInstance().translate("updatesPanel.updateStrategy." + updateSchedule.getId()));
            }
        }
        return result;
    }
}
