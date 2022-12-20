package com.patetlex.remoteexecution.ui;

import com.patetlex.displayphoenix.Application;
import com.patetlex.displayphoenix.lang.Localizer;
import com.patetlex.displayphoenix.system.web.DeviceConnection;
import com.patetlex.displayphoenix.system.web.WebConnection;
import com.patetlex.displayphoenix.ui.ApplicationFrame;
import com.patetlex.displayphoenix.ui.widget.RoundedButton;
import com.patetlex.displayphoenix.ui.widget.TextField;
import com.patetlex.displayphoenix.util.PanelHelper;
import com.patetlex.remoteexecution.RemoteExecution;
import com.patetlex.remoteexecution.vcs.VersionControl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.function.Consumer;

public class GUI {
    public static void open() {
        Application.openWindow((parentFrame) -> {
            JLabel deviceId = new JLabel(WebConnection.getClientIp());
            deviceId.setFont(deviceId.getFont().deriveFont(50F));

            TextField connectToServer = new TextField(Localizer.translate("field.connect.text", VersionControl.PORT));
            connectToServer.setPreferredSize(new Dimension(450, 35));
            connectToServer.setHorizontalAlignment(SwingConstants.CENTER);
            connectToServer.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    String[] r = connectToServer.getText().split(":");
                    if (r.length == 2) {
                        try {
                            String clientHostAddress = DeviceConnection.connectTo(r[0], Integer.parseInt(r[1]), new Consumer<byte[]>() {
                                @Override
                                public void accept(byte[] bytes) {
                                    VersionControl.handleMessageFromServer(bytes);
                                }
                            });
                            ClientUI.openConnectedServerPanel(clientHostAddress, r[0], Integer.parseInt(r[1]));
                        } catch (IOException e) {
                            parentFrame.addTopLayer(Application.getPromptPanel(Localizer.translate("error.connect.text", connectToServer.getText()), new Application.PromptedButton(Localizer.translate("okay"), new MouseAdapter() {
                                @Override
                                public void mouseClicked(MouseEvent e) {
                                    parentFrame.closeTopLayer();
                                }
                            }), new Application.PromptedButton(Localizer.translate("button.view_error_log.text"), new MouseAdapter() {
                                @Override
                                public void mouseClicked(MouseEvent ev) {
                                    Application.openPaste(e.toString());
                                }
                            })));
                        } catch (NumberFormatException e) {
                            parentFrame.addTopLayer(Application.getPromptPanel(Localizer.translate("error.port_integer.text"), new Application.PromptedButton(Localizer.translate("okay"), new MouseAdapter() {
                                @Override
                                public void mouseClicked(MouseEvent e) {
                                    parentFrame.closeTopLayer();
                                }
                            })));
                        }
                    } else {
                        parentFrame.addTopLayer(Application.getPromptPanel(Localizer.translate("error.address_regex.text"), new Application.PromptedButton(Localizer.translate("okay"), new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                parentFrame.closeTopLayer();
                            }
                        })));
                    }
                }
            });
            JPanel connectionPanel = PanelHelper.northAndCenterElements(PanelHelper.createSeperator(Application.getTheme().getColorTheme().getAccentColor(), 5, 5), PanelHelper.join(connectToServer));

            if (!DeviceConnection.hasServer()) {
                RoundedButton startServer = new RoundedButton(Localizer.translate("button.start_server.text"));
                startServer.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        super.mouseClicked(e);
                        try {
                            DeviceConnection.startServer(VersionControl.PORT, new Consumer<byte[]>() {
                                @Override
                                public void accept(byte[] bytes) {
                                    VersionControl.handleMessageFromClient(bytes);
                                }
                            });
                            ServerUI.openServerPanel(WebConnection.getClientIp(), VersionControl.PORT);
                            parentFrame.open((ApplicationFrame) parentFrame.clone());
                            parentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            parentFrame.dispose();
                        } catch (IOException ex) {
                            parentFrame.addTopLayer(Application.getPromptPanel(Localizer.translate("error.start_server.text"), new Application.PromptedButton(Localizer.translate("okay"), new MouseAdapter() {
                                @Override
                                public void mouseClicked(MouseEvent e) {
                                    parentFrame.closeTopLayer();
                                }
                            })));
                        }
                    }
                });
                connectionPanel = PanelHelper.northAndCenterElements(connectionPanel, PanelHelper.northAndCenterElements(PanelHelper.createSeperator(Application.getTheme().getColorTheme().getAccentColor(), 5, 5), PanelHelper.join(startServer)));
            }

            JPanel leftPanel = PanelHelper.northAndCenterElements(PanelHelper.join(deviceId), connectionPanel);
            JPanel rightPanel = PanelHelper.northAndCenterElements(PanelHelper.join(Application.getLocalChangePanel()), PanelHelper.join(Application.getColorThemeChangePanel(RemoteExecution.THEMES)));
            parentFrame.add(PanelHelper.centerAndEastElements(leftPanel, rightPanel));
            rightPanel.setOpaque(true);
            rightPanel.setBackground(Application.getTheme().getColorTheme().getSecondaryColor());
        });
    }
}
