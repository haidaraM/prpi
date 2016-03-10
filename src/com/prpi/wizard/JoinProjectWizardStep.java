package com.prpi.wizard;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.prpi.actions.JoinProject;
import com.prpi.networkv2.NetworkManager;
import com.prpi.networkv2.PrPiClient;
import com.prpi.networkv2.PrPiClientThread;


import javax.swing.*;
import java.awt.*;
import java.io.IOException;


public class JoinProjectWizardStep extends ModuleWizardStep {
    JTextField jtf;

    @Override
    public JComponent getComponent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("New Shared Project"),  BorderLayout.NORTH);
        JPanel ipPanel = new JPanel(new FlowLayout());
        panel.add(ipPanel, BorderLayout.CENTER);
        jtf = new JTextField("127.0.0.1");
        ipPanel.add(new JLabel("IP : "));
        jtf.setPreferredSize(new Dimension(200, 22));
        ipPanel.add(jtf);
        return panel;
    }

    @Override
    public void updateDataModel() {
        String ipAddress = jtf.getText();
        if(ipAddress != null){
            try {
                NetworkManager.addClient(new PrPiClientThread(ipAddress, NetworkManager.DEFAULT_PORT));
            } catch (IOException ex) {
                Messages.showErrorDialog((Project)null, ex.getMessage(), "Error Starting Server Thread");
            }
        }
    }
}