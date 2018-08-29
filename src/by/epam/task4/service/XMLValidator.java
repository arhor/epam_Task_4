package by.epam.task4.service;

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

public class XMLValidator {
	
	private static final Logger LOG = LogManager.getLogger(XMLValidator.class);

	public static boolean validate(String xml, String xsd) {
		Schema schema = null;
		try {
			schema = SchemaReader.getSchema(xsd);			
			Validator validator = schema.newValidator();
			Source source = new StreamSource(xml);
			validator.validate(source);
			LOG.info(xml + " is valid\n");
			return true;
		} catch (IOException e) {
			LOG.error("I/O exception: ", e);
		} catch (SAXException e) {
			LOG.error("SAX exception: ", e);
		}
		LOG.info(xml + " is invalid\n");
		return false;
	}
}