package util;

import app.ConsoleApplication;
import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
import org.dom4j.DocumentException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Graphics extends JFrame {

    private static final long serialVersionUID = -4825279720757909719L;
    private static ConsoleApplication consoleApplication = new ConsoleApplication();

    public static void main() throws InterruptedException {
        showWelcome();
        showGraphics();
    }

    private static void showGraphics() {
        JFrame jf = new JFrame();
        jf.setIconImage(new ImageIcon("images/llvm.jpg").getImage());
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setTitle("LYRON V0.5");
        LLBegin(jf);
    }

    private static void LLBegin(JFrame jf) {
        jf.getContentPane().removeAll();
        JPanel jpTexts = new JPanel();
        jpTexts.setLayout(new GridLayout(1, 1));
        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        JScrollPane jspane = new JScrollPane(textArea);
        jpTexts.add(jspane);
        jf.add(jpTexts, BorderLayout.CENTER);
        JPanel selections = new JPanel();
        JButton selection = new JButton("选择XML文件");
        JButton next = new JButton("下一步");
        selections.add(selection);
        selections.add(next);
        jf.add(selections, BorderLayout.SOUTH);
        JFileChooser fileChooser = new JFileChooser();

        selection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int i = fileChooser.showOpenDialog(jf);// 显示文件选择对话框

                // 判断用户单击的是否为“打开”按钮
                if (i == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();// 获得选中的文件对象
                    byte [] filecontent = new byte[(int) (selectedFile.length() + 1)];
                    String content = null;
                    try {
                        FileInputStream in = new FileInputStream(selectedFile);
                        in.read(filecontent);
                        content = new String(filecontent, StandardCharsets.UTF_8);
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, e.getStackTrace(), "错误", JOptionPane.PLAIN_MESSAGE);
                    }
                    textArea.setText(content);
                }
            }
        });
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String content = textArea.getText().trim();
                StringBufferInputStream inputStream = new StringBufferInputStream(content);
                try {
                    consoleApplication.LLBegin(inputStream);
                    LLParse(jf);
                } catch (PLDLParsingException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, e.getStackTrace(), "错误", JOptionPane.PLAIN_MESSAGE);
                } catch (PLDLAnalysisException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, e.getStackTrace(), "错误", JOptionPane.PLAIN_MESSAGE);
                } catch (DocumentException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, e.getStackTrace(), "错误", JOptionPane.PLAIN_MESSAGE);
                }
            }
        });
        jf.setSize(500, 400);
        jf.setLocationRelativeTo(null);
        jf.setVisible(true);
    }

    private static void LLParse(JFrame jf) {
        jf.getContentPane().removeAll();
        JPanel jpTexts = new JPanel();
        jpTexts.setLayout(new GridLayout(1, 1));
        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        JScrollPane jspane = new JScrollPane(textArea);
        jpTexts.add(jspane);
        jf.add(jpTexts, BorderLayout.CENTER);
        JPanel selections = new JPanel();
        JButton selection = new JButton("选择代码文件");
        JButton next = new JButton("下一步");
        selections.add(selection);
        selections.add(next);
        jf.add(selections, BorderLayout.SOUTH);
        JFileChooser fileChooser = new JFileChooser();

        selection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int i = fileChooser.showOpenDialog(jf);// 显示文件选择对话框

                // 判断用户单击的是否为“打开”按钮
                if (i == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();// 获得选中的文件对象
                    byte [] filecontent = new byte[(int) (selectedFile.length() + 1)];
                    String content = null;
                    try {
                        FileInputStream in = new FileInputStream(selectedFile);
                        in.read(filecontent);
                        content = new String(filecontent, StandardCharsets.UTF_8);
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, e.getStackTrace(), "错误", JOptionPane.PLAIN_MESSAGE);
                    }
                    textArea.setText(content);
                }
            }
        });
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String content = textArea.getText().trim();
                StringBufferInputStream inputStream = new StringBufferInputStream(content);
                try {
                    consoleApplication.LLParse(inputStream);
                    LLEnd(jf);
                } catch (PLDLAnalysisException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, e.getStackTrace(), "错误", JOptionPane.PLAIN_MESSAGE);
                } catch (PLDLParsingException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, e.getStackTrace(), "错误", JOptionPane.PLAIN_MESSAGE);
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, e.getStackTrace(), "错误", JOptionPane.PLAIN_MESSAGE);
                }
            }
        });
        jf.setSize(500, 400);
        jf.setLocationRelativeTo(null);
        jf.setVisible(true);
    }

    private static void LLEnd(JFrame jf){
        jf.getContentPane().removeAll();
        JPanel jpTexts = new JPanel();
        jpTexts.setLayout(new GridLayout(1, 1));
        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        JScrollPane jspane = new JScrollPane(textArea);
        jpTexts.add(jspane);
        jf.add(jpTexts, BorderLayout.CENTER);
        JPanel selections = new JPanel();
        JButton selection = new JButton("选择四元式保存位置");
        JButton close = new JButton("关闭");
        selections.add(selection);
        selections.add(close);
        jf.add(selections, BorderLayout.SOUTH);
        JFileChooser fileChooser = new JFileChooser();

        textArea.setText(consoleApplication.getResults().toString());

        selection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int i = fileChooser.showSaveDialog(jf);// 显示文件选择对话框

                if (i == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();// 获得选中的文件对象
                    try {
                        consoleApplication.LLEnd(new FileOutputStream(selectedFile));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, e.getStackTrace(), "错误", JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
        });
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                jf.dispose();
                System.exit(0);
            }
        });
        jf.setSize(500, 400);
        jf.setLocationRelativeTo(null);
        jf.setVisible(true);
    }

    private static void showWelcome() throws InterruptedException {
        Thread beginT = new Thread() {
            @Override
            public void run() {
                JFrame jf = new JFrame();
                ImageIcon icon = new ImageIcon("images/back.jpg");
                jf.setUndecorated(true);
                jf.setSize(icon.getIconWidth(), icon.getIconHeight());
                jf.setVisible(true);
                jf.setLocationRelativeTo(null);
                jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                JLabel label = new JLabel(icon);
                label.setBounds(0, 0, icon.getIconWidth(), icon.getIconHeight());
                jf.getLayeredPane().add(label);
                jf.setVisible(true);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                jf.setVisible(false);
                jf.dispose();
            }
        };
        beginT.start();
        beginT.join();
    }
}
