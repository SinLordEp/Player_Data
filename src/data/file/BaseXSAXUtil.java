package data.file;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.*;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;


public final class BaseXSAXUtil {

	Context context;

	public BaseXSAXUtil() {

		// Database context.
		context = new Context();		
	}

	public void cerrarContext() {

		// Close the database context
		context.close();

	}

	public void crearDB(String nombre, String rutaFicheroXML) throws BaseXException {
		
		// Habría que añadir algo para ver si está ya creada y no crearla
		
		System.out.println("=== Creando BD ===");
		
		new CreateDB(nombre, rutaFicheroXML).execute(context);

	}
	
	public void abrirDB(String nombre) throws BaseXException {
		
		System.out.println("=== Abrir BD ===");
		
		new Open(nombre).execute(context);		
		
	}	
	
	public void infoDB() throws BaseXException {
		
		System.out.println("\n* Información de la DB abierta:");

		System.out.print(new InfoDB().execute(context));
		
	}
	
	public void borrarDB(String nombre) throws BaseXException {

		new DropDB(nombre).execute(context);

	}
	

	/*
	 * Prueba de XQuery (llama a métodos auxiliares que están definidos debajo)
	 */
	public void pruebaVerTodos() throws Exception {

		System.out.println("=== RunQueries ===");

		// Evaluate the specified XQuery
		String query = "/Libros";

		System.out.println("QUERY QUE SE VA A LANZAR:\n" + query);
		
		// Process the query by using the database command
		//System.out.println("\n* Use the database command:");
		
		querySelect(query);

	}

	/**
	 * This method evaluates a query by using the database command. The results are
	 * automatically serialized and printed.
	 * 
	 * @param query query to be evaluated
	 * @throws BaseXException if a database command fails
	 */
	private void querySelect(String query) throws Exception {
		
		String datosConsulta = new XQuery(query).execute(context);
		
		System.out.println("DATOS DE LA CONSULTA SIN FILTRAR: \n" + datosConsulta);
		System.out.println();
		System.out.println("INTENTAMOS PROCESAR LA RESPUESTA (saltará excepción si no está bien formada la respuesta");
		
		SAXBuilder saxBuilder = new SAXBuilder();
		InputStream fichero = new ByteArrayInputStream(datosConsulta.getBytes());
		Document document = saxBuilder.build(fichero);
		Element classElement = document.getRootElement();

		List<Element> listaNodos = classElement.getChildren();
		
		for (int temp = 0; temp < listaNodos.size(); temp++) {
			Element elementoLibro = listaNodos.get(temp);
		
			System.out.println("-------- LIBRO " + (temp+1) + "--------");
			
			System.out.println(elementoLibro.getAttributeValue("publicado_en"));
			System.out.println(elementoLibro.getChild("Titulo").getText());
			System.out.println(elementoLibro.getChild("Autor").getText());
			
			System.out.println("-------------------\n ");			
			
		}
				
	}

	/**
	 * This method evaluates a query by using the database command. The results are
	 * automatically serialized and printed.
	 * 
	 * @param query query to be evaluated
	 * @throws BaseXException if a database command fails
	 */
	public void queryOtra(String query) throws Exception {
		
		String datosConsulta = new XQuery(query).execute(context);
		
		System.out.println(datosConsulta);
				
	}
	
	/*
	 * Prueba insercion
	 * https://docs.basex.org/wiki/XQuery_Update
	 * Los datos los debería recibir por parámetro
	 */
	
	public void pruebaInsercion() throws Exception {
		
		System.out.println("== PRUEBA INSERCION ==");
		
		// Creamos nodo XML (incluyendo "hijos" y nodos de texto
		Element nuevo = new Element("Libro");
		Element elem_titulo = new Element("Titulo");
		elem_titulo.setText("Prueba Insert desde Eclipse 2");
		Element elem_autor = new Element("Autor");
		elem_autor.setText("YO");
		nuevo.setAttribute("publicado_en","2023");

		nuevo.addContent(elem_titulo);
		nuevo.addContent(elem_autor);
		
		// Formateamos como string y lo añadimos a la query de inserción 		
		XMLOutputter xmlOut = new XMLOutputter();
		String formateado = xmlOut.outputString(nuevo);
		
		String queryInsert = "insert node " + formateado + " into /Libros ";

		// Ejecutamos la query (IMPORTANTE: la base de datos tiene que estar cerrada en BaseX, porque se bloquea) 
		
		queryOtra(queryInsert);
		
		System.out.println("== LIBRO INSERTADO CORRECTAMENTE ==");		
		
	}

	/*
	 * Prueba Eliminación
	 * https://docs.basex.org/wiki/XQuery_Update
	 * Prueba estática (siempre con los mismos datos)
	 * Los datos los debería recibir por parámetro
	 */	
	
	public void pruebaEliminacion() {
		
	}

	/*
	 * Prueba Modificación
	 * https://docs.basex.org/wiki/XQuery_Update
	 * Prueba estática (siempre con los mismos datos)
	 * Los datos los debería recibir por parámetro
	 */		
	
	public void pruebaModificacion() {
		
		
		
	}
	
	/*
	 * Prueba Búsqueda
	 * https://docs.basex.org/wiki/XQuery_Update
	 * Prueba estática (siempre con los mismos datos)
	 * Los datos los debería recibir por parámetro
	 */		
	
	public void pruebaBusqueda() throws Exception {

		// Evaluate the specified XQuery
		String query = "<respuesta> { //Libro[Autor='YO'] } </respuesta>" 
				+ "";

		System.out.println("QUERY QUE SE VA A LANZAR:" + query);
		
		// Process the query by using the database command
		//System.out.println("\n* Use the database command:");
		
		querySelect(query);
		
		
	}	
	
}