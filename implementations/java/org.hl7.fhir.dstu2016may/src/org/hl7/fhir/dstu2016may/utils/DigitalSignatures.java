package org.hl7.fhir.dstu2016may.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.utilities.xml.XmlGenerator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class DigitalSignatures {


  public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyException, MarshalException, XMLSignatureException, FHIRException, org.hl7.fhir.exceptions.FHIRException {
    // http://docs.oracle.com/javase/7/docs/technotes/guides/security/xmldsig/XMLDigitalSignature.html
    //
    byte[] inputXml = "<Envelope xmlns=\"urn:envelope\">\r\n</Envelope>\r\n".getBytes();
    // load the document that's going to be signed
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
    dbf.setNamespaceAware(true);
    DocumentBuilder builder = dbf.newDocumentBuilder();  
    Document doc = builder.parse(new ByteArrayInputStream(inputXml)); 
    
    // create a key pair
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(512);
    KeyPair kp = kpg.generateKeyPair(); 
    
    // sign the document
    DOMSignContext dsc = new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()); 
    XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM"); 
   
    Reference ref = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA1, null), Collections.singletonList(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
    SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null), fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null), Collections.singletonList(ref));
    
    KeyInfoFactory kif = fac.getKeyInfoFactory(); 
    KeyValue kv = kif.newKeyValue(kp.getPublic());
    KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));
    XMLSignature signature = fac.newXMLSignature(si, ki); 
    signature.sign(dsc);
    
    OutputStream os = System.out;
    new XmlGenerator().generate(doc.getDocumentElement(), os);
  }
}
