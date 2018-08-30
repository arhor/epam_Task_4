/*
 * class: MedicinsDOMBuilder
 */

package by.epam.task4.service.parsing.dom;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.w3c.dom.*;

import org.xml.sax.SAXException;

import by.epam.task4.exception.BuildMedicineException;
import by.epam.task4.exception.MedicineNotPresentedException;
import by.epam.task4.model.*;
import by.epam.task4.service.factory.MedicineFactory;
import by.epam.task4.service.parsing.AttributesEnum;
import by.epam.task4.service.parsing.ElementsEnum;
import by.epam.task4.service.parsing.MedicinsAbstractBuilder;

/**
 * Class MedicinsDOMBuilder extends abstract class MedicinsAbstractBuilder,
 * serves for building set of Medicine objects based on XML-document by parsing
 * it using DOM-parser for XML
 * 
 * @author Maxim Burishinets
 * @version 2.0
 * @since 30 Aug 2018
 */
public class MedicinsDOMBuilder extends MedicinsAbstractBuilder {
    
    private static final Logger LOG = LogManager.getLogger();

    private DocumentBuilder docBuilder;
    private MedicineFactory mFactory;
    private DateFormat dateFormat;
    
    public MedicinsDOMBuilder() {
        medicins = new HashSet<Medicine>();
        mFactory = new MedicineFactory();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            docBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            LOG.error("Parser configuration exception: ", e);
        }
    }

    /**
     * Parses XML-document using DOM-parser, gets root-element of current 
     * document runs through it and builds set of Medicine objects
     * 
     * @param xml - path to XML-document to parse
     * @return true - if parsing was successful; false - if there occurred any 
     * kind of exception during XML-document parsing
     */
    @Override
    public boolean buildSetMedicins(String xml) {
        Document document = null;
        try {
            document = docBuilder.parse(xml);
            Element root = document.getDocumentElement();
            NodeList medicinsList = root.getChildNodes();
            for (int i = 0; i < medicinsList.getLength(); i++) {
                Node node = medicinsList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element medicineElement = (Element) node;
                    Medicine medicine = buildMedicine(medicineElement);
                    medicins.add(medicine);
                }
            }
            return true;
        } catch (IOException e) {
            LOG.error("I/O exception", e);
        } catch (SAXException e) {
            LOG.error("SAX pasring exception: ", e);
        } catch (BuildMedicineException e) {
            LOG.error("An error occured within building Medicine object", e);
        }
        return false;
    }
    
    /**
     * Runs through all child-nodes of 'medicineElement' and builds concrete
     * Medicine object depending on it
     * 
     * @param medicineElement - DOM-element that represents concrete medicine
     * @return Medicine object
     * @throws BuildMedicineException
     */
    private Medicine buildMedicine(Element medicineElement)
            throws BuildMedicineException {
        Medicine currentMedicine;
        try {
            currentMedicine = mFactory.getMedicine(
                    ElementsEnum.valueOf(
                            medicineElement.getTagName().toUpperCase()));
        } catch (MedicineNotPresentedException e) {
            LOG.error("Medicine not presented exception", e);
            throw new BuildMedicineException("Medicine not presented", e);
        }
        setMedicineAttributes(currentMedicine, medicineElement);
        Element pharm = (Element) medicineElement.getElementsByTagName(
                ElementsEnum.PHARM.getValue()).item(0);
        currentMedicine.setPharm(pharm.getTextContent());
        currentMedicine.setVersions(buildVersions(medicineElement));
        return currentMedicine;
    }
    
    /**
     * Initializes Medicine object's fields depending on attribute-nodes of 
     * passed DOM-element
     * 
     * @param medicine - Medicine object with fields supposed to initialize
     * @param medElement - DOM-element which contains relevant attributes
     */
    private void setMedicineAttributes(Medicine medicine, Element medElement) {
        NamedNodeMap attributes = medElement.getAttributes();
        for (int i = 0; i < attributes.getLength() ; i++) {
            Attr attribute = (Attr) attributes.item(i);
            String name = attribute.getName();
            String value = attribute.getValue();
            AttributesEnum currentAttribute = AttributesEnum.valueOf(
                    name.toUpperCase());
            switch (currentAttribute) {
                case NAME:
                    medicine.setName(value);
                    break;
                case CAS:
                    medicine.setCas(value);
                    break;
                case DRUG_BANK:
                    medicine.setDrugBank(value);
                    break;
                case RECIPE:
                    boolean recipe = Boolean.parseBoolean(value);
                    ((Antibiotic)medicine).setRecipe(recipe);
                    break;
                case SOLUTION:
                    ((Vitamin)medicine).setSolution(value);
                    break;
                case NARCOTIC:
                    boolean narcotic = Boolean.parseBoolean(value);
                    ((Analgetic)medicine).setNarcotic(narcotic);
                    break;
                default:
                    break;
            }
        }
    }
    
    /**
     * Runs through all version-nodes of passed DOM-element and builds set of 
     * Version objects
     * 
     * @param medicineElement - DOM-element that represents concrete medicine
     * @return set of Version objects
     */
    private HashSet<Version> buildVersions(Element medicineElement) {
        HashSet<Version> versions = new HashSet<>();
        NodeList versionElements = medicineElement.getElementsByTagName(
                ElementsEnum.VERSION.getValue());
        for (int i = 0; i < versionElements.getLength(); i++) {
            Element versionElement = (Element) versionElements.item(i);
            versions.add(buildVersion(versionElement));
        }
        return versions;
    }
    
    /**
     * Runs through all child-nodes of passed DOM-element and builds Version 
     * object depending on it
     * 
     * @param versionElement - DOM-element that represents version of 
     * concrete medicine
     * @return Version object
     */
    private Version buildVersion(Element versionElement) {
        Version currentVersion = new Version();
        currentVersion.setTradeName(versionElement.getAttribute(
                AttributesEnum.TRADE_NAME.getValue()));
        currentVersion.setProducer(versionElement.getElementsByTagName(
                ElementsEnum.PRODUCER.getValue()).item(0).getTextContent());
        currentVersion.setForm(versionElement.getElementsByTagName(
                ElementsEnum.FORM.getValue()).item(0).getTextContent());
        Element certificateElement = 
                (Element) versionElement.getElementsByTagName(
                        ElementsEnum.CERTIFICATE.getValue()).item(0);
        currentVersion.setCertificate(buildCertificate(certificateElement));
        currentVersion.setPacks(buildPacks(versionElement));
        Element dosageElement =    
                (Element) versionElement.getElementsByTagName(
                        ElementsEnum.DOSAGE.getValue()).item(0);
        currentVersion.setDosage(buildDosage(dosageElement));
        return currentVersion;
    }
    
    /**
     * Runs through all child-nodes of passed DOM-element and builds 
     * Certificate object depending on it
     * 
     * @param certificateElement - DOM-element that represents certificate of 
     * concrete medicine version
     * @return Certificate object
     */
    private Certificate buildCertificate(Element certificateElement) {
        Certificate currentCertificate = new Certificate();
        NodeList certificateFields = certificateElement.getChildNodes();
        for (int j = 0; j < certificateFields.getLength(); j++) {
            Node certField = certificateFields.item(j);
            if (certField.getNodeType() == Node.ELEMENT_NODE) {
                String tagName = 
                        ((Element)certField).getTagName().toUpperCase();
                ElementsEnum currentField = ElementsEnum.valueOf(tagName);
                switch (currentField) {
                    case REGISTRED_BY:
                        currentCertificate.setRegistredBy(
                                certField.getTextContent());
                        break;
                    case REGISTRATION_DATE:
                        try {
                            Date date = dateFormat.parse(
                                    certField.getTextContent());
                            currentCertificate.setRegistrationDate(date);
                        } catch (ParseException e) {
                            LOG.error("Date parsing exception: ", e);
                        }
                        break;
                    case EXPIRE_DATE:
                        try {
                            Date date = dateFormat.parse(
                                    certField.getTextContent());
                            currentCertificate.setExpireDate(date);
                        } catch (ParseException e) {
                            LOG.error("Date parsing exception: ", e);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return currentCertificate;
    }
    
    /**
     * Runs through all pack-nodes of passed DOM-element and builds set of 
     * Pack objects
     * 
     * @param versionElement - DOM-element that represents version of 
     * concrete medicine
     * @return set of Pack objects
     */
    private HashSet<Pack> buildPacks(Element versionElement) {
        HashSet<Pack> packs = new HashSet<Pack>();
        NodeList packElements = versionElement.getElementsByTagName(
                ElementsEnum.PACK.getValue());
        for (int k = 0; k < packElements.getLength(); k++) {
            Element packElement = (Element) packElements.item(k);
            packs.add(buildPack(packElement));
        }
        return packs;
    }
    
    /**
     * Runs through all child-nodes of passed DOM-element and builds 
     * Pack object depending on it
     * 
     * @param packElement - DOM-element that represents package form for 
     * version of concrete medicine
     * @return Pack object
     */
    private Pack buildPack(Element packElement) {
        Pack currentPack = new Pack();
        if (packElement.hasAttributes()) {
            Attr size = packElement.getAttributeNode(
                    AttributesEnum.SIZE.getValue());
            currentPack.setSize(size.getValue());
        }
        NodeList packFields = packElement.getChildNodes();
        for (int n = 0; n < packFields.getLength(); n++) {
            Node packField = packFields.item(n);
            if (packField.getNodeType() == Node.ELEMENT_NODE) {
                ElementsEnum currentField = ElementsEnum.valueOf(
                        ((Element)packField).getTagName().toUpperCase());
                switch (currentField) {
                    case QUANTITY:
                        currentPack.setQuantity(Integer.parseInt(
                                packField.getTextContent()));
                        break;
                    case PRICE:
                        currentPack.setPrice(Double.parseDouble(
                                packField.getTextContent()));
                        break;
                    default:
                        break;
                }
            }
        }
        return currentPack;
    }

    /**
     * Runs through all child-nodes of passed DOM-element and builds 
     * Dosage object depending on it
     * 
     * @param dosageElement - DOM-element that represents dosage for version 
     * of concrete medicine
     * @return Dosage object
     */
    private Dosage buildDosage(Element dosageElement) {
        Dosage currentDosage = new Dosage();
        NodeList dosageFields = dosageElement.getChildNodes();
        for (int k = 0; k < dosageFields.getLength(); k++) {
            Node dosageField = dosageFields.item(k);
            if (dosageField.getNodeType() == Node.ELEMENT_NODE) {
                ElementsEnum currentField = ElementsEnum.valueOf(
                        ((Element)dosageField).getTagName().toUpperCase());
                switch (currentField) {
                    case AMOUNT:
                        currentDosage.setAmount(
                                dosageField.getTextContent());
                        break;
                    case FREQUENCY:
                        currentDosage.setFrequency(
                                dosageField.getTextContent());
                        break;
                    default:
                        break;
                }
            }
        }
        return currentDosage;
    }
}