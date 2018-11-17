package fr.inria.DisableAccessibility;

import com.sun.org.apache.xerces.internal.dom.DeferredElementImpl;
import org.w3c.dom.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResourceProcessor {

    private static final String ACCESSIBILITY_ATTRIBUTE = "android:importantForAccessibility";
    private static final String ID_ATTRIBUTE = "android:id";

    static HashMap<String, resourceHolder> resources = new HashMap<String, resourceHolder>();
    static List<File> resDirs = new ArrayList<File>();


    enum AccessibilityStatus {
        IMPORTANT_FOR_ACCESSIBILITY_AUTO,
        IMPORTANT_FOR_ACCESSIBILITY_YES,
        IMPORTANT_FOR_ACCESSIBILITY_NO,
        IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
    }

    public static void process() {
        File dir = new File("/Users/mnaseri/Desktop/Inria/Project/testAccessiblity");
        findFile(dir);
        populateResources(resDirs);
    }

    public static void findFile(File targetFile) {
        File[] list = targetFile.listFiles();
        if (list != null)
            for (File file : list) {
                if (file.isDirectory() && file.getPath().contains("res")
                        && file.getPath().contains("main")
                        && file.getName().contains("layout")) {
                    resDirs.add(file);
                } else {
                    findFile(file);
                }
            }
    }

    public static void populateResources(List<File> resDirs) {
        for (File file : resDirs) {
            for (File xmlFile : file.listFiles()) {
                readXmlFile(xmlFile);
            }
        }
    }

    public static void readXmlFile(File file) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
            Document doc = dbBuilder.parse(file);

            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("*");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String accAttr = ((DeferredElementImpl) node).getAttribute(ACCESSIBILITY_ATTRIBUTE);
                    String idAttr = ((DeferredElementImpl) node).getAttribute(ID_ATTRIBUTE);
                    CtType nodeReference = App.launcher.getFactory().Type().get(node.getNodeName());
                    if (nodeReference.isSubtypeOf(App.androidEditTextReference)) {
                        // do something with the current element
                        resources.put(getId(idAttr), new resourceHolder(accAttr));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void enforceChanges() {
        File dir = new File("/Users/mnaseri/Desktop/Inria/Project/testAccessiblity");
        findFile(dir);
        for (File file : resDirs) {
            for (File xmlFile : file.listFiles()) {
                enforce(xmlFile);
            }
        }
    }

    public static void enforce(File file) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
            Document doc = dbBuilder.parse(file);

            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("*");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String accAttr = ((DeferredElementImpl) node).getAttribute(ACCESSIBILITY_ATTRIBUTE);
                    String idAttr = ((DeferredElementImpl) node).getAttribute(ID_ATTRIBUTE);
                    idAttr = getId(idAttr);
                    if (node.getNodeName().toLowerCase().contains("edittext")) {
                        if (!(accAttr.equals("no") || accAttr.equals("noHideDescendants")) && App.editTextIds.contains(idAttr)) {
//                            ((DeferredElementImpl) node).setAttribute(ACCESSIBILITY_ATTRIBUTE, "no");
                            int a = 12;
                        }

                    }
                }
            }


            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", new Integer(2));
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file.getPath()), "utf-8");
            StreamResult result = new StreamResult(outputStreamWriter);

            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean hasAttribute(Element element, String value) {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node node = attributes.item(i);
            if (value.equals(node.getNodeValue())) {
                return true;
            }
        }
        return false;
    }

    public static String getId(String id) {
        return id.replace("@+id/", "");
    }

    static class resourceHolder {
        public ResourceProcessor.AccessibilityStatus accessibilityStatus;

        public resourceHolder(String status) {

            if (status.equals("yes"))
                accessibilityStatus = AccessibilityStatus.IMPORTANT_FOR_ACCESSIBILITY_YES;
            if (status.equals("no"))
                accessibilityStatus = AccessibilityStatus.IMPORTANT_FOR_ACCESSIBILITY_NO;
            if (status.equals("noHideDescendants"))
                accessibilityStatus = AccessibilityStatus.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS;
            if (status.equals("auto"))
                accessibilityStatus = AccessibilityStatus.IMPORTANT_FOR_ACCESSIBILITY_AUTO;
        }

    }


}

