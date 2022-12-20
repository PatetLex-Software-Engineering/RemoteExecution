package com.patetlex.remoteexecution.vcs;

import com.patetlex.displayphoenix.Application;
import com.patetlex.displayphoenix.file.Data;
import com.patetlex.displayphoenix.interfaces.FileIteration;
import com.patetlex.displayphoenix.system.web.DeviceConnection;
import com.patetlex.displayphoenix.system.web.WebConnection;
import com.patetlex.displayphoenix.ui.ApplicationFrame;
import com.patetlex.remoteexecution.ui.ClientUI;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VersionControl {

    public static int PORT = DeviceConnection.getDefaultPort();

    public static void handleMessageFromServer(byte[] data) {
        String s = new String(data);
        System.out.println("Packet from Server: " + s);
        String[] r = s.split("-");
        if (r.length > 1) {
            String address = r[1];
            String event = r[0];
            if (event.equalsIgnoreCase("close")) {
                ApplicationFrame frame = ClientUI.getOpenClient(address);
                if (frame != null) {
                    frame.dispose();
                }
            } else if (event.equalsIgnoreCase("currentFiles")) {
                JList<String> list = ClientUI.getFileList(address);
                if (list != null) {
                    ((DefaultListModel<String>) list.getModel()).clear();
                    for (int i = 2; i < r.length; i++) {
                        ((DefaultListModel<String>) list.getModel()).addElement(r[i]);
                    }
                }
            }
        }

    }

    public static void handleMessageFromClient(byte[] data) {
        String s = new String(data);
        System.out.println("Packet from Client: " + s);
        String[] r = s.split("-");
        if (r.length > 2) {
            String event = r[0];
            String password = r[1];
            if (password == null || password.isEmpty())
                password = "NOPASS";
            String ip = r[2];
            String message = r.length > 3 ? r[3] : "";
            String serverPassword = Data.has("serverPassword") ? String.valueOf(Data.get("serverPassword")) : null;
            if (event.equalsIgnoreCase("sentFile")) {
                ExecutablePacket packet = ExecutablePacket.read(message);
                if (packet != null) {
                    if (!password.equals(serverPassword)) {
                        packet.getExecutable().delete();
                    }
                    StringBuilder response = new StringBuilder();
                    response.append("currentFiles").append("-").append(WebConnection.getClientIp());
                    Data.forCachedFile("\\exe\\", new FileIteration() {
                        @Override
                        public void iterate(File file) {
                            response.append("-").append(file.getName());
                        }
                    });
                    DeviceConnection.sendData(ip, response.toString().getBytes());
                }
            } else if (event.equalsIgnoreCase("executeFile")) {
                if (password.equals(serverPassword)) {
                    Data.forCachedFile("\\exe\\", new FileIteration() {
                        @Override
                        public void iterate(File file) {
                            if (file.getName().equalsIgnoreCase(message)) {
                                try {
                                    Application.getSystemProcessor().executeFile(file);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    });
                }
            } else if (event.equalsIgnoreCase("clearProcesses")) {
                if (password.equals(serverPassword)) {
                    Application.getSystemProcessor().clear();
                }
            } else if (event.equalsIgnoreCase("pullFiles")) {
                if (password.equals(serverPassword)) {
                    StringBuilder response = new StringBuilder();
                    response.append("currentFiles").append("-").append(WebConnection.getClientIp());
                    Data.forCachedFile("\\exe\\", new FileIteration() {
                        @Override
                        public void iterate(File file) {
                            response.append("-").append(file.getName());
                        }
                    });
                    DeviceConnection.sendData(ip, response.toString().getBytes());
                }
            } else if (event.equalsIgnoreCase("deleteFile")) {
                if (password.equals(serverPassword)) {
                    Application.getSystemProcessor().clear();
                    Data.forCachedFile("\\exe\\", new FileIteration() {
                        @Override
                        public void iterate(File file) {
                            if (file.getName().equalsIgnoreCase(message)) {
                                file.delete();
                            }
                        }
                    });
                    StringBuilder response = new StringBuilder();
                    response.append("currentFiles").append("-").append(WebConnection.getClientIp());
                    Data.forCachedFile("\\exe\\", new FileIteration() {
                        @Override
                        public void iterate(File file) {
                            response.append("-").append(file.getName());
                        }
                    });
                    DeviceConnection.sendData(ip, response.toString().getBytes());
                }
            }
        }
    }
}
