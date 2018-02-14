package others;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class HtmlTools {
	//those tools only work with the intended Website Template
	
	public static final String CONTENT_SECTION = "<div id=\"CONTENT_SECTION\">";
	public static final String BUTTON_SECTION = "<nav class=\"mbr-navbar__menu-box mbr-navbar__menu-box--inline-right\">";
									//useOldAliasMetadata... ist notwendig da sonst alias im export-file nicht funktioniert
	public static final String META_SECTION = "<head>";
	public static final String TITLE_SECTION = "<span class=\"mbr-brand__name text-white\">";
	public static final String SIDEBAR_SECTION = "<div id=\"SIDEBAR_SECTION\">";
	public static final String BEHIND_SIDEBAR = "<div id=\"BEHIND_SIDEBAR\">";
	private static final String TEMPLATE_HTML = "C:\\Program Files\\Apache Software Foundation\\Tomcat\\webapps\\ROOT\\template.html";
	
	public static void main(String[] args) {
		System.out.println(insertIntoHtmlSection(getBlankHtmlTemplate(), CONTENT_SECTION, "!!!"));
		//for testing
	}
	
	//returns blank/empty website-template-sourcecode (without content)
	public static String getBlankHtmlTemplate() {
		String result = "";
		BufferedReader br = null;
		try {
			String currentLine;
			br = new BufferedReader(new FileReader(TEMPLATE_HTML));

			while ((currentLine = br.readLine()) != null) {
				result = result.concat(currentLine + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		//got index.html sourcecode. now delete its content:
		result = deleteHtmlSection(result, CONTENT_SECTION);
		result = deleteHtmlSection(result, BUTTON_SECTION);
		return result;
	}
	
	//deletes all code inside a specific section
	public static String deleteHtmlSection(String sourceCode, String section) { 
		String typeOpen = section.split(" ", 2)[0];  //for example  "<div"
		String typeClose = typeOpen.replaceAll("<", "</");  //for example "</div"
		
		final int positionBeginning = sourceCode.indexOf(section) + section.length();
		int positionCurrent = positionBeginning;
		int typeCounter = 1;
		while (typeCounter != 0) {	//walk until our main-type was closed (found last "</div")
			if (sourceCode.substring(positionCurrent, positionCurrent + typeOpen.length()).equals(typeOpen)) {
				typeCounter++; //found another "<div"
			} else if (sourceCode.substring(positionCurrent, positionCurrent + typeClose.length()).equals(typeClose)) {
				typeCounter--; //found another "</div"
			}
			positionCurrent++;
		}
		final int positionEnding = --positionCurrent;  //because last pC++ was unnecessary
		
		return sourceCode.substring(0, positionBeginning)
				.concat(sourceCode.substring(positionEnding, sourceCode.length()));
	}
	
	//returns sourceCode with added titles and header-titles (cannot rewrite header-title, it just adds new text to it)
	public static String addTitle(String sourceCode, String title) {
		String result = sourceCode;
		result = HtmlTools.insertIntoHtmlSection(result, HtmlTools.META_SECTION, "<title>" + title + "</title>");
		result = insertIntoHtmlSection(result, HtmlTools.TITLE_SECTION, title);
		return result;
	}
	
	//inserts any String to the end of a specific html section 
	//(section starts with first tag in section. this method adds input to the end of this section right before its closer-tag)
	public static String insertIntoHtmlSection(String sourceCode, String section, String input) {
		//we need to find the closer tag of this section in order to append our input there.
		int skipTags = 0;
		String tag = "";
		int currentPos = sourceCode.indexOf(section);
		if (currentPos == -1) System.out.println("Section not found: " + section);
		char current = sourceCode.charAt(currentPos);
		boolean foundCloserTag = false; //closerTag is like </div
		
		//go until '<' or end of sourceCode
		while (currentPos < sourceCode.length() && current != '<') {
			currentPos++;
			current = sourceCode.charAt(currentPos);
		}
		
		if (current == '<') { 
			//set tag to first element. for example: "<div"
			while (current != ' ' && current != '\n' && current != '>' && current != '/') {
				tag = tag + current;
				current = sourceCode.charAt(++currentPos);
			}
		} else {
			//couldnt find tag
			return sourceCode;
		}
		
		while (!foundCloserTag) {
			current = sourceCode.charAt(++currentPos);
			if (current == '<') {
				boolean foundSameOpenerTag = true; //found another element with same tag-name
				for (int i = 0; foundSameOpenerTag && i < tag.length(); i++) {
					if (sourceCode.charAt(currentPos + i) != tag.charAt(i)) {
						foundSameOpenerTag = false;
					}
				} //really found if its still true
				if (foundSameOpenerTag) {
					skipTags++;  //will have to skip one more closer for this tag. for example </div
				} else { //else check if it is a closer that we are looking at
					if (sourceCode.charAt(currentPos + 1) == '/') {
						foundCloserTag = true;
						for (int i = 1; i < tag.length(); i++) {
							if (sourceCode.charAt(currentPos + 1 + i) != tag.charAt(i)) {
								foundCloserTag = false;
							}
						} //if still true, then we really found one closer tag
						if (foundCloserTag) {
							if (skipTags != 0) {
								skipTags--;
								foundCloserTag = false;
							} //else we really found the closer we were searching for
						}
					}
				}
			}	
		} //found closer tag and currentpos is its position
		
		//insert input into end of section
		sourceCode = sourceCode.substring(0, currentPos)
				+ input 
				+ sourceCode.substring(currentPos, sourceCode.length());
		
		return sourceCode;
	}
	
	//per default no centering
	public static String insertContentHtml(String sourceCode, String title, String content) {
		return insertContentHtml(sourceCode, title, content, false);
	}
	//inserts content (like an article or blogpost)
	public static String insertContentHtml(String sourceCode, String title, String content, boolean center) {
		String result = "<div class=\"mbr-section__col\">\r\n" + 
				"                \r\n";
		if (title != null && !title.isEmpty()) {
			result = result.concat(
				"                <div class=\"mbr-section__container\">\r\n" + 
				"                    <div class=\"mbr-header--center mbr-header--wysiwyg\">\r\n" + 
				"                        <h3 class=\"mbr-header__text\">\r\n" +
				"						 	 <span class=\"underlinespan\">\r\n" +
												title + "\r\n" +
				"							 </span>\r\n" +
				"						 </h3>\r\n" + 
				"                    </div>\r\n" + 
				"                </div>\r\n" );
		}
		if (content != null && !content.isEmpty()) { //only adds horiz-center class if center==true
			result = result.concat(
				"                <div class=\"mbr-section__container\">\r\n" + 
				"                    <div class=\"mbr-article-custom mbr-article--wysiwyg\">\r\n" + 
				(center ? 
				"                        <div class=\"horiz-center\">\r\n" 
				: "") + 
											content + 
				(center ?
				"						 </div>\r\n"  
				: "") +
				"                    </div>\r\n" + 
				"                </div>\r\n" + 
				"                \r\n");
		}
		result = result.concat("         </div>\r\n");
		result = insertIntoHtmlSection(sourceCode, CONTENT_SECTION, result);
		return result;
	}
	
	//inserts a navigation button
	public static String insertNavigationButtonHtml(String sourceCode, String name, String url, String id) {
		String result = "<div class=\"mbr-navbar__column\">\r\n" +
				"	<ul class=\"mbr-navbar__items mbr-navbar__items--right mbr-buttons mbr-buttons--freeze mbr-buttons--right btn-decorator mbr-buttons--active\">\r\n" +
				"		<li class=\"mbr-navbar__item\">\r\n" +
				"			<a class=\"mbr-buttons__link btn nav_btn text-white\" id=\"" + id + "\" href=\"" + url + "\">\r\n" +
				"				" + name + "\r\n" +
				"			</a>\r\n" +
				"		</li>\r\n" +
				"	</ul>\r\n" +
				"</div>";
		result = insertIntoHtmlSection(sourceCode, BUTTON_SECTION, result);
		return result;
	}
	
	//inserts a sidebar button. buttons have no function and have to be handled by javascript.
	public static String insertSidebarButtonHtml(String sourceCode, String id, String text) {
		String button = "<div class=\"sidebar-button\" id=\"" + id + "\">\r\n" + text + "</div>";
		String buttonPlaceholder = "<div class=\"sidebar-button\">\r\n" + text + "</div>";
		String result = insertIntoHtmlSection(sourceCode, SIDEBAR_SECTION, button);
		result = insertIntoHtmlSection(result, BEHIND_SIDEBAR, buttonPlaceholder); 		//have to create one fixed and one non-fixed sidebar so width of fixed sidebar gets respected by other elements 
		return result;
	}
	
	//inserts html code of a table
	public static String insertHtmlTable(String sourceCode, String[] titles, String[][] values) {
		Tabelle tab = new Tabelle(titles, values);
		String result = tab.getHtml("", false);
		result = insertIntoHtmlSection(sourceCode, CONTENT_SECTION, result);
		return result;
	}
	
	//returns the URL from request. including GET query
	/*public static String getWholeURL(HttpServletRequest request) {
		return  request.getScheme() + "://" +
	             request.getServerName() + 
	             ("http".equals(request.getScheme()) && request.getServerPort() == 80 || "https".equals(request.getScheme()) && request.getServerPort() == 443 ? "" : ":" + request.getServerPort() ) +
	             request.getRequestURI() +
	            (request.getQueryString() != null ? "?" + request.getQueryString() : "");
	}*/
	
	public static String getTestTable() {
		 return "<div class=\"mbr-article-custom\" style=\"overflow:auto\"> \r\n"
				+ " <table>\r\n"
		 		+ "  <tr>\r\n"
		 		+ "    <td><input type=\"text\" name=\"field1.1\" placeholder=\"Leer\" value=\"Name1\"/></td>\r\n"
		 		+ "    <td><input type=\"text\" name=\"field1.2\" placeholder=\"Leer\" value=\"Name2\"/></td>\r\n"
		 		+ "    <td><input type=\"text\" name=\"field1.3\" placeholder=\"Leer\" value=\"Name3\"/></td>\r\n"
		 		+ "    <td><input type=\"text\" name=\"field1.4\" placeholder=\"Leer\" value=\"Name4\"/></td>\r\n"
		 		+ "    <td><input type=\"text\" name=\"field1.5\" placeholder=\"Leer\" value=\"Name5\"/></td>\r\n"
		 		+ "    <td><input type=\"text\" name=\"field1.6\" placeholder=\"Leer\" value=\"Name6\"/></td>\r\n"
		 		+ "    <td><input type=\"text\" name=\"field1.7\" placeholder=\"Leer\" value=\"Name7\"/></td>\r\n"
		 		+ "    <td><input type=\"text\" name=\"field1.8\" placeholder=\"Leer\" value=\"Name8\"/></td>\r\n"
		 		+ "    <td><input type=\"text\" name=\"field1.9\" placeholder=\"Leer\" value=\"Name9\"/></td>\r\n"
		 		+ "    <td><input type=\"text\" name=\"field1.99\" placeholder=\"Leer\" value=\"Name10\"/></td>\r\n"
		 		+ "    <td><input type=\"text\" name=\"field1.999\" placeholder=\"Leer\" value=\"Name11\"/></td>\r\n"
		 		+ "    <td><input type=\"text\" name=\"field1.9999\" placeholder=\"Leer\" value=\"Name12\"/></td>\r\n"
		 		+ "  </tr>\r\n"
		 		+ "  <tr>\r\n"
		 		+ "    <td><input type=\"text\" name=\"field2.1\" placeholder=\"Leer\" value=\"Attribut1\"/></td>\r\n"
		 		+ "    <td><input type=\"text\" name=\"field2.2\" placeholder=\"Leer\" value=\"Attribut2\"/></td>\r\n"
		 		+ "    <td><input type=\"text\" name=\"field2.3\" placeholder=\"Leer\" value=\"Attribut3\"/></td>\r\n"
		 		+ "    <td><input type=\"text\" name=\"field2.4\" placeholder=\"Leer\" value=\"Attribut4\"/></td>\r\n"
		 		+ "    <td><input type=\"text\" name=\"field2.5\" placeholder=\"Leer\" value=\"Attribut5\"/></td>\r\n"
		 		+ "    <td><input type=\"text\" name=\"field2.6\" placeholder=\"Leer\" value=\"Attribut6\"/></td>\r\n"
		 		+ "    <td><input type=\"text\" name=\"field2.7\" placeholder=\"Leer\" value=\"Attribut7\"/></td>\r\n"
		 		+ "  </tr>\r\n"
		 		+ "</table> \r\n"
		 		+ "</div> \r\n";
	}
}
