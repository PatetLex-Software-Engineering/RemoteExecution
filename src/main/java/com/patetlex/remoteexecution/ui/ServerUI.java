package com.patetlex.remoteexecution.ui;

import com.patetlex.displayphoenix.Application;
import com.patetlex.displayphoenix.file.Data;
import com.patetlex.displayphoenix.lang.Localizer;
import com.patetlex.displayphoenix.system.web.DeviceConnection;
import com.patetlex.displayphoenix.system.web.WebConnection;
import com.patetlex.displayphoenix.ui.widget.FadeOnHoverWidget;
import com.patetlex.displayphoenix.ui.widget.TextField;
import com.patetlex.displayphoenix.util.ComponentHelper;
import com.patetlex.displayphoenix.util.ImageHelper;
import com.patetlex.displayphoenix.util.PanelHelper;
import com.patetlex.displayphoenix.util.StringHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.Socket;

public class ServerUI {
    public static void openServerPanel(String address, int port) {
        Application.openWindow(JFrame.DISPOSE_ON_CLOSE, parentFrame -> {
            JLabel serverIP = new JLabel(address);
            serverIP.setFont(serverIP.getFont().deriveFont(50F));
            JLabel atPort = new JLabel(Localizer.translate("field.server_port.text", port));
            serverIP.setFont(atPort.getFont().deriveFont(25F));

            TextField passField = new TextField(Data.has("serverPassword") ? String.valueOf(Data.get("serverPassword")) : Localizer.translate("field.server_password.text"));
            passField.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    String pass = StringHelper.condense(passField.getText());
                    Data.store("serverPassword", pass);
                }
            });
            passField.setPreferredSize(new Dimension(350, 35));
            passField.setHorizontalAlignment(SwingConstants.CENTER);

            JList<String> clientsList = ComponentHelper.createJList(new Renderer());
            FadeOnHoverWidget removeButton = new FadeOnHoverWidget(new ImageIcon(ImageHelper.rotate(ImageHelper.overlay(ImageHelper.getImage(Application.getTheme().getWidgetStyle().getName() + "_plus").getImage(), Application.getTheme().getColorTheme().getAccentColor(), 1F), 45)), new ImageIcon(ImageHelper.rotate(ImageHelper.overlay(ImageHelper.getImage(Application.getTheme().getWidgetStyle().getName() + "_plus").getImage(), Color.RED, 1F), 45)), 0.01F);
            removeButton.setPreferredSize(new Dimension(25, 25));
            removeButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (clientsList.getSelectedValue() != null) {
                        for (String address0 : DeviceConnection.CONNECTED_CLIENTS.keySet()) {
                            if (address0.equals(clientsList.getSelectedValue())) {
                                DeviceConnection.sendData(address0, ("close-" + WebConnection.getClientIp()).getBytes());
                                try {
                                    DeviceConnection.CONNECTED_CLIENTS.get(address0).close();
                                } catch (IOException ex) {
                                    parentFrame.addTopLayer(Application.getPromptPanel(Localizer.translate("error.unable_close_client.text", address0), new Application.PromptedButton(Localizer.translate("okay"), new MouseAdapter() {
                                        @Override
                                        public void mouseClicked(MouseEvent e) {
                                            parentFrame.closeTopLayer();
                                        }
                                    })));
                                }
                                break;
                            }
                        }
                    }
                }
            });
            JPanel clientsPanel = PanelHelper.northAndCenterElements(PanelHelper.join(removeButton), ComponentHelper.addScrollPane(clientsList));

            new Thread(() -> {
                while (DeviceConnection.hasServer()) {
                    try {
                        Thread.sleep(3000);
                        ((DefaultListModel<String>) clientsList.getModel()).clear();
                        for (String address0 : DeviceConnection.CONNECTED_CLIENTS.keySet()) {
                            if (address0 != null) {
                                Socket socket = DeviceConnection.CONNECTED_CLIENTS.get(address0);
                                if (!socket.isClosed() && socket.isConnected())
                                    ((DefaultListModel<String>) clientsList.getModel()).addElement(address0);
                            }
                        }
                    } catch (InterruptedException e) {

                    }
                }
            }).start();

            parentFrame.add(PanelHelper.northAndCenterElements(PanelHelper.northAndCenterElements(PanelHelper.northAndCenterElements(PanelHelper.join(serverIP), PanelHelper.join(atPort)), PanelHelper.join(passField)), PanelHelper.northAndCenterElements(PanelHelper.createVerticalSeperator(Application.getTheme().getColorTheme().getAccentColor(), 5, 5), clientsPanel)));
        },Application.getTheme().getWidth() / 2, Math.max(Application.getTheme().getHeight() - 100, 500));
    }

    private static class Renderer extends JLabel implements ListCellRenderer<String> {
        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            setFont(Application.getTheme().getFont());
            setForeground(Application.getTheme().getColorTheme().getTextColor());
            setOpaque(isSelected);
            setBackground(Application.getTheme().getColorTheme().getSecondaryColor());
            setText(value);
            setHorizontalAlignment(SwingConstants.CENTER);
            return this;
        }
    }
}
