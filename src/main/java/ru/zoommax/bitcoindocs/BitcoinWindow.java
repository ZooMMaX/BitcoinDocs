package ru.zoommax.bitcoindocs;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.zoommax.bitcoindocs.parser.Blocks;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class BitcoinWindow implements ToolWindowFactory, DumbAware {
    private static final Logger log = Logger.getInstance(BitcoinWindow.class);

    static void setToolWindowWidth(Project project, String toolWindowID, int desiredWidth) {
        ToolWindowManager instance = ToolWindowManager.getInstance(project);
        ToolWindowEx tw = (ToolWindowEx) instance.getToolWindow(toolWindowID);
        int width = tw.getComponent().getWidth();
        tw.stretchWidth(desiredWidth - width);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        BitcoinDocsWindowContent toolWindowContent = new BitcoinDocsWindowContent();
        Content content = ContentFactory.getInstance().createContent(toolWindowContent.getContentPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
        setToolWindowWidth(project, toolWindow.getId(), 450);
    }

    private static class BitcoinDocsWindowContent {
        private static final String LOADING = "/icons/847.gif";
        private final JBPanel contentPanel = new JBPanel();

        public BitcoinDocsWindowContent() {
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

                Document doc = null;
                try {
                    doc = Jsoup.connect("https://bitcoincore.org/en/doc/").get();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Elements elements = doc.select("article").select("li");
                for (Element element : elements) {
                    String version = element.select("a").text();
                    comboBox.addItem(version);
                }

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

                    Blocks blocks = new Blocks(code);

                    JBLabel label = new JBLabel("RPC API");
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setFont(new Font("Arial", Font.BOLD, 20));

                    JButton back = new JButton("Back");
                    back.addActionListener(e -> listMethods(rpcMenu));

                    JBLabel path = new JBLabel(rpcMenu.getH3(), SwingConstants.CENTER);
                    path.setFont(new Font("Arial", Font.BOLD, 15));

                    JBPanel panelDocs = new JBPanel();
                    panelDocs.setLayout(new MigLayout("fillx", "[grow,fill]", "[][fill]"));

                    JBPanel panel = new JBPanel();
                    panel.setLayout(new MigLayout("fillx", "[grow,fill]", "[][fill]"));

                    JBTextArea description = new JBTextArea();
                    description.setEditable(false);
                    description.setLineWrap(true);
                    description.setWrapStyleWord(true);
                    description.setText(blocks.getDescription());

                    panelDocs.add(description, "wrap");
                    panelDocs.add(new JBLabel("Result:"), "wrap");
                    for (int i = 0; i < blocks.getResult().getJsonschema().size(); i++) {
                        JBTextArea jsonschema = new JBTextArea(blocks.getResult().getJsonschema().get(i));
                        jsonschema.setEditable(false);
                        jsonschema.setLineWrap(true);
                        jsonschema.setWrapStyleWord(true);
                        jsonschema.setFont(new Font("Arial", Font.BOLD, 15));
                        jsonschema.setBackground(JBColor.LIGHT_GRAY);
                        jsonschema.setDisabledTextColor(JBColor.BLACK);
                        jsonschema.setBorder(BorderFactory.createLineBorder(JBColor.BLACK));
                        panel.add(jsonschema);
                        JBTextArea descriptionTextArea = new JBTextArea(blocks.getResult().getDescription().get(i));
                        descriptionTextArea.setEditable(false);
                        descriptionTextArea.setLineWrap(true);
                        descriptionTextArea.setWrapStyleWord(true);
                        descriptionTextArea.setFont(new Font("Arial", Font.BOLD, 15));
                        descriptionTextArea.setBackground(JBColor.LIGHT_GRAY);
                        descriptionTextArea.setDisabledTextColor(JBColor.BLACK);
                        descriptionTextArea.setBorder(BorderFactory.createLineBorder(JBColor.BLACK));
                        panel.add(descriptionTextArea, "wrap");
                    }

                    panelDocs.add(panel, "wrap");
                    panelDocs.add(new JBLabel("Examples:"), "wrap");
                    for (String example : blocks.getExamples()) {
                        if (example.isEmpty()) {
                            continue;
                        }
                        JBTextArea exampleTextArea = new JBTextArea(example);
                        exampleTextArea.setEditable(false);
                        exampleTextArea.setLineWrap(true);
                        exampleTextArea.setWrapStyleWord(true);
                        exampleTextArea.setBackground(JBColor.LIGHT_GRAY);
                        exampleTextArea.setDisabledTextColor(JBColor.BLACK);
                        exampleTextArea.setBorder(BorderFactory.createLineBorder(JBColor.BLACK));
                        panelDocs.add(exampleTextArea, "wrap");
                    }

                    JScrollPane scrollPane = new JScrollPane(panelDocs);
                    scrollPane.setBorder(BorderFactory.createEmptyBorder());
                    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                    scrollPane.getVerticalScrollBar().setUnitIncrement(16);

                    contentPanel.add(label, "wrap");
                    contentPanel.add(back, "wrap");
                    contentPanel.add(path, "wrap");
                    contentPanel.add(scrollPane, "wrap");
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
