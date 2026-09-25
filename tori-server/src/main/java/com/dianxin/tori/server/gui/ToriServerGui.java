package com.dianxin.tori.server.gui;

import com.dianxin.tori.base.annotations.ReleasedSince;
import com.dianxin.tori.server.Main;
import com.formdev.flatlaf.FlatDarkLaf;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

@ReleasedSince("26.8.301")
public class ToriServerGui extends JFrame {
    private static ToriServerGui instance;
    private final JTextArea logArea;
    private final JTextField commandInput;
    private final JLabel memoryLabel;

    public ToriServerGui() {
        super("Tori Server Dashboard");
        instance = this;

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ignored) {}

        setSize(950, 600);
        setMinimumSize(new Dimension(700, 450));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(55, 55, 60)));

        JLabel titleLabel = new JLabel("⚡ Tori Multibot Server");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        memoryLabel = new JLabel("RAM: 0 MB / 0 MB");
        memoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        topPanel.add(titleLabel);
        topPanel.add(new JSeparator(JSeparator.VERTICAL));
        topPanel.add(memoryLabel);
        add(topPanel, BorderLayout.NORTH);

        // Center Panel: Log Console
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setBackground(new Color(24, 24, 27));
        logArea.setForeground(new Color(220, 220, 225));
        logArea.setMargin(new Insets(8, 10, 8, 10));

        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel: CLI Input
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 0));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 10));

        commandInput = new JTextField();
        commandInput.setFont(new Font("Consolas", Font.PLAIN, 13));
        commandInput.putClientProperty("JTextField.placeholderText", "Type command (ex: stop, reload, help)...");

        commandInput.addActionListener(e -> {
            String cmd = commandInput.getText().trim();
            if (!cmd.isEmpty()) {
                commandInput.setText("");
                new Thread(() -> {
                    try {
                        if (Main.getServer() != null && Main.getServer().getConsoleCommandManager() != null) {
                            Main.getServer().getConsoleCommandManager().dispatch(cmd);
                        }
                    } catch (Exception ex) {
                        System.err.println("Failed to dispatch command from GUI: " + ex.getMessage());
                    }
                }, "GUI-CommandDispatch-Thread").start();
            }
        });

        bottomPanel.add(commandInput, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // redirect System.out but keep output console terminal
        redirectSystemStreams();

        startMemoryUpdater();
    }

    public static void appendText(String text) {
        if (instance != null && instance.logArea != null) {
            SwingUtilities.invokeLater(() -> instance.logArea.append(text));
        }
    }

    private void redirectSystemStreams() {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        OutputStream outStream = new OutputStream() {
            @Override
            public void write(int b) {
                originalOut.write(b); // print to Terminal Console
            }

            @Override
            public void write(byte @NonNull [] b, int off, int len) {
                originalOut.write(b, off, len); // print to Terminal Console
            }
        };

        // System.out keep terminal, log GUI has been managed via GuiLogAppender
        System.setOut(new PrintStream(outStream, true, StandardCharsets.UTF_8));
    }

    private void startMemoryUpdater() {
        Timer timer = new Timer(2000, e -> {
            Runtime rt = Runtime.getRuntime();
            long used = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
            long total = rt.totalMemory() / 1024 / 1024;
            long max = rt.maxMemory() / 1024 / 1024;
            memoryLabel.setText(String.format("RAM Usage: %d MB / %d MB (Max: %d MB)", used, total, max));
        });
        timer.start();
    }
}