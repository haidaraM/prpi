package com.prpi.wizard;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.prpi.network.NetworkManager;
import com.prpi.network.PrPiClientThread;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.Future;


public class JoinProjectInputIPAndPortStep extends ModuleWizardStep {
    private static Logger logger = Logger.getLogger(JoinProjectInputIPAndPortStep.class);

    private JTextField ipTextField = new JTextField();
    private JTextField portTextField = new JTextField(Integer.toString(NetworkManager.DEFAULT_PORT));
    private JTextField connectionResultTextField = new JTextField();

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
        ipPanel.add(new JLabel("IP :"));
        ipPanel.add(ipTextField);
        ipTextField.setPreferredSize(new Dimension(200, 23));

        JPanel portPanel = new JPanel(new FlowLayout());
        portPanel.add(new JLabel("Port :"));
        portPanel.add(portTextField);
        portPanel.add(new JLabel(String.format("(default port : %d)", NetworkManager.DEFAULT_PORT)));
        portTextField.setPreferredSize(new Dimension(70, 23));

        panel.add(ipPanel);
        panel.add(portPanel);
        panel.add(createTestConnectionButton());

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
            String ipAddress = ipTextField.getText();
            int port;
            try {
                port = Integer.parseInt(portTextField.getText());
            } catch (NumberFormatException ex) {
                port = NetworkManager.DEFAULT_PORT;
            }

            Future<Boolean> couldConnectFuture = PrPiClientThread.testConnection(ipAddress, port);
            connectionResultTextField.setForeground(Color.BLACK);
            connectionResultTextField.setText("Processing...");

            String msgText;
            Color color;
            boolean couldConnect;
            try {
                couldConnect = couldConnectFuture.get();
            } catch (Exception ex) {
                couldConnect = false;
            }

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
        // TODO verifications + refus + message d'erreur
        String ipAddress = ipTextField.getText();
        int port;
        try {
            port = Integer.parseInt(portTextField.getText());
        } catch (NumberFormatException e) {
            port = NetworkManager.DEFAULT_PORT;
        }

        if (StringUtils.isNotEmpty(ipAddress)) {
            try {
                NetworkManager.addClient(new PrPiClientThread(ipAddress, port));
            } catch (IOException ex) {
                //Messages.showErrorDialog((Project)null, ex.getMessage(), "Error Starting Server Thread");
                logger.error(String.format("Could not connect to %s:d", ipAddress, port));
            }
        }
    }
}