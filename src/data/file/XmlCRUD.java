package data.file;

import Interface.GeneralCRUD;
import Interface.ParserCallBack;
import data.DataOperation;
import exceptions.FileManageException;
import model.DataInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * @author SIN
 */
public class XmlCRUD implements GeneralCRUD<DataInfo> {
    File file;
    String stringXML;
    boolean parseRawXML = false;
    DataInfo dataInfo;

    @Override
    public GeneralCRUD<DataInfo> prepare(DataInfo dataInfo) {
        this.dataInfo = dataInfo;
        if(dataInfo.getUrl().startsWith("<")){
            stringXML = dataInfo.getUrl();
            parseRawXML = true;
            return this;
        }else{
            file = new File(dataInfo.getUrl());
            if (file.exists() && file.canRead() && file.canWrite()){
                return this;
            }else{
                throw new FileManageException("File cannot be read or write");
            }
        }
    }



    @Override
    public void release() {
        file = null;
    }

    @Override
    public <R, U> GeneralCRUD<DataInfo> search(ParserCallBack<R, U> parser, DataOperation operation, U dataMap) {
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> read(ParserCallBack<R, U> parser, DataOperation operation, U dataMap) {
        Element element;
        try {
            if(parseRawXML){
                element = parseStringXml(stringXML);
            }else{
                element = readXml(file);
            }
        } catch (Exception e) {
            throw new FileManageException(e.getMessage());
        }
        if (!element.hasChildNodes()) {
            return this;
        }
        parser.parse((R) element, null, dataMap);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation operation, U object) {
        try {
            Document document = createDocument();
            parser.parse((R)document,null, object);
            writeXml(document, file);
        } catch (Exception e) {
            throw new FileManageException(e.getMessage());
        }
        return this;
    }


    /**
     * Reads and parses an XML file into an {@code Element} object representing the root element of the document.
     *
     * @param file the XML file to be read and parsed
     * @return the root element of the parsed XML document
     * @throws Exception if an error occurs while reading or parsing the XML file, such as file not found or invalid XML structure
     */
    public static Element readXml(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        document.getDocumentElement().normalize();
        return document.getDocumentElement();
    }

    public static Element parseStringXml(String rawXML) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(rawXML.getBytes()));
        document.getDocumentElement().normalize();
        return document.getDocumentElement();
    }

    /**
     * Writes the given {@code Document} to the specified file as an XML file. The method ensures
     * the output is formatted with indentation for better readability. In case of errors during
     * the transformation or file writing process, it throws a {@code FileManageException}.
     *
     * @param document the {@code Document} object to be written to the XML file
     * @param file the {@code File} object representing the target XML file
     * @throws FileManageException if an error occurs during the XML writing process, such as
     *                              transformation or file output errors
     */
    public static void writeXml(Document document, File file) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
        } catch (Exception e) {
            throw new FileManageException(e.getMessage());
        }
    }

    /**
     * Creates a new, empty {@code Document} object that can be used for building
     * or manipulating XML structures programmatically. This method utilizes
     * {@code DocumentBuilderFactory} and {@code DocumentBuilder} to instantiate the
     * {@code Document} object. In case of failure during the document creation process,
     * it throws a {@code FileManageException}.
     *
     * @return a new instance of {@code Document} representing an empty XML document
     * @throws FileManageException if the document creation process fails due to
     *                              configuration errors or internal exceptions
     */
    public static Document createDocument() {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            return documentBuilder.newDocument();
        } catch (Exception e) {
            throw new FileManageException(e.getMessage());
        }
    }

}
