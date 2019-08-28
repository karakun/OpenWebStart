package com.openwebstart.jvm.ui.sample;

import com.openwebstart.jvm.JavaRuntimeSelector;
import com.openwebstart.jvm.LocalRuntimeManager;
import com.openwebstart.jvm.RuntimeManagerConfig;
import com.openwebstart.jvm.json.JsonHandler;
import com.openwebstart.jvm.json.RemoteRuntimeList;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import com.openwebstart.jvm.ui.RuntimeManagerPanel;
import com.openwebstart.jvm.ui.dialogs.AskForRuntimeUpdateDialog;
import com.openwebstart.jvm.ui.dialogs.ErrorDialog;
import com.openwebstart.jvm.ui.dialogs.RuntimeDownloadDialog;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import spark.Spark;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

import static com.openwebstart.jvm.os.OperationSystem.LINUX64;
import static com.openwebstart.jvm.os.OperationSystem.MAC64;
import static com.openwebstart.jvm.os.OperationSystem.WIN64;
import static com.openwebstart.jvm.runtimes.Vendor.ANY_VENDOR;

public class JvmManagerDemo {

    public static void main(String[] args) throws Exception {

        RuntimeManagerConfig.setSupportedVersionRange(VersionString.fromString("1.8*"));
        RuntimeManagerConfig.setDefaultRemoteEndpoint(new URI("http://localhost:8090/jvms"));
        RuntimeManagerConfig.setNonDefaultServerAllowed(true);
        RuntimeManagerConfig.setDefaultVendor(ANY_VENDOR.getName());
        RuntimeManagerConfig.setNonDefaultVendorsAllowed(true);

        JavaRuntimeSelector.setDownloadHandler(RuntimeDownloadDialog::showDownloadDialog);
        JavaRuntimeSelector.setAskForUpdateFunction(AskForRuntimeUpdateDialog::askForUpdate);

        LocalRuntimeManager.getInstance().loadRuntimes();

        SwingUtilities.invokeLater(() -> {
            try {
                startServer();
                showManagerWindow();
                showDummyRequestWindow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void startServer() throws Exception {

        final List<RemoteJavaRuntime> runtimes = new CopyOnWriteArrayList<>();
        final URL theOneAndOnlyJdkZip = new URL("http://localhost:8090/jvms/jdk.zip");

        for (OperationSystem os : Arrays.asList(MAC64, WIN64, LINUX64)) {
            runtimes.add(new RemoteJavaRuntime("1.8.145", os, "adopt", "4711", theOneAndOnlyJdkZip));
            runtimes.add(new RemoteJavaRuntime("1.8.220", os, "adopt", "471sddw1", theOneAndOnlyJdkZip));
            runtimes.add(new RemoteJavaRuntime("1.8.224", os, "adopt", "471sddw1", theOneAndOnlyJdkZip));

            runtimes.add(new RemoteJavaRuntime("1.8.146", os, "oracle", "4711", theOneAndOnlyJdkZip));
            runtimes.add(new RemoteJavaRuntime("1.8.221", os, "oracle", "471sddw1", theOneAndOnlyJdkZip));
            runtimes.add(new RemoteJavaRuntime("1.8.225", os, "oracle", "471sddw1", theOneAndOnlyJdkZip));

            runtimes.add(new RemoteJavaRuntime("11.0.1", os, "adopt", "471sddw1", theOneAndOnlyJdkZip));

            runtimes.add(new RemoteJavaRuntime("11.0.2", os, "oracle", "471sddw1", theOneAndOnlyJdkZip));
        }

        final JPanel serverRuntimePanel = new JPanel();
        serverRuntimePanel.setLayout(new GridLayout(0, 1, 12, 12));
        serverRuntimePanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        runtimes.forEach(r -> {
            final JCheckBox checkBox = new JCheckBox(r.getVersion() + "-" + r.getVendor() + "-" + r.getOperationSystem().getName());
            checkBox.addItemListener(e -> {
                if(checkBox.isSelected()) {
                    if(!runtimes.contains(r)) {
                        runtimes.add(r);
                    }
                } else {
                    runtimes.remove(r);
                }
            });
            serverRuntimePanel.add(checkBox);
            checkBox.setSelected(true);
        });
        final JFrame frame = new JFrame("Server runtimes");
        frame.add(serverRuntimePanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        Spark.port(8090);
        Spark.staticFileLocation("/public");
        Spark.get("/jvms", ((request, response) -> {
            try {
                final RemoteRuntimeList list = new RemoteRuntimeList(runtimes, 5_000);
                return JsonHandler.getInstance().toJson(list);
            } catch (final Exception e) {
                e.printStackTrace();
                throw e;
            }
        }));


    }

    private static void showManagerWindow() {
        final JFrame frame = new JFrame("JVM Manager");
        final RuntimeManagerPanel panel = new RuntimeManagerPanel();
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void showDummyRequestWindow() {
        final JFrame frame = new JFrame("Request Version");

        final JButton requestButton = new JButton("Request");
        final JTextField requestedVersionField = new JTextField();
        final JTextField requestedVendorField = new JTextField("*");
        final JTextField requestedEndpointField = new JTextField("http://localhost:8090/jvms");
        final JLabel responseVersionLabel = new JLabel("XXXXXX");
        final JLabel responseVendorLabel = new JLabel("XXXXXX");
        final JLabel responseOsLabel = new JLabel("XXXXXX");
        final JLabel responsePathLabel = new JLabel("XXXXXX");
        final JLabel responseActiveLabel = new JLabel("XXXXXX");
        final JLabel responseManagedLabel = new JLabel("XXXXXX");


        requestButton.addActionListener(event -> Executors.newSingleThreadExecutor().execute(() -> {
            try {
                final VersionString version = VersionString.fromString(requestedVersionField.getText());
                final String vendor = requestedVendorField.getText();
                final URL serverEndpoint = new URL(requestedEndpointField.getText());
                final LocalJavaRuntime runtime = JavaRuntimeSelector.getInstance().getRuntime(version, vendor, serverEndpoint);

                SwingUtilities.invokeLater(() -> {
                    responseVersionLabel.setText(runtime.getVersion().toString());
                    responseVendorLabel.setText(runtime.getVendor().getName());
                    responseOsLabel.setText(runtime.getOperationSystem().getName());
                    responsePathLabel.setText(runtime.getJavaHome().toString());
                    responseActiveLabel.setText(runtime.isActive() + "");
                    responseManagedLabel.setText(runtime.isManaged() + "");
                });
            } catch (Exception e) {
                new ErrorDialog("Error while getting matching runtime", e).showAndWait();
            }
        }));

        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0, 2, 6, 2));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        mainPanel.add(new JLabel("Requested Version:"));
        mainPanel.add(requestedVersionField);
        mainPanel.add(new JLabel("Requested Vendor:"));
        mainPanel.add(requestedVendorField);
        mainPanel.add(new JLabel("Requested Endpoint:"));
        mainPanel.add(requestedEndpointField);

        mainPanel.add(new JPanel());
        mainPanel.add(requestButton);

        final JPanel divider = new JPanel();
        divider.setPreferredSize(new Dimension(12, 12));
        divider.setMinimumSize(divider.getPreferredSize());
        divider.setMaximumSize(divider.getPreferredSize());

        mainPanel.add(new JPanel());
        mainPanel.add(divider);

        mainPanel.add(new JLabel("Version:"));
        mainPanel.add(responseVersionLabel);
        mainPanel.add(new JLabel("Vendor:"));
        mainPanel.add(responseVendorLabel);
        mainPanel.add(new JLabel("OS:"));
        mainPanel.add(responseOsLabel);
        mainPanel.add(new JLabel("Path:"));
        mainPanel.add(responsePathLabel);
        mainPanel.add(new JLabel("Active:"));
        mainPanel.add(responseActiveLabel);
        mainPanel.add(new JLabel("Managed:"));
        mainPanel.add(responseManagedLabel);

        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
