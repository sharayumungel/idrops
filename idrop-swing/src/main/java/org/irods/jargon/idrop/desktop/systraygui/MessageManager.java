package org.irods.jargon.idrop.desktop.systraygui;

import java.awt.Component;

import javax.swing.JOptionPane;

/**
 * 
 * @author jdr0887
 *
 */
public class MessageManager {

    public static void showError(Component rootComponent, String message, String title) {
        JOptionPane.showMessageDialog(rootComponent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarning(Component rootComponent, String message, String title) {
        JOptionPane.showMessageDialog(rootComponent, message, title, JOptionPane.WARNING_MESSAGE);
    }

}