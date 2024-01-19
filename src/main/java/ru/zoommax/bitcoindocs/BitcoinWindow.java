package ru.zoommax.bitcoindocs;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class BitcoinWindow implements ToolWindowFactory, DumbAware {
    private static final Logger log = Logger.getInstance(BitcoinWindow.class);

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        BitcoinDocsWindowContent toolWindowContent = new BitcoinDocsWindowContent(project, toolWindow);
        Content content = ContentFactory.getInstance().createContent(toolWindowContent.getContentPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private static class BitcoinDocsWindowContent {
        private static final String LOADING = "/icons/847.gif";
        private final JBPanel contentPanel = new JBPanel();
        private final Project project;
        private final ToolWindow toolWindow;

        public BitcoinDocsWindowContent(Project project, ToolWindow toolWindow) {
            this.project = project;
            this.toolWindow = toolWindow;
            contentPanel.setLayout(new MigLayout("fillx", "[grow,fill]", "[][fill]"));
            bitcoinCoreVersionChooser();
        }

        private void loading(boolean show){
            if (show) {
                try {
                    contentPanel.removeAll();
                    JBLabel label = new JBLabel(new ImageIcon(getClass().getResource(LOADING)));
                    contentPanel.add(label, "wrap");
                    contentPanel.updateUI();
                } catch (NullPointerException e) {
                    log.error(e);
                }
            } else {
                contentPanel.remove(0);
                contentPanel.updateUI();
            }
        }

        private void bitcoinCoreVersionChooser() {
            loading(true);
            new Thread(() -> {
                JBLabel label = new JBLabel("Choose Bitcoin Core version");
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setVerticalAlignment(SwingConstants.TOP);
                label.setFont(new Font("Arial", Font.BOLD, 20));
                ComboBox<String> comboBox = new ComboBox<>();
                comboBox.addItem("26.0.0");
                comboBox.addItem("25.0.0");
                comboBox.addItem("24.0.0");
                comboBox.addItem("23.0.0");
                comboBox.addItem("22.0.0");
                comboBox.addItem("0.21.0");
                comboBox.addItem("0.20.0");
                comboBox.addItem("0.19.0");
                comboBox.addItem("0.18.0");
                comboBox.addItem("0.17.0");
                comboBox.addItem("0.16.0");
                comboBox.addItem("0.16.3");
                comboBox.addItem("0.16.2");
                comboBox.addItem("0.16.1");
                comboBox.addItem("0.16.0");
                comboBox.setSelectedItem("26.0.0");

                JButton button = new JButton("Go to docs");
                button.addActionListener(e -> {
                    String version = (String) comboBox.getSelectedItem();
                    String url = "https://bitcoincore.org/en/doc/" + version + "/";
                    rpcMenu(url, version);
                });
                contentPanel.add(label, "wrap");
                contentPanel.add(comboBox);
                contentPanel.add(button, "wrap");
                loading(false);
            }).start();
        }



        private void rpcMenu(String url, String version) {
            loading(true);
            new Thread(() -> {
                JBLabel label = new JBLabel("RPC API");
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Arial", Font.BOLD, 20));
                List<RpcMenu> rpcMenus = new ArrayList<>();
                try {
                    Document doc = Jsoup.connect(url).get();
                    Elements headers = doc.select("header");
                    for (Element header : headers) {
                        RpcMenu rpcMenu = new RpcMenu();
                        rpcMenu.setVersion(version);
                        rpcMenu.setH3(header.select("h3").text());
                        Elements lis = header.select("li");
                        List<String> urls = new ArrayList<>();
                        List<String> names = new ArrayList<>();
                        for (Element li : lis) {
                            Elements hrefs = li.select("a");
                            for (Element href : hrefs) {
                                urls.add("https://bitcoincore.org"+href.attr("href"));
                                names.add(href.text());
                            }
                        }
                        rpcMenu.setUrls(urls);
                        rpcMenu.setNames(names);
                        rpcMenus.add(rpcMenu);
                    }
                } catch (IOException e) {
                    log.error(e);
                }
                ComboBox<String> comboBoxMenuItem = new ComboBox<>();
                for (RpcMenu rpcMenu : rpcMenus) {
                    comboBoxMenuItem.addItem(rpcMenu.getH3());
                }
                contentPanel.add(label, "wrap");
                contentPanel.add(comboBoxMenuItem, "wrap");
                JButton open = new JButton("Open");
                open.addActionListener(e -> {
                    String itemName = (String) comboBoxMenuItem.getSelectedItem();
                    for (RpcMenu rpcMenu : rpcMenus) {
                        if (rpcMenu.getH3().equals(itemName)) {
                            listMethods(rpcMenu);
                        }
                    }
                });
                contentPanel.add(open, "wrap");
                JButton back = new JButton("Back");
                back.addActionListener(e -> bitcoinCoreVersionChooser());
                contentPanel.add(back, "wrap");
                loading(false);
            }).start();
        }

        private void listMethods(RpcMenu rpcMenu){
            loading(true);
            new Thread(() -> {
                JBLabel label = new JBLabel("RPC API");
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Arial", Font.BOLD, 20));
                ComboBox<String> comboBoxMenuItem = new ComboBox<>();
                for (String name : rpcMenu.getNames()) {
                    comboBoxMenuItem.addItem(name);
                }
                contentPanel.add(label, "wrap");
                contentPanel.add(comboBoxMenuItem, "wrap");
                JButton open = new JButton("Open");
                open.addActionListener(e -> {
                    String itemName = (String) comboBoxMenuItem.getSelectedItem();
                    for (String name : rpcMenu.getNames()) {
                        if (name.equals(itemName)) {
                            openDocs(rpcMenu.getUrls().get(rpcMenu.getNames().indexOf(name)), rpcMenu);
                        }
                    }
                });
                contentPanel.add(open, "wrap");
                JButton back = new JButton("Back");
                back.addActionListener(e -> rpcMenu("https://bitcoincore.org/en/doc/"+rpcMenu.getVersion()+"/", rpcMenu.getVersion()));
                contentPanel.add(back, "wrap");
                loading(false);
            }).start();

        }

        private void openDocs(String url, RpcMenu rpcMenu){
            loading(true);
            new Thread(() -> {
                try {
                    Document doc = Jsoup.connect(url).get();
                    String code = doc.select("code").first().text();


                    String html = "<html><body>" +
                            "<h3>"+rpcMenu.getH3()+"</h3>" +
                            "<h4>"+rpcMenu.getNames().get(rpcMenu.getUrls().indexOf(url))+"</h4>" +
                            "<p>"+doc.select("p").first().text()+"</p>" +
                            "<p>Example:</p>" +
                            "<pre><code>"+code+"</code></pre>" +
                            "</body></html>";
                    JEditorPane editorPane = new JEditorPane("text/html", html);
                    editorPane.setEditable(false);

                    JButton switchView = new JButton("Switch view");
                    switchView.addActionListener(e -> {
                        if (editorPane.getContentType().equals("text/html")) {
                            editorPane.setContentType("text/plain");
                            editorPane.setText(code);
                            contentPanel.updateUI();
                        } else {
                            editorPane.setContentType("text/html");
                            editorPane.setText(html);
                            contentPanel.updateUI();
                        }
                    });

                    JBLabel label = new JBLabel("RPC API");
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setFont(new Font("Arial", Font.BOLD, 20));

                    JButton back = new JButton("Back");
                    back.addActionListener(e -> listMethods(rpcMenu));

                    contentPanel.add(label, "wrap");
                    contentPanel.add(back, "wrap");
                    contentPanel.add(editorPane, "wrap");
                    contentPanel.add(switchView, "wrap");
                    loading(false);
                } catch (IOException e) {
                    log.error(e);
                }
            }).start();
        }

        public JPanel getContentPanel() {
            return contentPanel;
        }
    }

}
