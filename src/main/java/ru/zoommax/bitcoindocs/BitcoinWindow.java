package ru.zoommax.bitcoindocs;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBOptionButton;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import groovy.namespace.QName;
import groovy.util.Node;
import groovy.util.NodeList;
import groovy.xml.XmlParser;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.net.http.HttpClient;

final class BitcoinWindow implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        BitcoinDocsWindowContent toolWindowContent = new BitcoinDocsWindowContent(toolWindow);
        Content content = ContentFactory.getInstance().createContent(toolWindowContent.getContentPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private static class BitcoinDocsWindowContent {


        private static final String ICON = "/icons/bitcoin.png";
        private final JBPanel contentPanel = new JBPanel();

        private final ToolWindow toolWindow;

        public BitcoinDocsWindowContent(ToolWindow toolWindow) {
            this.toolWindow = toolWindow;
            contentPanel.setLayout(new MigLayout("fillx", "[grow,fill]", "[][fill]"));
            bitcoinCoreVersionChooser();
        }

        private void bitcoinCoreVersionChooser() {
            JBLabel label = new JBLabel("Choose Bitcoin Core version");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setVerticalAlignment(SwingConstants.TOP);
            label.setFont(new Font("Arial", Font.BOLD, 20));
            contentPanel.add(label, "wrap");

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
            contentPanel.add(comboBox);

            JButton button = new JButton("Go to docs");
            button.addActionListener(e -> {
                String version = (String) comboBox.getSelectedItem();
                String url = "https://bitcoincore.org/en/doc/" + version + "/";
                rpcMenu(url);
            });
            contentPanel.add(button, "wrap");
        }

        private void rpcMenu(String url){
            try {
                Document doc = Jsoup.connect(url).get();
                Elements rpcMenu = doc.getElementsByTag("section class=\"toc\"");
                for (Element element : rpcMenu) {
                    System.out.println(element + "123");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public JPanel getContentPanel() {
            return contentPanel;
        }
    }

}
