package com.anzodeg;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v28.message.ADT_A01;
import ca.uhn.hl7v2.model.v28.message.ADT_A02;
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.parser.XMLParser;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws Exception {
        String inputFile = "parser/src/resources/input.hl7";
        String outputFile = "parser/src/resources/output.xml";

        parseHl7(inputFile, outputFile);
    }

    /**
     * Parses an HL7v2 message into XML using the HAPI xml parser
     * 
     * @param hl7 - Hl7v2 message
     * @return - String of Hl7v2 message in XML
     *         - Returns "hl7exception" if parsing fails
     * @throws FileNotFoundException
     * @todo add error handling for infinite looping issue in parser
     */
    public static void parseHl7(String inputFile, String outputFile) throws Exception {
        FileReader fr = new FileReader(inputFile);
        BufferedReader br = new BufferedReader(fr);
        String hl7 = "";
        String line = br.readLine();
        while (line != null) {
            hl7 += line;
            line = br.readLine();
        }
        fr.close();
        br.close();

        HapiContext hapiContext = new DefaultHapiContext();
        CanonicalModelClassFactory mcf = new CanonicalModelClassFactory("2.8");
        hapiContext.setModelClassFactory(mcf);
        hapiContext.getParserConfiguration().setValidating(false);

        try {
            PipeParser parser = hapiContext.getPipeParser();
            Message message = parser.parse(hl7);
            String messageType = getMessageType(hl7);
            XMLParser xmlParser = hapiContext.getXMLParser();
            String xmlPayload;

            switch (messageType) {
                case "ADT_A01":
                    ADT_A01 a01message = (ADT_A01) message;
                    xmlPayload = xmlParser.encode(a01message);
                    break;
                case "ADT_A02":
                    ADT_A02 a02message = (ADT_A02) message;
                    xmlPayload = xmlParser.encode(a02message);
                    break;
                default:
                    xmlPayload = "empty";
                    break;
            }

            FileWriter fw = new FileWriter(outputFile);
            fw.write(xmlPayload);
            fw.close();
        } catch (HL7Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Parses the message type from an HL7v2 message
     * 
     * @param hl7 - Hl7v2 message
     * @return - String representing message type (ex ADT_A01)
     */
    public static String getMessageType(String hl7) {
        int pipeIndex = hl7.indexOf("|");
        for (int _i = 0; _i < 7; _i++) {
            int next = hl7.indexOf("|", pipeIndex + 1);
            pipeIndex = next;
        }
        return hl7.substring(pipeIndex + 1, pipeIndex + 8).replace('^', '_');
    }
}
