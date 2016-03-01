package com.prpi.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by elmhaidara on 01/03/2016.
 */
public class HelloWorld extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        System.out.println("clicked");
    }
}
