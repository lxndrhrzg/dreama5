package others;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tabelle {
	
	private List<String> titles;
	private List<List<String>> content;
	
	public Tabelle() {
		
	}
	
	public Tabelle(String[] titles, String[][] content) {
		setTitlesArray(titles);
		setContentArray(content);
	}
	
	public Tabelle(List<String> titles, List<List<String>> content) {
		setTitles(titles);
		setContent(content);
	}
	
	public Tabelle(Tabelle toClone) {
		this.titles = new ArrayList<String>();
		this.content = new ArrayList<List<String>>();
		List<String> titles = toClone.getTitles();
		List<List<String>> content = toClone.getContent();
		for (String title : titles) {
			this.titles.add(title);
		}
		for (List<String> row : content) {
			List<String> thisRow = new ArrayList<String>();
			for (String value : row) {
				thisRow.add(value);
			}
			this.content.add(thisRow);
		}
	}
	
	public List<String> getTitles() {
		return titles;
	}
	
	public List<List<String>> getContent() {
		return content;
	}
	
	public void setTitles(List<String> titles) {
		this.titles = titles;
	}
	
	public void setContent( List<List<String>> content) {
		this.content = content;
	}
	
	public String[] getTitlesArray() {
        return titles.toArray(new String[0]);
	}
	
	public String[][] getContentArray() {
		String[][] values = new String[content.size()][];
        for (int i = 0; i < content.size(); i++) {
            List<String> row = content.get(i);
            values[i] = row.toArray(new String[row.size()]);
        }
        return values;
	}
	
	public void setTitlesArray(String[] titles) {
		setTitles(Arrays.asList(titles));
	}
	
	public void setContentArray(String[][] content) {
		List<List<String>> result = new ArrayList<List<String>>();
		List<String[]> temp = Arrays.asList(content);
		for (String[] row : temp) {
			result.add(Arrays.asList(row));
		}
		setContent(result);
	}
	//first content row is 0
	public String getValueFromTitleAtRow(String title, int rowNumber) {
		if (titles == null || content == null) return null;
		int pos = -1;
		for (int i = 0; i < titles.size(); i++) {
			if (titles.get(i).equals(title)) {
				pos = i;
			}
		}
		if (pos == -1) return null;
		List<String> row = content.get(rowNumber);
		return row == null ? null : row.get(pos);
	}
	
	public boolean isEmpty() {	//go through all entries until one is not empty
		if (content != null && content.size() > 0) {
			for (int i = 0; i < content.size(); i++) {
				List<String> row = content.get(i);
				for (int j = 0; j < row.size(); j++) {
					if (row.get(j) != null && !row.get(j).equals("")) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	//gets html-table from current table
	public String getHtml(String tableName, boolean editable) {
		String[] titles = getTitlesArray();
		String[][] content = getContentArray();
		String result = "";
		
		if (!editable) {
			result = "<div class=\"mbr-article-custom\" style=\"overflow:auto\"> \r\n"
				+ " <table name=\"" + tableName + "\" class=\"sortable\">\r\n"
		 		+ "  <tr>\r\n";
			for (int i = 0; i < titles.length; i++) {
				result = result.concat("    <td class=\"tableTitle\"><b>" + titles[i] + "</b></td>\r\n");
			}
			result = result.concat("  </tr>\r\n");
			for (int i = 0; i < content.length; i++) {
				result = result.concat("  <tr>\r\n");
				for (int j = 0; j < content[0].length; j++) {
					final String value = (content[i][j] == null || content[i][j].isEmpty()) ? "<span style=\"opacity: 0.5;\">Leer</span>" : content[i][j];
					result = result.concat("    <td>" + value + "</td>\r\n");
				}
				result = result.concat("  </tr>\r\n");
			}
			result = result.concat("</table> \r\n</div> \r\n");
		} else { //use inputs as cells
			result = "<div class=\"mbr-article-custom window-table\" style=\"overflow:auto\"> \r\n"
					+ " <span name=\"" + tableName + "\">\r\n";
			for (int i = 0; i < content.length; i++) {
				if (i != 0) //empty table-row after every content-block
					result = result.concat("<div>&nbsp</div>\r\n");
				for (int j = 0; j < titles.length; j++) {
					result = result.concat("<div class=\"table-cell\">\r\n" 
							+ "<label for=\"" + titles[j] + "\">" + titles[j] + ": </label>\r\n"
							+ "<input type=\"text\" name=\"" + titles[j]  + "\" value=\"" + (content[i][j] == null ? "" : content[i][j]) + "\" placeholder=\"Leer\" />\r\n"
							+ "</div>\r\n");
				}
			}
			result = result.concat("</span> \r\n</div> \r\n");
		}
		return result;
	}
	
	/*//shows only those columns, that both tables have
	public Tabelle merge(Tabelle tab1) {
		List<String> resTitles = new ArrayList<String>();
		List<List<String>> resContent = new ArrayList<List<String>>();
		
		for (String title : this.getTitles()) {
			if (tab1.getTitles().contains(title)) { //if both tables contain this column
				resTitles.add(title);
			}
		}
		for (List<String> row : this.getContent()) {
			//continue here. this method isnt finished yet
		}

		Tabelle resTab = new Tabelle(resTitles, resContent);
		return resTab;
	}*/
	
}
