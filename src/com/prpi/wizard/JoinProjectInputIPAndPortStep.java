package com.prpi.wizard;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.prpi.PrPiApplicationComponent;
import com.prpi.network.PrPiClient;
import com.prpi.network.PrPiServer;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;

public class JoinProjectInputIPAndPortStep extends ModuleWizardStep {

    private static final Logger logger = Logger.getLogger(JoinProjectInputIPAndPortStep.class);
    private static final int TextFieldHeight = 25;

    private JTextField ipTextField = new JTextField();
    private JTextField portTextField = new JTextField(Integer.toString(PrPiServer.DEFAULT_PORT));
    private JLabel connectionResultTextField = new JLabel();

    private JoinProjectBuilder builder;
    private WizardContext context;

    private JoinProjectInputIPAndPortStep() {
        super();
    }

    public JoinProjectInputIPAndPortStep(JoinProjectBuilder joinProjectBuilder, WizardContext wizardContext) {
        this();
        this.builder = joinProjectBuilder;
        this.context = wizardContext;
    }

    @Override
    public JComponent getComponent() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createIpAndPortLayout(), BorderLayout.NORTH);

        return mainPanel;
    }

    private JComponent createIpAndPortLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel ipPanel = new JPanel(new FlowLayout());
        ipPanel.add(new JLabel("Hostname / IP :"));
        ipPanel.add(ipTextField);
        ipTextField.setPreferredSize(new Dimension(200, TextFieldHeight));

        JPanel portPanel = new JPanel(new FlowLayout());
        portPanel.add(new JLabel("Port :"));
        portPanel.add(portTextField);
        portPanel.add(new JLabel(String.format("(default port : %d)", PrPiServer.DEFAULT_PORT)));
        portTextField.setPreferredSize(new Dimension(70, TextFieldHeight));

        panel.add(ipPanel);
        panel.add(portPanel);

        // TODO -> uncomment this line
        //panel.add(createTestConnectionButton());

        return panel;
    }

    private JComponent createTestConnectionButton() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JButton testConnectionButton = new JButton("Test connection");
        testConnectionButton.addActionListener(new TestConnectionActionListener());

        panel.add(testConnectionButton);
        panel.add(connectionResultTextField);

        return panel;
    }

    private class TestConnectionActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {

            connectionResultTextField.setForeground(Color.BLACK);
            connectionResultTextField.setText("Processing...");

            boolean couldConnect = false;

            String ipAddress = checkAndGetHostnameImput();
            int port = checkAndGetPortImput();
            if (ipAddress != null && port != 0) {
                //couldConnect = PrPiClient.testConnection(ipAddress, port);
            }

            String msgText;
            Color color;
            logger.debug("Connection status : " + couldConnect);
            if (couldConnect) {
                msgText = "Connection working!";
                color = Color.GREEN;
            } else {
                msgText = String.format("Could not reach %s:%d", ipAddress, port);
                color = Color.RED;
            }

            connectionResultTextField.setText(msgText);
            connectionResultTextField.setForeground(color);
        }
    }

    @Override
    public void updateDataModel() {

    }

    @Override
    public boolean validate() throws ConfigurationException {
        return this.checkHostnameAndPortImputs();
    }

    private boolean checkHostnameAndPortImputs() {
        boolean validate = false;
        String ipAddress = checkAndGetHostnameImput();
        if (ipAddress != null) {
            int port = checkAndGetPortImput();
            if (port != 0) {
                validate = true;
                logger.debug(String.format("Writin ip %s and port %d into module properties", ipAddress, port));
            }
        }
        return validate;
    }

    private int checkAndGetPortImput() {
        int port = 0;
        try {
            port = Integer.parseInt(portTextField.getText());
        } catch (NumberFormatException e) {
            Messages.showWarningDialog("The port is not correctly formed.", "PrPi Warning - Port Problem");
        }
        return port;
    }

    private @Nullable String checkAndGetHostnameImput() {
        String ipAddress = ipTextField.getText();
        try {
            if (ipAddress.isEmpty() || !InetAddress.getByName(ipAddress).isReachable(10000)) {
                ipAddress = null;
                Messages.showWarningDialog("No hostname, or not reachable!", "PrPi Warning - Hostname Problem");
            }
        } catch (IOException e) {
            ipAddress = null;
            logger.error("Network error", e);
        }
        return ipAddress;
    }

    @Override
    public void onWizardFinished() throws CommitStepException {
        PrPiApplicationComponent app = PrPiApplicationComponent.getPrPiAppComp(null);
        app.setClientThread(new PrPiClient(this.checkAndGetHostnameImput(), this.checkAndGetPortImput()));
    }
}