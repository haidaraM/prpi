package com.prpi.wizard;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import com.prpi.network.PrPiServer;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;

public class JoinProjectInputIPAndPortStep extends ModuleWizardStep {

    private static final Logger logger = Logger.getLogger(JoinProjectInputIPAndPortStep.class);
    private static final int TextFieldHeight = 25;

    private JTextField ipTextField = new JTextField();
    private JTextField portTextField = new JTextField(Integer.toString(PrPiServer.DEFAULT_PORT));

    @Override
    public JComponent getComponent() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createIpAndPortLayout(), BorderLayout.NORTH);

        return mainPanel;
    }

    @Override
    public void updateDataModel() {

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

        return panel;
    }

    @Override
    public boolean validate() throws ConfigurationException {
        boolean validate = false;
        try {
            String ipAddress = ipTextField.getText();
            if (!ipAddress.isEmpty()) {
                int port = Integer.parseInt(portTextField.getText());

                // TODO make a better check (with port and prpi plugin version)
                validate = InetAddress.getByName(ipAddress).isReachable(10000);
                logger.debug(String.format("Writin ip %s and port %d into module properties", ipAddress, port));
            } else {
                Messages.showWarningDialog("No hostname enter!", "PrPi Warning - No Hostname");
            }
        } catch (NumberFormatException e) {
            logger.debug(e);
            Messages.showWarningDialog("The hostname/ip address or port are not correctly formed.", "PrPi Warning - Problem In Host Or Port");
        } catch (IOException e) {
            logger.debug(e);
            Messages.showWarningDialog("The hostname is not reachable.", "PrPi Warning - Host Not Reachable");
        }
        return validate;
    }

}