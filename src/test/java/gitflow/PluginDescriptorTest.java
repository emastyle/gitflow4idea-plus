package gitflow;

import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class PluginDescriptorTest {
    @Test
    public void declaresTaskPlatformDependency() throws Exception {
        try (InputStream descriptor = getClass().getResourceAsStream("/META-INF/plugin.xml")) {
            assertNotNull(descriptor);
            NodeList dependencies = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(descriptor).getElementsByTagName("depends");
            for (int i = 0; i < dependencies.getLength(); i++) {
                if ("com.intellij.platform.tasks".equals(dependencies.item(i).getTextContent().trim())) {
                    return;
                }
            }
            fail("Missing PhpStorm 2026.3 task platform dependency");
        }
    }

    @Test
    public void settingsPageDeclaresDisplayNameWithoutLoadingConfigurable() throws Exception {
        try (InputStream descriptor = getClass().getResourceAsStream("/META-INF/plugin.xml")) {
            assertNotNull(descriptor);
            NodeList configurables = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(descriptor).getElementsByTagName("projectConfigurable");
            for (int i = 0; i < configurables.getLength(); i++) {
                Element configurable = (Element) configurables.item(i);
                if ("gitflow.GitflowConfigurable".equals(configurable.getAttribute("instance"))) {
                    assertEquals("Gitflow", configurable.getAttribute("displayName"));
                    return;
                }
            }
            fail("Missing Gitflow settings page registration");
        }
    }
}
