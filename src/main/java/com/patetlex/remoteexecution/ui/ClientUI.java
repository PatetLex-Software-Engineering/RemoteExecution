package com.patetlex.remoteexecution.ui;

import com.patetlex.displayphoenix.Application;
import com.patetlex.displayphoenix.file.Data;
import com.patetlex.displayphoenix.file.DetailedFile;
import com.patetlex.displayphoenix.file.indexly.Indexly;
import com.patetlex.displayphoenix.lang.Localizer;
import com.patetlex.displayphoenix.system.web.DeviceConnection;
import com.patetlex.displayphoenix.system.web.WebConnection;
import com.patetlex.displayphoenix.ui.ApplicationFrame;
import com.patetlex.displayphoenix.ui.widget.FadeOnHoverWidget;
import com.patetlex.displayphoenix.ui.widget.RoundedButton;
import com.patetlex.displayphoenix.ui.widget.TextField;
import com.patetlex.displayphoenix.util.ComponentHelper;
import com.patetlex.displayphoenix.util.ImageHelper;
import com.patetlex.displayphoenix.util.PanelHelper;
import com.patetlex.displayphoenix.util.StringHelper;
import com.patetlex.remoteexecution.vcs.ExecutablePacket;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ClientUI {

    private static final Map<String, ApplicationFrame> OPEN_CLIENTS = new HashMap<>();
    private static final Map<String, JList<String>> FILES_PER_ADDRESS = new HashMap<>();

    public static void openConnectedServerPanel(String clientHostAddress, String address, int port) {
        Application.openWindow(JFrame.DISPOSE_ON_CLOSE, parentFrame -> {
            OPEN_CLIENTS.put(address, parentFrame);
            JLabel serverIP = new JLabel(address);
            serverIP.setFont(serverIP.getFont().deriveFont(50F));
            JLabel atPort = new JLabel(Localizer.translate("field.connected_port.text", port));
            serverIP.setFont(atPort.getFont().deriveFont(25F));

            TextField password = new TextField(Data.has(address + "-password") ? String.valueOf(Data.get(address + "-password")) : Localizer.translate("field.password.text"));
            password.setPreferredSize(new Dimension(300, 35));
            password.setHorizontalAlignment(SwingConstants.CENTER);
            RoundedButton clearProcesses = new RoundedButton(Localizer.translate("button.clear_processes.text"));
            clearProcesses.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    String pass = StringHelper.condense(password.getText());
                    DeviceConnection.sendData(address, ("clearProcesses-" + pass + "-" + clientHostAddress).getBytes());
                }
            });
            RoundedButton selectExecutable = new RoundedButton(Localizer.translate("button.select_executable.text"));
            selectExecutable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Indexly.openFile(new Consumer<DetailedFile>() {
                        @Override
                        public void accept(DetailedFile detailedFile) {
                            String pass = StringHelper.condense(password.getText());
                            if (detailedFile.getFile().canExecute()) {
                                DeviceConnection.sendData(address, ("sentFile-" + pass + "-" + clientHostAddress + "-" + new ExecutablePacket(detailedFile.getFile()).getJson()).getBytes());
                                ApplicationFrame clone = (ApplicationFrame) parentFrame.clone();
                                parentFrame.open(clone);
                                if (OPEN_CLIENTS.containsKey(address))
                                    OPEN_CLIENTS.remove(address);
                                OPEN_CLIENTS.put(address, clone);
                                parentFrame.dispose();
                            } else {
                                parentFrame.addTopLayer(Application.getPromptPanel(Localizer.translate("error.not_execute.text"), new Application.PromptedButton(Localizer.translate("okay"), new MouseAdapter() {
                                    @Override
                                    public void mouseClicked(MouseEvent e) {
                                        parentFrame.closeTopLayer();
                                    }
                                })));
                            }
                        }
                    });
                }
            });
            new Thread(() -> {
                while (DeviceConnection.isConnected(address)) {
                    try {
                        Thread.sleep(10000);
                        String pass = StringHelper.condense(password.getText());
                        Data.store(address + "-password", pass);
                        if (DeviceConnection.isConnected(address)) {
                            DeviceConnection.sendData(address, ("pullFiles-" + pass + "-" + clientHostAddress).getBytes());
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();

            JList<String> list = ComponentHelper.createJList(new Renderer());
            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        String pass = StringHelper.condense(password.getText());
                        if (list.getSelectedValue() != null) {
                            DeviceConnection.sendData(address, ("executeFile-" + pass + "-" + clientHostAddress + "-" + list.getSelectedValue()).getBytes());
                            parentFrame.addTopLayer(Application.getPromptPanel(Localizer.translate("prompt.executed_file.text", list.getSelectedValue()), new Application.PromptedButton(Localizer.translate("okay"), new MouseAdapter() {
                                @Override
                                public void mouseClicked(MouseEvent e) {
                                    parentFrame.closeTopLayer();
                                }
                            })));
                        }
                    }
                }
            });
            FadeOnHoverWidget removeButton = new FadeOnHoverWidget(new ImageIcon(ImageHelper.rotate(ImageHelper.overlay(ImageHelper.getImage(Application.getTheme().getWidgetStyle().getName() + "_plus").getImage(), Application.getTheme().getColorTheme().getAccentColor(), 1F), 45)), new ImageIcon(ImageHelper.rotate(ImageHelper.overlay(ImageHelper.getImage(Application.getTheme().getWidgetStyle().getName() + "_plus").getImage(), Color.RED, 1F), 45)), 0.01F);
            removeButton.setPreferredSize(new Dimension(25, 25));
            removeButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    String pass = StringHelper.condense(password.getText());
                    if (list.getSelectedValue() != null) {
                        DeviceConnection.sendData(address, ("deleteFile-" + pass + "-" + clientHostAddress + "-" + list.getSelectedValue()).getBytes());
                    }
                }
            });
            FILES_PER_ADDRESS.put(address, list);

            JPanel exeSelection = PanelHelper.join(PanelHelper.join(password), PanelHelper.join(clearProcesses), PanelHelper.join(selectExecutable));

            parentFrame.add(PanelHelper.northAndCenterElements(PanelHelper.northAndCenterElements(PanelHelper.join(serverIP), PanelHelper.join(atPort)), PanelHelper.northAndCenterElements(PanelHelper.createSeperator(Application.getTheme().getColorTheme().getAccentColor(), 5, 5), PanelHelper.northAndCenterElements(exeSelection, PanelHelper.northAndCenterElements(PanelHelper.join(removeButton), ComponentHelper.addScrollPane(list))))));
        }, Application.getTheme().getWidth() / 2, Math.max(Application.getTheme().getHeight() - 100, 500));
    }

    public static ApplicationFrame getOpenClient(String address) {
        for (String address0 : OPEN_CLIENTS.keySet()) {
            if (address0.equals(address))
                return OPEN_CLIENTS.get(address0);
            if (address0.equalsIgnoreCase("localhost")) {
                String clientIp = WebConnection.getClientIp();
                if (address.equalsIgnoreCase(clientIp)) {
                    return OPEN_CLIENTS.get(address0);
                }
            }
        }
        return null;
    }

    public static JList<String> getFileList(String address) {
        for (String address0 : FILES_PER_ADDRESS.keySet()) {
            if (address0.equals(address))
                return FILES_PER_ADDRESS.get(address0);
            if (address0.equalsIgnoreCase("localhost")) {
                String clientIp = WebConnection.getClientIp();
                if (address.equalsIgnoreCase(clientIp)) {
                    return FILES_PER_ADDRESS.get(address0);
                }
            }
        }
        return null;
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
