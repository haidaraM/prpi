package com.prpi.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.prpi.ProjectComponent;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.net.InetSocketAddress;
import java.util.Collection;

public class EditProjectConfiguration extends AnAction {

    private static final Logger logger = Logger.getLogger(EditProjectConfiguration.class);

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        logger.trace("EditProjectConfiguration actionPerformed begin");

        ProjectComponent projectComponent = anActionEvent.getProject().getComponent(ProjectComponent.class);

        if (projectComponent != null && projectComponent.isHosting()) {
            DialogBuilder dialogBuilder = new DialogBuilder(anActionEvent.getProject());
            dialogBuilder.removeAllActions();
            dialogBuilder.addOkAction();
            dialogBuilder.setTitle("Client of this shared project");

            String[] columnNames = {"Machine Name", "Host Name (port)"};

            Collection<Pair<String,InetSocketAddress>> clientInfoList = projectComponent.getServer().getClientsInfo();
            Object[][] data = new Object[clientInfoList.size()][2];
            int row = 0;
            for (Pair<String,InetSocketAddress> clientInfo : clientInfoList) {
                data[row][0] = clientInfo.first;
                data[row][1] = clientInfo.second.getHostName() + " (" + clientInfo.second.getPort()+")";
                row++;
            }

            TableModel model = new DefaultTableModel(data, columnNames);
            JBTable table = new JBTable(model);
            table.setPreferredScrollableViewportSize(new Dimension(500, 300));
            table.setFillsViewportHeight(true);
            JScrollPane scrollTable = new JBScrollPane(table);

            dialogBuilder.setCenterPanel(scrollTable);
            dialogBuilder.show();
        }

        logger.trace("EditProjectConfiguration actionPerformed end");
    }

    @Override
    public void update(AnActionEvent anActionEvent) {
        super.update(anActionEvent);
        boolean isHosting;
        Project p = anActionEvent.getProject();
        if (p == null) {
            isHosting = false;
        } else {
            ProjectComponent pc = p.getComponent(ProjectComponent.class);
            isHosting = (pc != null && pc.isHosting());
        }

        anActionEvent.getPresentation().setEnabled(isHosting);
    }
}
