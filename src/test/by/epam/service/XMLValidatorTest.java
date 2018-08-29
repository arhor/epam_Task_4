package test.by.epam.service;

import org.testng.Assert;
import org.testng.annotations.Test;

import by.epam.task4.service.XMLValidator;

public class XMLValidatorTest {
	
	public static String VALIDATION_XSD = "Medicins.xsd";
	public static String VALID_XML = "validTest.xml";
	public static String INVALID_XML = "invalidTest.xml";
    
    @Test
    public void validatePositiveTest() {
	    boolean actual = XMLValidator.validate(VALID_XML, VALIDATION_XSD);
	    Assert.assertTrue(actual);
    }
    
    @Test
    public void validateNegativeTest() {
    	boolean actual = XMLValidator.validate(INVALID_XML, VALIDATION_XSD);
    	Assert.assertFalse(actual);
    }
}